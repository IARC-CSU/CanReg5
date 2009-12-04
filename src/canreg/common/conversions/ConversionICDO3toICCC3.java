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

    /**
     * 
     */
    public ConversionICDO3toICCC3() {
        try {
            // replace by getResourceAsStream to allow for packaging in a jar/!
            topographyICD10Map = LookUpLoader.load(this.getClass().getResourceAsStream(topographyLookUpFileResource), topographyCodeLength);
            morphologyICD10Map = LookUpLoader.load(this.getClass().getResourceAsStream(morphologyLookUpFileResource), morphologyCodeLength);
            topographyRule8Map = RulesLoader.load(this.getClass().getResourceAsStream(topographyRule8FileResource), topographyRule8CodeLength);
            topographyRule9Map = RulesLoader.load(this.getClass().getResourceAsStream(topographyRule9FileResource), topographyRule9CodeLength);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConversionICDO3toICCC3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConversionICDO3toICCC3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ConversionICDO3toICCC3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @return
     */
    public StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     * 
     * @return
     */
    public StandardVariableNames[] getVariablesCreated() {
        return variablesCreated;
    }

    /**
     * 
     * @param variables
     * @return
     */
    public ConversionResult[] performConversion(Map<StandardVariableNames, Object> variables) {
        String ICCCcode = "";
        String ErrorMessage = "";

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

        if (!(behaviourCode.equals("0") || behaviourCode.equals("1") || behaviourCode.equals("2") || behaviourCode.equals("3"))) {
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            result[0].setMessage("Invalid behaviour code.");
            return result;
        }

        String morphology5Code = morphologyCode + behaviourCode;
        String morphologyLookUpLine = morphologyICD10Map.get(morphology5Code.substring(0, morphologyCodeLength));
        if (morphologyLookUpLine == null) {
            result[0].setMessage(morphologyCode + "/" + behaviourCode);
            result[0].setResultCode(ConversionResult.ResultCode.Invalid);
            return result;
        }

        /// do the conversionthingy!!!

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
    public ConversionName getConversionName() {
        return conversionName;
    }
}
