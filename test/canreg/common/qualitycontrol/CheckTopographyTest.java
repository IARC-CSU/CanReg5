package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckTopography}.
 * The check loads its lookup table from a classpath resource; no external I/O needed.
 */
public class CheckTopographyTest {

    private CheckTopography newCheck() { return new CheckTopography(); }

    // ------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------

    @Test
    public void testGetCheckName() {
        assertEquals(Checker.CheckNames.Topography, newCheck().getCheckName());
    }

    @Test
    public void testGetVariablesNeeded() {
        StandardVariableNames[] needed = newCheck().getVariablesNeeded();
        assertNotNull(needed);
        assertEquals(1, needed.length);
        assertEquals(StandardVariableNames.Topography, needed[0]);
    }

    // ------------------------------------------------------------------
    // Valid topography codes (present in O3_10T.txt – 3-digit numeric codes)
    // ------------------------------------------------------------------

    @Test
    public void testValidCode_500_Breast_OK() {
        // C50 → Breast (topography number 500)
        CheckResult result = newCheck().performCheck(vars("500"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testValidCode_000_Lip_OK() {
        // C00 → Lip
        CheckResult result = newCheck().performCheck(vars("000"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testValidCode_809_Haematopoietic_OK() {
        // C80.9 → ill-defined sites; common valid upper boundary
        CheckResult result = newCheck().performCheck(vars("809"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Invalid topography codes
    // ------------------------------------------------------------------

    @Test
    public void testInvalidCode_999_Invalid() {
        // 999 does not exist in ICD-O-3 topography
        CheckResult result = newCheck().performCheck(vars("999"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testInvalidCode_Negative_Invalid() {
        CheckResult result = newCheck().performCheck(vars("-001"));
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
        vars.put(StandardVariableNames.Topography, null);
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Map<StandardVariableNames, Object> vars(String topCode) {
        Map<StandardVariableNames, Object> m = new LinkedHashMap<StandardVariableNames, Object>();
        m.put(StandardVariableNames.Topography, topCode);
        return m;
    }
}
