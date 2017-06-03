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
 *         Patricio Ezequiel Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.dataentry2;

import canreg.client.CanRegClientApp;
import canreg.client.gui.components.VariableEditorPanelInterface;
import canreg.client.gui.dataentry2.RecordEditorPanel.panelTypes;
import canreg.client.gui.dataentry2.components.DateVariableEditorPanel;
import canreg.client.gui.dataentry2.components.DictionaryVariableEditorPanel;
import canreg.client.gui.dataentry2.components.TextAreaVariableEditorPanel;
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
import canreg.common.qualitycontrol.CheckResult;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.w3c.dom.Document;

/**
 * @author ervikm, patri_000
 */
public class RecordEditorSource extends javax.swing.JPanel 
        implements ActionListener, RecordEditorPanel, PropertyChangeListener {

    private Map<Integer, Dictionary> dictionary;
    private Document doc;
    private ActionListener actionListener;
    private DatabaseRecord databaseRecord;
    private boolean hasChanged = false;
    private DatabaseGroupsListElement[] groupListElements;
    private DatabaseVariablesListElement[] variablesInTable;
    private Map<String, VariableEditorPanelInterface> variableEditorPanels;
    private final LinkedList<DatabaseVariablesListElement> autoFillList;
    private final GlobalToolBox globalToolBox;
    private final panelTypes panelType = panelTypes.SOURCE;
    private final SimpleDateFormat dateFormat;
   
    public RecordEditorSource(ActionListener listener) {
        initComponents();
        this.actionListener = listener; 
        autoFillList = new LinkedList<DatabaseVariablesListElement>();
        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
        dateFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING);
    }
               
    public void setRecordAndBuildPanel(DatabaseRecord dbr) {
        //setChecksResultCode(ResultCode.NotDone);
        setDatabaseRecord(dbr);
        buildPanel();
    }
    
    @Override
    public void setResultCodeOfVariable(String databaseVariableName, CheckResult.ResultCode resultCode) {
        VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariableName);
        panel.setResultCode(resultCode);
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
    
    @Override
    public DatabaseRecord getDatabaseRecord() {
        buildDatabaseRecord();
        return this.databaseRecord;
    }
    
    @Override
    public void setDatabaseRecord(DatabaseRecord dbr) {
        this.databaseRecord = dbr;
        setSaveNeeded(false);
        groupListElements = Tools.getGroupsListElements(doc, Globals.NAMESPACE);            

        String tableName = null;
        if (panelType == panelTypes.SOURCE) 
            tableName = Globals.SOURCE_TABLE_NAME;
        
        if (panelType != null) switch (panelType) {
            case PATIENT:                
                throw new IllegalArgumentException("This should be a Source panelType, not a Patient.");
            case TUMOUR:
                throw new IllegalArgumentException("This should be a Source panelType, not a Tumour.");
            case SOURCE:
                tableName = Globals.SOURCE_TABLE_NAME;
            default:
                break;
        }
        
        variablesInTable = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        Arrays.sort(variablesInTable, new DatabaseVariablesListElementPositionSorter());
    }
    
    private void buildPanel() {
        dataPanel.removeAll();

        if (variableEditorPanels != null) 
            for (VariableEditorPanelInterface vep : variableEditorPanels.values()) 
                vep.removeListener();            
        
        variableEditorPanels = new LinkedHashMap();

        Map<Integer, VariableEditorGroupPanel> groupIDtoPanelMap = new LinkedHashMap<Integer, VariableEditorGroupPanel>();        

        for (int i = 0; i < variablesInTable.length; i++) {
            DatabaseVariablesListElement currentVariable = variablesInTable[i];
            VariableEditorPanel vep;

            String variableType = currentVariable.getVariableType();
            
            if (Globals.VARIABLE_TYPE_DATE_NAME.equalsIgnoreCase(variableType)) 
                vep = new DateVariableEditorPanel(this);
            else if (Globals.VARIABLE_TYPE_TEXT_AREA_NAME.equalsIgnoreCase(variableType)) 
                vep = new TextAreaVariableEditorPanel(this);
            else if (currentVariable.getDictionaryID() >= 0 && dictionary.get(currentVariable.getDictionaryID()) != null)
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

                panel.addVariablePanel(vep);
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

        dataPanel.revalidate();
        dataPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(VariableEditorPanelInterface.CHANGED_STRING)) {           
            //Bubbles to RecordEditorTumour (it takes care of all change management)
            RecordEditorTumour tumour = (RecordEditorTumour) actionListener;
            tumour.changesDone(e.getSource(), true, false);
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
    
    private void buildDatabaseRecord() {
        Iterator<VariableEditorPanelInterface> iterator = variableEditorPanels.values().iterator();
        while (iterator.hasNext()) {
            VariableEditorPanelInterface vep = iterator.next();
            databaseRecord.setVariable(vep.getKey(), vep.getValue());
        }
    }
    
    private void changesDone() {
        setSaveNeeded(true);
    }
    
    @Override
    public LinkedList<DatabaseVariablesListElement> getAutoFillList() {
        return autoFillList;
    }
   
    public Map<Integer, Dictionary> getDictionary() {
        return dictionary;
    }
    
    @Override
    public boolean isSaveNeeded() {
        for(DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if (panel != null) 
                hasChanged = hasChanged || panel.hasChanged();  
            //Whenever hasChanged is true, than it will never turn to false.
            //We break, so we don't have to check all the panels, is pointless.
            if (hasChanged)
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
            actionListener.actionPerformed(new ActionEvent(this, 0, RecordEditorMainFrame.CHANGED));
            // saveButton.setEnabled(saveNeeded);
        } else {
            // Do nothing.
        }
    }
    
    @Override
    public void refreshDatabaseRecord(DatabaseRecord record, boolean isSaveNeeded) {
        setDatabaseRecord(record);
        setSaveNeeded(isSaveNeeded);

        buildPanel();
    }
        
    @Override
    public void prepareToSaveRecord() {
        buildDatabaseRecord();
        
        //This is now performed solely by RecordEditor
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
   
    @Override
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }
   
    public Document getDocument() {
        return doc;
    }
   
    @Override
    public void setDocument(Document doc) {
        this.doc = doc;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataScrollPane = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        dataScrollPane.setBorder(null);
        dataScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        dataScrollPane.setViewportView(dataPanel);

        add(dataScrollPane);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScrollPane;
    // End of variables declaration//GEN-END:variables
   
}
