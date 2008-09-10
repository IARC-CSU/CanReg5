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
import cachingtableapi.DistributedTableDataSource;
import cachingtableapi.DistributedTableDescription;
import canreg.common.DatabaseFilter;
import canreg.common.Globals;
import canreg.server.ListEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

import java.util.ArrayList;
// import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.w3c.dom.*;

/**
 *
 * @author morten (based on code by John O'Conner)
 */
public class CanRegDAO {

    /**
     * 
     * @param dbName
     * @param doc
     */
    public CanRegDAO(String dbName, Document doc) {
        this.doc = doc;

        this.dbName = dbName;

        distributedDataSources = new LinkedHashMap<Subject, DistributedTableDataSource>();

        System.out.println(canreg.server.xml.Tools.getTextContent(new String[]{ns + "canreg", ns + "general", ns + "registry_name"}, doc));

        // Prepare the SQL strings
        strSavePatient = QueryGenerator.strSavePatient(doc);
        strSaveTumour = QueryGenerator.strSaveTumour(doc);
        strEditPatient = QueryGenerator.strEditPatient(doc);
        strEditTumour = QueryGenerator.strEditTumour(doc);
        strSaveDictionary = QueryGenerator.strSaveDictionary();
        strSaveDictionaryEntry = QueryGenerator.strSaveDictionaryEntry();

        setDBSystemDir();
        dbProperties = loadDBProperties();
        String driverName = dbProperties.getProperty("derby.driver");
        loadDatabaseDriver(driverName);
        if (!dbExists()) {
            createDatabase();
            tableOfDictionariesFilled = false;
        }
    }

    public Map<Integer, Map<String, String>> getDictionary() {
        Map<Integer, Map<String, String>> dictionaryMap = new LinkedHashMap<Integer, Map<String, String>>();
        Statement queryStatement = null;
        ResultSet results = null;

        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetDictionaryEntries);
            while (results.next()) {
                int id = results.getInt(1);
                Integer dictionary = results.getInt(2);
                String code = results.getString(3);
                String desc = results.getString(4);
                Map dic = dictionaryMap.get(dictionary);
                if (dic == null) {
                    dic = new LinkedHashMap<String, String>();
                }
                dictionaryMap.put(dictionary, dic);
                dic.put(code, desc);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return dictionaryMap;
    }

