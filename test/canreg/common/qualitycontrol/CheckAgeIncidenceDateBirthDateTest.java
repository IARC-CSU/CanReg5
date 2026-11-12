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
    public void testOneYearDifferenceExactDates_Query() {
        // Calculated age is 50, stated age is 49 (1-year lower difference)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "49");

        CheckResult result = check.performCheck(vars);
        assertEquals("A 1-year discrepancy with exact dates should be a Query rather than Invalid",
                CheckResult.ResultCode.Query, result.getResultCode());
        assertTrue(result.getMessage().contains("Age query"));
    }

    @Test
    public void testOneYearDifferenceExactDatesUpper_Query() {
        // Calculated age is 50, stated age is 51 (1-year upper difference)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "51");

        CheckResult result = check.performCheck(vars);
        assertEquals("A 1-year discrepancy with exact dates should be a Query rather than Invalid",
                CheckResult.ResultCode.Query, result.getResultCode());
        assertTrue(result.getMessage().contains("Age query"));
    }

    @Test
    public void testTwoYearDifferenceExactDates_Invalid() {
        // Calculated age is 50, stated age is 48 (2-year difference -> Invalid)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19600515");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "48");

        CheckResult result = check.performCheck(vars);
        assertEquals("A 2-year discrepancy with exact dates should remain Invalid",
                CheckResult.ResultCode.Invalid, result.getResultCode());
        assertTrue(result.getMessage().contains("Age error"));
    }

    @Test
    public void testOneYearBeyondUnknownMonthAllowance_Query() {
        // Birth: 19609999, Incidence: 20100515 -> calculatedAge 49, allowedDifference 1
        // Stated age 47 is 2 years from 49 (1 year beyond allowedDifference 1) -> Query
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19609999");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "47");

        CheckResult result = check.performCheck(vars);
        assertEquals("A 1-year difference beyond unknown month allowance should be a Query",
                CheckResult.ResultCode.Query, result.getResultCode());
        assertTrue(result.getMessage().contains("Age query"));
    }

    @Test
    public void testTwoYearsBeyondUnknownMonthAllowance_Invalid() {
        // Birth: 19609999, Incidence: 20100515 -> calculatedAge 49, allowedDifference 1
        // Stated age 46 is 3 years from 49 (2 years beyond allowedDifference 1) -> Invalid
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19609999");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "46");

        CheckResult result = check.performCheck(vars);
        assertEquals("A 2+ year difference beyond unknown month allowance should be Invalid",
                CheckResult.ResultCode.Invalid, result.getResultCode());
        assertTrue(result.getMessage().contains("Age error"));
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

    @Test
    public void testInvalidFebruaryDateReturnsInvalidInsteadOfThrowingException() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        // 1950 is not a leap year; previously caused uncaught IllegalArgumentException: MONTH: 1 -> 2
        vars.put(StandardVariableNames.BirthDate, "19500229");
        vars.put(StandardVariableNames.IncidenceDate, "20100515");
        vars.put(StandardVariableNames.Age, "60");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.Invalid, result.getResultCode());
    }

    @Test
    public void testUnknownDayInFebruary_OK() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19500299");
        vars.put(StandardVariableNames.IncidenceDate, "20100215");
        vars.put(StandardVariableNames.Age, "60");

        CheckResult result = check.performCheck(vars);
        assertEquals(CheckResult.ResultCode.OK, result.getResultCode());
    }
}

