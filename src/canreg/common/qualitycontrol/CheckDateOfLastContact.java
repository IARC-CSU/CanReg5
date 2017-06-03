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
 * @author Andy Cooke
 */

package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import canreg.common.qualitycontrol.Checker.CheckNames;
import com.ibm.icu.util.Calendar;

import java.text.ParseException;
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
            long calculatedDifference = DateHelper.daysBetween(incidenceDate, dlc);

            System.out.println("Calculated differnce:"+ calculatedDifference);
                        
            if (calculatedDifference < 0) {
                result.setMessage("Date of last contact before incidence date.");
                result.setResultCode(CheckResult.ResultCode.Invalid);
                return result;
            }

            // check for todays date
            GregorianCalendarCanReg today = new GregorianCalendarCanReg(Calendar.getInstance());
            calculatedDifference = DateHelper.daysBetween(dlc, today);
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

