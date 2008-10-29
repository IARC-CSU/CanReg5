package canreg.server;

import cachingtableapi.DistributedTableDescription;
import canreg.common.DatabaseFilter;
import canreg.common.Globals.UserRightLevels;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.CanRegDAO;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Dictionary;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.NameSexRecord;
import canreg.server.database.Patient;
import canreg.server.database.PopulationDataset;
import canreg.server.database.Tumour;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
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
     * have permissions for executing this method.
     * @throws SecurityException 
     * @throws RemoteException
     */
    public void doOperationA()
            throws RemoteException, SecurityException;

    /** The second operation. @throws SecurityException If the client doesn't 
     * have permissions for executing this method.
     * @throws SecurityException 
     * @throws RemoteException
     */
    public void doOperationB()
            throws RemoteException, SecurityException;

    /**
     * 
     * @param patient
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void editPatient(Patient patient)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param tumour
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void editTumour(Tumour tumour)
            throws RemoteException, SecurityException;

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getCanRegSystemName()
            throws RemoteException, SecurityException;

    // returns the description of the database
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Document getDatabseDescription()
            throws RemoteException, SecurityException;

    // returns the connection to the database
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public CanRegDAO getDatabseConnection()
            throws RemoteException, SecurityException;

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException;

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException;

    /**
     * 
     * @param pds
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveNewPopulationDataset(PopulationDataset pds)
            throws RemoteException, SecurityException;
 
    /**
     * 
     * @param recordID
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public DatabaseRecord getRecord(int recordID, String tableName)
            throws RemoteException, SecurityException;
     
    // administrative tools
    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void addUser(String username)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void removeUser(String username)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void setUserPassword(String username)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getUserPassword(String username)
            throws RemoteException, SecurityException;

    /**
     * 
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void startNetworkDBServer()
            throws RemoteException, SecurityException;

    /**
     * 
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void stopNetworkDBServer()
            throws RemoteException, SecurityException;

    //Users
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listCurrentUsers()
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void userLoggedIn(String username)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void userLoggedOut(String username)
            throws RemoteException, SecurityException;

    //Add cases
    /**
     * 
     * @param patient
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int savePatient(Patient patient) throws RemoteException, SecurityException;

    /**
     * 
     * @param patientID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public DatabaseRecord getPatient(int patientID) throws RemoteException, SecurityException;

    /**
     * 
     * @param tumour
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException;

    /**
     * 
     * @param dictionaryEntry
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException;
    // Drop cases
    /**
     * 
     * @param dictionaryID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException;
    // Get the dictionary
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException;
    // Backup
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String performBackup()
            throws RemoteException, SecurityException;
    /**
     * 
     * @param path
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String restoreFromBackup(String path)
            throws RemoteException, SecurityException;

    // Get version information
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getCanRegVersion()
            throws RemoteException, SecurityException;
    
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public InetAddress getIPAddress()
            throws RemoteException, SecurityException;
    
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
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws SQLException, RemoteException, SecurityException, Exception;
    
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
    public Object[][] retrieveRows(String resultSetID, int from, int to)  throws RemoteException, SecurityException, Exception;

    /**
     * 
     * @param resultSetID
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException;
    
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException;
    
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException;
    
    /**
     * 
     * @param nameSexRecord
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveNameSexRecord(NameSexRecord nameSexRecord) throws RemoteException, SecurityException;
    
    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public boolean clearNameSexTable() throws RemoteException, SecurityException;
    
    /**
     * 
     * @param patient
     * @param searcher
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map <Integer, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException;

    /**
     * 
     * @param searcher
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, Map<Float, Integer>> performGlobalPersonSearch(PersonSearcher searcher) throws RemoteException, SecurityException;
}
