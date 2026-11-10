# CanReg5

CanReg5 is a multi-user, multi-platform, open-source tool for cancer registries to input, store, check, and analyse cancer registry data.

## Description

Cancer registries need a tool to input, store, check, and analyse their data. If these data are coded and verified in a standard way, it facilitates the production of comparable analyses across registry populations worldwide. The main goal of the CanReg5 project is to provide a flexible and easy-to-use tool to accomplish these objectives.

Key features include:
- **Data Entry & Management:** Configurable database structures, dictionary-driven fields, patient deduplication, and holding database workflows.
- **Quality Control & Consistency Checks:** Automated validation rules, morphology/topography cross-checks, and duplicate record detection.
- **Analysis & Reporting:** Built-in frequency tables, survival analysis, population pyramids, and integrated R analysis (Table Builder) powered by the `Rcan` package.
- **Completeness Analyzer:** Offline browser-based interactive dashboard to inspect registry completeness and trends by source over time.
- **Internationalization:** Multi-language interface available in English, French, Spanish, Portuguese, Russian, Turkish, Georgian, and Chinese.

## Documentation

- **User Instructions & Handbook:** Browse the [CanReg5 User Instructions](doc/instructions/README.md) directly on GitHub (or download [`doc/CanReg5-Instructions.pdf`](doc/CanReg5-Instructions.pdf) / [`doc/CanReg5-Instructions.html`](doc/CanReg5-Instructions.html)).
- **Analysis & R Specification:** Read the [CanReg Analysis & R Whitepaper](doc/CanReg5-Analysis/README.md).
- **Future Improvements Roadmap:** Explore the [CanReg5 Future Roadmap](doc/CanReg5-Future/README.md).
- **Changelog:** View the complete version history in [`changelog.txt`](changelog.txt) or formatted [HTML](changelog.html).

---

## Download & Installers

