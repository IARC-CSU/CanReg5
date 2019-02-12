/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2019  International Agency for Research on Cancer
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
 * @author Patricio Carranza, patocarranza@gmail.com
 */
package canreg.server;

import canreg.common.DatabaseFilter;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDataset;
import canreg.common.database.Tumour;
import canreg.common.database.User;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.CanRegDAO;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import canreg.server.management.SystemDescription;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.w3c.dom.Document;

/**
 * This class intercepts every method that needs to be executed on the server. In
 * every interception it indicates to the server in which registry has to perform
 * the method. ALWAYS use this class for every operation, and NEVER use directy an
 * instance of any other CanRegServerInterface. 
 *  
 * @author Patricio Carranza, patocarranza@gmail.com
 */
public class CanRegRegistryProxy implements CanRegServerInterface, Serializable {
    
    private final CanRegServerInterface serverProxy;
    private final String registryCode;
    
    /**
     * 
     * @param realServer MUST be an instance of CanRegServerImpl
     * @param registryCode
     * @param user
     * @throws RemoteException 
     */
    public CanRegRegistryProxy(CanRegServerInterface realServer, String registryCode, Subject user) 
            throws RemoteException {
        this.serverProxy = new CanRegServerProxy(user, realServer);
        if(registryCode == null)
            throw new NullPointerException("registryCode cannot be null.");
        this.registryCode = registryCode;
    }
    
    private CanRegRegistryProxy(CanRegServerInterface serverProxy, String registryCode) {
        this.serverProxy = serverProxy;
        this.registryCode = registryCode;
    }
    
    /**
     * Use this method to obtain a new instance of CanRegRegistryProxy that 
     * connects to a different registry.
     * @param registryCode
     * @return 
     */
    public CanRegRegistryProxy getNewInstance(String registryCode) {
        return new CanRegRegistryProxy(serverProxy, registryCode);
    }
    
    private Object invokeMethodOnSpecificRegistry(String methodName, Class<?>[] parameterTypes, Object... parameters) {
        try {
            serverProxy.changeRegistryDB(registryCode);
            Method method = serverProxy.getClass().getMethod(methodName, parameterTypes);
            Object toReturn = method.invoke(serverProxy, parameters);
            serverProxy.resetRegistryDB();
            return toReturn;
        } catch (Exception ex) {
            Logger.getLogger(CanRegRegistryProxy.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } 
    }
    
    public String testMethod(String str ) {
        return (String) invokeMethodOnSpecificRegistry("testMethod", new Class[] {String.class}, new String[]{str});
    }
    
    @Override
    public void addUser(User user) throws RemoteException, SecurityException {
        invokeMethodOnSpecificRegistry("addUser", new Class[] {User.class}, new User[]{user});
    }

    @Override
    public void removeUser(User user) throws RemoteException, SecurityException {
        invokeMethodOnSpecificRegistry("removeUser", new Class[] {User.class}, new User[]{user});
//        serverProxy.removeUser(user);
    }

    @Override
    public void setUserPassword(String username, String password) throws RemoteException, SecurityException {
        invokeMethodOnSpecificRegistry("setUserPassword", new Class[] {String.class, String.class}, new String[]{username, password});
//        serverProxy.setUserPassword(username, password);
    }

    @Override
    public String getCanRegRegistryName() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String str = serverProxy.getCanRegRegistryName();
        serverProxy.resetRegistryDB();
        return str;
    }

