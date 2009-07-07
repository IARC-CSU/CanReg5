/*
 * CanRegClientApp.java
 */
package canreg.client;

import cachingtableapi.DistributedTableDescription;
import canreg.client.dataentry.Relation;
import canreg.client.gui.CanRegClientView;
import canreg.client.dataentry.ImportOptions;
import canreg.common.DatabaseFilter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.Checker;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.exceptions.WrongCanRegVersionException;
import canreg.server.CanRegLoginInterface;
import canreg.server.CanRegServerInterface;
import canreg.server.database.User;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Dictionary;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.NameSexRecord;
import canreg.server.database.Patient;
import canreg.server.database.PopulationDataset;
import canreg.server.database.Tumour;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;

/**
 * The main class of the application.
 */
public class CanRegClientApp extends SingleFrameApplication {

    private CanRegServerInterface server;
    static boolean debug = true;
    private boolean canregServerRunningInThisThread = false;
    private String systemName = null;
    private String username = null;
    private static LocalSettings localSettings;
    private DateFormat dateFormat;
    /**
     * 
     */
    public boolean loggedIn = false;
    private CanRegClientView canRegClientView;
    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private boolean canregServerRunningOnThisMachine = false;
    private GlobalToolBox globalToolBox;
    private Checker checker;
    private Converter converter;
    private Properties appInfoProperties;
    private String canRegSystemVersionString;

    public void changePassword(String encrypted) throws SecurityException, RemoteException {
        server.setUserPassword(null, encrypted);
    }

    public boolean deletePopulationDataset(int populationDatasetID) throws SQLException, RemoteException, SecurityException {
        return server.deletePopulationDataset(populationDatasetID);
    }

    public Patient getPatientRecord(String requestedPatientRecordID) throws SQLException, RemoteException, SecurityException, Exception {
        return (Patient) getRecordByID(requestedPatientRecordID, Globals.PATIENT_TABLE_NAME);
    }

    public Patient getPatientRecordByID(String requestedPatientID) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Tumour getTumourRecord(String requestedPatientRecordID) throws SQLException, RemoteException, SecurityException, Exception {
        return (Tumour) getRecordByID(requestedPatientRecordID, Globals.TUMOUR_TABLE_NAME);
    }

    public void saveUser(User user) throws SQLException, RemoteException, SecurityException {
        server.saveUser(user);
    }