    public DistributedTableDescription getDistributedTableDescriptionAndInitiateDatabaseQuery(Subject theUser, DatabaseFilter filter, String tableName) throws SQLException, Exception {
        distributedDataSources.remove(theUser);
        ResultSet result;
        Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        int rowCount = 0;
        DistributedTableDataSource dataSource;

        if (DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR.equals(filter.getQueryType())) {
            String filterString = filter.getFilterString();
            String query = "";
            if (!filterString.isEmpty()) {
                filterString = " AND " + filterString;
            }
            String[] variables = filter.getDatabaseVariables();
            String variablesList = "";
            if (variables.length > 0) {

                for (String variable : variables) {
                    if (variable != null) {
                        variablesList += ", " + variable;
                    }
                }
            // variablesList = variablesList.substring(0, variablesList.length() - 2);

            }

            query = "SELECT INCID/10000 " + variablesList + ", COUNT(*) as Cases " +
                    "FROM APP.TUMOUR, APP.PATIENT " +
                    "WHERE APP.PATIENT.ID = APP.TUMOUR.PATIENTID " + filterString + " " +
                    "GROUP BY INCID/10000 " + variablesList + " " +
                    "ORDER BY INCID/10000 " + variablesList;
            System.out.print(query);
            result = statement.executeQuery(query);

        } else if (tableName.equalsIgnoreCase("tumour")) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " WHERE " + filterString;
            }
            ResultSet countRowSet = statement.executeQuery(strCountTumours + filterString);
            if (countRowSet.next()) {
                rowCount = countRowSet.getInt(1);
            }
            result = statement.executeQuery(strGetTumours + filterString);
        } else if (tableName.equalsIgnoreCase("patient")) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " WHERE " + filterString;
            }
            ResultSet countRowSet = statement.executeQuery(strCountPatients + filterString);
            if (countRowSet.next()) {
                rowCount = countRowSet.getInt(1);
            }
            result = statement.executeQuery(strGetPatients + filterString);
        } else if (tableName.equalsIgnoreCase("both")) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " AND " + filterString.trim();
            }
            ResultSet countRowSet = statement.executeQuery(strCountPatientsAndTumours + filterString);
            if (countRowSet.next()) {
                rowCount = countRowSet.getInt(1);
            }
            result = statement.executeQuery(strGetPatientsAndTumours + filterString);
        } else {
            throw new Exception("Unknown table name.");
        }
        if (rowCount > 0) {
            dataSource = new DistributedTableDataSourceResultSetImpl(rowCount, result);
        } else {
            dataSource = new DistributedTableDataSourceResultSetImpl(result);
        }

        DistributedTableDescription tableDescription = dataSource.getTableDescription();
        //distributedDataSources.put(tableDescription, dataSource);
        distributedDataSources.put(theUser, dataSource);
        System.out.println(tableDescription.toString());
        return tableDescription;
    }

    public DatabaseRecord getRecord(int recordID, String tableName) {
        if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            return getPatient(recordID);
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            return getTumour(recordID);
        } else {
            return null;
        }
    }

    /**
     * Perform backup of the database
     * @return Path to backup
     */
    public String performBackup() {
        String path = null;
        try {
            path = canreg.server.database.derby.Backup.backUpDatabase(dbConnection, Globals.CANREG_BACKUP_FOLDER + Globals.FILE_SEPARATOR + dbName);
        // TODO - record date of last backup somewhere...
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return path;
    }

    public Object[][] retrieveRows(Subject theUser, int from, int to) throws Exception {
        // DistributedTableDataSource ts = distributedDataSources.get(temporaryGlobalDescription);
        DistributedTableDataSource ts = distributedDataSources.get(theUser);
        if (ts != null) {
            return ts.retrieveRows(from, to);
        } else {
            return null;
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
        String systemDir = Globals.CANREG_SERVER_DATABASE_FOLDER;
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

    /**
     * Restore database from backup.
     * @param path to database backup.
     * @return true if successfull, false if not
     */
    public String restoreFromBackup(String path) {
        boolean bRestored = false, shutdownSuccess = false;
        SQLException ex = null;
        String dbUrl = getDatabaseUrl();

        try {
            dbConnection.close(); // Close current connection.
            dbProperties.put("shutdown", "true");
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
        } catch (SQLException e) {
            if (e.getSQLState().equals("08006")) {
                shutdownSuccess = true; // single db.
            }
            ex = e;
        }
        if (!shutdownSuccess) {
            dbProperties.remove("shutdown");
            ex.printStackTrace();
            // ((DonMan) parent).signalError("Error during shutdown for RESTORE: ", ex,
            //        "in: DonDao.restore", false);
            return "shutdown failed";
        }
        try {
            dbProperties.remove("shutdown");
            dbConnection.close(); // Close current connection.
            dbProperties.put("restoreFrom", path + "/" + dbName);
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bRestored = true;
        } catch (SQLException e) {
            e.printStackTrace();
        //((DonMan) parent).signalError("Error during RESTORE: ", e,
        //       "in: DonDao.restore", false);
        }
        dbProperties.remove("restoreFrom");
        connect(); // Reconnect.
        if (bRestored) {
            return "success";
        } else {
            return "failed";
        }
    }

    /**
     * 
     * @return 
     */
    public boolean connect() {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);

            //Prepare the SQL statements
            stmtSaveNewPatient = dbConnection.prepareStatement(strSavePatient, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewTumour = dbConnection.prepareStatement(strSaveTumour, Statement.RETURN_GENERATED_KEYS);
            stmtEditPatient = dbConnection.prepareStatement(strEditPatient, Statement.RETURN_GENERATED_KEYS);
            stmtEditTumour = dbConnection.prepareStatement(strEditTumour, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewDictionary = dbConnection.prepareStatement(strSaveDictionary, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewDictionaryEntry = dbConnection.prepareStatement(strSaveDictionaryEntry, Statement.RETURN_GENERATED_KEYS);
            stmtDeleteDictionaryEntries = dbConnection.prepareStatement(strDeleteDictionaryEntries);
            //stmtUpdateExistingPatient = dbConnection.prepareStatement(strUpdatePatient);
            stmtGetPatient = dbConnection.prepareStatement(strGetPatient);
            stmtGetPatients = dbConnection.prepareStatement(strGetPatients);
            stmtGetPatientsAndTumours = dbConnection.prepareStatement(strGetPatientsAndTumours);

            stmtGetTumour = dbConnection.prepareStatement(strGetTumour);
            stmtGetTumours = dbConnection.prepareStatement(strGetTumours);

            stmtGetDictionary = dbConnection.prepareStatement(strGetDictionary);
            // stmtGetDictionaries = dbConnection.prepareStatement(strGetDictionaries);
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

    /**
     * 
     */
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

    /**
     * 
     * @return String location of the database
     */
    public String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
        return dbLocation;
    }

    /**
     * 
     * @return
     */
    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + dbName;
        return dbUrl;
    }

    private synchronized int saveRecord(String tableName, DatabaseRecord record, PreparedStatement stmtSaveNewRecord) {
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

    /**
     * 
     * @param patient
     * @return
     */
    public int savePatient(Patient patient) {
        return saveRecord("Patient", patient, stmtSaveNewPatient);
    }

    /**
     * 
     * @param tumour
     * @return
     */
    public int saveTumour(Tumour tumour) {
        return saveRecord("Tumour", tumour, stmtSaveNewTumour);
    }

    /**
     * 
     * @param dictionary
     * @return
     */
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

    /**
     * 
     * @param dictionary
     * @return
     */
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        int id = -1;
        try {
            stmtSaveNewDictionaryEntry.clearParameters();

            stmtSaveNewDictionaryEntry.setInt(1, dictionaryEntry.getDictionaryID());
            stmtSaveNewDictionaryEntry.setString(2, dictionaryEntry.getCode());
            stmtSaveNewDictionaryEntry.setString(3, dictionaryEntry.getDescription());

            int rowCount = stmtSaveNewDictionaryEntry.executeUpdate();
            ResultSet results = stmtSaveNewDictionaryEntry.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return id;
    }

    public boolean deleteDictionaryEntries(int dictionaryID) {
        boolean success = false;
        try {
            stmtDeleteDictionaryEntries.clearParameters();
            stmtDeleteDictionaryEntries.setInt(1, dictionaryID);

            stmtDeleteDictionaryEntries.executeUpdate();
            success = true;

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return success;
    }

     /**
     * 
     * @param patient
     * @return
     */
    public boolean editPatient(Patient patient) {
        return editRecord("Patient", patient, stmtEditPatient);
    }

    /**
     * 
     * @param tumour
     * @return
     */
    public boolean editTumour(Tumour tumour) {
        return editRecord("Tumour", tumour, stmtEditTumour);
    }
    
     /*
     * 
     * @param record
     * @return
     */
    public synchronized boolean editRecord(String tableName, DatabaseRecord record, PreparedStatement stmtEditRecord) {
        boolean bEdited = false;
   
        int id = -1;
        try {
            stmtEditRecord.clearParameters();

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
                                stmtEditRecord.setString(patientVariableNumber, strObj);
                            } else {
                                stmtEditRecord.setString(patientVariableNumber, "");
                            }
                        } else {
                            stmtEditRecord.setString(patientVariableNumber, "");
                        }
                    } else if (variableType.equalsIgnoreCase("Number") || variableType.equalsIgnoreCase("Date")) {
                        if (obj != null) {
                            Integer intObj = (Integer) obj;
                            stmtEditRecord.setInt(patientVariableNumber, intObj.intValue());
                        } else {
                            stmtEditRecord.setInt(patientVariableNumber, -1);
                        }
                    }
                }
            }
            // add the ID
            
            int idInt = (Integer) record.getVariable("id");
            stmtEditRecord.setInt(patientVariableNumber+1,idInt);
 
            int rowCount = stmtEditRecord.executeUpdate();

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

    /**
     * 
     * @param id
     * @return
     */
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

    /**
     * 
     * @param record
     * @return
    
    public boolean deleteRecord(Patient record) {
    int id = record.getId();
    return deleteRecord(id);
    }
     */
    /**
     * 
     * @return
     */
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


    /* 
     * @param index
     * @return
     */
    public Patient getPatient(int recordID) {
        Patient record = null;
        ResultSetMetaData metadata;
        try {
            stmtGetPatient.clearParameters();
            stmtGetPatient.setInt(1, recordID);
            ResultSet result = stmtGetPatient.executeQuery();
            metadata = result.getMetaData();
            int numberOfColumns = metadata.getColumnCount();
            if (result.next()) {
                record = new Patient();
                for (int i = 1; i <= numberOfColumns; i++) {
                    if (metadata.getColumnType(i) == java.sql.Types.VARCHAR) {
                        record.setVariable(metadata.getColumnName(i), result.getString(metadata.getColumnName(i)));
                    } else if (metadata.getColumnType(i) == java.sql.Types.INTEGER) {
                        record.setVariable(metadata.getColumnName(i), result.getInt(metadata.getColumnName(i)));
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return record;
    }

    /* 
     * @param index
     * @return
     */
    public Tumour getTumour(int recordID) {
        Tumour record = null;
        ResultSetMetaData metadata;
        try {
            stmtGetTumour.clearParameters();
            stmtGetTumour.setInt(1, recordID);
            ResultSet result = stmtGetTumour.executeQuery();
            metadata = result.getMetaData();
            int numberOfColumns = metadata.getColumnCount();
            if (result.next()) {
                record = new Tumour();
                for (int i = 1; i <= numberOfColumns; i++) {
                    if (metadata.getColumnType(i) == java.sql.Types.VARCHAR) {
                        record.setVariable(metadata.getColumnName(i), result.getString(metadata.getColumnName(i)));
                    } else if (metadata.getColumnType(i) == java.sql.Types.INTEGER) {
                        record.setVariable(metadata.getColumnName(i), result.getInt(metadata.getColumnName(i)));
                    }
                }
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
    private Map<Subject, DistributedTableDataSource> distributedDataSources;
    private boolean tableOfDictionariesFilled = true;
    private PreparedStatement stmtSaveNewPatient;
    private PreparedStatement stmtSaveNewTumour;
    private PreparedStatement stmtEditPatient;
    private PreparedStatement stmtEditTumour;
    private PreparedStatement stmtSaveNewDictionary;
    private PreparedStatement stmtSaveNewDictionaryEntry;
    private PreparedStatement stmtUpdateExistingPatient;
    private PreparedStatement stmtGetListEntries;
    private PreparedStatement stmtGetPatient;
    private PreparedStatement stmtGetTumour;
    private PreparedStatement stmtGetPatients;
    private PreparedStatement stmtGetTumours;
    private PreparedStatement stmtGetPatientsAndTumours;
    private PreparedStatement stmtGetRecord;
    private PreparedStatement stmtGetRecords;
    private PreparedStatement stmtGetDictionary;
    private PreparedStatement stmtGetDictionaryEntry;
    private PreparedStatement stmtDeleteDictionaryEntry;
    private PreparedStatement stmtDeleteDictionaryEntries;
    private PreparedStatement stmtDeletePatient;
    private PreparedStatement stmtDeleteTumour;
    private String ns = "ns3:";
    private static final String strGetPatient =
            "SELECT * FROM APP.PATIENT " +
            "WHERE ID = ?";
    private String strGetPatients =
            "SELECT * FROM APP.PATIENT";
    private String strCountPatients =
            "SELECT COUNT(*) FROM APP.PATIENT";
    private String strGetPatientsAndTumours =
            "SELECT * FROM APP.PATIENT, APP.TUMOUR " +
            "WHERE APP.PATIENT.ID = APP.TUMOUR.PATIENTID";
    private String strCountPatientsAndTumours =
            "SELECT COUNT(*) FROM APP.PATIENT, APP.TUMOUR " +
            "WHERE APP.PATIENT.ID = APP.TUMOUR.PATIENTID";
    private static final String strGetTumour =
            "SELECT * FROM APP.TUMOUR " +
            "WHERE ID = ?";
    private String strGetTumours =
            "SELECT * FROM APP.TUMOUR";
    private String strCountTumours =
            "SELECT COUNT(*) FROM APP.TUMOUR";
    private static final String strGetDictionary =
            "SELECT * FROM APP.DICTIONARIES " +
            "WHERE ID = ?";
    private String strGetDictionaries =
            "SELECT * FROM APP.DICTIONARIES ";
    private static final String strGetDictionaryEntry =
            "SELECT * FROM APP.DICTIONARY " +
            "WHERE ID = ?";
    private static final String strGetDictionaryEntries =
            "SELECT * FROM APP.DICTIONARY ";
    private static final String strGetListEntries =
            "SELECT ID, LASTNAME, FIRSTNAME, MIDDLENAME FROM APP.PATIENT " +
            "ORDER BY LASTNAME ASC";
    private static final String strDeletePatient =
            "DELETE FROM APP.PATIENT " +
            "WHERE ID = ?";
    private static final String strDeleteTumour =
            "DELETE FROM APP.TUMOUR " +
            "WHERE ID = ?";
    private static final String strDeleteDictionaryEntries =
            "DELETE FROM APP.DICTIONARY " +
            "WHERE DICTIONARY = ?";    // The Dynamic ones
    private String strSavePatient;
    private String strSaveTumour;
    private String strEditPatient;
    private String strEditTumour;
    private String strSaveDictionary;
    private String strSaveDictionaryEntry;
}

