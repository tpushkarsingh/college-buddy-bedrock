import sys
import os
import shutil

# Add app to path
sys.path.append(os.path.join(os.getcwd(), "app"))

from ingest import ingest
from rag import ask

def test_flow():
    # 1. Create a sample text file
    os.makedirs("data", exist_ok=True)
    sample_file = "data/test.txt"
    with open(sample_file, "w") as f:
        f.write("The capital of France is Paris. It is known as the City of Light.")
    
    print("Ingesting sample document...")
    ingest(sample_file)
    
    # 2. Query the RAG system
    query = "What is the capital of France?"
    print(f"Querying: {query}")
    answer = ask(query)
    
    print(f"\nAnswer: {answer}")
    
    if "Paris" in answer:
        print("\nTest PASSED!")
    else:
        print("\nTest FAILED (Answer didn't contain 'Paris')")

if __name__ == "__main__":
    test_flow()
