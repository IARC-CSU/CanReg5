package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckAgeTopography}.
 */
public class CheckAgeTopographyTest {

    private final CheckAgeTopography check = new CheckAgeTopography();

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.AgeTopography, check.getCheckName());
        assertEquals(2, check.getVariablesNeeded().length);
    }

    @Test
    public void testNormalAdultTopography_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "60");
        vars.put(StandardVariableNames.Topography, "619");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testUnder5Prostate_Rare() {
        // age < 5 and topographyGroup 61 -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "3");
        vars.put(StandardVariableNames.Topography, "619");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testUnder20EsophagusOrBreast_Rare() {
        // age < 20 and topographyGroup 15 (esophagus) or 50 (breast) -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "16");
        vars.put(StandardVariableNames.Topography, "509");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testUnder20Pleura384_Rare() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "18");
        vars.put(StandardVariableNames.Topography, "384");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMissingAndInvalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());

        vars.put(StandardVariableNames.Age, "ABC");
        vars.put(StandardVariableNames.Topography, "509");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }
}

