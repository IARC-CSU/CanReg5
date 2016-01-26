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
package canreg.client.gui.components;

import canreg.client.CanRegClientApp;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.dataentry.RecordEditor;
import canreg.client.gui.tools.MaxLengthDocument;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;

/**
 *
 * @author  ervikm
 */
public class VariableEditorPanel extends javax.swing.JPanel implements ActionListener, VariableEditorPanelInterface {

    /**
     * 
     */
    protected DatabaseVariablesListElement databaseListElement;
    /**
     * 
     */
    protected Map<String, DictionaryEntry> possibleValuesMap = null;
    /**
     * 
     */
    protected int maxLength;
    /**
     * 
     */
    private Dictionary dictionary;
    Object initialValue = null;
    protected boolean mandatory;
    private boolean hasChanged = false;
    protected ActionListener listener;
    DictionaryElementChooser dictionaryElementChooser;

    /** Creates new form VariableEditorPanel */
    public VariableEditorPanel(ActionListener listener) {
        initComponents();
        this.listener = listener;
        hasChanged = false;
    }

    /**
     * 
     * @return
     */
    @Override
    public String getKey() {
        return databaseListElement.getDatabaseVariableName();
    }

    @Override
    public boolean isFilledOK() {
        boolean filledOK = false;
        if (mandatory) {
            if (databaseListElement.getDictionary() != null && codeTextField.getText().trim().length() < databaseListElement.getDictionary().getFullDictionaryCodeLength()) {
                filledOK = false;
            } else if (getValue() != null) {
                filledOK = getValue().toString().trim().length() > 0;
            } else {
                filledOK = false;
            }
        } else {
            filledOK = true;
        }
        return filledOK;
    }

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    /**
     * 
     * @param aThis
     */
    @Override
    public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        codeTextField.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * 
     * @param listener
     */
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        variableNameLabel = new javax.swing.JLabel();
        splitPane1 = new javax.swing.JSplitPane();
        splitPane2 = new javax.swing.JSplitPane();
        categoryTextField = new javax.swing.JTextField();
        descriptionTextField = new javax.swing.JTextField();
        codeTextField = new javax.swing.JTextField();

        setName("Form"); // NOI18N

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        variableNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(VariableEditorPanel.class);
        variableNameLabel.setText(resourceMap.getString("variableNameLabel.text")); // NOI18N
        variableNameLabel.setName("variableNameLabel"); // NOI18N
        jSplitPane1.setLeftComponent(variableNameLabel);

        splitPane1.setDividerLocation(150);
        splitPane1.setResizeWeight(0.5);
        splitPane1.setFocusable(false);
        splitPane1.setName("splitPane1"); // NOI18N

        splitPane2.setResizeWeight(0.5);
        splitPane2.setFocusable(false);
        splitPane2.setName("splitPane2"); // NOI18N

        categoryTextField.setEditable(false);
        categoryTextField.setText(resourceMap.getString("categoryTextField.text")); // NOI18N
        categoryTextField.setFocusable(false);
        categoryTextField.setName("categoryTextField"); // NOI18N
        categoryTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickHandler(evt);
            }
        });
        categoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryTextFieldActionPerformed(evt);
            }
        });
        splitPane2.setLeftComponent(categoryTextField);

        descriptionTextField.setEditable(false);
        descriptionTextField.setText(resourceMap.getString("descriptionTextField.text")); // NOI18N
        descriptionTextField.setFocusable(false);
        descriptionTextField.setName("descriptionTextField"); // NOI18N
        descriptionTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                descriptionTextFieldMouseClicked(evt);
            }
        });
        descriptionTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptionTextFieldActionPerformed(evt);
            }
        });
        splitPane2.setRightComponent(descriptionTextField);

        splitPane1.setRightComponent(splitPane2);

        codeTextField.setText(resourceMap.getString("codeTextField.text")); // NOI18N
        codeTextField.setName("codeTextField"); // NOI18N
        codeTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                codeTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                codeTextFieldMouseReleased(evt);
            }
        });
        codeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeTextFieldActionPerformed1(evt);
            }
        });
        codeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                codeTextFieldActionPerformed(evt);
            }
        });
        codeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codeTextFieldKeyTyped(evt);
            }
        });
        splitPane1.setLeftComponent(codeTextField);

        jSplitPane1.setRightComponent(splitPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void mouseClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickHandler

    showDictionaryChooser();


}//GEN-LAST:event_mouseClickHandler

