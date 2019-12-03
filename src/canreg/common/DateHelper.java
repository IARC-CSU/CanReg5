/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2019 International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.common;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author ervikm
 */
public class DateHelper {

    public static GregorianCalendarCanReg parseDateStringToGregorianCalendarCanReg(String dateString, String dateFormatString) throws ParseException, IllegalArgumentException {

        if (dateString.length() != dateFormatString.length()) {
            return null;
        }

        boolean unknownDay = false;
        boolean unknownMonth = false;
        boolean unknownYear = false;

        int day = 99;
        int month = 99;
        int year = 9999;

        String yearString = getYear(dateString, dateFormatString);
        String monthString = getMonth(dateString, dateFormatString);
        String dayString = getDay(dateString, dateFormatString);

        if (dayString.trim().length() > 0) {
            day = Integer.parseInt(dayString);
        } else {
            day = 99;
        }
        if (monthString.trim().length() > 0) {
            month = Integer.parseInt(monthString);
        } else {
            month = 99;

        }
        if (yearString.trim().length() > 0) {
            year = Integer.parseInt(yearString);

        } else {
            year = 9999;
        }

        GregorianCalendarCanReg calendar = new GregorianCalendarCanReg();

        calendar.clear();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day);

        boolean dateReadProperly = false;

        try {
            calendar.getTimeInMillis(); // This is just to trigger an error - if we have one latent...
            dateReadProperly = true;
        } catch (IllegalArgumentException iae) {
            if ("YEAR".equalsIgnoreCase(iae.getMessage())) {
                calendar.clear(Calendar.YEAR);
                calendar.setUnknownYearValue(yearString);
                // if this is what triggers the error set it to unknown
                unknownYear = true;
            } else if ("MONTH".equalsIgnoreCase(iae.getMessage()) || "MONTH: 1 -> 2".equalsIgnoreCase(iae.getMessage())) {
                calendar.clear(Calendar.MONTH);
                calendar.setUnknownMonthValue(monthString);
                // if this is what triggers the error set it to unknown
                unknownMonth = true;
            } else if ("DAY_OF_MONTH".equalsIgnoreCase(iae.getMessage())) {
                calendar.clear(Calendar.DAY_OF_MONTH);
                calendar.setUnknownDayValue(dayString);
                // if this is what triggers the error set it to unknown
                unknownDay = true;
            } else {
                throw iae;
            }
        }

        calendar.setUnknownDay(unknownDay);
        calendar.setUnkownMonth(unknownMonth);
        calendar.setUnknownYear(unknownYear);

