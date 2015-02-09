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
public class CheckTopographyMorphology extends CheckInterface {

    public static Map<String, String> morphologicalFamiliesMap = null;
    public static Map<String, String> lookUpMustMap = null;
    public static Map<String, String> lookUpMustNotMap = null;
    /**
     *
     */
    public static final int mustKeyLength = 6;
    private static final int mustNotkeyLength = 6;
    public static String Must_LookupFile = "/canreg/common/resources/lookup/Must.txt";
    public static String MustNot_LookupFile = "/canreg/common/resources/lookup/MustNot.txt";

    @Override
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return new Globals.StandardVariableNames[]{
                    Globals.StandardVariableNames.Topography,
                    Globals.StandardVariableNames.Morphology};
    }

    @Override
    public CheckNames getCheckName() {
        return Checker.CheckNames.TopographyMorphology;
    }

    @Override
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {
        CheckResult result = new CheckResult();
        result.setCheckName(CheckNames.TopographyMorphology.toString());
        result.setResultCode(CheckResult.ResultCode.NotDone);

        String morphologyCode = null;
        String topographyCode = null;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Topography);
            topographyCode = variables.get(
                    Globals.StandardVariableNames.Topography).toString();
            result.addVariableInvolved(Globals.StandardVariableNames.Morphology);
            morphologyCode = variables.get(
                    Globals.StandardVariableNames.Morphology).toString();
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }

        if (morphologyCode.length() < 4) {
            // System.out.println("SHOULDN'T HAVE BEEN CALLED WHEN INVALID");
            result.setMessage(morphologyCode + " is too short.");
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }

        morphologyCode = morphologyCode.substring(0, 4);
        String morphologyFamilyString = morphologicalFamiliesMap.get(morphologyCode);

        if (morphologyFamilyString == null) {
            result.setMessage(morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            // System.out.println("not a valid morph code? " + morphologyCode);
            return result;
        }
        if (morphologyFamilyString.length() < 3) {
            // System.out.println("SHOULDN'T HAVE BEEN CALLED WHEN INVALID - Morph_fam_line");
            result.setMessage(morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }
        // If Code Topo est de la forme 42, on sort l'enreg � unlikely... car il
        // manquerait le zero
        // on the left
        if (topographyCode.length() == 2) {
            // System.out.println("SHOULDN'T HAVE BEEN CALLED WHEN INVALID - topo");
            result.setMessage(topographyCode);
            result.setResultCode(CheckResult.ResultCode.Query);
            return result;
        }
        if (topographyCode.length() < 3) {
            // System.out.println("SHOULDN'T HAVE BEEN CALLED WHEN INVALID - topo");
            result.setMessage(topographyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }

        String MustLookupResult = "";
        String MustNotLookupResult = "";

        int familyInt = 0;
        String familyString = morphologyFamilyString.substring(1, 3);
        try {
            familyInt = Integer.parseInt(familyString);
        } catch (java.lang.NumberFormatException nfe) {
            familyInt = -999;
        }
        String look = familyString + "C" + topographyCode;

        // ------< '*' all families, '+' must, '-' mustnot >-----
        switch (morphologyFamilyString.charAt(0)) {
            case '*': // all families
            {
                result.setMessage("");
                result.setResultCode(CheckResult.ResultCode.OK);
                break;
            }

            case '+': // ----------------------------< must file
            {
                // --------< "look" is "family"+"topography"
                // -----------< compare up to topog site only
                // Get The partial key and if the key exits, ended by '*' , or
                // complete key
                // MustLookupResult = lookUpMustMap5.get(look.substring(0, 5));

                String look1 = look.substring(0, mustKeyLength - 1) + "*";

                if (lookUpMustMap.containsKey(look1)) {

                    result.setMessage("");
                    result.setResultCode(CheckResult.ResultCode.OK);

                    break;
                } else if (lookUpMustMap.containsKey(look.substring(0, mustKeyLength))) {


                    result.setMessage("");
                    result.setResultCode(CheckResult.ResultCode.OK);

                } else {
                    // -----------< entry not found on partial key  Test all the key
                    String messageString = morphologyFamilyString;
                    try {
                        messageString = "" + (Integer.parseInt(morphologyFamilyString.substring(1)) + 1);
                    } catch (NumberFormatException nfe) {
                        messageString = morphologyFamilyString;
                    }
                    result.setMessage("Morpology ("+morphologyCode+") is in family " + messageString +", but topography is "+topographyCode+".");
                    result.setResultCode(CheckResult.ResultCode.Rare);

                    break;
                }
            }

            case '-': // ----------------------< must not file
            { // If we find combination of morpho and topo, it must be unlikely
                // because
                // we must not find this association.!!+
                // For example, We search for 5 characters in the key (67C77*)
                // Then we search for the 6 characters
                String look1 = look.substring(0, mustNotkeyLength - 1) + "*";
                if (lookUpMustNotMap.containsKey(look1)) {
                    String messageString = morphologyFamilyString;
                    try {
                        messageString = "" + (Integer.parseInt(morphologyFamilyString.substring(1)) + 64);
                    } catch (NumberFormatException nfe) {
                        messageString = morphologyFamilyString;
                    }
                    result.setMessage("Morpology ("+morphologyCode+") is in family " + messageString +", but topography is "+topographyCode+".");
                    result.setResultCode(CheckResult.ResultCode.Rare);
                    
                    break;
                    // Search for complete key
                    // There is not * in the key for example String key="68C420";
                    // We search the key in 6 items
                } else if (lookUpMustNotMap.containsKey(look.substring(0,
                        mustNotkeyLength))) {
                    // We have found forbidden code.

                    result.setMessage("Fam.:" + morphologyFamilyString);
                    result.setResultCode(CheckResult.ResultCode.Rare);

                    break;

                } else {
                    // We haven't found this combination >> OK
                    result.setMessage("");
                    result.setResultCode(CheckResult.ResultCode.OK);
                    break;
                }
            }// break;
        }// end of switch

        return result;
    }

    public CheckTopographyMorphology() {
        InputStream morphFamResourceStream = this.getClass().getResourceAsStream(
                Globals.morphologicalFamiliesLookUpFileResource);
        InputStream lookUpMustResourceStream = this.getClass().getResourceAsStream(Globals.mustLookupFile);
        InputStream lookUpMustNotResourceStream = this.getClass().getResourceAsStream(Globals.mustNotLookupFile);

        try {
            morphologicalFamiliesMap = LookUpLoader.load(morphFamResourceStream, 4);
            lookUpMustMap = LookUpLoader.load(lookUpMustResourceStream, mustKeyLength);
            lookUpMustNotMap = LookUpLoader.load(lookUpMustNotResourceStream, mustNotkeyLength);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }
}
