# CanReg5

CanReg5 is a multi-user, multi-platform, open-source tool for cancer registries to input, store, check, and analyse cancer registry data.

## Description

Cancer registries need a tool to input, store, check, and analyse their data. If these data are coded and verified in a standard way, it facilitates the production of comparable analyses across registry populations worldwide. The main goal of the CanReg5 project is to provide a flexible and easy-to-use tool to accomplish these objectives.

Key features include:
- **Data Entry & Management:** Configurable database structures, dictionary-driven fields, patient deduplication, and holding database workflows.
- **Quality Control & Consistency Checks:** Automated validation rules, morphology/topography cross-checks, and duplicate record detection.
- **Analysis & Reporting:** Built-in frequency tables, survival analysis, population pyramids, and integrated R analysis (Table Builder) powered by the `Rcan` package.
- **Completeness Analyzer:** Offline browser-based interactive dashboard to inspect registry completeness and trends by source over time.
- **Modern Cross-Platform UI:** FlatLaf 3.x Look & Feel theming (including Light, Dark, IntelliJ, Darcula, macOS Light/Dark, Solarized, and High Contrast themes) with live font and theme switching, plus native macOS menu bar and desktop integration.
- **Internationalization:** Multi-language interface available in English, French, Spanish, Portuguese, Russian, Turkish, Georgian, and Chinese.

## Documentation

- **User Instructions & Handbook:** Browse the [CanReg5 User Instructions](doc/instructions/README.md) directly on GitHub (or download [`doc/CanReg5-Instructions.pdf`](doc/CanReg5-Instructions.pdf) / [`doc/CanReg5-Instructions.html`](doc/CanReg5-Instructions.html)).
- **Analysis & R Specification:** Read the [CanReg Analysis & R Whitepaper](doc/CanReg5-Analysis/README.md).
- **Java 25 Migration & Architecture Notes:** See [`doc/java25-migration.md`](doc/java25-migration.md) for details on the Java 25 LTS upgrade, custom `jlink` runtimes, and dependency updates.
- **Future Improvements Roadmap:** Explore the [CanReg5 Future Roadmap](doc/CanReg5-Future/README.md).
- **Changelog:** View the complete version history in [`changelog.txt`](changelog.txt) or formatted [HTML](changelog.html).

---

## Download & Installers

