/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016  International Agency for Research on Cancer
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

package canreg.client.gui.components;

import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import java.awt.event.ActionListener;
import javax.swing.JTextArea;

/**
 *
 * @author ervikm
 */
public class TextFieldVariableEditorPanel extends VariableEditorPanel {

    private JTextArea textArea;

    public TextFieldVariableEditorPanel(ActionListener listener) {
        super(listener);
    }

    /**
     * 
     * @param databaseListElement
     */
    @Override
    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement) {
        this.databaseListElement = databaseListElement;
        setVariableName(databaseListElement.getFullName());
        textArea = new JTextArea();

        splitPane1.remove(splitPane1.getRightComponent());

        splitPane1.setTopComponent(textArea);

        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_AUTOMATIC_STRING)) {
            textArea.setFocusable(false);
            textArea.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
            textArea.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
        }


        // Not yet ready for text areas
        // setMaximumLength(databaseListElement.getVariableLength());

        textArea.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                componentFocusGained(evt);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                componentFocusLost(evt);
            }
        });

        textArea.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(textArea, evt);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(textArea, evt);
            }
        });
    }

    /**
     * 
     * @param value
     */
    @Override
    public void setValue(String value) {
        initialValue = value;
        if (value.trim().length() == 0) {
            if (databaseListElement.getFillInStatus().equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
                codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            }
            textArea.setText(value);
        } else {
            codeTextField.setBackground(java.awt.SystemColor.text);
            textArea.setText(value);
        }
    }

    /**
     * 
     * @return
     */
    @Override
    public Object getValue() {
        String valueString = textArea.getText().trim();
        return valueString;
    }
}
