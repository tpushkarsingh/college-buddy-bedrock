#Query → Embed → Vector search → Context → Prompt → SLM → Answer

from llama_cpp import Llama
import faiss
import json
import os
import sys
from sentence_transformers import SentenceTransformer

# Determine absolute path to the 'offline' directory
def get_base_dir():
    if getattr(sys, 'frozen', False):
        # We are bundled. Check root and _internal
        bundle_root = sys._MEIPASS
        internal_path = os.path.join(bundle_root, "_internal")
        if os.path.isdir(os.path.join(internal_path, "models")):
            return internal_path
        return bundle_root
    return os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

BASE_DIR = get_base_dir()
MODEL_PATH = os.path.join(BASE_DIR, "models", "slm", "Phi-3-mini-4k-instruct-q4.gguf")
EMBED_PATH = os.path.join(BASE_DIR, "models", "embedding")

# Load Phi-3-Mini (3.8B) Model with GPU Acceleration
# n_gpu_layers=-1 offloads all layers to the Apple Silicon GPU (Metal)
llm = Llama(model_path=MODEL_PATH, n_ctx=2048, n_gpu_layers=-1)
embedder = SentenceTransformer(EMBED_PATH)

def load_chunks(indices):
    chunks_path = os.path.join(BASE_DIR, "vectors", "chunks.json")
    if not os.path.exists(chunks_path):
        return ["No context available."]
    
    with open(chunks_path, "r") as f:
        all_chunks = json.load(f)
    
    return [all_chunks[i] for i in indices[0] if i < len(all_chunks)]

def ask(query):
    index_path = os.path.join(BASE_DIR, "vectors", "index.faiss")
    if not os.path.exists(index_path):
        return "System not initialized. Please ingest documents first."
        
    index = faiss.read_index(index_path)
    qvec = embedder.encode([query])
    D, I = index.search(qvec, k=3)
    
    print(f"DEBUG: RAG Query: '{query}' | Distance: {D[0][0]:.4f}")

    if D[0][0] > 1.3:
        return "I'm sorry, I couldn't find technical details in the documents to answer that."

    context_chunks = load_chunks(I)
    unique_context = list(dict.fromkeys([c.strip() for c in context_chunks if len(c.strip()) > 50]))
    context_text = "\n---\n".join(unique_context[:3])
    
    # Phi-3 Native Prompt Template
    prompt = f"""<|system|>
You are a technical expert. Knowledge: {context_text}<|end|>
<|user|>
Answer in 3 bullet points: {query}<|end|>
<|assistant|>"""
    
    output = llm(prompt, max_tokens=300, stop=["<|end|>", "<|user|>"], temperature=0.1, repeat_penalty=1.2)
    return output["choices"][0]["text"].strip()