    private DatabaseRecord getRecordByID(String recordID, String tableName) throws SQLException, RemoteException, SecurityException, Exception {
        String recordIDVariableName = null;
        String databaseRecordIDVariableName = null;
        if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
            databaseRecordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordID.toString()).getDatabaseVariableName();
            databaseRecordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        }

        DatabaseFilter filter = new DatabaseFilter();
        filter.setFilterString(recordIDVariableName + " = '" + recordID + "' ");
        DistributedTableDescription distributedTableDescription;
        Object[][] rows;
        DatabaseRecord record = null;

        distributedTableDescription = getDistributedTableDescription(filter, tableName);
        int numberOfRecords = distributedTableDescription.getRowCount();

        rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords);
        releaseResultSet(distributedTableDescription.getResultSetID());
        if (rows.length > 0) {
            String[] columnNames = distributedTableDescription.getColumnNames();
            int ids[] = new int[numberOfRecords];
            boolean found = false;
            int idColumnNumber = 0;

            while (!found && idColumnNumber < columnNames.length) {
                found = columnNames[idColumnNumber++].equalsIgnoreCase(databaseRecordIDVariableName);
            }
            if (found) {
                idColumnNumber--;
                int id = (Integer) rows[0][idColumnNumber];
                record = getRecord(id, Globals.PATIENT_TABLE_NAME);
            }
        }
        return record;
    }

    /**
     * Save a new population dataset on the server
     * @param pds Population Data Set to save
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void saveNewPopulationDataset(PopulationDataset pds) throws SecurityException, RemoteException {
        int populationDatasetID = server.saveNewPopulationDataset(pds);
        pds.setPopulationDatasetID(populationDatasetID);
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        applyPreferences();

        InputStream in = null;
        appInfoProperties = new Properties();
        //
        // load properties file
        //
        try {
            //
            // get Application information
            //
            in = getClass().getResourceAsStream(Globals.APPINFO_PROPERTIES_PATH);
            appInfoProperties.load(in);
            in.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } // end-try-catch

        canRegSystemVersionString = "";
        for (String part : Globals.versionStringParts) {
            canRegSystemVersionString += appInfoProperties.getProperty(part);
        }

        String versionString = canRegSystemVersionString;
        versionString += "b" + appInfoProperties.getProperty("program.BUILDNUM");
        versionString += " (" + appInfoProperties.getProperty("program.BUILDDATE") + ")";
        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, "CanReg version: " + versionString);

        canRegClientView = new CanRegClientView(this);

        show(canRegClientView);

        ExitListener maybeExit = new ExitListener() {

            public boolean canExit(EventObject e) {
                int option = JOptionPane.NO_OPTION;
                if (!isCanregServerRunningInThisThread()) {
                    option = JOptionPane.showConfirmDialog(null, "Really exit?", "Really exit?", JOptionPane.YES_NO_OPTION);
                } else {
                    try {
                        if (loggedIn) {
                            int users = listUsersLoggedIn().length - 1;
                            option = JOptionPane.showConfirmDialog(null, "Really exit?\n" + users + " other user(s) connected to this server will be disconnected.", "Really exit?", JOptionPane.YES_NO_OPTION);
                        } else {
                            option = JOptionPane.showConfirmDialog(null, "Really exit?\nOther user(s) connected to this server will be disconnected.", "Really exit?", JOptionPane.YES_NO_OPTION);
                        }
                    } catch (RemoteException ex) {
                    }
                }
                return option == JOptionPane.YES_OPTION;
            }

            public void willExit(EventObject e) {
                if (loggedIn) {
                    logOut();
                }
            }
        };

        dateFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING);

        addExitListener(maybeExit);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     * @param root 
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of CanRegClientApp
     */
    public static CanRegClientApp getApplication() {
        return Application.getInstance(CanRegClientApp.class);
    }

    /**
     * Initialize the application
     */
    public static void init() {
        // Testing the environment - disabled
        // canreg.common.Tools.testEnvironment();
        // Initialize the user settings
        try {
            localSettings = new LocalSettings("settings.xml");
            initializeLookAndFeels();
            // Locale.setDefault(localSettings.getLocale());
            // Initialize logger
            Handler fh = new FileHandler(Globals.LOGFILE_PATTERN);
            Logger.getLogger("").addHandler(fh);
            Logger.getLogger("canreg").setLevel(Level.parse(Globals.LOG_LEVEL));
        } catch (IOException ioe) {
            debugOut(ioe.getLocalizedMessage());
        }
    }

    /**
     * Test the connection to a server
     * @param serverObjectString The address to a server
     * @return name of the system, null if not able to connect
     */
    public String testConnection(String serverObjectString) {
        debugOut("testing the connecting to server=" + serverObjectString + ".");
        CanRegLoginInterface loginServer = null;
        String sysName = null;
        try {
            //authenticate credentials
            loginServer = (CanRegLoginInterface) Naming.lookup(serverObjectString);
            //login object received
            // try to get system name
            sysName = loginServer.getSystemName();
        } catch (Exception e) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, null, e);
        // System.exit(0);
        }
        return sysName;
    }

    /**
     * Log on to the CanReg system and set up the server connection.
     *
     * @param serverObjectString The address to a server
     * @param username The username of the user
     * @param password The password of the user
     * @return CanReg System's name if successfull - null if not
     * @throws javax.security.auth.login.LoginException
     * @throws java.lang.NullPointerException
     * @throws java.rmi.NotBoundException
     * @throws java.net.MalformedURLException
     * @throws java.rmi.RemoteException
     * @throws java.net.UnknownHostException
     * @throws canreg.exceptions.WrongCanRegVersionException
     */
    public String login(String serverObjectString, String username, char[] password) throws LoginException, NullPointerException, NotBoundException, MalformedURLException, RemoteException, UnknownHostException, WrongCanRegVersionException {
        this.username = username;
        debugOut("connecting to server=" + serverObjectString + " as " + username + ".");
        CanRegLoginInterface loginServer = null;

        //authenticate credentials
        loginServer = (CanRegLoginInterface) Naming.lookup(serverObjectString);
        //login object received

        if (!canRegSystemVersionString.trim().equalsIgnoreCase(loginServer.getSystemVersion().trim())) {
            throw (new WrongCanRegVersionException("Server: " + loginServer.getSystemVersion() + ", Client: " + canRegSystemVersionString));
        }
        //do the login 
        debugOut("ATTEMPTING LOGIN");
        server = (CanRegServerInterface) loginServer.login(username, new String(password));
        if (server != null) {
            // See if server version of CanReg matches the 


            debugOut("LOGIN SUCCESSFULL");
            // This should work...
            systemName = server.getCanRegSystemName();

            loggedIn = true;
            doc = server.getDatabseDescription();
            dictionary = server.getDictionary();
            globalToolBox = new GlobalToolBox(doc);

            canregServerRunningOnThisMachine = InetAddress.getLocalHost().
                    equals(server.getIPAddress());
            Globals.UserRightLevels i = getUserRightLevel();
            canRegClientView.setUserRightsLevel(i);

            checker = new Checker(globalToolBox);
            converter = new Converter(globalToolBox);

            return systemName;
        } else {
            return null;
        }
    }

    public String getSystemName() {
        return systemName;
    }

    /**
     * Get list of users logged in to the CanReg server
     * @return List of users logged in
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listUsersLoggedIn() throws RemoteException, SecurityException {
        return server.listCurrentUsers();
    }

    /**
     * Simple console trace to system.out for debug purposes only.
     *
     * @param message the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, msg);
        }
    }

    /**
     * Start the database server
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void startDatabaseServer() throws RemoteException, SecurityException {
        server.startNetworkDBServer();
    }

    /**
     * Stop the database server
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void stopDatabaseServer() throws RemoteException, SecurityException {
        server.stopNetworkDBServer();
    }

    /**
     * 
     * @return
     */
    public LocalSettings getLocalSettings() {
        return localSettings;
    }

    /**
     * 
     * @return
     */
    public Document getDatabseDescription() {
        return doc;
    }

    /**
     * 
     * @return
     */
    public Map<Integer, Dictionary> getDictionary() {
        return dictionary;
    }

    /**
     * 
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void refreshDictionary() throws SecurityException, RemoteException {
        dictionary = server.getDictionary();
    }

    /**
     * 
     */
    public void applyPreferences() {
        Locale.setDefault(localSettings.getLocale());
        if (localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY).length() > 0) {
            try {
                if (localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY).equalsIgnoreCase("System")) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } else {
                    UIManager.setLookAndFeel(localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY));
                }
            // Locale.setDefault(localSettings.getLocale());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }

    /**
     * 
     * @param task
     * @param doc
     * @param map
     * @param file
     * @param io
     * @throws java.rmi.RemoteException
     */
    public boolean importFile(Task<Object, Void> task, Document doc, List<Relation> map, File file, ImportOptions io) throws RemoteException, SQLException {
        return canreg.client.dataentry.Import.importFile(task, doc, map, file, server, io);
    }

    /**
     * 
     * @param canregServerRunningInThisThread
     */
    public void setCanregServerRunningInThisThread(boolean canregServerRunningInThisThread) {
        this.canregServerRunningInThisThread = canregServerRunningInThisThread;
    }

    /**
     * 
     * @param evt
     */
    @Action
    @Override
    public void quit(ActionEvent evt) {
        try {
            // logOut();
            super.quit(evt);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     */
    public void logOut() {
        try {
            server.userLoggedOut(username);
            server = null;
            systemName = "";
            loggedIn = false;
            canRegClientView.setUserRightsLevel(Globals.UserRightLevels.NOT_LOGGED_IN);
            localSettings.writeSettings();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @return
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * 
     * @return
     */
    public Globals.UserRightLevels getUserRightLevel() {
        Globals.UserRightLevels level = Globals.UserRightLevels.NOT_LOGGED_IN;
        try {
            level = server.getUserRightLevel();
        // For now all users are supervisors
        // return Globals.UserRightLevels.SUPERVISOR;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        // For now all users are supervisors
        // return Globals.UserRightLevels.SUPERVISOR;
        return level;
    }

    /**
     * 
     * @return
     */
    public String performBackup() {
        String path = null;
        try {
            path = server.performBackup();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return path;
    }

    /**
     * 
     * @param path
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public String restoreBackup(String path) throws SecurityException, RemoteException {
        String message = server.restoreFromBackup(path);
        // Refresh the dictionary
        refreshDictionary();
        return message;
    }

    /**
     * 
     * @return
     */
    public boolean isCanregServerRunningInThisThread() {
        return canregServerRunningInThisThread;
    }

    /**
     * 
     * @return
     */
    public boolean isCanRegServerRunningOnThisMachine() {
        return canregServerRunningOnThisMachine;
    }

    /**
     * 
     * @param filter
     * @param tableName
     * @return
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.lang.Exception
     */
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws SQLException, RemoteException, SecurityException, Exception {
        return server.getDistributedTableDescription(filter, tableName);
    }

    /**
     * 
     * @param recordID
     * @param tableName
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public DatabaseRecord getRecord(int recordID, String tableName) throws SecurityException, RemoteException {
        return server.getRecord(recordID, tableName);
    }



    /**
     * 
     * @param databaseRecord
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public int saveRecord(DatabaseRecord databaseRecord) throws SecurityException, RemoteException, SQLException {
        int recordNumber = -1;
        if (databaseRecord instanceof Patient) {
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdatedBy.toString()).getDatabaseVariableName(), username);
            recordNumber = server.savePatient((Patient) databaseRecord);
        } else if (databaseRecord instanceof Tumour) {
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdatedBy.toString()).getDatabaseVariableName(), username);
            recordNumber = server.saveTumour((Tumour) databaseRecord);
        } else if (databaseRecord instanceof NameSexRecord) {
            recordNumber = server.saveNameSexRecord((NameSexRecord) databaseRecord);
        } else if (databaseRecord instanceof PopulationDataset) {
            // recordNumber = server.savePopulationDataset((PopulationDataset) databaseRecord);
        }
        return recordNumber;
    }

    /**
     * 
     * @param databaseRecord
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void editRecord(DatabaseRecord databaseRecord) throws SecurityException, RemoteException {
        if (databaseRecord instanceof Patient) {
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdatedBy.toString()).getDatabaseVariableName(), username);
            server.editPatient((Patient) databaseRecord);
        } else if (databaseRecord instanceof Tumour) {
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
            databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdatedBy.toString()).getDatabaseVariableName(), username);
            server.editTumour((Tumour) databaseRecord);
        }
    }

    public boolean deleteRecord(int id, String tableName) throws SecurityException, RemoteException {
        return server.deleteRecord(id, tableName);
    }

    /**
     * 
     * @param dictionaryID
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public boolean deleteDictionaryEntries(int dictionaryID) throws SecurityException, RemoteException {
        return server.deleteDictionaryEntries(dictionaryID);
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public boolean clearNameSexTable() throws SecurityException, RemoteException {
        return server.clearNameSexTable();
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Map<String, Integer> getNameSexTables() throws SecurityException, RemoteException {
        return server.getNameSexTables();
    }

    /**
     * 
     * @param entry
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void saveDictionaryEntry(DictionaryEntry entry) throws SecurityException, RemoteException {
        server.saveDictionaryEntry(entry);
    }

    /**
     * 
     * @param idString
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.sql.SQLException
     * @throws java.lang.Exception
     */
    public DatabaseRecord[] getTumourRecordsBasedOnPatientID(String idString) throws RemoteException, SecurityException, SQLException, Exception {
        DatabaseRecord[] records = null;
        String lookUpTableName = "";
        DatabaseFilter filter = new DatabaseFilter();
        String lookUpColumnName = "";

        lookUpTableName = Globals.TUMOUR_TABLE_NAME;
        filter.setFilterString(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName() + " = '" + idString + "'");

        lookUpColumnName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        Object[][] rows;

        DistributedTableDescription distributedTableDescription = CanRegClientApp.getApplication().getDistributedTableDescription(filter, lookUpTableName);
        int numberOfRecords = distributedTableDescription.getRowCount();

        // Retrieve all rows
        rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords);

        String[] columnNames = distributedTableDescription.getColumnNames();

        boolean found = false;
        int idColumnNumber = 0;
        while (!found && idColumnNumber < columnNames.length) {
            found = columnNames[idColumnNumber++].equalsIgnoreCase(lookUpColumnName);
        }

        if (found) {
            int id;
            records = new DatabaseRecord[numberOfRecords];
            idColumnNumber--;
            for (int j = 0; j < numberOfRecords; j++) {
                id = (Integer) rows[j][idColumnNumber];
                records[j] = getRecord(id, lookUpTableName);
            }
        }

        return records;
    }

    /**
     * Installs the JGoodies Look & Feels, if available, in classpath.
     */
    public static void initializeLookAndFeels() {
        // if in classpath try to load JGoodies Plastic Look & Feel
        try {
            LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
            boolean found = false;
            for (int i = 0; i < lnfs.length; i++) {
                if (lnfs[i].getName().equals("JGoodies Plastic 3D")) {
                    found = true;
                }
            }
            if (!found) {
                UIManager.installLookAndFeel("JGoodies Plastic 3D",
                        "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
            }
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @return
     */
    public GlobalToolBox getGlobalToolBox() {
        return globalToolBox;
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws SecurityException, RemoteException {
        return server.getPopulationDatasets();
    }

    /**
     * 
     * @param resultSetID
     * @param from
     * @param to
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.lang.Exception
     */
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException, Exception {
        return server.retrieveRows(resultSetID, from, to);
    }

    /**
     * 
     * @param resultSetID
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void releaseResultSet(String resultSetID) throws SecurityException, RemoteException {
        server.releaseResultSet(resultSetID);
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Date getDateOfLastBackUp() throws SecurityException, RemoteException {
        if (server != null) {
            return server.getDateOfLastBackUp();
        } else {
            return null;
        }
    }

    /**
     * 
     * @param patient
     * @param searcher
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Map<String, Float> performDuplicateSearch(Patient patient, PersonSearcher searcher) throws SecurityException, RemoteException {
        return server.performPersonSearch(patient, searcher);
    }

    /**
     * 
     * @param patient
     * @param tumour
     * @return
     */
    public LinkedList<CheckResult> performChecks(Patient patient, Tumour tumour) {
        return checker.performChecks(patient, tumour);
    }

    /**
     * 
     * @param conversionName
     * @param patient
     * @param tumour
     * @return
     */
    public ConversionResult[] performConversions(Converter.ConversionName conversionName, Patient patient, Tumour tumour) {
        return converter.performConversion(conversionName, patient, tumour);
    }

    /**
     * 
     * @param searcher
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public String initiateGlobalDuplicateSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws SecurityException, RemoteException {
        return server.initiateGlobalPersonSearch(searcher, rangeStart, rangeEnd);
    }

    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, Exception {
        return server.nextStepGlobalPersonSearch(idString);
    }

    public void interuptGlobalPersonSearch(String idString) throws SecurityException, RemoteException {
        server.interuptGlobalPersonSearch(idString);
    }

    /**
     *
     * @return
     */
    public String getCanRegVersionString() {
        return canRegSystemVersionString;
    }

    public Vector<User> listUsers() throws SecurityException, RemoteException {
        return server.listUsers();
    }

    /**
     * Main method launching the application.
     * @param args
     */
    public static void main(String[] args) {
        init();
        launch(CanRegClientApp.class, args);
    }
}
