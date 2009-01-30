/*
 * RecordEditorPanel.java
 *
 * Created on 16 July 2008, 14:53
 */
package canreg.client.gui.dataentry;

import canreg.client.CanRegClientApp;
import canreg.client.gui.components.DateVariableEditorPanel;
import canreg.client.gui.components.VariableEditorGroupPanel;
import canreg.client.gui.components.VariableEditorPanel;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.DatabaseVariablesListElementPositionSorter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Dictionary;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author  ervikm
 */
public class RecordEditorPanel extends javax.swing.JPanel implements Cloneable, PropertyChangeListener {

    private DatabaseRecord databaseRecord;
    private Document doc;
    private panelTypes panelType;
    private DatabaseVariablesListElement[] variablesInTable;
    private Map<String, VariableEditorPanel> variableEditorPanels;
    private Map<Integer, Dictionary> dictionary;
    private DatabaseGroupsListElement[] groupListElements;
    private GlobalToolBox globalToolBox;
    private boolean saveNeeded = false;
    private ActionListener actionListener;
    private DatabaseVariablesListElement recordStatusVariableListElement;
    private DatabaseVariablesListElement unduplicationVariableListElement;
    private DatabaseVariablesListElement checkVariableListElement;
    private Map<String, DictionaryEntry> recStatusDict;
    private ResultCode resultCode = null;
    private DatabaseVariablesListElement patientIDVariableListElement;

    boolean areAllVariablesPresent() {
        boolean allPresent = true;
        for (DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanel panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if (panel != null) {
                boolean filledOK = panel.isFilledOK();
                if (!filledOK) {
                    panel.updateFilledInStatusColor();
                }
                allPresent = allPresent & filledOK;
            }
        }
        return allPresent;
    }

    void refreshDatabaseRecord(DatabaseRecord record) {
        this.databaseRecord = record;
        buildPanel();
    }

    void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    /**
     * 
     * @return
     */
    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    /**
     * 
     * @param saveNeeded
     */
    public void setSaveNeeded(boolean saveNeeded) {
        this.saveNeeded = saveNeeded;
    }

    public void setResultCode(String databaseVariableName, ResultCode resultCode) {
        VariableEditorPanel panel = variableEditorPanels.get(databaseVariableName);
        panel.setResultCode(resultCode);
    }

