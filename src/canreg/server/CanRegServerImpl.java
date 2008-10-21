package canreg.server;

import cachingtableapi.DistributedTableDescription;
import canreg.common.DatabaseFilter;
import canreg.common.Globals;
import canreg.common.Globals.UserRightLevels;
import canreg.common.PersonSearchVariable;
import canreg.common.Tools;
import canreg.common.qualitycontrol.PersonSearch;
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
import java.util.TreeMap;
import org.w3c.dom.Document;

/**
 *
 * @author morten
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

    public CanRegServerImpl(String systemCode) throws RemoteException {
        this.systemCode = systemCode;

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
        personSearcher = new PersonSearch(
                Tools.getVariableListElements(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
        PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE);
        personSearcher.setSearchVariables(searchVariables);
        personSearcher.setThreshold(Tools.getPersonSearchMinimumMatch(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
    }

    // Initialize the database connection
    private boolean initDataBase() {
        boolean success = false;

        // Connect to the database
        db = new CanRegDAO(systemCode, systemDescription.getSystemDescriptionDocument());
        db.connect();

        if (db != null) {
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
    public void startNetworkDBServer() {
        try {
            debugOut("Start Network DB Server.");
            dbServer = new NetworkServerControl(InetAddress.getByName("localhost"), 1528);
            dbServer.start(null);
        } catch (Exception ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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

    public Document getDatabseDescription() throws RemoteException, SecurityException {
        return systemDescription.getSystemDescriptionDocument();
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
        String name = null;

        if (systemDescription != null) {
            name = systemDescription.getSystemName();
        }
        return name;
    }

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

    public void userLoggedIn(String username)
            throws RemoteException, SecurityException {
        fClients.add(username);
    }

    public void userLoggedOut(String username)
            throws RemoteException, SecurityException {
        fClients.remove(fClients.indexOf(username));
    }

    // For testing purposes only - not secure enough...
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        return db;
    }

    private void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[CanRegServer] " + msg);
        }
    }
    // add and remove records
    public int savePatient(Patient patient) {
        return db.savePatient(patient);
    }

    public int saveTumour(Tumour tumour) {
        return db.saveTumour(tumour);
    }

    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        return db.saveDictionaryEntry(dictionaryEntry);
    }

    public String performBackup() throws RemoteException, SecurityException {
        systemSettings.setDateOfLastbackup(new Date());
        systemSettings.writeSettings();
        return db.performBackup();
    }

    public String getCanRegVersion() throws RemoteException, SecurityException {
        return Globals.VERSION_STRING;
    }

    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        return db.deleteDictionaryEntries(dictionaryID);
    }

    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        return db.getDictionary();
    }

    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        return db.restoreFromBackup(path);
    }

    public InetAddress getIPAddress() throws RemoteException, SecurityException {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(CanRegServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addr;
    }

    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName) throws RemoteException, SecurityException, SQLException, Exception {
        return db.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, tableName);
    }

    public DatabaseRecord getPatient(int patientID) throws RemoteException, SecurityException {
        return db.getPatient(patientID);
    }

    public DatabaseRecord getRecord(int recordID, String tableName) throws RemoteException, SecurityException {
        return db.getRecord(recordID, tableName);
    }

    public void editPatient(Patient patient) throws RemoteException, SecurityException {
        db.editPatient(patient);
    }

    public void editTumour(Tumour tumour) throws RemoteException, SecurityException {
        db.editTumour(tumour);
    }

    public Object[][] retrieveRows(String resultSetID, int from, int to) throws RemoteException, SecurityException, Exception {
        return db.retrieveRows(resultSetID, from, to);
    }

    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException {
        db.releaseResultSet(resultSetID);
    }

    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        return db.getPopulationDatasets();
    }

    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        return db.saveNewPopulationDataset(pds);
    }

    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        return systemSettings.getDateOfLastBackUp();
    }

    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {
        return db.getNameSexTables();
    }

    public int saveNameSexRecord(NameSexRecord nameSexRecord) throws RemoteException, SecurityException {
        return db.saveNameSexRecord(nameSexRecord);
    }

    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        return db.clearNameSexTable();
    }

    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        // For now everyone is a supervisor...
        return Globals.UserRightLevels.REGISTRAR;
    }

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
            Logger.getLogger(PersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

        return patientIDScorePatientIDMap;
    }

    public Map<Integer, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException {
        DatabaseFilter filter = new DatabaseFilter();
        Map<Integer, Float> patientIDScoreMap = new TreeMap<Integer, Float>();
        filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);
        DistributedTableDescription dataDescription;
        String resultSetID;
        int patientIDA = (Integer) patient.getVariable("ID");
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
                            System.out.println("Found " + patientIDB + " " + score);
                        } else {
                            // System.out.println("Not found " + patientIDB + " " + score);
                        }
                    }
                }
            }
            releaseResultSet(resultSetID);
        } catch (SQLException ex) {
            Logger.getLogger(PersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PersonSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return patientIDScoreMap;
    }
}
