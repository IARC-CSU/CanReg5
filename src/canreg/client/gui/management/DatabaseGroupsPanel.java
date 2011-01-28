package canreg.client.gui.management;

import canreg.common.DatabaseElement;
import canreg.common.DatabaseGroupsListElement;
import java.awt.Color;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseGroupsPanel extends DatabaseElementsPanel {

    @Action
    @Override
    public void addAction() {
        add(new DatabaseGroupsListElement("Default Group Name", -1, -1));
    }

    @Override
    public DatabaseGroupsListElement[] getDatabaseElements() {
        DatabaseGroupsListElement[] elements = new DatabaseGroupsListElement[elementPanelsSet.size()];
        int i = 0;
        for (DatabaseElementPanel elementPanel : elementPanelsSet) {
            elements[i] = (DatabaseGroupsListElement) elementPanel.getDatabaseElement();
            elements[i].setGroupIndex(i);
            elements[i].setGroupPosition(i * 100);
            i++;
        }
        return elements;
    }

    @Override
    public boolean removable(DatabaseElement dbe) {
        return true;
    }

    @Override
    public boolean visible(DatabaseElement dbe) {
        // groups are always visible
        return true;
    }

    @Override
    public Color colorize(DatabaseElement element) {
        return null;
    }
}
