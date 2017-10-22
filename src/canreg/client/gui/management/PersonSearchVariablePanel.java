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
/*
 * PersonSearchVariablePanel.java
 *
 * Created on 21 October 2008, 14:08
 */
package canreg.client.gui.management;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.LocalizationHelper;
import canreg.common.PersonSearchVariable;
import canreg.common.qualitycontrol.PersonSearcher.CompareAlgorithms;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.jdesktop.application.Action;

/**
 *
 * @author  ervikm
 */
public class PersonSearchVariablePanel extends javax.swing.JPanel {

    private ActionListener listener;

    /** Creates new form PersonSearchVariablePanel */
    public PersonSearchVariablePanel() {
        initComponents();
        discPowerTextField.setVisible(false);
        discPowerjLabel.setVisible(false);
        //<ictl.co>
        if (LocalizationHelper.isRtlLanguageActive()) {
            CompareAlgorithms[] algorithms = CompareAlgorithms.values();
            List<CompareAlgorithms> result = new ArrayList<CompareAlgorithms>();
            for (CompareAlgorithms item : algorithms) {
                if (CompareAlgorithms.soundex != item) {
                    result.add(item);
                }
            }
            compareAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(result.toArray(new CompareAlgorithms[]{})));
        } else {
            compareAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(CompareAlgorithms.values()));
        }
        //<ictl.co>
    }

    /**
     * 
     * @param variables
     */
    public void setDatabaseVariables(DatabaseVariablesListElement[] variables) {
        variablesComboBox.setModel(new javax.swing.DefaultComboBoxModel(variables));
    }

    /**
     * 
     * @param variable
     */
    public void setSelectedVariable(DatabaseVariablesListElement variable) {
        variablesComboBox.setSelectedItem(variable);
    }

    void setPersonSearchVariable(PersonSearchVariable searchVariable) {
        boolean found = false;
        String searchVariableName = searchVariable.getName();
        compareAlgorithmComboBox.setSelectedItem(searchVariable.getCompareAlgorithm());
        DatabaseVariablesListElement databaseVariablesListElement = null;
        int i = 0;
        while (!found && i < variablesComboBox.getItemCount()) {
            databaseVariablesListElement = (DatabaseVariablesListElement) variablesComboBox.getItemAt(i++);
            found = searchVariableName.equalsIgnoreCase(databaseVariablesListElement.getShortName());
        }
        if (found) {
            variablesComboBox.setSelectedItem(databaseVariablesListElement);
        }
        weightTextField.setText(searchVariable.getWeight() + "");
        discPowerTextField.setText(searchVariable.getDiscPower() + "");
    }

    private DatabaseVariablesListElement getSelectedVariable() {
        return (DatabaseVariablesListElement) variablesComboBox.getSelectedItem();
    }

    /**
     * 
     * @param weight
     */
    public void setWeigth(int weight) {
        weightTextField.setText(weight + "");
    }

    private float getWeight() {
        float weight = 0;
        try {
            weight = Float.parseFloat(weightTextField.getText());
        } catch (NumberFormatException nfe) {
        }
        return weight;
    }

    private CompareAlgorithms getCompareAlgorithm() {
        return (CompareAlgorithms) compareAlgorithmComboBox.getSelectedItem();
    }

    /**
     * 
     * @param listener
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * 
     * @return
     */
    public PersonSearchVariable getPersonSearchVariable() {
        PersonSearchVariable psv = new PersonSearchVariable();
        psv.setVariable(getSelectedVariable());
        psv.setWeight(getWeight());
        psv.setDiscPower(getDiscPower());
        psv.setAlgorithm(getCompareAlgorithm());
        return psv;
    }

    public void setActive(boolean enabled) {
        removeButton.setEnabled(enabled);
        weightTextField.setEnabled(enabled);
        variablesComboBox.setEnabled(enabled);
        compareAlgorithmComboBox.setEnabled(enabled);
        discPowerTextField.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        removeButton = new javax.swing.JButton();
        variablesComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        weightTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        compareAlgorithmComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        discPowerTextField = new javax.swing.JTextField();
        discPowerjLabel = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(PersonSearchVariablePanel.class, this);
        removeButton.setAction(actionMap.get("removeVariableAction")); // NOI18N
        removeButton.setName("removeButton"); // NOI18N

        variablesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        variablesComboBox.setAction(actionMap.get("variableChanged")); // NOI18N
        variablesComboBox.setName("variablesComboBox"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        weightTextField.setText(resourceMap.getString("weightTextField.text")); // NOI18N
        weightTextField.setName("weightTextField"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        compareAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        compareAlgorithmComboBox.setName("compareAlgorithmComboBox"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        discPowerTextField.setText(resourceMap.getString("discPowerTextField.text")); // NOI18N
        discPowerTextField.setName("discPowerTextField"); // NOI18N

        discPowerjLabel.setText(resourceMap.getString("discPowerjLabel.text")); // NOI18N
        discPowerjLabel.setName("discPowerjLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(variablesComboBox, 0, 115, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compareAlgorithmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(discPowerjLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(discPowerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(removeButton)
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(variablesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(weightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel2)
                .addComponent(compareAlgorithmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel3)
                .addComponent(discPowerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(discPowerjLabel)
                .addComponent(removeButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 
     */
    @Action
    public void removeVariableAction() {
        listener.actionPerformed(new ActionEvent(this, 0, "remove"));
    }

    @Action
    public void variableChanged() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox compareAlgorithmComboBox;
    private javax.swing.JTextField discPowerTextField;
    private javax.swing.JLabel discPowerjLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton removeButton;
    private javax.swing.JComboBox variablesComboBox;
    private javax.swing.JTextField weightTextField;
    // End of variables declaration//GEN-END:variables

    private float getDiscPower() {
        float discPower = 0;
        try {
            discPower = Float.parseFloat(discPowerTextField.getText());
        } catch (NumberFormatException nfe) {
        }
        return discPower;
    }
}
