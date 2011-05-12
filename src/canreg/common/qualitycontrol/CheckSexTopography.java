/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
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
public class CheckSexTopography extends CheckInterface {

    /**
     * 
     */
    public static Checker.CheckNames checkName = Checker.CheckNames.SexTopography;
    
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Topography
    };

    /**
     * 
     */
    public static int maleCode = 1;
    /**
     * 
     */
    public static int femaleCode = 2;
    
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

        String sexCode = null;
        String topographyCode = null;

        int sexNumber = 0;
        int topographyNumber = 0;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Sex);
            sexCode = variables.get(Globals.StandardVariableNames.Sex).toString();
            sexNumber = Integer.parseInt(sexCode);
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

        int TopGroup = topographyNumber / 10;
        if (((sexNumber == maleCode) && (TopGroup >= 51 && TopGroup <= 58))
                || ((sexNumber == femaleCode) && (TopGroup >= 60 && TopGroup <= 63))) {
            result.setMessage(sexCode + ", " + topographyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
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
