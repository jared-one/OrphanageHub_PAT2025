#!/usr/bin/env bash
set -euo pipefail

# Detect Java/resources roots (supports either src/main or main)
if [[ -d "src/main/java/com/orphanagehub" ]]; then
  JAVA_ROOT="src/main/java/com/orphanagehub"
  RES_ROOT="src/main/resources"
elif [[ -d "main/java/com/orphanagehub" ]]; then
  JAVA_ROOT="main/java/com/orphanagehub"
  RES_ROOT="main/resources"
else
  echo "Could not find Java root. Expected at src/main/java/com/orphanagehub or main/java/com/orphanagehub"
  exit 1
fi

OUTFILE="${1:-all_code.txt}"

SECTIONS=(util dao model service gui)

uc() { echo "$1" | tr '[:lower:]' '[:upper:]'; }
emit() { printf '%s\n' "$1" >> "$OUTFILE"; }
sep()  { emit "============================================================"; }

# Start fresh
: > "$OUTFILE"

# Header
emit "PROJECT EXPORT"
emit "Path: $(pwd)"
emit "Date (UTC): $(date -u +"%Y-%m-%d %H:%M:%S")"
sep
emit "TABLE OF CONTENTS"

total_count=0
for s in "${SECTIONS[@]}"; do
  if [[ -d "$JAVA_ROOT/$s" ]]; then
    count=$(find "$JAVA_ROOT/$s" -maxdepth 1 -type f -name '*.java' | wc -l || true)
  else
    count=0
  fi
  total_count=$((total_count + count))
  emit "- $(uc "$s") SECTION: $count files"
done

# Count config-like files (no KATEX_INLINE_OPEN KATEX_INLINE_CLOSE grouping to avoid KaTeX issues)
prop_count=0
if [[ -d "$RES_ROOT" ]]; then
  prop_count=$(find "$RES_ROOT" -type f -name '*.properties' \
    -o -type f -name '*.conf' \
    -o -type f -name '*.json' \
    -o -type f -name '*.yml' \
    -o -type f -name '*.yaml' | wc -l || true)
fi
emit "- CONFIG SECTION: $prop_count files"
emit ""
sep
emit ""

# Emit Java sections
for s in "${SECTIONS[@]}"; do
  sec_dir="$JAVA_ROOT/$s"
  [[ -d "$sec_dir" ]] || continue

  emit "== $(uc "$s") SECTION =="
  emit ""

  mapfile -t files < <(find "$sec_dir" -type f -name '*.java' | LC_ALL=C sort)

  if [[ ${#files[@]} -eq 0 ]]; then
    emit "(no files)"
    emit ""
    continue
  fi

  for f in "${files[@]}"; do
    fname=$(basename "$f")
    relpath="$f"
    lines=$(wc -l < "$f" | tr -d ' ')
    modified=$(stat -c "%y" "$f" 2>/dev/null || echo "n/a")

    emit "== ${fname} =="
    emit "Path: ${relpath}"
    emit "Lines: ${lines} | Last-Modified: ${modified}"
    emit "-----"
    cat "$f" >> "$OUTFILE"
    emit ""
    emit ""
  done

  sep
  emit ""
done

# Emit config files (properties, yaml, json, conf) — KaTeX-safe filters
if [[ -d "$RES_ROOT" ]]; then
  emit "== CONFIG SECTION =="
  emit ""
  mapfile -t cfgs < <(find "$RES_ROOT" -type f -name '*.properties' \
    -o -type f -name '*.conf' \
    -o -type f -name '*.json' \
    -o -type f -name '*.yml' \
    -o -type f -name '*.yaml' | LC_ALL=C sort)
  if [[ ${#cfgs[@]} -eq 0 ]]; then
    emit "(no config files)"
    emit ""
  else
    for c in "${cfgs[@]}"; do
      cname=$(basename "$c")
      lines=$(wc -l < "$c" | tr -d ' ')
      modified=$(stat -c "%y" "$c" 2>/dev/null || echo "n/a")
      emit "== ${cname} =="
      emit "Path: ${c}"
      emit "Lines: ${lines} | Last-Modified: ${modified}"
      emit "-----"
      cat "$c" >> "$OUTFILE"
      emit ""
      emit ""
    done
  fi
  sep
  emit ""
fi

# Note images (skip binary) — KaTeX-safe filters
if [[ -d "$RES_ROOT" ]]; then
  emit "== RESOURCES (BINARY) =="
  emit "(skipping binary files; listing names only)"
  mapfile -t imgs < <(find "$RES_ROOT" -type f -iname '*.png' \
    -o -type f -iname '*.jpg' \
    -o -type f -iname '*.jpeg' \
    -o -type f -iname '*.gif' \
    -o -type f -iname '*.ico' \
    -o -type f -iname '*.svg' | LC_ALL=C sort)
  if [[ ${#imgs[@]} -eq 0 ]]; then
    emit "(no images)"
  else
    for i in "${imgs[@]}"; do
      size=$(stat -c "%s" "$i" 2>/dev/null || echo "?")
      emit "- ${i} (${size} bytes)"
    done
  fi
  emit ""
  sep
fi

echo "Done! Wrote $(realpath "$OUTFILE")"
