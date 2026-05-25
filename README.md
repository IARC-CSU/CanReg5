# CanReg5

CanReg5 is a multi user, multi platform, open source tool to input, store, check and analyse cancer registry data.

## Description

Cancer registries need a tool to input, store, check and analyse their data. If these data are also coded and verified in a standard way, it facilitates the production of comparable analyses across registry populations. The main goal of the CanReg5 project is to provide a flexible and easy to use tool to accomplish these objectives.

CanReg5 is a multi user, multi platform, open source tool to input, store, check and analyse cancer registry data.

It has modules to do: data entry, quality control, consistency checks and basic analysis of the data

It was designed with an emphasis on user friendliness, it has a modern user interface and is easy to navigate.

Is available in several languages. (English, French, Spanish, Portuguese, Russian, Turkish, Georgian, and Chinese.)

[CanReg5 Web Site](http://www.iacr.com.fr/CanReg5)

[Follow @canreg](http://twitter.com/canreg)

## Development and Build Instructions

CanReg5 is built using **Apache Ant** with a classic NetBeans project structure. 

### Prerequisites
- **Java Development Kit (JDK):** Java 8 (1.8) is required for compatibility.
- **Apache Ant:** Installed and available on your system `PATH`.

### Common Build Targets
You can execute these standard Ant commands in the root of the project:

| Command | Action |
| :--- | :--- |
| `ant clean` | Deletes build artifacts and the `dist/` directory. |
| `ant compile` | Compiles the Java source files. |
| `ant jar` | Compiles and packages the application into `dist/CanReg.jar`, copy dependencies, and runs packaging scripts (`-post-jar`) to generate the distribution zip. |
| `ant test` | Compiles and runs all JUnit 4 test cases. |

### Running the Application

#### After building the JAR (`ant jar`):
You can run the packaged desktop client directly:
```bash
java -jar dist/CanReg.jar
```
Or run it by explicitly adding dependencies on macOS/Linux:
```bash
java -cp "dist/CanReg.jar:lib/*" canreg.client.CanRegClientApp
```

#### Running on Windows:
```cmd
java -cp "dist/CanReg.jar;lib/*" canreg.client.CanRegClientApp
```

### Packaging Distribution (macOS/Linux)
When you run `ant jar`, the post-build sequence automatically invokes:
1. `ruby update_version_number.rb` to update version identifiers.
2. `create-zip.sh` to package translations, documentation, configurations, and core assets into a clean distributable ZIP structure found under `dist/web/CanReg5.zip`.

