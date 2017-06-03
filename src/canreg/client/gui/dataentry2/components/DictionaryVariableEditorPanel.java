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
package canreg.client.gui.dataentry2.components;


import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import canreg.client.dataentry.DictionaryHelper;
import canreg.client.gui.components.DictionaryElementTextFilterator;
import canreg.client.gui.tools.MaxLengthDocument;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;


/**
 *
 * @author ervikm, patri_000
 */
public class DictionaryVariableEditorPanel extends VariableEditorPanel {
    
    private Dictionary dictionary;    
    private AutoCompleteSupport<DictionaryEntry> categoryComboCompSup;
    private AutoCompleteSupport<DictionaryEntry> descriptionComboCompSup;    
    private static final Color ORDER_CODE_COLOR = new Color(51,51,51);
    private static final Color ORDER_DESCRIPTION_COLOR = new Color(255,255,255);
    private ActionListener categoryComboListener;
    private ActionListener descriptionComboListener;
    private boolean avoidActionPerformed = false;
    private boolean doNotSetText = false;
    private final org.jdesktop.application.ResourceMap resourceMap;
    
    public DictionaryVariableEditorPanel() {
        super();
        resourceMap = org.jdesktop.application.Application.getInstance(
                canreg.client.CanRegClientApp.class).getContext().getResourceMap(DictionaryVariableEditorPanel.class);
        initComponents();
    }
    
    public DictionaryVariableEditorPanel(ActionListener listener) {
        super(listener);      
        resourceMap = org.jdesktop.application.Application.getInstance(
                canreg.client.CanRegClientApp.class).getContext().getResourceMap(DictionaryVariableEditorPanel.class);
        initComponents();
        this.outerSplitPane.setDividerLocation(90);
        this.outerSplitPane.setResizeWeight(0.0);
    }
    
