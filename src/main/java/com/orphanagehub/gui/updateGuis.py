#!/usr/bin/env python3
"""
updateGuis.py

Scan a GUI folder and produce a nicely-sectioned `all_gui_code.txt` similar to the example you showed.
Fixed syntax error (incorrect f-string usage) and some robustness improvements.

Usage:
    python3 updateGuis.py                # defaults to current directory
    python3 updateGuis.py /path/to/dir   # specify folder
    python3 updateGuis.py -o output.txt  # change output filename
"""

from __future__ import annotations
import os
import sys
import datetime
import argparse
from typing import List

# -------- CONFIG --------
DEFAULT_INPUT_DIR = "."
DEFAULT_OUTPUT_FILE = "all_gui_code.txt"
READABLE_EXT = {'.java', '.txt', '.bak', '.md', '.properties', '.xml', '.gradle', '.json'}
BINARY_EXT = {'.png', '.jpg', '.jpeg', '.gif', '.ico', '.class', '.bmp', '.svg'}
MAX_LINES_PER_FILE = 1000  # set to None to disable truncation
# ------------------------

def safe_read_text_file(path: str, max_lines=MAX_LINES_PER_FILE) -> str:
    """Read a text file safely. Truncate after max_lines if configured."""
    try:
        with open(path, 'r', encoding='utf-8', errors='replace') as f:
            if max_lines is None:
                return f.read()
            lines = []
            for i, line in enumerate(f):
                if i >= max_lines:
                    lines.append(f"... (file truncated after {max_lines} lines)")
                    break
                lines.append(line.rstrip('\n'))
            return '\n'.join(lines)
    except Exception as e:
        return f"[Error reading file: {e}]"

def collect_files(root: str) -> List[str]:
    files = []
    for dirpath, dirnames, filenames in os.walk(root):
        # ignore common build/git dirs
        dirnames[:] = [d for d in dirnames if d not in {'.git', 'target', 'build', '__pycache__'}]
        for fn in filenames:
            files.append(os.path.join(dirpath, fn))
    return sorted(files)

def short_tree(root: str, max_depth: int = 2) -> str:
    out = []
    root = os.path.abspath(root)
    for dirpath, dirnames, filenames in os.walk(root):
        rel = os.path.relpath(dirpath, root)
        if rel == '.':
            depth = 0
        else:
            depth = rel.count(os.sep) + 1
        if depth > max_depth:
            dirnames[:] = []
            continue
        indent = "  " * depth
        out.append(f"{indent}{os.path.basename(dirpath)}/")
        for f in sorted(filenames)[:50]:
            out.append(f"{indent}  {f}")
    return '\n'.join(out)

def section_sep() -> str:
    return "\n" + "=" * 80 + "\n"

def write_output(out_lines: List[str], out_file: str) -> None:
    with open(out_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(out_lines))
    size_kb = os.path.getsize(out_file) / 1024
    print(f"✅ Written {out_file} ({size_kb:.2f} KB)")

