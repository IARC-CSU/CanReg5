/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;

/**
 *
 * @author ervikm
 */
public class DateHelper {

    public static GregorianCalendarCanReg parseDateStringToGregorianCalendarCanReg(String dateString, String dateFormatString) throws ParseException, IllegalArgumentException {
        boolean unknownDay = false;
        boolean unknownMonth = false;
        boolean unknownYear = false;

        String yearString = getYear(dateString, dateFormatString);
        String monthString = getMonth(dateString, dateFormatString);
        String dayString = getDay(dateString, dateFormatString);

        int day = Integer.parseInt(dayString);
        int month = Integer.parseInt(monthString);
        int year = Integer.parseInt(yearString);

        GregorianCalendarCanReg calendar = new GregorianCalendarCanReg();

        calendar.clear();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day);

        boolean dateReadProperly = false;

        while (!dateReadProperly) {
            try {
                calendar.getTimeInMillis(); // This is just to trigger an error - if we have one latent...
                dateReadProperly = true;
            } catch (IllegalArgumentException iae) {
                if ("YEAR".equalsIgnoreCase(iae.getMessage())) {
                    calendar.clear(Calendar.YEAR);
                    if ("9999".equals(yearString) || "0000".equals(yearString)) {
                        unknownYear = true;
                    } else {
                        throw iae;
                    }
                } else if ("MONTH".equalsIgnoreCase(iae.getMessage())) {
                    calendar.clear(Calendar.MONTH);
                    if ("99".equals(monthString) || "00".equals(monthString)) {
                        unknownMonth = true;
                    } else {
                        throw iae;
                    }
                } else if ("DAY_OF_MONTH".equalsIgnoreCase(iae.getMessage())) {
                    calendar.clear(Calendar.DAY_OF_MONTH);
                    if ("99".equals(dayString) || "00".equals(dayString)) {
                        unknownDay = true;
                    } else {
                        throw iae;
                    }
                }
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
        if (calendar.isUnknownYear()) {
            dateString = setYear(dateString, dateFormatString, "9999");
        } else {
            dateString = setYear(dateString, dateFormatString, format.format(calendar.get(Calendar.YEAR)));
        }
        if (calendar.isUnknownMonth()) {
            dateString = setMonth(dateString, dateFormatString, "99");
        } else {
            dateString = setMonth(dateString, dateFormatString, format.format(calendar.get(Calendar.MONTH) + 1));
        }
        if (calendar.isUnknownDay()) {
            dateString = setDay(dateString, dateFormatString, "99");
        } else {
            dateString = setDay(dateString, dateFormatString, format.format(calendar.get(Calendar.DAY_OF_MONTH)));
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
        filter = filter.toLowerCase();

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
        filter = filter.toLowerCase();
        int placeInReplacementString = 0;
        for (int i = 0; i < string.length() && i < filter.length(); i++) {
            if (filter.charAt(i) == lookFor) {
                newString += replacementString.charAt(placeInReplacementString++);
                if (placeInReplacementString >= replacementString.length()) {
                    placeInReplacementString = 0;
                }
            } else {
                newString += string.charAt(i);
            }
        }
        return newString;
    }

    public static long daysBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        long daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween-1;
    }

    public static long yearsBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        long yearsBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.YEAR, 1);
            yearsBetween++;
        }
        return yearsBetween-1;
    }
}
