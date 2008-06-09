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

    public LocalSettings(String localSettingsFileName) throws IOException {
        boolean settingsLoaded = false;
        this.settingsFileName = localSettingsFileName;
        // set (and create) settings directory
        settingsDir = setCanRegClientSettingsDir();

        // Try to load the settings file
        settingsLoaded = loadSettings();

        if (!settingsLoaded) {
            createDefaultProperties();
        }
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
        return new Locale(properties.getProperty("locale"));
    }

    public void setLocale(String localeCode) {
        setProperty("locale", localeCode);
    }

    public void setProperty(String key, String string) {
        if (!string.equals(properties.getProperty(key))) {
            settingsChanged = true;
        }
        properties.setProperty(key, string);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private boolean loadSettings() {
        InputStream propInputStream = null;
        settingsChanged = false;
        boolean success = false;
        try {
            propInputStream = new FileInputStream(settingsDir + "/" + settingsFileName);
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
                propOutputStream = new FileOutputStream(settingsDir + "/" + settingsFileName);
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

    private Properties createDefaultProperties() {
        properties = new Properties();
        properties.setProperty("locale", Locale.getDefault().getLanguage());
        properties.setProperty("remember_password", "false");
        properties.setProperty("username", "");
        properties.setProperty("password", "");
        // properties.setProperty("server1.name", "Default");
        settingsChanged = true;
        return properties;
    }

    private static String setCanRegClientSettingsDir() {
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/.CanRegClient";

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
        properties.setProperty("server." + sd.getId() + ".name", sd.getName());
        properties.setProperty("server." + sd.getId() + ".port", sd.getPort() + "");
        properties.setProperty("server." + sd.getId() + ".code", sd.getCode());
        properties.setProperty("server." + sd.getId() + ".url", sd.getUrl());

        settingsChanged = true;
    }

    public String getLanguage() {
        String localeString = properties.getProperty("locale");
        Locale loc = new Locale(localeString);
        return loc.getDisplayLanguage(new Locale("en"));
    }

    public String getLanguageCode() {
        return properties.getProperty("locale");
    }

    public boolean isOutlineDragMode() {
        boolean isOutLineDragMode = false;
        String isOutlineDragModeString = properties.getProperty("outline_drag_mode");
        if (isOutlineDragModeString != null) {
            isOutLineDragMode = isOutlineDragModeString.equalsIgnoreCase("on");
        }
        return isOutLineDragMode;
    }

    public void setOutlineDragMode(boolean outlineDragMode) {
        if (outlineDragMode) {
            properties.setProperty("outline_drag_mode", "on");
        } else {
            properties.setProperty("outline_drag_mode", "off");
        }
        settingsChanged = true;
    }
}
