import fitz  # PyMuPDF
import json
import os
from sentence_transformers import SentenceTransformer
import faiss

import sys

# Determine absolute path to the 'offline' directory
def get_base_dir():
    if getattr(sys, 'frozen', False):
        bundle_root = sys._MEIPASS
        internal_path = os.path.join(bundle_root, "_internal")
        if os.path.isdir(os.path.join(internal_path, "models")):
            return internal_path
        return bundle_root
    return os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

BASE_DIR = get_base_dir()
EMBED_PATH = os.path.join(BASE_DIR, "models", "embedding")

embedder = SentenceTransformer(EMBED_PATH)

def extract_text_from_pdf(pdf_path):
    doc = fitz.open(pdf_path)
    text = ""
    for page in doc:
        text += page.get_text()
    return text

def chunk_text(text, chunk_size=1000, overlap=150):
    chunks = []
    for i in range(0, len(text), chunk_size - overlap):
        chunks.append(text[i:i + chunk_size])
    return chunks

import re

def is_noise(text):
    # Filter out chunks that are mostly links or references
    urls = re.findall(r'http[s]?://', text)
    if len(urls) > 2: return True
    if "References" in text or "Bibliography" in text: return True
    if "CHAPTER" in text and len(text) < 200: return True
    return False

def ingest(file_path):
    if file_path.endswith('.pdf'):
        text = extract_text_from_pdf(file_path)
    else:
        with open(file_path, 'r') as f:
            text = f.read()
    
    raw_chunks = chunk_text(text)
    
    # FILTER: Remove chunks that look like meta-data or link-lists
    chunks = [c for c in raw_chunks if not is_noise(c) and len(c.strip()) > 150]
    
    if not chunks:
        print("Warning: No quality content found in file.")
        return

    vectors = embedder.encode(chunks)
    
    # Ensure vectors directory exists
    vectors_dir = os.path.join(BASE_DIR, "vectors")
    os.makedirs(vectors_dir, exist_ok=True)
    
    # Store vectors in FAISS
    index = faiss.IndexFlatL2(len(vectors[0]))
    index.add(vectors)
    index_path = os.path.join(vectors_dir, "index.faiss")
    faiss.write_index(index, index_path)
    
    # Store chunks in JSON for retrieval
    chunks_path = os.path.join(vectors_dir, "chunks.json")
    with open(chunks_path, "w") as f:
        json.dump(chunks, f)
    
    print(f"Ingested {len(chunks)} technical chunks from {file_path}")
