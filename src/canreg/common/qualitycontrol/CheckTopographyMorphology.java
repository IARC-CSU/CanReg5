/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common.qualitycontrol;

import canreg.common.Globals;
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

    private Map<String, String> morphologicalFamiliesMap = null;
    private Map<String, String> lookUpMustMap5 = null;
    private Map<String, String> lookUpMustMap6 = null;
    private Map<String, String> lookUpMustNotMap5 = null;
    private Map<String, String> lookUpMustNotMap6 = null;
    
    /**
     *
     */
    public static String morphologicalFamiliesLookUpFileResource = "/canreg/common/resources/lookup/MorphFam.txt";
    public static final int mustCodeLength = 5;
    public static String Must_LookupFile = "/canreg/common/resources/lookup/Must.txt";
    public static final int mustNotCodeLength = 6;
    public static String MustNot_LookupFile = "/canreg/common/resources/lookup/MustNot.txt";

    @Override
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return new Globals.StandardVariableNames[]{
                    Globals.StandardVariableNames.Topography,
                    Globals.StandardVariableNames.Morphology
                };
    }

    @Override
    public CheckNames getCheckName() {
        return Checker.CheckNames.TopographyMorphology;
    }

    @Override
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {
        CheckResult result = new CheckResult();
        result.setCheckName(Checker.CheckNames.TopographyMorphology.toString());
        result.setResultCode(CheckResult.ResultCode.NotDone);

        String morphologyCode = null;
        String topographyCode = null;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Topography);
            topographyCode = variables.get(Globals.StandardVariableNames.Topography).toString();
            result.addVariableInvolved(Globals.StandardVariableNames.Morphology);
            morphologyCode = variables.get(Globals.StandardVariableNames.Morphology).toString();
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
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
            System.out.println("SHOULDN'T HAVE BEEN CALLED WHEN INVALID - Morph_fam_line");
            result.setMessage(morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }

        String MustLookupResult = "";
        String MustNotLookupResult = "";

        int familyInt = 0;
        String familyString = morphologyFamilyString.substring(1, 3);
        try {
            familyInt = Integer.parseInt(familyString);
        } catch (java.lang.NumberFormatException nfe){
            familyInt = -999;
        }
        String look = familyString + "C" + topographyCode;

        //------< '*' all families, '+' must, '-' mustnot >-----
        switch (morphologyFamilyString.charAt(0)) {
            case '*': // all families
            {
                result.setMessage("");
                result.setResultCode(CheckResult.ResultCode.OK);
                return result;
            }

            case '+': //----------------------------< must file
            {
                //--------< "look" is "family"+"topography"
                //-----------< compare up to topog site only
                MustLookupResult = lookUpMustMap5.get(look.substring(0, 5));
                if (MustLookupResult != null && MustLookupResult.length() > 5) {
                    if (MustLookupResult.charAt(5) == '*'
                            || MustLookupResult.charAt(5) == topographyCode.charAt(2)) {
                        result.setMessage("");
                        result.setResultCode(CheckResult.ResultCode.OK);
                        return result;
                    }
                    // case of 8270/3,75.1 etc
                    MustLookupResult = lookUpMustMap6.get(look.substring(0, 6));
                    if (MustLookupResult.length() > 5) {
                        if (MustLookupResult.charAt(5) == topographyCode.charAt(2)) {
                            result.setMessage("");
                            result.setResultCode(CheckResult.ResultCode.OK);
                            return result;
                        }
                    }
                }

                //-----------< entry not found either 5 or 6 characters
                result.setMessage(topographyCode + ", " + morphologyCode);
                result.setResultCode(CheckResult.ResultCode.Rare);
                return result;
            }

            case '-': //----------------------< must not file
            {
                MustNotLookupResult = lookUpMustNotMap5.get(look.substring(0, 5));
                if (MustNotLookupResult!=null && MustNotLookupResult.length() > 5) {
                    if (MustNotLookupResult.charAt(5) == '*'
                            || MustLookupResult.charAt(5) == topographyCode.charAt(2)) {
                        result.setMessage(topographyCode + ", " + morphologyCode);
                        result.setResultCode(CheckResult.ResultCode.Rare);
                        return result;
                    }
                    //-----------------------< in case sub-site dependant
                    MustNotLookupResult = lookUpMustNotMap6.get(look.substring(0, 6));
                    if (MustNotLookupResult.length() > 5) {
                        if (MustLookupResult.charAt(5) == topographyCode.charAt(2)) {
                        result.setMessage(topographyCode + ", " + morphologyCode);
                        result.setResultCode(CheckResult.ResultCode.Rare);
                        return result;
                        }
                    }
                }
                //	not found in MustNot file
                result.setMessage("");
                result.setResultCode(CheckResult.ResultCode.OK);
                return result;
            }//break;
        }// end of switch
        result.setMessage("");
        result.setResultCode(CheckResult.ResultCode.OK);
        return result;
    }

    public CheckTopographyMorphology() {
        InputStream morphFamResourceStream = this.getClass().getResourceAsStream(morphologicalFamiliesLookUpFileResource);
        InputStream lookUpMustResourceStream = this.getClass().getResourceAsStream(Must_LookupFile);
        InputStream lookUpMustNotResourceStream = this.getClass().getResourceAsStream(MustNot_LookupFile);

        try {
            morphologicalFamiliesMap = iarctools.tools.LookUpLoader.load(morphFamResourceStream, 4);
            lookUpMustMap5 = iarctools.tools.LookUpLoader.load(lookUpMustResourceStream, 5);
            lookUpMustNotMap5 = iarctools.tools.LookUpLoader.load(lookUpMustNotResourceStream, 5);
            lookUpMustMap6 = iarctools.tools.LookUpLoader.load(lookUpMustResourceStream, 6);
            lookUpMustNotMap6 = iarctools.tools.LookUpLoader.load(lookUpMustNotResourceStream, 6);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckMorphology.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
