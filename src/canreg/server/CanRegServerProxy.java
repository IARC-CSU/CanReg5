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
import canreg.server.security.ValidateMethodCall;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
class CanRegServerProxy extends UnicastRemoteObject implements CanRegServerInterface {

    private CanRegServerInterface theServer;
    private Subject theUser;

    public CanRegServerProxy(Subject user, CanRegServerInterface server/*<ictl.co>*/,int port/*</ictl.co>*/) throws RemoteException {
        //<ictl.co>
        super(port);
        //</ictl.co>
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

    @Override
    public void addUser(User user) throws RemoteException, SecurityException {
        checkPermission("addUser");
        theServer.addUser(user);
    }

    @Override
    public void removeUser(User user) throws RemoteException, SecurityException {
        checkPermission("removeUser");
        theServer.removeUser(user);
    }

    @Override
    public void setUserPassword(String username, String password) throws RemoteException, SecurityException {
        RMILoginPrincipal principal = (RMILoginPrincipal) theUser.getPrincipals().toArray()[0];
        username = principal.getName();
        theServer.setUserPassword(username, password);
    }

    @Override
    public String getCanRegSystemName() throws RemoteException, SecurityException {
        checkPermission("getCanRegSystemName");
        return theServer.getCanRegSystemName();
    }

    @Override
    public void startNetworkDBServer() throws RemoteException, SecurityException {
        checkPermission("startNetworkDBServer");
        theServer.startNetworkDBServer();
    }

    @Override
    public void stopNetworkDBServer() throws RemoteException, SecurityException {
        checkPermission("stopNetworkDBServer");
        theServer.stopNetworkDBServer();
    }

    @Override
    public Document getDatabseDescription() throws RemoteException, SecurityException {
        checkPermission("getDatabseDescription");
        return theServer.getDatabseDescription();
    }

    @Override
    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        checkPermission("listCurrentUsers");
        return theServer.listCurrentUsers();
    }

    @Override
    public void userLoggedIn(String username) throws RemoteException, SecurityException {
        checkPermission("userLoggedIn");
        theServer.userLoggedIn(username);
    }

    @Override
    public void userLoggedOut(String username) throws RemoteException, SecurityException {
        checkPermission("userLoggedOut");
        theServer.userLoggedOut(username);
    }

