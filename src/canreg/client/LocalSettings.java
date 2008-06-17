/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client;

import canreg.common.Globals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author morten
 */
public class LocalSettings {

    // Programming related
    private String settingsFileName;
    private String settingsDir;
    private Properties properties;
    private boolean settingsChanged;

    // Key names
    public String lastServerIDKey = "last_server_id";
    public String importPathKey = "import_path";
    public String userNameKey = "username";
    public String passwordKey = "password";
    public String localeKey = "locale";
    public String rememberPasswordKey = "remember_password";
    public String outlineDragModeKey = "outline_drag_mode";
    public String workingDirPathKey = "working_path";
    public String autoStartServerKey = "auto_start_server";
    // Property names
    public String yesProperty = "yes";
    public String noProperty = "no";
    public String onProperty = "on";
    public String offProperty = "off";
    public String trueProperty = "true";
    public String falseProperty = "false";

    public LocalSettings(String localSettingsFileName) throws IOException {
        boolean settingsLoaded = false;
        this.settingsFileName = localSettingsFileName;
        // set (and create) settings directory
        settingsDir = setCanRegClientSettingsDir();

        // Initialize properties
        properties = new Properties();
        // Try to load the settings file
        settingsLoaded = loadSettings();

        if (!settingsLoaded) {
            // If not possible to load the settings - get the default ones
            createDefaultProperties();
        }
        // create the working dir
        createWorkingDir(properties.getProperty(workingDirPathKey));
        writeSettings();
    }

    public String[] getLanguageList() {
        String list[] = new String[Globals.LANGUAGES_AVAILABLE.length];
        for (int i = 0; i < list.length; i++) {
            Locale locale = new Locale(Globals.LANGUAGES_AVAILABLE[i]);
            list[i] = locale.getDisplayLanguage();
        }
        return list;
    }

    public Locale getLocale() {
        return new Locale(properties.getProperty(localeKey));
    }

    public void setLocale(String localeCode) {
        setProperty(localeKey, localeCode);
    }

