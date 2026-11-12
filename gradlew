#!/bin/sh
# Gradle Wrapper script for Linux/macOS/GitHub Actions
set -e

DIRNAME=$(dirname "$0")
[ -z "$DIRNAME" ] && DIRNAME="."

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

WRAPPER_JAR="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    mkdir -p "$DIRNAME/gradle/wrapper"
    curl -sLo "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || true
fi

if [ -f "$WRAPPER_JAR" ]; then
    exec "$JAVACMD" -jar "$WRAPPER_JAR" "$@"
else
    exec gradle "$@"
fi
