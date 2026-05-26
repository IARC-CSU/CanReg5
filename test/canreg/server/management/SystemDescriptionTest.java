package canreg.server.management;

import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import java.io.File;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Hermetic XML-parsing tests for {@link SystemDescription}.
 *
 * Uses the committed training registry configuration file
 * {@code conf/database/TRN.xml} as a test fixture.
 *
 * NOTE: These tests are currently @Ignored because
 * {@code SystemDescription} calls {@code canreg.common.Tools.getVariableListElements},
 * which in turn calls {@code CanRegClientApp.getApplication()}.  That method
 * throws {@code IllegalStateException("Application is not launched.")} when
 * invoked outside a running Swing application.  The tests are preserved here as
 * a regression suite to be un-ignored once the coupling is broken.
 *
 * No database or network access is required once that blocker is resolved.
 */
@Ignore("SystemDescription calls CanRegClientApp.getApplication() which requires a running Swing application")
public class SystemDescriptionTest {

    private static final String TRN_XML_PATH = "conf/database/TRN.xml";

    private SystemDescription createSD() {
        File f = new File(TRN_XML_PATH);
        org.junit.Assume.assumeTrue("Skipping: " + TRN_XML_PATH + " not found", f.exists() && f.isFile());
        return new SystemDescription(TRN_XML_PATH);
    }

    // ------------------------------------------------------------------
    // Basic metadata from <ns3:general>
    // ------------------------------------------------------------------

    @Test
    public void testRegistryNameIsNotNull() {
        SystemDescription sd = createSD();
        assertNotNull("Registry name should be parsed from XML", sd.getRegistryName());
        assertFalse("Registry name should not be empty", sd.getRegistryName().trim().isEmpty());
    }

    @Test
    public void testRegistryNameIsTrainingRegistry() {
        SystemDescription sd = createSD();
        // TRN.xml declares: <ns3:registry_name>Training Cancer Registry</ns3:registry_name>
        assertEquals("Training Cancer Registry", sd.getRegistryName());
    }

    @Test
    public void testRegistryCodeIsTRN() {
        SystemDescription sd = createSD();
        // TRN.xml declares: <ns3:registry_code>TRN</ns3:registry_code>
        assertEquals("TRN", sd.getRegistryCode());
    }

    @Test
    public void testDescriptionFilePathIsSet() {
        SystemDescription sd = createSD();
        assertEquals(TRN_XML_PATH, sd.getDescriptionFilePath());
    }

    // ------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------

    @Test
    public void testVariableListElementsNotNull() {
        SystemDescription sd = createSD();
        DatabaseVariablesListElement[] vars = sd.getDatabaseVariableListElements();
        assertNotNull(vars);
    }

    @Test
    public void testVariableListElementsNonEmpty() {
        SystemDescription sd = createSD();
        DatabaseVariablesListElement[] vars = sd.getDatabaseVariableListElements();
        assertTrue("TRN.xml should define at least one variable", vars.length > 0);
    }

    @Test
    public void testAllVariablesHaveShortName() {
        SystemDescription sd = createSD();
        for (DatabaseVariablesListElement v : sd.getDatabaseVariableListElements()) {
            assertNotNull("Every variable must have a short name", v.getShortName());
            assertFalse("Short name must not be empty", v.getShortName().trim().isEmpty());
        }
    }

    @Test
    public void testAllVariablesHaveTableAssigned() {
        SystemDescription sd = createSD();
        for (DatabaseVariablesListElement v : sd.getDatabaseVariableListElements()) {
            assertNotNull("Every variable must belong to a table", v.getDatabaseTableName());
            assertFalse("Table name must not be empty", v.getDatabaseTableName().trim().isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // Dictionaries
    // ------------------------------------------------------------------

    @Test
    public void testDictionaryListElementsNotNull() {
        SystemDescription sd = createSD();
        DatabaseDictionaryListElement[] dicts = sd.getDatabaseDictionaryListElements();
        assertNotNull(dicts);
    }

    @Test
    public void testDictionaryListElementsNonEmpty() {
        SystemDescription sd = createSD();
        DatabaseDictionaryListElement[] dicts = sd.getDatabaseDictionaryListElements();
        assertTrue("TRN.xml should define at least one dictionary", dicts.length > 0);
    }

    @Test
    public void testEachDictionaryHasName() {
        SystemDescription sd = createSD();
        for (DatabaseDictionaryListElement d : sd.getDatabaseDictionaryListElements()) {
            assertNotNull("Dictionary name must not be null",  d.getName());
            assertFalse("Dictionary name must not be empty",   d.getName().trim().isEmpty());
        }
    }

    @Test
    public void testRecordStatusDictionaryIsPresent() {
        SystemDescription sd = createSD();
        boolean found = false;
        for (DatabaseDictionaryListElement d : sd.getDatabaseDictionaryListElements()) {
            if ("Record status".equalsIgnoreCase(d.getName())) {
                found = true;
                break;
            }
        }
        assertTrue("TRN.xml should contain a 'Record status' dictionary", found);
    }

    // ------------------------------------------------------------------
    // Groups
    // ------------------------------------------------------------------

    @Test
    public void testGroupListElementsNotNull() {
        SystemDescription sd = createSD();
        DatabaseGroupsListElement[] groups = sd.getDatabaseGroupsListElements();
        assertNotNull(groups);
    }

    @Test
    public void testGroupListElementsNonEmpty() {
        SystemDescription sd = createSD();
        DatabaseGroupsListElement[] groups = sd.getDatabaseGroupsListElements();
        assertTrue("TRN.xml should define at least one group", groups.length > 0);
    }

    @Test
    public void testEachGroupHasName() {
        SystemDescription sd = createSD();
        for (DatabaseGroupsListElement g : sd.getDatabaseGroupsListElements()) {
            assertNotNull("Group name must not be null",  g.getGroupName());
            assertFalse("Group name must not be empty",   g.getGroupName().trim().isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // Internal DOM document
    // ------------------------------------------------------------------

    @Test
    public void testSystemDescriptionDocumentIsNotNull() {
        SystemDescription sd = createSD();
        assertNotNull("Parsed DOM document must not be null", sd.getSystemDescriptionDocument());
    }
}
