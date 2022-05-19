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
public class CheckSexMorphology extends CheckInterface {

    private static final Logger LOGGER = Logger.getLogger(CheckSexMorphology.class.getName());
    /**
     * 
     */
    public static Checker.CheckNames checkName = Checker.CheckNames.SexMorphology;
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour
    };
    /**
     * 
     */
    public static int maleCode = 1;
    /**
     * 
     */
    public static int femaleCode = 2;
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
    public CheckSexMorphology() {
        InputStream resourceStream = this.getClass().getResourceAsStream(lookUpFileResource);
        try {
            morphologicalFamiliesMap = LookUpLoader.load(resourceStream, codeLength);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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

        String sexCode = null;
        String morphologyCode = null;
        String behaviourCode = null;

        int sexNumber = 0;
        int morphologyNumber = 0;
        int behaviourNumber = 0;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Sex);
            sexCode = variables.get(Globals.StandardVariableNames.Sex).toString();
            sexNumber = Integer.parseInt(sexCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Morphology);
            morphologyCode = variables.get(Globals.StandardVariableNames.Morphology).toString();

            // see to that morphology has 4 digits
            if (morphologyCode.length() < 4) {
                result.setMessage(morphologyCode);
                result.setResultCode(CheckResult.ResultCode.Invalid);
                // System.out.println("not a valid morph code? " + morphologyCode);
                return result;
            }

            // look at the first four digits only
            morphologyCode = morphologyCode.substring(0, 4);

            morphologyNumber = Integer.parseInt(morphologyCode);
            result.addVariableInvolved(Globals.StandardVariableNames.Behaviour);
            behaviourCode = variables.get(Globals.StandardVariableNames.Behaviour).toString();
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }


        /*
        if ( (sexNumber==maleCode) &&
        (morphologyNumber==8380 || morphologyNumber==8381 || morphologyNumber==8441 || morphologyNumber==8460 ||
        morphologyNumber==8461 || morphologyNumber==8462 || morphologyNumber==8470 ||
        morphologyNumber==8471 || morphologyNumber==8472 || morphologyNumber==8473 ||
        morphologyNumber==8600 || morphologyNumber==8601 || morphologyNumber==8602 ||
        morphologyNumber==8610 || morphologyNumber==8620 || morphologyNumber==8621 ||
        morphologyNumber==8622 || morphologyNumber==8623 || morphologyNumber==8632 ||
        morphologyNumber==8660 || morphologyNumber==8670 || morphologyNumber==8931 ||
        morphologyNumber==9000 || morphologyNumber==9013 || morphologyNumber==9014 ||
        morphologyNumber==9015 || morphologyNumber==9090 || morphologyNumber==9091) )
         */

        int morphologyFamily = 0;

        String morphologyFamilyString = morphologicalFamiliesMap.get(morphologyCode);

        if (morphologyFamilyString == null) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Invalid morphology code.");
            return result;
        } else {
            try {
                morphologyFamily = Integer.parseInt(morphologyFamilyString.substring(1, 3));
            } catch (NumberFormatException numberFormatException) {
                morphologyFamily = 0;
            }
        }

        if ((sexNumber == maleCode) && (morphologyFamily == 22 // Vulva, Vagina
                || morphologyFamily == 23 // Uterus
                || morphologyFamily == 24 // Ovary
                || morphologyFamily == 25 // Female Gen and other tissues
                || morphologyFamily == 26)) // Placenta
        {
            result.setMessage(sexCode + ", " + morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
            return result;
        }

        if (sexNumber == maleCode && morphologyNumber == 9084 && behaviourNumber == 3) // Malig only in Ovary
        {
            result.setMessage(sexCode + ", " + morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
            return result;
        }

        if ((sexNumber == femaleCode) && (morphologyFamily == 27 // Penis
                || morphologyFamily == 28)) // Prostate, Testis
        {
            result.setMessage(sexCode + ", " + morphologyCode);
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
    public CheckNames getCheckName() {
        return checkName;
    }
}
