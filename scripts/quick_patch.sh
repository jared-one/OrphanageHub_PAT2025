#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# 1) class X() -> class X
find "$ROOT/src/main/java" -name "*.java" -print0 | xargs -0 sed -i -E 's/(class[[:space:]]+[A-Za-z_][A-Za-z0-9_]*)KATEX_INLINE_OPENKATEX_INLINE_CLOSE/\1/g'

# 2) interface/enum Foo() -> Foo
find "$ROOT/src/main/java" -name "*.java" -print0 | xargs -0 sed -i -E 's/((interface|enum)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*)KATEX_INLINE_OPENKATEX_INLINE_CLOSE/\1/g'

# 3) throws X() -> throws X
find "$ROOT/src/main/java" -name "*.java" -print0 | xargs -0 sed -i -E 's/(throws[[:space:]]+[A-Za-z_][A-Za-z0-9_.]*)KATEX_INLINE_OPENKATEX_INLINE_CLOSE/\1/g'

# 4) Remove lines that are only close parens/braces ()))), )) etc.
awk -i inplace '
  { s=$0; gsub(/^[[:space:]]+|[[:space:]]+$/, "", s);
    if (s ~ /^[KATEX_INLINE_CLOSE\}]+$/) next;
    print $0
  }' $(find "$ROOT/src/main/java" -name "*.java")

# 5) Remove stray bare comment-closers */
sed -i -E '/^[[:space:]]*\*\/[[:space:]]*$/d' $(find "$ROOT/src/main/java" -name "*.java")

# Specific RegistrationService patterns
sed -i -E 's/(setUserIdKATEX_INLINE_OPEN.*[^;])\;$/\1KATEX_INLINE_CLOSE;/g' $(find "$ROOT/src/main/java" -name "RegistrationService.java")
sed -i -E 's/(setDateRegisteredKATEX_INLINE_OPEN.*System\.currentTimeMillisKATEX_INLINE_OPENKATEX_INLINE_CLOSE[[:space:]]*)KATEX_INLINE_CLOSE\;$/\1KATEX_INLINE_CLOSEKATEX_INLINE_CLOSE;/g' $(find "$ROOT/src/main/java" -name "RegistrationService.java")
sed -i -E 's/(linkUserToOrphanageKATEX_INLINE_OPEN[^;]*[^)])\;$/\1KATEX_INLINE_CLOSE;/' $(find "$ROOT/src/main/java" -name "RegistrationService.java")

echo "Bulk patches applied."
