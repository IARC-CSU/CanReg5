package canreg.client.gui.management;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseVariablePanel extends DatabaseElementsPanel {
    @Action
    public void addAction() {
        add(new DatabaseVariablesListElement(Globals.PATIENT_TABLE_NAME, -1, "", Globals.VARIABLE_TYPE_ALPHA_NAME));
    }
}
