package canreg.server.database;

import canreg.common.Globals;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for canreg.server.database.QueryGenerator.
 * Tests isolated SQL template construction, DOM column parsing, and lookup queries.
 */
public class QueryGeneratorTest {

    public QueryGeneratorTest() {
    }

    // ------------------------------------------------------------------
    // Helper: build a minimal XML doc with variables for a named table
    // ------------------------------------------------------------------

    /**
     * Creates a Document with two variables: one Alpha, one Number.
     * Used to test SQL generation that depends on the schema document.
     */
    private Document createTestDoc() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element variables = doc.createElement("ns3:variables");
        doc.appendChild(variables);

        // Variable 1: first_name (Alpha type), table=patient
        variables.appendChild(variable(doc, "patient", "Alpha", "first_name", "100", null));

        // Variable 2: age (Number type), table=patient
        variables.appendChild(variable(doc, "patient", "Number", "age", null, null));

        return doc;
    }

    /** Build a doc with one variable per requested table. */
    private Document createMultiTableDoc() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element variables = doc.createElement("ns3:variables");
        doc.appendChild(variables);

        variables.appendChild(variable(doc, "patient", "Alpha",  "pat_name",  "50", null));
        variables.appendChild(variable(doc, "tumour",  "Number", "tum_year",  null, null));
        variables.appendChild(variable(doc, "source",  "Alpha",  "src_id",    "20", null));

        return doc;
    }

    private Element variable(Document doc, String table, String type, String name,
                              String length, String dictName) {
        Element v = doc.createElement("ns3:variable");
        v.appendChild(text(doc, "ns3:table",         table));
        v.appendChild(text(doc, "ns3:variable_type", type));
        v.appendChild(text(doc, "ns3:short_name",    name));
        if (length != null) {
            v.appendChild(text(doc, "ns3:variable_length", length));
        }
        if (dictName != null) {
            v.appendChild(text(doc, "ns3:use_dictionary", dictName));
        }
        return v;
    }

    private Element text(Document doc, String tag, String value) {
        Element e = doc.createElement(tag);
        e.setTextContent(value);
        return e;
    }

    // ------------------------------------------------------------------
    // strCreateVariableTable
    // ------------------------------------------------------------------

    @Test
    public void testStrCreateVariableTable() throws Exception {
        System.out.println("strCreateVariableTable");
        Document doc = createTestDoc();
        String query = QueryGenerator.strCreateVariableTable("patient", doc);

        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE APP.PATIENT"));
        assertTrue(query.toUpperCase().contains("FIRST_NAME"));
        assertTrue(query.toUpperCase().contains("VARCHAR(100)"));
        assertTrue(query.toUpperCase().contains("AGE"));
        assertTrue(query.toUpperCase().contains("INTEGER"));
    }

    @Test
    public void testStrCreateVariableTable_Tumour() throws Exception {
        Document doc = createMultiTableDoc();
        String query = QueryGenerator.strCreateVariableTable("tumour", doc);

        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE APP.TUMOUR"));
        assertTrue(query.toUpperCase().contains("TUM_YEAR"));
        assertTrue(query.toUpperCase().contains("INTEGER"));
    }

    @Test
    public void testStrCreateVariableTable_Source() throws Exception {
        Document doc = createMultiTableDoc();
        String query = QueryGenerator.strCreateVariableTable("source", doc);

        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE APP.SOURCE"));
        assertTrue(query.toUpperCase().contains("SRC_ID"));
    }

    // ------------------------------------------------------------------
    // strSavePatient / strSaveTumour / strSaveSource
    // ------------------------------------------------------------------

    @Test
    public void testStrSavePatient() throws Exception {
        System.out.println("strSavePatient");
        Document doc = createTestDoc();
        String query = QueryGenerator.strSavePatient(doc);

        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO APP.PATIENT"));
        assertTrue(query.toUpperCase().contains("FIRST_NAME"));
        assertTrue(query.toUpperCase().contains("AGE"));
        assertTrue(query.toUpperCase().contains("VALUES"));
    }

    @Test
    public void testStrSaveTumour() throws Exception {
        Document doc = createMultiTableDoc();
        String query = QueryGenerator.strSaveTumour(doc);

        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO APP.TUMOUR"));
        assertTrue(query.toUpperCase().contains("TUM_YEAR"));
        assertTrue(query.toUpperCase().contains("VALUES"));
    }

    @Test
    public void testStrSaveSource() throws Exception {
        Document doc = createMultiTableDoc();
        String query = QueryGenerator.strSaveSource(doc);

        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO APP.SOURCE"));
        assertTrue(query.toUpperCase().contains("SRC_ID"));
        assertTrue(query.toUpperCase().contains("VALUES"));
    }

    // ------------------------------------------------------------------
    // strCreateDictionaryTable / strCreateTablesOfDictionaries
    // ------------------------------------------------------------------

    @Test
    public void testStrCreateDictionaryTable() {
        String query = QueryGenerator.strCreateDictionaryTable(null);
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("DICTIONARY"));
        assertTrue(query.toUpperCase().contains("CODE"));
        assertTrue(query.toUpperCase().contains("DESCRIPTION"));
    }

    @Test
    public void testStrCreateTablesOfDictionaries() {
        String query = QueryGenerator.strCreateTablesOfDictionaries(null);
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("DICTIONARIES"));
        assertTrue(query.toUpperCase().contains("DICTIONARYID"));
        assertTrue(query.toUpperCase().contains("NAME"));
    }

    // ------------------------------------------------------------------
    // Zero-arg static INSERT queries (no Document needed)
    // ------------------------------------------------------------------

    @Test
    public void testStrSaveDictionary() {
        String query = QueryGenerator.strSaveDictionary();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO"));
        assertTrue(query.toUpperCase().contains("DICTIONARIES"));
        assertTrue(query.contains("?, ?, ?, ?, ?, ?, ?, ?"));
    }

    @Test
    public void testStrSaveDictionaryEntry() {
        String query = QueryGenerator.strSaveDictionaryEntry();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO"));
        assertTrue(query.toUpperCase().contains("DICTIONARY"));
        // 3 parameters: DICTIONARY, CODE, DESCRIPTION
        assertTrue(query.contains("?, ?, ?"));
    }

    @Test
    public void testStrSaveUser() {
        String query = QueryGenerator.strSaveUser();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO"));
        assertTrue(query.toUpperCase().contains("USERS"));
        assertTrue(query.toUpperCase().contains("USERNAME"));
        assertTrue(query.toUpperCase().contains("PASSWORD"));
    }

    @Test
    public void testStrEditUser() {
        String query = QueryGenerator.strEditUser();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("UPDATE"));
        assertTrue(query.toUpperCase().contains("USERS"));
        assertTrue(query.toUpperCase().contains("WHERE"));
    }

    // ------------------------------------------------------------------
    // Population dataset tables
    // ------------------------------------------------------------------

    @Test
    public void testStrCreatePopulationDatasetTable() {
        String query = QueryGenerator.strCreatePopulationDatasetTable();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("PDSETS"));
        assertTrue(query.toUpperCase().contains("PDS_NAME"));
    }

    @Test
    public void testStrCreatePopulationDatasetsTable() {
        String query = QueryGenerator.strCreatePopulationDatasetsTable();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("PDSET"));
        assertTrue(query.toUpperCase().contains("AGE_GROUP"));
    }

    // ------------------------------------------------------------------
    // DDL migration helpers
    // ------------------------------------------------------------------

    @Test
    public void testMigrationFormatting() {
        System.out.println("testMigrationFormatting");

        String add = QueryGenerator.strAddColumnToTable("email", "VARCHAR(255)", "USERS");
        assertEquals("ALTER TABLE APP.USERS ADD COLUMN EMAIL VARCHAR(255)", add);

        String drop = QueryGenerator.strDropColumnFromTable("email", "USERS");
        assertEquals("ALTER TABLE APP.USERS DROP COLUMN EMAIL", drop);

        String alter = QueryGenerator.strSetColumnDataType("email", "VARCHAR(100)", "USERS");
        assertEquals("ALTER TABLE APP.USERS ALTER EMAIL SET DATA TYPE VARCHAR(100)", alter);
    }

    // ------------------------------------------------------------------
    // Count queries
    // ------------------------------------------------------------------

    @Test
    public void testCountQueries() {
        System.out.println("testCountQueries");

        assertEquals("SELECT COUNT(*) FROM APP.Patient WHERE REGNUM = ?",
                     QueryGenerator.strCountPatientByRegistryNumber("REGNUM"));
        assertEquals("SELECT COUNT(*) FROM APP.Patient WHERE ID = ?",
                     QueryGenerator.strCountPatientByRecordID("ID"));
        assertEquals("SELECT COUNT(*) FROM APP.Tumour WHERE ID = ?",
                     QueryGenerator.strCountTumourByTumourID("ID"));
        assertEquals("SELECT COUNT(*) FROM APP.Source WHERE ID = ?",
                     QueryGenerator.strCountSourceByRecordID("ID"));
    }

    // ------------------------------------------------------------------
    // System / Users / NameSex table DDL
    // ------------------------------------------------------------------

    @Test
    public void testStrCreateUsersTable() {
        String query = QueryGenerator.strCreateUsersTable();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("USERS"));
        assertTrue(query.toUpperCase().contains("USERNAME"));
        assertTrue(query.toUpperCase().contains("PASSWORD"));
    }

    @Test
    public void testStrCreateSystemPropertiesTable() {
        String query = QueryGenerator.strCreateSystemPropertiesTable();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("SYSTEM"));
        assertTrue(query.toUpperCase().contains("LOOKUP"));
        assertTrue(query.toUpperCase().contains("VALUE"));
    }

    @Test
    public void testStrCreateNameSexTable() {
        String query = QueryGenerator.strCreateNameSexTable();
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE"));
        assertTrue(query.toUpperCase().contains("NAME"));
        assertTrue(query.toUpperCase().contains("SEX"));
    }
}
