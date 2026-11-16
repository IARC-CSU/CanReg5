# Upgrading CanReg5 from Java 8 to Java 25 (LTS)

## Overview

This document explains the rationale, technical changes, and deployment considerations for upgrading CanReg5 from **Java 8 (1.8)** to **Java 25 (LTS)** on the `feature/java25` branch.

While CanReg5 has long targeted Java 8 for broad compatibility with older registry machines, the desktop, security, and library ecosystem has moved forward. Moving to Java 25 LTS modernizes the platform while requiring minimal changes to the application source code.

---

## Why Upgrade to Java 25 LTS?

### 1. Fewer Support Problems in the Field
- **Self-contained runtimes (`jlink` / `jpackage`):** Java 9+ enables building custom, minimal Java runtimes with `jlink` and native installers (`.msi`, `.dmg`/`.pkg`, `.deb`) with `jpackage`. Bundling the exact tested runtime removes "which Java version is installed?" support issues on registry workstations and eliminates the prerequisite for users to install Java separately.
- **Helpful `NullPointerException` diagnostics (JEP 358):** Since Java 14, `NullPointerException` stack traces specify exactly which variable or expression was `null`, allowing bug reports and logs submitted by cancer registries to pinpoint failures immediately.
- **Built-in production profiling:** Java Flight Recorder (JFR) is included at no cost and with negligible overhead (Java 11+), making it far easier to diagnose memory leaks or performance bottlenecks in large databases.

### 2. Display Scaling & Internationalization
- **High-DPI Swing scaling:** Java 8 Swing applications frequently appear tiny or blurry on modern high-resolution Windows and Linux laptops. Java 9+ automatically scales Swing interfaces to match OS display scaling.
- **UTF-8 by default (JEP 400):** On Windows, Java 8 defaults to the local Windows code page when reading or writing files without an explicit encoding—a common cause of corrupted patient names or characters in CSV imports and exports. Since Java 18, UTF-8 is the default charset across all operating systems.
- **Modern Unicode and locale data:** Newer CLDR/Unicode locale rules improve formatting and collation across non-Latin and internationalized deployments (e.g., Thai, Arabic, Chinese, French, Spanish).
- **Native macOS integration & Apple Silicon:** Standard `java.awt.Desktop` APIs handle native About, Preferences, and Quit menu actions, and modern JDKs run natively on Apple Silicon (`arm64`) without Rosetta emulation.

### 3. Security & Ecosystem Longevity
- **Long-term support horizon:** Java 25 is a Long-Term Support (LTS) release with security updates extending into the 2030s.
- **Modern security defaults:** Up-to-date TLS 1.3 cipher suites, root CA certificates, and the standard `java.net.http.HttpClient` API for GitHub version checks and release lookups.
- **Unblocking dependency upgrades:** Maintaining Java 8 locks the project onto older libraries. Modern versions of key dependencies (such as Apache Derby 10.17+, FlatLaf 3.x for improved HiDPI theming, Jackson, and JUnit) require Java 11, 17, or 21+.

### 4. Performance & Memory Efficiency
- **Faster startup:** Modern JVM class-data sharing and ahead-of-time (AOT) class loading/linking caches reduce launch times for desktop workflows.
- **Improved memory management:** Decades of G1GC refinements and optional Compact Object Headers (introduced in Java 25) reduce memory overhead during large CSV imports, person searches, and ad-hoc database operations.
- **Faster execution:** General JIT compiler optimizations accelerate quality control checks and batch operations with no application code changes required.

### 5. Developer Productivity & Maintainability
- **Modern language ergonomics:** Moving past Java 8 enables `var`, `record` types, switch expressions, pattern matching, text blocks, immutable collection factories (`List.of`, `Map.of`), and convenience APIs (`String.isBlank()`, `Locale.of()`, `Files.readString()`).
- **Contributor onboarding:** Most Java developers and modern IDE/static-analysis tools expect Java 17+.

---

## Summary of Code & Build Changes

