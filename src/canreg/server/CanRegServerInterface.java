/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server;

import cachingtableapi.DistributedTableDescription;
import canreg.common.DatabaseFilter;
import canreg.server.database.CanRegDAO;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import javax.security.auth.Subject;
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

    //Add cases
    public int savePatient(Patient patient) throws RemoteException, SecurityException;

    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException;

    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException;
    // Drop cases
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException;
    // Get the dictionary
    public HashMap<Integer, HashMap<String, String>> getDictionary() throws RemoteException, SecurityException;
    // Backup
    public String performBackup()
            throws RemoteException, SecurityException;
    public String restoreFromBackup(String path)
            throws RemoteException, SecurityException;

    // Get version information
    public String getCanRegVersion()
            throws RemoteException, SecurityException;
    
    public InetAddress getIPAddress()
            throws RemoteException, SecurityException;
    
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws SQLException, RemoteException, SecurityException, Exception;

    public DistributedTableDescription getDistributedTableDescription(Subject theUser, DatabaseFilter filter, String tableName) throws SQLException, RemoteException, SecurityException, Exception;
    
    public Object[][] retrieveRows(Subject theUser, int from, int to) throws RemoteException, SecurityException, Exception ;
    
    public Object[][] retrieveRows(int from, int to) throws RemoteException, SecurityException, Exception;
}
