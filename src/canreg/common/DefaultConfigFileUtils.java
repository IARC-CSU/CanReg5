package canreg.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public boolean writeDefaultConfigFile(String registryCode,String isEncrypted) {
        boolean success = false;
        OutputStream propOutputStream = null;
        Properties properties = new Properties();
        properties.setProperty(registryCode,isEncrypted);

        try {
            propOutputStream = new FileOutputStream(Globals.CANREG_CONFIG_DATABASE);
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
                        + " the default file database.properties", ex);
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Update or create the file database.properties in the .CanRegServer.
     * The file is created if it doesn't exist.
     * The file is updated if it alredy exists
     *
     * 
     * @param registryCode the registry code 
     * @param value true or false
     */
 
    public void updateConfigFileProperties(String registryCode , String value){
        
        OutputStream propOutputStream = null;
        try {
            InputStream inputStream = new FileInputStream(Globals.CANREG_CONFIG_DATABASE);
            Properties props = new Properties();
            props.loadFromXML(inputStream);
            inputStream.close();
            if(Boolean.parseBoolean(props.getProperty(registryCode))){
                propOutputStream = new FileOutputStream(Globals.CANREG_CONFIG_DATABASE);
                props.setProperty(registryCode,value);
                props.storeToXML(propOutputStream, "properties");
            }
            else {
                writeDefaultConfigFile(registryCode,value);
            }
        } catch (IOException ex) {
            Logger.getLogger(DefaultConfigFileUtils.class.getName()).log(Level.SEVERE, "Error while updating"
                + " the default file database.properties", ex);
        } finally {
            if (propOutputStream != null) {
                try {
                    propOutputStream.flush();
                    propOutputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(DefaultConfigFileUtils.class.getName()).log(Level.SEVERE, "Error while closing"
                        + " the default file database.properties", ex);
                }
            }
        }
    }

    /**
     * read all the properties available in the config file "config.propeties"
     * @return properties : all the properties present in the config file 
     */
    public Properties readConfigFile() {

        Properties properties = new Properties();
        try {
            FileInputStream propInputStream = new FileInputStream(Globals.CANREG_CONFIG_DATABASE);
            properties.loadFromXML(propInputStream);
            propInputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(DefaultConfigFileUtils.class.getName()).log(Level.SEVERE, "Error while reading"
                + " the default file database.properties", ex);
        }
        return properties;
    }
}