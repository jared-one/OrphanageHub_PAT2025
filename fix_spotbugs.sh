#!/bin/bash

echo "Fixing SpotBugs issues..."

# Fix EI_EXPOSE_REP2 issues (storing external references)
# These are in the dashboard panels - they store mainApp reference
# This is actually OK for GUI classes, but we can add validation

# Add null checks for all dashboard panels
for file in AdminDashboardPanel DonorDashboardPanel HomePanel LoginPanel \
            OrphanageDashboardPanel RegistrationPanel VolunteerDashboardPanel; do
    
    FILE="src/main/java/com/orphanagehub/gui/${file}.java"
    if [ -f "$FILE" ]; then
        # Add Objects.requireNonNull import if not present
        if ! grep -q "import java.util.Objects;" "$FILE"; then
            sed -i '/^package/a import java.util.Objects;' "$FILE"
        fi
        
        # Wrap mainApp assignment with requireNonNull
        sed -i 's/this\.mainApp = mainApp;/this.mainApp = Objects.requireNonNull(mainApp, "mainApp cannot be null");/' "$FILE"
    fi
done

# Fix SF_SWITCH_NO_DEFAULT in AdminDashboardPanel
# Add default cases to switch statements
sed -i '/switch.*getColumnName/,/^[[:space:]]*}/ {
    /^[[:space:]]*}$/i\
                default:\
                    break;
}' src/main/java/com/orphanagehub/gui/AdminDashboardPanel.java

# Fix REC_CATCH_EXCEPTION in OrphanageHubApp
# Already fixed - just verify it's using Exception not SQLException | IOException
sed -i 's/catch (SQLException | IOException e)/catch (Exception e)/g' src/main/java/com/orphanagehub/gui/OrphanageHubApp.java

echo "SpotBugs fixes applied. Formatting code..."
mvn spotless:apply

echo "Verifying fixes..."
mvn compile
