/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server;

import canreg.server.database.CanRegDAO;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import canreg.server.security.ValidateMethodCall;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.security.auth.Subject;
import org.w3c.dom.Document;

/**
 *
 * @author morten
 */
class CanRegServerProxy extends UnicastRemoteObject implements CanRegServerInterface {

    private CanRegServerInterface theServer;
    private Subject theUser;

    public CanRegServerProxy(Subject user, CanRegServerInterface server) throws RemoteException {
        /** The user associated with this proxy 
         */
        this.theUser = user;
        /** A reference to the real server object 
         */
        this.theServer = server;
    }

    /**
     * Proxy implementation of (1st) method in the server interface.
     * 
     * The client calls this method. If he client has the
     * appropriate permissions, the call goes through.
     */
    public void doOperationA() throws java.rmi.RemoteException, SecurityException {
        checkPermission("doOperationA");
        theServer.doOperationA();
    }

    /**
     * Proxy implementation of (2nd) method in the server interface.
     * 
     * The client calls this method. If he client has the
     * appropriate permissions, the call goes through.
     */
    public void doOperationB() throws java.rmi.RemoteException, SecurityException {
        checkPermission("doOperationB");
        theServer.doOperationB();
    }

    private void checkPermission(String methodName) throws SecurityException {
        // Assume the identity of the user, and validate if he can
        // call this method

        try {
            Subject.doAs(theUser, new ValidateMethodCall(methodName));
        } catch (java.security.PrivilegedActionException e) {
            throw (SecurityException) e.getException();
        }

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
        checkPermission("getCanRegSystemName");
        return theServer.getCanRegSystemName();
    }

    public void startNetworkDBServer() throws RemoteException, SecurityException {
        checkPermission("startNetworkDBServer");
        theServer.startNetworkDBServer();
    }

    public void stopNetworkDBServer() throws RemoteException, SecurityException {
        checkPermission("stopNetworkDBServer");
        theServer.stopNetworkDBServer();
    }

    public Document getDatabseDescription() throws RemoteException, SecurityException {
        checkPermission("getDatabseDescription");
        return theServer.getDatabseDescription();
    }

    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        checkPermission("listCurrentUsers");
        return theServer.listCurrentUsers();
    }

    public void userLoggedIn(String username) throws RemoteException, SecurityException {
        checkPermission("userLoggedIn");
        theServer.userLoggedIn(username);
    }

    public void userLoggedOut(String username) throws RemoteException, SecurityException {
        checkPermission("userLoggedOut");
        theServer.userLoggedOut(username);
    }

    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        // Cannot be sent over RMI. Good.
        checkPermission("getDatabseConnection");
        return theServer.getDatabseConnection();
    }

    public int savePatient(Patient patient) throws RemoteException, SecurityException {
        checkPermission("savePatient");
        return theServer.savePatient(patient);
    }

    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException {
        checkPermission("saveTumour");
        return theServer.saveTumour(tumour);
    }

    public String performBackup() throws RemoteException, SecurityException {
        checkPermission("performBackup");
        return theServer.performBackup();
    }
}
