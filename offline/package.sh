#!/bin/bash

# Navigate to the offline directory
cd "$(dirname "$0")"

# Activate virtual environment
if [ -d "ai_env" ]; then
    source ai_env/bin/activate
fi

# Clean up previous builds completely
rm -rf build dist CollegeBuddyOffline.spec
mkdir -p dist

echo "Preparing models and static files..."
# Ensure dist and models are in place
cp -r ../frontend/dist ./dist

echo "Installing PyInstaller..."
pip install pyinstaller

echo "Running PyInstaller..."
# We use --onedir because --onefile has a size limit that our 2GB model hits.
# This also makes the app start much faster.
pyinstaller --noconfirm --onedir \
    --name "CollegeBuddy" \
    --add-data "app:app" \
    --add-data "dist:dist" \
    --add-data "models:models" \
    --add-data "vectors:vectors" \
    --hidden-import "uvicorn.logging" \
    --hidden-import "uvicorn.loops" \
    --hidden-import "uvicorn.loops.auto" \
    --hidden-import "uvicorn.protocols" \
    --hidden-import "uvicorn.protocols.http" \
    --hidden-import "uvicorn.protocols.http.auto" \
    --hidden-import "uvicorn.protocols.websockets" \
    --hidden-import "uvicorn.protocols.websockets.auto" \
    --hidden-import "uvicorn.lifespan" \
    --hidden-import "uvicorn.lifespan.off" \
    --hidden-import "uvicorn.lifespan.on" \
    --collect-all "sentence_transformers" \
    --collect-all "llama_cpp" \
    --collect-all "sqlalchemy" \
    --collect-all "jose" \
    --collect-all "passlib" \
    --collect-all "bcrypt" \
    --collect-all "pydantic" \
    --collect-all "fitz" \
    --collect-all "fastapi" \
    --collect-all "uvicorn" \
    main.py

echo "------------------------------------------------"
echo "BUILD COMPLETE!"
echo "Your app is in: offline/dist/CollegeBuddyOffline/"
echo "COPY THE ENTIRE 'CollegeBuddyOffline' FOLDER to your pendrive."
echo "Inside that folder, double-click the file named 'CollegeBuddyOffline'."
echo "------------------------------------------------"
