package canreg.common.qualitycontrol;

import canreg.TestFixtures;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import java.util.LinkedList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Checker} orchestration.
 */
public class CheckerTest {

    @Test
    public void testPerformChecks_AllValidRecord_ReturnsAllOK() {
        Checker checker = new Checker(TestFixtures.createStandardVariablesList());

        Patient patient = new Patient();
        patient.setVariable("BIRTHD", "19600115");
        patient.setVariable("DLC", "20200601");
        patient.setVariable("SEX", "2");

        Tumour tumour = new Tumour();
        tumour.setVariable("AGE", "55");
        tumour.setVariable("INCID", "20150115");
        tumour.setVariable("TOP", "509");
        tumour.setVariable("MOR", "8500");
        tumour.setVariable("BEH", "3");
        tumour.setVariable("BAS", "7");
        tumour.setVariable("GRADE", "2");

        LinkedList<CheckResult> results = checker.performChecks(patient, tumour);
        assertFalse("Checker should execute registered checks", results.isEmpty());
        for (CheckResult res : results) {
            assertEquals("Expected OK for check " + res.getCheckName() + " (" + res.getMessage() + ")",
                    CheckResult.ResultCode.OK, res.getResultCode());
        }
    }

    @Test
    public void testPerformChecks_DetectsSexTopographyConflict() {
        Checker checker = new Checker(TestFixtures.createStandardVariablesList());

        Patient patient = new Patient();
        patient.setVariable("BIRTHD", "19600115");
        patient.setVariable("DLC", "20200601");
        patient.setVariable("SEX", "2"); // Female

        Tumour tumour = new Tumour();
        tumour.setVariable("AGE", "55");
        tumour.setVariable("INCID", "20150115");
        tumour.setVariable("TOP", "619"); // Prostate in Female -> Invalid
        tumour.setVariable("MOR", "8140");
        tumour.setVariable("BEH", "3");
        tumour.setVariable("BAS", "7");
        tumour.setVariable("GRADE", "2");

        LinkedList<CheckResult> results = checker.performChecks(patient, tumour);
        CheckResult.ResultCode worst = CheckResult.ResultCode.OK;
        for (CheckResult res : results) {
            worst = CheckResult.decideWorstResultCode(res.getResultCode(), worst);
        }
        assertEquals(CheckResult.ResultCode.Invalid, worst);
    }

    @Test
    public void testCheckResultDatabaseMapping() {
        assertEquals("0", CheckResult.toDatabaseVariable(CheckResult.ResultCode.NotDone));
        assertEquals("1", CheckResult.toDatabaseVariable(CheckResult.ResultCode.OK));
        assertEquals("2", CheckResult.toDatabaseVariable(CheckResult.ResultCode.Rare));
        assertEquals("3", CheckResult.toDatabaseVariable(CheckResult.ResultCode.Invalid));

        assertEquals(CheckResult.ResultCode.NotDone, CheckResult.toResultCode("0"));
        assertEquals(CheckResult.ResultCode.OK, CheckResult.toResultCode("1"));
        assertEquals(CheckResult.ResultCode.Rare, CheckResult.toResultCode("2"));
        assertEquals(CheckResult.ResultCode.Invalid, CheckResult.toResultCode("3"));
    }
}