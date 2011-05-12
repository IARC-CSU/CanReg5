/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
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

public final class LocalSettings {
    // Programming related

    private String settingsFileName;
    private String settingsDir;
    private Properties properties;
    private boolean settingsChanged;    // Key names
    /**
     * 
     */
    public static String LAST_SERVER_ID_KEY = "last_server_id";
    /**
     * 
     */
    public static String IMPORT_PATH_KEY = "import_path";
    /**
     * 
     */
    public static String USERNAME_KEY = "username";
    /**
     * 
     */
    public static String PASSWORD_KEY = "password";
    /**
     * 
     */
    public static String LOCALE_KEY = "locale";
    /**
     * 
     */
    public static String REMEMBER_PASSWORD_KEY = "remember_password";
    /**
     * 
     */
    public static String OUTLINE_DRAG_MODE_KEY = "outline_drag_mode";
     /**
     *
     */
    public static final String TABLES_PATH_KEY = "tables_path";
    /**
     * 
     */
    public static String WORKING_DIR_PATH_KEY = TABLES_PATH_KEY;
    /**
     * 
     */
    public static String AUTO_START_SERVER_KEY = "auto_start_server";
    /**
     * 
     */
    public static String LOOK_AND_FEEL_KEY = "look_and_feel";
    /**
     * 
     */
    public static String AUTO_BACKUP_KEY = "auto_backup";
    /**
     * 
     */
    public static String BACKUP_EVERY_KEY = "backup_every";
    // Property names
    /**
     * 
     */
    public static String YES_PROPERTY = "yes";
    /**
     * 
     */
    public static String NO_PROPERTY = "no";
    /**
     * 
     */
    public static String ON_PROPERTY = "on";
    /**
     * 
     */
    public static String OFF_PROPERTY = "off";
    /**
     * 
     */
    public static String TRUE_PROPERTY = "true";
    /**
     * 
     */
    public static String FALSE_PROPERTY = "false";

    /**
     * 
     * @param localSettingsFileName
     * @throws java.io.IOException
     */
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

    /**
     * 
     * @param name
     * @param url
     * @param port
     * @param code
     * @return
     */
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

    /*
    public String[] getLanguageList() {
    String list[] = new String[Globals.LANGUAGES_AVAILABLE.length];
    for (int i = 0; i < list.length; i++) {
    Locale locale = new Locale(Globals.LANGUAGES_AVAILABLE[i]);
    list[i] = locale.getDisplayLanguage();
    }
    return list;
    }
     */
    /**
     * 
     * @return
     */
    public Locale getLocale() {
        return new Locale(properties.getProperty(LOCALE_KEY));
    }

    /**
     * 
     * @return
     */
    public boolean isAutoBackup() {
        return getProperty(AUTO_BACKUP_KEY).equalsIgnoreCase(TRUE_PROPERTY);
    }

    /**
     * 
     * @param b
     */
    public void setAutomaticBackup(boolean b) {
        if (b) {
            setProperty(AUTO_BACKUP_KEY, TRUE_PROPERTY);
        } else {
            setProperty(AUTO_BACKUP_KEY, FALSE_PROPERTY);
        }
    }

    /**
     * 
     * @param localeCode
     */
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

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @param key
     * @param string
     */
    public void setProperty(String key, String string) {
        // Not sure why this didn't work, but OK... 
//        String property = properties.getProperty(key);
//        if (property != null && !string.trim().equalsIgnoreCase(property)) {
//            settingsChanged = true;
//        }
        properties.setProperty(key, string);
        settingsChanged = true;
    }

    /**
     * 
     * @param key
     * @return
     */
    public String getProperty(String key) {
        String property = properties.getProperty(key);
        if (property == null) {
            return getDefaultProperty(key);
        } else {
            return property;
        }
    }

