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
 *         Patricio Ezequiel Carranza, patocarranza@gmail.com
 */

package canreg.client.gui.dataentry2.components;

import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author ervikm, patri_000
 */
public class TextAreaVariableEditorPanel extends VariableEditorPanel {

    private JTextArea textArea;

    
    public TextAreaVariableEditorPanel(ActionListener listener) {
        super(listener);
    }
    
    @Override
    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement) {
        this.databaseListElement = databaseListElement;
        setVariableName(databaseListElement.getFullName());
        textArea = new JTextArea();
        textArea.setMinimumSize(new Dimension(20, 60));
        this.setMinimumSize(new Dimension(20, 60));
        this.setPreferredSize(new Dimension(200, 60));
        mainSplitPane.setMinimumSize(new Dimension(20, 60));
        this.setMaximumSize(new Dimension(32767, 60));
        mainSplitPane.setMaximumSize(new Dimension(2147483647, 60));
        
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setMinimumSize(new Dimension(20, 60));
        
        mainSplitPane.remove(mainSplitPane.getRightComponent());
        mainSplitPane.setRightComponent(scroll);
        
        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_AUTOMATIC_STRING)) {
            textArea.setFocusable(false);
            textArea.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) 
            textArea.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);

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
        
        
        textArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textAreaKeyTyped(evt);
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
        
        this.revalidate();
        this.repaint();
    }

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

    @Override
    public Object getValue() {
        String valueString = textArea.getText().trim();
        return valueString;
    }
    
    protected void textAreaKeyTyped(java.awt.event.KeyEvent evt) {
        this.checkForChanges();
        if (evt.getKeyChar() == '+') {
            evt.consume();
            transferFocusToPrevious();
        }
    }
}
