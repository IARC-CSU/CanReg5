/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.client.gui.management.systemeditor;

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
    @Override
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
}
