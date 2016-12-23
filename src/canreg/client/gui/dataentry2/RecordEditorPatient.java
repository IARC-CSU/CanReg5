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
import canreg.common.GregorianCalendarCanReg;
import canreg.common.Tools;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.Patient;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author patri_000
 */
public class RecordEditorPatient extends javax.swing.JPanel 
                                 implements RecordEditorPanel, 
                                            ActionListener,
                                            Cloneable,
                                            PropertyChangeListener {

    private DatabaseRecord databaseRecord;
    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private DatabaseGroupsListElement[] groupListElements;
    private final GlobalToolBox globalToolBox;
    private final panelTypes panelType = panelTypes.PATIENT;
    private boolean hasChanged = false;
    private ActionListener actionListener;
    
    private Map<String, VariableEditorPanelInterface> variableEditorPanels;
    private DatabaseVariablesListElement[] variablesInTable;
    private DatabaseVariablesListElement recordStatusVariableListElement;
    private DatabaseVariablesListElement unduplicationVariableListElement;
    //private DatabaseVariablesListElement checkVariableListElement;
    private Map<String, DictionaryEntry> recStatusDictMap;
    private DictionaryEntry[] recStatusDictWithConfirmArray;
    private DictionaryEntry[] recStatusDictWithoutConfirmArray;
    private ResultCode resultCode = null;
    private DatabaseVariablesListElement patientIDVariableListElement;
    private DatabaseVariablesListElement patientRecordIDVariableListElement;
    //private DatabaseVariablesListElement obsoleteFlagVariableListElement;
    private DatabaseVariablesListElement updatedByVariableListElement;
    private DatabaseVariablesListElement updateDateVariableListElement;
    //private DatabaseVariablesListElement tumourSequenceNumberVariableListElement;
    //private DatabaseVariablesListElement tumourSequenceTotalVariableListElement;    
    private final SimpleDateFormat dateFormat;
    private final LinkedList<DatabaseVariablesListElement> autoFillList;
    
    /**
     * To be used ONLY for GUI modeling and mockups, NOT FOR PRODUCTION.
     */
    public RecordEditorPatient() {
        initComponents();
        this.globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();        
        this.dateFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING);
        this.autoFillList = new LinkedList<DatabaseVariablesListElement>();
    }
    
    public RecordEditorPatient(ActionListener listener) {
        initComponents();
        this.actionListener = listener;
        this.globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        
        // setChecksResultCode(resultCode);
        // Remove this for now?
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
        this.dateFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING);
        this.autoFillList = new LinkedList<DatabaseVariablesListElement>();        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(VariableEditorPanelInterface.CHANGED_STRING)) {
            /*if (e.getSource().equals(saveButton)) {
                // do nothing...
            else {*/
                changesDone();
                actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGED));
            //}
        } else {
            // pass it on
            actionListener.actionPerformed(e);
        }
    }
    
    @Override
    public boolean areAllVariablesPresent() {
        boolean allPresent = true;
        for (DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if (panel != null) {
                boolean filledOK = panel.isFilledOK();
                if (!filledOK) 
                    panel.updateFilledInStatusColor();
                
                allPresent = allPresent & filledOK;
            }
        }
        return allPresent;
    }
    
    private void changesDone() {
        setSaveNeeded(true);
        //No checks in Patient panel
        //setChecksResultCode(ResultCode.NotDone);
    }
    
    @Override
    public RecordEditorPatient clone() throws CloneNotSupportedException {
        RecordEditorPatient clone = (RecordEditorPatient) super.clone();
        return clone();
    }
    
    /*Now handled by RecordEditor
    @Action
    public void deleteRecord() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.DELETE));
    }*/
    
    /* ONly for tumour panel
    public void setChecksResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
        String recStatus = null;
        boolean canBeConfirmed = false;
        
        //There's no checksLabel or checksPanel in the Patient panel, only in tumour panel.
        /*if (resultCode == null) {
            checksLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/RecordEditorPanel").getString("NOT_DONE"));
            canBeConfirmed = false;
        } else if (resultCode == ResultCode.NotDone) {
            checksLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/RecordEditorPanel").getString("NOT_DONE"));
            canBeConfirmed = false;
        } else {
            checksLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/RecordEditorPanel").getString("DONE:_") + resultCode.toString());
            if (resultCode == ResultCode.OK || resultCode == ResultCode.Query) {
                canBeConfirmed = true;
            } else if (resultCode == ResultCode.Rare) {
                if (CanRegClientApp.getApplication().getUserRightLevel() == Globals.UserRightLevels.SUPERVISOR) {
                    canBeConfirmed = true;
                }
            }
        }*/
        
        //There's no record status on Patient panel, only in tumour panel.
        // Set record status
        /*if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatus = "0";
            if (hasChanged) {
                recStatus = "0";
            } else {
                recStatus = (String) databaseRecord.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
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
            databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recStatus);
            databaseRecord.setVariable(checkVariableListElement.getDatabaseVariableName(), CheckResult.toDatabaseVariable(resultCode));
        }
    }*/
    
    @Override
    public void setSaveNeeded(boolean saveNeeded) {
        this.hasChanged = saveNeeded;
    }
    
    @Override
    public boolean isSaveNeeded() {        
        for(DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if(panel != null) 
                hasChanged = hasChanged || panel.hasChanged();   
            //Whenever hasChanged is true, than it will never turn to false.
            //We break, so we don't have to check all the panels, is pointless.
            if(hasChanged)
                break;
        }
        return hasChanged;
    }
    
    public void maximizeSize() {
        int heightToGrowBy = this.getHeight() - dataScrollPane.getHeight() + dataPanel.getHeight();
        int widthToGrowBy = this.getWidth() - dataScrollPane.getWidth() + dataPanel.getWidth();
        this.setSize(this.getHeight() + heightToGrowBy, this.getWidth() + widthToGrowBy);
        this.revalidate();
    }
            
    @Override
    public void setDocument(Document doc) {
        this.doc = doc;
    }
    
    @Override
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }
    
    /*No obsolete in Patient panel
    @Action
    public void setObsoleteFlag() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.OBSOLETE));
    }*/
    
    /*Not used in Patient panel
    void setPending(){
         databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), "0");
         refreshDatabaseRecord(databaseRecord);
    }*/
    
    public DatabaseRecord getDatabaseRecord() {
        buildDatabaseRecord();
        return databaseRecord;
    }
    
    /*public void toggleObsolete(boolean confirmed) {
        //Only used in tumour panel
        if (confirmed) {
            DatabaseVariablesListElement dbvle = obsoleteFlagVariableListElement;
            if (dbvle != null) {
                boolean obsolete = obsoleteToggleButton.isSelected();
                if (obsolete) {
                    databaseRecord.setVariable(dbvle.getDatabaseVariableName(), Globals.OBSOLETE_VALUE);
                } else {
                    databaseRecord.setVariable(dbvle.getDatabaseVariableName(), Globals.NOT_OBSOLETE_VALUE);
                }
            }
        } else {
            obsoleteToggleButton.setSelected(!obsoleteToggleButton.isSelected());
        }
    }*/
    
    
    @Override
    public void setDatabaseRecord(DatabaseRecord dbr) {
        this.databaseRecord = dbr;
        setSaveNeeded(false);
        groupListElements = Tools.getGroupsListElements(doc, Globals.NAMESPACE);
        if (databaseRecord.getClass().isInstance(new Patient())) {
            //panelType = panelTypes.PATIENT;
            recordStatusVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordStatus.toString());
            unduplicationVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString());
            patientIDVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
            patientRecordIDVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString());
            /*obsoleteFlagVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString());*/
            updateDateVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString());
            updatedByVariableListElement =
                    globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdatedBy.toString());

        } /*else if (databaseRecord.getClass().isInstance(new Tumour())) {
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
        } else if (databaseRecord.getClass().isInstance(new Source())) {
            panelType = panelTypes.SOURCE;
            recordStatusVariableListElement = null;
            unduplicationVariableListElement = null;
            obsoleteFlagVariableListElement = null;
            checkVariableListElement = null;            
        }*/

        /*
         * Build the record status map.
         */
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatusDictMap = dictionary.get(canreg.client.dataentry.DictionaryHelper
                    .getDictionaryIDbyName(doc, recordStatusVariableListElement.getUseDictionary())).getDictionaryEntries();

            Collection<DictionaryEntry> recStatusDictCollection = recStatusDictMap.values();
            recStatusDictWithConfirmArray =
                    recStatusDictCollection.toArray(new DictionaryEntry[0]);

            LinkedList<DictionaryEntry> recStatusDictWithoutConfirmVector = new LinkedList<DictionaryEntry>();
            for (DictionaryEntry entry : recStatusDictCollection) {
                // "1" is the code for confirmed... TODO: change to dynamic code...
                if (!entry.getCode().equalsIgnoreCase("1")) 
                    recStatusDictWithoutConfirmVector.add(entry);                
            }
            recStatusDictWithoutConfirmArray = recStatusDictWithoutConfirmVector.toArray(new DictionaryEntry[0]);
        }

        String tableName = null;
        
        if(panelType != null) switch (panelType) {
            case PATIENT:
                tableName = Globals.PATIENT_TABLE_NAME;
                //            mpPanel.setVisible(false);
                /*mpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Exact Search")); // This hack works, but is far from ideal...
                changePatientRecordMenuItem.setVisible(false);
                obsoleteToggleButton.setVisible(false);
                checksPanel.setVisible(false);
                sequencePanel.setVisible(false);*/
                break;
            case TUMOUR:
                /*tableName = Globals.TUMOUR_TABLE_NAME;
                personSearchPanel.setVisible(false);
                break;*/
                throw new IllegalArgumentException("This should be a Patient panelType, not a Tumour.");
            case SOURCE:
                /*tableName = Globals.SOURCE_TABLE_NAME;
                systemPanel.setVisible(false);
                break;*/
                throw new IllegalArgumentException("This should be a Patient panelType, not a Source.");
            default:
                break;
         }
                        
        variablesInTable =
                canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        Arrays.sort(variablesInTable, new DatabaseVariablesListElementPositionSorter());
    }
    
    public void setRecordAndBuildPanel(DatabaseRecord dbr) {
        //No checks in Patient panel
        //setChecksResultCode(ResultCode.NotDone);
        setDatabaseRecord(dbr);
        buildPanel();
    }

    @Override
    public void refreshDatabaseRecord(DatabaseRecord record, boolean isSaveNeeded) {
        setDatabaseRecord(record);
        setSaveNeeded(isSaveNeeded);

        buildPanel();

        // set record status and check status
        //No check status in patient
        //refreshCheckStatus(record);
        //No refresh status in patient
        //refreshRecordStatus(record);
        refreshUpdatedBy();
    }
    
   /**
    * Set the obsolete status
    */
    /*On obsolete status in patient
    private void refreshObsoleteStatus(DatabaseRecord record) {        
        String obsoleteStatus = (String) record.getVariable(obsoleteFlagVariableListElement.getDatabaseVariableName());
        if (obsoleteStatus != null && obsoleteStatus.equalsIgnoreCase(Globals.OBSOLETE_VALUE)) 
            obsoleteToggleButton.setSelected(true);
        else 
            obsoleteToggleButton.setSelected(false);        
    }*/
    
    /**
     * Set the record status.
     * @param record 
     */
    /*No record status in patient
    private void refreshRecordStatus(DatabaseRecord record) {                
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithConfirmArray));
            String recStatus = (String) record.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
            if (recStatus != null) {
                recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));
            } else {
                recordStatusComboBox.setSelectedItem(recStatusDictMap.get("0"));
            }
        } else {
            recordStatusPanel.setVisible(false);
        }
    }*/
    
    /* ONLY IN TUMOUR PANEL
    private void refreshSequence() {        
        if (tumourSequenceNumberVariableListElement != null) {
            String tumourSequenceNumberString = (String) databaseRecord.getVariable(tumourSequenceNumberVariableListElement.getDatabaseVariableName());
            sequenceNumberValueLabel.setText(tumourSequenceNumberString);
        }
        if (tumourSequenceTotalVariableListElement != null) {
            String tumourSequenceTotalString = (String) databaseRecord.getVariable(tumourSequenceTotalVariableListElement.getDatabaseVariableName());
            sequenceTotalValueLabel.setText(tumourSequenceTotalString);
        }
    }*/
    
    private void refreshUpdatedBy() {
        String updatedBy = java.util.ResourceBundle
                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient").getString("UNKNOWN_BY");
        String updateDateToolTip = java.util.ResourceBundle
                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient").getString("UNKNOWN_DATE");
        /*
         * Set the updatedBy
         */
        if (updatedByVariableListElement != null) {
            String updatedByString = (String) databaseRecord.getVariable(updatedByVariableListElement.getDatabaseVariableName());
            if (updatedByString != null && updatedByString.trim().length() > 0) 
                updatedBy = updatedByString;            
        }
        userLabel.setText(updatedBy);
        /*
         * Set the update date
         */
        if (updateDateVariableListElement != null) {
            String updateDate = (String) databaseRecord.getVariable(updateDateVariableListElement.getDatabaseVariableName());
            String updateDateString = "";
            if (updateDate != null && updateDate.length() > 0) {
                Date date;
                try {
                    date = dateFormat.parse(updateDate);
                } catch (ParseException ex) {
                    date = null;
                    Logger.getLogger(RecordEditorPatient.class.getName()).log(Level.INFO, null, ex);
                }
                if (date != null) {
                    Calendar todayCal = new GregorianCalendarCanReg();
                    Calendar recordCal = new GregorianCalendarCanReg();
                    recordCal.setTime(date);
                    if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.DAY_OF_YEAR) == recordCal.get(Calendar.DAY_OF_YEAR)) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient").getString("TODAY");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.DAY_OF_YEAR) - recordCal.get(Calendar.DAY_OF_YEAR) == 1) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient").getString("YESTERDAY");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.WEEK_OF_YEAR) == recordCal.get(Calendar.WEEK_OF_YEAR)) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient").getString("THIS_WEEK");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.WEEK_OF_YEAR) - recordCal.get(Calendar.WEEK_OF_YEAR) == 1) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient").getString("LAST_WEEK");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else {
                        updateDateString = DateFormat.getDateInstance().format(date);
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    }
                }
            }
            dateLabel.setText(updateDateString);
        }
        updatedByPanel.setToolTipText(java.util.ResourceBundle
                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorPatient")
                .getString("RECORD_UPDATED_BY_") + updatedBy + ", " + updateDateToolTip);
    }
    
    /*Only in tumour panel
    @Action
    public void runChecksAction() {       
        autoFill();
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHECKS));
    }*/
    
    @Action
    public void runPersonSearch() {
        autoFill();
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.PERSON_SEARCH));
    }
        
    @Override
    public void prepareToSaveRecord() {
        buildDatabaseRecord();
                
        //actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.SAVE));
        
        //COMMENTED: the next code is already executed when the record is saved 
        //by executing refreshDatabaseRecord()
        /*Iterator<VariableEditorPanelInterface> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanelInterface vep = iterator.next();
            vep.setSaved();
        }*/
    }
    
    void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }
    
    @Action
    public void runExactSearch() {
        /*if ( panelType == panelTypes.TUMOUR )
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.RUN_MP));
        else */
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.RUN_EXACT));
    }

    @Override
    public LinkedList<DatabaseVariablesListElement> getAutoFillList() {
        return autoFillList;
    }

    @Override
    public void setVariable(DatabaseVariablesListElement variable, String value) {
        VariableEditorPanelInterface vep = variableEditorPanels.get(variable.getDatabaseVariableName());
        vep.setValue(value);
    }

    /**
     * Set the resultcode of individual variables.
     * @param databaseVariableName
     * @param resultCode 
     */
    @Override
    public void setResultCodeOfVariable(String databaseVariableName, CheckResult.ResultCode resultCode) {
        VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariableName);
        panel.setResultCode(resultCode);
    }
    
    @Action
    public void autoFill() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.AUTO_FILL));
    }
    
    private void buildDatabaseRecord() {
        Iterator<VariableEditorPanelInterface> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanelInterface vep = iterator.next();
            databaseRecord.setVariable(vep.getKey(), vep.getValue());
        }
        
        /*if (panelType == panelTypes.TUMOUR) {
            Tumour tumour = (Tumour) databaseRecord;
            tumour.setSources(sourcesPanel.getSources());
        }*/

        if (recordStatusVariableListElement != null) {
            if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
                
                //COMMENTED: No record status in patient panel.
                /*DictionaryEntry recordStatusValue = (DictionaryEntry) recordStatusComboBox.getSelectedItem();
                if (recordStatusValue != null) {
                    databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recordStatusValue.getCode());
                } else {*/
                
                    databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), "0");
                    // JOptionPane.showInternalMessageDialog(this, "Record status dictionary entries missing.");
                    Logger.getLogger(RecordEditorPatient.class.getName()).log(Level.WARNING, 
                                     "Warning! Record status dictionary entries missing.");
                //}
            } else {
                databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), "0");
                // JOptionPane.showInternalMessageDialog(this, "Record status dictionary entries missing.");
                Logger.getLogger(RecordEditorPatient.class.getName()).log(Level.WARNING, 
                                 "Warning! Record status dictionary entries missing.");
            }
        }
        
        //No obsolete flag in patient
        /*if (obsoleteFlagVariableListElement != null) {
            if (obsoleteToggleButton.isSelected()) {
                databaseRecord.setVariable(obsoleteFlagVariableListElement.getDatabaseVariableName(), Globals.OBSOLETE_VALUE);
            } else {
                databaseRecord.setVariable(obsoleteFlagVariableListElement.getDatabaseVariableName(), Globals.NOT_OBSOLETE_VALUE);
            }
        }
        
        //No check status in patient
        if (checkVariableListElement != null) {
            if (resultCode == null) {
                resultCode = ResultCode.NotDone;
            }
            databaseRecord.setVariable(checkVariableListElement.getDatabaseVariableName(),
                    CheckResult.toDatabaseVariable(resultCode));
        }*/
    }
    
    private void buildPanel() {
        dataPanel.removeAll();

        if (variableEditorPanels != null) {
            for (VariableEditorPanelInterface vep : variableEditorPanels.values()) 
                vep.removeListener();            
        }
        variableEditorPanels = new LinkedHashMap();
        Map<Integer, VariableEditorGroupPanel> groupIDtoPanelMap = new LinkedHashMap<Integer, VariableEditorGroupPanel>();
        //Map<String, DictionaryEntry> possibleValues;

        for (int i = 0; i < variablesInTable.length; i++) {
            DatabaseVariablesListElement currentVariable = variablesInTable[i];
            VariableEditorPanel vep;
            String variableType = currentVariable.getVariableType();

            if (Globals.VARIABLE_TYPE_DATE_NAME.equalsIgnoreCase(variableType)) 
                vep = new DateVariableEditorPanel(this);
            else if (Globals.VARIABLE_TYPE_TEXT_AREA_NAME.equalsIgnoreCase(variableType)) 
                vep = new TextFieldVariableEditorPanel(this);
            else if(dictionary.get(currentVariable.getDictionaryID()) != null && currentVariable.getDictionaryID() >= 0)
                vep = new DictionaryVariableEditorPanel(this);
            else
                vep = new VariableEditorPanel(this);            

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
            if (variableValue != null) 
                vep.setInitialValue(variableValue.toString());            

            String variableFillStatus = currentVariable.getFillInStatus();
            if (Globals.FILL_IN_STATUS_AUTOMATIC_STRING.equalsIgnoreCase(variableFillStatus)) 
                autoFillList.add(currentVariable);            

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
        for (DatabaseGroupsListElement groupListElement : groupListElements) {
            int groupID = groupListElement.getGroupIndex();
            JPanel panel = groupIDtoPanelMap.get(groupID);
            if (panel != null) {
                dataPanel.add(panel);
                panel.setVisible(true);
            }
        }

        //This panel only holds a patient
        /*if (panelType == panelTypes.TUMOUR) {
            sourcesPanel = new SourcesPanel(this);
            sourcesPanel.setDictionary(dictionary);
            sourcesPanel.setDoc(doc);
            sourcesPanel.setVisible(true);
            Tumour tumour = (Tumour) databaseRecord;
            sourcesPanel.setSources(tumour.getSources());
            dataPanel.add(sourcesPanel);
            refreshSequence();
        }*/
        if (panelType != panelTypes.SOURCE) {
            //No obsolete status in patient
            //refreshObsoleteStatus(databaseRecord);
            //No record status in patient
            //refreshRecordStatus(databaseRecord);
            //No check status in patient
            //refreshCheckStatus(databaseRecord);
        }
        refreshUpdatedBy();
        dataPanel.revalidate();
        dataPanel.repaint();
    }
    
    //This is never used
    /*@Action
    public void calculateAge() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CALC_AGE));
    }*/
    
    /*
    //Only in tumour panel
    @Action
    public void changePatientRecord() {        
        //actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGE_PATIENT_RECORD));
    }*/
    
   /**
    * Set the check status
    */
    /* THERE'S NO CHECK STATUS ON PATIENT, ONLY IN TUMOUR
    private void refreshCheckStatus(DatabaseRecord record) {        
        if (checkVariableListElement != null) {
            Object checkStatus = record.getVariable(checkVariableListElement.getDatabaseVariableName());
            if (checkStatus != null) {
                String checkStatusString = (String) checkStatus;
                resultCode = CheckResult.toResultCode(checkStatusString);
                // setSaveNeeded(false);
                setChecksResultCode(resultCode);
            } else {
                // String checkStatusString = (String) checkStatus;
                // resultCode = CheckResult.toResultCode(checkStatusString);
                setSaveNeeded(true);
                setChecksResultCode(ResultCode.NotDone);
            }
        }
    }*/

    @Override
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
            //Temporarily disabled
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGED));
            // saveButton.setEnabled(saveNeeded);
        } else {
            // Do nothing.
        }
    }    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        systemPanel = new javax.swing.JPanel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        buttonsPanel = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        exactButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        updatedByPanel = new javax.swing.JPanel();
        byLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        dataScrollPane = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(600, 510));

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        systemPanel.setMaximumSize(new java.awt.Dimension(32767, 100));
        systemPanel.setPreferredSize(new java.awt.Dimension(1062, 80));
        systemPanel.setLayout(new javax.swing.BoxLayout(systemPanel, javax.swing.BoxLayout.LINE_AXIS));
        systemPanel.add(filler4);

        buttonsPanel.setMaximumSize(new java.awt.Dimension(32767, 95));
        buttonsPanel.setPreferredSize(new java.awt.Dimension(130, 50));
        buttonsPanel.setLayout(new javax.swing.BoxLayout(buttonsPanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditorPatient.class, this);
        searchButton.setAction(actionMap.get("runPersonSearch")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditorPatient.class);
        searchButton.setText(resourceMap.getString("personSearchPanel.border.title")); // NOI18N
        searchButton.setToolTipText(resourceMap.getString("searchButton.toolTipText"));
        searchButton.setMaximumSize(new java.awt.Dimension(200, 45));
        buttonsPanel.add(searchButton);
        buttonsPanel.add(filler1);

        exactButton.setAction(actionMap.get("runExactSearch")); // NOI18N
        exactButton.setText(resourceMap.getString("exactSearchButton.text")); // NOI18N
        exactButton.setToolTipText(resourceMap.getString("exactSearchButton.Action.shortDescription")); // NOI18N
        exactButton.setMaximumSize(new java.awt.Dimension(200, 45));
        buttonsPanel.add(exactButton);

        systemPanel.add(buttonsPanel);
        systemPanel.add(filler3);
        systemPanel.add(filler5);

        updatedByPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("updatedByPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        updatedByPanel.setMaximumSize(new java.awt.Dimension(32767, 95));
        updatedByPanel.setMinimumSize(new java.awt.Dimension(100, 30));

        byLabel.setText(resourceMap.getString("byLabel.text")); // NOI18N
        byLabel.setName("byLabel"); // NOI18N

        userLabel.setText(resourceMap.getString("userLabel.text")); // NOI18N
        userLabel.setName("userLabel"); // NOI18N

        dateLabel.setText(resourceMap.getString("dateLabel.text")); // NOI18N
        dateLabel.setName("dateLabel"); // NOI18N

        javax.swing.GroupLayout updatedByPanelLayout = new javax.swing.GroupLayout(updatedByPanel);
        updatedByPanel.setLayout(updatedByPanelLayout);
        updatedByPanelLayout.setHorizontalGroup(
            updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(updatedByPanelLayout.createSequentialGroup()
                        .addComponent(byLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                    .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        updatedByPanelLayout.setVerticalGroup(
            updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanelLayout.createSequentialGroup()
                .addGroup(updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(byLabel)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        systemPanel.add(updatedByPanel);

        jPanel2.add(systemPanel);

        dataScrollPane.setBorder(null);

        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        dataScrollPane.setViewportView(dataPanel);

        jPanel2.add(dataScrollPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JLabel byLabel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScrollPane;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JButton exactButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JPanel updatedByPanel;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    
}
