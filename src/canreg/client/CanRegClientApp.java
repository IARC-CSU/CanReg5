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
import canreg.exceptions.WrongCanRegVersionException;
import canreg.server.CanRegLoginInterface;
import canreg.server.CanRegServerInterface;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.Patient;
import canreg.server.database.PopulationDataset;
import canreg.server.database.Tumour;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public boolean loggedIn = false;
    private CanRegClientView canRegClientView;
    private Document doc;
    private Map<Integer, Map<String, String>> dictionary;
    private boolean canregServerRunningOnThisMachine = false;
    private GlobalToolBox globalToolBox;

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

        canRegClientView = new CanRegClientView(this);

        show(canRegClientView);

        ExitListener maybeExit = new ExitListener() {

            public boolean canExit(EventObject e) {
                int option = JOptionPane.NO_OPTION;
                if (!isCanregServerRunningInThisThread()) {
                    option = JOptionPane.showConfirmDialog(null, "Really exit?");
                } else {
                    try {
                        if (loggedIn) {
                            int users = listUsersLoggedIn().length - 1;
                            option = JOptionPane.showConfirmDialog(null, "Really exit?\n" + users + " other user(s) connected to this server will be disconnected.");
                        } else {
                            option = JOptionPane.showConfirmDialog(null, "Really exit?\nOther user(s) connected to this server will be disconnected.");
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
     * 
     */
    public static void init() {
        //Testing the environment - disabled
        // canreg.common.Tools.testEnvironment();
        // Initialize the user settings
        try {
            localSettings = new LocalSettings("settings.xml");
            initializeLookAndFeels();

        // Locale.setDefault(localSettings.getLocale());
        } catch (IOException ioe) {
            debugOut(ioe.getLocalizedMessage());
        }
    }

    /**
     * 
     * @param serverObjectString
     * @return
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
            System.out.println(e);
        // System.exit(0);
        }
        return sysName;
    }

    //  Log on to the CanReg system and set up the server connection.
    //  Returns CanReg System's name if successfull - null if not
    /**
     * 
     * @param serverObjectString
     * @param username
     * @param password
     * @return
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
        if (!Globals.VERSION_STRING.equalsIgnoreCase(loginServer.getSystemVersion())) {
            throw (new WrongCanRegVersionException(loginServer.getSystemVersion()));
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
            return systemName;
        } else {
            return null;
        }
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listUsersLoggedIn() throws RemoteException, SecurityException {
        return server.listCurrentUsers();
    }

    /**
     * Simple console trace to system.out for debug purposes only.&Ltp>
     *
     * @param msg the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[CanRegClient] " + msg);
        }
    }

    /**
     * 
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void startDatabaseServer() throws RemoteException, SecurityException {
        server.startNetworkDBServer();
    }

    /**
     * 
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
    public Map<Integer, Map<String, String>> getDictionary() {
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
    public void importFile(Task<Object, Void> task, Document doc, List<Relation> map, File file, ImportOptions io) throws RemoteException {
        canreg.client.dataentry.Import.importFile(task, doc, map, file, server, io);
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
        // For now all users are supervisors
        return Globals.UserRightLevels.SUPERVISOR;
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

    public void saveRecord(DatabaseRecord databaseRecord) throws SecurityException, RemoteException {
        if (databaseRecord instanceof Patient) {
            server.savePatient((Patient) databaseRecord);
        } else if (databaseRecord instanceof Tumour) {
            server.saveTumour((Tumour) databaseRecord);
        }
    }

    public void editRecord(DatabaseRecord databaseRecord) throws SecurityException, RemoteException {
        if (databaseRecord instanceof Patient) {
            server.editPatient((Patient) databaseRecord);
        } else if (databaseRecord instanceof Tumour) {
            server.editTumour((Tumour) databaseRecord);
        }
    }
    
    public boolean deleteDictionaryEntries(int dictionaryID) throws SecurityException, RemoteException {
        return server.deleteDictionaryEntries(dictionaryID);
    }

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
    public DatabaseRecord[] getRecordsFromOtherTableBasedOnID(String idString, String tableName) throws RemoteException, SecurityException, SQLException, Exception {
        DatabaseRecord[] records = null;
        String lookUpTableName = "";
        DatabaseFilter filter = new DatabaseFilter();
        String lookUpColumnName = "";

        // We want the records from the "other table"
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            lookUpTableName = Globals.PATIENT_TABLE_NAME;
            filter.setFilterString("TUMOURID =" + idString + "");
            lookUpColumnName = "ID";
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            lookUpTableName = Globals.TUMOUR_TABLE_NAME;
            filter.setFilterString("PATIENTID = " + idString + "");
            lookUpColumnName = "ID";
        } else {
            return null;
        }
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
        // if in classpath thry to load JGoodies Plastic Look & Feel
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

    public GlobalToolBox getGlobalToolBox() {
        return globalToolBox;
    }
    
    public Map<Integer,PopulationDataset> getPopulationDatasets() throws SecurityException, RemoteException{
        return server.getPopulationDatasets();
    }

    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException, Exception {
        return server.retrieveRows(resultSetID, from, to);
    }
    
    public void releaseResultSet(String resultSetID) throws SecurityException, RemoteException{
        server.releaseResultSet(resultSetID);
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
