/*
 * RecordEditorPanel.java
 *
 * Created on 16 July 2008, 14:53
 */
package canreg.client.gui.dataentry;

import canreg.client.CanRegClientApp;
import canreg.client.gui.components.DateVariableEditorPanel;
import canreg.client.gui.components.TextFieldVariableEditorPanel;
import canreg.client.gui.components.VariableEditorGroupPanel;
import canreg.client.gui.components.VariableEditorPanel;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.DatabaseVariablesListElementPositionSorter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.qualitycontrol.CheckResult;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
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
public class RecordEditorPanel extends javax.swing.JPanel implements ActionListener, Cloneable, PropertyChangeListener {

    private DatabaseRecord databaseRecord;
    private Document doc;
    private panelTypes panelType;
    private DatabaseVariablesListElement[] variablesInTable;
    private Map<String, VariableEditorPanel> variableEditorPanels;
    private Map<Integer, Dictionary> dictionary;
    private DatabaseGroupsListElement[] groupListElements;
    private GlobalToolBox globalToolBox;
    private boolean hasChanged = false;
    private ActionListener actionListener;
    private DatabaseVariablesListElement recordStatusVariableListElement;
    private DatabaseVariablesListElement unduplicationVariableListElement;
    private DatabaseVariablesListElement checkVariableListElement;
    private Map<String, DictionaryEntry> recStatusDictMap;
    private DictionaryEntry[] recStatusDictWithConfirmArray;
    private DictionaryEntry[] recStatusDictWithoutConfirmArray;
    private ResultCode resultCode = null;
    private DatabaseVariablesListElement patientIDVariableListElement;
    private DatabaseVariablesListElement patientRecordIDVariableListElement;
    private DatabaseVariablesListElement obsoleteFlagVariableListElement;

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
        hasChanged = false;

        for (DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanel panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if (panel != null) {
                hasChanged = hasChanged || panel.hasChanged();
            }
        }