    @Override
    public void startNetworkDBServer() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.startNetworkDBServer();
        serverProxy.resetRegistryDB();
    }

    @Override
    public void stopNetworkDBServer() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.stopNetworkDBServer();
        serverProxy.resetRegistryDB();
    }

    @Override
    public Document getDatabseDescription() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Document doc = serverProxy.getDatabseDescription();
        serverProxy.resetRegistryDB();
        return doc;
    }

    @Override
    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String[] list = serverProxy.listCurrentUsers();
        serverProxy.resetRegistryDB();
        return list;
    }

    @Override
    public void userLoggedIn(String username) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.userLoggedIn(username);
        serverProxy.resetRegistryDB();
    }

    @Override
    public void userLoggedOut(String username) throws RemoteException, SecurityException {        
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.userLoggedOut(username);
        serverProxy.resetRegistryDB();
    }

    @Override
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        CanRegDAO dao = serverProxy.getDatabseConnection();
        serverProxy.resetRegistryDB();
        return dao;
    }

    @Override
    public int savePatient(Patient patient) throws RemoteException, SecurityException, SQLException, RecordLockedException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.savePatient(patient);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException, SQLException, RecordLockedException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveTumour(tumour);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveDictionaryEntry(dictionaryEntry);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public String performBackup() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String toReturn = serverProxy.performBackup();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String toReturn = serverProxy.restoreFromBackup(path);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public String getCanRegVersion() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String toReturn = serverProxy.getCanRegVersion();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.deleteDictionaryEntries(dictionaryID);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Map<Integer, Dictionary> toReturn = serverProxy.getDictionary();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public InetAddress getIPAddress() throws RemoteException, SecurityException {        
        serverProxy.changeRegistryDB(registryCode);
        InetAddress toReturn = serverProxy.getIPAddress();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) 
            throws RemoteException, SecurityException, SQLException, UnknownTableException, DistributedTableDescriptionException {
        serverProxy.changeRegistryDB(registryCode);
        DistributedTableDescription toReturn = serverProxy.getDistributedTableDescription(filter, tableName);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    public Object[][] retrieveRows(Subject theUser, int from, int to) 
            throws RemoteException, SecurityException, Exception {
        throw new UnsupportedOperationException("Not supported."); // This should not be implemented!
    }

    @Override
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock) 
            throws RemoteException, SecurityException, RecordLockedException {
        serverProxy.changeRegistryDB(registryCode);
        DatabaseRecord toReturn = serverProxy.getRecord(recordID, tableName, lock);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public void editPatient(Patient patient) throws RemoteException, SecurityException, RecordLockedException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.editPatient(patient);
        serverProxy.resetRegistryDB();
    }

    @Override
    public void editTumour(Tumour tumour) throws RemoteException, SecurityException, RecordLockedException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.editTumour(tumour);
        serverProxy.resetRegistryDB();
    }

    @Override
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Object[][] toReturn = serverProxy.retrieveRows(resultSetID, from, to);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException, SQLException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.releaseResultSet(resultSetID);
        serverProxy.resetRegistryDB();
    }

    @Override
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Map<Integer, PopulationDataset> toReturn = serverProxy.getPopulationDatasets();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveNewPopulationDataset(pds);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Date toReturn = serverProxy.getDateOfLastBackUp();
        serverProxy.resetRegistryDB();
        return toReturn;
    }    

    @Override
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {                
        serverProxy.changeRegistryDB(registryCode);
        Map<String, Integer> toReturn = serverProxy.getNameSexTables();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace)
            throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveNameSexRecord(nameSexRecord, replace);
        serverProxy.resetRegistryDB();
        return toReturn;
    }    

    @Override
    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.clearNameSexTable();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public Globals.UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Globals.UserRightLevels toReturn = serverProxy.getUserRightLevel();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher) 
            throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        Map<String, Float> toReturn = serverProxy.performPersonSearch(patient, searcher);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String toReturn = serverProxy.initiateGlobalPersonSearch(searcher, rangeStart, rangeEnd);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public boolean deleteRecord(int id, String tableName) throws RemoteException, SecurityException, RecordLockedException, SQLException {
        serverProxy.changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.deleteRecord(id, tableName);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.deletePopulationDataset(populationDatasetID);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public List<User> listUsers() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        List<User> toReturn = serverProxy.listUsers();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveUser(User user) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveUser(user);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, Exception {
        serverProxy.changeRegistryDB(registryCode);
        Map<String, Map<String, Float>> toReturn = serverProxy.nextStepGlobalPersonSearch(idString);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public void interuptGlobalPersonSearch(String idString) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.interuptGlobalPersonSearch(idString);
        serverProxy.resetRegistryDB();
    }

    @Override
    public void releaseRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.releaseRecord(recordID, tableName);
        serverProxy.resetRegistryDB();
    }

    @Override
    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        DatabaseStats toReturn = serverProxy.getDatabaseStats();
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public void shutDownServer() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        serverProxy.shutDownServer();
        serverProxy.resetRegistryDB();
    }

    @Override
    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray, String encryptionAlgorithm, String encryptionKeyLength) 
            throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.setDBPassword(newPasswordArray, oldPasswordArray, encryptionAlgorithm, encryptionKeyLength);
        serverProxy.resetRegistryDB();
        return toReturn;
    }

    @Override
    public String getCanRegRegistryCode() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String toReturn = serverProxy.getCanRegRegistryCode();
        serverProxy.resetRegistryDB();
        return toReturn;
    }
    
    @Override
    public String getCanRegSystemRegion() throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        String toReturn = serverProxy.getCanRegSystemRegion();
        serverProxy.resetRegistryDB();
        return toReturn;
    }    
    
    @Override
    public int getLastHoldingDBnumber(String registryCode) 
            throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        int toReturn = serverProxy.getLastHoldingDBnumber(registryCode);
        serverProxy.resetRegistryDB();
        return toReturn;
    }
    
    @Override
    public SystemDescription createNewHoldingDB(String registryCode, String dbName, SystemDescription sysDesc)
            throws RemoteException, IOException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
        SystemDescription toReturn = serverProxy.createNewHoldingDB(registryCode, dbName, sysDesc);
        serverProxy.resetRegistryDB();
        return toReturn;
    }
    
    @Override
    public void changeRegistryDB(String registryCode) 
            throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
    }
    
    @Override
    public void resetRegistryDB() throws RemoteException, SecurityException {
        serverProxy.resetRegistryDB();
    }
}
