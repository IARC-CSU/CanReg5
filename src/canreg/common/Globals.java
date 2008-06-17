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
    
    static public String DATABASE_CONFIG ="dbConfiguration.properties";      
    static public String PASS_FILENAME = "Passwords.properties";
    
    static public String POLICY_FILENAME = "./conf/CanRegPolicy.conf";
    static public String LOGIN_FILENAME = "./conf/CanRegLogin.conf";
    
    static public String CANREG_SYSTEM_CONFIG_FOLDER = "./conf/database/";
    
    static public String CANREG_SYSTEM_CONFIG = CANREG_SYSTEM_CONFIG_FOLDER+"TRN.xml";
    
    static public String SCHEMA_NAME = "APP";
    
    // User levels
    static public String[] USER_RIGHT_LEVELS = {"Not logged in", "Supervisor", "Registrar", "Analyst"};
    static public int NOT_LOGGED_IN = 0;
    static public int SUPERVISOR = 1;
    static public int REGISTRAR = 2;
    static public int ANALYST = 3;
    
    // Languages - consider moving it to config-file or build it automatically...
    static public String[] LANGUAGES_AVAILABLE = {"en", "fr", "ar", "no"};

}
