package canreg.client.gui.management;

import canreg.common.DatabaseIndexesListElement;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseIndexPanel extends DatabaseElementsPanel {
    @Action
    public void addAction() {
        add(new DatabaseIndexesListElement("New Index"));
    }
}

