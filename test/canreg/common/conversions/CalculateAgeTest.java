package canreg.common.conversions;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CalculateAge}.
 * All tests are hermetic – no I/O, no DB.
 */
public class CalculateAgeTest {

    private final CalculateAge calc = new CalculateAge();

    // ------------------------------------------------------------------
    // Valid date pairs
    // ------------------------------------------------------------------

    @Test
    public void testCalculateAge_ValidDates_ReturnsAge30() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19800101");
        vars.put(StandardVariableNames.IncidenceDate, "20100101");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, results[0].getResultCode());
        assertEquals(30, results[0].getValue());
    }

    @Test
    public void testCalculateAge_OneYearApart_ReturnsAge1() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "20050101");
        vars.put(StandardVariableNames.IncidenceDate, "20060101");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, results[0].getResultCode());
        assertEquals(1, results[0].getValue());
    }

    @Test
    public void testCalculateAge_PartialYear_ReturnsAge29() {
        // Born 1 July 1980, diagnosed 30 June 2010 (1 day before 30th birthday)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19800701");
        vars.put(StandardVariableNames.IncidenceDate, "20100630");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, results[0].getResultCode());
        assertEquals(29, results[0].getValue());
    }

    @Test
    public void testCalculateAge_SameDay_ReturnsAge0() {
        // Both birth and incidence are the same date (age 0)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "20000315");
        vars.put(StandardVariableNames.IncidenceDate, "20000315");
        ConversionResult[] results = calc.performConversion(vars);
        assertNotNull("Same-day case: result code must be set", results[0].getResultCode());
        assertEquals(ConversionResult.ResultCode.OK, results[0].getResultCode());
        assertEquals(0, results[0].getValue());
    }

    @Test
    public void testCalculateAge_IncidenceBeforeBirth_ReturnsNullResultCode() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "20100101");
        vars.put(StandardVariableNames.IncidenceDate, "19800101");
        ConversionResult[] results = calc.performConversion(vars);
        assertNull(results[0].getResultCode());
    }

    // ------------------------------------------------------------------
    // Missing variables
    // ------------------------------------------------------------------

    @Test
    public void testMissingBothVariables() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Missing, results[0].getResultCode());
    }

    @Test
    public void testMissingBirthDate() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.IncidenceDate, "20100101");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Missing, results[0].getResultCode());
    }

    @Test
    public void testMissingIncidenceDate() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19800101");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Missing, results[0].getResultCode());
    }

    // ------------------------------------------------------------------
    // Invalid dates – wrong-length strings cause parseDateString to return null,
    // then yearsBetween(null, ...) throws NPE → caught as Missing in the source.
    // ------------------------------------------------------------------

    @Test
    public void testInvalidBirthDateString_WrongLength_Missing() {
        // Wrong-length string → parseDateStringToGregorianCalendarCanReg returns null
        // → NullPointerException inside yearsBetween → caught as Missing
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "NOT_A_DATE");
        vars.put(StandardVariableNames.IncidenceDate, "20100101");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Missing, results[0].getResultCode());
    }

    @Test
    public void testInvalidIncidenceDateString_WrongLength_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate, "19800101");
        vars.put(StandardVariableNames.IncidenceDate, "BADDATE");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Missing, results[0].getResultCode());
    }

    // ------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------

    @Test
    public void testGetVariablesNeeded() {
        StandardVariableNames[] needed = calc.getVariablesNeeded();
        assertNotNull(needed);
        boolean hasBirth = false, hasIncidence = false;
        for (StandardVariableNames v : needed) {
            if (v == StandardVariableNames.BirthDate)
                hasBirth = true;
            if (v == StandardVariableNames.IncidenceDate)
                hasIncidence = true;
        }
        assertTrue(hasBirth);
        assertTrue(hasIncidence);
    }

    @Test
    public void testGetVariablesCreated() {
        StandardVariableNames[] created = calc.getVariablesCreated();
        assertNotNull(created);
        assertEquals(1, created.length);
        assertEquals(StandardVariableNames.Age, created[0]);
    }

    @Test
    public void testGetConversionName() {
        assertEquals(Converter.ConversionName.BirthIncidencetoAge, calc.getConversionName());
    }
}
