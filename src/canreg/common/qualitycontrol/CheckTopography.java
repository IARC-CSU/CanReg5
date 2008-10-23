package canreg.common.qualitycontrol;

import canreg.common.Globals;
import canreg.common.LookUpFileDescription;
import canreg.common.LookUpLoader;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class CheckTopography implements CheckInterface {

    public static Checker.CheckNames checkName = Checker.CheckNames.Topography;
    
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Topography,
    };
    
    Map<String,String> topographyICD10Map;
    private int codeLength = 3;
    private String lookUpFileResource = "/canreg/common/resources/lookup/O3_10T.txt";

    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    public CheckTopography() {
        URL resourceURL = this.getClass().getResource(lookUpFileResource);       
        LookUpFileDescription description = new LookUpFileDescription(resourceURL, codeLength);
        try {
            topographyICD10Map = LookUpLoader.load(description);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckTopography.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckTopography.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckTopography.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {

        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String topographyCode = null;

        try {
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
            System.out.println("not a valid top code? " + topographyCode);
            return result;
        } else {
            result.setMessage("");
            result.setResultCode(CheckResult.ResultCode.OK);
            return result;
        }
    }

    public CheckNames getCheckName() {
        return checkName;
    }
}
