#!/bin/bash
# CanReg5 Linux/Unix Launcher Wrapper
# Resolves path so it can be safely executed or double-clicked from any directory.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

if [ -f "dist/CanReg.jar" ]; then
    java -jar dist/CanReg.jar
elif [ -f "CanReg.jar" ]; then
    java -jar CanReg.jar
else
    echo "Error: CanReg.jar could not be found." >&2
    echo "Please compile with 'ant jar' or ensure CanReg.jar is in the same folder." >&2
    exit 1
fi
