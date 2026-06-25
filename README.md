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

### Packaging Distribution
When you run `ant jar`, the post-build sequence automatically invokes packaging tasks to generate the distribution zips for users. 

**Native Desktop Installers (jDeploy & Mac Bundle)**
CanReg5 now uses a hybrid approach for native distribution:
1. **Windows and Linux:** Built using [jDeploy](https://www.jdeploy.com/) to generate standalone installers (`.exe`, `.deb`, `.AppImage`). This requires **Node.js (`npm` and `npx`)** on the developer's machine.
2. **macOS:** Built using a native `create-mac-app.sh` script to generate a fully self-contained `CanReg5.app` macOS application bundle. This step only triggers if the build is executed on a macOS machine.

The packaging process follows these steps:
1. `ruby update_version_number.rb` updates version identifiers.
2. `npx jdeploy package` generates the local native desktop installers in `jdeploy/installers`.
3. Ant creates platform-specific distribution zips in `dist/web/`:
   - `CanReg5-Windows.zip` (Contains Windows `.exe` and base project files)
   - `CanReg5-macOS.zip` (Contains the `CanReg5.app` application bundle)
   - `CanReg5-Linux.zip` (Contains Linux `.deb`/`.AppImage` and base project files)


## Installation for Developers

CanReg5 was developed with NetBeans IDE with an additional Swing application framework support to help with the application design. It uses jdk1.8.0_201.
Before building the project, copy `appinfo.properties` from `/src/canreg/client` into `/src`.

Build and run the project. Then the CanReg5 window should appear.
On the first run, click "Add a new system" and import a system (.xml file). Then you'll be invited to log into the database. The default password is "123456789".
Next, select the correct system and log in to the application using your credentials.

### Installation troubleshooting
- In the `pom.xml`, the Derby dependency might have its scope set to test, which causes the build to fail since the database dependency will be missing. Try to remove its `<scope>` tag.
- Sometimes, the `xml-apis` dependency won't work with version 2.0.2. Try to use version 1.4.01 instead.
- In some branches, if you're running junit tests, the JUnit dependency might be missing. In this case, please refer to the master branch to get the missing dependency.
- For NetBeans, you may encounter some issues while trying to open the project. If Netbeans doesn't recognize the repository as a project, see if `nbproject/project.xml` uses HTTP links in the "xmlns" attribute. If so, replace them with HTTPS links and try to open the repository again.
- Even if you've configured the IDE's platform manager, NetBeans can have trouble locating your JDK. Consequently, it causes the IDE to be unable to download Maven dependencies, thus making the project impossible to build. However, it is possible to force NetBeans' JDK path directly in NetBeans' configuration file. To do so, open `\netbeans\etc\netbeans.conf`, edit line 77 with your JDK path and uncomment the line. It should look like `netbeans_jdkhome="C:\Apps\jdk1.8.0_201"`.
