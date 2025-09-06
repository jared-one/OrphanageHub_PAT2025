#!/bin/bash

echo "Fixing Python scripts..."

# Fix section_build.py line 43 (in the DummyConsole class)
sed -i '43,44c\            text = re.sub(r'"'"'```(?:math)?.*?```'"'"', '"'"''"'"', str(text), flags=re.DOTALL)' scripts/section_build.py

# Fix log_analyzer.py - likely has similar issue
if grep -q 'r"```math' scripts/log_analyzer.py; then
    sed -i 's/r"```math/r"```(?:math)?.*?```"/g' scripts/log_analyzer.py
    sed -i '/re.sub.*```/s/)$/, flags=re.DOTALL)/' scripts/log_analyzer.py
fi

# Verify the fixes
echo "Testing section_build.py..."
python3 scripts/section_build.py --clean

echo "Testing log_analyzer.py..."
python3 scripts/log_analyzer.py --help 2>/dev/null || echo "log_analyzer.py may need additional fixes"
