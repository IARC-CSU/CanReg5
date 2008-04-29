/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server;

import canreg.server.database.CanRegDAO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.w3c.dom.Document;

/**
 *
 * @author morten
 */
public interface CanRegServerInterface extends Remote {

    //Record[] getRecord() throws RemoteException;
    //void updateRecord(Record r) throws RemoteException, 
    /** The first operation. @throws SecurityException If the client doesn't 
     * have permissions for executing this method. */
    public void doOperationA()
            throws RemoteException, SecurityException;

    /** The second operation. @throws SecurityException If the client doesn't 
     * have permissions for executing this method. */
    public void doOperationB()
            throws RemoteException, SecurityException;

    public String getCanRegSystemName()
            throws RemoteException, SecurityException;

    // returns the description of the database
    public Document getDatabseDescription()
            throws RemoteException, SecurityException;

        // returns the connection to the database
    public CanRegDAO getDatabseConnection()
            throws RemoteException, SecurityException;
    
    // administrative tools
    public void addUser(String username)
            throws RemoteException, SecurityException;

    public void removeUser(String username)
            throws RemoteException, SecurityException;

    public void setUserPassword(String username)
            throws RemoteException, SecurityException;

    public String getUserPassword(String username)
            throws RemoteException, SecurityException;

    public void startNetworkDBServer()
            throws RemoteException, SecurityException;

    public void stopNetworkDBServer()
            throws RemoteException, SecurityException;

    //Users
    public String[] listCurrentUsers()
            throws RemoteException, SecurityException;  
    public void userLoggedIn(String username)
            throws RemoteException, SecurityException;
    public void userLoggedOut(String username)
            throws RemoteException, SecurityException;
}
