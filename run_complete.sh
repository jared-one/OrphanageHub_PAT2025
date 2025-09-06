#!/bin/bash

echo "=== OrphanageHub Launcher ==="

# 1. Ensure everything is built
if [ ! -f target/OrphanageHub-1.0.0.jar ]; then
    echo "Building application..."
    mvn clean package -DskipTests
fi

# 2. Generate classpath if needed
if [ ! -f target/ext-cp.txt ]; then
    echo "Generating classpath..."
    mvn dependency:build-classpath -Dmdep.outputFile=target/ext-cp.txt
fi

# 3. Run with proper classpath
echo "Starting application..."
java -cp "target/classes:$(cat target/ext-cp.txt)" \
     com.orphanagehub.gui.OrphanageHubApp

echo "Application closed."
