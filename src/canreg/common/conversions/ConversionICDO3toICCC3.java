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
import canreg.common.RulesLoader;
import canreg.common.conversions.ConversionResult.ResultCode;
import canreg.common.conversions.Converter.ConversionName;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConversionICDO3toICCC3 implements ConversionInterface {

    private static ConversionName conversionName = ConversionName.ICDO3toICCC3;
    private static StandardVariableNames[] variablesNeeded = new StandardVariableNames[]{
        StandardVariableNames.Sex,
        StandardVariableNames.Topography,
        StandardVariableNames.Morphology,
        StandardVariableNames.Behaviour
    };
    private static StandardVariableNames[] variablesCreated = new StandardVariableNames[]{
        StandardVariableNames.ICCC
    };
    static private final int UNASSIGNED = -1;
    static private final int MAXMORPHRANGE = 1990;	//	Highest morph(9990) -  Lowest morph(8000)
    static private final int ICCC1Max = 12;			//	ICCC first part - group code, Roman nums
    static private final String ICCCGroup[] = {"??", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
    static private final int ICCC2Max = 6;			//	ICCC second part - subgroup, letters
    static private final String ICCCSubGrp[] = {"", "a", "b", "c", "d", "e", "f"};
    static private final int ICCC3Max = 11;			//	ICCC third part - extended code, numeric

    private String ICCCLookUpFileResource = "/canreg/common/resources/lookup/ICCC-Table.txt";
    private int nofTopGroups, nofMorLines;
    private String[] topMin;
    private String[] topMax;
    private int[] morList;
    private short[][] iccc3Table;
    private boolean showExtendedCode = false, romanNums = false;

    /**
     * 
     */
    public ConversionICDO3toICCC3() {
        try {
            LinkedList<String[]> ICCCTableRaw = RulesLoader.loadTable(this.getClass().getResourceAsStream(ICCCLookUpFileResource));
            nofMorLines = ICCCTableRaw.size() - 2;

            String[] lineElements = ICCCTableRaw.getFirst();
            nofTopGroups = lineElements.length - 1;

            topMin = new String[nofTopGroups];
            topMax = new String[nofTopGroups];

            for (int t = 0; t < nofTopGroups; ++t) {
                topMin[t] = lineElements[t + 1];
            }
            lineElements = ICCCTableRaw.get(1);
            for (int t = 0; t < nofTopGroups; ++t) {
                topMax[t] = lineElements[t + 1];
            }
            //------------------------------------------------------------< Prepare memory arrays
            morList = new int[MAXMORPHRANGE];
            for (int m = 0; m < MAXMORPHRANGE; ++m) {
                morList[m] = UNASSIGNED;
            }

            iccc3Table = new short[nofMorLines][nofTopGroups];

            //---------------------------------------< Morphology and ICCC codes in subsequent lines
            for (int m = 0; m < nofMorLines; ++m) {

                lineElements = ICCCTableRaw.get(m + 2);
                int MorLine = Integer.parseInt(lineElements[0]) - 8000;
                if (MorLine < 0 || MorLine > MAXMORPHRANGE) {
                    throw new IOException("ICCC Morph Line invalid value: " + lineElements[0]);
                }
                morList[MorLine] = m;

                for (int t = 0; t < nofTopGroups; ++t) {
                    if (t + 1 < lineElements.length) {
                        String s = lineElements[t + 1];
                        if (s.length() == 0) {
                            iccc3Table[m][t] = 0;
                        } else {
                            iccc3Table[m][t] = (short) Integer.parseInt(s);
                        }
                    } else {
                        iccc3Table[m][t] = 0;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConversionICDO3toICCC3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConversionICDO3toICCC3.class.getName()).log(Level.SEVERE, null, ex);
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
        String ICCCcode = "";
        String errorMessage = "";

        ConversionResult result[] = new ConversionResult[1];
        result[0] = new ConversionResult();
        result[0].setVariableName(StandardVariableNames.ICCC);
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

        if (!(behaviourCode.equals("0") || behaviourCode.equals("1") || behaviourCode.equals("2") || behaviourCode.equals("3"))) {
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage("Invalid behaviour code.");
            return result;
        }

        //------------------------------------< find Topog range (colum in Table)
        int TopColum = -1;
        for (int t = 0; t < nofTopGroups; ++t) {
            if (topographyCode.compareTo(topMin[t]) >= 0
                    && topographyCode.compareTo(topMax[t]) <= 0) {
                TopColum = t;
                break;
            }
        }
        if (TopColum == -1) {
            //ErrorMessage = "Invalid Topography";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage("Invalid Topography");
            return result;
        }

        //------------------------------------------< find Morph line in Table
        int MorNum = morphologyNumber - 8000;
        if (MorNum < 0 || MorNum > MAXMORPHRANGE) {
            //ErrorMessage = "Invalid Morphology";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage("Invalid Morphology");
            return result;
        }
        int MorLine = morList[MorNum];
        if (MorLine <= UNASSIGNED || MorLine > MAXMORPHRANGE) {
            //ErrorMessage = "Invalid Morphology";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage("Invalid Morphology");
            return result;
        }

        //--------------------------------------------< lookup in ICCC Table
        int ICCCnum = iccc3Table[MorLine][TopColum];
        if (ICCCnum == 0) {
            result[0].setResultCode(ConversionResult.ResultCode.Rare);
            result[0].setMessage("No ICCC code (unlikely combination)");
            return result;
        }

        // ICCCnum(5digits) consists of Group(2digits), SubGrp(1dig), ExtendCode(2digs)

        int groupNum = ICCCnum / 1000;
        if (groupNum == 0) {
            errorMessage = "Error - GroupNum zero";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage(errorMessage);
            return result;
        }
        if (groupNum > ICCC1Max) {
            errorMessage = "Error - GroupNum too large";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage(errorMessage);
            return result;
        }

        int ICCC23 = ICCCnum % 1000;
        int subGrpNum = ICCC23 / 100;
        int extendedNum = ICCC23 % 100;

        if (subGrpNum == 0) {
            errorMessage = "Error - SubGrpNum zero";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage(errorMessage);
            return result;
        }
        if (subGrpNum > ICCC2Max) {
            errorMessage = "Error - SubGrpNum too large";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage(errorMessage);
            return result;
        }

        //-------------------------< only certain combinations allow Non-Malignant codes
        Boolean nonMalignantException = false;
        if ((groupNum == 3) || (groupNum == 10 && subGrpNum == 1)) // INTRACRANIAL/SPINAL
        {
            nonMalignantException = true;
        }

        if (!nonMalignantException && !behaviourCode.equals("3")) {
            errorMessage = "Non-Malignant Behaviour excluded";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage(errorMessage);
            return result;
        }

        //-----------------------------------------< construct string ICCC code
        if (romanNums) {
            ICCCcode = ICCCGroup[groupNum];
        } else {
            ICCCcode = Integer.toString(groupNum);
        }

        if (groupNum != 5) //  group 5 has no subgroups
        {
            ICCCcode += ("" + ICCCSubGrp[subGrpNum]);
        }

        if (!showExtendedCode) {
            result[0].setResultCode(ConversionResult.ResultCode.OK);
            result[0].setMessage(errorMessage);
            result[0].setValue(ICCCcode);
            return result;
        }

        //-----------------------------------------------------< extended code
        if (extendedNum == 0) // no extended code exists
        {
            result[0].setResultCode(ConversionResult.ResultCode.OK);
            result[0].setMessage(errorMessage);
            result[0].setValue(ICCCcode);
            return result;
        }
        if (extendedNum > ICCC3Max) {
            errorMessage = "Error - ExtendedNum too large";
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage(errorMessage);
            result[0].setValue(ICCCcode);
            return result;
        }

        ICCCcode += (" " + Integer.toString(extendedNum));
        result[0].setResultCode(ConversionResult.ResultCode.OK);
        result[0].setMessage(errorMessage);
        result[0].setValue(ICCCcode);
        return result;
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
