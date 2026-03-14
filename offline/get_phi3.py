from huggingface_hub import hf_hub_download
import os

model_dir = "models/slm"
os.makedirs(model_dir, exist_ok=True)

print("Downloading Phi-3-Mini (3.8B) GGUF... This is ~2.3GB.")
print("This may take a few minutes depending on your internet speed.")

path = hf_hub_download(
    repo_id="microsoft/Phi-3-mini-4k-instruct-gguf",
    filename="Phi-3-mini-4k-instruct-q4.gguf",
    local_dir=model_dir
)

print(f"\nSUCCESS! Model downloaded to: {path}")
print("You can now delete the old TinyLlama model to save space.")
