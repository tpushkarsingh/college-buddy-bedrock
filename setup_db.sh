#!/bin/bash
# Check if psql is available
if ! command -v psql &> /dev/null; then
    echo "psql could not be found. Please ensure PostgreSQL client tools are installed."
    exit 1
fi

# Create database if it doesn't exist
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'collegedb'" | grep -q 1 || psql -U postgres -c "CREATE DATABASE collegedb"

# Enable pgvector extension
psql -U postgres -d collegedb -c "CREATE EXTENSION IF NOT EXISTS vector"
