package canreg.common;

import canreg.server.database.AgeGroupStructure;

/**
 *
 * @author ervikm
 */
public class Globals {

    /**
     * 
     */
    static public String APPINFO_PROPERTIES_PATH = "/appinfo.properties";
    /**
     * 
     */
    static public String[] versionStringParts = new String[]{"program.VERSION"};
    /**
     * 
     */
    static public String downloadCanRegURL = "http://www.iacr.com.fr/CanReg5/CanReg5.zip";
    /**
     * 
     */
    static public String newestVersionURLString = "http://www.iacr.com.fr/version.txt";
    /**
     * 
     */
    static public String NAMESPACE = "ns3:";
    /**
     * 
     */
    static public int RMI_PORT = 1199;
    /**
     * 
     */
    static public boolean DEBUG = true;
    // FILES AND FOLDERS
    /**
     *
     */
    static public String FILE_SEPARATOR = System.getProperty("file.separator", ".");
    static private String CANREG_USER_FOLDER = System.getProperty("user.home", ".");
    static private String CANREG_SERVER_FOLDER_NAME = ".CanRegServer";
    static private String CANREG_CLIENT_FOLDER_NAME = ".CanRegClient";
    /**
     * 
     */
    static public String DATABASE_CONFIG = "dbConfiguration.properties";
    /**
     * 
     */
    static public String PASS_FILENAME = "Passwords.properties";
    /**
     *
     */
    static public String LEVELS_FILENAME = "Levels.properties";
    /**
     * 
     */
    static public String POLICY_FILENAME = "./conf/CanRegPolicy.conf";
    /**
     * 
     */
    static public String LOGIN_FILENAME = "./conf/CanRegLogin.conf";
    static public String TABLES_CONF_PATH = "./conf/tables";
    static public String TABLES_PREVIEW_PATH = "./conf/tables/previews";
    static public String DEFAULT_PREVIEW_FILENAME = "blanc.png";
    /**
     * 
     */
    static public String CANREG_SERVER_FOLDER = Globals.CANREG_USER_FOLDER + Globals.FILE_SEPARATOR + Globals.CANREG_SERVER_FOLDER_NAME;
    /**
     * 
     */
    static public String CANREG_CLIENT_FOLDER = Globals.CANREG_USER_FOLDER + Globals.FILE_SEPARATOR + Globals.CANREG_CLIENT_FOLDER_NAME;
    static private String CANREG_SERVER_SYSTEM_CONFIG_FOLDER_NAME = "System";
    static private String CANREG_SERVER_DATABASE_FOLDER_NAME = "Database";
    static private String CANREG_BACKUP_FOLDER_NAME = "Backup";
    static public String CANREG_PASSWORD_FILE_NAME = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + "passwords.properties";
    static public String CANREG_LEVELS_FILE_NAME = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + "levels.properties";
    /**
     * 
     */
    static public String CANREG_SERVER_SYSTEM_CONFIG_FOLDER = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + CANREG_SERVER_SYSTEM_CONFIG_FOLDER_NAME;
    /**
     * 
     */
    static public String CANREG_SERVER_DATABASE_FOLDER = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + CANREG_SERVER_DATABASE_FOLDER_NAME;
    /**
     * 
     */
    static public String CANREG_BACKUP_FOLDER = CANREG_SERVER_FOLDER + Globals.FILE_SEPARATOR + CANREG_BACKUP_FOLDER_NAME;
    /**
     * 
     */
    public static String CANREG4_SYSTEM_FOLDER = "C:\\CR4SHARE\\CANREG4\\CR4-SYST\\";
    /**
     * 
     */
    static public String SCHEMA_NAME = "APP";    // User levels
    /**
     * 
     */
    public static int DEFAULT_PORT = 1199;
    /**
     * 
     */
    public static int NUMBER_OF_LINES_IN_IMPORT_PREVIEW = 42;
    /**
     * 
     */
    public static String TUMOUR_TABLE_NAME = "Tumour";
    public static String PATIENT_TABLE_NAME = "Patient";
    public static String SOURCE_TABLE_NAME = "Source";
    public static String USERS_TABLE_NAME = "USERS";

