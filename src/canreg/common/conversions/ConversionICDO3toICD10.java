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
package canreg.common.conversions;

import canreg.common.Globals.StandardVariableNames;
import canreg.common.LookUpLoader;
import canreg.common.RulesLoader;
import canreg.common.conversions.ConversionResult.ResultCode;
import canreg.common.conversions.Converter.ConversionName;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class ConversionICDO3toICD10 implements ConversionInterface {

    private static final ConversionName conversionName = ConversionName.ICDO3toICD10;
    private static final StandardVariableNames[] variablesNeeded = new StandardVariableNames[]{
        StandardVariableNames.Sex,
        StandardVariableNames.Topography,
        StandardVariableNames.Morphology,
        StandardVariableNames.Behaviour
    };
    private static final StandardVariableNames[] variablesCreated = new StandardVariableNames[]{
        StandardVariableNames.ICD10
    };
    /**
     * 
     */
    public static int maleCode = 1;
    /**
     * 
     */
    public static int femaleCode = 2;
    private Map<String, String> topographyICD10Map;
    private int topographyCodeLength = 3;
    private String topographyLookUpFileResource = "/canreg/common/resources/lookup/O3_10T.txt";
    private Map<String, String> morphologyICD10Map;
    private int morphologyCodeLength = 5;
    private String morphologyLookUpFileResource = "/canreg/common/resources/lookup/O3_10M.txt";
    private Map<Integer, String> topographyRule8Map;
    private int topographyRule8CodeLength = 7;
    private String topographyRule8FileResource = "/canreg/common/resources/lookup/O3_10r8.txt";
    private Map<Integer, String> topographyRule9Map;
    private int topographyRule9CodeLength = 7;
    private String topographyRule9FileResource = "/canreg/common/resources/lookup/O3_10r9.txt";
    private char flag = ' ';
    private String ICD10Male = "";
    private String ICD10Female = "";
    private boolean sexDependent;
    private String ICD10;

    /**
     * 
     */
    public ConversionICDO3toICD10() {
        try {
            // replace by getResourceAsStream to allow for packaging in a jar/!
            topographyICD10Map = LookUpLoader.load(this.getClass().getResourceAsStream(topographyLookUpFileResource), topographyCodeLength);
            morphologyICD10Map = LookUpLoader.load(this.getClass().getResourceAsStream(morphologyLookUpFileResource), morphologyCodeLength);
            topographyRule8Map = RulesLoader.load(this.getClass().getResourceAsStream(topographyRule8FileResource), topographyRule8CodeLength);
            topographyRule9Map = RulesLoader.load(this.getClass().getResourceAsStream(topographyRule9FileResource), topographyRule9CodeLength);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConversionICDO3toICD10.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConversionICDO3toICD10.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ConversionICDO3toICD10.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @return
     */
    @Override
    public StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     * 
     * @return
     */
    @Override
    public StandardVariableNames[] getVariablesCreated() {
        return variablesCreated;
    }

    /**
     * 
     * @param variables
     * @return
     */
    @Override
    public ConversionResult[] performConversion(Map<StandardVariableNames, Object> variables) {

        ICD10Male = "?????";
        ICD10Female = "?????";
        sexDependent = false;
        flag = ' ';

        ConversionResult result[] = new ConversionResult[1];
        result[0] = new ConversionResult();
        result[0].setVariableName(StandardVariableNames.ICD10);
        result[0].setResultCode(ResultCode.OK);

        String sexCode = null;
        String topographyCode = null;
        String morphologyCode = null;
        String behaviourCode = null;

        int sexNumber = 0;
        int morphologyNumber = 0;
        int behaviourNumber = 0;
        int topographyNumber = 0;

        try {
            sexCode = variables.get(StandardVariableNames.Sex).toString();
            sexNumber = Integer.parseInt(sexCode);
            morphologyCode = variables.get(StandardVariableNames.Morphology).toString();
            morphologyNumber = Integer.parseInt(morphologyCode);
            topographyCode = variables.get(StandardVariableNames.Topography).toString();
            topographyNumber = Integer.parseInt(morphologyCode);
            behaviourCode = variables.get(StandardVariableNames.Behaviour).toString();
        } catch (NumberFormatException numberFormatException) {
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage("Not a number");
            return result;
        } catch (NullPointerException nullPointerException) {
            result[0].setResultCode(ConversionResult.ResultCode.Missing);
            result[0].setMessage("Missing variable(s) needed.");
            return result;
        }

        String morphology5Code = morphologyCode + behaviourCode;
        // we can only generate ICD10 if we have enough characters
        if (morphology5Code.length() >= morphologyCodeLength) {
            String morphologyLookUpLine = morphologyICD10Map.get(morphology5Code.substring(0, morphologyCodeLength));
            if (morphologyLookUpLine == null) {
                result[0].setMessage(morphologyCode + "/" + behaviourCode);
                result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                return result;
            }


            //---------------------------------------------< extract conversion rule
            int rule;
            if (morphologyLookUpLine.charAt(0) > 58) //* 'E','F','G','H', 'K','L','M'
            // (" - Rare Morphology/Behaviour combination")
            {
                rule = (int) morphologyLookUpLine.charAt(0) - 68;   // 1,2,3,4, 7,8,9
            } else // '1','2','3','4','5',  '7','8','9'
            {
                rule = (int) morphologyLookUpLine.charAt(0) - 48; // 1,2,3,4,5, 7,8,9

                //--------------------------------------------< act according to rule
            }

            if (rule <= 5) // conversion dependent on Topography code
            {
                String topographyLookUpLine = topographyICD10Map.get(topographyCode);
                TopogConv(rule, topographyLookUpLine);
            } else if (rule == 7) // conversion dependent on Morphology only
            {
                ICD10Male = morphologyLookUpLine.substring(1, morphologyLookUpLine.length());
                if (ICD10Male.length() > 3 && ICD10Male.charAt(3) == '*') {
                    ICD10Male = ICD10Male.substring(0, 3) + topographyCode.charAt(2);
                }
                if (morphologyLookUpLine.length() > 5) {
                    flag = morphologyLookUpLine.charAt(5);
                }
            } else if (rule == 8) // dependent on Top and Mor
            {
                boolean ret = Rule8(morphologyLookUpLine, topographyCode);
                if (!ret) {
                    result[0].setMessage("Error - conversion Rule 8");
                    result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                    return result;
                }
            } else if (rule == 9) // dependent on Top, Mor and Sex
            {
                boolean ret = Rule9(morphologyLookUpLine, topographyCode);
                if (!ret) {
                    result[0].setMessage("Error - conversion Rule 9");
                    result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                    return result;
                }
            } else {
                result[0].setMessage("Error - Wrong conversion Rule");
                result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                return result;
            }
            //----------------------------------------------------< LIVER (Malignant)
            if (topographyCode.startsWith("22") && behaviourCode.equals("3")) {
                int MorphNum = Integer.parseInt(morphologyCode);

                if ((MorphNum >= 9120 && MorphNum <= 9133) || MorphNum == 9161) {
                    ICD10Male = "C223";
                } else if (MorphNum == 8970) {
                    ICD10Male = "C222";
                } else if (MorphNum == 8160 || MorphNum == 8161 || MorphNum == 8162
                        || MorphNum == 8140 || MorphNum == 8141 || MorphNum == 8260
                        || MorphNum == 8440 || MorphNum == 8480 || MorphNum == 8481
                        || MorphNum == 8490 || MorphNum == 8500 || MorphNum == 8550 || MorphNum == 8560) {
                    ICD10Male = "C221";
                } else if (MorphNum == 8170 || MorphNum == 8171) {
                    ICD10Male = "C220";
                } else if (MorphNum == 8000) {
                    ICD10Male = "C229";
                } else if (MorphNum < 8800) {
                    ICD10Male = "C227";
                } else if (MorphNum < 9590) {
                    ICD10Male = "C224";
                }
            }

            if (ICD10Male.equals("D218")) {
                ICD10Male = "D219";
                // Icd10m = DEPedits.SetStringChar(Icd10m, '9', 3);
            }
            if (ICD10Male.startsWith("C50") && rule == 9) {
                sexDependent = false;
            }
            ICD10 = ICD10Male;
            ICD10 = ICD10.trim();
            //if (PutDot && ICD10.length()>3)
            //	ICD10 = ICD10.substring(0,3) + "." + ICD10.substring(3,4);

            if (flag != ' ') {
                if (flag == '+' && sexNumber != femaleCode) // female symbol
                {
                    //  Female Histology; Not Female Sex code
                    result[0].setMessage("Female Histology; Not Female Sex code " + morphologyCode);
                    result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                    return result;
                }
                if (flag == '@' && sexNumber != maleCode) // male symbol
                {
                    //  Male Histology; Not Male Sex code
                    result[0].setMessage(" Male Histology; Not Male Sex code " + morphologyCode);
                    result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                    return result;
                }
            }

            if ((sexDependent) && (!ICD10Female.substring(0, 3).equals(ICD10Male.substring(0, 3)))) {
                if (sexNumber == maleCode) {
                    result[0].setValue(tidyICD10code(ICD10));
                    return result;
                } else if (sexNumber == femaleCode) //  overwrite ICD10Code[]
                {
                    ICD10 = ICD10Female;
                    ICD10 = ICD10.trim();
                    result[0].setValue(tidyICD10code(ICD10));
                    return result;
                } else // SexVal is not MALE or FEMALE, yet conversion is SexDependant
                {
                    result[0].setMessage("SexVal is not MALE or FEMALE, yet conversion is SexDependant " + morphologyCode);
                    result[0].setResultCode(ConversionResult.ResultCode.Invalid);
                    return result;
                }
            }
        }
        result[0].setValue(tidyICD10code(ICD10));
        return result;
    }

    private String tidyICD10code(String code) {
        if (code.length() >= 4) {
            code = code.substring(0, 4);
        }
        return code;
    }

    private void TopogConv(int rule, String O3_10TLookLine) {
        // conversion depends only on Topography code
        // rule and pos are determined by Behaviour
        //------------------------------------------
        if (rule == 0 || rule > 5) {
            Logger.getLogger(ConversionICDO3toICD10.class.getName()).log(Level.WARNING, "ERROR : ICD10 Rule invalid (O3_10clas.java)");
            return;
        }
        if (O3_10TLookLine.length() < 20) {
            Logger.getLogger(ConversionICDO3toICD10.class.getName()).log(Level.WARNING, "ERROR : O3_10TLookLine too short");
            return;
        }

        int pos = (4 * (rule)) - 1 - 3;
        ICD10Male = O3_10TLookLine.substring(pos, pos + 4);
    }
    //__________________________________________________________________

    private boolean Rule8(String morphologyLookUpLine, String topographyCode) {

        int i1 = 0;
        try {
            i1 = Integer.parseInt(morphologyLookUpLine.substring(2).trim());
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
        i1--;
        do {
            // topographyCode isn't in the table,	use conversion for "all other sites"
            if (i1 >= topographyRule8Map.size()) {
                return false;
            }
            String topog8FileLine = topographyRule8Map.get(i1);
            if (topog8FileLine.startsWith("aos.")) {
                if (topog8FileLine.charAt(4) == ':') {
                    int rule = (int) topog8FileLine.charAt(5) - 48;
                    TopogConv(rule, topographyICD10Map.get(topographyCode));
                    break;
                }
                ICD10Male = topog8FileLine.substring(4, 8);
                flag = topog8FileLine.charAt(8);
                break;
            } //  topographyCode is in the table with all 3 digits
            else if (topographyCode.equals(topog8FileLine.substring(1, 4))) {
                ICD10Male = topog8FileLine.substring(4, 8);
                flag = topog8FileLine.charAt(8);
                break;
            } // topographyCode is in the table with 2 digits and "-"
            else if (topographyCode.length() == 3 && topographyCode.substring(0, 2).equals(topog8FileLine.substring(1, 3)) && topog8FileLine.charAt(3) == '-') {
                ICD10Male = topog8FileLine.substring(4, 8);
                if (ICD10Male.charAt(3) == '-') {
                    ICD10Male = setStringChar(ICD10Male, topographyCode.charAt(2), 3);
                }
                flag = topog8FileLine.charAt(8);
                break;
            }
            i1++;
        } while (true);
        return true;
    }
    //____________________________________________________________

    private boolean Rule9(String morphologyLookUpLine, String topographyCode) {
        sexDependent = true;

        int i1 = 0;
        try {
            morphologyLookUpLine = morphologyLookUpLine.trim();
            i1 = Integer.parseInt(morphologyLookUpLine.substring(2));
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
        i1--;

        do {
            if (i1 >= topographyRule9Map.size()) {
                return false;
            }
            String topog9FileLine = topographyRule9Map.get(i1);
            if (topog9FileLine.startsWith("aos.")) {
                if (topog9FileLine.charAt(8) == ':') // female specific site
                {
                    int rule = (int) topog9FileLine.charAt(9) - 48;
                    TopogConv(rule, topographyICD10Map.get(topographyCode));
                    ICD10Female = ICD10Male;
                } else {
                    ICD10Female = topog9FileLine.substring(8, 12);
                }
                if (topog9FileLine.charAt(4) == ':') //  male specific site
                {
                    int rule = (int) topog9FileLine.charAt(5) - 48;
                    TopogConv(rule, topographyICD10Map.get(topographyCode));
                } else {
                    ICD10Male = topog9FileLine.substring(4, 8);
                }
                break;
            } else if (topographyCode.equals(topog9FileLine.substring(1, 4))) {
                ICD10Male = topog9FileLine.substring(4, 8);
                ICD10Female = topog9FileLine.substring(8, 12);
                break;
            } else if (topographyCode.equals(topog9FileLine.substring(1, 3)) && topog9FileLine.charAt(3) == '-') {
                ICD10Male = topog9FileLine.substring(4, 8);
                if (ICD10Male.charAt(3) == '-') {
                    ICD10Male = setStringChar(ICD10Male, topographyCode.charAt(2), 3);
                }
                ICD10Female = topog9FileLine.substring(8, 12);
                if (ICD10Female.charAt(3) == '-') {
                    ICD10Female = setStringChar(ICD10Female, topographyCode.charAt(2), 3);
                }
                break;
            }
            i1++;
        } while (true);
        return true;
    }

    private String setStringChar(String theString, char theChar, int i) {
        String string = theString.substring(0, i);
        string += theChar;
        string += theString.substring(i + 1);
        return string;
    }

    /**
     * 
     * @return
     */
    @Override
    public ConversionName getConversionName() {
        return conversionName;
    }
}
