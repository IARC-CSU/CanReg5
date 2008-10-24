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
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Dictionary;
import canreg.server.database.DictionaryEntry;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.awt.Graphics;
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

    void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    public void setSaveNeeded(boolean saveNeeded) {
        this.saveNeeded = saveNeeded;
    }

    private enum panelTypes {
        PATIENT, TUMOUR
    }

    /** Creates new form RecordEditorPanel */
    public RecordEditorPanel() {
        initComponents();
        saveButton.setEnabled(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
    }

    public void setDocument(Document doc) {
        this.doc = doc;
    }

    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }

    public void setRecord(DatabaseRecord dbr) {
        this.databaseRecord = dbr;
        if (databaseRecord.getClass().isInstance(new Patient())) {
            panelType = panelTypes.PATIENT;
        } else if (databaseRecord.getClass().isInstance(new Tumour())) {
            panelType = panelTypes.TUMOUR;
        }
        buildPanel();
    }

    public DatabaseRecord getRecord() {
        buildDatabaseRecord();
        return databaseRecord;
    }

    private void buildPanel() {
        String tableName = null;
        dataPanel.removeAll();

        variableEditorPanels = new TreeMap();

        if (panelType == panelTypes.PATIENT) {
            tableName = Globals.PATIENT_TABLE_NAME;
            // checksButton.setVisible(false);
            mpButton.setVisible(false);
        } else if (panelType == panelTypes.TUMOUR) {
            tableName = Globals.TUMOUR_TABLE_NAME;
            searchButton.setVisible(false);
        }
        Map<String, DictionaryEntry> recStatusDict = dictionary.get(canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, "Record status")).getDictionaryEntries();
        statusComboBox.setModel(new DefaultComboBoxModel(recStatusDict.values().toArray()));
        Object recStatus = databaseRecord.getVariable("RecS");
        if (recStatus != null) {
            statusComboBox.setSelectedItem(recStatusDict.get(recStatus));
        }

        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
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
            actionListener.actionPerformed(new ActionEvent(this,0,"changed"));
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
        searchButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        mpButton = new javax.swing.JButton();
        checksButton = new javax.swing.JButton();
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

        searchButton.setAction(actionMap.get("runPersonSearch")); // NOI18N
        searchButton.setText(resourceMap.getString("searchButton.text")); // NOI18N
        searchButton.setName("searchButton"); // NOI18N

        statusLabel.setText(resourceMap.getString("statusLabel.text")); // NOI18N
        statusLabel.setName("statusLabel"); // NOI18N

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        statusComboBox.setName("statusComboBox"); // NOI18N

        mpButton.setText(resourceMap.getString("mpButton.text")); // NOI18N
        mpButton.setName("mpButton"); // NOI18N

        checksButton.setAction(actionMap.get("runChecksAction")); // NOI18N
        checksButton.setText(resourceMap.getString("checksButton.text")); // NOI18N
        checksButton.setName("checksButton"); // NOI18N

        javax.swing.GroupLayout systemPanelLayout = new javax.swing.GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        systemPanelLayout.setHorizontalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, systemPanelLayout.createSequentialGroup()
                .addComponent(checksButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mpButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusComboBox, 0, 114, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap())
        );
        systemPanelLayout.setVerticalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(saveButton)
                .addComponent(searchButton)
                .addComponent(checksButton)
                .addComponent(mpButton)
                .addComponent(statusLabel)
                .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        dataPanel.setName("dataPanel"); // NOI18N
        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(dataPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
            .addComponent(systemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(systemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))
        );

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checksButton;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton mpButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JComboBox statusComboBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel systemPanel;
    // End of variables declaration//GEN-END:variables
    @Override
    public RecordEditorPanel clone() throws CloneNotSupportedException {
        RecordEditorPanel clone = (RecordEditorPanel) super.clone();

        return clone();
    }

    @Action
    public void saveRecord() {
        try {
            if ((Integer) databaseRecord.getVariable("id") != null) {
                canreg.client.CanRegClientApp.getApplication().editRecord(databaseRecord);
            } else {
                canreg.client.CanRegClientApp.getApplication().saveRecord(databaseRecord);
            }
            JOptionPane.showInternalMessageDialog(this, "Record saved.");
            setSaveNeeded(false);
        // saveButton.setEnabled(saveNeeded);

        } catch (SecurityException ex) {
            Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildDatabaseRecord(){
        Iterator<VariableEditorPanel> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanel vep = iterator.next();
            databaseRecord.setVariable(vep.getKey(), vep.getValue());
        }
    }
    
    @Action
    public void runPersonSearch() {
        try {
            buildDatabaseRecord();
            Map<Integer, Float> map = canreg.client.CanRegClientApp.getApplication().performDuplicateSearch((Patient) databaseRecord, null);
            if (map.size() == 0) {
                JOptionPane.showInternalMessageDialog(this, "No duplicates found.");
            } else {
                String records = "";
                for (Integer i : map.keySet()) {
                    records += i + ": " + map.get(i) + "\n";
                }
                JOptionPane.showInternalMessageDialog(this, "Duplicates found:\n" + records);
            }
        } catch (SecurityException ex) {
            Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void runChecksAction() {
        actionListener.actionPerformed(new ActionEvent(this, 0, "checks"));
    }
}
