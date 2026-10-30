package canreg.common.qualitycontrol;

import canreg.TestFixtures;
import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckDateOfLastContact} and {@link CheckDateOfDeathDateDLCBasis}.
 */
public class CheckDateOfLastContactTest {

    private CheckDateOfLastContact check;
    private CheckDateOfDeathDateDLCBasis checkDlcBasis;

    @Before
    public void setUp() {
        Map<StandardVariableNames, canreg.common.DatabaseVariablesListElement> map =
                TestFixtures.createStandardVariablesMap();
        check = new CheckDateOfLastContact();
        check.setVariableListElementsMap(map);
        checkDlcBasis = new CheckDateOfDeathDateDLCBasis();
        checkDlcBasis.setVariableListElementsMap(map);
    }

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.DateOfLastContact, check.getCheckName());
        assertEquals(2, check.getVariablesNeeded().length);
        assertEquals(Checker.CheckNames.DateOfLastContact, checkDlcBasis.getCheckName());
    }

    @Test
    public void testValidDlcAfterIncidence_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.IncidenceDate, "20150101");
        vars.put(StandardVariableNames.Lastcontact, "20180615");

        assertEquals(CheckResult.ResultCode.OK, check.performCheck(vars).getResultCode());
        assertEquals(CheckResult.ResultCode.OK, checkDlcBasis.performCheck(vars).getResultCode());
    }

    @Test
    public void testDlcBeforeIncidence_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.IncidenceDate, "20180615");
        vars.put(StandardVariableNames.Lastcontact, "20150101");

        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
        assertEquals(CheckResult.ResultCode.Invalid, checkDlcBasis.performCheck(vars).getResultCode());
    }

    @Test
    public void testDlcInFuture_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.IncidenceDate, "20180615");
        vars.put(StandardVariableNames.Lastcontact, "20990101");

        assertEquals(CheckResult.ResultCode.Invalid, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testUnknownDlcYear_NotDone() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.IncidenceDate, "20180615");
        vars.put(StandardVariableNames.Lastcontact, "99999999");

        assertEquals(CheckResult.ResultCode.NotDone, check.performCheck(vars).getResultCode());
    }

    @Test
    public void testMissingVariables_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(CheckResult.ResultCode.Missing, check.performCheck(vars).getResultCode());
    }
}

