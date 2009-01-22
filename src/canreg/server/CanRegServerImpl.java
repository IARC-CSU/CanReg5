package canreg.server;

import cachingtableapi.DistributedTableDescription;
import canreg.common.DatabaseFilter;
import canreg.common.Globals;
import canreg.common.Globals.UserRightLevels;
import canreg.common.PersonSearchVariable;
import canreg.common.Tools;
import canreg.common.qualitycontrol.DefaultPersonSearch;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.CanRegDAO;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Dictionary;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.NameSexRecord;
import canreg.server.database.Patient;
import canreg.server.database.PopulationDataset;
import canreg.server.database.Tumour;
import canreg.server.management.SystemDescription;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.apache.derby.drda.NetworkServerControl;
import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class CanRegServerImpl extends UnicastRemoteObject implements CanRegServerInterface {

    private static boolean debug = true;
    Vector fClients = new Vector();
    private CanRegDAO db;
    private NetworkServerControl dbServer;
    private SystemDescription systemDescription;
    private String systemCode;
    private SystemSettings systemSettings;
    private PersonSearcher personSearcher;
    private Properties appInfoProperties;

    /**
     * 
     * @param systemCode
     * @throws java.rmi.RemoteException
     */
    public CanRegServerImpl(String systemCode) throws RemoteException {
        this.systemCode = systemCode;

        appInfoProperties = new Properties();
        InputStream in = null;
        //
        // load properties file
        //
        try {
            //
            // get Application information
            //
            in = getClass().getResourceAsStream(Globals.APPINFO_PROPERTIES_PATH);
            appInfoProperties.load(in);
            in.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } // end-try-catch

        // Step one load the system definition...
        if (!initSystemDefinition()) {
            throw new RemoteException("Faulty system definitions...");
        }
        // Step two: start the database...
        if (!initDataBase()) {
            throw new RemoteException("Cannot initialize database...");
        }
        try {
            systemSettings = new SystemSettings(systemCode + "settings.xml");
        } catch (IOException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Step three: initiate the quality controllers
        personSearcher = new DefaultPersonSearch(
                Tools.getVariableListElements(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
        PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE);
        personSearcher.setSearchVariables(searchVariables);
        personSearcher.setThreshold(Tools.getPersonSearchMinimumMatch(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
    }

    // Initialize the database connection
    private boolean initDataBase() {
        boolean success = false;
        boolean connected = false;

        // Connect to the database
        db = new CanRegDAO(systemCode, systemDescription.getSystemDescriptionDocument());
        connected = db.connect();

        if (connected && db != null) {
            success = true;
        }

        return success;
    }

    // Initialize the system description object
    private boolean initSystemDefinition() {
        boolean success = false;

        // Load the system description object
        systemDescription = new SystemDescription(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + systemCode + ".xml");

        if (systemDescription.getSystemDescriptionDocument() != null) {
            success = true;
        }
        return success;
    }

    // This lets one connect to the CanReg database from other programs
    // to connect from an ODBC compliant program you have to "prepare" the
    // connection using db2-tools available here: 
    // http://www-1.ibm.com/support/docview.wss?rs=71&uid=swg21256059
    // Download and install the DB2 Run-Time Client
    // Windows: https://www6.software.ibm.com/dl/rtcl/rtcl-p
    // Linux: https://www6.software.ibm.com/dl/cloudscape/cloudscape-i?S_PKG=db2rtlin_ww
    //
    // Afterwards:
    // Linux: http://www.ibm.com/developerworks/db2/library/techarticle/dm-0409cline2/readme_linux.txt
    // Windows: http://www.ibm.com/developerworks/db2/library/techarticle/dm-0409cline2/readme_win.txt
    //
    /**
     * 
     */
    public void startNetworkDBServer() {
        try {
            debugOut("Start Network DB Server.");
            dbServer = new NetworkServerControl(InetAddress.getByName("localhost"), 1528);
            dbServer.start(null);
        } catch (Exception ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     */
    public void stopNetworkDBServer() {
        try {
            if (dbServer != null) {
                debugOut("Stop Network DB Server.");
                dbServer.shutdown();
            }
        } catch (Exception ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** The first operation. */
    public void doOperationA() {
        debugOut("Operation A!");
    }

    /** The second operation. */
    public void doOperationB() {
        debugOut("Operation B!");
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Document getDatabseDescription() throws RemoteException, SecurityException {
        return systemDescription.getSystemDescriptionDocument();
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void addUser(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void removeUser(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void setUserPassword(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @param username
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getUserPassword(String username) throws RemoteException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getCanRegSystemName() throws RemoteException, SecurityException {
        String name = null;

        if (systemDescription != null) {
            name = systemDescription.getSystemName();
        }
        return name;
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        String[] users = new String[fClients.size()];
        int i = 0;
        for (Iterator it = fClients.iterator(); it.hasNext();) {
            users[i] = (String) it.next();
            System.out.println("element is " + users[i]);
            i++;
        }
        return users;
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void userLoggedIn(String username)
            throws RemoteException, SecurityException {
        fClients.add(username);
    }

    /**
     * 
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void userLoggedOut(String username)
            throws RemoteException, SecurityException {
        fClients.remove(fClients.indexOf(username));
    }

    // 
    /**
     * For testing purposes only - not secure enough...
     * Not used!
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        return db;
    }

    private void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[CanRegServer] " + msg);
        }
    }
    // add and remove records
    /**
     * 
     * @param patient
     * @return
     */
    public int savePatient(Patient patient) {
        return db.savePatient(patient);
    }

    /**
     * 
     * @param tumour
     * @return
     */
    public int saveTumour(Tumour tumour) {
        return db.saveTumour(tumour);
    }

    /**
     * 
     * @param dictionaryEntry
     * @return
     */
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        return db.saveDictionaryEntry(dictionaryEntry);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String performBackup() throws RemoteException, SecurityException {
        systemSettings.setDateOfLastbackup(new Date());
        systemSettings.writeSettings();
        return db.performBackup();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String getCanRegVersion() throws RemoteException, SecurityException {
        String versionString = "";
        for (String part : Globals.versionStringParts) {
            versionString += appInfoProperties.getProperty(part);
        }
        return versionString;
    }

    /**
     * 
     * @param dictionaryID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        return db.deleteDictionaryEntries(dictionaryID);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        return db.getDictionary();
    }

    /**
     * 
     * @param path
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        return db.restoreFromBackup(path);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public InetAddress getIPAddress() throws RemoteException, SecurityException {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addr;
    }

    /**
     * 
     * @param filter
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws java.sql.SQLException
     * @throws java.lang.Exception
     */
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws RemoteException, SecurityException, SQLException, Exception {
        return db.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, tableName);
    }

    /**
     * 
     * @param patientID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public DatabaseRecord getPatient(int patientID) throws RemoteException, SecurityException {
        return db.getPatient(patientID);
    }

    /**
     * 
     * @param recordID
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public DatabaseRecord getRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        return db.getRecord(recordID, tableName);
    }

    /**
     * 
     * @param patient
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void editPatient(Patient patient) throws RemoteException, SecurityException {
        db.editPatient(patient);
    }

    /**
     * 
     * @param tumour
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void editTumour(Tumour tumour) throws RemoteException, SecurityException {
        db.editTumour(tumour);
    }

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
    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException, Exception {
        return db.retrieveRows(resultSetID, from, to);
    }

    /**
     * 
     * @param resultSetID
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException {
        db.releaseResultSet(resultSetID);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        return db.getPopulationDatasets();
    }

    /**
     * 
     * @param pds
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        return db.saveNewPopulationDataset(pds);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        return systemSettings.getDateOfLastBackUp();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {
        return db.getNameSexTables();
    }

    /**
     * 
     * @param nameSexRecord
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public int saveNameSexRecord(NameSexRecord nameSexRecord) throws RemoteException, SecurityException {
        return db.saveNameSexRecord(nameSexRecord);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        return db.clearNameSexTable();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        // For now everyone is a supervisor...
        return Globals.UserRightLevels.REGISTRAR;
    }

    /**
     * 
     * @param searcher
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, Map<Float, Integer>> performGlobalPersonSearch(PersonSearcher searcher) throws RemoteException, SecurityException {
        Map<Integer, Map<Float, Integer>> patientIDScorePatientIDMap = new TreeMap<Integer, Map<Float, Integer>>();
        DatabaseFilter filter = new DatabaseFilter();
        filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);
        DistributedTableDescription dataDescription;
        String resultSetID;
        try {
            dataDescription = db.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, Globals.PATIENT_TABLE_NAME);
            if (searcher == null) {
                // listener.actionPerformed(new ActionEvent(this, 0, "range " + dataDescription.getRowCount()));
                // searcher = personSearcher;
            }
            float threshold = searcher.getThreshold();
            resultSetID = dataDescription.getResultSetID();
            Patient patientA;
            Patient patientB;
            for (int row = 0; row < dataDescription.getRowCount(); row++) {
                Object[][] rowData = db.retrieveRows(resultSetID, row, dataDescription.getRowCount());
                for (Object[] r : rowData) {
                    int patientIDA = (Integer) r[0];
                    Map<Float, Integer> patientIDScoreMap = new TreeMap<Float, Integer>();
                    patientA = (Patient) getPatient(patientIDA);
                    for (Object[] r2 : rowData) {
                        int patientIDB = (Integer) r2[0];
                        if (patientIDB != patientIDA) {
                            patientB = (Patient) getPatient(patientIDB);
                            float score = searcher.compare(patientA, patientB);
                            if (score > threshold) {
                                patientIDScoreMap.put(score, patientIDB);
                                System.out.println("Found " + patientIDA + " " + score + " " + patientIDB);
                            }
                        }
                    }
                    if (patientIDScoreMap.size() > 0) {
                        patientIDScorePatientIDMap.put(patientIDA, patientIDScoreMap);
                    }
                }
            }
            releaseResultSet(resultSetID);
        } catch (SQLException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

        return patientIDScorePatientIDMap;
    }

    /**
     * 
     * @param patient
     * @param searcher
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    public Map<Integer, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException {
        DatabaseFilter filter = new DatabaseFilter();
        Map<Integer, Float> patientIDScoreMap = new TreeMap<Integer, Float>();
        filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);
        DistributedTableDescription dataDescription;
        String resultSetID;
        Object patientIDAObject = patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);

        int patientIDA;
        if (patientIDAObject != null) {
            patientIDA = (Integer) patientIDAObject;
        } else {
            patientIDA = -1;
        }
        
        try {
            dataDescription = db.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, Globals.PATIENT_TABLE_NAME);
            resultSetID = dataDescription.getResultSetID();
            Patient patientB;
            if (searcher == null) {
                searcher = personSearcher;
            }
            float threshold = searcher.getThreshold();
            for (int row = 0; row < dataDescription.getRowCount(); row++) {
                Object[][] rowData = db.retrieveRows(resultSetID, row, dataDescription.getRowCount());
                for (Object[] r : rowData) {
                    int patientIDB = (Integer) r[0];
                    if (patientIDB != patientIDA) {
                        patientB = (Patient) getPatient(patientIDB);
                        float score = personSearcher.compare(patient, patientB);
                        if (score > threshold) {
                            patientIDScoreMap.put(patientIDB, score);
                            System.out.println("Found patient id: " + patientIDB + ", score: " + score +"%");
                        } else {
                            // System.out.println("Not found " + patientIDB + " " + score);
                        }
                    }
                }
            }
            releaseResultSet(resultSetID);
        } catch (SQLException ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DefaultPersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientIDScoreMap;
    }

    public boolean deleteRecord(int id, String tableName) throws RemoteException, SecurityException {
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)){
            return db.deleteTumourRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            return db.deletePatientRecord(id);
        } else return false;
    }
}
