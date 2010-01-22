package canreg.client.gui.management;

import canreg.common.DatabaseDictionaryListElement;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseDictionaryPanel extends DatabaseElementsPanel {
    @Action
    public void addAction() {
        add(new DatabaseDictionaryListElement());
    }
}