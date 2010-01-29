package canreg.client.gui.management;

import canreg.common.DatabaseElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import java.awt.Color;
import java.util.Arrays;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseVariablesPanel extends DatabaseElementsPanel {

    @Action
    public void addAction() {
        add(new DatabaseVariablesListElement(Globals.PATIENT_TABLE_NAME, 1, "Default name", Globals.VARIABLE_TYPE_ALPHA_NAME));
    }

    @Override
    public DatabaseElement[] getDatabaseElements() {
        DatabaseVariablesListElement[] elements = new DatabaseVariablesListElement[elementPanelsSet.size()];
        int i = 0;
        for (DatabaseElementPanel elementPanel : elementPanelsSet) {
            elements[i] = (DatabaseVariablesListElement) elementPanel.getDatabaseElement();
            elements[i].setDatabaseTableVariableID(i);
            elements[i].setYPos(i * 100);
            i++;
        }
        return elements;
    }

    public boolean removable(DatabaseElement dbe) {
        DatabaseVariablesListElement dve = (DatabaseVariablesListElement) dbe;
        return Arrays.asList(
                ModifyDatabaseStructureInternalFrame.listOfAutomaticlyGeneratedVariables).indexOf(Globals.StandardVariableNames.valueOf(dve.getStandardVariableName())) < 0;
    }

    public boolean visible(DatabaseElement dbe) {
        DatabaseVariablesListElement davaliel = (DatabaseVariablesListElement) dbe;
        return (davaliel.getGroupID() > 0);
    }

    @Override
    public Color colorize(DatabaseElement element) {
        DatabaseVariablesListElement davaliel = (DatabaseVariablesListElement) element;
        Color color = null;
        if (davaliel.getStandardVariableName() != null) {
            color = Color.yellow;
        }
        return color;
    }
}
