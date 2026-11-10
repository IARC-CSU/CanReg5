#!/usr/bin/env python3
"""
build_instructions_pdf.py
Compiles the Markdown instructions in doc/instructions/ into doc/CanReg5-Instructions.pdf and doc/CanReg5-Instructions.html.
Injects CanReg version and Logo on the first page of both HTML and PDF.
"""

import os
import re
import shutil
import subprocess
import sys
import tempfile

DOC_DIR = "doc/instructions"
OUTPUT_PDF = "doc/CanReg5-Instructions.pdf"
OUTPUT_HTML = "doc/CanReg5-Instructions.html"
LOGO_FILE = os.path.abspath(os.path.join(DOC_DIR, "images/LogoBetaNewer.png"))

def get_canreg_version():
    if os.path.exists("version.txt"):
        with open("version.txt", "r", encoding="utf-8") as f:
            v = f.read().strip()
            if v:
                return v
    for prop in ["appinfo.properties", "src/canreg/client/appinfo.properties"]:
        if os.path.exists(prop):
            with open(prop, "r", encoding="utf-8") as f:
                for line in f:
                    if line.startswith("program.VERSION="):
                        return line.split("=", 1)[1].strip()
    return "5.00.46"

def get_ordered_chapters():
    files = sorted([f for f in os.listdir(DOC_DIR) if f.endswith(".md") and f != "README.md"])
    return [os.path.join(DOC_DIR, f) for f in files]

def prepare_combined_markdown(chapter_files):
    combined_content = []
    
    for filepath in chapter_files:
        with open(filepath, "r", encoding="utf-8") as f:
            lines = f.readlines()

        # Strip web navigation header
        start = 0
        if len(lines) > 2 and lines[0].startswith("*[") and lines[2].startswith("---"):
            start = 3

        # Strip web navigation footer
        end = len(lines)
        if len(lines) > 2 and lines[-1].strip().startswith("*[") and lines[-3].startswith("---"):
            end = len(lines) - 3

        text = "".join(lines[start:end])

        # Convert lyxcode divs to markdown code blocks
        text = re.sub(r'<div class="lyxcode">\s*(.*?)\s*</div>', r'```\n\1\n```', text, flags=re.DOTALL)
        # Remove remaining HTML div containers
        text = re.sub(r'</?div[^>]*>', '', text)

        # Wrap Windows paths not already in backticks
        text = re.sub(r'(?<!`)([A-Za-z]:\\[A-Za-z0-9_.\-\\]+)(?!`)', r'`\1`', text)
        text = re.sub(r'(?<!`)(\\\\[A-Za-z0-9_.\-\\]+)(?!`)', r'`\1`', text)

        # Remove trailing \part artifacts if any
        text = re.sub(r'# Installing and running CanReg5\s*$', '', text)

        combined_content.append(text.strip())
        combined_content.append("\n\n\\newpage\n\n")

    return "\n\n".join(combined_content)

def build_pdf(combined_md_path, version):
    print(f"Building CanReg5-Instructions.pdf with pandoc (Version {version})...")

    # Generate custom LaTeX titlepage with logo and injected version
    titlepage_tex = f"""
\\begin{{titlepage}}
\\centering
\\vspace*{{1.5cm}}
\\includegraphics[width=0.7\\textwidth]{{{LOGO_FILE}}}\\par
\\vspace{{1.8cm}}
{{\\Huge\\bfseries CanReg5 Instructions \\& Handbook\\par}}
\\vspace{{0.6cm}}
{{\\Large\\bfseries Open Source Cancer Registry Software\\par}}
\\vspace{{1.5cm}}
{{\\Large\\textbf{{Version:}} {version}\\par}}
\\vspace{{0.6cm}}
{{\\large Morten Johannes Ervik\\par}}
{{\\large International Agency for Research on Cancer (IARC / WHO)\\par}}
\\vfill
{{\\large \\today\\par}}
\\end{{titlepage}}
"""
    tmp_title_tex = "/tmp/canreg_titlepage.tex"
    with open(tmp_title_tex, "w", encoding="utf-8") as f:
        f.write(titlepage_tex)

    cmd = [
        "pandoc", combined_md_path,
        "-o", OUTPUT_PDF,
        f"--resource-path={DOC_DIR}",
        "--pdf-engine=xelatex",
        "--toc",
        "--toc-depth=2",
        "--number-sections",
        "-B", tmp_title_tex,
        "-V", "geometry:margin=1in",
        "-V", "documentclass=report",
        "-V", "graphics=true",
        "-V", "linkcolor=blue",
        "-V", "urlcolor=blue",
        "-V", "toccolor=black",
    ]
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    if os.path.exists(tmp_title_tex):
        os.remove(tmp_title_tex)

    if result.returncode != 0:
        print(f"Error during PDF compilation:\n{result.stderr}")
        return False
        
    print(f"Successfully generated {OUTPUT_PDF} ({os.path.getsize(OUTPUT_PDF):,} bytes)")
    return True

def build_html(combined_md_path, version):
    print(f"Building standalone HTML manual CanReg5-Instructions.html (Version {version})...")

    # Generate custom HTML header with logo and injected version
    titlepage_html = f"""
<header id="title-block-header" style="text-align: center; margin-top: 2.5em; margin-bottom: 3.5em; padding-bottom: 2em; border-bottom: 1px solid #eaecef;">
  <img src="images/LogoBetaNewer.png" alt="CanReg5 Logo" style="max-width: 480px; width: 80%; height: auto; margin-bottom: 1.5em;" />
  <h1 style="margin: 0.3em 0; font-size: 2.4em;">CanReg5 Instructions &amp; Handbook</h1>
  <p style="font-size: 1.25em; color: #555; margin: 0.2em 0 1.5em 0;">Open Source Cancer Registry Software</p>
  <div style="font-size: 1.1em; line-height: 1.7; color: #333;">
    <p><strong>Version:</strong> {version}</p>
    <p><strong>Author:</strong> Morten Johannes Ervik</p>
    <p><strong>Publisher:</strong> International Agency for Research on Cancer (IARC / WHO)</p>
  </div>
</header>
"""
    tmp_title_html = "/tmp/canreg_titlepage.html"
    with open(tmp_title_html, "w", encoding="utf-8") as f:
        f.write(titlepage_html)

    cmd = [
        "pandoc", combined_md_path,
        "-o", OUTPUT_HTML,
        f"--resource-path={DOC_DIR}",
        "--toc",
        "--toc-depth=2",
        "--number-sections",
        "-B", tmp_title_html,
        "-s",
        "--metadata", f"pagetitle=CanReg5 Instructions & Handbook (Version {version})"
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if os.path.exists(tmp_title_html):
        os.remove(tmp_title_html)

    if result.returncode == 0:
        print(f"Successfully generated {OUTPUT_HTML} ({os.path.getsize(OUTPUT_HTML):,} bytes)")

def main():
    version = get_canreg_version()
    print(f"CanReg5 Version: {version}")
    chapters = get_ordered_chapters()
    if not chapters:
        print("No markdown chapters found in doc/instructions/")
        sys.exit(1)
        
    print(f"Found {len(chapters)} chapters to compile.")
    combined_md = prepare_combined_markdown(chapters)
    
    tmp_path = "/tmp/canreg5_combined_instructions.md"
    with open(tmp_path, "w", encoding="utf-8") as f:
        f.write(combined_md)
        
    success = build_pdf(tmp_path, version)
    build_html(tmp_path, version)
    
    if os.path.exists(tmp_path):
        os.remove(tmp_path)
        
    if not success:
        sys.exit(1)

if __name__ == "__main__":
    main()