    private boolean loadSettings() {
        InputStream propInputStream = null;
        settingsChanged = false;
        boolean success = false;
        try {
            propInputStream = new FileInputStream(settingsDir + System.getProperty("file.separator") + settingsFileName);
            setProperties(new Properties());
            getProperties().loadFromXML(propInputStream);
            propInputStream.close();
            success = true;
        } catch (InvalidPropertiesFormatException ex) {
            Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LocalSettings.class.getName()).log(Level.INFO, null, ex);
            success = false;
        } catch (IOException ex) {
            Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            if (propInputStream != null) {
                try {
                    propInputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(LocalSettings.class.getName()).log(Level.INFO, null, ex);
                    success = false;
                }
            }
            return success;
        }
    }

    public boolean writeSettings() {
        if (settingsChanged = true) {
            OutputStream propOutputStream = null;
            boolean success = false;
            try {
                propOutputStream = new FileOutputStream(settingsDir + System.getProperty("file.separator") + settingsFileName);
                getProperties().storeToXML(propOutputStream, "CanReg5 local settings");

                success = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            } catch (IOException ex) {
                Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            } finally {
                if (propOutputStream != null) {
                    try {
                        propOutputStream.flush();
                        propOutputStream.close();
                        settingsChanged = false;
                    } catch (IOException ex) {
                        Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
                        success = false;
                    }
                }
                return success;
            }
        } else {
            return false;
        }
    }

    public void setProperty(String key, String string) {
        // Not sure why this didn't work, but OK... 
//        String property = properties.getProperty(key);
//        if (property != null && !string.trim().equalsIgnoreCase(property)) {
//            settingsChanged = true;
//        }
        properties.setProperty(key, string);
        settingsChanged = true;
    }

    public String getProperty(String key) {
        String property = properties.getProperty(key);
        if (property == null) {
            return getDefalutProperty(key);
        } else {
            return property;
        }
    }

    public String getDefalutProperty(String key) {
        String property = "";
        if (key.equalsIgnoreCase(userNameKey)) {
            property = "";
        } else if (key.equalsIgnoreCase(passwordKey)) {
            property = "";
        } else if (key.equalsIgnoreCase(localeKey)) {
            property = Locale.getDefault().getLanguage();
        } else if (key.equalsIgnoreCase(rememberPasswordKey)) {
            property = falseProperty;
        } else if (key.equalsIgnoreCase(workingDirPathKey)) {
            property = System.getProperty("user.home", ".") + System.getProperty("file.separator") + "CanReg";
        } 
        return property;
    }

    private void createDefaultProperties() {
        setProperty(localeKey, getDefalutProperty(localeKey));
        setProperty(rememberPasswordKey, getDefalutProperty(rememberPasswordKey));
        setProperty(userNameKey, getDefalutProperty(userNameKey));
        setProperty(passwordKey, getDefalutProperty(passwordKey));
        setProperty(workingDirPathKey,getDefalutProperty(workingDirPathKey));
        settingsChanged = true;
    }

    private static String setCanRegClientSettingsDir() {
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + System.getProperty("file.separator") + ".CanRegClient";

        // Create directory if missing
        File settingsFileDir = new File(systemDir);
        if (!settingsFileDir.exists()) {
            // create the db system directory
            File fileSystemDir = new File(systemDir);
            fileSystemDir.mkdir();
        }
        return systemDir;
    }

    // Consider making private 
    public Properties getProperties() {
        return properties;
    }

    // Consider making private 
    public void setProperties(Properties properties) {
        this.properties = properties;
        settingsChanged = true;
    }

    public LinkedList<ServerDescription> getServerDescriptions() {
        LinkedList<ServerDescription> serverList = new LinkedList();
        Set<String> set = properties.stringPropertyNames();

        // First we find all strings mentioning server in our properties
        // ie. server.0.name

        Iterator<String> i = set.iterator();
        Set foundServers = new HashSet();

        while (i.hasNext()) {
            String s = i.next();
            if (s.length() > 7 && s.substring(0, 7).equalsIgnoreCase("server.")) {
                int serverNumber = Integer.parseInt(s.substring(7, 8));
                boolean notSeen = foundServers.add(serverNumber);
                if (notSeen) {
                    String name = properties.getProperty("server." + serverNumber + ".name");
                    String url = properties.getProperty("server." + serverNumber + ".url");
                    String port = properties.getProperty("server." + serverNumber + ".port");
                    String code = properties.getProperty("server." + serverNumber + ".code");
                    serverList.add(new ServerDescription(name, url, Integer.parseInt(port), code, serverNumber));
                }
            }
        }
        return serverList;
    }

    public String[] getServerNames() {
        Iterator<ServerDescription> sd = getServerDescriptions().iterator();
        int i = 0;
        // count the servers
        while (sd.hasNext()) {
            sd.next();
            i++;
        }
        String[] namesArray = new String[i];

        int j = 0;
        // reset iterator
        sd = getServerDescriptions().iterator();
        while (sd.hasNext()) {
            namesArray[j] = sd.next().toString();
            j++;
        }

        if (i > 0) {
            return namesArray;
        } else {
            return null;
        }
    }

    public ServerDescription getServerDescription(int serverID) {
        LinkedList<ServerDescription> serverList = getServerDescriptions();
        ServerDescription sd = null;
        boolean found = false;
        int i = 0;
        while (!found && i < serverList.size()) {
            ServerDescription sdTemp = serverList.get(i++);
            found = sdTemp.getId() == serverID;
            if (found) {
                sd = sdTemp;
            }
        }
        return sd;
    }

    public void addServerDescription(ServerDescription sd) {
        setProperty("server." + sd.getId() + ".name", sd.getName());
        setProperty("server." + sd.getId() + ".port", sd.getPort() + "");
        setProperty("server." + sd.getId() + ".code", sd.getCode());
        setProperty("server." + sd.getId() + ".url", sd.getUrl());
    }

    public String getLanguage() {
        String localeString = properties.getProperty(localeKey);
        Locale loc = new Locale(localeString);
        return loc.getDisplayLanguage(new Locale("en"));
    }

    public String getLanguageCode() {
        return properties.getProperty(localeKey);
    }

    public boolean isOutlineDragMode() {
        boolean isOutLineDragMode = false;
        String isOutlineDragModeString = properties.getProperty(outlineDragModeKey);
        if (isOutlineDragModeString != null) {
            isOutLineDragMode = isOutlineDragModeString.trim().equalsIgnoreCase(onProperty);
        }
        return isOutLineDragMode;
    }

    public void setOutlineDragMode(boolean outlineDragMode) {
        if (outlineDragMode) {
            setProperty(outlineDragModeKey, onProperty);
        } else {
            setProperty(outlineDragModeKey, offProperty);
        }
    }

    public void createWorkingDir(String dir) {
        setProperty(workingDirPathKey, dir);
        File settingsFileDir = new File(dir);
        if (!settingsFileDir.exists()) {
            // create the db system directory
            File fileSystemDir = new File(dir);
            fileSystemDir.mkdir();
        }
    }
}
