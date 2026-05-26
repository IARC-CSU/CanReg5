package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckMorphology}.
 * The check loads its lookup table from a classpath resource; no external I/O needed.
 */
public class CheckMorphologyTest {

    private CheckMorphology newCheck() { return new CheckMorphology(); }

    // ------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------

    @Test
    public void testGetCheckName() {
        assertEquals(Checker.CheckNames.Morphology, newCheck().getCheckName());
    }

    @Test
    public void testGetVariablesNeeded() {
        StandardVariableNames[] needed = newCheck().getVariablesNeeded();
        assertNotNull(needed);
        assertEquals(1, needed.length);
        assertEquals(StandardVariableNames.Morphology, needed[0]);
    }

    // ------------------------------------------------------------------
    // Valid morphology codes (present in MorphFam.txt)
    // ------------------------------------------------------------------

    @Test
    public void testValidCode_8000_OK() {
        Map<StandardVariableNames, Object> vars = vars("8000");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testValidCode_8500_OK() {
        CheckResult result = newCheck().performCheck(vars("8500"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testValidCode_WithBehaviourSuffix_OK() {
        // CanReg often stores morphology as "85003" (code + behaviour digit)
        // The check only looks at the first 4 characters
        CheckResult result = newCheck().performCheck(vars("85003"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Invalid morphology codes
    // ------------------------------------------------------------------

    @Test
    public void testInvalidCode_0000_Invalid() {
        // 0000 is not a valid ICD-O-3 morphology code
        CheckResult result = newCheck().performCheck(vars("0000"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testTooShortCode_Invalid() {
        // Fewer than 4 digits → Invalid (length check)
        CheckResult result = newCheck().performCheck(vars("850"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Missing variable
    // ------------------------------------------------------------------

    @Test
    public void testMissingVariable_Missing() {
        CheckResult result = newCheck().performCheck(new LinkedHashMap<StandardVariableNames, Object>());
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testNullValue_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Morphology, null);
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Map<StandardVariableNames, Object> vars(String morphCode) {
        Map<StandardVariableNames, Object> m = new LinkedHashMap<StandardVariableNames, Object>();
        m.put(StandardVariableNames.Morphology, morphCode);
        return m;
    }
}
