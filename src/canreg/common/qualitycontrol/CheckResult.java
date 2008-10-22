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

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
    
    public enum ResultCode {
        NotDone,  // Relevant variable not declared
	OK,
	Query ,   // Warning, or other problem, tho' not invalid
	Rare,     // Rare, possibly an error
	Missing,   // Relevant variable missing value
	Invalid
 }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString(){
        return checkName + ": " + resultCode + " - " + message;
    }
}
