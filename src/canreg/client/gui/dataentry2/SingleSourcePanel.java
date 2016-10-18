/*
 * Copyright (C) 2016 patri_000
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
 */
package canreg.client.gui.dataentry2;

import canreg.client.CanRegClientApp;
import canreg.client.gui.components.VariableEditorPanelInterface;
import canreg.client.gui.dataentry2.RecordEditorPanel;
import canreg.client.gui.dataentry2.RecordEditorPanel.panelTypes;
import canreg.client.gui.dataentry2.components.DateVariableEditorPanel;
import canreg.client.gui.dataentry2.components.DictionaryVariableEditorPanel;
import canreg.client.gui.dataentry2.components.TextFieldVariableEditorPanel;
import canreg.client.gui.dataentry2.components.VariableEditorGroupPanel;
import canreg.client.gui.dataentry2.components.VariableEditorPanel;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.DatabaseVariablesListElementPositionSorter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.Source;
import canreg.common.qualitycontrol.CheckResult;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author patri_000
 */
public class SingleSourcePanel extends javax.swing.JPanel implements ActionListener {

    private Map<Integer, Dictionary> dictionary;
    private Document doc;
    private final ActionListener listener;
    private DatabaseRecord databaseRecord;
    private boolean hasChanged = false;
    private DatabaseGroupsListElement[] groupListElements;
    private DatabaseVariablesListElement[] variablesInTable;
    private Map<String, VariableEditorPanelInterface> variableEditorPanels;
    private final LinkedList<DatabaseVariablesListElement> autoFillList;
    private final GlobalToolBox globalToolBox;
    private panelTypes panelType;
   
    public SingleSourcePanel(ActionListener listener) {
        initComponents();
        this.listener = listener; 
        autoFillList = new LinkedList<DatabaseVariablesListElement>();
        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
    }
               
    public void setRecordAndBuildPanel(DatabaseRecord dbr) {
        //setChecksResultCode(ResultCode.NotDone);
        setDatabaseRecord(dbr);
        buildPanel();
    }
    
    public void setSaveNeeded(boolean saveNeeded) {
        this.hasChanged = saveNeeded;
    }
    
    public DatabaseRecord getDatabaseRecord() {
        return this.databaseRecord;
    }
    
    private void setDatabaseRecord(DatabaseRecord dbr) {
        this.databaseRecord = dbr;
        setSaveNeeded(false);
        groupListElements = Tools.getGroupsListElements(doc, Globals.NAMESPACE);
        /*if (databaseRecord.getClass().isInstance(new Patient())) {
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
            updateDateVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString());
            updatedByVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdatedBy.toString());

        } else if (databaseRecord.getClass().isInstance(new Tumour())) {
            panelType = panelTypes.TUMOUR;
            recordStatusVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString());
            obsoleteFlagVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString());
            checkVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.CheckStatus.toString());
            updateDateVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString());
            updatedByVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdatedBy.toString());
            tumourSequenceNumberVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimSeq.toString());
            tumourSequenceTotalVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimTot.toString());
        } else*/ if (databaseRecord.getClass().isInstance(new Source())) {
            panelType = panelTypes.SOURCE;
            /*recordStatusVariableListElement = null;
            unduplicationVariableListElement = null;
            obsoleteFlagVariableListElement = null;
            checkVariableListElement = null;*/
        }

        /*
         * Build the record status map.
         */
        /*if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatusDictMap = dictionary.get(canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, recordStatusVariableListElement.getUseDictionary())).getDictionaryEntries();

            Collection<DictionaryEntry> recStatusDictCollection = recStatusDictMap.values();
            recStatusDictWithConfirmArray =
                    recStatusDictCollection.toArray(new DictionaryEntry[0]);

            LinkedList<DictionaryEntry> recStatusDictWithoutConfirmVector = new LinkedList<DictionaryEntry>();
            for (DictionaryEntry entry : recStatusDictCollection) {
                // "1" is the code for confirmed... TODO: change to dynamic code...
                if (!entry.getCode().equalsIgnoreCase("1")) {
                    recStatusDictWithoutConfirmVector.add(entry);
                }
            }
            recStatusDictWithoutConfirmArray = recStatusDictWithoutConfirmVector.toArray(new DictionaryEntry[0]);
        }*/        

        String tableName = null;
        /*if (panelType == panelTypes.PATIENT) {
            tableName = Globals.PATIENT_TABLE_NAME;
//            mpPanel.setVisible(false);
            mpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Exact Search")); // This hack works, but is far from ideal...
            changePatientRecordMenuItem.setVisible(false);
            obsoleteToggleButton.setVisible(false);
            checksPanel.setVisible(false);
            sequencePanel.setVisible(false);
        } else if (panelType == panelTypes.TUMOUR) {
            tableName = Globals.TUMOUR_TABLE_NAME;
            personSearchPanel.setVisible(false);
        } else*/ if (panelType == panelTypes.SOURCE) {
            tableName = Globals.SOURCE_TABLE_NAME;
            //systemPanel.setVisible(false);
        }
        variablesInTable = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        Arrays.sort(variablesInTable, new DatabaseVariablesListElementPositionSorter());
    }
    