    void setChecksResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
        if (resultCode == null){
            checksLabel.setText("Not done");
        } else {
            checksLabel.setText("Done: "+resultCode.toString());
        }
    }

    private enum panelTypes {
        PATIENT, TUMOUR
    }

    /** Creates new form RecordEditorPanel */
    public RecordEditorPanel() {
        initComponents();
        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        saveButton.setEnabled(true);
        setChecksResultCode(resultCode);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
    }

    /**
     * 
     * @param doc
     */
    public void setDocument(Document doc) {
        this.doc = doc;
    }

    /**
     * 
     * @param dictionary
     */
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * 
     * @param dbr
     */
    public void setRecord(DatabaseRecord dbr) {
        this.databaseRecord = dbr;
        if (databaseRecord.getClass().isInstance(new Patient())) {
            panelType = panelTypes.PATIENT;
            recordStatusVariableListElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordStatus.toString());
            unduplicationVariableListElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString());
            patientIDVariableListElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
        } else if (databaseRecord.getClass().isInstance(new Tumour())) {
            panelType = panelTypes.TUMOUR;
            recordStatusVariableListElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString());
            unduplicationVariableListElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString());
        }

        buildPanel();
    }

    /**
     * 
     * @return
     */
    public DatabaseRecord getDatabaseRecord() {
        buildDatabaseRecord();
        return databaseRecord;
    }

    private void buildPanel() {
        String tableName = null;
        dataPanel.removeAll();

        variableEditorPanels = new TreeMap();

        if (panelType == panelTypes.PATIENT) {
            tableName = Globals.PATIENT_TABLE_NAME;
            mpPanel.setVisible(false);
        } else if (panelType == panelTypes.TUMOUR) {
            tableName = Globals.TUMOUR_TABLE_NAME;
            personSearchPanel.setVisible(false);
        }

        /*
         * Set the record status.
         */
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatusDict = dictionary.get(canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, recordStatusVariableListElement.getUseDictionary())).getDictionaryEntries();
            recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDict.values().toArray()));
            Object recStatus = databaseRecord.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
            if (recStatus != null) {
                recordStatusComboBox.setSelectedItem(recStatusDict.get(recStatus));
            }
        } else {
            recordStatusPanel.setVisible(false);
        }

        Map<Integer, VariableEditorGroupPanel> groupIDtoPanelMap = new TreeMap<Integer, VariableEditorGroupPanel>();

        variablesInTable = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        Arrays.sort(variablesInTable, new DatabaseVariablesListElementPositionSorter());
        Map<String, DictionaryEntry> possibleValues;

        for (int i = 0; i < variablesInTable.length; i++) {
            DatabaseVariablesListElement currentVariable = variablesInTable[i];
            VariableEditorPanel vep;

            String variableType = currentVariable.getVariableType();

            if ("date".equalsIgnoreCase(variableType)) {
                vep = new DateVariableEditorPanel();
            } else {
                vep = new VariableEditorPanel();
            }

            vep.setDatabaseVariablesListElement(currentVariable);

            int id = currentVariable.getDictionaryID();

            if (id >= 0) {
                Dictionary dic = dictionary.get(id);

                if (dic != null) {
                    vep.setDictionary(dic);
                }
            } else {
                vep.setDictionary(null);
            }
            String variableName = currentVariable.getDatabaseVariableName();
            Object variableValue = databaseRecord.getVariable(variableName);
            if (variableValue != null) {
                vep.setValue(variableValue.toString());
            }
            Integer groupID = currentVariable.getGroupID();
            //Skip 0 and -1 - System groups
            if (groupID > 0) {
                VariableEditorGroupPanel panel = groupIDtoPanelMap.get(groupID);
                if (panel == null) {
                    panel = new VariableEditorGroupPanel();
                    panel.setGroupName(globalToolBox.translateGroupIDToDatabaseGroupListElement(groupID).getGroupName());
                    groupIDtoPanelMap.put(currentVariable.getGroupID(), panel);
                }
                panel.add(vep);
            }

            // vep.setPropertyChangeListener(this);

            variableEditorPanels.put(currentVariable.getDatabaseVariableName(), vep);
        }
        // Iterate trough groups
        Iterator<Integer> iterator = groupIDtoPanelMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer groupID = iterator.next();
            JPanel panel = groupIDtoPanelMap.get(groupID);
            dataPanel.add(panel);
            panel.setVisible(true);
        }

        dataPanel.revalidate();
        dataPanel.repaint();
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propName = e.getPropertyName();
        if ("focusOwner".equals(propName)) {
            if (e.getNewValue() instanceof JTextField) {
                JTextField textField = (JTextField) e.getNewValue();
                textField.selectAll();
            }
        } /** Called when a field's "value" property changes. */
        else if ("value".equals(propName)) {
            setSaveNeeded(true);
            actionListener.actionPerformed(new ActionEvent(this, 0, "changed"));
        // saveButton.setEnabled(saveNeeded);
        } else {
            // System.out.println(e.getPropertyName());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        systemPanel = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        checksPanel = new javax.swing.JPanel();
        checksButton = new javax.swing.JButton();
        checksLabel = new javax.swing.JLabel();
        personSearchPanel = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        mpPanel = new javax.swing.JPanel();
        mpButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        recordStatusPanel = new javax.swing.JPanel();
        recordStatusComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditorPanel.class);
        systemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("systemPanel.border.title"))); // NOI18N
        systemPanel.setName("systemPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditorPanel.class, this);
        saveButton.setAction(actionMap.get("saveRecord")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        jButton1.setAction(actionMap.get("deleteRecord")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        checksPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("checksPanel.border.title"))); // NOI18N
        checksPanel.setName("checksPanel"); // NOI18N

        checksButton.setAction(actionMap.get("runChecksAction")); // NOI18N
        checksButton.setText(resourceMap.getString("checksButton.text")); // NOI18N
        checksButton.setName("checksButton"); // NOI18N

        checksLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        checksLabel.setText(resourceMap.getString("checksLabel.text")); // NOI18N
        checksLabel.setName("checksLabel"); // NOI18N

        javax.swing.GroupLayout checksPanelLayout = new javax.swing.GroupLayout(checksPanel);
        checksPanel.setLayout(checksPanelLayout);
        checksPanelLayout.setHorizontalGroup(
            checksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(checksButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(checksLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
        );
        checksPanelLayout.setVerticalGroup(
            checksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checksPanelLayout.createSequentialGroup()
                .addComponent(checksButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checksLabel))
        );

        personSearchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("personSearchPanel.border.title"))); // NOI18N
        personSearchPanel.setName("personSearchPanel"); // NOI18N

        searchButton.setAction(actionMap.get("runPersonSearch")); // NOI18N
        searchButton.setText(resourceMap.getString("searchButton.text")); // NOI18N
        searchButton.setToolTipText(resourceMap.getString("searchButton.toolTipText")); // NOI18N
        searchButton.setName("searchButton"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout personSearchPanelLayout = new javax.swing.GroupLayout(personSearchPanel);
        personSearchPanel.setLayout(personSearchPanelLayout);
        personSearchPanelLayout.setHorizontalGroup(
            personSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
        );
        personSearchPanelLayout.setVerticalGroup(
            personSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(personSearchPanelLayout.createSequentialGroup()
                .addComponent(searchButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2))
        );

        mpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("mpPanel.border.title"))); // NOI18N
        mpPanel.setName("mpPanel"); // NOI18N

        mpButton.setText(resourceMap.getString("mpButton.text")); // NOI18N
        mpButton.setToolTipText(resourceMap.getString("mpButton.toolTipText")); // NOI18N
        mpButton.setName("mpButton"); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout mpPanelLayout = new javax.swing.GroupLayout(mpPanel);
        mpPanel.setLayout(mpPanelLayout);
        mpPanelLayout.setHorizontalGroup(
            mpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mpButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
        );
        mpPanelLayout.setVerticalGroup(
            mpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mpPanelLayout.createSequentialGroup()
                .addComponent(mpButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3))
        );

        recordStatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("recordStatusPanel.border.title"))); // NOI18N
        recordStatusPanel.setName("recordStatusPanel"); // NOI18N

        recordStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        recordStatusComboBox.setName("recordStatusComboBox"); // NOI18N

        javax.swing.GroupLayout recordStatusPanelLayout = new javax.swing.GroupLayout(recordStatusPanel);
        recordStatusPanel.setLayout(recordStatusPanelLayout);
        recordStatusPanelLayout.setHorizontalGroup(
            recordStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordStatusPanelLayout.createSequentialGroup()
                .addComponent(recordStatusComboBox, 0, 114, Short.MAX_VALUE)
                .addContainerGap())
        );
        recordStatusPanelLayout.setVerticalGroup(
            recordStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordStatusPanelLayout.createSequentialGroup()
                .addComponent(recordStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout systemPanelLayout = new javax.swing.GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        systemPanelLayout.setHorizontalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addComponent(checksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(personSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recordStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)))
        );
        systemPanelLayout.setVerticalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
            .addComponent(mpPanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(personSearchPanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(checksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(recordStatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        dataPanel.setName("dataPanel"); // NOI18N
        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(dataPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(systemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(systemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))
        );

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checksButton;
    private javax.swing.JLabel checksLabel;
    private javax.swing.JPanel checksPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton mpButton;
    private javax.swing.JPanel mpPanel;
    private javax.swing.JPanel personSearchPanel;
    private javax.swing.JComboBox recordStatusComboBox;
    private javax.swing.JPanel recordStatusPanel;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel systemPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public RecordEditorPanel clone() throws CloneNotSupportedException {
        RecordEditorPanel clone = (RecordEditorPanel) super.clone();

        return clone();
    }

    /**
     * 
     */
    @Action
    public void saveRecord() {
        buildDatabaseRecord();
        actionListener.actionPerformed(new ActionEvent(this, 0, "save"));
    }

    private void buildDatabaseRecord() {
        Iterator<VariableEditorPanel> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanel vep = iterator.next();
            databaseRecord.setVariable(vep.getKey(), vep.getValue());
        }
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            DictionaryEntry recordStatusValue = (DictionaryEntry) recordStatusComboBox.getSelectedItem();
            databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recordStatusValue.getCode());
        }
    }

    /**
     * 
     */
    @Action
    public void runPersonSearch() {
        try {
            buildDatabaseRecord();
            Map<Integer, Float> map =
                    canreg.client.CanRegClientApp.getApplication().performDuplicateSearch((Patient) databaseRecord, null);
            if (map.size() == 0) {
                JOptionPane.showInternalMessageDialog(this, "No duplicates found.");
            } else {
                String records = "";
                for (Integer i : map.keySet()) {
                    // records += i + ": " + map.get(i) + "\n";
                    DatabaseRecord patientRecord = canreg.client.CanRegClientApp.getApplication().getRecord(i, Globals.PATIENT_TABLE_NAME);
                    
                    records += "Patient id: " + patientRecord.getVariable(patientIDVariableListElement.getDatabaseVariableName()) + ", score: " + map.get(i) + "%\n";
                }
                JOptionPane.showInternalMessageDialog(this, "Duplicates found:\n" + records);
            // TODO add user feedback and options to handle duplicates
            }
        } catch (SecurityException ex) {
            Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     */
    @Action
    public void runChecksAction() {
        actionListener.actionPerformed(new ActionEvent(this, 0, "checks"));
    }

    @Action
    public void deleteRecord() {
        actionListener.actionPerformed(new ActionEvent(this, 0, "delete"));
    }
}
