#!/usr/bin/env python3
"""
Syntax fixer for OrphanageHub Java files
"""

import os
import re
from pathlib import Path

def fix_java_file(filepath):
    """Fix common syntax errors in Java files"""
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Fix class declaration extending JPanel with parentheses
    content = re.sub(r'extends\s+JPanel\s*KATEX_INLINE_OPEN\s*KATEX_INLINE_CLOSE', 'extends JPanel', content)
    
    # Fix malformed JavaDoc comments
    content = re.sub(r'/\s+\*\*', '/**', content)
    content = re.sub(r'\*\s+/', '*/', content)
    
    # Fix lambda expressions with misplaced parentheses
    content = re.sub(r'e\s*->\s*KATEX_INLINE_CLOSE\s*{', 'e -> {', content)
    
    # Fix logger calls with missing closing parentheses
    content = re.sub(r'(logger\.\w+\s*KATEX_INLINE_OPEN[^;)]*?)"([^"]*?)"\s*;', r'\1"\2");', content)
    content = re.sub(r'(Logger\.\w+\s*KATEX_INLINE_OPEN[^;)]*?)"([^"]*?)"\s*;', r'\1"\2");', content)
    
    # Fix method calls with syntax errors
    content = re.sub(r'\.getLayoutComponent\s*KATEX_INLINE_OPEN[^)]+KATEX_INLINE_CLOSE\s*;', '', content)
    
    # Fix println/print statements
    content = re.sub(r'(System\.(out|err)\.println?\s*KATEX_INLINE_OPEN[^;)]*?)\s*;', r'\1);', content)
    
    # Fix getText() calls
    content = re.sub(r'\.getText\s*KATEX_INLINE_OPEN\s*KATEX_INLINE_CLOSE\s*;', '.getText());', content)
    
    # Fix setSelectedItem calls
    content = re.sub(r'\.setSelectedItem\s*KATEX_INLINE_OPEN[^)]+KATEX_INLINE_CLOSE\s*;', '.setSelectedItem(status);', content)
    
    # Fix misplaced semicolons after closing braces in lambdas
    content = re.sub(r'}\s*;\s*KATEX_INLINE_CLOSE', '})', content)
    
    # Fix missing closing parentheses in method calls
    lines = content.split('\n')
    fixed_lines = []
    
    for i, line in enumerate(lines):
        # Count parentheses
        open_count = line.count('(')
        close_count = line.count(')')
        
        # Skip lines that are likely okay
        if open_count == close_count:
            fixed_lines.append(line)
            continue
            
        # Try to fix common patterns
        if 'Logger.' in line or 'logger.' in line:
            if not line.strip().endswith(');') and not line.strip().endswith(','):
                if line.strip().endswith(';'):
                    line = line[:-1] + ');'
                elif '"' in line:
                    line = line.rstrip() + ');'
        
        fixed_lines.append(line)
    
    content = '\n'.join(fixed_lines)
    
    # Only write if changes were made
    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    """Main function to fix all Java files"""
    
    src_dir = Path('src/main/java')
    
    if not src_dir.exists():
        print("Error: src/main/java directory not found!")
        return
    
    fixed_count = 0
    
    for java_file in src_dir.rglob('*.java'):
        if fix_java_file(java_file):
            print(f"Fixed: {java_file}")
            fixed_count += 1
    
    print(f"\nFixed {fixed_count} files")

if __name__ == '__main__':
    main()