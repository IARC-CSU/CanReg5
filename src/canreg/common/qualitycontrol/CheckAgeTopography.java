package canreg.common.qualitycontrol;

import canreg.common.Globals;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class CheckAgeTopography implements CheckInterface {

    /**
     * 
     */
    public Checker.CheckNames checkName = Checker.CheckNames.AgeTopography;
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.Topography,
    };

    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     * 
     * @param variables
     * @return
     */
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {
        
        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String ageCode = null;
        String topographyCode = null;

        int ageNumber = 0;
        int topographyNumber = 0;

        try {
            ageCode = variables.get(Globals.StandardVariableNames.Age).toString();
            ageNumber = Integer.parseInt(ageCode);
            topographyCode = variables.get(Globals.StandardVariableNames.Topography).toString();
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

        int topographyGroup = topographyNumber / 10;

        if (ageNumber < 5 && (topographyGroup == 53 || topographyGroup == 61)) {
            result.setMessage(ageCode + ", " + topographyCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
            return result;
        }

        if (ageNumber < 20 &&
                (topographyGroup == 15 || topographyGroup == 19 || topographyGroup == 20 ||
                topographyGroup == 21 || topographyGroup == 23 || topographyGroup == 24 ||
                topographyGroup == 25 || topographyNumber == 384 || topographyGroup == 50 ||
                topographyGroup == 53 || topographyGroup == 54 || topographyGroup == 55)) {
            result.setMessage(ageCode + ", " + topographyCode);
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
