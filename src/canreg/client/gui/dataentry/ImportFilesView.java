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
 */

/*
 * ImportView.java
 *
 * Created on 22 February 2008, 09:30
 */
package canreg.client.gui.dataentry;

// import canreg.client.gui.components.VariableMappingPanel;
import canreg.client.LocalSettings;
import canreg.client.CanRegClientApp;
import canreg.client.gui.LoginInternalFrame;
import canreg.client.gui.tools.WaitFrame;
import canreg.common.DatabaseVariablesListElement;
import canreg.client.dataentry.Import;
import canreg.client.dataentry.ImportOptions;
import canreg.client.dataentry.Relation;
import canreg.client.gui.components.PreviewFilePanel;
import canreg.client.gui.components.VariableMappingAlternativePanel;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.server.database.RecordLockedException;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;

/**
 *
 * @author  morten
 * 
 * This module 
 * 
 * TODO: Accept various date formats?
 * Implement the various options
 * 
 * 
 */
public class ImportFilesView extends javax.swing.JInternalFrame implements ActionListener {

    private boolean needToRebuildVariableMap = true;
    private File patientInFile;
    private File tumourInFile;
    private File sourceInFile;
    private Document doc;
    private DatabaseVariablesListElement[] patientVariablesInDB;
    private DatabaseVariablesListElement[] tumourVariablesInDB;
    private DatabaseVariablesListElement[] sourceVariablesInDB;
    private String path;
    private LocalSettings localSettings;
    private GlobalToolBox globalToolBox;
    private Task importTask;
    private JFileChooser chooser;
    private boolean reportFileNameSet = false;
    //<ictl.co>
    JDesktopPane desktopPane;
    //</ictl.co>

