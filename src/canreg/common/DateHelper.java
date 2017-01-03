/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015 International Agency for Research on Cancer
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.common;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.ULocale;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            dayString = "99";
        }
        if (monthString.trim().length() > 0) {
            month = Integer.parseInt(monthString);
        } else {
            monthString = "99";

        }
        if (yearString.trim().length() > 0) {
            year = Integer.parseInt(yearString);

        } else {
            yearString = "9999";
        }

        GregorianCalendarCanReg calendar = new GregorianCalendarCanReg();

        calendar.clear();
        //<ictl.co>
        //calendar.setLenient(false);

        ULocale locale = new ULocale("fa_IR");
        if (DateHelper.isGregorianYear(year)) {
            locale = ULocale.ENGLISH;
        }
        calendar.setTime(
                new SimpleDateFormat("yyyyMMdd",
                        locale)
                        .parse(String.format("%s%s%s",
                                year,
                                (month) < 10 ? "0" + (month) : month,
                                day < 10 ? "0" + day : day + "")));
//        calendar.set(year, month - 1, day);
        //</ictl.co>

        boolean dateReadProperly = false;

        while (!dateReadProperly) {
            try {
                calendar.getTimeInMillis(); // This is just to trigger an error - if we have one latent...
                dateReadProperly = true;
            } catch (IllegalArgumentException iae) {
                //<ictl.co>                if ("YEAR".equalsIgnoreCase(iae.getMessage())) {</ictl.co>
                if (iae.getMessage().toUpperCase().startsWith("YEAR")) {
                    calendar.clear(Calendar.YEAR);
                    if ("9999".equals(yearString) || "0000".equals(yearString)) {
                        unknownYear = true;
                    } else {
                        throw iae;
                    }
                    //<ictl.co>                } else if ("MONTH".equalsIgnoreCase(iae.getMessage())) {</ictl.co>
                } else if (iae.getMessage().toUpperCase().startsWith("MONTH")) {
                    calendar.clear(Calendar.MONTH);
                    if ("99".equals(monthString) || "00".equals(monthString)) {
                        unknownMonth = true;
                    } else {
                        throw iae;
                    }
                    //<ictl.co>                } else if ("DAY_OF_MONTH".equalsIgnoreCase(iae.getMessage())) {</ictl.co>
                } else if (iae.getMessage().toUpperCase().startsWith("DAY_OF_MONTH")) {
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

        //<ictl.co>
        //return calendar;
        return (LocalizationHelper.isRtlLanguageActive() ? convertGregorianCalendarCanRegToLocaleCalendar(calendar) : calendar);
        //</ictl.co>
    }

    public static String parseGregorianCalendarCanRegToDateString(GregorianCalendarCanReg calendar, String dateFormatString) {
        String dateString = dateFormatString;
        DecimalFormat format = new DecimalFormat();
        // NumberFormatter nf = new NumberFormatter(format);
        format.setMinimumIntegerDigits(2);
        format.setGroupingUsed(false);
        try {
            if (calendar.isUnknownYear() || !calendar.isSet(Calendar.YEAR)) {
                dateString = setYear(dateString, dateFormatString, "9999");
            } else {
                dateString = setYear(dateString, dateFormatString, format.format(calendar.get(Calendar.YEAR)));
            }
            if (calendar.isUnknownMonth() || !calendar.isSet(Calendar.MONTH)) {
                dateString = setMonth(dateString, dateFormatString, "99");
            } else {
                dateString = setMonth(dateString, dateFormatString, format.format(calendar.get(Calendar.MONTH) + 1));
            }
            if (calendar.isUnknownDay() || !calendar.isSet(Calendar.DAY_OF_MONTH)) {
                dateString = setDay(dateString, dateFormatString, "99");
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
        if (startDate.after(endDate)) {
            GregorianCalendarCanReg tempDate = startDate;
            startDate = endDate;
            endDate = tempDate;
            sign = -1;
        }
        GregorianCalendarCanReg date = (GregorianCalendarCanReg) startDate.clone();
        long daysBetween = 0;
        while (date.compareTo(endDate) <= 0) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return sign * (daysBetween - 1);
    }

    public static long yearsBetween(GregorianCalendarCanReg startDate, GregorianCalendarCanReg endDate) {
        startDate = correctUnknown(startDate);
        endDate = correctUnknown(endDate);

        int sign = 1;
        if (startDate.after(endDate)) {
            GregorianCalendarCanReg tempDate = startDate;
            startDate = endDate;
            endDate = tempDate;
            sign = -1;
        }

        GregorianCalendarCanReg date = startDate.clone();
        long yearsBetween = 0;
        while (date.compareTo(endDate) <= 0) {
            date.add(Calendar.YEAR, 1);
            yearsBetween++;
        }
        return (yearsBetween - 1) * sign;
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
    //<ictl.co>

    public static GregorianCalendar convertLocalCalendarToGregorianCalendar(Calendar localeCalendar) {
        GregorianCalendar gc = new GregorianCalendar(Locale.ENGLISH);
        gc.setTime(localeCalendar.getTime());
        return gc;
    }

    public static GregorianCalendarCanReg convertGregorianCalendarToGregorianCalendarCanReg(GregorianCalendar localeCalendar, Boolean isUnknownYear, Boolean isUnknownMonth, Boolean isUnknownDay) {
        GregorianCalendar gc = convertLocalCalendarToGregorianCalendar(localeCalendar);
        GregorianCalendarCanReg cal = new GregorianCalendarCanReg();
        cal.setTime(gc.getTime());
        cal.setUnknownYear(isUnknownYear);
        cal.setUnkownMonth(isUnknownMonth);
        cal.setUnknownDay(isUnknownDay);
        return cal;
    }

    public static Calendar createJalaliCalendar(int year, int month, int dayOfMonth) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", new ULocale("fa_IR"));
        Calendar localeCalendar = Calendar.getInstance(new ULocale("fa_IR"));
        try {
            month += 1;
            String smonth = (month < 10 ? "0" + month : "" + month);
            String sdayOfMonth = (dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth);
            localeCalendar.setTime(format.parse(String.format("%s%s%s", year, smonth, sdayOfMonth)));
        } catch (ParseException ignore) {
        }
/*
        localeCalendar.set(Calendar.YEAR, year);
        localeCalendar.set(Calendar.MONTH, month);
        localeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
*/
        return localeCalendar;
    }

    public static Calendar createJalaliCalendar(Date date) {
        Calendar localeCalendar = Calendar.getInstance(new ULocale("fa_IR"));
        localeCalendar.setTime(date);
        return localeCalendar;
    }

    public static Calendar createJalaliCalendar(String value, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, new ULocale("fa_IR"));
        Calendar localeCalendar = Calendar.getInstance(new ULocale("fa_IR"));
        try {
            localeCalendar.setTime(format.parse(value));
        } catch (ParseException ignore) {
        }
        return localeCalendar;
    }

    public static GregorianCalendarCanReg convertGregorianCalendarCanRegToLocaleCalendar(GregorianCalendarCanReg calendar) {
        Calendar localeCalendar = null;
        if (LocalizationHelper.isPersianLocale() && DateHelper.analyseContentForPersianDateValue(calendar)) {
            localeCalendar = createJalaliCalendar(calendar.getTime());
        }
        if (localeCalendar != null) {
            GregorianCalendar gc = convertLocalCalendarToGregorianCalendar(localeCalendar);
            return convertGregorianCalendarToGregorianCalendarCanReg(gc, calendar.isUnknownYear(), calendar.isUnknownMonth(), calendar.isUnknownDay());
        }
        return calendar;
    }

    public static String parseGregorianCalendarCanRegToDateStringLocale(GregorianCalendarCanReg calendarReg, String dateFormatString) {
        if (LocalizationHelper.isRtlLanguageActive()) {
            Calendar _calendar = Calendar.getInstance();
            _calendar.setTime(calendarReg.getTime());
            String dateString = dateFormatString;
            DecimalFormat format = new DecimalFormat();
            format.setMinimumIntegerDigits(2);
            format.setGroupingUsed(false);
            try {
                dateString = setYear(dateString, dateFormatString, format.format(_calendar.get(Calendar.YEAR)));
                dateString = setMonth(dateString, dateFormatString, format.format(_calendar.get(Calendar.MONTH) + 1));
                dateString = setDay(dateString, dateFormatString, format.format(_calendar.get(Calendar.DAY_OF_MONTH)));
            } catch (IllegalArgumentException iae) {
                System.out.println(iae + ": " + _calendar);
            }
            return dateString;
        } else {
            return parseGregorianCalendarCanRegToDateString(calendarReg, dateFormatString);
        }
    }

    public static String localeDateStringToGregorianDateString(String value, String dateFormatString) {
        try {
            GregorianCalendarCanReg gc = parseDateStringToGregorianCalendarCanReg(value, dateFormatString);
            return parseGregorianCalendarCanRegToDateString(gc, dateFormatString);
        } catch (ParseException ex) {
            Logger.getLogger(DateHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    public static String gregorianDateStringToLocaleDateString(String value, String dateFormatString) {
        try {
            GregorianCalendarCanReg calendarCanReg = DateHelper.parseDateStringToGregorianCalendarCanReg(value, dateFormatString);
            return DateHelper.parseGregorianCalendarCanRegToDateStringLocale(calendarCanReg, dateFormatString);
        } catch (ParseException ex) {
            Logger.getLogger(DateHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    public static Boolean analyseContentForPersianDateValue(GregorianCalendarCanReg calendar) {
        if (!calendar.isUnknownYear()) {
            return calendar.get(Calendar.YEAR) < 1900;
        } else if ("fa".equals(Locale.getDefault().getLanguage())) {
            return true;
        }
        return false;
    }

    public static Boolean analyseContentForYearValue(String value) {
        //1st length equal to 4
        try {
            if (value != null && value.length() == 4) {
                //2nd year section gt 1200 for shamsi and ghamari gt 1800 for miladi
                String year = value;
                int iyear = Integer.parseInt(year);
                if (/*iyear == 0 || */iyear > 1200 || iyear > 1800) {
                    return true;
                }
            }
        } catch (NumberFormatException nfe) {

        }
        return false;
    }

    public static Boolean analyseContentForDateValue(String value) {
        //1st length equal to 8
        try {
            if (value != null && value.length() == 8) {
                //2nd year section gt 1200 for shamsi and ghamari gt 1800 for miladi
                String year = value.substring(0, 4);
                int iyear = Integer.parseInt(year);
                if (/*iyear == 0 || */iyear > 1200 || iyear > 1800) {
                    //3rd analyse month section between 1 and 12
                    String month = value.substring(4, 6);
                    int imonth = Integer.parseInt(month);
                    if (/*imonth == 0 ||*/ imonth <= 12 && imonth >= 1)
                        return true;
                }
            }
        } catch (NumberFormatException nfe) {

        }
        return false;
    }

    public static String parseGregorianCalendarCanRegToDateStringForFilter(GregorianCalendarCanReg calendar, String dateFormatString) {
        String dateString = dateFormatString;
        DecimalFormat format = new DecimalFormat();
        // NumberFormatter nf = new NumberFormatter(format);
        format.setMinimumIntegerDigits(2);
        format.setGroupingUsed(false);
        try {
            if (calendar.isUnknownYear() || !calendar.isSet(Calendar.YEAR)) {
                dateString = setYear(dateString, dateFormatString, "%");
                dateString = dateString.replace("%%%%", "%");
            } else {
                dateString = setYear(dateString, dateFormatString, format.format(calendar.get(Calendar.YEAR)));
            }
            if (calendar.isUnknownMonth() || !calendar.isSet(Calendar.MONTH)) {
                dateString = setMonth(dateString, dateFormatString, "%");
                dateString = dateString.replace("%%", "%");
            } else {
/*
                if ("fa".equals(Locale.getDefault().getLanguage())) {
                    dateString = setMonth(dateString, dateFormatString, format.format(persianMonthToGregorianMonth[calendar.get(Calendar.MONTH)] + 1));
                } else {
*/
                dateString = setMonth(dateString, dateFormatString, format.format(calendar.get(Calendar.MONTH) + 1));
//                }
            }
            if (calendar.isUnknownDay() || !calendar.isSet(Calendar.DAY_OF_MONTH)) {
                dateString = setDay(dateString, dateFormatString, "%");
                dateString = dateString.replace("%%", "%");
            } else {
                dateString = setDay(dateString, dateFormatString, format.format(calendar.get(Calendar.DAY_OF_MONTH)));
            }
        } catch (IllegalArgumentException iae) {
            System.out.println(iae + ": " + calendar);
        }
        return dateString;
    }

    public static int convertGregorianYearToPersianYear(int year) {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.set(Calendar.YEAR, year);
        gcal.set(Calendar.MONTH, 1);
        gcal.set(Calendar.DAY_OF_MONTH, 1);
        Calendar pcal = Calendar.getInstance(new ULocale("fa_IR@calendar=persian"));
        pcal.setTime(gcal.getTime());
        return pcal.get(Calendar.YEAR);

    }

    public static int convertJalaliYearToGregoranYear(int year) {
        Calendar localeCalendar = createJalaliCalendar(year, 0, 0);
        GregorianCalendar cal = convertLocalCalendarToGregorianCalendar(localeCalendar);
        return cal.get(Calendar.YEAR);
    }

    private static Set<String> skipNames = new HashSet<String>(Arrays.asList("PATIENTRECORDID", "REGNO", "PID"));

    public static String analyseJTableColumnValue(String value, String name) {
        String _value = value;
        if (!skipNames.contains(name)) {
            if (DateHelper.analyseContentForDateValue(value)) {
                _value = DateHelper.gregorianDateStringToLocaleDateString(value, Globals.DATE_FORMAT_STRING);
            }
            if (DateHelper.analyseContentForYearValue(value)) {
                if (LocalizationHelper.isPersianLocale()) {
                    _value = DateHelper.convertGregorianYearToPersianYear(Integer.parseInt(value)) + "";
                }
            }

        }
        return _value;
    }

    public static boolean isGregorianYear(int year) {
        return year > 1900;
    }

    public static boolean isJalaliYear(int year) {
        return (year > (1394 - 140)) && (year < 1800);
    }

    protected static final int floorDivide(int numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[0] = numerator % denominator;
            return numerator / denominator;
        } else {
            int quotient = (numerator + 1) / denominator - 1;
            remainder[0] = numerator - quotient * denominator;
            return quotient;
        }
    }

    public static final boolean isLeapYear(int year) {
        int[] remainder = new int[1];
        floorDivide(25 * year + 11, 33, remainder);
        return remainder[0] < 8;
    }

    public static String[] getGregorianYearsInPersianYear(String year) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", ULocale.ENGLISH);
        String y1, y2;
        Calendar calendar = Calendar.getInstance(new ULocale("fa_IR@calendar=persian"));
        calendar.set(Integer.parseInt(year), 1, 1);
        y1 = simpleDateFormat.format(calendar);
        calendar.set(Integer.parseInt(year), 12, 29);
        y2 = simpleDateFormat.format(calendar);
        return new String[]{y1, y2};
    }
    //</ictl.co>
}
