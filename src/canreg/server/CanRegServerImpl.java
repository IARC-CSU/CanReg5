/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2020  International Agency for Research on Cancer
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

import canreg.common.DatabaseFilter;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DefaultConfigFileUtils;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Globals.UserRightLevels;
import canreg.common.PersonSearchVariable;
import canreg.common.Tools;
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
import canreg.common.qualitycontrol.DefaultPersonSearch;
import canreg.common.qualitycontrol.GlobalPersonSearchHandler;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.CanRegDAO;
import canreg.server.database.Migrator;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import canreg.server.management.SystemDescription;
import canreg.server.management.UserManagerNew;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.apache.commons.lang.StringUtils;
import org.apache.derby.drda.NetworkServerControl;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class CanRegServerImpl extends UnicastRemoteObject implements CanRegServerInterface {

    private static final Logger LOGGER = Logger.getLogger(CanRegServerImpl.class.getName());
    private static boolean debug = true;

    private NetworkServerControl dbServer;
    private SystemDescription systemDescription;
    private String defaultRegistryCode;
    private SystemSettings systemSettings;
    private PersonSearcher personSearcher;
    private Properties appInfoProperties;
    private UserManagerNew userManager;
    private Map<String, GlobalPersonSearchHandler> activePersonSearchers;
    private String patientRecordIDvariableName;
    private GlobalToolBox serverToolbox;
    private TrayIcon trayIcon;
    private DefaultConfigFileUtils defaultConfigFileUtils;

    private CanRegDAO currentDAO;
    private HashMap<String, CanRegDAO> registriesDAOs;
    private Map<Integer, Object[]> patientsData;

    /**
     *
     * @param registryCode
     * @throws java.rmi.RemoteException
     */
    public CanRegServerImpl(String registryCode) throws RemoteException {
        // Prevent JAVA to use a random port.
        super(1099);
        initialize(registryCode, false);
    }

    public CanRegServerImpl(String registryCode, boolean isAdHocDB)
            throws RemoteException {
        // Prevent JAVA to use a random port.
        super(1099);
        initialize(registryCode, isAdHocDB);
    }

    private void initialize(String registryCode, boolean isAdHocDB) throws RemoteException {
        registriesDAOs = new HashMap<>();
        defaultRegistryCode = registryCode;
        defaultConfigFileUtils = new DefaultConfigFileUtils();
        LOGGER.log(Level.INFO, "Java version: {0}", System.getProperty("java.version"));

        // If we can we add a tray icon to show that the CanReg server is running.
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            java.net.URL imageURL = CanRegServerImpl.class.getResource("resources/LogoBetaNew32x32.png");
            if (imageURL != null) {
                Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
                trayIcon = new TrayIcon(image, "CanReg5 server starting...", null);
                trayIcon.setImageAutoSize(true);
                try {
                    tray.add(trayIcon);
                } catch (AWTException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }

        appInfoProperties = new Properties();
        InputStream in = null;
        //
        // load properties file
        //

        //
        // get Application information
        //
        in = getClass().getResourceAsStream(Globals.APPINFO_PROPERTIES_PATH);
        try {
            appInfoProperties.load(in);
            in.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        // Step one load the system definition...
        systemDescription = initSystemDescription(defaultRegistryCode, null, false, isAdHocDB);
        setTrayIconToolTip("CanReg5 server " + registryCode + " definitions read and initialized...");

        // Step two: start the database...
        initDataBase(systemDescription, false);
        currentDAO = registriesDAOs.get(defaultRegistryCode);

        setTrayIconToolTip("CanReg5 server " + registryCode + " database initialized...");
        try {
            systemSettings = new SystemSettings(registryCode + "settings.xml");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        setTrayIconToolTip("CanReg5 server " + registryCode + " database initialized...");

        // Step three: set up some variables
        serverToolbox = new GlobalToolBox(getDatabseDescription());
        patientRecordIDvariableName = serverToolbox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();

        // temp!
        // db.setSystemPropery("DATABASE_VERSION", "5.00.05");
        // migrate the database if necessary
        Migrator migrator = new Migrator(getCanRegVersion(), currentDAO);
        setTrayIconToolTip("Migrating CanReg5 " + registryCode + " database to newest version specification...");
        migrator.migrate();

        // Step four: initiate the quality controllers
        personSearcher = new DefaultPersonSearch(
                Tools.getVariableListElements(
                        systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
        PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE);
        personSearcher.setSearchVariables(searchVariables);
        personSearcher.setThreshold(Tools.getPersonSearchMinimumMatch(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));

        activePersonSearchers = new LinkedHashMap<String, GlobalPersonSearchHandler>();

        setTrayIconToolTip("CanReg5 " + registryCode + " server quality controllers initialized...");

        // Step five: start the user manager
        userManager = new UserManagerNew(currentDAO);
        userManager.writePasswordsToFile();

        setTrayIconToolTip("CanReg5 " + registryCode + " server user manager started initialized...");

        // Update the tooltip now that everything is up and running...
        setTrayIconToolTip("CanReg5 server " + systemDescription.getRegistryName() + " (" + registryCode + ") running");
        displayTrayIconPopUpMessage("Server running", "CanReg5 server " + systemDescription.getRegistryName() + " (" + registryCode + ") running", MessageType.INFO);
        
        // Step six: create the holding DBs for the API users
        initHoldingDbsForApiUsers(systemDescription);
    }

    /**
     * Initialize all the databases for the users allowed to run the "rest api".
     * @param mainSystemDescription the main system description
     * @throws RemoteException remote exception
     */
    private void initHoldingDbsForApiUsers(final SystemDescription mainSystemDescription) throws RemoteException {
        // TODO: add config to force or not the start of these databases
        Map<String, User> usersMap = currentDAO.getUsers();
        for(User apiUser : usersMap.values()) {
            // TODO: read the RESTAPI role in the configuration, use ANALYST until then 
            if(apiUser.getUserRightLevel().equals(UserRightLevels.ANALYST)) {
                LOGGER.info("Creating holding DB for User: " + apiUser.getUserName());
                String mainRegistryCode = getCanRegRegistryCode();
                try {
                    SystemDescription holdingSystemDescription = createNewApiHoldingDBSystemDescription(
                                    mainRegistryCode, 
                                    mainSystemDescription, 
                                    apiUser);
                    initDataBase(holdingSystemDescription, true);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error creating holding DB for " + apiUser.getUserName(), e);
                    // go on...
                }
            }
        }
    }

    /**
     * Create the system description for a user "rest api".
     * @param mainRegistryCode the main registry code, like TRN
     * @param mainSystemDescription the main system description
     * @param apiUser the api user
     * @return a new SystemDescription for the holding db for this api user
     * @throws SecurityException SecurityException
     */
    public SystemDescription createNewApiHoldingDBSystemDescription(String mainRegistryCode, SystemDescription mainSystemDescription, User apiUser)
            throws SecurityException {
        SystemDescription holdingSystemDescrption = new SystemDescription(mainSystemDescription.getDescriptionFilePath());
        File registryCodeHoldingFolder = getRegistryCodeHoldingFolder(mainRegistryCode);
        String holdingRegistryCode = getRegistryCodeForApiHolding(mainRegistryCode, apiUser, false);
        File holdingXmlPath = new File(registryCodeHoldingFolder, holdingRegistryCode);
        holdingXmlPath.mkdirs();
        File holdingXml = new File(holdingXmlPath, holdingRegistryCode + ".xml");
        holdingSystemDescrption.setRegistryCode(holdingRegistryCode);
        holdingSystemDescrption.setSystemDescriptionLocation(holdingXmlPath);
        holdingSystemDescrption.saveSystemDescriptionXML(holdingXml.getAbsolutePath());
        return holdingSystemDescrption;
    }

    /**
     * Build the registry code for a holding db for an api user.
     * @param mainRegistryCode main registry code like TRN
     * @param apiUser the api user
     * @param withDate true to add the date: _yyyy-mm-dd                 
     * @return "HOLDING_" + registryCode + "_" + normalizedUserName + "_" + yyyy-mm-dd
     *      or "HOLDING_" + registryCode + "_" + normalizedUserName
     */
    public String getRegistryCodeForApiHolding(String mainRegistryCode, User apiUser, boolean withDate) {
        String dateSuffix = "";
        if(withDate) {
            dateSuffix = new SimpleDateFormat("yyyy-MM-dd").format((Calendar.getInstance()).getTime());
        }
        // Normalize the user name: remove spaces and quotes
        String normalizedUserName = StringUtils.replaceChars(apiUser.getUserName().trim(), " '", "-");
        return "HOLDING_" + mainRegistryCode + "_" + normalizedUserName + dateSuffix;
    }

    @Override
    public void initDataBase(SystemDescription sysDesc, boolean holding)
            throws RemoteException {
        boolean connected = false;

        // Connect to the database
        CanRegDAO dao = null;
        try {
            dao = new CanRegDAO(sysDesc.getRegistryCode(), sysDesc.getSystemDescriptionDocument(), holding);
            connected = dao.connect();
        } catch (SQLException ex) {
            System.out.println("ERROR ***");
             System.out.println(ex.getCause());
             System.out.println("Error-code: " + ex.getErrorCode());
             System.out.println("SQL-state:" + ex.getSQLState());
            // If we reach this step and get a SQLexception - try with password
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(null, pf, "Please enter the database boot password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (okCxl == JOptionPane.OK_OPTION) {
                String password = new String(pf.getPassword());
                if (password.length() > 0) {
                    try {
                        dao = new CanRegDAO(sysDesc.getRegistryCode(), sysDesc.getSystemDescriptionDocument(), holding);
                        connected = dao.connectWithBootPassword(password.toCharArray());
                    } catch (RemoteException | SQLException ex1) {
                        LOGGER.log(Level.SEVERE, null, ex1);
                        throw new RuntimeException(ex1);
                    }
                }
            } else {
                throw new RuntimeException(ex);
            }
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw ex;
        }

        if (connected && dao != null) {
            registriesDAOs.put(sysDesc.getRegistryCode(), dao);
        } else {
            throw new RuntimeException("Connection to Database not possible.");
        }
    }

    @Override
    public SystemDescription initSystemDescription(String originalRegistryCode, String holdingRegistryCode, boolean holding, boolean isAdHocDB) {
        SystemDescription sysDesc = null;

        if (isAdHocDB) {
            sysDesc = new SystemDescription(Globals.CANREG_SERVER_ADHOC_DB_SYSTEM_DESCRIPTION_FOLDER + Globals.FILE_SEPARATOR + originalRegistryCode + ".xml");
        } else {
            if (holding) {
                sysDesc = new SystemDescription(getRegistryCodeHoldingFolder(originalRegistryCode).getAbsolutePath()
                        + Globals.FILE_SEPARATOR + holdingRegistryCode + Globals.FILE_SEPARATOR + holdingRegistryCode + ".xml");
            } else {
                sysDesc = new SystemDescription(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + originalRegistryCode + ".xml");
            }
        }

        if (sysDesc.getSystemDescriptionDocument() == null) {
            throw new RuntimeException("Failed to initiate System Definition");
        }

        return sysDesc;
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
    @Override
    public void startNetworkDBServer() {
        try {
            debugOut("Start Network DB Server.");
            dbServer = new NetworkServerControl(InetAddress.getByName("localhost"), 1528);
            dbServer.start(null);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Override
    public void stopNetworkDBServer() {
        try {
            if (dbServer != null) {
                debugOut("Stop Network DB Server.");
                dbServer.shutdown();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public final Document getDatabseDescription() throws RemoteException, SecurityException {
        return systemDescription.getSystemDescriptionDocument();
    }

    /**
     *
     * @param user
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void addUser(User user) throws RemoteException, SecurityException {
        userManager.addUser(user);
    }

    /**
     *
     * @param user
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void removeUser(User user) throws RemoteException, SecurityException {
        userManager.removeUser(user);
    }

    /**
     * Change the password corresponding to the username. The password can only be changed by the user itself.
     *
     * @param username username
     * @param password password to change
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void setUserPassword(String username, String password) throws RemoteException, SecurityException {
        for (User user : listUsers()) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                user.setPassword(password.toCharArray());
                saveUser(user,false);
                userManager.deleteFileReminder(user.getUserName());
            }
        }
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String getCanRegRegistryName() throws RemoteException, SecurityException {
        String name = null;

        if (systemDescription != null) {
            name = systemDescription.getRegistryName();
        }
        return name;
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String[] listCurrentUsers() throws RemoteException, SecurityException {
        return userManager.listCurrentUsers();
    }

    /**
     *
     * @return @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public List<User> listUsers() throws RemoteException, SecurityException {
        return userManager.listUsers();
    }

    /**
     *
     * @param username
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public void userLoggedIn(Integer remoteHashCode, String username)
            throws RemoteException, SecurityException {
        userManager.userLoggedIn(remoteHashCode, username);
        displayTrayIconPopUpMessage("User logged in", "User " + username + " logged in.", MessageType.INFO);
    }

    @Override
    public void userLoggedOut(Integer remoteHashCode, String username)
            throws RemoteException, SecurityException {
        userManager.userLoggedOut(remoteHashCode);
        displayTrayIconPopUpMessage("User logged out", "User " + username + " logged out.", MessageType.INFO);
    }

    // 
    /**
     * For testing purposes only - not secure enough... Not used!
     *
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public CanRegDAO getDatabseConnection() throws RemoteException, SecurityException {
        return currentDAO;
    }

    private void debugOut(String msg) {
        if (debug) {
            LOGGER.log(Level.INFO, msg);
        }
    }
    // add and remove records

    /**
     *
     * @param patient
     * @return
     * @throws SQLException
     */
    @Override
    public int savePatient(Patient patient) throws SQLException {
        return currentDAO.savePatient(patient);
    }

    /**
     *
     * @param tumour
     * @return
     * @throws SQLException
     * @throws RecordLockedException
     */
    @Override
    public int saveTumour(Tumour tumour) throws SQLException, RecordLockedException {
        return currentDAO.saveTumour(tumour);

    }

    /**
     *
     * @param dictionaryEntry
     * @return
     */
    @Override
    public int saveDictionaryEntry(DictionaryEntry dictionaryEntry) {
        return currentDAO.saveDictionaryEntry(dictionaryEntry);
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String performBackup() throws RemoteException, SecurityException {
        systemSettings.setDateOfLastbackup(new Date());
        systemSettings.writeSettings();
        return currentDAO.performBackup();
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public final String getCanRegVersion() throws RemoteException, SecurityException {
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
    @Override
    public boolean deleteDictionaryEntries(int dictionaryID) throws RemoteException, SecurityException {
        return currentDAO.deleteDictionaryEntries(dictionaryID);
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Map<Integer, Dictionary> getDictionary() throws RemoteException, SecurityException {
        return currentDAO.getDictionary();
    }

    /**
     *
     * @param path
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public String restoreFromBackup(String path) throws RemoteException, SecurityException {
        // TODO: Add functionality to log off automatically after successful restore...
        return currentDAO.restoreFromBackup(path);
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public InetAddress getIPAddress() throws RemoteException, SecurityException {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
     * @throws UnknownTableException
     * @throws DistributedTableDescriptionException
     */
    @Override
    public DistributedTableDescription getDistributedTableDescription(DatabaseFilter filter, String tableName)
            throws RemoteException, SecurityException, SQLException, UnknownTableException, DistributedTableDescriptionException {
        return currentDAO.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, tableName, currentDAO.generateResultSetID());
    }

    /**
     *
     * @param patientID
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    private DatabaseRecord getPatient(int patientID)
            throws RemoteException, SecurityException, RecordLockedException {
        return getRecord(patientID, Globals.PATIENT_TABLE_NAME, false, null);
    }

    /**
     *
     * @param recordID
     * @param tableName
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException
     */
    @Override
    public DatabaseRecord getRecord(int recordID, String tableName, boolean lock, Integer remoteHashCode)
            throws RemoteException, SecurityException, RecordLockedException {
        DatabaseRecord rec = currentDAO.getRecord(recordID, tableName, lock);
        if (lock) {
            userManager.lockRecord(recordID, tableName, remoteHashCode);
        }
        return rec;
    }

    /**
     *
     * @param recordID
     * @param tableName
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public void releaseRecord(int recordID, String tableName, Integer remoteHashCode)
            throws RemoteException, SecurityException {
        currentDAO.releaseRecord(recordID, tableName);
        userManager.releaseRecord(recordID, tableName, remoteHashCode);
    }

    /**
     *
     * @param patient
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException
     */
    @Override
    public synchronized void editPatient(Patient patient)
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        currentDAO.editPatient(patient, false);
    }

    public void editPatientFromHoldingToProduction(Patient patient)
            throws RemoteException, SecurityException, RecordLockedException, SQLException {
        currentDAO.editPatient(patient, true);
    }

    /**
     *
     * @param tumour
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws RecordLockedException
     */
    @Override
    public void editTumour(Tumour tumour)
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        currentDAO.editTumour(tumour, false);
    }

    public void editTumourFromHoldingToProduction(Tumour tumour)
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        currentDAO.editTumour(tumour, true);
    }

    /**
     *
     * @param resultSetID
     * @param from
     * @param to
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Object[][] retrieveRows(String resultSetID, int from, int to)
            throws RemoteException, SecurityException {
        Object[][] rows = null;
        try {
            rows = currentDAO.retrieveRows(resultSetID, from, to);
        } catch (DistributedTableDescriptionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RemoteException("Retrieve rows failed: " + ex.getMessage());
        }
        return rows;
    }

    /**
     *
     * @param resultSetID
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     * @throws SQLException
     */
    @Override
    public void releaseResultSet(String resultSetID) throws RemoteException, SecurityException, SQLException {
        currentDAO.releaseResultSet(resultSetID);
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Map<Integer, PopulationDataset> getPopulationDatasets() throws RemoteException, SecurityException {
        return currentDAO.getPopulationDatasets();
    }

    /**
     *
     * @param pds
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public int saveNewPopulationDataset(PopulationDataset pds) throws RemoteException, SecurityException {
        return currentDAO.saveNewPopulationDataset(pds);
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Date getDateOfLastBackUp() throws RemoteException, SecurityException {
        return systemSettings.getDateOfLastBackUp();
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public Map<String, Integer> getNameSexTables() throws RemoteException, SecurityException {
        return currentDAO.getNameSexTables();
    }

    /**
     *
     * @param nameSexRecord
     * @param replace
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public int saveNameSexRecord(NameSexRecord nameSexRecord, boolean replace) throws RemoteException, SecurityException {
        return currentDAO.saveNameSexRecord(nameSexRecord, replace);
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public boolean clearNameSexTable() throws RemoteException, SecurityException {
        return currentDAO.clearNameSexTable();
    }

    /**
     *
     * @return @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public UserRightLevels getUserRightLevel() throws RemoteException, SecurityException {
        // This should never be called but be taken care of by the proxy...
        return Globals.UserRightLevels.NOT_LOGGED_IN;
    }

    /**
     *
     * @param searcher
     * @param rangeStart
     * @param rangeEnd
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public synchronized String initiateGlobalPersonSearch(PersonSearcher searcher, String rangeStart, String rangeEnd) throws RemoteException, SecurityException {
        DistributedTableDescription dataDescription;
        GlobalPersonSearchHandler gpsh = new GlobalPersonSearchHandler();
        String resultSetID = null;
        boolean isOK = false;
        try {
            if (searcher == null) {
                searcher = personSearcher;
            }

            DatabaseFilter filter = new DatabaseFilter();
            filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);

            dataDescription = getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
            resultSetID = dataDescription.getResultSetID();
            gpsh.setAllPatientRecordIDs(retrieveRows(resultSetID, 0, dataDescription.getRowCount()));
            releaseResultSet(resultSetID);
            DatabaseIndexesListElement dbile = new DatabaseIndexesListElement(null);
            dbile.setDatabaseTableName(Globals.PATIENT_TABLE_NAME);
            filter.setRangeDatabaseIndexedListElement(dbile);
            filter.setRangeDatabaseVariablesListElement(serverToolbox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()));
            filter.setRangeStart(rangeStart);
            filter.setRangeEnd(rangeEnd);
            dataDescription = getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
            resultSetID = dataDescription.getResultSetID();
            gpsh.setDistributedTableDescription(dataDescription);
            gpsh.setPersonSearcher(searcher);
            gpsh.setPosition(0);
            gpsh.setPatientRecordIDsWithinRange(retrieveRows(resultSetID, 0, dataDescription.getRowCount()));
            releaseResultSet(resultSetID);

            activePersonSearchers.put(resultSetID, gpsh);
            // releaseResultSet(resultSetID);
            isOK = true;
        } catch (SQLException | UnknownTableException | DistributedTableDescriptionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally {
            if(! isOK){
                releaseNotNullResultSet(resultSetID);
            }
        }
        return resultSetID;
    }

    /**
     * Find the duplicate present in the selected row of the database using a collection of variables
     * selected before the search.
     * A score is compute between each row and a duplicate is found only if the score is superior to threeshold
     * This function return a map of all the duplicate found in the computed sample

     * @param idString id of the current operation in the server
     * @throws SecurityException security exception
     * @throws RemoteException remote exception
     * @throws RecordLockedException if the fonction is locked
     * @return patientIDScorePatientIDMap
     */
    @Override
    public synchronized Map<String, Map<String, Float>> nextStepGlobalPersonSearch(String idString) throws SecurityException, RemoteException, RecordLockedException {
        Map<String, Map<String, Float>> patientIDScorePatientIDMap = new TreeMap<>();
        GlobalPersonSearchHandler globalPersonSearchHandler = activePersonSearchers.get(idString);
        if (globalPersonSearchHandler != null) {
            PersonSearcher searcher = globalPersonSearchHandler.getPersonSearcher();

            int startRow = globalPersonSearchHandler.getPosition();
            int endRow = startRow + Globals.GLOBAL_PERSON_SEARCH_STEP_SIZE;
            globalPersonSearchHandler.setPosition(endRow);

            if (startRow >= globalPersonSearchHandler.getDistributedTableDescription().getRowCount()) {
                releasePersonSearcher(idString);
                patientIDScorePatientIDMap = null;
            } else {
                // Object[][] rowData = retrieveRows(idString, startRow, endRow);
                Object[][] rowData = globalPersonSearchHandler.getPatientRecordIDsWithinRange();
                if (patientsData == null) {
                    //read all users one time in database and keep only the data corresponding to the variables selected
                    // on the duplicate Person Search Panel
                    patientsData = new HashMap<>(rowData.length);
                    try {
                        for (Object[] r : rowData) {
                            int patientID = (Integer) r[0];
                            // all selected data from the database are on the variable patientsData
                            try {
                                patientsData.put(patientID,
                                    ((DefaultPersonSearch) searcher)
                                        .getPatientVariables((Patient) getPatient(patientID),
                                            patientRecordIDvariableName));
                            } catch (RecordLockedException ex) {
                                LOGGER.log(Level.SEVERE,null,ex);
                            }
                        }
                    } catch (RemoteException | SecurityException ex) {
                        LOGGER.log(Level.SEVERE,null,ex);
                    }
                }
                //  read all the stored data in memory to compute the patientIDScorePatientIDMap
                for (int row = startRow; row < endRow && row < rowData.length; row++) {
                    int patientIDA = (Integer) rowData[row][0];
                    Object[] patientAData = patientsData.get(patientIDA);
                    // Map<String, Float> patientIDScoreMap = performPersonSearch(patientA, searcher, globalPersonSearchHandler.getDistributedTableDescription());

                    if (patientAData != null) {
                        Map<String, Float> patientIDScoreMap = performPersonSearchDataOnly(patientIDA, patientAData, searcher, patientsData, globalPersonSearchHandler.getAllPatientRecordIDs());
                        if (patientIDScoreMap.size() > 0) {
                            patientIDScorePatientIDMap.put(patientAData[5].toString(), patientIDScoreMap);
                        }
                    }
                }
            }
        } else {
            patientIDScorePatientIDMap = null;
        }
        return patientIDScorePatientIDMap;
    }

    private synchronized void releasePersonSearcher(String idString) {
        try {
            releaseResultSet(idString);
            activePersonSearchers.remove(idString);
        } catch (SQLException | RemoteException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param idString
     */
    @Override
    public synchronized void interuptGlobalPersonSearch(String idString) {
        releasePersonSearcher(idString);
    }

    /**
     *
     * @param patient
     * @param searcher
     * @return
     * @throws java.rmi.RemoteException
     * @throws java.lang.SecurityException
     */
    @Override
    public synchronized Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher) throws RemoteException, SecurityException {
        DatabaseFilter filter = new DatabaseFilter();
        filter.setQueryType(DatabaseFilter.QueryType.PERSON_SEARCH);
        DistributedTableDescription dataDescription;
        Map<String, Float> patientIDScoreMap = null;
        String resultSetID = null;
        if (searcher == null) {
            if (personSearcher == null) {
                personSearcher = new DefaultPersonSearch(
                        Tools.getVariableListElements(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
                PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE);
                personSearcher.setSearchVariables(searchVariables);
                personSearcher.setThreshold(Tools.getPersonSearchMinimumMatch(systemDescription.getSystemDescriptionDocument(), Globals.NAMESPACE));
            }
            searcher = personSearcher;
        }
        try {
            dataDescription = currentDAO.getDistributedTableDescriptionAndInitiateDatabaseQuery(filter, Globals.PATIENT_TABLE_NAME, currentDAO.generateResultSetID());
            resultSetID = dataDescription.getResultSetID();
            Object[][] rowData = retrieveRows(resultSetID, 0, dataDescription.getRowCount() - 1);
            patientIDScoreMap = performPersonSearch(patient, searcher, rowData);
            releaseResultSet(resultSetID);
            // Set the resultSetID to null after release of the resultSet to avoid another release in the finally
            resultSetID = null;
        } catch (SQLException | UnknownTableException | DistributedTableDescriptionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }finally {
            releaseNotNullResultSet(resultSetID);
        }
        return patientIDScoreMap;
    }

    /**
     * Compute the PatientId Score for each couple patient and add it to the patientIDScoreMap if score > threshold
     *
     * @param patientIDA id of the patient A (to be compare against the other patient)
     * @param patient  the information of the patientA
     * @param searcher all the information about the duplicate search parameters
     * @param patientsData selected variables of each patient of the database
     * @param rowData map that contains all the temporary patient'Ids
     */
    private Map<String, Float> performPersonSearchDataOnly(int patientIDA, Object[] patient, PersonSearcher searcher,
        Map<Integer, Object[]> patientsData, Object[][] rowData) {
        Map<String, Float> patientIDScoreMap = new TreeMap<>();
        Object[] patientB;
        float threshold = searcher.getThreshold();
        for (Object[] r : rowData) {
            int patientIDB = (Integer) r[0];
            if (patientIDB != patientIDA) {
                patientB = patientsData.get(patientIDB);

                if (patientB != null) {
                    //compute the score between the two patients
                    float score = ((DefaultPersonSearch) searcher).compareDataOnly(patient, patientB);
                    if (score > threshold) {
                        patientIDScoreMap.put(patientB[5].toString(), score);
                    }
                }
            }
        }
        return patientIDScoreMap;
    }

    private Map<String, Float> performPersonSearch(Patient patient, PersonSearcher searcher, Object[][] rowData) throws RemoteException, SecurityException {
        Map<String, Float> patientIDScoreMap = new TreeMap<>();
        
        Patient patientB;

        Object patientIDAObject = patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);

        int patientIDA;
        if (patientIDAObject != null) {
            patientIDA = (Integer) patientIDAObject;
        } else {
            patientIDA = -1;
        }

        float threshold = searcher.getThreshold();
        try {
            for (Object[] r : rowData) {
                int patientIDB = (Integer) r[0];
                if (patientIDB != patientIDA) {
                    try {
                        patientB = (Patient) getPatient(patientIDB);
                        float score = searcher.compare(patient, patientB);
                        if (score > threshold) {
                            patientIDScoreMap.put((String) patientB.getVariable(patientRecordIDvariableName), score);
                            // debugOut("Found patient id: " + patientB.getVariable(patientRecordIDvariableName) + ", score: " + score + "%");
                        } else {
                            // debugOut("Not found " + patientB.getVariable(patientRecordIDvariableName) + " " + score);
                        }
                    } catch (RecordLockedException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (RemoteException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return patientIDScoreMap;
    }

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
    @Override
    public synchronized boolean deleteRecord(int id, String tableName)
            throws RemoteException, SecurityException, RecordLockedException, SQLException {
        boolean success = false;
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            success = currentDAO.deleteTumourRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            success = currentDAO.deletePatientRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            success = currentDAO.deleteSourceRecord(id);
        } else if (tableName.equalsIgnoreCase(Globals.USERS_TABLE_NAME)) {
            success = currentDAO.deleteRecord(id, Globals.USERS_TABLE_NAME);
            userManager.writePasswordsToFile();
        } else {
            success = currentDAO.deleteRecord(id, tableName);
        }
        return success;
    }

    /**
     *
     * @param populationDatasetID
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public synchronized boolean deletePopulationDataset(int populationDatasetID) throws RemoteException, SecurityException {
        return currentDAO.deletePopulationDataSet(populationDatasetID);
    }

    /**
     *
     * @param user
     * @return
     * @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public synchronized int saveUser(User user, boolean addPasswordReminder) throws RemoteException, SecurityException {
        int id = currentDAO.saveUser(user);
        userManager.writePasswordsToFile();
        if (addPasswordReminder) {
            userManager.createFileReminder(user.getUserName());
        }
        return id;
    }

    /**
     *
     * @return @throws RemoteException
     * @throws SecurityException
     */
    @Override
    public DatabaseStats getDatabaseStats() throws RemoteException, SecurityException {
        return currentDAO.getDatabaseStats();
    }

    private void setTrayIconToolTip(String toolTip) {
        if (trayIcon != null) {
            trayIcon.setToolTip(toolTip);
        }
    }

    private void displayTrayIconPopUpMessage(String caption, String text, MessageType messageType) {
        if (trayIcon != null) {
            trayIcon.displayMessage(caption, text, messageType);
        }
    }

    @Override
    public void shutDownServer() throws RemoteException, SecurityException {
        if (trayIcon != null) {
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }
    }

    @Override
    public boolean setDBPassword(char[] newPasswordArray, char[] oldPasswordArray, String encryptionAlgorithm, String encryptionKeyLength) throws RemoteException, SecurityException {
        boolean success = false;
        try {
            success = currentDAO.encryptDatabase(newPasswordArray, oldPasswordArray, encryptionAlgorithm, encryptionKeyLength);
            if(success){
                if (newPasswordArray.length > 0) {
                    // create or change a password
                    defaultConfigFileUtils.updateConfigFileProperties(getCanRegRegistryCode(), "true");
                } else{
                    // the database password is removed after having been set
                    defaultConfigFileUtils.updateConfigFileProperties(getCanRegRegistryCode(), "false");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return success;
    }

    @Override
    public boolean checkDatabaseEncryption(String registryCode) throws RemoteException, SecurityException {
        File file = new File(Globals.CANREG_CONFIG_DATABASE);
        if (file.exists()) {
            Properties properties = defaultConfigFileUtils.readConfigFile();
            return properties!= null && Boolean.parseBoolean(properties.getProperty(registryCode));
        }
        return false;
    }

    @Override
    public String getCanRegRegistryCode() throws RemoteException, SecurityException {
        return systemDescription.getRegistryCode();
    }

    @Override
    public String getCanRegSystemRegion() throws RemoteException, SecurityException {
        return systemDescription.getRegion();
    }

    private int getLastHoldingDBnumber(String registryCode) {
        File holdingDir = getRegistryCodeHoldingFolder(registryCode);

        if (!holdingDir.exists()) {
            holdingDir.mkdirs();
        }

        int highestNumber = 0;
        for (String folder : holdingDir.list()) {
            //registryCode = ENR0
            //folder = HOLDING_ENR0_2_2019-01-21
            folder = folder.substring(folder.indexOf("_") + 1 + registryCode.length() + 1); // before substring = ENR0_2_2019-01-21
            folder = folder.substring(0, folder.indexOf("_"));//before substring = 2_2019-01-21
            int holdingNumber = Integer.valueOf(folder);
            if (holdingNumber > highestNumber) {
                highestNumber = holdingNumber;
            }
        }
        return highestNumber;
    }

    @Override
    public SystemDescription createNewHoldingDB(String registryCode, SystemDescription sysDesc)
            throws RemoteException, IOException, SecurityException {
        File registryCodeHoldingFolder = getRegistryCodeHoldingFolder(registryCode);
        //Include the date AND a number in the HDB system code (the user COULD do more than 1 HDB of the same xml on the same date)
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format((Calendar.getInstance()).getTime());
        int newHoldingDBNumber = this.getLastHoldingDBnumber(registryCode) + 1;
        String holdingRegistryCode = "HOLDING_" + registryCode + "_" + +newHoldingDBNumber + "_" + dateStr;
        File holdingXmlPath = new File(registryCodeHoldingFolder, holdingRegistryCode);
        holdingXmlPath.mkdirs();
        File holdingXml = new File(holdingXmlPath, holdingRegistryCode + ".xml");
        sysDesc.setRegistryCode(holdingRegistryCode);
        sysDesc.setSystemDescriptionLocation(holdingXmlPath);
        sysDesc.saveSystemDescriptionXML(holdingXml.getAbsolutePath());
        return sysDesc;
    }

    /**
     * Returns the main folder for the system of a holding database: like  'xxx/.CanRegServer/Holding/TRN'  
     * @param registryCode the registry code
     * @return File pointing on the filesystem folder
     */
    private File getRegistryCodeHoldingFolder(String registryCode) {
        return new File(Globals.CANREG_SERVER_HOLDING_DB_SYSTEM_DESCRIPTION_FOLDER, registryCode);
    }

    @Override
    public void deleteHoldingDB(String holdingRegistryCode)
            throws SQLException, RemoteException, IOException, SecurityException {
        CanRegDAO dao = this.registriesDAOs.remove(holdingRegistryCode);
        if (dao == null) {
            return;
        }
        dao.disconnect();

        String originalRegistryCode = holdingRegistryCode.substring(holdingRegistryCode.indexOf("_") + 1);
        originalRegistryCode = originalRegistryCode.substring(0, originalRegistryCode.indexOf("_"));
        File holdingDBSystemDescriptionFolder = new File(
                getRegistryCodeHoldingFolder(originalRegistryCode), holdingRegistryCode);
        if (!Tools.deleteFolderRecursively(holdingDBSystemDescriptionFolder)) {
            holdingDBSystemDescriptionFolder.deleteOnExit();
        }

        File holdingDBFolder = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + holdingRegistryCode);
        if (!Tools.deleteFolderRecursively(holdingDBFolder)) {
            holdingDBFolder.deleteOnExit();
        }
    }

    @Override
    public void changeRegistryDB(String registryCode)
            throws RemoteException, SecurityException {
        CanRegDAO dao = this.registriesDAOs.get(registryCode);
        if (dao == null) {
            throw new NullPointerException("Database with registry code " + registryCode + " has not been succesfully initialized.");
        }
        currentDAO = dao;
    }

    @Override
    public void resetRegistryDB() throws RemoteException, SecurityException {
        currentDAO = this.registriesDAOs.get(this.defaultRegistryCode);
    }

    @Override
    public List<String> getHoldingDBsList()
            throws IOException, RemoteException, SecurityException {
        File registryCodeHoldingFolder = getRegistryCodeHoldingFolder(this.defaultRegistryCode);
        List<String> holdingList = new LinkedList<>();
        if (registryCodeHoldingFolder.exists()) {
            holdingList = Arrays.asList(registryCodeHoldingFolder.list());
        }

        return holdingList;
    }

    @Override
    public void pingRemote(Integer remoteClientHashCode)
            throws RemoteException, Exception {
        userManager.remotePingReceived(remoteClientHashCode);
    }

    @Override
    public boolean checkFileReminder(String username) {
        return userManager.checkPasswordReminderFile(username);
    }

    @Override
    public void deleteFileReminder(String username) throws RemoteException {
        userManager.deleteFileReminder(username);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    // Method to release the ResultSet in finally block if the ResultSetID is not null 
    private void releaseNotNullResultSet(String resultSetID) throws RemoteException{
        try {
            if(resultSetID != null){
                releaseResultSet(resultSetID);
            }
        }catch (SQLException ex){
            LOGGER.log(Level.SEVERE,String.format("Sql error in the release of the ResultSet with resultSetID : %s", resultSetID),ex);
        }
    }
    //not used in our case
    @Override
    public boolean checkPassword(String username, String encryptedPassword) throws java.rmi.RemoteException {
        boolean validPassword = false;
        for (User user : listUsers()) {
            if (user.getUserName().equalsIgnoreCase(username) && String.valueOf(user.getPassword()).equalsIgnoreCase(encryptedPassword)) {
                validPassword = true;
                break;
            }
        }
        return validPassword;
    }
}
