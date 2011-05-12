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
import canreg.common.Globals.StandardVariableNames;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class CheckBasis extends CheckInterface {

    /**
     * 
     */
    public CheckBasis(){
        init();
    }
    
    private boolean ready = true;

    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames [] {
        Globals.StandardVariableNames.BasisDiagnosis,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Topography
    };
    
    private enum BasisCat {
        MicroscopicallyConfirmed,
        NonMicroscopicallyConfirmed,
        Unknown
    };
    
    /**
     * 
     */
    public Checker.CheckNames checkName = Checker.CheckNames.Basis;
    
    private static Map<String,BasisCat> basisCatMap = new LinkedHashMap();
            
    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    private boolean init() {
        ready = true;
        basisCatMap.put("0", BasisCat.NonMicroscopicallyConfirmed);
        basisCatMap.put("1", BasisCat.NonMicroscopicallyConfirmed);
        basisCatMap.put("2", BasisCat.NonMicroscopicallyConfirmed);
        basisCatMap.put("3", BasisCat.NonMicroscopicallyConfirmed);
        basisCatMap.put("4", BasisCat.NonMicroscopicallyConfirmed);
        basisCatMap.put("5", BasisCat.MicroscopicallyConfirmed);
        basisCatMap.put("6", BasisCat.MicroscopicallyConfirmed);
        basisCatMap.put("7", BasisCat.MicroscopicallyConfirmed);
        basisCatMap.put("8", BasisCat.MicroscopicallyConfirmed);
        basisCatMap.put("9", BasisCat.Unknown);
        return ready;
    }

    /**
     * 
     * @param variables
     * @return
     */
    public CheckResult performCheck(Map<StandardVariableNames, Object> variables)     {
        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());
        
        int morphologyNumber = 0;
        int topographyNumber = 0;
        String basisCode = null;
        String morphologyCode = null;
        
        try {
            result.addVariableInvolved(Globals.StandardVariableNames.BasisDiagnosis);
            basisCode = variables.get(Globals.StandardVariableNames.BasisDiagnosis).toString();
            int basisNumber = Integer.parseInt(basisCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Morphology);
            morphologyCode = variables.get(Globals.StandardVariableNames.Morphology).toString();
            morphologyNumber = Integer.parseInt(morphologyCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Topography);
            String topographyCode = variables.get(Globals.StandardVariableNames.Topography).toString();
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
        
    	//-------------< Morph codes accepted whether HV or not...
    	if ((morphologyNumber == 8000 ||
    		(morphologyNumber >= 8150 && morphologyNumber <= 8154) ||
    		 morphologyNumber == 8170 ||
    		(morphologyNumber >= 8270 && morphologyNumber <= 8281) ||
    		 morphologyNumber == 8800 ||
    		(morphologyNumber == 8720 && topographyNumber/10 == 69) ||
    		(morphologyNumber == 8720 && topographyNumber/10 == 44) ||
    		 morphologyNumber == 8960 ||
    		 morphologyNumber == 9050 ||
    		 morphologyNumber == 9100 ||
    		 morphologyNumber == 9140 ||
    		 morphologyNumber == 9350 ||
    		 morphologyNumber == 9380 ||
    		 morphologyNumber == 9384 ||
    		 morphologyNumber == 9500 ||
    		 morphologyNumber == 9510 ||
    		(morphologyNumber >= 9530 && morphologyNumber <= 9539) ||
    		 morphologyNumber == 9590 ||
    		 morphologyNumber == 9732 ||
    		 morphologyNumber == 9761 ||
    		 morphologyNumber == 9800))
    	{
    		result.setMessage("");
    		result.setResultCode(ResultCode.OK);
    		return result;
    	}
    	else if (BasisCat.MicroscopicallyConfirmed != basisCatMap.get(basisCode)) // should be Microscopically Verified
    	{
    		result.setResultCode(ResultCode.Query); // crRare ?? crQuery ?? crInvalid ??
    		result.setMessage(morphologyCode + ", " + basisCode);
    		return result;
    	}
    	else
    	{
    		result.setMessage("");
    		result.setResultCode(ResultCode.OK);
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
