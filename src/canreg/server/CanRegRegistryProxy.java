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
 * the method. ALWAYS use this class for every operation, and NEVER use directly an
 * instance of any other CanRegServerInterface. 
 *  
 * @author Patricio Carranza, patocarranza@gmail.com
 */
public class CanRegRegistryProxy implements CanRegServerInterface, Serializable {
    
    private final CanRegServerInterface serverProxy;
    private static CanRegServerInterface realServer;
    private final String registryCode;
    private static CanRegRegistryProxy singleton;
    
    
    /**
     * When creating the first CanRegRegistryProxy it also creates the client RMI connection, 
     * and more than 1 RMI connection against the same server is not desired. That;s why
     * this is a singleton-ish.
     * @param realServerParam MUST be an instance of CanRegServerImpl
     * @param registryCode
     * @param user
     * @return
     * @throws RemoteException 
     */
    public static CanRegRegistryProxy getInstance(CanRegServerInterface realServerParam, String registryCode, Subject user)
            throws RemoteException{
        if(singleton == null)
            singleton = new CanRegRegistryProxy(realServerParam, registryCode, user);
        else {
            if( ! realServerParam.equals(realServer))
                singleton = new CanRegRegistryProxy(realServerParam, registryCode, user);
        }
        return singleton;
    }
    
    private CanRegRegistryProxy(CanRegServerInterface realServerParam, String registryCode, Subject user) 
            throws RemoteException {
        this.serverProxy = new CanRegServerProxy(user, realServerParam);
        realServer = realServerParam;
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
     * connects to holding database.
     * This new instance will have the same RMI client connection as the singleton-ish
     * instance that you have acquired through getInstance().
     * @param originalRegistryCode
     * @param holdingRegistryCode
     * @param registryCode
     * @return 
     */
    public CanRegRegistryProxy getInstanceForHoldingDB(String originalRegistryCode, String holdingRegistryCode)
            throws RemoteException {
        SystemDescription sysDesc = serverProxy.initSystemDescription(originalRegistryCode, holdingRegistryCode, true);
        initDataBase(sysDesc, true);
        return new CanRegRegistryProxy(serverProxy, holdingRegistryCode);
    }
    
    private Object invokeMethodOnSpecificRegistry(String methodName, Class<?>[] parameterTypes, Object... parameters) {
        try {
            changeRegistryDB(registryCode);
            Method method = serverProxy.getClass().getMethod(methodName, parameterTypes);
            Object toReturn = method.invoke(serverProxy, parameters);
            resetRegistryDB();
            return toReturn;
        } catch (Exception ex) {
            Logger.getLogger(CanRegRegistryProxy.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } 
    }
    
    @Override
    public void addUser(User user) throws RemoteException, SecurityException {
//        invokeMethodOnSpecificRegistry("addUser", new Class[] {User.class}, new User[]{user});
        changeRegistryDB(registryCode);
        serverProxy.addUser(user);
        resetRegistryDB();
    }

    @Override
    public void removeUser(User user) throws RemoteException, SecurityException {
//        invokeMethodOnSpecificRegistry("removeUser", new Class[] {User.class}, new User[]{user});
        changeRegistryDB(registryCode);
        serverProxy.removeUser(user);
        resetRegistryDB();
    }

    @Override
    public void setUserPassword(String username, String password) throws RemoteException, SecurityException {
//        invokeMethodOnSpecificRegistry("setUserPassword", new Class[] {String.class, String.class}, new String[]{username, password});
        changeRegistryDB(registryCode);
        serverProxy.setUserPassword(username, password);
        resetRegistryDB();
    }

    @Override
    public String getCanRegRegistryName() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String str = serverProxy.getCanRegRegistryName();
        resetRegistryDB();
        return str;
    }

    @Override
    public void startNetworkDBServer() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.startNetworkDBServer();
        resetRegistryDB();
    }

