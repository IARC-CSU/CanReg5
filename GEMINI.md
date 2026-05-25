# Gemini CLI - CanReg5 Project Mandates

This document serves as the foundational guide for Gemini CLI when working on the CanReg5 project. These instructions take precedence over general defaults.

## Project Context
CanReg5 is a multi-user, multi-platform, open-source tool for cancer registries to input, store, check, and analyze data. It is a Java-based desktop application with a focus on user-friendliness and internationalization.

## Tech Stack
- **Language:** Java 8 (1.8)
- **Build System:** Apache Ant (NetBeans project structure)
- **Frameworks/Libraries:** 
  - Swing (GUI)
  - Apache Commons (IO, Lang, CSV, DBCP, Logging, Pool)
  - Apache Batik (SVG)
  - Apache Derby (Database)
  - JFreeChart (Charts)
  - iText (PDF generation)
  - Jackson (JSON)
- **Testing:** JUnit 4

## Coding Standards
- **Java 8 Compatibility:** Ensure all code changes are compatible with Java 8. Do not use features from later Java versions.
- **Naming Conventions:** Follow standard Java naming conventions (PascalCase for classes, camelCase for methods/variables).
- **Style:** Maintain consistency with the existing codebase (refer to `src/canreg`).
- **Internationalization:** Be mindful of multi-language support. String literals for UI should ideally be externalized (check `appinfo.properties` and other resource bundles).
- **NetBeans GUI Forms Sync:** Whenever modifying a Swing GUI layout, always update both the `.java` source code and its corresponding `.form` XML layout metadata file. This ensures compatibility with the NetBeans GUI Builder and prevents automatic design reversion on subsequent visual edits.

## Testing Strategy
- **Framework:** JUnit 4.
- **Test Location:** All tests should be placed in the `test/` directory, following the package structure of the source code.
- **Validation:** Always run existing tests before and after making changes.
- **New Tests:** For every bug fix or new feature, a corresponding JUnit test case must be added.

## Common Workflows
- **Build:** `ant jar` (via `run_shell_command`)
- **Test:** `ant test` (via `run_shell_command`)
- **Clean:** `ant clean`

## Critical Directories
- `src/canreg/`: Core application logic and GUI.
- `test/canreg/`: Unit and integration tests.
- `conf/`: Configuration files and report definitions.
- `lib/`: Project dependencies (external JARs).
- `nbproject/`: NetBeans project configuration.

## Documentation
- `README.md`: General project overview.
- `doc/`: Detailed documentation, including database schema and changelogs.
- `changelog.txt`: Historical changes.

## Maintainability & Technical Debt
- **Architecture:** Clear client-server-common separation (`src/canreg/client`, `server`, `common`) facilitates navigation and modularity.
- **Build System:** Uses Apache Ant with a NetBeans structure. While functional, it lacks the dependency management and lifecycle features of modern systems like Maven or Gradle.
- **Dependencies:** Significant technical debt exists due to outdated and mixed library versions (e.g., Apache Batik 1.6/1.14 mix, older Apache Commons versions).
- **Coupling:** High degree of coupling within the Swing GUI components and server-side logic, which may complicate isolation for testing.

## Feasibility of New Features
- **High Feasibility:** Core features within the existing Swing/Derby/JFreeChart paradigm are straightforward to implement by following established patterns.
- **Java 8 Constraint:** All new features MUST strictly adhere to Java 8 (1.8) syntax and libraries.
- **Verification:** Feasibility is supported by an existing JUnit 4 suite, which should be extended for all new functionality.
- **UI Consistency:** New UI elements should use existing Swing components or custom wrappers to maintain the application's look and feel.

## Development & Versioning
- **Versioning Scheme:** Uses a `Major.Minor.PatchLetter` format (e.g., `5.00.44k`).
- **Branching Strategy (GitFlow):** The repository employs a robust GitFlow model for managing parallel development:
    - `master`: Reserved for stable, production-ready releases.
    - `develop`: The main integration branch for ongoing development.
    - `feature/*`: Dedicated branches for new capabilities (e.g., `feature/shiny`, `feature/ad_hoc_analysis`, `feature/population-work`).
    - `release/*`: Used for final polish and testing before a major/minor release (e.g., `release/R44`, `release/R45`).
    - `bugfix/*` & `hotfix/*`: Targeted branches for resolving specific issues or critical production bugs.
- **Tagging Convention:** Extensive use of tags for tracking milestones:
    - **Release Tags:** Final versions are tagged alphabetically (e.g., `v5.00.44h`, `v5.00.44i`).
    - **Pre-releases:** Includes tags for beta versions (`BETA1`, `BETA10`) and release candidates (`RC1`).
- **Release Documentation:** The `changelog.txt` remains the authoritative source for detailed version history and feature updates.
- **VCS History:** Repository shows a migration path from Mercurial to Git, evidenced by the presence of both `.hgignore` and `.gitignore`.
