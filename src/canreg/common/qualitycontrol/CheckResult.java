package canreg.common.qualitycontrol;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class CheckResult implements Serializable {
    private boolean passed = false;
    private String checkName = "Check";
    private String message ="";
    private ResultCode resultCode;

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
        NotDone,  // Relevant variable not declared
        /**
         * 
         */
        OK,
        /**
         * 
         */
        Query ,   // Warning, or other problem, tho' not invalid
        /**
         * 
         */
        Rare,     // Rare, possibly an error
        /**
         * 
         */
        Missing,   // Relevant variable missing value
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

    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString(){
        return checkName + ": " + resultCode + " - " + message;
    }
}
