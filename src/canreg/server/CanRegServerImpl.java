/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.server;

import canreg.common.database.User;
import canreg.server.management.UserManagerNew;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Globals.UserRightLevels;
import canreg.common.PersonSearchVariable;
import canreg.common.Tools;
import canreg.common.qualitycontrol.DefaultPersonSearch;
import canreg.common.qualitycontrol.GlobalPersonSearchHandler;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.CanRegDAO;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.server.database.Migrator;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDataset;
import canreg.server.database.RecordLockedException;
import canreg.common.database.Tumour;
import canreg.server.database.UnknownTableException;
import canreg.server.management.SystemDescription;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.drda.NetworkServerControl;
import java.net.InetAddress;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class CanRegServerImpl extends UnicastRemoteObject implements CanRegServerInterface {

    private static boolean debug = true;
    private CanRegDAO db;
    private NetworkServerControl dbServer;
    private SystemDescription systemDescription;
    private String systemCode;
    private SystemSettings systemSettings;
    private PersonSearcher personSearcher;
    private Properties appInfoProperties;
    private UserManagerNew userManager;
    private Map<String, GlobalPersonSearchHandler> activePersonSearchers;
    private String patientRecordIDvariableName;
    private GlobalToolBox serverToolbox;
    private TrayIcon trayIcon;
    //<ictl.co>
    private int port;
    //</ictl.co>

    /**
     * 
     * @param systemCode
     * @throws java.rmi.RemoteException
     */
    public CanRegServerImpl(String systemCode/*<ictl.co>*/, int port/*<ictl.co>*/) throws RemoteException {
        //<ictl.co>
        super(port);
        this.port = port;
        //</ictl.co>
        Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.INFO, "Java version: {0}", System.getProperty("java.version"));
                
        this.systemCode = systemCode;

        // If we can we add a tray icon to show that the CanReg server is running.
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            java.net.URL imageURL = CanRegServerImpl.class.getResource("resources/LogoBetaNew32x32.png");
            if (imageURL != null) {
                Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
                trayIcon = new TrayIcon(image, "CanReg5 server starting...", null);
                trayIcon.setImageAutoSize(true);
                try {
                    tray.add(trayIcon);
                } catch (AWTException ex) {
                    Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        appInfoProperties = new Properties();
        InputStream in = null;
        //
        // load properties file
        //

        //
        // get Application information
        //
        in = getClass().getResourceAsStream(Globals.APPINFO_PROPERTIES_PATH);
        try {
            appInfoProperties.load(in);
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        // end-try-catch

        // Step one load the system definition...
        if (!initSystemDefinition()) {
            throw new RemoteException("Faulty system definitions...");
        }
        setTrayIconToolTip("CanReg5 server " + systemCode + " definitions read and initialized...");
        // Step two: start the database...
        if (!initDataBase()) {
            throw new RemoteException("Cannot initialize database...");
        }

        setTrayIconToolTip("CanReg5 server " + systemCode + " database initialized...");
        try {
            systemSettings = new SystemSettings(systemCode + "settings.xml");
        } catch (IOException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        setTrayIconToolTip("CanReg5 server " + systemCode + " database initialized...");

        // Step three: set up some variables
        serverToolbox = new GlobalToolBox(getDatabseDescription());
        patientRecordIDvariableName = serverToolbox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();

        // temp!
        // db.setSystemPropery("DATABASE_VERSION", "5.00.05");

        // migrate the database if necessary
        Migrator migrator = new Migrator(getCanRegVersion(), db);
        setTrayIconToolTip("Migrating CanReg5 " + systemCode + " database to newest version specification...");
        migrator.migrate();

        // Step four: initiate the quality controllers
        personSearcher = new DefaultPersonSearch(
                Tools.getVariableListElements(
                systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
        PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE);
        personSearcher.setSearchVariables(searchVariables);
        personSearcher.setThreshold(Tools.getPersonSearchMinimumMatch(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));

        activePersonSearchers = new LinkedHashMap<String, GlobalPersonSearchHandler>();

        setTrayIconToolTip("CanReg5 " + systemCode + " server quality controllers initialized...");

        // Step five: start the user manager
        userManager = new UserManagerNew(db);
        userManager.writePasswordsToFile();

        setTrayIconToolTip("CanReg5 " + systemCode + " server user manager started initialized...");

        // Update the tooltip now that everything is up and running...
        setTrayIconToolTip("CanReg5 server " + systemDescription.getSystemName() + " (" + systemCode + ") running");
        displayTrayIconPopUpMessage("Server running", "CanReg5 server " + systemDescription.getSystemName() + " (" + systemCode + ") running", MessageType.INFO);
    }

    // Initialize the database connection
    private boolean initDataBase() throws RemoteException {
        boolean success = false;
        boolean connected = false;

        // Connect to the database
        db = new CanRegDAO(systemCode, systemDescription.getSystemDescriptionDocument());
        try {
            connected = db.connect();
        } catch (SQLException ex) {
            // System.out.println(ex.getCause());
            // System.out.println("Error-code: " + ex.getErrorCode());
            //  System.out.println("SQL-state:" + ex.getSQLState());
            // If we reach this step and get a SQLexception - try with passord
            String password = JOptionPane.showInputDialog("Please enter database boot password:");
            if (password != null) {
                connected = initDataBase(password);
                if (!connected) {
                    Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }

        if (connected && db != null) {
            success = true;
        }

        return success;
    }

    // Initialize the database connection with password
    private boolean initDataBase(String password) throws RemoteException {
        boolean success = false;
        boolean connected = false;

        // Connect to the database
        db = new CanRegDAO(systemCode, systemDescription.getSystemDescriptionDocument());
        try {
            connected = db.connectWithBootPassword(password.toCharArray());
        } catch (SQLException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        if (connected && db != null) {
            success = true;
        }
        return success;
    }

    // Initialize the system description object
    private boolean initSystemDefinition() {
        boolean success = false;

        // Load the system description object
        systemDescription = new SystemDescription(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + systemCode + ".xml");

        if (systemDescription.getSystemDescriptionDocument() != null) {
            success = true;
        }
        return success;
    }

    // This lets one connect to the CanReg database from other programs
    // to connect from an ODBC compliant program you have to "prepare" the
    // connection using db2-tools available here: 
    // http://www-1.ibm.com/support/docview.wss?rs=71&uid=swg21256059
    // Download and install the DB2 Run-Time Client
    // Windows: https://www6.software.ibm.com/dl/rtcl/rtcl-p
    // Linux: https://www6.software.ibm.com/dl/cloudscape/cloudscape-i?S_PKG=db2rtlin_ww
    //
    // Afterwards:
    // Linux: http://www.ibm.com/developerworks/db2/library/techarticle/dm-0409cline2/readme_linux.txt
    // Windows: http://www.ibm.com/developerworks/db2/library/techarticle/dm-0409cline2/readme_win.txt
    //
    /**
     * 
     */
    @Override
    public void startNetworkDBServer() {
        try {
            debugOut("Start Network DB Server.");
            dbServer = new NetworkServerControl(InetAddress.getByName("localhost"), 1528);
            dbServer.start(null);
        } catch (Exception ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     */
    @Override
    public void stopNetworkDBServer() {
        try {
            if (dbServer != null) {
                debugOut("Stop Network DB Server.");
                dbServer.shutdown();
            }
        } catch (Exception ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public final Document getDatabseDescription() throws RemoteException, SecurityException {
        return systemDescription.getSystemDescriptionDocument();
    }

    /**
     * 
     * @param user
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void addUser(User user) throws RemoteException, SecurityException {
        userManager.addUser(user);
    }

    /**
     * 
     * @param user
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void removeUser(User user) throws RemoteException, SecurityException {
        userManager.removeUser(user);
    }

    /**
     * 
     * @param username
     * @param password
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void setUserPassword(String username, String password) throws RemoteException, SecurityException {
        for (User user : listUsers()) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                user.setPassword(password.toCharArray());
                saveUser(user);
            }
        }
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String getCanRegSystemName() throws RemoteException, SecurityException {
        String name = null;

        if (systemDescription != null) {
            name = systemDescription.getSystemName();
        }
        return name;
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        return userManager.listCurrentUsers();
    }

    /**
     *
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public List<User> listUsers() throws RemoteException, SecurityException {
        return userManager.listUsers();
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void userLoggedIn(String username)
            throws RemoteException, SecurityException {
        userManager.userLoggedIn(username);
        displayTrayIconPopUpMessage("User logged in", "User " + username + " logged in.", MessageType.INFO);
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void userLoggedOut(String username)
            throws RemoteException, SecurityException {
        userManager.userLoggedOut(username);
        displayTrayIconPopUpMessage("User logged out", "User " + username + " logged out.", MessageType.INFO);
    }

    // 
    /**
     * For testing purposes only - not secure enough...
     * Not used!
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        return db;
    }

    private void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.INFO, msg);
        }
    }
    // add and remove records

    /**
     * 
     * @param patient
     * @return
     * @throws SQLException
     */
    @Override
    public int savePatient(Patient patient) throws SQLException {
        return db.savePatient(patient);
    }

    /**
     * 
     * @param tumour
     * @return
     * @throws SQLException
     * @throws RecordLockedException
     */
    @Override
    public int saveTumour(Tumour tumour) throws SQLException, RecordLockedException {
        return db.saveTumour(tumour);

    }

    /**
     * 
     * @param dictionaryEntry
     * @return
     */
    @Override
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        return db.saveDictionaryEntry(dictionaryEntry);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String performBackup() throws RemoteException, SecurityException {
        systemSettings.setDateOfLastbackup(new Date());
        systemSettings.writeSettings();
        return db.performBackup();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public final String getCanRegVersion() throws RemoteException, SecurityException {
        String versionString = "";
        for (String part : Globals.versionStringParts) {
            versionString += appInfoProperties.getProperty(part);
        }
        return versionString;
    }

    /**
     * 
     * @param dictionaryID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        return db.deleteDictionaryEntries(dictionaryID);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        return db.getDictionary();
    }

    /**
     * 
     * @param path
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        // TODO: Add functionality to log off automatically after successful restore...
        return db.restoreFromBackup(path);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public InetAddress getIPAddress() throws RemoteException, SecurityException {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addr;
    }

    /**
     * 
     * @param filter
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.sql.SQLException
     * @throws UnknownTableException
     * @throws DistributedTableDescriptionException
     */
    @Override
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws RemoteException, SecurityException, SQLException, UnknownTableException, DistributedTableDescriptionException {
        return db.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, tableName, db.generateResultSetID());
    }

    /**
     * 
     * @param patientID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    private DatabaseRecord getPatient(int patientID, boolean lock) throws RemoteException, SecurityException, RecordLockedException {
        return getRecord(patientID, Globals.PATIENT_TABLE_NAME, lock);
    }

    /**
     * 
     * @param recordID
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException
     */
    @Override
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock) throws RemoteException, SecurityException, RecordLockedException {
        return db.getRecord(recordID, tableName, lock);
    }

    /**
     *
     * @param recordID
     * @param tableName
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public void releaseRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        db.releaseRecord(recordID, tableName);
    }

    /**
     * 
     * @param patient
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException 
     */
    @Override
    public synchronized void editPatient(Patient patient) throws RemoteException, SecurityException, RecordLockedException {
        db.editPatient(patient);
    }

    /**
     * 
     * @param tumour
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException
     */
    @Override
    public void editTumour(Tumour tumour) throws RemoteException, SecurityException, RecordLockedException {
        db.editTumour(tumour);
    }

    /**
     * 
     * @param resultSetID
     * @param from
     * @param to
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException {
        Object[][] rows = null;
        try {
            rows = db.retrieveRows(resultSetID, from, to);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RemoteException("Retrieve rows failed: " + ex.getMessage());
        }
        return rows;
    }

    /**
     * 
     * @param resultSetID
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws SQLException
     */
    @Override
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException, SQLException {
        db.releaseResultSet(resultSetID);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        return db.getPopulationDatasets();
    }

    /**
     * 
     * @param pds
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        return db.saveNewPopulationDataset(pds);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        return systemSettings.getDateOfLastBackUp();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {
        return db.getNameSexTables();
    }

    /**
     * 
     * @param nameSexRecord
     * @param replace
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace) throws RemoteException, SecurityException {
        return db.saveNameSexRecord(nameSexRecord, replace);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        return db.clearNameSexTable();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        // This should never be called but be taken care of by the proxy...
        return Globals.UserRightLevels.NOT_LOGGED_IN;
    }

    /**
     * 
     * @param searcher
     * @param rangeStart 
     * @param rangeEnd
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public synchronized String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException {
        DistributedTableDescription dataDescription;
        GlobalPersonSearchHandler gpsh = new GlobalPersonSearchHandler();
        String resultSetID = null;

        try {
            if (searcher == null) {
                searcher = personSearcher;
            }

            DatabaseFilter filter = new DatabaseFilter();
            filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);

            dataDescription = getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
            gpsh.setAllPatientRecordIDs(retrieveRows(dataDescription.getResultSetID(), 0, dataDescription.getRowCount()));
            releaseResultSet(dataDescription.getResultSetID());
            DatabaseIndexesListElement dbile = new DatabaseIndexesListElement(null);
            dbile.setDatabaseTableName(Globals.PATIENT_TABLE_NAME);
            filter.setRangeDatabaseIndexedListElement(dbile);
            filter.setRangeDatabaseVariablesListElement(serverToolbox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()));
            filter.setRangeStart(rangeStart);
            filter.setRangeEnd(rangeEnd);
            dataDescription = getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
            resultSetID = dataDescription.getResultSetID();
            gpsh.setDistributedTableDescription(dataDescription);
            gpsh.setPersonSearcher(searcher);
            gpsh.setPosition(0);
            gpsh.setPatientRecordIDsWithinRange(retrieveRows(resultSetID, 0, dataDescription.getRowCount()));
            releaseResultSet(resultSetID);

            activePersonSearchers.put(resultSetID, gpsh);
            // releaseResultSet(resultSetID);
        } catch (SQLException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownTableException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultSetID;
    }

    /**
     *
     * @param idString
     * @return
     * @throws SecurityException
     * @throws RemoteException
     * @throws Exception
     */
    @Override
    public synchronized Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, RecordLockedException {
        Map<String, Map<String, Float>> patientIDScorePatientIDMap = new TreeMap<String, Map<String, Float>>();
        GlobalPersonSearchHandler globalPersonSearchHandler = activePersonSearchers.get(idString);
        if (globalPersonSearchHandler != null) {
            PersonSearcher searcher = globalPersonSearchHandler.getPersonSearcher();

            int startRow = globalPersonSearchHandler.getPosition();
            int endRow = startRow + Globals.GLOBAL_PERSON_SEARCH_STEP_SIZE;
            globalPersonSearchHandler.setPosition(endRow);

            if (startRow >= globalPersonSearchHandler.getDistributedTableDescription().getRowCount()) {
                releasePersonSearcher(idString);
                patientIDScorePatientIDMap = null;
            } else {
                // Object[][] rowData = retrieveRows(idString, startRow, endRow);
                Object[][] rowData = globalPersonSearchHandler.getPatientRecordIDsWithinRange();
                for (int row = startRow; row < endRow && row < rowData.length; row++) {
                    int patientIDA = (Integer) rowData[row][0];
                    Patient patientA = (Patient) getPatient(patientIDA, false);
                    // Map<String, Float> patientIDScoreMap = performPersonSearch(patientA, searcher, globalPersonSearchHandler.getDistributedTableDescription());
                    Map<String, Float> patientIDScoreMap = performPersonSearch(patientA, searcher, globalPersonSearchHandler.getAllPatientRecordIDs());
                    if (patientIDScoreMap.size() > 0) {
                        patientIDScorePatientIDMap.put((String) patientA.getVariable(patientRecordIDvariableName), patientIDScoreMap);
                    }
                }
            }
        } else {
            patientIDScorePatientIDMap = null;
        }
        return patientIDScorePatientIDMap;
    }

    private synchronized void releasePersonSearcher(String idString) {
        try {
            releaseResultSet(idString);
            activePersonSearchers.remove(idString);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param idString
     */
    @Override
    public synchronized void interuptGlobalPersonSearch(String idString) {
        releasePersonSearcher(idString);
    }

    /**
     * 
     * @param patient
     * @param searcher
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public synchronized Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException {
        DatabaseFilter filter = new DatabaseFilter();
        filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);
        DistributedTableDescription dataDescription;
        Map<String, Float> patientIDScoreMap = null;
        if (searcher == null) {
            if (personSearcher == null) {
                personSearcher = new DefaultPersonSearch(
                        Tools.getVariableListElements(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
                PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE);
                personSearcher.setSearchVariables(searchVariables);
                personSearcher.setThreshold(Tools.getPersonSearchMinimumMatch(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
            }
            searcher = personSearcher;
        }
        try {
            dataDescription = db.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, Globals.PATIENT_TABLE_NAME, db.generateResultSetID());
            Object[][] rowData = retrieveRows(dataDescription.getResultSetID(), 0, dataDescription.getRowCount() - 1);
            patientIDScoreMap = performPersonSearch(patient, searcher, rowData);
            releaseResultSet(dataDescription.getResultSetID());
        } catch (SQLException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownTableException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientIDScoreMap;
    }

    private Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher, Object[][] rowData) throws RemoteException, SecurityException {
        Map<String, Float> patientIDScoreMap = new TreeMap<String, Float>();

        Patient patientB;

        Object patientIDAObject = patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);

        int patientIDA;
        if (patientIDAObject != null) {
            patientIDA = (Integer) patientIDAObject;
        } else {
            patientIDA = -1;
        }

        float threshold = searcher.getThreshold();
        try {
            for (int row = 0; row < rowData.length; row++) {
                Object[] r = rowData[row];
                int patientIDB = (Integer) r[0];
                if (patientIDB != patientIDA) {
                    try {
                        patientB = (Patient) getPatient(patientIDB, false);
                        float score = searcher.compare(patient, patientB);
                        if (score > threshold) {
                            patientIDScoreMap.put((String) patientB.getVariable(patientRecordIDvariableName), score);
                            // debugOut("Found patient id: " + patientB.getVariable(patientRecordIDvariableName) + ", score: " + score + "%");
                        } else {
                            // debugOut("Not found " + patientB.getVariable(patientRecordIDvariableName) + " " + score);
                        }
                    } catch (RecordLockedException ex) {
                        Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientIDScoreMap;
    }

    /**
     *
     * @param id
     * @param tableName
     * @return
     * @throws RemoteException
     * @throws SecurityException
     * @throws RecordLockedException
     * @throws SQLException
     */
    @Override
    public synchronized boolean deleteRecord(int id, String tableName) throws RemoteException, SecurityException, RecordLockedException, SQLException {
        boolean success = false;
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            success = db.deleteTumourRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            success = db.deletePatientRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            success = db.deleteSourceRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.USERS_TABLE_NAME)) {
            success = db.deleteRecord(id, Globals.USERS_TABLE_NAME);
            userManager.writePasswordsToFile();
        } else {
            success = db.deleteRecord(id, tableName);
        }
        return success;
    }

    /**
     *
     * @param populationDatasetID
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public synchronized boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException {
        return db.deletePopulationDataSet(populationDatasetID);
    }

    /**
     *
     * @param user
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public synchronized int saveUser(User user) throws RemoteException, SecurityException {
        int id = db.saveUser(user);
        userManager.writePasswordsToFile();
        return id;
    }

    /**
     *
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException {
        return db.getDatabaseStats();
    }

    private void setTrayIconToolTip(String toolTip) {
        if (trayIcon != null) {
            trayIcon.setToolTip(toolTip);
        }
    }

    private void displayTrayIconPopUpMessage(String caption, String text, MessageType messageType) {
        if (trayIcon != null) {
            trayIcon.displayMessage(caption, text, messageType);
        }
    }

    @Override
    public void shutDownServer() throws RemoteException, SecurityException {
        if (trayIcon != null) {
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }
    }

    @Override
    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray) throws RemoteException, SecurityException {
        boolean success = false;
        try {
            success = db.encryptDatabase(newPasswordArray, oldPasswordArray);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    //<ictl.co>

    public Integer getPort() throws RemoteException, SecurityException {
        return port;
    }

    //<ictl.co>
}
