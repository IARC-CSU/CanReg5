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
import canreg.server.security.ValidateMethodCall;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import javax.security.auth.Subject;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
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

    public int savePatient(Patient patient) throws RemoteException, SecurityException, SQLException {
        checkPermission("savePatient");
        return theServer.savePatient(patient);
    }

    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException, SQLException {
        checkPermission("saveTumour");
        return theServer.saveTumour(tumour);
    }

    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException {
        checkPermission("saveDictionaryEntry");
        return theServer.saveDictionaryEntry(dictionaryEntry);
    }

    public String performBackup() throws RemoteException, SecurityException {
        checkPermission("performBackup");
        return theServer.performBackup();
    }

    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        checkPermission("restoreFromBackup");
        return theServer.restoreFromBackup(path);
    }

    public String getCanRegVersion() throws RemoteException, SecurityException {
        checkPermission("getCanRegVersion");
        return theServer.getCanRegVersion();
    }

    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        checkPermission("deleteDictionaryEntries");
        return theServer.deleteDictionaryEntries(dictionaryID);
    }

    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        checkPermission("getDictionary");
        return theServer.getDictionary();
    }

    public InetAddress getIPAddress() throws RemoteException, SecurityException {
        checkPermission("getIPAddress");
        return theServer.getIPAddress();
    }

    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws RemoteException, SecurityException, SQLException, Exception {
        checkPermission("getDistributedTableDescription");
        return theServer.getDistributedTableDescription(filter, tableName);
    }

    public Object[][] retrieveRows(Subject theUser, int from, int to) throws RemoteException, SecurityException, Exception {
        throw new UnsupportedOperationException("Not supported."); // This should not be implemented!
    }

    public DatabaseRecord getPatient(int patientID) throws RemoteException, SecurityException {
        checkPermission("getPatient");
        return theServer.getPatient(patientID);
    }

    public DatabaseRecord getRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        checkPermission("get:" + tableName);
        return theServer.getRecord(recordID, tableName);
    }

    public void editPatient(Patient patient) throws RemoteException, SecurityException {
        checkPermission("editPatient");
        theServer.editPatient(patient);
    }

    public void editTumour(Tumour tumour) throws RemoteException, SecurityException {
        checkPermission("editTumour");
        theServer.editTumour(tumour);
    }

    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException, Exception {
        checkPermission("retrieveRows:" + resultSetID);
        return theServer.retrieveRows(resultSetID, from, to);
    }

    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException {
        checkPermission("retrieveRows:" + resultSetID);
        theServer.releaseResultSet(resultSetID);
    }

    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        checkPermission("getPopulationDatasets");
        return theServer.getPopulationDatasets();
    }

    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        checkPermission("saveNewPopulationDataset");
        return theServer.saveNewPopulationDataset(pds);
    }

    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        checkPermission("getDateOfLastBackUp");
        return theServer.getDateOfLastBackUp();
    }

    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {
        checkPermission("getNameSexTables");
        return theServer.getNameSexTables();
    }

    public int saveNameSexRecord(NameSexRecord nameSexRecord) throws RemoteException, SecurityException {
        checkPermission("saveNameSexRecord");
        return theServer.saveNameSexRecord(nameSexRecord);
    }

    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        checkPermission("clearNameSexTable");
        return theServer.clearNameSexTable();
    }

    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        checkPermission("getUserRightLevel");
        RMILoginPrincipal principal = (RMILoginPrincipal) theUser.getPrincipals().toArray()[0];

        /*
        Globals.UserRightLevels level = theServer.getUserRightLevel();
        // Ad hoc to test the user levels in the GUI
        String userName = principal.getName();
        if (userName.equalsIgnoreCase("morten")){
        level = Globals.UserRightLevels.SUPERVISOR;
        }*/

        return principal.getUserRightLevel();
    }

    public Map<Integer, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException {
        checkPermission("performPersonSearch");
        return theServer.performPersonSearch(patient, searcher);
    }

    public Map<Integer, Map<Float, Integer>> performGlobalPersonSearch(PersonSearcher searcher) throws RemoteException, SecurityException {
        checkPermission("performGlobalPersonSearch");
        return theServer.performGlobalPersonSearch(searcher);
    }

    public boolean deleteRecord(int id, String tableName) throws RemoteException, SecurityException {
        checkPermission("deleteRecord: " + tableName);
        return theServer.deleteRecord(id, tableName);
    }

    public boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException {
        checkPermission("deletePopulationDataset");
        return theServer.deletePopulationDataset(populationDatasetID);
    }

    public Vector<User> listUsers() throws RemoteException, SecurityException {
        checkPermission("listUsers");
        return theServer.listUsers();
    }
}