        return hasChanged;
    }

    /**
     * 
     * @param saveNeeded
     */
    public void setSaveNeeded(boolean saveNeeded) {
        this.hasChanged = saveNeeded;
    }


    /*
     * Set the resultcode of individual variables.
     *
     */
    public void setResultCodeOfVariable(String databaseVariableName, ResultCode resultCode) {
        VariableEditorPanel panel = variableEditorPanels.get(databaseVariableName);
        panel.setResultCode(resultCode);
    }

    void setChecksResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
        Object recStatus = null;
        boolean canBeConfirmed = false;
        if (resultCode == null || resultCode == ResultCode.NotDone) {
            checksLabel.setText("Not done");
        } else {
            checksLabel.setText("Done: " + resultCode.toString());
            if (resultCode == ResultCode.OK || resultCode == ResultCode.Query) {
                canBeConfirmed = true;
            } else if (resultCode == resultCode.Rare) {
                if (CanRegClientApp.getApplication().getUserRightLevel() == Globals.UserRightLevels.SUPERVISOR) {
                    canBeConfirmed = true;
                }
            }
        }
        // Set record status
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            if (hasChanged) {
                recStatus = "0";
            } else {
                recStatus = databaseRecord.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
            }
            if (canBeConfirmed) {
                recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithConfirmArray));

                if (recStatus != null) {
                    recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));
                }
            } else {
                recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithoutConfirmArray));

                if (recStatus != null) {
                    recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));
                }
            }
        // databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recStatus);
        }
    }

    void toggleObsolete(boolean confirmed) {
        if (confirmed) {
            DatabaseVariablesListElement dbvle = obsoleteFlagVariableListElement;
            if (dbvle != null) {
                boolean obsolete = obsoleteToggleButton.isSelected();
                if (obsolete) {
                    databaseRecord.setVariable(dbvle.getDatabaseVariableName(), 1);
                } else {
                    databaseRecord.setVariable(dbvle.getDatabaseVariableName(), 0);
                }

            }
        } else {
            obsoleteToggleButton.setSelected(!obsoleteToggleButton.isSelected());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("Changed")) {

            changesDone();
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGED));
        }
    }

    private void changesDone() {
        setSaveNeeded(true);
        setChecksResultCode(ResultCode.NotDone);
    }

    private enum panelTypes {

        PATIENT, TUMOUR
    }

    /** Creates new form RecordEditorPanel */
    public RecordEditorPanel(ActionListener listener) {
        initComponents();

        this.actionListener = listener;

        globalToolBox =
                CanRegClientApp.getApplication().getGlobalToolBox();
        saveButton.setEnabled(true);
        // setChecksResultCode(resultCode);
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
            recordStatusVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordStatus.toString());
            unduplicationVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString());
            patientIDVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
            patientRecordIDVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString());
            obsoleteFlagVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString());
        } else if (databaseRecord.getClass().isInstance(new Tumour())) {
            panelType = panelTypes.TUMOUR;
            recordStatusVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString());
            unduplicationVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString());
            obsoleteFlagVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString());
            checkVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.CheckStatus.toString());

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

        variableEditorPanels =
                new LinkedHashMap();

        if (panelType == panelTypes.PATIENT) {
            tableName = Globals.PATIENT_TABLE_NAME;
            mpPanel.setVisible(false);
            changePatientRecordButton.setVisible(false);
            checksPanel.setVisible(false);
        } else if (panelType == panelTypes.TUMOUR) {
            tableName = Globals.TUMOUR_TABLE_NAME;
            personSearchPanel.setVisible(false);
        }

        Map<Integer, VariableEditorGroupPanel> groupIDtoPanelMap = new LinkedHashMap<Integer, VariableEditorGroupPanel>();

        variablesInTable =
                canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        Arrays.sort(variablesInTable, new DatabaseVariablesListElementPositionSorter());
        Map<String, DictionaryEntry> possibleValues;

        for (int i = 0; i <
                variablesInTable.length; i++) {
            DatabaseVariablesListElement currentVariable = variablesInTable[i];
            VariableEditorPanel vep;

            String variableType = currentVariable.getVariableType();

            if ("Date".equalsIgnoreCase(variableType)) {
                vep = new DateVariableEditorPanel(this);
            } if ("TextArea".equalsIgnoreCase(variableType)) {
                vep = new TextFieldVariableEditorPanel(this);
            } else {
                vep = new VariableEditorPanel(this);
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

        // Iterator<Integer> iterator = groupIDtoPanelMap.keySet().iterator();
        for (DatabaseGroupsListElement groupListElement : Tools.getGroupsListElements(doc, Globals.NAMESPACE)) {
            int groupID = groupListElement.getGroupIndex();
            JPanel panel = groupIDtoPanelMap.get(groupID);
            if (panel != null) {
                dataPanel.add(panel);
                panel.setVisible(true);
            }
        }

        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatusDictMap = dictionary.get(canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, recordStatusVariableListElement.getUseDictionary())).getDictionaryEntries();

            Collection<DictionaryEntry> recStatusDictCollection = recStatusDictMap.values();
            recStatusDictWithConfirmArray =
                    recStatusDictCollection.toArray(new DictionaryEntry[0]);

            Vector<DictionaryEntry> recStatusDictWithoutConfirmVector = new Vector<DictionaryEntry>();
            for (DictionaryEntry entry : recStatusDictCollection) {
                if (!entry.getCode().equalsIgnoreCase("1")) {
                    recStatusDictWithoutConfirmVector.add(entry);
                }
            }
            recStatusDictWithoutConfirmArray = recStatusDictWithoutConfirmVector.toArray(new DictionaryEntry[0]);
        }

        /*
         * Set the obsolete status
         */
        Object obsoleteStatus = databaseRecord.getVariable(obsoleteFlagVariableListElement.getDatabaseVariableName());
        if (obsoleteStatus != null && (Integer) obsoleteStatus == 1) {
            obsoleteToggleButton.setSelected(true);
        } else {
            obsoleteToggleButton.setSelected(false);
        }

        /*
         * Set the record status.
         */
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {

            recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithConfirmArray));
            Object recStatus = databaseRecord.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
            if (recStatus != null) {
                recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));
            }

        } else {
            recordStatusPanel.setVisible(false);
        }

        setSaveNeeded(false);

        /*
         * Set the check status
         */
        if (checkVariableListElement != null) {
            Object checkStatus = databaseRecord.getVariable(checkVariableListElement.getDatabaseVariableName());
            if (checkStatus != null) {
                String checkStatusString = (String) checkStatus;
                resultCode = CheckResult.toResultCode(checkStatusString);
                setChecksResultCode(resultCode);
            }
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
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGED));
        // saveButton.setEnabled(saveNeeded);
        } else {
            // Do nothing.
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
        recordStatusPanel1 = new javax.swing.JPanel();
        changePatientRecordButton = new javax.swing.JButton();
        obsoleteToggleButton = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditorPanel.class);
        systemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("systemPanel.border.title"))); // NOI18N
        systemPanel.setName("systemPanel"); // NOI18N

        checksPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("checksPanel.border.title"))); // NOI18N
        checksPanel.setName("checksPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditorPanel.class, this);
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
            .addComponent(checksLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
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

        mpButton.setAction(actionMap.get("runMultiplePrimarySearch")); // NOI18N
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
            .addComponent(mpButton, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
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
                .addComponent(recordStatusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        recordStatusPanelLayout.setVerticalGroup(
            recordStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordStatusPanelLayout.createSequentialGroup()
                .addComponent(recordStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        recordStatusPanel1.setName("recordStatusPanel1"); // NOI18N

        changePatientRecordButton.setAction(actionMap.get("changePatientRecord")); // NOI18N
        changePatientRecordButton.setText(resourceMap.getString("changePatientRecordButton.text")); // NOI18N
        changePatientRecordButton.setToolTipText(resourceMap.getString("changePatientRecordButton.toolTipText")); // NOI18N
        changePatientRecordButton.setName("changePatientRecordButton"); // NOI18N

        obsoleteToggleButton.setAction(actionMap.get("setObsoleteFlag")); // NOI18N
        obsoleteToggleButton.setText(resourceMap.getString("obsoleteToggleButton.text")); // NOI18N
        obsoleteToggleButton.setName("obsoleteToggleButton"); // NOI18N

        jButton1.setAction(actionMap.get("deleteRecord")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        saveButton.setAction(actionMap.get("saveRecord")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        javax.swing.GroupLayout recordStatusPanel1Layout = new javax.swing.GroupLayout(recordStatusPanel1);
        recordStatusPanel1.setLayout(recordStatusPanel1Layout);
        recordStatusPanel1Layout.setHorizontalGroup(
            recordStatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, recordStatusPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordStatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(obsoleteToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(recordStatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(changePatientRecordButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        recordStatusPanel1Layout.setVerticalGroup(
            recordStatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordStatusPanel1Layout.createSequentialGroup()
                .addGroup(recordStatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(obsoleteToggleButton)
                    .addComponent(changePatientRecordButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(recordStatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(saveButton)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(recordStatusPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        systemPanelLayout.setVerticalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(personSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(checksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(recordStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recordStatusPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(systemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE))
        );

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changePatientRecordButton;
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
    private javax.swing.JToggleButton obsoleteToggleButton;
    private javax.swing.JPanel personSearchPanel;
    private javax.swing.JComboBox recordStatusComboBox;
    private javax.swing.JPanel recordStatusPanel;
    private javax.swing.JPanel recordStatusPanel1;
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
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.SAVE));
        Iterator<VariableEditorPanel> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanel vep = iterator.next();
            vep.setSaved();
        }
    }

    private void buildDatabaseRecord() {
        Iterator<VariableEditorPanel> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanel vep = iterator.next();
            databaseRecord.setVariable(vep.getKey(), vep.getValue());
        }
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            DictionaryEntry recordStatusValue = (DictionaryEntry) recordStatusComboBox.getSelectedItem();
            if (recordStatusValue != null) {
                databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recordStatusValue.getCode());
            } else {
                // JOptionPane.showInternalMessageDialog(this, "Record status dictionary entries missing.");
                Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.WARNING, "Warning! Record status dictionary entries missing.");
            }
        }
        if (obsoleteToggleButton.isSelected()) {
            databaseRecord.setVariable(obsoleteFlagVariableListElement.getDatabaseVariableName(), 1);
        } else {
            databaseRecord.setVariable(obsoleteFlagVariableListElement.getDatabaseVariableName(), 0);
        }
    }

    /**
     * 
     */
    @Action
    public void runPersonSearch() {
        try {
            buildDatabaseRecord();
            Map<String, Float> map =
                    canreg.client.CanRegClientApp.getApplication().performDuplicateSearch((Patient) databaseRecord, null);
            //remove patients with the same patientID -- already mapped
            String patientRecordID = databaseRecord.getVariable(patientIDVariableListElement.getDatabaseVariableName()).toString();

            String records = "";
            for (String otherPatientRecordID : map.keySet()) {
                // records += i + ": " + map.get(i) + "\n";
                if (patientRecordID.equals(otherPatientRecordID)) {
                    // do nothing
//                } else if (patientRecordID.equals(otherPatientRecordID)) {
//                   records += "Patient id: " + otherPatientRecordID + ", score: " + map.get(otherPatientRecordID) + "% (Already matched.)\n";
                } else {
                    records += "Patient id: " + otherPatientRecordID + ", score: " + map.get(otherPatientRecordID) + "%\n";
                }
            }
            if (records.length() > 0) {
                JOptionPane.showInternalMessageDialog(this, "Duplicates found:\n" + records);
            } else {
                JOptionPane.showInternalMessageDialog(this, "No duplicates found.");
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
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHECKS));
    }

    @Action
    public void deleteRecord() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.DELETE));
    }

    @Action
    public void changePatientRecord() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGE_PATIENT_RECORD));
    }

    @Action
    public void setObsoleteFlag() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.OBSOLETE));
    }

    @Action
    public void runMultiplePrimarySearch() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.RUN_MP));
    }

    @Action
    public void calculateAge() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CALC_AGE));
    }
}
