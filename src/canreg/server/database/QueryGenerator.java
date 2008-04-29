/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import canreg.common.Globals;
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

    public static final String strCreateVariableTable(String tableName, Document doc) {

        // Common for all tables
        String query = "create table " + Globals.SCHEMA_NAME + "." + tableName.toUpperCase() +
                " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";

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

    public static final String strCreateDictionaryTable(Document doc) {
        String queryLine = "create table " + Globals.SCHEMA_NAME + ".DICTIONARY" +
                " ( ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "DICTIONARY INT, " +
                "CODE VARCHAR(20), " +
                "DESCRIPTION VARCHAR(50) " +
                ")";
        return queryLine;
    }

    public static final String strSaveDictionary() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARIES " +
                "   (DICTIONARYID, NAME, FONT, TYPE, CODELENGTH, CATEGORYDESCLENGTH, " +
                "    FULLDICTCODELENGTH, FULLDICTDESCLENGTH) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return queryLine;
    }

    public static final String strSaveDictionaryEntry() {
        String queryLine = "INSERT INTO " + Globals.SCHEMA_NAME + ".DICTIONARY " +
                "   (DICTIONARY, CODE, DESCRIPTION) " +
                "VALUES (?, ?, ?)";
        return queryLine;
    }

    public static final String strSavePatient(Document doc) {
        return strSaveEntry(doc, "patient");
    }

    public static final String strSaveTumour(Document doc) {
        return strSaveEntry(doc, "tumour");
    }

    private static final String strSaveEntry(Document doc, String tableName) {
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
                variableNamesPart += "\""+ element.getElementsByTagName(namespace + "short_name").item(0).getTextContent().toUpperCase()+"\"";
                valuesPart += "?";
            }
        }
        variableNamesPart += ") ";
        valuesPart += ") ";

        return variableNamesPart + valuesPart;
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
