/*
 * CanRegClientApp.java
 */
package canreg.client;

import canreg.client.dataentry.Relation;
import canreg.client.gui.CanRegClientView;
import canreg.common.Globals;
import canreg.server.CanRegLoginInterface;
import canreg.server.CanRegServerInterface;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.w3c.dom.Document;

/**
 * The main class of the application.
 */
public class CanRegClientApp extends SingleFrameApplication {

    private CanRegServerInterface server;
    static boolean debug = true;
    private String systemName = null;
    private String username = null;
    private static LocalSettings localSettings;
    public boolean loggedIn = false;
    private CanRegClientView canRegClientView;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        canRegClientView = new CanRegClientView(this);
        show(canRegClientView);
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
        
        // Initialize the user settings
        try {
            localSettings = new LocalSettings("settings.xml");
            Locale.setDefault(localSettings.getLocale());
        } catch (IOException ioe){
            debugOut(ioe.getLocalizedMessage());
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
                int i = getUserRightLevel();
                canRegClientView.setUserRightsLevel(i);
                loggedIn = true;
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
    
    public LocalSettings getLocalSettings(){
        return localSettings;
    }
    
    public Document getDatabseDescription() throws RemoteException{
        return server.getDatabseDescription();
    }
    
    public void applyPreferences(){
        Locale.setDefault(localSettings.getLocale());
    }
    
    public void importFile(Document doc, List<Relation> map, File file) throws RemoteException{
        // placeholder... 
        // getting database connection... obly for Supervisor?
        canreg.client.dataentry.Import.importFile(doc, map, file, server);
    }
    
    @Action
    @Override
    public void quit(ActionEvent evt){
        try {
            logOut();
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
            canRegClientView.setUserRightsLevel(Globals.NOT_LOGGED_IN);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isLoggedIn(){
        return loggedIn;
    }
    
    public int getUserRightLevel(){
        return Globals.SUPERVISOR;
    }
}
