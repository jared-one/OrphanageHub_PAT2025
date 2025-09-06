#!/usr/bin/env python3
"""
all_guis_only.py

Collect only files that live in any 'gui' directory across commits and write a single
combined text file (default: all_guis.txt) with nice sections per commit.

Highlights:
 - filters files to only those where a path segment equals 'gui'
 - writes a single combined output file (all_guis.txt) in the out-dir
 - preserves binary detection and optional saving of binary blobs
 - supports truncation, names-only, limit, --all, etc.

Usage examples:
  python all_guis_only.py                                 # HEAD only -> commit section(s) in commit_outputs/all_guis.txt
  python all_guis_only.py --limit 10                      # last 10 commits
  python all_guis_only.py --all --out-dir out --truncate-kb 200
  python all_guis_only.py --only-names                    # list only file paths (no contents)
"""
from __future__ import annotations
import argparse
import subprocess
import os
import sys
from pathlib import Path, PurePosixPath
import datetime
import textwrap

def run_git_bytes(args, repo_path="."):
    cmd = ["git"] + args
    try:
        proc = subprocess.run(cmd, cwd=repo_path, capture_output=True, text=False)
        return proc.returncode, proc.stdout, proc.stderr
    except FileNotFoundError:
        print("ERROR: git not found. Install git and ensure it's in PATH.", file=sys.stderr)
        sys.exit(2)

def decode_bytes(b: bytes) -> str:
    if b is None:
        return ""
    try:
        return b.decode("utf-8")
    except Exception:
        try:
            return b.decode("latin-1")
        except Exception:
            return b.decode("utf-8", errors="replace")

def get_commit_list(repo_path: str = ".", all_commits: bool = False, limit: int | None = None, reverse: bool = True):
    args = ["rev-list"]
    if all_commits:
        args.append("--all")
    else:
        args.append("HEAD")
    if limit:
        args.extend(["-n", str(limit)])
    if reverse:
        args.append("--reverse")
    rc, out, err = run_git_bytes(args, repo_path)
    if rc != 0:
        print("ERROR: git rev-list failed:\n", decode_bytes(err), file=sys.stderr)
        sys.exit(3)
    commits = [line.strip() for line in decode_bytes(out).splitlines() if line.strip()]
    return commits

def get_commit_info(commit_hash: str, repo_path: str = ".") -> dict:
    fmt = "%H%n%an%n%ai%n%s"
    rc, out, err = run_git_bytes(["show", "-s", f"--format={fmt}", commit_hash], repo_path)
    if rc != 0:
        return {"hash": commit_hash, "author": "", "date": "", "message": "(failed to read)"}
    lines = decode_bytes(out).splitlines()
    return {
        "hash": lines[0] if len(lines) > 0 else commit_hash,
        "author": lines[1] if len(lines) > 1 else "",
        "date": lines[2] if len(lines) > 2 else "",
        "message": lines[3] if len(lines) > 3 else "",
    }

def list_files_in_commit(commit_hash: str, repo_path: str = ".") -> list:
    rc, out, err = run_git_bytes(["diff-tree", "--no-commit-id", "--name-only", "-r", commit_hash], repo_path)
    if rc != 0:
        return []
    return [line.strip() for line in decode_bytes(out).splitlines() if line.strip()]

def get_diff_for_commit(commit_hash: str, repo_path: str = ".") -> str:
    rc, out, err = run_git_bytes(["show", "--pretty=short", "--no-color", "--patch", "--max-count=1", commit_hash], repo_path)
    if rc != 0:
        return ""
    return decode_bytes(out)

def show_file_bytes(commit_hash: str, filepath: str, repo_path: str = "."):
    return run_git_bytes(["show", f"{commit_hash}:{filepath}"], repo_path)

def safe_write_bytes(path: Path, data: bytes):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "wb") as fh:
        fh.write(data)

def safe_write_text(path: Path, text: str, encoding: str = "utf-8"):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding=encoding) as fh:
        fh.write(text)

def path_contains_gui_segment(posix_path_str: str) -> bool:
    """
    Return True if any path segment equals 'gui' (case-sensitive).
    Uses PurePosixPath because git uses posix-style paths.
    """
    p = PurePosixPath(posix_path_str)
    return any(part == "gui" for part in p.parts)

