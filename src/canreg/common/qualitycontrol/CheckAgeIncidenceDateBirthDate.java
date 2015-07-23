/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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
    @Override
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     *
     * @param variables
     * @return
     */
    @Override
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
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CheckAgeIncidenceDateBirthDate.class.getName()).log(Level.WARNING, "Dates: " + birthDateCode + ", " + incidenceDate, ex);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        if (birthDate != null && incidenceDate != null && !(birthDate.isUnknownYear() || incidenceDate.isUnknownYear())) {
            int allowedDifference = 0;
            if (birthDate.isUnknownMonth() || birthDate.isUnknownDay()
                    || incidenceDate.isUnknownMonth() || incidenceDate.isUnknownDay()) {
                allowedDifference = 1;
            }
            long calculatedAge = DateHelper.yearsBetween(birthDate, incidenceDate);

            // System.out.println("Calculated age:"+ calculatedAge);

            if (calculatedAge + allowedDifference < 0) {
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
    @Override
    public CheckNames getCheckName() {
        return checkName;
    }
}

