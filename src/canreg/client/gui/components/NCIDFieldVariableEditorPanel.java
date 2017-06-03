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

import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 *
 * @author ervikm
 */
public class NCIDFieldVariableEditorPanel extends VariableEditorPanel {

    private JTextArea textArea;

    public NCIDFieldVariableEditorPanel(ActionListener listener) {
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
                textArea.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            }
            textArea.setText(value);
        } else {
            if(!validateNCID(value)){
                textArea.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            }else{
                textArea.setBackground(java.awt.SystemColor.text);
            }
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

    //<ictl.co>
    public  boolean validateNCID(String value) {
        if (value == null || "".equals(value))
            return false;

        String nCID = value;
        if (nCID.length() != 10) {
            return false;
        }

        if ("0000000000".equals(nCID)
                || "1111111111".equals(nCID)
                || "2222222222".equals(nCID)
                || "3333333333".equals(nCID)
                || "4444444444".equals(nCID)
                || "5555555555".equals(nCID)
                || "6666666666".equals(nCID)
                || "7777777777".equals(nCID)
                || "8888888888".equals(nCID)
                || "9999999999".equals(nCID)) {
            return false;
        }

        int[] numArray = new int[10];

        int num3;
        for (num3 = 0; num3 < 10; ++num3) {
            numArray[num3] = Integer.parseInt(nCID.charAt(num3)+"");
        }

        num3 = numArray[0] * 10 + numArray[1] * 9 + numArray[2] * 8 + numArray[3] * 7 + numArray[4] * 6 + numArray[5] * 5 + numArray[6] * 4 + numArray[7] * 3 + numArray[8] * 2;
        int num2 = numArray[9];
        int num4 = num3 - num3 / 11 * 11;

        if((num4 == 0 && num4==num2) || (num4==1 && num2==1) || (num4>1 && num2 == 11 - num4)){
            return true;
        }
        return false;
    }
    //</ictl.co>
}
