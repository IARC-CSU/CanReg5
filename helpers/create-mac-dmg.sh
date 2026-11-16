#!/bin/bash
# create-mac-dmg.sh: Packages CanReg5.app with the custom jlink Java 25 runtime into a macOS .dmg disk image.
set -e

APP_NAME="CanReg5.app"
STAGING_DIR="build/dmg-staging"
STAGING_APP="${STAGING_DIR}/${APP_NAME}"
OUTPUT_DMG="dist/web/CanReg5-macOS.dmg"

if [ ! -d "dist/jre" ]; then
    echo "Error: Custom Java runtime not found at dist/jre." >&2
    echo "Please run 'ant build-jlink-jre' (or 'ant build-mac-dmg') first." >&2
    exit 1
fi

if [ ! -d "${APP_NAME}" ]; then
    echo "${APP_NAME} not found; building lightweight app bundle first..."
    BUNDLE_JRE=false /bin/bash helpers/create-mac-app.sh
fi

trap 'rm -rf "${STAGING_DIR}"' EXIT

echo "Staging self-contained macOS app bundle with bundled Java 25 runtime..."
rm -rf "${STAGING_DIR}"
mkdir -p "${STAGING_DIR}"

cp -R "${APP_NAME}" "${STAGING_APP}"
rm -rf "${STAGING_APP}/Contents/Resources/jre"
cp -R "dist/jre" "${STAGING_APP}/Contents/Resources/"

chmod +x "${STAGING_APP}/Contents/MacOS/CanReg5"
chmod +x "${STAGING_APP}/Contents/Resources/jre/bin/"* 2>/dev/null || true
if [ -f "${STAGING_APP}/Contents/Resources/jre/lib/jspawnhelper" ]; then
    chmod +x "${STAGING_APP}/Contents/Resources/jre/lib/jspawnhelper"
fi

echo "Applying ad-hoc code signature to staged app bundle..."
if command -v codesign &>/dev/null; then
    codesign --force --deep --sign - "${STAGING_APP}"
else
    echo "Warning: codesign utility not found. Staged app bundle signature was skipped."
fi

ln -s /Applications "${STAGING_DIR}/Applications"

echo "Creating ${OUTPUT_DMG}..."
mkdir -p dist/web
rm -f "${OUTPUT_DMG}"
hdiutil create -volname "CanReg5" -srcfolder "${STAGING_DIR}" -ov -format UDZO "${OUTPUT_DMG}"

rm -rf "${STAGING_DIR}"
echo "macOS DMG created successfully at ${OUTPUT_DMG}"
