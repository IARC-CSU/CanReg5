/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public final class SystemSettings {

    // Programming related
    private String settingsFileName;
    private String settingsDir;
    private Properties properties;
    private DateFormat dateFormat;
    private boolean settingsChanged;    
    // Key names
    /**
     * 
     */
    public static String DATE_OF_LAST_BACKUP_KEY = "date_of_last_backup";
    /**
     * 
     */
    public static String SYSTEM_DIR_PATH_KEY = "system_path";
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
     * @param settingsFileName
     * @throws java.io.IOException
     */
    public SystemSettings(String settingsFileName) throws IOException {
        dateFormat = new SimpleDateFormat("yyyyMMdd");
        
        boolean settingsLoaded = false;
        this.settingsFileName = settingsFileName;
        // set (and create) settings directory
        settingsDir = getCanRegSystemSettingsDir();

        // Initialize properties
        properties = new Properties();
        // Try to load the settings file
        settingsLoaded = loadSettings();

        if (!settingsLoaded) {
            // If not possible to load the settings - get the default ones
            createDefaultProperties();
        }
        // create the working dir
        createWorkingDir(properties.getProperty(SYSTEM_DIR_PATH_KEY));
        writeSettings();
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
            Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SystemSettings.class.getName()).log(Level.INFO, null, ex);
            success = false;
        } catch (IOException ex) {
            Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            if (propInputStream != null) {
                try {
                    propInputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(SystemSettings.class.getName()).log(Level.INFO, null, ex);
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
                getProperties().storeToXML(propOutputStream, "CanReg5 system settings");
                success = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            } catch (IOException ex) {
                Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            } finally {
                if (propOutputStream != null) {
                    try {
                        propOutputStream.flush();
                        propOutputStream.close();
                        settingsChanged = false;
                    } catch (IOException ex) {
                        Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
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
            return getDefalutProperty(key);
        } else {
            return property;
        }
    }

    private String getDefalutProperty(String key) {
        String property = "";
        if (key.equalsIgnoreCase(DATE_OF_LAST_BACKUP_KEY)) {
            property = "";
        } 
        return property;
    }

    private void createDefaultProperties() {
        setProperty(SYSTEM_DIR_PATH_KEY, getCanRegSystemSettingsDir());
        setProperty(DATE_OF_LAST_BACKUP_KEY, getDefalutProperty(DATE_OF_LAST_BACKUP_KEY));
        settingsChanged = true;
    }

    private static String getCanRegSystemSettingsDir() {
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + System.getProperty("file.separator") + ".CanRegServer" + System.getProperty("file.separator") + "Settings";

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
     * @param dir
     */
    public void createWorkingDir(String dir) {
        setProperty(SYSTEM_DIR_PATH_KEY, dir);
        File settingsFileDir = new File(dir);
        if (!settingsFileDir.exists()) {
            // create the db system directory
            File fileSystemDir = new File(dir);
            fileSystemDir.mkdir();
        }
    }
    
    /**
     * 
     * @param date
     */
    public void setDateOfLastbackup(Date date){
        setProperty(SystemSettings.DATE_OF_LAST_BACKUP_KEY, dateFormat.format(date));
    }
    
    /**
     * 
     * @return
     */
    public Date getDateOfLastBackUp(){
        String dateString = getProperty(SystemSettings.DATE_OF_LAST_BACKUP_KEY);
        Date date = null;
        if (dateString != null && dateString.trim().length()>0){
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException ex) {
                date = null;
                Logger.getLogger(SystemSettings.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return date;
    }
}