        return calendar;
    }

    public static String parseGregorianCalendarCanRegToDateString(GregorianCalendarCanReg calendar, String dateFormatString) {
        String dateString = dateFormatString;
        DecimalFormat format = new DecimalFormat();
        // NumberFormatter nf = new NumberFormatter(format);
        format.setMinimumIntegerDigits(2);
        format.setGroupingUsed(false);
        try {
            if (calendar.isUnknownYear() || !calendar.isSet(Calendar.YEAR)) {
                dateString = setYear(dateString, dateFormatString, calendar.getUnknownYearValue() + "");
            } else {
                dateString = setYear(dateString, dateFormatString, format.format(calendar.get(Calendar.YEAR)));
            }
            if (calendar.isUnknownMonth() || !calendar.isSet(Calendar.MONTH)) {
                dateString = setMonth(dateString, dateFormatString, calendar.getUnknownMonthValue() + "");
            } else {
                dateString = setMonth(dateString, dateFormatString, format.format(calendar.get(Calendar.MONTH) + 1));
            }
            if (calendar.isUnknownDay() || !calendar.isSet(Calendar.DAY_OF_MONTH)) {
                dateString = setDay(dateString, dateFormatString, calendar.getUnknownDayValue() + "");
            } else {
                dateString = setDay(dateString, dateFormatString, format.format(calendar.get(Calendar.DAY_OF_MONTH)));
            }
        } catch (IllegalArgumentException iae) {
            System.out.println(iae + ": " + calendar);
        }
        return dateString;
    }

    public static String getYear(String dateString, String dateFormatString) {
        return getPartOfStringBasedOnFilter(dateString, dateFormatString, 'y');
    }

    public static String getMonth(String dateString, String dateFormatString) {
        return getPartOfStringBasedOnFilter(dateString, dateFormatString, 'm');
    }

    public static String getDay(String dateString, String dateFormatString) {
        return getPartOfStringBasedOnFilter(dateString, dateFormatString, 'd');
    }

    private static String getPartOfStringBasedOnFilter(String string, String filter, char lookFor) {
        String returnString = "";

        // Case insensitive
        filter = canreg.common.Tools.toLowerCaseStandardized(filter);

        for (int i = 0; i < string.length() && i < filter.length(); i++) {
            if (filter.charAt(i) == lookFor) {
                returnString += string.charAt(i);
            }
        }
        return returnString;
    }

    public static String setYear(String dateString, String dateFormatString, String replacementString) {
        return setPartOfStringBasedOnFilter(dateString, dateFormatString, 'y', replacementString);
    }

    public static String setMonth(String dateString, String dateFormatString, String replacementString) {
        return setPartOfStringBasedOnFilter(dateString, dateFormatString, 'm', replacementString);
    }

    public static String setDay(String dateString, String dateFormatString, String replacementString) {
        return setPartOfStringBasedOnFilter(dateString, dateFormatString, 'd', replacementString);
    }

    private static String setPartOfStringBasedOnFilter(String string, String filter, char lookFor, String replacementString) {
        // Case insensitive
        String newString = "";
        filter = canreg.common.Tools.toLowerCaseStandardized(filter);
        int placeInReplacementString = 0;
        for (int i = 0; i < string.length() && i < filter.length(); i++) {
            if (filter.charAt(filter.length() - 1 - i) == lookFor) {
                newString = replacementString.charAt(replacementString.length() - 1 - placeInReplacementString++) + newString;
                if (placeInReplacementString >= replacementString.length()) {
                    placeInReplacementString = 0;
                }
            } else {
                newString = string.charAt(string.length() - 1 - i) + newString;
            }
        }
        return newString;
    }

    public static long daysBetween(GregorianCalendarCanReg startDate, GregorianCalendarCanReg endDate) {
        int sign = 1;
        if (startDate.after(endDate)){
            GregorianCalendarCanReg tempDate = startDate;
            startDate = endDate;
            endDate = tempDate;
            sign = -1;
        }
        GregorianCalendarCanReg date = (GregorianCalendarCanReg) startDate.clone();
        long daysBetween = 0;
        while (date.compareTo(endDate)<=0) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return sign * (daysBetween - 1);
    }

    public static long yearsBetween(GregorianCalendarCanReg startDate, GregorianCalendarCanReg endDate) {
        startDate = correctUnknown(startDate);
        endDate = correctUnknown(endDate);
        
        int sign = 1;
        if (startDate.after(endDate)){
            GregorianCalendarCanReg tempDate = startDate;
            startDate = endDate;
            endDate = tempDate;
            sign = -1;
        }
        
        GregorianCalendarCanReg date = startDate.clone();
        long yearsBetween = 0;
        while (date.compareTo(endDate)<=0) {
            date.add(Calendar.YEAR, 1);
            yearsBetween++;
        }
        return (yearsBetween -1) * sign;
    }

    public static GregorianCalendarCanReg correctUnknown(GregorianCalendarCanReg date) {
        GregorianCalendarCanReg newDate = date.clone();
        // for calculations "correct unknown"
        if (date.isUnknownMonth()) {
            // Set month to July
            date.set(Calendar.MONTH, 7 - 1);
            date.setUnkownMonth(false);
            // And day to first
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.setUnknownDay(false);
        } else if (date.isUnknownDay()) {
            // Set day to mid-month
            date.set(Calendar.DAY_OF_MONTH, 15);
            date.setUnknownDay(false);
        }
        return date;
    }

    public static Calendar parseTimestamp(String timestamp, String dateFormat, Locale locale) throws ParseException {
        /*
         ** we specify Locale.US since months are in english
         */
        if (locale == null) {
            locale = Locale.getDefault();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, locale);
        Date d = sdf.parse(timestamp);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal;
    }
}
