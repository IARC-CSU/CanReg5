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
        DatabaseVariablesListElement variable = new DatabaseVariablesListElement(Globals.PATIENT_TABLE_NAME, 1, "", Globals.VARIABLE_TYPE_ALPHA_NAME);
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
        if (dve.getStandardVariableName() == null) {
            return true;
        } else {
            return Arrays.asList(
                    ModifyDatabaseStructureInternalFrame.listOfAutomaticlyGeneratedVariables).indexOf(Globals.StandardVariableNames.valueOf(dve.getStandardVariableName())) < 0;
        }
    }

    @Override
    public boolean visible(DatabaseElement dbe) {
        return dbe.userVariable();
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
        if (variableName.trim().length()==0){
            return null;
        }
        for (DatabaseElementPanel elementPanel : elementPanelsSet) {
            element = (DatabaseVariablesListElement) elementPanel.getDatabaseElement();
            if (element.getStandardVariableName() != null && variableName.equalsIgnoreCase(element.getStandardVariableName())) {
                return element;
            }
        }
        return null;
    }
}
