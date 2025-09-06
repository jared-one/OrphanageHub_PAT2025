#!/bin/bash

echo "Fixing Java compilation errors..."

# 1. Fix OrphanageHubApp.java
echo "Fixing OrphanageHubApp.java..."
# Revert the broken multi-catch syntax
sed -i 's/catch (SQLException | IOException e)/catch (Exception e)/g' src/main/java/com/orphanagehub/gui/OrphanageHubApp.java

# 2. Fix RegistrationPanel.java
echo "Fixing RegistrationPanel.java..."
# Revert the broken multi-catch syntax
sed -i 's/catch (SQLException | IOException e)/catch (Exception e)/g' src/main/java/com/orphanagehub/gui/RegistrationPanel.java

# 3. Fix any null reference issues in RegistrationPanel.java
# Look for the specific lines with errors (456 and 459)
sed -i '456s/null\./e./g' src/main/java/com/orphanagehub/gui/RegistrationPanel.java
sed -i '459s/null\./e./g' src/main/java/com/orphanagehub/gui/RegistrationPanel.java

echo "Java fixes applied."
