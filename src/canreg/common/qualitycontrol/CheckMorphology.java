/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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
public class CheckMorphology extends CheckInterface {

    /**
     * 
     */
    public static Checker.CheckNames checkName = Checker.CheckNames.Morphology;
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Morphology,};
    Map<String, String> morphologicalFamiliesMap;
    private int codeLength = 4;
    private String lookUpFileResource = "/canreg/common/resources/lookup/MorphFam.txt";

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
    public CheckMorphology() {
        InputStream resourceStream = this.getClass().getResourceAsStream(lookUpFileResource);
        try {
            morphologicalFamiliesMap = LookUpLoader.load(resourceStream, codeLength);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE, null, ex);
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

        String morphologyCode = null;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Morphology);
            morphologyCode = variables.get(Globals.StandardVariableNames.Morphology).toString();

        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        // see to that morphology has 4 digits
        if (morphologyCode.length() < 4) {
            result.setMessage(morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            // System.out.println("not a valid morph code? " + morphologyCode);
            return result;
        }

        // look at the first four digits only
        morphologyCode = morphologyCode.substring(0, 4);

        String morphologyFamilyString = morphologicalFamiliesMap.get(morphologyCode);

        if (morphologyFamilyString == null) {
            result.setMessage(morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            // System.out.println("not a valid morph code? " + morphologyCode);
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
