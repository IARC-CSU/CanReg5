/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.server.database;

import canreg.common.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryGenerator {

    static final boolean debug = true;
    static final String namespace = "ns3:";

    /**
     *
     * @param tableName
     * @param doc
     * @return
     */
    public static String strCreateVariableTable(String tableName, Document doc) {

        String recordIDVariableName = new String();
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            recordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            recordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            recordIDVariableName = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
        }

        // Common for all tables
        String query = "create table " + Globals.SCHEMA_NAME + "."
                + canreg.common.Tools.toUpperCaseStandardized(tableName)
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
        DatabaseVariablesListElement rangeDatabaseVariableListElement = filter.getRangeDatabaseVariablesListElement();
        if (rangeDatabaseVariableListElement != null) {
            String tableName = rangeDatabaseVariableListElement.getDatabaseTableName();
            String tableRecordIDVariableName = "";

            if (tableName.contains(Globals.SOURCE_TABLE_NAME)) {
                tableRecordIDVariableName = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (tableName.contains(Globals.TUMOUR_TABLE_NAME)) {
                tableRecordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (tableName.contains(Globals.PATIENT_TABLE_NAME)) {
                tableRecordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
            }

            filterString += "APP." + tableName + "." + tableRecordIDVariableName
                    + " IN ( SELECT " + tableRecordIDVariableName
                    + " FROM APP." + tableName
                    + " WHERE ";
            if (filter.getRangeStart() != null && filter.getRangeStart().length() > 0) {
                //<ictl.co>
                if (LocalizationHelper.isPersianLocale()) {
                    String[] years = DateHelper.getGregorianYearsInPersianYear(filter.getRangeStart().replace("'",""));
                    filterString += "(APP.TODATE(" + filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                            + ",'yyyy','en') >= '" + years[0] + "' OR APP.TODATE(" + filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                            + ",'yyyy','en') >= '" + years[1] + "')";
                } else {
                    filterString += "APP.TODATE("+filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                            + ",'yyyy','en') >= " + filter.getRangeStart();
                }
                //</ictl.co>
/*
                filterString += filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                        + " >= " + filter.getRangeStart();
*/
            }
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) && (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
                filterString += " AND ";
            }
            if (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0) {
                //<ictl.co>
                if (LocalizationHelper.isPersianLocale()) {
                    String[] years = DateHelper.getGregorianYearsInPersianYear(filter.getRangeEnd().replace("'",""));
                    filterString += "(APP.TODATE(" + filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                            + ",'yyyy','en') <= '" + years[0] + "' OR APP.TODATE(" + filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                            + ",'yyyy','en') <= '" + years[1] + "')";
                } else {
                    filterString += "APP.TODATE("+filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                            + ",'yyyy','en') <= " + filter.getRangeEnd();
                }
                //</ictl.co>
/*
                filterString += filter.getRangeDatabaseVariablesListElement().getDatabaseVariableName()
                        + " <= " + filter.getRangeEnd();
*/
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
                    query += "\"" + canreg.common.Tools.toUpperCaseStandardized(indexedVariables.get(j)) + "\"";
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
    public static String strCreateTablesOfDictionaries(Document doc) {
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
    public static String strCreateDictionaryTable(Document doc) {
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
    public static String strSaveDictionary() {
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
    public static String strSaveDictionaryEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARY "
                + "   (DICTIONARY, CODE, DESCRIPTION) "
                + "VALUES (?, ?, ?)";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strSaveUser() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".USERS "
                + "   (USERNAME, PASSWORD, USER_LEVEL, EMAIL, REAL_NAME) "
                + "VALUES (?, ?, ?, ?, ?)";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strEditUser() {
        String queryLine = "UPDATE " + Globals.SCHEMA_NAME + ".USERS "
                + "   SET USERNAME = ?, PASSWORD = ?, USER_LEVEL = ?, EMAIL = ?, REAL_NAME = ? "
                + "WHERE ID = ?";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strSavePopoulationDataset() {
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
    public static String strSavePopoulationDatasetsEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".PDSET "
                + "   (PDS_ID, AGE_GROUP, SEX, COUNT) "
                + "VALUES (?, ?, ?, ?)";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strCreatePopulationDatasetTable() {
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
    public static String strCreatePopulationDatasetsTable() {
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
    public static String strCreateUsersTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".USERS ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "USERNAME VARCHAR(255),"
                + "PASSWORD VARCHAR(255),"
                + "USER_LEVEL INT,"
                + "EMAIL VARCHAR(255),"
                + "REAL_NAME VARCHAR(255),"
                + "USER_ROLE INT"
                + " )";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strCreateSystemPropertiesTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".SYSTEM ("
                + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "LOOKUP VARCHAR(255) NOT NULL UNIQUE,"
                + "VALUE VARCHAR(255)"
                + ")";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strCreateNameSexTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + "." + Globals.NAMESEX_TABLE_NAME + " ("
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
    public static String strSaveNameSexEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + "." + Globals.NAMESEX_TABLE_NAME + " "
                + "   (" + Globals.NAMESEX_TABLE_FIRST_NAME_VARIABLE_NAME + ", "
                + Globals.NAMESEX_TABLE_SEX_VARIABLE_NAME + ") "
                + "VALUES (?, ?)";
        return queryLine;
    }

    /**
     *
     * @return
     */
    public static String strDeleteNameSexEntry() {
        String queryLine = "DELETE FROM APP." + Globals.NAMESEX_TABLE_NAME + " "
                + "WHERE " + Globals.NAMESEX_TABLE_FIRST_NAME_VARIABLE_NAME + " = ?";
        return queryLine;
    }

    /**
     *
     * @param doc
     * @return
     */
    public static String strSavePatient(Document doc) {
        return strSaveRecord(doc, "patient");
    }

    /**
     *
     * @param doc
     * @return
     */
    public static String strSaveTumour(Document doc) {
        return strSaveRecord(doc, "tumour");
    }

    /**
     *
     * @param doc
     * @return
     */
    public static String strSaveSource(Document doc) {
        return strSaveRecord(doc, "source");
    }

    static String[] strCreatePatientTablePrimaryKey(String databaseVariableName) {
        LinkedList<String> commands = new LinkedList<String>();
        // drop the current primary key
        commands.add("ALTER TABLE APP.PATIENT DROP PRIMARY KEY");
        // create primary key
        commands.add("ALTER TABLE APP.PATIENT ADD PRIMARY KEY ( " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " , " + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName) + ") ");
        return commands.toArray(new String[0]);
    }

    static String[] strCreateTumourTablePrimaryKey(String databaseVariableName) {
        LinkedList<String> commands = new LinkedList<String>();
        // drop the current primary key
        commands.add("ALTER TABLE APP.TUMOUR DROP PRIMARY KEY");
        // add an unique identifier
        // commands.add("ALTER TABLE APP.TUMOUR DROP UNIQUE (" + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName) + ")");

        commands.add("ALTER TABLE APP.TUMOUR ADD UNIQUE (" + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName) + ")");
        // create primary key
        commands.add("ALTER TABLE APP.TUMOUR ADD PRIMARY KEY ( " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " , " + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName) + ") ");
        return commands.toArray(new String[0]);
    }

    static String[] strCreateSourceTablePrimaryKey(String databaseVariableName) {
        LinkedList<String> commands = new LinkedList<String>();
        // drop the current primary key
        commands.add("ALTER TABLE APP.SOURCE DROP PRIMARY KEY");
        // add an unique identifier
        commands.add("ALTER TABLE APP.SOURCE ADD UNIQUE (" + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName) + ")");

        commands.add("ALTER TABLE APP.SOURCE ALTER COLUMN " + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName) + "  NOT NULL");
        // create primary key
        commands.add("ALTER TABLE APP.SOURCE ADD PRIMARY KEY ( " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME
                + " , " + canreg.common.Tools.toUpperCaseStandardized(databaseVariableName)
                + ")");
        return commands.toArray(new String[0]);
    }

    static String[] strCreateTumourTableForeignKey(String tumourDatabaseVariableNames, String patientDatabaseVariableNames) {
        // set variables unique
        LinkedList<String> commands = new LinkedList<String>();
        // create foreign key
        commands.add("ALTER TABLE APP.TUMOUR ADD FOREIGN KEY ("
                + canreg.common.Tools.toUpperCaseStandardized(tumourDatabaseVariableNames) + ") "
                + "REFERENCES APP.PATIENT ("
                + canreg.common.Tools.toUpperCaseStandardized(patientDatabaseVariableNames) + ") ");
        return commands.toArray(new String[0]);
    }

    static String[] strCreateSourceTableForeignKey(String sourceDatabaseVariableNames, String tumourDatabaseVariableNames) {
        // set variables unique
        LinkedList<String> commands = new LinkedList<String>();
        // create foreign key
        commands.add("ALTER TABLE APP.SOURCE ADD FOREIGN KEY ("
                + canreg.common.Tools.toUpperCaseStandardized(sourceDatabaseVariableNames) + ") "
                + "REFERENCES APP.TUMOUR ("
                + canreg.common.Tools.toUpperCaseStandardized(tumourDatabaseVariableNames) + ") ");
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

    static String strGetRecordsAllTables(GlobalToolBox globalToolBox) {
        String patientRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientRecordIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        return "SELECT * FROM APP.SOURCE, APP.TUMOUR, APP.PATIENT "
                + "WHERE APP.TUMOUR." + tumourIDVariableNameTumourTable + " = APP.SOURCE." + tumourIDVariableNameSourceTable
                + " AND "
                + "APP.TUMOUR." + patientRecordIDVariableNameTumourTable + " = APP.PATIENT." + patientRecordIDVariableNamePatientTable;
    }

    static String strCountRecordsAllTables(GlobalToolBox globalToolBox) {
        String patientRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientRecordIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        return "SELECT COUNT(*) FROM APP.SOURCE, APP.TUMOUR, APP.PATIENT "
                + "WHERE APP.TUMOUR." + tumourIDVariableNameTumourTable + " = APP.SOURCE." + tumourIDVariableNameSourceTable
                + " AND "
                + "APP.TUMOUR." + patientRecordIDVariableNameTumourTable + " = APP.PATIENT." + patientRecordIDVariableNamePatientTable;
    }

    private static String strSaveRecord(Document doc, String tableName) {
        String variableNamesPart = "INSERT INTO " + Globals.SCHEMA_NAME + "."
                + canreg.common.Tools.toUpperCaseStandardized(tableName);
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
                    variableNamesPart += "\""
                            + canreg.common.Tools.toUpperCaseStandardized(element.getElementsByTagName(namespace + "short_name").item(0).getTextContent()) + "\"";
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
        return "SELECT max(\"" + canreg.common.Tools.toUpperCaseStandardized(patientIDVariableNamePatientTable) + "\") FROM APP.PATIENT";
    }

    static String strGetHighestTumourID(GlobalToolBox globalToolBox) {
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        String patientRecordIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        return "SELECT max(\"" + canreg.common.Tools.toUpperCaseStandardized(tumourIDVariableNameTumourTable) + "\") FROM APP.TUMOUR WHERE " + canreg.common.Tools.toUpperCaseStandardized(patientRecordIDVariableNameTumourTable) + " = ?";
    }

    static String strGetHighestPatientRecordID(GlobalToolBox globalToolBox) {
        String patientRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        return "SELECT max(\"" + canreg.common.Tools.toUpperCaseStandardized(patientRecordIDVariableNamePatientTable) + "\") FROM APP.PATIENT WHERE " + canreg.common.Tools.toUpperCaseStandardized(patientIDVariableNamePatientTable) + " = ?";
    }

    static String strGetHighestSourceRecordID(GlobalToolBox globalToolBox) {
        String returnString = "";
        DatabaseVariablesListElement le = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString());
        if (le != null) {
            String sourceRecordIDVariableName = le.getDatabaseVariableName();
            String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
            returnString = "SELECT max(\"" + canreg.common.Tools.toUpperCaseStandardized(sourceRecordIDVariableName) + "\") FROM APP.SOURCE WHERE " + canreg.common.Tools.toUpperCaseStandardized(tumourIDVariableNameSourceTable) + " = ?";
        }
        return returnString;
    }

    /* We don't use tumour record ID...
    static String strGetHighestTumourRecordID(GlobalToolBox globalToolBox){
    String tumourRecordIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordID.toString()).getDatabaseVariableName();
    return "SELECT max(\""+canreg.common.Tools.toUpperCaseStandardized(tumourRecordIDVariableNamePatientTable)+"\") FROM APP.TUMOUR";
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

        String variableNamesPart = "UPDATE " + Globals.SCHEMA_NAME + "."
                + canreg.common.Tools.toUpperCaseStandardized(tableName);
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
                    variableNamesPart += "\""
                            + canreg.common.Tools.toUpperCaseStandardized(element.getElementsByTagName(namespace + "short_name").item(0).getTextContent()) + "\" = ?";
                }
            }
        }
        variableNamesPart += "\nWHERE " + recordIDVariableName + " = ? ";

        return variableNamesPart;
    }

    private static String createVariable(Element element, Document doc) {
        String queryLine = "";

        //Get the variable name for the database
        queryLine = "\""
                + canreg.common.Tools.toUpperCaseStandardized(element.getElementsByTagName("ns3:short_name").item(0).getTextContent()) + "\"";

        //Get the variable type
        String variableType = element.getElementsByTagName("ns3:variable_type").item(0).getTextContent();

        if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME)
                || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME)
                || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)
                /*<ictl.co>*/ || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)/*</ictl.co>*/) {
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
                queryLine += " NOT NULL UNIQUE ";
                // } else if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.TumourRecordID.toString())) {
                //    queryLine += " NOT NULL UNIQUE ";
            } else if (standardVariableName.equalsIgnoreCase(Globals.StandardVariableNames.SourceRecordID.toString())) {
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

    static String strAddColumnToTable(String columnName, String columnType, String table) {
        return "ALTER TABLE APP." + table + " ADD COLUMN " + columnName.toUpperCase() + " " + columnType;
    }

    static String strDropColumnFromTable(String columnName, String table) {
        return "ALTER TABLE APP." + table + " DROP COLUMN " + columnName.toUpperCase();
    }

    static String strSetColumnDataType(String columnName, String columnType, String table) {
        return "ALTER TABLE APP." + table + " ALTER " + columnName.toUpperCase() + " SET DATA TYPE " + columnType;
    }

    //<ictl.co>
    public static String strCreateUtilsToDateFunction() {
        String queryLine = "CREATE FUNCTION " + Globals.SCHEMA_NAME + ".TODATE(DTE VARCHAR(20),PATTERN VARCHAR(20),LOCALE VARCHAR(10)) RETURNS VARCHAR(20)" +
                "  PARAMETER STYLE JAVA NO SQL LANGUAGE  JAVA" +
                "  EXTERNAL NAME 'canreg.server.database.derby.Functions.toDate'";
        return queryLine;
    }
    //</ictl.co>
}
