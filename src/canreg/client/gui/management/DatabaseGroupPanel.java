package canreg.client.gui.management;

import canreg.common.DatabaseGroupsListElement;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseGroupPanel extends DatabaseElementsPanel {
    @Action
    public void addAction() {
        add(new DatabaseGroupsListElement("Default", -1, -1));
    }
}
