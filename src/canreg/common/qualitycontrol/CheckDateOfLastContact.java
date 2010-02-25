package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class CheckDateOfLastContact extends CheckInterface {

    /**
     *
     */
    public Checker.CheckNames checkName = Checker.CheckNames.DateOfLastContact;
    /**
     *
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Lastcontact,
        Globals.StandardVariableNames.IncidenceDate
    };

    /**
     *
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     *
     * @param variables
     * @return
     */
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {

        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String dlcCode = null;
        String incidenceDateCode = null;
        String birthDateCode = null;

        GregorianCalendarCanReg incidenceDate = null;
        GregorianCalendarCanReg dlc = null;

        DatabaseVariablesListElement ageVariableListElement = variableListElementMap.get(Globals.StandardVariableNames.Age);

        Object unknownAgeCodeObject = ageVariableListElement.getUnknownCode();

        String unknownAgeCodeString = "";
        if (unknownAgeCodeObject == null) {
            for (int i = 0; i < ageVariableListElement.getVariableLength(); i++) {
                unknownAgeCodeString += "9";
            }
        } else {
            unknownAgeCodeString = unknownAgeCodeObject.toString();
        }
        int unknownAge = Integer.parseInt(unknownAgeCodeString);

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Lastcontact);
            dlcCode = variables.get(Globals.StandardVariableNames.Lastcontact).toString();
            dlc = DateHelper.parseDateStringToGregorianCalendarCanReg(dlcCode, Globals.DATE_FORMAT_STRING);
            result.addVariableInvolved(Globals.StandardVariableNames.IncidenceDate);
            incidenceDateCode = variables.get(Globals.StandardVariableNames.IncidenceDate).toString();
            incidenceDate = DateHelper.parseDateStringToGregorianCalendarCanReg(incidenceDateCode, Globals.DATE_FORMAT_STRING);
        } catch (ParseException ex) {
            Logger.getLogger(CheckDateOfLastContact.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CheckDateOfLastContact.class.getName()).log(Level.WARNING, "Dates: " + birthDateCode + ", " + incidenceDate, ex);
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        if (dlc != null && incidenceDate != null && !(incidenceDate.isUnknownYear() || dlc.isUnknownYear())) {
            int allowedDifference = 0;
            if (incidenceDate.isUnknownMonth() || incidenceDate.isUnknownDay()) {
                allowedDifference = 1;
            }
            long calculatedDifference = DateHelper.yearsBetween(incidenceDate, dlc);

            // System.out.println("Calculated age:"+ calculatedAge);
            if (calculatedDifference < 0) {
                result.setMessage("Date of last contact before incidence date.");
                result.setResultCode(CheckResult.ResultCode.Invalid);
                return result;
            }

            // check for todays date
            calculatedDifference = DateHelper.yearsBetween(dlc, Calendar.getInstance());
            if (calculatedDifference < 0) {
                result.setMessage("Date of last contact after todays date.");
                result.setResultCode(CheckResult.ResultCode.Invalid);
                return result;
            }
        } else {
            result.setMessage("Not done");
            result.setResultCode(CheckResult.ResultCode.NotDone);
            return result;
        }

        result.setMessage("");
        result.setResultCode(CheckResult.ResultCode.OK);
        return result;
    }

    /**
     *
     * @return
     */
    public CheckNames getCheckName() {
        return checkName;
    }
}

