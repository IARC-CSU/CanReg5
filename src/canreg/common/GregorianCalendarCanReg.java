package canreg.common;

import java.util.GregorianCalendar;

/**
 *
 * @author ervikm
 */
public class GregorianCalendarCanReg extends GregorianCalendar implements CalendarCanReg  {
    private boolean unkownMonth = false;
    private boolean unknownDay = false;
    private boolean unknownYear = false;

    /**
     * @return the unkownMonth
     */
    @Override
    public boolean isUnknownMonth() {
        return unkownMonth;
    }

    /**
     * @param unkownMonth the unkownMonth to set
     */
    @Override
    public void setUnkownMonth(boolean unkownMonth) {
        this.unkownMonth = unkownMonth;
    }

    /**
     * @return the unknownDay
     */
    @Override
    public boolean isUnknownDay() {
        return unknownDay;
    }

    /**
     * @param unknownDay the unknownDay to set
     */
    @Override
    public void setUnknownDay(boolean unknownDay) {
        this.unknownDay = unknownDay;
    }

    /**
     * @return the unknownYear
     */
    @Override
    public boolean isUnknownYear() {
        return unknownYear;
    }

    /**
     * @param unknownYear the unknownYear to set
     */
    @Override
    public void setUnknownYear(boolean unknownYear) {
        this.unknownYear = unknownYear;
    }
}
