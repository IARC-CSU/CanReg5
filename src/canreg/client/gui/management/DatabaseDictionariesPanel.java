package canreg.client.gui.management;

import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseElement;
import java.awt.Color;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseDictionariesPanel extends DatabaseElementsPanel {

    @Action
    public void addAction() {
        add(new DatabaseDictionaryListElement());
    }

    @Override
    public DatabaseDictionaryListElement[] getDatabaseElements() {
        DatabaseDictionaryListElement[] elements = new DatabaseDictionaryListElement[elementPanelsSet.size()];
        int i = 0;
        for (DatabaseElementPanel elementPanel : elementPanelsSet) {
            elements[i] = (DatabaseDictionaryListElement) elementPanel.getDatabaseElement();
            elements[i].setDictionaryID(i);
            i++;
        }
        return elements;
    }

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
}
