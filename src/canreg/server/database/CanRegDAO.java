/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016 International Agency for Research on Cancer
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.server.database;

import canreg.common.*;
import canreg.common.cachingtableapi.DistributedTableDataSource;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.*;
import canreg.common.database.Dictionary;
import canreg.server.DatabaseStats;
import com.ibm.icu.util.Calendar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm (based on code by John O'Conner)
 */
public class CanRegDAO {

    private static final boolean debug = false;
    private final DatabaseVariablesListElement[] variables;
    StringBuilder counterStringBuilder = new StringBuilder();
    StringBuilder getterStringBuilder = new StringBuilder();
    StringBuilder filterStringBuilder = new StringBuilder();

    /**
     *
     * @param systemCode
     * @param doc
     */
    public CanRegDAO(String systemCode, Document doc) {
        this.doc = doc;

        this.systemCode = systemCode;

        globalToolBox = new GlobalToolBox(doc);

        variables = globalToolBox.getVariables();

        distributedDataSources = new LinkedHashMap<String, DistributedTableDataSource>();
        activeStatements = new LinkedHashMap<String, Statement>();
        dictionaryMap = buildDictionaryMap(doc);

        locksMap = new TreeMap<String, Set<Integer>>();

        debugOut(canreg.server.xml.Tools.getTextContent(
                new String[]{ns + "canreg", ns + "general", ns + "registry_name"}, doc));

        // Prepare the SQL strings
        strSavePatient = QueryGenerator.strSavePatient(doc);
        strSaveTumour = QueryGenerator.strSaveTumour(doc);
        strSaveSource = QueryGenerator.strSaveSource(doc);
        strEditPatient = QueryGenerator.strEditPatient(doc);
        strEditTumour = QueryGenerator.strEditTumour(doc);
        strEditSource = QueryGenerator.strEditSource(doc);
        strSaveDictionary = QueryGenerator.strSaveDictionary();
        strSaveDictionaryEntry = QueryGenerator.strSaveDictionaryEntry();
        strSavePopoulationDataset = QueryGenerator.strSavePopoulationDataset();
        strSavePopoulationDatasetsEntry = QueryGenerator.strSavePopoulationDatasetsEntry();
        strSaveNameSexRecord = QueryGenerator.strSaveNameSexEntry();
        strDeleteNameSexRecord = QueryGenerator.strDeleteNameSexEntry();
        strGetPatientsAndTumours = QueryGenerator.strGetPatientsAndTumours(globalToolBox);
        strGetSourcesAndTumours = QueryGenerator.strGetSourcesAndTumours(globalToolBox);
        strCountPatientsAndTumours = QueryGenerator.strCountPatientsAndTumours(globalToolBox);
        strCountSourcesAndTumours = QueryGenerator.strCountSourcesAndTumours(globalToolBox);
        strGetSourcesAndTumoursAndPatients = QueryGenerator.strGetRecordsAllTables(globalToolBox);
        strCountSourcesAndTumoursAndPatients = QueryGenerator.strCountRecordsAllTables(globalToolBox);
        strGetHighestPatientID = QueryGenerator.strGetHighestPatientID(globalToolBox);
        strGetHighestTumourID = QueryGenerator.strGetHighestTumourID(globalToolBox);
        strGetHighestPatientRecordID = QueryGenerator.strGetHighestPatientRecordID(globalToolBox);
        strGetHighestSourceRecordID = QueryGenerator.strGetHighestSourceRecordID(globalToolBox);
        strEditUser = QueryGenerator.strEditUser();
        strSaveUser = QueryGenerator.strSaveUser();
        strMaxNumberOfSourcesPerTumourRecord = QueryGenerator.strMaxNumberOfSourcesPerTumourRecord(globalToolBox);
        /* We don't use tumour record ID...
         strGetHighestTumourRecordID = QueryGenerator.strGetHighestTumourRecordID(globalToolBox);
         */
        setDBSystemDir();
        dbProperties = loadDBProperties();
        String driverName = dbProperties.getProperty("derby.driver");
        loadDatabaseDriver(driverName);
        if (!dbExists()) {
            createDatabase();
            tableOfDictionariesFilled = false;
            tableOfPopulationDataSets = false;
        }
    }

    /**
     *
     * @return
     */
    public synchronized Map<Integer, Dictionary> getDictionary() {
        // Map<Integer, Dictionary> dictionaryMap = new LinkedHashMap<Integer, Dictionary>();
        Statement queryStatement;
        ResultSet results;

        // rebuild dictionary map
        dictionaryMap = buildDictionaryMap(doc);

        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetDictionaryEntries);
            while (results.next()) {
                int id = results.getInt(1);
                Integer dictionary = results.getInt(2);
                String code = results.getString(3);
                String desc = results.getString(4);
                Dictionary dic = dictionaryMap.get(dictionary);
                if (dic == null) {
                    dic = new Dictionary();
                }
                dictionaryMap.put(dictionary, dic);
                dic.addDictionaryEntry(code, new DictionaryEntry(id, code, desc));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return dictionaryMap;
    }

    /**
     *
     * @return
     */
    public synchronized Map<String, Integer> getNameSexTables() {

        Map<String, Integer> nameSexMap = new LinkedHashMap<String, Integer>();
        Statement queryStatement;
        ResultSet results;

        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetNameSexRecords);
            while (results.next()) {
                int id = results.getInt(1);
                String name = results.getString(2);
                Integer sex = results.getInt(3);
                nameSexMap.put(name, sex);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return nameSexMap;
    }

    /**
     *
     * @param lookup
     * @return
     */
    public synchronized String getSystemPropery(String lookup) {
        String value = null;
        try {
            String query = "SELECT * FROM " + Globals.SCHEMA_NAME + ".SYSTEM WHERE LOOKUP = '" + lookup + "'";
            Statement queryStatement;
            ResultSet results;
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(query);
            while (results.next()) {
                value = results.getString(3);
                debugOut(query);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    /**
     *
     * @param lookup
     * @param value
     */
    public synchronized void setSystemPropery(String lookup, String value) {
        try {
            String query = "DELETE FROM " + Globals.SCHEMA_NAME + ".SYSTEM WHERE LOOKUP = '" + lookup + "'";
            Statement queryStatement;
            queryStatement = dbConnection.createStatement();
            boolean result = queryStatement.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            String query = "INSERT INTO " + Globals.SCHEMA_NAME + ".SYSTEM (LOOKUP, VALUE) VALUES ('" + lookup + "', '" + value + "')";
            Statement queryStatement;
            queryStatement = dbConnection.createStatement();
            boolean result = queryStatement.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param user
     * @return
     */
    public synchronized int saveUser(User user) {
        int ID = user.getID();

        if (ID > -1) {
            // edit user
            ID = editUser(user);
        } else {
            // save new user
            ID = saveNewUser(user);
        }
        return ID;
    }

    private synchronized int editUser(User user) {
        int ID = user.getID();
        ResultSet results;
        try {
            stmtEditUser.clearParameters();
            stmtEditUser.setString(1, user.getUserName());
            stmtEditUser.setString(2, new String(user.getPassword()));
            stmtEditUser.setInt(3, user.getUserRightLevelIndex());
            stmtEditUser.setString(4, user.getEmail());
            stmtEditUser.setString(5, user.getRealName());
            stmtEditUser.setInt(6, ID);

            int rowCount = stmtEditUser.executeUpdate();

            results = stmtEditUser.getGeneratedKeys();
            if (results != null) {
                if (results.next()) {
                    ID = results.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ID;
    }

    private synchronized int saveNewUser(User user) {
        int ID = -1;
        ResultSet results;
        try {
            stmtSaveNewUser.clearParameters();
            stmtSaveNewUser.setString(1, user.getUserName());
            stmtSaveNewUser.setString(2, new String(user.getPassword()));
            stmtSaveNewUser.setInt(3, user.getUserRightLevelIndex());
            stmtSaveNewUser.setString(4, user.getEmail());
            stmtSaveNewUser.setString(5, user.getRealName());
            int rowCount = stmtSaveNewUser.executeUpdate();

            results = stmtSaveNewUser.getGeneratedKeys();
            if (results != null) {
                if (results.next()) {
                    ID = results.getInt(1);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ID;
    }

    /**
     *
     * @return
     */
    public synchronized Map<String, User> getUsers() {
        Map<String, User> usersMap = new LinkedHashMap<String, User>();
        Statement queryStatement;
        ResultSet results;
        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetUsers);
            while (results.next()) {
                int id = results.getInt(1);
                String username = results.getString(2);
                String password = results.getString(3);
                int userLevelIndex = results.getInt(4);
                String email = results.getString(5);
                String realName = results.getString(6);

                User user = new User();
                user.setID(id);
                user.setUserName(username);
                user.setPassword(password.toCharArray());
                user.setUserRightLevelIndex(userLevelIndex);
                user.setEmail(email);
                user.setRealName(realName);
                usersMap.put(username, user);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return usersMap;
    }

    /**
     *
     * @return
     */
    public synchronized Map<Integer, PopulationDataset> getPopulationDatasets() {
        Map<Integer, PopulationDataset> populationDatasetMap = new LinkedHashMap<Integer, PopulationDataset>();
        Statement queryStatement;
        ResultSet results;

        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetPopulationDatasets);
            while (results.next()) {
                int id = results.getInt(1);
                PopulationDataset populationDataset = new PopulationDataset();

                Integer pdsId = results.getInt(2);
                populationDataset.setPopulationDatasetID(pdsId);

                String name = results.getString(3);
                populationDataset.setPopulationDatasetName(name);

                String filter = results.getString(4);
                populationDataset.setFilter(filter);

                String dateString = results.getString(5);
                populationDataset.setDate(dateString);

                String source = results.getString(6);
                populationDataset.setSource(source);

                String ageGroupStructure = results.getString(7);
                populationDataset.setAgeGroupStructure(new AgeGroupStructure(ageGroupStructure));

                String description = results.getString(8);
                populationDataset.setDescription(description);

                Integer worldPopulationPDSID = results.getInt(9);
                populationDataset.setWorldPopulationID(worldPopulationPDSID);

                boolean worldPopulationBool = results.getInt(10) == 1;
                populationDataset.setWorldPopulationBool(worldPopulationBool);

                populationDatasetMap.put(pdsId, populationDataset);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }


        for (PopulationDataset popset : populationDatasetMap.values()) {
            if (!popset.isWorldPopulationBool()) {
                popset.setWorldPopulation(
                        populationDatasetMap.get(
                                popset.getWorldPopulationID()));
            }
        }

        try {
            queryStatement = dbConnection.createStatement();
            results = queryStatement.executeQuery(strGetPopulationDatasetEntries);
            while (results.next()) {
                int id = results.getInt(1);

                Integer pdsId = results.getInt(2);

                PopulationDataset populationDataset = populationDatasetMap.get(pdsId);

                Integer ageGroup = results.getInt(3);
                Integer sex = results.getInt(4);
                Integer count = results.getInt(5);

                PopulationDatasetsEntry populationDatasetEntry = new PopulationDatasetsEntry(ageGroup, sex, count);
                populationDatasetEntry.setPopulationDatasetID(pdsId);
                populationDataset.addAgeGroup(populationDatasetEntry);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }

        return populationDatasetMap;
    }

    /**
     *
     * @return
     */
    public String generateResultSetID() {
        // generate resultSetID
        boolean foundPlace = false;
        int i = 0;
        String resultSetID = Integer.toString(i);
        // Find a spot in the map of datasources
        while (!foundPlace) {
            resultSetID = Integer.toString(i++);
            foundPlace = !distributedDataSources.containsKey(resultSetID);
        }
        return resultSetID;
    }

    /**
     *
     * @param filter
     * @param tableName
     * @param resultSetID
     * @return
     * @throws java.sql.SQLException
     * @throws UnknownTableException
     * @throws DistributedTableDescriptionException
     */
    public synchronized DistributedTableDescription getDistributedTableDescriptionAndInitiateDatabaseQuery(DatabaseFilter filter, String tableName, String resultSetID)
            throws SQLException, UnknownTableException, DistributedTableDescriptionException {
        // distributedDataSources.remove(theUser);
        // ResultSet result;
        Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        // int rowCount = 0;
        DistributedTableDataSource dataSource;

        activeStatements.put(resultSetID, statement);

        // Is this a person search query?
        if (DatabaseFilter.QueryType.PERSON_SEARCH.equals(filter.getQueryType())) {
            dataSource = initiatePersonSearchQuery(filter, statement);
        } // Or a Frequency by year query?
        else if (DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR.equals(filter.getQueryType())) {
            dataSource = initiateFrequenciesByYearQuery(filter, statement, tableName);
        } // Or a "regular" query
        else {
            dataSource = initiateTableQuery(filter, statement, tableName);
        }
        distributedDataSources.put(resultSetID, dataSource);
        activeStatements.remove(resultSetID);
        dataSource.getTableDescription().setResultSetID(resultSetID);
        return dataSource.getTableDescription();
    }

    /**
     *
     * @param resultSetID
     * @throws SQLException
     */
    public synchronized void releaseResultSet(String resultSetID) throws SQLException {
        DistributedTableDataSourceResultSetImpl dataSource = (DistributedTableDataSourceResultSetImpl) distributedDataSources.get(resultSetID);
        if (dataSource != null) {
            dataSource.releaseResultSet();
        }
        distributedDataSources.remove(resultSetID);
    }

    /**
     *
     * @param recordID
     * @param tableName
     * @param lock
     * @return
     * @throws RecordLockedException
     */
    public synchronized DatabaseRecord getRecord(int recordID, String tableName, boolean lock) throws RecordLockedException {
        DatabaseRecord returnRecord;
        if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            returnRecord = getPatient(recordID, lock);
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            returnRecord = getTumour(recordID, lock);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            returnRecord = getSource(recordID, lock);
        } else {
            returnRecord = null;
        }
        if (returnRecord != null && lock) {
            lockRecord(recordID, tableName);
        }
        return returnRecord;
    }

    /**
     * Perform backup of the database
     *
     * @return Path to backup
     */
    public synchronized String performBackup() {
        String path = null;
        try {
            path = canreg.server.database.derby.Backup.backUpDatabase(dbConnection, Globals.CANREG_BACKUP_FOLDER + Globals.FILE_SEPARATOR + getSystemCode());
            canreg.server.xml.Tools.writeXmlFile(doc, path + Globals.FILE_SEPARATOR + getSystemCode() + ".xml");
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return path;
    }

    /**
     *
     * @param resultSetID
     * @param from
     * @param to
     * @return
     * @throws DistributedTableDescriptionException
     */
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws DistributedTableDescriptionException {
        DistributedTableDataSource ts = distributedDataSources.get(resultSetID);
        if (ts != null) {
            return ts.retrieveRows(from, to);
        } else {
            return null;
        }
    }
    // TODO: This only works for Embedded databases - will look into it!
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
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Properties loadDBProperties() {
        InputStream dbPropInputStream;
        dbPropInputStream = CanRegDAO.class.getResourceAsStream(Globals.DATABASE_CONFIG);
        dbProperties = new Properties();
        try {
            dbProperties.load(dbPropInputStream);
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbProperties;
    }

    private synchronized boolean createTables(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement;
        try {
            statement = dbConnection.createStatement();

            // Dynamic creation of tables
            statement.execute(QueryGenerator.strCreateVariableTable(Globals.TUMOUR_TABLE_NAME, doc));
            statement.execute(QueryGenerator.strCreateVariableTable(Globals.PATIENT_TABLE_NAME, doc));
            statement.execute(QueryGenerator.strCreateVariableTable(Globals.SOURCE_TABLE_NAME, doc));
            // Dictionaries part
            statement.execute(QueryGenerator.strCreateDictionaryTable(doc));
            statement.execute(QueryGenerator.strCreateTablesOfDictionaries(doc));

            // Population dataset part
            statement.execute(QueryGenerator.strCreatePopulationDatasetTable());
            statement.execute(QueryGenerator.strCreatePopulationDatasetsTable());

            // name/sex part
            statement.execute(QueryGenerator.strCreateNameSexTable());

            // System part
            statement.execute(QueryGenerator.strCreateUsersTable());
            statement.execute(QueryGenerator.strCreateSystemPropertiesTable());

            // Set primary keys in patient table
            //for (String command : QueryGenerator.strCreatePatientTablePrimaryKey(
            //        globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName())) {
            //    statement.execute(command);
            //}

            // Build keys
            dropAndRebuildKeys(statement);

            // Create indexes - do last: least important
            LinkedList<String> tumourIndexList = QueryGenerator.strCreateIndexTable("Tumour", doc);
            for (String query : tumourIndexList) {
                // debugOut(query);
                statement.execute(query);
            }
            LinkedList<String> patientIndexList = QueryGenerator.strCreateIndexTable("Patient", doc);
            for (String query : patientIndexList) {
                // debugOut(query);
                statement.execute(query);
            }
            LinkedList<String> sourceIndexList = QueryGenerator.strCreateIndexTable("Source", doc);
            for (String query : sourceIndexList) {
                // debugOut(query);
                statement.execute(query);
            }
            //<ictl.co>
            statement.execute(QueryGenerator.strCreateUtilsToDateFunction());
            //</ictl.co>
            bCreatedTables = true;
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bCreatedTables;
    }

    private boolean createDatabase() {

        boolean bCreated = false;
        dbConnection = null;

        String dbUrl = getDatabaseUrl();
        dbProperties.put("create", "true");

        // testing the case insensitivity
        // REF: https://issues.apache.org/jira/secure/attachment/12439250/devguide.txt
        // http://db.apache.org/derby/docs/dev/devguide/tdevdvlpcollation.html#tdevdvlpcollation
        // We do it without the territory set so that the default JVM one is taken
        // Should this be moved to an option? I guess not...

        dbProperties.put("collation", "TERRITORY_BASED:PRIMARY");

        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bCreated = createTables(dbConnection);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        dbProperties.remove("create");
        return bCreated;
    }

    /**
     * Restore database from backup.
     *
     * @param path to database backup.
     * @return true if successful, false if not
     */
    public synchronized String restoreFromBackup(String path) {
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
            } else {
                return "shutdown failed";
            }
            ex = e;
        }
        if (!shutdownSuccess) {
            dbProperties.remove("shutdown");
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            // ((DonMan) parent).signalError("Error during shutdown for RESTORE: ", ex,
            //        "in: DonDao.restore", false);
            return "shutdown failed";
        }
        try {
            dbProperties.remove("shutdown");
            dbConnection.close(); // Close current connection.

            // check to see if there is a database already - rename it
            File databaseFolder = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + getSystemCode());
            if (databaseFolder.exists()) {
                int i = 0;
                File folder2 = databaseFolder;
                while (folder2.exists()) {
                    i++;
                    folder2 = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + getSystemCode() + i);
                }
                databaseFolder.renameTo(folder2);
                try {
                    canreg.common.Tools.fileCopy(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + getSystemCode() + ".xml",
                            Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + getSystemCode() + i + ".xml");
                } catch (IOException ex1) {
                    Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            dbProperties.put("restoreFrom", path + "/" + getSystemCode());
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bRestored = true;
        } catch (SQLException ex2) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex2);
            //((DonMan) parent).signalError("Error during RESTORE: ", e,
            //       "in: DonDao.restore", false);
        }
        dbProperties.remove("restoreFrom");
        // connect(); // Do not reconnect as this would be a potential security problem...
        if (bRestored) {
            try {
                // install the xml
                canreg.common.Tools.fileCopy(path + Globals.FILE_SEPARATOR + getSystemCode() + ".xml",
                        Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + getSystemCode() + ".xml");
            } catch (IOException ex1) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return "success";
        } else {
            return "failed";
        }
    }

    /**
     *
     * @return
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     */
    public boolean connect() throws SQLException, RemoteException {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.INFO, "JavaDB Version: {0}", dbConnection.getMetaData().getDatabaseProductVersion());
        } catch (SQLException ex) {
            throw ex;
        }
        try {
            //Prepare the SQL statements
            stmtSaveNewPatient = dbConnection.prepareStatement(strSavePatient, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewTumour = dbConnection.prepareStatement(strSaveTumour, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewSource = dbConnection.prepareStatement(strSaveSource, Statement.RETURN_GENERATED_KEYS);
            stmtEditPatient = dbConnection.prepareStatement(strEditPatient, Statement.RETURN_GENERATED_KEYS);
            stmtEditTumour = dbConnection.prepareStatement(strEditTumour, Statement.RETURN_GENERATED_KEYS);
            stmtEditSource = dbConnection.prepareStatement(strEditSource, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewDictionary = dbConnection.prepareStatement(strSaveDictionary, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewDictionaryEntry = dbConnection.prepareStatement(strSaveDictionaryEntry, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewPopoulationDataset = dbConnection.prepareStatement(strSavePopoulationDataset, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewPopoulationDatasetsEntry = dbConnection.prepareStatement(strSavePopoulationDatasetsEntry, Statement.RETURN_GENERATED_KEYS);
            stmtSaveNewUser = dbConnection.prepareStatement(strSaveUser);
            stmtEditUser = dbConnection.prepareStatement(strEditUser);
            stmtSaveNewNameSexRecord = dbConnection.prepareStatement(strSaveNameSexRecord, Statement.RETURN_GENERATED_KEYS);
            stmtDeleteDictionaryEntries = dbConnection.prepareStatement(strDeleteDictionaryEntries);
            stmtClearNameSexTable = dbConnection.prepareStatement(strClearNameSexTable);
            stmtDeleteNameSexRecord = dbConnection.prepareStatement(strDeleteNameSexRecord);
            stmtDeletePopoulationDataset = dbConnection.prepareStatement(strDeletePopulationDataset);
            stmtDeletePopoulationDatasetEntries = dbConnection.prepareStatement(strDeletePopulationDatasetEntries);
            //stmtUpdateExistingPatient = dbConnection.prepareStatement(strUpdatePatient);
            stmtGetPatient = dbConnection.prepareStatement(strGetPatient);
            stmtGetPatients = dbConnection.prepareStatement(strGetPatients);
            stmtGetSources = dbConnection.prepareStatement(strGetSources);
            stmtGetPatientsAndTumours = dbConnection.prepareStatement(strGetPatientsAndTumours);
            stmtGetSourcesAndTumours = dbConnection.prepareStatement(strGetSourcesAndTumours);
            stmtGetRecordsAllTables = dbConnection.prepareStatement(strGetSourcesAndTumoursAndPatients);
            stmtGetHighestPatientID = dbConnection.prepareStatement(strGetHighestPatientID);
            stmtGetHighestTumourID = dbConnection.prepareStatement(strGetHighestTumourID);
            stmtGetHighestPatientRecordID = dbConnection.prepareStatement(strGetHighestPatientRecordID);
            stmtGetHighestSourceRecordID = dbConnection.prepareStatement(strGetHighestSourceRecordID);
            /* We don't use tumour record ID...
             stmtGetHighestTumourRecordID = dbConnection.prepareStatement(strGetHighestTumourRecordID);
             */
            stmtGetTumour = dbConnection.prepareStatement(strGetTumour);
            stmtGetTumours = dbConnection.prepareStatement(strGetTumours);

            stmtGetSource = dbConnection.prepareStatement(strGetSource);

            stmtDeleteTumourRecord = dbConnection.prepareStatement(strDeleteTumourRecord);
            stmtDeletePatientRecord = dbConnection.prepareStatement(strDeletePatientRecord);
            stmtDeleteSourceRecord = dbConnection.prepareStatement(strDeleteSourceRecord);

            stmtGetDictionary = dbConnection.prepareStatement(strGetDictionary);
            stmtMaxNumberOfSourcesPerTumourRecord = dbConnection.prepareStatement(strMaxNumberOfSourcesPerTumourRecord);
            // stmtGetDictionaries = dbConnection.prepareStatement(strGetDictionaries);
            // stmtDeletePatient = dbConnection.prepareStatement(strDeletePatient);

            isConnected = dbConnection != null;

            // Consider moving this function...
            if (isConnected && !tableOfDictionariesFilled) {
                fillDictionariesTable();
            }

            if (isConnected && !tableOfPopulationDataSets) {
                fillPopulationDatasetTables();
            }

            // test

            debugOut("Cocuou from the database connection...\nVersion: " + dbConnection.getMetaData().getDatabaseProductVersion());
            debugOut("Next patient ID = " + getNextPatientID());
        } catch (SQLException ex) {
            debugOut("SQLerror... ");
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            isConnected = false;
            // CanRegDAO now throws database mismatch exceptions if the database structure doesn't match the prepared queries.
            throw new RemoteException("Database description mismatch... \n" + ex.getLocalizedMessage());
        }

        //<ictl.co>
        //Deprecate on next version
        try {
            if (isConnected) {
                Statement statement = dbConnection.createStatement();
                statement.execute(QueryGenerator.strCreateUtilsToDateFunction());
                statement.close();
            }
        } catch (Exception ignore) {

        }
        //</ictl.co>
        return isConnected;
    }

    public boolean connectWithBootPassword(char[] passwordArray) throws RemoteException, SQLException {
        String password = new String(passwordArray);
        dbProperties.setProperty("bootPassword", password);
        boolean success = connect();
        dbProperties.remove("bootPassword");
        return success;
    }

    public boolean encryptDatabase(char[] newPasswordArray, char[] oldPasswordArray) throws RemoteException, SQLException {

        if (oldPasswordArray.length != 0) {
            // already encrypted? Change password
            // http://db.apache.org/derby/docs/10.4/devguide/cdevcsecure55054.html
            String oldPassword = new String(oldPasswordArray);
            String newPassword = new String(newPasswordArray);
            String command = "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
                    + "\'bootPassword\', \'" + oldPassword + " , " + newPassword + "\')";
            Statement statement = dbConnection.createStatement();
            statement.execute(command);
            return true;
        } else if (newPasswordArray.length == 0) {
            // remove password? 
            // Doesn't work!
            String oldPassword = new String(oldPasswordArray);
            dbProperties.setProperty("bootPassword", oldPassword);
            try {
                disconnect();
            } catch (SQLException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
            dbProperties.setProperty("dataEncryption", "false");
        } else {
            // Encrypt database
            // http://db.apache.org/derby/docs/10.4/devguide/cdevcsecure866716.html
            try {
                disconnect();
            } catch (SQLException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
            dbProperties.setProperty("dataEncryption", "true");
            String password = new String(newPasswordArray);
            dbProperties.setProperty("bootPassword", password);
        }
        dbProperties.remove("bootPassword");
        dbProperties.remove("newBootPassword");
        boolean success = connectWithBootPassword(newPasswordArray);
        dbProperties.remove("dataEncryption");
        return success;
    }

    /**
     *
     * @return
     * @throws java.sql.SQLException
     */
    public boolean disconnect() throws SQLException {
        boolean shutdownSuccess = false;
        if (isConnected) {
            String dbUrl = getDatabaseUrl();
            try {
                dbConnection.close(); // Close current connection.
                dbProperties.put("shutdown", "true");
                dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            } catch (SQLException e) {
                if (e.getSQLState().equals("08006")) {
                    shutdownSuccess = true; // single db.
                } else {
                    Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, e);
                    throw e;
                }
            }
            dbProperties.remove("shutdown");
        }
        return shutdownSuccess;
    }

    /**
     *
     * @return String location of the database
     */
    public String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "/" + getSystemCode();
        return dbLocation;
    }

    /**
     *
     * @return
     */
    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + getSystemCode();
        return dbUrl;
    }

    private synchronized int saveRecord(String tableName, DatabaseRecord record, PreparedStatement stmtSaveNewRecord) throws SQLException {
        int id = -1;

        stmtSaveNewRecord.clearParameters();

        int recordVariableNumber = 0;

        // Go through all the variable definitions
        // todo: optimize this code!
        for (DatabaseVariablesListElement variable : variables) {

            // Create line
            String tableNameDB = variable.getDatabaseTableName();

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                recordVariableNumber++;
                int variableLength;
                String variableType = variable.getVariableType();
                variableLength = variable.getVariableLength();
                // String variableLenghtString = null;

                Object obj = record.getVariable(variable.getDatabaseVariableName());

                // System.out.println(
                //         element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent() +
                //         ": " + obj.toString());

                if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)) {
                    if (obj != null) {
                        try {
                            String strObj = obj.toString();
                            if (strObj.length() > 0) {
                                if (variableLength > 0 && strObj.length() > variableLength) {
                                    strObj = strObj.substring(0, variableLength);
                                }
                                stmtSaveNewRecord.setString(recordVariableNumber, strObj);
                            } else {
                                stmtSaveNewRecord.setString(recordVariableNumber, "");
                            }
                        } catch (java.lang.ClassCastException cce) {
                            debugOut("Cast to String Error. Type:" + variableType + ", Value: " + obj + ", Variable Number: " + recordVariableNumber);
                            throw cce;
                        }
                    } else {
                        stmtSaveNewRecord.setString(recordVariableNumber, "");
                    }
                    //<ictl.co>
                } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)) {
                    if (obj != null) {
                        try {
                            String strObj = obj.toString();
                            if (strObj.length() > 0) {
                                if (variableLength > 0 && strObj.length() > variableLength) {
                                    strObj = strObj.substring(0, variableLength);
                                }
                                stmtSaveNewRecord.setString(recordVariableNumber, strObj);
                            } else {
                                stmtSaveNewRecord.setString(recordVariableNumber, "");
                            }
/*
                            Long intObj = Long.parseLong(strObj);
                            if (!GlobalToolBox.validateNCID(strObj)) {
                                throw new IllegalArgumentException("Invalid NCID");
                            }
                            stmtSaveNewRecord.setString(recordVariableNumber, strObj);
*/
                        } catch (java.lang.NumberFormatException cce) {
                            debugOut("NCID " + variableType + " " + obj);
                            throw cce;
                        } catch (java.lang.ClassCastException cce) {
                            debugOut("NCID " + variableType + " " + obj);
                            throw cce;
                        }
                    }
//</ictl.co>
                } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                    if (obj != null) {
                        try {
                            Integer intObj = (Integer) obj;
                            stmtSaveNewRecord.setInt(recordVariableNumber, intObj);
                        } catch (java.lang.ClassCastException cce) {
                            debugOut("Number " + variableType + " " + obj);
                            throw cce;
                        }
                    } else {
                        stmtSaveNewRecord.setInt(recordVariableNumber, -1);
                    }
                }
            }
        }

        int rowCount = stmtSaveNewRecord.executeUpdate();
        ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
        if (results.next()) {
            id = results.getInt(1);
        }

        return id;
    }

    /**
     *
     * @param patient
     * @return
     * @throws SQLException
     */
    public synchronized int savePatient(Patient patient) throws SQLException {
        DatabaseVariablesListElement patientIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
        DatabaseVariablesListElement patientRecordIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString());
        String patientID = (String) patient.getVariable(patientIDVariable.getDatabaseVariableName());
        String patientRecordID = (String) patient.getVariable(patientRecordIDVariable.getDatabaseVariableName());
        if (patientID == null || patientID.trim().length() == 0) {
            patientID = getNextPatientID();
            patient.setVariable(patientIDVariable.getDatabaseVariableName(), patientID);
            patientRecordID = getNextPatientRecordID(patientID);
            patient.setVariable(patientRecordIDVariable.getDatabaseVariableName(), patientRecordID);
        } else if (patientRecordID == null || patientRecordID.trim().length() == 0) {
            patientRecordID = getNextPatientRecordID(patientID);
            patient.setVariable(patientRecordIDVariable.getDatabaseVariableName(), patientRecordID);
            patient.setVariable(patientRecordIDVariable.getDatabaseVariableName(), getNextPatientRecordID(patientID));
        }
        DatabaseVariablesListElement patientRecordStatusVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientCheckStatus.toString());
        if (patient.getVariable(patientRecordStatusVariable.getDatabaseVariableName()) == null) {
            patient.setVariable(patientRecordStatusVariable.getDatabaseVariableName(), "0");
        }
        DatabaseVariablesListElement patientUnduplicationStatusVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString());
        if (patient.getVariable(patientUnduplicationStatusVariable.getDatabaseVariableName()) == null) {
            patient.setVariable(patientUnduplicationStatusVariable.getDatabaseVariableName(), "0");
        }
        return saveRecord(Globals.PATIENT_TABLE_NAME, patient, stmtSaveNewPatient);
    }

    /**
     *
     * @param tumour
     * @return
     * @throws SQLException
     * @throws RecordLockedException
     */
    public synchronized int saveTumour(Tumour tumour) throws SQLException, RecordLockedException {
        String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        Object tumourID = tumour.getVariable(tumourIDVariableName);
        String patientRecordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        String patientRecordID = (String) tumour.getVariable(patientRecordIDVariableName);
        // 

        if (tumourID == null
                || tumourID.toString().trim().length() == 0 // || !tumourID.toString().trim().startsWith(patientRecordID) // TODO: fix this! For now - disable it... (Maybe that is the best solution in the long run as well...)
                ) {
            if (patientRecordID == null || patientRecordID.trim().length() == 0) {
            }
            tumourID = getNextTumourID(patientRecordID);
            tumour.setVariable(tumourIDVariableName, tumourID);
        }
        DatabaseVariablesListElement tumourRecordStatusVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString());
        if (tumour.getVariable(tumourRecordStatusVariable.getDatabaseVariableName()) == null) {
            tumour.setVariable(tumourRecordStatusVariable.getDatabaseVariableName(), "0");
        }
        DatabaseVariablesListElement tumourUnduplicationStatusVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUnduplicationStatus.toString());
        if (tumour.getVariable(tumourUnduplicationStatusVariable.getDatabaseVariableName()) == null) {
            tumour.setVariable(tumourUnduplicationStatusVariable.getDatabaseVariableName(), "0");
        }
        DatabaseVariablesListElement tumourCheckStatusVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.CheckStatus.toString());
        if (tumour.getVariable(tumourCheckStatusVariable.getDatabaseVariableName()) == null) {
            tumour.setVariable(tumourCheckStatusVariable.getDatabaseVariableName(), "0");
        }

        // save tumour before we save the sources...
        int id = saveRecord(Globals.TUMOUR_TABLE_NAME, tumour, stmtSaveNewTumour);

        Set<Source> sources = tumour.getSources();
        // delete old sources
        try {
            deleteSources(tumourID);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownTableException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // save each of the source records
        saveSources(tumourID, sources);

        return id;
    }

    /**
     *
     * @param tumour
     * @return
     */
    private synchronized int saveSource(Source source) throws SQLException {
        String sourceIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName();
        Object sourceID = source.getVariable(sourceIDVariableName);
        if (sourceID == null || sourceID.toString().trim().length() == 0) {
            String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
            String tumourID = (String) source.getVariable(tumourIDVariableName);
            sourceID = getNextSourceID(tumourID);
            source.setVariable(sourceIDVariableName, sourceID);
        }
        return saveRecord(Globals.SOURCE_TABLE_NAME, source, stmtSaveNewSource);
    }

    /**
     *
     * @param dictionary
     * @return
     */
    public synchronized int saveDictionary(Dictionary dictionary) {
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
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    /**
     *
     * @param dictionaryEntry
     * @return
     */
    public synchronized int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        int id = -1;
        try {
            stmtSaveNewDictionaryEntry.clearParameters();

            stmtSaveNewDictionaryEntry.setInt(1, dictionaryEntry.getDictionaryID());

            Dictionary dict = dictionaryMap.get(dictionaryEntry.getDictionaryID());

            //TODO implement a check for valid dictionary code?
            stmtSaveNewDictionaryEntry.setString(2, dictionaryEntry.getCode());

            //Make sure that we have valid length
            String description = dictionaryEntry.getDescription();
            if (dict != null) {
                if (Globals.DICTIONARY_DESCRIPTION_LENGTH < description.length()) {
                    description = description.substring(0, Globals.DICTIONARY_DESCRIPTION_LENGTH);
                }
            } else {
                return id;
            }

            stmtSaveNewDictionaryEntry.setString(3, description);

            int rowCount = stmtSaveNewDictionaryEntry.executeUpdate();
            ResultSet results = stmtSaveNewDictionaryEntry.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    /**
     *
     * @param populationDataSet
     * @return
     */
    public synchronized int saveNewPopulationDataset(PopulationDataset populationDataSet) {

        Map<Integer, PopulationDataset> populationDataSets;
        populationDataSets = getPopulationDatasets();

        int dataSetID = 0;
        while (populationDataSets.get(dataSetID) != null) {
            dataSetID++;
        }
        populationDataSet.setPopulationDatasetID(dataSetID);
        try {
            stmtSaveNewPopoulationDataset.clearParameters();

            stmtSaveNewPopoulationDataset.setInt(1, populationDataSet.getPopulationDatasetID());
            stmtSaveNewPopoulationDataset.setString(2, populationDataSet.getPopulationDatasetName().substring(0, Math.min(Globals.PDS_DATABASE_NAME_LENGTH, populationDataSet.getPopulationDatasetName().length())));
            stmtSaveNewPopoulationDataset.setString(3, populationDataSet.getFilter());
            stmtSaveNewPopoulationDataset.setString(4, populationDataSet.getDate());
            stmtSaveNewPopoulationDataset.setString(5, populationDataSet.getSource());
            stmtSaveNewPopoulationDataset.setString(6, populationDataSet.getAgeGroupStructure().getConstructor());
            stmtSaveNewPopoulationDataset.setString(7, populationDataSet.getDescription());
            stmtSaveNewPopoulationDataset.setInt(8, populationDataSet.getWorldPopulationID());
            if (populationDataSet.isWorldPopulationBool()) {
                stmtSaveNewPopoulationDataset.setInt(9, 1);
            } else {
                stmtSaveNewPopoulationDataset.setInt(9, 0);
            }
            int rowCount = stmtSaveNewPopoulationDataset.executeUpdate();
            ResultSet results = stmtSaveNewPopoulationDataset.getGeneratedKeys();
            if (results.next()) {
                int id = results.getInt(1);
            }

            // Save entries
            PopulationDatasetsEntry[] entries = populationDataSet.getAgeGroups();
            for (PopulationDatasetsEntry entry : entries) {
                entry.setPopulationDatasetID(populationDataSet.getPopulationDatasetID());
                savePopoulationDatasetsEntry(entry);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return populationDataSet.getPopulationDatasetID();

    }

    /**
     *
     * @param populationDatasetsEntry
     * @return
     */
    public synchronized int savePopoulationDatasetsEntry(PopulationDatasetsEntry populationDatasetsEntry) {
        int id = -1;
        try {
            stmtSaveNewPopoulationDatasetsEntry.clearParameters();

            stmtSaveNewPopoulationDatasetsEntry.setInt(1, populationDatasetsEntry.getPopulationDatasetID());
            stmtSaveNewPopoulationDatasetsEntry.setInt(2, populationDatasetsEntry.getAgeGroup());
            stmtSaveNewPopoulationDatasetsEntry.setInt(3, populationDatasetsEntry.getSex());
            stmtSaveNewPopoulationDatasetsEntry.setInt(4, populationDatasetsEntry.getCount());

            int rowCount = stmtSaveNewPopoulationDatasetsEntry.executeUpdate();
            ResultSet results = stmtSaveNewPopoulationDatasetsEntry.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    /**
     *
     * @param nameSexRecord
     * @param replace
     * @return
     */
    public synchronized int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace) {
        int id = -1;
        if (replace) {
            try {
                stmtDeleteNameSexRecord.clearParameters();
                stmtDeleteNameSexRecord.setString(1, nameSexRecord.getName());
                stmtDeleteNameSexRecord.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            stmtSaveNewNameSexRecord.clearParameters();

            stmtSaveNewNameSexRecord.setString(1, nameSexRecord.getName());
            stmtSaveNewNameSexRecord.setInt(2, nameSexRecord.getSex());

            int rowCount = stmtSaveNewNameSexRecord.executeUpdate();
            ResultSet results = stmtSaveNewNameSexRecord.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (java.sql.SQLIntegrityConstraintViolationException sqle) {
            // System.out.println(nameSexRecord.getName());
            // Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        } catch (SQLException sqle) {
            System.out.println(nameSexRecord.getName());
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    /**
     *
     * @return
     */
    public synchronized boolean clearNameSexTable() {
        boolean success = false;
        try {
            stmtClearNameSexTable.clearParameters();

            stmtClearNameSexTable.executeUpdate();
            success = true;

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param dictionaryID
     * @return
     */
    public synchronized boolean deleteDictionaryEntries(int dictionaryID) {
        boolean success = false;
        try {
            stmtDeleteDictionaryEntries.clearParameters();
            stmtDeleteDictionaryEntries.setInt(1, dictionaryID);

            stmtDeleteDictionaryEntries.executeUpdate();
            success = true;

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param patientRecordID
     * @return
     * @throws RecordLockedException
     * @throws SQLException
     */
    public synchronized boolean deletePatientRecord(int patientRecordID) throws RecordLockedException, SQLException {
        boolean success = false;
        if (isRecordLocked(patientRecordID, Globals.PATIENT_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try {
            stmtDeletePatientRecord.clearParameters();
            stmtDeletePatientRecord.setInt(1, patientRecordID);
            stmtDeletePatientRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param tumourRecordID
     * @return
     * @throws RecordLockedException
     */
    public synchronized boolean deleteTumourRecord(int tumourRecordID) throws RecordLockedException {
        boolean success = false;
        if (isRecordLocked(tumourRecordID, Globals.TUMOUR_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try {
            stmtDeleteTumourRecord.clearParameters();
            stmtDeleteTumourRecord.setInt(1, tumourRecordID);
            stmtDeleteTumourRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param sourceRecordID
     * @return
     * @throws RecordLockedException
     */
    public synchronized boolean deleteSourceRecord(int sourceRecordID) throws RecordLockedException {
        boolean success = false;
        if (isRecordLocked(sourceRecordID, Globals.SOURCE_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try {
            stmtDeleteSourceRecord.clearParameters();
            stmtDeleteSourceRecord.setInt(1, sourceRecordID);
            stmtDeleteSourceRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param recordID
     * @param tableName
     * @return
     * @throws RecordLockedException
     * @throws java.sql.SQLException
     */
    public synchronized boolean deleteRecord(int recordID, String tableName) throws RecordLockedException, SQLException {
        boolean success = false;
        if (isRecordLocked(recordID, tableName)) {
            throw new RecordLockedException();
        } else {
            if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                success = deletePatientRecord(recordID);
            } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                success = deleteTumourRecord(recordID);
            } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                success = deleteSourceRecord(recordID);
            } else {
                String idString = "ID";
                // ResultSet results = null;
                Statement statement = dbConnection.createStatement();
                statement.execute("DELETE FROM " + Globals.SCHEMA_NAME + "." + tableName + " WHERE " + idString + " = " + recordID);
                success = true;
            }
        }
        return success;
    }

    /**
     *
     * @param id
     * @return
     */
    public synchronized boolean deletePopulationDataSet(int id) {
        boolean success = false;
        // if (isRecordLocked(id, Globals.POPULATION_DATASET_TABLE_NAME)) {
        //     throw new RecordLockedException();
        // }
        try {
            // First delete entries
            stmtDeletePopoulationDatasetEntries.clearParameters();
            stmtDeletePopoulationDatasetEntries.setInt(1, id);
            stmtDeletePopoulationDatasetEntries.executeUpdate();

            // Then delete description
            stmtDeletePopoulationDataset.clearParameters();
            stmtDeletePopoulationDataset.setInt(1, id);
            stmtDeletePopoulationDataset.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param patient
     * @return
     * @throws RecordLockedException
     */
    public synchronized boolean editPatient(Patient patient) throws RecordLockedException {
        return editRecord("Patient", patient, stmtEditPatient);
    }

    /**
     *
     * @param tumour
     * @return
     * @throws RecordLockedException
     */
    public synchronized boolean editTumour(Tumour tumour) throws RecordLockedException {
        return editRecord("Tumour", tumour, stmtEditTumour);
    }

    /**
     *
     * @param source
     * @return
     * @throws RecordLockedException
     */
    public synchronized boolean editSource(Source source) throws RecordLockedException {
        return editRecord("Source", source, stmtEditSource);
    }

    /**
     *
     * @param tableName
     * @param record
     * @param stmtEditRecord
     * @return
     * @throws RecordLockedException
     */
    private synchronized boolean editRecord(String tableName, DatabaseRecord record, PreparedStatement stmtEditRecord) throws RecordLockedException {
        boolean bEdited = false;
        int id = -1;
        try {
            stmtEditRecord.clearParameters();

            int variableNumber = 0;

            for (DatabaseVariablesListElement variable : variables) {
                String tableNameDB = variable.getDatabaseTableName();
                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    variableNumber++;
                    String variableType = variable.getVariableType();
                    Object obj = record.getVariable(variable.getDatabaseVariableName());
                    if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME) || variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)) {
                        if (obj != null) {
                            try {
                                String strObj = obj.toString();
                                if (strObj.length() > 0) {
                                    stmtEditRecord.setString(variableNumber, strObj);
                                } else {
                                    stmtEditRecord.setString(variableNumber, "");
                                }
                                // System.out.println(
                                //        element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent() +
                                //        ": " + strObj);
                            } catch (java.lang.ClassCastException cce) {
                                debugOut("Cast to String Error. Type:" + variableType + ", Value: " + obj + ", Variable Number: " + variableNumber);
                                throw cce;
                            }
                        } else {
                            stmtEditRecord.setString(variableNumber, "");
                        }
                        //<ictl.co>
                    } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)) {
                        if (obj != null) {
                            try {
                                String strObj = obj.toString();
                                if (strObj.length() > 0) {
                                    stmtEditRecord.setString(variableNumber, strObj);
                                } else {
                                    stmtEditRecord.setString(variableNumber, "");
                                }
/*
                                Long intObj = Long.parseLong(strObj);
                                if (!GlobalToolBox.validateNCID(strObj)) {
                                    throw new IllegalArgumentException("Invalid NCID");
                                }
                                stmtEditRecord.setString(variableNumber, strObj);
*/
                            } catch (java.lang.NumberFormatException cce) {
                                debugOut("NCID " + variableType + " " + obj);
                                throw cce;
                            } catch (java.lang.ClassCastException cce) {
                                debugOut("NCID " + variableType + " " + obj);
                                throw cce;
                            }
                        }
//</ictl.co>
                    } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                        if (obj != null) {
                            try {
                                Integer intObj = (Integer) obj;
                                stmtEditRecord.setInt(variableNumber, intObj);
                                // System.out.println(
                                //        element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent() +
                                //        ": " + obj.toString());
                            } catch (java.lang.ClassCastException cce) {
                                debugOut("Number " + variableType + " " + obj);
                                throw cce;
                            }
                        } else {
                            stmtEditRecord.setInt(variableNumber, -1);
                        }
                    }
                }
            }
            // add the ID
            String idString = "id";
            if (record instanceof Patient) {
                idString = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (record instanceof Tumour) {
                idString = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (record instanceof Source) {
                idString = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
            }
            int idInt = (Integer) record.getVariable(idString);
            stmtEditRecord.setInt(variableNumber + 1, idInt);
            if (isRecordLocked(idInt, tableName)) {
                throw new RecordLockedException();
            }
            int rowCount = stmtEditRecord.executeUpdate();

            // If this is a tumour we save the sources...
            if (record instanceof Tumour) {
                Tumour tumour = (Tumour) record;
                String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
                Object tumourID = tumour.getVariable(tumourIDVariableName);
                Set<Source> sources = tumour.getSources();
                // delete old sources
                try {
                    deleteSources(tumourID);
                } catch (DistributedTableDescriptionException ex) {
                    Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownTableException ex) {
                    Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
                // save each of the source records
                saveSources(tumourID, sources);
            }

            bEdited = true;

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }

        return bEdited;
    }

    private synchronized boolean fillPopulationDatasetTables() {
        PopulationDataset pds = new PopulationDataset();
        pds.setWorldPopulationBool(true);
        pds.setPopulationDatasetName("World Standard Population");
        pds.setSource("SEGI 1960 / World Health Organization");
        pds.setDescription("http://www.who.int/healthinfo/paper31.pdf");
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));

        int i = 0;
        for (int ageGroupWeight : Globals.standardWorldPopulationWeights) {
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 1, ageGroupWeight));
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 2, ageGroupWeight));
            i++;
        }
        saveNewPopulationDataset(pds);

        pds = new PopulationDataset();
        pds.setWorldPopulationBool(true);
        pds.setPopulationDatasetName("European Standard Population");
        pds.setSource("World Health Organization");
        pds.setDescription("http://www.who.int/healthinfo/paper31.pdf");
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));

        i = 0;
        for (int ageGroupWeight : Globals.standardEuropeanPopulationWeights) {
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 1, ageGroupWeight));
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 2, ageGroupWeight));
            i++;
        }
        saveNewPopulationDataset(pds);

        pds = new PopulationDataset();
        pds.setWorldPopulationBool(true);
        pds.setPopulationDatasetName("WHO Standard Population");
        pds.setSource("World Health Organization");
        pds.setDescription("http://www.who.int/healthinfo/paper31.pdf");
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));

        i = 0;
        for (int ageGroupWeight : Globals.standardWHOPopulationWeights) {
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 1, ageGroupWeight));
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 2, ageGroupWeight));
            i++;
        }
        saveNewPopulationDataset(pds);

        return true;
    }

    private synchronized boolean fillDictionariesTable() {
        boolean bFilled = false;

        // Go through all the variable definitions
        for (Dictionary dic : dictionaryMap.values()) {
            saveDictionary(dic);
        }
        // fill individual dictionaries with default codes
        // Record status
        try {
            fillDictionary(Globals.StandardVariableNames.TumourRecordStatus, Globals.DEFAULT_DICTIONARIES_FOLDER + "/recordstatus.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Check status
        try {
            fillDictionary(Globals.StandardVariableNames.CheckStatus, Globals.DEFAULT_DICTIONARIES_FOLDER + "/checkstatus.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Person search
        try {
            fillDictionary(Globals.StandardVariableNames.PersonSearch, Globals.DEFAULT_DICTIONARIES_FOLDER + "/mpstatus.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Topography
        try {
            fillDictionary(Globals.StandardVariableNames.Topography, Globals.DEFAULT_DICTIONARIES_FOLDER + "/topography.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Morphology
        try {
            fillDictionary(Globals.StandardVariableNames.Morphology, Globals.DEFAULT_DICTIONARIES_FOLDER + "/morphology.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Behaviour
        try {
            fillDictionary(Globals.StandardVariableNames.Behaviour, Globals.DEFAULT_DICTIONARIES_FOLDER + "/behaviour.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Basis
        try {
            fillDictionary(Globals.StandardVariableNames.BasisDiagnosis, Globals.DEFAULT_DICTIONARIES_FOLDER + "/basis.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Sex
        try {
            fillDictionary(Globals.StandardVariableNames.Sex, Globals.DEFAULT_DICTIONARIES_FOLDER + "/sex.tsv");
        } catch (IOException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        bFilled = true;

        return bFilled;
    }

    private synchronized static Map<Integer, Dictionary> buildDictionaryMap(Document doc) {

        Map<Integer, Dictionary> dictionariesMap = new LinkedHashMap();
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
            dictionariesMap.put(i, dic);
        }
        return dictionariesMap;
    }

    /* 
     * @param index
     * @return
     */

    /**
     *
     * @param recordID
     * @return
     */
    private synchronized Patient getPatient(int recordID, boolean lock) throws RecordLockedException {
        Patient record = null;
        ResultSetMetaData metadata;
        // we are allowed to read a record that is locked...
        if (lock && isRecordLocked(recordID, Globals.PATIENT_TABLE_NAME)) {
            throw new RecordLockedException();
        } else if (lock) {
            lockRecord(recordID, Globals.PATIENT_TABLE_NAME);
        }
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
            result = null;
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }

        return record;
    }

    /* 
     * @param index
     * @return
     */

    /**
     *
     * @param recordID
     * @return
     */
    private synchronized Tumour getTumour(int recordID, boolean lock) throws RecordLockedException {
        Tumour record = null;
        ResultSetMetaData metadata;
        if (lock && isRecordLocked(recordID, Globals.TUMOUR_TABLE_NAME)) {
            throw new RecordLockedException();
        } else if (lock) {
            lockRecord(recordID, Globals.TUMOUR_TABLE_NAME);
        }
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

            // get the source information
            String recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
            Object tumourID;
            tumourID = record.getVariable(recordIDVariableName);

            Set<Source> sources = null;
            try {
                // We don't lock the sources...
                sources = getSourcesByTumourID(tumourID, false);
            } catch (DistributedTableDescriptionException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownTableException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            }

            record.setSources(sources);
            result = null;

        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return record;
    }

    /**
     *
     * @param recordID
     * @return
     */
    private synchronized Source getSource(int recordID, boolean lock) throws RecordLockedException {
        Source record = null;
        ResultSetMetaData metadata;
        if (lock && isRecordLocked(recordID, Globals.SOURCE_TABLE_NAME)) {
            throw new RecordLockedException();
        } else if (lock) {
            lockRecord(recordID, Globals.SOURCE_TABLE_NAME);
        }
        try {
            stmtGetSource.clearParameters();
            stmtGetSource.setInt(1, recordID);
            ResultSet result = stmtGetSource.executeQuery();
            metadata = result.getMetaData();
            int numberOfColumns = metadata.getColumnCount();
            if (result.next()) {
                record = new Source();
                for (int i = 1; i <= numberOfColumns; i++) {
                    if (metadata.getColumnType(i) == java.sql.Types.VARCHAR) {
                        record.setVariable(metadata.getColumnName(i), result.getString(metadata.getColumnName(i)));
                    } else if (metadata.getColumnType(i) == java.sql.Types.INTEGER) {
                        record.setVariable(metadata.getColumnName(i), result.getInt(metadata.getColumnName(i)));
                    }
                }
            }
            result = null;
        } catch (SQLException sqle) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return record;
    }

    /**
     *
     * @return
     */
    public synchronized String getNextPatientID() {
        String patientID = null;
        try {
            ResultSet result = stmtGetHighestPatientID.executeQuery();
            result.next();
            String highestPatientID = result.getString(1);
            result.close();
            if (highestPatientID != null) {
                patientID = canreg.common.Tools.increment(highestPatientID);
            } else {
                //<ictl.co>
                int year = Calendar.getInstance().get(Calendar.YEAR);
                if (LocalizationHelper.isRtlLanguageActive() && LocalizationHelper.isPersianLocale()) {
                    year = DateHelper.convertJalaliYearToGregoranYear(year);
                }
                //<ictl.co>
                patientID = year + "";
                int patientIDlength = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getVariableLength();
                while (patientID.length() < patientIDlength - 1) {
                    patientID += "0";
                }
                if (patientID.length() == patientIDlength - 1) {
                    patientID += "1";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientID;
    }

    /**
     *
     * @param patientRecordID
     * @return
     */
    public synchronized String getNextTumourID(String patientRecordID) {
        String tumourID = null;
        try {
            stmtGetHighestTumourID.clearParameters();
            stmtGetHighestTumourID.setString(1, patientRecordID);
            ResultSet result = stmtGetHighestTumourID.executeQuery();
            result.next();
            String highestTumourID = result.getString(1);
            if (highestTumourID != null) {
                tumourID = canreg.common.Tools.increment(highestTumourID);
            } else {
                tumourID = patientRecordID;
                int tumourIDlength = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getVariableLength();
                while (tumourID.length() < tumourIDlength - 1) {
                    tumourID += "0";
                }
                if (tumourID.length() == tumourIDlength - 1) {
                    tumourID += "1";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tumourID;
    }

    /**
     *
     * @param patientID
     * @return
     */
    public synchronized String getNextPatientRecordID(String patientID) {
        String patientRecordID = null;
        try {
            stmtGetHighestPatientRecordID.clearParameters();
            stmtGetHighestPatientRecordID.setString(1, patientID);
            ResultSet result = stmtGetHighestPatientRecordID.executeQuery();
            result.next();
            String highestPatientRecordID = result.getString(1);
            if (highestPatientRecordID != null) {
                patientRecordID = canreg.common.Tools.increment(highestPatientRecordID);
            } else {
                patientRecordID = patientID;
                int patientRecordIDlength = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getVariableLength();
                while (patientRecordID.length() < patientRecordIDlength - 1) {
                    patientRecordID += "0";
                }
                if (patientRecordID.length() == patientRecordIDlength - 1) {
                    patientRecordID += "1";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientRecordID;
    }

    /**
     *
     * @param tumourRecordID
     * @return
     */
    public synchronized String getNextSourceID(String tumourRecordID) {
        String sourceID = null;
        try {
            stmtGetHighestSourceRecordID.clearParameters();
            stmtGetHighestSourceRecordID.setString(1, tumourRecordID);
            ResultSet result = stmtGetHighestSourceRecordID.executeQuery();
            result.next();
            String highestSourceID = result.getString(1);
            if (highestSourceID != null) {
                sourceID = canreg.common.Tools.increment(highestSourceID);
            } else {
                sourceID = tumourRecordID;
                int sourceIDlength = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getVariableLength();
                while (sourceID.length() < sourceIDlength - 1) {
                    sourceID += "0";
                }
                if (sourceID.length() == sourceIDlength - 1) {
                    sourceID += "1";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sourceID;
    }

    /**
     *
     * @return
     */
    public synchronized String getNextTumourRecordID() {
        String tumourRecordID = null;
        try {
            ResultSet result = stmtGetHighestTumourRecordID.executeQuery();
            result.next();
            String highestTumourRecordID = result.getString(1);
            if (highestTumourRecordID != null) {
                tumourRecordID = canreg.common.Tools.increment(highestTumourRecordID);
            } else {
                tumourRecordID = "1";
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tumourRecordID;
    }

    /**
     *
     */
    protected synchronized void dropAndRebuildUsersTable() {
        try {
            Statement statement;
            statement = dbConnection.createStatement();
            statement.execute("DROP TABLE " + Globals.SCHEMA_NAME + ".USERS");
            statement.execute(QueryGenerator.strCreateUsersTable());
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<Source> getSourcesByTumourID(Object tumourID, boolean lock) throws SQLException, DistributedTableDescriptionException, UnknownTableException, RecordLockedException {
        String recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();

        DatabaseFilter filter = new DatabaseFilter();
        filter.setFilterString(recordIDVariableName + " = '" + tumourID + "' ");
        DistributedTableDescription distributedTableDescription;
        Object[][] rows;

        distributedTableDescription = getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, Globals.SOURCE_TABLE_NAME, generateResultSetID());
        int numberOfRecords = distributedTableDescription.getRowCount();

        rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords);
        releaseResultSet(distributedTableDescription.getResultSetID());

        Set<Source> sources = Collections.synchronizedSet(new LinkedHashSet<Source>());

        String[] columnNames = distributedTableDescription.getColumnNames();
        int ids[] = new int[numberOfRecords];
        boolean found = false;
        int idColumnNumber = 0;

        while (!found && idColumnNumber < columnNames.length) {
            found = columnNames[idColumnNumber++].equalsIgnoreCase(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
        }
        if (found) {
            idColumnNumber--;
            Source source;
            for (Object[] row : rows) {
                int id = (Integer) row[idColumnNumber];
                source = (Source) getRecord(id, Globals.SOURCE_TABLE_NAME, lock);
                sources.add(source);
            }
        }

        return sources;
    }

    /**
     * Simple console trace to system.out for debug purposes only.
     *
     * @param message the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.INFO, msg);
        }
    }

    private Connection dbConnection;
    private Properties dbProperties;
    private boolean isConnected;
    private final String systemCode;
    private final Document doc;
    private Map<Integer, Dictionary> dictionaryMap;
    private final Map<String, Set<Integer>> locksMap;
    private final Map<String, DistributedTableDataSource> distributedDataSources;
    private final Map<String, Statement> activeStatements;
    private boolean tableOfDictionariesFilled = true;
    private boolean tableOfPopulationDataSets = true;
    private PreparedStatement stmtSaveNewPatient;
    private PreparedStatement stmtSaveNewTumour;
    private PreparedStatement stmtSaveNewSource;
    private PreparedStatement stmtEditPatient;
    private PreparedStatement stmtEditTumour;
    private PreparedStatement stmtEditSource;
    private PreparedStatement stmtSaveNewDictionary;
    private PreparedStatement stmtSaveNewDictionaryEntry;
    private PreparedStatement stmtSaveNewPopoulationDatasetsEntry;
    private PreparedStatement stmtSaveNewUser;
    private PreparedStatement stmtEditUser;
    private PreparedStatement stmtSaveNewPopoulationDataset;
    private PreparedStatement stmtSaveNewNameSexRecord;
    private PreparedStatement stmtDeleteNameSexRecord;
    private PreparedStatement stmtUpdateExistingPatient;
    private PreparedStatement stmtGetPatient;
    private PreparedStatement stmtGetTumour;
    private PreparedStatement stmtGetSource;
    private PreparedStatement stmtGetPatients;
    private PreparedStatement stmtGetSources;
    private PreparedStatement stmtGetTumours;
    private PreparedStatement stmtGetPatientsAndTumours;
    private PreparedStatement stmtGetSourcesAndTumours;
    private PreparedStatement stmtGetRecordsAllTables;
    private PreparedStatement stmtGetRecord;
    private PreparedStatement stmtGetRecords;
    private PreparedStatement stmtGetDictionary;
    private PreparedStatement stmtGetDictionaryEntry;
    private PreparedStatement stmtDeleteDictionaryEntry;
    private PreparedStatement stmtDeleteDictionaryEntries;
    private PreparedStatement stmtClearNameSexTable;
    private PreparedStatement stmtDeletePatientRecord;
    private PreparedStatement stmtDeleteTumourRecord;
    private PreparedStatement stmtDeleteSourceRecord;
    private PreparedStatement stmtDeletePopoulationDataset;
    private PreparedStatement stmtDeletePopoulationDatasetEntries;
    private PreparedStatement stmtGetHighestPatientID;
    private PreparedStatement stmtGetHighestPatientRecordID;
    private PreparedStatement stmtGetHighestTumourID;
    private PreparedStatement stmtGetHighestTumourRecordID;
    private PreparedStatement stmtGetHighestSourceRecordID;
    private PreparedStatement stmtMaxNumberOfSourcesPerTumourRecord;
    private final String ns = Globals.NAMESPACE;
    private static final String strGetPatient =
            "SELECT * FROM APP.PATIENT "
                    + "WHERE " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private final String strGetPatients =
            "SELECT * FROM APP.PATIENT";
    private final String strGetUsers =
            "SELECT * FROM APP.USERS";
    private final String strCountPatients =
            "SELECT COUNT(*) FROM APP.PATIENT";
    private final String strCountSources =
            "SELECT COUNT(*) FROM APP.SOURCE";
    private final String strGetSources =
            "SELECT * FROM APP.SOURCE";
    private final String strGetPatientsAndTumours;
    private final String strCountPatientsAndTumours;
    private final String strGetSourcesAndTumours;
    private final String strCountSourcesAndTumours;
    private final String strGetSourcesAndTumoursAndPatients;
    private final String strCountSourcesAndTumoursAndPatients;
    private static final String strGetTumour =
            "SELECT * FROM APP.TUMOUR "
                    + "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strGetSource =
            "SELECT * FROM APP.Source "
                    + "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private final String strGetTumours =
            "SELECT * FROM APP.TUMOUR";
    private final String strCountTumours =
            "SELECT COUNT(*) FROM APP.TUMOUR";
    private static final String strGetDictionary =
            "SELECT * FROM APP.DICTIONARIES "
                    + "WHERE ID = ?";
    private final String strGetDictionaries =
            "SELECT * FROM APP.DICTIONARIES ";
    private static final String strGetDictionaryEntry =
            "SELECT * FROM APP.DICTIONARY "
                    + "WHERE ID = ?";
    private static final String strGetDictionaryEntries =
            "SELECT * FROM APP.DICTIONARY ORDER BY ID";
    private static final String strGetPopulationDatasetEntries =
            "SELECT * FROM APP.PDSET ";
    private static final String strGetPopulationDatasets =
            "SELECT * FROM APP.PDSETS ";
    private static final String strGetNameSexRecords =
            "SELECT * FROM APP.NAMESEX ";
    private static final String strDeletePatientRecord =
            "DELETE FROM APP.PATIENT "
                    + "WHERE " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strDeleteSourceRecord =
            "DELETE FROM APP.SOURCE "
                    + "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strDeleteTumourRecord =
            "DELETE FROM APP.TUMOUR "
                    + "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strDeleteDictionaryEntries =
            "DELETE FROM APP.DICTIONARY "
                    + "WHERE DICTIONARY = ?";
    private static final String strClearNameSexTable =
            "DELETE FROM APP.NAMESEX";
    private static final String strDeletePopulationDataset =
            "DELETE FROM APP.PDSETS "
                    + "WHERE PDS_ID = ?";
    private static final String strDeletePopulationDatasetEntries =
            "DELETE FROM APP.PDSET "
                    + "WHERE PDS_ID = ?";
    // The Dynamic ones
    private String strMaxNumberOfSourcesPerTumourRecord;
    private String strSavePatient;
    private String strSaveTumour;
    private String strSaveSource;
    private String strEditPatient;
    private String strEditTumour;
    private String strEditSource;
    private String strSaveDictionary;
    private String strSaveDictionaryEntry;
    private String strSavePopoulationDataset;
    private String strSavePopoulationDatasetsEntry;
    private String strSaveNameSexRecord;
    private String strDeleteNameSexRecord;
    private String strGetHighestPatientID;
    private String strGetHighestTumourID;
    private String strGetHighestPatientRecordID;
    private String strGetHighestSourceRecordID;
    private String strEditUser;
    private String strSaveUser;
    /* We don't use tumour record ID...
     private String strGetHighestTumourRecordID;
     */
    private final GlobalToolBox globalToolBox;

    private synchronized void saveSources(Object tumourID, Set<Source> sources) throws SQLException {
        if (sources != null) {
            String tumourIDSourceTableVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
            for (Source source : sources) {
                source.setVariable(tumourIDSourceTableVariableName, tumourID);
                saveSource(source);
            }
        }
    }

    private synchronized void deleteSources(Object tumourID) throws SQLException, DistributedTableDescriptionException, UnknownTableException, RecordLockedException {
        Set<Source> sources = getSourcesByTumourID(tumourID, false);
        if (sources != null) {
            for (Source source : sources) {
                if (source != null) {
                    int recordID = (Integer) source.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                    deleteSourceRecord(recordID);
                }
            }
        }
    }

    // release a locked record

    /**
     *
     * @param recordID
     * @param tableName
     */
    public synchronized void releaseRecord(int recordID, String tableName) {
        // release a locked record
        Set lockSet = locksMap.get(tableName);
        if (lockSet != null) {
            lockSet.remove(recordID);
        }
    }

    private synchronized void lockRecord(int recordID, String tableName) {
        Set lockSet = locksMap.get(tableName);
        if (lockSet == null) {
            lockSet = new TreeSet<Integer>();
            locksMap.put(tableName, lockSet);
        }
        lockSet.add(recordID);
    }

    private synchronized boolean isRecordLocked(int recordID, String tableName) {
        boolean lock = false;
        Set lockSet = locksMap.get(tableName);
        if (lockSet != null) {
            lock = lockSet.contains(recordID);
        }
        return lock;
    }

    /**
     *
     * @return
     */
    public DatabaseStats getDatabaseStats() {
        DatabaseStats dbs = new DatabaseStats();
        try {
            ResultSet result = stmtMaxNumberOfSourcesPerTumourRecord.executeQuery();
            result.next();
            int maxNumberOfSourcesPerTumourRecord = result.getInt(1);
            dbs.setMaxNumberOfSourcesPerTumourRecord(maxNumberOfSourcesPerTumourRecord);

        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbs;
    }

    /**
     *
     * @param resultSetID
     * @return
     * @throws SQLException
     */
    public boolean interruptQuery(String resultSetID) throws SQLException {
        Statement statement = activeStatements.get(resultSetID);
        if (statement != null) {
            statement.cancel();
            return true;
        } else {
            return false;
        }
    }

    private DistributedTableDataSource initiatePersonSearchQuery(DatabaseFilter filter, Statement statement) throws SQLException, DistributedTableDescriptionException {
        ResultSet result;
        String query = "";
        String rangePart = "";
        int rowCount = -1;
        DistributedTableDataSource dataSource;

        if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
            rangePart = QueryGenerator.buildRangePart(filter);
            if (rangePart.length() > 0) {
                rangePart = " WHERE " + rangePart;
            }
        }

        query = "SELECT COUNT(*) FROM APP.PATIENT" + rangePart;
        System.out.print(query);
        ResultSet countRowSet = statement.executeQuery(query);
        if (countRowSet.next()) {
            rowCount = countRowSet.getInt(1);
        }
        countRowSet = null;

        query = "SELECT " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " FROM APP.PATIENT" + rangePart;
        try {
            result = statement.executeQuery(query);
        } catch (java.sql.SQLSyntaxErrorException ex) {
            throw ex;
        }
        if (rowCount > 0) {
            dataSource = new DistributedTableDataSourceResultSetImpl(rowCount, result);
        } else {
            dataSource = new DistributedTableDataSourceResultSetImpl(result);
        }

        return dataSource;
    }

    private DistributedTableDataSource initiateFrequenciesByYearQuery(DatabaseFilter filter, Statement statement, String tableName) throws SQLException, DistributedTableDescriptionException {
        ResultSet result;
        String query = "";
        String rangePart = "";
        int rowCount = -1;
        DistributedTableDataSource dataSource;

        Set<DatabaseVariablesListElement> filterVariables;
        String filterString = filter.getFilterString();

        if (!filterString.isEmpty()) {
/*<ictl.co>
            if ((tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME))) {
                filterString = " WHERE ( " + filterString + " )";
            } else {
                filterString = " AND ( " + filterString + " )";
            }
</ictl.co>*/
//<ictl.co>
            filterString = " AND (" + filterString + ")";
//</ictl.co>
        }

        // Add the range part
        if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
/*<ictl.co>
            if (!filterString.isEmpty())
                filterString += " AND ";
            else { 
                filterString += " WHERE ";
            }
</ictl.co>*/
//<ictl.co>
            filterString += " AND ";
//</ictl.co>
            String rangeFilterString = QueryGenerator.buildRangePart(filter);
            filterString += rangeFilterString;
        }
        filterVariables = filter.getDatabaseVariables();
        String variablesList = "";
        //<ictl.co>
        String aliasList = "";
        //</ictl.co>
        if (filterVariables.size() > 0) {
            for (DatabaseVariablesListElement variable : filterVariables) {
                if (variable != null) {
                    //<ictl.co>
//                    variablesList += ", APP." + variable.getDatabaseTableName() + "." + variable.getDatabaseVariableName();
                    variablesList += ", APP." + variable.getDatabaseTableName() + "." + variable.getDatabaseVariableName() + " as " + variable.getDatabaseVariableName();
                    aliasList += "," + variable.getDatabaseVariableName();
                    //</ictl.co>
                }
            }

            // variablesList = variablesList.substring(0, variablesList.length() - 2);

        }
        String patientIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        String incidenceDateVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString()).getDatabaseVariableName();
        String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();

        if (tableName.equalsIgnoreCase(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)
                || tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            //<ictl.co>
/*
            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.TUMOUR, APP.PATIENT "
                    + "WHERE APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " "
                    + filterString + " "
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
*/
            query = "SELECT x as \"YEAR\"" + aliasList + ",COUNT(*) as Cases " +
                    "FROM (SELECT " + Globals.SCHEMA_NAME + ".TODATE(" + incidenceDateVariableName + ",'yyyy','" + Locale.getDefault().getLanguage() + "') " + variablesList + " "
                    + "FROM APP.TUMOUR, APP.PATIENT "
                    + "WHERE APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " "
                    + filterString + ") t(x" + aliasList + ") "
                    + "GROUP BY x " + aliasList + " "
                    + "ORDER BY x " + aliasList;

//</ictl.co>
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)
                || tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
            //<ictl.co>
/*
            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.SOURCE, APP.TUMOUR "
                    + "WHERE APP.SOURCE." + tumourIDVariableNameSourceTable + " = APP.TUMOUR." + tumourIDVariableNameTumourTable + " "
                    + filterString + " "
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
*/
            query = "SELECT x as \"YEAR\"" + aliasList + ",COUNT(*) as Cases " +
                    "FROM (SELECT " + Globals.SCHEMA_NAME + ".TODATE(" + incidenceDateVariableName + ",'yyyy','" + Locale.getDefault().getLanguage() + "') " + variablesList + " "
                    + "FROM APP.SOURCE, APP.TUMOUR "
                    + "WHERE APP.SOURCE." + tumourIDVariableNameSourceTable + " = APP.TUMOUR." + tumourIDVariableNameTumourTable + " "
                    + filterString + ") t(x" + aliasList + ") "
                    + "GROUP BY x " + aliasList + " "
                    + "ORDER BY x " + aliasList;
//</ictl.co>
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            //<ictl.co>
/*            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.TUMOUR "
                    + (filterString.trim().length() > 0 ? filterString + " " : "")
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
*/
            query = "SELECT x as \"YEAR\"" + aliasList + ",COUNT(*) as Cases " +
                    "FROM (SELECT " + Globals.SCHEMA_NAME + ".TODATE(" + incidenceDateVariableName + ",'yyyy','" + Locale.getDefault().getLanguage() + "') " + variablesList + " "
                    + "FROM APP.TUMOUR "
                    + (filterString.trim().length() > 0 ? filterString + " " : "") + ") t(x" + aliasList + ") "
                    + "GROUP BY x " + aliasList + " "
                    + "ORDER BY x " + aliasList;
//</ictl.co>
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            //<ictl.co>
/*            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.SOURCE, APP.TUMOUR, APP.PATIENT "
                    + "WHERE APP.SOURCE." + tumourIDVariableNameSourceTable + " = APP.TUMOUR." + tumourIDVariableNameTumourTable + " "
                    + "AND APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " "
                    + filterString + " "
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
*/
            query = "SELECT x as \"YEAR\"" + aliasList + ",COUNT(*) as Cases " +
                    "FROM (SELECT " + Globals.SCHEMA_NAME + ".TODATE(" + incidenceDateVariableName + ",'yyyy','" + Locale.getDefault().getLanguage() + "') " + variablesList + " "
                    + "FROM APP.SOURCE, APP.TUMOUR, APP.PATIENT "
                    + "WHERE APP.SOURCE." + tumourIDVariableNameSourceTable + " = APP.TUMOUR." + tumourIDVariableNameTumourTable + " "
                    + "AND APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " "
                    + filterString + ") t(x" + aliasList + ") "
                    + "GROUP BY x " + aliasList + " "
                    + "ORDER BY x " + aliasList;
//</ictl.co>
        }
        System.out.println(query);
        try {
            result = statement.executeQuery(query);
        } catch (java.sql.SQLSyntaxErrorException ex) {
            throw ex;
        }

        if (rowCount > 0) {
            dataSource = new DistributedTableDataSourceResultSetImpl(rowCount, result);
        } else {
            dataSource = new DistributedTableDataSourceResultSetImpl(result);
        }

        return dataSource;
    }

    private DistributedTableDataSource initiateTableQuery(DatabaseFilter filter, Statement statement, String tableName) throws UnknownTableException, SQLException, DistributedTableDescriptionException {
        ResultSet result;
        int rowCount = -1;
        DistributedTableDataSource dataSource;

        counterStringBuilder.delete(0, counterStringBuilder.length());
        getterStringBuilder.delete(0, getterStringBuilder.length());
        filterStringBuilder.delete(0, filterStringBuilder.length());

        boolean joinedTables = false;

        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            counterStringBuilder.append(strCountTumours);
            getterStringBuilder.append(strGetTumours);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            counterStringBuilder.append(strCountPatients);
            getterStringBuilder.append(strGetPatients);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            counterStringBuilder.append(strCountSources);
            getterStringBuilder.append(strGetSources);
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            counterStringBuilder.append(strCountPatientsAndTumours);
            getterStringBuilder.append(strGetPatientsAndTumours);
            joinedTables = true;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
            counterStringBuilder.append(strCountSourcesAndTumours);
            getterStringBuilder.append(strGetSourcesAndTumours);
            joinedTables = true;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            counterStringBuilder.append(strCountSourcesAndTumoursAndPatients);
            getterStringBuilder.append(strGetSourcesAndTumoursAndPatients);
            joinedTables = true;
        } else {
            throw new UnknownTableException("Unknown table name.");
        }
        String filterString = filter.getFilterString();
        int parentesOverskudd = 0;
        if (!filterString.isEmpty()) {
            if (joinedTables) {
                filterStringBuilder.append(" AND ").append("(").append(filterString);
                parentesOverskudd++;
            } else {
                filterStringBuilder.append(" WHERE ").append("(").append(filterString).append(")");
            }
        }
        //else {
        //    filterStringBuilder = filterStringBuilder.append("(").append(filter.getFilterString()).append(")");
        // }

        // Add the range part
        if ((filter.getRangeStart() != null
                && filter.getRangeStart().length() > 0)
                || (filter.getRangeEnd()
                != null && filter.getRangeEnd().length() > 0)) {
            if (filterStringBuilder.length() == 0 && !joinedTables) {
                filterStringBuilder = new StringBuilder(" WHERE ").append(filterString);
            } else {
                filterStringBuilder.append(" AND ");
            }
            filterStringBuilder.append(QueryGenerator.buildRangePart(filter));
        }

        for (int i = 0; i < parentesOverskudd; i++) {
            filterStringBuilder.append(")");
        }

        /* debug stuff */
        System.out.println("filterString: " + filterStringBuilder);
        System.out.println("getterString: " + getterStringBuilder);
        System.out.println("counterString: " + counterStringBuilder);
         /* */

        ResultSet countRowSet;
        try {
            countRowSet = statement.executeQuery(counterStringBuilder.toString() + " " + filterStringBuilder.toString());
        } catch (java.sql.SQLSyntaxErrorException ex) {
            throw ex;
        }

        // Count the rows...
        if (countRowSet.next()) {
            rowCount = countRowSet.getInt(1);
        }
        // feed it to the garbage dump
        countRowSet = null;
        if (filter.getSortByVariable() != null) {
            filterStringBuilder.append(" ORDER BY \"").append(canreg.common.Tools.toUpperCaseStandardized(filter.getSortByVariable())).append("\"");
        }
        try {
            result = statement.executeQuery(getterStringBuilder.toString() + " " + filterStringBuilder.toString());
        } catch (java.sql.SQLSyntaxErrorException ex) {
            throw ex;
        }

        if (rowCount > 0) {
            dataSource = new DistributedTableDataSourceResultSetImpl(rowCount, result);
        } else {
            dataSource = new DistributedTableDataSourceResultSetImpl(result);
        }
        return dataSource;
    }

    private void fillDictionary(Globals.StandardVariableNames standardVariableName, String fileName) throws IOException {
        DatabaseVariablesListElement element =
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        standardVariableName.toString());
        if (element != null) {
            DatabaseDictionaryListElement dictionary = element.getDictionary();
            InputStream in = getClass().getResourceAsStream(fileName);
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String[] elements;
            try {
                String line = br.readLine();
                while (line != null) {
                    elements = line.split("\t");
                    DictionaryEntry entry = new DictionaryEntry(dictionary.getDictionaryID(), elements[0], elements[1]);
                    saveDictionaryEntry(entry);
                    line = br.readLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                br.close();
            }
        }
    }

    public boolean dropAndRebuildKeys() throws SQLException {
        Statement statement = dbConnection.createStatement();
        return dropAndRebuildKeys(statement);
    }

    private boolean dropAndRebuildKeys(Statement statement) throws SQLException {
        boolean success = true;
        // Set primary keys in patient table
        for (String command : QueryGenerator.strCreatePatientTablePrimaryKey(
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName())) {
            try {
                System.out.println(command);
                statement.execute(command);
            } catch (SQLException sqle) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.WARNING, null, sqle);
                success = false;
            }
        }
        // Set primary keys in tumour table
        for (String command : QueryGenerator.strCreateTumourTablePrimaryKey(
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName())) {
            try {
                System.out.println(command);
                statement.execute(command);
            } catch (SQLException sqle) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.WARNING, null, sqle);
                success = false;
            }
        }

        // Set primary keys in source table
        for (String command : QueryGenerator.strCreateSourceTablePrimaryKey(
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName())) {
            try {
                System.out.println(command);
                statement.execute(command);
            } catch (SQLException sqle) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.WARNING, null, sqle);
                success = false;
            }
        }

        // Set foreign keys in tumour table
        for (String command : QueryGenerator.strCreateTumourTableForeignKey(
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName(),
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName())) {
            try {
                System.out.println(command);
                statement.execute(command);
            } catch (SQLException sqle) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.WARNING, null, sqle);
                success = false;
            }
        }
        // Set foreign keys in source table
        for (String command : QueryGenerator.strCreateSourceTableForeignKey(
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName(),
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName())) {
            try {
                System.out.println(command);
                statement.execute(command);
            } catch (SQLException sqle) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.WARNING, null, sqle);
                success = false;
            }
        }
        return success;
    }

    public boolean addColumnToTable(String columnName, String columnType, String table) throws SQLException {
        boolean success = false;

        Statement statement = dbConnection.createStatement();
        statement.execute(QueryGenerator.strAddColumnToTable(columnName, columnType, table));
        success = true;

        return success;
    }

    public boolean setColumnDataType(String columnName, String columnType, String table) throws SQLException {
        boolean success;

        Statement statement = dbConnection.createStatement();
        statement.execute(QueryGenerator.strSetColumnDataType(columnName, columnType, table));
        success = true;

        return success;
    }

    public boolean dropColumnFromTable(String columnName, String table) throws SQLException {
        boolean success;

        Statement statement = dbConnection.createStatement();
        statement.execute(QueryGenerator.strDropColumnFromTable(columnName, table));
        success = true;

        return success;
    }

    /**
     * @return the systemCode
     */
    public String getSystemCode() {
        return systemCode;
    }
}