Because CanReg5 uses standard Swing, JDBC, and RMI APIs, compiling cleanly on Java 25 required very few code modifications:

### Removed Encapsulated JDK Internal Usage
- Removed `src/canreg/common/BuddhistCalendarCanReg.java`, an unused class that imported the encapsulated internal class `sun.util.BuddhistCalendar`.
- Updated the Javadoc in `test/canreg/common/CalendarCanRegTest.java` accordingly.

### Deprecated-for-Removal & Deprecated Standard API Updates
- **Security Subject Execution (`Subject.doAs` -> `Subject.callAs`):**
  - Updated `src/canreg/server/security/ValidateMethodCall.java` to implement `java.util.concurrent.Callable<Object>` with `call()` (retaining a `run()` delegating helper for compatibility).
  - Updated `src/canreg/server/CanRegServerProxy.java` to invoke `Subject.callAs(...)` and handle `CompletionException`.
- **Locale Construction:**
  - Replaced `new Locale(...)` constructors with `Locale.of(...)` and `Locale.ENGLISH` in `src/canreg/client/LocalSettings.java` and `src/canreg/server/database/CanRegDAO.java`.
- **URL & Process Execution:**
  - Replaced `new URL(String)` constructors with `URI.create(...).toURL()` / `File.toURI().toURL()` in `src/canreg/common/Tools.java` and `src/canreg/server/xml/Tools.java`.
  - Replaced single-string `Runtime.getRuntime().exec(String)` calls with argument-array `exec(String[])` overloads in `src/canreg/client/gui/tools/BareBonesBrowserLaunch.java` and `src/canreg/common/Tools.java`.
- **SAX Parser & Swing Key Mask:**
  - Replaced deprecated `XMLReaderFactory.createXMLReader()` with `SAXParserFactory` in `src/canreg/server/xml/MySAXApp.java`.
  - Replaced `InputEvent.CTRL_MASK` with `KeyEvent.CTRL_DOWN_MASK` in `src/canreg/client/gui/dataentry2/components/VariableEditorPanel.java`.

### Build, CI, and Project Mandate Updates
- Updated source/target version to `25` in:
  - `nbproject/project.properties` (`javac.source=25`, `javac.target=25`)
  - `nbproject/build-impl.xml` (`default.javac.source=25`, `default.javac.target=25`)
  - `pom.xml` (`maven.compiler.source`, `maven.compiler.target`, and compiler plugin config)
  - `package.json` (`jdeploy.javaVersion`: `"25"`)
- Updated `.github/workflows/release.yml` to provision JDK 25 (`java-version: '25'`) in both build jobs, compile the 64-bit `CanReg.exe` launcher, generate the custom `jlink` runtime, and bundle it in the Inno Setup installers.
- Updated `AGENTS.md` to reflect Java 25 (LTS) as the project language target.

