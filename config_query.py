#!/usr/bin/env python3
"""
config_query.py - Smart Configuration Query System for OrphanageHub
Functional, fast, and flexible with zero nested conditionals
"""

import os
import sys
import subprocess
import datetime
import argparse
from pathlib import Path, PurePosixPath
from typing import Optional, List, Generator, Tuple, NamedTuple
from functools import partial, lru_cache
from dataclasses import dataclass
from enum import Enum

# ================== Type System ==================
class Mode(Enum):
    CURRENT = "current"
    GIT = "git"
    BOTH = "both"

@dataclass(frozen=True)
class FileData:
    path: str
    content: str
    source: str
    size: int
    language: str

# ================== Pure Functions ==================
def safe_run(cmd: List[str], cwd: Path = Path('.')) -> Tuple[int, bytes, bytes]:
    """Execute command safely"""
    try:
        proc = subprocess.run(cmd, cwd=str(cwd), capture_output=True, timeout=10)
        return proc.returncode, proc.stdout, proc.stderr
    except Exception:
        return 127, b'', b'Command not found'

def decode_safe(data: bytes) -> str:
    """Decode bytes safely with fallbacks"""
    for encoding in ('utf-8', 'latin-1'):
        try:
            return data.decode(encoding)
        except:
            continue
    return data.decode('utf-8', errors='replace')

@lru_cache(maxsize=128)
def get_language(filepath: str) -> str:
    """Get language from file extension"""
    mapping = {
        '.java': 'java', '.py': 'python', '.xml': 'xml',
        '.properties': 'properties', '.md': 'markdown',
        '.txt': 'text', '.sh': 'bash', '.sql': 'sql'
    }
    return mapping.get(Path(filepath).suffix.lower(), '')

def path_matches(path_str: str, keyword: str) -> bool:
    """Check if path contains keyword in any segment"""
    parts = PurePosixPath(path_str).parts
    keyword_lower = keyword.lower()
    return any(keyword_lower in part.lower() for part in parts)

def read_file(path: Path) -> Optional[str]:
    """Read file content safely"""
    try:
        return path.read_text(encoding='utf-8', errors='ignore')
    except:
        return None

# ================== Git Operations ==================
@lru_cache(maxsize=32)
def get_commits(repo: Path, limit: int = 10, all_commits: bool = False) -> Tuple[str, ...]:
    """Get commit hashes (cached)"""
    args = ['git', 'rev-list']
    args.append('--all' if all_commits else 'HEAD')
    if limit and not all_commits:
        args.extend(['-n', str(limit)])
    
    rc, out, _ = safe_run(args, repo)
    return tuple(decode_safe(out).splitlines() if rc == 0 else [])

def get_commit_files(repo: Path, commit: str) -> Generator[str, None, None]:
    """Get files in a commit"""
    rc, out, _ = safe_run(['git', 'ls-tree', '-r', '--name-only', commit], repo)
    if rc == 0:
        yield from decode_safe(out).splitlines()

def get_file_from_commit(repo: Path, commit: str, filepath: str) -> Optional[str]:
    """Get file content from commit"""
    rc, out, _ = safe_run(['git', 'show', f'{commit}:{filepath}'], repo)
    if rc != 0 or b'\x00' in out:  # Skip binary files
        return None
    return decode_safe(out)

# ================== Current Files Operations ==================
def scan_current_files(base: Path, keyword: Optional[str]) -> Generator[FileData, None, None]:
    """Scan current working directory for matching files"""
    src_path = base / "src"
    if not src_path.exists():
        return
    
    # Combine patterns efficiently
    patterns = ['**/*.java', '**/*.xml', '**/*.properties', '**/*.md', '**/*.txt']
    all_files = (p for pattern in patterns for p in src_path.glob(pattern) if p.is_file())
    
    for filepath in all_files:
        rel_path = filepath.relative_to(base).as_posix()
        
        # Skip if keyword doesn't match
        if keyword and not path_matches(rel_path, keyword):
            continue
        
        content = read_file(filepath)
        if content:
            yield FileData(
                path=rel_path,
                content=content,
                source='current',
                size=filepath.stat().st_size,
                language=get_language(str(filepath))
            )

# ================== Git Files Operations ==================
def scan_git_files(repo: Path, keyword: Optional[str], limit: int = 5) -> Generator[FileData, None, None]:
    """Scan git history for matching files"""
    commits = get_commits(repo, limit)
    
    for commit in commits:
        short = commit[:8]
        for filepath in get_commit_files(repo, commit):
            # Skip if keyword doesn't match
            if keyword and not path_matches(filepath, keyword):
                continue
            
            content = get_file_from_commit(repo, commit, filepath)
            if content:
                yield FileData(
                    path=filepath,
                    content=content,
                    source=f'git:{short}',
                    size=len(content),
                    language=get_language(filepath)
                )

