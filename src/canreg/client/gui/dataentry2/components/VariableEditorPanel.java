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

import canreg.client.gui.components.*;
import canreg.client.gui.dataentry2.RecordEditorMainFrame;
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
 * @author ervikm, patri_000
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
        variableNameLabel.setText(variableName + "  ");
        variableNameLabel.setToolTipText(variableName); 
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
        if (databaseListElement.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
            if (valueString.trim().length() > 0) {
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
        updateFilledInStatusColor();
        if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.MAX_LENGTH_ACTION_STRING)) {
            //Nothing
        } else if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.CHANGED_ACTION_STRING)) {
            this.checkForChanges();
        }
        if (e.getActionCommand().equalsIgnoreCase(DictionaryElementChooser.OK_ACTION)) {            
            listener.actionPerformed(new ActionEvent(this, 0, RecordEditorMainFrame.REQUEST_FOCUS));
            transferFocusToNext();
        }
    }
    
    protected void checkForChanges() {
        try {
            Object currentValue = getValue();
            if (listener != null) {
                if ((currentValue != null && !currentValue.equals(initialValue)) ||
                   (initialValue != null && !initialValue.equals(currentValue))) 
                    hasChanged = true;
                else 
                   hasChanged = false;
                listener.actionPerformed(new ActionEvent(this, 0, CHANGED_STRING));
            }
        } catch(NullPointerException ne) {
            Logger.getLogger(VariableEditorPanel.class.getName())
                    .log(Level.WARNING, "Warning! NPE in VariableEditorPanel.checkForChanges()", ne);
        }
    }
    
    protected void transferFocusToPrevious() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
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
        this.checkForChanges();
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) 
            transferFocusToNext();
        else if (evt.getKeyChar() == '+') {
            evt.consume();
            transferFocusToPrevious();
        }            
    }
    
    protected void codeTextFieldFocusLost(java.awt.event.FocusEvent evt) {
        updateFilledInStatusColor();        
    }
    
    @Override
    public void requestFocus() {
        this.codeTextField.setText("niggaaahhh");
        this.codeTextField.setFocusable(true);
        this.codeTextField.grabFocus();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.variableNameLabel != null ? this.variableNameLabel.getText().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariableEditorPanel other = (VariableEditorPanel) obj;
        if (this.variableNameLabel != other.variableNameLabel && 
            (this.variableNameLabel == null || ! this.variableNameLabel.getText().equals(other.variableNameLabel.getText()))) {
            return false;
        }
        return true;
    }
         
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();
        variableNameLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        codeTextField = new javax.swing.JTextField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));

        setMaximumSize(new java.awt.Dimension(32767, 26));
        setMinimumSize(new java.awt.Dimension(40, 26));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(260, 26));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerLocation(150);
        mainSplitPane.setDividerSize(7);
        mainSplitPane.setMaximumSize(new java.awt.Dimension(2147483647, 26));
        mainSplitPane.setMinimumSize(new java.awt.Dimension(20, 26));
        mainSplitPane.setName("mainSplitPane"); // NOI18N
        mainSplitPane.setPreferredSize(new java.awt.Dimension(250, 26));
        mainSplitPane.setUI(new DottedDividerSplitPane());
        mainSplitPane.setBorder(null);

        variableNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(VariableEditorPanel.class);
        variableNameLabel.setText(resourceMap.getString("variableNameLabel.text")); // NOI18N
        variableNameLabel.setMaximumSize(new java.awt.Dimension(150, 26));
        variableNameLabel.setMinimumSize(new java.awt.Dimension(20, 26));
        variableNameLabel.setName("variableNameLabel"); // NOI18N
        variableNameLabel.setPreferredSize(new java.awt.Dimension(150, 26));
        mainSplitPane.setLeftComponent(variableNameLabel);

        jPanel1.setMaximumSize(new java.awt.Dimension(2147483647, 26));
        jPanel1.setMinimumSize(new java.awt.Dimension(20, 26));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 26));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        codeTextField.setText(resourceMap.getString("codeTextField.text")); // NOI18N
        codeTextField.setMaximumSize(new java.awt.Dimension(2147483647, 26));
        codeTextField.setMinimumSize(new java.awt.Dimension(20, 26));
        codeTextField.setName("codeTextField"); // NOI18N
        codeTextField.setPreferredSize(new java.awt.Dimension(100, 26));
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
        codeTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                codeTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                codeTextFieldMouseReleased(evt);
            }
        });
        jPanel1.add(codeTextField);

        mainSplitPane.setRightComponent(jPanel1);

        add(mainSplitPane);

        filler1.setName("filler1"); // NOI18N
        add(filler1);
    }// </editor-fold>//GEN-END:initComponents
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JTextField codeTextField;
    private javax.swing.Box.Filler filler1;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JSplitPane mainSplitPane;
    protected javax.swing.JLabel variableNameLabel;
    // End of variables declaration//GEN-END:variables
        
}
