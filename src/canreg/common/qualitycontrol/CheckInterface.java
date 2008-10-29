package canreg.common.qualitycontrol;

import canreg.common.Globals;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public interface CheckInterface {
    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded();
    /**
     * 
     * @return
     */
    public Checker.CheckNames getCheckName();
    /**
     * 
     * @param variables
     * @return
     */
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables);
}
