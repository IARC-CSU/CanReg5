/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

package canreg.client.gui.management.systemeditor;

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
        // groups are always userVariable
        return true;
    }

    @Override
    public Color colorize(DatabaseElement element) {
        return null;
    }

}
