package canreg.common.qualitycontrol;

import canreg.common.Globals;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public interface CheckInterface {
    public Globals.StandardVariableNames[] getVariablesNeeded();
    public Checks.CheckNames getCheckName();
    public CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables);
}
