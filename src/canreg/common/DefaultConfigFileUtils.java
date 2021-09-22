package canreg.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultConfigFileUtils {

    /**
     * Write the hard coded credentials in a dedicate file.
     * The file is set once at the installation and filed with the default supervisor credentials.
     * It will be later replace with new credentials from a supervisor
     * @return
     */
    public static boolean writeDefaultConfigFile() {
        boolean success = false;
        OutputStream propOutputStream = null;
        Properties properties = new Properties();
        properties.setProperty("username", "morten");
        properties.setProperty("password", "ervik");

        try {
            propOutputStream = new FileOutputStream(Globals.CANREG_CONFIG_FILENAME);
            properties.storeToXML(propOutputStream, "properties");
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(DefaultConfigFileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (propOutputStream != null) {
                try {
                    propOutputStream.flush();
                    propOutputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(DefaultConfigFileUtils.class.getName()).log(Level.SEVERE, "Error while creating" 
                        + " the default file config.properties", ex);
                    success = false;
                }
            }
        }
        return success;
    }

    public static Properties readConfigFile() {

        Properties properties = new Properties();
        try {
            FileInputStream propInputStream = new FileInputStream(Globals.CANREG_CONFIG_FILENAME);
            properties.loadFromXML(propInputStream);
        } catch (IOException ex) {
            Logger.getLogger(DefaultConfigFileUtils.class.getName()).log(Level.SEVERE, "Error while reading"
                + " the default file config.properties", ex);
        }
        return properties;
    }
}