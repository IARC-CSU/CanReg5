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
import canreg.common.PersonSearchVariable;
import canreg.common.qualitycontrol.PersonSearcher.CompareAlgorithms;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;

import javax.swing.*;

/**
 *
 * @author  ervikm
 */
public class PersonSearchVariablePanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(PersonSearchVariable.class.getName());
    private ActionListener listener;

    /** Creates new form PersonSearchVariablePanel */
    public PersonSearchVariablePanel() {
        initComponents();
        discPowerTextField.setVisible(false);
        discPowerjLabel.setVisible(false);
        yearMarginTextField.setVisible(false);
        yearMarginLabel.setVisible(false);

        compareAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(CompareAlgorithms.values()));
        compareAlgorithmComboBox.addActionListener(e -> {
            if (((JComboBox) e.getSource()).getModel().getSelectedItem().equals(CompareAlgorithms.valueOf("date"))) {
                yearMarginTextField.setVisible(true);
                yearMarginLabel.setVisible(true);
            } else {
                yearMarginTextField.setVisible(false);
                yearMarginLabel.setVisible(false);
            }
        });
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
        yearMarginTextField.setText(searchVariable.getYearRange() + "");
        discPowerTextField.setText(searchVariable.getDiscPower() + "");
        blockCheckBox.setSelected(searchVariable.isBlock());
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
            LOGGER.log(Level.WARNING, null, nfe);
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
        psv.setYearRange(getYearRange());
        psv.setDiscPower(getDiscPower());
        psv.setAlgorithm(getCompareAlgorithm());
        psv.setBlock(getBlocked());
        return psv;
    }

    public void setActive(boolean enabled) {
        removeButton.setEnabled(enabled);
        weightTextField.setEnabled(enabled);
        variablesComboBox.setEnabled(enabled);
        compareAlgorithmComboBox.setEnabled(enabled);
        discPowerTextField.setEnabled(enabled);
        blockCheckBox.setEnabled(enabled);
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
        blockCheckBox = new javax.swing.JCheckBox();
        yearMarginTextField = new javax.swing.JTextField();
        yearMarginLabel = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        removeButton.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("removeButton.text")); // NOI18N
        removeButton.setName("removeButton"); // NOI18N

        variablesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        variablesComboBox.setMaximumSize(new java.awt.Dimension(90, 26));
        variablesComboBox.setMinimumSize(new java.awt.Dimension(90, 26));
        variablesComboBox.setName("variablesComboBox"); // NOI18N
        variablesComboBox.setPreferredSize(new java.awt.Dimension(90, 26));

        jLabel1.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        weightTextField.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("weightTextField.text")); // NOI18N
        weightTextField.setName("weightTextField"); // NOI18N

        jLabel2.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("jLabel2.text")); // NOI18N
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setName("jLabel2"); // NOI18N

        compareAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        compareAlgorithmComboBox.setName("compareAlgorithmComboBox"); // NOI18N

        jLabel3.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        discPowerTextField.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("discPowerTextField.text")); // NOI18N
        discPowerTextField.setName("discPowerTextField"); // NOI18N

        discPowerjLabel.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("discPowerjLabel.text")); // NOI18N
        discPowerjLabel.setName("discPowerjLabel"); // NOI18N

        blockCheckBox.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("jCheckBox1.text")); // NOI18N
        blockCheckBox.setName("jCheckBox1"); // NOI18N
        blockCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blockCheckBoxActionPerformed(evt);
            }
        });

        yearMarginTextField.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("yearMarginTextField.text")); // NOI18N
        yearMarginTextField.setName("yearMarginTextField"); // NOI18N

        yearMarginLabel.setText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("yearMarginLabel.text")); // NOI18N
        yearMarginLabel.setToolTipText(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("yearMarginLabel.toolTipText")); // NOI18N
        yearMarginLabel.setName("yearMarginLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(variablesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compareAlgorithmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(discPowerjLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(discPowerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(blockCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(removeButton)
                .addGap(18, 18, 18)
                .addComponent(yearMarginLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yearMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(yearMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(yearMarginLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(removeButton)
                        .addComponent(blockCheckBox))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(discPowerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(discPowerjLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(variablesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(compareAlgorithmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(weightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        yearMarginLabel.getAccessibleContext().setAccessibleDescription(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablePanel.class).getString("yearMarginLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void blockCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blockCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_blockCheckBoxActionPerformed

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
    private javax.swing.JCheckBox blockCheckBox;
    private javax.swing.JComboBox compareAlgorithmComboBox;
    private javax.swing.JTextField discPowerTextField;
    private javax.swing.JLabel discPowerjLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton removeButton;
    private javax.swing.JComboBox variablesComboBox;
    private javax.swing.JTextField weightTextField;
    private javax.swing.JLabel yearMarginLabel;
    private javax.swing.JTextField yearMarginTextField;
    // End of variables declaration//GEN-END:variables

    private float getDiscPower() {
        float discPower = 0;
        try {
            discPower = Float.parseFloat(discPowerTextField.getText());
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.WARNING, null, nfe);
        }
        return discPower;
    }

    private int getYearRange() {
        return Integer.parseInt(yearMarginTextField.getText());
    }

    private boolean getBlocked(){
        return blockCheckBox.isSelected();
    }
}
