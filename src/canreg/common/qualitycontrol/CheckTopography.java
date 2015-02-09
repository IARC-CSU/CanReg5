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
import canreg.common.LookUpLoader;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class CheckTopography extends CheckInterface {

    /**
     * 
     */
    public static Checker.CheckNames checkName = Checker.CheckNames.Topography;
    
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Topography,
    };
    
    Map<String,String> topographyICD10Map;
    private int codeLength = 3;
    private String lookUpFileResource = "/canreg/common/resources/lookup/O3_10T.txt";

    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     * 
     */
    public CheckTopography() {
        InputStream resourceStream = this.getClass().getResourceAsStream(lookUpFileResource);
        try {
            topographyICD10Map = LookUpLoader.load(resourceStream, codeLength);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckTopography.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckTopography.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckTopography.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param variables
     * @return
     */
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {

        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String topographyCode = null;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Topography);
            topographyCode = variables.get(Globals.StandardVariableNames.Topography).toString();
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        String morphologyFamilyString = topographyICD10Map.get(topographyCode);

        if (morphologyFamilyString == null) {
            result.setMessage(topographyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            // System.out.println("not a valid top code? " + topographyCode);
            return result;
        } else {
            result.setMessage("");
            result.setResultCode(CheckResult.ResultCode.OK);
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
