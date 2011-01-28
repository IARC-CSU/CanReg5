package canreg.common.qualitycontrol;

import canreg.common.Globals;
import canreg.common.qualitycontrol.Checker.CheckNames;
import java.util.Map;

/**
 *
 * @author 
 * Based on the "age histology"-check of Andy Cooke
 */
public class CheckAgeMorphology extends CheckInterface {

    /**
     * 
     */
    public Checker.CheckNames checkName = Checker.CheckNames.AgeMorphology;
    /**
     * 
     */
    public static Globals.StandardVariableNames[] variablesNeeded = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.Morphology,};

    /**
     * 
     * @return
     */
    @Override
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    /**
     * 
     * @param variables
     * @return
     */
    @Override
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables) {

        CheckResult result = new CheckResult();
        result.setCheckName(checkName.toString());

        String ageCode = null;
        int ageNumber = 0;

        String morphologyCode = null;
        int morphologyNumber = 0;

        boolean ok = true;

        try {
            result.addVariableInvolved(Globals.StandardVariableNames.Age);
            ageCode = variables.get(Globals.StandardVariableNames.Age).toString();
            ageNumber = Integer.parseInt(ageCode);
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
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(CheckResult.ResultCode.Invalid);
            result.setMessage("Not a number");
            return result;
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(CheckResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
            return result;
        }
        if (ageNumber <= 25 &&
                (morphologyNumber == 9730 || morphologyNumber == 9823 || morphologyNumber == 9890)) {
            ok = false;
        }
        if (ageNumber <= 15 && morphologyNumber == 9863) {
            ok = false;
        }
        if (ageNumber >= 15 &&
                (morphologyNumber == 8910 || morphologyNumber == 8960 || morphologyNumber == 8961 ||
                morphologyNumber == 8962 || morphologyNumber == 8970 || morphologyNumber == 8981 ||
                morphologyNumber == 8991 || morphologyNumber == 9072 || morphologyNumber == 9470 ||
                morphologyNumber == 9687 || morphologyNumber == 9750)) {
            ok = false;
        }
        if (ok) {
            result.setMessage("");
            result.setResultCode(CheckResult.ResultCode.OK);
            return result;
        } else {
            result.setMessage(ageCode + ", " + morphologyCode);
            result.setResultCode(CheckResult.ResultCode.Rare);
            return result;
        }
    }

    /**
     * 
     * @return
     */
    @Override
    public CheckNames getCheckName() {
        return checkName;
    }
}