    /**
     * 
     */
    public static String DATE_FORMAT_STRING = "yyyyMMdd";
    public static String DATAENTRY_LANGUAGE_ENGLISH = "E";
    public static String DATAENTRY_LANGUAGE_FRENCH = "F";
    public static String DATAENTRY_LANGUAGE_SPANISH = "S";
    public static String DATAENTRY_LANGUAGE_ITALIAN = "I";
    public static String DATAENTRY_LANGUAGE_TURKISH = "T";
    public static String DATAENTRY_LANGUAGE_ROMANIAN = "R";
    public static String DATAENTRY_LANGUAGE_PORTUGUESE = "P";
    public static String DATAENTRY_LANGUAGE_CHINESE = "C";
    public static String DATAENTRY_LANGUAGE_THAI = "H";
    public static String DATAENTRY_LANGUAGE_KOREAN = "K";
    public static String DATAENTRY_LANGUAGE_ARABIC = "A";
    public static String DATAENTRY_LANGUAGE_FARSI = "Z";
    public static String DATAENTRY_LANGUAGE_RUSSIAN = "U";
    public static String CHARSET_ENGLISH = "windows-1252";
    public static String CHARSET_FRENCH = "windows-1252";
    public static String CHARSET_SPANISH = "windows-1252";
    public static String CHARSET_ITALIAN = "windows-1252";
    public static String CHARSET_TURKISH = "windows-1254";
    public static String CHARSET_ROMANIAN = "windows-1250";
    public static String CHARSET_PORTUGUESE = "windows-1252";
    public static String CHARSET_CHINESE = "UTF-8";
    public static String CHARSET_THAI = "windows-874";
    public static String CHARSET_KOREAN = "UTF-8";
    public static String CHARSET_ARABIC = "windows-1256";
    public static String CHARSET_FARSI = "UTF-8";
    public static String CHARSET_RUSSIAN = "ISO-8859-5";
    public static String LOGFILE_PATTERN = "%h/canreg5client.log";
    public static String LOG_LEVEL = "FINEST";
    public static String DEFAULT_BACK_UP_EVERY = "7";
    public static int GLOBAL_PERSON_SEARCH_STEP_SIZE = 1;
    public static String CANREG_HELP_FILE_NAME = "CanReg5-functionality.html";
    public static String RECORD_STATUS_PENDING_CODE = "0";
    public static int UNDUPLICATION_NOT_DONE_CODE = 0;
    public static int MAX_POPULATION_DATASETS_IN_TABLE = 50;
    public static String OBSOLETE_VALUE = "1";
    public static String NOT_OBSOLETE_VALUE = "0";
    /**
     * 
     */
    static public enum UserRightLevels {

        /**
         * 
         */
        NOT_LOGGED_IN,
        /**
         * 
         */
        SUPERVISOR,
        /**
         * 
         */
        REGISTRAR,
        /**
         * 
         */
        ANALYST
    };
    /**
     * 
     */
    static public String PATIENT_TABLE_RECORD_ID_VARIABLE_NAME = "PRID";
    /**
     * 
     */
    static public String TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME = "TRID";
        /**
     *
     */
    static public String SOURCE_TABLE_RECORD_ID_VARIABLE_NAME = "SRID";
    /**
     * 
     */
    static public AgeGroupStructure[] defaultAgeGroupStructures = {
        new AgeGroupStructure(5, 85),
        new AgeGroupStructure(5, 75),
        new AgeGroupStructure(5, 65),
        new AgeGroupStructure(10, 75, 15),
        new AgeGroupStructure(10, 65, 15),
        new AgeGroupStructure(5, 15, 1, 15)
    };
    /**
     * 
     */
    static public int[] standardWorldPopulationWeights = {120, 100, 90, 90, 80, 80, 60, 60, 60, 60, 50, 40, 40, 30, 20, 10, 5, 5};
    /**
     * 
     */
    static public int[] standardEuropeanPopulationWeights = {80, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 60, 50, 40, 30, 20, 10, 10};
    /**
     * 
     */
    static public int[] standardWHOPopulationWeights = {886, 869, 860, 847, 822, 793, 761, 715, 659, 604, 537, 455, 372, 296, 221, 152, 91, 63};

