/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2021  International Agency for Research on Cancer
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

import canreg.client.dataentry.Relation;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.importers.ImportOptions;
import canreg.client.gui.tools.UITools;
import canreg.common.DatabaseFilter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDataset;
import canreg.common.database.Tumour;
import canreg.common.database.User;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.Checker;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.exceptions.WrongCanRegVersionException;
import canreg.server.CanRegLoginImpl;
import canreg.server.CanRegLoginInterface;
import canreg.server.CanRegServerInterface;
import canreg.server.DatabaseStats;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private CanRegServerInterface mainServer;
    private ScheduledExecutorService pingExecutor;
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
    private Map<String, Set<Integer>> locksMap;
    private LockFile lockFile;

    public void changePassword(String encrypted) throws SecurityException, RemoteException {
        try {
            mainServer.setUserPassword(null, encrypted);
        } catch (RemoteException ex) {
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Check if the hashed password correspond to the password in the database
     * 
     * @param username username
     * @param hashedPassword password of the user
     * @return boolean true if the password is right else false
     * @throws SecurityException a security exception
     * @throws RemoteException a remote exception
     */
    public boolean checkPassword(String username,String hashedPassword)
        throws SecurityException, RemoteException {
             return mainServer.checkPassword(username,hashedPassword);
        }
        
    public boolean deletePopulationDataset(int populationDatasetID, CanRegServerInterface server)
            throws SQLException, SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

    public Patient getPatientRecord(String requestedPatientRecordID, boolean lock, CanRegServerInterface server)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException {
        if(server == null)
            server = this.mainServer;
        return (Patient) getRecordByID(requestedPatientRecordID, Globals.PATIENT_TABLE_NAME, lock, server);
    }

    public Tumour getTumourRecord(String requestedPatientRecordID, boolean lock, CanRegServerInterface server) 
            throws SQLException, SecurityException, RecordLockedException, UnknownTableException, 
                   DistributedTableDescriptionException, RemoteException {
        if(server == null)
            server = this.mainServer;
        return (Tumour) getRecordByID(requestedPatientRecordID, Globals.TUMOUR_TABLE_NAME, lock, server);
    }

    public void saveUser(User user) throws SQLException, RemoteException, SecurityException {
        mainServer.saveUser(user);
    }
    
    private DatabaseRecord getRecordByID(String recordID, String tableName, boolean lock, CanRegServerInterface server)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

            distributedTableDescription = getDistributedTableDescription(filter, tableName, server);
            int numberOfRecords = distributedTableDescription.getRowCount();

            rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords, server);
            releaseResultSet(distributedTableDescription.getResultSetID(), server);
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
                    record = getRecord(id, tableName, lock, server);
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
     *
     * @param pds Population Data Set to save
     * @param server
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public void saveNewPopulationDataset(PopulationDataset pds, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
                        option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?") + "\n" + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("YOU_HAVE_") + " " + numberOfRecordsOpen + java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("_RECORDS_OPEN."), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                    } else {
                        option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("REALLY_EXIT?"), JOptionPane.YES_NO_OPTION);
                    }
                } else if (loggedIn) {
                    int users = 0;
                    try {
                        users = listUsersLoggedIn().length - 1;
                    } catch (SecurityException | RemoteException ex) {
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
                if (isCanregServerRunningInThisThread() && mainServer != null) {
                    try {
                        mainServer.shutDownServer();
                    } catch (RemoteException | SecurityException ex) {
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
     *
     * @param root
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of CanRegClientApp
     */
    public static CanRegClientApp getApplication() {
        return Application.getInstance(CanRegClientApp.class);
    }

    /**
     * Initialize the application
     */
    public static void init() {

        // Initialize logger
        try {
            splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("INITIALIZING LOGGER..."), 20);
            Handler fh = new FileHandler(Globals.LOGFILE_PATTERN);
            Logger.getLogger("").addHandler(fh);
            Logger.getLogger("canreg").setLevel(Level.parse(Globals.LOG_LEVEL));
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        splashMessage(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("LOADING USER SETTINGS..."), 40);
        // Initialize the user settings
        localSettings = new LocalSettings("settings.xml");
    }

    /**
     * Test the connection to a server
     *
     * @param serverObjectString The address to a server
     * @return name of the system, null if not able to connect
     */
    public String testConnection(String serverObjectString) {
        debugOut("testing the connecting to server=" + serverObjectString + ".");
        CanRegLoginInterface loginServer;
        String sysName = null;
        try {
            //authenticate credentials
            loginServer = (CanRegLoginInterface) Naming.lookup(serverObjectString);
            //login object received
            // try to get system name
            sysName = loginServer.getRegistryName();
        } catch (NotBoundException | RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, null, ex);
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
     * @throws javax.security.auth.login.LoginException if successfull - null if
     * not
     * @throws java.lang.NullPointerException
     * @throws java.rmi.NotBoundException
     * @throws java.net.MalformedURLException
     * @throws java.rmi.RemoteException
     * @throws java.net.UnknownHostException
     * @throws canreg.exceptions.WrongCanRegVersionException
     */
    public String loginRMI(String serverObjectString, String username, char[] password) 
            throws LoginException, NullPointerException, NotBoundException, 
                   MalformedURLException, RemoteException, UnknownHostException, 
                   WrongCanRegVersionException {
        String returnString = null;
        debugOut("connecting to server=" + serverObjectString + " as " + username + ".");
        //authenticate credentials
        CanRegLoginInterface loginServer = getCanRegLoginServer(serverObjectString);
        //login object received
        if (loginServer != null) {
            returnString = login(loginServer, username, password, false);
        }
        return returnString;
    }

    public String loginDirect(String serverCode, String username, char[] password, boolean isAdHocDB)
            throws LoginException, NullPointerException, NotBoundException, MalformedURLException,
                   RemoteException, UnknownHostException, WrongCanRegVersionException {
        // should this be moved to the loginserver?
        CanRegLoginInterface loginServer = new CanRegLoginImpl(serverCode, isAdHocDB);
        return login(loginServer, username, password, isAdHocDB);
    }

    private CanRegLoginInterface getCanRegLoginServer(String serverObjectString) 
            throws NotBoundException, MalformedURLException, RemoteException {
        return (CanRegLoginInterface) Naming.lookup(serverObjectString);
    }

    private String login(CanRegLoginInterface loginServer, String username, char[] password, boolean isAdHocDB) 
            throws LoginException, NullPointerException, NotBoundException, MalformedURLException,
            RemoteException, UnknownHostException, WrongCanRegVersionException {
        if (!canRegSystemVersionString.trim().equalsIgnoreCase(loginServer.getSystemVersion().trim())) {
            throw (new WrongCanRegVersionException("Server: " + loginServer.getSystemVersion() + ", Client: " + canRegSystemVersionString));
        }
        //do the loginRMI 
        debugOut("ATTEMPTING LOGIN");

        mainServer = loginServer.login(username, password);
        if (mainServer != null) {
            this.pingExecutor = Executors.newSingleThreadScheduledExecutor();
            Integer seconds = Integer.parseInt(localSettings.getProperty(LocalSettings.CLIENT_TO_SERVER_PING_KEY));
            this.pingExecutor.scheduleAtFixedRate(new PingToServer(), 0, seconds, TimeUnit.SECONDS);
            
            // See if server version of CanReg matches the 
            debugOut("LOGIN SUCCESSFULL");
            // This should work...
            systemName = mainServer.getCanRegRegistryName();
            loggedIn = true;
            doc = mainServer.getDatabseDescription();
            dictionary = mainServer.getDictionary();
            
            if(isAdHocDB) 
                globalToolBox = null;
            
            globalToolBox = getGlobalToolBox();

            canregServerRunningOnThisMachine = InetAddress.getLocalHost().
                    equals(mainServer.getIPAddress());
            
            if( ! isAdHocDB) {
                Globals.UserRightLevels i = getUserRightLevel();
                canRegClientView.setUserRightsLevel(i);

                try {
                    canRegClientView.setHoldingDBsList(mainServer.getHoldingDBsList());
                } catch(IOException ex) {
                    List<String> strs = new LinkedList<>();
                    strs.add(java.util.ResourceBundle.getBundle("canreg/client/resources/CanRegClientApp").getString("ERROR HOLDING DB"));
                    canRegClientView.setHoldingDBsList(strs);
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, "Error while trying to list holding DBs", ex);
                }
            }
            

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
    
    public CanRegServerInterface getServer() {
        return mainServer;
    }

    public String getSystemCode(CanRegServerInterface server) throws RemoteException {
        if(server == null)
            server = this.mainServer;
        return server.getCanRegRegistryCode();
    }

    public String getSystemRegion(CanRegServerInterface server) throws RemoteException {
        if(server == null)
            server = this.mainServer;
        return server.getCanRegSystemRegion();
    }

    public DatabaseStats getDatabaseStats(CanRegServerInterface server) throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
     *
     * @return List of users logged in
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listUsersLoggedIn() throws SecurityException, RemoteException {
        try {
            return mainServer.listCurrentUsers();
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
     * @param msg the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, msg);
        }
    }

    /**
     * Start the database server
     *
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void startDatabaseServer() throws SecurityException, RemoteException {
        try {
            mainServer.startNetworkDBServer();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }

    /**
     * Stop the database server
     *
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void stopDatabaseServer() throws SecurityException, RemoteException {
        try {
            mainServer.stopNetworkDBServer();
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
            dictionary = mainServer.getDictionary();
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
            if (fontSize.equalsIgnoreCase(LocalSettings.FONT_SIZE_BIG)) {
                fontSizeInt = 14;
            } else if (fontSize.equalsIgnoreCase(LocalSettings.FONT_SIZE_SMALL)) {
                fontSizeInt = 10;
            }
        }
        if (setFont) {
            setUIFont(fontName, fontSizeInt);
        }
        if (localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY).length() > 0) {
            try {
                if (localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY).equalsIgnoreCase("Dark")) {
                    UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatDarculaLaf() );
                } else if (localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY).equalsIgnoreCase("Light")){
                    UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatIntelliJLaf() );
                }
                else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                    UIManager.setLookAndFeel(localSettings.getProperty(LocalSettings.LOOK_AND_FEEL_KEY));
                }
                // Locale.setDefault(localSettings.getLocale());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    public boolean importFile(Task<Object, String> task, Document doc, List<Relation> map, File file, ImportOptions io) 
            throws SQLException, SecurityException, RecordLockedException, RemoteException {
        //public boolean importFile(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, Document doc, List<Relation> map, File file, ImportOptions io) throws SQLException, SecurityException, RecordLockedException, RemoteException {
        try {
            return canreg.client.gui.importers.Import.importFile(task, doc, map, file, mainServer, io);
            //return canreg.client.dataentry.Convert.importFile(task, doc, map, file, server, io);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return false;
    }

    public boolean importCRFile(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, 
                                Document doc, List<Relation> map, File file, ImportOptions io)
            throws SQLException, SecurityException, RecordLockedException, RemoteException {
        try {
            return canreg.client.dataentry.Convert.importFile(task, doc, map, file, mainServer, io);
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
    public boolean importFiles(Task<Object, Void> task, List<Relation> map, File[] files, ImportOptions io) 
            throws SQLException, SecurityException, RecordLockedException, RemoteException, 
                   UnknownTableException, DistributedTableDescriptionException, Exception {
        try {
            return canreg.client.gui.importers.Import.importFiles(task, map, files, mainServer, io, false, doc);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
        return false;
    }
    
    public boolean importFilesIntoHoldingDB(Task<Object, Void> task, Document doc, List<Relation> map, File[] files, ImportOptions io) 
            throws Exception {
        try {
            boolean toReturn = canreg.client.gui.importers.Import.importFilesIntoHoldingDB(task, doc, map, files, mainServer, io);
            canRegClientView.setHoldingDBsList(mainServer.getHoldingDBsList());
            return toReturn;
        } catch (Exception ex) {
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
        dicsuccess = canreg.client.dataentry.Convert.convertDictionary(task, filepath, dictionaryfile, regcode);
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
            datsuccess = canreg.client.dataentry.Convert.convertData(task, filepath, datafile, regcode);
        } catch (Exception ex) {
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


    public void logOut() throws RemoteException {
        try {
            releaseAllRecordsHeldByThisClient();
            if (mainServer != null) {
                try {
                    mainServer.userLoggedOut(mainServer.hashCode(), username);
                } catch (RemoteException ex) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    if (!handlePotentialDisconnect(ex)) {
                        throw ex;
                    }
                }
            }
            mainServer = null;
            this.pingExecutor.shutdownNow();
            this.pingExecutor = null;
            
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
    
    public void refreshHoldingDBsList() {
        try {
            canRegClientView.setHoldingDBsList(this.mainServer.getHoldingDBsList());
        } catch(Exception ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public Globals.UserRightLevels getUserRightLevel() {
        Globals.UserRightLevels level = Globals.UserRightLevels.NOT_LOGGED_IN;
        try {

            level = mainServer.getUserRightLevel();
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
     * @return @throws java.rmi.RemoteException
     */
    public String performBackup() throws RemoteException {
        String path = null;
        try {
            path = mainServer.performBackup();
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
            String message = mainServer.restoreFromBackup(path);
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
     * @param server
     * @return
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     * @throws canreg.server.database.UnknownTableException
     * @throws
     * canreg.common.cachingtableapi.DistributedTableDescriptionException
     * @throws java.lang.SecurityException
     */
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, 
                                                                      String tableName, 
                                                                      CanRegServerInterface server)
            throws SQLException, SecurityException, UnknownTableException, 
                   DistributedTableDescriptionException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock, CanRegServerInterface server)
            throws SecurityException, RecordLockedException, RemoteException {
        if(server == null)
            server = this.mainServer;
        try {
            DatabaseRecord record = server.getRecord(recordID, tableName, lock, server.hashCode());
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
    public int saveRecord(DatabaseRecord databaseRecord, CanRegServerInterface server)
            throws SecurityException, SQLException, RecordLockedException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
     * @param server
     * @throws java.lang.SecurityException
     * @throws canreg.server.database.RecordLockedException
     * @throws java.rmi.RemoteException
     */
    public void editRecord(DatabaseRecord databaseRecord, CanRegServerInterface server) 
            throws SQLException, SecurityException, RecordLockedException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

    public boolean deleteRecord(int id, String tableName, CanRegServerInterface server) 
            throws SecurityException, RecordLockedException, SQLException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

    public synchronized void releaseRecord(int recordID, String tableName, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
        try {
            // release a locked record
            server.releaseRecord(recordID, tableName, server.hashCode());
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

  
    public boolean deleteDictionaryEntries(int dictionaryID, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;

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

   
    public boolean clearNameSexTable(CanRegServerInterface server) throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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


    public Map<String, Integer> getNameSexTables(CanRegServerInterface server) throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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


    public void saveDictionaryEntry(DictionaryEntry entry, CanRegServerInterface server) throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
        try {
            server.saveDictionaryEntry(entry);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            if (!handlePotentialDisconnect(ex)) {
                throw ex;
            }
        }
    }
    
    public Tumour[] getTumourRecordsBasedOnPatientRecordID(String patientRecordID, boolean lock, CanRegServerInterface server)
            throws SecurityException, SQLException, RecordLockedException, 
                   DistributedTableDescriptionException, UnknownTableException, RemoteException {
        return getTumourRecordsBasedOnVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                .getDatabaseVariableName(), patientRecordID, lock, server);
    }
    
    public Tumour[] getTumourRecordsBasedOnPatientID(String patientID, boolean lock, CanRegServerInterface server)
            throws SecurityException, SQLException, RecordLockedException, 
                   DistributedTableDescriptionException, UnknownTableException, RemoteException {
        return getTumourRecordsBasedOnVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString())
                .getDatabaseVariableName(), patientID, lock, server);
    }
    
    public Tumour[] getTumourRecordsBasedOnVariable(String variableName, String varID, boolean lock, CanRegServerInterface server)
            throws SecurityException, SQLException, RecordLockedException, 
                   DistributedTableDescriptionException, UnknownTableException, RemoteException {
        if(server == null)
            server = this.mainServer;
        Tumour[] records = null;
        String lookUpTableName;
        DatabaseFilter filter = new DatabaseFilter();
        String lookUpColumnName;

        lookUpTableName = Globals.TUMOUR_TABLE_NAME;
        filter.setFilterString(variableName + " = '" + varID + "'");

        lookUpColumnName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        Object[][] rows;

        DistributedTableDescription distributedTableDescription = 
                CanRegClientApp.getApplication().getDistributedTableDescription(filter, lookUpTableName, server);
        int numberOfRecords = distributedTableDescription.getRowCount();

        // Retrieve all rows
        rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords, server);

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
                    records[j] = (Tumour) getRecord(id, lookUpTableName, lock, server);
                } catch (RecordLockedException recordLockedException) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, "Tumour record " + id + " already locked?", recordLockedException);
//                    throw recordLockedException;
                } catch (RemoteException ex) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
                    if (!handlePotentialDisconnect(ex)) {
                        throw ex;
                    }
                }
            }
        }
        releaseResultSet(distributedTableDescription.getResultSetID(), server);
        return records;
    }
        

    public Tumour getTumourRecordBasedOnTumourID(String idString, boolean lock, CanRegServerInterface server) 
            throws SecurityException, SQLException, RecordLockedException, 
                   DistributedTableDescriptionException, UnknownTableException, RemoteException {
        if(server == null)
            server = this.mainServer;
        Tumour[] records = null;
        Tumour tumourToReturn = null;
        String lookUpTableName;
        DatabaseFilter filter = new DatabaseFilter();
        String lookUpColumnName;

        lookUpTableName = Globals.TUMOUR_TABLE_NAME;
        filter.setFilterString(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName() + " = '" + idString + "'");

        lookUpColumnName = Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME;
        Object[][] rows;

        DistributedTableDescription distributedTableDescription = 
                CanRegClientApp.getApplication().getDistributedTableDescription(filter, lookUpTableName, server);
        int numberOfRecords = distributedTableDescription.getRowCount();

        if (numberOfRecords > 0) {
            // Retrieve all rows
            rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords, server);

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
                        records[j] = (Tumour) getRecord(id, lookUpTableName, lock, server);
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
        } else {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.WARNING, "Tumour record {0} not found", idString);
        }
        releaseResultSet(distributedTableDescription.getResultSetID(), server);
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
     * @return @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Map<Integer, PopulationDataset> getPopulationDatasets(CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
    public Object[][] retrieveRows(String resultSetID, int from, int to, CanRegServerInterface server) 
            throws SecurityException, DistributedTableDescriptionException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
    public void releaseResultSet(String resultSetID, CanRegServerInterface server) 
            throws SecurityException, SQLException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
     * @return @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public Date getDateOfLastBackUp() throws SecurityException, RemoteException {
        if (mainServer != null) {
            try {
                return mainServer.getDateOfLastBackUp();
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
    public Map<String, Float> performDuplicateSearch(Patient patient, PersonSearcher searcher, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
    public String initiateGlobalDuplicateSearch(PersonSearcher searcher, String rangeStart, String rangeEnd, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

    public void interuptGlobalPersonSearch(String idString, CanRegServerInterface server) 
            throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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

    public List<User> listUsers(CanRegServerInterface server) throws SecurityException, RemoteException {
        if(server == null)
            server = this.mainServer;
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
     *
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
                SplashScreen splash = null;
                try {
                    splash = SplashScreen.getSplashScreen();
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
                } catch (java.awt.HeadlessException he) {
                    Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, null, he);
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
            lockSet = new TreeSet<>();
            locksMap.put(tableName, lockSet);
        }
        lockSet.add(recordID);
        lockFile.writeMap();
    }

    public Patient[] getPatientsByPatientID(String patientID, boolean lock, CanRegServerInterface server) 
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException {
        if(server == null)
            server = this.mainServer;
        try {
            Patient[] records;

            String databaseRecordIDVariableName;
            String tableName = Globals.PATIENT_TABLE_NAME;
            String patientIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
            databaseRecordIDVariableName = Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME;

            DatabaseFilter filter = new DatabaseFilter();
            filter.setFilterString(patientIDVariableName + " = '" + patientID + "' ");
            DistributedTableDescription distributedTableDescription;
            Object[][] rows;
            DatabaseRecord record = null;

            distributedTableDescription = getDistributedTableDescription(filter, tableName, server);

            int numberOfRecords = distributedTableDescription.getRowCount();
            records = new Patient[numberOfRecords];

            rows = retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords, server);
            releaseResultSet(distributedTableDescription.getResultSetID(), server);
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
                        records[recordNo] = (Patient) getRecord(id, Globals.PATIENT_TABLE_NAME, lock, server);
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
            if(lockSet != null)
                numberOfRecords += lockSet.size();
        }
        return numberOfRecords;
    }

    public JDesktopPane getDesktopPane() {
        return canRegClientView.getDesktopPane();
    }

    private static void splashMessage(String message, int progress) {
        try {
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
        } catch (java.awt.HeadlessException he) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.INFO, null, he);
        }
    }

    private synchronized void releaseAllRecordsHeldByThisClient() {
        locksMap.forEach((t, u) -> {
            locksMap.put(t, null);
        });
        
        lockFile.writeMap();
    }

    public void showLogginFrame() {
        canRegClientView.showLoginFrame();
    }

    public boolean encryptDatabase(char[] newPasswordArray, char[] oldPasswordArray, String encryptionAlgorithm, String encryptionKeyLength) 
            throws SecurityException, RemoteException {
        try {
            return mainServer.setDBPassword(newPasswordArray, oldPasswordArray, encryptionAlgorithm, encryptionKeyLength);
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

            mainServer = null;
            this.pingExecutor.shutdownNow();
            this.pingExecutor = null;

            systemName = "";
            loggedIn = false;
            releaseAllRecordsHeldByThisClient();
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

    public JDesktopPane getDeskTopPane() {
        if (canRegClientView == null) {
            return null;
        } else {
            return canRegClientView.getDesktopPane();
        }
    }

    /**
     *  Check if the password reminder file exists in the .CanRegServer 
      * @param username name of the user 
     * @return  bolean true if the file exists else false
     */    
    public boolean checkPasswordReminder(String username)  {
        try {
            return this.mainServer.checkFileReminder(username);
        } catch (RemoteException e) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE,"Unable to check the file reminder for user :"+ username, e);
        }
        return false;
    }

    /**
     * Create a file which title is the encoded username. The file is created only when a supervisor
     * reset the password of a user 
     * @param username user name 
     */
    public void createFileReminder(String username) {
        try {
            this.mainServer.createFileReminder(username);
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientApp.class.getName())
                .log(Level.SEVERE,"Unable to create the file reminder for user :"+ username, ex);
        }
    }

    /**
     * Delete the file which title is the encoded username. The file is deleted when the user change his password
     * or when his account is deleted
     * @param username
     */
    public void deleteFileReminder(String username) {
        try {
            this.mainServer.deleteFileReminder(username);
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientApp.class.getName())
                .log(Level.SEVERE,"Unable to delete the file reminder for user :"+ username, ex);
        }
    }
    
    private class PingToServer implements Runnable {
        @Override
        public void run() {
            try {
                //pingRemote's parameter is not needed here
                if(mainServer != null)
                    mainServer.pingRemote(mainServer.hashCode());
            } catch (RemoteException ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