# ================== Formatting ==================
def format_section(keyword: str, files: List[FileData], brief: bool = False, truncate_kb: int = 0) -> str:
    """Format files into a section"""
    lines = [
        "=" * 80,
        f"## {keyword.upper() if keyword else 'ALL'} FILES",
        f"Found: {len(files)} files",
        "=" * 80,
        ""
    ]
    
    if brief:
        # Just list files
        for f in files:
            lines.append(f"- {f.path} [{f.source}] ({f.size} bytes)")
    else:
        # Full content
        for f in files:
            content = f.content
            if truncate_kb > 0:
                max_chars = truncate_kb * 1024
                if len(content) > max_chars:
                    content = content[:max_chars] + f"\n*(truncated to {truncate_kb}KB)*"
            
            lines.extend([
                "-" * 60,
                f"File: {f.path}",
                f"Source: {f.source} | Size: {f.size} bytes",
                f"```{f.language}",
                content,
                "```",
                ""
            ])
    
    return "\n".join(lines)

def format_header(mode: Mode, keyword: Optional[str]) -> str:
    """Generate header"""
    return "\n".join([
        "# 🔍 ORPHANAGEHUB CONFIGURATION QUERY",
        f"Generated: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
        f"Mode: {mode.value}",
        f"Keyword: {keyword or 'ALL'}",
        ""
    ])

# ================== Main Query Logic ==================
def query_files(repo: Path, mode: Mode, keyword: Optional[str], git_limit: int = 5) -> List[FileData]:
    """Main query function"""
    files = []
    
    # Collect based on mode
    if mode in (Mode.CURRENT, Mode.BOTH):
        files.extend(scan_current_files(repo, keyword))
    
    if mode in (Mode.GIT, Mode.BOTH):
        files.extend(scan_git_files(repo, keyword, git_limit))
    
    # Remove duplicates (keep first occurrence)
    seen = set()
    unique = []
    for f in files:
        key = (f.path, f.source)
        if key not in seen:
            seen.add(key)
            unique.append(f)
    
    return unique

# ================== CLI ==================
def parse_args():
    parser = argparse.ArgumentParser(
        description="Query project files by keyword",
        epilog="""
Examples:
  %(prog)s util                  # Query 'util' files from current
  %(prog)s gui --git             # Query 'gui' files from git history  
  %(prog)s dao --both            # Query 'dao' from both current and git
  %(prog)s --both -o full.txt    # Query all files from both sources
  %(prog)s model --brief         # Show only file names for 'model'
        """
    )
    
    parser.add_argument('keyword', nargs='?', help='Directory keyword (e.g., util, gui, dao)')
    parser.add_argument('--current', action='store_true', help='Search current files only')
    parser.add_argument('--git', action='store_true', help='Search git history only')
    parser.add_argument('--both', action='store_true', help='Search both current and git')
    parser.add_argument('-o', '--output', help='Output file (default: stdout)')
    parser.add_argument('-b', '--brief', action='store_true', help='Brief output (names only)')
    parser.add_argument('--limit', type=int, default=5, help='Git commits to check (default: 5)')
    parser.add_argument('--truncate-kb', type=int, default=0, help='Truncate files to N KB')
    
    return parser.parse_args()

def main():
    args = parse_args()
    
    # Determine mode
    if args.git:
        mode = Mode.GIT
    elif args.both:
        mode = Mode.BOTH
    else:
        mode = Mode.CURRENT  # Default
    
    # Find repo root
    repo = Path.cwd()
    
    # Query files
    files = query_files(repo, mode, args.keyword, args.limit)
    
    # Format output
    output = format_header(mode, args.keyword)
    
    if not files:
        output += "\nNo matching files found.\n"
    else:
        output += format_section(args.keyword, files, args.brief, args.truncate_kb)
        
        # Add statistics
        total_size = sum(f.size for f in files)
        languages = set(f.language for f in files if f.language)
        output += f"\n{'=' * 80}\n"
        output += f"Total: {len(files)} files | {total_size:,} bytes\n"
        output += f"Languages: {', '.join(sorted(languages))}\n"
    
    # Output
    if args.output:
        Path(args.output).write_text(output, encoding='utf-8')
        print(f"✅ Written to {args.output} ({len(output):,} bytes)")
    else:
        print(output)

if __name__ == "__main__":
    main()