def make_document(input_dir: str) -> List[str]:
    lines: List[str] = []
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    lines.append("# all_gui_code.txt")
    lines.append("")
    lines.append(f"Generated: {now} (local)")
    lines.append(f"Source directory: {os.path.abspath(input_dir)}")
    lines.append("")
    lines.append("=" * 80)
    lines.append("## 1. DIRECTORY SNAPSHOT")
    lines.append("=" * 80)
    lines.append("")

    try:
        dir_listing = sorted(os.listdir(input_dir))
        lines.append("```")
        for item in dir_listing:
            lines.append(item)
        lines.append("```")
    except Exception as e:
        lines.append(f"[Could not read directory listing: {e}]")

    lines.append("")
    lines.append("=" * 80)
    lines.append("## 2. PROJECT OVERVIEW")
    lines.append("=" * 80)
    lines.append("")
    lines.append("This file groups GUI source files, backup copies, and resources for easy review.")
    lines.append("Plaintext files (.java, .txt, .md, .properties, etc.) are embedded below (truncated if large).")
    lines.append("Binary files (images, .class) are listed with size and a note.")
    lines.append("")

    files = collect_files(input_dir)
    java_files = [f for f in files if f.lower().endswith('.java')]
    bak_files = [f for f in files if f.lower().endswith('.bak') or f.endswith('.bak')]
    images = [f for f in files if os.path.splitext(f)[1].lower() in BINARY_EXT]
    other_text = [f for f in files if os.path.splitext(f)[1].lower() in READABLE_EXT and not f.lower().endswith('.java')]

    lines.append("=" * 80)
    lines.append("## 3. FILE GROUPS")
    lines.append("=" * 80)
    lines.append("")
    lines.append("### Java source files")
    lines.append("```")
    for f in java_files:
        lines.append(f)
    lines.append("```")
    lines.append("")
    lines.append("### Backup files (.bak)")
    lines.append("```")
    for f in bak_files:
        lines.append(f)
    lines.append("```")
    lines.append("")
    lines.append("### Image / Binary resources")
    lines.append("```")
    for f in images:
        lines.append(f)
    lines.append("```")
    lines.append("")
    lines.append("### Other readable files")
    lines.append("```")
    for f in other_text:
        lines.append(f)
    lines.append("```")

    lines.append("")
    lines.append("=" * 80)
    lines.append("## 4. DIRECTORY TREE (short)")
    lines.append("=" * 80)
    lines.append("")
    lines.append("```")
    lines.append(short_tree(input_dir, max_depth=2))
    lines.append("```")

    lines.append("")
    lines.append("=" * 80)
    lines.append("## 5. DETAILED SECTIONS (FILES)")
    lines.append("=" * 80)

    def add_file_section(path: str) -> None:
        rel = os.path.relpath(path, start=input_dir)
        ext = os.path.splitext(path)[1].lower()
        lines.append("-" * 60)
        lines.append(f"### File: {rel}")
        if ext in READABLE_EXT or ext == '.java' or ext == '.md':
            content = safe_read_text_file(path)
            fence_lang = "java" if ext == '.java' else "text"
            lines.append("```" + fence_lang)
            lines.append(content)
            lines.append("```")
        elif ext in BINARY_EXT:
            try:
                size = os.path.getsize(path)
                lines.append(f"[Binary file: {os.path.basename(path)}] ({size} bytes) — not embeddable as text. Keep in resources.")
            except:
                lines.append("[Binary file] — not embeddable as text.")
        else:
            content = safe_read_text_file(path)
            lines.append("```text")
            lines.append(content)
            lines.append("```")

    for jf in java_files:
        add_file_section(jf)
    for bf in bak_files:
        add_file_section(bf)
    for of in other_text:
        if of not in java_files and of not in bak_files:
            add_file_section(of)
    for im in images:
        rel = os.path.relpath(im, start=input_dir)
        lines.append("-" * 60)
        lines.append(f"### Resource: {rel}")
        try:
            size = os.path.getsize(im)
            lines.append(f"[Binary image file ({size} bytes) — not shown inline]")
        except:
            lines.append("[Binary image file — not shown inline]")

    lines.append("")
    lines.append("=" * 80)
    lines.append("## 6. SUMMARY / STATS")
    lines.append("=" * 80)
    lines.append("")
    lines.append(f"- Total files scanned: {len(files)}")
    lines.append(f"- Java files: {len(java_files)}")
    lines.append(f"- Backup (.bak) files: {len(bak_files)}")
    lines.append(f"- Images / binary: {len(images)}")
    lines.append("")
    lines.append("Generated by updateGuis.py")
    lines.append("")

    return lines

def main():
    parser = argparse.ArgumentParser(description="Generate all_gui_code.txt from a GUI folder")
    parser.add_argument("input_dir", nargs='?', default=DEFAULT_INPUT_DIR,
                        help="Path to GUI folder (default: current directory)")
    parser.add_argument("-o", "--output", default=DEFAULT_OUTPUT_FILE,
                        help=f"Output filename (default: {DEFAULT_OUTPUT_FILE})")
    args = parser.parse_args()

    input_dir = args.input_dir
    out_file = args.output

    if not os.path.exists(input_dir) or not os.path.isdir(input_dir):
        print(f"ERROR: input directory does not exist or is not a directory: {input_dir}")
        sys.exit(1)

    doc_lines = make_document(input_dir)
    write_output(doc_lines, out_file)
    print("Done. Open", out_file)

if __name__ == "__main__":
    main()
