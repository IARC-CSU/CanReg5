package canreg.common.conversions;

import canreg.common.Globals;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class ConversionResult implements Serializable {
    private Globals.StandardVariableNames variableName;
    private Object value;
    
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

    public Globals.StandardVariableNames getVariableName() {
        return variableName;
    }

    public void setVariableName(Globals.StandardVariableNames variableName) {
        this.variableName = variableName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
