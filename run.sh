#!/usr/bin/env bash
# Compila y ejecuta el programa principal. Uso: ./run.sh > salida_nueva.txt
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac -encoding UTF-8 -d out src/*.java 2>&1 | grep -v JAVA_TOOL_OPTIONS || true
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out Main 2> /dev/null
