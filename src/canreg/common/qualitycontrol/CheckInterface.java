package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public abstract class CheckInterface {
    protected Map<StandardVariableNames, DatabaseVariablesListElement> variableListElementMap;

    /**
     * 
     * @return the variables needed to perform this check.
     */
    public abstract Globals.StandardVariableNames[] getVariablesNeeded();

    /**
     *  This is just a mapping from the standard variable names to the names used in the CanReg database.
     */
    public void setVariableListElementsMap(Map<Globals.StandardVariableNames, DatabaseVariablesListElement> variableListElementMap) {
        this.variableListElementMap = variableListElementMap;
    }

    /**
     * 
     * @return a standard name of this check.
     */
    public abstract Checker.CheckNames getCheckName();
    /**
     * 
     * @param variables
     * @return
     */
    public abstract CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables);
}
