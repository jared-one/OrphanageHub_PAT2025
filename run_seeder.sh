#!/bin/bash
# DataSeeder Runner Script
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║              ORPHANAGEHUB DATA SEEDER                         ║"
echo "╚══════════════════════════════════════════════════════════════╝"

# Ensure we're in project root
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: Run this from project root (where pom.xml is)"
    exit 1
fi

# Set database path for the application
export DB_PATH="db/OrphanageHub.sqlite"

# Check if database exists
if [ ! -f "$DB_PATH" ]; then
    echo "❌ Database not found at: $DB_PATH"
    exit 1
fi

echo "📁 Database found at: $DB_PATH"
echo ""

# Compile first
echo "🔨 Compiling project..."
mvn clean compile -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

# Run DataSeeder with system property for DB path
echo "🌱 Running DataSeeder..."
echo ""

mvn exec:java \
    -Dexec.mainClass="com.orphanagehub.tools.DataSeeder" \
    -Dexec.classpathScope="compile" \
    -Ddb.path="$DB_PATH" \
    -Dexec.cleanupDaemonThreads=false \
    -q

echo ""
echo "✅ Done! Check your database now."