package canreg.common;

import canreg.TestFixtures;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.management.SystemDescription;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link GlobalToolBox}.
 */
public class GlobalToolBoxTest {

    @Test
    public void testGlobalToolBoxMappingsAndLookups() {
        SystemDescription sd = TestFixtures.loadTestSystemDescription();
        GlobalToolBox toolBox = new GlobalToolBox(sd.getSystemDescriptionDocument());

        assertNotNull(toolBox.getDocument());
        assertNotNull(toolBox.getStandardCharset());
        assertTrue(toolBox.getVariables().length > 0);
        assertFalse(toolBox.getStandardVariables().isEmpty());

        assertEquals("RegNo", toolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.PatientID.toString()));
        assertNull(toolBox.translateStandardVariableNameToDatabaseVariableName("NonExistentStdVar"));

        assertEquals("RegNo", toolBox.getPatientIDVariableName(new Patient()));
        assertTrue("PATIENTIDTUMOURTABLE".equalsIgnoreCase(toolBox.getPatientIDVariableName(new Tumour())));
        assertTrue("TUMOURIDSOURCETABLE".equalsIgnoreCase(toolBox.getPatientIDVariableName(new Source())));

        assertEquals(999, toolBox.getUnknownAgeCode());
        assertNotNull(toolBox.getDateFormatString());
    }
}