package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckTopographyMorphology}.
 */
public class CheckTopographyMorphologyTest {

    private final CheckTopographyMorphology check = new CheckTopographyMorphology();

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.TopographyMorphology, check.getCheckName());
        assertEquals(2, check.getVariablesNeeded().length);
    }

    @Test
    public void testValidCombinations_OK() {
        // 8000 (Neoplasm, NOS -> '*' family) + any valid 3-digit topography -> OK
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Morphology, "8000");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());

        // 8500 (Infiltrating duct carcinoma -> '+21' Breast) + C509 -> OK
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Morphology, "8500");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());

        // 8170 (Hepatocellular carcinoma -> '+07' Liver) + C220 -> OK
        vars.put(StandardVariableNames.Topography, "220");
        vars.put(StandardVariableNames.Morphology, "8170");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMustFamilyMismatch_Rare() {
        // 8170 (Hepatocellular carcinoma -> '+07' Liver C220) in Breast (509) -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Morphology, "8170");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMustNotFamilyMatch_Rare() {
        // 8140 (Adenocarcinoma NOS -> '-02' MustNot in C40*, C41*, C42*, C70*, C71*, C72*, C77*)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "710");
        vars.put(StandardVariableNames.Morphology, "8140");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());

        // 8140 in colon (180) -> OK
        vars.put(StandardVariableNames.Topography, "180");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testTwoDigitTopography_Query() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "42");
        vars.put(StandardVariableNames.Morphology, "8000");
        assertEquals(CheckResult.ResultCode.Query, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testInvalidAndMissing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());

        // 1-digit topography -> Invalid
        vars.put(StandardVariableNames.Topography, "5");
        vars.put(StandardVariableNames.Morphology, "8000");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());

        // Short morphology -> Invalid
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Morphology, "80");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());

        // Non-existent morphology -> Invalid
        vars.put(StandardVariableNames.Morphology, "9999");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }
}