    /**
     * 
     */
    public static enum SystemVariableNames {

        /**
         * 
         */
        PRID,
        /**
         * 
         */
        TRID,
        /**
         * 
         */
        NEXT_RECORD_DB_ID,
        /**
         * 
         */
        LAST_RECORD_DB_ID
    }

    /**
     * 
     */
    public static enum StandardVariableNames {

        /**
         * 
         */
        TumourID,
        /**
         * 
         */
        IncidenceDate,
        /**
         * 
         */
        BirthDate,
        /**
         * 
         */
        Age,
        /**
         * 
         */
        Sex,
        /**
         * 
         */
        Topography,
        /**
         * 
         */
        Morphology,
        /**
         * 
         */
        Behaviour,
        /**
         * 
         */
        BasisDiagnosis,
        /**
         * 
         */
        ICD10,
        /**
         * 
         */
        MultPrimCode,
        /**
         * 
         */
        CheckStatus,
        /**
         * 
         */
        PersonSearch,
        /**
         * 
         */
        TumourRecordStatus,
        /**
         * 
         */
        FirstName,
        /**
         * 
         */
        Surname,
        /**
         * 
         */
        PatientUpdateDate,
        /**
         *
         */
        TumourUpdateDate,
        /**
         *
         */
        PatientUpdatedBy,
        /**
         *
         */
        TumourUpdatedBy,
        /**
         *
         */
        Lastcontact,
        /**
         * 
         */
        Grade,
        /**
         * 
         */
        ICCC,
        /**
         * 
         */
        AddressCode,
        /**
         * 
         */
        MultPrimSeq,
        /**
         * 
         */
        MultPrimTot,
        /**
         * 
         */
        Stage,
        /**
         * 
         */
        Source1,
        /**
         * 
         */
        Source2,
        /**
         * 
         */
        Source3,
        /**
         * 
         */
        Source4,
        /**
         * 
         */
        Source5,
        /**
         * 
         */
        Source6,
        /**
         *
         */
        PatientID,
        /**
         *
         */
        PatientRecordStatus,
        /**
         *
         */
        PatientCheckStatus,
        /**
         * The ID of the patient in the tumour table
         */
        PatientIDTumourTable,
        /**
         * The ID of the patient record
         */
        PatientRecordID,
        /**
         * The ID of the patient record in the tumour table
         */
        PatientRecordIDTumourTable,
        /**
         * 
         */
        TumourRecordID,
        /**
         * 
         */
        ObsoleteFlagTumourTable,
        /**
         *
         */
        ObsoleteFlagPatientTable,
        /**
         *
         */
        TumourUnduplicationStatus,
        /**
         * The ID of the tumour record in the source table
         */
        TumourIDSourceTable,
        /**
         * The ID of the source record
         */
        SourceRecordID
    }
    public static String FILL_IN_STATUS_MANDATORY_STRING = "Mandatory";
    public static String FILL_IN_STATUS_AUTOMATIC_STRING = "Automatic";
    public static String FILL_IN_STATUS_OPTIONAL_STRING = "Optional";

    public static int PDS_DATABASE_NAME_LENGTH = 100;

    public static int ADDITIONAL_DIGITS_FOR_PATIENT_RECORD = 2;

    public static int ADDITIONAL_DIGITS_FOR_TUMOUR_ID = 2;

    public static int ADDITIONAL_DIGITS_FOR_SOURCE_ID = 2;

    public static int MAX_USERNAME_LENGHT = 16;
}
