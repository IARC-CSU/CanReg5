//<ictl.co>
package canreg.common;

import java.text.ParseException;

/**
 * Created by amin on 8/10/2015.
 */
public class FilterHelper {

    public static String analyzeFilterForDateAndReplace(String filter) {
        String newFilter = filter;
        int parsPositon = filter.indexOf("'");
        while (true) {
            if (parsPositon != -1) {
                int nextParsPosition = filter.indexOf("'", parsPositon + 1);
                if (nextParsPosition != -1) {
                    String value = filter.substring(parsPositon + 1, nextParsPosition);
                    if (DateHelper.analyseContentForDateValue(value)) {
                        GregorianCalendarCanReg calendarCanReg = null;
                        try {
                            calendarCanReg = DateHelper.parseDateStringToGregorianCalendarCanReg(value, Globals.DATE_FORMAT_STRING);
                            String newValue = DateHelper.parseGregorianCalendarCanRegToDateStringForFilter(calendarCanReg, Globals.DATE_FORMAT_STRING);
                            newFilter = newFilter.replace(value, newValue);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    break;
                }
                parsPositon = filter.indexOf("'", nextParsPosition + 1);
            } else {
                break;
            }
        }
        return newFilter;
    }
}

//</ictl.co>