    private String getDefaultProperty(String key) {
        String property = "";
        if (key.equalsIgnoreCase(USERNAME_KEY)) {
            property = "";
        } else if (key.equalsIgnoreCase(PASSWORD_KEY)) {
            property = "";
        } else if (key.equalsIgnoreCase(LOCALE_KEY)) {
            property = Locale.getDefault().getLanguage();
            // turkish small i capitalizes to big i with dot, which we don't support yet, so we default it to english...
            //if (property.equalsIgnoreCase("tr")) {
            //    property = Locale.ENGLISH.getLanguage();
            //}
        } else if (key.equalsIgnoreCase(REMEMBER_PASSWORD_KEY)) {
            property = FALSE_PROPERTY;
        } else if (key.equalsIgnoreCase(WORKING_DIR_PATH_KEY)) {
            property = System.getProperty("user.home", ".") + System.getProperty("file.separator") + "CanReg";
        } else if (key.equalsIgnoreCase(LOOK_AND_FEEL_KEY)) {
            property = "System";
        } else if (key.equalsIgnoreCase(AUTO_BACKUP_KEY)) {
            property = TRUE_PROPERTY;
        } else if (key.equalsIgnoreCase(BACKUP_EVERY_KEY)) {
            property = Globals.DEFAULT_BACK_UP_EVERY;
        }
        return property;
    }

    private void createDefaultProperties() {
        setProperty(LOCALE_KEY, getDefaultProperty(LOCALE_KEY));
        setProperty(REMEMBER_PASSWORD_KEY, getDefaultProperty(REMEMBER_PASSWORD_KEY));
        setProperty(USERNAME_KEY, getDefaultProperty(USERNAME_KEY));
        setProperty(PASSWORD_KEY, getDefaultProperty(PASSWORD_KEY));
        setProperty(WORKING_DIR_PATH_KEY, getDefaultProperty(WORKING_DIR_PATH_KEY));
        setProperty(AUTO_BACKUP_KEY, getDefaultProperty(AUTO_BACKUP_KEY));
        setProperty(BACKUP_EVERY_KEY, getDefaultProperty(BACKUP_EVERY_KEY));
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
    private Properties getProperties() {
        return properties;
    }

    // Consider making private 
    private void setProperties(Properties properties) {
        this.properties = properties;
        settingsChanged = true;
    }

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @param serverID
     * @return
     */
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

    /**
     * 
     * @param sd
     */
    public void addServerDescription(ServerDescription sd) {
        setProperty("server." + sd.getId() + ".name", sd.getName());
        setProperty("server." + sd.getId() + ".port", sd.getPort() + "");
        setProperty("server." + sd.getId() + ".code", sd.getCode());
        setProperty("server." + sd.getId() + ".url", sd.getUrl());
    }

    /**
     * 
     * @return
     */
    public String getLanguage() {
        String localeString = properties.getProperty(LOCALE_KEY);
        Locale loc = new Locale(localeString);
        return loc.getDisplayLanguage(new Locale("en"));
    }

    /**
     * 
     * @return
     */
    public String getLanguageCode() {
        return properties.getProperty(LOCALE_KEY);
    }

    /**
     * 
     * @return
     */
    public boolean isOutlineDragMode() {
        boolean isOutLineDragMode = false;
        String isOutlineDragModeString = properties.getProperty(OUTLINE_DRAG_MODE_KEY);
        if (isOutlineDragModeString != null) {
            isOutLineDragMode = isOutlineDragModeString.trim().equalsIgnoreCase(ON_PROPERTY);
        }
        return isOutLineDragMode;
    }

    /**
     * 
     * @param outlineDragMode
     */
    public void setOutlineDragMode(boolean outlineDragMode) {
        if (outlineDragMode) {
            setProperty(OUTLINE_DRAG_MODE_KEY, ON_PROPERTY);
        } else {
            setProperty(OUTLINE_DRAG_MODE_KEY, OFF_PROPERTY);
        }
    }

    /**
     * 
     * @param dir
     */
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
