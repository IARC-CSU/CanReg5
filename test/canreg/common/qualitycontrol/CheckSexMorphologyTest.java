package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckSexMorphology}.
 */
public class CheckSexMorphologyTest {

    private final CheckSexMorphology check = new CheckSexMorphology();

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.SexMorphology, check.getCheckName());
        assertEquals(3, check.getVariablesNeeded().length);
    }

    @Test
    public void testNormalMaleAndFemale_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Morphology, "8140");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());

        vars.put(StandardVariableNames.Sex, "2");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMaleWithOvarianSpecificMorphology_Rare() {
        // 8441 (Serous cystadenocarcinoma, family +24 Ovary) in male (sex 1) -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Morphology, "8441");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testFemaleWithOvarianSpecificMorphology_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "2");
        vars.put(StandardVariableNames.Morphology, "8441");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testFemaleWithTesticularSeminoma_Rare() {
        // 9061 (Seminoma, family +28 Prostate/Testis) in female (sex 2) -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "2");
        vars.put(StandardVariableNames.Morphology, "9061");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testUnknownMorphology_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Morphology, "9999");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMissingAndShortMorphology() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());

        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Morphology, "80");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }
}

