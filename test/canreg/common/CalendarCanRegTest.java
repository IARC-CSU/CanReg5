package canreg.common;

import java.util.Calendar;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link GregorianCalendarCanReg} and {@link BuddhistCalendarCanReg}.
 */
public class CalendarCanRegTest {

    @Test
    public void testGregorianCalendarCanRegUnknownFlagsAndClone() {
        GregorianCalendarCanReg cal = new GregorianCalendarCanReg();
        cal.set(2020, Calendar.JUNE, 15);
        cal.setUnknownDay(true);
        cal.setUnkownMonth(false);
        cal.setUnknownYear(false);

        assertTrue(cal.isUnknownDay());
        assertFalse(cal.isUnknownMonth());
        assertFalse(cal.isUnknownYear());

        GregorianCalendarCanReg clone = cal.clone();
        assertEquals(2020, clone.get(Calendar.YEAR));
        assertTrue(clone.isUnknownDay());
        assertFalse(clone.isUnknownMonth());
    }

    @Test
    public void testGregorianCalendarCanRegFromExistingCalendar() {
        Calendar base = Calendar.getInstance();
        base.set(1995, Calendar.OCTOBER, 20);
        GregorianCalendarCanReg cal = new GregorianCalendarCanReg(base);

        assertEquals(1995, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
        assertFalse(cal.isUnknownDay());
        assertFalse(cal.isUnknownMonth());
        assertFalse(cal.isUnknownYear());
    }
}