Pre-built binaries, installers, and platform distributions are available on the [GitHub Releases page](https://github.com/IARC-CSU/CanReg5/releases/latest).

### Package Options

| Platform | Download Asset | Description |
| :--- | :--- | :--- |
| **Windows** *(Recommended)* | `CanReg5-with-R-Setup.zip` | **Full Windows Installer with bundled R runtime.** Includes an embedded standalone 64-bit R runtime (v4.x) and all required pre-compiled R packages (`Rcan`, `ggplot2`, `shiny`, etc.). **Zero configuration required**—no internet connection or separate R installation needed. |
| **Windows** | `CanReg5-Setup.zip` | **Standard Windows Installer.** Lightweight installer for environments that already have R installed or do not require R-based Table Builder reports. |
| **Windows** | `CanReg5-Windows-with-R.zip` | **Portable Windows Archive with R.** Unzip and run `CanReg.bat` or `CanReg.exe` without installation; includes embedded `r-runtime/` and pre-downloaded R packages. |
| **Windows** | `CanReg5-Windows.zip` | **Portable Windows Archive.** Standard portable distribution (requires local Java 8 and optional local R). |
| **macOS** | `CanReg5-macOS.zip` | **macOS Application Bundle.** Contains the self-contained `CanReg5.app` bundle. Extract and move to `/Applications`. Compatible with macOS on Intel and Apple Silicon (via Rosetta 2 / Java 8). |
| **Linux** | `CanReg5-Linux.zip` | **Portable Linux Archive.** Unzip and launch via `./CanReg.sh`. Requires Java 8 and optional local R. |
| **Offline R Packages** | `CanReg5-R-packages.zip` | **Offline R Packages Bundle.** Pre-downloaded Windows binary packages (`.zip`) and source packages (`.tar.gz`) for air-gapped or restricted network environments. |
| **Translations** | `translations.zip` | Language property bundles for translators and localized deployments. |

### R Integration & Offline Environments

CanReg5 uses R for advanced statistical reports (incidence tables, age-standardized rates, child cancer incidence, and population pyramids).
- **Automatic Package Isolation:** CanReg5 isolates its R user packages in a dedicated, version-specific directory (`CanReg5/r-library/<R.major>.<R.minor>/`) to prevent conflicts with other software or older R libraries.
- **Offline Package Installation:** If an internet connection is unavailable, CanReg5 automatically searches for pre-downloaded package archives in `conf/tables/r/r-packages/` (or from `CanReg5-R-packages.zip`) and installs them locally without needing network access.

---

## Development and Build Instructions

CanReg5 is built using **Apache Ant** with a classic NetBeans project structure.

### Prerequisites
- **Java Development Kit (JDK):** Java 8 (1.8) is required for compatibility.
- **Apache Ant:** Installed and available on your system `PATH`.
- **Inno Setup 6** *(Optional, Windows only)*: Required to compile `CanReg5-Setup.exe` and `CanReg5-with-R-Setup.exe`.
- **Python 3 & Pandoc** *(Optional)*: Required for generating the instructions PDF (`helpers/build_instructions_pdf.py`) and formatted changelogs.

### Common Build Targets

You can execute these standard Ant commands in the root of the project:

| Command | Action |
| :--- | :--- |
| `ant clean` | Deletes build artifacts and the `dist/` directory. |
| `ant compile` | Compiles Java source files into `build/classes/`. |
| `ant jar` | Compiles and packages `dist/CanReg.jar`, copies libraries into `dist/lib/`, updates version numbers, and builds distribution archives in `dist/web/`. |
| `ant test` | Compiles and runs the full JUnit 4 headless test suite. |
| `ant build-instructions-pdf` | Compiles `doc/instructions/*.md` into `doc/CanReg5-Instructions.pdf` and standalone HTML. |
| `ant package-windows-with-r` | Builds `CanReg5-Windows-with-R.zip` bundling the standalone R runtime from `r-runtime/`. |

### Running the Application Locally

#### After building the JAR (`ant jar`):
You can run the desktop client directly:
```bash
java -jar dist/CanReg.jar
```

Or run with explicit classpath dependencies:
- **macOS / Linux:**
  ```bash
  java -cp "dist/CanReg.jar:lib/*" canreg.client.CanRegClientApp
  ```
- **Windows:**
  ```cmd
  java -cp "dist/CanReg.jar;lib/*" canreg.client.CanRegClientApp
  ```

### Packaging Distribution

When you execute `ant jar`, the post-compile packaging tasks generate:
1. `dist/translations.zip`: Localized properties for all supported languages.
2. `dist/web/CanReg5-Windows.zip`: Standard Windows portable zip.
3. `dist/web/CanReg5-Linux.zip`: Linux distribution with launcher script.
4. `dist/web/CanReg5-macOS.zip`: macOS `CanReg5.app` application bundle (when built on macOS).
5. `dist/web/CanReg5-R-packages.zip`: Pre-downloaded R packages archive.

#### Building Native Windows Installers
Windows Inno Setup installers are configured in `helpers/InnoSetup/`:
- **Standard Installer:** `ISCC.exe /DCanReg5Dir="." helpers\InnoSetup\CanReg5-InnoSetup.iss`
- **With-R Installer:** `ISCC.exe /DCanReg5Dir="." /DRSourceDir=".\r-runtime" helpers\InnoSetup\CanReg5-with-R-InnoSetup.iss`

#### CI/CD Release Automation
Automated builds and releases are managed via GitHub Actions (`.github/workflows/release.yml`):
- Pushing a version tag (e.g. `v5.00.46BETA9`) triggers multi-platform build workflows on macOS and Windows runners.
- Downloads and packages offline R packages from CRAN and Posit Public Package Manager.
- Automatically compiles `doc/CanReg5-Instructions.pdf` and changelog formats.
- Packages and attaches all installers and zip archives to the GitHub Release.

---

## Installation for Developers

CanReg5 was developed with NetBeans IDE with Swing Application Framework support. It uses JDK 1.8.
Before building the project in an IDE, ensure `appinfo.properties` exists in `/src/canreg/client` and `/src`.

Build and run the project. The CanReg5 main window will appear:
1. On the first run, click **"Add a new system"** and import a system definition (`.xml` file, e.g. from `systems/` or `demo/`).
2. Next, select the system and log in using the default supervisor credentials:
   - **Username:** `morten`
   - **Password:** `ervik`
3. After logging in, you can configure users, tables, variables, and dictionaries via the **Management** menu.

### Developer Troubleshooting
- **Missing Database Dependency:** In `pom.xml`, verify that Derby dependencies do not have `<scope>test</scope>` if building via Maven.
- **XML APIs:** If encountering XML parser issues, ensure `xml-apis` version 1.4.01 is referenced rather than older versions.
- **NetBeans XML namespaces:** If NetBeans fails to recognize the project, check `nbproject/project.xml` for `http://` URLs in namespace definitions and replace with `https://`.
- **Forcing NetBeans JDK:** If NetBeans cannot detect JDK 8, configure `netbeans_jdkhome` in `\netbeans\etc\netbeans.conf` (e.g. `netbeans_jdkhome="C:\Program Files\Java\jdk1.8.0_201"`).
