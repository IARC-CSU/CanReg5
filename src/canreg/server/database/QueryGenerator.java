/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import canreg.common.Globals;
import java.util.LinkedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author morten
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

        // Common for all tables
        String query = "create table " + Globals.SCHEMA_NAME + "." + tableName.toUpperCase() +
                // Add the system variables
                // ID is just a variable for the database
                " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)" +
                // NEXT_RECORD_DB_ID is a pointer to the ID of the next version of this record - used only by the database 
                ", NEXT_RECORD_DB_ID INTEGER" +
                // LAST_RECORD_DB_ID is a pointer to the ID of the last version of this record - used only by the database 
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

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                query += ", ";
                query += createVariable(element, doc);
            }
        }

        query += ") ";
        return query;
    }

    static LinkedList<String> strCreateIndexTable(String tableName, Document doc) {
        LinkedList<String> queries = new LinkedList();

        NodeList nodes = doc.getElementsByTagName(namespace + "indexes");
        Element variablesElement = (Element) nodes.item(0);

        NodeList indexes = variablesElement.getElementsByTagName(namespace + "index");

        // Go through all the variable definitions
        for (int i = 0; i < indexes.getLength(); i++) {

            // Get element
            Element element = (Element) indexes.item(i);

            // Create line
            String tableNameDB = element.getElementsByTagName(namespace + "table").item(0).getTextContent().toUpperCase();

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                String nameDB = element.getElementsByTagName(namespace + "name").item(0).getTextContent();

                String query = "create index \"" + nameDB + "_idx\" on " + Globals.SCHEMA_NAME + "." + tableName + " (";
                NodeList variables = element.getElementsByTagName(namespace + "indexed_variable");

                // Go through all the variable definitions
                for (int j = 0; j < variables.getLength(); j++) {
                    Element variableElement = (Element) variables.item(j);
                    if (j > 0) {
                        query += ", ";
                    }
                    query += "\"" + variableElement.getElementsByTagName(namespace + "variable_name").item(0).getTextContent().toUpperCase() + "\"";
                }
                query += ") ";
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
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".DICTIONARIES" +
                " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "DICTIONARYID INT, " +
                "NAME VARCHAR(50), " +
                "FONT VARCHAR(20), " +
                "TYPE VARCHAR(20), " +
                "CODELENGTH INT, " +
                "CATEGORYDESCLENGTH INT, " +
                "FULLDICTCODELENGTH INT, " +
                "FULLDICTDESCLENGTH INT" +
                ")";
        return queryLine;
    }

    /**
     * 
     * @param doc
     * @return
     */
    public static final String strCreateDictionaryTable(Document doc) {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".DICTIONARY" +
                " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "DICTIONARY INT, " +
                "CODE VARCHAR(20), " +
                "DESCRIPTION VARCHAR(50) " +
                ")";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSaveDictionary() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARIES " +
                "   (DICTIONARYID, NAME, FONT, TYPE, CODELENGTH, CATEGORYDESCLENGTH, " +
                "    FULLDICTCODELENGTH, FULLDICTDESCLENGTH) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSaveDictionaryEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARY " +
                "   (DICTIONARY, CODE, DESCRIPTION) " +
                "VALUES (?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSavePopoulationDataset() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".PDSETS " +
                "   (PDS_ID, PDS_NAME, FILTER, DATE, SOURCE,AGE_GROUP_STRUCTURE, " +
                "DESCRIPTION, WORLD_POPULATION_ID, WORLD_POPULATION_BOOL) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSavePopoulationDatasetsEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".PDSET " +
                "   (PDS_ID, AGE_GROUP, SEX, COUNT) " +
                "VALUES (?, ?, ?, ?)";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreatePopulationDatasetTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".PDSETS (" +
                "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "PDS_ID INT not null unique," +
                "PDS_NAME VARCHAR(40)," +
                "FILTER VARCHAR(255)," +
                "DATE INT," +
                "SOURCE VARCHAR(255)," +
                "AGE_GROUP_STRUCTURE VARCHAR(40), " +
                "DESCRIPTION VARCHAR(255), " +
                "WORLD_POPULATION_ID INT, " +
                "WORLD_POPULATION_BOOL INT" +
                " )";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreatePopulationDatasetsTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".PDSET (" +
                "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "PDS_ID INT not null," +
                "AGE_GROUP INT," +
                "SEX INT, " +
                "COUNT INT" +
                " )";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreateUsersTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".USERS (" +
                "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "USERNAME VARCHAR(255)," +
                "PASSWORD VARCHAR(255) FOR BIT DATA NOT NULL," +
                "USER_LEVEL INT," +
                "EMAIL VARCHAR(255)," +
                "REAL_NAME VARCHAR(255)" +
                " )";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strCreateSystemPropertiesTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".SYSTEM (" +
                "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "LOOKUP VARCHAR(255) NOT NULL UNIQUE," +
                "VALUE VARCHAR(255)" +
                ")";
        return queryLine;
    }

    static String strCreateNameSexTable() {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".NAMESEX (" +
                "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "NAME VARCHAR(255) NOT NULL UNIQUE," +
                "SEX INT" +
                ")";
        return queryLine;
    }

    /**
     * 
     * @return
     */
    public static final String strSaveNameSexEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".NAMESEX " +
                "   (NAME, SEX) " +
                "VALUES (?, ?)";
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

    private static String strEditRecord(Document doc, String tableName) {
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

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                if (first) {
                    first = false;
                } else {
                    variableNamesPart += "\n, ";
                }
                variableNamesPart += "\"" + element.getElementsByTagName(namespace + "short_name").item(0).getTextContent().toUpperCase() + "\" = ?";
            }
        }
        variableNamesPart += "\nWHERE ID = ? ";

        return variableNamesPart;
    }

    private static final String createVariable(Element element, Document doc) {
        String queryLine = "";

        //Get the variable name for the database
        queryLine = "\"" + element.getElementsByTagName("ns3:short_name").item(0).getTextContent().toUpperCase() + "\"";

        //Get the variable type
        String variableType = element.getElementsByTagName("ns3:variable_type").item(0).getTextContent();

        if (variableType.equalsIgnoreCase("Alpha") || variableType.equalsIgnoreCase("AsianText")) {
            queryLine += " VARCHAR(";
            queryLine += element.getElementsByTagName("ns3:variable_length").item(0).getTextContent() + ") ";
        } else if (variableType.equalsIgnoreCase("Number") || variableType.equalsIgnoreCase("Date")) {
            queryLine += " INTEGER";
        // queryLine += element.getElementsByTagName("ns3:variable_length").item(0).getTextContent()+") ";
        } else if (variableType.equalsIgnoreCase("Dict")) {
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
        return queryLine;
    }

    private static void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[QueryGenerator] " + msg);
        }
    }
}
