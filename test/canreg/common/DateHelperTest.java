/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common;

import java.util.Calendar;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for DateHelper.
 * Covers parseGregorianDateToCalendar, days/years between, correctUnknown, and parseTimestamp.
 * 
 * @author ervikm
 */
public class DateHelperTest {

    public DateHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private GregorianCalendarCanReg createDate(int year, int month, int day) {
        GregorianCalendarCanReg cal = new GregorianCalendarCanReg();
        cal.clear();
        cal.set(year, month, day);
        return cal;
    }

    /**
     * Test of parseGregorianDateToCalendar method, of class DateHelper.
     */
    @Test
    public void testParseGregorianDateToCalendar() throws Exception {
        System.out.println("parseGregorianDateToCalendar");
        String dateString = "99991201";
        String dateFormatString = "yyyyMMdd";
        String expResult = dateString;
        GregorianCalendarCanReg tempResult = DateHelper.parseDateStringToGregorianCalendarCanReg(dateString, dateFormatString);
        String result = DateHelper.parseGregorianCalendarCanRegToDateString(tempResult, dateFormatString);
        assertEquals(expResult, result);
    }

    /**
     * Test of daysBetween method, of class DateHelper.
     */
    @Test
    public void testDaysBetween() {
        System.out.println("daysBetween");
        GregorianCalendarCanReg start = createDate(2023, Calendar.JANUARY, 1);
        GregorianCalendarCanReg end = createDate(2023, Calendar.JANUARY, 10);
        
        assertEquals(9, DateHelper.daysBetween(start, end));
        assertEquals(-9, DateHelper.daysBetween(end, start));
        
        // Same day
        assertEquals(0, DateHelper.daysBetween(start, start));
        
        // Leap year check (2024 is a leap year; Feb has 29 days)
        GregorianCalendarCanReg leapStart = createDate(2024, Calendar.FEBRUARY, 28);
        GregorianCalendarCanReg leapEnd = createDate(2024, Calendar.MARCH, 1);
        assertEquals(2, DateHelper.daysBetween(leapStart, leapEnd));
    }

    /**
     * Test of yearsBetween method, of class DateHelper.
     */
    @Test
    public void testYearsBetween() {
        System.out.println("yearsBetween");
        GregorianCalendarCanReg start = createDate(2020, Calendar.MAY, 15);
        GregorianCalendarCanReg end = createDate(2025, Calendar.MAY, 15);
        
        assertEquals(5, DateHelper.yearsBetween(start, end));
        assertEquals(-5, DateHelper.yearsBetween(end, start));
        
        // Less than a full year should result in 0
        GregorianCalendarCanReg endAlmost = createDate(2021, Calendar.MAY, 14);
        assertEquals(0, DateHelper.yearsBetween(start, endAlmost));
    }

    /**
     * Test of correctUnknown method, of class DateHelper.
     */
    @Test
    public void testCorrectUnknown() {
        System.out.println("correctUnknown");
        
        // Test unknown month and day
        GregorianCalendarCanReg unknownMonthDate = new GregorianCalendarCanReg();
        unknownMonthDate.setUnknownYear(false);
        unknownMonthDate.setUnkownMonth(true);
        unknownMonthDate.setUnknownDay(true);
        unknownMonthDate.set(Calendar.YEAR, 2020);
        
        // After correctUnknown, month should be July (index 6) and day should be 1
        GregorianCalendarCanReg corrected = DateHelper.correctUnknown(unknownMonthDate);
        assertEquals(2020, corrected.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, corrected.get(Calendar.MONTH));
        assertEquals(1, corrected.get(Calendar.DAY_OF_MONTH));
        
        // Test unknown day but known month
        GregorianCalendarCanReg unknownDayDate = new GregorianCalendarCanReg();
        unknownDayDate.setUnknownYear(false);
        unknownDayDate.setUnkownMonth(false);
        unknownDayDate.setUnknownDay(true);
        unknownDayDate.set(2020, Calendar.MARCH, 1);
        
        // After correctUnknown, day should be 15
        GregorianCalendarCanReg correctedDay = DateHelper.correctUnknown(unknownDayDate);
        assertEquals(2020, correctedDay.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, correctedDay.get(Calendar.MONTH));
        assertEquals(15, correctedDay.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Test of parseTimestamp method, of class DateHelper.
     */
    @Test
    public void testParseTimestamp() throws Exception {
        System.out.println("parseTimestamp");
        String timestamp = "2026-05-26 14:30:00";
        String format = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = DateHelper.parseTimestamp(timestamp, format, Locale.US);
        
        assertNotNull(cal);
        assertEquals(2026, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(26, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }
}