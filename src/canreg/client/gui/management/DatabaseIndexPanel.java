package canreg.client.gui.management;

import canreg.common.DatabaseElement;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import java.awt.Color;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseIndexPanel extends DatabaseElementsPanel {

    @Action
    @Override
    public void addAction() {
        DatabaseIndexesListElement dile = new DatabaseIndexesListElement("New Index");
        dile.setDatabaseTableName(Globals.PATIENT_TABLE_NAME);
        dile.setVariablesInIndex(new DatabaseVariablesListElement[0]);
        add(dile);
    }

    @Override
    public boolean removable(DatabaseElement dbe) {
        return true;
    }

    @Override
    public boolean visible(DatabaseElement element) {
        return true;
    }

    @Override
    public Color colorize(DatabaseElement element) {
        return null;
    }

    @Override
    public DatabaseIndexesListElement[] getDatabaseElements() {
        DatabaseIndexesListElement[] elements = new DatabaseIndexesListElement[elementPanelsSet.size()];
        int i = 0;
        for (DatabaseElementPanel elementPanel : elementPanelsSet) {
            elements[i] = (DatabaseIndexesListElement) elementPanel.getDatabaseElement();
            i++;
        }
        return elements;
    }
}

