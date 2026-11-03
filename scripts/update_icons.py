#!/usr/bin/env python3
"""
update_icons.py
Updates all application icons across macOS, Windows, Linux, and Web from the official LogoBetaNewer.png (1000x1000).
"""
import os
import shutil
import struct
import subprocess
import tempfile

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OFFICIAL_LOGO = os.path.join(BASE_DIR, "src", "canreg", "client", "gui", "resources", "LogoBetaNewer.png")

if not os.path.exists(OFFICIAL_LOGO):
    raise FileNotFoundError(f"Official logo not found at {OFFICIAL_LOGO}")

print(f"Generating icons from official logo: {OFFICIAL_LOGO}")

# 1. Generate multi-resolution Windows .ico
sizes_ico = [16, 24, 32, 48, 64, 128, 256]
with tempfile.TemporaryDirectory() as tmpdir:
    images_data = []
    for s in sizes_ico:
        png_path = os.path.join(tmpdir, f"icon_{s}.png")
        subprocess.check_call(["sips", "-z", str(s), str(s), OFFICIAL_LOGO, "--out", png_path], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        with open(png_path, "rb") as f:
            images_data.append((s, f.read()))
    
    header = struct.pack("<HHH", 0, 1, len(images_data))
    offset = 6 + len(images_data) * 16
    entries = []
    for s, data in images_data:
        w = 0 if s == 256 else s
        h = 0 if s == 256 else s
        entry = struct.pack("<BBBBHHII", w, h, 0, 0, 1, 32, len(data), offset)
        entries.append(entry)
        offset += len(data)
    
    ico_bytes = header + b"".join(entries) + b"".join(d for _, d in images_data)

    ico_destinations = [
        os.path.join(BASE_DIR, "src", "canreg", "client", "gui", "resources", "LogoBetaNew64x64.ico"),
        os.path.join(BASE_DIR, "conf", "LogoBetaNew64x64.ico"),
        os.path.join(BASE_DIR, "conf", "tables", "r", "shiny", "www", "LogoBetaNew64x64.ico"),
        os.path.join(BASE_DIR, "conf", "tables", "r", "shiny", "www", "favicon.ico"),
        os.path.join(BASE_DIR, "conf", "tables", "r", "shiny", "www", "default.ico"),
    ]
    for dest in ico_destinations:
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        with open(dest, "wb") as f:
            f.write(ico_bytes)
        print(f"Updated ICO: {os.path.relpath(dest, BASE_DIR)}")

# 2. Update resized PNGs
png_targets = [
    (os.path.join(BASE_DIR, "src", "canreg", "client", "gui", "resources", "LogoBetaNew16x16.png"), 16),
    (os.path.join(BASE_DIR, "src", "canreg", "client", "gui", "resources", "LogoBetaNew22x22.png"), 22),
    (os.path.join(BASE_DIR, "src", "canreg", "client", "gui", "resources", "LogoBetaNew32x32.png"), 32),
    (os.path.join(BASE_DIR, "src", "canreg", "client", "gui", "resources", "LogoBetaNew64x64.png"), 64),
    (os.path.join(BASE_DIR, "src", "canreg", "server", "resources", "LogoBetaNew32x32.png"), 32),
    (os.path.join(BASE_DIR, "jdeploy-bundle", "icon.png"), 512),
    (os.path.join(BASE_DIR, "dist", "icon.png"), 512),
    (os.path.join(BASE_DIR, "jdeploy", ".jdeploy-files", "icon.png"), 512),
]

for dest, sz in png_targets:
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    subprocess.check_call(["sips", "-z", str(sz), str(sz), OFFICIAL_LOGO, "--out", dest], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    print(f"Updated PNG ({sz}x{sz}): {os.path.relpath(dest, BASE_DIR)}")

# Also copy high-res LogoBetaNewer.png to doc if exists
doc_logo = os.path.join(BASE_DIR, "doc", "CanReg5-Instructions", "LogoBetaNewer.png")
if os.path.exists(os.path.dirname(doc_logo)):
    shutil.copy2(OFFICIAL_LOGO, doc_logo)
    print(f"Updated Doc Logo: {os.path.relpath(doc_logo, BASE_DIR)}")

# 3. Regenerate macOS App icon (.icns)
print("Regenerating macOS icon (CanReg5.icns)...")
iconset_dir = os.path.join(BASE_DIR, "CanReg5.iconset")
os.makedirs(iconset_dir, exist_ok=True)
icns_sizes = [
    (16, "icon_16x16.png"),
    (32, "icon_16x16@2x.png"),
    (32, "icon_32x32.png"),
    (64, "icon_32x32@2x.png"),
    (128, "icon_128x128.png"),
    (256, "icon_128x128@2x.png"),
    (256, "icon_256x256.png"),
    (512, "icon_256x256@2x.png"),
    (512, "icon_512x512.png"),
    (1024, "icon_512x512@2x.png"),
]
for sz, fname in icns_sizes:
    subprocess.check_call(["sips", "-z", str(sz), str(sz), OFFICIAL_LOGO, "--out", os.path.join(iconset_dir, fname)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

mac_icns = os.path.join(BASE_DIR, "CanReg5.app", "Contents", "Resources", "CanReg5.icns")
if os.path.exists(os.path.dirname(mac_icns)):
    subprocess.check_call(["iconutil", "-c", "icns", iconset_dir, "-o", mac_icns], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    print(f"Updated macOS ICNS: {os.path.relpath(mac_icns, BASE_DIR)}")
shutil.rmtree(iconset_dir, ignore_errors=True)

print("=== Icon update complete across all platforms! ===")

