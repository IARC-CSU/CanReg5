package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckSexTopography}.
 * All tests are hermetic – no I/O, no DB.
 *
 * Key rules encoded in the class:
 *  - Male (sex=1) + female-only topographies (TopGroup 51-58) → Invalid
 *  - Female (sex=2) + male-only topographies (TopGroup 60-63) → Invalid
 *  - Any other combination → OK
 *
 * TopGroup = topographyNumber / 10  (integer division)
 * e.g. topography 620 → TopGroup 62 (prostate is around C61, so we use sites that map correctly)
 */
public class CheckSexTopographyTest {

    private CheckSexTopography newCheck() { return new CheckSexTopography(); }

    // ------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------

    @Test
    public void testGetCheckName() {
        assertEquals(Checker.CheckNames.SexTopography, newCheck().getCheckName());
    }

    @Test
    public void testGetVariablesNeeded() {
        StandardVariableNames[] needed = newCheck().getVariablesNeeded();
        assertNotNull(needed);
        assertEquals(2, needed.length);
    }

    // ------------------------------------------------------------------
    // Compatible sex/topography combinations → OK
    // ------------------------------------------------------------------

    @Test
    public void testMale_NonSexSpecificSite_OK() {
        // Male, Lung (C34 → 340) → OK
        CheckResult result = newCheck().performCheck(vars("1", "340"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testFemale_NonSexSpecificSite_OK() {
        // Female, Colon (C18 → 180) → OK
        CheckResult result = newCheck().performCheck(vars("2", "180"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testFemale_FemaleSpecificSite_OK() {
        // Female, Cervix (C53 → 530, TopGroup 53) → OK
        CheckResult result = newCheck().performCheck(vars("2", "530"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testMale_MaleSpecificSite_OK() {
        // Male, Prostate area: topography 610 (TopGroup 61) → OK for male
        // Actually, TopGroup 60-63 is the "male-only" zone that is Invalid for Female
        CheckResult result = newCheck().performCheck(vars("1", "610"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testUnknownSex_NonSpecificSite_OK() {
        // Sex 9 (unknown) → no rule fires → OK
        CheckResult result = newCheck().performCheck(vars("9", "200"));
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Incompatible sex/topography combinations → Invalid
    // ------------------------------------------------------------------

    @Test
    public void testMale_FemaleOnlySite_Invalid() {
        // Male (sex=1), Cervix (topography=530, TopGroup=53 which is in 51-58) → Invalid
        CheckResult result = newCheck().performCheck(vars("1", "530"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testMale_Ovary_Invalid() {
        // Male (sex=1), Ovary (topography=560, TopGroup=56) → Invalid
        CheckResult result = newCheck().performCheck(vars("1", "560"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testFemale_MaleOnlySite_Invalid() {
        // Female (sex=2), Prostate area (topography=610, TopGroup=61 which is in 60-63) → Invalid
        CheckResult result = newCheck().performCheck(vars("2", "610"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testFemale_Testis_Invalid() {
        // Female (sex=2), Testis (topography=620, TopGroup=62) → Invalid
        CheckResult result = newCheck().performCheck(vars("2", "620"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Error handling
    // ------------------------------------------------------------------

    @Test
    public void testMissingBothVariables_Missing() {
        CheckResult result = newCheck().performCheck(new LinkedHashMap<StandardVariableNames, Object>());
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testMissingSex_Missing() {
        Map<StandardVariableNames, Object> m = new LinkedHashMap<StandardVariableNames, Object>();
        m.put(StandardVariableNames.Topography, "500");
        CheckResult result = newCheck().performCheck(m);
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testNonNumericSex_Invalid() {
        CheckResult result = newCheck().performCheck(vars("M", "500"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testNonNumericTopography_Invalid() {
        CheckResult result = newCheck().performCheck(vars("1", "LUNG"));
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Map<StandardVariableNames, Object> vars(String sex, String topography) {
        Map<StandardVariableNames, Object> m = new LinkedHashMap<StandardVariableNames, Object>();
        m.put(StandardVariableNames.Sex, sex);
        m.put(StandardVariableNames.Topography, topography);
        return m;
    }
}
