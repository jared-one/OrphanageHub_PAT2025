#!/bin/bash

echo "Fixing all panel files..."

# List of panel files to check
PANELS=(
    "AdminDashboardPanel"
    "OrphanageDashboardPanel"
    "VolunteerDashboardPanel"
    "HomePanel"
    "LoginPanel"
    "RegistrationPanel"
    "OrphanageHubApp"
)

for panel in "${PANELS[@]}"; do
    FILE="src/main/java/com/orphanagehub/gui/${panel}.java"
    if [ -f "$FILE" ]; then
        echo "Fixing $panel..."
        # Fix extends JPanel() to extends JPanel
        sed -i 's/extends JPanel()/extends JPanel/g' "$FILE"
        # Fix malformed JavaDoc comments
        sed -i 's/\/ \*\*/\/\*\*/g' "$FILE"
        sed -i 's/\* \//\*\//g' "$FILE"
        # Fix Logger calls
        sed -i 's/Logger\.KATEX_INLINE_OPENinfo\|debug\|error\|warnKATEX_INLINE_CLOSE(\s*"KATEX_INLINE_OPEN[^"]*KATEX_INLINE_CLOSE"\s*;/Logger.\1("\2");/g' "$FILE"
        sed -i 's/logger\.KATEX_INLINE_OPENinfo\|debug\|error\|warnKATEX_INLINE_CLOSE(\s*"KATEX_INLINE_OPEN[^"]*KATEX_INLINE_CLOSE"\s*;/logger.\1("\2");/g' "$FILE"
    fi
done

echo "Done fixing panel files!"
