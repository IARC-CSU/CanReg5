/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

/**
 *
 * @author morten
 */
/*
 * CanRegDAO.java
 *
 * Copyright 2006 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html .
 */
import canreg.common.Globals;
import canreg.server.ListEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.*;

/**
 *
 * @author morten (based on code by John O'Conner)
 */
public class CanRegDAO {

    /** Creates a new instance of CanRegDAO */
    public CanRegDAO(Document doc) {
        // Database name should be <= 8 characters long to access them with ODBC
        this("CanReg5", doc);
    }

    public CanRegDAO(String dbName, Document doc) {
        this.doc = doc;

        this.dbName = dbName;

        System.out.println(canreg.server.xml.Tools.getTextContent(new String[]{ns + "canreg", ns + "general", ns + "registry_name"}, doc));

        // Prepare the SQL strings
        strSavePatient = QueryGenerator.strSavePatient(doc);
        strSaveTumour = QueryGenerator.strSaveTumour(doc);
        strSaveDictionary = QueryGenerator.strSaveDictionary();

        setDBSystemDir();
        dbProperties = loadDBProperties();
        String driverName = dbProperties.getProperty("derby.driver");
        loadDatabaseDriver(driverName);
        if (!dbExists()) {
            createDatabase();
            tableOfDictionariesFilled = false;
        }


    }

    // This only works for Embedded databases - will look into it!
    // When using Derby this is OK as we can access it via Embedded 
    // and Client drivers at the same time...
    private boolean dbExists() {
        boolean bExists = false;
        String dbLocation = getDatabaseLocation();
        File dbFileDir = new File(dbLocation);
        if (dbFileDir.exists()) {
            bExists = true;
        }
        return bExists;
    }

    private void setDBSystemDir() {
        // decide on the db system directory
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/.CanReg5DB";
        System.setProperty("derby.system.home", systemDir);

        // create the db system directory
        File fileSystemDir = new File(systemDir);
        fileSystemDir.mkdir();
    }