    @Override
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        // Cannot be sent over RMI. Good.
        checkPermission("getDatabseConnection");
        return theServer.getDatabseConnection();
    }

    @Override
    public int savePatient(Patient patient) throws RemoteException, SecurityException, SQLException, RecordLockedException {
        checkPermission("savePatient");
        return theServer.savePatient(patient);
    }

    @Override
    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException, SQLException, RecordLockedException {
        checkPermission("saveTumour");
        return theServer.saveTumour(tumour);
    }

    @Override
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException {
        checkPermission("saveDictionaryEntry");
        return theServer.saveDictionaryEntry(dictionaryEntry);
    }

    @Override
    public String performBackup() throws RemoteException, SecurityException {
        checkPermission("performBackup");
        return theServer.performBackup();
    }

    @Override
    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        checkPermission("restoreFromBackup");
        return theServer.restoreFromBackup(path);
    }

    @Override
    public String getCanRegVersion() throws RemoteException, SecurityException {
        checkPermission("getCanRegVersion");
        return theServer.getCanRegVersion();
    }

    @Override
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        checkPermission("deleteDictionaryEntries");
        return theServer.deleteDictionaryEntries(dictionaryID);
    }

    @Override
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        checkPermission("getDictionary");
        return theServer.getDictionary();
    }

    @Override
    public InetAddress getIPAddress() throws RemoteException, SecurityException {
        checkPermission("getIPAddress");
        return theServer.getIPAddress();
    }

    @Override
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws RemoteException, SecurityException, SQLException, UnknownTableException, DistributedTableDescriptionException {
        checkPermission("getDistributedTableDescription");
        return theServer.getDistributedTableDescription(filter, tableName);
    }

    public Object[][] retrieveRows(Subject theUser, int from, int to) throws RemoteException, SecurityException, Exception {
        throw new UnsupportedOperationException("Not supported."); // This should not be implemented!
    }

    @Override
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock) throws RemoteException, SecurityException, RecordLockedException {
        checkPermission("get:" + tableName);
        return theServer.getRecord(recordID, tableName, lock);
    }

    @Override
    public void editPatient(Patient patient) throws RemoteException, SecurityException, RecordLockedException {
        checkPermission("editPatient");
        theServer.editPatient(patient);
    }

    @Override
    public void editTumour(Tumour tumour) throws RemoteException, SecurityException, RecordLockedException {
        checkPermission("editTumour");
        theServer.editTumour(tumour);
    }

    @Override
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException {
        checkPermission("retrieveRows:" + resultSetID);
        return theServer.retrieveRows(resultSetID, from, to);
    }

    @Override
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException, SQLException {
        checkPermission("retrieveRows:" + resultSetID);
        theServer.releaseResultSet(resultSetID);
    }

    @Override
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        checkPermission("getPopulationDatasets");
        return theServer.getPopulationDatasets();
    }

    @Override
    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        checkPermission("saveNewPopulationDataset");
        return theServer.saveNewPopulationDataset(pds);
    }

    @Override
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        checkPermission("getDateOfLastBackUp");
        return theServer.getDateOfLastBackUp();
    }

    @Override
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {
        checkPermission("getNameSexTables");
        return theServer.getNameSexTables();
    }

    @Override
    public int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace) throws RemoteException, SecurityException {
        checkPermission("saveNameSexRecord");
        return theServer.saveNameSexRecord(nameSexRecord, replace);
    }

    @Override
    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        checkPermission("clearNameSexTable");
        return theServer.clearNameSexTable();
    }

    @Override
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

    @Override
    public Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException {
        checkPermission("performPersonSearch");
        return theServer.performPersonSearch(patient, searcher);
    }

    @Override
    public String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException {
        checkPermission("performGlobalPersonSearch");
        return theServer.initiateGlobalPersonSearch(searcher, rangeStart, rangeEnd);
    }

    @Override
    public boolean deleteRecord(int id, String tableName) throws RemoteException, SecurityException, RecordLockedException, SQLException {
        checkPermission("deleteRecord: " + tableName);
        return theServer.deleteRecord(id, tableName);
    }

    @Override
    public boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException {
        checkPermission("deletePopulationDataset");
        return theServer.deletePopulationDataset(populationDatasetID);
    }

    @Override
    public List<User> listUsers() throws RemoteException, SecurityException {
        checkPermission("listUsers");
        return theServer.listUsers();
    }

    @Override
    public int saveUser(User user) throws RemoteException, SecurityException {
        checkPermission("saveUser");
        return theServer.saveUser(user);
    }

    @Override
    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, Exception {
        checkPermission("nextStepGlobalPersonSearch" + idString);
        return theServer.nextStepGlobalPersonSearch(idString);
    }

    @Override
    public void interuptGlobalPersonSearch(String idString) throws RemoteException, SecurityException {
        checkPermission("interuptGlobalPersonSearch:" + idString);
        theServer.interuptGlobalPersonSearch(idString);
    }

    @Override
    public void releaseRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        checkPermission("releaseRecord: " + tableName + "-" + recordID);
        theServer.releaseRecord(recordID, tableName);
    }

    @Override
    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException {
        checkPermission("getDatabaseStats");
        return theServer.getDatabaseStats();
    }

    @Override
    public void shutDownServer() throws RemoteException, SecurityException {
        checkPermission("shutDownServer");
        theServer.shutDownServer();
    }

    @Override
    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray) throws RemoteException, SecurityException {
        checkPermission("setDBPassword");
        return theServer.setDBPassword(newPasswordArray, oldPasswordArray);
    }
}