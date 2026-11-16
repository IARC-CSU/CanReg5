#!/bin/bash
# CanReg5 Linux/Unix Launcher Wrapper
# Resolves path so it can be safely executed or double-clicked from any directory.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Add standard macOS/Linux Homebrew and openjdk paths to PATH (GUI launches do not inherit shell profiles)
export PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:/usr/local/bin:/usr/local/opt/openjdk/bin:$PATH"

JAVA_CMD="java"
if [ -x "$DIR/jre/bin/java" ]; then
    JAVA_CMD="$DIR/jre/bin/java"
elif [ -x "$DIR/dist/jre/bin/java" ]; then
    JAVA_CMD="$DIR/dist/jre/bin/java"
fi

if [ -f "dist/CanReg.jar" ]; then
    "$JAVA_CMD" -Xms256m -Xmx1024m -cp "dist/CanReg.jar:dist/lib/*" canreg.client.CanRegClientApp "$@"
elif [ -f "CanReg.jar" ]; then
    "$JAVA_CMD" -Xms256m -Xmx1024m -cp "CanReg.jar:lib/*" canreg.client.CanRegClientApp "$@"
else
    echo "Error: CanReg.jar could not be found." >&2
    echo "Please compile with 'ant jar' or ensure CanReg.jar is in the same folder." >&2
    exit 1
fi
