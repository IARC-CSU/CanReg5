package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class CheckAgeIncidenceDateBirthDate extends CheckInterface {

    /**
     *
     */
    public Checker.CheckNames checkName = Checker.CheckNames.AgeIncidenceDateBirthDate;
    /**
     *
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.IncidenceDate,
        Globals.StandardVariableNames.BirthDate,};

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

        String ageCode = null;
        String incidenceDateCode = null;
        String birthDateCode = null;

        int ageNumber = 0;
        GregorianCalendarCanReg incidenceDate = null;
        GregorianCalendarCanReg birthDate = null;

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
            result.addVariableInvolved(Globals.StandardVariableNames.Age);
            ageCode = variables.get(Globals.StandardVariableNames.Age).toString();
            ageNumber = Integer.parseInt(ageCode);
            result.addVariableInvolved(Globals.StandardVariableNames.IncidenceDate);
            incidenceDateCode = variables.get(Globals.StandardVariableNames.IncidenceDate).toString();
            incidenceDate = DateHelper.parseDateStringToGregorianCalendarCanReg(incidenceDateCode, Globals.DATE_FORMAT_STRING);
            result.addVariableInvolved(Globals.StandardVariableNames.BirthDate);
            birthDateCode = variables.get(Globals.StandardVariableNames.BirthDate).toString();
            birthDate = DateHelper.parseDateStringToGregorianCalendarCanReg(birthDateCode, Globals.DATE_FORMAT_STRING);
        } catch (ParseException ex) {
            Logger.getLogger(CheckAgeIncidenceDateBirthDate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CheckAgeIncidenceDateBirthDate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        if (!(birthDate.isUnknownYear() || incidenceDate.isUnknownYear())) {
            int allowedDifference = 0;
            if (birthDate.isUnknownMonth() || birthDate.isUnknownDay()
                    || incidenceDate.isUnknownMonth() || incidenceDate.isUnknownDay()) {
                allowedDifference = 1;
            }
            long calculatedAge = DateHelper.yearsBetween(birthDate, incidenceDate);

            // System.out.println("Calculated age:"+ calculatedAge);

            if (calculatedAge < 0) {
                result.setMessage("Incidence date before birth date.");
                result.setResultCode(CheckResult.ResultCode.Invalid);
                return result;
            } else if (ageNumber == unknownAge) {
                result.setMessage("Unknown age stated, but both birth year and incidence year are known.");
                result.setResultCode(CheckResult.ResultCode.Query);
                return result;
            } else if (Math.abs((calculatedAge - ageNumber)) > allowedDifference) {
                result.setMessage("Age error. Stated: " + ageNumber + ", Calculated: " + calculatedAge);
                result.setResultCode(CheckResult.ResultCode.Invalid);
                return result;
            }
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

