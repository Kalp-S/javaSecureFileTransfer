#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "======================================================"
echo "    Compiling Java Secure File Transfer Protocol      "
echo "======================================================"

mkdir -p bin
javac -d bin src/lab3/*.java test/lab3/*.java

echo "✔ Compilation succeeded."
echo ""
echo "Running automated cryptographic test suite..."
java -cp bin lab3.CryptoTestSuite
