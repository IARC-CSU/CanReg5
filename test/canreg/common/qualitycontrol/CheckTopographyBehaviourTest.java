package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckTopographyBehaviour}.
 */
public class CheckTopographyBehaviourTest {

    private final CheckTopographyBehaviour check = new CheckTopographyBehaviour();

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.TopographyBehaviour, check.getCheckName());
        assertEquals(2, check.getVariablesNeeded().length);
    }

    @Test
    public void testMalignantBehaviour_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "400");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testInSituInEpithelialSite_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Behaviour, "2");
        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testInSituInBoneOrBrainOrBlood_Rare() {
        // TopGroups 40, 41, 42, 47, 49, 70-72 with behaviour 2 -> Rare
        String[] topographies = {"400", "410", "421", "470", "490", "700", "710", "720"};
        for (String top : topographies) {
            Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
            vars.put(StandardVariableNames.Topography, top);
            vars.put(StandardVariableNames.Behaviour, "2");
            assertEquals("Expected Rare for topography " + top,
                    CheckResult.ResultCode.Rare, check.performCheck(vars).getResultCode());
        }
    }

    @Test
    public void testMissingAndInvalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());

        vars.put(StandardVariableNames.Topography, "ABC");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }
}

