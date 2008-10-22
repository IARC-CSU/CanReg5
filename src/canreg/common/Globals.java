package canreg.common;

import canreg.server.database.AgeGroupStructure;

/**
 *
 * @author morten
 */
public class Globals {

    // http://en.wikipedia.org/wiki/Software_versioning
    static private int VERSION_MAJOR = 4;
    static private int VERSION_MINOR = 99;
    static private int VERSION_BUILD = 0;
    static private int VERSION_REVISION = 0;
    static private String VERSION_APPENDIX = "";
    static public String VERSION_STRING = VERSION_MAJOR+"."+VERSION_MINOR+VERSION_APPENDIX;
    static public String downloadCanRegURL = "http://www.iacr.com.fr/iacr_canreg4.htm";
    static public String newestVersionURLString = "http://www.iacr.com.fr/version.txt";
    static public String NAMESPACE = "ns3:";
    static public int RMI_PORT = 1199;
    static public boolean DEBUG = true;
    static public String DATABASE_CONFIG = "dbConfiguration.properties";
    static public String PASS_FILENAME = "Passwords.properties";
    static public String POLICY_FILENAME = "./conf/CanRegPolicy.conf";
    static public String LOGIN_FILENAME = "./conf/CanRegLogin.conf";
    
    // FILES AND FOLDERS
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
    public static String CANREG4_SYSTEM_FOLDER = "C:\\CR4SHARE\\CANREG4\\CR4-SYST\\";

    static public String SCHEMA_NAME = "APP";    // User levels
    public static int DEFAULT_PORT = 1199;
    public static int NUMBER_OF_LINES_IN_IMPORT_PREVIEW = 42;
    public static String TUMOUR_TABLE_NAME = "Tumour";
    public static String PATIENT_TABLE_NAME = "Patient";
    // public static String PATIENT_TABLE_NAME = "Patient";
    public static String DATE_FORMAT_STRING = "yyyyMMdd";
    static public enum UserRightLevels {NOT_LOGGED_IN, SUPERVISOR, REGISTRAR, ANALYST };

    static public String PATIENT_TABLE_RECORD_ID_VARIABLE_NAME = "PRID";
    static public String TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME = "TRID";
    
    static public AgeGroupStructure[] defaultAgeGroupStructures = {
        new AgeGroupStructure(5,85),
        new AgeGroupStructure(5,75),        
        new AgeGroupStructure(5,65),
        new AgeGroupStructure(5,75,15),        
        new AgeGroupStructure(5,65,15),
        new AgeGroupStructure(5,15,1,15)
    };
    
    static public int[] standardWorldPopulationWeights = 
    { 120,100,90,90,80,80,60,60,60,60,50,40,40,30,20,10,5,5 };
    
    static public int[] standardEuropeanPopulationWeights = 
    { 80,70,70,70,70,70,70,70,70,70,70,60,50,40,30,20,10,10 };
    
    static public int[] standardWHOPopulationWeights = 
    { 886,869,860,847,822,793,761,715,659,604,537,455,372,296,221,152, 91, 63 };

    public static enum SystemVariableNames{
        PRID,
        TRID,
        NEXT_RECORD_DB_ID,
        LAST_RECORD_DB_ID
    }
    
    public static enum StandardVariableNames {
        RegistrationNo,
        IncidenceDate,
        BirthDate,
        Age,
        Sex,
        Topography,
        Morphology,
        Behaviour,
        BasisDiagnosis,
        ICD10,
        MultPrimCode,
        CheckStatus,
        PersonSearch,
        RecordSearch,
        FirstName,
        Surname,
        UpdateDate,
        Lastcontact,
        Grade,
        ICCC,
        AddressCode,
        MultPrimSeq,
        MultPrimTot,
        Stage,
        Source1,
        Source2,
        Source3,
        Source4,
        Source5,
        Source6,
        PatientID,
        TumourID
    }
}
