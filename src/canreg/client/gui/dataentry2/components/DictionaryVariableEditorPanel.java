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
package canreg.client.gui.dataentry2.components;


import canreg.client.dataentry.DictionaryHelper;
import canreg.client.gui.dataentry.RecordEditor;
//import canreg.client.gui.dataentry2.dataentry.RecordEditor;
import canreg.client.gui.tools.MaxLengthDocument;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.DefaultComboBoxModel;


/**
 *
 * @author patri_000
 */
public class DictionaryVariableEditorPanel extends VariableEditorPanel {
    
    private Dictionary dictionary;    
    //private Map<String, DictionaryEntry> possibleValuesMap;
    private static final Color ORDER_CODE_COLOR = new Color(51,51,51);
    private static final Color ORDER_DESCRIPTION_COLOR = new Color(255,255,255);
    private ActionListener categoryComboListener;
    private ActionListener descriptionComboListener;
    private DictionaryEntry categorySelected;
    private DictionaryEntry descriptionSelected;
    private boolean avoidActionPerformed = false;
    private org.jdesktop.application.ResourceMap resourceMap;
    
    
    public DictionaryVariableEditorPanel(ActionListener listener) {
        super(listener);        
        resourceMap = org.jdesktop.application.Application.getInstance(
                canreg.client.CanRegClientApp.class).getContext().getResourceMap(DictionaryVariableEditorPanel.class);
        initComponents();
    }
    
