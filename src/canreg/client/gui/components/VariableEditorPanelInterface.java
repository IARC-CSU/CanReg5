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

package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import java.beans.PropertyChangeListener;
import javax.swing.event.DocumentListener;

/**
 *
 * @author ervikm
 */
public interface VariableEditorPanelInterface {

    public static java.awt.Color MANDATORY_VARIABLE_MISSING_COLOR = java.awt.Color.PINK;
    public static java.awt.Color VARIABLE_INVALID_COLOR = java.awt.Color.PINK;
    public static java.awt.Color VARIABLE_QUERY_COLOR = java.awt.Color.GREEN;
    public static java.awt.Color VARIABLE_RARE_COLOR = java.awt.Color.YELLOW;
    public static java.awt.Color VARIABLE_OK_COLOR = java.awt.SystemColor.text;
    public static String CHANGED_STRING = "Changed";

    public String getKey();

    public boolean isFilledOK();

    public void setResultCode(ResultCode resultCode);

    public void setSaved();

    public void setInitialValue(String value);

    public boolean hasChanged();

    public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    public void setDocumentListener(DocumentListener listener);

    public void setValue(String value);

    public void updateFilledInStatusColor();

    public Object getValue();

    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement);

    public void removeListener();
}
