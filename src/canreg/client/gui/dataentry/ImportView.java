/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

/*
 * ImportView.java
 *
 * Created on 22 February 2008, 09:30
 */
package canreg.client.gui.dataentry;

import au.com.bytecode.opencsv.CSVReader;
import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.dataentry.Import;
import canreg.client.dataentry.ImportOptions;
import canreg.client.dataentry.Relation;
import canreg.client.gui.LoginInternalFrame;
import canreg.client.gui.components.VariableMappingPanel;
import canreg.client.gui.tools.WaitFrame;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.server.database.RecordLockedException;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author morten
 *
 * This module 
 *
 * TODO: Accept various date formats?
 * Implement the various options
 *
 *
 */
public class ImportView extends javax.swing.JInternalFrame {

    private boolean needToRebuildVariableMap = true;
    private File inFile;
    private Document doc;
    private List<VariableMappingPanel> panelList;
    private DatabaseVariablesListElement[] variablesInDB;
    private JFileChooser chooser;
    private String path;
    private LocalSettings localSettings;
    private GlobalToolBox globalToolBox;
    private Task importTask;
    //<ictl.co>
    JDesktopPane desktopPane;
    //</ictl.co>

    /** Creates new form ImportView */
    public ImportView(/*<ictl.co>*/JDesktopPane desktopPane/*</ictl.co>*/) {
        //<ictl.co>
        this.desktopPane = desktopPane;
        //</ictl.co>
        initComponents();
        previewPanel.setVisible(false);

        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();

        changeTab(0);

        // Add a listener for changing the active tab
        ChangeListener tabbedPaneChangeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                initializeVariableMappingTab();
                changeTab(tabbedPane.getSelectedIndex());
            }
        };
        // And add the listener to the tabbedPane
        tabbedPane.addChangeListener(tabbedPaneChangeListener);

        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        path = localSettings.getProperty("import_path");

        if (path == null) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(path);
        }
        // Group the radiobuttons
        ButtonGroup discrepanciesButtonGroup = new ButtonGroup();
        // Add to the button group
        discrepanciesButtonGroup.add(rejectRadioButton);
        discrepanciesButtonGroup.add(updateRadioButton);
        discrepanciesButtonGroup.add(overwriteRadioButton);

        // Get the system description
        doc = CanRegClientApp.getApplication().getDatabseDescription();
        variablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE);

        // get the available charsets
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        charsetsComboBox.setModel(new javax.swing.DefaultComboBoxModel(charsets.values().toArray()));
        // set the default mapping
        charsetsComboBox.setSelectedItem(globalToolBox.getStandardCharset());
        // initializeVariableMappingTab();
    }

    private void changeFile() {
        inFile = new File(fileNameTextField.getText().trim());
        path = inFile.getPath();
        needToRebuildVariableMap = true;
        try {
            numberOfRecordsTextField.setText("" + (canreg.common.Tools.numberOfLinesInFile(inFile.getAbsolutePath()) - 1));
            // autodetectFileEncodingAction();
        } catch (IOException ex) {
            Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void changeTab(int tabNumber) {
        tabbedPane.setSelectedIndex(tabNumber);
        nextButton.setEnabled(tabNumber < tabbedPane.getTabCount() - 1);
        backButton.setEnabled(tabNumber > 0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        chooseFilePanel = new javax.swing.JPanel();
        fileNameTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        numberOfRecordsLabel = new javax.swing.JLabel();
        numberOfRecordsTextField = new javax.swing.JTextField();
        previewTableScrollPane = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();
        numberOfRecordsShownLabel = new javax.swing.JLabel();
        numberOfRecordsShownTextField = new javax.swing.JTextField();
        previewButton = new javax.swing.JButton();
        fileLabel = new javax.swing.JLabel();
        separatingCharacterComboBox = new javax.swing.JComboBox();
        separatingCharacterLabel = new javax.swing.JLabel();
        autodetectButton = new javax.swing.JButton();
        fileEncodingLabel = new javax.swing.JLabel();
        charsetsComboBox = new javax.swing.JComboBox();
        associateVariablesPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        variablesScrollPane = new javax.swing.JScrollPane();
        variablesPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        importFilePanel = new javax.swing.JPanel();
        importButton = new javax.swing.JButton();
        discrepanciesPanel = new javax.swing.JPanel();
        rejectRadioButton = new javax.swing.JRadioButton();
        updateRadioButton = new javax.swing.JRadioButton();
        overwriteRadioButton = new javax.swing.JRadioButton();
        jPanel7 = new javax.swing.JPanel();
        doChecksCheckBox = new javax.swing.JCheckBox();
        personSearchCheckBox = new javax.swing.JCheckBox();
        queryNewNameCheckBox = new javax.swing.JCheckBox();
        previousCanRegDataCheckBox = new javax.swing.JCheckBox();
        maxLinesPanel = new javax.swing.JPanel();
        maxLinesTextField = new javax.swing.JTextField();
        testOnlyCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        nextButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(ImportView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        tabbedPane.setName("tabbedPane"); // NOI18N

        chooseFilePanel.setName("chooseFilePanel"); // NOI18N

        fileNameTextField.setName("fileNameTextField"); // NOI18N
        fileNameTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fileNameTextFieldMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileNameTextFieldMouseReleased(evt);
            }
        });
        fileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fileNameTextFieldFocusLost(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(ImportView.class, this);
        browseButton.setAction(actionMap.get("browseFiles")); // NOI18N
        browseButton.setName("browseButton"); // NOI18N

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("previewPanel.border.title"))); // NOI18N
        previewPanel.setEnabled(false);
        previewPanel.setName("previewPanel"); // NOI18N

        numberOfRecordsLabel.setText(resourceMap.getString("numberOfRecordsLabel.text")); // NOI18N
        numberOfRecordsLabel.setFocusable(false);
        numberOfRecordsLabel.setName("numberOfRecordsLabel"); // NOI18N

        numberOfRecordsTextField.setEditable(false);
        numberOfRecordsTextField.setText(resourceMap.getString("numberOfRecordsTextField.text")); // NOI18N
        numberOfRecordsTextField.setFocusable(false);
        numberOfRecordsTextField.setName("numberOfRecordsTextField"); // NOI18N

        previewTableScrollPane.setName("previewTableScrollPane"); // NOI18N

        previewTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null}
                },
                new String[]{
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }
        ));
        previewTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        previewTable.setEnabled(false);
        previewTable.setFocusable(false);
        previewTable.setName("previewTable"); // NOI18N
        previewTableScrollPane.setViewportView(previewTable);

        numberOfRecordsShownLabel.setText(resourceMap.getString("numberOfRecordsShownLabel.text")); // NOI18N
        numberOfRecordsShownLabel.setFocusable(false);
        numberOfRecordsShownLabel.setName("numberOfRecordsShownLabel"); // NOI18N

        numberOfRecordsShownTextField.setEditable(false);
        numberOfRecordsShownTextField.setFocusable(false);
        numberOfRecordsShownTextField.setName("numberOfRecordsShownTextField"); // NOI18N

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
                previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(previewPanelLayout.createSequentialGroup()
                                .addComponent(numberOfRecordsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numberOfRecordsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(numberOfRecordsShownLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numberOfRecordsShownTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
                        .addComponent(previewTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
                previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(previewPanelLayout.createSequentialGroup()
                                .addGroup(previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(numberOfRecordsLabel)
                                        .addComponent(numberOfRecordsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(numberOfRecordsShownLabel)
                                        .addComponent(numberOfRecordsShownTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(previewTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
        );

        previewButton.setAction(actionMap.get("previewAction")); // NOI18N
        previewButton.setName("previewButton"); // NOI18N

        fileLabel.setText(resourceMap.getString("fileLabel.text")); // NOI18N
        fileLabel.setName("fileLabel"); // NOI18N

        separatingCharacterComboBox.setEditable(true);
        separatingCharacterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Tab", "Comma"}));
        separatingCharacterComboBox.setAction(actionMap.get("comboBoxChanged")); // NOI18N
        separatingCharacterComboBox.setName("separatingCharacterComboBox"); // NOI18N

        separatingCharacterLabel.setText(resourceMap.getString("separatingCharacterLabel.text")); // NOI18N
        separatingCharacterLabel.setName("separatingCharacterLabel"); // NOI18N

        autodetectButton.setAction(actionMap.get("autodetectFileEncodingAction")); // NOI18N
        autodetectButton.setText(resourceMap.getString("autodetectButton.text")); // NOI18N
        autodetectButton.setToolTipText(resourceMap.getString("autodetectButton.toolTipText")); // NOI18N
        autodetectButton.setName("autodetectButton"); // NOI18N

        fileEncodingLabel.setText(resourceMap.getString("fileEncodingLabel.text")); // NOI18N
        fileEncodingLabel.setName("fileEncodingLabel"); // NOI18N

        charsetsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        charsetsComboBox.setName("charsetsComboBox"); // NOI18N

        javax.swing.GroupLayout chooseFilePanelLayout = new javax.swing.GroupLayout(chooseFilePanel);
        chooseFilePanel.setLayout(chooseFilePanelLayout);
        chooseFilePanelLayout.setHorizontalGroup(
                chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chooseFilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(previewPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(chooseFilePanelLayout.createSequentialGroup()
                                                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(chooseFilePanelLayout.createSequentialGroup()
                                                                .addComponent(fileLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(fileNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                                                        .addGroup(chooseFilePanelLayout.createSequentialGroup()
                                                                .addComponent(fileEncodingLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(charsetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(autodetectButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(separatingCharacterLabel)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(separatingCharacterComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chooseFilePanelLayout.createSequentialGroup()
                                                                .addComponent(browseButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(previewButton)))))
                                .addContainerGap())
        );
        chooseFilePanelLayout.setVerticalGroup(
                chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(chooseFilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(previewButton)
                                        .addComponent(browseButton)
                                        .addComponent(fileLabel)
                                        .addComponent(fileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileEncodingLabel)
                                        .addComponent(charsetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(separatingCharacterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(autodetectButton)
                                        .addComponent(separatingCharacterLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        tabbedPane.addTab("Choose File", chooseFilePanel);

        associateVariablesPanel.setName("associateVariablesPanel"); // NOI18N

        jLabel8.setName("jLabel8"); // NOI18N

        variablesScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        variablesScrollPane.setName("variablesScrollPane"); // NOI18N

        variablesPanel.setName("variablesPanel"); // NOI18N
        variablesPanel.setLayout(new java.awt.GridLayout(0, 1));
        variablesScrollPane.setViewportView(variablesPanel);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jSplitPane1.setLeftComponent(jLabel2);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setMaximumSize(new java.awt.Dimension(139, 14));
        jLabel4.setMinimumSize(new java.awt.Dimension(139, 14));
        jLabel4.setName("jLabel4"); // NOI18N
        jSplitPane1.setRightComponent(jLabel4);

        javax.swing.GroupLayout associateVariablesPanelLayout = new javax.swing.GroupLayout(associateVariablesPanel);
        associateVariablesPanel.setLayout(associateVariablesPanelLayout);
        associateVariablesPanelLayout.setHorizontalGroup(
                associateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(associateVariablesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(associateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                                        .addComponent(jLabel8)
                                        .addComponent(variablesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE))
                                .addContainerGap())
        );
        associateVariablesPanelLayout.setVerticalGroup(
                associateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(associateVariablesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(variablesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                .addContainerGap())
        );

        tabbedPane.addTab("Associate Variables", associateVariablesPanel);

        importFilePanel.setName("importFilePanel"); // NOI18N

        importButton.setAction(actionMap.get("importAction")); // NOI18N
        importButton.setName("importButton"); // NOI18N

        discrepanciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Discrepancies"));
        discrepanciesPanel.setToolTipText(resourceMap.getString("discrepanciesPanel.toolTipText")); // NOI18N
        discrepanciesPanel.setEnabled(false);
        discrepanciesPanel.setName("discrepanciesPanel"); // NOI18N

        rejectRadioButton.setText(resourceMap.getString("rejectRadioButton.text")); // NOI18N
        rejectRadioButton.setEnabled(false);
        rejectRadioButton.setName("rejectRadioButton"); // NOI18N

        updateRadioButton.setSelected(true);
        updateRadioButton.setText(resourceMap.getString("updateRadioButton.text")); // NOI18N
        updateRadioButton.setEnabled(false);
        updateRadioButton.setName("updateRadioButton"); // NOI18N

        overwriteRadioButton.setText(resourceMap.getString("overwriteRadioButton.text")); // NOI18N
        overwriteRadioButton.setEnabled(false);
        overwriteRadioButton.setName("overwriteRadioButton"); // NOI18N

        javax.swing.GroupLayout discrepanciesPanelLayout = new javax.swing.GroupLayout(discrepanciesPanel);
        discrepanciesPanel.setLayout(discrepanciesPanelLayout);
        discrepanciesPanelLayout.setHorizontalGroup(
                discrepanciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(discrepanciesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(discrepanciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(rejectRadioButton)
                                        .addComponent(updateRadioButton)
                                        .addComponent(overwriteRadioButton))
                                .addContainerGap(12, Short.MAX_VALUE))
        );
        discrepanciesPanelLayout.setVerticalGroup(
                discrepanciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(discrepanciesPanelLayout.createSequentialGroup()
                                .addComponent(rejectRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(updateRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(overwriteRadioButton)
                                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("CanReg data"));
        jPanel7.setName("jPanel7"); // NOI18N

        doChecksCheckBox.setText(resourceMap.getString("doChecksCheckBox.text")); // NOI18N
        doChecksCheckBox.setToolTipText(resourceMap.getString("doChecksCheckBox.toolTipText")); // NOI18N
        doChecksCheckBox.setEnabled(false);
        doChecksCheckBox.setName("doChecksCheckBox"); // NOI18N

        personSearchCheckBox.setSelected(true);
        personSearchCheckBox.setText(resourceMap.getString("personSearchCheckBox.text")); // NOI18N
        personSearchCheckBox.setToolTipText(resourceMap.getString("personSearchCheckBox.toolTipText")); // NOI18N
        personSearchCheckBox.setEnabled(false);
        personSearchCheckBox.setName("personSearchCheckBox"); // NOI18N

        queryNewNameCheckBox.setText(resourceMap.getString("queryNewNameCheckBox.text")); // NOI18N
        queryNewNameCheckBox.setToolTipText(resourceMap.getString("queryNewNameCheckBox.toolTipText")); // NOI18N
        queryNewNameCheckBox.setEnabled(false);
        queryNewNameCheckBox.setName("queryNewNameCheckBox"); // NOI18N

        previousCanRegDataCheckBox.setSelected(true);
        previousCanRegDataCheckBox.setText(resourceMap.getString("previousCanRegDataCheckBox.text")); // NOI18N
        previousCanRegDataCheckBox.setToolTipText(resourceMap.getString("previousCanRegDataCheckBox.toolTipText")); // NOI18N
        previousCanRegDataCheckBox.setName("previousCanRegDataCheckBox"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(doChecksCheckBox)
                                        .addComponent(personSearchCheckBox)
                                        .addComponent(queryNewNameCheckBox)
                                        .addComponent(previousCanRegDataCheckBox))
                                .addContainerGap(94, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(doChecksCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(personSearchCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(queryNewNameCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(previousCanRegDataCheckBox)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        maxLinesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Max Lines"));
        maxLinesPanel.setName("maxLinesPanel"); // NOI18N

        maxLinesTextField.setToolTipText(resourceMap.getString("maxLinesTextField.toolTipText")); // NOI18N
        maxLinesTextField.setName("maxLinesTextField"); // NOI18N
        maxLinesTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                maxLinesTextFieldMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxLinesTextFieldMouseReleased(evt);
            }
        });

        testOnlyCheckBox.setText(resourceMap.getString("testOnlyCheckBox.text")); // NOI18N
        testOnlyCheckBox.setToolTipText(resourceMap.getString("testOnlyCheckBox.toolTipText")); // NOI18N
        testOnlyCheckBox.setName("testOnlyCheckBox"); // NOI18N
        //<ictl.co>
        testOnlyCheckBox.setVisible(false);
        //</ictl.co>

        javax.swing.GroupLayout maxLinesPanelLayout = new javax.swing.GroupLayout(maxLinesPanel);
        maxLinesPanel.setLayout(maxLinesPanelLayout);
        maxLinesPanelLayout.setHorizontalGroup(
                maxLinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, maxLinesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(maxLinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(testOnlyCheckBox)
                                        .addComponent(maxLinesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
                                .addContainerGap())
        );
        maxLinesPanelLayout.setVerticalGroup(
                maxLinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(maxLinesPanelLayout.createSequentialGroup()
                                .addComponent(maxLinesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testOnlyCheckBox)
                                .addContainerGap(54, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout importFilePanelLayout = new javax.swing.GroupLayout(importFilePanel);
        importFilePanel.setLayout(importFilePanelLayout);
        importFilePanelLayout.setHorizontalGroup(
                importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(importFilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(importFilePanelLayout.createSequentialGroup()
                                                .addComponent(discrepanciesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(maxLinesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(importButton, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addContainerGap())
        );
        importFilePanelLayout.setVerticalGroup(
                importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(importFilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(discrepanciesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(maxLinesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(importButton)
                                .addGap(115, 115, 115))
        );

        tabbedPane.addTab("Import File", importFilePanel);

        nextButton.setAction(actionMap.get("jumpToNextTabAction")); // NOI18N
        nextButton.setName("nextButton"); // NOI18N

        cancelButton.setAction(actionMap.get("cancelAction")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        backButton.setAction(actionMap.get("jumpToPreviousTabAction")); // NOI18N
        backButton.setName("backButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(backButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nextButton)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nextButton)
                                        .addComponent(cancelButton)
                                        .addComponent(backButton))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fileNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fileNameTextFieldFocusLost
        changeFile();
    }//GEN-LAST:event_fileNameTextFieldFocusLost

    private void fileNameTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileNameTextFieldMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(fileNameTextField, evt);
    }//GEN-LAST:event_fileNameTextFieldMousePressed

    private void fileNameTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileNameTextFieldMouseReleased
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(fileNameTextField, evt);
    }//GEN-LAST:event_fileNameTextFieldMouseReleased

    private void maxLinesTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxLinesTextFieldMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(maxLinesTextField, evt);
    }//GEN-LAST:event_maxLinesTextFieldMousePressed

    private void maxLinesTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxLinesTextFieldMouseReleased
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(maxLinesTextField, evt);
    }//GEN-LAST:event_maxLinesTextFieldMouseReleased

    /**
     *
     */
    @Action
    public void browseFiles() {

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                //set the file name
                fileNameTextField.setText(chooser.getSelectedFile().getCanonicalPath());
                changeFile();
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     */
    @Action
    public void jumpToNextTabAction() {
        initializeVariableMappingTab();
        int tabNumber = tabbedPane.getSelectedIndex();
        if (tabNumber < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(++tabNumber);
            changeTab(tabNumber);
        }
    }

    /**
     *
     */
    @Action
    public void jumpToPreviousTabAction() {
        initializeVariableMappingTab();
        int tabNumber = tabbedPane.getSelectedIndex();
        if (tabNumber >= 1) {
            tabbedPane.setSelectedIndex(--tabNumber);
            changeTab(tabNumber);
        }
    }

    /**
     *
     */
    @Action
    public void cancelAction() {
        if (importTask != null) {
            if (JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("REALLY_CANCEL?"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("PLEASE_CONFIRM."), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                importTask.cancel(true);
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("IMPORT_OF_FILE_INTERUPTED"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("WARNING"), JOptionPane.WARNING_MESSAGE);
                importTask = null;
                this.dispose();
            }
        } else {
            this.dispose();
        }
    }

    /**
     *
     * @return
     */
    @Action()
    public Task importAction() {
        // TODO: Add a handler for errors in the file structure...
        //<ictl.co>
        int showInternalConfirmDialog = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(),
                java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("PERFORM_BACKUP_NOW?"),
                java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("BACK_UP?"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (showInternalConfirmDialog == JOptionPane.YES_OPTION) {
            Task backupTask = new PerformBackUpActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
            backupTask.execute();
        }

        //<ictl.co>
        localSettings.setProperty("import_path", path);
        localSettings.writeSettings();
        progressBar.setStringPainted(true);
        importButton.setEnabled(false);
        // this.dispose();
        importTask = new ImportActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
        importTask.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                    progressBar.setString(evt.getNewValue().toString() + "%");
                } else if ("finished".equals(evt.getPropertyName())) {
                    dispose();
                }
            }
        });
        return importTask;
    }

    private class ImportActionTask extends org.jdesktop.application.Task<Object, String> {

        ImportActionTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ImportActionTask fields, here.
            super(app);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            boolean success = false;
            try {
                // Calls the client app import action with the file parameters provided,
                success = CanRegClientApp.getApplication().importFile(this, doc, buildMap(), inFile, buildImportOptions());
                //<ictl.co>
            } catch (NullPointerException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
                this.firePropertyChange("error", null, ex.getMessage());
                //</ictl.co>
            } catch (SecurityException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RecordLockedException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
            return success;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            if (!(Boolean) result) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("SOMETHING_WRONG_WITH_FILE") + " " + inFile.getAbsolutePath() + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("FILE_NOT_SUCCESSFULLY_IMPORTED"), JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("SUCCESSFULLY_IMPORTED_FILE") + " " + inFile.getAbsolutePath() + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("SUCCESSFULLY_IMPORTED_FILE"), JOptionPane.INFORMATION_MESSAGE);
            }
            importTask = null;
        }
    }

    private void initializeVariableMappingTab() {
        if (needToRebuildVariableMap && fileNameTextField.getText().trim().length() > 0) {
            BufferedReader br = null;
            List<Relation> map = null;
            panelList = new LinkedList();
            try {
                // Remove all variable mappings
                variablesPanel.removeAll();

                // Read the first line of the file
                br = new BufferedReader(new FileReader(inFile));
                String line = br.readLine();
//<ictl.co>
                if(!StringUtils.isEmpty(line) && (int)line.charAt(0) == 65279){
                    line = line.substring(1);
                }
//</ictl.co>
//                String[] lineElements = canreg.common.Tools.breakDownLine('\t', line);
                String[] lineElements = canreg.common.Tools.breakDownLine(getSeparator(), line);
                // Build variable mapping
                map = Import.constructRelations(doc, lineElements);

                // Add the panels
                for (Relation rel : map) {
                    VariableMappingPanel vmp = new VariableMappingPanel();
                    panelList.add(vmp);
                    vmp.setDBVariables(variablesInDB);
                    vmp.setFileVariableName(rel.getFileVariableName());
                    vmp.setSelectedDBIndex(rel.getDatabaseTableVariableID());
                    variablesPanel.add(vmp);
                    vmp.setVisible(true);
                }

                variablesPanel.revalidate();
                variablesPanel.repaint();

            } catch (RemoteException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("COULD_NOT_OPEN_FILE:_") + "\'" + fileNameTextField.getText().trim() + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                needToRebuildVariableMap = false;
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    private List<Relation> buildMap() {
        List<Relation> map = new LinkedList();
        int i = 0;
        for (VariableMappingPanel vmp : panelList) {
            Relation rel = new Relation();

            DatabaseVariablesListElement dbVLE = vmp.getSelectedDBVariableObject();
            if (dbVLE != null) {
                rel.setDatabaseTableName(dbVLE.getDatabaseTableName());
                rel.setDatabaseTableVariableID(vmp.getDBVariableIndex());
                rel.setDatabaseVariableName(dbVLE.getDatabaseVariableName());
                rel.setFileColumnNumber(i);
                rel.setFileVariableName(vmp.getFileVariableName());
                rel.setVariableType(dbVLE.getVariableType());

                map.add(rel);
            }
            i++;
        }
        return map;
    }

    private ImportOptions buildImportOptions() {
        ImportOptions io = new ImportOptions();

        // Discrepencies
        if (updateRadioButton.isSelected()) {
            io.setDiscrepancies(ImportOptions.UPDATE);
        } else if (rejectRadioButton.isSelected()) {
            io.setDiscrepancies(ImportOptions.REJECT);
        } else if (overwriteRadioButton.isSelected()) {
            io.setDiscrepancies(ImportOptions.OVERWRITE);
        }
        // Max Lines
        if (maxLinesTextField.getText().trim().length() > 0) {
            io.setMaxLines(Integer.parseInt(maxLinesTextField.getText().trim()));
        } else {
            io.setMaxLines(-1);
        }
        io.setTestOnly(testOnlyCheckBox.isSelected());

        // separator
        io.setSeparator(getSeparator());

        // CanReg data
        io.setDoChecks(doChecksCheckBox.isSelected());
        io.setDoPersonSearch(personSearchCheckBox.isSelected());
        io.setQueryNewNames(queryNewNameCheckBox.isSelected());
        io.setDataFromPreviousCanReg(previousCanRegDataCheckBox.isSelected());

        // Set standard variable names
        io.setMultiplePrimaryVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.MultPrimCode.toString()));
        io.setPatientIDTumourTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.PatientIDTumourTable.toString()));
        io.setPatientIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.PatientID.toString()));
        io.setTumourUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.TumourUpdateDate.toString()));
        io.setPatientUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.PatientUpdateDate.toString()));
        io.setTumourIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.TumourID.toString()));
        io.setPatientRecordIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.PatientRecordID.toString()));
        io.setSourceIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName());
        io.setPatientRecordIDTumourTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()));
        io.setObsoletePatientFlagVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString()));
        io.setObsoleteTumourFlagVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString()));
        io.setTumourSequenceVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.MultPrimSeq.toString()));
        io.setFirstNameVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.FirstName.toString()));
        io.setSexVariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.Sex.toString()));
        io.setTumourCheckStatus(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.CheckStatus.toString()));
        io.setTumourRecordStatus(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.TumourRecordStatus.toString()));
        io.setICD10VariableName(globalToolBox.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.ICD10.toString()));

        // Set the characterset
        io.setFileCharset((Charset) charsetsComboBox.getSelectedItem());
//<ictl.co>
        File file = new File(chooser.getCurrentDirectory().getAbsolutePath() + File.separatorChar + "import_report.txt");
        io.setReportFileName(file.getAbsolutePath());
//</ictl.co>
        return io;
    }

    private char getSeparator() {
        String sc = separatingCharacterComboBox.getSelectedItem().toString();
        char schar = ','; // Default
        if (sc.equalsIgnoreCase("Tab")) {
            schar = '\t';
        } else if (sc.equalsIgnoreCase("Comma")) {
            schar = ',';
        } else if (sc.length() > 0) {
            schar = sc.charAt(0);
        }
        return schar;
    }

    /**
     *
     */
    @Action
    public void previewAction() {
        // show the contents of the file
        BufferedReader br = null;
        try {
            changeFile();
            // numberOfRecordsTextField.setText(""+(canreg.common.Tools.numberOfLinesInFile(inFile.getAbsolutePath())-1));
            FileInputStream fis = new FileInputStream(inFile);
            br = new BufferedReader(new InputStreamReader(fis, (Charset) charsetsComboBox.getSelectedItem()));

            CSVReader reader = new CSVReader(br, getSeparator());
            String[] lineElements;

            int linesToRead = Globals.NUMBER_OF_LINES_IN_IMPORT_PREVIEW;
            int numberOfLinesRead = 0;
            String[] headers = {};
            Vector<Vector<String>> data = new Vector<Vector<String>>();
            while ((lineElements = reader.readNext()) != null && (numberOfLinesRead < linesToRead)) {
                if (numberOfLinesRead == 0) {
                    headers = lineElements;
                } else {
                    Vector vec = new Vector(Arrays.asList(lineElements));
                    data.add(vec);
                }
                numberOfLinesRead++;
            }
            numberOfRecordsShownTextField.setText(numberOfLinesRead + "");

            // previewTextArea.setText(headers + "\n" + dataText);
            // previewTextArea.setCaretPosition(0);
            previewPanel.setVisible(true);
            Vector columnNames = new Vector(Arrays.asList(headers));
            previewTable.setModel(new DefaultTableModel(data, columnNames));
        } catch (FileNotFoundException fileNotFoundException) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("COULD_NOT_PREVIEW_FILE:") + " \'" + fileNameTextField.getText().trim() + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, fileNotFoundException);
        } catch (IOException ex) {
            Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     */
    @Action
    public void comboBoxChanged() {
        needToRebuildVariableMap = true;
    }

    /**
     *
     */
    @Action
    public void autodetectFileEncodingAction() throws java.io.IOException {
        String encoding = Tools.detectCharacterCodingOfFile(fileNameTextField.getText());
        if (encoding != null) {
            Charset charset = Charset.forName(encoding);
            charsetsComboBox.setSelectedItem(charset);
            // System.out.println("Detected encoding = " + encoding);
        } else {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("NO_ENCODING_DETECTED."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            // System.out.println("No encoding detected.");
        }
        return;
    }

    //<ictl.co>
    private class PerformBackUpActionTask extends org.jdesktop.application.Task<Object, Void> {

        WaitFrame waitFrame;

        PerformBackUpActionTask(org.jdesktop.application.Application app) {
            super(app);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
            waitFrame = new WaitFrame();
            waitFrame.setLabel(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("PERFORMING_BACKUP..."));
            waitFrame.setIndeterminate(true);
            desktopPane.add(waitFrame, javax.swing.JLayeredPane.POPUP_LAYER);
            waitFrame.setVisible(true);
            waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
        }

        @Override
        protected Object doInBackground() {
            String result = null;
            try {
                result = CanRegClientApp.getApplication().performBackup();
            } catch (RemoteException ex) {
                Logger.getLogger(LoginInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }

        @Override
        protected void succeeded(Object resultObject) {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            waitFrame.dispose();
            String resultString = (String) resultObject;
            if (resultString != null) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("SYSTEM_BACKED_UP_TO_") + resultString + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("BACKUP_PERFORMED."), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    //</ictl.co>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel associateVariablesPanel;
    private javax.swing.JButton autodetectButton;
    private javax.swing.JButton backButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox charsetsComboBox;
    private javax.swing.JPanel chooseFilePanel;
    private javax.swing.JPanel discrepanciesPanel;
    private javax.swing.JCheckBox doChecksCheckBox;
    private javax.swing.JLabel fileEncodingLabel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel importFilePanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel maxLinesPanel;
    private javax.swing.JTextField maxLinesTextField;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel numberOfRecordsLabel;
    private javax.swing.JLabel numberOfRecordsShownLabel;
    private javax.swing.JTextField numberOfRecordsShownTextField;
    private javax.swing.JTextField numberOfRecordsTextField;
    private javax.swing.JRadioButton overwriteRadioButton;
    private javax.swing.JCheckBox personSearchCheckBox;
    private javax.swing.JButton previewButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JTable previewTable;
    private javax.swing.JScrollPane previewTableScrollPane;
    private javax.swing.JCheckBox previousCanRegDataCheckBox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBox queryNewNameCheckBox;
    private javax.swing.JRadioButton rejectRadioButton;
    private javax.swing.JComboBox separatingCharacterComboBox;
    private javax.swing.JLabel separatingCharacterLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox testOnlyCheckBox;
    private javax.swing.JRadioButton updateRadioButton;
    private javax.swing.JPanel variablesPanel;
    private javax.swing.JScrollPane variablesScrollPane;
    // End of variables declaration//GEN-END:variables
}
