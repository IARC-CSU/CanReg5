package canreg.client.gui.management;

import canreg.common.DatabaseElement;
import canreg.common.DatabaseGroupsListElement;
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
    private DatabaseGroupsListElement defaultGroup;

    @Action
    @Override
    public void addAction() {
        DatabaseVariablesListElement variable = new DatabaseVariablesListElement(Globals.PATIENT_TABLE_NAME, 1, "Default name", Globals.VARIABLE_TYPE_ALPHA_NAME);
        variable.setGroup(defaultGroup);
        add(variable);
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

    @Override
    public boolean removable(DatabaseElement dbe) {
        DatabaseVariablesListElement dve = (DatabaseVariablesListElement) dbe;
        return Arrays.asList(
                ModifyDatabaseStructureInternalFrame.listOfAutomaticlyGeneratedVariables).indexOf(Globals.StandardVariableNames.valueOf(dve.getStandardVariableName())) < 0;
    }

    @Override
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

    public void setDefaultGroup(DatabaseGroupsListElement defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public DatabaseVariablesListElement isThisStandardVariableAlreadyMapped(String variableName) {
        DatabaseVariablesListElement element;
        for (DatabaseElementPanel elementPanel : elementPanelsSet) {
            element = (DatabaseVariablesListElement) elementPanel.getDatabaseElement();
            if (element.getStandardVariableName()!=null && variableName.equalsIgnoreCase(element.getStandardVariableName())){
                return element;
            }
        }
        return null;
    }
}
