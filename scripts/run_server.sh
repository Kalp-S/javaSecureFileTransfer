#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [ ! -d "bin" ]; then
    echo "Compiled binaries not found. Building project..."
    ./scripts/build.sh
fi

PORT="${1:-8080}"
OUTPUT_PATH="${2:-src/lab3/server_files/output.jpg}"

echo "Starting Server on port $PORT (output: $OUTPUT_PATH)..."
java -cp bin lab3.Server "$PORT" "$OUTPUT_PATH"
