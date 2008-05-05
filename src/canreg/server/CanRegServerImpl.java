/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server;

import canreg.common.Globals;
import canreg.server.database.CanRegDAO;
import canreg.server.management.SystemDescription;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.drda.NetworkServerControl;
import java.net.InetAddress;
import java.util.Iterator;
import org.w3c.dom.Document;

/**
 *
 * @author morten
 */
public class CanRegServerImpl extends UnicastRemoteObject implements CanRegServerInterface {

    private static boolean debug = true;
    Vector fClients = new Vector();
    private CanRegDAO db;
    private NetworkServerControl dbServer;
    private SystemDescription systemDescription;
    private String systemCode;

    public CanRegServerImpl() throws RemoteException {
        this("TRN");
    }

    public CanRegServerImpl(String systemCode) throws RemoteException {
        this.systemCode = systemCode;
        // Step one load the system definition...
        if (!initSystemDefinition()) {
            throw new RemoteException("Faulty system definitions...");
        }
        // Step two: start the database...
        if (!initDataBase()) {
            throw new RemoteException("Cannot initialize database...");
        }
    // for debugging purposes we start a derby network server as well
    // moved into main GUI
    // startNetworkDBServer();
    }

    // Initialize the database connection
    private boolean initDataBase() {
        boolean success = false;

        // Connect to the database
        db = new CanRegDAO(systemDescription.getSystemDescriptionDocument());
        db.connect();

        if (db != null) {
            success = true;
        }

        return success;
    }

    // Initialize the system description object
    private boolean initSystemDefinition() {
        boolean success = false;

        // Load the system description object
        systemDescription = new SystemDescription(Globals.CANREG_SYSTEM_CONFIG_FOLDER + systemCode + ".xml");

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
    // Afterwards:
    // Linux: http://www.ibm.com/developerworks/db2/library/techarticle/dm-0409cline2/readme_linux.txt
    // Windows: http://www.ibm.com/developerworks/db2/library/techarticle/dm-0409cline2/readme_win.txt
    //
    public void startNetworkDBServer() {
        try {
            debugOut("Start Network DB Server.");
            dbServer = new NetworkServerControl(InetAddress.getByName("localhost"), 1528);
            dbServer.start(null);
        } catch (Exception ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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

    /** The first operation. */
    public void doOperationA() {
        debugOut("Operation A!");
    }

    /** The second operation. */
    public void doOperationB() {
        debugOut("Operation B!");
    }

    public Document getDatabseDescription() throws RemoteException, SecurityException {
        return systemDescription.getSystemDescriptionDocument();
    }

    public void addUser(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUser(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUserPassword(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getUserPassword(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCanRegSystemName() throws RemoteException, SecurityException {
        String name = null;

        if (systemDescription != null) {
            name = systemDescription.getSystemName();
        }

        return name;
    }

    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        String[] users = new String[fClients.size()];
        int i = 0;
        for (Iterator it = fClients.iterator(); it.hasNext();) {
            users[i] = (String) it.next();
            System.out.println("element is " + users[i]);
            i++;
        }

        return users;
    }

    public void userLoggedIn(String username)
            throws RemoteException, SecurityException {
        fClients.add(username);
    }

    public void userLoggedOut(String username)
            throws RemoteException, SecurityException {
        fClients.remove(fClients.indexOf(username));
    }

    // For testing purposes only - not secure enough...
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        return db;
    }

    private void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[CanRegServer] " + msg);
        }
    }
}