    @Override
    public void stopNetworkDBServer() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.stopNetworkDBServer();
        resetRegistryDB();
    }

    @Override
    public Document getDatabseDescription() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Document doc = serverProxy.getDatabseDescription();
        resetRegistryDB();
        return doc;
    }

    @Override
    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String[] list = serverProxy.listCurrentUsers();
        resetRegistryDB();
        return list;
    }

    @Override
    public void userLoggedIn(String username) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.userLoggedIn(username);
        resetRegistryDB();
    }

    @Override
    public void userLoggedOut(String username) throws RemoteException, SecurityException {        
        changeRegistryDB(registryCode);
        serverProxy.userLoggedOut(username);
        resetRegistryDB();
    }

    @Override
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        CanRegDAO dao = serverProxy.getDatabseConnection();
        resetRegistryDB();
        return dao;
    }

    @Override
    public int savePatient(Patient patient) throws RemoteException, SecurityException, SQLException, RecordLockedException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.savePatient(patient);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveTumour(Tumour tumour) throws RemoteException, SecurityException, SQLException, RecordLockedException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveTumour(tumour);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveDictionaryEntry(dictionaryEntry);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public String performBackup() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String toReturn = serverProxy.performBackup();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String toReturn = serverProxy.restoreFromBackup(path);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public String getCanRegVersion() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String toReturn = serverProxy.getCanRegVersion();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.deleteDictionaryEntries(dictionaryID);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Map<Integer, Dictionary> toReturn = serverProxy.getDictionary();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public InetAddress getIPAddress() throws RemoteException, SecurityException {        
        changeRegistryDB(registryCode);
        InetAddress toReturn = serverProxy.getIPAddress();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) 
            throws RemoteException, SecurityException, SQLException, UnknownTableException, DistributedTableDescriptionException {
        changeRegistryDB(registryCode);
        DistributedTableDescription toReturn = serverProxy.getDistributedTableDescription(filter, tableName);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock) 
            throws RemoteException, SecurityException, RecordLockedException {
        changeRegistryDB(registryCode);
        DatabaseRecord toReturn = serverProxy.getRecord(recordID, tableName, lock);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public void editPatient(Patient patient) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        changeRegistryDB(registryCode);
        serverProxy.editPatient(patient);
        resetRegistryDB();
    }

    @Override
    public void editTumour(Tumour tumour) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        changeRegistryDB(registryCode);
        serverProxy.editTumour(tumour);
        resetRegistryDB();
    }

    @Override
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Object[][] toReturn = serverProxy.retrieveRows(resultSetID, from, to);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException, SQLException {
        changeRegistryDB(registryCode);
        serverProxy.releaseResultSet(resultSetID);
        resetRegistryDB();
    }

    @Override
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Map<Integer, PopulationDataset> toReturn = serverProxy.getPopulationDatasets();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveNewPopulationDataset(pds);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Date toReturn = serverProxy.getDateOfLastBackUp();
        resetRegistryDB();
        return toReturn;
    }    

    @Override
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {                
        changeRegistryDB(registryCode);
        Map<String, Integer> toReturn = serverProxy.getNameSexTables();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace)
            throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveNameSexRecord(nameSexRecord, replace);
        resetRegistryDB();
        return toReturn;
    }    

    @Override
    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.clearNameSexTable();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public Globals.UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Globals.UserRightLevels toReturn = serverProxy.getUserRightLevel();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher) 
            throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        Map<String, Float> toReturn = serverProxy.performPersonSearch(patient, searcher);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String toReturn = serverProxy.initiateGlobalPersonSearch(searcher, rangeStart, rangeEnd);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public boolean deleteRecord(int id, String tableName) throws RemoteException, SecurityException, RecordLockedException, SQLException {
        changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.deleteRecord(id, tableName);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.deletePopulationDataset(populationDatasetID);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public List<User> listUsers() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        List<User> toReturn = serverProxy.listUsers();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public int saveUser(User user) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.saveUser(user);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, Exception {
        changeRegistryDB(registryCode);
        Map<String, Map<String, Float>> toReturn = serverProxy.nextStepGlobalPersonSearch(idString);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public void interuptGlobalPersonSearch(String idString) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.interuptGlobalPersonSearch(idString);
        resetRegistryDB();
    }

    @Override
    public void releaseRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.releaseRecord(recordID, tableName);
        resetRegistryDB();
    }

    @Override
    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        DatabaseStats toReturn = serverProxy.getDatabaseStats();
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public void shutDownServer() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.shutDownServer();
        resetRegistryDB();
    }

    @Override
    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray, String encryptionAlgorithm, String encryptionKeyLength) 
            throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        boolean toReturn = serverProxy.setDBPassword(newPasswordArray, oldPasswordArray, encryptionAlgorithm, encryptionKeyLength);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public String getCanRegRegistryCode() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String toReturn = serverProxy.getCanRegRegistryCode();
        resetRegistryDB();
        return toReturn;
    }
    
    @Override
    public String getCanRegSystemRegion() throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        String toReturn = serverProxy.getCanRegSystemRegion();
        resetRegistryDB();
        return toReturn;
    }    
    
    @Override
    public int getLastHoldingDBnumber(String registryCode) 
            throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        int toReturn = serverProxy.getLastHoldingDBnumber(registryCode);
        resetRegistryDB();
        return toReturn;
    }
    
    @Override
    public SystemDescription createNewHoldingDB(String registryCode, String dbName, SystemDescription sysDesc)
            throws RemoteException, IOException, SecurityException {
        changeRegistryDB(registryCode);
        SystemDescription toReturn = serverProxy.createNewHoldingDB(registryCode, dbName, sysDesc);
        resetRegistryDB();
        return toReturn;
    }
    
    @Override
    public void changeRegistryDB(String registryCode) 
            throws RemoteException, SecurityException {
        serverProxy.changeRegistryDB(registryCode);
    }
    
    @Override
    public void resetRegistryDB() throws RemoteException, SecurityException {
        //serverProxy.resetRegistryDB();
    }

    @Override
    public SystemDescription initSystemDescription(String originalRegistryCode, 
                                                  String holdingRegistryCode, 
                                                  boolean holding) 
            throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        SystemDescription toReturn = serverProxy.initSystemDescription(originalRegistryCode, holdingRegistryCode, holding);
        resetRegistryDB();
        return toReturn;
    }

    @Override
    public void initDataBase(SystemDescription systemDescription, boolean holding) 
            throws RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        serverProxy.initDataBase(systemDescription, holding);
        resetRegistryDB();
    }

    @Override
    public List<String> getHoldingDBsList() throws IOException, RemoteException, SecurityException {
        changeRegistryDB(registryCode);
        List<String> strs = serverProxy.getHoldingDBsList();
        resetRegistryDB();
        return strs;
    }
}
