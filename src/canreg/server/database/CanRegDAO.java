/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2020 International Agency for Research on Cancer
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
package canreg.server.database;

import canreg.common.Tools;
import canreg.common.database.User;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDatasetsEntry;
import canreg.common.database.Tumour;
import canreg.common.database.PopulationDataset;
import canreg.common.database.Source;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.AgeGroupStructure;
import canreg.common.database.DatabaseRecord;
import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDataSource;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.server.DatabaseStats;
import org.w3c.dom.Document;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    
    private static final Logger LOGGER = Logger.getLogger(CanRegDAO.class.getName());
    private static final boolean DEBUG = false;
    private static final String  BOOT_PASSWORD ="bootPassword";
    private static final String  SHUT_DOWN = "shutdown";
    private static final String  PAPER_31_PDF = "http://www.who.int/healthinfo/paper31.pdf";

    private final DatabaseVariablesListElement[] variables;
    StringBuilder counterStringBuilder = new StringBuilder();
    StringBuilder getterStringBuilder = new StringBuilder();
    StringBuilder filterStringBuilder = new StringBuilder();
    
    /** 
     * The dataSource is used only for access to a remote database (localhost and port), <br> 
     * on a Canreg server with the database server started (NetworkServerControl), <br>
     * and with multiple connections (pool).<br>
     * <br>
     * It must remain null on Canreg5 Server 
     * = use the unique connection dbConnection on the server.<br>
     * (an embedded connection must be unique and connot be handled in a pool,
     * and a remote connection cannot work if the NetworkServerControl is not started automatically)
     */
    private DataSource dbDatasource;

    /**
     * Constructor for local dao on Canreg server.
     * @param registryCode registry code
     * @param doc doc
     * @param holding true if holding db
     */
    public CanRegDAO(String registryCode, Document doc, boolean holding) {
        this(doc, registryCode, holding);
        setDBSystemDir();
        dbProperties = loadDBProperties();
        if (!dbExists()) {
            createDatabase();
            tableOfDictionariesFilled = false;
            tableOfPopulationDataSets = false;
        }
        // In this local mode, dbDatasource must be null
        dbDatasource = null;
    }
    
    /**
     * Constructor for a remote dao.
     * @param registryCode registry code
     * @param doc doc
     * @param databaseProperties database properties with user, password, bootPassword if required, pool properties...            
     */
    public CanRegDAO(String registryCode, Document doc, Properties databaseProperties) {
        this(doc, registryCode, false);
        dbProperties = databaseProperties;
        this.bootPassword = databaseProperties.getProperty(BOOT_PASSWORD);
        // Initialize the datasource
        initDataSource(databaseProperties);
    }
    
    /**
     * Constructor
     * @param doc doc
     * @param registryCode registry code
     * @param holding true if holding db
     */
    private CanRegDAO(Document doc, String registryCode, boolean holding) {
        this.doc = doc;

        this.registryCode = registryCode;

        globalToolBox = new GlobalToolBox(doc);

        variables = globalToolBox.getVariables();

        distributedDataSources = new LinkedHashMap<String, DistributedTableDataSource>();
        activeStatements = new LinkedHashMap<String, Statement>();
        dictionaryMap = buildDictionaryMap(doc);

        locksMap = new TreeMap<String, Set<Integer>>();

        debugOut(canreg.server.xml.Tools.getTextContent(
                new String[]{ns + "canreg", ns + "general", ns + "registry_name"}, doc));

        patientIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        patientRecordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();

        tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();

        sourceRecordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName();

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
        strMaxNumberOfSourcesPerTumourRecord = QueryGenerator.strMaxNumberOfSourcesPerTumourRecord(globalToolBox);
        strCountPatientByRegistryNumber = QueryGenerator.strCountPatientByRegistryNumber(patientIDVariableName);
        strCountPatientByRecordID = QueryGenerator.strCountPatientByRecordID(patientRecordIDVariableName);
        strCountTumourByTumourID = QueryGenerator.strCountTumourByTumourID(Globals.StandardVariableNames.TumourID.toString());
        strCountSourceByRecordID = QueryGenerator.strCountSourceByRecordID(Globals.StandardVariableNames.SourceRecordID.toString());
        /* We don't use tumour record ID...
         strGetHighestTumourRecordID = QueryGenerator.strGetHighestTumourRecordID(globalToolBox);
         */
    }

    /**
     * Wrap the unique connection.<br>
     * See DbConnectionWrapper
     * @param connection new connection
     * @return DbConnectionWrapper
     */
    private DbConnectionWrapper wrapUniqueConnection(Connection connection) {
        return new DbConnectionWrapper(connection);
    }

    /**
     * Open the unique connection (embedded) and sets dbConnection.
     * @param dbUrl database url
     * @throws SQLException SQLException
     */
    private void openUniqueConnection(String dbUrl) throws SQLException {
        dbConnection = wrapUniqueConnection(DriverManager.getConnection(dbUrl, dbProperties));
    }

    /** Get the connection from the dataSource
     *
     * @return a connection 
     * @throws SQLException SQLException
     */
    public Connection getDbConnection() throws SQLException {
        if(dbDatasource == null) {
            // Unique embedded connection on Canreg server
            return dbConnection;
        }
        // create a new connection for a remote access
        return dbDatasource.getConnection();
    }

    /**
     * Create a datasource with a connection pool FOR REMOTE ACCESS only.<br> 
     * The Connection pool allows handling simultaneous multiple connection from the server to the database
     * each connection is treated separately.
     *
     * @return a data source
     * @param databaseProperties database properties with user, password, bootPassword if required, pool properties... 
     */
    public DataSource initDataSource(Properties databaseProperties) {
        String dbUrl = getDatabaseUrl();
        dbDatasource = PoolConnection.DbDatasource(dbUrl, databaseProperties);
        LOGGER.log(Level.INFO, String.format("DataSource created\n %s", databaseProperties.toString()));
        return dbDatasource;
    }

    public synchronized Map<Integer, Dictionary> getDictionary() {
        ResultSet results;

        // rebuild dictionary map
        dictionaryMap = buildDictionaryMap(doc);
        
        try(Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            results = queryStatement.executeQuery("SELECT * FROM APP.DICTIONARY ORDER BY ID");
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return dictionaryMap;
    }

    public synchronized Map<String, Integer> getNameSexTables() {
        Map<String, Integer> nameSexMap = new LinkedHashMap<>();

        try(Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            String strGetNameSexRecords = "SELECT * FROM APP.NAMESEX ";
            ResultSet results = queryStatement.executeQuery(strGetNameSexRecords);
            while (results.next()) {
                int id = results.getInt(1);
                String name = results.getString(2);
                Integer sex = results.getInt(3);
                nameSexMap.put(name, sex);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return nameSexMap;
    }

    public synchronized String getSystemPropery(String lookup) {
        String value = null;
        String query = "SELECT * FROM " + Globals.SCHEMA_NAME + ".SYSTEM WHERE LOOKUP = '" + lookup + "'";
        
        try(Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            ResultSet results = queryStatement.executeQuery(query);
            while (results.next()) {
                value = results.getString(3);
                debugOut(query);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return value;
    }

    public synchronized void setSystemPropery(String lookup, String value) {
        try( Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            String query = "DELETE FROM " + Globals.SCHEMA_NAME + ".SYSTEM WHERE LOOKUP = '" + lookup + "'";
            queryStatement.execute(query);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        try( Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            String query = "INSERT INTO " + Globals.SCHEMA_NAME + ".SYSTEM (LOOKUP, VALUE) VALUES ('" + lookup + "', '" + value + "')";
            queryStatement.execute(query);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized int saveUser(User user) {
        int id = user.getID();

        if (id > -1) {
            // edit user
            id = editUser(user);
        } else {
            // save new user
            id = saveNewUser(user);
        }
        return id;
    }

    private synchronized int editUser(User user) {
        int id = user.getID();
        ResultSet results;
        try( Connection connection = getDbConnection();
            PreparedStatement stmtEditUser = connection.prepareStatement(QueryGenerator.strEditUser())) {
            stmtEditUser.clearParameters();
            stmtEditUser.setString(1, user.getUserName());
            stmtEditUser.setString(2, new String(user.getPassword()));
            stmtEditUser.setInt(3, user.getUserRightLevelIndex());
            stmtEditUser.setString(4, user.getEmail());
            stmtEditUser.setString(5, user.getRealName());
            stmtEditUser.setInt(6, id);

            int rowCount = stmtEditUser.executeUpdate();

            results = stmtEditUser.getResultSet();
            if (results != null) {
                if (results.next()) {
                    id = results.getInt(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return id;
    }

    private synchronized int saveNewUser(User user) {
        int id = -1;
        ResultSet results;
        try( Connection connection = getDbConnection();
            PreparedStatement stmtSaveNewUser = connection.prepareStatement(QueryGenerator.strSaveUser())) {
            stmtSaveNewUser.clearParameters();
            stmtSaveNewUser.setString(1, user.getUserName());
            stmtSaveNewUser.setString(2, new String(user.getPassword()));
            stmtSaveNewUser.setInt(3, user.getUserRightLevelIndex());
            stmtSaveNewUser.setString(4, user.getEmail());
            stmtSaveNewUser.setString(5, user.getRealName());
            int rowCount = stmtSaveNewUser.executeUpdate();
            results = stmtSaveNewUser.getResultSet();
            if (results != null && results.next()) {
                    id = results.getInt(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return id;
    }

    public synchronized Map<String, User> getUsers() {
        Map<String, User> usersMap = new LinkedHashMap<>();
        try( Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            ResultSet results = queryStatement.executeQuery("SELECT * FROM APP.USERS");
            while (results.next()) {
                User user = buildUserFromResultSet(results);
                usersMap.put(user.getUserName(), user);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return usersMap;
    }

    /**
     * Get a user by userName
     * @param userName the user name
     * @return the User if found, else null
     */
    public synchronized User getUserByUsername(String userName) {
        User user = null;
        try( Connection connection = getDbConnection();
            PreparedStatement stmtGetUser = connection.prepareStatement(STR_GET_USER_BY_USER_NAME)
            ) {
            stmtGetUser.clearParameters();
            stmtGetUser.setString(1, userName);
            ResultSet results = stmtGetUser.executeQuery();
            // Read only the first result
            if (results.next()) {
                user = buildUserFromResultSet(results);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return user;
    }

    private User buildUserFromResultSet(ResultSet results) throws SQLException {
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
        return user;
    }


    public synchronized Map<Integer, PopulationDataset> getPopulationDatasets() {
        Map<Integer, PopulationDataset> populationDatasetMap = new LinkedHashMap<>();
        
        try( Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            String strGetPopulationDatasets = "SELECT * FROM APP.PDSETS ";
            ResultSet results = queryStatement.executeQuery(strGetPopulationDatasets);
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
                populationDataset.setReferencePopulationID(worldPopulationPDSID);

                boolean worldPopulationBool = results.getInt(10) == 1;
                populationDataset.setReferencePopulationBool(worldPopulationBool);

                populationDatasetMap.put(pdsId, populationDataset);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }

        for (PopulationDataset popset : populationDatasetMap.values()) {
            if (!popset.isReferencePopulationBool()) {
                popset.setReferencePopulation(
                        populationDatasetMap.get(
                                popset.getReferencePopulationID()));
            }
        }

        try( Connection connection = getDbConnection();
            Statement queryStatement = connection.createStatement()) {
            ResultSet  results = queryStatement.executeQuery(STR_GET_POPULATION_DATASET_ENTRIES);
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }

        return populationDatasetMap;
    }

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
     * Initialise the query to fetch the patient, tumor and sources from the database.
     * This query is used to fill the table that contains all ths 
     *
     * @param filter filter fo the SQL query
     * @param tableName the table name
     * @param resultSetID the ID of the resultSet
     * @return a DistributedTableDescription
     * @throws SQLException SQLException
     * @throws UnknownTableException UnknownTableException
     * @throws DistributedTableDescriptionException DistributedTableDescriptionException
     */
    public DistributedTableDescription getDistributedTableDescriptionAndInitiateDatabaseQuery(
            DatabaseFilter filter, String tableName, String resultSetID)
            throws SQLException, UnknownTableException, DistributedTableDescriptionException {
        return getDistributedTableDescriptionAndInitiateDatabaseQuery(dbConnection, filter, tableName, resultSetID);
    }
    
    /**
     * Initialise the query to fetch the patient, tumor and sources from the database.
     * This query is used to fill the table that contains all ths 
     *
     * @param connection db connection, unique or created for the current call
     * @param filter filter fo the SQL query
     * @param tableName the table name
     * @param resultSetID the ID of the resultSet
     * @return a DistributedTableDescription
     * @throws SQLException SQLException
     * @throws UnknownTableException UnknownTableException
     * @throws DistributedTableDescriptionException DistributedTableDescriptionException
     */
    public synchronized DistributedTableDescription getDistributedTableDescriptionAndInitiateDatabaseQuery(
            Connection connection, DatabaseFilter filter, String tableName, String resultSetID)
        throws SQLException, UnknownTableException, DistributedTableDescriptionException {
        // distributedDataSources.remove(theUser);
        // ResultSet result;
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        // int rowCount = 0;
        DistributedTableDataSource dataSource;

        activeStatements.put(resultSetID, statement);

        if (null == filter.getQueryType()) {
            dataSource = initiateTableQuery(filter, statement, tableName);
        } else // Is this a person search query?
        {
            switch (filter.getQueryType()) {
                // Or a Frequency by year query?
                case PERSON_SEARCH:
                    dataSource = initiatePersonSearchQuery(filter, statement);
                    break;
                // Or a "regular" query
                case FREQUENCIES_BY_YEAR:
                    dataSource = initiateFrequenciesByYearQuery(filter, statement, tableName);
                    break;
                default:
                    dataSource = initiateTableQuery(filter, statement, tableName);
                    break;
            }
        }
        distributedDataSources.put(resultSetID, dataSource);
        activeStatements.remove(resultSetID);
        dataSource.getTableDescription().setResultSetID(resultSetID);
        return dataSource.getTableDescription();
        
    }

    /**
     * Release the resultSet if not null and always close the connection linked to the resultSetID.
     * The resultSetID is also removed from the distributedDataSources
     * @param resultSetID the id of the resultSet
     * @throws SQLException SQLException
     */
    public synchronized void releaseResultSet(String resultSetID) throws SQLException {
        DistributedTableDataSourceResultSetImpl dataSource = (DistributedTableDataSourceResultSetImpl) distributedDataSources.get(resultSetID);
        if (dataSource != null) {
            dataSource.releaseResultSet();
        }
        distributedDataSources.remove(resultSetID);
    }

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
            checkAndLockRecord(recordID, tableName);
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
            path = canreg.server.database.derby.Backup.backUpDatabase(dbConnection, Globals.CANREG_BACKUP_FOLDER + Globals.FILE_SEPARATOR + getRegistryCode());
            canreg.server.xml.Tools.writeXmlFile(doc, path + Globals.FILE_SEPARATOR + getRegistryCode() + ".xml");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return path;
    }


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
    
    private Properties loadDBProperties() {
        InputStream dbPropInputStream;
        dbPropInputStream = CanRegDAO.class.getResourceAsStream(Globals.DATABASE_CONFIG);
        Properties props = new Properties();
        try {
            props.load(dbPropInputStream);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return props;
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
                statement.execute(query);
            }
            LinkedList<String> patientIndexList = QueryGenerator.strCreateIndexTable("Patient", doc);
            for (String query : patientIndexList) {
                statement.execute(query);
            }
            LinkedList<String> sourceIndexList = QueryGenerator.strCreateIndexTable("Source", doc);
            for (String query : sourceIndexList) {
                statement.execute(query);
            }
            bCreatedTables = true;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
            openUniqueConnection(dbUrl);
            bCreated = createTables(dbConnection);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
            dbConnection.trulyClose(); // Close current connection.
            dbProperties.put(SHUT_DOWN, "true");
            openUniqueConnection(dbUrl);
        } catch (SQLException e) {
            if (e.getSQLState().equals("08006")) {
                shutdownSuccess = true; // single db.
            } else {
                return "shutdown failed";
            }
            ex = e;
        }
        if (!shutdownSuccess) {
            dbProperties.remove(SHUT_DOWN);
            LOGGER.log(Level.SEVERE, null, ex);
            return "shutdown failed";
        }
        try {
            dbProperties.remove(SHUT_DOWN);
            dbConnection.trulyClose(); // Close current connection.

            // check to see if there is a database already - rename it
            File databaseFolder = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + getRegistryCode());
            if (databaseFolder.exists()) {
                int i = 0;
                File folder2 = databaseFolder;
                while (folder2.exists()) {
                    i++;
                    folder2 = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + getRegistryCode() + i);
                }
                databaseFolder.renameTo(folder2);
                try {
                    canreg.common.Tools.fileCopy(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + getRegistryCode() + ".xml",
                            Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + getRegistryCode() + i + ".xml");
                } catch (IOException ex1) {
                    LOGGER.log(Level.SEVERE, null, ex1);
                }
            }
            dbProperties.put("restoreFrom", path + "/" + getRegistryCode());
            openUniqueConnection(dbUrl);
            bRestored = true;
        } catch (SQLException ex2) {
            LOGGER.log(Level.SEVERE, null, ex2);

        }
        dbProperties.remove("restoreFrom");
        // connect(); // Do not reconnect as this would be a potential security problem...
        if (bRestored) {
            try {
                // install the xml
                canreg.common.Tools.fileCopy(path + Globals.FILE_SEPARATOR + getRegistryCode() + ".xml",
                        Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + getRegistryCode() + ".xml");
            } catch (IOException ex1) {
                LOGGER.log(Level.SEVERE, null, ex1);
            }
            return "success";
        } else {
            return "failed";
        }
    }
    
    public boolean connect() throws SQLException, RemoteException {
        String dbUrl = getDatabaseUrl();
        try {
            openUniqueConnection(dbUrl);
            debugOut("Connection successful");
            LOGGER.log(Level.INFO, "JavaDB Version: {0}", dbConnection.getMetaData().getDatabaseProductVersion());
        } catch (SQLException ex) {
            // Important: throw an SQLException here, in cas the DB is encrypted
            throw ex;
        }


        try {
            /* We don't use tumour record ID...
             stmtGetHighestTumourRecordID = dbConnection.prepareStatement(strGetHighestTumourRecordID);
             */

            isConnected = dbConnection != null;

            // Consider moving this function...
            if (isConnected && !tableOfDictionariesFilled) {
                fillDictionariesTable();
            }

            if (isConnected && !tableOfPopulationDataSets) {
                fillPopulationDatasetTables();
            }

            // test
            debugOut("database connection OK\nVersion: " + dbConnection.getMetaData().getDatabaseProductVersion());
            debugOut("Next patient ID = " + getNextPatientID());
        } catch (SQLException ex) {
            debugOut("SQLerror... ");
            LOGGER.log(Level.SEVERE, null, ex);
            isConnected = false;
            // CanRegDAO now throws database mismatch exceptions if the database structure doesn't match the prepared queries.
            throw new RemoteException("Database description mismatch... \n" + ex.getLocalizedMessage());
        }
        return isConnected;
    }

    /**
     * Begin the transaction
     * from here all records will be save if there is no issue
     *
     * @throws SQLException SQLException
     */
    public void openTransaction() throws SQLException {
        if (dbConnection == null) {
            dbConnection = (DbConnectionWrapper) DriverManager.getConnection(getDatabaseUrl(), dbProperties);
        }
        dbConnection.setAutoCommit(false);
    }

    /**
     *  If there is an exception all record will be rollback
     *  
     * @throws SQLException SQLException
     */
    public void rollbackTransaction() throws SQLException {
        if (dbConnection != null) {
            dbConnection.rollback();
            dbConnection.setAutoCommit(true);
        }
    }

    /**
     * If there no exception all record will be saved in the database
     * 
     * @throws SQLException SQLException
     */
    public void commitTransaction() throws SQLException {
        if (dbConnection != null) {
            dbConnection.commit();
            dbConnection.setAutoCommit(true);
        }
    }

    public boolean connectWithBootPassword(char[] passwordArray) throws RemoteException, SQLException {
        String password = new String(passwordArray);
        dbProperties.setProperty(BOOT_PASSWORD, password);
        boolean success = connect();
        if(success) {
            this.bootPassword = password;
        }
        dbProperties.remove(BOOT_PASSWORD);
        return success;
    }

    public boolean encryptDatabase(char[] newPasswordArray, char[] oldPasswordArray,
            String encryptionAlgorithm, String encryptionKeyLength)
            throws RemoteException, SQLException {
        // To use the AES algorithm with a key length of 192 or 256, you must use unrestricted policy jar files for your JRE. You can obtain these files from your Java provider. They might have a name like "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files." If you specify a non-default key length using the default policy jar files, a Java exception occurs.
        // https://db.apache.org/derby/docs/10.9/devguide/cdevcsecure67151.html

        String defaultFeedbackMode = "CBC";
        String defaultPadding = "NoPadding";
        boolean success;
        if (oldPasswordArray.length != 0 && newPasswordArray.length > 0) {
            // already encrypted? Change password
            // http://db.apache.org/derby/docs/10.4/devguide/cdevcsecure55054.html
            String oldPassword = new String(oldPasswordArray);
            String newPassword = new String(newPasswordArray);
            String command = "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
                    + "\'bootPassword\', \'" + oldPassword + " , " + newPassword + "\')";
            try {
                Statement statement = dbConnection.createStatement(); 
                statement.execute(command);
                return true;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                return false;
            }
        } else if (newPasswordArray.length == 0) {
            // remove password
            String oldPassword = new String(oldPasswordArray);
            dbProperties.setProperty(BOOT_PASSWORD, oldPassword);
            try {
                disconnect();
                // side effect of removing password is that we have to upgrade the database version
                openUniqueConnection(getDatabaseUrl() 
                        + ";bootPassword= " + oldPassword + ";upgrade=true");
                disconnect();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                return false;
            }
            dbProperties.setProperty("decryptDatabase", "true");
            Connection conn = DriverManager.getConnection(getDatabaseUrl(), dbProperties);
            conn.commit();
            bootPassword = null;
            
        } else {
            // Encrypt database
            // http://db.apache.org/derby/docs/10.4/devguide/cdevcsecure866716.html
            try {
                disconnect();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            dbProperties.setProperty("dataEncryption", "true");
            dbProperties.setProperty("encryptionKeyLength", encryptionKeyLength);
            dbProperties.setProperty("encryptionAlgorithm", encryptionAlgorithm + "/" + defaultFeedbackMode + "/" + defaultPadding);
            String password = new String(newPasswordArray);
            dbProperties.setProperty(BOOT_PASSWORD, password);
        }
        dbProperties.remove(BOOT_PASSWORD);
        dbProperties.remove("newBootPassword");

        if (newPasswordArray.length == 0) {
            success = connect();
        } else {
            success = connectWithBootPassword(newPasswordArray);
        }
        dbProperties.remove("dataEncryption");
        return success;
    }

    public boolean disconnect() throws SQLException {
        boolean shutdownSuccess = false;
        if (isConnected) {
            String dbUrl = getDatabaseUrl();
            try{
                dbConnection.trulyClose(); // Close current connection.
                dbProperties.put(SHUT_DOWN, "true");
                openUniqueConnection(dbUrl);
            } catch (SQLException e) {
                if (e.getSQLState().equals("08006")) {
                    shutdownSuccess = true; // single db.
                } else {
                    LOGGER.log(Level.SEVERE, null, e);
                    throw e;
                }
            }
            dbProperties.remove(SHUT_DOWN);
        }
        return shutdownSuccess;
    }

    /**
     *
     * @return String location of the database
     */
    public String getDatabaseLocation() {
        return System.getProperty("derby.system.home") + "/" + getRegistryCode();
    }

    /**
     * Return the database url with the bootPassword, null or not, stored in this object.
     * @return database url
     */
    public String getDatabaseUrl() {
        return getDatabaseUrl(this.bootPassword);
    }

    public String getDatabaseUrl(String bootPassword) {
        String dbUrl = dbProperties.getProperty("derby.url") + getRegistryCode();
        if(bootPassword != null) {
            dbUrl = dbUrl + ";bootPassword="+bootPassword;
        }
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

    public synchronized int savePatient(Patient patient)
            throws SQLException {
        
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

        try( Connection connection = getDbConnection();
            PreparedStatement stmtSaveNewPatient = connection.prepareStatement(strSavePatient, Statement.RETURN_GENERATED_KEYS);
        ) {
            return saveRecord(Globals.PATIENT_TABLE_NAME, patient, stmtSaveNewPatient);
        }
    }

    public synchronized int saveTumour(Tumour tumour)
            throws SQLException, RecordLockedException {
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
        try(Connection connection = getDbConnection();
             PreparedStatement stmtSaveNewTumour = connection.prepareStatement(strSaveTumour, Statement.RETURN_GENERATED_KEYS)) {
            int id = saveRecord(Globals.TUMOUR_TABLE_NAME, tumour, stmtSaveNewTumour);

            Set<Source> sources = tumour.getSources();
            // delete old sources ## DEPRECATED
//        try {
//            deleteSources(tumourID);
//        } catch (DistributedTableDescriptionException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        } catch (UnknownTableException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
            // save each of the source records
            saveSources(tumourID, sources);

            return id;
        }
    }

    public synchronized int saveSource(Source source) throws SQLException, RecordLockedException {
        String sourceIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(
            Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName();
        Object sourceRecordID = source.getVariable(canreg.common.Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
        int id = -1;

        String sourceID = source.getVariableAsString(sourceIDVariableName);
        if (sourceID != null && sourceID.contains("@H")) {
            sourceID = "";
        }
        try (Connection connection = getDbConnection();
            PreparedStatement stmtSaveNewSource = connection.prepareStatement(strSaveSource, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement stmtEditSource = connection.prepareStatement(strEditSource, Statement.RETURN_GENERATED_KEYS)) {
            if (sourceID == null || sourceID.trim().length() == 0) {
                String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                    Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
                String tumourID = (String) source.getVariable(tumourIDVariableName);
                sourceID = getNextSourceID(tumourID);
                source.setVariable(sourceIDVariableName, sourceID);
                id = saveRecord(Globals.SOURCE_TABLE_NAME, source, stmtSaveNewSource);
            } else if (sourceRecordID == null || sourceRecordID.toString().trim().length() == 0) {
                id = saveRecord(Globals.SOURCE_TABLE_NAME, source, stmtSaveNewSource);
            } else {
                boolean success = editRecord(Globals.SOURCE_TABLE_NAME, source, stmtEditSource,
                    canreg.common.Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                if (success) {
                    sourceRecordID = source.getVariable(canreg.common.Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                    id = (int) sourceRecordID;
                }
            }
        }
        return id;
    }

    public synchronized int saveDictionary(Dictionary dictionary) {
        int id = -1;
        try(Connection connection = getDbConnection();
            PreparedStatement stmtSaveNewDictionary = connection.prepareStatement(strSaveDictionary,
                Statement.RETURN_GENERATED_KEYS)) {

            stmtSaveNewDictionary.clearParameters();

            stmtSaveNewDictionary.setInt(1, dictionary.getDictionaryID());
            stmtSaveNewDictionary.setString(2, dictionary.getName());
            stmtSaveNewDictionary.setString(3, dictionary.getFont());
            stmtSaveNewDictionary.setString(4, dictionary.getType());
            stmtSaveNewDictionary.setInt(5, dictionary.getCodeLength());
            stmtSaveNewDictionary.setInt(6, dictionary.getCategoryDescriptionLength());
            stmtSaveNewDictionary.setInt(7, dictionary.getFullDictionaryCodeLength());
            stmtSaveNewDictionary.setInt(8, dictionary.getFullDictionaryDescriptionLength());

            int rowCount = stmtSaveNewDictionary.executeUpdate();
            ResultSet results = stmtSaveNewDictionary.getResultSet();
            if(results == null) {
                results = stmtSaveNewDictionary.getGeneratedKeys();
            }
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    public synchronized int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        int id = -1;
        try(Connection connection =getDbConnection();
             PreparedStatement stmtSaveNewDictionaryEntry = connection.prepareStatement(strSaveDictionaryEntry, 
                 Statement.RETURN_GENERATED_KEYS)) {
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
            ResultSet results = stmtSaveNewDictionaryEntry.getResultSet();
            if(results == null) {
                results = stmtSaveNewDictionaryEntry.getGeneratedKeys();
            }
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    public synchronized int saveNewPopulationDataset(PopulationDataset populationDataSet) {
        Map<Integer, PopulationDataset> populationDataSets;
        populationDataSets = getPopulationDatasets();

        int dataSetID = 0;
        while (populationDataSets.get(dataSetID) != null) {
            dataSetID++;
        }
        populationDataSet.setPopulationDatasetID(dataSetID);
        savePopulationDataset(populationDataSet);
        return dataSetID;
    }

    /**
     * Update an existing population dataset.
     *
     * @param populationDataSet populationDataSet with populationDatasetID already set
     * @return -1 if does not exist else return the id in input
     */
    public synchronized int updatePopulationDataset(PopulationDataset populationDataSet) throws SQLException {
        Map<Integer, PopulationDataset> populationDataSets;
        populationDataSets = getPopulationDatasets();

        if (populationDataSets.get(populationDataSet.getPopulationDatasetID()) == null) {
            return -1;
        }
        deletePopulationDataSet(populationDataSet.getPopulationDatasetID());
        savePopulationDataset(populationDataSet);

        return populationDataSet.getPopulationDatasetID();
    }

    private synchronized int savePopulationDataset(PopulationDataset populationDataSet) {
        try (Connection connection = getDbConnection(); PreparedStatement stmtSaveNewPopoulationDataset = connection.prepareStatement(strSavePopoulationDataset, Statement.RETURN_GENERATED_KEYS)) {
            stmtSaveNewPopoulationDataset.clearParameters();

            stmtSaveNewPopoulationDataset.setInt(1, populationDataSet.getPopulationDatasetID());
            stmtSaveNewPopoulationDataset.setString(2, populationDataSet.getPopulationDatasetName().substring(0, Math.min(Globals.PDS_DATABASE_NAME_LENGTH, populationDataSet.getPopulationDatasetName().length())));
            stmtSaveNewPopoulationDataset.setString(3, populationDataSet.getFilter());
            stmtSaveNewPopoulationDataset.setString(4, populationDataSet.getDate());
            stmtSaveNewPopoulationDataset.setString(5, populationDataSet.getSource());
            stmtSaveNewPopoulationDataset.setString(6, populationDataSet.getAgeGroupStructure().getConstructor());
            stmtSaveNewPopoulationDataset.setString(7, populationDataSet.getDescription());
            stmtSaveNewPopoulationDataset.setInt(8, populationDataSet.getReferencePopulationID());
            if (populationDataSet.isReferencePopulationBool()) {
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return populationDataSet.getPopulationDatasetID();

    }

    public synchronized int savePopoulationDatasetsEntry(PopulationDatasetsEntry populationDatasetsEntry) {
        int id = -1;
        try( Connection connection = getDbConnection();
            PreparedStatement stmtSaveNewPopoulationDatasetsEntry =
                connection.prepareStatement(strSavePopoulationDatasetsEntry, Statement.RETURN_GENERATED_KEYS))
        {
            stmtSaveNewPopoulationDatasetsEntry.clearParameters();

            stmtSaveNewPopoulationDatasetsEntry.setInt(1, populationDatasetsEntry.getPopulationDatasetID());
            stmtSaveNewPopoulationDatasetsEntry.setInt(2, populationDatasetsEntry.getAgeGroup());
            stmtSaveNewPopoulationDatasetsEntry.setInt(3, populationDatasetsEntry.getSex());
            stmtSaveNewPopoulationDatasetsEntry.setInt(4, populationDatasetsEntry.getCount());

            int rowCount = stmtSaveNewPopoulationDatasetsEntry.executeUpdate();
            ResultSet results = stmtSaveNewPopoulationDatasetsEntry.getResultSet();
            if(results == null) {
                results = stmtSaveNewPopoulationDatasetsEntry.getGeneratedKeys();
            }
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return id;
    }

    public synchronized int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace) {
        int id = -1;
        if (replace) {
            try(Connection connection =getDbConnection();
                PreparedStatement stmtDeleteNameSexRecord = connection.prepareStatement(strDeleteNameSexRecord)) {
                stmtDeleteNameSexRecord.clearParameters();
                stmtDeleteNameSexRecord.setString(1, nameSexRecord.getName());
                stmtDeleteNameSexRecord.executeUpdate();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        
        try(
            Connection connection =getDbConnection();
            PreparedStatement stmtSaveNewNameSexRecord = connection.prepareStatement(strSaveNameSexRecord,
                Statement.RETURN_GENERATED_KEYS))
        {
            stmtSaveNewNameSexRecord.clearParameters();

            stmtSaveNewNameSexRecord.setString(1, nameSexRecord.getName());
            stmtSaveNewNameSexRecord.setInt(2, nameSexRecord.getSex());

            int rowCount = stmtSaveNewNameSexRecord.executeUpdate();
            ResultSet results = stmtSaveNewNameSexRecord.getGeneratedKeys();
            if (results.next()) {
                id = results.getInt(1);
            }

        } catch (java.sql.SQLIntegrityConstraintViolationException sqle) {
             LOGGER.log(Level.SEVERE,String.format(" Error : an integrity constraint has been violated for nameSexRecord : %s",nameSexRecord.getName()), sqle);
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, String.format("SQL error : for nameSexRecord : %s",nameSexRecord.getName()), sqle);
        }
        return id;
    }

    public synchronized boolean clearNameSexTable() {
        boolean success = false;
        try(Connection connection =getDbConnection();
            PreparedStatement stmtClearNameSexTable = connection.prepareStatement(STR_CLEAR_NAME_SEX_TABLE)) {
            stmtClearNameSexTable.clearParameters();

            stmtClearNameSexTable.executeUpdate();
            success = true;

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    public synchronized boolean deleteDictionaryEntries(int dictionaryID) {
        boolean success = false;
        try(Connection connection = getDbConnection();
            PreparedStatement stmtDeleteDictionaryEntries = connection.prepareStatement(STR_DELETE_DICTIONARY_ENTRIES)) {
            stmtDeleteDictionaryEntries.clearParameters();
            stmtDeleteDictionaryEntries.setInt(1, dictionaryID);

            stmtDeleteDictionaryEntries.executeUpdate();
            success = true;

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    public synchronized boolean deletePatientRecord(int patientRecordID) throws RecordLockedException, SQLException {
        boolean success = false;
        if (isRecordLocked(patientRecordID, Globals.PATIENT_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try(Connection connection = getDbConnection();
            PreparedStatement  stmtDeletePatientRecord = connection.prepareStatement(STR_DELETE_PATIENT_RECORD)) {
            stmtDeletePatientRecord.clearParameters();
            stmtDeletePatientRecord.setInt(1, patientRecordID);
            stmtDeletePatientRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    public synchronized boolean deleteTumourRecord(int tumourRecordID) throws RecordLockedException {
        boolean success = false;
        if (isRecordLocked(tumourRecordID, Globals.TUMOUR_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try(Connection connection = getDbConnection();
            PreparedStatement stmtDeleteTumourRecord = connection.prepareStatement(STR_DELETE_TUMOUR_RECORD)) {
            stmtDeleteTumourRecord.clearParameters();
            stmtDeleteTumourRecord.setInt(1, tumourRecordID);
            stmtDeleteTumourRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    public synchronized boolean deleteSourceRecord(int sourceRecordID) throws RecordLockedException {
        boolean success = false;
        if (isRecordLocked(sourceRecordID, Globals.SOURCE_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try(Connection connection = getDbConnection();
            PreparedStatement  stmtDeleteSourceRecord = connection.prepareStatement(STR_DELETE_SOURCE_RECORD)) {
            stmtDeleteSourceRecord.clearParameters();
            stmtDeleteSourceRecord.setInt(1, sourceRecordID);
            stmtDeleteSourceRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    public synchronized boolean deleteRecord(int recordID, String tableName) throws RecordLockedException, SQLException {
        boolean success = false;
        if (isRecordLocked(recordID, tableName)) {
            throw new RecordLockedException();
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            success = deletePatientRecord(recordID);
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            success = deleteTumourRecord(recordID);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            success = deleteSourceRecord(recordID);
        } else {
            String idString = "ID";
            try( Connection connection = getDbConnection();
                Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM " + Globals.SCHEMA_NAME + "." + tableName + " WHERE " + idString + " = " + recordID);
                success = true;
            }
        }
        return success;
    }

    public synchronized boolean deletePopulationDataSet(int id) {
        boolean success = false;
        // if (isRecordLocked(id, Globals.POPULATION_DATASET_TABLE_NAME)) {
        //     throw new RecordLockedException();
        // }
        try(Connection connection = getDbConnection();
            PreparedStatement stmtDeletePopoulationDataset = connection.prepareStatement(STR_DELETE_POPULATION_DATASET);
            PreparedStatement stmtDeletePopoulationDatasetEntries =
                connection.prepareStatement(STR_DELETE_POPULATION_DATASET_ENTRIES))
        {
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return success;
    }

    /**
     *
     * @param patient
     * @param fromHoldingToProduction
     * @return
     * @throws RecordLockedException
     * @throws java.sql.SQLException
     */
    public synchronized boolean editPatient(Patient patient, boolean fromHoldingToProduction) 
            throws RecordLockedException, SQLException {
        try( Connection connection = getDbConnection();
            PreparedStatement stmtEditPatient = connection.prepareStatement(strEditPatient,
                Statement.RETURN_GENERATED_KEYS))
        {
            if(fromHoldingToProduction)
                return editRecord(Globals.PATIENT_TABLE_NAME, patient, stmtEditPatient,
                    Globals.StandardVariableNames.PatientRecordID.toString());
            else
                return editRecord(Globals.PATIENT_TABLE_NAME, patient, stmtEditPatient,
                    Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
        }
    }

    /**
     *
     * @param tumour
     * @param fromHoldingToProduction
     * @return
     * @throws RecordLockedException
     * @throws java.sql.SQLException
     */
    public synchronized boolean editTumour(Tumour tumour, boolean fromHoldingToProduction)
            throws RecordLockedException, SQLException {
        
        try(Connection connection = getDbConnection();
            PreparedStatement stmtEditTumour = connection.prepareStatement(strEditTumour,
                Statement.RETURN_GENERATED_KEYS))
            {
            if(fromHoldingToProduction)
                return editRecord(Globals.TUMOUR_TABLE_NAME, tumour, stmtEditTumour,
                    Globals.StandardVariableNames.TumourID.toString());
            else
                return editRecord(Globals.TUMOUR_TABLE_NAME, tumour, stmtEditTumour,
                    Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
        }
    }

    /**
     *
     * @param source
     * @param fromHoldingToProduction
     * @return
     * @throws RecordLockedException
     */
    public synchronized boolean editSource(Source source, boolean fromHoldingToProduction)
            throws RecordLockedException, SQLException {
        try(Connection connection = getDbConnection();  
            PreparedStatement stmtEditSource =
                connection.prepareStatement(strEditSource, Statement.RETURN_GENERATED_KEYS)) {
            if(fromHoldingToProduction)
                return editRecord(Globals.SOURCE_TABLE_NAME, source, stmtEditSource,
                    Globals.StandardVariableNames.SourceRecordID.toString());
            else
                return editRecord(Globals.SOURCE_TABLE_NAME, source, stmtEditSource,
                    Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
        }
    }

    /**
     *
     * @param tableName
     * @param record
     * @param stmtEditRecord
     * @return
     * @throws RecordLockedException
     */
    private synchronized boolean editRecord(String tableName, DatabaseRecord record, 
                                            PreparedStatement stmtEditRecord, String idRecordVariable) 
            throws RecordLockedException, SQLException, SecurityException {
        boolean bEdited = false;
        try {
            stmtEditRecord.clearParameters();

            int variableNumber = 0;

            for (DatabaseVariablesListElement variable : variables) {
//                if(variable.getDatabaseVariableName().equalsIgnoreCase(idRecordVariable))
//                    continue;
                
                String tableNameDB = variable.getDatabaseTableName();
                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    variableNumber++;
                    String variableType = variable.getVariableType();
                    int variableLength = variable.getVariableLength();
                    Object obj = record.getVariable(variable.getDatabaseVariableName());
                    if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME) ||
                        variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME) ||
                        variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME) ||
                        variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME) ||
                        variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)) {
                        if (obj != null) {
                            try {
                                String strObj = obj.toString();
                                if (strObj.length() > 0) {
                                    if (variableLength > 0 && strObj.length() > variableLength) {
                                        strObj = strObj.substring(0, variableLength);
                                    }
                                    stmtEditRecord.setString(variableNumber, strObj);
                                } else {
                                    stmtEditRecord.setString(variableNumber, "");
                                }
                            } catch (java.lang.ClassCastException cce) {
                                debugOut("Cast to String Error. Type:" + variableType + ", Value: " + obj + ", Variable Number: " + variableNumber);
                                throw cce;
                            } 
                        } else {
                            stmtEditRecord.setString(variableNumber, "");
                        }
                    } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                        if (obj != null) {
                            try {
                                Integer intObj = (Integer) obj;
                                stmtEditRecord.setInt(variableNumber, intObj);
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
            
            String idString = null;
            if (record instanceof Patient) {
                idString = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (record instanceof Tumour) {
                idString = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (record instanceof Source) {
                idString = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
            }
            
            Integer idInt = null;
            if( ! idString.equalsIgnoreCase(idRecordVariable)) {
                if(record instanceof Patient) {
                    String patientIDVariableName = globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                            .getDatabaseVariableName();
                    String patientRecordID = record.getVariableAsString(patientIDVariableName);
                    Patient patient = getPatientByPatientRecordID(patientRecordID);
                    idInt = (Integer) patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                } else if(record instanceof Tumour) {
                    String tumourIDVariableName = globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString())
                            .getDatabaseVariableName();
                    String tumourID = record.getVariableAsString(tumourIDVariableName);
                    Tumour tumour = getTumourByTumourID(tumourID);
                    idInt = (Integer) tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                } else if(record instanceof Source) {
                    String sourceIDVariableName = globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString())
                            .getDatabaseVariableName();
                    String sourceID = record.getVariableAsString(sourceIDVariableName);
                    Source source = getSourceBySourceID(sourceID);
                    idInt = (Integer) source.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                }                 
            } else {
                idInt = (Integer) record.getVariable(idString);
            }
            
            if (isRecordLocked(idInt, tableName)) 
                throw new RecordLockedException();
                
            stmtEditRecord.setInt(variableNumber + 1, idInt);            
            
            int rowCount = stmtEditRecord.executeUpdate();  

            // If this is a tumour we save the sources...
            if (record instanceof Tumour) {
                Tumour tumour = (Tumour) record;
                String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
                Object tumourID = tumour.getVariable(tumourIDVariableName);
                Set<Source> sources = tumour.getSources();
                // delete old sources
                // FIX ME : delete source doesn't work, it deletes more than what is requested
                /*
                try {
                    deleteSources(tumourID);
                } catch (DistributedTableDescriptionException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } catch (UnknownTableException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                */
                // save each of the source records
                saveSources(tumourID, sources);
            }

            bEdited = true;

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
            throw sqle;
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        return bEdited;
    }

    private synchronized boolean fillPopulationDatasetTables() {
        PopulationDataset pds = new PopulationDataset();
        pds.setReferencePopulationBool(true);
        pds.setPopulationDatasetName("World Standard Population");
        pds.setSource("SEGI 1960 / World Health Organization");
        pds.setDescription(PAPER_31_PDF);
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));

        int i = 0;
        for (int ageGroupWeight : Globals.standardWorldPopulationWeights) {
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 1, ageGroupWeight));
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 2, ageGroupWeight));
            i++;
        }
        saveNewPopulationDataset(pds);

        pds = new PopulationDataset();
        pds.setReferencePopulationBool(true);
        pds.setPopulationDatasetName("European Standard Population");
        pds.setSource("World Health Organization");
        pds.setDescription(PAPER_31_PDF);
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));

        i = 0;
        for (int ageGroupWeight : Globals.standardEuropeanPopulationWeights) {
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 1, ageGroupWeight));
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 2, ageGroupWeight));
            i++;
        }
        saveNewPopulationDataset(pds);

        pds = new PopulationDataset();
        pds.setReferencePopulationBool(true);
        pds.setPopulationDatasetName("WHO Standard Population");
        pds.setSource("World Health Organization");
        pds.setDescription(PAPER_31_PDF);
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Check status
        try {
            fillDictionary(Globals.StandardVariableNames.CheckStatus, Globals.DEFAULT_DICTIONARIES_FOLDER + "/checkstatus.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Person search
        try {
            fillDictionary(Globals.StandardVariableNames.PersonSearch, Globals.DEFAULT_DICTIONARIES_FOLDER + "/mpstatus.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Topography
        try {
            fillDictionary(Globals.StandardVariableNames.Topography, Globals.DEFAULT_DICTIONARIES_FOLDER + "/topography.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Morphology
        try {
            fillDictionary(Globals.StandardVariableNames.Morphology, Globals.DEFAULT_DICTIONARIES_FOLDER + "/morphology4.tsv");
            // TODO -- autofill five character morphologies as well...
            // fillDictionary(Globals.StandardVariableNames.Morphology, Globals.DEFAULT_DICTIONARIES_FOLDER + "/morphology5.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Behaviour
        try {
            fillDictionary(Globals.StandardVariableNames.Behaviour, Globals.DEFAULT_DICTIONARIES_FOLDER + "/behaviour.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Behaviour
        try {
            fillDictionary(Globals.StandardVariableNames.Grade, Globals.DEFAULT_DICTIONARIES_FOLDER + "/grade.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Basis
        try {
            fillDictionary(Globals.StandardVariableNames.BasisDiagnosis, Globals.DEFAULT_DICTIONARIES_FOLDER + "/basis.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        // Sex
        try {
            fillDictionary(Globals.StandardVariableNames.Sex, Globals.DEFAULT_DICTIONARIES_FOLDER + "/sex.tsv");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        bFilled = true;

        return bFilled;
    }

    public synchronized static Map<Integer, Dictionary> buildDictionaryMap(Document doc) {

        Map<Integer, Dictionary> dictionariesMap = new LinkedHashMap<>();

        // NodeList dictionaries = variablesElement.getElementsByTagName(Globals.NAMESPACE + "dictionary");
        DatabaseDictionaryListElement[] dictionaries = canreg.common.Tools.getDictionaryListElements(doc, Globals.NAMESPACE);

        // Go through all the variable definitions
        for (DatabaseDictionaryListElement ddle : dictionaries) {
            Dictionary dic = new Dictionary();

            // Create dictionary
            dic.setDictionaryID(ddle.getDictionaryID());
            dic.setName(ddle.getName());
            dic.setFont(ddle.getFont());
            dic.setType(ddle.getType());
            dic.setCodeLength(ddle.getCodeLength());
            dic.setCategoryDescriptionLength(ddle.getCategoryDescriptionLength());
            dic.setFullDictionaryCodeLength(ddle.getFullDictionaryCodeLength());
            dic.setFullDictionaryDescriptionLength(ddle.getFullDictionaryDescriptionLength()); // Not implemented
            // dic.setLocked(ddle.isLocked());
            dic.setAllowCodesOfDifferentLength(ddle.isAllowCodesOfDifferentLength());

            dictionariesMap.put(ddle.getDictionaryID(), dic);
        }
        return dictionariesMap;
    }


    private synchronized Patient getPatient(int recordID, boolean lock) throws RecordLockedException {
        Patient record = null;
        ResultSetMetaData metadata;
        // we are allowed to read a record that is locked...
        if (lock && checkAndLockRecord(recordID, Globals.PATIENT_TABLE_NAME)) {
            throw new RecordLockedException();
        } 
        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetPatient = connection.prepareStatement(STR_GET_PATIENT)) {
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }

        return record;
    }

    /**
     * Count the number of Patient records for the patientID of the Patient object
     * (usually Registry Number = "regno" column).
     * @param patient the patient with the PatientID to be checked
     * @return the number of patients, 0 if not found of if patientID null or blank in Patient
     * @throws SQLException exception while runnning the query
     */
    public int countPatientByPatientID(Patient patient) throws SQLException {
        DatabaseVariablesListElement patientIDVariable =
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.PatientID.toString());
        String patientID = (String) patient.getVariable(
                Tools.toLowerCaseStandardized(patientIDVariable.getDatabaseVariableName()));
        if (patientID != null && !patientID.trim().isEmpty()) {
            return countPatientByPatientID(patientID);
        }
        return 0;
    }

    /**
     * Count the number of Patient records for a patientID (usually Registry Number = "regno" column)
     * @param patientID the patient ID
     * @return the number of patients
     * @throws SQLException exception while runnning the query
     */
    public int countPatientByPatientID(String patientID) throws SQLException {
        int result = 0;
        try(Connection connection = getDbConnection();
            PreparedStatement statement = connection.prepareStatement(strCountPatientByRegistryNumber)) {
            statement.clearParameters();
            statement.setString(1, patientID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
            throw sqle;
        }
        return result;
    }
    /**
     * Count the number of Patient records for the patientRecordID of the Patient object
     * @param patient the patient with the patientRecordID to be checked
     * @return the number of patients, 0 if not found of if patientRecordID null or blank in Patient
     * @throws SQLException exception while runnning the query
     */
    public int countPatientByPatientRecordID(Patient patient) throws SQLException {
        DatabaseVariablesListElement patientRecordIDVariable =
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.PatientRecordID.toString());
        String patientRecordID = (String) patient.getVariable(
                Tools.toLowerCaseStandardized(patientRecordIDVariable.getDatabaseVariableName()));
        if (patientRecordID != null && !patientRecordID.trim().isEmpty()) {
            return countPatientByPatientRecordID(patientRecordID);
        }
        return 0;
    }

    /**
     * Count the number of Patient records for a patientRecordID
     * @param patientRecordID the patient RecordID
     * @return the number of patients
     * @throws SQLException exception while runnning the query
     */
    public int countPatientByPatientRecordID(String patientRecordID) throws SQLException {
        int result = 0;
        try(Connection connection = getDbConnection();
            PreparedStatement statement = connection.prepareStatement(strCountPatientByRecordID)) {
            statement.clearParameters();
            statement.setString(1, patientRecordID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
            throw sqle;
        }
        return result;
    }

    /**
     * Count the number of Tumour records for the tumourID of the tumour object
     * @param tumour the tumour with the tumourID to be checked
     * @return the number of tumours, 0 if not found of if tumourID null or blank in Tumour
     * @throws SQLException exception while runnning the query
     */
    public int countTumourByTumourID(Tumour tumour) throws SQLException {
        DatabaseVariablesListElement tumourIDVariable =
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.TumourID.toString());
        String tumourID = (String) tumour.getVariable(
                Tools.toLowerCaseStandardized(tumourIDVariable.getDatabaseVariableName()));
        if (tumourID != null && !tumourID.trim().isEmpty()) {
            return countTumourByTumourID(tumourID);
        }
        return 0;
    }

    /**
     * Count the number of Tumour records for a tumourID
     * @param tumourID the tumour  ID
     * @return the number of tumours
     * @throws SQLException exception while runnning the query
     */
    public int countTumourByTumourID(String tumourID) throws SQLException {
        int result = 0;
        try(Connection connection = getDbConnection();
            PreparedStatement statement = connection.prepareStatement(strCountTumourByTumourID)) {
            statement.clearParameters();
            statement.setString(1, tumourID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
            throw sqle;
        }
        return result;
    }

    /**
     * Count the number of Source records for the sourceRecordID of the Source object
     * @param source the source with the sourceRecordID to be checked
     * @return the number of sources, 0 if not found of if sourceRecordID null or blank in Source
     * @throws SQLException exception while runnning the query
     */
    public int countSourceBySourceRecordID(Source source) throws SQLException {
        DatabaseVariablesListElement sourceRecordIDVariable =
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.SourceRecordID.toString());
        String sourceRecordID = (String) source.getVariable(
                Tools.toLowerCaseStandardized(sourceRecordIDVariable.getDatabaseVariableName()));
        if (sourceRecordID != null && !sourceRecordID.trim().isEmpty()) {
            return countSourceBySourceRecordID(sourceRecordID);
        }
        return 0;
    }

    /**
     * Count the number of Source records for a sourceRecordID
     * @param sourceRecordID the patient RecordID
     * @return the number of sources
     * @throws SQLException exception while runnning the query
     */
    public int countSourceBySourceRecordID(String sourceRecordID) throws SQLException {
        int result = 0;
        try(Connection connection = getDbConnection();
            PreparedStatement statement = connection.prepareStatement(strCountSourceByRecordID)) {
            statement.clearParameters();
            statement.setString(1, sourceRecordID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
            throw sqle;
        }
        return result;
    }


    public synchronized Patient getPatientByPatientRecordID(String patientRecordID) {

        Patient record = null;
        ResultSetMetaData metadata;
        try(Connection connection = getDbConnection() ;
            PreparedStatement stmtGetPatientByPatientRecordID =
                connection.prepareStatement(STR_GET_PATIENT_BY_PATIENT_RECORD_ID))
        {
            stmtGetPatientByPatientRecordID.clearParameters();
            stmtGetPatientByPatientRecordID.setString(1, patientRecordID);
            ResultSet result = stmtGetPatientByPatientRecordID.executeQuery();
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }        
        
        return record;
    }

    private synchronized Tumour getTumour(int recordID, boolean lock) throws RecordLockedException {
        Tumour record = null;
        ResultSetMetaData metadata;
        if (lock && checkAndLockRecord(recordID, Globals.TUMOUR_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetTumour = connection.prepareStatement(STR_GET_TUMOUR)) {
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
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (UnknownTableException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            record.setSources(sources);
            result = null;

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return record;
    }
    
    public synchronized Tumour getTumourByTumourID(String tumourID) throws RecordLockedException {
        Tumour record = null;
        ResultSetMetaData metadata;

        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetTumourByTumourID = connection.prepareStatement(STR_GET_TUMOUR_BY_TUMOUR_ID))
        {
            stmtGetTumourByTumourID.clearParameters();
            stmtGetTumourByTumourID.setString(1, tumourID);
            ResultSet result = stmtGetTumourByTumourID.executeQuery();
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

            Set<Source> sources = null;
            try {
                // We don't lock the sources...
                sources = getSourcesByTumourID(tumourID, false);
            } catch (DistributedTableDescriptionException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (UnknownTableException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            record.setSources(sources);
            result = null;

        } catch (SQLException sqle) {
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return record;
    }

    private synchronized Source getSource(int recordID, boolean lock) throws RecordLockedException {
        Source record = null;
        ResultSetMetaData metadata;
        if (lock && checkAndLockRecord(recordID, Globals.SOURCE_TABLE_NAME)) {
            throw new RecordLockedException();
        }
        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetSource = connection.prepareStatement(STR_GET_SOURCE)) {
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return record;
    }
    
    public synchronized Source getSourceBySourceID(String sourceID) throws RecordLockedException {
        Source record = null;
        ResultSetMetaData metadata;

        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetSourceBySourceID = connection.prepareStatement(STR_GET_SOURCE_BY_SOURCE_ID)) {
            stmtGetSourceBySourceID.clearParameters();
            stmtGetSourceBySourceID.setString(1, sourceID);
            ResultSet result = stmtGetSourceBySourceID.executeQuery();
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
            LOGGER.log(Level.SEVERE, null, sqle);
        }
        return record;
    }

    public synchronized String getNextPatientID() {
        String patientID = null;
        try(Connection connection = getDbConnection();
            PreparedStatement  stmtGetHighestPatientID = connection.prepareStatement(strGetHighestPatientID);) 
        {
            ResultSet result = stmtGetHighestPatientID.executeQuery();
            result.next();
            String highestPatientID = result.getString(1);
            result.close();
            if (highestPatientID != null) {
                patientID = canreg.common.Tools.increment(highestPatientID);
            } else {
                int year = Calendar.getInstance().get(Calendar.YEAR);
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return patientID;
    }

    public synchronized String getNextTumourID(String patientRecordID) {
        String tumourID = null;
        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetHighestTumourID = connection.prepareStatement(strGetHighestTumourID)) {
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return tumourID;
    }

    public synchronized String getNextPatientRecordID(String patientID) {
        String patientRecordID = null;
        try(Connection connection = getDbConnection();
        PreparedStatement stmtGetHighestPatientRecordID = connection.prepareStatement(strGetHighestPatientRecordID))
        {
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return patientRecordID;
    }

    public synchronized String getNextSourceID(String tumourRecordID) {
        String sourceID = null;
        try(Connection connection = getDbConnection();
            PreparedStatement stmtGetHighestSourceRecordID = connection.prepareStatement(strGetHighestSourceRecordID))
        {
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return sourceID;
    }

    //not used
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return tumourRecordID;
    }

    protected synchronized void dropAndRebuildUsersTable() {
        try {
            Statement statement;
            statement = dbConnection.createStatement();
            statement.execute("DROP TABLE " + Globals.SCHEMA_NAME + ".USERS");
            statement.execute(QueryGenerator.strCreateUsersTable());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private Set<Source> getSourcesByTumourID(Object tumourID, boolean lock) throws SQLException, DistributedTableDescriptionException, UnknownTableException, RecordLockedException {
        String recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();

        DatabaseFilter filter = new DatabaseFilter();
        filter.setFilterString(recordIDVariableName + " = '" + tumourID + "' ");
        DistributedTableDescription distributedTableDescription;
        Object[][] rows;
        try(Connection connection = getDbConnection()) {
            distributedTableDescription = getDistributedTableDescriptionAndInitiateDatabaseQuery(
                    connection, filter, Globals.SOURCE_TABLE_NAME, generateResultSetID());
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
    }

    /**
     * Simple console trace to system.out for debug purposes only.
     *
     * @param msg the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (DEBUG) {
            LOGGER.log(Level.INFO, msg);
        }
    }
    private DbConnectionWrapper dbConnection;
    private Properties dbProperties;
    private String bootPassword = null;
    private boolean isConnected;
    private final String registryCode;
    private final Document doc;
    private Map<Integer, Dictionary> dictionaryMap;
    private final Map<String, Set<Integer>> locksMap;
    private final Map<String, DistributedTableDataSource> distributedDataSources;
    private final Map<String, Statement> activeStatements;
    private boolean tableOfDictionariesFilled = true;
    private boolean tableOfPopulationDataSets = true;
    private PreparedStatement stmtGetHighestTumourRecordID; // not used
    private final String ns = Globals.NAMESPACE;
    private final String patientIDVariableName;
    private final String patientRecordIDVariableName;
    private final String tumourIDVariableName;
    private final String sourceRecordIDVariableName;
    private static final String STR_GET_PATIENT
            = "SELECT * FROM APP.PATIENT "
            + "WHERE " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String STR_GET_PATIENT_BY_PATIENT_RECORD_ID = "SELECT * FROM APP.PATIENT WHERE PATIENTRECORDID = ?";
    private static final String STR_GET_PATIENTS
            = "SELECT * FROM APP.PATIENT";
    private static final String STR_COUNT_PATIENTS
            = "SELECT COUNT(*) FROM APP.PATIENT";
    private static final String STR_COUNT_SOURCES
            = "SELECT COUNT(*) FROM APP.SOURCE";
    private static final String STR_GET_SOURCES
            = "SELECT * FROM APP.SOURCE";
    private final String strGetPatientsAndTumours;
    private final String strCountPatientsAndTumours;
    private final String strGetSourcesAndTumours;
    private final String strCountSourcesAndTumours;
    private final String strGetSourcesAndTumoursAndPatients;
    private final String strCountSourcesAndTumoursAndPatients;
    private static final String STR_GET_TUMOUR
            = "SELECT * FROM APP.TUMOUR "
            + "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String STR_GET_TUMOUR_BY_TUMOUR_ID
            = "SELECT * FROM APP.TUMOUR "
            + "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME_FOR_HOLDING + " = ?";
    private static final String STR_GET_SOURCE
            = "SELECT * FROM APP.Source "
            + "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String STR_GET_SOURCE_BY_SOURCE_ID
            = "SELECT * FROM APP.Source "
            + "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME_FOR_HOLDING + " = ?";
    private static final String STR_GET_TUMOURS
            = "SELECT * FROM APP.TUMOUR";
    private static final String STR_COUNT_TUMOURS
            = "SELECT COUNT(*) FROM APP.TUMOUR";
    private static final String STR_GET_DICTIONARY
            = "SELECT * FROM APP.DICTIONARIES WHERE ID = ?";
    private static final String STR_GET_DICTIONARIES
            = "SELECT * FROM APP.DICTIONARIES ";
    private static final String STR_GET_DICTIONARY_ENTRY
            = "SELECT * FROM APP.DICTIONARY "
            + "WHERE ID = ?";
   
    private static final String STR_GET_POPULATION_DATASET_ENTRIES
            = "SELECT * FROM APP.PDSET ";
   
    private static final String STR_DELETE_PATIENT_RECORD
            = "DELETE FROM APP.PATIENT "
            + "WHERE " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String STR_DELETE_SOURCE_RECORD
            = "DELETE FROM APP.SOURCE "
            + "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String STR_DELETE_TUMOUR_RECORD
            = "DELETE FROM APP.TUMOUR "
            + "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String STR_DELETE_DICTIONARY_ENTRIES
            = "DELETE FROM APP.DICTIONARY "
            + "WHERE DICTIONARY = ?";
    private static final String STR_CLEAR_NAME_SEX_TABLE
            = "DELETE FROM APP.NAMESEX";
    private static final String STR_DELETE_POPULATION_DATASET
            = "DELETE FROM APP.PDSETS "
            + "WHERE PDS_ID = ?";
    private static final String STR_DELETE_POPULATION_DATASET_ENTRIES
            = "DELETE FROM APP.PDSET "
            + "WHERE PDS_ID = ?";
    private static final String STR_GET_USER_BY_USER_NAME
            = "SELECT * FROM APP.USERS WHERE username = ?";
    // The Dynamic ones
    private final String strMaxNumberOfSourcesPerTumourRecord;
    private final String strSavePatient;
    private final String strSaveTumour;
    private final String strSaveSource;
    private final String strEditPatient;
    private final String strEditTumour;
    private final String strEditSource;
    private final String strSaveDictionary;
    private final String strSaveDictionaryEntry;
    private final String strSavePopoulationDataset;
    private final String strSavePopoulationDatasetsEntry;
    private final String strSaveNameSexRecord;
    private final String strDeleteNameSexRecord;
    private final String strGetHighestPatientID;
    private final String strGetHighestTumourID;
    private final String strGetHighestPatientRecordID;
    private final String strGetHighestSourceRecordID;
    private final String strCountPatientByRegistryNumber;
    private final String strCountPatientByRecordID;
    private final String strCountTumourByTumourID;
    private final String strCountSourceByRecordID;
    /* We don't use tumour record ID...
     private String strGetHighestTumourRecordID;
     */
    private final GlobalToolBox globalToolBox;

    private synchronized void saveSources(Object tumourID, Set<Source> sources) throws SQLException {
        if (sources != null) {
            String tumourIDSourceTableVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
            for (Source source : sources) {
                source.setVariable(tumourIDSourceTableVariableName, tumourID);
                // FIXME: connection is not used
                try(Connection connection = getDbConnection() ) {
                    saveSource(source);
                } catch (RecordLockedException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private synchronized void deleteSources(Object tumourID)
            throws SQLException, DistributedTableDescriptionException, UnknownTableException, RecordLockedException {
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

    public synchronized void releaseRecord(int recordID, String tableName) {
        // release a locked record
        Set lockSet = locksMap.get(tableName);
        if (lockSet != null) {
            lockSet.remove(recordID);
        }
    }
    
    /**
     * Method that check if a record in a table is already lock 
     * and lock it if it's not the case
     * 
     * @param recordID the id of the record 
     * @param tableName name of  the table in the database
     * @return  true if the record was already locked, else false
     */
    private synchronized boolean checkAndLockRecord(int recordID, String tableName) {
        boolean wasLocked = false;
        Set<Integer> lockSet = locksMap.get(tableName);
        // if the wasLocked set is null create a new wasLocked set
        if (lockSet == null) {
            lockSet = new TreeSet<>();
            locksMap.put(tableName, lockSet);
        } else {
            wasLocked = lockSet.contains(recordID);
        }
        if(!wasLocked){
            lockSet.add(recordID);
        }
        return wasLocked;
    }

    private synchronized boolean isRecordLocked(int recordID, String tableName) {
        boolean lock = false;
        Set lockSet = locksMap.get(tableName);
        if (lockSet != null) {
            lock = lockSet.contains(recordID);
        }
        return lock;
    }

    public DatabaseStats getDatabaseStats() {
        DatabaseStats dbs = new DatabaseStats();
        try(Connection connection = getDbConnection();
             Statement stmtMaxNumberOfSourcesPerTumourRecord = connection.prepareStatement
                 (strMaxNumberOfSourcesPerTumourRecord)) {
            ResultSet result = stmtMaxNumberOfSourcesPerTumourRecord.executeQuery(strMaxNumberOfSourcesPerTumourRecord);
            result.next();
            int maxNumberOfSourcesPerTumourRecord = result.getInt(1);
            dbs.setMaxNumberOfSourcesPerTumourRecord(maxNumberOfSourcesPerTumourRecord);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return dbs;
    }

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
        String query; // the sql query
        String rangePart; // the sql query part where the ID range is set
        String blockAttributesPart = ""; // the sql query part where the blocked attributes are listed
        int rowCount = -1;
        DistributedTableDataSource dataSource;
        StringBuilder whereQuery; // the "WHERE" sql query part
        ArrayList<String> useWhereQueryParts = new ArrayList<>(); // arrayList which contains the different parts of the "WHERE" section

        // This part is used during the global duplicate person search
        if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
            rangePart = QueryGenerator.buildRangePart(filter);
            if (rangePart.length() > 0) {
                useWhereQueryParts.add(rangePart);
            }
        }
        // this part is used during a single person duplicate search
        if ( filter.getFilterString() != null && filter.getFilterString().length() > 0 ) {
            useWhereQueryParts.add(filter.getFilterString()); //
        }

        whereQuery = new StringBuilder((useWhereQueryParts.size() > 0) ? " WHERE " + useWhereQueryParts.get(0) : "");
        // normally, useWhereQueryParts size should always be 1
        for (int i = 1; i < useWhereQueryParts.size(); i++) {
            whereQuery.append(" AND ").append(useWhereQueryParts.get(i));
        }

        query = "SELECT " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " FROM APP.PATIENT" + whereQuery;
        try {
            result = statement.executeQuery(query);
        } catch (java.sql.SQLSyntaxErrorException ex) {
            throw ex;
        }
        // counting rows
        if (result.last()) {
            rowCount = result.getRow();
            result.first();
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
            if ((tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME))) {
                filterString = " WHERE ( " + filterString + " )";
            } else {
                filterString = " AND ( " + filterString + " )";
            }
        }

        // Add the range part
        if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
            if (!filterString.isEmpty()) {
                filterString += " AND ";
            } else {
                filterString += " WHERE ";
            }
            String rangeFilterString = QueryGenerator.buildRangePart(filter);
            filterString += rangeFilterString;
        }
        filterVariables = filter.getDatabaseVariables();
        String variablesList = "";
        if (filterVariables.size() > 0) {
            for (DatabaseVariablesListElement variable : filterVariables) {
                if (variable != null) {
                    variablesList += ", APP." + variable.getDatabaseTableName() + "." + variable.getDatabaseVariableName();
                }
            }
        }
        String patientIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        String patientIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        String incidenceDateVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString()).getDatabaseVariableName();
        String tumourIDVariableNameSourceTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        String tumourIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();

        if (tableName.equalsIgnoreCase(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)
                || tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.TUMOUR, APP.PATIENT "
                    + "WHERE APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " "
                    + filterString + " "
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)
                || tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.SOURCE, APP.TUMOUR "
                    + "WHERE APP.SOURCE." + tumourIDVariableNameSourceTable + " = APP.TUMOUR." + tumourIDVariableNameTumourTable + " "
                    + filterString + " "
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.TUMOUR "
                    + (filterString.trim().length() > 0 ? filterString + " " : "")
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            query = "SELECT SUBSTR(" + incidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases "
                    + "FROM APP.SOURCE, APP.TUMOUR, APP.PATIENT "
                    + "WHERE APP.SOURCE." + tumourIDVariableNameSourceTable + " = APP.TUMOUR." + tumourIDVariableNameTumourTable + " "
                    + "AND APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " "
                    + filterString + " "
                    + "GROUP BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList + " "
                    + "ORDER BY SUBSTR(" + incidenceDateVariableName + ",1,4) " + variablesList;
        }
        LOGGER.info(query);
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
            counterStringBuilder.append(STR_COUNT_TUMOURS);
            getterStringBuilder.append(STR_GET_TUMOURS);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            counterStringBuilder.append(STR_COUNT_PATIENTS);
            getterStringBuilder.append(STR_GET_PATIENTS);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            counterStringBuilder.append(STR_COUNT_SOURCES);
            getterStringBuilder.append(STR_GET_SOURCES);
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

        if(DEBUG) {
            LOGGER.log(Level.INFO,"filterString: {}", filterStringBuilder);
            LOGGER.log(Level.INFO,"getterString: {}", getterStringBuilder);
            LOGGER.log(Level.INFO,"counterString: {}",counterStringBuilder);
        }

        ResultSet countRowSet;
        try {
            // already a declared statement
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
            // already a declared statement
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
        DatabaseVariablesListElement element
                = globalToolBox.translateStandardVariableNameToDatabaseListElement(
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
                LOGGER.log(Level.SEVERE, null, ex);
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
                LOGGER.log(Level.INFO, command);
                statement.execute(command);
            } catch (SQLException sqle) {
                LOGGER.log(Level.SEVERE, String.format("Exception in : %s", command ), sqle);
                success = false;
            }
        }
        // Set primary keys in tumour table
        for (String command : QueryGenerator.strCreateTumourTablePrimaryKey(
                globalToolBox.translateStandardVariableNameToDatabaseListElement(
                        Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName())) {
            try {
                LOGGER.log(Level.INFO, command);
                statement.execute(command);
            } catch (SQLException sqle) {
                LOGGER.log(Level.SEVERE, String.format("Exception in : %s", command), sqle);
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
                LOGGER.log(Level.SEVERE, "Exception in : " + command, sqle);
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
                LOGGER.log(Level.SEVERE, "Exception in : " + command, sqle);
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
                LOGGER.log(Level.SEVERE, String.format("Exception in : %s", command), sqle);
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

    public String getRegistryCode() {
        return registryCode;
    }

    void upgrade() throws SQLException, RemoteException {
        openUniqueConnection(getDatabaseUrl() + ";upgrade=true");
        LOGGER.log(Level.INFO, "JavaDB Version: {0}", dbConnection.getMetaData().getDatabaseProductVersion());
    }

    /**
     * Getter variables.
     *
     * @return variables variables.
     */
    public DatabaseVariablesListElement[] getDatabaseVariablesList() {
        return variables;
    }

    /**
     * Getter patientIDVariableName.
     *
     * @return patientIDVariableName patientIDVariableName.
     */
    public String getPatientIDVariableName() {
        return patientIDVariableName;
    }

    /**
     * Getter patientRecordIDVariableName
     * @return patientRecordIDVariableName patientRecordIDVariableName
     */
    public String getPatientRecordIDVariableName() {
        return patientRecordIDVariableName;
    }

    /**
     * Getter tumourIDVariableName
     * @return tumourIDVariableName tumourIDVariableName
     */
    public String getTumourIDVariableName() {
        return tumourIDVariableName;
    }
    /**
     * Getter sourceRecordIDVariableName
     * @return sourceRecordIDVariableName sourceRecordIDVariableName
     */
    public String getSourceRecordIDVariableName() {
        return sourceRecordIDVariableName;
    }
}
