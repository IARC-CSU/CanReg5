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
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import java.awt.Component;
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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author patri_000
 */
public class RecordEditorTumour extends javax.swing.JPanel 
        implements RecordEditorPanel, ActionListener, Cloneable, PropertyChangeListener {
   
    private DatabaseRecord databaseRecord;
    private Document doc;
    private final panelTypes panelType = panelTypes.TUMOUR;
    private DatabaseVariablesListElement[] variablesInTable;
    private Map<String, VariableEditorPanelInterface> variableEditorPanels;
    private Map<Integer, Dictionary> dictionary;
    private DatabaseGroupsListElement[] groupListElements;
    private final GlobalToolBox globalToolBox;
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
    private DatabaseVariablesListElement updatedByVariableListElement;
    private DatabaseVariablesListElement updateDateVariableListElement;
    private DatabaseVariablesListElement tumourSequenceNumberVariableListElement;
    private DatabaseVariablesListElement tumourSequenceTotalVariableListElement;
    //private RecordEditorSource sourcesPanel;
    private final SimpleDateFormat dateFormat;
    private final LinkedList<DatabaseVariablesListElement> autoFillList;
    private final RecordEditor recordEditor;
    private final org.jdesktop.application.ResourceMap resourceMap;
    private Set<Source> sources;
    
    
    public RecordEditorTumour(ActionListener listener, 
                              RecordEditor recordEditor) {
        initComponents(); 
        this.actionListener = listener;
        this.recordEditor = recordEditor;

        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        //saveButton.setEnabled(true);
        // setChecksResultCode(resultCode);
        // Remove this for now?
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);

        // TODO: update the label of person search and MP search
        // for now - hide this untill it is used...
        //searchLabel.setVisible(false);
        //mpLabel.setVisible(false);

        dateFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING);
        autoFillList = new LinkedList<DatabaseVariablesListElement>();
        
        //tumourLinkedPanel.setVisible(false);        
        
        resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class)
                .getContext().getResourceMap(RecordEditorTumour.class);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("Changed")) {
            /*if (e.getSource().equals(saveButton)) {
                // do nothing...
            } else {*/
                changesDone();
                actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGED));
            //}
        } else {
            // pass it on
            actionListener.actionPerformed(e);
        }
    }
    
    private void buildDatabaseRecord() {
        Iterator<VariableEditorPanelInterface> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanelInterface vep = iterator.next();
            databaseRecord.setVariable(vep.getKey(), vep.getValue());
        }

        if (panelType == panelTypes.TUMOUR) {
            Tumour tumour = (Tumour) databaseRecord;
            tumour.setSources(getSources());
        }

        if (recordStatusVariableListElement != null) {
            if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
                DictionaryEntry recordStatusValue = (DictionaryEntry) recordStatusComboBox.getSelectedItem();
                if (recordStatusValue != null) 
                    databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recordStatusValue.getCode());
                else {
                    databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), "0");
                    // JOptionPane.showInternalMessageDialog(this, "Record status dictionary entries missing.");
                    Logger.getLogger(RecordEditorTumour.class.getName()).log(Level.WARNING, "Warning! Record status dictionary entries missing.");
                }
            } else {
                databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), "0");
                // JOptionPane.showInternalMessageDialog(this, "Record status dictionary entries missing.");
                Logger.getLogger(RecordEditorTumour.class.getName()).log(Level.WARNING, "Warning! Record status dictionary entries missing.");
            }
        }
        if (obsoleteFlagVariableListElement != null) {
            if (recordEditor.isObsoleteToggleButtonSelected(this))
                databaseRecord.setVariable(obsoleteFlagVariableListElement.getDatabaseVariableName(), Globals.OBSOLETE_VALUE);
            else 
                databaseRecord.setVariable(obsoleteFlagVariableListElement.getDatabaseVariableName(), Globals.NOT_OBSOLETE_VALUE);            
        }
        if (checkVariableListElement != null) {
            if (resultCode == null) 
                resultCode = ResultCode.NotDone;            
            databaseRecord.setVariable(checkVariableListElement.getDatabaseVariableName(),
                    CheckResult.toDatabaseVariable(resultCode));
        }
    }
    
    private void buildPanel() {
        dataPanel.removeAll();

        if (variableEditorPanels != null) {
            for (VariableEditorPanelInterface vep : variableEditorPanels.values()) 
                vep.removeListener();            
        }
        
        variableEditorPanels = new LinkedHashMap();
        Map<Integer, VariableEditorGroupPanel> groupIDtoPanelMap = new LinkedHashMap<Integer, VariableEditorGroupPanel>();        

        for(int i = 0; i < variablesInTable.length; i++) {
            DatabaseVariablesListElement currentVariable = variablesInTable[i];
            VariableEditorPanel vep;

            String variableType = currentVariable.getVariableType();

            if (Globals.VARIABLE_TYPE_DATE_NAME.equalsIgnoreCase(variableType)) 
                vep = new DateVariableEditorPanel(this);
            else if (Globals.VARIABLE_TYPE_TEXT_AREA_NAME.equalsIgnoreCase(variableType)) 
                vep = new TextFieldVariableEditorPanel(this);
            else if(currentVariable.getDictionaryID() >= 0 && dictionary.get(currentVariable.getDictionaryID()) != null)
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

        //If this is the tumour part we add the source table
        if (panelType == panelTypes.TUMOUR) {
            //In this implementation the sources tabbed pane is always present
            /*sourcesPanel = new SourcesPanel(this);
            sourcesPanel.setDictionary(dictionary);
            sourcesPanel.setDoc(doc);
            sourcesPanel.setVisible(true);*/
            
            Tumour tumour = (Tumour) databaseRecord;
            
            //sourcesPanel.setSources(tumour.getSources());
            this.setSources(tumour.getSources());
                        
            //dataPanel.add(sourcesPanel);
            refreshSequence();
        }
        if (panelType != panelTypes.SOURCE) {
            refreshObsoleteStatus(databaseRecord);
            refreshRecordStatus(databaseRecord);
            refreshCheckStatus(databaseRecord);
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
            
    private void changesDone() {
        setSaveNeeded(true);
        setChecksResultCode(ResultCode.NotDone);
    }
    
    @Override
    public RecordEditorTumour clone() throws CloneNotSupportedException {
        RecordEditorTumour clone = (RecordEditorTumour) super.clone();
        return clone();
    }        
    
    @Override
    public LinkedList<DatabaseVariablesListElement> getAutoFillList() {
        return autoFillList;
    }
    
    public void setSources(Set<Source> sources) {
        if (sources == null) 
            sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
        
        if (sources.isEmpty()) 
            sources.add(new Source());
        
        this.sources = sources;
        buildTabs();
        refreshTitles();
        //setScrollPaneSize();
    }
    
    public Set<Source> getSources() {        
        sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
        for (Component comp : sourcesTabbedPane.getComponents()) {
            RecordEditorSource rep = (RecordEditorSource) comp;
            Source source = (Source) rep.getDatabaseRecord();
            sources.add(source);
        }
        return sources;
    }
    
    private void buildTabs() {
        sourcesTabbedPane.removeAll();
        for (Source source : sources) {
            RecordEditorSource newPanel = new RecordEditorSource(this);
            newPanel.setDictionary(dictionary);
            newPanel.setDocument(doc);
            newPanel.setRecordAndBuildPanel(source);
            sourcesTabbedPane.add(newPanel);
        }
    }
    
    @Action
    public void addSourceAction() {
        Source newSource = new Source();
        RecordEditorSource newPanel = new RecordEditorSource(this);
        newPanel.setDictionary(dictionary);
        newPanel.setDocument(doc);
        newPanel.setRecordAndBuildPanel(newSource);
        sources.add(newSource);
        sourcesTabbedPane.add(newPanel);
        refreshTitles();
        //setScrollPaneSize();
    }
    
    @Action
    public void removeSourceAction() {
        RecordEditorSource oldPanel = (RecordEditorSource) sourcesTabbedPane.getSelectedComponent();
        Source oldSource = (Source) oldPanel.getDatabaseRecord();
        sources.remove(oldSource);
        sourcesTabbedPane.remove(oldPanel);
    }
    
    private void refreshTitles() {
        int index = 0;
        for(Component comp : sourcesTabbedPane.getComponents()) {
            sourcesTabbedPane.setTitleAt(index, java.util.ResourceBundle
                    .getBundle("canreg/client/gui/dataentry2/resources/SingleSourcePanel").getString("SOURCE:_") + (index + 1));
            index++;
        }
    }
    
    @Override
    public boolean isSaveNeeded() {
        // hasChanged = false;

        for (DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if (panel != null) 
                hasChanged = hasChanged || panel.hasChanged();            
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
    public void propertyChange(PropertyChangeEvent e) {
        String propName = e.getPropertyName();
        if ("focusOwner".equals(propName)) {
            if (e.getNewValue() instanceof JTextField) {
                JTextField textField = (JTextField) e.getNewValue();
                textField.selectAll();
            }
        } 
        //Called when a field's "value" property changes.
        else if ("value".equals(propName)) {
            setSaveNeeded(true);
            //Temporarily disabled
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGED));
            // saveButton.setEnabled(saveNeeded);
        } else {
            // Do nothing.
        }
    }
    
    private void refreshCheckStatus(DatabaseRecord record) {
        // Set the check status
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
    }
    
    @Override
    public void refreshDatabaseRecord(DatabaseRecord record) {
        setDatabaseRecord(record);
        setSaveNeeded(false);

        buildPanel();

        // set record status and check status
        refreshCheckStatus(record);
        refreshRecordStatus(record);
        refreshUpdatedBy();
    }
    
   /**
     * Set the obsolete status
     */
   private void refreshObsoleteStatus(DatabaseRecord record) {       
        String obsoleteStatus = (String) record.getVariable(obsoleteFlagVariableListElement.getDatabaseVariableName());
        if (obsoleteStatus != null && obsoleteStatus.equalsIgnoreCase(Globals.OBSOLETE_VALUE)) 
            recordEditor.setObsoleteToggleButtonSelected(this, true);
        else 
            recordEditor.setObsoleteToggleButtonSelected(this, false);
    }        
    
    @Override
    public void setDocument(Document doc) {
        this.doc = doc;
    }
    
    @Override
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }    
          
    void setPending(){
         databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), "0");
         refreshDatabaseRecord(databaseRecord);
    }
    
    public void setRecordAndBuildPanel(DatabaseRecord dbr) {
        setChecksResultCode(ResultCode.NotDone);
        setDatabaseRecord(dbr);
        buildPanel();
    }
    
    @Override
    public void setSaveNeeded(boolean saveNeeded) {
        this.hasChanged = saveNeeded;
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
    public void setResultCodeOfVariable(String databaseVariableName, ResultCode resultCode) {
        VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariableName);
        panel.setResultCode(resultCode);
    }
    
    @Override
    public void setDatabaseRecord(DatabaseRecord dbr) {
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
        } else*/ if (databaseRecord.getClass().isInstance(new Tumour())) {
            //panelType = panelTypes.TUMOUR;
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
        } /*else if (databaseRecord.getClass().isInstance(new Source())) {
            panelType = panelTypes.SOURCE;
            recordStatusVariableListElement = null;
            unduplicationVariableListElement = null;
            obsoleteFlagVariableListElement = null;
            checkVariableListElement = null;            
        }*/

        //Build the record status map.
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatusDictMap = dictionary.get(canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, recordStatusVariableListElement.getUseDictionary())).getDictionaryEntries();

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
                /*tableName = Globals.PATIENT_TABLE_NAME;
                //            mpPanel.setVisible(false);
                mpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Exact Search")); // This hack works, but is far from ideal...
                changePatientRecordMenuItem.setVisible(false);
                obsoleteToggleButton.setVisible(false);
                checksPanel.setVisible(false);
                sequencePanel.setVisible(false);
                break;*/
                throw new IllegalArgumentException("This should be a Tumour panelType, not a Patient.");
            case TUMOUR:
                tableName = Globals.TUMOUR_TABLE_NAME;
                //personSearchPanel.setVisible(false);
                break;                
            case SOURCE:
                /*tableName = Globals.SOURCE_TABLE_NAME;
                systemPanel.setVisible(false);
                break;*/
                throw new IllegalArgumentException("This should be a Tumour panelType, not a Source.");
            default:
                break;
        }
        
        variablesInTable =
                canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        Arrays.sort(variablesInTable, new DatabaseVariablesListElementPositionSorter());
    }

    @Override
    public DatabaseRecord getDatabaseRecord() {
        buildDatabaseRecord();
        return databaseRecord;
    }
    
    public DatabaseVariablesListElement getObsoleteFlagVariableListElement() {
        return obsoleteFlagVariableListElement;
    }
    
    private void refreshRecordStatus(DatabaseRecord record) {        
        //Set the record status.        
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithConfirmArray));
            String recStatus = (String) record.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
            if (recStatus != null) 
                recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));
            else 
                recordStatusComboBox.setSelectedItem(recStatusDictMap.get("0"));            
        } else 
            recordStatusPanel.setVisible(false);        
    }
    
    private void refreshSequence() {
        if (tumourSequenceNumberVariableListElement != null) {
            String tumourSequenceNumberString = (String) databaseRecord.getVariable(tumourSequenceNumberVariableListElement.getDatabaseVariableName());
            sequenceNumberValueLabel.setText(tumourSequenceNumberString);
        }
        if (tumourSequenceTotalVariableListElement != null) {
            String tumourSequenceTotalString = (String) databaseRecord.getVariable(tumourSequenceTotalVariableListElement.getDatabaseVariableName());
            sequenceTotalValueLabel.setText(tumourSequenceTotalString);
        }
    }
    
    private void refreshUpdatedBy() {
        String updatedBy = java.util.ResourceBundle
                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("UNKNOWN_BY");
        String updateDateToolTip = java.util.ResourceBundle
                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("UNKNOWN_DATE");
        
        //Set the updatedBy
        if (updatedByVariableListElement != null) {
            String updatedByString = (String) databaseRecord.getVariable(updatedByVariableListElement.getDatabaseVariableName());
            if (updatedByString != null && updatedByString.trim().length() > 0) 
                updatedBy = updatedByString;            
        }
        userLabel.setText(updatedBy);
        
        //Set the update date
        if (updateDateVariableListElement != null) {
            String updateDate = (String) databaseRecord.getVariable(updateDateVariableListElement.getDatabaseVariableName());
            String updateDateString = "";
            if (updateDate != null && updateDate.length() > 0) {
                Date date;
                try {
                    date = dateFormat.parse(updateDate);
                } catch (ParseException ex) {
                    date = null;
                    Logger.getLogger(RecordEditorTumour.class.getName()).log(Level.INFO, null, ex);
                }
                if (date != null) {
                    Calendar todayCal = new GregorianCalendarCanReg();
                    Calendar recordCal = new GregorianCalendarCanReg();
                    recordCal.setTime(date);
                    if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.DAY_OF_YEAR) == recordCal.get(Calendar.DAY_OF_YEAR)) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("TODAY");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.DAY_OF_YEAR) - recordCal.get(Calendar.DAY_OF_YEAR) == 1) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("YESTERDAY");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.WEEK_OF_YEAR) == recordCal.get(Calendar.WEEK_OF_YEAR)) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("THIS_WEEK");
                        updateDateToolTip = DateFormat.getDateInstance().format(date);
                    } else if (todayCal.get(Calendar.YEAR) == recordCal.get(Calendar.YEAR)
                            && todayCal.get(Calendar.WEEK_OF_YEAR) - recordCal.get(Calendar.WEEK_OF_YEAR) == 1) {
                        updateDateString = java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("LAST_WEEK");
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
                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour")
                .getString("RECORD_UPDATED_BY_") + updatedBy + ", " + updateDateToolTip);
    }
   
    @Action
    public void runChecksAction() {
        autoFill();
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHECKS));
    }
    
    @Action
    public void runMultiplePrimarySearch() {
        //if ( panelType == panelTypes.TUMOUR )
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.RUN_MP));
        /*else 
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.RUN_EXACT));*/
    }
    
    /*Not present in Tumour panel
    @Action
    public void runPersonSearch() {
        autoFill();
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.PERSON_SEARCH));
    }*/
    
    @Action
    public void saveRecord() {
        buildDatabaseRecord();
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.SAVE));
        Iterator<VariableEditorPanelInterface> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanelInterface vep = iterator.next();
            vep.setSaved();
        }
    }
    
    void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    public void setChecksResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
        String recStatus = null;
        boolean canBeConfirmed = false;
        
        if (resultCode == null
            || resultCode == ResultCode.NotDone) {            
            checksButton.setText(resourceMap.getString("checksPanel.border.title") + " " +
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour").getString("NOT_DONE"));
            //Red colored border
            checksButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 0), 2));
            canBeConfirmed = false;
        } else {            
            checksButton.setText(resourceMap.getString("checksPanel.border.title") + " " +
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorTumour")
                            .getString("DONE:_") + resultCode.toString());  
            
            if (resultCode == ResultCode.OK || resultCode == ResultCode.Query) {
                //Green colored border
                checksButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(32, 166, 72), 2));
                canBeConfirmed = true;
            } else if (resultCode == ResultCode.Rare) {
                //Yellow colored border
                checksButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(249, 238, 43), 2));
                if (CanRegClientApp.getApplication().getUserRightLevel() == Globals.UserRightLevels.SUPERVISOR) 
                    canBeConfirmed = true;                
            }
        }
        // Set record status
        if (recordStatusVariableListElement != null && recordStatusVariableListElement.getUseDictionary() != null) {
            recStatus = "0";
            if (hasChanged) 
                recStatus = "0";
            else 
                recStatus = (String) databaseRecord.getVariable(recordStatusVariableListElement.getDatabaseVariableName());
           
            if (canBeConfirmed) {
                recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithConfirmArray));
                if (recStatus != null) 
                    recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));                
            } else {
                recordStatusComboBox.setModel(new DefaultComboBoxModel(recStatusDictWithoutConfirmArray));
                if (recStatus != null) 
                    recordStatusComboBox.setSelectedItem(recStatusDictMap.get(recStatus));                
            }
            databaseRecord.setVariable(recordStatusVariableListElement.getDatabaseVariableName(), recStatus);
            databaseRecord.setVariable(checkVariableListElement.getDatabaseVariableName(), CheckResult.toDatabaseVariable(resultCode));
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
    
    @Action
    public void autoFill() {
        actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditor.AUTO_FILL));
    }   
    
    @Action
    public void sourceMenuAction() {
        sourcePopupMenu.show(sourceMenuButton, 0, 0);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourcePopupMenu = new javax.swing.JPopupMenu();
        sourceDeleteMenuItem = new javax.swing.JMenuItem();
        systemPanel = new javax.swing.JPanel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        jPanel1 = new javax.swing.JPanel();
        checksButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        mpButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        tumourLinkedPanel = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        tumourPatientLinkLabel = new javax.swing.JLabel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jPanel13 = new javax.swing.JPanel();
        openPatientsComboBox = new javax.swing.JComboBox();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        recordStatusPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jPanel3 = new javax.swing.JPanel();
        recordStatusComboBox = new javax.swing.JComboBox();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        updatedByPanel = new javax.swing.JPanel();
        byLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        updatedByPanel1 = new javax.swing.JPanel();
        sequenceNumberDescriptionLabel = new javax.swing.JLabel();
        sequenceNumberValueLabel = new javax.swing.JLabel();
        sequenceTotalDescriptionLabel = new javax.swing.JLabel();
        sequenceTotalValueLabel = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jPanel4 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        dataScrollPane = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        addSourceRecordButton = new javax.swing.JButton();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        sourceMenuButton = new javax.swing.JButton();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jPanel10 = new javax.swing.JPanel();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        sourcesTabbedPane = new canreg.client.gui.dataentry2.components.FixedWidthRowTabbedPane();

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditorTumour.class, this);
        sourceDeleteMenuItem.setAction(actionMap.get("removeSourceAction")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditorTumour.class);
        sourceDeleteMenuItem.setText(resourceMap.getString("removeSourceAction.Action.text")); // NOI18N
        sourceDeleteMenuItem.setName("deleteRecord"); // NOI18N
        sourcePopupMenu.add(sourceDeleteMenuItem);

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        systemPanel.setMaximumSize(new java.awt.Dimension(32767, 100));
        systemPanel.setPreferredSize(new java.awt.Dimension(1062, 80));
        systemPanel.setLayout(new javax.swing.BoxLayout(systemPanel, javax.swing.BoxLayout.LINE_AXIS));
        systemPanel.add(filler4);

        jPanel1.setMaximumSize(new java.awt.Dimension(32767, 95));
        jPanel1.setPreferredSize(new java.awt.Dimension(130, 50));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        checksButton.setAction(actionMap.get("runChecksAction")); // NOI18N
        checksButton.setText(resourceMap.getString("checksButton.text")); // NOI18N
        checksButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 0), 2));
        checksButton.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel1.add(checksButton);
        jPanel1.add(filler1);

        mpButton.setAction(actionMap.get("runMultiplePrimarySearch")); // NOI18N
        mpButton.setText(resourceMap.getString("mpButton.text")); // NOI18N
        mpButton.setToolTipText(resourceMap.getString("mpButton.toolTipText")); // NOI18N
        mpButton.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel1.add(mpButton);

        systemPanel.add(jPanel1);
        systemPanel.add(filler3);

        tumourLinkedPanel.setMaximumSize(new java.awt.Dimension(32767, 95));
        tumourLinkedPanel.setPreferredSize(new java.awt.Dimension(150, 50));
        tumourLinkedPanel.setLayout(new javax.swing.BoxLayout(tumourLinkedPanel, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel12.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

        tumourPatientLinkLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tumourPatientLinkLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tumourPatientLinkLabel.setText(resourceMap.getString("tumourPatientLinkLabel.text")); // NOI18N
        tumourPatientLinkLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        tumourPatientLinkLabel.setMaximumSize(new java.awt.Dimension(200, 100));
        tumourPatientLinkLabel.setPreferredSize(new java.awt.Dimension(68, 23));
        jPanel12.add(tumourPatientLinkLabel);

        tumourLinkedPanel.add(jPanel12);
        tumourLinkedPanel.add(filler10);

        jPanel13.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel13.setLayout(new javax.swing.BoxLayout(jPanel13, javax.swing.BoxLayout.LINE_AXIS));

        openPatientsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Coming Soon!" }));
        openPatientsComboBox.setName("possiblePatientsComboBox"); // NOI18N
        jPanel13.add(openPatientsComboBox);

        tumourLinkedPanel.add(jPanel13);

        systemPanel.add(tumourLinkedPanel);
        systemPanel.add(filler11);

        recordStatusPanel.setMaximumSize(new java.awt.Dimension(32767, 95));
        recordStatusPanel.setPreferredSize(new java.awt.Dimension(150, 50));
        recordStatusPanel.setLayout(new javax.swing.BoxLayout(recordStatusPanel, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel6.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(resourceMap.getString("recordStatusPanel.border.title")); // NOI18N
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabel2.setMaximumSize(new java.awt.Dimension(200, 100));
        jLabel2.setPreferredSize(new java.awt.Dimension(68, 23));
        jPanel6.add(jLabel2);

        recordStatusPanel.add(jPanel6);
        recordStatusPanel.add(filler2);

        jPanel3.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        recordStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        recordStatusComboBox.setName("recordStatusComboBox"); // NOI18N
        jPanel3.add(recordStatusComboBox);

        recordStatusPanel.add(jPanel3);

        systemPanel.add(recordStatusPanel);
        systemPanel.add(filler5);

        updatedByPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("updatedByPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        updatedByPanel.setMaximumSize(new java.awt.Dimension(400, 95));
        updatedByPanel.setMinimumSize(new java.awt.Dimension(100, 30));
        updatedByPanel.setPreferredSize(new java.awt.Dimension(300, 69));

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
                        .addComponent(userLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
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

        updatedByPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("sequencePanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        updatedByPanel1.setMaximumSize(new java.awt.Dimension(150, 95));
        updatedByPanel1.setMinimumSize(new java.awt.Dimension(100, 30));
        updatedByPanel1.setPreferredSize(new java.awt.Dimension(150, 80));

        sequenceNumberDescriptionLabel.setText(resourceMap.getString("sequenceNumberDescriptionLabel.text")); // NOI18N

        sequenceNumberValueLabel.setText(resourceMap.getString("sequenceNumberValueLabel.text")); // NOI18N

        sequenceTotalDescriptionLabel.setText(resourceMap.getString("sequenceTotalDescriptionLabel.text")); // NOI18N

        sequenceTotalValueLabel.setText(resourceMap.getString("sequenceTotalValueLabel.text")); // NOI18N

        javax.swing.GroupLayout updatedByPanel1Layout = new javax.swing.GroupLayout(updatedByPanel1);
        updatedByPanel1.setLayout(updatedByPanel1Layout);
        updatedByPanel1Layout.setHorizontalGroup(
            updatedByPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updatedByPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sequenceTotalDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sequenceNumberDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updatedByPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequenceNumberValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                    .addComponent(sequenceTotalValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        updatedByPanel1Layout.setVerticalGroup(
            updatedByPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanel1Layout.createSequentialGroup()
                .addGroup(updatedByPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sequenceNumberDescriptionLabel)
                    .addComponent(sequenceNumberValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updatedByPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sequenceTotalDescriptionLabel)
                    .addComponent(sequenceTotalValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        systemPanel.add(updatedByPanel1);

        add(systemPanel);
        add(filler6);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setContinuousLayout(true);

        dataScrollPane.setBorder(null);
        dataScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        dataScrollPane.setViewportView(dataPanel);

        jSplitPane2.setLeftComponent(dataScrollPane);

        jPanel7.setLayout(new javax.swing.OverlayLayout(jPanel7));

        jPanel8.setOpaque(false);

        jPanel9.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel9.setMinimumSize(new java.awt.Dimension(20, 36));
        jPanel9.setOpaque(false);
        jPanel9.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));
        jPanel9.add(filler7);

        addSourceRecordButton.setAction(actionMap.get("addSourceAction")); // NOI18N
        addSourceRecordButton.setText(resourceMap.getString("addSourceAction.Action.text")); // NOI18N
        addSourceRecordButton.setMaximumSize(new java.awt.Dimension(220, 23));
        addSourceRecordButton.setMinimumSize(new java.awt.Dimension(21, 23));
        addSourceRecordButton.setName("addSourceAction"); // NOI18N
        jPanel9.add(addSourceRecordButton);
        jPanel9.add(filler8);

        sourceMenuButton.setAction(actionMap.get("sourceMenuAction")); // NOI18N
        sourceMenuButton.setText(resourceMap.getString("menuButton.text")); // NOI18N
        sourceMenuButton.setMaximumSize(new java.awt.Dimension(100, 23));
        sourceMenuButton.setMinimumSize(new java.awt.Dimension(30, 23));
        jPanel9.add(sourceMenuButton);
        jPanel9.add(filler9);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1143, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(185, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel8);

        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel10.add(filler12);
        jPanel10.add(sourcesTabbedPane);

        jPanel7.add(jPanel10);

        jSplitPane2.setRightComponent(jPanel7);

        jPanel4.add(jSplitPane2);

        add(jPanel4);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRecordButton;
    private javax.swing.JLabel byLabel;
    private javax.swing.JButton checksButton;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScrollPane;
    private javax.swing.JLabel dateLabel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton mpButton;
    private javax.swing.JComboBox<String> openPatientsComboBox;
    private javax.swing.JComboBox<String> recordStatusComboBox;
    private javax.swing.JPanel recordStatusPanel;
    private javax.swing.JLabel sequenceNumberDescriptionLabel;
    private javax.swing.JLabel sequenceNumberValueLabel;
    private javax.swing.JLabel sequenceTotalDescriptionLabel;
    private javax.swing.JLabel sequenceTotalValueLabel;
    private javax.swing.JMenuItem sourceDeleteMenuItem;
    private javax.swing.JButton sourceMenuButton;
    private javax.swing.JPopupMenu sourcePopupMenu;
    private canreg.client.gui.dataentry2.components.FixedWidthRowTabbedPane sourcesTabbedPane;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JPanel tumourLinkedPanel;
    private javax.swing.JLabel tumourPatientLinkLabel;
    private javax.swing.JPanel updatedByPanel;
    private javax.swing.JPanel updatedByPanel1;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
   
}
