/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2018  International Agency for Research on Cancer
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
 */

/*
 * ModifyDatabaseStructureInternalFrame.java
 *
 * Created on 20-Jan-2010, 14:15:41
 */
package canreg.client.gui.management.systemeditor;

import canreg.client.gui.CanRegClientView;
import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseElement;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.management.SystemDescription;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class ModifyDatabaseStructureInternalFrame extends javax.swing.JInternalFrame implements ActionListener {

    private JFileChooser chooser;
    private String fileName;
    public static Globals.StandardVariableNames[] listOfAutomaticlyGeneratedVariables = new Globals.StandardVariableNames[]{
        Globals.StandardVariableNames.CheckStatus,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.MultPrimCode,
        Globals.StandardVariableNames.MultPrimSeq,
        Globals.StandardVariableNames.MultPrimTot,
        Globals.StandardVariableNames.ObsoleteFlagPatientTable,
        Globals.StandardVariableNames.ObsoleteFlagTumourTable,
        Globals.StandardVariableNames.PatientCheckStatus,
        Globals.StandardVariableNames.PatientIDTumourTable,
        Globals.StandardVariableNames.PatientRecordID,
        Globals.StandardVariableNames.PatientRecordIDTumourTable,
        Globals.StandardVariableNames.PatientRecordStatus,
        Globals.StandardVariableNames.PatientUpdateDate,
        Globals.StandardVariableNames.PatientUpdatedBy,
        Globals.StandardVariableNames.PersonSearch,
        Globals.StandardVariableNames.SourceRecordID,
        Globals.StandardVariableNames.TumourID,
        Globals.StandardVariableNames.TumourIDSourceTable,
        // Globals.StandardVariableNames.TumourRecordID,
        Globals.StandardVariableNames.TumourRecordStatus,
        Globals.StandardVariableNames.TumourUnduplicationStatus,
        Globals.StandardVariableNames.TumourUpdateDate,
        Globals.StandardVariableNames.TumourUpdatedBy
    };
    private SystemDescription systemDescription;
    private final JDesktopPane dtp;
    private boolean structureChanged = false;
    private boolean adHoc = false;
    

    /** Creates new form ModifyDatabaseStructureInternalFrame */
    public ModifyDatabaseStructureInternalFrame(JDesktopPane dtp) {
        initComponents();
        this.dtp = dtp;
        regionComboBox.setModel(new DefaultComboBoxModel(new String[]{
                    Globals.REGIONS[1],
                    Globals.REGIONS[2],
                    Globals.REGIONS[3],
                    Globals.REGIONS[4],
                    Globals.REGIONS[5],
                    Globals.REGIONS[6],
                    Globals.REGIONS[9]
                }));
        setListeners();
        // searchVariablesPanel.setEnabled(true);
        // databaseIndexPanel.setVisible(false);
        codingPanel.setVisible(false);
        settingsPanel.setVisible(false);
    }
    
    public JComponent getMainPanel() {
        return this.jSplitPane1;
    }
    
    public String getRegistryName() {
        return registryNameTextField.getText();
    }
    
    public String getRegistryCode() {
        return registryCodeTextField.getText();
    }

    private void setListeners() {
        databaseDictionaryPanel.setActionListener(this);
        databaseVariablePanel.setActionListener(this);
        databaseGroupPanel.setActionListener(this);
        databaseIndexPanel.setActionListener(this);
    }

    public Document getDoc(Document doc) {
        return doc;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        dictionariesScrollPane = new javax.swing.JScrollPane();
        databaseDictionaryPanel = new canreg.client.gui.management.systemeditor.DatabaseDictionariesPanel();
        groupsScrollPane = new javax.swing.JScrollPane();
        databaseGroupPanel = new canreg.client.gui.management.systemeditor.DatabaseGroupsPanel();
        variablesScrollPane = new javax.swing.JScrollPane();
        databaseVariablePanel = new canreg.client.gui.management.systemeditor.DatabaseVariablesPanel();
        searchVariablesScrollPane = new javax.swing.JScrollPane();
        personSearchVariablesPanel = new canreg.client.gui.management.PersonSearchVariablesPanel();
        codingPanel = new javax.swing.JPanel();
        maleCodeLabel = new javax.swing.JLabel();
        maleCodeTextField = new javax.swing.JTextField();
        femaleCodeTextField = new javax.swing.JTextField();
        femaleCodeLabel = new javax.swing.JLabel();
        unknownSexLabel = new javax.swing.JLabel();
        unknownSexTextField = new javax.swing.JTextField();
        dateFormatLabel = new javax.swing.JLabel();
        dateFormatComboBox = new javax.swing.JComboBox();
        dateSeparatorLabel = new javax.swing.JLabel();
        dateSeparatorTextField = new javax.swing.JTextField();
        morphologyLengthLabel = new javax.swing.JLabel();
        morphologyLengthTextField = new javax.swing.JTextField();
        basisCodesCheckBox = new javax.swing.JCheckBox();
        settingsPanel = new javax.swing.JPanel();
        fastSafeModeCheckBox = new javax.swing.JCheckBox();
        mprulesCheckBox = new javax.swing.JCheckBox();
        specialRegistryCheckBox = new javax.swing.JCheckBox();
        strictPasswordModeCheckBox = new javax.swing.JCheckBox();
        indexesScrollPane1 = new javax.swing.JScrollPane();
        databaseIndexPanel = new canreg.client.gui.management.systemeditor.DatabaseIndexPanel();
        generalPanel = new javax.swing.JPanel();
        registryCodeLabel = new javax.swing.JLabel();
        registryCodeTextField = new javax.swing.JTextField();
        checkIfUniqueButton = new javax.swing.JButton();
        regionLabel = new javax.swing.JLabel();
        regionComboBox = new javax.swing.JComboBox();
        registryNameLabel = new javax.swing.JLabel();
        registryNameTextField = new javax.swing.JTextField();
        loadXMLbtn = new javax.swing.JButton();
        saveXML = new javax.swing.JButton();

        setClosable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(ModifyDatabaseStructureInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        jSplitPane1.setDividerSize(0);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(111, 10));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        dictionariesScrollPane.setMinimumSize(new java.awt.Dimension(20, 10));
        dictionariesScrollPane.setName("dictionariesScrollPane"); // NOI18N

        databaseDictionaryPanel.setMinimumSize(new java.awt.Dimension(64, 10));
        databaseDictionaryPanel.setName("databaseDictionaryPanel"); // NOI18N
        dictionariesScrollPane.setViewportView(databaseDictionaryPanel);

        jTabbedPane1.addTab(resourceMap.getString("dictionariesScrollPane.TabConstraints.tabTitle"), dictionariesScrollPane); // NOI18N
        dictionariesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        groupsScrollPane.setName("groupsScrollPane"); // NOI18N

        databaseGroupPanel.setName("databaseGroupPanel"); // NOI18N
        groupsScrollPane.setViewportView(databaseGroupPanel);

        jTabbedPane1.addTab(resourceMap.getString("groupsScrollPane.TabConstraints.tabTitle"), groupsScrollPane); // NOI18N
        groupsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        variablesScrollPane.setName("variablesScrollPane"); // NOI18N

        databaseVariablePanel.setName("databaseVariablePanel"); // NOI18N
        variablesScrollPane.setViewportView(databaseVariablePanel);

        jTabbedPane1.addTab(resourceMap.getString("variablesScrollPane.TabConstraints.tabTitle"), variablesScrollPane); // NOI18N
        variablesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        searchVariablesScrollPane.setName("searchVariablesScrollPane"); // NOI18N

        personSearchVariablesPanel.setName("personSearchVariablesPanel"); // NOI18N
        searchVariablesScrollPane.setViewportView(personSearchVariablesPanel);

        jTabbedPane1.addTab(resourceMap.getString("searchVariablesScrollPane.TabConstraints.tabTitle"), searchVariablesScrollPane); // NOI18N
        searchVariablesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        codingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("codingPanel.border.title"))); // NOI18N
        codingPanel.setName("codingPanel"); // NOI18N

        maleCodeLabel.setText(resourceMap.getString("maleCodeLabel.text")); // NOI18N
        maleCodeLabel.setName("maleCodeLabel"); // NOI18N

        maleCodeTextField.setText(resourceMap.getString("maleCodeTextField.text")); // NOI18N
        maleCodeTextField.setName("maleCodeTextField"); // NOI18N

        femaleCodeTextField.setText(resourceMap.getString("femaleCodeTextField.text")); // NOI18N
        femaleCodeTextField.setName("femaleCodeTextField"); // NOI18N

        femaleCodeLabel.setText(resourceMap.getString("femaleCodeLabel.text")); // NOI18N
        femaleCodeLabel.setName("femaleCodeLabel"); // NOI18N

        unknownSexLabel.setText(resourceMap.getString("unknownSexLabel.text")); // NOI18N
        unknownSexLabel.setName("unknownSexLabel"); // NOI18N

        unknownSexTextField.setEditable(false);
        unknownSexTextField.setText(resourceMap.getString("unknownSexTextField.text")); // NOI18N
        unknownSexTextField.setName("unknownSexTextField"); // NOI18N

        dateFormatLabel.setText(resourceMap.getString("dateFormatLabel.text")); // NOI18N
        dateFormatLabel.setName("dateFormatLabel"); // NOI18N

        dateFormatComboBox.setEditable(true);
        dateFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "dd/mm/yyyy", "mm/dd/yyyy", "mm/dd/yy", "yyyy/mm/dd" }));
        dateFormatComboBox.setName("dateFormatComboBox"); // NOI18N

        dateSeparatorLabel.setText(resourceMap.getString("dateSeparatorLabel.text")); // NOI18N
        dateSeparatorLabel.setName("dateSeparatorLabel"); // NOI18N

        dateSeparatorTextField.setText(resourceMap.getString("dateSeparatorTextField.text")); // NOI18N
        dateSeparatorTextField.setName("dateSeparatorTextField"); // NOI18N

        morphologyLengthLabel.setText(resourceMap.getString("morphologyLengthLabel.text")); // NOI18N
        morphologyLengthLabel.setName("morphologyLengthLabel"); // NOI18N

        morphologyLengthTextField.setEditable(false);
        morphologyLengthTextField.setText(resourceMap.getString("morphologyLengthTextField.text")); // NOI18N
        morphologyLengthTextField.setName("morphologyLengthTextField"); // NOI18N

        basisCodesCheckBox.setSelected(true);
        basisCodesCheckBox.setText(resourceMap.getString("basisCodesCheckBox.text")); // NOI18N
        basisCodesCheckBox.setName("basisCodesCheckBox"); // NOI18N

        javax.swing.GroupLayout codingPanelLayout = new javax.swing.GroupLayout(codingPanel);
        codingPanel.setLayout(codingPanelLayout);
        codingPanelLayout.setHorizontalGroup(
            codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(codingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(codingPanelLayout.createSequentialGroup()
                        .addGroup(codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(codingPanelLayout.createSequentialGroup()
                                .addComponent(morphologyLengthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(morphologyLengthTextField))
                            .addGroup(codingPanelLayout.createSequentialGroup()
                                .addComponent(maleCodeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maleCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(femaleCodeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(femaleCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(codingPanelLayout.createSequentialGroup()
                                .addComponent(dateFormatLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateFormatComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addGroup(codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(codingPanelLayout.createSequentialGroup()
                                .addComponent(unknownSexLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unknownSexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(codingPanelLayout.createSequentialGroup()
                                .addComponent(dateSeparatorLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateSeparatorTextField, 0, 1, Short.MAX_VALUE))))
                    .addComponent(basisCodesCheckBox))
                .addContainerGap(159, Short.MAX_VALUE))
        );
        codingPanelLayout.setVerticalGroup(
            codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(codingPanelLayout.createSequentialGroup()
                .addGroup(codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maleCodeLabel)
                    .addComponent(maleCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(femaleCodeLabel)
                    .addComponent(unknownSexLabel)
                    .addComponent(femaleCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unknownSexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateFormatLabel)
                    .addComponent(dateFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateSeparatorLabel)
                    .addComponent(dateSeparatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(codingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(morphologyLengthLabel)
                    .addComponent(morphologyLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(basisCodesCheckBox)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("codingPanel.TabConstraints.tabTitle"), codingPanel); // NOI18N

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("settingsPanel.border.title"))); // NOI18N
        settingsPanel.setName("settingsPanel"); // NOI18N

        fastSafeModeCheckBox.setText(resourceMap.getString("fastSafeModeCheckBox.text")); // NOI18N
        fastSafeModeCheckBox.setToolTipText(resourceMap.getString("fastSafeModeCheckBox.toolTipText")); // NOI18N
        fastSafeModeCheckBox.setEnabled(false);
        fastSafeModeCheckBox.setName("fastSafeModeCheckBox"); // NOI18N

        mprulesCheckBox.setSelected(true);
        mprulesCheckBox.setText(resourceMap.getString("mprulesCheckBox.text")); // NOI18N
        mprulesCheckBox.setName("mprulesCheckBox"); // NOI18N

        specialRegistryCheckBox.setText(resourceMap.getString("specialRegistryCheckBox.text")); // NOI18N
        specialRegistryCheckBox.setName("specialRegistryCheckBox"); // NOI18N

        strictPasswordModeCheckBox.setText(resourceMap.getString("strictPasswordModeCheckBox.text")); // NOI18N
        strictPasswordModeCheckBox.setToolTipText(resourceMap.getString("strictPasswordModeCheckBox.toolTipText")); // NOI18N
        strictPasswordModeCheckBox.setEnabled(false);
        strictPasswordModeCheckBox.setName("strictPasswordModeCheckBox"); // NOI18N

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fastSafeModeCheckBox)
                    .addComponent(mprulesCheckBox)
                    .addComponent(specialRegistryCheckBox)
                    .addComponent(strictPasswordModeCheckBox))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addComponent(fastSafeModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mprulesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(specialRegistryCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strictPasswordModeCheckBox))
        );

        jTabbedPane1.addTab(resourceMap.getString("settingsPanel.TabConstraints.tabTitle"), settingsPanel); // NOI18N

        indexesScrollPane1.setName("indexesScrollPane1"); // NOI18N

        databaseIndexPanel.setName("databaseIndexPanel"); // NOI18N
        indexesScrollPane1.setViewportView(databaseIndexPanel);

        jTabbedPane1.addTab(resourceMap.getString("indexesScrollPane1.TabConstraints.tabTitle"), indexesScrollPane1); // NOI18N
        indexesScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        jSplitPane1.setRightComponent(jTabbedPane1);

        generalPanel.setName("generalPanel"); // NOI18N

        registryCodeLabel.setText(resourceMap.getString("registryCodeLabel.text")); // NOI18N
        registryCodeLabel.setName("registryCodeLabel"); // NOI18N

        registryCodeTextField.setText(resourceMap.getString("registryCodeTextField.text")); // NOI18N
        registryCodeTextField.setName("registryCodeTextField"); // NOI18N
        registryCodeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registryCodeTextFieldActionPerformed(evt);
            }
        });
        registryCodeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                registryCodeTextFieldFocusLost(evt);
            }
        });
        registryCodeTextField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                registryCodeTextFieldPropertyChange(evt);
            }
        });

        checkIfUniqueButton.setText(resourceMap.getString("checkIfUniqueButton.text")); // NOI18N
        checkIfUniqueButton.setToolTipText(resourceMap.getString("checkIfUniqueButton.toolTipText")); // NOI18N
        checkIfUniqueButton.setEnabled(false);
        checkIfUniqueButton.setName("checkIfUniqueButton"); // NOI18N

        regionLabel.setText(resourceMap.getString("regionLabel.text")); // NOI18N
        regionLabel.setName("regionLabel"); // NOI18N

        regionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        regionComboBox.setName("regionComboBox"); // NOI18N

        registryNameLabel.setText(resourceMap.getString("registryNameLabel.text")); // NOI18N
        registryNameLabel.setName("registryNameLabel"); // NOI18N

        registryNameTextField.setText(resourceMap.getString("registryNameTextField.text")); // NOI18N
        registryNameTextField.setName("registryNameTextField"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(ModifyDatabaseStructureInternalFrame.class, this);
        loadXMLbtn.setAction(actionMap.get("pickXML")); // NOI18N
        loadXMLbtn.setName("loadXMLbtn"); // NOI18N

        saveXML.setAction(actionMap.get("saveXML")); // NOI18N
        saveXML.setName("saveXML"); // NOI18N

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(registryCodeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(registryCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkIfUniqueButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(regionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(regionComboBox, 0, 82, Short.MAX_VALUE))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(registryNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(registryNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(loadXMLbtn)
                    .addComponent(saveXML))
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registryNameLabel)
                    .addComponent(registryNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadXMLbtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registryCodeLabel)
                    .addComponent(registryCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkIfUniqueButton)
                    .addComponent(regionLabel)
                    .addComponent(regionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveXML))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(generalPanel);

        getContentPane().add(jSplitPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void registryCodeTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_registryCodeTextFieldPropertyChange
        // System.out.println("Code changed: Property");
        // codeChanged(registryCodeTextField.getText());
    }//GEN-LAST:event_registryCodeTextFieldPropertyChange

    private void registryCodeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registryCodeTextFieldActionPerformed
        // System.out.println("Code changed: Action");
        codeChanged(registryCodeTextField.getText());
    }//GEN-LAST:event_registryCodeTextFieldActionPerformed

    private void registryCodeTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_registryCodeTextFieldFocusLost
        // System.out.println("Code changed: Focus");
        codeChanged(registryCodeTextField.getText());
    }//GEN-LAST:event_registryCodeTextFieldFocusLost

    private void codeChanged(String code) {
        if(this.dtp == null)
            return;
        
        if (checkCode(code)) {
            JOptionPane.showMessageDialog(this, "Database '" + code + "' exists.\nIf you haven't done so already, please connect to your CanReg system and perform a backup before proceeding.\nPlease refer to the handbook for more information on what changes you can do to a running CanReg5 system.", "Database exists", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean checkCode(String code) {
        // does the database folder exist?
        File file = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + code);
        return file.exists();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Action
    public void pickXML() {
        // open browser - default folder where the current XML is stored, if possible...
        if (chooser == null) {
            chooser = new JFileChooser(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER);
        }
        // pick XML file
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        }
        openXML();
    }
    
    public void pickXML(String pathToXML) {
        fileName = pathToXML;
        openXML();
    }

    public void openXML() {
        // load system desc
        systemDescription = new SystemDescription(fileName);

        personSearchVariablesPanel.setDoc(systemDescription.getSystemDescriptionDocument());
        personSearchVariablesPanel.toggleDefaultSettingsAction();
        personSearchVariablesPanel.setDefaultButtonVisibility(false);

        registryNameTextField.setText(systemDescription.getRegistryName());
        registryCodeTextField.setText(systemDescription.getRegistryCode());
        regionComboBox.setSelectedItem(systemDescription.getRegion());
        databaseVariablePanel.setElements(systemDescription.getDatabaseVariableListElements());
        databaseVariablePanel.setDefaultGroup(systemDescription.getDatabaseGroupsListElements()[1]);
        databaseGroupPanel.setElements(systemDescription.getDatabaseGroupsListElements());
        databaseDictionaryPanel.setElements(systemDescription.getDatabaseDictionaryListElements());
        databaseIndexPanel.setElements(systemDescription.getDatabaseIndexesListElements());

        // coding
        dateFormatComboBox.setSelectedItem(systemDescription.getDateFormat());
        maleCodeTextField.setText(systemDescription.getTextContentFromElement("male_code"));
        femaleCodeTextField.setText(systemDescription.getTextContentFromElement("female_code"));
        unknownSexTextField.setText(systemDescription.getTextContentFromElement("unknown_sex_code"));
        morphologyLengthTextField.setText(systemDescription.getTextContentFromElement("morphology_length"));
        basisCodesCheckBox.setSelected(!"1".equals(systemDescription.getTextContentFromElement("basis_diag_codes")));

        // settings
        fastSafeModeCheckBox.setSelected(!"1".equals(systemDescription.getTextContentFromElement("fast_safe_mode")));
        mprulesCheckBox.setSelected(!"1".equals(systemDescription.getTextContentFromElement("mult_prim_rules")));
        specialRegistryCheckBox.setSelected("1".equals(systemDescription.getTextContentFromElement("special_registry")));
        strictPasswordModeCheckBox.setSelected("1".equals(systemDescription.getTextContentFromElement("password_rules")));

        codeChanged(registryCodeTextField.getText());
    }
    
    public void saveXML(String xmlPath) {
        fileName = xmlPath;
        systemDescription = new SystemDescription(fileName);
        saveXML();
    }

    @Action
    public void saveXML() {
        saveXML.setEnabled(false);
        
        if(! adHoc) {
            // first check to see if all minimum required variables are present
            Set<String> missingStandardVariables = Tools.getMissingStandardVariables((DatabaseVariablesListElement[]) databaseVariablePanel.getDatabaseElements());
            // In this editor we only warn - during database boot we will stop if not all variables are there...
            if (!missingStandardVariables.isEmpty()) {
                String warning = "Warning! The following variables are missing from the minimum required set of variables:";
                for (String variable : missingStandardVariables) {
                    warning += "\n" + variable;
                }
                warning += "\n\nDo you still want to save?";
                int option = JOptionPane.showConfirmDialog(this, warning, "Warning!", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            if (checkCode(registryCodeTextField.getText())) {
                // overwriting the XML of a running system
                if (structureChanged) {
                    // don't let the user save their new XML
                    JOptionPane.showMessageDialog(this, "Database '" + registryCodeTextField.getText() + "' exists and you have done changes to the structure of the database.\n"
                            + "You can't save this XML with this code before you have deleted the old database files.\n"
                            + "Please refer to the handbook for more information on this.", "Database exists", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        // refresh the doc
        // set the system stuff
        systemDescription.setRegistryName(registryNameTextField.getText());
        systemDescription.setRegistryCode(registryCodeTextField.getText());
        systemDescription.setRegionCode(Arrays.asList(Globals.REGIONS).indexOf(regionComboBox.getSelectedItem()));
        systemDescription.setDictionaries((DatabaseDictionaryListElement[]) databaseDictionaryPanel.getDatabaseElements());
        systemDescription.setGroups((DatabaseGroupsListElement[]) databaseGroupPanel.getDatabaseElements());
        systemDescription.setVariables((DatabaseVariablesListElement[]) databaseVariablePanel.getDatabaseElements());
        systemDescription.setIndexes((DatabaseIndexesListElement[]) databaseIndexPanel.getDatabaseElements());
        systemDescription.setPersonSearcher((PersonSearcher) personSearchVariablesPanel.getSearcher());
        
        File oldFile = null;
        File file = new File(fileName);
        if(! adHoc) {
            fileName = Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + File.separator + registryCodeTextField.getText().trim() + ".xml";
                
            if (file.exists()) {
                int i = 0;
                oldFile = new File(fileName + "." + i);
                while (oldFile.exists()) {
                    i++;
                    oldFile = new File(fileName + "." + i);
                }
                file.renameTo(oldFile.getAbsoluteFile());
            }
        }
        
        try {
            systemDescription.saveSystemDescriptionXML(fileName);
            String message = java.util.ResourceBundle.getBundle("canreg/client/gui/management/systemeditor/resources/ModifyDatabaseStructureInternalFrame").getString("SYSTEM_DEFINITION_SAVED_AS_") + " " + fileName + ".";
            if (oldFile != null) {
                message += "\n" + java.util.ResourceBundle.getBundle("canreg/client/gui/management/systemeditor/resources/ModifyDatabaseStructureInternalFrame").getString("OLD_FILE_BACKED_UP_AS_") + oldFile.getAbsolutePath();
            }
            JOptionPane.showMessageDialog(this, message, "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex) {
             // Rename XML to error...
            file = new File(fileName);
            file.renameTo(new File(fileName + "-error.xml"));
            // Restore old xml if possible...
            if (oldFile != null) {
                oldFile.renameTo(new File(fileName));
            }
            String message = "Something went wrong... Please look into the links between dictionaries, indexes, person search and variables";
            JOptionPane.showMessageDialog(this, message, "Not saved", JOptionPane.INFORMATION_MESSAGE);
        }
        saveXML.setEnabled(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox basisCodesCheckBox;
    private javax.swing.JButton checkIfUniqueButton;
    private javax.swing.JPanel codingPanel;
    private canreg.client.gui.management.systemeditor.DatabaseDictionariesPanel databaseDictionaryPanel;
    private canreg.client.gui.management.systemeditor.DatabaseGroupsPanel databaseGroupPanel;
    private canreg.client.gui.management.systemeditor.DatabaseIndexPanel databaseIndexPanel;
    private canreg.client.gui.management.systemeditor.DatabaseVariablesPanel databaseVariablePanel;
    private javax.swing.JComboBox dateFormatComboBox;
    private javax.swing.JLabel dateFormatLabel;
    private javax.swing.JLabel dateSeparatorLabel;
    private javax.swing.JTextField dateSeparatorTextField;
    private javax.swing.JScrollPane dictionariesScrollPane;
    private javax.swing.JCheckBox fastSafeModeCheckBox;
    private javax.swing.JLabel femaleCodeLabel;
    private javax.swing.JTextField femaleCodeTextField;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JScrollPane groupsScrollPane;
    private javax.swing.JScrollPane indexesScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton loadXMLbtn;
    private javax.swing.JLabel maleCodeLabel;
    private javax.swing.JTextField maleCodeTextField;
    private javax.swing.JLabel morphologyLengthLabel;
    private javax.swing.JTextField morphologyLengthTextField;
    private javax.swing.JCheckBox mprulesCheckBox;
    private canreg.client.gui.management.PersonSearchVariablesPanel personSearchVariablesPanel;
    private javax.swing.JComboBox regionComboBox;
    private javax.swing.JLabel regionLabel;
    private javax.swing.JLabel registryCodeLabel;
    private javax.swing.JTextField registryCodeTextField;
    private javax.swing.JLabel registryNameLabel;
    private javax.swing.JTextField registryNameTextField;
    private javax.swing.JButton saveXML;
    private javax.swing.JScrollPane searchVariablesScrollPane;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JCheckBox specialRegistryCheckBox;
    private javax.swing.JCheckBox strictPasswordModeCheckBox;
    private javax.swing.JLabel unknownSexLabel;
    private javax.swing.JTextField unknownSexTextField;
    private javax.swing.JScrollPane variablesScrollPane;
    // End of variables declaration//GEN-END:variables

    private void fillAutoVariables() {
        // TODO write an fillAutoVariables makes sure all system variables are present...
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(DatabaseElementsPanel.UPDATED)) {
            databaseVariablePanel.redrawTable();

        } else if (e.getActionCommand().equals(DatabaseVariableEditorInternalFrame.STANDARDVARIABLEMAPPINGCHANGED)) {
            DatabaseVariableEditorPanel dbve = (DatabaseVariableEditorPanel) e.getSource();
            DatabaseVariablesListElement variable = databaseVariablePanel.isThisStandardVariableAlreadyMapped(dbve.getStandardVariable());

            if (variable != null && variable != dbve.getDatabaseVariablesListElement()) {
                JOptionPane.showInternalMessageDialog(dbve,
                        "This standard variable is already mapped to " + variable.getFullName() + ". Please revise.");

            }
        } else if (e.getActionCommand().equals(DatabaseVariableEditorInternalFrame.SHORTNAMECHANGED)) {
            DatabaseVariableEditorPanel dbve = (DatabaseVariableEditorPanel) e.getSource();
            DatabaseVariablesListElement variable = databaseVariablePanel.isThisShortVariableNameAlreadyPresent(dbve.getShortName());

            if (variable != null && variable != dbve.getDatabaseVariablesListElement()) {
                JOptionPane.showInternalMessageDialog(dbve,
                        "This standard variable is already mapped to " + variable.getFullName() + ". Please revise.");

            }
        } else if (e.getActionCommand().equals(DatabaseVariableEditorInternalFrame.UPDATED)) {
            databaseVariablePanel.redrawTable();


        } else if (e.getActionCommand().equals(DatabaseGroupEditorInternalFrame.UPDATED)) {
            databaseGroupPanel.redrawTable();


        } else if (e.getActionCommand().equals(DatabaseDictionaryEditorInternalFrame.UPDATED)) {
            databaseDictionaryPanel.redrawTable();


        } else if (e.getActionCommand().equals(DatabaseIndexEditorInternalFrame.UPDATED)) {
            databaseIndexPanel.redrawTable();

        } else if (e.getActionCommand().equals(DatabaseElementPanel.STRUCTURE_CHANGE_ACTION)) {
            if (!structureChanged) {
                this.setTitle(this.getTitle() + " - Database structure changed");
                structureChanged = true;
            }
        } else if (e.getActionCommand().equals(DatabaseElementPanel.EDIT_ACTION)) {
            if (systemDescription != null) {
                DatabaseElementPanel ep = (DatabaseElementPanel) e.getSource();
                DatabaseElement dbe = ep.getDatabaseElement();
                if (dbe instanceof DatabaseVariablesListElement) {
                    DatabaseVariableEditorInternalFrame dveif = new DatabaseVariableEditorInternalFrame();
                    dveif.setDictionaries(databaseDictionaryPanel.getDatabaseElements());
                    dveif.setGroups(databaseGroupPanel.getDatabaseElements());
                    dveif.setDatabaseVariablesListElement((DatabaseVariablesListElement) dbe);
                    dveif.setActionListener(this);
                    CanRegClientView.showAndPositionInternalFrame(dtp, dveif);
                } else if (dbe instanceof DatabaseDictionaryListElement) {
                    DatabaseDictionaryEditorInternalFrame dveif = new DatabaseDictionaryEditorInternalFrame();
                    dveif.setDatabaseDictionaryListElement((DatabaseDictionaryListElement) dbe);
                    dveif.setActionListener(this);
                    CanRegClientView.showAndPositionInternalFrame(dtp, dveif);
                } else if (dbe instanceof DatabaseGroupsListElement) {
                    DatabaseGroupEditorInternalFrame dveif = new DatabaseGroupEditorInternalFrame();
                    dveif.setDatabaseGroupsListElement((DatabaseGroupsListElement) dbe);
                    dveif.setActionListener(this);
                    CanRegClientView.showAndPositionInternalFrame(dtp, dveif);
                } else if (dbe instanceof DatabaseIndexesListElement) {
                    DatabaseIndexEditorInternalFrame diep = new DatabaseIndexEditorInternalFrame();
                    diep.setVariablesInDatabase((DatabaseVariablesListElement[]) databaseVariablePanel.getDatabaseElements());
                    diep.setDatabaseIndexesListElement((DatabaseIndexesListElement) dbe);
                    diep.setActionListener(this);
                    CanRegClientView.showAndPositionInternalFrame(dtp, diep);
                }
            } else {
                JOptionPane.showInternalMessageDialog(this,
                        "Please load a system definition XML first.");
            }
        }
    }

    public void configureForAdHoc() {
        this.adHoc = true;
        fileName = Globals.ADHOC_SYSTEM_XML;
        openXML();
        this.loadXMLbtn.setVisible(false);
        this.saveXML.setVisible(false);
        this.jTabbedPane1.setVisible(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.registryNameTextField.setEnabled(enabled);
        this.registryCodeTextField.setEnabled(enabled);
        this.regionComboBox.setEnabled(enabled);
        this.loadXMLbtn.setEnabled(enabled);
        this.saveXML.setEnabled(enabled);
        this.jTabbedPane1.setEnabled(enabled);
    }
    
    
}
