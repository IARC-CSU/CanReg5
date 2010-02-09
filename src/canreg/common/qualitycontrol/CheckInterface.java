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
     * @return
     */
    public abstract Globals.StandardVariableNames[] getVariablesNeeded();

    /**
     *
     */
    public void setVariableListElementsMap(Map<Globals.StandardVariableNames, DatabaseVariablesListElement> variableListElementMap) {
        this.variableListElementMap = variableListElementMap;
    }

    /**
     * 
     * @return
     */
    public abstract Checker.CheckNames getCheckName();
    /**
     * 
     * @param variables
     * @return
     */
    public abstract CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables);
}
