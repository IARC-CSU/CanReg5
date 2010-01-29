package canreg.server.database;

import canreg.common.DatabaseFilter;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author ervikm
 */
public class QueryGenerator {

    static final boolean debug = true;
    static final String namespace = "ns3:";

    /**
     * 
     * @param tableName
     * @param doc
     * @return
     */
    public static final String strCreateVariableTable(String tableName, Document doc) {

        String recordIDVariableName = new String();
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            recordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            recordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            recordIDVariableName = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
        }

        // Common for all tables
        String query = "create table " + Globals.SCHEMA_NAME + "." + tableName.toUpperCase()
                + // Add the system variables
                // ID is just a variable for the database
                " ( " + recordIDVariableName + " INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)"
                + // NEXT_RECORD_DB_ID is a pointer to the ID of the next version of this record - used only by the database
                ", NEXT_RECORD_DB_ID INTEGER"
                + // LAST_RECORD_DB_ID is a pointer to the ID of the last version of this record - used only by the database
                ", LAST_RECORD_DB_ID INTEGER";

        // Get the variables node in the XML
        NodeList nodes = doc.getElementsByTagName(namespace + "variables");
        Element variablesElement = (Element) nodes.item(0);

        NodeList variables = variablesElement.getElementsByTagName(namespace + "variable");

