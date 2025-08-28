#!/usr/bin/env python3
"""
parse_errors.py - Hybrid Maven compilation error parser with smart grouping, quick fixes, and source file content display.
Enhanced with associated classes extraction, error line context, and auto project root detection.
"""

import re
import sys
import json
import argparse
from pathlib import Path
from collections import defaultdict, Counter
from datetime import datetime

class ErrorParser:
    def __init__(self, log_file='compile_errors.log', project_root=None, context_lines=3):
        self.log_file = Path(log_file)
        if not self.log_file.exists():
            raise FileNotFoundError(f"Log file not found: {self.log_file}")
        self.project_root = self._detect_project_root() if not project_root else Path(project_root)
        self.context_lines = context_lines  # Lines of context around errors
        self.errors_by_file = defaultdict(list)
        self.errors_by_type = defaultdict(list)
        self.error_counts = Counter()
        self.quick_fixes = set()
        self.other_errors = []  # For non-matching [ERROR] lines
        self.associated_classes = defaultdict(dict)  # Error key -> {'main': str, 'subsidiary': list[str]}
        self.file_contents_cache = {}  # Cache for file contents
        self.error_patterns = [
            # Main: [ERROR] /path/File.java:[line,col] message
            re.compile(r'\[ERROR\] (.+?\.java):\[(\d+),(\d+)\] (.+)'),
            # Follow-up: [ERROR] symbol/location/required/found/reason: details
            re.compile(r'\[ERROR\] (symbol|location|required|found|reason): (.+)'),
            # Compilation error header
            re.compile(r'\[ERROR\] COMPILATION ERROR :'),
            # Build failure
            re.compile(r'\[ERROR\] Failed to execute goal (.+)'),
            # Empty [ERROR]
            re.compile(r'\[ERROR\]\s*$'),
            # Help references
            re.compile(r'\[ERROR\] \[Help \d+\]'),
            # Fallback: Any other [ERROR]
            re.compile(r'\[ERROR\] (.+)'),
        ]
        
    def _detect_project_root(self):
        """Auto-detect project root from log paths."""
        with self.log_file.open('r', encoding='utf-8') as f:
            for line in f:
                # Look for full paths in error messages
                match = re.search(r'(/[^:]+/OrphanageHub[^/]*)', line)
                if match:
                    path = Path(match.group(1))
                    if path.exists():
                        return path
        return Path.cwd()  # Fallback to current dir
        
    def parse(self):
        current_error = None
        current_file = None
        current_line = None
        current_col = None
        current_key = None
        seen_errors = set()  # To deduplicate
        
        with self.log_file.open('r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line.startswith('[ERROR]'):
                    continue
                    
                matched = False
                for i, pattern in enumerate(self.error_patterns):
                    match = pattern.match(line)
                    if match:
                        if i == 0:  # Main error with file:line:col
                            file_path, line_num, col_num, msg = match.groups()
                            # Clean the file path
                            file_path_clean = re.sub(r'^.*/(src/main/java/.+)$', r'\1', file_path)
                            error_entry = f"Line {line_num}, Col {col_num}: {msg}"
                            error_key = f"{file_path_clean}:{line_num}:{col_num}:{msg}"
                            
                            if error_key not in seen_errors:
                                seen_errors.add(error_key)
                                
                                self.errors_by_file[file_path_clean].append(error_entry)
                                error_type = self._categorize_error(msg)
                                self.errors_by_type[error_type].append(f"{file_path_clean}:{line_num}")
                                self.error_counts[error_type] += 1
                                self._detect_quick_fix(msg)
                                
                                current_error = error_entry
                                current_file = file_path_clean
                                current_line = line_num
                                current_col = col_num
                                current_key = error_key
                                
                                # Extract main class from file path
                                main_class = file_path_clean.replace('src/main/java/', '').replace('.java', '').replace('/', '.')
                                self.associated_classes[current_key] = {'main': main_class, 'subsidiary': []}
                            
                        elif i == 1 and current_error:  # Follow-up details
                            detail_type, detail = match.groups()
                            full_detail = f" [{detail_type}: {detail}]"
                            self.errors_by_file[current_file][-1] += full_detail
                            
                            # Extract subsidiary class if location
                            if detail_type == 'location' and 'class ' in detail:
                                sub_class = re.search(r'class (\S+)', detail)
                                if sub_class:
                                    self.associated_classes[current_key]['subsidiary'].append(sub_class.group(1))
                                    
                        elif i in [2, 3, 4, 5]:  # Known non-error lines
                            pass  # Skip these silently
                            
                        else:  # Fallback for other [ERROR] lines
                            other_msg = match.group(1) if match.groups() else line
                            if other_msg and other_msg not in self.other_errors:
                                # Filter out common non-error messages
                                skip_patterns = [
                                    r'^-+$',
                                    r'^To see the full stack trace',
                                    r'^Re-run Maven',
                                    r'^For more information',
                                    r'^\[Help \d+\]',
                                    r'^->',
                                ]
                                if not any(re.search(p, other_msg) for p in skip_patterns):
                                    self.other_errors.append(other_msg)
                        matched = True
                        break
                        
                if not matched and line != '[ERROR]':
                    # Only warn for truly unmatched non-empty lines
                    print(f"Warning: Unmatched line: {line}", file=sys.stderr)
    
    def _categorize_error(self, msg):
        msg_lower = msg.lower()
        if 'cannot find symbol' in msg_lower:
            if 'lightgray' in msg_lower:
                return 'Color Constant Error'
            elif 'variable' in msg_lower:
                return 'Missing Variable'
            elif 'method' in msg_lower:
                return 'Missing Method'
            elif 'class' in msg_lower:
                return 'Missing Class'
            return 'Symbol Not Found'
        elif 'unreported exception' in msg_lower:
            return 'Uncaught Exception'
        elif 'exception' in msg_lower and 'must be caught' in msg_lower:
            return 'Uncaught Exception'
        elif 'static context' in msg_lower:
            return 'Static Context Error'
        elif 'constructor' in msg_lower:
            return 'Constructor Mismatch'
        elif 'incompatible types' in msg_lower:
            return 'Type Mismatch'
        elif 'package' in msg_lower and 'does not exist' in msg_lower:
            return 'Missing Package'
        return 'Other Compilation Error'
    
    def _detect_quick_fix(self, msg):
        msg_lower = msg.lower()
        if 'lightgray' in msg_lower:
            self.quick_fixes.add("Replace 'LIGHTGRAY' with 'Color.LIGHT_GRAY' (ensure 'import java.awt.Color;')")
        if 'mainapp' in msg_lower and 'cannot find symbol' in msg_lower:
            self.quick_fixes.add("Add 'private OrphanageHubApp mainApp;' field and pass via constructor")
        if 'constructor user' in msg_lower:
            self.quick_fixes.add("Add no-arg constructor to User.java: public User() {} or fix args")
        if 'static context' in msg_lower:
            self.quick_fixes.add("Use 'this.method()' for instance methods or make method static")
        if 'variable e' in msg_lower:
            self.quick_fixes.add("In catch block, add 'e': catch (Exception e) { ... }")
        if 'sqlexception' in msg_lower and 'must be caught' in msg_lower:
            self.quick_fixes.add("Wrap SQL operations in try-catch:\n    try {\n        // SQL code\n    } catch (SQLException e) {\n        e.printStackTrace();\n    }\n    OR add 'throws SQLException' to method signature (ensure 'import java.sql.SQLException;')")
        if 'unreported exception' in msg_lower:
            exception_type = re.search(r'exception (\S+);', msg)
            if exception_type:
                ex_name = exception_type.group(1).split('.')[-1]
                self.quick_fixes.add(f"Handle {ex_name}: try-catch block or 'throws {ex_name}' in method signature (ensure import for {ex_name})")
            
    def _get_file_content(self, file_path):
        """Read file content."""
        if file_path in self.file_contents_cache:
            return self.file_contents_cache[file_path]
        
        # Try multiple path combinations
        possible_paths = [
            self.project_root / file_path,
            self.project_root / file_path.replace('src/main/java/', ''),
            Path(file_path),
        ]
        
        for full_path in possible_paths:
            if full_path.exists():
                try:
                    with full_path.open('r', encoding='utf-8') as f:
                        content = f.read()
                        self.file_contents_cache[file_path] = content
                        return content
                except Exception as e:
                    return f"[Error reading file: {e}]"
        
        return f"[File not found: {file_path}]"
    
    def _extract_class_name(self, file_path):
        """Extract class name from file path."""
        class_name = file_path.replace('.java', '').split('/')[-1]
        return class_name
            
    def generate_report(self, output_file='grouped_errors.txt', filter_str=None, verbose=False, 
                       show_source=True, max_source_lines=None, context_lines=None):
        self.parse()
        if context_lines is not None:
            self.context_lines = context_lines
            
        with Path(output_file).open('w', encoding='utf-8') as out:
            out.write("=" * 70 + "\n")
            out.write(f"COMPILATION ERROR REPORT\n")
            out.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            out.write(f"Project Root: {self.project_root}\n")
            out.write("=" * 70 + "\n\n")
            
            total_errors = sum(len(errs) for errs in self.errors_by_file.values())
            out.write(f"Total Syntax Errors: {total_errors}\n")
            out.write(f"Files Affected: {len(self.errors_by_file)}\n")
            out.write(f"Error Types: {len(self.error_counts)}\n\n")
            
            if self.quick_fixes:
                out.write("🔧 QUICK FIXES DETECTED:\n")
                out.write("-" * 40 + "\n")
                for i, fix in enumerate(sorted(self.quick_fixes), 1):
                    out.write(f"  {i}. {fix}\n")
                out.write("\n")
            
            out.write("📁 ERRORS BY FILE (sorted by count):\n")
            out.write("=" * 70 + "\n")
            files_with_errors = []
            for file, errs in sorted(self.errors_by_file.items(), key=lambda x: len(x[1]), reverse=True):
                if filter_str and filter_str.lower() not in file.lower():
                    continue
                files_with_errors.append(file)
                out.write(f"\n{file} ({len(errs)} error{'s' if len(errs) > 1 else ''}):\n")
                
                # Sort by line number
                def extract_line_num(err):
                    match = re.search(r'Line (\d+)', err)
                    return int(match.group(1)) if match else 0
                    
                sorted_errs = sorted(errs, key=extract_line_num)
                for err in sorted_errs:
                    out.write(f"  ❌ {err}\n")
                    
                    # Find associated classes
                    for key, assoc in self.associated_classes.items():
                        if key.startswith(file):
                            out.write(f"     └─ Class: {assoc['main']}")
                            if assoc['subsidiary']:
                                out.write(f" (uses: {', '.join(set(assoc['subsidiary']))})")
                            out.write("\n")
                            break
            
            out.write("\n" + "=" * 70 + "\n")
            out.write("📊 ERROR TYPE SUMMARY (sorted by frequency):\n")
            out.write("-" * 40 + "\n")
            for err_type, count in self.error_counts.most_common():
                out.write(f"  {err_type}: {count} occurrence{'s' if count > 1 else ''}\n")
                affected = len(set(f.split(':')[0] for f in self.errors_by_type[err_type]))
                out.write(f"    → Affects {affected} file{'s' if affected > 1 else ''}\n")
            
            if self.other_errors:
                out.write("\n⚠️ OTHER ERRORS (build/config):\n")
                out.write("-" * 40 + "\n")
                for oe in self.other_errors[:10]:  # Limit to first 10
                    out.write(f"  • {oe}\n")
                if len(self.other_errors) > 10:
                    out.write(f"  ... and {len(self.other_errors) - 10} more\n")
            
            # Source code section with context
            if show_source and files_with_errors:
                out.write("\n" + "=" * 70 + "\n")
                out.write("📝 SOURCE CODE OF FILES WITH ERRORS:\n")
                out.write("=" * 70 + "\n")
                
                for file_path in files_with_errors:
                    class_name = self._extract_class_name(file_path)
                    out.write(f"\n╔══ {class_name}.java ══╗\n")
                    out.write(f"Path: {file_path}\n")
                    out.write("-" * 70 + "\n")
                    
                    # Get error line numbers
                    error_lines = {}
                    for err in self.errors_by_file[file_path]:
                        match = re.search(r'Line (\d+), Col (\d+): (.+?)(\[|$)', err)
                        if match:
                            line_num = int(match.group(1))
                            col_num = int(match.group(2))
                            msg = match.group(3).strip()
                            error_lines[line_num] = {'col': col_num, 'msg': msg}
                    
                    # Get file content
                    content = self._get_file_content(file_path)
                    if content.startswith('['):  # Error message
                        out.write(content + "\n")
                    else:
                        lines = content.splitlines()
                        total_lines = len(lines)
                        
                        # Show context around each error
                        displayed = set()
                        for err_line in sorted(error_lines.keys()):
                            if err_line in displayed:
                                continue
                                
                            start = max(1, err_line - self.context_lines)
                            end = min(total_lines, err_line + self.context_lines)
                            
                            # Check for overlap with previous context
                            if displayed and min(displayed) <= end and max(displayed) >= start:
                                continue
                                
                            out.write(f"\n📍 Error at line {err_line}: {error_lines[err_line]['msg']}\n")
                            out.write("─" * 50 + "\n")
                            
                            for i in range(start, min(end + 1, len(lines) + 1)):
                                if i > len(lines):
                                    break
                                line_content = lines[i-1]
                                if i == err_line:
                                    out.write(f">>> {i:4d}: {line_content}\n")
                                    # Show column indicator
                                    col = error_lines[err_line]['col']
                                    out.write(f"         " + " " * (col - 1) + "^ Error here\n")
                                else:
                                    out.write(f"    {i:4d}: {line_content}\n")
                            
                            displayed.update(range(start, end + 1))
                        
                        # If we want to show the full file after context
                        if max_source_lines and max_source_lines > 0:
                            out.write("\n" + "─" * 50 + "\n")
                            out.write("Full file (limited to first {} lines):\n".format(max_source_lines))
                            out.write("─" * 50 + "\n")
                            for i, line in enumerate(lines[:max_source_lines], 1):
                                marker = ">>>" if i in error_lines else "   "
                                out.write(f"{marker} {i:4d}: {line}\n")
                    
                    out.write("╚" + "═" * 68 + "╝\n")
            
            if verbose:
                out.write("\n" + "=" * 70 + "\n")
                out.write("🔍 RAW LOG DUMP:\n")
                out.write("=" * 70 + "\n")
                out.write(self.log_file.read_text())
                
    def export_json(self, json_file='errors.json'):
        self.parse()
        data = {
            'summary': {
                'total_errors': sum(len(errs) for errs in self.errors_by_file.values()),
                'files_affected': len(self.errors_by_file),
                'error_types': len(self.error_counts),
                'other_errors': len(self.other_errors),
                'project_root': str(self.project_root),
                'timestamp': datetime.now().isoformat()
            },
            'quick_fixes': list(self.quick_fixes),
            'errors_by_file': dict(self.errors_by_file),
            'errors_by_type': dict(self.errors_by_type),
            'error_counts': dict(self.error_counts),
            'associated_classes': dict(self.associated_classes),
            'other_errors': self.other_errors
        }
        with Path(json_file).open('w', encoding='utf-8') as jf:
            json.dump(data, jf, indent=2)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Parse Maven compilation errors with smart categorization, quick fixes, and source context display.')
    parser.add_argument('--log', default='compile_errors.log', help='Input log file (default: compile_errors.log)')
    parser.add_argument('--out', default='grouped_errors.txt', help='Output text report (default: grouped_errors.txt)')
    parser.add_argument('--json', help='Export JSON file (optional)')
    parser.add_argument('--filter', help='Filter files by substring (e.g., "dao")')
    parser.add_argument('--root', help='Project root directory (auto-detected if omitted)')
    parser.add_argument('--no-source', action='store_true', help='Disable source code display')
    parser.add_argument('--max-lines', type=int, help='Max lines for full file display (after context)')
    parser.add_argument('--context-lines', type=int, default=3, help='Lines of context around each error (default: 3)')
    parser.add_argument('-q', '--quiet', action='store_true', help='Suppress console output')
    parser.add_argument('-v', '--verbose', action='store_true', help='Include raw log in report')
    args = parser.parse_args()
    
    try:
        ep = ErrorParser(args.log, args.root, args.context_lines)
        ep.generate_report(
            args.out, 
            args.filter, 
            args.verbose,
            show_source=not args.no_source,
            max_source_lines=args.max_lines,
            context_lines=args.context_lines
        )
        if args.json:
            ep.export_json(args.json)
        if not args.quiet:
            total = sum(len(errs) for errs in ep.errors_by_file.values())
            if total > 0:
                print(f"✅ Parsed {total} error{'s' if total != 1 else ''} from {len(ep.errors_by_file)} file{'s' if len(ep.errors_by_file) != 1 else ''}")
                if ep.other_errors:
                    print(f"   + {len(ep.other_errors)} build/config issue{'s' if len(ep.other_errors) != 1 else ''}")
            else:
                print("✨ No compilation errors found!")
            print(f"📄 Report: {args.out}")
            if args.json:
                print(f"📊 JSON: {args.json}")
            if ep.quick_fixes:
                print(f"💡 {len(ep.quick_fixes)} quick fix{'es' if len(ep.quick_fixes) != 1 else ''} detected")
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        sys.exit(1)