def make_combined_output(commits: list, repo_path: str, out_dir: Path, args):
    out_dir.mkdir(parents=True, exist_ok=True)
    combined_path = out_dir / (args.output_file or "all_guis.txt")
    sections = []

    for idx, commit in enumerate(commits, start=1):
        info = get_commit_info(commit, repo_path)
        files = list_files_in_commit(commit, repo_path)

        # Filter for GUI files (any path segment == 'gui')
        gui_files = [f for f in files if path_contains_gui_segment(f)]
        if not gui_files:
            # skip commits that don't touch any gui files
            continue

        header = []
        short = commit[:8]
        header.append("=" * 80)
        header.append(f"Commit {idx} — {short}")
        header.append(f"Full hash: {info.get('hash')}")
        header.append(f"Author: {info.get('author')}")
        header.append(f"Date: {info.get('date')}")
        header.append(f"Message: {info.get('message')}")
        header.append(f"Generated: {datetime.datetime.now().isoformat()}")
        header.append("")
        if args.show_diff:
            diff_text = get_diff_for_commit(commit, repo_path)
            if diff_text:
                header.append("--- DIFF ---")
                header.append(diff_text)
                header.append("")

        header.append("Files (gui-only):")
        for f in gui_files:
            header.append(f"- {f}")
        header.append("")

        sections.append("\n".join(header))

        if args.only_names:
            # don't include file contents
            continue

        # Add file contents
        for f in gui_files:
            sec = []
            sec.append("-" * 60)
            sec.append(f"File: {f}")
            rc, out, err = show_file_bytes(commit, f, repo_path)
            if rc != 0:
                sec.append(f"*(failed to read file from commit: {decode_bytes(err)})*")
                sections.append("\n".join(sec))
                continue

            if out is None:
                sec.append("*(empty file)*")
                sections.append("\n".join(sec))
                continue

            is_binary = b"\x00" in out
            if is_binary:
                sec.append("*(binary file — contents not printed inline)*")
                if args.save_binaries:
                    # save binary under out_dir/files/<commit>/<filepath>
                    binary_rel = Path("files") / commit / Path(f)
                    target = out_dir / binary_rel
                    safe_write_bytes(target, out)
                    sec.append(f"Saved binary to: {target}")
                else:
                    sec.append("Use --save-binaries to store the raw file in the output directory.")
                sections.append("\n".join(sec))
                continue

            text = decode_bytes(out)
            if args.truncate_kb and args.truncate_kb > 0:
                kb = args.truncate_kb
                max_chars = kb * 1024
                if len(text) > max_chars:
                    sec.append(f"*(file too large — showing first {kb} KB, use --truncate-kb to change)*")
                    sec.append("```")
                    sec.append(text[:max_chars])
                    sec.append("```")
                    sections.append("\n".join(sec))
                    continue

            sec.append("```")
            sec.append(text)
            sec.append("```")
            sections.append("\n".join(sec))

    if not sections:
        combined_text = "No GUI files found across selected commits."
    else:
        combined_text = "\n\n".join(sections)

    safe_write_text(combined_path, combined_text)
    return combined_path

def main():
    p = argparse.ArgumentParser(description="Export GUI files from commits into a single all_guis.txt file.")
    p.add_argument("--repo", "-r", default=".", help="path to git repository (default: current dir)")
    p.add_argument("--all", action="store_true", help="iterate all commits (default: only HEAD)")
    p.add_argument("--limit", "-n", type=int, default=None, help="limit number of commits to process")
    p.add_argument("--out-dir", default="commit_outputs", help="directory to write outputs")
    p.add_argument("--only-names", action="store_true", help="only list filenames (no contents)")
    p.add_argument("--show-diff", action="store_true", help="include full commit diff in the output")
    p.add_argument("--save-binaries", action="store_true", help="save binary files to out-dir/files/<commit>/...")
    p.add_argument("--truncate-kb", type=int, default=0, help="if >0, truncate printed file contents to this many KB")
    p.add_argument("--reverse", action="store_true", help="process newest->oldest instead of oldest->newest")
    p.add_argument("--output-file", default="all_guis.txt", help="name of the combined output file (default: all_guis.txt)")
    args = p.parse_args()

    repo = Path(args.repo).resolve()
    if not repo.exists():
        print(f"ERROR: repo path not found: {repo}", file=sys.stderr)
        sys.exit(1)

    rc, out, err = run_git_bytes(["rev-parse", "--git-dir"], str(repo))
    if rc != 0:
        print("ERROR: not a git repository (or git missing) at", repo, file=sys.stderr)
        print(decode_bytes(err), file=sys.stderr)
        sys.exit(1)

    commits = get_commit_list(str(repo), all_commits=args.all, limit=args.limit, reverse=not args.reverse)
    if not commits:
        print("No commits found.")
        sys.exit(0)

    out_dir = Path(args.out_dir).resolve()
    out_dir.mkdir(parents=True, exist_ok=True)

    print(f"Collecting GUI files into: {out_dir / args.output_file} — scanning {len(commits)} commits")

    combined_path = make_combined_output(commits, str(repo), out_dir, args)
    print(f"Wrote combined GUI file: {combined_path}")
    print("Done.")

if __name__ == '__main__':
    main()