        // Go through all the variable definitions
        for (int i = 0; i < variables.getLength(); i++) {

            // Get element
            Element element = (Element) variables.item(i);

            // Create line
            String tableNameDB = element.getElementsByTagName(namespace + "table").item(0).getTextContent();
            String variableType = element.getElementsByTagName(namespace + "variable_type").item(0).getTextContent();
            if (!"Meta".equalsIgnoreCase(variableType)) {
                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    query += ", ";
                    query += createVariable(element, doc);
                }
            }
        }

        query += ") ";
        return query;
    }

    static String buildRangePart(DatabaseFilter filter) {
        String filterString = "";
        DatabaseIndexesListElement rangeDBile = filter.getRangeDatabaseIndexedListElement();
        if (rangeDBile != null) {
            String tableName = rangeDBile.getDatabaseTableName();
            String tableRecordIDVariableName = "";

            if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                tableRecordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                tableRecordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
            }

            filterString += "APP." + tableName + "." + tableRecordIDVariableName
                    + " IN ( SELECT " + tableRecordIDVariableName
                    + " FROM APP." + tableName
                    + " WHERE ";
            if (filter.getRangeStart() != null && filter.getRangeStart().length() > 0) {
                filterString += filter.getRangeDatabaseIndexedListElement().getMainVariable()
                        + " >= " + filter.getRangeStart();
            }
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) && (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
                filterString += " AND ";
            }
            if (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0) {
                filterString += filter.getRangeDatabaseIndexedListElement().getMainVariable()
                        + " <= " + filter.getRangeEnd();
            }
            filterString += ")";
        }
        return filterString;

    }

    static LinkedList<String> strCreateIndexTable(String tableName, Document doc) {
        LinkedList<String> queries = new LinkedList();
        DatabaseIndexesListElement[] indexes = canreg.common.Tools.getIndexesListElements(doc, namespace);
        // Go through all the indexes definitions...
        for (DatabaseIndexesListElement index : indexes) {
            if (tableName.equalsIgnoreCase(index.getDatabaseTableName())) {
                String query = "create index \"" + index.getIndexName() + "_idx\" on " + Globals.SCHEMA_NAME + "." + tableName + " (";
                // Go through all database variables in the index...
                LinkedList<String> indexedVariables = index.getVariableNamesInIndex();
                for (int j = 0; j < indexedVariables.size(); j++) {
                    if (j > 0) {
                        query += ", ";
                    }
                    query += "\"" + indexedVariables.get(j) + "\"";
                }
                query += ") ";
                debugOut(query);
                queries.add(query);
            }
        }
        return queries;
    }

    /**
     * 
     * @param doc
     * @return
     */
    public static final String strCreateTablesOfDictionaries(Document doc) {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".DICTIONARIES"
                + " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "DICTIONARYID INT, "
                + "NAME VARCHAR(50), "
                + "FONT VARCHAR(20), "
                + "TYPE VARCHAR(20), "
                + "CODELENGTH INT, "
                + "CATEGORYDESCLENGTH INT, "
                + "FULLDICTCODELENGTH INT, "
                + "FULLDICTDESCLENGTH INT"
                + ")";
        return queryLine;
    }

    /**
     * 
     * @param doc
     * @return
     */
    public static final String strCreateDictionaryTable(Document doc) {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".DICTIONARY"
                + " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "DICTIONARY INT, "
                + "CODE VARCHAR(" + Globals.DICTIONARY_MAX_CODE_LENGTH + "), "
                + "DESCRIPTION VARCHAR(" + Globals.DICTIONARY_DESCRIPTION_LENGTH + ") " + // How long should we allow the labels to be?
                ")";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSaveDictionary() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARIES "
                + "   (DICTIONARYID, NAME, FONT, TYPE, CODELENGTH, CATEGORYDESCLENGTH, "
                + "    FULLDICTCODELENGTH, FULLDICTDESCLENGTH) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSaveDictionaryEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARY "
                + "   (DICTIONARY, CODE, DESCRIPTION) "
                + "VALUES (?, ?, ?)";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static final String strSaveUser() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".USERS "
                + "   (USERNAME, PASSWORD, USER_LEVEL, EMAIL, REAL_NAME) "
                + "VALUES (?, ?, ?, ?, ?)";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static final String strEditUser() {
        String queryLine = "UPDATE " + Globals.SCHEMA_NAME + ".USERS "
                + "   SET USERNAME = ?, PASSWORD = ?, USER_LEVEL = ?, EMAIL = ?, REAL_NAME = ? "
                + "WHERE ID = ?";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSavePopoulationDataset() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".PDSETS "
                + "   (PDS_ID, PDS_NAME, FILTER, DATE, SOURCE,AGE_GROUP_STRUCTURE, "
                + "DESCRIPTION, WORLD_POPULATION_ID, WORLD_POPULATION_BOOL) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSavePopoulationDatasetsEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".PDSET "
                + "   (PDS_ID, AGE_GROUP, SEX, COUNT) "
                + "VALUES (?, ?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreatePopulationDatasetTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".PDSETS ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "PDS_ID INT not null unique,"
                + "PDS_NAME VARCHAR(" + Globals.PDS_DATABASE_NAME_LENGTH + "),"
                + "FILTER VARCHAR(" + Globals.PDS_FILTER_LENGTH + "),"
                + "DATE VARCHAR(" + Globals.DATE_FORMAT_STRING.length() + "),"
                + "SOURCE VARCHAR(" + Globals.PDS_SOURCE_LENGTH + "),"
                + "AGE_GROUP_STRUCTURE VARCHAR(" + Globals.PDS_AGE_GROUP_STRUCTURE_STRING_MAX_LENGTH + "), "
                + "DESCRIPTION VARCHAR(" + Globals.PDS_DESCRIPTION_LENGTH + "), "
                + "WORLD_POPULATION_ID INT, "
                + "WORLD_POPULATION_BOOL INT"
                + " )";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreatePopulationDatasetsTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".PDSET ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "PDS_ID INT not null,"
                + "AGE_GROUP INT,"
                + "SEX INT, "
                + "COUNT INT"
                + " )";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreateUsersTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".USERS ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "USERNAME VARCHAR(255),"
                + "PASSWORD VARCHAR(255),"
                + "USER_LEVEL INT,"
                + "EMAIL VARCHAR(255),"
                + "REAL_NAME VARCHAR(255)"
                + " )";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreateSystemPropertiesTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".SYSTEM ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "LOOKUP VARCHAR(255) NOT NULL UNIQUE,"
                + "VALUE VARCHAR(255)"
                + ")";
        return queryLine;
    }

    static String strCreateNameSexTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".NAMESEX ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "NAME VARCHAR(255) NOT NULL UNIQUE,"
                + "SEX INT"
                + ")";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSaveNameSexEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".NAMESEX "
                + "   (NAME, SEX) "
                + "VALUES (?, ?)";
        return queryLine;
    }

    /**
     * 
     * @param doc
     * @return
     */
    public static final String strSavePatient(Document doc) {
        return strSaveRecord(doc, "patient");
    }

    /**
     * 
     * @param doc
     * @return
     */
    public static final String strSaveTumour(Document doc) {
        return strSaveRecord(doc, "tumour");
    }

    /**
     *
     * @param doc
     * @return
     */
    public static final String strSaveSource(Document doc) {
        return strSaveRecord(doc, "source");
    }

    static String[] strCreatePatientTablePrimaryKey(String databaseVariableName) {
        LinkedList<String> commands = new LinkedList<String>();
        // drop the current primary key
        commands.add("ALTER TABLE APP.PATIENT DROP PRIMARY KEY");
        // create primary key
        commands.add("ALTER TABLE APP.PATIENT ADD PRIMARY KEY ( " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " , " + databaseVariableName.toUpperCase() + ") ");
        return commands.toArray(new String[0]);
    }

    static String[] strCreateTumourTableForeignKey(String tumourDatabaseVariableNames, String patientDatabaseVariableNames) {
        // set variables unique
        LinkedList<String> commands = new LinkedList<String>();
        // create foreign key
        commands.add("ALTER TABLE APP.TUMOUR ADD FOREIGN KEY (" + tumourDatabaseVariableNames.toUpperCase() + ") "
                + "REFERENCES APP.PATIENT (" + patientDatabaseVariableNames.toUpperCase() + ") ");
        return commands.toArray(new String[0]);
    }

    static String strGetPatientsAndTumours(GlobalToolBox globalToolBox) {
        String patientRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientRecordIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        return "SELECT * FROM APP.TUMOUR, APP.PATIENT "
                + "WHERE APP.TUMOUR." + patientRecordIDVariableNameTumourTable + "= APP.PATIENT." + patientRecordIDVariableNamePatientTable;
    }

    static String strCountPatientsAndTumours(GlobalToolBox globalToolBox) {
        String patientRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientRecordIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        return "SELECT COUNT(*) FROM APP.TUMOUR, APP.PATIENT "
                + "WHERE APP.TUMOUR." + patientRecordIDVariableNameTumourTable + "= APP.PATIENT." + patientRecordIDVariableNamePatientTable;
    }

    static String strGetSourcesAndTumours(GlobalToolBox globalToolBox) {
        String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        return "SELECT * FROM APP.SOURCE, APP.TUMOUR "
                + "WHERE APP.TUMOUR." + tumourIDVariableNameTumourTable + "= APP.SOURCE." + tumourIDVariableNameSourceTable;
    }

    static String strCountSourcesAndTumours(GlobalToolBox globalToolBox) {
        String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        return "SELECT COUNT(*) FROM APP.SOURCE, APP.TUMOUR "
                + "WHERE APP.TUMOUR." + tumourIDVariableNameTumourTable + "= APP.SOURCE." + tumourIDVariableNameSourceTable;
    }

    private static final String strSaveRecord(Document doc, String tableName) {
        String variableNamesPart = "INSERT INTO " + Globals.SCHEMA_NAME + "." + tableName.toUpperCase();
        String valuesPart = "VALUES ";
        // Get the variables node in the XML
        NodeList nodes = doc.getElementsByTagName(namespace + "variables");
        Element variablesElement = (Element) nodes.item(0);

        NodeList variables = variablesElement.getElementsByTagName(namespace + "variable");

        variableNamesPart += " (";
        valuesPart += " (";
        // Go through all the variable definitions
        boolean first = true;
        for (int i = 0; i < variables.getLength(); i++) {
            // Get element
            Element element = (Element) variables.item(i);

            // Create line
            String tableNameDB = element.getElementsByTagName(namespace + "table").item(0).getTextContent();
            String variableType = element.getElementsByTagName(namespace + "variable_type").item(0).getTextContent();
            if (!"Meta".equalsIgnoreCase(variableType)) {
                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    if (first) {
                        first = false;
                    } else {
                        variableNamesPart += ", ";
                        valuesPart += ", ";
                    }
                    variableNamesPart += "\"" + element.getElementsByTagName(namespace + "short_name").item(0).getTextContent().toUpperCase() + "\"";
                    valuesPart += "?";
                }
            }
        }
        variableNamesPart += ") ";
        valuesPart += ") ";

        return variableNamesPart + valuesPart;
    }

    static String strEditPatient(Document doc) {
        return strEditRecord(doc, "patient");
    }

    static String strEditTumour(Document doc) {
        return strEditRecord(doc, "tumour");
    }

    static String strEditSource(Document doc) {
        return strEditRecord(doc, "source");
    }

    static String strGetHighestPatientID(GlobalToolBox globalToolBox) {
        String patientIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        return "SELECT max(\"" + patientIDVariableNamePatientTable.toUpperCase() + "\") FROM APP.PATIENT";
    }

    static String strGetHighestTumourID(GlobalToolBox globalToolBox) {
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        String patientRecordIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        return "SELECT max(\"" + tumourIDVariableNameTumourTable.toUpperCase() + "\") FROM APP.TUMOUR WHERE " + patientRecordIDVariableNameTumourTable.toUpperCase() + " = ?";
    }

    static String strGetHighestPatientRecordID(GlobalToolBox globalToolBox) {
        String patientRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        return "SELECT max(\"" + patientRecordIDVariableNamePatientTable.toUpperCase() + "\") FROM APP.PATIENT WHERE " + patientIDVariableNamePatientTable.toUpperCase() + " = ?";
    }

    static String strGetHighestSourceRecordID(GlobalToolBox globalToolBox) {
        String returnString = "";
        DatabaseVariablesListElement le = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString());
        if (le != null) {
            String sourceRecordIDVariableName = le.getDatabaseVariableName();
            String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
            returnString = "SELECT max(\"" + sourceRecordIDVariableName.toUpperCase() + "\") FROM APP.SOURCE WHERE " + tumourIDVariableNameSourceTable.toUpperCase() + " = ?";
        }
        return returnString;
    }

    /* We don't use tumour record ID...
    static String strGetHighestTumourRecordID(GlobalToolBox globalToolBox){
    String tumourRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordID.toString()).getDatabaseVariableName();
    return "SELECT max(\""+tumourRecordIDVariableNamePatientTable.toUpperCase()+"\") FROM APP.TUMOUR";
    }
     */
    private static String strEditRecord(Document doc, String tableName) {

        String recordIDVariableName = "ID";
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            recordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            recordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            recordIDVariableName = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
        }

        String variableNamesPart = "UPDATE " + Globals.SCHEMA_NAME + "." + tableName.toUpperCase();
        // Get the variables node in the XML
        NodeList nodes = doc.getElementsByTagName(namespace + "variables");
        Element variablesElement = (Element) nodes.item(0);

        NodeList variables = variablesElement.getElementsByTagName(namespace + "variable");

        variableNamesPart += " SET ";
        // Go through all the variable definitions
        boolean first = true;
        for (int i = 0; i < variables.getLength(); i++) {
            // Get element
            Element element = (Element) variables.item(i);

            // Create line
            String tableNameDB = element.getElementsByTagName(namespace + "table").item(0).getTextContent();
            String variableType = element.getElementsByTagName(namespace + "variable_type").item(0).getTextContent();
            if (!"Meta".equalsIgnoreCase(variableType)) {
                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    if (first) {
                        first = false;
                    } else {
                        variableNamesPart += "\n, ";
                    }
                    variableNamesPart += "\"" + element.getElementsByTagName(namespace + "short_name").item(0).getTextContent().toUpperCase() + "\" = ?";
                }
            }
        }
        variableNamesPart += "\nWHERE " + recordIDVariableName + " = ? ";

        return variableNamesPart;
    }

    private static final String createVariable(Element element, Document doc) {
        String queryLine = "";

        //Get the variable name for the database
        queryLine = "\"" + element.getElementsByTagName("ns3:short_name").item(0).getTextContent().toUpperCase() + "\"";

        //Get the variable type
        String variableType = element.getElementsByTagName("ns3:variable_type").item(0).getTextContent();

        if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME)
                || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME)
                || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)) {
            queryLine += " VARCHAR(";
            queryLine += element.getElementsByTagName("ns3:variable_length").item(0).getTextContent() + ") ";
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
            queryLine += " INTEGER";
            // queryLine += element.getElementsByTagName("ns3:variable_length").item(0).getTextContent()+") ";
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
            queryLine += " VARCHAR(8)";
            // queryLine += element.getElementsByTagName("ns3:variable_length").item(0).getTextContent()+") ";
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME)) {
            queryLine += " VARCHAR(";
            String dictionaryName = element.getElementsByTagName("ns3:use_dictionary").item(0).getTextContent();
            NodeList nl = doc.getElementsByTagName("ns3:dictionary");
            boolean found = false;
            int i = 0;
            Element dictionaryElement = null;
            while (!found && i < nl.getLength()) {
                dictionaryElement = (Element) nl.item(i++);
                found = dictionaryElement.getElementsByTagName("ns3:name").item(0).getTextContent().equalsIgnoreCase(dictionaryName);
            }
            if (found) {
                queryLine += dictionaryElement.getElementsByTagName("ns3:full_dictionary_code_length").item(0).getTextContent() + ") ";
            }
        }
        // unique or not null? Move to XML?
        NodeList standardVariableNodeList = element.getElementsByTagName(namespace + "standard_variable_name");

        if (standardVariableNodeList.getLength() > 0) {
            String standardVariableName = standardVariableNodeList.item(0).getTextContent();
            if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())) {
                queryLine += " NOT NULL ";
            } else if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.PatientRecordID.toString())) {
                queryLine += " NOT NULL UNIQUE ";
            } else if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.PatientID.toString())) {
                queryLine += " NOT NULL ";
            } else if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.TumourID.toString())) {
                queryLine += " NOT NULL ";
            } else if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.TumourRecordID.toString())) {
                queryLine += " NOT NULL UNIQUE ";
            }
        }
        return queryLine;
    }

    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(QueryGenerator.class.getName()).log(Level.INFO, msg);
        }
    }

    static String strMaxNumberOfSourcesPerTumourRecord(GlobalToolBox globalToolBox) {
        String tumourRecordIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        return "SELECT COUNT(*) AS N FROM APP.SOURCE GROUP BY " + tumourRecordIDVariableNameSourceTable + " ORDER BY N DESC";
    }
}
