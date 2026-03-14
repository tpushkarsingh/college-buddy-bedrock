#!/bin/bash

# Navigate to the offline directory
cd "$(dirname "$0")"

# Clean up hidden macOS metadata files that cause UnicodeDecodeErrors on external drives
find . -name "._*" -type f -delete

# Activate virtual environment if it exists
if [ -d "ai_env" ]; then
    source ai_env/bin/activate
fi

# Set environment variables if needed
export PYTHONPATH=$PYTHONPATH:$(pwd)/app

# Start the server
echo "Starting College Buddy Offline Server..."
python3 -m uvicorn app.server:app --host 0.0.0.0 --port 8000 --reload
