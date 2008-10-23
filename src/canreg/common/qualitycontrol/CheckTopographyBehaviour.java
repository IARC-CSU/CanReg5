package canreg.common.qualitycontrol;

import canreg.common.Globals;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.util.Map;

/**
 *
 * @author ervikm
 * 
 * DepEdits check #25
 */
public class CheckTopographyBehaviour implements CheckInterface {

    public Checker.CheckNames checkName = Checker.CheckNames.TopographyBehaviour;
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Topography,
        Globals.StandardVariableNames.Behaviour
    };

    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {

        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String behaviourCode = null;
        String topographyCode = null;

        int behaviourNumber = 0;
        int topographyNumber = 0;

        try {
            behaviourCode = variables.get(Globals.StandardVariableNames.Behaviour).toString();
            behaviourNumber = Integer.parseInt(behaviourCode);
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

        if ((TopGroup == 40 // Bones
                || TopGroup == 41 // Skull
                || TopGroup == 42 // Blood, Marrow, spleen
                || TopGroup == 47 // Perif Nervous system
                || TopGroup == 49 // soft tissue
                || (TopGroup >= 70 && TopGroup <= 72)) // Meninge,Brain,Nerves
                && behaviourNumber == 2) {
            result.setMessage(topographyCode + ", " + behaviourCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
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
