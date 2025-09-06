#!/bin/bash

echo "Starting OrphanageHub..."

# Start the app in background with proper output
java -cp "target/section-classes/*:target/classes:$(cat target/ext-cp.txt 2>/dev/null)" \
     com.orphanagehub.gui.OrphanageHubApp &

APP_PID=$!
echo "Application started with PID: $APP_PID"

# Save PID for make commands
echo $APP_PID > target/app.pid

# Wait a bit for startup
sleep 3

# Check if still running
if kill -0 $APP_PID 2>/dev/null; then
    echo "Application is running successfully"
    echo "Use 'kill $APP_PID' to stop"
    
    # Generate diagnostics
    jstack $APP_PID > logs/thread-dump-$(date +%s).txt 2>/dev/null
    echo "Thread dump saved to logs/"
    
    # Keep script running until app closes
    wait $APP_PID
else
    echo "Application failed to start"
    exit 1
fi
