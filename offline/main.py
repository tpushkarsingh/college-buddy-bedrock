import uvicorn
import webbrowser
import threading
import time
import os
import sys

def open_browser():
    # Wait a moment for the server to start
    time.sleep(2)
    webbrowser.open("http://localhost:8000")

if __name__ == "__main__":
    # Add the current directory to sys.path so 'app.server' can be imported
    # Handle both normal execution and PyInstaller execution
    if getattr(sys, 'frozen', False):
        # We are bundled. 
        base_path = sys._MEIPASS
        # In --onedir mode, modules are usually in _internal
        internal_path = os.path.join(base_path, "_internal")
        if os.path.isdir(internal_path):
            sys.path.insert(0, internal_path)
            # Help uvicorn/subprocesses find modules
            os.environ["PYTHONPATH"] = f"{internal_path}:{os.environ.get('PYTHONPATH', '')}"
        sys.path.insert(0, base_path)
    else:
        base_path = os.path.dirname(os.path.abspath(__file__))
        sys.path.insert(0, base_path)

    print("Starting College Buddy Offline...")
    print("Your browser will open automatically in a moment.")
    
    # Start browser in a background thread
    threading.Thread(target=open_browser, daemon=True).start()
    
    # Start uvicorn server
    uvicorn.run("app.server:app", host="0.0.0.0", port=8000, reload=False)
