/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017 International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.common;

import canreg.common.database.AgeGroupStructure;

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
    public static String TWITTER_RSS_DATE_FORMAT_STRING = "EEE MMM d HH:mm:ss Z yyyy";
    static public String CANREG_TWITTER_RSS_URL = "http://www.twitter-rss.com/user_timeline.php?screen_name=canreg";
    // static public String CANREG_TWITTER_RSS_URL = "http://twitter.com/statuses/user_timeline/86306086.rss";
    // static public String CANREG_TWITTER_RSS_URL = "http://api.twitter.com/1/statuses/user_timeline.rss?screen_name=canreg";
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
    static final public String FILE_SEPARATOR = System.getProperty("file.separator", ".");
    private static final String CANREG_USER_FOLDER = System.getProperty("user.home", ".");
    private static final String CANREG_SERVER_FOLDER_NAME = ".CanRegServer";
    private static final String CANREG_CLIENT_FOLDER_NAME = ".CanRegClient";
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
    static public String DEFAULT_SYSTEM_XML = "./conf/database/TRN.xml";
    public static String DEFAULT_DICTIONARIES_FOLDER = "/canreg/common/resources/dictionaries";
    /**
     *
     */
    static final public String CANREG_SERVER_FOLDER = Globals.CANREG_USER_FOLDER + Globals.FILE_SEPARATOR + Globals.CANREG_SERVER_FOLDER_NAME;
    static final public String CANREG_CLIENT_FOLDER = Globals.CANREG_USER_FOLDER + Globals.FILE_SEPARATOR + Globals.CANREG_CLIENT_FOLDER_NAME;
    static final public String CANREG_PATIENT_PDFS_FOLDER = CANREG_CLIENT_FOLDER + Globals.FILE_SEPARATOR + Globals.PATIENT_TABLE_NAME + "-PDFS";
    static final private String CANREG_SERVER_SYSTEM_CONFIG_FOLDER_NAME = "System";
    static final private String CANREG_SERVER_DATABASE_FOLDER_NAME = "Database";
    static final private String CANREG_BACKUP_FOLDER_NAME = "Backup";
    static public String CANREG_PASSWORD_FILE_NAME = CANREG_SERVER_FOLDER + FILE_SEPARATOR + "passwords.properties";
    static public String CANREG_LEVELS_FILE_NAME = CANREG_SERVER_FOLDER + FILE_SEPARATOR + "levels.properties";
    static public String CANREG_UPDATED_INSTRUCTIONS_URL = "http://www.iacr.com.fr/CanReg5/CanReg5-instructions.pdf";
    static public String CANREG_UPDATED_INSTRUCTIONS_LOCAL_FILE = CANREG_CLIENT_FOLDER + Globals.FILE_SEPARATOR + "CanReg5-instructions.pdf";
    static public String CANREG_LOCAL_LOCKED_RECORDS_FILE_NAME_SUFFIX = "-records.locked";
    static public String CANREG_INSTRUCTIONS_LOCAL_FILE = "CanReg5-instructions.pdf";
    public static String CANREG_CHANGELOG_URL = "http://www.iacr.com.fr/CanReg5/changelog.txt";
    /**
     *
     */
    static public String LOGIN_FILENAME = "./conf/CanRegLogin.conf";
    /**
     *
     */
    static final public String TABLES_CONF_PATH = "./conf/tables";
    static final public String R_SCRIPTS_PATH = TABLES_CONF_PATH + "/r";
    static public String TABLES_PREVIEW_PATH = TABLES_CONF_PATH + "/previews";
    public final static String USER_TABLES_CONF_PATH = CANREG_CLIENT_FOLDER + FILE_SEPARATOR + TABLES_CONF_PATH;
    static final public String USER_R_SCRIPTS_PATH = USER_TABLES_CONF_PATH + "/r";
    static public String USER_TABLES_PREVIEW_PATH = USER_TABLES_CONF_PATH + "/previews";
    static public String DEFAULT_PREVIEW_FILENAME = "blanc.png";
    public static String R_INSTALL_PACKAGES_SCRIPT = R_SCRIPTS_PATH + "/" + "r-packages/install_packages.r";
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
    public static String DEFAULT_SERVER_ADDRESS = "localhost";
    /**
     *
     */
    public static int NUMBER_OF_LINES_IN_IMPORT_PREVIEW = 100;
    /**
     *
     */
    public final static String TUMOUR_TABLE_NAME = "Tumour";
    public final static String PATIENT_TABLE_NAME = "Patient";
    public final static String SOURCE_TABLE_NAME = "Source";
    public final static String USERS_TABLE_NAME = "USERS";
    static final public String TUMOUR_AND_PATIENT_JOIN_TABLE_NAME = TUMOUR_TABLE_NAME + "+" + PATIENT_TABLE_NAME;
    static final public String SOURCE_AND_TUMOUR_JOIN_TABLE_NAME = SOURCE_TABLE_NAME + "+" + TUMOUR_TABLE_NAME;
    public final static String SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME = SOURCE_TABLE_NAME + "+" + TUMOUR_TABLE_NAME + "+" + PATIENT_TABLE_NAME;
    static final public String[] DEFAULT_TABLE_CHOOSER_TABLE_LIST = {
        TUMOUR_TABLE_NAME,
        PATIENT_TABLE_NAME,
        TUMOUR_AND_PATIENT_JOIN_TABLE_NAME,
        SOURCE_TABLE_NAME,
        SOURCE_AND_TUMOUR_JOIN_TABLE_NAME,
        SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME
    };
    static final public String[] DEFAULT_FREQUENCY_BY_YEAR_TABLE_CHOOSER_TABLE_LIST = {
        TUMOUR_TABLE_NAME,
        TUMOUR_AND_PATIENT_JOIN_TABLE_NAME,
        SOURCE_AND_TUMOUR_JOIN_TABLE_NAME,
        SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME
    };
    static public String[] TABLE_LIST = {
        PATIENT_TABLE_NAME,
        TUMOUR_TABLE_NAME,
        SOURCE_TABLE_NAME,};
    /**
     *
     */
    public static String DATE_FORMAT_STRING = "yyyyMMdd";
    
    public static String[] DATE_FORMAT_STRINGS_ARRAY = {
        "yyyyMMdd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy-MM-dd"
    };
   
    public static String DATAENTRY_LANGUAGE_ENGLISH = "en";
    public static String DATAENTRY_LANGUAGE_FRENCH = "fr";
    public static String DATAENTRY_LANGUAGE_SPANISH = "es";
    public static String DATAENTRY_LANGUAGE_ITALIAN = "it";
    public static String DATAENTRY_LANGUAGE_TURKISH = "tr";
    public static String DATAENTRY_LANGUAGE_ROMANIAN = "ro";
    public static String DATAENTRY_LANGUAGE_PORTUGUESE = "pt";
    public static String DATAENTRY_LANGUAGE_CHINESE = "zh";
    public static String DATAENTRY_LANGUAGE_THAI = "th";
    public static String DATAENTRY_LANGUAGE_KOREAN = "ko";
    public static String DATAENTRY_LANGUAGE_ARABIC = "ar";
    public static String DATAENTRY_LANGUAGE_FARSI = "fa";
    public static String DATAENTRY_LANGUAGE_RUSSIAN = "ru";
    public static String DATAENTRY_LANGUAGE_GREEK = "el";
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
    // public static String CANREG_HELP_FILE_NAME = "CanReg5-functionality.html";
    public static int UNDUPLICATION_NOT_DONE_CODE = 0;
    public static int MAX_POPULATION_DATASETS_IN_TABLE = 50;
    public static String OBSOLETE_VALUE = "1";
    public static String NOT_OBSOLETE_VALUE = "0";
    public static int MAX_DICTIONARY_DISPLAY_SIZE = 200000;
    public static int DICTIONARY_DESCRIPTION_LENGTH = 255;
    public static int DICTIONARY_MAX_CODE_LENGTH = 20;
    public static int PDS_FILTER_LENGTH = 255;
    public static int PDS_SOURCE_LENGTH = 255;
    public static int PDS_AGE_GROUP_STRUCTURE_STRING_MAX_LENGTH = 40;
    public static int PDS_DESCRIPTION_LENGTH = 255;
    public static String NAMESEX_TABLE_FIRST_NAME_VARIABLE_NAME = "NAME";
    public static String NAMESEX_TABLE_SEX_VARIABLE_NAME = "SEX";
    public static String NAMESEX_TABLE_NAME = "NAMESEX";
    public static final int MAX_ERROR_LINES = 25;
    public static final String MORPHOLOGICAL_FAMILIES_LOOKUP_FILE_RESOURCE = "/canreg/common/resources/lookup/MorphFam.txt";
    public static final String MUST_LOOKUP_FILE_RESOURCE = "/canreg/common/resources/lookup/Must.txt";
    public static final String MUST_NOT_LOOKUP_FILE_RESOURCE = "/canreg/common/resources/lookup/MustNot.txt";
    public static final String MORPH_FAM_DICT_LOOKUP_FILE_RESOURCE = "/canreg/common/resources/lookup/MorphFamDict.txt";
    public static String newline = System.getProperty("line.separator");
    public static boolean SHOW_GARBLER = false;
    public static int DEFAULT_UNKNOWN_AGE_CODE = 999;
    public static String DD_FILE_PATH = "/canreg/common/ruby/canreg5.dd";
    public static String DD_FILE_VARIABLES_FILE = "/canreg/common/ruby/export_format_canreg5.tsv";

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
        new AgeGroupStructure(5, 80),
        new AgeGroupStructure(5, 75),
        new AgeGroupStructure(5, 70),
        new AgeGroupStructure(5, 65),
        new AgeGroupStructure(5, 60),
        new AgeGroupStructure(10, 75, 15),
        new AgeGroupStructure(10, 65, 15),
        new AgeGroupStructure(5, 15, 15),
        new AgeGroupStructure(5, 15, 1, 15),
        new AgeGroupStructure(5, 20, 5, 20)
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

        TumourID,
        /**
         * Lenght Patient ID + 4
         */
        IncidenceDate,
        /**
         * Formatted as yyyyMMdd
         */
        BirthDate,
        /**
         * Formatted as yyyyMMdd
         */
        Age,
        /**
         * 2 or 3 digits (with age unknown as 99 or 999)
         */
        Sex,
        /**
         * Most registries use 1 and 2, some M and F.
         */
        Topography,
        /**
         * ICD-O-3, only the 3 digits
         */
        Morphology,
        /**
         * ICD-O-3, only the 4 digits
         */
        Behaviour,
        /**
         * ICD-O-3, 1 digit
         */
        BasisDiagnosis,
        /**
         * IACR/IARC standard, 1 digit
         */
        ICD10,
        /**
         * 4 characters (C or D followed by 3 digits)
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
         * Date of last contact, Formatted as yyyyMMdd
         */
        Grade,
        /**
         * ICD-O-3, 1 digit
         */
        ICCC,
        /**
         * Children classification, (max) 4 characters
         */
        AddressCode,
        /**
         * Any number of characters/digits. (Most registries would have 2 or 3
         * digits long, though.)
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
         * Any coding is valid (as of now)
         */
        Source1,
        /**
         * Any coding is valid
         */
        Source2,
        /**
         * Any coding is valid
         */
        Source3,
        /**
         * Any coding is valid
         */
        Source4,
        /**
         * Any coding is valid
         */
        Source5,
        /**
         * Any coding is valid
         */
        Source6,
        /**
         * Any coding is valid
         */
        PatientID,
        /**
         * Most registries would have 8 digits, some characters and some longer
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
        // TumourRecordID,
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
        SourceRecordID,
        /**
         * The age group
         */
        AgeGroup,
        /**
         * The count
         */
        Count,
        /**
         * The count
         */
        VitalStatus
    }
    public static String FILL_IN_STATUS_MANDATORY_STRING = "Mandatory";
    public static String FILL_IN_STATUS_AUTOMATIC_STRING = "Automatic";
    public static String FILL_IN_STATUS_OPTIONAL_STRING = "Optional";
    public static String FILL_IN_STATUS_SYSTEM_STRING = "System";
    public static int PDS_DATABASE_NAME_LENGTH = 100;
    public static int ADDITIONAL_DIGITS_FOR_PATIENT_RECORD = 2;
    public static int ADDITIONAL_DIGITS_FOR_TUMOUR_ID = 2;
    public static int ADDITIONAL_DIGITS_FOR_SOURCE_ID = 2;
    public static int MAX_USERNAME_LENGHT = 16;
    public static String FONT_LATIN = "Latin";
    public static String FONT_ASIAN = "Asian";
    public static String DICTIONARY_TYPE_SIMPLE = "Simple";
    public static String DICTIONARY_TYPE_COMPOUND = "Compound";
    public static String VARIABLE_TYPE_DATE_NAME = "Date";
    public static String VARIABLE_TYPE_ALPHA_NAME = "Alpha";
    public static String VARIABLE_TYPE_NUMBER_NAME = "Number";
    public static String VARIABLE_TYPE_DICTIONARY_NAME = "Dict";
    public static String VARIABLE_TYPE_ASIAN_TEXT_NAME = "AsianText";
    public static String VARIABLE_TYPE_TEXT_AREA_NAME = "TextArea";
    public static String MULTIPLEPRIMARY_COPY_INTERESTING_STRING = "Intr";
    public static String MULTIPLEPRIMARY_COPY_OTHER_STRING = "Othr";
    public static String MULTIPLEPRIMARY_COPY_MUST_STRING = "Must";
    public static String MULTIPLEPRIMARY_COPY_PROBABLY_STRING = "Prob";
    public static String[] REGIONS = new String[]{
        "0 - not assigned",
        "Africa",
        "Americas",
        "EastMed",
        "Europe",
        "SEAsia",
        "West Pacific",
        "7 - not assigned",
        "8 - not assigned",
        "Training"
    };

    public static enum RecordStatusValues {

        Pending,
        Confirmed
    }
    public static String RECORD_STATUS_PENDING_CODE = "0";
    public static String RECORD_STATUS_CONFIRMED_CODE = "1";

    public static enum TurmourCheckStatusValues {

        Invalid,
        OK,
        Rare,
        Missing
    }
    public static String TUMOUR_CHECK_STATUS_INVALID_CODE = "0";
    public static String TUMOUR_CHECK_STATUS_OK_CODE = "1";
    public static String TUMOUR_CHECK_STATUS_RARE_CODE = "2";
    public static String TUMOUR_CHECK_STATUS_MISSING_CODE = "3";

    public static enum PatientUnduplicationStatusValues {
    }
    public static String[] TRANSLATED_LOCALES = {
        "en",
        "fr",
        "pt",
        "pt_PT",
        "es",
        "ru",
        "zh",
        "tr"
    };
}
