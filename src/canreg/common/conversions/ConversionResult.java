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
    public Globals.StandardVariableNames getVariableName() {
        return variableName;
    }

    /**
     * 
     * @param variableName
     */
    public void setVariableName(Globals.StandardVariableNames variableName) {
        this.variableName = variableName;
    }

    /**
     * 
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * 
     * @param value
     */
    public void setValue(Object value) {
        this.value = value;
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
}
