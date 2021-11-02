package canreg.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that contains a method to create and update the file database.properties.
 * Another method is also here to read the properties stored in the file
 * The file is created in the .CanRegServer directory
 */
public class DefaultConfigFileUtils {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultConfigFileUtils.class.getName());
    
    /**
     * Update or create the file database.properties in the .CanRegServer.
     * The file is created if it doesn't exist.
     * The file is updated if it already exists
     *
     * @param registryCode the registry code 
     * @param value true or false
     */

    public void updateConfigFileProperties(String registryCode, String value) {
        Properties properties = new Properties();
        File file = new File(Globals.CANREG_CONFIG_DATABASE);
        if (file.exists()) {
            properties = readConfigFile();
        }
        // properties are null if the read failed
        if (properties != null) {
            try (OutputStream propOutputStream = new FileOutputStream(Globals.CANREG_CONFIG_DATABASE)) {
                properties.setProperty(registryCode, value);
                properties.storeToXML(propOutputStream, "properties");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error while updating"
                    + " the default file database.properties", ex);
            }
        }
    }

    /**
     * read all the properties available in the config file "database.properties"
     * @return properties: all the properties present in the database.properties file, null if impossible to read 
     */
    public Properties readConfigFile() {
        Properties properties = new Properties();
        try(FileInputStream propInputStream = new FileInputStream(Globals.CANREG_CONFIG_DATABASE)) {
            properties.loadFromXML(propInputStream);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error while reading"
                + " the default file database.properties", ex);
            return null;
        }
        return properties;
    }
}