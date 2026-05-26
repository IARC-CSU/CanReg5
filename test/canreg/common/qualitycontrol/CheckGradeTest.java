package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckGrade}.
 * All tests are hermetic – no I/O, no DB.
 */
public class CheckGradeTest {

    private CheckGrade newCheck() { return new CheckGrade(); }

    private Map<StandardVariableNames, Object> vars(String behaviour, String morphology, String grade) {
        Map<StandardVariableNames, Object> m = new LinkedHashMap<StandardVariableNames, Object>();
        m.put(StandardVariableNames.Behaviour, behaviour);
        m.put(StandardVariableNames.Morphology, morphology);
        m.put(StandardVariableNames.Grade, grade);
        return m;
    }

    // ------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------

    @Test
    public void testGetCheckName() {
        assertEquals(Checker.CheckNames.Grade, newCheck().getCheckName());
    }

    @Test
    public void testGetVariablesNeeded() {
        StandardVariableNames[] needed = newCheck().getVariablesNeeded();
        assertNotNull(needed);
        assertEquals(3, needed.length);
    }

    // ------------------------------------------------------------------
    // Non-malignant (behaviour != 3)
    // ------------------------------------------------------------------

    @Test
    public void testNonMalignant_GradeNine_OK() {
        // Behaviour 2 (in situ) with grade 9 (unknown) → OK
        CheckResult result = newCheck().performCheck(vars("2", "8500", "9"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testNonMalignant_GradeBlank_Invalid() {
        // Integer.parseInt("") throws NumberFormatException → Invalid
        // (The source code's logic for empty-grade would only be reached if parseInt succeeds first)
        CheckResult result = newCheck().performCheck(vars("2", "8500", ""));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testNonMalignant_GradeOne_Invalid() {
        // Non-malignant with a non-blank non-9 grade → Invalid
        CheckResult result = newCheck().performCheck(vars("2", "8500", "1"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testBehaviourZero_GradeThree_Invalid() {
        CheckResult result = newCheck().performCheck(vars("0", "8000", "3"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Malignant (behaviour = 3) – missing grade
    // ------------------------------------------------------------------

    @Test
    public void testMalignant_EmptyGrade_Invalid() {
        // Integer.parseInt("") throws NumberFormatException → Invalid
        // Note: the source's empty-grade check is unreachable because parseInt fires first
        CheckResult result = newCheck().performCheck(vars("3", "8500", ""));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Malignant (behaviour = 3) – grade out of range
    // ------------------------------------------------------------------

    @Test
    public void testMalignant_GradeZero_Invalid() {
        // Grade 0 is not a valid code (valid range 1-9)
        CheckResult result = newCheck().performCheck(vars("3", "8500", "0"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testMalignant_GradeTen_Invalid() {
        CheckResult result = newCheck().performCheck(vars("3", "8500", "10"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Malignant – morphology-constrained grade checks
    // ------------------------------------------------------------------

    @Test
    public void testWellDifferentiated_Morphology8331_GradeOne_OK() {
        // Morphology 8331 must have grade 1
        CheckResult result = newCheck().performCheck(vars("3", "8331", "1"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testWellDifferentiated_Morphology8331_GradeTwo_Invalid() {
        CheckResult result = newCheck().performCheck(vars("3", "8331", "2"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testUndifferentiated_Morphology8020_GradeFour_OK() {
        CheckResult result = newCheck().performCheck(vars("3", "8020", "4"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testUndifferentiated_Morphology8020_GradeOne_Invalid() {
        CheckResult result = newCheck().performCheck(vars("3", "8020", "1"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testBcellLymphoma_Morphology9675_GradeSix_OK() {
        // Morphology 9675 (B-cell lymphoma range 9670-9699), grade 6
        CheckResult result = newCheck().performCheck(vars("3", "9675", "6"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testBcellLymphoma_Morphology9675_GradeOne_Invalid() {
        CheckResult result = newCheck().performCheck(vars("3", "9675", "1"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testGrade5to8_NonLymphomaMorphology_Invalid() {
        // Grade 5-8 only valid for lymphomas (morphology >= 9590); regular morphology → Invalid
        CheckResult result = newCheck().performCheck(vars("3", "8500", "5"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testGradeNine_AlwaysOK_ForMalignant() {
        // Grade 9 (unknown) is always OK for malignant tumours
        CheckResult result = newCheck().performCheck(vars("3", "8500", "9"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Error handling
    // ------------------------------------------------------------------

    @Test
    public void testEmptyMap_Missing() {
        CheckResult result = newCheck().performCheck(new LinkedHashMap<StandardVariableNames, Object>());
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testNonNumericBehaviour_Invalid() {
        CheckResult result = newCheck().performCheck(vars("X", "8500", "1"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testNonNumericGrade_Invalid() {
        CheckResult result = newCheck().performCheck(vars("3", "8500", "G"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testNullBehaviour_Missing() {
        Map<StandardVariableNames, Object> m = new LinkedHashMap<StandardVariableNames, Object>();
        m.put(StandardVariableNames.Morphology, "8500");
        m.put(StandardVariableNames.Grade,      "1");
        CheckResult result = newCheck().performCheck(m);
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }
}