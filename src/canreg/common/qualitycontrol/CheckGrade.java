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
public class CheckGrade extends CheckInterface {
    
    /**
     * 
     */
    public Checker.CheckNames checkName = Checker.CheckNames.Grade;

    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames [] {
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Grade
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
        
        int behaviourNumber = 0;
        String gradeCode = null;
        String morphologyCode = null;
        String behaviourCode = null;
        int gradeNumber = 0;
        int morphologyNumber = 0;
        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Grade);
            gradeCode = variables.get(Globals.StandardVariableNames.Grade).toString();
            gradeNumber = Integer.parseInt(gradeCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Behaviour);
            behaviourCode = variables.get(Globals.StandardVariableNames.Behaviour).toString();
            behaviourNumber = Integer.parseInt(behaviourCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Morphology);
            morphologyCode = variables.get(Globals.StandardVariableNames.Morphology).toString();
            morphologyNumber = Integer.parseInt(morphologyCode);
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }
 
        boolean gradeMandatory = true;

        if (behaviourNumber != 3) // non-malignant; shouldn't have Grade
        {
            if (gradeCode.trim().length() == 0 || gradeCode.equals("9")) {
                result.setMessage("");
                result.setResultCode(CheckResult.ResultCode.OK);
                return result;
            } else {
                result.setMessage(morphologyCode + "/" + behaviourCode + ", " + gradeCode);
                result.setResultCode(CheckResult.ResultCode.Invalid);
                return result;
            }
        }

        // now we have malignant case, Grade variable has been identified...
        if (gradeCode.trim().length() == 0) {
            if (gradeMandatory) // mandatory
            {
                result.setMessage("");
                result.setResultCode(CheckResult.ResultCode.Missing);
            } else // optional
            {
                result.setMessage("");
                result.setResultCode(CheckResult.ResultCode.OK);
            }
            return result;
        }

//    	GraNum = (int)(gradeCode.charAt(0) - '0');
        
        if (gradeNumber < 1 || gradeNumber > 9) // invalid code
        {
            result.setMessage(gradeCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }

        //---------------< valid Grade code is present, check for consistency
        boolean graderesult = true;

        // Skip the edit if Grade = 9        
        if (gradeNumber == 9) {
            // Do nothing
        } else {

            //-----------------< Well differentiated
            if ((morphologyNumber == 8331 || morphologyNumber == 9187 || morphologyNumber == 9511) && (gradeNumber != 1)) {
                graderesult = false;
            }

            //-----------------< Mod differentiated
            if ((morphologyNumber == 8332 || morphologyNumber == 9083 || morphologyNumber == 9243 || morphologyNumber == 9372) && (gradeNumber != 2)) {
                graderesult = false;
            }

            //-----------------< Poorly differentiated
            if ((morphologyNumber == 8631 || morphologyNumber == 8634) && (gradeNumber != 3)) {
                graderesult = false;            
            }

            //-----------------< Undifferentiated, Anaplastic
            if ((morphologyNumber == 8020 || morphologyNumber == 8021 || morphologyNumber == 8805 || morphologyNumber == 9062 || morphologyNumber == 9082 || morphologyNumber == 9390 || morphologyNumber == 9392 || morphologyNumber == 9401 || morphologyNumber == 9451 || morphologyNumber == 9505 || morphologyNumber == 9512) && (gradeNumber != 4)) {
                graderesult = false;            
            }

            //------------< if not lymphomas or leukemia..
            if ((gradeNumber >= 5 && gradeNumber <= 8) && morphologyNumber < 9590) {
                graderesult = false;            
            }

            //-----------------< T cell lymphomas
            if ((morphologyNumber == 9702 || morphologyNumber == 9705 || morphologyNumber == 9708 || morphologyNumber == 9709 || morphologyNumber == 9717 || morphologyNumber == 9718 || morphologyNumber == 9719 || morphologyNumber == 9729 || morphologyNumber == 9827 || morphologyNumber == 9834 || morphologyNumber == 9837) && (gradeNumber != 5)) {
                graderesult = false;            
            }

            //---------< T-cell and Null cell lymphoma, anaplastic
            if ((morphologyNumber == 9714) && (gradeNumber != 4 && gradeNumber != 5 && gradeNumber != 7)) {
                graderesult = false;            
            }

            //---------< T-cell Killer cell lymphoma
            if ((morphologyNumber >= 9700 && morphologyNumber <= 9719) && (gradeNumber != 5 && gradeNumber != 8)) {
                graderesult = false;            
            }

            //-----------------< B cell lymphomas
            if (((morphologyNumber >= 9670 && morphologyNumber <= 9699) || morphologyNumber == 9728 || morphologyNumber == 9823 || morphologyNumber == 9833 || morphologyNumber == 9836) && (gradeNumber != 6)) {
                graderesult = false;            
            }

            //---------< Killer cell
            if ((morphologyNumber == 9948) && (gradeNumber != 8)) {
                graderesult = false;
            }
        }

        //--------------------------------< conclusion of cross checks
        if (graderesult) // valid
        {
            result.setMessage("");
            result.setResultCode(CheckResult.ResultCode.OK);
            return result;
        } else {
            result.setMessage(morphologyCode + ", " + gradeCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }
    }

    /**
     * 
     * @return
     */
    public CheckNames getCheckName() {
        return checkName;
    }
}