    /** Creates new form ImportView */
    public ImportFilesView(/*<ictl.co>*/JDesktopPane desktopPane/*</ictl.co>*/) {
        initComponents();
        // previewPanel.setVisible(false);

        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();

        changeTab(0);

        // Add a listener for changing the active tab
        ChangeListener tabbedPaneChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                initializeVariableMappingTab();
                changeTab(tabbedPane.getSelectedIndex());
            }
        };

        patientPreviewFilePanel.init(this);
        tumourPreviewFilePanel.init(this);
        sourcePreviewFilePanel.init(this);

        // And add the listener to the tabbedPane
        tabbedPane.addChangeListener(tabbedPaneChangeListener);

        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        path = localSettings.getProperty("import_path");

        if (path == null) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(path);
        }

        patientPreviewFilePanel.setChooser(chooser);
        tumourPreviewFilePanel.setChooser(chooser);
        sourcePreviewFilePanel.setChooser(chooser);

        // Group the radiobuttons
        ButtonGroup discrepanciesButtonGroup = new ButtonGroup();
        // Add to the button group
        discrepanciesButtonGroup.add(rejectRadioButton);
        discrepanciesButtonGroup.add(updateRadioButton);
        discrepanciesButtonGroup.add(overwriteRadioButton);

        // Get the system description
        doc = CanRegClientApp.getApplication().getDatabseDescription();
        // variablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE);
        patientVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.PATIENT_TABLE_NAME);
        tumourVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.TUMOUR_TABLE_NAME);
        sourceVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.SOURCE_TABLE_NAME);
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
        chooseFilesTabbedPane = new javax.swing.JTabbedPane();
        patientPreviewFilePanel = new canreg.client.gui.components.PreviewFilePanel();
        tumourPreviewFilePanel = new canreg.client.gui.components.PreviewFilePanel();
        sourcePreviewFilePanel = new canreg.client.gui.components.PreviewFilePanel();
        associateVariavlesTabbedPane = new javax.swing.JTabbedPane();
        patientVariablesAssociationPanel = new canreg.client.gui.components.VariablesAssociationPanel();
        tumourVariablesAssociationPanel = new canreg.client.gui.components.VariablesAssociationPanel();
        sourceVariablesAssociationPanel = new canreg.client.gui.components.VariablesAssociationPanel();
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
        maxLinesPanel = new javax.swing.JPanel();
        maxLinesTextField = new javax.swing.JTextField();
        testOnlyCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        browseButton = new javax.swing.JButton();
        reportFileNameTextField = new javax.swing.JTextField();
        fileLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        overallLabel = new javax.swing.JLabel();
        recordLabel = new javax.swing.JLabel();
        patientsLabel = new javax.swing.JLabel();
        tumoursLabel = new javax.swing.JLabel();
        recordProgressBar = new javax.swing.JProgressBar();
        patientsProgressBar = new javax.swing.JProgressBar();
        tumoursProgressBar = new javax.swing.JProgressBar();
        sourcesProgressBar = new javax.swing.JProgressBar();
        sourcesLabel = new javax.swing.JLabel();
        nextButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(ImportFilesView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        tabbedPane.setName("tabbedPane"); // NOI18N

        chooseFilesTabbedPane.setName("chooseFilesTabbedPane"); // NOI18N

        patientPreviewFilePanel.setName("patientPreviewFilePanel"); // NOI18N
        chooseFilesTabbedPane.addTab(resourceMap.getString("patientPreviewFilePanel.TabConstraints.tabTitle"), patientPreviewFilePanel); // NOI18N

        tumourPreviewFilePanel.setName("tumourPreviewFilePanel"); // NOI18N
        chooseFilesTabbedPane.addTab(resourceMap.getString("tumourPreviewFilePanel.TabConstraints.tabTitle"), tumourPreviewFilePanel); // NOI18N

        sourcePreviewFilePanel.setName("sourcePreviewFilePanel"); // NOI18N
        chooseFilesTabbedPane.addTab(resourceMap.getString("sourcePreviewFilePanel.TabConstraints.tabTitle"), sourcePreviewFilePanel); // NOI18N

        tabbedPane.addTab(resourceMap.getString("chooseFilesTabbedPane.TabConstraints.tabTitle"), chooseFilesTabbedPane); // NOI18N

        associateVariavlesTabbedPane.setName("associateVariavlesTabbedPane"); // NOI18N

        patientVariablesAssociationPanel.setName("patientVariablesAssociationPanel"); // NOI18N
        patientVariablesAssociationPanel.setPreferredSize(new java.awt.Dimension(531, 100));
        associateVariavlesTabbedPane.addTab(resourceMap.getString("patientVariablesAssociationPanel.TabConstraints.tabTitle"), patientVariablesAssociationPanel); // NOI18N

        tumourVariablesAssociationPanel.setName("tumourVariablesAssociationPanel"); // NOI18N
        associateVariavlesTabbedPane.addTab(resourceMap.getString("tumourVariablesAssociationPanel.TabConstraints.tabTitle"), tumourVariablesAssociationPanel); // NOI18N

        sourceVariablesAssociationPanel.setName("sourceVariablesAssociationPanel"); // NOI18N
        associateVariavlesTabbedPane.addTab(resourceMap.getString("sourceVariablesAssociationPanel.TabConstraints.tabTitle"), sourceVariablesAssociationPanel); // NOI18N

        tabbedPane.addTab(resourceMap.getString("associateVariavlesTabbedPane.TabConstraints.tabTitle"), associateVariavlesTabbedPane); // NOI18N

        importFilePanel.setName("importFilePanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(ImportFilesView.class, this);
        importButton.setAction(actionMap.get("importAction")); // NOI18N
        importButton.setName("importButton"); // NOI18N

        discrepanciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Discrepancies"));
        discrepanciesPanel.setToolTipText(resourceMap.getString("discrepanciesPanel.toolTipText")); // NOI18N
        discrepanciesPanel.setName("discrepanciesPanel"); // NOI18N

        rejectRadioButton.setText(resourceMap.getString("rejectRadioButton.text")); // NOI18N
        rejectRadioButton.setName("rejectRadioButton"); // NOI18N

        updateRadioButton.setSelected(true);
        updateRadioButton.setText(resourceMap.getString("updateRadioButton.text")); // NOI18N
        updateRadioButton.setName("updateRadioButton"); // NOI18N

        overwriteRadioButton.setText(resourceMap.getString("overwriteRadioButton.text")); // NOI18N
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
                .addComponent(overwriteRadioButton))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("CanReg data"));
        jPanel7.setName("jPanel7"); // NOI18N

        doChecksCheckBox.setSelected(true);
        doChecksCheckBox.setText(resourceMap.getString("doChecksCheckBox.text")); // NOI18N
        doChecksCheckBox.setToolTipText(resourceMap.getString("doChecksCheckBox.toolTipText")); // NOI18N
        doChecksCheckBox.setName("doChecksCheckBox"); // NOI18N

        personSearchCheckBox.setSelected(true);
        personSearchCheckBox.setText(resourceMap.getString("personSearchCheckBox.text")); // NOI18N
        personSearchCheckBox.setToolTipText(resourceMap.getString("personSearchCheckBox.toolTipText")); // NOI18N
        personSearchCheckBox.setEnabled(false);
        personSearchCheckBox.setName("personSearchCheckBox"); // NOI18N

        queryNewNameCheckBox.setSelected(true);
        queryNewNameCheckBox.setText(resourceMap.getString("queryNewNameCheckBox.text")); // NOI18N
        queryNewNameCheckBox.setToolTipText(resourceMap.getString("queryNewNameCheckBox.toolTipText")); // NOI18N
        queryNewNameCheckBox.setEnabled(false);
        queryNewNameCheckBox.setName("queryNewNameCheckBox"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(doChecksCheckBox)
                    .addComponent(personSearchCheckBox)
                    .addComponent(queryNewNameCheckBox))
                .addContainerGap(143, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(doChecksCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(personSearchCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryNewNameCheckBox)
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
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        browseButton.setAction(actionMap.get("browseFiles")); // NOI18N
        browseButton.setName("browseButton"); // NOI18N

        reportFileNameTextField.setName("reportFileNameTextField"); // NOI18N
        reportFileNameTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                reportFileNameTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                reportFileNameTextFieldMouseReleased(evt);
            }
        });
        reportFileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                reportFileNameTextFieldFocusLost(evt);
            }
        });

        fileLabel.setText(resourceMap.getString("fileLabel.text")); // NOI18N
        fileLabel.setName("fileLabel"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(fileLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reportFileNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(reportFileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        overallLabel.setText(resourceMap.getString("overallLabel.text")); // NOI18N
        overallLabel.setName("overallLabel"); // NOI18N

        recordLabel.setText(resourceMap.getString("recordLabel.text")); // NOI18N
        recordLabel.setName("recordLabel"); // NOI18N

        patientsLabel.setText(resourceMap.getString("patientsLabel.text")); // NOI18N
        patientsLabel.setName("patientsLabel"); // NOI18N

        tumoursLabel.setText(resourceMap.getString("tumoursLabel.text")); // NOI18N
        tumoursLabel.setName("tumoursLabel"); // NOI18N

        recordProgressBar.setName("recordProgressBar"); // NOI18N

        patientsProgressBar.setName("patientsProgressBar"); // NOI18N

        tumoursProgressBar.setName("tumoursProgressBar"); // NOI18N

        sourcesProgressBar.setName("sourcesProgressBar"); // NOI18N

        sourcesLabel.setText(resourceMap.getString("sourcesLabel.text")); // NOI18N
        sourcesLabel.setName("sourcesLabel"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(patientsLabel)
                    .addComponent(tumoursLabel)
                    .addComponent(recordLabel)
                    .addComponent(sourcesLabel)
                    .addComponent(overallLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourcesProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .addComponent(tumoursProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .addComponent(patientsProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .addComponent(recordProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recordLabel)
                    .addComponent(recordProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(patientsLabel)
                    .addComponent(patientsProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tumoursProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tumoursLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourcesProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourcesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(overallLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout importFilePanelLayout = new javax.swing.GroupLayout(importFilePanel);
        importFilePanel.setLayout(importFilePanelLayout);
        importFilePanelLayout.setHorizontalGroup(
            importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, importFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(importFilePanelLayout.createSequentialGroup()
                        .addComponent(discrepanciesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxLinesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importButton)))
                .addContainerGap())
        );
        importFilePanelLayout.setVerticalGroup(
            importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(importFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(importFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(importButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxLinesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(discrepanciesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
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
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextButton)
                    .addComponent(cancelButton)
                    .addComponent(backButton))
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(resourceMap.getString("Form.AccessibleContext.accessibleName")); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void reportFileNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_reportFileNameTextFieldFocusLost
        reportFileNameSet = true;
}//GEN-LAST:event_reportFileNameTextFieldFocusLost

    private void maxLinesTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxLinesTextFieldMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(maxLinesTextField, evt);
    }//GEN-LAST:event_maxLinesTextFieldMousePressed

    private void maxLinesTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxLinesTextFieldMouseReleased
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(maxLinesTextField, evt);
    }//GEN-LAST:event_maxLinesTextFieldMouseReleased

    private void reportFileNameTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportFileNameTextFieldMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(reportFileNameTextField, evt);
    }//GEN-LAST:event_reportFileNameTextFieldMousePressed

    private void reportFileNameTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportFileNameTextFieldMouseReleased
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(reportFileNameTextField, evt);
    }//GEN-LAST:event_reportFileNameTextFieldMouseReleased

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
            if (JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("REALLY_CANCEL?"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("PLEASE_CONFIRM"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                importTask.cancel(true);
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("IMPORT_OF_FILE_INTERUPTED"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("WARNING"), JOptionPane.WARNING_MESSAGE);
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
        //</ictl.co>
        if (overwriteRadioButton.isSelected()) {
            // if the overwrite is selected, warn the user.
            int result = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(),
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("THIS_MIGHT_DROP_DATA_IN_THE_DATABASE._REALLY_GO_AHEAD_WITH_OVERWRITE?"),
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("RELLY_OVERWRITE?"),
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return null;
            }
        }
        // see if the report file already exists...
        File file = new File(reportFileNameTextField.getText());
        if (file.exists()) {
            int result = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(),
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("THE_REPORT_FILE") + file.getAbsolutePath() + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("ALREADY_EXISTS.") + " \n" + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("DO_YOU_WANT_TO_OVERWRITE_IT?"),
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("REPORT_FILE_EXISTS._OVERWRITE?"),
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return null;
            }
        }

        try {
            localSettings.setProperty("import_path", chooser.getCurrentDirectory().getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
        }

        localSettings.writeSettings();
        progressBar.setStringPainted(true);
        importButton.setEnabled(false);
        // this.dispose();

        importTask = new ImportActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
        importTask.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (Import.PROGRESS.equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                    progressBar.setString(evt.getNewValue().toString() + "%");
                } else if (Import.FINISHED.equals(evt.getPropertyName())) {
                    // dispose();
                } else if (Import.RECORD.equals(evt.getPropertyName())) {
                    recordProgressBar.setValue((Integer) evt.getNewValue());
                    recordProgressBar.setString(evt.getNewValue().toString() + "%");
                } else if (Import.PATIENTS.equals(evt.getPropertyName())) {
                    patientsProgressBar.setValue((Integer) evt.getNewValue());
                    patientsProgressBar.setString(evt.getNewValue().toString() + "%");
                } else if (Import.TUMOURS.equals(evt.getPropertyName())) {
                    tumoursProgressBar.setValue((Integer) evt.getNewValue());
                    tumoursProgressBar.setString(evt.getNewValue().toString() + "%");
                } else if (Import.SOURCES.equals(evt.getPropertyName())) {
                    sourcesProgressBar.setValue((Integer) evt.getNewValue());
                    sourcesProgressBar.setString(evt.getNewValue().toString() + "%");
                }
            }
        });
        return importTask;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(PreviewFilePanel.FILE_CHANGED_ACTION)) {
            needToRebuildVariableMap = true;
            if (!reportFileNameSet) {
                File file = new File(chooser.getCurrentDirectory().getAbsolutePath() + File.separatorChar + "import_report.txt");
                // chooser.setSelectedFile(file);
                reportFileNameTextField.setText(file.getAbsolutePath());
            }
        }
    }

    private class ImportActionTask extends org.jdesktop.application.Task<Object, Void> {

        private final List<Relation> variablesMap;
        private final File[] files;
        private final ImportOptions io;

        ImportActionTask(org.jdesktop.application.Application app) {
            super(app);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
            variablesMap = buildMap();

            files = new File[]{
                patientPreviewFilePanel.getInFile(),
                tumourPreviewFilePanel.getInFile(),
                sourcePreviewFilePanel.getInFile()
            };

            io = buildImportOptions();
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            boolean success = false;
            try {
                // Calls the client app import action with the file parameters provided,
                success = CanRegClientApp.getApplication().importFiles(this, doc, variablesMap, files, io);
            } catch (SecurityException ex) {
                Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RecordLockedException ex) {
                Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
            return success;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            String fileListString = "";
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null) {
                    fileListString += files[i].getName() + ", ";
                }
            }
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            if (!(Boolean) result) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("SOMETHING_WRONG_WITH_THE_FILE(S)_") + fileListString + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("FILE(S)_NOT_SUCCESSFULLY_IMPORTED"), JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("SUCCESSFULLY_IMPORTED_FILE(S)_") + fileListString + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("FILE(S)_SUCCESSFULLY_IMPORTED"), JOptionPane.INFORMATION_MESSAGE);
            }
            importTask = null;
            dispose();
        }
    }

    private void initializeVariableMappingTab() {
        BufferedReader br = null;
        try {
            // patient mapping
            patientInFile = patientPreviewFilePanel.getInFile();
            if (needToRebuildVariableMap && patientInFile != null) {
                br = new BufferedReader(new FileReader(patientInFile));
                String line = br.readLine();
                String[] lineElements = canreg.common.Tools.breakDownLine(patientPreviewFilePanel.getSeparator(), line);
                List<Relation> map = Import.constructRelations(doc, lineElements);
                patientVariablesAssociationPanel.initializeVariableMappingPanel(map, patientVariablesInDB, lineElements);
            }
            // tumour mapping
            tumourInFile = tumourPreviewFilePanel.getInFile();
            if (needToRebuildVariableMap && tumourInFile != null) {
                br = new BufferedReader(new FileReader(tumourInFile));
                String line = br.readLine();
                String[] lineElements = canreg.common.Tools.breakDownLine(tumourPreviewFilePanel.getSeparator(), line);
                List<Relation> map = Import.constructRelations(doc, lineElements);
                tumourVariablesAssociationPanel.initializeVariableMappingPanel(map, tumourVariablesInDB, lineElements);
            }
            // source mapping
            sourceInFile = sourcePreviewFilePanel.getInFile();
            if (needToRebuildVariableMap && sourceInFile != null) {
                br = new BufferedReader(new FileReader(sourceInFile));
                String line = br.readLine();
                String[] lineElements = canreg.common.Tools.breakDownLine(sourcePreviewFilePanel.getSeparator(), line);
                List<Relation> map = Import.constructRelations(doc, lineElements);
                sourceVariablesAssociationPanel.initializeVariableMappingPanel(map, sourceVariablesInDB, lineElements);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Could not open file: \'" + patientInFile.getPath() + "\'.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            needToRebuildVariableMap = false;
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ImportFilesView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private List<Relation> buildMap() {
        List<Relation> map = new LinkedList();
        if (patientVariablesAssociationPanel.getPanelList() != null) {
            for (VariableMappingAlternativePanel vmp : patientVariablesAssociationPanel.getPanelList()) {
                Relation rel = new Relation();
                int fileColumnNumber = vmp.getSelectedFileColumnNumber();
                if (fileColumnNumber >= 0) {
                    DatabaseVariablesListElement dbVLE = vmp.getDatabaseVariablesListElement();
                    rel.setDatabaseTableName(dbVLE.getDatabaseTableName());
                    rel.setDatabaseTableVariableID(vmp.getDBVariableIndex());
                    rel.setDatabaseVariableName(dbVLE.getDatabaseVariableName());
                    rel.setFileColumnNumber(fileColumnNumber);
                    rel.setFileVariableName(vmp.getSelectedFileElement());
                    rel.setVariableType(dbVLE.getVariableType());
                    map.add(rel);
                }
            }
        }
        if (tumourVariablesAssociationPanel.getPanelList() != null) {
            for (VariableMappingAlternativePanel vmp : tumourVariablesAssociationPanel.getPanelList()) {
                Relation rel = new Relation();
                int fileColumnNumber = vmp.getSelectedFileColumnNumber();
                if (fileColumnNumber >= 0) {
                    DatabaseVariablesListElement dbVLE = vmp.getDatabaseVariablesListElement();
                    rel.setDatabaseTableName(dbVLE.getDatabaseTableName());
                    rel.setDatabaseTableVariableID(vmp.getDBVariableIndex());
                    rel.setDatabaseVariableName(dbVLE.getDatabaseVariableName());
                    rel.setFileColumnNumber(fileColumnNumber);
                    rel.setFileVariableName(vmp.getSelectedFileElement());
                    rel.setVariableType(dbVLE.getVariableType());
                    map.add(rel);
                }
            }
        }
        if (sourceVariablesAssociationPanel.getPanelList() != null) {
            for (VariableMappingAlternativePanel vmp : sourceVariablesAssociationPanel.getPanelList()) {
                Relation rel = new Relation();
                int fileColumnNumber = vmp.getSelectedFileColumnNumber();
                if (fileColumnNumber >= 0) {
                    DatabaseVariablesListElement dbVLE = vmp.getDatabaseVariablesListElement();
                    rel.setDatabaseTableName(dbVLE.getDatabaseTableName());
                    rel.setDatabaseTableVariableID(vmp.getDBVariableIndex());
                    rel.setDatabaseVariableName(dbVLE.getDatabaseVariableName());
                    rel.setFileColumnNumber(fileColumnNumber);
                    rel.setFileVariableName(vmp.getSelectedFileElement());
                    rel.setVariableType(dbVLE.getVariableType());
                    map.add(rel);
                }
            }
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

        // separators
        io.setSeparators(new char[]{
                    patientPreviewFilePanel.getSeparator(),
                    tumourPreviewFilePanel.getSeparator(),
                    sourcePreviewFilePanel.getSeparator()
                });

        // CanReg data
        io.setDoChecks(doChecksCheckBox.isSelected());
        io.setDoPersonSearch(personSearchCheckBox.isSelected());
        io.setQueryNewNames(queryNewNameCheckBox.isSelected());
        io.setDataFromPreviousCanReg(false);

        // Set standard variable names
        io.setMultiplePrimaryVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimCode.toString()).getDatabaseVariableName());
        io.setPatientIDTumourTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName());
        io.setPatientIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
        io.setTumourUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName());
        io.setPatientUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName());
        io.setTumourIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
        io.setPatientRecordIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
        io.setPatientRecordIDTumourTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName());
        io.setTumourIDSourceTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName());
        io.setSourceIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName());
        io.setObsoletePatientFlagVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString()).getDatabaseVariableName());
        io.setObsoleteTumourFlagVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString()).getDatabaseVariableName());
        io.setTumourSequenceVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimSeq.toString()).getDatabaseVariableName());
        io.setTumourCheckStatus(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.CheckStatus.toString()).getDatabaseVariableName());
        io.setTumourRecordStatus(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString()).getDatabaseVariableName());
        io.setICD10VariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICD10.toString()).getDatabaseVariableName());

        // Set the charactersets
        io.setFilesCharsets(getCharsets());

        // Set the report fileName
        io.setReportFileName(reportFileNameTextField.getText());

        return io;
    }

    private Charset[] getCharsets() {
        // get one from each table
        return new Charset[]{
                    patientPreviewFilePanel.getCharacterSet(),
                    tumourPreviewFilePanel.getCharacterSet(),
                    sourcePreviewFilePanel.getCharacterSet()
                };
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
    public void autodetectSeparatingCharacterAction() {
        JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("NOT_YET_IMPLEMENTED."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportFilesView").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     *
     */
    @Action
    public void browseFiles() {
        if (!reportFileNameSet) {
            File file = new File(chooser.getCurrentDirectory().getAbsolutePath() + File.separatorChar + "import_report.txt");
            chooser.setSelectedFile(file);
        } else {
            File file = new File(reportFileNameTextField.getText());
            chooser.setSelectedFile(file);
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                //set the file name
                reportFileNameTextField.setText(chooser.getSelectedFile().getCanonicalPath());
                // changeFile();
            } catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
            }
            reportFileNameSet = true;
        }
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
    private javax.swing.JTabbedPane associateVariavlesTabbedPane;
    private javax.swing.JButton backButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTabbedPane chooseFilesTabbedPane;
    private javax.swing.JPanel discrepanciesPanel;
    private javax.swing.JCheckBox doChecksCheckBox;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel importFilePanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel maxLinesPanel;
    private javax.swing.JTextField maxLinesTextField;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel overallLabel;
    private javax.swing.JRadioButton overwriteRadioButton;
    private canreg.client.gui.components.PreviewFilePanel patientPreviewFilePanel;
    private canreg.client.gui.components.VariablesAssociationPanel patientVariablesAssociationPanel;
    private javax.swing.JLabel patientsLabel;
    private javax.swing.JProgressBar patientsProgressBar;
    private javax.swing.JCheckBox personSearchCheckBox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBox queryNewNameCheckBox;
    private javax.swing.JLabel recordLabel;
    private javax.swing.JProgressBar recordProgressBar;
    private javax.swing.JRadioButton rejectRadioButton;
    private javax.swing.JTextField reportFileNameTextField;
    private canreg.client.gui.components.PreviewFilePanel sourcePreviewFilePanel;
    private canreg.client.gui.components.VariablesAssociationPanel sourceVariablesAssociationPanel;
    private javax.swing.JLabel sourcesLabel;
    private javax.swing.JProgressBar sourcesProgressBar;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox testOnlyCheckBox;
    private canreg.client.gui.components.PreviewFilePanel tumourPreviewFilePanel;
    private canreg.client.gui.components.VariablesAssociationPanel tumourVariablesAssociationPanel;
    private javax.swing.JLabel tumoursLabel;
    private javax.swing.JProgressBar tumoursProgressBar;
    private javax.swing.JRadioButton updateRadioButton;
    // End of variables declaration//GEN-END:variables
}
