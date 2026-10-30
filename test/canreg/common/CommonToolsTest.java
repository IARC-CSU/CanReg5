package canreg.common;

import canreg.TestFixtures;
import canreg.server.management.SystemDescription;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link canreg.common.Tools}.
 */
public class CommonToolsTest {

    @Test
    public void testFindInArray() {
        String[] arr = {"alpha", "beta", "gamma"};
        assertEquals(1, Tools.findInArray(arr, "beta"));
        assertEquals(-1, Tools.findInArray(arr, "delta"));
    }

    @Test
    public void testBreakDownLineAndPF() {
        String csvLine = "100,\"Smith, John\",19800101";
        String[] parts = Tools.breakDownLine(',', csvLine);
        assertArrayEquals(new String[]{"100", "Smith, John", "19800101"}, parts);

        String[] partsPf = Tools.breakDownLinePF(',', csvLine);
        assertArrayEquals(new String[]{"100", "Smith, John", "19800101"}, partsPf);
    }

    @Test
    public void testIncrement() {
        assertEquals("1", Tools.increment(""));
        assertEquals("20200002", Tools.increment("20200001"));
        assertEquals("20200010", Tools.increment("20200009"));
        assertEquals("100", Tools.increment("99"));
    }

    @Test
    public void testStandardizedCaseConversions() {
        // Specifically important for Turkish locale where 'i'.toUpperCase() != 'I' in default locale
        assertEquals("INCIDENCE", Tools.toUpperCaseStandardized("incidence"));
        assertEquals("incidence", Tools.toLowerCaseStandardized("INCIDENCE"));
    }

    @Test
    public void testXmlVariableAndIndexExtraction() {
        SystemDescription sd = TestFixtures.loadTestSystemDescription();
        Document doc = sd.getSystemDescriptionDocument();

        DatabaseVariablesListElement[] allVars = Tools.getVariableListElements(doc, Globals.NAMESPACE);
        assertTrue(allVars.length > 0);

        DatabaseVariablesListElement[] patientVars = Tools.getVariableListElements(doc, Globals.NAMESPACE, "Patient");
        assertTrue(patientVars.length > 0);
        for (DatabaseVariablesListElement v : patientVars) {
            assertTrue(v.getDatabaseTableName().equalsIgnoreCase("Patient"));
        }

        TreeMap<String, DatabaseVariablesListElement> varsMap = Tools.buildVariablesMap(allVars);
        assertTrue(varsMap.containsKey("REGNO"));

        TreeMap<Globals.StandardVariableNames, DatabaseVariablesListElement> stdMap =
                Tools.buildStandardVariablesMap(allVars);
        assertTrue(stdMap.containsKey(Globals.StandardVariableNames.PatientID));

        DatabaseIndexesListElement[] indexes = Tools.getIndexesListElements(doc, Globals.NAMESPACE, varsMap);
        assertNotNull(indexes);

        PersonSearchVariable[] searchVars = Tools.getPersonSearchVariables(doc, Globals.NAMESPACE);
        assertNotNull(searchVars);

        String[] varNames = Tools.getVariableNames(doc, Globals.NAMESPACE);
        assertEquals(allVars.length, varNames.length);
    }
}

