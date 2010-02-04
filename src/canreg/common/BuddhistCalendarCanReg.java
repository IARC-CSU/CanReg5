/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common;

import sun.util.BuddhistCalendar;
/**
 *
 * @author ervikm
 */
public class BuddhistCalendarCanReg extends BuddhistCalendar implements CalendarCanReg  {
    private boolean unkownMonth = false;
    private boolean unknownDay = false;
    private boolean unknownYear = false;

    /**
     * @return the unkownMonth
     */
    public boolean isUnknownMonth() {
        return unkownMonth;
    }

    /**
     * @param unkownMonth the unkownMonth to set
     */
    public void setUnkownMonth(boolean unkownMonth) {
        this.unkownMonth = unkownMonth;
    }

    /**
     * @return the unknownDay
     */
    public boolean isUnknownDay() {
        return unknownDay;
    }

    /**
     * @param unknownDay the unknownDay to set
     */
    public void setUnknownDay(boolean unknownDay) {
        this.unknownDay = unknownDay;
    }

    /**
     * @return the unknownYear
     */
    public boolean isUnknownYear() {
        return unknownYear;
    }

    /**
     * @param unknownYear the unknownYear to set
     */
    public void setUnknownYear(boolean unknownYear) {
        this.unknownYear = unknownYear;
    }
}
