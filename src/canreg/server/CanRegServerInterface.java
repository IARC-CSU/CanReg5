/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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

package canreg.server;

import canreg.common.database.User;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.DatabaseFilter;
import canreg.common.Globals.UserRightLevels;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.CanRegDAO;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDataset;
import canreg.server.database.RecordLockedException;
import canreg.common.database.Tumour;
import canreg.server.database.UnknownTableException;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
// import javax.security.auth.Subject;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public interface CanRegServerInterface extends Remote {

    /**
     *
     * @param populationDatasetID
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    public boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException;

    /**
     * @throws SecurityException If the client doesn't
     * have permissions for executing this method.
     * @param patient
     * @throws java.rmi.RemoteException
     * @throws RecordLockedException
     */
    public void editPatient(Patient patient)
            throws RemoteException, SecurityException, RecordLockedException;

    /**
     * 
     * @param tumour
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException
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
     * @throws RecordLockedException
     */
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock)
            throws RemoteException, SecurityException, RecordLockedException ;

    // administrative tools
    /**
     * 
     * @param user
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void addUser(User user)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param user
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void removeUser(User user)
            throws RemoteException, SecurityException;

    /**
     * 
     * @param username
     * @param password
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void setUserPassword(String username, String password)
            throws RemoteException, SecurityException;

    /**
     *
     * @param id
     * @param tableName
     * @return
     * @throws RemoteException
     * @throws SecurityException
     * @throws RecordLockedException
     * @throws SQLException
     */
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

    /**
     *
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    public List<User> listUsers() throws RemoteException, SecurityException;

    /**
     *
     * @param user
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
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
     * @throws SQLException
     * @throws RecordLockedException
     */
    public int savePatient(Patient patient) throws RemoteException, SecurityException, SQLException, RecordLockedException;

    /**
     * 
     * @param tumour
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws SQLException
     * @throws RecordLockedException
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
     * @throws UnknownTableException
     * @throws DistributedTableDescriptionException
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
     */
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException;

    /**
     * Release result set to avoid unessesary server load
     * Can this be automated?
     * Security-issues?
     * @param resultSetID id of the resultset to be released
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws SQLException
     */
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException, SQLException;

    /**
     *
     * @param recordID
     * @param tableName
     * @throws RemoteException
     * @throws SecurityException
     */
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
     * @param replace
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
     * @param rangeStart
     * @param rangeEnd
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException;

    /**
     *
     * @param idString
     * @return
     * @throws SecurityException
     * @throws RemoteException
     * @throws Exception
     */
    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, Exception;

    /**
     *
     * @param idString
     * @throws RemoteException
     * @throws SecurityException
     */
    public void interuptGlobalPersonSearch(String idString) throws RemoteException, SecurityException;

    /**
     *
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException;

    public void shutDownServer() throws RemoteException, SecurityException;

    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray) throws RemoteException, SecurityException;
}
