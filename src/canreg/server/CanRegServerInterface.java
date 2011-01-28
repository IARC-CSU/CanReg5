package canreg.server;

import canreg.server.database.User;
import cachingtableapi.DistributedTableDescription;
import cachingtableapi.DistributedTableDescriptionException;
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
import canreg.server.database.RecordLockedException;
import canreg.server.database.Tumour;
import canreg.server.database.UnknownTableException;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public interface CanRegServerInterface extends Remote {

    public boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException;

    /**
     * @throws SecurityException If the client doesn't
     * have permissions for executing this method.
     * @param patient
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void editPatient(Patient patient)
            throws RemoteException, SecurityException, RecordLockedException;

    /**
     * 
     * @param tumour
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void editTumour(Tumour tumour)
            throws RemoteException, SecurityException, RecordLockedException;

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getCanRegSystemName()
            throws RemoteException, SecurityException;

    /**
     * Returns the description of the database
     * @return A document description of the database
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Document getDatabseDescription()
            throws RemoteException, SecurityException;

    // 
    /**
     * Returns the connection to the database
     * NOT IN USE - FOR TESTING ONLY
     * @return The CanRegDAO for the current database
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public CanRegDAO getDatabseConnection()
            throws RemoteException, SecurityException;

    /**
     * Returns the date of the last backup
     * @return Date of last backup
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException;

    /**
     * Returns the user right level of the current user
     * @return the user right level of the current user as a UserRightLevels
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException;

    /**
     * Store new population data set in the database
     * @param pds the population dataset to store
     * @return the database id of the population dataset
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveNewPopulationDataset(PopulationDataset pds)
            throws RemoteException, SecurityException;

    /**
     * Returns a database record with the given details
     * @param recordID the database record id
     * @param tableName the table to get the record from
     * @param lock lock the record? true/false
     * @return a DatabaseRecord
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock)
            throws RemoteException, SecurityException, RecordLockedException ;

    // administrative tools
    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void addUser(User user)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void removeUser(User user)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void setUserPassword(String username, String password)
            throws RemoteException, SecurityException;

    public boolean deleteRecord(int id, String tableName)
            throws RemoteException, SecurityException, RecordLockedException, SQLException;

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

    /**
     * Get a list of users currently logged in to this server
     * @return a list of users currently logged in to this server as a String array
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listCurrentUsers()
            throws RemoteException, SecurityException;

    public List<User> listUsers() throws RemoteException, SecurityException;

    public int saveUser(User user) throws RemoteException, SecurityException;

    /**
     * User logs in
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void userLoggedIn(String username)
            throws RemoteException, SecurityException;

    /**
     * User logs out
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void userLoggedOut(String username)
            throws RemoteException, SecurityException;

    /**
     * Store a patient in the patient database
     * @param patient to store
     * @return database id of the patient
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int savePatient(Patient patient) throws RemoteException, SecurityException, SQLException, RecordLockedException;

    /**
     * 
     * @param tumour
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException, SQLException, RecordLockedException;

    /**
     * Store a dictionary entry on the server
     * @param dictionaryEntry
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException;

    /**
     * Drop a dictionary
     * @param dictionaryID
     * @return true if successful, false if not
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException;

    /**
     * Get the dictionary
     * @return a map of dictionary IDs and dictionaries
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException;

    /**
     * Perform backup
     * @return message
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String performBackup()
            throws RemoteException, SecurityException;

    /**
     * Restore from backup
     * @param path Path to backup - on server
     * @return message
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String restoreFromBackup(String path)
            throws RemoteException, SecurityException;

    /**
     * Get version information
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getCanRegVersion()
            throws RemoteException, SecurityException;

    /**
     * Get the ip-address of the server
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public InetAddress getIPAddress()
            throws RemoteException, SecurityException;

    /**
     * Initiate a new result set using the filter provided
     * @param filter
     * @param tableName
     * @return
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.lang.Exception
     */
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws SQLException, RemoteException, SecurityException, UnknownTableException, DistributedTableDescriptionException ;

    /**
     * Retrieve rows from a resultset
     * @param resultSetID id of the resultset 
     * @param from from this row
     * @param to to this row
     * @return a 2D array of objects containing the row data
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.lang.Exception
     */
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException;

    /**
     * Release result set to avoid unessesary server load
     * Can this be automated?
     * Security-issues?
     * @param resultSetID id of the resultset to be released
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException;

    public void releaseRecord(int recordID, String tableName) throws RemoteException, SecurityException;

    /**
     * Get the population datasets
     * @return the population datasets 
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException;

    /**
     * Get the table of names per sex 
     * @return the table of names per sex 
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
    public int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace) throws RemoteException, SecurityException;

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
    public Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException;

    /**
     * Perform global person search
     * @param searcher 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException;

    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, Exception;

    public void interuptGlobalPersonSearch(String idString) throws RemoteException, SecurityException;

    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException;
}