    private void loadDatabaseDriver(String driverName) {
        // load Derby driver
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private Properties loadDBProperties() {
        InputStream dbPropInputStream = null;
        dbPropInputStream = CanRegDAO.class.getResourceAsStream(Globals.DATABASE_CONFIG);
        dbProperties = new Properties();
        try {
            dbProperties.load(dbPropInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dbProperties;
    }

    private boolean createTables(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();

            // Dynamic creation of tables
            statement.execute(QueryGenerator.strCreateVariableTable("Tumour", doc));
            statement.execute(QueryGenerator.strCreateVariableTable("Patient", doc));
            // Dictionaries part
            statement.execute(QueryGenerator.strCreateDictionaryTable(doc));
            statement.execute(QueryGenerator.strCreateTablesOfDictionaries(doc));
            // System part
            // TODO

            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return bCreatedTables;
    }

    private boolean createDatabase() {

        boolean bCreated = false;
        dbConnection = null;

        String dbUrl = getDatabaseUrl();
        dbProperties.put("create", "true");

        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bCreated = createTables(dbConnection);
        } catch (SQLException ex) {
        }
        dbProperties.remove("create");
        return bCreated;
    }

    public boolean connect() {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            
            //Prepare the SQL statements
            stmtSaveNewPatient = dbConnection.prepareStatement(strSavePatient, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewTumour = dbConnection.prepareStatement(strSaveTumour, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewDictionary = dbConnection.prepareStatement(strSaveDictionary, Statement.RETURN_GENERATED_KEYS);
            //stmtUpdateExistingPatient = dbConnection.prepareStatement(strUpdatePatient);
            stmtGetPatient = dbConnection.prepareStatement(strGetPatient);
            stmtGetPatients = dbConnection.prepareStatement(strGetPatients);
            
            stmtGetTumour = dbConnection.prepareStatement(strGetTumour);
            stmtGetTumours = dbConnection.prepareStatement(strGetTumours);
            
            stmtGetTumour = dbConnection.prepareStatement(strGetDictionary);
            stmtGetTumours = dbConnection.prepareStatement(strGetDictionaries);
            // stmtDeletePatient = dbConnection.prepareStatement(strDeletePatient);

            isConnected = dbConnection != null;

            // Consider moving this function...
            if (isConnected && !tableOfDictionariesFilled) {
                fillDictionariesTable();
            }

            System.out.println("Cocuou from the database connection...");
        } catch (SQLException ex) {
            System.out.println("SQLerror... ");
            ex.printStackTrace();
            isConnected = false;
        }
        return isConnected;
    }

    private String getHomeDir() {
        return System.getProperty("user.home");
    }

    public void disconnect() {
        if (isConnected) {
            String dbUrl = getDatabaseUrl();
            dbProperties.put("shutdown", "true");
            try {
                DriverManager.getConnection(dbUrl, dbProperties);
            } catch (SQLException ex) {
            }
            isConnected = false;
        }
    }

    public String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
        return dbLocation;
    }

    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + dbName;
        return dbUrl;
    }

    private int saveRecord(String tableName, DatabaseRecord record, PreparedStatement stmtSaveNewRecord) {
        int id = -1;
        try {
            stmtSaveNewRecord.clearParameters();

            // Get the dictionaries node in the XML
            NodeList nodes = doc.getElementsByTagName(Globals.NAMESPACE + "variables");
            Element variablesElement = (Element) nodes.item(0);

            NodeList variables = variablesElement.getElementsByTagName(Globals.NAMESPACE + "variable");

            int patientVariableNumber = 0;

            // Go through all the variable definitions
            for (int i = 0; i < variables.getLength(); i++) {
                // Get element
                Element element = (Element) variables.item(i);

                // Create line
                String tableNameDB = element.getElementsByTagName(Globals.NAMESPACE + "table").item(0).getTextContent();

                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    patientVariableNumber++;
                    String variableType = element.getElementsByTagName(Globals.NAMESPACE + "variable_type").item(0).getTextContent();
                    Object obj = record.getVariable(element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent());
                    if (variableType.equalsIgnoreCase("Alpha") || variableType.equalsIgnoreCase("AsianText") || variableType.equalsIgnoreCase("Dict")) {
                        if (obj != null) {
                            String strObj = (String) obj;
                            if (strObj.length() > 0) {
                                stmtSaveNewRecord.setString(patientVariableNumber, strObj);
                            } else {
                                stmtSaveNewRecord.setString(patientVariableNumber, "");
                            }
                        } else {
                            stmtSaveNewRecord.setString(patientVariableNumber, "");
                        }
                    } else if (variableType.equalsIgnoreCase("Number") || variableType.equalsIgnoreCase("Date")) {
                        if (obj != null) {
                            Integer intObj = (Integer) obj;
                            stmtSaveNewRecord.setInt(patientVariableNumber, intObj.intValue());
                        } else {
                            stmtSaveNewRecord.setInt(patientVariableNumber, -1);
                        }
                    }
                }
            }

            int rowCount = stmtSaveNewRecord.executeUpdate();
            ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return id;
    }

    public int savePatient(Patient patient) {
        return saveRecord("Patient", patient, stmtSaveNewPatient);
    }

    public int saveTumour(Tumour tumour) {
        return saveRecord("Tumour", tumour, stmtSaveNewTumour);
    }

    public int saveDictionary(Dictionary dictionary) {
        int id = -1;
        try {
            stmtSaveNewDictionary.clearParameters();

            stmtSaveNewDictionary.setInt(1, dictionary.getDictionaryId());
            stmtSaveNewDictionary.setString(2, dictionary.getName());
            stmtSaveNewDictionary.setString(3, dictionary.getFont());
            stmtSaveNewDictionary.setString(4, dictionary.getType());
            stmtSaveNewDictionary.setInt(5, dictionary.getCodeLength());
            stmtSaveNewDictionary.setInt(6, dictionary.getCategoryDescriptionLength());
            stmtSaveNewDictionary.setInt(7, dictionary.getFullDictionaryCodeLength());
            stmtSaveNewDictionary.setInt(8, dictionary.getFullDictionaryDescriptionLength());

            int rowCount = stmtSaveNewDictionary.executeUpdate();
            ResultSet results = stmtSaveNewDictionary.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return id;
    }

    public boolean editPatient(Patient record) {
        boolean bEdited = false;
        try {
            stmtUpdateExistingPatient.clearParameters();

            stmtUpdateExistingPatient.setString(1, record.getLastName());
            stmtUpdateExistingPatient.setString(2, record.getFirstName());
            stmtUpdateExistingPatient.setString(3, record.getMiddleName());
            stmtUpdateExistingPatient.setString(4, record.getPhone());
            stmtUpdateExistingPatient.setString(5, record.getEmail());
            stmtUpdateExistingPatient.setString(6, record.getAddress1());
            stmtUpdateExistingPatient.setString(7, record.getAddress2());
            stmtUpdateExistingPatient.setString(8, record.getCity());
            stmtUpdateExistingPatient.setString(9, record.getState());
            stmtUpdateExistingPatient.setString(10, record.getPostalCode());
            stmtUpdateExistingPatient.setString(11, record.getCountry());
            stmtUpdateExistingPatient.setInt(12, record.getId());

            stmtUpdateExistingPatient.executeUpdate();
            bEdited = true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return bEdited;

    }

    private boolean fillDictionariesTable() {
        boolean bFilled = false;

        // Get the dictionaries node in the XML
        NodeList nodes = doc.getElementsByTagName(Globals.NAMESPACE + "dictionaries");
        Element variablesElement = (Element) nodes.item(0);

        NodeList dictionaries = variablesElement.getElementsByTagName(Globals.NAMESPACE + "dictionary");

        // Go through all the variable definitions
        for (int i = 0; i < dictionaries.getLength(); i++) {
            Dictionary dic = new Dictionary();

            // Get element
            Element element = (Element) dictionaries.item(i);
            // Create dictionary
            dic.setName(element.getElementsByTagName(Globals.NAMESPACE + "name").item(0).getTextContent());
            dic.setFont(element.getElementsByTagName(Globals.NAMESPACE + "font").item(0).getTextContent());
            dic.setType(element.getElementsByTagName(Globals.NAMESPACE + "type").item(0).getTextContent());

            dic.setCodeLength(element.getElementsByTagName(Globals.NAMESPACE + "code_length").item(0).getTextContent());
            dic.setCategoryDescriptionLength(element.getElementsByTagName(Globals.NAMESPACE + "category_description_length").item(0).getTextContent());
            dic.setFullDictionaryCodeLength(element.getElementsByTagName(Globals.NAMESPACE + "full_dictionary_code_length").item(0).getTextContent());
            dic.setFullDictionaryDescriptionLength(element.getElementsByTagName(Globals.NAMESPACE + "full_dictionary_description_length").item(0).getTextContent());
            saveDictionary(dic);
        }
        return bFilled;
    }

    public boolean deleteRecord(int id) {
        boolean bDeleted = false;
        try {
            stmtDeletePatient.clearParameters();
            stmtDeletePatient.setInt(1, id);
            stmtDeletePatient.executeUpdate();
            bDeleted = true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return bDeleted;
    }

    public boolean deleteRecord(Patient record) {
        int id = record.getId();
        return deleteRecord(id);
    }

    public List<ListEntry> getListEntries() {
        List<ListEntry> listEntries = new ArrayList<ListEntry>();
        Statement queryStatement = null;
        ResultSet results = null;

        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetListEntries);
            while (results.next()) {
                int id = results.getInt(1);
                String lName = results.getString(2);
                String fName = results.getString(3);
                String mName = results.getString(4);

                ListEntry entry = new ListEntry(lName, fName, mName, id);
                listEntries.add(entry);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();

        }

        return listEntries;
    }

    public Patient getRecord(int index) {
        Patient record = null;
        try {
            stmtGetPatient.clearParameters();
            stmtGetPatient.setInt(1, index);
            ResultSet result = stmtGetPatient.executeQuery();
            if (result.next()) {
                String lastName = result.getString("LASTNAME");
                String firstName = result.getString("FIRSTNAME");
                String middleName = result.getString("MIDDLENAME");
                String phone = result.getString("PHONE");
                String email = result.getString("EMAIL");
                String add1 = result.getString("Record1");
                String add2 = result.getString("Record2");
                String city = result.getString("CITY");
                String state = result.getString("STATE");
                String postalCode = result.getString("POSTALCODE");
                String country = result.getString("COUNTRY");
                int id = result.getInt("ID");
                record = new Patient(lastName, firstName, middleName, phone,
                        email, add1, add2, city, state, postalCode,
                        country, id);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return record;
    }
    private Connection dbConnection;
    private Properties dbProperties;
    private boolean isConnected;
    private String dbName;
    private Document doc;
    private boolean tableOfDictionariesFilled = true;
    private PreparedStatement stmtSaveNewPatient;
    private PreparedStatement stmtSaveNewTumour;
    private PreparedStatement stmtSaveNewDictionary;
    private PreparedStatement stmtUpdateExistingPatient;
    private PreparedStatement stmtGetListEntries;
    private PreparedStatement stmtGetPatient;
    private PreparedStatement stmtGetTumour;
    private PreparedStatement stmtGetPatients;
    private PreparedStatement stmtGetTumours;
    private PreparedStatement stmtGetRecord;
    private PreparedStatement stmtGetRecords;
    private PreparedStatement stmtGetDictionary;
    private PreparedStatement stmtDeletePatient;
    private PreparedStatement stmtDeleteTumour;
    private String ns = "ns3:";
    
    private static final String strGetPatient =
            "SELECT * FROM APP.PATIENT " +
            "WHERE ID = ?";
     private String strGetPatients =
            "SELECT * FROM APP.PATIENT";
     
     private static final String strGetTumour =
            "SELECT * FROM APP.TUMOUR " +
            "WHERE ID = ?";
     private String strGetTumours =
            "SELECT * FROM APP.TUMOUR";
     
     private static final String strGetDictionary =
            "SELECT * FROM APP.DICTIONARIES " +
            "WHERE ID = ?";
     private String strGetDictionaries =
            "SELECT * FROM APP.DICTIONARIES ";

    private static final String strGetListEntries =
            "SELECT ID, LASTNAME, FIRSTNAME, MIDDLENAME FROM APP.PATIENT " +
            "ORDER BY LASTNAME ASC";
    private static final String strDeletePatient =
            "DELETE FROM APP.PATIENT " +
            "WHERE ID = ?";
    private static final String strDeleteTumour =
            "DELETE FROM APP.TUMOUR " +
            "WHERE ID = ?";
    private static final String strDeleteDictionary =
            "DELETE FROM APP.DICTIONARY " +
            "WHERE ID = ?";
    
    // The Dynamic ones
    private String strSavePatient;
    private String strSaveTumour;
    private String strSaveDictionary;
}

