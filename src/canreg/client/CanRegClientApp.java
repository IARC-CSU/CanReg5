/*
 * CanRegClientApp.java
 */
package canreg.client;

import cachingtableapi.DistributedTableDescription;
import cachingtableapi.DistributedTableModel;
import canreg.client.dataentry.Relation;
import canreg.client.gui.CanRegClientView;
import canreg.client.dataentry.ImportOptions;
import canreg.common.DatabaseFilter;
import canreg.common.Globals;
import canreg.server.CanRegLoginInterface;
import canreg.server.CanRegServerInterface;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;
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
    private HashMap<Integer, HashMap<String, String>> dictionary;
    private boolean canregServerRunningOnThisMachine = false;

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
     * Main method launching the application.
     */
    public static void main(String[] args) {
        init();
        launch(CanRegClientApp.class, args);
    }

    public static void init() {
        //Testing the environment
        testEnvironment();
        // Initialize the user settings
        try {
            localSettings = new LocalSettings("settings.xml");
        // Locale.setDefault(localSettings.getLocale());
        } catch (IOException ioe) {
            debugOut(ioe.getLocalizedMessage());
        }
    }

    private static void testEnvironment() {
        java.util.Properties prop = System.getProperties();
        java.util.Enumeration enumerator = prop.propertyNames();
        while (enumerator.hasMoreElements()) {
            String key = (String) enumerator.nextElement();
            System.out.println(key + " = " + System.getProperty(key));
        }
        File dir1 = new File(".");
        try {
            System.out.println("Current dir : " + dir1.getCanonicalPath());
        //System.out.println ("Parent  dir : " + dir2.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String testConnection(String serverObjectString) {
        debugOut("testing the connecting to server=" + serverObjectString + ".");
        CanRegLoginInterface loginServer = null;
        try {
            //authenticate credentials
            loginServer = (CanRegLoginInterface) Naming.lookup(serverObjectString);
            //login object received
            // try to get system name
            systemName = loginServer.getSystemName();
        } catch (Exception e) {
            System.out.println(e);
        // System.exit(0);
        }
        return systemName;
    }

    //  Log on to the CanReg system and set up the server connection.
    //  Returns CanReg System's name if successfull - null if not
    public String login(String serverObjectString, String username, char[] password) {
        this.username = username;
        debugOut("connecting to server=" + serverObjectString + " as " + username + ".");
        CanRegLoginInterface loginServer = null;
        try {
            //authenticate credentials
            loginServer = (CanRegLoginInterface) Naming.lookup(serverObjectString);
        //login object received
        } catch (Exception e) {
            System.out.println(e);
        // System.exit(0);
        }
//do the login 
        try {
            debugOut("ATTEMPTING LOGIN");
            server = (CanRegServerInterface) loginServer.login(username, new String(password));
            if (server != null) {
                debugOut("LOGIN SUCCESSFULL");
                // This should work...
                systemName = server.getCanRegSystemName();
                loggedIn = true;
                doc = server.getDatabseDescription();
                dictionary = server.getDictionary();
                canregServerRunningOnThisMachine = InetAddress.getLocalHost().
                        equals(server.getIPAddress());
                Globals.UserRightLevels i = getUserRightLevel();
                canRegClientView.setUserRightsLevel(i);
                return systemName;
            } else {
                return null;
            }
        } catch (LoginException le) {
            debugOut("LOGIN UNSUCCESSFULL");
            return null;
        } catch (NullPointerException npe) {
            debugOut("SERVER NOT FOUND");
            return null;
        } catch (Exception e) {
            debugOut("error : " + e);
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

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

    public void startDatabaseServer() throws RemoteException, SecurityException {
        server.startNetworkDBServer();
    }

    public void stopDatabaseServer() throws RemoteException, SecurityException {
        server.stopNetworkDBServer();
    }

    public LocalSettings getLocalSettings() {
        return localSettings;
    }

    public Document getDatabseDescription() {
        return doc;
    }

    public HashMap<Integer, HashMap<String, String>> getDictionary() {
        return dictionary;
    }

    public void refreshDictionary() throws SecurityException, RemoteException {
        dictionary = server.getDictionary();
    }

    public void applyPreferences() {
        Locale.setDefault(localSettings.getLocale());
    }

    public void importFile(Task<Object,Void> task, Document doc, List<Relation> map, File file, ImportOptions io) throws RemoteException {
        canreg.client.dataentry.Import.importFile(task , doc, map, file, server, io);
    }

    public void setCanregServerRunningInThisThread(boolean canregServerRunningInThisThread) {
        this.canregServerRunningInThisThread = canregServerRunningInThisThread;
    }

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

    public void logOut() {
        try {
            server.userLoggedOut(username);
            server = null;
            systemName = "";
            loggedIn = false;
            canRegClientView.setUserRightsLevel(Globals.UserRightLevels.NOT_LOGGED_IN);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public Globals.UserRightLevels getUserRightLevel() {
        // For now all users are supervisors
        return Globals.UserRightLevels.SUPERVISOR;
    }

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

    public String restoreBackup(String path) throws SecurityException, RemoteException {
        return server.restoreFromBackup(path);
    }

    public CanRegServerInterface getServer() {
        return server;
    }

    public boolean isCanregServerRunningInThisThread() {
        return canregServerRunningInThisThread;
    }

    public boolean isCanRegServerRunningOnThisMachine() {
        return canregServerRunningOnThisMachine;
    }
    
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws SQLException, RemoteException, SecurityException, Exception {
        return server.getDistributedTableDescription(filter, tableName);
    }
    
    public Object[][] retrieveRows(DistributedTableDescription description, int from, int to) throws RemoteException, SecurityException, Exception{
            return server.retrieveRows(description, from, to);
    }
}
