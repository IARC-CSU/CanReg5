/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2019  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.common;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class GregorianCalendarCanReg extends GregorianCalendar implements CalendarCanReg {

    private boolean unkownMonth = false;
    private boolean unknownDay = false;
    private boolean unknownYear = false;
    private String unknownMonthValue = "99";
    private String unknownDayValue = "99";
    private String unknownYearValue = "9999";

    /**
     * @return the unknownMonthValue
     */
    public String getUnknownMonthValue() {
        return unknownMonthValue;
    }

    /**
     * @param unknownMonthValue the unknownMonthValue to set
     */
    public void setUnknownMonthValue(String unknownMonthValue) {
        this.unknownMonthValue = unknownMonthValue;
    }

    /**
     * @return the unknownDayValue
     */
    public String getUnknownDayValue() {
        return unknownDayValue;
    }

    /**
     * @param unknownDayValue the unknownDayValue to set
     */
    public void setUnknownDayValue(String unknownDayValue) {
        this.unknownDayValue = unknownDayValue;
    }

    /**
     * @return the unknownYearValue
     */
    public String getUnknownYearValue() {
        return unknownYearValue;
    }

    /**
     * @param unknownYearValue the unknownYearValue to set
     */
    public void setUnknownYearValue(String unknownYearValue) {
        this.unknownYearValue = unknownYearValue;
    }

    /**
     * @param cal
     *
     */
    public GregorianCalendarCanReg(Calendar cal) {
        super();
        this.setTime(cal.getTime());
    }

    public GregorianCalendarCanReg() {
        super();
    }

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

    @Override
    public GregorianCalendarCanReg clone() {
        super.clone();
        GregorianCalendarCanReg newDate = new GregorianCalendarCanReg(this);
        newDate.setUnknownDay(unknownDay);
        newDate.setUnkownMonth(unkownMonth);
        newDate.setUnknownYear(unknownYear);
        newDate.setUnknownDayValue(unknownDayValue);
        newDate.setUnknownMonthValue(unknownMonthValue);
        newDate.setUnknownYearValue(unknownYearValue);
        return newDate;
    }
}
