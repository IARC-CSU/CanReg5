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

/*
 * VariableEditorPanel.java
 *
 * Created on 29 July 2008, 15:29
 */
package canreg.client.gui.dataentry2.components;

import canreg.client.gui.components.*;
import canreg.client.gui.dataentry2.RecordEditor;
import canreg.client.gui.tools.MaxLengthDocument;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.logging.*;
import javax.swing.event.DocumentListener;

/**
 *
 * @author  ervikm, patri_000
 */
public class VariableEditorPanel extends javax.swing.JPanel 
        implements ActionListener, VariableEditorPanelInterface {
   
    protected DatabaseVariablesListElement databaseListElement;  
    protected int maxLength;   
    Object initialValue = null;
    protected boolean mandatory;    
    protected boolean hasChanged = false;
    protected ActionListener listener;

    public VariableEditorPanel() {
        initComponents();
    }
    
    public VariableEditorPanel(ActionListener listener) {
        initComponents();
        this.listener = listener;
        hasChanged = false;
    }    
   
    @Override
    public String getKey() {
        return databaseListElement.getDatabaseVariableName();
    }

    @Override
    public boolean isFilledOK() {
        boolean filledOK = false;
        if (mandatory) {
            if (databaseListElement.getDictionary() != null && 
                codeTextField.getText().trim().length() < databaseListElement.getDictionary().getFullDictionaryCodeLength()) 
                filledOK = false;
            else if (getValue() != null)
                filledOK = getValue().toString().trim().length() > 0;
            else
                filledOK = false;            
        } else 
            filledOK = true;
        
        return filledOK;
    }

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }
    
    @Override
    public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        codeTextField.addPropertyChangeListener(propertyChangeListener);
    }
    
    @Override
    public void setDocumentListener(DocumentListener listener) {
        codeTextField.getDocument().addDocumentListener(listener);
    }

    @Override
    public void setResultCode(ResultCode resultCode) {
        switch (resultCode) {
            case Missing:
                codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
                break;
            case Query:
                codeTextField.setBackground(VARIABLE_QUERY_COLOR);
                break;
            case Rare:
                codeTextField.setBackground(VARIABLE_RARE_COLOR);
                break;
            case Invalid:
                codeTextField.setBackground(VARIABLE_INVALID_COLOR);
                break;
            case OK:
                codeTextField.setBackground(VARIABLE_OK_COLOR);
                break;
        }
        codeTextField.setToolTipText(resultCode.toString());
    }

    @Override
    public void setSaved() {
        initialValue = getValue();
        hasChanged = false;
    }
    
    protected void setVariableName(String variableName) {
        variableNameLabel.setText(variableName);
    }

    @Override
    public void setInitialValue(String value) {
        ActionListener tempListener = listener;
        listener = null;
        setValue(value);
        initialValue = getValue();
        listener = tempListener;
    }
    
    @Override
    public synchronized void setValue(String value) {
        codeTextField.setText(value);
        updateFilledInStatusColor();
    }

    @Override
    public void updateFilledInStatusColor() {
        if (!isFilledOK())
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
        else 
            codeTextField.setBackground(VARIABLE_OK_COLOR);        
    }
    
    @Override
    public synchronized Object getValue() {
        Object valueObject = null;
        String valueString = codeTextField.getText();
        if(databaseListElement.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
            if(valueString.trim().length() > 0) {
                try {
                    valueObject = Integer.parseInt(valueString.trim());
                } catch (NumberFormatException numberFormatException) {
                    Logger.getLogger(VariableEditorPanel.class.getName())
                            .log(Level.WARNING, databaseListElement.getShortName() 
                                 + " " + valueString, numberFormatException);
                }
            } else 
                valueObject = null;            
        } else 
            valueObject = valueString;
        
        return valueObject;
    }
    
    @Override
    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement) {
        this.databaseListElement = databaseListElement;
        setVariableName(databaseListElement.getFullName());
        
        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_AUTOMATIC_STRING)) {
            codeTextField.setFocusable(false);
            codeTextField.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) 
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);        

        setMaximumLength(databaseListElement.getVariableLength());
        mandatory = databaseListElement.getFillInStatus().equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING);
        codeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                componentFocusGained(evt);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                componentFocusLost(evt);
            }
        });
    }    
    
    protected void setMaximumLength(int length) {
        this.maxLength = length;
        if (this.maxLength > 0) 
            codeTextField.setDocument(new MaxLengthDocument(maxLength, this));        
    }
    
    protected void componentFocusGained(java.awt.event.FocusEvent evt) {
        Component focusedComponent = evt.getComponent();
        Point point = focusedComponent.getLocation();
        point.y += 42; // Trial and error
        this.scrollRectToVisible(new Rectangle(point));
    }

    protected void componentFocusLost(FocusEvent evt) {
        // if (!getValue().toString().equals(initialValue)) {
        //     hasChanged = true;
        // }
    }

    @Override
    public void removeListener() {
        listener = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.MAX_LENGTH_ACTION_STRING)) {
            updateFilledInStatusColor();
        } else if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.CHANGED_ACTION_STRING)) {
            try {
                //lookUpAndSetDescription();
                Object currentValue = getValue();
                if (listener != null && 
                     ((currentValue != null && !currentValue.equals(initialValue)) ||
                      (initialValue != null && !initialValue.equals(currentValue)))  ) {
                    hasChanged = true;
                    listener.actionPerformed(new ActionEvent(this, 0, CHANGED_STRING));
                }
            } catch (NullPointerException ne) {
                
            }
            updateFilledInStatusColor();
        }
        if (e.getActionCommand().equalsIgnoreCase(DictionaryElementChooser.OK_ACTION)) {
            updateFilledInStatusColor();
            listener.actionPerformed(new ActionEvent(this, 0, RecordEditor.REQUEST_FOCUS));
            transferFocusToNext();
        }
    }

    protected void transferFocusToNext() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
    }
    
    protected void codeTextFieldMousePressed(java.awt.event.MouseEvent evt) {                                           
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(codeTextField, evt);
    }                                          

    protected void codeTextFieldMouseReleased(java.awt.event.MouseEvent evt) {                                            
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(codeTextField, evt);
    } 

    protected void codeTextFieldKeyTyped(java.awt.event.KeyEvent evt) {
        if(evt.getKeyChar() == KeyEvent.VK_ENTER) 
            transferFocusToNext();        
    }
    
    protected void codeTextFieldFocusLost(java.awt.event.FocusEvent evt) {
        updateFilledInStatusColor();        
    }
 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();
        variableNameLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        codeTextField = new javax.swing.JTextField();

        setMaximumSize(new java.awt.Dimension(32767, 24));
        setMinimumSize(new java.awt.Dimension(40, 24));
        setName("Form"); // NOI18N

        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerLocation(150);
        mainSplitPane.setResizeWeight(0.3);
        mainSplitPane.setMinimumSize(new java.awt.Dimension(20, 20));
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        variableNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(VariableEditorPanel.class);
        variableNameLabel.setText(resourceMap.getString("variableNameLabel.text")); // NOI18N
        variableNameLabel.setMaximumSize(new java.awt.Dimension(67, 24));
        variableNameLabel.setMinimumSize(new java.awt.Dimension(20, 14));
        variableNameLabel.setName("variableNameLabel"); // NOI18N
        variableNameLabel.setPreferredSize(new java.awt.Dimension(67, 24));
        mainSplitPane.setLeftComponent(variableNameLabel);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        codeTextField.setText(resourceMap.getString("codeTextField.text")); // NOI18N
        codeTextField.setMinimumSize(new java.awt.Dimension(20, 20));
        codeTextField.setName("codeTextField"); // NOI18N
        codeTextField.setPreferredSize(new java.awt.Dimension(100, 20));
        codeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                codeTextFieldFocusLost(evt);
            }
        });
        codeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codeTextFieldKeyTyped(evt);
            }
        });
        jPanel1.add(codeTextField);

        mainSplitPane.setRightComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JTextField codeTextField;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JSplitPane mainSplitPane;
    protected javax.swing.JLabel variableNameLabel;
    // End of variables declaration//GEN-END:variables
        
}
