/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
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

}