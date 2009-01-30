package canreg.common.qualitycontrol;

import canreg.common.Globals;
import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class CheckResult implements Serializable {

    private boolean passed = false;
    private String checkName = "Check";
    private String message = "";
    private ResultCode resultCode;
    private LinkedList<Globals.StandardVariableNames> standardVariablesInvolved;

    public CheckResult() {
        standardVariablesInvolved = new LinkedList<Globals.StandardVariableNames>();
    }

    /**
     * 
     * @return
     */
    public ResultCode getResultCode() {
        return resultCode;
    }

    /**
     * 
     * @param resultCode
     */
    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * 
     */
    public enum ResultCode {

        /**
         * 
         */
        NotDone, // Relevant variable not declared
        /**
         * 
         */
        OK,
        /**
         * 
         */
        Query, // Warning, or other problem, tho' not invalid
        /**
         * 
         */
        Rare, // Rare, possibly an error
        /**
         * 
         */
        Missing, // Relevant variable missing value
        /**
         * 
         */
        Invalid
    }

    /**
     * 
     * @return
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * 
     * @param passed
     */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * 
     * @return
     */
    public String getCheckName() {
        return checkName;
    }

    /**
     * 
     * @param checkName
     */
    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    /**
     * 
     * @return
     */
    public String getMessage() {
        return message;
    }

    public Globals.StandardVariableNames[] getVariablesInvolved() {
        return standardVariablesInvolved.toArray(new Globals.StandardVariableNames[0]);
    }

    public void addVariableInvolved(Globals.StandardVariableNames variableInvolved) {
        standardVariablesInvolved.add(variableInvolved);
    }

    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return checkName + ": " + resultCode + " - " + message;
    }

    public static int compareResultSets(ResultCode resultCodeA, ResultCode resultCodeB) {
        if (resultCodeA == resultCodeB) {
            return 0;
        } else if (decideWorstResultCode(resultCodeA, resultCodeB) == resultCodeA) {
            return 1;
        } else {
            return -1;
        }
    }

    public static ResultCode decideWorstResultCode(ResultCode resultCodeA, ResultCode resultCodeB) {
        ResultCode worstResultCodeFound = resultCodeB;
        if (resultCodeA == CheckResult.ResultCode.OK) {
            worstResultCodeFound = resultCodeB;
        } else if (resultCodeA == CheckResult.ResultCode.Invalid) {
            worstResultCodeFound = CheckResult.ResultCode.Invalid;
        } else if (worstResultCodeFound != CheckResult.ResultCode.Invalid) {
            if (resultCodeA == CheckResult.ResultCode.Query) {
                worstResultCodeFound = CheckResult.ResultCode.Query;
            } else if (worstResultCodeFound != CheckResult.ResultCode.Query) {
                worstResultCodeFound = resultCodeA;
            }
        }
        return worstResultCodeFound;
    }
}
