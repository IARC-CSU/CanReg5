package canreg.server.database;

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

    /**
     * Helper to build a test XML Document describing database variables structure.
     */
    private Document createTestDoc() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Structure required by QueryGenerator:
        // <ns3:variables>
        //   <ns3:variable>
        //     <ns3:table>patient</ns3:table>
        //     <ns3:variable_type>Alpha</ns3:variable_type>
        //     <ns3:short_name>first_name</ns3:short_name>
        //     <ns3:variable_length>100</ns3:variable_length>
        //   </ns3:variable>
        //   <ns3:variable>
        //     <ns3:table>patient</ns3:table>
        //     <ns3:variable_type>Number</ns3:variable_type>
        //     <ns3:short_name>age</ns3:short_name>
        //   </ns3:variable>
        // </ns3:variables>
        
        Element variables = doc.createElement("ns3:variables");
        doc.appendChild(variables);
        
        // Variable 1: first_name (Alpha type)
        Element v1 = doc.createElement("ns3:variable");
        Element t1 = doc.createElement("ns3:table");
        t1.setTextContent("patient");
        v1.appendChild(t1);
        
        Element vt1 = doc.createElement("ns3:variable_type");
        vt1.setTextContent("Alpha");
        v1.appendChild(vt1);
        
        Element sn1 = doc.createElement("ns3:short_name");
        sn1.setTextContent("first_name");
        v1.appendChild(sn1);
        
        Element vl1 = doc.createElement("ns3:variable_length");
        vl1.setTextContent("100");
        v1.appendChild(vl1);
        
        variables.appendChild(v1);
        
        // Variable 2: age (Number type)
        Element v2 = doc.createElement("ns3:variable");
        Element t2 = doc.createElement("ns3:table");
        t2.setTextContent("patient");
        v2.appendChild(t2);
        
        Element vt2 = doc.createElement("ns3:variable_type");
        vt2.setTextContent("Number");
        v2.appendChild(vt2);
        
        Element sn2 = doc.createElement("ns3:short_name");
        sn2.setTextContent("age");
        v2.appendChild(sn2);
        
        variables.appendChild(v2);
        
        return doc;
    }

    /**
     * Test of strCreateVariableTable method, of class QueryGenerator.
     */
    @Test
    public void testStrCreateVariableTable() throws Exception {
        System.out.println("strCreateVariableTable");
        Document doc = createTestDoc();
        String query = QueryGenerator.strCreateVariableTable("patient", doc);
        
        // Output should define PATIENT columns and standard fields
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("CREATE TABLE APP.PATIENT"));
        assertTrue(query.toUpperCase().contains("FIRST_NAME"));
        assertTrue(query.toUpperCase().contains("VARCHAR(100)"));
        assertTrue(query.toUpperCase().contains("AGE"));
        assertTrue(query.toUpperCase().contains("INTEGER"));
    }

    /**
     * Test of strSavePatient method, of class QueryGenerator.
     */
    @Test
    public void testStrSavePatient() throws Exception {
        System.out.println("strSavePatient");
        Document doc = createTestDoc();
        String query = QueryGenerator.strSavePatient(doc);
        
        // Output should format INSERT query
        assertNotNull(query);
        assertTrue(query.toUpperCase().contains("INSERT INTO APP.PATIENT"));
        assertTrue(query.toUpperCase().contains("FIRST_NAME"));
        assertTrue(query.toUpperCase().contains("AGE"));
        assertTrue(query.toUpperCase().contains("VALUES"));
    }

    /**
     * Test of migration formatting methods, of class QueryGenerator.
     */
    @Test
    public void testMigrationFormatting() {
        System.out.println("testMigrationFormatting");
        
        // Add column
        String add = QueryGenerator.strAddColumnToTable("email", "VARCHAR(255)", "USERS");
        assertEquals("ALTER TABLE APP.USERS ADD COLUMN EMAIL VARCHAR(255)", add);
        
        // Drop column
        String drop = QueryGenerator.strDropColumnFromTable("email", "USERS");
        assertEquals("ALTER TABLE APP.USERS DROP COLUMN EMAIL", drop);
        
        // Set column data type
        String alter = QueryGenerator.strSetColumnDataType("email", "VARCHAR(100)", "USERS");
        assertEquals("ALTER TABLE APP.USERS ALTER EMAIL SET DATA TYPE VARCHAR(100)", alter);
    }

    /**
     * Test of query count methods, of class QueryGenerator.
     */
    @Test
    public void testCountQueries() {
        System.out.println("testCountQueries");
        
        assertEquals("SELECT COUNT(*) FROM APP.Patient WHERE REGNUM = ?", QueryGenerator.strCountPatientByRegistryNumber("REGNUM"));
        assertEquals("SELECT COUNT(*) FROM APP.Patient WHERE ID = ?", QueryGenerator.strCountPatientByRecordID("ID"));
        assertEquals("SELECT COUNT(*) FROM APP.Tumour WHERE ID = ?", QueryGenerator.strCountTumourByTumourID("ID"));
        assertEquals("SELECT COUNT(*) FROM APP.Source WHERE ID = ?", QueryGenerator.strCountSourceByRecordID("ID"));
    }
}
