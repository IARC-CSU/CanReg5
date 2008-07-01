/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common;

/**
 *
 * @author morten
 */
public class Globals {

    static public int VERSION = 4;
    static public int REVISION = 99;
    static public String VERSION_STRING = "4.99";
    static public String downloadCanRegURL = "http://www.iacr.com.fr/iacr_canreg4.htm";
    static public String newestVersionURLString = "http://www.iacr.com.fr/version.txt";
    static public String NAMESPACE = "ns3:";
    static public int RMI_PORT = 1199;
    static public boolean DEBUG = true;
    static public String DATABASE_CONFIG = "dbConfiguration.properties";
    static public String PASS_FILENAME = "Passwords.properties";
    static public String POLICY_FILENAME = "./conf/CanRegPolicy.conf";
    static public String LOGIN_FILENAME = "./conf/CanRegLogin.conf";
    static public String FILE_SEPARATOR = System.getProperty("file.separator", ".");
    static private String CANREG_USER_FOLDER = System.getProperty("user.home", ".");
    static private String CANREG_SERVER_FOLDER_NAME = ".CanRegServer";
    static private String CANREG_CLIENT_FOLDER_NAME = ".CanRegClient";
    static public String CANREG_SERVER_FOLDER = Globals.CANREG_USER_FOLDER + Globals.FILE_SEPARATOR + Globals.CANREG_SERVER_FOLDER_NAME;
    static public String CANREG_CLIENT_FOLDER = Globals.CANREG_USER_FOLDER + Globals.FILE_SEPARATOR + Globals.CANREG_CLIENT_FOLDER_NAME;
    static private String CANREG_SERVER_SYSTEM_CONFIG_FOLDER_NAME = "System";
    static private String CANREG_SERVER_DATABASE_FOLDER_NAME = "Database";
    static private String CANREG_BACKUP_FOLDER_NAME = "Backup";
    static public String CANREG_SERVER_SYSTEM_CONFIG_FOLDER = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + CANREG_SERVER_SYSTEM_CONFIG_FOLDER_NAME;
    static public String CANREG_SERVER_DATABASE_FOLDER = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + CANREG_SERVER_DATABASE_FOLDER_NAME;
    static public String CANREG_BACKUP_FOLDER = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + CANREG_BACKUP_FOLDER_NAME;

    static public String SCHEMA_NAME = "APP";    // User levels
    public static String CANREG4_SYSTEM_FOLDER = "C:\\CR4SHARE\\CANREG4\\CR4-SYST\\";
    public static int DEFAULT_PORT = 1199;
    static public enum UserRightLevels {NOT_LOGGED_IN, SUPERVISOR, REGISTRAR, ANALYST };

    // Languages - consider moving it to config-file or build it automatically...
    static public String[] LANGUAGES_AVAILABLE = {"en", "fr", "ar", "no"};
}