Pre-built binaries, self-contained installers, and portable distributions are available on the [GitHub Releases page](https://github.com/IARC-CSU/CanReg5/releases/latest).

### Package Options

| Platform | Download Asset | Included Runtime(s) | Description |
| :--- | :--- | :--- | :--- |
| **Windows** *(Recommended)* | `CanReg5-with-R-Setup.zip` *(contains `CanReg5-with-R-Setup.exe`)* | **Java 25 (`jlink`) + 64-bit R** | **Full Windows Installer.** Self-contained installer bundling a custom Java 25 runtime, an embedded standalone 64-bit R runtime, and pre-compiled R packages (`Rcan`, `ggplot2`, `shiny`, etc.). **Zero configuration required**—no internet connection, separate Java, or separate R installation needed. |
| **Windows** | `CanReg5-Setup.zip` *(contains `CanReg5-Setup.exe`)* | **Java 25 (`jlink`)** | **Standard Windows Installer.** Includes the bundled Java 25 runtime with a native 64-bit launcher; requires no separate Java installation and uses an existing local R installation if R-based Table Builder reports are needed. |
| **Windows** | `CanReg5-Windows-with-R.zip` | **64-bit R** *(uses system Java 25+)* | **Portable Windows Archive with R.** Unzip and run `CanReg.exe` or `CanReg.bat` without installation; includes the embedded `r-runtime/` and pre-downloaded R packages. Requires Java 25+ on the host system (or an optional local `jre/` folder). |
| **Windows** | `CanReg5-Windows.zip` | *Lightweight (none)* | **Portable Windows Archive.** Lightweight zip with 64-bit `CanReg.exe` and `CanReg.bat`. Requires Java 25+ on the host system and optional local R. |
| **macOS** *(Recommended)* | `CanReg5-macOS.dmg` | **Java 25 (`jlink`)** | **Self-Contained macOS Disk Image.** Drag `CanReg5.app` to `/Applications` and launch—includes the custom Java 25 runtime (`Contents/Resources/jre`), so no separate Java installation is required. |
| **macOS** | `CanReg5-macOS.zip` | *Lightweight (none)* | **Portable macOS Application Bundle.** Lightweight `CanReg5.app` archive without bundled Java. Extract and move to `/Applications`; requires Java 25+ installed on the Mac. |
| **Linux** | `CanReg5-Linux.zip` | *Lightweight (none)* | **Portable Linux Archive.** Unzip and launch via `./CanReg.sh`. Requires Java 25+ on the host system (or a local `./jre` directory) and optional local R. |
| **Offline R Packages** | `CanReg5-R-packages.zip` | — | **Offline R Packages Bundle.** Pre-downloaded Windows binary packages (`.zip`) and source packages (`.tar.gz`) for air-gapped or restricted network environments. |
| **Translations** | `translations.zip` | — | Language property bundles for translators and localized deployments. |
| **Documentation** | `CanReg5-Instructions.pdf`, `changelog.html`, `changelog.txt` | — | User instructions handbook and release notes. |

> **System Requirements:** Pre-built installers with a bundled `jlink` runtime (`CanReg5-Setup.zip`, `CanReg5-with-R-Setup.zip`, `CanReg5-macOS.dmg`) require a 64-bit OS and need no separate Java installation. Portable `.zip` packages require **Java 25 (LTS) or later**.

### R Integration & Offline Environments

CanReg5 uses R for advanced statistical reports (incidence tables, age-standardized rates, child cancer incidence, and population pyramids).
- **Automatic Package Isolation:** CanReg5 isolates its R user packages in a dedicated, version-specific directory (`CanReg5/r-library/<R.major>.<R.minor>/`) to prevent conflicts with other software or older R libraries.
- **Offline Package Installation:** If an internet connection is unavailable, CanReg5 automatically searches for pre-downloaded package archives in `conf/tables/r/r-packages/` (or from `CanReg5-R-packages.zip`) and installs them locally without needing network access.

---

## Development and Build Instructions

CanReg5 is built using **Apache Ant** with a classic NetBeans project structure.

### Prerequisites
- **Java Development Kit (JDK):** **Java 25 (LTS)** (including `jlink`) is required to compile and package CanReg5.
- **Apache Ant:** Installed and available on your system `PATH`.
- **Inno Setup 6 & PowerShell / .NET 64-bit `csc.exe`** *(Optional, Windows only)*: Required to compile the 64-bit `CanReg.exe` launcher and build `CanReg5-Setup.exe` / `CanReg5-with-R-Setup.exe`.
- **macOS (`hdiutil`, `sips`, `codesign`)** *(Optional, macOS only)*: Built-in macOS utilities used to generate icons, bundle `CanReg5.app`, and create `CanReg5-macOS.dmg`.
- **Python 3 & Pandoc** *(Optional)*: Required for generating the instructions PDF (`helpers/build_instructions_pdf.py`) and formatted changelogs.

### Common Build Targets

You can execute these standard Ant commands in the root of the project:

| Command | Action |
| :--- | :--- |
| `ant clean` | Deletes build artifacts and the `dist/` directory. |
| `ant compile` | Compiles Java source files into `build/classes/`. |
| `ant jar` | Compiles and packages `dist/CanReg.jar`, copies libraries into `dist/lib/`, updates version numbers, and builds lightweight portable archives in `dist/web/`. |
| `ant test` | Compiles and runs the full JUnit 4 headless test suite (`ant test -Drun.jvmargs="-Djava.awt.headless=true"`). |
| `ant build-jlink-jre` | Builds the custom slim Java 25 runtime in `dist/jre/` using `jlink`. |
| `ant build-mac-dmg` | Builds `dist/jre/` and packages the self-contained `dist/web/CanReg5-macOS.dmg` (macOS only). |
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
2. `dist/web/CanReg5-Windows.zip`: Lightweight Windows portable zip.
3. `dist/web/CanReg5-Linux.zip`: Lightweight Linux portable zip with `./CanReg.sh`.
4. `dist/web/CanReg5-macOS.zip`: Lightweight macOS `CanReg5.app` application bundle without bundled Java (when built on macOS).
5. `dist/web/CanReg5-R-packages.zip`: Pre-downloaded R packages archive.

#### Building macOS App Bundle & Disk Image
- **Lightweight ZIP (`CanReg5-macOS.zip`):** Built automatically by `ant jar` on macOS (runs `helpers/create-mac-app.sh` with `BUNDLE_JRE=false`).
- **Self-Contained DMG (`CanReg5-macOS.dmg`):** Run `ant build-mac-dmg` on macOS to generate `dist/jre/` via `jlink` and package `CanReg5.app` (with `Contents/Resources/jre` and an `/Applications` drag-and-drop shortcut) using `helpers/create-mac-dmg.sh`.

#### Building Native Windows Installers
1. Build the 64-bit launcher executable (`CanReg.exe`):
   ```powershell
   ./helpers/windows-launcher/build-launcher.ps1
   ```
2. Generate the custom Java 25 runtime (`dist\jre`):
   ```cmd
   ant build-jlink-jre
   ```
3. Compile the Inno Setup installers in `helpers/InnoSetup/`:
   - **Standard Installer:** `ISCC.exe /DCanReg5Dir="." /DJreSourceDir="dist\jre" helpers\InnoSetup\CanReg5-InnoSetup.iss`
   - **With-R Installer:** `ISCC.exe /DCanReg5Dir="." /DJreSourceDir="dist\jre" /DRSourceDir=".\r-runtime" helpers\InnoSetup\CanReg5-with-R-InnoSetup.iss`

#### CI/CD Release Automation
Automated builds and releases are managed via GitHub Actions (`.github/workflows/release.yml`):
- Pushing a version tag (e.g. `v5.00.46BETA11`) triggers multi-platform workflows provisioned with **JDK 25** on macOS and Windows runners.
- Builds the custom `jlink` Java 25 runtime, compiles the 64-bit Windows `CanReg.exe` launcher, and generates both `CanReg5-macOS.zip` and self-contained `CanReg5-macOS.dmg` on macOS.
- Downloads and packages offline R packages from CRAN and Posit Public Package Manager, compiles `doc/CanReg5-Instructions.pdf` and changelog formats, and produces both Windows Inno Setup installers (`CanReg5-Setup.zip`, `CanReg5-with-R-Setup.zip`).
- Attaches all installers, disk images, zip archives, and documentation assets to the GitHub Release.

---

## Installation for Developers

CanReg5 was developed with NetBeans IDE with Swing Application Framework support and targets **JDK 25 (LTS)**.
Before building the project in an IDE, ensure `appinfo.properties` exists in `/src/canreg/client` and `/src`.

Build and run the project. The CanReg5 main window will appear:
1. On the first run, click **"Add a new system"** and import a system definition (`.xml` file, e.g. from `systems/` or `demo/`).
2. Next, select the system and log in using the default supervisor credentials:
   - **Username:** `morten`
   - **Password:** `ervik`
3. After logging in, you can configure users, tables, variables, and dictionaries via the **Management** menu.

### Developer Troubleshooting
- **Missing Database Dependency:** In `pom.xml`, verify that Derby dependencies (10.17.1.0 modules including `derbyshared` and `derbyclient`) do not have `<scope>test</scope>` if building via Maven.
- **XML APIs:** If encountering XML parser issues, ensure `xml-apis` version 1.4.01 and `xml-apis-ext` version 1.3.04 are referenced rather than older versions.
- **NetBeans XML namespaces:** If NetBeans fails to recognize the project, check `nbproject/project.xml` for `http://` URLs in namespace definitions and replace with `https://`.
- **Forcing NetBeans JDK:** If NetBeans cannot detect JDK 25, configure `netbeans_jdkhome` in `\netbeans\etc\netbeans.conf` (e.g. `netbeans_jdkhome="C:\Program Files\Java\jdk-25"`).
