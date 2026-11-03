#!/usr/bin/env bash
# build_r_bundle.sh
# Prepares portable R for CanReg5 using innoextract or by packaging pre-installed library packages.
set -euo pipefail

R_VERSION="${1:-4.3.3}"
OUTPUT_DIR="${2:-r-runtime}"

echo "=== CanReg5 Portable R Bundle Builder (R ${R_VERSION}) ==="

if command -v innoextract >/dev/null 2>&1; then
    INSTALLER_URL="https://cran.r-project.org/bin/windows/base/old/R-${R_VERSION}-win.exe"
    TMP_DIR="$(mktemp -d)"
    trap 'rm -rf "${TMP_DIR}"' EXIT

    echo "Downloading Windows R installer from ${INSTALLER_URL}..."
    curl -sSL -o "${TMP_DIR}/R-win.exe" "${INSTALLER_URL}"

    echo "Extracting portable R with innoextract..."
    mkdir -p "${TMP_DIR}/extracted"
    innoextract -e -d "${TMP_DIR}/extracted" "${TMP_DIR}/R-win.exe"

    mkdir -p "${OUTPUT_DIR}"
    cp -R "${TMP_DIR}/extracted/app/"* "${OUTPUT_DIR}/"

    # Clean up docs
    rm -rf "${OUTPUT_DIR}/doc/manual" "${OUTPUT_DIR}/tests"

    echo "Configuring Rprofile.site..."
    cat <<'EOF' >> "${OUTPUT_DIR}/etc/Rprofile.site"

# CanReg5 Portable R configuration
options(repos = c(CRAN = 'https://cran.r-project.org'))
options(pkgType = 'win.binary')
EOF

    echo "=== Portable R extracted to ${OUTPUT_DIR} ==="
else
    echo "Notice: innoextract is not installed. To build the Windows portable bundle on macOS/Linux:"
    echo "  brew install innoextract (macOS) or apt-get install innoextract (Ubuntu)"
    echo "Or run scripts/build_r_bundle.ps1 on Windows / Windows GitHub runner."
fi

