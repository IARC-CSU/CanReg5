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

import canreg.common.Globals;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class CheckAgeTopography extends CheckInterface {

    /**
     * 
     */
    public Checker.CheckNames checkName = Checker.CheckNames.AgeTopography;
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.Topography,
    };

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
        String topographyCode = null;

        int ageNumber = 0;
        int topographyNumber = 0;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Age);
            ageCode = variables.get(Globals.StandardVariableNames.Age).toString();
            ageNumber = Integer.parseInt(ageCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Topography);
            topographyCode = variables.get(Globals.StandardVariableNames.Topography).toString();
            topographyNumber = Integer.parseInt(topographyCode);
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        int topographyGroup = topographyNumber / 10;

        if (ageNumber < 5 && (topographyGroup == 53 || topographyGroup == 61)) {
            result.setMessage(ageCode + ", " + topographyCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
            return result;
        }

        if (ageNumber < 20 &&
                (topographyGroup == 15 || topographyGroup == 19 || topographyGroup == 20 ||
                topographyGroup == 21 || topographyGroup == 23 || topographyGroup == 24 ||
                topographyNumber == 384 || topographyGroup == 50 ||
                topographyGroup == 53 || topographyGroup == 54 || topographyGroup == 55)) {
            result.setMessage(ageCode + ", " + topographyCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
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
    @Override
    public CheckNames getCheckName() {
        return checkName;
    }
}
