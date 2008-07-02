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
    private boolean settingsChanged;    // Key names
    public static String LAST_SERVER_ID_KEY = "last_server_id";
    public static String IMPORT_PATH_KEY = "import_path";
    public static String USERNAME_KEY = "username";
    public static String PASSWORD_KEY = "password";
    public static String LOCALE_KEY = "locale";
    public static String REMEMBER_PASSWORD_KEY = "remember_password";
    public static String OUTLINE_DRAG_MODE_KEY = "outline_drag_mode";
    public static String WORKING_DIR_PATH_KEY = "working_path";
    public static String AUTO_START_SERVER_KEY = "auto_start_server";
    // Property names
    public static String YES_PROPERTY = "yes";
    public static String NO_PROPERTY = "no";
    public static String ON_PROPERTY = "on";
    public static String OFF_PROPERTY = "off";
    public static String TRUE_PROPERTY = "true";
    public static String FALSE_PROPERTY = "false";

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
        createWorkingDir(properties.getProperty(WORKING_DIR_PATH_KEY));
        writeSettings();
    }

    public int addServerToServerList(String name, String url, int port, String code) {
        boolean found = false;
        int i = 0;

        while (!found) {
            found = getProperty("server." + (i++) + ".name").equals("");
        }
        // step one back
        i -= 1;

        ServerDescription sd = new ServerDescription(name,
                url,
                port,
                code, i);
        addServerDescription(sd);
        
        return i;
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
        return new Locale(properties.getProperty(LOCALE_KEY));
    }

    public void setLocale(String localeCode) {
        setProperty(LOCALE_KEY, localeCode);
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
        if (key.equalsIgnoreCase(USERNAME_KEY)) {
            property = "";
        } else if (key.equalsIgnoreCase(PASSWORD_KEY)) {
            property = "";
        } else if (key.equalsIgnoreCase(LOCALE_KEY)) {
            property = Locale.getDefault().getLanguage();
        } else if (key.equalsIgnoreCase(REMEMBER_PASSWORD_KEY)) {
            property = FALSE_PROPERTY;
        } else if (key.equalsIgnoreCase(WORKING_DIR_PATH_KEY)) {
            property = System.getProperty("user.home", ".") + System.getProperty("file.separator") + "CanReg";
        }
        return property;
    }

    private void createDefaultProperties() {
        setProperty(LOCALE_KEY, getDefalutProperty(LOCALE_KEY));
        setProperty(REMEMBER_PASSWORD_KEY, getDefalutProperty(REMEMBER_PASSWORD_KEY));
        setProperty(USERNAME_KEY, getDefalutProperty(USERNAME_KEY));
        setProperty(PASSWORD_KEY, getDefalutProperty(PASSWORD_KEY));
        setProperty(WORKING_DIR_PATH_KEY, getDefalutProperty(WORKING_DIR_PATH_KEY));
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
        String localeString = properties.getProperty(LOCALE_KEY);
        Locale loc = new Locale(localeString);
        return loc.getDisplayLanguage(new Locale("en"));
    }

    public String getLanguageCode() {
        return properties.getProperty(LOCALE_KEY);
    }

    public boolean isOutlineDragMode() {
        boolean isOutLineDragMode = false;
        String isOutlineDragModeString = properties.getProperty(OUTLINE_DRAG_MODE_KEY);
        if (isOutlineDragModeString != null) {
            isOutLineDragMode = isOutlineDragModeString.trim().equalsIgnoreCase(ON_PROPERTY);
        }
        return isOutLineDragMode;
    }

    public void setOutlineDragMode(boolean outlineDragMode) {
        if (outlineDragMode) {
            setProperty(OUTLINE_DRAG_MODE_KEY, ON_PROPERTY);
        } else {
            setProperty(OUTLINE_DRAG_MODE_KEY, OFF_PROPERTY);
        }
    }

    public void createWorkingDir(String dir) {
        setProperty(WORKING_DIR_PATH_KEY, dir);
        File settingsFileDir = new File(dir);
        if (!settingsFileDir.exists()) {
            // create the db system directory
            File fileSystemDir = new File(dir);
            fileSystemDir.mkdir();
        }
    }
}