### Custom `jlink` Runtime & Installer Packaging
- **Custom Java Runtime (`ant build-jlink-jre`):** Added the `build-jlink-jre` target in `build.xml` using `jlink` with only the JDK modules required by CanReg5 (`java.base`, `java.desktop`, `java.logging`, `java.management`, `java.naming`, `java.prefs`, `java.rmi`, `java.security.jgss`, `java.sql`, `java.sql.rowset`, `java.xml`, `java.xml.crypto`, `jdk.crypto.ec`, `jdk.localedata`, `jdk.unsupported`, `jdk.xml.dom`) plus `--strip-debug --no-man-pages --no-header-files --compress=zip-6`. This outputs a self-contained runtime at `dist/jre` (~59 MB uncompressed, ~30 MB compressed).
- **64-bit Windows Launcher (`helpers/windows-launcher/`):** Replaced the legacy 32-bit Launch4j wrapper dependencies with `CanRegLauncher.cs` and `build-launcher.ps1`, compiling a modern 64-bit GUI `CanReg.exe` using .NET's `csc.exe`. It sets the working directory to the installation root so relative paths (`./conf`, `./demo`, `./doc`, `./r-runtime`) resolve reliably and searches for Java in priority order: bundled `.\jre\bin\javaw.exe`, `.\runtime\bin\javaw.exe`, `%JAVA_HOME%`, common 64-bit JDK vendor folders, and `PATH`.
- **Cross-Platform Launcher Support:** Updated `CanReg.bat`, `CanReg.sh`, and `helpers/create-mac-app.sh` to prefer a bundled `./jre` runtime when present and fall back to a system Java 25+ installation.
- **Inno Setup & Portable Distributions:** Kept Inno Setup (`helpers/InnoSetup/CanReg5-InnoSetup.iss` and `CanReg5-with-R-InnoSetup.iss`) to preserve non-admin per-user install fallback, 7-language setup prompts, LZMA compression, and in-place upgrades over existing CanReg5 installations. Both `CanReg5-Setup.exe` and `CanReg5-with-R-Setup.exe` bundle `dist/jre` inside `{app}\jre`, while portable `.zip` distributions (`CanReg5-Windows.zip`, `CanReg5-Linux.zip`, `CanReg5-macOS.zip`) remain lightweight when built without a pre-generated `dist/jre`.

### Third-Party Library Upgrades
Moving to Java 25 unblocked upgrading the core third-party libraries in `lib/`, `pom.xml`, `nbproject/project.properties`, `CanReg5.iml`, and `manifest.mf`:
- **Apache Derby (`10.14.2.0` -> `10.17.1.0`):** Upgraded `derby`, `derbynet`, `derbytools`, and added the required `derbyshared` and `derbyclient` modules introduced in Derby 10.15+.
- **Apache Batik & XMLGraphics Commons (`1.6-1` / `1.14` mix -> unified `1.18` / `2.10`):** Eliminated the legacy mixed 1.6/1.14 Batik JARs in favor of unified `1.18` modules (`batik-svggen`, `batik-dom`, `batik-awt-util`, `batik-util`, `batik-constants`, `batik-css`, `batik-ext`, `batik-i18n`, `batik-shared-resources`, `batik-xml`, plus transitive `xml-apis-ext-1.3.04`) and `xmlgraphics-commons-2.10`.
- **FlatLaf (`1.3` -> `3.5.4`):** Upgraded `flatlaf` and `flatlaf-intellij-themes` for improved modern HiDPI Swing rendering.
- **Jackson (`2.12.3` -> `2.18.3`):** Upgraded `jackson-annotations`, `jackson-core`, and `jackson-databind`.
- **Apache Commons:** Upgraded `commons-csv` (`1.4` -> `1.12.0`), `commons-dbcp2` (`2.5.0` -> `2.13.0`), `commons-pool2` (`2.6.0` -> `2.12.1`), `commons-io` (`2.4` -> `2.18.0`), `commons-logging` (`1.2` -> `1.3.4`), plus transitive `commons-codec-1.17.1` and `jakarta.transaction-api-1.3.3` (`commons-lang 2.6` remains pinned to preserve `org.apache.commons.lang` APIs).
- **JFreeChart & JUniversalChardet:** Upgraded `jfreechart` (`1.5.3` -> `1.5.5`) and `juniversalchardet` (`1.0.3` -> `2.5.0`).

---

## Deployment & Rollout Considerations

1. **Bundled Java Runtime in Installers:** Bytecode compiled for Java 25 will not run on an external Java 8 JRE. Users running `CanReg5-Setup.exe` or `CanReg5-with-R-Setup.exe` automatically receive the bundled `jlink` runtime in `{app}\jre` without needing a separate Java installation, whereas portable `.zip` users need Java 25+ on the host system.
2. **64-Bit OS Requirement:** Modern JDK releases (Java 24+) no longer provide 32-bit Windows builds and require modern 64-bit operating systems.
3. **Coordinated Client/Server Upgrades:** In multi-user RMI setups, the server and all connected clients should be upgraded to the Java 25 release together to maintain compatible class/serialization versions.
