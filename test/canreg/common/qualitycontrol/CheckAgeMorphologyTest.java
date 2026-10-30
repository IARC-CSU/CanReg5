package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckAgeMorphology}.
 */
public class CheckAgeMorphologyTest {

    private final CheckAgeMorphology check = new CheckAgeMorphology();

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.AgeMorphology, check.getCheckName());
        assertEquals(2, check.getVariablesNeeded().length);
    }

    @Test
    public void testNormalAdultMorphology_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "55");
        vars.put(StandardVariableNames.Morphology, "8140");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testYoungAgeWithMyelomaOrCLL_Rare() {
        // age <= 25 with 9823 -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "20");
        vars.put(StandardVariableNames.Morphology, "9823");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testAdultWithPediatricBlastoma_Rare() {
        // age >= 15 with 8960 (nephroblastoma) or 9500 (neuroblastoma) -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "30");
        vars.put(StandardVariableNames.Morphology, "8960");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testChildWithPediatricBlastoma_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "4");
        vars.put(StandardVariableNames.Morphology, "8960");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testChildWithMultipleMyeloma9732_Rare() {
        // age < 15 with 9732 -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "10");
        vars.put(StandardVariableNames.Morphology, "9732");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testShortMorphology_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "50");
        vars.put(StandardVariableNames.Morphology, "81");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testNonNumericInput_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "XX");
        vars.put(StandardVariableNames.Morphology, "8140");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMissingInput_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());
    }
}

