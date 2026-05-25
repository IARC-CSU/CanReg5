#!/bin/bash
# create-mac-app.sh: Creates a native macOS App Bundle (CanReg5.app) for launching CanReg5.
set -e

# Define paths relative to the project root
APP_NAME="CanReg5.app"
CONTENTS_DIR="${APP_NAME}/Contents"
MACOS_DIR="${CONTENTS_DIR}/MacOS"
RESOURCES_DIR="${CONTENTS_DIR}/Resources"
ICON_SOURCE="src/canreg/client/gui/resources/LogoBetaNewer.png"

echo "Cleaning old macOS App Bundle..."
rm -rf "${APP_NAME}"

echo "Creating macOS App Bundle structure..."
mkdir -p "${MACOS_DIR}"
mkdir -p "${RESOURCES_DIR}"

# 1. Compile high-resolution icns file from LogoBetaNewer.png (1000x1000)
echo "Generating macOS Retina-compatible high-resolution app icon..."
if [ -f "$ICON_SOURCE" ]; then
    ICONSET="CanReg5.iconset"
    mkdir -p "$ICONSET"
    
    sips -z 16 16     "$ICON_SOURCE" --out "$ICONSET/icon_16x16.png" >/dev/null 2>&1
    sips -z 32 32     "$ICON_SOURCE" --out "$ICONSET/icon_16x16@2x.png" >/dev/null 2>&1
    sips -z 32 32     "$ICON_SOURCE" --out "$ICONSET/icon_32x32.png" >/dev/null 2>&1
    sips -z 64 64     "$ICON_SOURCE" --out "$ICONSET/icon_32x32@2x.png" >/dev/null 2>&1
    sips -z 128 128   "$ICON_SOURCE" --out "$ICONSET/icon_128x128.png" >/dev/null 2>&1
    sips -z 256 256   "$ICON_SOURCE" --out "$ICONSET/icon_128x128@2x.png" >/dev/null 2>&1
    sips -z 256 256   "$ICON_SOURCE" --out "$ICONSET/icon_256x256.png" >/dev/null 2>&1
    sips -z 512 512   "$ICON_SOURCE" --out "$ICONSET/icon_256x256@2x.png" >/dev/null 2>&1
    sips -z 512 512   "$ICON_SOURCE" --out "$ICONSET/icon_512x512.png" >/dev/null 2>&1
    sips -z 1024 1024 "$ICON_SOURCE" --out "$ICONSET/icon_512x512@2x.png" >/dev/null 2>&1
    
    iconutil -c icns "$ICONSET" -o "${RESOURCES_DIR}/CanReg5.icns"
    rm -rf "$ICONSET"
    echo "App icon generated successfully."
else
    echo "Warning: LogoBetaNewer.png not found. App bundle will use default system icon."
fi

# 2. Create Info.plist
echo "Writing Info.plist..."
cat << 'EOF' > "${CONTENTS_DIR}/Info.plist"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>English</string>
    <key>CFBundleExecutable</key>
    <string>CanReg5</string>
    <key>CFBundleIconFile</key>
    <string>CanReg5.icns</string>
    <key>CFBundleIdentifier</key>
    <string>canreg.client.CanReg5</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>CanReg5</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>5.00.44k</string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.9</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# 3. Create the launcher bash script inside MacOS/
echo "Writing Launcher script..."
cat << 'EOF' > "${MACOS_DIR}/CanReg5"
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Navigate to the self-contained Resources directory inside the .app bundle
cd "$DIR/../Resources"

# Add standard macOS Homebrew and JDK binary paths to PATH (GUI launches do not inherit shell profiles)
export PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:/usr/local/bin:/usr/local/opt/openjdk/bin:$PATH"

JAVA_CMD="java"

# Fallback to search standard macOS JVM paths if not found on updated PATH
if ! "$JAVA_CMD" -version &>/dev/null; then
    JVM_JAVA=$(ls -d /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java 2>/dev/null | tail -n 1)
    if [ -x "$JVM_JAVA" ]; then
        JAVA_CMD="$JVM_JAVA"
    fi
fi

# Double check if Java is resolved and runnable
if ! "$JAVA_CMD" -version &>/dev/null; then
    osascript -e 'display alert "CanReg5 Launcher Error" message "Unable to locate a Java Runtime.\n\nPlease ensure Java is installed on your Mac."' as critical
    exit 1
fi

if [ -f "CanReg.jar" ]; then
    "$JAVA_CMD" -cp "CanReg.jar:lib/*" canreg.client.CanRegClientApp
else
    # Show user-friendly macOS native error dialog if JAR is missing
    osascript -e 'display alert "CanReg5 Launcher Error" message "Could not find CanReg.jar inside the App Bundle.\n\nPlease rebuild the package using '\''ant jar'\''."' as critical
fi
EOF

# 4. Copy all compiled application resources inside the App Bundle to make it 100% self-contained
echo "Copying assets into App Bundle Resources..."
if [ -f "dist/CanReg.jar" ]; then
    cp "dist/CanReg.jar" "${RESOURCES_DIR}/"
fi
if [ -d "dist/lib" ]; then
    cp -R "dist/lib" "${RESOURCES_DIR}/"
fi
if [ -d "conf" ]; then
    cp -R "conf" "${RESOURCES_DIR}/"
fi
if [ -d "demo" ]; then
    cp -R "demo" "${RESOURCES_DIR}/"
fi
if [ -d "scripts" ]; then
    cp -R "scripts" "${RESOURCES_DIR}/"
fi

# 5. Make it executable and apply ad-hoc code signature
chmod +x "${MACOS_DIR}/CanReg5"

echo "Applying ad-hoc code signature..."
if command -v codesign &>/dev/null; then
    codesign --force --deep --sign - "${APP_NAME}"
    echo "Ad-hoc code signature applied successfully."
else
    echo "Warning: codesign utility not found. App bundle signature was skipped."
fi

echo "macOS App Bundle CanReg5.app created successfully!"