    private void initComponents() {        
        jPanel1 = new javax.swing.JPanel();        
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        outerSplitPane = new javax.swing.JSplitPane();
        innerSplitPane = new javax.swing.JSplitPane();        
        descriptionCombo = new javax.swing.JComboBox();
        categoryCombo = new javax.swing.JComboBox();
        //categoryToggle = new javax.swing.JToggleButton();                
        descriptionToggle = new javax.swing.JToggleButton();
                
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));
        
        mainSplitPane.remove(codeTextField);
        mainSplitPane.setRightComponent(jPanel1);
        
        innerSplitPane.setBorder(null);
        innerSplitPane.setResizeWeight(0.5);
        innerSplitPane.setFocusable(false);
        innerSplitPane.setMaximumSize(new java.awt.Dimension(32767, 32767));
        innerSplitPane.setName("innerSplitPane"); // NOI18N
        
        outerSplitPane.setDividerLocation(70);
        outerSplitPane.setBorder(null);
        outerSplitPane.setFocusable(false);
        outerSplitPane.setResizeWeight(0.2);
        outerSplitPane.setName("outerSplitPane"); // NOI18N               
        
        categoryCombo.setMinimumSize(new Dimension(20, 20));
        categoryCombo.setPreferredSize(new Dimension(20, 20));
        categoryCombo.setName("categoryCombo"); // NOI18N
        categoryCombo.setEditable(true);
        descriptionCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                comboBoxKeyTyped(evt);
            }
        });
                
        /*categoryToggle.setBackground(ORDER_CODE_COLOR); // NOI18N
        categoryToggle.setText("");
        categoryToggle.setIcon(resourceMap.getIcon("sortBy.icon.code")); // NOI18N
        categoryToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                      resourceMap.getString("sortByCodeSelected.Action.text")); // NOI18N
        categoryToggle.setBorder(null);
        categoryToggle.setContentAreaFilled(false);
        categoryToggle.setMaximumSize(new java.awt.Dimension(25, 25));
        categoryToggle.setMinimumSize(new java.awt.Dimension(20, 20));
        categoryToggle.setName("categoryToggle"); // NOI18N
        categoryToggle.setOpaque(true);
        categoryToggle.setPreferredSize(new java.awt.Dimension(20, 20));
        categoryToggle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                categoryToggleItemStateChanged(evt);
            }
        });*/
        
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));
        jPanel4.add(categoryCombo);
        //jPanel4.add(categoryToggle);
        
        descriptionCombo.setPreferredSize(new Dimension(20, 20));
        descriptionCombo.setMinimumSize(new Dimension(20, 20));
        descriptionCombo.setName("descriptionCombo"); // NOI18N
        descriptionCombo.setEditable(true);
        descriptionCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                comboBoxKeyTyped(evt);
            }
        });
        
        descriptionToggle.setBackground(ORDER_CODE_COLOR); // NOI18N
        descriptionToggle.setText("");
        descriptionToggle.setIcon(resourceMap.getIcon("sortBy.icon.code")); // NOI18N
        descriptionToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                         resourceMap.getString("sortByCodeSelected.Action.text")); // NOI18N
        descriptionToggle.setBorder(null);
        descriptionToggle.setFocusable(false);
        descriptionToggle.setContentAreaFilled(false);
        descriptionToggle.setMaximumSize(new java.awt.Dimension(25, 25));
        descriptionToggle.setMinimumSize(new java.awt.Dimension(20, 20));
        descriptionToggle.setName("descriptionToggle"); // NOI18N
        descriptionToggle.setOpaque(true);
        descriptionToggle.setPreferredSize(new java.awt.Dimension(20, 20));
        descriptionToggle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                descriptionToggleItemStateChanged(evt);
            }
        });
        
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));
        jPanel5.add(descriptionCombo);
        jPanel5.add(descriptionToggle);
        
        innerSplitPane.setLeftComponent(jPanel4);
        innerSplitPane.setRightComponent(jPanel5);                              
                
        outerSplitPane.setLeftComponent(codeTextField);
        outerSplitPane.setRightComponent(innerSplitPane);
        jPanel1.add(outerSplitPane);        
    }
    
    @Override
    public synchronized void setValue(String value) {
        codeTextField.setText(value);
        try {
            lookUpAndSetDescription();
        } catch (NullPointerException e) {
            //descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
            categoryCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Dictionary_Error")}));
            descriptionCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Dictionary_Error")}));
        }
        updateFilledInStatusColor();
    }

    private void lookUpAndSetDescription() throws NullPointerException {
        //descriptionTextField.setText("");        
        //categoryTextField.setText("");        
        if (codeTextField.getText().trim().length() > 0) { 
                try {
                    if(dictionary.isCompoundDictionary() && codeTextField.getText().length() >= dictionary.getCodeLength()) {
                        /*categoryTextField.setText(possibleValuesMap.get(
                                codeTextField.getText().substring(0, dictionary.getCodeLength())).getDescription());*/  
                        String code = codeTextField.getText();
                        categoryCombo.setSelectedItem(
                                dictionary.getDictionaryEntries().get(codeTextField.getText().substring(0, dictionary.getCodeLength())));
                        avoidActionPerformed = true;
                        codeTextField.setText(code);
                        avoidActionPerformed = false;
                    }
                    if(dictionary.isCompoundDictionary()) {
                        if (codeTextField.getText().length() == dictionary.getFullDictionaryCodeLength()) {
                            //descriptionTextField.setText(possibleValuesMap.get(codeTextField.getText()).getDescription());
                            descriptionCombo.setSelectedItem(
                                dictionary.getDictionaryEntries().get(codeTextField.getText()));
                        }
                    } else {
                        //descriptionTextField.setText(possibleValuesMap.get(codeTextField.getText()).getDescription());
                        descriptionCombo.setSelectedItem(dictionary.getDictionaryEntries().get(codeTextField.getText()));
                    }
                } catch (NullPointerException e) {
                    throw e;
                }            
        }
    }
    
    public void setDictionary(Dictionary dictionary) {
        if(dictionary == null) 
            return;        
        
        avoidActionPerformed = true;
        codeTextField.setText("");
        updateFilledInStatusColor();
        avoidActionPerformed = false;
        
        categoryCombo.removeActionListener(categoryComboListener);
        descriptionCombo.removeActionListener(descriptionComboListener);
        
        this.dictionary = dictionary;
        if(dictionary.isCompoundDictionary())
            //categoryCombo is inside the jPanel4
            jPanel4.setVisible(true);
        else
            jPanel4.setVisible(false);       
                
        if(dictionary.getDictionaryEntries() == null) {          
            categoryCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Empty_dictionary")}));        
            descriptionCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Empty_dictionary")}));            
        }
        else {    
            categoryCombo.setModel(new javax.swing.DefaultComboBoxModel<DictionaryEntry>() {});
            descriptionCombo.setModel(new javax.swing.DefaultComboBoxModel<DictionaryEntry>() {});        
            if(dictionary.isCompoundDictionary()) {                                
                for(DictionaryEntry entry : dictionary.getDictionaryEntries().values()) {                    
                    if(entry.getCode().length() < dictionary.getFullDictionaryCodeLength())                         
                        categoryCombo.addItem(entry);
                    else if(entry.getCode().length() == dictionary.getFullDictionaryCodeLength()) 
                        descriptionCombo.addItem(entry);
                }
                categoryCombo.setSelectedIndex(-1);
                categoryComboListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        categoryComboActionPerformed(e);
                    }
                };
                categoryCombo.addActionListener(categoryComboListener);                
            } else {
                for(DictionaryEntry entry : dictionary.getDictionaryEntries().values())
                    descriptionCombo.addItem(entry);
            }            
            
            descriptionCombo.setSelectedIndex(-1);
            descriptionComboListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    descriptionComboActionPerformed(e);
                }
            };
            descriptionCombo.addActionListener(descriptionComboListener);            
        }   
    }            
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(avoidActionPerformed) {
            return;            
        }
        else {
            if(e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.MAX_LENGTH_ACTION_STRING)) {
                try {
                    lookUpAndSetDescription();
                } catch (NullPointerException ne) {
                    descriptionCombo.setModel(new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Dictionary_Error")}));
                    //descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
                }
                updateFilledInStatusColor();
            } else if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.CHANGED_ACTION_STRING)) {
                try {
                    lookUpAndSetDescription();
                    Object currentValue = getValue();
                    if (listener != null && ((currentValue != null && !currentValue.equals(initialValue)) || (initialValue != null && !initialValue.equals(currentValue)))) {
                        hasChanged = true;
                        listener.actionPerformed(new ActionEvent(this, 0, CHANGED_STRING));
                    }
                } catch (NullPointerException ne) {
                    // descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
                    descriptionCombo.setModel(new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Dictionary_Error")}));
                }
                updateFilledInStatusColor();
            }     
        }
        
        /*if (e.getActionCommand().equalsIgnoreCase(DictionaryElementChooser.OK_ACTION)) {
            codeTextField.setText(dictionaryElementChooser.getSelectedElement().getCode());
            try {
                lookUpAndSetDescription();
            } catch (NullPointerException ne) {
                //descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Dictionary_Error"));
                descriptionCombo.setModel(new javax.swing.DefaultComboBoxModel(
                        new String[] {java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/DictionaryVariableEditorPanel").getString("Dictionary_Error")}));
            }
            updateFilledInStatusColor();
            listener.actionPerformed(new ActionEvent(this, 0, RecordEditor.REQUEST_FOCUS));
            // setFocus();            
        }*/        
    }
    
    @Override
    protected void codeTextFieldActionPerformed(java.awt.event.FocusEvent evt) {                                              
       try {
            lookUpAndSetDescription();
            updateFilledInStatusColor();
        } catch (NullPointerException e) {
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            descriptionCombo.setModel(new DefaultComboBoxModel(
                    new String[]{
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("Error")
                        + ": "
                        + codeTextField.getText()
                        + " "
                        + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel").getString("_is_not_a_valid_dictionary_code.")
                    }));
            /*descriptionTextField.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Error")
                    + ": "
                    + codeTextField.getText()
                    + " "
                    + java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("_is_not_a_valid_dictionary_code."));*/
            // JOptionPane.showInternalMessageDialog(this, codeTextField.getText() + " " + java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("_is_not_a_valid_dictionary_code."), java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }             
    
    @Override
    protected void codeTextFieldKeyTyped(java.awt.event.KeyEvent evt) {
        if (dictionary != null && evt.getKeyChar() == '?') {
            if(categoryCombo.isVisible())
                this.categoryCombo.showPopup();
            else if(descriptionCombo.isVisible())
                this.descriptionCombo.showPopup();
            //showDictionaryChooser();
        } else if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            transferFocusToNext();
        }
    }    
    
    private void descriptionToggleItemStateChanged(java.awt.event.ItemEvent evt) {                                                
        if(descriptionToggle.isSelected()) {            
            descriptionToggle.setBackground(ORDER_DESCRIPTION_COLOR);
            descriptionToggle.setIcon(resourceMap.getIcon("sortBy.icon.description")); // NOI18N
            descriptionToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                             resourceMap.getString("sortByDescriptionSelected.Action.text")); // NOI18N
            
            DictionaryEntry selectedDescEntry = (DictionaryEntry) descriptionCombo.getSelectedItem();
            DefaultComboBoxModel<DictionaryEntry> newDescModel = new DefaultComboBoxModel<DictionaryEntry>() {};
            for(int i = 0; i < descriptionCombo.getItemCount(); i++) {
                DictionaryEntry entry = (DictionaryEntry)descriptionCombo.getItemAt(i);
                entry.setSortByCode();
                newDescModel.addElement(entry);
            }            
            descriptionCombo.setModel(newDescModel);
            descriptionCombo.setSelectedItem(selectedDescEntry);
                        
            if(dictionary.isCompoundDictionary()) {
                DictionaryEntry selectedEntry = (DictionaryEntry) categoryCombo.getSelectedItem();
                DefaultComboBoxModel<DictionaryEntry> newModel = new DefaultComboBoxModel<DictionaryEntry>() {};
                for(int i = 0; i < categoryCombo.getItemCount(); i++) {
                    DictionaryEntry entry = (DictionaryEntry)categoryCombo.getItemAt(i);
                    entry.setSortByCode();
                    newModel.addElement(entry);
                }      
                categoryCombo.removeActionListener(categoryComboListener);
                categoryCombo.setModel(newModel);
                categoryCombo.setSelectedItem(selectedEntry);
                categoryCombo.addActionListener(categoryComboListener);
            }
                                    
            /*for(DictionaryEntry de : dictionary.getDictionaryEntries().values()) 
                de.setSortByCode();
            //Este setDictionary nos caga todo!!
            setDictionary(dictionary);*/
        } else {
            descriptionToggle.setBackground(ORDER_CODE_COLOR);
            descriptionToggle.setIcon(resourceMap.getIcon("sortBy.icon.code")); // NOI18N
            descriptionToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                             resourceMap.getString("sortByCodeSelected.Action.text")); // NOI18N 
           
            DictionaryEntry selectedDescEntry = (DictionaryEntry) descriptionCombo.getSelectedItem();
            DefaultComboBoxModel<DictionaryEntry> newDescModel = new DefaultComboBoxModel<DictionaryEntry>() {};
            for(int i = 0; i < descriptionCombo.getItemCount(); i++) {
                DictionaryEntry entry = (DictionaryEntry)descriptionCombo.getItemAt(i);
                entry.setSortByDescription();
                newDescModel.addElement(entry);
            }            
            descriptionCombo.setModel(newDescModel);
            descriptionCombo.setSelectedItem(selectedDescEntry);
            
            //categoryCombo is inside jPanel4
            if(jPanel4.isVisible()) {
                DictionaryEntry selectedEntry = (DictionaryEntry) categoryCombo.getSelectedItem();
                DefaultComboBoxModel<DictionaryEntry> newModel = new DefaultComboBoxModel<DictionaryEntry>() {};
                for(int i = 0; i < categoryCombo.getItemCount(); i++) {
                    DictionaryEntry entry = (DictionaryEntry)categoryCombo.getItemAt(i);
                    entry.setSortByDescription();
                    newModel.addElement(entry);
                }            
                categoryCombo.removeActionListener(categoryComboListener);
                categoryCombo.setModel(newModel);
                categoryCombo.setSelectedItem(selectedEntry);
                categoryCombo.addActionListener(categoryComboListener);
            }
            
            /*for(DictionaryEntry de : dictionary.getDictionaryEntries().values()) 
                de.setSortByDescription();   
            //Este setDictionary nos caga todo!!
            setDictionary(dictionary);*/
        }
    }
    
    private void categoryComboActionPerformed(java.awt.event.ActionEvent evt) {                                              
        if(categoryCombo.getSelectedItem() != null) { 
            String categoryCode = null;
            try {
                categoryCode = ((DictionaryEntry) categoryCombo.getSelectedItem()).getCode();
            } catch(ClassCastException ex) {
                setDictionary(dictionary);
            }

            if(categoryCode != null) {
                descriptionCombo.removeActionListener(descriptionComboListener);                    
                avoidActionPerformed = true;
                codeTextField.setText("");
                avoidActionPerformed = false;                    
                descriptionCombo.setModel(new DefaultComboBoxModel<DictionaryEntry>() {}); 
                DictionaryEntry[] entries = DictionaryHelper.getDictionaryEntriesStartingWith(categoryCode,
                                                                                              dictionary.getDictionaryEntries().values().toArray(new DictionaryEntry[0]));
                for(DictionaryEntry entry : entries)
                    descriptionCombo.addItem(entry);
                descriptionCombo.setSelectedIndex(-1);
                /*descriptionComboListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        descriptionComboActionPerformed(e);
                    }
                };*/
                descriptionCombo.addActionListener(descriptionComboListener);
            }            
            categorySelected = (DictionaryEntry) categoryCombo.getSelectedItem();
        }
    } 
    
    private void descriptionComboActionPerformed(java.awt.event.ActionEvent evt) {                                              
        if(descriptionCombo.getSelectedItem() != null) {
            String descriptionCode = ((DictionaryEntry) descriptionCombo.getSelectedItem()).getCode();
            if(descriptionCode != null) {
                if(dictionary.isCompoundDictionary() /*&& categoryCombo.getSelectedIndex() == -1*/) {                    
                    DictionaryEntry categoryEntry = DictionaryHelper.getDictionaryEntryBestMatchingSubcode(descriptionCode, 
                                                                                                           dictionary.getDictionaryEntries().values().toArray(new DictionaryEntry[0]));
                    if(categoryEntry != null) {
                        categoryCombo.removeActionListener(categoryComboListener);
                        categoryCombo.setSelectedItem(categoryEntry);
                        categoryCombo.addActionListener(categoryComboListener);
                    }  
                }
                avoidActionPerformed = true;
                codeTextField.setText(descriptionCode);
                avoidActionPerformed = false;
                updateFilledInStatusColor();
                //listener.actionPerformed(new ActionEvent(this, 0, RecordEditor.REQUEST_FOCUS));
                // setFocus();
                //transferFocusToNext();                                
            }            
        }
    } 
    
    private void comboBoxKeyTyped(java.awt.event.KeyEvent evt) {                                       
        if(evt.getKeyChar() == KeyEvent.VK_ENTER) 
            transferFocusToNext();        
    } 
    
    private javax.swing.JComboBox<DictionaryEntry> categoryCombo;    
    private javax.swing.JComboBox<DictionaryEntry> descriptionCombo;
    private javax.swing.JPanel jPanel1;    
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;    
    private javax.swing.JSplitPane outerSplitPane;
    private javax.swing.JSplitPane innerSplitPane;
    //private javax.swing.JToggleButton categoryToggle;
    private javax.swing.JToggleButton descriptionToggle;
}
