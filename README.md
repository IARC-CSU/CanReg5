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

## Installation for developpers

CanReg5 was developed with NetBeans IDE with an additional a Swing application framework support to help with the application design. It uses jdk1.8.0_201.
Before building the project, copy appinfo.properties from /src/canreg/client into /src.

Build and run the project. Then the CanReg5 window should appear.
On the first run, click "Add a new system" and import a system (.xml file). Then you'll be invited to log into the database. The default password is "123456789".
Next, select the correct system and log in to the application using your credentials.

### Installation troubleshooting
- In the pom.xml, the Derby dependency might have its scope set to test, which causes the build to fail since the database dependency will be missing. Try to remove its <scope> tag.


- Sometimes, the xml-apis dependency won't work with version 2.0.2. Try to use version 1.4.01 instead


- In some branches, if you're running junit tests, the JUnit dependency might be missing. In this case, please refer to the master branch to get the missing dependency.


- For NetBeans, you may encounter some issues while trying to open the project. If Netbeans doesn't recognize the repository as a project, see if CanReg5\nbproject\project.xml uses HTTP links in the "xmlns" attribute. If so, replace them with HTTPS links and try to open the repository again.


- Even if you've configured the IDE's platform manager, NetBeans can have trouble locating your JDK. Consequently, it causes the IDE to be unable to download Maven dependencies, thus making the project impossible to build. However, it is possible to force NetBeans' JDK path directly in NetBeans' configuration file. To do so, open \netbeans\etc\netbeans.conf, edit line 77 with your JDK path and uncomment the line. It should look like <netbeans_jdkhome="C:\Apps\jdk1.8.0_201">