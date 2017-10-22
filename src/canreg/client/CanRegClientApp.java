/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017  International Agency for Research on Cancer
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
package canreg.client;

import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.client.dataentry.Relation;
import canreg.client.gui.CanRegClientView;
import canreg.client.dataentry.ImportOptions;
import canreg.client.gui.tools.UITools;
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
import canreg.server.DatabaseStats;
import canreg.server.database.UnknownTableException;
import canreg.common.database.User;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDataset;
import canreg.server.database.RecordLockedException;
import canreg.common.database.Tumour;
import canreg.server.CanRegLoginImpl;
import canreg.server.management.SystemDefinitionConverter;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.rmi.AlreadyBoundException;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import javax.swing.JDesktopPane;
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
    private TreeMap<String, Set<Integer>> locksMap;
    private LockFile lockFile;

    public void changePassword(String encrypted) throws SecurityException, RemoteException {
        try {
            server.setUserPassword(null, encrypted);
        } catch (RemoteException ex) {
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean deletePopulationDataset(int populationDatasetID) throws SQLException, SecurityException, RemoteException {
        try {
            return server.deletePopulationDataset(populationDatasetID);
        } catch (RemoteException ex) {
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public Patient getPatientRecord(String requestedPatientRecordID, boolean lock) throws SQLException, SecurityException, RecordLockedException, UnknownTableException, DistributedTableDescriptionException, RemoteException {
        return (Patient) getRecordByID(requestedPatientRecordID, Globals.PATIENT_TABLE_NAME, lock);
    }

    public Tumour getTumourRecord(String requestedPatientRecordID, boolean lock) throws SQLException, SecurityException, RecordLockedException, UnknownTableException, DistributedTableDescriptionException, RemoteException {
        return (Tumour) getRecordByID(requestedPatientRecordID, Globals.TUMOUR_TABLE_NAME, lock);
    }

    public void saveUser(User user) throws SQLException, RemoteException, SecurityException {
        server.saveUser(user);
    }

    private DatabaseRecord getRecordByID(String recordID, String tableName, boolean lock) throws SQLException, SecurityException, RecordLockedException, UnknownTableException, DistributedTableDescriptionException, RemoteException {
        try {
            String recordIDVariableName = null;
            String databaseRecordIDVariableName = null;
            if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
                databaseRecordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
                databaseRecordIDVariableName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
            } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                recordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName();
                databaseRecordIDVariableName = Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME;
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
                    record = getRecord(id, tableName, lock);
                }
            }
            return record;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    /**
     * Save a new population dataset on the server
     * @param pds Population Data Set to save
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void saveNewPopulationDataset(PopulationDataset pds) throws SecurityException, RemoteException {
        try {
            int populationDatasetID = server.saveNewPopulationDataset(pds);
            pds.setPopulationDatasetID(populationDatasetID);
        } catch (RemoteException ex) {
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("APPLYING PREFERENCES..."), 60);
        applyPreferences();

        InputStream in;
        splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("LOADING APPLICATION PROPERTIES..."), 80);
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
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } // end-try-catch

        canRegSystemVersionString = "";
        for (String part : Globals.versionStringParts) {
            canRegSystemVersionString += appInfoProperties.getProperty(part);
        }

        String versionString = canRegSystemVersionString;
        versionString += "-build" + appInfoProperties.getProperty("program.BUILDNUM");
        versionString += " (" + appInfoProperties.getProperty("program.BUILDDATE") + ")";
        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, "CanReg version: {0}", versionString);

        splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("BUILDING GUI..."), 90);
        canRegClientView = new CanRegClientView(this);

        show(canRegClientView);

        ExitListener maybeExit;
        maybeExit = new ExitListener() {
            
            @Override
            public boolean canExit(EventObject e) {
                int option = JOptionPane.NO_OPTION;
                if (!isCanregServerRunningInThisThread()) {
                    int numberOfRecordsOpen = numberOfRecordsOpen();
                    if (numberOfRecordsOpen > 0) {
                        option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?") + "\n" + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("YOU_HAVE_") + numberOfRecordsOpen + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("_RECORDS_OPEN."), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                    } else {
                        option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                    }
                } else {
                    if (loggedIn) {
                        int users = 0;
                        try {
                            users = listUsersLoggedIn().length - 1;
                        } catch (SecurityException ex) {
                            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RemoteException ex) {
                            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        int numberOfRecordsOpen = numberOfRecordsOpen();
                        if (numberOfRecordsOpen > 0) {
                            option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?") + "\n" + users + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("_OTHERS_WILL_BE_DISCONNECTED.") + "\n" + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("YOU_HAVE_") + numberOfRecordsOpen + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("_RECORDS_OPEN.") + "\n" + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                        } else {
                            option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?") + "\n" + users + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("_OTHERS_WILL_BE_DISCONNECTED."), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                        }
                    } else {
                        option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?") + "\n" + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("_OTHERS_WILL_BE_DISCONNECTED."), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                    }
                }
                return option == JOptionPane.YES_OPTION;
            }

            @Override
            public void willExit(EventObject e) {
                if (loggedIn) {
                    try {
                        logOut();
                    } catch (RemoteException ex) {
                        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (isCanregServerRunningInThisThread() && server != null) {
                    try {
                        server.shutDownServer();
                    } catch (RemoteException ex) {
                        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        dateFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING);

        // initialize this map
        locksMap = new TreeMap<String, Set<Integer>>();

        addExitListener(maybeExit);
        splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("FINISHED."), 100);
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

        // Initialize logger
        try {
            splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("INITIALIZING LOGGER..."), 20);
            Handler fh = new FileHandler(Globals.LOGFILE_PATTERN);
            Logger.getLogger("").addHandler(fh);
            Logger.getLogger("canreg").setLevel(Level.parse(Globals.LOG_LEVEL));
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("LOADING USER SETTINGS..."), 40);
            // Initialize the user settings
            localSettings = new LocalSettings("settings.xml");
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Locale.setDefault(localSettings.getLocale());
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
        } catch (NotBoundException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException e) {
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
     * @return CanReg System's na
     * @throws javax.security.auth.login.LoginException  if successfull - null if not
     * @throws java.lang.NullPointerException
     * @throws java.rmi.NotBoundException
     * @throws java.net.MalformedURLException
     * @throws java.rmi.RemoteException
     * @throws java.net.UnknownHostException
     * @throws canreg.exceptions.WrongCanRegVersionException
     */
    public String loginRMI(String serverObjectString, String username, char[] password) throws LoginException, NullPointerException, NotBoundException, MalformedURLException, RemoteException, UnknownHostException, WrongCanRegVersionException {
        String returnString = null;
        debugOut("connecting to server=" + serverObjectString + " as " + username + ".");
        //authenticate credentials
        CanRegLoginInterface loginServer = getCanRegLoginServer(serverObjectString);
        //login object received
        if (loginServer != null) {
            returnString = login(loginServer, username, password);
        }
        return returnString;
    }

    public String loginDirect(String serverCode, String username, char[] password/*<ictl.co>*/, int port/*</ictl.co>*/) throws LoginException, NullPointerException, NotBoundException, MalformedURLException, RemoteException, UnknownHostException, WrongCanRegVersionException {
        // should this be moved to the loginserver?
        CanRegLoginInterface loginServer = new CanRegLoginImpl(serverCode/*<ictl.co>*/, port/*</ictl.co>*/);
        return login(loginServer, username, password);
    }

    private CanRegLoginInterface getCanRegLoginServer(String serverObjectString) throws NotBoundException, MalformedURLException, RemoteException {
        return (CanRegLoginInterface) Naming.lookup(serverObjectString);
    }

    private String login(CanRegLoginInterface loginServer, String username, char[] password) throws LoginException, NullPointerException, NotBoundException, MalformedURLException, RemoteException, UnknownHostException, WrongCanRegVersionException {
        if (!canRegSystemVersionString.trim().equalsIgnoreCase(loginServer.getSystemVersion().trim())) {
            throw (new WrongCanRegVersionException("Server: " + loginServer.getSystemVersion() + ", Client: " + canRegSystemVersionString));
        }
        //do the loginRMI 
        debugOut("ATTEMPTING LOGIN");
        server = (CanRegServerInterface) loginServer.login(username, password);
        if (server != null) {
            // See if server version of CanReg matches the 

            debugOut("LOGIN SUCCESSFULL");
            // This should work...
            systemName = server.getCanRegSystemName();
            loggedIn = true;
            doc = server.getDatabseDescription();
            dictionary = server.getDictionary();
            globalToolBox = getGlobalToolBox();

            canregServerRunningOnThisMachine = InetAddress.getLocalHost().
                    equals(server.getIPAddress());
            Globals.UserRightLevels i = getUserRightLevel();
            canRegClientView.setUserRightsLevel(i);

            checker = new Checker(globalToolBox.getStandardVariables());
            converter = new Converter(globalToolBox.getStandardVariables());
            this.username = username;

            // create file for locked records
            // locksMap = new TreeMap<String, Set<Integer>>();
            lockFile = new LockFile(systemName + "-" + username);
            locksMap = lockFile.getMap();

            return systemName;
        } else {
            return null;
        }
    }

    public String getSystemName() {
        return systemName;
    }

    public DatabaseStats getDatabaseStats() throws SecurityException, RemoteException {
        try {
            return server.getDatabaseStats();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return null;
    }

    /**
     * Get list of users logged in to the CanReg server
     * @return List of users logged in
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listUsersLoggedIn() throws SecurityException, RemoteException {
        try {
            return server.listCurrentUsers();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return null;
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
    public void startDatabaseServer() throws SecurityException, RemoteException {
        try {
            server.startNetworkDBServer();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    /**
     * Stop the database server
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void stopDatabaseServer() throws SecurityException, RemoteException {
        try {
            server.stopNetworkDBServer();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
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
        try {
            dictionary = server.getDictionary();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    /**
     * 
     */
    public void applyPreferences() {
        Locale.setDefault(localSettings.getLocale());
        String fontNameCode = localSettings.getProperty(LocalSettings.FONT_NAME_KEY);
        String fontSize = localSettings.getProperty(LocalSettings.FONT_SIZE_KEY);

        String fontName = "Tahoma";
        int fontSizeInt = 11;
        
        boolean setFont = false;
        if (fontNameCode != null && fontNameCode.length() > 0 && !fontNameCode.equalsIgnoreCase(LocalSettings.FONT_NAME_DEFAULT)) {
            setFont = true;
            fontName = fontNameCode.trim();
        }
        if (fontSize != null && fontSize.length() > 0 && !fontSize.equalsIgnoreCase(LocalSettings.FONT_SIZE_MEDIUM)) {
            setFont = true;
            if (fontSize.equalsIgnoreCase(LocalSettings.FONT_SIZE_BIG)){
                fontSizeInt = 14;
            } else if (fontSize.equalsIgnoreCase(LocalSettings.FONT_SIZE_SMALL)){
                fontSizeInt = 10;
            }
        }
        if (setFont) {
            setUIFont(fontName, fontSizeInt);
        }
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
     * @return 
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     * @throws canreg.server.database.RecordLockedException
     */
    public boolean importFile(Task<Object, String> task, Document doc, List<Relation> map, File file, ImportOptions io) throws SQLException, SecurityException, RecordLockedException, RemoteException {
    //public boolean importFile(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, Document doc, List<Relation> map, File file, ImportOptions io) throws SQLException, SecurityException, RecordLockedException, RemoteException {
        try {
            return canreg.client.dataentry.Import.importFile(task, doc, map, file, server, io);
            //return canreg.client.dataentry.Convert.importFile(task, doc, map, file, server, io);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return false;
    }

    public boolean importCRFile(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, Document doc, List<Relation> map, File file, ImportOptions io) throws SQLException, SecurityException, RecordLockedException, RemoteException {
        try {
            return canreg.client.dataentry.Convert.importFile(task, doc, map, file, server, io);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return false;
    }

    /**
     *
     * @param task
     * @param doc
     * @param map
     * @param files
     * @param io
     * @return 
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     * @throws canreg.server.database.RecordLockedException
     */
    public boolean importFiles(Task<Object, Void> task, Document doc, List<Relation> map, File[] files, ImportOptions io) throws SQLException, SecurityException, RecordLockedException, RemoteException {
        try {
            return canreg.client.dataentry.Import.importFiles(task, doc, map, files, server, io);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return false;
    }

    /**
     * 
     * @param task
     * @param filepath
     * @param dictionaryfile
     * @param regcode
     * @return 
     */
    //public boolean convertDictionary(Task<Object, String> task, String filepath, String dictionaryfile, String regcode) {
    public boolean convertDictionary(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, String filepath, String dictionaryfile, String regcode) {
       boolean dicsuccess = false;
       dicsuccess =  canreg.client.dataentry.Convert.convertDictionary(task, filepath, dictionaryfile, regcode);
       return dicsuccess;
    }

    /**
     *
     * @param task
     * @param filepath
     * @param datafile
     * @param regcode
     * @return 
     */
    public boolean convertData(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, String filepath, String datafile, String regcode) {
       boolean datsuccess = false;
       try {
            datsuccess =  canreg.client.dataentry.Convert.convertData(task, filepath, datafile, regcode);
        }
        catch(Exception ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return datsuccess;
    }

    /**
     *
     * @param task
     * @param dictionaryfile
     * @return 
     */
    //public Map importDictionary(Task<Object, String> task, String dictionaryfile) {
    public Map importDictionary(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, String dictionaryfile) {
        //boolean dicimpstatus = false;
        Map<Integer, Map<Integer, String>> allErrors;
        allErrors = canreg.client.dataentry.Convert.importDictionary(task, dictionaryfile);
        return allErrors;
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
            // TODO if canreg server is running in this thread - shut it down...
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @throws java.rmi.RemoteException
     */
    public void logOut() throws RemoteException {
        try {
            releaseAllRecordsHeldByThisClient();
            if (server != null) {
                try {
                    server.userLoggedOut(username);
                } catch (RemoteException ex) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    if (!handlePotentialDisconnect(ex)) {
                        throw ex;
                    }
                }
            }
            server = null;
            systemName = "";
            loggedIn = false;
            canRegClientView.setUserRightsLevel(Globals.UserRightLevels.NOT_LOGGED_IN);
            localSettings.writeSettings();
            lockFile.closeMap();
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
            // return Globals.UserRightLevels.SUPERVISOR;
            // return Globals.UserRightLevels.SUPERVISOR;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            handlePotentialDisconnect(ex);
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
     * @throws java.rmi.RemoteException
     */
    public String performBackup() throws RemoteException {
        String path = null;
        try {
            path = server.performBackup();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
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
        try {
            String message = server.restoreFromBackup(path);
            // refreshDictionary();
            // refreshDictionary();

            //Log out...
            logOut();
            return message;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return null;
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
     * @throws canreg.server.database.UnknownTableException
     * @throws canreg.common.cachingtableapi.DistributedTableDescriptionException
     * @throws java.lang.SecurityException
     */
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws SQLException, SecurityException, UnknownTableException, DistributedTableDescriptionException, RemoteException {
        try {
            return server.getDistributedTableDescription(filter, tableName);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return null;
    }

    /**
     * 
     * @param recordID
     * @param tableName
     * @param lock
     * @return
     * @throws java.lang.SecurityException
     * @throws canreg.server.database.RecordLockedException
     * @throws java.rmi.RemoteException
     */
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock) throws SecurityException, RecordLockedException, RemoteException {
        try {
            DatabaseRecord record = server.getRecord(recordID, tableName, lock);
            if (lock && record != null) {
                lockRecord(recordID, tableName);
            }
            return record;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return null;
    }

    /**
     * 
     * @param databaseRecord
     * @return 
     * @throws java.lang.SecurityException
     * @throws java.sql.SQLException
     * @throws canreg.server.database.RecordLockedException
     * @throws java.rmi.RemoteException
     */
    public int saveRecord(DatabaseRecord databaseRecord) throws SecurityException, SQLException, RecordLockedException, RemoteException {
        int recordNumber = -1;
        try {
            if (databaseRecord != null) {
                if (databaseRecord instanceof Patient) {

                    databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
                    databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdatedBy.toString()).getDatabaseVariableName(), username);
                    recordNumber = server.savePatient((Patient) databaseRecord);

                } else if (databaseRecord instanceof Tumour) {
                    databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
                    databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdatedBy.toString()).getDatabaseVariableName(), username);
                    recordNumber = server.saveTumour((Tumour) databaseRecord);
                } else if (databaseRecord instanceof NameSexRecord) {
                    recordNumber = server.saveNameSexRecord((NameSexRecord) databaseRecord, true);
                } else if (databaseRecord instanceof PopulationDataset) {
                    // recordNumber = server.savePopulationDataset((PopulationDataset) databaseRecord);
                }
            } else {
                debugOut("Trying to save null databaseRecord.");
            }
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return recordNumber;
    }

    /**
     * 
     * @param databaseRecord
     * @throws java.lang.SecurityException
     * @throws canreg.server.database.RecordLockedException
     * @throws java.rmi.RemoteException
     */
    public void editRecord(DatabaseRecord databaseRecord) throws SecurityException, RecordLockedException, RemoteException {
        try {
            if (databaseRecord instanceof Patient) {

                databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
                databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdatedBy.toString()).getDatabaseVariableName(), username);
                server.editPatient((Patient) databaseRecord);

            } else if (databaseRecord instanceof Tumour) {
                databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName(), dateFormat.format(new Date()));
                databaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdatedBy.toString()).getDatabaseVariableName(), username);
                server.editTumour((Tumour) databaseRecord);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    public boolean deleteRecord(int id, String tableName) throws SecurityException, RecordLockedException, SQLException, RemoteException {
        try {
            return server.deleteRecord(id, tableName);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return false;
    }

    public synchronized void releaseRecord(int recordID, String tableName) throws SecurityException, RemoteException {
        try {
            // release a locked record
            server.releaseRecord(recordID, tableName);
            Set lockSet = locksMap.get(tableName);
            if (lockSet != null) {
                lockSet.remove(recordID);
            }
            lockFile.writeMap();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
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
     * @param dictionaryID
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public boolean deleteDictionaryEntries(int dictionaryID) throws SecurityException, RemoteException {
        try {
            return server.deleteDictionaryEntries(dictionaryID);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return false;
        }
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public boolean clearNameSexTable() throws SecurityException, RemoteException {
        try {
            return server.clearNameSexTable();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return false;
        }
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Map<String, Integer> getNameSexTables() throws SecurityException, RemoteException {
        try {
            return server.getNameSexTables();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    /**
     * 
     * @param entry
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void saveDictionaryEntry(DictionaryEntry entry) throws SecurityException, RemoteException {
        try {
            server.saveDictionaryEntry(entry);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    /**
     * 
     * @param idString
     * @param lock
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws canreg.server.database.RecordLockedException
     * @throws canreg.common.cachingtableapi.DistributedTableDescriptionException
     * @throws canreg.server.database.UnknownTableException
     * @throws java.sql.SQLException
     */
    public Tumour[] getTumourRecordsBasedOnPatientID(String idString, boolean lock) throws SecurityException, SQLException, RecordLockedException, DistributedTableDescriptionException, UnknownTableException, RemoteException {
        Tumour[] records = null;
        String lookUpTableName;
        DatabaseFilter filter = new DatabaseFilter();
        String lookUpColumnName;

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
            records = new Tumour[numberOfRecords];
            idColumnNumber--;
            for (int j = 0; j < numberOfRecords; j++) {
                id = (Integer) rows[j][idColumnNumber];
                try {
                    records[j] = (Tumour) getRecord(id, lookUpTableName, lock);
                } catch (RecordLockedException recordLockedException) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, "Tumour record " + id + " already locked?", recordLockedException);
                } catch (RemoteException ex) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    if (!handlePotentialDisconnect(ex)) {
                        throw ex;
                    }
                }
            }
        }
        releaseResultSet(distributedTableDescription.getResultSetID());
        return records;
    }

    public Tumour getTumourRecordBasedOnTumourID(String idString, boolean lock) throws SecurityException, SQLException, RecordLockedException, DistributedTableDescriptionException, UnknownTableException, RemoteException {
        Tumour[] records = null;
        Tumour tumourToReturn = null;
        String lookUpTableName;
        DatabaseFilter filter = new DatabaseFilter();
        String lookUpColumnName;

        lookUpTableName = Globals.TUMOUR_TABLE_NAME;
        filter.setFilterString(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName() + " = '" + idString + "'");

        lookUpColumnName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        Object[][] rows;

        DistributedTableDescription distributedTableDescription = CanRegClientApp.getApplication().getDistributedTableDescription(filter, lookUpTableName);
        int numberOfRecords = distributedTableDescription.getRowCount();

        if (numberOfRecords > 0) {
            // Retrieve all rows
            rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords);

            String[] columnNames = distributedTableDescription.getColumnNames();

            boolean found = false;
            int idColumnNumber = 0;
            while (!found && idColumnNumber < columnNames.length) {
                found = columnNames[idColumnNumber++].equalsIgnoreCase(lookUpColumnName);
            }

            if (found) {
                int id = 0;
                records = new Tumour[numberOfRecords];
                idColumnNumber--;
                for (int j = 0; j < numberOfRecords; j++) {
                    try {
                        id = (Integer) rows[j][idColumnNumber];
                        records[j] = (Tumour) getRecord(id, lookUpTableName, lock);
                    } catch (RemoteException ex) {
                        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                        if (!handlePotentialDisconnect(ex)) {
                            throw ex;
                        }
                    }
                }
                try {
                    tumourToReturn = records[0];
                } catch (java.lang.ArrayIndexOutOfBoundsException aiobe) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, "Tumour record " + id + " already locked?", aiobe);
                }
            }
        }
        releaseResultSet(distributedTableDescription.getResultSetID());
        return tumourToReturn;
    }

    /**
     * Installs the JGoodies Look & Feels, if available, in classpath.
     */
    public static void initializeLookAndFeels() {
        // if in classpath try to load JGoodies Plastic Look & Feel
        try {
            LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
            boolean found = false;
            for (LookAndFeelInfo lnf : lnfs) {
                if (lnf.getName().equals("JGoodies Plastic 3D")) {
                    found = true;
                }
            }
            if (!found) {
                UIManager.installLookAndFeel("JGoodies Plastic 3D",
                        "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
            }
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        } catch (ClassNotFoundException t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IllegalAccessException t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (InstantiationException t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnsupportedLookAndFeelException t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 
     * @return
     */
    public GlobalToolBox getGlobalToolBox() {
        if (globalToolBox == null) {
            globalToolBox = new GlobalToolBox(doc);
        }
        return globalToolBox;
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws SecurityException, RemoteException {
        try {
            return server.getPopulationDatasets();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    /**
     * 
     * @param resultSetID
     * @param from
     * @param to
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws DistributedTableDescriptionException
     */
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws SecurityException, DistributedTableDescriptionException, RemoteException {
        try {
            return server.retrieveRows(resultSetID, from, to);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    /**
     * 
     * @param resultSetID
     * @throws java.lang.SecurityException
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     */
    public void releaseResultSet(String resultSetID) throws SecurityException, SQLException, RemoteException {
        try {
            server.releaseResultSet(resultSetID);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    /**
     * 
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Date getDateOfLastBackUp() throws SecurityException, RemoteException {
        if (server != null) {
            try {
                return server.getDateOfLastBackUp();
            } catch (RemoteException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                if (!handlePotentialDisconnect(ex)) {
                    throw ex;
                }
                return null;
            }
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
        try {
            return server.performPersonSearch(patient, searcher);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
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
     * @param rangeStart
     * @param rangeEnd
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public String initiateGlobalDuplicateSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws SecurityException, RemoteException {
        try {
            return server.initiateGlobalPersonSearch(searcher, rangeStart, rangeEnd);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException {
        try {
            return server.nextStepGlobalPersonSearch(idString);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        } catch (Exception ex) {
            // TODO: stop throwing general exceptions...
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void interuptGlobalPersonSearch(String idString) throws SecurityException, RemoteException {
        try {
            server.interuptGlobalPersonSearch(idString);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    /**
     *
     * @return
     */
    public String getCanRegVersionString() {
        return canRegSystemVersionString;
    }

    public List<User> listUsers() throws SecurityException, RemoteException {
        try {
            return server.listUsers();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    /**
     * Main method launching the application.
     * @param args
     */
    public static void main(String[] args) {
        // first see if we are dealing with something that doesn't use the GUI
        if (args.length > 0 && args[0].equalsIgnoreCase("--convert")) {
            // direct access to the converter
            // usage CanReg --convert <CanReg4 system definition file> [Charset name]
            try {
                SystemDefinitionConverter sdc = new SystemDefinitionConverter();
                if (args.length == 3) {
                    Charset cs = Charset.forName(args[2]);
                    sdc.setFileEncoding(cs);
                }
                sdc.convertAndSaveInSystemFolder(args[1]);
            } catch (FileNotFoundException ex) {
                System.out.println(args[1] + " not found. " + ex);
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("STARTING..."), 10);
            init();
            initializeLookAndFeels();
            if (args.length > 0) {
                SplashScreen splash = SplashScreen.getSplashScreen();
                if (splash != null) {
                    Graphics2D g = splash.createGraphics();
                    g.setComposite(AlphaComposite.Clear);
                    g.fillRect(0, 0, splash.getSize().width, splash.getSize().height);
                    g.setPaintMode();
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("SansSerif", Font.BOLD, 20));
                    g.drawString(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("CANREG5 SERVER STARTING..."), 35, splash.getSize().height / 2);
                    splash.update();
                }
                try {
                    canreg.common.ServerLauncher.start(Globals.DEFAULT_SERVER_ADDRESS, args[0], Globals.DEFAULT_PORT);
                    if (splash != null) {
                        splash.close();
                    }
                    JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("CANREG SERVER ") + args[0] + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString(" LAUNCHED."));
                } catch (AlreadyBoundException ex) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                launch(CanRegClientApp.class, args);
            }
        }
    }

    private synchronized void lockRecord(int recordID, String tableName) {
        Set lockSet = locksMap.get(tableName);
        if (lockSet == null) {
            lockSet = new TreeSet<Integer>();
            locksMap.put(tableName, lockSet);
        }
        lockSet.add(recordID);
        lockFile.writeMap();
    }

    public Patient[] getPatientRecordsByID(String recordID, boolean lock) throws SQLException, SecurityException, RecordLockedException, UnknownTableException, DistributedTableDescriptionException, RemoteException {
        try {
            Patient[] records;

            String databaseRecordIDVariableName;
            String tableName = Globals.PATIENT_TABLE_NAME;
            String patientIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
            databaseRecordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;

            DatabaseFilter filter = new DatabaseFilter();
            filter.setFilterString(patientIDVariableName + " = '" + recordID + "' ");
            DistributedTableDescription distributedTableDescription;
            Object[][] rows;
            DatabaseRecord record = null;

            distributedTableDescription = getDistributedTableDescription(filter, tableName);

            int numberOfRecords = distributedTableDescription.getRowCount();
            records = new Patient[numberOfRecords];

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
                    for (int recordNo = 0; recordNo < numberOfRecords; recordNo++) {
                        int id = (Integer) rows[recordNo][idColumnNumber];
                        records[recordNo] = (Patient) getRecord(id, Globals.PATIENT_TABLE_NAME, lock);
                    }
                }
            }
            return records;
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return null;
        }
    }

    private synchronized int numberOfRecordsOpen() {
        int numberOfRecords = 0;
        for (String tableName : locksMap.keySet()) {
            Set<Integer> lockSet = locksMap.get(tableName);
            numberOfRecords += lockSet.size();
        }
        return numberOfRecords;
    }

    public JDesktopPane getDesktopPane() {
        return canRegClientView.getDesktopPane();
    }

    private static void splashMessage(String message, int progress) {
        SplashScreen splash = SplashScreen.getSplashScreen();
        int maxProgress = 100;
        if (splash != null) {
            Graphics2D g = splash.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, splash.getSize().width, splash.getSize().height);
            g.setPaintMode();
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(message, 35, splash.getSize().height / 2 + 20);
            g.drawRect(35, splash.getSize().height / 2 + 30, splash.getSize().width - 70, 9);
            g.fillRect(37, splash.getSize().height / 2 + 32, (progress * (splash.getSize().width - 68) / maxProgress), 5);
            splash.update();
        }
    }

    private synchronized void releaseAllRecordsHeldByThisClient() {
        for (String tableName : locksMap.keySet()) {
            Set<Integer> lockSet = locksMap.get(tableName);
            if (lockSet != null) {
                for (Integer recordID : lockSet) {
                    try {
                        releaseRecord(recordID, tableName);
                    } catch (RemoteException ex) {
                        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        lockFile.writeMap();
    }

    public void showLogginFrame() {
        canRegClientView.showLoginFrame();
    }

    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray) throws SecurityException, RemoteException {
        try {
            return server.setDBPassword(newPasswordArray, oldPasswordArray);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            return false;
        }
    }

    public boolean handlePotentialDisconnect(Throwable ex) {
        if (ex.getCause() instanceof java.net.ConnectException
                || ex instanceof java.rmi.ConnectIOException
                || ex instanceof java.net.SocketException
                || ex.getCause() instanceof java.net.SocketException) {
            JOptionPane.showMessageDialog(
                    canRegClientView.getDesktopPane(),
                    "You seem to have been disconnected from the server. \n"
                    + "Please log in again...");
            server = null;
            systemName = "";
            loggedIn = false;
            canRegClientView.setLoggedOut();
            showLogginFrame();
            return true;
        }
        return false;
    }

    public int getNumberOfRecordsLocked() {
        return lockFile.getNumberOfRecordsLocked();
    }

    public void clearListOfLockedRecords() {
        releaseAllRecordsHeldByThisClient();
    }

    public void setUIFont(String fontName, int fontSize) {
        UITools.setUIFont(new javax.swing.plaf.FontUIResource(new Font(fontName, Font.PLAIN, fontSize)));
    }
    
    public JDesktopPane getDeskTopPane(){
        return canRegClientView.getDesktopPane();
    }
}
