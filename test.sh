#!/usr/bin/env bash
# Compila src/ + test/ y corre todas las clases *Test con JUnit 4.
set -e
cd "$(dirname "$0")"
CP="out-test:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
rm -rf out-test && mkdir -p out-test
javac -encoding UTF-8 -cp "$CP" -d out-test src/*.java test/*.java 2>&1 | grep -v JAVA_TOOL_OPTIONS || true
CLASES=$(cd test && ls *Test.java | sed 's/\.java$//')
java -Dstdout.encoding=UTF-8 -cp "$CP" org.junit.runner.JUnitCore $CLASES 2> /dev/null