private void categoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_categoryTextFieldActionPerformed

private void codeTextFieldActionPerformed(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codeTextFieldActionPerformed
    try {
        lookUpAndSetDescription();
        updateFilledInStatusColor();
    } catch (NullPointerException e) {
        codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
        descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Error")
                + ": "
                + codeTextField.getText()
                + " "
                + java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("_is_not_a_valid_dictionary_code."));
        // JOptionPane.showInternalMessageDialog(this, codeTextField.getText() + " " + java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("_is_not_a_valid_dictionary_code."), java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Error"), JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_codeTextFieldActionPerformed

private void descriptionTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptionTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_descriptionTextFieldActionPerformed

private void descriptionTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_descriptionTextFieldMouseClicked
    mouseClickHandler(evt);
}//GEN-LAST:event_descriptionTextFieldMouseClicked

private void codeTextFieldActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTextFieldActionPerformed1
    //
}//GEN-LAST:event_codeTextFieldActionPerformed1

private void codeTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_codeTextFieldMousePressed
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(codeTextField, evt);
}//GEN-LAST:event_codeTextFieldMousePressed

private void codeTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_codeTextFieldMouseReleased
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(codeTextField, evt);
}//GEN-LAST:event_codeTextFieldMouseReleased

private void codeTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codeTextFieldKeyTyped
    if (dictionary != null && evt.getKeyChar() == '?') {
        showDictionaryChooser();
    } else if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
        transferFocusToNext();
    }
}//GEN-LAST:event_codeTextFieldKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JTextField categoryTextField;
    protected javax.swing.JTextField codeTextField;
    protected javax.swing.JTextField descriptionTextField;
    private javax.swing.JSplitPane jSplitPane1;
    protected javax.swing.JSplitPane splitPane1;
    protected javax.swing.JSplitPane splitPane2;
    private javax.swing.JLabel variableNameLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * 
     * @param variableName
     */
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

    /**
     * 
     * @param value
     */
    @Override
    public synchronized void setValue(String value) {
        codeTextField.setText(value);
        try {
            lookUpAndSetDescription();
        } catch (NullPointerException e) {
            descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
        }
        updateFilledInStatusColor();
    }

    private void lookUpAndSetDescription() throws NullPointerException {
        descriptionTextField.setText("");
        categoryTextField.setText("");
        if (codeTextField.getText().trim().length() > 0) {
            if (possibleValuesMap != null) {
                try {
                    if (dictionary.isCompoundDictionary() && codeTextField.getText().length() >= dictionary.getCodeLength()) {
                        categoryTextField.setText(possibleValuesMap.get(
                                codeTextField.getText().substring(0, dictionary.getCodeLength())).getDescription());
                    }
                    if (dictionary.isCompoundDictionary()) {
                        if (codeTextField.getText().length() == dictionary.getFullDictionaryCodeLength()) {
                            descriptionTextField.setText(possibleValuesMap.get(codeTextField.getText()).getDescription());
                        }
                    } else {
                        descriptionTextField.setText(possibleValuesMap.get(codeTextField.getText()).getDescription());
                    }
                } catch (NullPointerException e) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void updateFilledInStatusColor() {
        if (!isFilledOK()) {
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
        } else {
            codeTextField.setBackground(VARIABLE_OK_COLOR);
        }
    }

    /**
     * 
     * @return
     */
    @Override
    public synchronized Object getValue() {
        Object valueObject = null;
        String valueString = codeTextField.getText();
        if (databaseListElement.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
            if (valueString.trim().length() > 0) {
                try {
                    valueObject = Integer.parseInt(valueString.trim());
                } catch (NumberFormatException numberFormatException) {
                    // valueObject = -1;
                    Logger.getLogger(VariableEditorPanel.class.getName()).log(Level.WARNING, databaseListElement.getShortName() + " " + valueString, numberFormatException);
                }
            } else {
                valueObject = null;
            }
        } else {
            valueObject = valueString;
        }
        return valueObject;
    }

    /**
     * 
     * @param databaseListElement
     */
    @Override
    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement) {
        this.databaseListElement = databaseListElement;
        setVariableName(databaseListElement.getFullName());
        if (databaseListElement.getDictionaryID() < 0) {
            splitPane1.remove(1);
        }
        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_AUTOMATIC_STRING)) {
            codeTextField.setFocusable(false);
            codeTextField.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
        }

        // String variableType = databaseListElement.getVariableType();
        descriptionTextField.setVisible(false);
        categoryTextField.setVisible(false);
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

    /**
     * 
     * @param dictionary
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
        if (dictionary != null) {
            if (dictionary.isCompoundDictionary()) {
                categoryTextField.setVisible(true);
            }
            descriptionTextField.setVisible(true);
            setPossibleValues(dictionary.getDictionaryEntries());
        }
    }

    private void setPossibleValues(Map<String, DictionaryEntry> possibleValues) {
        this.possibleValuesMap = possibleValues;
        DictionaryEntry m;
        if (possibleValuesMap == null) {
            splitPane2.setVisible(false);
        } else {
            Object value = getValue();
            if (value != null) {
                m = possibleValuesMap.get(value.toString());
                if (m != null) {
                    descriptionTextField.setText(m.getDescription());
                }
            }
        }
    }

    /**
     * 
     * @param length
     */
    protected void setMaximumLength(int length) {
        this.maxLength = length;
        if (this.maxLength > 0) {
            codeTextField.setDocument(new MaxLengthDocument(maxLength, this));
        }
    }

    /**
     * 
     * @param evt
     */
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
            try {
                lookUpAndSetDescription();
            } catch (NullPointerException ne) {
                descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
            }
            updateFilledInStatusColor();
        } else if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.CHANGED_ACTION_STRING)) {
            try {
                lookUpAndSetDescription();
                Object currentValue = getValue();
                if (listener != null && ((currentValue != null && !currentValue.equals(initialValue)) || (initialValue != null && !initialValue.equals(currentValue)))) {
                    hasChanged = true;
                    listener.actionPerformed(new ActionEvent(this, 0, CHANGED_STRING));
                }
            } catch (NullPointerException ne) {
                // descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
            }
            updateFilledInStatusColor();
        }
        if (e.getActionCommand().equalsIgnoreCase(DictionaryElementChooser.OK_ACTION)) {
            codeTextField.setText(dictionaryElementChooser.getSelectedElement().getCode());
            try {
                lookUpAndSetDescription();
            } catch (NullPointerException ne) {
                descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
            }
            updateFilledInStatusColor();
            listener.actionPerformed(new ActionEvent(this, 0, RecordEditor.REQUEST_FOCUS));
            // setFocus();
            transferFocusToNext();
        }
    }

    private void showDictionaryChooser() {
        if (databaseListElement.getVariableType().equalsIgnoreCase("dict")) {
            if (possibleValuesMap == null) {
                JOptionPane.showInternalMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Empty_dictionary."), java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Warning"), JOptionPane.WARNING_MESSAGE);
            } else {
                String oldValue = getValue().toString();
                DictionaryEntry oldSelection = possibleValuesMap.get(oldValue);
                if (dictionaryElementChooser == null) {
                    dictionaryElementChooser = new DictionaryElementChooser(this);
                } else {
                    dictionaryElementChooser.setFirstPass();
                }
                dictionaryElementChooser.setDictionary(dictionary);
                dictionaryElementChooser.setSelectedElement(oldSelection);

                CanRegClientView.showAndPositionInternalFrame(
                        CanRegClientApp.getApplication().getDesktopPane(),
                        dictionaryElementChooser);
            }
        } else {
            // Do nothing
            // This should never happen...
        }
    }

    private void setFocus() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                codeTextField.setRequestFocusEnabled(true);
                codeTextField.requestFocus();
            }
        });
    }

    protected void transferFocusToNext() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                codeTextField.setRequestFocusEnabled(true);
                codeTextField.requestFocus();
                codeTextField.transferFocus();
            }
        });
    }
}
