package canreg.server.database;

import canreg.TestFixtures;
import canreg.common.Globals;
import canreg.common.database.AgeGroupStructure;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.common.database.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

/**
 * Hermetic integration tests for {@link CanRegDAO} backed by an in-memory Apache Derby database.
 */
public class CanRegDAOTest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger(1);

    private String registryCode;
    private Document doc;
    private Connection setupConn;
    private InMemoryCanRegDAO dao;

    private static class InMemoryCanRegDAO extends CanRegDAO {
        InMemoryCanRegDAO(String registryCode, Document doc, Properties databaseProperties) {
            super(registryCode, doc, databaseProperties);
        }

        @Override
        public DataSource initDataSource(Properties databaseProperties) {
            // Keep dbDatasource null so CanRegDAO uses its embedded DbConnectionWrapper
            return null;
        }
    }

    @Before
    public void setUp() throws Exception {
        registryCode = "MEMDB_" + DB_COUNTER.getAndIncrement();
        doc = TestFixtures.loadTestSystemDocument();

        String createUrl = "jdbc:derby:memory:" + registryCode + ";create=true";
        setupConn = DriverManager.getConnection(createUrl);
        createAllTables(setupConn, doc);

        Properties props = new Properties();
        props.setProperty("derby.url", "jdbc:derby:memory:");
        dao = new InMemoryCanRegDAO(registryCode, doc, props);
        assertTrue("In-memory DAO should connect", dao.connect());
    }

    @After
    public void tearDown() {
        if (setupConn != null) {
            try {
                setupConn.close();
            } catch (SQLException ignored) {
            }
        }
        try {
            DriverManager.getConnection("jdbc:derby:memory:" + registryCode + ";drop=true");
        } catch (SQLException ignored) {
            // Derby throws SQLState 08006 on successful drop
        }
    }

    private void createAllTables(Connection conn, Document schemaDoc) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.execute(QueryGenerator.strCreateVariableTable(Globals.TUMOUR_TABLE_NAME, schemaDoc));
            stmt.execute(QueryGenerator.strCreateVariableTable(Globals.PATIENT_TABLE_NAME, schemaDoc));
            stmt.execute(QueryGenerator.strCreateVariableTable(Globals.SOURCE_TABLE_NAME, schemaDoc));
            stmt.execute(QueryGenerator.strCreateDictionaryTable(schemaDoc));
            stmt.execute(QueryGenerator.strCreateTablesOfDictionaries(schemaDoc));
            stmt.execute(QueryGenerator.strCreatePopulationDatasetTable());
            stmt.execute(QueryGenerator.strCreatePopulationDatasetsTable());
            stmt.execute(QueryGenerator.strCreateNameSexTable());
            stmt.execute(QueryGenerator.strCreateUsersTable());
            stmt.execute(QueryGenerator.strCreateSystemPropertiesTable());
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testSystemPropertiesRoundTrip() {
        assertNull(dao.getSystemPropery("DATABASE_VERSION"));
        dao.setSystemPropery("DATABASE_VERSION", "5.00.44");
        assertEquals("5.00.44", dao.getSystemPropery("DATABASE_VERSION"));

        // Overwrite existing property
        dao.setSystemPropery("DATABASE_VERSION", "5.00.45");
        assertEquals("5.00.45", dao.getSystemPropery("DATABASE_VERSION"));
    }

    @Test
    public void testUsersCrud() {
        User user = new User();
        user.setUserName("alice");
        user.setPassword("secret123".toCharArray());
        user.setUserRightLevelIndex(1);
        user.setEmail("alice@example.org");
        user.setRealName("Alice Registrar");

        dao.saveUser(user);

        Map<String, User> users = dao.getUsers();
        assertTrue(users.containsKey("alice"));
        User fetched = dao.getUserByUsername("alice");
        assertNotNull(fetched);
        assertEquals("Alice Registrar", fetched.getRealName());
        assertEquals("alice@example.org", fetched.getEmail());

        // Update existing user
        fetched.setEmail("alice.updated@example.org");
        dao.saveUser(fetched);
        User updated = dao.getUserByUsername("alice");
        assertEquals("alice.updated@example.org", updated.getEmail());
    }

    @Test
    public void testDictionariesAndNameSexCrud() {
        Map<Integer, Dictionary> initialDicts = dao.getDictionary();
        assertNotNull(initialDicts);
        assertFalse(initialDicts.isEmpty());

        Integer firstDictId = initialDicts.keySet().iterator().next();
        DictionaryEntry entry = new DictionaryEntry(firstDictId, "01", "Test Entry One");
        int entryId = dao.saveDictionaryEntry(entry);
        assertTrue(entryId > 0);

        Map<Integer, Dictionary> reloaded = dao.getDictionary();
        assertTrue(reloaded.get(firstDictId).getDictionaryEntries().containsKey("01"));

        assertTrue(dao.deleteDictionaryEntries(firstDictId));
        assertTrue(dao.getDictionary().get(firstDictId).getDictionaryEntries().isEmpty());

        // NameSex table
        NameSexRecord nsr = new NameSexRecord();
        nsr.setName("MARIA");
        nsr.setSex(2);
        assertTrue(dao.saveNameSexRecord(nsr, true) > 0);
        Map<String, Integer> nameSexMap = dao.getNameSexTables();
        assertEquals(Integer.valueOf(2), nameSexMap.get("MARIA"));

        assertTrue(dao.clearNameSexTable());
        assertTrue(dao.getNameSexTables().isEmpty());
    }

    @Test
    public void testPopulationDatasetCrud() throws Exception {
        PopulationDataset pds = new PopulationDataset();
        pds.setPopulationDatasetName("Test Registry Pop 2020");
        pds.setFilter("");
        pds.setDate("20200701");
        pds.setSource("National Census");
        pds.setDescription("Unit test population");
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));
        pds.setReferencePopulationBool(true);
        pds.addAgeGroup(new PopulationDatasetsEntry(0, 1, 5000));
        pds.addAgeGroup(new PopulationDatasetsEntry(0, 2, 4800));

        int pdsId = dao.saveNewPopulationDataset(pds);
        assertEquals(0, pdsId);

        Map<Integer, PopulationDataset> allPds = dao.getPopulationDatasets();
        assertTrue(allPds.containsKey(pdsId));
        assertEquals("Test Registry Pop 2020", allPds.get(pdsId).getPopulationDatasetName());
        assertEquals(5000, allPds.get(pdsId).getAgeGroupCount(1, 0));

        pds.setDescription("Updated description");
        assertEquals(pdsId, dao.updatePopulationDataset(pds));
        assertEquals("Updated description", dao.getPopulationDatasets().get(pdsId).getDescription());

        assertTrue(dao.deletePopulationDataSet(pdsId));
        assertFalse(dao.getPopulationDatasets().containsKey(pdsId));
    }

    @Test
    public void testPatientTumourSourceCrudAndLocking() throws Exception {
        Patient patient = new Patient();
        patient.setVariable("RegNo", "20200001");
        patient.setVariable("PatientRecordID", "2020000101");
        patient.setVariable("FirstN", "JOHN");
        patient.setVariable("FamN", "DOE");
        patient.setVariable("Sex", "1");
        patient.setVariable("BirthD", "19600101");

        int patDbId = dao.savePatient(patient);
        assertTrue("Saved patient DB ID should be positive", patDbId > 0);
        assertEquals(1, dao.countPatientByPatientID("20200001"));
        assertEquals(1, dao.countPatientByPatientRecordID("2020000101"));

        Patient fetchedByRecId = dao.getPatientByPatientRecordID("2020000101");
        assertNotNull(fetchedByRecId);
        assertEquals("JOHN", fetchedByRecId.getVariable("FIRSTN"));

        // Lock patient record via getRecord(..., lock=true)
        Patient lockedPat = (Patient) dao.getRecord(patDbId, Globals.PATIENT_TABLE_NAME, true);
        assertNotNull(lockedPat);

        try {
            dao.getRecord(patDbId, Globals.PATIENT_TABLE_NAME, true);
            fail("Expected RecordLockedException when locking an already locked patient record");
        } catch (RecordLockedException expected) {
            // expected
        }

        // Release lock and edit patient
        dao.releaseRecord(patDbId, Globals.PATIENT_TABLE_NAME);
        lockedPat.setVariable("FIRSTN", "JOHNNY");
        assertTrue(dao.editPatient(lockedPat, false));

        Patient afterEdit = (Patient) dao.getRecord(patDbId, Globals.PATIENT_TABLE_NAME, false);
        assertEquals("JOHNNY", afterEdit.getVariable("FIRSTN"));

        // Save Tumour with a Source record
        Tumour tumour = new Tumour();
        tumour.setVariable("PatientIDtumourtable", "20200001");
        tumour.setVariable("PatientRecordIDtumourtable", "2020000101");
        tumour.setVariable("TumourID", "202000010101");
        tumour.setVariable("IncDate", "20200515");
        tumour.setVariable("Top", "619");
        tumour.setVariable("Mor", "8140");
        tumour.setVariable("Beh", "3");

        Source source = new Source();
        source.setVariable("TumourIDsourcetable", "202000010101");
        source.setVariable("SourceRecordID", "20200001010101");
        Set<Source> sources = new LinkedHashSet<Source>();
        sources.add(source);
        tumour.setSources(sources);

        int tumDbId = dao.saveTumour(tumour);
        assertTrue("Saved tumour DB ID should be positive", tumDbId > 0);
        assertEquals(1, dao.countTumourByTumourID("202000010101"));
        assertEquals(1, dao.countSourceBySourceRecordID("20200001010101"));

        Tumour fetchedTumour = dao.getTumourByTumourID("202000010101");
        assertNotNull(fetchedTumour);
        assertEquals("619", fetchedTumour.getVariable("TOP"));
        assertNotNull(fetchedTumour.getSources());
        assertEquals(1, fetchedTumour.getSources().size());

        // Delete records
        assertTrue(dao.deleteTumourRecord(tumDbId));
        assertTrue(dao.deletePatientRecord(patDbId));
        assertEquals(0, dao.countPatientByPatientID("20200001"));
    }
}