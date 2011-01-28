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
                keyVariableNames.add(dble.getDatabaseVariableName().toUpperCase());
            }
            if (dble.getFillInStatus().equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
                mandatoryVariableNames.add(dble.getDatabaseVariableName().toUpperCase());
            }
            // Add it to the all list
            allVariableNames.add(dble.getDatabaseVariableName().toUpperCase());
        }

        /*
        mapTableVariableNamesKey = new LinkedHashMap<String, LinkedList<String>>();
        mapTableVariableNamesMandatory = new LinkedHashMap<String, LinkedList<String>>();
        mapTableVariableNamesAll = new LinkedHashMap<String, LinkedList<String>>();
        for (DatabaseVariablesListElement dble : dbles) {
        if (dble.getStandardVariableName() != null) {
        // Add it to the proper list
        LinkedList list = mapTableVariableNamesKey.get(dble.getTable());
        // Create the list if necessary
        if (list == null) {
        list = new LinkedList<String>();
        mapTableVariableNamesKey.put(dble.getDatabaseTableName(), list);
        }
        list.add(dble.getDatabaseVariableName().toUpperCase());
        }
        if (dble.getFillInStatus().equalsIgnoreCase("Mandatory")) {
        // Add it to the proper list
        LinkedList list = mapTableVariableNamesMandatory.get(dble.getTable());
        // Create the list if necessary
        if (list == null) {
        list = new LinkedList<String>();
        mapTableVariableNamesMandatory.put(dble.getDatabaseTableName(), list);
        }
        list.add(dble.getDatabaseVariableName().toUpperCase());
        }
        // Add it to the all list
        LinkedList list = mapTableVariableNamesAll.get(dble.getTable());
        // Create the list if necessary
        if (list == null) {
        list = new LinkedList<String>();
        mapTableVariableNamesAll.put(dble.getDatabaseTableName(), list);
        }
        list.add(dble.getDatabaseVariableName().toUpperCase());
        }

        LinkedList tumourAndPatientKeylist = new LinkedList<String>();
        Iterator<LinkedList<String>> it = mapTableVariableNamesKey.values().iterator();
        while (it.hasNext()) {
        tumourAndPatientKeylist.addAll(it.next());
        }
        mapTableVariableNamesKey.put(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME, tumourAndPatientKeylist);

        LinkedList tumourAndPatientMandatoryList = new LinkedList<String>();
        Iterator<LinkedList<String>> it2 = mapTableVariableNamesMandatory.values().iterator();
        while (it2.hasNext()) {
        tumourAndPatientMandatoryList.addAll(it2.next());
        }
        mapTableVariableNamesMandatory.put(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME, tumourAndPatientMandatoryList);

        LinkedList tumourAndPatientAllList = new LinkedList<String>();
        Iterator<LinkedList<String>> it3 = mapTableVariableNamesAll.values().iterator();
        while (it3.hasNext()) {
        tumourAndPatientAllList.addAll(it3.next());
        }
        mapTableVariableNamesAll.put(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME, tumourAndPatientAllList);

        LinkedList tumourAndSourceKeylist = new LinkedList<String>();
        Iterator<LinkedList<String>> it4 = mapTableVariableNamesKey.values().iterator();
        while (it4.hasNext()) {
        tumourAndSourceKeylist.addAll(it4.next());
        }
        mapTableVariableNamesKey.put(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME, tumourAndPatientKeylist);

        LinkedList tumourAndSourceMandatoryList = new LinkedList<String>();
        Iterator<LinkedList<String>> it5 = mapTableVariableNamesMandatory.values().iterator();
        while (it5.hasNext()) {
        tumourAndSourceMandatoryList.addAll(it5.next());
        }
        mapTableVariableNamesMandatory.put(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME, tumourAndPatientMandatoryList);

        LinkedList tumourAndSourceAllList = new LinkedList<String>();
        Iterator<LinkedList<String>> it6 = mapTableVariableNamesAll.values().iterator();
        while (it6.hasNext()) {
        tumourAndSourceAllList.addAll(it6.next());
        }
        mapTableVariableNamesAll.put(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME, tumourAndPatientAllList);
         */
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
