package canreg.common.qualitycontrol;

import canreg.common.Globals;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class CheckSexTopography implements CheckInterface {

    public static Checker.CheckNames checkName = Checker.CheckNames.SexTopography;
    
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Topography
    };

    public static int maleCode = 1;
    public static int femaleCode = 2;
    
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {

        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String sexCode = null;
        String topographyCode = null;

        int sexNumber = 0;
        int topographyNumber = 0;

        try {
            sexCode = variables.get(Globals.StandardVariableNames.Sex).toString();
            sexNumber = Integer.parseInt(sexCode);
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

        int TopGroup = topographyNumber / 10;
        if (((sexNumber == maleCode) && (TopGroup >= 51 && TopGroup <= 58))
                || ((sexNumber == femaleCode) && (TopGroup >= 60 && TopGroup <= 63))) {
            result.setMessage(sexCode + ", " + topographyCode);
            result.setResultCode(CheckResult.ResultCode.Invalid);
            return result;
        }

        result.setMessage("");
        result.setResultCode(CheckResult.ResultCode.OK);
        return result;
    }

    public CheckNames getCheckName() {
        return checkName;
    }
}
