#!/usr/bin/env python3
"""
all_code_based_on_all_commit.py

Improved version that: 
 - writes a nicely formatted markdown file per commit (not spamming terminal)
 - captures git output as bytes and decodes safely (avoids UnicodeDecodeError)
 - detects binary files and saves them as raw files when requested
 - optional truncation for very large files
 - CLI flags: --all, --limit, --out-dir, --save-binaries, --truncate-kb, --only-names, --show-diff

Usage examples:
  python all_code_based_on_all_commit.py                 # process HEAD only -> creates commit_HEAD.md
  python all_code_based_on_all_commit.py --limit 5      # process last 5 commits (oldest->newest)
  python all_code_based_on_all_commit.py --all --out-dir out --save-binaries --truncate-kb 200

Designed to be run from the repository root or pass --repo /path/to/repo
"""
from __future__ import annotations
import argparse
import subprocess
import os
import sys
from pathlib import Path
import datetime
import shutil
import textwrap


def run_git_bytes(args, repo_path="."):
    """Run git command and return (returncode, stdout_bytes, stderr_bytes). Never use text=True."""
    cmd = ["git"] + args
    try:
        proc = subprocess.run(cmd, cwd=repo_path, capture_output=True, text=False)
        return proc.returncode, proc.stdout, proc.stderr
    except FileNotFoundError:
        print("ERROR: git not found. Install git and ensure it's in PATH.", file=sys.stderr)
        sys.exit(2)


def decode_bytes(b: bytes) -> str:
    """Decode bytes to str robustly. Use utf-8 with replacement for any invalid sequences."""
    if b is None:
        return ""
    try:
        return b.decode("utf-8")
    except Exception:
        try:
            return b.decode("latin-1")
        except Exception:
            # Fallback: replace invalid sequences
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
    """Return (rc, bytes stdout, bytes stderr)."""
    return run_git_bytes(["show", f"{commit_hash}:{filepath}"], repo_path)


def safe_write_bytes(path: Path, data: bytes):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "wb") as fh:
        fh.write(data)


def safe_write_text(path: Path, text: str, encoding: str = "utf-8"):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding=encoding) as fh:
        fh.write(text)


def make_commit_markdown(commit_idx: int, commit_hash: str, info: dict, files: list, diff_text: str, repo_path: str, out_dir: Path, args):
    short = commit_hash[:8]
    filename = f"commit_{commit_idx:03d}_{short}.md" if args.limit or args.all else f"commit_{short}.md"
    md_path = out_dir / filename

    lines = []
    lines.append(f"# Commit {commit_idx} — {short}")
    lines.append("")
    lines.append(f"- Full hash: `{info.get('hash')}`")
    lines.append(f"- Author: {info.get('author')}")
    lines.append(f"- Date: {info.get('date')}")
    lines.append(f"- Message: {info.get('message')}")
    lines.append("")
    lines.append(f"Generated: {datetime.datetime.now().isoformat()}")
    lines.append("")

    if args.show_diff and diff_text:
        lines.append("---")
        lines.append("## Diff")
        lines.append("```")
        lines.append(diff_text)
        lines.append("```")
        lines.append("")

    lines.append("---")
    lines.append("## Files changed")
    for f in files:
        lines.append(f"- {f}")
    lines.append("")

    if args.only_names:
        safe_write_text(md_path, "\n".join(lines))
        return md_path

    # Add contents per file
    for f in files:
        lines.append(f"---")
        lines.append(f"### {f}")
        rc, out, err = show_file_bytes(commit_hash, f, repo_path)
        if rc != 0:
            lines.append(f"*(failed to read file from commit: {decode_bytes(err)})*")
            continue

        # detect binary
        if out is None:
            lines.append("*(empty file)*")
            continue

        is_binary = b"\x00" in out
        if is_binary:
            lines.append("*(binary file — contents not printed inline)*")
            if args.save_binaries:
                binary_rel = Path("files") / commit_hash / f
                target = out_dir / binary_rel
                safe_write_bytes(target, out)
                lines.append(f"Saved binary to: `{target}`")
            else:
                lines.append("Use --save-binaries to store the raw file in the output directory.")
            continue

        text = decode_bytes(out)
        if args.truncate_kb and args.truncate_kb > 0:
            kb = args.truncate_kb
            max_chars = kb * 1024
            if len(text) > max_chars:
                lines.append(f"*(file too large — showing first {kb} KB, use --truncate-kb to change)*")
                lines.append("```")
                lines.append(text[:max_chars])
                lines.append("```")
                continue

        # normal text file
        lines.append("```")
        lines.append(text)
        lines.append("```")

    safe_write_text(md_path, "\n".join(lines))
    return md_path


def main():
    p = argparse.ArgumentParser(description="Export files & contents from commits into per-commit markdown files.")
    p.add_argument("--repo", "-r", default=".", help="path to git repository (default: current dir)")
    p.add_argument("--all", action="store_true", help="iterate all commits (default: only HEAD)")
    p.add_argument("--limit", "-n", type=int, default=None, help="limit number of commits to process")
    p.add_argument("--out-dir", default="commit_outputs", help="directory to write outputs")
    p.add_argument("--only-names", action="store_true", help="only list filenames (no contents)")
    p.add_argument("--show-diff", action="store_true", help="include full commit diff in the markdown")
    p.add_argument("--save-binaries", action="store_true", help="save binary files to out-dir/files/<commit>/...")
    p.add_argument("--truncate-kb", type=int, default=0, help="if >0, truncate printed file contents to this many KB")
    p.add_argument("--reverse", action="store_true", help="process newest->oldest instead of oldest->newest")
    args = p.parse_args()

    repo = Path(args.repo).resolve()
    if not repo.exists():
        print(f"ERROR: repo path not found: {repo}", file=sys.stderr)
        sys.exit(1)

    # verify git repo
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
    if out_dir.exists() and not out_dir.is_dir():
        print(f"ERROR: out-dir exists and is not a directory: {out_dir}", file=sys.stderr)
        sys.exit(1)
    out_dir.mkdir(parents=True, exist_ok=True)

    print(f"Writing outputs to: {out_dir} — processing {len(commits)} commits")

    for idx, commit in enumerate(commits, start=1):
        info = get_commit_info(commit, str(repo))
        files = list_files_in_commit(commit, str(repo))
        diff_text = get_diff_for_commit(commit, str(repo)) if args.show_diff else ""
        md_path = make_commit_markdown(idx, commit, info, files, diff_text, str(repo), out_dir, args)
        print(f"Wrote: {md_path}")

    print("Done.")


if __name__ == '__main__':
    main()
