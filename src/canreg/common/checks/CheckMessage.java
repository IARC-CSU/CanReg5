package canreg.common.checks;

/**
 * Check message.
 */
public class CheckMessage {
    public static final int LEVEL_ERROR = 1;
    private final String variableName;
    private final Object variableValue;
    private final String message;
    /** 1 for an error, 0 for a warning. */
    private int level;

    /**
     * Constructor.
     * @param variableName variableName
     * @param variableValue variableValue
     * @param message message
     * @param isError true for an error, false for a warning
     */
    public CheckMessage(String variableName, Object variableValue, String message, boolean isError) {
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.message = message;
        if(isError) {
            this.level = LEVEL_ERROR;
        }
    }

    /**
     * Getter variableName.
     *
     * @return variableName variableName.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Getter variableValue.
     *
     * @return variableValue variableValue.
     */
    public Object getVariableValue() {
        return variableValue;
    }

    /**
     * Getter message.
     *
     * @return message message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return true if this is an error, false if it is a warning.
     * @return true if this is an error, false if it is a warning.
     */
    public boolean isError() {
        return this.level == LEVEL_ERROR;
    }

    /**
     * Build a short message: "Error: " + message or "Warning: " + message
     * @return String
     */
    public String shortMessage() {
        return (level == LEVEL_ERROR ? "Error: " : "Warning: ") + message;
    }
    
    @Override
    public String toString() {
        return "{level=" + (level == LEVEL_ERROR ? "'error'" : "'warning'") +
                ", variable='" + variableName + '\'' +
                ", value=" + (variableValue instanceof Integer ? variableValue : ("'" + variableValue + '\'') ) +
                ", message='" + message + '\'' +
                '}';
    }
}
