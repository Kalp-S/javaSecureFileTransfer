#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [ ! -d "bin" ]; then
    echo "Compiled binaries not found. Building project..."
    ./scripts/build.sh
fi

INPUT_FILE="${1:-src/lab3/client_files/input.jpg}"
HOST="${2:-localhost}"
PORT="${3:-8080}"

echo "Starting Client connecting to $HOST:$PORT (input: $INPUT_FILE)..."
java -cp bin lab3.Client "$INPUT_FILE" "$HOST" "$PORT"
