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

/*
 * DisplayVariablesPanel.java
 *
 * Created on 23 June 2008, 11:52
 */
package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author  ervikm
 */
public class DisplayVariablesPanel extends javax.swing.JPanel {

    DatabaseVariablesListElement[] databaseVariablesListElements;
    Map<String, LinkedList<String>> mapTableVariableNamesKey;
    Map<String, LinkedList<String>> mapTableVariableNamesMandatory;
    Map<String, LinkedList<String>> mapTableVariableNamesAll;
    LinkedList<String> keyVariableNames = new LinkedList<String>();
    LinkedList<String> mandatoryVariableNames = new LinkedList<String>();
    LinkedList<String> allVariableNames = new LinkedList<String>();

    /** Creates new form DisplayVariablesPanel */
    public DisplayVariablesPanel() {
        initComponents();
    }

    /**
     * 
     * @param dbles
     */
    public void setDatabaseVariables(DatabaseVariablesListElement[] dbles) {
        // Build maps
        keyVariableNames = new LinkedList<String>();
        mandatoryVariableNames = new LinkedList<String>();
        allVariableNames = new LinkedList<String>();
        for (DatabaseVariablesListElement dble : dbles) {
            if (dble.getStandardVariableName() != null) {
                keyVariableNames.add(canreg.common.Tools.toUpperCaseStandardized(
                        dble.getDatabaseVariableName()));
            }
            if (dble.getFillInStatus().equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
                mandatoryVariableNames.add(
                        canreg.common.Tools.toUpperCaseStandardized(
                        dble.getDatabaseVariableName()));
            }
            // Add it to the all list
            allVariableNames.add(
                    canreg.common.Tools.toUpperCaseStandardized(
                    dble.getDatabaseVariableName()));
        }
    }

    /**
     * 
     * @param tableName
     * @return
     */
    public LinkedList<String> getVariablesToShow(String tableName) {
        if (keyRadioButton.isSelected()) {
            return keyVariableNames;
        } else if (mandatoryRadioButton.isSelected()) {
            return mandatoryVariableNames;
        } else {
            return allVariableNames;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        variablesPanel = new javax.swing.JPanel();
        allRadioButton = new javax.swing.JRadioButton();
        mandatoryRadioButton = new javax.swing.JRadioButton();
        keyRadioButton = new javax.swing.JRadioButton();
        personalizedRadioButton = new javax.swing.JRadioButton();
        jComboBox1 = new javax.swing.JComboBox();

        buttonGroup.add(allRadioButton);
        buttonGroup.add(keyRadioButton);
        buttonGroup.add(mandatoryRadioButton);

        setName("Form"); // NOI18N

        variablesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Variables"));
        variablesPanel.setName("variablesPanel"); // NOI18N

        buttonGroup.add(allRadioButton);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(DisplayVariablesPanel.class);
        allRadioButton.setText(resourceMap.getString("allRadioButton.text")); // NOI18N
        allRadioButton.setName("allRadioButton"); // NOI18N

        buttonGroup.add(mandatoryRadioButton);
        mandatoryRadioButton.setText(resourceMap.getString("mandatoryRadioButton.text")); // NOI18N
        mandatoryRadioButton.setName("mandatoryRadioButton"); // NOI18N

        buttonGroup.add(keyRadioButton);
        keyRadioButton.setSelected(true);
        keyRadioButton.setText(resourceMap.getString("keyRadioButton.text")); // NOI18N
        keyRadioButton.setName("keyRadioButton"); // NOI18N

        buttonGroup.add(personalizedRadioButton);
        personalizedRadioButton.setText(resourceMap.getString("personalizedRadioButton.text")); // NOI18N
        personalizedRadioButton.setEnabled(false);
        personalizedRadioButton.setName("personalizedRadioButton"); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "<New>" }));
        jComboBox1.setEnabled(false);
        jComboBox1.setName("jComboBox1"); // NOI18N

        javax.swing.GroupLayout variablesPanelLayout = new javax.swing.GroupLayout(variablesPanel);
        variablesPanel.setLayout(variablesPanelLayout);
        variablesPanelLayout.setHorizontalGroup(
            variablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(personalizedRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(keyRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
            .addComponent(mandatoryRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
            .addComponent(allRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
            .addGroup(variablesPanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jComboBox1, 0, 64, Short.MAX_VALUE))
        );
        variablesPanelLayout.setVerticalGroup(
            variablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(variablesPanelLayout.createSequentialGroup()
                .addComponent(allRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mandatoryRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(personalizedRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(variablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(variablesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allRadioButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JRadioButton keyRadioButton;
    private javax.swing.JRadioButton mandatoryRadioButton;
    private javax.swing.JRadioButton personalizedRadioButton;
    private javax.swing.JPanel variablesPanel;
    // End of variables declaration//GEN-END:variables
}
