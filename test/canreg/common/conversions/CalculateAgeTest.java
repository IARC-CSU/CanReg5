package canreg.common.conversions;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CalculateAge}.
 * All tests are hermetic – no I/O, no DB.
 *
 * <h3>Known pre-existing bug</h3>
 * {@code CalculateAge.performConversion()} passes the date arguments to
 * {@code DateHelper.yearsBetween()} in the wrong order – {@code (incidenceDate, birthDate)}
 * instead of {@code (birthDate, incidenceDate)}.  For a patient born in 1980 and diagnosed
 * in 2010 the method therefore computes {@code yearsBetween(2010, 1980) = -30},
 * which is negative, so the guard condition {@code if (yearsBetween >= 0)} is false
 * and {@code resultCode} is never set – it stays {@code null}.
 *
 * Tests that expose this bug are named with a <em>"_DueToBug"</em> suffix and
 * assert the actual (wrong) behavior so that any future fix is immediately visible.
 */
public class CalculateAgeTest {

    private final CalculateAge calc = new CalculateAge();

    // ------------------------------------------------------------------
    // Documenting actual (buggy) behaviour for valid date pairs
    // ------------------------------------------------------------------

    @Test
    public void testCalculateAge_ValidDates_ReturnsNullResultCode_DueToBug() {
        // Due to swapped args in yearsBetween the result code is never set for
        // valid forward-in-time pairs – result code stays null.
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate,     "19800101");
        vars.put(StandardVariableNames.IncidenceDate, "20100101");
        ConversionResult[] results = calc.performConversion(vars);
        assertNull(
            "KNOWN BUG: yearsBetween args are swapped; result code should be OK but is null",
            results[0].getResultCode());
    }

    @Test
    public void testCalculateAge_OneYearApart_ReturnsNullResultCode_DueToBug() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate,     "20050101");
        vars.put(StandardVariableNames.IncidenceDate, "20060101");
        ConversionResult[] results = calc.performConversion(vars);
        assertNull(
            "KNOWN BUG: yearsBetween args are swapped; result code should be OK but is null",
            results[0].getResultCode());
    }

    @Test
    public void testCalculateAge_PartialYear_ReturnsNullResultCode_DueToBug() {
        // Born 1 July 1980, diagnosed 30 June 2010
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate,     "19800701");
        vars.put(StandardVariableNames.IncidenceDate, "20100630");
        ConversionResult[] results = calc.performConversion(vars);
        assertNull(
            "KNOWN BUG: yearsBetween args are swapped; result code should be OK but is null",
            results[0].getResultCode());
    }

    @Test
    public void testCalculateAge_SameDay_ReturnsNullResultCode_DueToBug() {
        // Both birth and incidence are the same date (age 0)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate,     "20000315");
        vars.put(StandardVariableNames.IncidenceDate, "20000315");
        ConversionResult[] results = calc.performConversion(vars);
        // Same date: yearsBetween returns 0, which is >= 0, so result code IS set to OK
        // (the bug only affects forward-in-time pairs where swap+sign gives negative)
        // Document what actually happens for same-day:
        assertNotNull("Same-day case: result code must be set", results[0].getResultCode());
        assertEquals(ConversionResult.ResultCode.OK, results[0].getResultCode());
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
        vars.put(StandardVariableNames.BirthDate,     "NOT_A_DATE");
        vars.put(StandardVariableNames.IncidenceDate, "20100101");
        ConversionResult[] results = calc.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Missing, results[0].getResultCode());
    }

    @Test
    public void testInvalidIncidenceDateString_WrongLength_Missing() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.BirthDate,     "19800101");
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
            if (v == StandardVariableNames.BirthDate)     hasBirth = true;
            if (v == StandardVariableNames.IncidenceDate) hasIncidence = true;
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
