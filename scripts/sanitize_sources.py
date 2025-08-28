#!/usr/bin/env python3
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src" / "main" / "java"

STRIP_IF_CONTAINS = (
    "```", "mermaid", "Markdown", "SQL", "Bash", "JSON", "properties", "dir:", "text"
)
STRIP_IF_EXACT = set(x.lower() for x in ("text","java","sql","bash","json","markdown","mermaid","properties","batch","dir:"))

def expected_package(p: Path) -> str:
    rel = p.relative_to(SRC).parent
    return ".".join(rel.parts)

def sanitize_file(p: Path) -> bool:
    raw = p.read_text(encoding="utf-8", errors="ignore")
    raw = raw.replace("\u00A0", " ")  # NBSP to space
    lines = raw.splitlines()
    changed = False

    # drop obvious non-java fence lines
    kept = []
    for ln in lines:
        s = ln.strip()
        if any(tok in s for tok in STRIP_IF_CONTAINS):
            changed = True
            continue
        if s.lower() in STRIP_IF_EXACT:
            changed = True
            continue
        kept.append(ln)

    # remove duplicate bare comment closers like stray */
    kept = [ln for ln in kept if ln.strip() != "*/"]

    # ensure correct package at top
    kept = [ln for ln in kept if not ln.strip().startswith("package ")]
    pkg = expected_package(p)
    if pkg:
        kept.insert(0, f"package {pkg};")
        changed = True

    text = "\n".join(kept) + "\n"
    if text != raw:
        p.write_text(text, encoding="utf-8")
        changed = True
    return changed

def main():
    any_change = False
    for java in SRC.rglob("*.java"):
        if sanitize_file(java):
            print(f"[sanitize] fixed {java.relative_to(ROOT)}")
            any_change = True
    if not any_change:
        print("Nothing to sanitize.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
