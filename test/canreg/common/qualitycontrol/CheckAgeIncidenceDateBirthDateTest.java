package canreg.common.qualitycontrol;

import canreg.TestFixtures;
import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CheckAgeIncidenceDateBirthDate}.
 */
public class CheckAgeIncidenceDateBirthDateTest {

    private CheckAgeIncidenceDateBirthDate check;

    @Before
    public void setUp() {
        check = new CheckAgeIncidenceDateBirthDate();
        check.setVariableListElementsMap(TestFixtures.createStandardVariablesMap());
    }

    @Test
    public void testMetadata() {
        assertEquals(Checker.CheckNames.AgeIncidenceDateBirthDate, check.getCheckName());
        StandardVariableNames[] needed = check.getVariablesNeeded();
        assertEquals(3, needed.length);
    }

    @Test
    public void testConsistentAgeAndDates_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "50");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testUnknownMonthAllowsOneYearDifference_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19609999");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "49");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testIncidenceBeforeBirthDate_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "20100515");
        vars.put(StandardVariableNames.IncidenceDate, "20000515");
        vars.put(StandardVariableNames.Age, "0");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testUnknownAgeWithKnownYears_Query() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "999");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.Query, result.getResultCode());
    }

    @Test
    public void testAgeMismatch_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "65");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testUnknownBirthYear_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "99999999");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "999");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }

    @Test
    public void testMissingVariables_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Age, "50");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.Missing, result.getResultCode());
    }

    @Test
    public void testNonNumericAge_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "FIFTY");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }
}

