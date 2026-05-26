package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckBasis}.
 * All tests are hermetic – no I/O, no DB.
 */
public class CheckBasisTest {

    private CheckBasis newCheck() { return new CheckBasis(); }

    // ------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------

    @Test
    public void testGetVariablesNeeded() {
        CheckBasis instance = newCheck();
        StandardVariableNames[] result = instance.getVariablesNeeded();
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(StandardVariableNames.BasisDiagnosis, result[0]);
        assertEquals(StandardVariableNames.Morphology,     result[1]);
        assertEquals(StandardVariableNames.Topography,     result[2]);
    }

    @Test
    public void testGetCheckName() {
        assertEquals(Checker.CheckNames.Basis, newCheck().getCheckName());
    }

    // ------------------------------------------------------------------
    // Non-microscopically confirmed (basis 0-4) → Query for non-exempt morphology
    // ------------------------------------------------------------------

    @Test
    public void testNonMicroscopicallyConfirmedBasicMorphology_Query() {
        // Basis 3 (clinical only) + morphology 8331 (NOT in exempt list) → Query
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "3");
        vars.put(StandardVariableNames.Topography,     "100");
        vars.put(StandardVariableNames.Morphology,     "8331");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Query, result.getResultCode());
    }

    @Test
    public void testBasisZeroNonExemptMorphology_Query() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "0");
        vars.put(StandardVariableNames.Topography,     "100");
        vars.put(StandardVariableNames.Morphology,     "8500");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Query, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Microscopically confirmed (basis 5-8) → OK for non-exempt morphology
    // ------------------------------------------------------------------

    @Test
    public void testMicroscopicallyConfirmed_OK() {
        // Basis 7 (histology) + normal morphology → OK
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "7");
        vars.put(StandardVariableNames.Topography,     "500");
        vars.put(StandardVariableNames.Morphology,     "8500");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testBasisFiveIsAlsoMicroscopicallyConfirmed() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "5");
        vars.put(StandardVariableNames.Topography,     "200");
        vars.put(StandardVariableNames.Morphology,     "8500");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Unknown basis (9) → Query for non-exempt morphology
    // ------------------------------------------------------------------

    @Test
    public void testUnknownBasis_Query() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "9");
        vars.put(StandardVariableNames.Topography,     "100");
        vars.put(StandardVariableNames.Morphology,     "8500");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Query, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Exempt morphology codes → always OK regardless of basis
    // ------------------------------------------------------------------

    @Test
    public void testExemptMorphology_8000_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "0"); // Non-MV
        vars.put(StandardVariableNames.Topography,     "100");
        vars.put(StandardVariableNames.Morphology,     "8000");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testExemptMorphology_9732_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "2"); // Non-MV
        vars.put(StandardVariableNames.Topography,     "420");
        vars.put(StandardVariableNames.Morphology,     "9732");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testExemptMorphology_9800_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "1");
        vars.put(StandardVariableNames.Topography,     "420");
        vars.put(StandardVariableNames.Morphology,     "9800");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testExemptMorphology_8720_InMelanomaTopography_OK() {
        // 8720 + topography 690..699 (TopGroup 69) should be exempt
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "0");
        vars.put(StandardVariableNames.Topography,     "690");
        vars.put(StandardVariableNames.Morphology,     "8720");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    // ------------------------------------------------------------------
    // Error handling
    // ------------------------------------------------------------------

    @Test
    public void testMissingBasisDiagnosis_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "100");
        vars.put(StandardVariableNames.Morphology, "8000");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testEmptyMap_Missing() {
        CheckResult result = newCheck().performCheck(new LinkedHashMap<StandardVariableNames, Object>());
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testNonNumericBasis_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "X");
        vars.put(StandardVariableNames.Topography,     "100");
        vars.put(StandardVariableNames.Morphology,     "8000");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testNonNumericMorphology_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BasisDiagnosis, "7");
        vars.put(StandardVariableNames.Topography,     "100");
        vars.put(StandardVariableNames.Morphology,     "MORPH");
        CheckResult result = newCheck().performCheck(vars);
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }
}