    private void buildPanel() {
        dataPanel.removeAll();

        if (variableEditorPanels != null) {
            for (VariableEditorPanelInterface vep : variableEditorPanels.values()) {
                vep.removeListener();
            }
        }
        variableEditorPanels = new LinkedHashMap();

        Map<Integer, VariableEditorGroupPanel> groupIDtoPanelMap = new LinkedHashMap<Integer, VariableEditorGroupPanel>();
        Map<String, DictionaryEntry> possibleValues;

        for (int i = 0; i < variablesInTable.length; i++) {
            DatabaseVariablesListElement currentVariable = variablesInTable[i];
            VariableEditorPanel vep;

            String variableType = currentVariable.getVariableType();
            
            if (Globals.VARIABLE_TYPE_DATE_NAME.equalsIgnoreCase(variableType)) {
                vep = new DateVariableEditorPanel(this);
            } else if (Globals.VARIABLE_TYPE_TEXT_AREA_NAME.equalsIgnoreCase(variableType)) {
                vep = new TextFieldVariableEditorPanel(this);
            } else if(currentVariable.getDictionaryID() >= 0 && dictionary.get(currentVariable.getDictionaryID()) != null)
                vep = new DictionaryVariableEditorPanel(this);
            else {
                vep = new VariableEditorPanel(this);
            }

            vep.setDatabaseVariablesListElement(currentVariable);

            int dictionaryID = currentVariable.getDictionaryID();
            if (dictionaryID >= 0) {
                Dictionary dic = dictionary.get(dictionaryID);
                if (dic != null)            
                    ((DictionaryVariableEditorPanel)vep).setDictionary(dic);                
            } else {
                //vep.setDictionary(null);
            }

            String variableName = currentVariable.getDatabaseVariableName();
            Object variableValue = databaseRecord.getVariable(variableName);
            if (variableValue != null) {
                vep.setInitialValue(variableValue.toString());
            }

            String variableFillStatus = currentVariable.getFillInStatus();
            if (Globals.FILL_IN_STATUS_AUTOMATIC_STRING.equalsIgnoreCase(variableFillStatus)) {
                autoFillList.add(currentVariable);
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
        for (DatabaseGroupsListElement groupListElement : groupListElements) {
            int groupID = groupListElement.getGroupIndex();
            JPanel panel = groupIDtoPanelMap.get(groupID);
            if (panel != null) {
                dataPanel.add(panel);
                panel.setVisible(true);
            }
        }

        // If this is the tumour part we add the source table
        /*if (panelType == panelTypes.TUMOUR) {
            sourcesPanel = new SourcesPanel(this);
            sourcesPanel.setDictionary(dictionary);
            sourcesPanel.setDoc(doc);
            sourcesPanel.setVisible(true);
            Tumour tumour = (Tumour) databaseRecord;
            sourcesPanel.setSources(tumour.getSources());
            dataPanel.add(sourcesPanel);
            refreshSequence();
        }

        if (panelType != panelTypes.SOURCE) {
            refreshObsoleteStatus(databaseRecord);
            refreshRecordStatus(databaseRecord);
            refreshCheckStatus(databaseRecord);
        }
        refreshUpdatedBy();*/
        dataPanel.revalidate();
        dataPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // forward the event...
        listener.actionPerformed(e);
    }
   
    public Map<Integer, Dictionary> getDictionary() {
        return dictionary;
    }

   
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }
   
    public Document getDocument() {
        return doc;
    }
   
    public void setDocument(Document doc) {
        this.doc = doc;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataScrollPane = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        dataScrollPane.setBorder(null);

        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        dataScrollPane.setViewportView(dataPanel);

        add(dataScrollPane);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScrollPane;
    // End of variables declaration//GEN-END:variables
   
}
