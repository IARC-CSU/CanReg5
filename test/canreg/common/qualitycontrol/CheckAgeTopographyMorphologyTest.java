package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckAgeTopographyMorphology}.
 */
public class CheckAgeTopographyMorphologyTest {

    private final CheckAgeTopographyMorphology check = new CheckAgeTopographyMorphology();

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.AgeTopographyMorphology, check.getCheckName());
        assertEquals(3, check.getVariablesNeeded().length);
    }

    @Test
    public void testNormalCombination_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "65");
        vars.put(StandardVariableNames.Topography, "619");
        vars.put(StandardVariableNames.Morphology, "8140");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testYoungProstateAdenocarcinoma_Rare() {
        // age < 40, topGroup 61, morph 814x -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "30");
        vars.put(StandardVariableNames.Topography, "619");
        vars.put(StandardVariableNames.Morphology, "8140");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testYoungColonNonCarcinoid_Rare() {
        // age < 20, topGroup 18, morph / 10 != 824 -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "15");
        vars.put(StandardVariableNames.Topography, "180");
        vars.put(StandardVariableNames.Morphology, "8140");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testYoungColonCarcinoid_OK() {
        // age < 20, topGroup 18, morph 8240 -> OK
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "15");
        vars.put(StandardVariableNames.Topography, "180");
        vars.put(StandardVariableNames.Morphology, "8240");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testRetinoblastomaOverAge5_Rare() {
        // age > 5, morph 9510, topGroup 69 -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "12");
        vars.put(StandardVariableNames.Topography, "692");
        vars.put(StandardVariableNames.Morphology, "9510");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testChoriocarcinomaPlacentaOutsideReproductiveAge_Rare() {
        // (age < 15 || age > 45), topGroup 58, morph 9100 -> Rare
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "50");
        vars.put(StandardVariableNames.Topography, "589");
        vars.put(StandardVariableNames.Morphology, "9100");
        assertEquals(CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMissingAndInvalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());

        vars.put(StandardVariableNames.Age, "45");
        vars.put(StandardVariableNames.Topography, "619");
        vars.put(StandardVariableNames.Morphology, "81");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }
}

