package canreg.server.database;

/**
 *
 * @author ervikm
 */
import cachingtableapi.DistributedTableDataSource;
import cachingtableapi.DistributedTableDescription;
import cachingtableapi.DistributedTableDescriptionException;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;

/**
 *
 * @author ervikm (based on code by John O'Conner)
 */
public class CanRegDAO {

    /**
     * 
     * @param systemCode 
     * @param doc
     */
    public CanRegDAO(String systemCode, Document doc) {
        this.doc = doc;

        this.systemCode = systemCode;

        globalToolBox = new GlobalToolBox(doc);

        distributedDataSources = new LinkedHashMap<String, DistributedTableDataSource>();
        dictionaryMap = buildDictionaryMap(doc);

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
        strGetPatientsAndTumours = QueryGenerator.strGetPatientsAndTumours(globalToolBox);
        strCountPatientsAndTumours = QueryGenerator.strCountPatientsAndTumours(globalToolBox);
        strGetHighestPatientID = QueryGenerator.strGetHighestPatientID(globalToolBox);
        strGetHighestTumourID = QueryGenerator.strGetHighestTumourID(globalToolBox);
        strGetHighestPatientRecordID = QueryGenerator.strGetHighestPatientRecordID(globalToolBox);
        strGetHighestSourceRecordID = QueryGenerator.strGetHighestSourceRecordID(globalToolBox);
        strEditUser = QueryGenerator.strEditUser();
        strSaveUser = QueryGenerator.strSaveUser();
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
        Statement queryStatement = null;
        ResultSet results = null;

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
            sqle.printStackTrace();
        }
        return dictionaryMap;
    }

    /**
     * 
     * @return
     */
    public synchronized Map<String, Integer> getNameSexTables() {

        Map<String, Integer> nameSexMap = new LinkedHashMap<String, Integer>();
        Statement queryStatement = null;
        ResultSet results = null;

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
            sqle.printStackTrace();
        }
        return nameSexMap;
    }

    public synchronized String getSystemPropery(String lookup) {
        String value = null;
        try {
            String query = "SELECT * FROM " + Globals.SCHEMA_NAME + ".SYSTEM WHERE LOOKUP = '" + lookup + "'";
            Statement queryStatement = null;
            ResultSet results = null;
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

    public synchronized void setSystemPropery(String lookup, String value) {
        try {
            String query = "DELETE FROM " + Globals.SCHEMA_NAME + ".SYSTEM WHERE LOOKUP = '" + lookup + "'";
            Statement queryStatement = null;
            queryStatement = dbConnection.createStatement();
            boolean result = queryStatement.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            String query = "INSERT INTO " + Globals.SCHEMA_NAME + ".SYSTEM (LOOKUP, VALUE) VALUES ('" + lookup + "', '" + value + "')";
            Statement queryStatement = null;
            queryStatement = dbConnection.createStatement();
            boolean result = queryStatement.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
        ResultSet results = null;
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
        ResultSet results = null;
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

    public synchronized Map<String, User> getUsers() {
        Map<String, User> usersMap = new LinkedHashMap<String, User>();
        Statement queryStatement = null;
        ResultSet results = null;
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
            sqle.printStackTrace();
        }
        return usersMap;
    }

    /**
     * 
     * @return
     */
    public synchronized Map<Integer, PopulationDataset> getPopulationDatasets() {
        Map<Integer, PopulationDataset> populationDatasetMap = new LinkedHashMap<Integer, PopulationDataset>();
        Statement queryStatement = null;
        ResultSet results = null;

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
            sqle.printStackTrace();
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
            sqle.printStackTrace();
        }

        return populationDatasetMap;
    }

    /**
     * 
     * @param filter
     * @param tableName
     * @return
     * @throws java.sql.SQLException
     * @throws java.lang.Exception
     */
    public synchronized DistributedTableDescription getDistributedTableDescriptionAndInitiateDatabaseQuery(DatabaseFilter filter, String tableName)
            throws SQLException, UnknownTableException, DistributedTableDescriptionException {
        // distributedDataSources.remove(theUser);
        ResultSet result;
        Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        int rowCount = 0;
        DistributedTableDataSource dataSource;
        Set<DatabaseVariablesListElement> variables;

        // Is this a person search query?
        if (DatabaseFilter.QueryType.PERSON_SEARCH.equals(filter.getQueryType())) {
            String query = "";
            String rangePart = "";

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

        } // Or a Frequency by year query?
        else if (DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR.equals(filter.getQueryType())) {
            String filterString = filter.getFilterString();
            String query = "";
            if (!filterString.isEmpty()) {
                filterString = " AND ( " + filterString + " )";
            }

            // Add the range part
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
                filterString += " AND ";
                String rangeFilterString = QueryGenerator.buildRangePart(filter);
                filterString += rangeFilterString;
            }
            variables = filter.getDatabaseVariables();
            String variablesList = "";
            if (variables.size() > 0) {
                for (DatabaseVariablesListElement variable : variables) {
                    if (variable != null) {
                        variablesList += ", " + variable.getDatabaseVariableName();
                    }
                }

                // variablesList = variablesList.substring(0, variablesList.length() - 2);

            }
            String patientIDVariableNamePatientTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
            String patientIDVariableNameTumourTable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
            String IncidenceDateVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString()).getDatabaseVariableName();

            query = "SELECT SUBSTR(" + IncidenceDateVariableName + ",1,4) as \"YEAR\" " + variablesList + ", COUNT(*) as Cases " +
                    "FROM APP.TUMOUR, APP.PATIENT " +
                    "WHERE APP.PATIENT." + patientIDVariableNamePatientTable + " = APP.TUMOUR." + patientIDVariableNameTumourTable + " " + filterString + " " +
                    "GROUP BY SUBSTR(" + IncidenceDateVariableName + ",1,4) " + variablesList + " " +
                    "ORDER BY SUBSTR(" + IncidenceDateVariableName + ",1,4) " + variablesList;
            System.out.print(query);
            try {
                result = statement.executeQuery(query);
            } catch (java.sql.SQLSyntaxErrorException ex) {
                throw ex;
            }


        } // Or a "regular" query from the tumour table
        else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " WHERE " + filterString;
            }

            // Add the range part
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
                if (filterString.isEmpty()) {
                    filterString = " WHERE " + filterString;
                } else {
                    filterString += " AND ";
                }
                filterString += QueryGenerator.buildRangePart(filter);
            }

            debugOut(strCountTumours + filterString);
            ResultSet countRowSet;
            try {
                countRowSet = statement.executeQuery(strCountTumours + filterString);
            } catch (java.sql.SQLException ex) {
                throw ex;
            }
            if (countRowSet.next()) {
                rowCount = countRowSet.getInt(1);
            }
            if (filter.getSortByVariable() != null) {
                filterString += " ORDER BY \"" + filter.getSortByVariable().toUpperCase() + "\"";
            }
            try {
                result = statement.executeQuery(strGetTumours + filterString);
            } catch (java.sql.SQLSyntaxErrorException ex) {
                throw ex;
            }
        } // Or a "regular" query from the patient table
        else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " WHERE (" + filterString + " )";
            }
            // Add the range part
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
                if (filterString.isEmpty()) {
                    filterString = " WHERE " + filterString;
                } else {
                    filterString += " AND ";
                }
                filterString += QueryGenerator.buildRangePart(filter);
            }
            debugOut(strCountPatients + filterString);
            ResultSet countRowSet;
            try {
                countRowSet = statement.executeQuery(strCountPatients + filterString);
            } catch (java.sql.SQLSyntaxErrorException ex) {
                throw ex;
            }

            if (countRowSet != null && countRowSet.next()) {
                rowCount = countRowSet.getInt(1);
            }
            if (filter.getSortByVariable() != null) {
                filterString += " ORDER BY \"" + filter.getSortByVariable().toUpperCase() + "\"";
            }
            try {
                result = statement.executeQuery(strGetPatients + filterString);
            } catch (java.sql.SQLSyntaxErrorException ex) {
                throw ex;
            }
        } // Or a "regular" query from a join of both tables
        else if (tableName.equalsIgnoreCase("both")) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " AND (" + filterString.trim() + ")";
            }
            // Add the range part
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {

                filterString += " AND ";

                filterString += QueryGenerator.buildRangePart(filter);
            }

            debugOut(strCountPatientsAndTumours + filterString);


            ResultSet countRowSet;
            try {
                countRowSet = statement.executeQuery(strCountPatientsAndTumours + filterString);
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
                filterString += " ORDER BY \"" + filter.getSortByVariable().toUpperCase() + "\"";
            }
            try {
                result = statement.executeQuery(strGetPatientsAndTumours + filterString);
            } catch (java.sql.SQLSyntaxErrorException ex) {
                throw ex;
            }

        } // Or a "regular" query from the tumour table
        else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            String filterString = filter.getFilterString();
            if (!filterString.isEmpty()) {
                filterString = " WHERE " + filterString;
            }

            // Add the range part
            if ((filter.getRangeStart() != null && filter.getRangeStart().length() > 0) || (filter.getRangeEnd() != null && filter.getRangeEnd().length() > 0)) {
                if (filterString.isEmpty()) {
                    filterString = " WHERE " + filterString;
                } else {
                    filterString += " AND ";
                }
                filterString += QueryGenerator.buildRangePart(filter);
            }

            debugOut(strCountSources + filterString);
            ResultSet countRowSet;
            try {
                countRowSet = statement.executeQuery(strCountSources + filterString);
            } catch (java.sql.SQLException ex) {
                throw ex;
            }
            if (countRowSet.next()) {
                rowCount = countRowSet.getInt(1);
            }
            if (filter.getSortByVariable() != null) {
                filterString += " ORDER BY \"" + filter.getSortByVariable().toUpperCase() + "\"";
            }
            try {
                result = statement.executeQuery(strGetSources + filterString);
            } catch (java.sql.SQLSyntaxErrorException ex) {
                throw ex;
            }
        }// Or an unknown query...
        else {
            throw new UnknownTableException("Unknown table name.");
        }


        if (rowCount > 0) {
            dataSource = new DistributedTableDataSourceResultSetImpl(rowCount, result);
        } else {
            dataSource = new DistributedTableDataSourceResultSetImpl(result);
        }

        DistributedTableDescription tableDescription = dataSource.getTableDescription();
        //distributedDataSources.put(tableDescription, dataSource);

        boolean foundPlace = false;
        int i = 0;
        String place = Integer.toString(i);
        // Find a spot in the map of datasources
        while (!foundPlace) {
            place = Integer.toString(i++);
            foundPlace = !distributedDataSources.containsKey(place);
        }

        tableDescription.setResultSetID(place);

        distributedDataSources.put(place, dataSource);
        return tableDescription;
    }

    /**
     * 
     * @param resultSetID
     */
    public synchronized void releaseResultSet(String resultSetID) {
        distributedDataSources.remove(resultSetID);
    }

    /**
     * 
     * @param recordID
     * @param tableName
     * @return
     */
    public synchronized DatabaseRecord getRecord(int recordID, String tableName) {
        if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            return getPatient(recordID);
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            return getTumour(recordID);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            return getSource(recordID);
        } else {
            return null;
        }
    }

    /**
     * Perform backup of the database
     * @return Path to backup
     */
    public synchronized String performBackup() {
        String path = null;
        try {
            path = canreg.server.database.derby.Backup.backUpDatabase(dbConnection, Globals.CANREG_BACKUP_FOLDER + Globals.FILE_SEPARATOR + systemCode);
            canreg.server.xml.Tools.writeXmlFile(doc, path + Globals.FILE_SEPARATOR + systemCode + ".xml");
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
     * @throws java.lang.Exception
     */
    public synchronized Object[][] retrieveRows(String resultSetID, int from, int to) throws DistributedTableDescriptionException {
        DistributedTableDataSource ts = distributedDataSources.get(resultSetID);
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

    private synchronized boolean createTables(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
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
            for (String command : QueryGenerator.strCreatePatientTablePrimaryKey(
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName())) {
                statement.execute(command);
            }

            // Set foreign keys in tumour table
            for (String command : QueryGenerator.strCreateTumourTableForeignKey(
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName(),
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName())) {
                statement.execute(command);
            }

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
            ex.printStackTrace();
            // ((DonMan) parent).signalError("Error during shutdown for RESTORE: ", ex,
            //        "in: DonDao.restore", false);
            return "shutdown failed";
        }
        try {
            dbProperties.remove("shutdown");
            dbConnection.close(); // Close current connection.
            dbProperties.put("restoreFrom", path + "/" + systemCode);
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bRestored = true;
        } catch (SQLException e) {
            e.printStackTrace();
            //((DonMan) parent).signalError("Error during RESTORE: ", e,
            //       "in: DonDao.restore", false);
        }
        dbProperties.remove("restoreFrom");
        // connect(); // Do not reconnect as this would be a potential security problem...
        if (bRestored) {
            try {
                // install the xml
                canreg.common.Tools.fileCopy(path + Globals.FILE_SEPARATOR + systemCode + ".xml",
                        Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + systemCode + ".xml");
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
     */
    public boolean connect() {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);

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
            stmtDeletePopoulationDataset = dbConnection.prepareStatement(strDeletePopulationDataset);
            stmtDeletePopoulationDatasetEntries = dbConnection.prepareStatement(strDeletePopulationDatasetEntries);
            //stmtUpdateExistingPatient = dbConnection.prepareStatement(strUpdatePatient);
            stmtGetPatient = dbConnection.prepareStatement(strGetPatient);
            stmtGetPatients = dbConnection.prepareStatement(strGetPatients);
            stmtGetSources = dbConnection.prepareStatement(strGetSources);
            stmtGetPatientsAndTumours = dbConnection.prepareStatement(strGetPatientsAndTumours);

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


            debugOut("Cocuou from the database connection...");
            debugOut("Next tumour ID = " + getNextTumourID());
        } catch (SQLException ex) {
            debugOut("SQLerror... ");
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
        String dbLocation = System.getProperty("derby.system.home") + "/" + systemCode;
        return dbLocation;
    }

    /**
     * 
     * @return
     */
    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + systemCode;
        return dbUrl;
    }

    private synchronized int saveRecord(String tableName, DatabaseRecord record, PreparedStatement stmtSaveNewRecord) throws SQLException {
        int id = -1;

        stmtSaveNewRecord.clearParameters();

        // Get the dictionaries node in the XML
        NodeList nodes = doc.getElementsByTagName(Globals.NAMESPACE + "variables");
        Element variablesElement = (Element) nodes.item(0);

        NodeList variables = variablesElement.getElementsByTagName(Globals.NAMESPACE + "variable");

        int recordVariableNumber = 0;

        // Go through all the variable definitions
        for (int i = 0; i < variables.getLength(); i++) {
            // Get element
            Element element = (Element) variables.item(i);

            // Create line
            String tableNameDB = element.getElementsByTagName(Globals.NAMESPACE + "table").item(0).getTextContent();

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                recordVariableNumber++;
                String variableType = element.getElementsByTagName(Globals.NAMESPACE + "variable_type").item(0).getTextContent();
                Object obj = record.getVariable(element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent());

                // System.out.println(
                //         element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent() +
                //         ": " + obj.toString());

                if (variableType.equalsIgnoreCase("Alpha") || variableType.equalsIgnoreCase("AsianText") || variableType.equalsIgnoreCase("Dict") || variableType.equalsIgnoreCase("Date")) {
                    if (obj != null) {
                        try {
                            String strObj = (String) obj;
                            if (strObj.length() > 0) {
                                stmtSaveNewRecord.setString(recordVariableNumber, strObj);
                            } else {
                                stmtSaveNewRecord.setString(recordVariableNumber, "");
                            }
                        } catch (java.lang.ClassCastException cce) {
                            System.out.println("String " + variableType + " " + obj);
                            throw cce;
                        }
                    } else {
                        stmtSaveNewRecord.setString(recordVariableNumber, "");
                    }
                } else if (variableType.equalsIgnoreCase("Number")) {
                    if (obj != null) {
                        try {
                            Integer intObj = (Integer) obj;
                            stmtSaveNewRecord.setInt(recordVariableNumber, intObj.intValue());
                        } catch (java.lang.ClassCastException cce) {
                            System.out.println("Number " + variableType + " " + obj);
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
     */
    public synchronized int savePatient(Patient patient) throws SQLException {
        String patientIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        Object patientID = patient.getVariable(patientIDVariableName);
        if (patientID == null || patientID.toString().trim().length() == 0) {
            patient.setVariable(patientIDVariableName, getNextPatientID());
        }
        String patientRecordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        Object patientRecordID = patient.getVariable(patientRecordIDVariableName);
        if (patientRecordID == null || patientRecordID.toString().trim().length() == 0) {
            patient.setVariable(patientRecordIDVariableName, getNextPatientRecordID());
        }
        return saveRecord("Patient", patient, stmtSaveNewPatient);
    }

    /**
     * 
     * @param tumour
     * @return
     */
    public synchronized int saveTumour(Tumour tumour) throws SQLException {
        String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        Object tumourID = tumour.getVariable(tumourIDVariableName);
        if (tumourID == null || tumourID.toString().trim().length() == 0) {
            tumourID = getNextTumourID();
            tumour.setVariable(tumourIDVariableName, tumourID);
        }


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

        return saveRecord(Globals.TUMOUR_TABLE_NAME, tumour, stmtSaveNewTumour);
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
            source.setVariable(sourceIDVariableName, getNextSourceID());
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
            sqle.printStackTrace();
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
            if (dict.getFullDictionaryDescriptionLength() < description.length()) {
                description = description.substring(0, dict.getFullDictionaryDescriptionLength());
            }
            stmtSaveNewDictionaryEntry.setString(3, description);

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
            sqle.printStackTrace();
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
            sqle.printStackTrace();
        }
        return id;
    }

    /**
     * 
     * @param nameSexRecord
     * @return
     */
    public synchronized int saveNameSexRecord(NameSexRecord nameSexRecord) {
        int id = -1;
        try {
            stmtSaveNewNameSexRecord.clearParameters();

            stmtSaveNewNameSexRecord.setString(1, nameSexRecord.getName());
            stmtSaveNewNameSexRecord.setInt(2, nameSexRecord.getSex());

            int rowCount = stmtSaveNewNameSexRecord.executeUpdate();
            ResultSet results = stmtSaveNewNameSexRecord.getGeneratedKeys();
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
     * @return
     */
    public synchronized boolean clearNameSexTable() {
        boolean success = false;
        try {
            stmtClearNameSexTable.clearParameters();

            stmtClearNameSexTable.executeUpdate();
            success = true;

        } catch (SQLException sqle) {
            sqle.printStackTrace();
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
            sqle.printStackTrace();
        }
        return success;
    }

    public synchronized boolean deletePatientRecord(int patientRecordID) {
        boolean success = false;
        try {
            stmtDeletePatientRecord.clearParameters();
            stmtDeletePatientRecord.setInt(1, patientRecordID);
            stmtDeletePatientRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return success;
    }

    public synchronized boolean deleteTumourRecord(int tumourRecordID) {
        boolean success = false;
        try {
            stmtDeleteTumourRecord.clearParameters();
            stmtDeleteTumourRecord.setInt(1, tumourRecordID);
            stmtDeleteTumourRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return success;
    }

        public synchronized boolean deleteSourceRecord(int sourceRecordID) {
        boolean success = false;
        try {
            stmtDeleteSourceRecord.clearParameters();
            stmtDeleteSourceRecord.setInt(1, sourceRecordID);
            stmtDeleteSourceRecord.executeUpdate();
            success = true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return success;
    }

    public synchronized boolean deleteRecord(int recordID, String tableName) {
        boolean success = false;
        String idString = "ID";
        try {
            Statement statement = null;
            ResultSet results = null;
            statement = dbConnection.createStatement();
            statement.execute("DELETE FROM " + Globals.SCHEMA_NAME + "." + tableName + " WHERE " + idString + " = " + recordID);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return success;
    }

    public synchronized boolean deletePopulationDataSet(int id) {
        boolean success = false;
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
            sqle.printStackTrace();
        }
        return success;
    }

    /**
     * 
     * @param patient
     * @return
     */
    public synchronized boolean editPatient(Patient patient) {
        return editRecord("Patient", patient, stmtEditPatient);
    }

    /**
     * 
     * @param tumour
     * @return
     */
    public synchronized boolean editTumour(Tumour tumour) {
        return editRecord("Tumour", tumour, stmtEditTumour);
    }

    /**
     *
     * @param tumour
     * @return
     */
    public synchronized boolean editSource(Source source) {
        return editRecord("Source", source, stmtEditSource);
    }

    /**
     * 
     * @param tableName
     * @param record
     * @param stmtEditRecord
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

            int variableNumber = 0;

            // Go through all the variable definitions
            for (int i = 0; i < variables.getLength(); i++) {
                // Get element
                Element element = (Element) variables.item(i);

                // Create line
                String tableNameDB = element.getElementsByTagName(Globals.NAMESPACE + "table").item(0).getTextContent();

                if (tableNameDB.equalsIgnoreCase(tableName)) {
                    variableNumber++;
                    String variableType = element.getElementsByTagName(Globals.NAMESPACE + "variable_type").item(0).getTextContent();
                    Object obj = record.getVariable(element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent());

                    if (variableType.equalsIgnoreCase("Alpha") || variableType.equalsIgnoreCase("AsianText") || variableType.equalsIgnoreCase("Dict") || variableType.equalsIgnoreCase("Date")) {
                        if (obj != null) {
                            try {
                                String strObj = (String) obj;
                                if (strObj.length() > 0) {
                                    stmtEditRecord.setString(variableNumber, strObj);
                                } else {
                                    stmtEditRecord.setString(variableNumber, "");
                                }
                                System.out.println(
                                        element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent() +
                                        ": " + strObj);
                            } catch (java.lang.ClassCastException cce) {
                                System.out.println("String " + variableType + " " + obj);
                                throw cce;
                            }
                        } else {
                            stmtEditRecord.setString(variableNumber, "");
                        }
                    } else if (variableType.equalsIgnoreCase("Number")) {
                        if (obj != null) {
                            try {
                                Integer intObj = (Integer) obj;
                                stmtEditRecord.setInt(variableNumber, intObj.intValue());
                                System.out.println(
                                        element.getElementsByTagName(Globals.NAMESPACE + "short_name").item(0).getTextContent() +
                                        ": " + obj.toString());
                            } catch (java.lang.ClassCastException cce) {
                                System.out.println("Number " + variableType + " " + obj);
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
            sqle.printStackTrace();
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
    public synchronized Patient getPatient(int recordID) {
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
    /**
     * 
     * @param recordID
     * @return
     */
    public synchronized Tumour getTumour(int recordID) {
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

            // get the source information
            String recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
            Object tumourID = record.getVariable(recordIDVariableName);

            Set<Source> sources = null;
            try {
                sources = getSourcesByTumourID(tumourID);
            } catch (DistributedTableDescriptionException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownTableException ex) {
                Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
            }

            record.setSources(sources);

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return record;
    }

    /**
     *
     * @param recordID
     * @return
     */
    public synchronized Source getSource(int recordID) {
        Source record = null;
        ResultSetMetaData metadata;
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

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return record;
    }

    public synchronized String getNextPatientID() {
        String patientID = null;
        try {
            ResultSet result = stmtGetHighestPatientID.executeQuery();
            result.next();
            String highestPatientID = result.getString(1);
            if (highestPatientID != null) {
                patientID = canreg.common.Tools.increment(highestPatientID);
            } else {
                // TODO replace with today's year and 00..001
                patientID = "1";
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientID;
    }

    public synchronized String getNextTumourID() {
        String tumourID = null;
        try {
            ResultSet result = stmtGetHighestTumourID.executeQuery();
            result.next();
            String highestTumourID = result.getString(1);
            if (highestTumourID != null) {
                tumourID = canreg.common.Tools.increment(highestTumourID);
            } else {
                // TODO replace with today's year and 00..001
                tumourID = "1";
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tumourID;
    }

    public synchronized String getNextPatientRecordID() {
        String patientRecordID = null;
        try {
            ResultSet result = stmtGetHighestPatientRecordID.executeQuery();
            result.next();
            String highestPatientRecordID = result.getString(1);
            if (highestPatientRecordID != null) {
                patientRecordID = canreg.common.Tools.increment(highestPatientRecordID);
            } else {
                patientRecordID = "1";
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientRecordID;
    }

    public synchronized String getNextSourceID() {
        String sourceID = null;
        try {
            ResultSet result = stmtGetHighestSourceRecordID.executeQuery();
            result.next();
            String highestSourceID = result.getString(1);
            if (highestSourceID != null) {
                sourceID = canreg.common.Tools.increment(highestSourceID);
            } else {
                // TODO replace with today's year and 00..001
                sourceID = "1";
            }
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sourceID;
    }

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

    protected synchronized void dropAndRebuildUsersTable() {
        try {
            Statement statement = null;
            ResultSet results = null;
            statement = dbConnection.createStatement();
            statement.execute("DROP TABLE " + Globals.SCHEMA_NAME + ".USERS");
            statement.execute(QueryGenerator.strCreateUsersTable());
        } catch (SQLException ex) {
            Logger.getLogger(CanRegDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<Source> getSourcesByTumourID(Object tumourID) throws SQLException, DistributedTableDescriptionException, UnknownTableException {
        String recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();

        DatabaseFilter filter = new DatabaseFilter();
        filter.setFilterString(recordIDVariableName + " = '" + tumourID + "' ");
        DistributedTableDescription distributedTableDescription;
        Object[][] rows;

        distributedTableDescription = getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, Globals.SOURCE_TABLE_NAME);
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
            Source source = null;
            for (Object[] row : rows) {
                int id = (Integer) row[idColumnNumber];
                source = (Source) getRecord(id, Globals.SOURCE_TABLE_NAME);
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
    private String systemCode;
    private Document doc;
    private Map<Integer, Dictionary> dictionaryMap;
    private Map<String, DistributedTableDataSource> distributedDataSources;
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
    private PreparedStatement stmtUpdateExistingPatient;
    private PreparedStatement stmtGetPatient;
    private PreparedStatement stmtGetTumour;
    private PreparedStatement stmtGetSource;
    private PreparedStatement stmtGetPatients;
    private PreparedStatement stmtGetSources;
    private PreparedStatement stmtGetTumours;
    private PreparedStatement stmtGetPatientsAndTumours;
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
    private String ns = Globals.NAMESPACE;
    private static final String strGetPatient =
            "SELECT * FROM APP.PATIENT " +
            "WHERE " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private String strGetPatients =
            "SELECT * FROM APP.PATIENT";
    private String strGetUsers =
            "SELECT * FROM APP.USERS";
    private String strCountPatients =
            "SELECT COUNT(*) FROM APP.PATIENT";
    private String strCountSources =
            "SELECT COUNT(*) FROM APP.SOURCE";
    private String strGetSources =
            "SELECT * FROM APP.SOURCE";
    private String strGetPatientsAndTumours;
    private String strCountPatientsAndTumours;
    private static final String strGetTumour =
            "SELECT * FROM APP.TUMOUR " +
            "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strGetSource =
            "SELECT * FROM APP.Source " +
            "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
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
            "SELECT * FROM APP.DICTIONARY ORDER BY ID";
    private static final String strGetPopulationDatasetEntries =
            "SELECT * FROM APP.PDSET ";
    private static final String strGetPopulationDatasets =
            "SELECT * FROM APP.PDSETS ";
    private static final String strGetNameSexRecords =
            "SELECT * FROM APP.NAMESEX ";
    private static final String strDeletePatientRecord =
            "DELETE FROM APP.PATIENT " +
            "WHERE " + Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strDeleteSourceRecord =
            "DELETE FROM APP.SOURCE " +
            "WHERE " + Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strDeleteTumourRecord =
            "DELETE FROM APP.TUMOUR " +
            "WHERE " + Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME + " = ?";
    private static final String strDeleteDictionaryEntries =
            "DELETE FROM APP.DICTIONARY " +
            "WHERE DICTIONARY = ?";
    private static final String strClearNameSexTable =
            "DELETE FROM APP.NAMESEX";
    private static final String strDeletePopulationDataset =
            "DELETE FROM APP.PDSETS " +
            "WHERE PDS_ID = ?";
    private static final String strDeletePopulationDatasetEntries =
            "DELETE FROM APP.PDSET " +
            "WHERE PDS_ID = ?";
    // The Dynamic ones
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
    private String strGetHighestPatientID;
    private String strGetHighestTumourID;
    private String strGetHighestPatientRecordID;
    private String strGetHighestSourceRecordID;
    private String strEditUser;
    private String strSaveUser;
    /* We don't use tumour record ID...
    private String strGetHighestTumourRecordID;
     */
    private GlobalToolBox globalToolBox;
    private static boolean debug = true;

    private void saveSources(Object tumourID, Set<Source> sources) throws SQLException {
        if (sources != null) {
            String tumourIDSourceTableVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
            for (Source source : sources) {
                source.setVariable(tumourIDSourceTableVariableName, tumourID);
                saveSource(source);
            }
        }
    }

    private void deleteSources(Object tumourID) throws SQLException, DistributedTableDescriptionException, UnknownTableException {
        Set<Source> sources = getSourcesByTumourID(tumourID);
        if (sources != null) {
            for (Source source : sources) {
                if (source != null) {
                    int recordID = (Integer) source.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                    deleteSourceRecord(recordID);
                }
            }
        }
    }
}