    private void initComponents() {        
        jPanel1 = new javax.swing.JPanel();        
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        outerSplitPane = new javax.swing.JSplitPane();
        innerSplitPane = new javax.swing.JSplitPane();        
        descriptionCombo = new javax.swing.JComboBox();
        categoryCombo = new javax.swing.JComboBox();                     
        sortToggle = new javax.swing.JToggleButton();
                
        jPanel1.setName("jPanel1");
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));
        
        mainSplitPane.remove(codeTextField);
        mainSplitPane.setRightComponent(jPanel1);
        
        innerSplitPane.setBorder(null);
        innerSplitPane.setResizeWeight(0.5);
        innerSplitPane.setFocusable(false);
        innerSplitPane.setMinimumSize(new java.awt.Dimension(20, 26));
        innerSplitPane.setPreferredSize(new java.awt.Dimension(100, 26));
        innerSplitPane.setName("innerSplitPane"); // NOI18N
        innerSplitPane.setDividerSize(7);
        innerSplitPane.setUI(new DottedDividerSplitPane());
        innerSplitPane.setBorder(null);
        
        outerSplitPane.setBorder(null);
        outerSplitPane.setFocusable(false);
        outerSplitPane.setMinimumSize(new java.awt.Dimension(20, 26));
        outerSplitPane.setPreferredSize(new java.awt.Dimension(100, 26));
        outerSplitPane.setName("outerSplitPane"); // NOI18N
        outerSplitPane.setDividerSize(7);
        outerSplitPane.setUI(new DottedDividerSplitPane());
        outerSplitPane.setBorder(null);
        
        categoryCombo.setMinimumSize(new Dimension(20, 26));
        categoryCombo.setPreferredSize(new Dimension(20, 26));
        categoryCombo.setName("categoryCombo"); // NOI18N
        categoryCombo.setEditable(true);        
        categoryCombo.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                categoryComboBoxKeyTyped(evt);
            }
        });
        categoryCombo.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                categoryCombo.showPopup();
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                categoryCombo.hidePopup();
            }
        });
        categoryCombo.getEditor().getEditorComponent().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                comboBoxMousePressed(categoryCombo, evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                comboBoxMouseReleased(categoryCombo, evt);
            }
        });
        //Tabbing is handled by keyTiped method
        categoryCombo.setFocusTraversalKeysEnabled(false);
        
        //Tabbing is handled by keyTiped method
        codeTextField.setFocusTraversalKeysEnabled(false);
                        
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));
        jPanel4.add(categoryCombo);        
        
        descriptionCombo.setPreferredSize(new Dimension(20, 26));
        descriptionCombo.setMinimumSize(new Dimension(20, 26));
        descriptionCombo.setName("descriptionCombo"); // NOI18N
        descriptionCombo.setEditable(true);
        descriptionCombo.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                descriptionComboBoxKeyTyped(evt);
            }
        });
        descriptionCombo.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                descriptionCombo.showPopup();
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                descriptionCombo.hidePopup();
            }
        });
        descriptionCombo.getEditor().getEditorComponent().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                comboBoxMousePressed(descriptionCombo, evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                comboBoxMouseReleased(descriptionCombo, evt);
            }
        });
        //Tabbing is handled by keyTiped method
        descriptionCombo.setFocusTraversalKeysEnabled(false);
        
        sortToggle.setBackground(ORDER_CODE_COLOR); // NOI18N
        sortToggle.setText("");
        sortToggle.setIcon(resourceMap.getIcon("sortBy.icon.code")); // NOI18N
        sortToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                         resourceMap.getString("sortByCodeSelected.Action.text")); // NOI18N
        sortToggle.setBorder(null);
        sortToggle.setFocusable(false);
        sortToggle.setContentAreaFilled(false);
        sortToggle.setMaximumSize(new java.awt.Dimension(25, 26));
        sortToggle.setMinimumSize(new java.awt.Dimension(20, 26));
        sortToggle.setName("descriptionToggle"); // NOI18N
        sortToggle.setOpaque(true);
        sortToggle.setPreferredSize(new java.awt.Dimension(20, 26));
        sortToggle.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                descriptionToggleItemStateChanged(evt);
            }
        });
        
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));
        jPanel5.add(descriptionCombo);
        jPanel5.add(sortToggle);
        
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
            if(categoryComboCompSup != null && categoryComboCompSup.isInstalled())
                categoryComboCompSup.uninstall();
            categoryCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle
                                    .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                    .getString("Dictionary_Error")}));
            if(descriptionComboCompSup != null && descriptionComboCompSup.isInstalled())
                descriptionComboCompSup.uninstall();
            descriptionCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle
                                    .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                    .getString("Dictionary_Error")}));
        }
        updateFilledInStatusColor();
    }
    
    private void comboBoxMousePressed(JComboBox comboBox, java.awt.event.MouseEvent evt) {                                           
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent((JTextComponent)comboBox.getEditor().getEditorComponent(), evt);
    }                                          

    private void comboBoxMouseReleased(JComboBox comboBox, java.awt.event.MouseEvent evt) {                                            
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent((JTextComponent)comboBox.getEditor().getEditorComponent(), evt);
    }

    /**
     * If codeTextField has a code written in it, it looks for a category item
     * and/or description item that matches the code.
     * @throws NullPointerException 
     */
    private void lookUpAndSetDescription() 
            throws NullPointerException {
        if (codeTextField.getText().trim().length() > 0) { 
            try {
                if (dictionary.isCompoundDictionary()) {
                    
                    if(codeTextField.getText().length() >= dictionary.getCodeLength()) {                        
                        this.doNotSetText = true;
                        
                        //The dictionary code could be in Upper case or in lower. First
                        //we try as the user wrote it, and then we try in upper and lower.
                        DictionaryEntry entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().substring(0, dictionary.getCodeLength()));
                        if(entry == null)
                            entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().toUpperCase().substring(0, dictionary.getCodeLength()));
                        if(entry == null)
                            entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().toLowerCase().substring(0, dictionary.getCodeLength()));
                        
                        categoryCombo.setSelectedItem(entry);
                        this.doNotSetText = false;
                    } 
                    else if (codeTextField.getText().length() < dictionary.getCodeLength()){
                        categoryCombo.removeActionListener(categoryComboListener);
                        categoryCombo.setSelectedIndex(-1);
                        categoryCombo.addActionListener(categoryComboListener);
                        descriptionCombo.removeActionListener(descriptionComboListener);
                        descriptionCombo.setSelectedIndex(-1);
                        descriptionCombo.addActionListener(descriptionComboListener);
                    }
                    
                    if (codeTextField.getText().length() == dictionary.getFullDictionaryCodeLength()) {
                        this.doNotSetText = true;
                        
                        //The dictionary code could be in Upper case or in lower. First
                        //we try as the user wrote it, and then we try in upper and lower.
                        DictionaryEntry entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().substring(0, dictionary.getFullDictionaryCodeLength()));
                        if(entry == null)
                            entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().toUpperCase().substring(0, dictionary.getFullDictionaryCodeLength()));
                        if(entry == null)
                            entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().toLowerCase().substring(0, dictionary.getFullDictionaryCodeLength()));
                        
                        descriptionCombo.setSelectedItem(entry);
                        
                        this.doNotSetText = false;
                    }
                }  
                else {
                    if (codeTextField.getText().length() == dictionary.getFullDictionaryCodeLength()) {
                        this.doNotSetText = true;
                        
                        //The dictionary code could be in Upper case or in lower. First
                        //we try as the user wrote it, and then we try in upper and lower.
                        DictionaryEntry entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().substring(0, dictionary.getFullDictionaryCodeLength()));
                        if(entry == null)
                            entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().toUpperCase().substring(0, dictionary.getFullDictionaryCodeLength()));
                        if(entry == null)
                            entry = dictionary.getDictionaryEntries().get(
                                codeTextField.getText().toLowerCase().substring(0, dictionary.getFullDictionaryCodeLength()));
                        
                        descriptionCombo.setSelectedItem(entry);
                        
                        this.doNotSetText = false;
                    }
                    else {
                        descriptionCombo.removeActionListener(descriptionComboListener);
                        descriptionCombo.setSelectedIndex(-1);
                        descriptionCombo.addActionListener(descriptionComboListener);
                    }
                }

            } catch (NullPointerException e) {
                throw e;
            }            
        } else {
            if (dictionary.isCompoundDictionary()) {
                categoryCombo.removeActionListener(categoryComboListener);
                categoryCombo.setSelectedIndex(-1);
                categoryCombo.addActionListener(categoryComboListener);
            }
            descriptionCombo.removeActionListener(descriptionComboListener);
            descriptionCombo.setSelectedIndex(-1);
            descriptionCombo.addActionListener(descriptionComboListener);
        }
    }
    
    public void setDictionary(Dictionary dictionary) {
        if (dictionary == null) 
            return;        
        
        //avoidActionPerformed = true;
        codeTextField.setText("");
        updateFilledInStatusColor();
        avoidActionPerformed = false;
        
        categoryCombo.removeActionListener(categoryComboListener);
        descriptionCombo.removeActionListener(descriptionComboListener);
        
        this.dictionary = dictionary;
        if (dictionary.isCompoundDictionary())
            //categoryCombo is inside the jPanel4
            jPanel4.setVisible(true);
        else {
            jPanel4.setVisible(false);
            innerSplitPane.setDividerSize(0);
        }
                
        if (dictionary.getDictionaryEntries() == null || dictionary.getDictionaryEntries().isEmpty()) {          
            categoryCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle
                                    .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                    .getString("Empty_dictionary")}));        
            descriptionCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle
                                    .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                    .getString("Empty_dictionary")}));            
        }
        else {            
            LinkedList<DictionaryEntry> categoryPossibleValuesCollection = new LinkedList<DictionaryEntry>();
            LinkedList<DictionaryEntry> descriptionPossibleValuesCollection = new LinkedList<DictionaryEntry>();
                  
            if (dictionary.isCompoundDictionary()) {                                
                for(DictionaryEntry entry : dictionary.getDictionaryEntries().values()) {                    
                    if (entry.getCode().length() < dictionary.getFullDictionaryCodeLength()) 
                        categoryPossibleValuesCollection.add(entry);                   
                    else if (entry.getCode().length() == dictionary.getFullDictionaryCodeLength())
                        descriptionPossibleValuesCollection.add(entry);                    
                }
                          
                categoryComboCompSup = this.setComboModel(categoryComboCompSup, 
                                                          categoryCombo, 
                                                          categoryPossibleValuesCollection);

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
                    descriptionPossibleValuesCollection.add(entry);                
            }            
                                    
            descriptionComboCompSup = this.setComboModel(descriptionComboCompSup, 
                                                         descriptionCombo, 
                                                         descriptionPossibleValuesCollection);

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
    
    /**
     * GlazedList's comboBox autocompletion and autofiltering.
     * @param combo
     * @param values 
     */
    private AutoCompleteSupport<DictionaryEntry> setComboModel(AutoCompleteSupport<DictionaryEntry> compSup, 
                               JComboBox combo, 
                               List<DictionaryEntry> values) {
        EventList<DictionaryEntry> possibleValuesEventList = new BasicEventList<DictionaryEntry>(values);
        if (compSup != null) {
            if(compSup.isInstalled())
                compSup.uninstall();
            //gc should clean it
            compSup = null;
        }
        compSup = AutoCompleteSupport.install(combo, possibleValuesEventList, new DictionaryElementTextFilterator());        
        compSup.setFilterMode(TextMatcherEditor.CONTAINS);
        return compSup;
    }
    
    /**
     * Executed when:
     * - A key is pressed on codeTextField
     * - codeTextField.setText() without previously indicating avoidActionPerformed = true     
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {    
        if (! avoidActionPerformed) {
            if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.MAX_LENGTH_ACTION_STRING)) {
                try {
                    lookUpAndSetDescription();
                } catch (NullPointerException ne) {
                    if(categoryComboCompSup != null && categoryComboCompSup.isInstalled())
                        categoryComboCompSup.uninstall();
                    categoryCombo.setModel(
                            new javax.swing.DefaultComboBoxModel(
                                    new String[] {java.util.ResourceBundle
                                            .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                            .getString("Dictionary_Error")}));
                    if(descriptionComboCompSup != null && descriptionComboCompSup.isInstalled())
                        descriptionComboCompSup.uninstall();
                    descriptionCombo.setModel(
                            new javax.swing.DefaultComboBoxModel(
                                    new String[] {java.util.ResourceBundle
                                            .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                            .getString("Dictionary_Error")}));
                }
            } else if (e.getActionCommand().equalsIgnoreCase(MaxLengthDocument.CHANGED_ACTION_STRING)) {
                try {
                    lookUpAndSetDescription();
                    checkForChanges();
                } catch (NullPointerException ne) {  
                    if(categoryComboCompSup != null && categoryComboCompSup.isInstalled())
                        categoryComboCompSup.uninstall();
                    categoryCombo.setModel(
                            new javax.swing.DefaultComboBoxModel(
                                    new String[] {java.util.ResourceBundle
                                            .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                            .getString("Dictionary_Error")}));
                    if(descriptionComboCompSup != null && descriptionComboCompSup.isInstalled())
                        descriptionComboCompSup.uninstall();
                    descriptionCombo.setModel(
                            new javax.swing.DefaultComboBoxModel(
                                    new String[] {java.util.ResourceBundle
                                            .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                            .getString("Dictionary_Error")}));
                }
                //updateFilledInStatusColor();
            }
        }    
    }
    
    @Override
    protected void codeTextFieldFocusLost(java.awt.event.FocusEvent evt) {                                              
       try {
            lookUpAndSetDescription();
            updateFilledInStatusColor();
        } catch (NullPointerException e) {
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            if(categoryComboCompSup != null && categoryComboCompSup.isInstalled())
                categoryComboCompSup.uninstall();
            categoryCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle
                                    .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                    .getString("Dictionary_Error")}));
            if(descriptionComboCompSup != null && descriptionComboCompSup.isInstalled())
                descriptionComboCompSup.uninstall();
            descriptionCombo.setModel(
                    new javax.swing.DefaultComboBoxModel(
                            new String[] {java.util.ResourceBundle
                                    .getBundle("canreg/client/gui/dataentry2/components/resources/DictionaryVariableEditorPanel")
                                    .getString("Dictionary_Error")}));
        }
    }             
    
    @Override
    protected void codeTextFieldKeyTyped(java.awt.event.KeyEvent evt) {
        //updateFilledInStatusColor();
        this.actionPerformed(new ActionEvent(this, 0, MaxLengthDocument.CHANGED_ACTION_STRING));
        
        if (dictionary != null && evt.getKeyChar() == '?') {
            if (categoryCombo.isVisible())
                this.categoryCombo.showPopup();
            else if (descriptionCombo.isVisible())
                this.descriptionCombo.showPopup();            
        } 
        
        else if (evt.getKeyChar() == KeyEvent.VK_ENTER ||
                   (evt.getKeyChar() == KeyEvent.VK_TAB && ! evt.isShiftDown())) {   
            //Skip to next VariableEditorPanel if this dictionary code is complete and correct
            if (this.codeTextField.getBackground() == VARIABLE_OK_COLOR
                && this.descriptionCombo.getSelectedIndex() != -1) {

                    this.descriptionCombo.setFocusable(false);                
                    if (this.dictionary.isCompoundDictionary()) 
                        this.categoryCombo.setFocusable(false);
                    
                    transferFocusToNext();
                    this.descriptionCombo.setFocusable(true);
                    
                    if (this.dictionary.isCompoundDictionary()) 
                        this.categoryCombo.setFocusable(true);
            } else
                transferFocusToNext();
        } 
        
        else if (evt.getKeyChar() == KeyEvent.VK_TAB && evt.isShiftDown()) {
            transferFocusToPrevious();
        } 
        
        else if (evt.getKeyChar() == '+') {
            evt.consume();
            transferFocusToPrevious();
        }
    }    
    
    /**
     * Triggered when the user clicks on the arrows crossing button (change
     * sort button)     
     * @param evt 
     */
    private void descriptionToggleItemStateChanged(java.awt.event.ItemEvent evt) {                                                
        if (sortToggle.isSelected()) {            
            sortToggle.setBackground(ORDER_DESCRIPTION_COLOR);
            sortToggle.setIcon(resourceMap.getIcon("sortBy.icon.description")); // NOI18N
            sortToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                             resourceMap.getString("sortByDescriptionSelected.Action.text")); // NOI18N
            
            descriptionComboCompSup = this.changeComboListSorting(descriptionComboCompSup, descriptionCombo, true);
                        
            if (dictionary.isCompoundDictionary()) {
                categoryCombo.removeActionListener(categoryComboListener);
                categoryComboCompSup = this.changeComboListSorting(categoryComboCompSup, categoryCombo, true);
                categoryCombo.addActionListener(categoryComboListener);
            }                                    
        } else {
            sortToggle.setBackground(ORDER_CODE_COLOR);
            sortToggle.setIcon(resourceMap.getIcon("sortBy.icon.code")); // NOI18N
            sortToggle.setToolTipText(resourceMap.getString("sortByLabel.text") + " " + 
                                             resourceMap.getString("sortByCodeSelected.Action.text")); // NOI18N 
           
            descriptionComboCompSup = this.changeComboListSorting(descriptionComboCompSup, descriptionCombo, false);
                        
            if (dictionary.isCompoundDictionary()) {        
                categoryCombo.removeActionListener(categoryComboListener);
                categoryComboCompSup = this.changeComboListSorting(categoryComboCompSup, categoryCombo, false);
                categoryCombo.addActionListener(categoryComboListener);
            }            
        }
    }
    
    /**
     * Changes the sorting (sort by code or sort by description) of a comboBox 
     * list.
     * @param compSup
     * @param combo
     * @param sortByCode
     * @return 
     */
    private AutoCompleteSupport<DictionaryEntry> changeComboListSorting(AutoCompleteSupport<DictionaryEntry> compSup, 
                                                                        JComboBox<DictionaryEntry> combo, 
                                                                        boolean sortByCode) {
        //We need to setup the new comboBox values. We traverse the current combo
        //list because it could be already filtered by a specific category
        DictionaryEntry selectedDescEntry = (DictionaryEntry) combo.getSelectedItem();
        //List<DictionaryEntry> newValuesList = new LinkedList<DictionaryEntry>() {};
        for(int i = 0; i < combo.getItemCount(); i++) {            
            DictionaryEntry entry = compSup.getItemList().get(i);
            if (sortByCode)
                entry.setSortByCode();
            else
                entry.setSortByDescription();            
        }                 
        if (combo.isPopupVisible()) {
            combo.hidePopup();
            combo.revalidate();
            combo.repaint();
            combo.showPopup();
        } else {
            combo.revalidate();
            combo.repaint();
        }
        combo.setSelectedItem(selectedDescEntry);
        return compSup;
    }
    
    /**
     * Triggered when:
     * - A key is pressed in the categoryComboBox (because when presseing a key, 
     * AutoCompleteSupport selects a possible match for what we are writing)
     * - An item is selected when using the mouse
     * @param evt 
     */
    private void categoryComboActionPerformed(java.awt.event.ActionEvent evt) {                                              
        if (categoryCombo.getSelectedItem() != null) { 
            String categoryCode = null;
            try {
                categoryCode = ((DictionaryEntry) categoryCombo.getSelectedItem()).getCode();
            } catch(ClassCastException ex) {
                setDictionary(dictionary);
            }

            if (categoryCode != null) {                
                avoidActionPerformed = true;
                if( ! this.doNotSetText ) 
                    codeTextField.setText(categoryCode);
                avoidActionPerformed = false;
                
                descriptionCombo.removeActionListener(descriptionComboListener);
                List<DictionaryEntry> entries = DictionaryHelper
                        .getDictionaryEntriesCodeStartingWith(categoryCode, 
                                                              dictionary.getDictionaryEntries()
                                                                      .values().toArray(new DictionaryEntry[0]));
                descriptionComboCompSup = this.setComboModel(descriptionComboCompSup, descriptionCombo, entries);
                descriptionCombo.setSelectedIndex(-1);
                descriptionCombo.addActionListener(descriptionComboListener);
            }            
            //categorySelected = (DictionaryEntry) categoryCombo.getSelectedItem();
        } 
        //if selectedItem == null, then we must remove all filters from descriptionComboBox so
        //all items in the descriptionComboBox are available
        else {
            //avoidActionPerformed = true;
            codeTextField.setText("");
            updateFilledInStatusColor();
            checkForChanges();                
            //avoidActionPerformed = false;
            
            descriptionCombo.removeActionListener(descriptionComboListener);
            LinkedList<DictionaryEntry> descriptionPossibleValuesCollection = new LinkedList<DictionaryEntry>();
            
            for(DictionaryEntry entry : dictionary.getDictionaryEntries().values()) {
                if (entry.getCode().length() == dictionary.getFullDictionaryCodeLength())
                    descriptionPossibleValuesCollection.add(entry);                    
            }
            
            descriptionComboCompSup = this.setComboModel(descriptionComboCompSup, 
                                                         descriptionCombo, 
                                                         descriptionPossibleValuesCollection);
            descriptionCombo.addActionListener(descriptionComboListener);
        }
    } 
    
    /**
     * Triggered when:
     * - A key is pressed in the descriptonComboBox (because when pressing a key, 
     * AutoCompleteSupport selects a possible match for what we are writing)
     * - When pressing ENTER, even if there was already a selected item
     * - An item is selected when using the mouse
     * - When the keys UP or DOWN are pressed when traversing the combo's list
     * - descriptionComboBox loses focus
     * @param evt 
     */
    private void descriptionComboActionPerformed(java.awt.event.ActionEvent evt) {                                              
        if (descriptionCombo.getSelectedItem() != null) {
            String descriptionCode = null;
            try {
                 descriptionCode = ((DictionaryEntry) descriptionCombo.getSelectedItem()).getCode();
            } catch(ClassCastException ex) {
                descriptionCombo.removeActionListener(descriptionComboListener);
                descriptionCombo.setSelectedIndex(-1);
                descriptionCombo.addActionListener(descriptionComboListener); 
                //avoidActionPerformed = true;
                codeTextField.setText("");
                updateFilledInStatusColor();
                checkForChanges();
                avoidActionPerformed = false;                
                return;
            }
            if (descriptionCode != null) {
                if (dictionary.isCompoundDictionary() /*&& categoryCombo.getSelectedIndex() == -1*/) {                    
                    DictionaryEntry categoryEntry = DictionaryHelper.
                            getDictionaryEntryBestMatchingSubcode(descriptionCode, 
                                                                  dictionary.getDictionaryEntries()
                                                                    .values().toArray(new DictionaryEntry[0]));
                    if (categoryEntry != null) {
                        categoryCombo.removeActionListener(categoryComboListener);
                        categoryCombo.setSelectedItem(categoryEntry);
                        categoryCombo.addActionListener(categoryComboListener);
                    }  
                }
                updateFilledInStatusColor();
                avoidActionPerformed = true;
                if ( ! this.doNotSetText ) {
                    codeTextField.setText(descriptionCode);                    
                    checkForChanges(); 
                }
                avoidActionPerformed = false;
            }            
        }
        //The user has erased/deleted the text contained in the combobox, pretty much
        //the same as setting the selectedIndex of the combo to -1
        else {
            //avoidActionPerformed = true;
            //codeTextField.setText("");
            codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            //checkForChanges();               
            //avoidActionPerformed = false;
        }
    }    
       
    private void categoryComboBoxKeyTyped(java.awt.event.KeyEvent evt) {
        categoryComboActionPerformed(null);
        
        if (evt.getKeyChar() == KeyEvent.VK_ENTER ||
            evt.getKeyChar() == KeyEvent.VK_TAB)             
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
        
        //Change the sorting
        else if (evt.getKeyChar() == '-') {
            evt.consume();
            this.sortToggle.doClick();
        }
        
        else if (evt.getKeyChar() == '+') {
            evt.consume();
            transferFocusToPrevious();
        }
    }
    
    private void descriptionComboBoxKeyTyped(java.awt.event.KeyEvent evt) {
        descriptionComboActionPerformed(null);

        if (evt.getKeyChar() == KeyEvent.VK_ENTER ||
           evt.getKeyChar() == KeyEvent.VK_TAB) 
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
        
        //Change the sorting
        else if (evt.getKeyChar() == '-') {
            evt.consume();
            this.sortToggle.doClick();
        }
        
        else if (evt.getKeyChar() == '+') {
            evt.consume();
            transferFocusToPrevious();
        }
    }
       
    
    private javax.swing.JComboBox<DictionaryEntry> categoryCombo;    
    private javax.swing.JComboBox<DictionaryEntry> descriptionCombo;
    private javax.swing.JPanel jPanel1;    
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;    
    private javax.swing.JSplitPane outerSplitPane;
    private javax.swing.JSplitPane innerSplitPane;    
    private javax.swing.JToggleButton sortToggle;
}
