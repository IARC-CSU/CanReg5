#!/bin/bash
# CanReg5 macOS/Linux ZIP distribution packaging script
set -e

echo "Building zip-distribution on macOS/Linux..."

# 1. Clean up old packaging assets and regenerate macOS App Bundle on macOS
rm -rf translations || true
rm -f CanReg5.zip CanReg5-R-packages.zip translations.zip CanReg5-macOS.zip || true
rm -rf dist/web || true

if [ "$(uname)" = "Darwin" ]; then
    echo "Regenerating and signing macOS App Bundle..."
    ./helpers/create-mac-app.sh
fi

# 2. Extract translation properties via rsync
mkdir -p translations
rsync -Rtrv --include "*/" --include "*.properties" --exclude "*" ./src/canreg/client/ ./translations/

# 3. Zip translation sources
if [ -d "translations/src" ]; then
    zip -r translations.zip translations/src
else
    zip -r translations.zip translations
fi

# 4. Pack core directories and launchers, excluding full R packages and CanReg5.app
zip -r CanReg5.zip conf demo scripts CanReg.sh -x "conf/tables/r/r-packages/*"

# 5. Pack R package tarballs only
zip -g CanReg5.zip conf/tables/r/r-packages/*.tar.gz || true

# 6. Include documentation & changelogs
if [ -f "doc/CanReg5-Instructions/CanReg5-Instructions.pdf" ]; then
    cp doc/CanReg5-Instructions/CanReg5-Instructions.pdf doc/ || true
    zip -g CanReg5.zip doc/CanReg5-Instructions.pdf || true
fi
zip -g CanReg5.zip changelog.txt changelog.html || true

# 7. Create separate R packages zip
zip -r CanReg5-R-packages.zip conf/tables/r/r-packages

# 7b. Create separate self-contained macOS bundle distribution zip
if [ "$(uname)" = "Darwin" ]; then
    echo "Creating dedicated macOS App Bundle ZIP..."
    rm -f CanReg5-macOS.zip
    zip -r CanReg5-macOS.zip CanReg5.app
fi

# 8. Zip up dist output and structure the web directory
cd dist
zip -r ../CanReg5.zip .
mkdir -p web
cp ../changelog.txt web/ || true
cp ../changelog.html web/ || true
cp ../version.txt web/ || true
if [ -f "../doc/CanReg5-Instructions/CanReg5-Instructions.pdf" ]; then
    cp ../doc/CanReg5-Instructions/CanReg5-Instructions.pdf web/ || true
fi
mv ../CanReg5.zip web/ || true
mv ../CanReg5-R-packages.zip web/ || true
mv ../CanReg5-macOS.zip web/ || true
mv ../translations.zip ./ || true
cd ..

echo "macOS/Linux ZIP packaging completed successfully!"
