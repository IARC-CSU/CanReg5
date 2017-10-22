/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017 International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.client.gui;

import canreg.client.*;
import canreg.client.gui.analysis.ExportReportInternalFrame;
import canreg.client.gui.analysis.FrequenciesByYearInternalFrame;
import canreg.client.gui.analysis.TableBuilderInternalFrame;
import canreg.client.gui.dataentry.BrowseInternalFrame;
import canreg.client.gui.dataentry.EditDictionaryInternalFrame;
import canreg.client.gui.dataentry.ImportFilesView;
import canreg.client.gui.dataentry.ImportView;
import canreg.client.gui.dataentry.PDSChooserInternalFrame;
import canreg.client.gui.dataentry.RecordEditor;
import canreg.client.gui.management.BackUpInternalFrame;
import canreg.client.gui.management.CanReg4PDSImporterInternalFrame;
import canreg.client.gui.management.CanReg4SystemConverterInternalFrame;
import canreg.client.gui.management.FirstNameSexInternalFrame;
import canreg.client.gui.management.InstallNewSystemInternalFrame;
import canreg.client.gui.management.OptionsFrame;
import canreg.client.gui.management.PersonSearchFrame;
import canreg.client.gui.management.RestoreInternalFrame;
import canreg.client.gui.management.UserManagerInternalFrame;
import canreg.client.gui.management.systemeditor.ModifyDatabaseStructureInternalFrame;
import canreg.client.gui.management.CanReg4MigrationInternalFrame;
import canreg.client.gui.tools.StandardDialog;
import canreg.client.gui.tools.WaitFrame;
import canreg.client.management.DatabaseGarbler;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * The application's main frame.
 */
public final class CanRegClientView extends FrameView {

    /**
     *
     * @param app
     */
    public CanRegClientView(SingleFrameApplication app) {
        super(app);

        initComponents();

        ResourceMap resourceMap = getResourceMap();

        // Set the main programs icon
        java.net.URL imageURL = CanRegClientView.class.getResource(resourceMap.getString("icon", String.class));
        if (imageURL != null) {
            java.awt.Window.getWindows()[0].setIconImage(new ImageIcon(imageURL).getImage());
        }

        setUserRightsLevel(userRightsLevel);

        applyPreferences();

        // status bar initialization - message timeout, idle icon and busy animation, etc  
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        this.getFrame().pack();
        // Show the welcome frame...
        if (!CanRegClientApp.getApplication().isLoggedIn()) {
            showWelcomeFrame();
        }
    }

    /**
     *
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = CanRegClientApp.getApplication().getMainFrame();
            aboutBox = new CanRegClientAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        CanRegClientApp.getApplication().show(aboutBox);
    }

    /**
     *
     */
    public void applyPreferences() {
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        // Apply the outline drag mode
        if (localSettings.isOutlineDragMode()) {
            desktopPane.setDragMode(javax.swing.JDesktopPane.OUTLINE_DRAG_MODE);
        } else {
            desktopPane.setDragMode(javax.swing.JDesktopPane.LIVE_DRAG_MODE);
        }
        // Apply the settings to main program
        CanRegClientApp.getApplication().applyPreferences();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        desktopPane = new javax.swing.JDesktopPane();
        toolBar = new javax.swing.JToolBar();
        browseEditButton = new javax.swing.JButton();
        createNewRecordButton = new javax.swing.JButton();
        jSeparator14 = new javax.swing.JToolBar.Separator();
        tableBuilderButton = new javax.swing.JButton();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        optionsButton = new javax.swing.JButton();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        handbookButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        logInMenuItem = new javax.swing.JMenuItem();
        logOutMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        viewWorkFilesMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        dataEntryMenu = new javax.swing.JMenu();
        browseEditMenuItem = new javax.swing.JMenuItem();
        createNewRecordMenuItem = new javax.swing.JMenuItem();
        editDictionaryMenuItem = new javax.swing.JMenuItem();
        editPDSMenuItem = new javax.swing.JMenuItem();
        importDataMenuItem = new javax.swing.JMenuItem();
        analysisMenu = new javax.swing.JMenu();
        frequenciesMenuItem = new javax.swing.JMenuItem();
        incidenceTablesMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        exportDataReportsMenuItem = new javax.swing.JMenuItem();
        managementMenu = new javax.swing.JMenu();
        backupMenuItem = new javax.swing.JMenuItem();
        restoreMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        qualityControlMenu = new javax.swing.JMenu();
        nameSexMenuItem = new javax.swing.JMenuItem();
        duplicateSearchMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        usersMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        advancedMenu = new javax.swing.JMenu();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        optionsMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        migrateMenu = new javax.swing.JMenu();
        convertCR4SystDefMenuItem = new javax.swing.JMenuItem();
        importFromCR4MenuItem = new javax.swing.JMenuItem();
        migratePopulationSetMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        canreg4migrationMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        databaseStructureMenu = new javax.swing.JMenu();
        setUpDatabaseMenuItem = new javax.swing.JMenuItem();
        installRPackagesMenuItem = new javax.swing.JMenuItem();
        garbleDatabaseMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        linksMenu = new javax.swing.JMenu();
        iacrWebsiteMenuItem = new javax.swing.JMenuItem();
        encrWebsiteMenuItem = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        icdo3DocumentationWebsiteMenuItem = new javax.swing.JMenuItem();
        latestNewsMenuItem = new javax.swing.JMenuItem();
        changelogMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        userLevelLabel = new javax.swing.JLabel();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        desktopPane.setAutoscrolls(true);
        desktopPane.setDoubleBuffered(true);
        desktopPane.setMinimumSize(new java.awt.Dimension(400, 300));
        desktopPane.setName("desktopPane"); // NOI18N

        toolBar.setRollover(true);
        toolBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        toolBar.setName("Tools"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(CanRegClientView.class, this);
        browseEditButton.setAction(actionMap.get("browseEditAction")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(CanRegClientView.class);
        browseEditButton.setText(resourceMap.getString("browseEditButton.text")); // NOI18N
        browseEditButton.setToolTipText(resourceMap.getString("browseEditButton.toolTipText")); // NOI18N
        browseEditButton.setFocusable(false);
        browseEditButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        browseEditButton.setName("browseEditButton"); // NOI18N
        browseEditButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(browseEditButton);

        createNewRecordButton.setAction(actionMap.get("createNewRecordSetAction")); // NOI18N
        createNewRecordButton.setToolTipText(resourceMap.getString("createNewRecordButton.toolTipText")); // NOI18N
        createNewRecordButton.setFocusable(false);
        createNewRecordButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        createNewRecordButton.setName("createNewRecordButton"); // NOI18N
        createNewRecordButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(createNewRecordButton);

        jSeparator14.setName("jSeparator14"); // NOI18N
        toolBar.add(jSeparator14);

        tableBuilderButton.setAction(actionMap.get("showTableBuilder")); // NOI18N
        tableBuilderButton.setToolTipText(resourceMap.getString("tableBuilderButton.toolTipText")); // NOI18N
        tableBuilderButton.setFocusable(false);
        tableBuilderButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tableBuilderButton.setName("tableBuilderButton"); // NOI18N
        tableBuilderButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(tableBuilderButton);

        jSeparator13.setName("jSeparator13"); // NOI18N
        toolBar.add(jSeparator13);

        optionsButton.setAction(actionMap.get("showOptionFrame")); // NOI18N
        optionsButton.setToolTipText(resourceMap.getString("optionsButton.toolTipText")); // NOI18N
        optionsButton.setFocusable(false);
        optionsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        optionsButton.setName("optionsButton"); // NOI18N
        optionsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(optionsButton);

        jSeparator15.setName("jSeparator15"); // NOI18N
        toolBar.add(jSeparator15);

        handbookButton.setAction(actionMap.get("openCanReg5Instructions")); // NOI18N
        handbookButton.setFocusable(false);
        handbookButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        handbookButton.setName("handbookButton"); // NOI18N
        handbookButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(handbookButton);

        desktopPane.add(toolBar);
        toolBar.setBounds(0, 0, 800, 43);

        jScrollPane1.setViewportView(desktopPane);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 797, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        logInMenuItem.setAction(actionMap.get("showLoginFrame")); // NOI18N
        logInMenuItem.setToolTipText(resourceMap.getString("logInMenuItem.toolTipText")); // NOI18N
        logInMenuItem.setName("logInMenuItem"); // NOI18N
        fileMenu.add(logInMenuItem);

        logOutMenuItem.setAction(actionMap.get("logOutaction")); // NOI18N
        logOutMenuItem.setToolTipText(resourceMap.getString("logOutMenuItem.toolTipText")); // NOI18N
        logOutMenuItem.setName("logOutMenuItem"); // NOI18N
        fileMenu.add(logOutMenuItem);

        jSeparator11.setName("jSeparator11"); // NOI18N
        fileMenu.add(jSeparator11);

        viewWorkFilesMenuItem.setAction(actionMap.get("viewWorkFiles")); // NOI18N
        viewWorkFilesMenuItem.setText(resourceMap.getString("viewWorkFilesMenuItem.text")); // NOI18N
        viewWorkFilesMenuItem.setToolTipText(resourceMap.getString("viewWorkFilesMenuItem.toolTipText")); // NOI18N
        viewWorkFilesMenuItem.setName("viewWorkFilesMenuItem"); // NOI18N
        fileMenu.add(viewWorkFilesMenuItem);

        jSeparator3.setName("jSeparator3"); // NOI18N
        fileMenu.add(jSeparator3);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        dataEntryMenu.setText(resourceMap.getString("dataEntryMenu.text")); // NOI18N
        dataEntryMenu.setName("dataEntryMenu"); // NOI18N

        browseEditMenuItem.setAction(actionMap.get("browseEditAction")); // NOI18N
        browseEditMenuItem.setText(resourceMap.getString("browseMenuItem.text")); // NOI18N
        browseEditMenuItem.setToolTipText(resourceMap.getString("browseMenuItem.toolTipText")); // NOI18N
        browseEditMenuItem.setName("browseMenuItem"); // NOI18N
        dataEntryMenu.add(browseEditMenuItem);

        createNewRecordMenuItem.setAction(actionMap.get("createNewRecordSetAction")); // NOI18N
        createNewRecordMenuItem.setName("createNewRecordMenuItem"); // NOI18N
        dataEntryMenu.add(createNewRecordMenuItem);

        editDictionaryMenuItem.setAction(actionMap.get("editDictionaryAction")); // NOI18N
        editDictionaryMenuItem.setText(resourceMap.getString("dictionaryMenuItem.text")); // NOI18N
        editDictionaryMenuItem.setToolTipText(resourceMap.getString("dictionaryMenuItem.toolTipText")); // NOI18N
        editDictionaryMenuItem.setName("dictionaryMenuItem"); // NOI18N
        dataEntryMenu.add(editDictionaryMenuItem);

        editPDSMenuItem.setAction(actionMap.get("editPopulationDataSets")); // NOI18N
        editPDSMenuItem.setToolTipText(resourceMap.getString("editPDSMenuItem.toolTipText")); // NOI18N
        editPDSMenuItem.setName("editPDSMenuItem"); // NOI18N
        dataEntryMenu.add(editPDSMenuItem);

        importDataMenuItem.setAction(actionMap.get("importData")); // NOI18N
        importDataMenuItem.setToolTipText(resourceMap.getString("importMenuItem.toolTipText")); // NOI18N
        importDataMenuItem.setName("importMenuItem"); // NOI18N
        dataEntryMenu.add(importDataMenuItem);

        menuBar.add(dataEntryMenu);

        analysisMenu.setText(resourceMap.getString("analysisMenu.text")); // NOI18N
        analysisMenu.setName("analysisMenu"); // NOI18N

        frequenciesMenuItem.setAction(actionMap.get("showFrequenciesFrame")); // NOI18N
        frequenciesMenuItem.setName("frequencyDistributionsMenuItem"); // NOI18N
        analysisMenu.add(frequenciesMenuItem);

        incidenceTablesMenuItem.setAction(actionMap.get("showTableBuilder")); // NOI18N
        incidenceTablesMenuItem.setText(resourceMap.getString("incidenceTablesMenuItem.text")); // NOI18N
        incidenceTablesMenuItem.setToolTipText(resourceMap.getString("incidenceTablesMenuItem.toolTipText")); // NOI18N
        incidenceTablesMenuItem.setName("incidenceTablesMenuItem"); // NOI18N
        analysisMenu.add(incidenceTablesMenuItem);

        jSeparator8.setName("jSeparator8"); // NOI18N
        analysisMenu.add(jSeparator8);

        exportDataReportsMenuItem.setAction(actionMap.get("showExportFrame")); // NOI18N
        exportDataReportsMenuItem.setText(resourceMap.getString("exportReportsMenuItem.text")); // NOI18N
        exportDataReportsMenuItem.setName("exportReportsMenuItem"); // NOI18N
        analysisMenu.add(exportDataReportsMenuItem);

        menuBar.add(analysisMenu);

        managementMenu.setAction(actionMap.get("restoreAction")); // NOI18N
        managementMenu.setText(resourceMap.getString("managementMenu.text")); // NOI18N
        managementMenu.setName("managementMenu"); // NOI18N

        backupMenuItem.setAction(actionMap.get("backupAction")); // NOI18N
        backupMenuItem.setText(resourceMap.getString("backupMenuItem.text")); // NOI18N
        backupMenuItem.setName("backupMenuItem"); // NOI18N
        managementMenu.add(backupMenuItem);

        restoreMenuItem.setAction(actionMap.get("restoreAction")); // NOI18N
        restoreMenuItem.setText(resourceMap.getString("restoreMenuItem.text")); // NOI18N
        restoreMenuItem.setName("restoreMenuItem"); // NOI18N
        managementMenu.add(restoreMenuItem);

        jSeparator7.setName("jSeparator7"); // NOI18N
        managementMenu.add(jSeparator7);

        qualityControlMenu.setText(resourceMap.getString("qualityControlMenu.text")); // NOI18N
        qualityControlMenu.setName("qualityControlMenu"); // NOI18N

        nameSexMenuItem.setAction(actionMap.get("showNameSexAction")); // NOI18N
        nameSexMenuItem.setText(resourceMap.getString("nameSexMenuItem.text")); // NOI18N
        nameSexMenuItem.setName("nameSexMenuItem"); // NOI18N
        qualityControlMenu.add(nameSexMenuItem);

        duplicateSearchMenuItem.setAction(actionMap.get("duplicateSearchAction")); // NOI18N
        duplicateSearchMenuItem.setText(resourceMap.getString("duplicateSearchMenuItem.text")); // NOI18N
        duplicateSearchMenuItem.setName("duplicateSearchMenuItem"); // NOI18N
        qualityControlMenu.add(duplicateSearchMenuItem);

        managementMenu.add(qualityControlMenu);

        jSeparator6.setName("jSeparator6"); // NOI18N
        managementMenu.add(jSeparator6);

        usersMenuItem.setAction(actionMap.get("showUserManagement")); // NOI18N
        usersMenuItem.setText(resourceMap.getString("usersMenuItem.text")); // NOI18N
        usersMenuItem.setName("usersMenuItem"); // NOI18N
        managementMenu.add(usersMenuItem);

        jSeparator10.setName("jSeparator10"); // NOI18N
        managementMenu.add(jSeparator10);

        advancedMenu.setText(resourceMap.getString("advancedMenu.text")); // NOI18N
        advancedMenu.setName("advancedMenu"); // NOI18N

        jMenuItem18.setAction(actionMap.get("showUsersLoggedIn")); // NOI18N
        jMenuItem18.setName("showUsersLoggedInMenuItem"); // NOI18N
        advancedMenu.add(jMenuItem18);

        jMenuItem16.setAction(actionMap.get("startDatabaseServer")); // NOI18N
        jMenuItem16.setText(resourceMap.getString("startDBMenuItem.text")); // NOI18N
        jMenuItem16.setName("startDBMenuItem"); // NOI18N
        advancedMenu.add(jMenuItem16);

        jMenuItem17.setAction(actionMap.get("stopDatabaseServer")); // NOI18N
        jMenuItem17.setText(resourceMap.getString("stopDBMenuItem.text")); // NOI18N
        jMenuItem17.setName("stopDBMenuItem"); // NOI18N
        advancedMenu.add(jMenuItem17);

        managementMenu.add(advancedMenu);

        jSeparator5.setName("jSeparator5"); // NOI18N
        managementMenu.add(jSeparator5);

        optionsMenuItem.setAction(actionMap.get("showOptionFrame")); // NOI18N
        optionsMenuItem.setToolTipText(resourceMap.getString("optionsMenuItem.toolTipText")); // NOI18N
        optionsMenuItem.setName("optionsMenuItem"); // NOI18N
        managementMenu.add(optionsMenuItem);

        menuBar.add(managementMenu);

        toolsMenu.setText(resourceMap.getString("toolsMenu.text")); // NOI18N
        toolsMenu.setName("toolsMenu"); // NOI18N

        migrateMenu.setText(resourceMap.getString("migrateMenu.text")); // NOI18N
        migrateMenu.setName("migrateMenu"); // NOI18N

        convertCR4SystDefMenuItem.setAction(actionMap.get("convertCanReg4SystemAction")); // NOI18N
        convertCR4SystDefMenuItem.setName("convertCR4SystDefMenuItem"); // NOI18N
        migrateMenu.add(convertCR4SystDefMenuItem);

        importFromCR4MenuItem.setAction(actionMap.get("importDataFromCR4")); // NOI18N
        importFromCR4MenuItem.setText(resourceMap.getString("importFromCR4MenuItem.text")); // NOI18N
        importFromCR4MenuItem.setName("importFromCR4MenuItem"); // NOI18N
        migrateMenu.add(importFromCR4MenuItem);

        migratePopulationSetMenuItem.setAction(actionMap.get("loadCanReg4PDS")); // NOI18N
        migratePopulationSetMenuItem.setText(resourceMap.getString("migratePopulationSetMenuItem.text")); // NOI18N
        migratePopulationSetMenuItem.setName("migratePopulationSetMenuItem"); // NOI18N
        migrateMenu.add(migratePopulationSetMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        migrateMenu.add(jSeparator2);

        canreg4migrationMenuItem.setAction(actionMap.get("canreg4Migration")); // NOI18N
        canreg4migrationMenuItem.setText(resourceMap.getString("canreg4migrationMenuItem.text")); // NOI18N
        canreg4migrationMenuItem.setName("canreg4migrationMenuItem"); // NOI18N
        migrateMenu.add(canreg4migrationMenuItem);

        toolsMenu.add(migrateMenu);

        jSeparator4.setName("jSeparator4"); // NOI18N
        toolsMenu.add(jSeparator4);

        databaseStructureMenu.setText(resourceMap.getString("databaseStructureMenu.text")); // NOI18N
        databaseStructureMenu.setName("databaseStructureMenu"); // NOI18N

        setUpDatabaseMenuItem.setAction(actionMap.get("setUpNewDatabaseStructureAction")); // NOI18N
        setUpDatabaseMenuItem.setText(resourceMap.getString("setUpDatabaseMenuItem.text")); // NOI18N
        setUpDatabaseMenuItem.setName("setUpDatabaseMenuItem"); // NOI18N
        databaseStructureMenu.add(setUpDatabaseMenuItem);

        toolsMenu.add(databaseStructureMenu);

        installRPackagesMenuItem.setAction(actionMap.get("installRpackagesAction")); // NOI18N
        installRPackagesMenuItem.setName("installRPackagesMenuItem"); // NOI18N
        toolsMenu.add(installRPackagesMenuItem);

        garbleDatabaseMenuItem.setAction(actionMap.get("garbleDatabaseAction")); // NOI18N
        garbleDatabaseMenuItem.setName("garbleDatabaseMenuItem"); // NOI18N
        toolsMenu.add(garbleDatabaseMenuItem);

        menuBar.add(toolsMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItem3.setAction(actionMap.get("openCanReg5Instructions")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        helpMenu.add(jMenuItem3);

        jMenuItem4.setAction(actionMap.get("downloadLatestInstructions")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        helpMenu.add(jMenuItem4);

        jMenuItem7.setAction(actionMap.get("openReportBug")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        helpMenu.add(jMenuItem7);

        jSeparator1.setName("jSeparator1"); // NOI18N
        helpMenu.add(jSeparator1);

        linksMenu.setText(resourceMap.getString("linksMenu.text")); // NOI18N
        linksMenu.setName("linksMenu"); // NOI18N

        iacrWebsiteMenuItem.setAction(actionMap.get("openIacrWebsite")); // NOI18N
        iacrWebsiteMenuItem.setText(resourceMap.getString("iacrWebsiteMenuItem.text")); // NOI18N
        iacrWebsiteMenuItem.setName("iacrWebsiteMenuItem"); // NOI18N
        linksMenu.add(iacrWebsiteMenuItem);

        encrWebsiteMenuItem.setAction(actionMap.get("openENCRweb")); // NOI18N
        encrWebsiteMenuItem.setName("encrWebsiteMenuItem"); // NOI18N
        linksMenu.add(encrWebsiteMenuItem);

        jSeparator12.setName("jSeparator12"); // NOI18N
        linksMenu.add(jSeparator12);

        jMenuItem5.setAction(actionMap.get("openPubCanWebsite")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        linksMenu.add(jMenuItem5);

        icdo3DocumentationWebsiteMenuItem.setAction(actionMap.get("openICDO3web")); // NOI18N
        icdo3DocumentationWebsiteMenuItem.setName("icdo3MenuItem"); // NOI18N
        linksMenu.add(icdo3DocumentationWebsiteMenuItem);

        helpMenu.add(linksMenu);

        latestNewsMenuItem.setAction(actionMap.get("openLatestNews")); // NOI18N
        latestNewsMenuItem.setText(resourceMap.getString("latestNewsMenuItem.text")); // NOI18N
        latestNewsMenuItem.setName("latestNewsMenuItem"); // NOI18N
        helpMenu.add(latestNewsMenuItem);

        changelogMenuItem.setAction(actionMap.get("showChangeLogAction")); // NOI18N
        changelogMenuItem.setText(resourceMap.getString("changelogMenuItem.text")); // NOI18N
        changelogMenuItem.setName("changelogMenuItem"); // NOI18N
        helpMenu.add(changelogMenuItem);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setText(resourceMap.getString("statusMessageLabel.text")); // NOI18N
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        userLevelLabel.setText(resourceMap.getString("userLevelLabel.text")); // NOI18N
        userLevelLabel.setName("userLevelLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userLevelLabel)
                .addGap(91, 91, 91)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addComponent(statusMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 469, Short.MAX_VALUE)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusAnimationLabel)
                        .addContainerGap())))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11))
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userLevelLabel)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolBar);
    }// </editor-fold>//GEN-END:initComponents

    private void showWelcomeFrame() {
        WelcomeInternalFrame welcomeInternalFrame = new WelcomeInternalFrame(this);
        /*
         desktopPane.add(welcomeInternalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
         Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
         scr = this.getFrame().getSize();
         welcomeInternalFrame.setVisible(true);
         welcomeInternalFrame.setLocation(scr.width / 2 - welcomeInternalFrame.getWidth() / 2, (scr.height - 180) / 2 - welcomeInternalFrame.getHeight() / 2);
         */
        showAndPositionInternalFrame(desktopPane, welcomeInternalFrame);
        centerInternalFrame(desktopPane, welcomeInternalFrame);
        welcomeInternalFrame.setDesktopPane(desktopPane);
    }

    public static void maximizeHeight(JDesktopPane desktopPane, JInternalFrame internalFrame) {
        int x = internalFrame.getX(), y = internalFrame.getY();
        internalFrame.setBounds(x, y, internalFrame.getWidth(), desktopPane.getHeight() - y);
    }

    public static void maximizeWidth(JDesktopPane desktopPane, JInternalFrame internalFrame) {
        int x = internalFrame.getX(), y = internalFrame.getY();
        internalFrame.setBounds(x, y, internalFrame.getWidth(), desktopPane.getHeight() - y);
    }

    public static void centerInternalFrame(JDesktopPane desktopPane, JInternalFrame internalFrame) {
        int width = internalFrame.getWidth(), height = internalFrame.getHeight();
        int leftX = desktopPane.getWidth() / 2 - width / 2, topY = (desktopPane.getHeight() - toolBarHeight) / 2 - height / 2;
        internalFrame.setBounds(leftX, topY, width, height);
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    public void showChangeLog() {
        Task task = showChangeLogAction();
        task.run();
    }

    private class OpenICDO3ManualTask extends org.jdesktop.application.Task<Object, Void> {

        OpenICDO3ManualTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to OpenICDO3ManualTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() throws IOException {
            canreg.common.Tools.openFile("no file");
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    /**
     *
     */
    @Action
    public void showLoginFrame() {
        if (CanRegClientApp.getApplication().isLoggedIn()) {
            int i = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("DO WANT TO LOG OUT OF THE CURRENT CANREG SYSTEM?"), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("ALREADY LOGGED IN."), JOptionPane.YES_NO_OPTION);
            if (i == 0) {
                logOut();
            }
        }
        // If still logged in
        if (CanRegClientApp.getApplication().isLoggedIn()) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("ALREADY LOGGED IN."), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("MESSAGE"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            LoginInternalFrame loginInternalFrame = new LoginInternalFrame(this, desktopPane);
            showAndPositionInternalFrame(desktopPane, loginInternalFrame);
        }
    }

    /**
     *
     */
    @Action
    public void logOutaction() {
        int i = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("DO YOU REALLY WANT TO LOG OUT?"), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("LOG OUT?"), JOptionPane.YES_NO_OPTION);
        if (i == 0) {
            logOut();
        }
    }

    /**
     *
     * @return
     */
    @Action
    public Task viewWorkFiles() {
        return new ViewWorkFilesTask(getApplication());
    }

    // Works only on windows so far...
    private class ViewWorkFilesTask extends org.jdesktop.application.Task<Object, Void> {

        ViewWorkFilesTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ViewWorkFilesTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() throws IOException {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            String fileName = localSettings.getProperty(LocalSettings.WORKING_DIR_PATH_KEY);
            if (fileName == null) {
                fileName = ".";
            }
            canreg.common.Tools.openFile(fileName);
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    private static void debugOut(String msg) {
        if (DEBUG) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.INFO, msg);
        }
    }

    /**
     *
     */
    @Action
    public void showUsersLoggedIn() {
        try {
            // Get list of users...
            String[] list = CanRegClientApp.getApplication().listUsersLoggedIn();

            // format the list
            String users = "";
            for (int i = 0; i < list.length; i++) {
                users += list[i] + "\n";
            }
            // send some debug out
            debugOut(users);

            // show the dialog box
            StandardDialog sd = new StandardDialog(getFrame(), true, java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("USERS LOGGED IN"), users);
            sd.setLocationRelativeTo(null);
            sd.setVisible(true);

        } catch (RemoteException re) {
        } catch (SecurityException se) {
        }
    }

    /**
     *
     * @return
     */
    @Action
    public Task startDatabaseServer() {
        return new StartDatabaseServerTask(getApplication());
    }

    private class StartDatabaseServerTask extends org.jdesktop.application.Task<Object, Void> {

        StartDatabaseServerTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to StartDatabaseServerTask fields, here.
            super(app);

        }

        @Override
        protected Object doInBackground() {
            boolean success = false;
            try {
                CanRegClientApp.getApplication().startDatabaseServer();
                success = true;
            } catch (RemoteException ex) {
                Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            } catch (SecurityException ex) {
                Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
            return success;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            boolean success = (Boolean) result;
            if (success) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("Database_server_started."), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("MESSAGE"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("Database_server_not_started."), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("MESSAGE"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     *
     */
    @Action
    public void stopDatabaseServer() {
        try {
            CanRegClientApp.getApplication().stopDatabaseServer();
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("Database_server_stopped."), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("MESSAGE"), JOptionPane.INFORMATION_MESSAGE);

        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param userRightsLevel
     */
    public void setUserRightsLevel(Globals.UserRightLevels userRightsLevel) {
        this.userRightsLevel = userRightsLevel;
        userLevelLabel.setText(userRightsLevel.toString());

        boolean analysis = false;
        boolean management = false;
        boolean dataEntry = false;
        boolean loggedIn = false;

        // Hide/reveal menus
        if (userRightsLevel == Globals.UserRightLevels.NOT_LOGGED_IN) {
            analysis = false;
            management = false;
            dataEntry = false;
            loggedIn = false;
        } else {
            loggedIn = true;
            if (userRightsLevel == Globals.UserRightLevels.SUPERVISOR) {
                management = true;
                dataEntry = true;
                analysis = true;
            }
            if (userRightsLevel == Globals.UserRightLevels.REGISTRAR) {
                dataEntry = true;
                analysis = true;
            }
            if (userRightsLevel == Globals.UserRightLevels.ANALYST) {
                analysis = true;
            }
        }

        // setUpDatabaseMenuItem.setVisible(false);
        //toolbar buttons        
        // a bit too drastic : toolBar.setVisible(loggedIn);
        browseEditButton.setEnabled(dataEntry);
        createNewRecordButton.setEnabled(dataEntry);
        tableBuilderButton.setEnabled(analysis);

        // startDatabaseServerButton.setEnabled(management);
        //Menus
        editDictionaryMenuItem.setEnabled(management);

        analysisMenu.setEnabled(analysis);
        managementMenu.setEnabled(true);

        backupMenuItem.setEnabled(loggedIn);
        restoreMenuItem.setEnabled(management);
        
        usersMenuItem.setEnabled(loggedIn);
        
        advancedMenu.setEnabled(management);
        qualityControlMenu.setEnabled(management);

        dataEntryMenu.setEnabled(dataEntry);
        logOutMenuItem.setEnabled(loggedIn);

        importDataMenuItem.setEnabled(management);
        importFromCR4MenuItem.setEnabled(management);
        
        migratePopulationSetMenuItem.setEnabled(analysis);

        // If we are accessing a remote CanReg system not all things are available to us...
        if (!CanRegClientApp.getApplication().isCanRegServerRunningOnThisMachine()) {
            toolsMenu.setEnabled(!loggedIn);
            restoreMenuItem.setEnabled(false);
            // We show the install system button if we are not logged in to a remote server...
            // installSystemButton.setEnabled((userRightsLevel == Globals.UserRightLevels.NOT_LOGGED_IN));
        } else {
            toolsMenu.setEnabled(true);
            restoreMenuItem.setEnabled(true);
        }

        garbleDatabaseMenuItem.setVisible(management && Globals.SHOW_GARBLER);
        canreg4migrationMenuItem.setVisible(DEBUG);

        latestNewsMenuItem.setVisible(false);
        
        installRPackagesMenuItem.setVisible(new File(Globals.R_INSTALL_PACKAGES_SCRIPT).exists());
    }

    /**
     *
     */
    @Action
    public void showPatient() {
        // PatientFrame1.setVisible(!PatientFrame1.isVisible());
    }

    /**
     *
     * @return
     */
    @Action
    public Task openICDO3Manual() {
        return new OpenICDO3ManualTask(getApplication());
    }

    private void logOut() {
        try {
            CanRegClientApp.getApplication().logOut();
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
        setLoggedOut();
    }

    public void setLoggedOut() {
        setUserRightsLevel(Globals.UserRightLevels.NOT_LOGGED_IN);
        desktopPane.removeAll();
        desktopPane.validate();
        if (browseInternalFrame != null) {
            browseInternalFrame.close();
            browseInternalFrame = null;
        }
        getFrame().setTitle(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("CANREG5 - NOT LOGGED IN."));
        userLevelLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("NOT LOGGED IN."));
    }

    /**
     *
     */
    @Action
    public void openIacrWebsite() {
        try {
            canreg.common.Tools.browse((java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("http://www.iacr.com.fr/")));
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Action
    public void openICDO3web() {
        try {
            canreg.common.Tools.browse((java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("http://training.seer.cancer.gov/icdo3")));
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Action
    public void showCanRegHelpFile() {
        try {
            File file = new File("doc");
            canreg.common.Tools.openFile(file.getAbsolutePath() + Globals.FILE_SEPARATOR + Globals.CANREG_INSTRUCTIONS_LOCAL_FILE);
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Action
    public void openENCRweb() {
        try {
            canreg.common.Tools.browse(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("http://www.encr.com.fr/"));
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Action
    public void importData() {
        JInternalFrame importInternalFrame;
        // int i = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("DO YOU HAVA ALL YOUR DATA IN ONE FILE?"), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("ONE FILE?"), JOptionPane.YES_NO_OPTION);
        // if (i == JOptionPane.YES_OPTION) {
        //    importInternalFrame = new ImportView(/*<ictl.co>*/getDesktopPane()/*</ictl.co>*/);
        //} else {
        importInternalFrame = new ImportFilesView(/*<ictl.co>*/getDesktopPane()/*</ictl.co>*/);
        //}

        showAndPositionInternalFrame(desktopPane, importInternalFrame);
        maximizeHeight(desktopPane, importInternalFrame);
    }

        /**
     *
     */
    @Action
    public void importDataFromCR4() {
        JInternalFrame importInternalFrame = new ImportView(/*<ictl.co>*/getDesktopPane()/*</ictl.co>*/);
        showAndPositionInternalFrame(desktopPane, importInternalFrame);
        maximizeHeight(desktopPane, importInternalFrame);
    }
    
    /**
     *
     */
    @Action
    public void showExportFrame() {
        ExportReportInternalFrame exportFrame = new ExportReportInternalFrame(desktopPane);
        showAndPositionInternalFrame(desktopPane, exportFrame);
        maximizeHeight(desktopPane, exportFrame);
    }

    /**
     *
     */
    @Action
    public void browseEditAction() {
        if (browseInternalFrame == null) {
            browseInternalFrame = new BrowseInternalFrame(desktopPane);
            showAndPositionInternalFrame(desktopPane, browseInternalFrame);
            maximizeHeight(desktopPane, browseInternalFrame);
        } else {
            boolean newBrowser = false;
            if (browseInternalFrame.isVisible()) {
                int i = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("ONLY ONE BROWSER CAN BE OPEN AT A TIME. OPEN NEW ONE?"), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("NEW BROWSER?"), JOptionPane.YES_NO_OPTION);
                if (i == JOptionPane.YES_OPTION) {
                    newBrowser = true;
                }
            } else {
                newBrowser = true;
            }
            if (newBrowser) {
                browseInternalFrame.close();
                desktopPane.remove(browseInternalFrame);
                desktopPane.validate();
                browseInternalFrame = new BrowseInternalFrame(desktopPane);
                showAndPositionInternalFrame(desktopPane, browseInternalFrame);
                maximizeHeight(desktopPane, browseInternalFrame);
            } else {
                browseInternalFrame.setVisible(true);
            }
        }
    }

    /**
     *
     */
    @Action
    public void backupAction() {
        JInternalFrame internalFrame = new BackUpInternalFrame();
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void editDictionaryAction() {
        JInternalFrame internalFrame = new EditDictionaryInternalFrame(desktopPane);
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void showOptionFrame() {
        JInternalFrame internalFrame = new OptionsFrame(this);
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void showNameSexAction() {
        JInternalFrame internalFrame = new FirstNameSexInternalFrame();
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     * @param desktopPane
     * @param internalFrame
     */
    public static void showAndPositionInternalFrame(JDesktopPane desktopPane, JInternalFrame internalFrame) {
        int numberOfOpenFrames = desktopPane.getAllFrames().length;
        try {
            desktopPane.add(internalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        } catch (java.lang.IllegalArgumentException iae) {
            // frame already displayed
        }
        internalFrame.setVisible(true);
        int posX = 0;
        // Math.max(desktopPane.getWidth() / 2 - internalFrame.getWidth() / 2, 0);
        posX = Math.min(posX + numberOfOpenFrames * xOffset, desktopPane.getWidth() - xOffset);
        int posY = 0;
        // Math.max(desktopPane.getHeight() / 2 - internalFrame.getHeight() / 2, 0);
        posY = Math.min(posY + numberOfOpenFrames * yOffset, desktopPane.getHeight() - yOffset);
        internalFrame.setLocation(posX, posY);
        //CanRegClientApp.getApplication().getMainFrame();
        internalFrame.setVisible(true);
        internalFrame.toFront();
    }

    /**
     *
     */
    @Action
    public void installSystemAction() {
        // Choose a system.xml
        // copy it to system folder
        // Give log in window with settings from XML
        JInternalFrame internalFrame = new InstallNewSystemInternalFrame();
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void convertCanReg4SystemAction() {
        CanReg4SystemConverterInternalFrame internalFrame = new CanReg4SystemConverterInternalFrame();
        internalFrame.setDesktopPane(desktopPane);
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void restoreAction() {
        JInternalFrame internalFrame = new RestoreInternalFrame();
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void showLastRecord() {
        canreg.client.gui.dataentry2.RecordEditor internalFrame = null;
        String dataEntryVersion = localSettings.getProperty(LocalSettings.DATA_ENTRY_VERSION_KEY);
        if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW))
            internalFrame = new canreg.client.gui.dataentry2.RecordEditorMainFrame(desktopPane);
        else 
            internalFrame = new RecordEditor(desktopPane);

        internalFrame.setGlobalToolBox(CanRegClientApp.getApplication().getGlobalToolBox());
        internalFrame.setDictionary(CanRegClientApp.getApplication().getDictionary());
        DatabaseRecord patientRecord = null;
        DatabaseRecord[] tumourRecords;
        try {
            int patientID = 1;
            patientRecord = CanRegClientApp.getApplication().getRecord(patientID, "patient", true);

            internalFrame.addRecord(patientRecord);
            tumourRecords = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID(patientID + "", true);
            for (DatabaseRecord rec : tumourRecords) {
                internalFrame.addRecord(rec);
            }
        } catch (UnknownTableException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RecordLockedException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        showAndPositionInternalFrame(desktopPane, (JInternalFrame)internalFrame);
        maximizeHeight(desktopPane, (JInternalFrame)internalFrame);
    }

    /**
     *
     */
    @Action
    public void showFrequenciesFrame() {
        JInternalFrame internalFrame = new FrequenciesByYearInternalFrame(desktopPane);
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    /**
     *
     */
    @Action
    public void createNewRecordSetAction() {
        canreg.client.gui.dataentry2.RecordEditor internalFrame = null;
        String dataEntryVersion = localSettings.getProperty(LocalSettings.DATA_ENTRY_VERSION_KEY);
        if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW))
            internalFrame = new canreg.client.gui.dataentry2.RecordEditorMainFrame(desktopPane);
        else 
            internalFrame = new RecordEditor(desktopPane);
        
        internalFrame.setGlobalToolBox(CanRegClientApp.getApplication().getGlobalToolBox());
        internalFrame.setDictionary(CanRegClientApp.getApplication().getDictionary());
        internalFrame.addRecord(new Patient());
        internalFrame.addRecord(new Tumour());
        showAndPositionInternalFrame(desktopPane, (JInternalFrame)internalFrame);
        maximizeHeight(desktopPane, (JInternalFrame)internalFrame);
    }

    /**
     *
     */
    @Action
    public void editPopulationDataSets() {
        JInternalFrame internalFrame;
        try {
            internalFrame = new PDSChooserInternalFrame(desktopPane);
            showAndPositionInternalFrame(desktopPane, internalFrame);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Action
    public void duplicateSearchAction() {
        JInternalFrame internalFrame = new PersonSearchFrame(desktopPane);
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    @Action
    public Task showTableBuilder() {
        return new ShowTableBuilderTask(getApplication());
    }

    private class ShowTableBuilderTask extends org.jdesktop.application.Task<Object, Void> {

        ShowTableBuilderTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ShowTableBuilderTask fields, here.
            super(app);

        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            JInternalFrame internalFrame = new TableBuilderInternalFrame();
            showAndPositionInternalFrame(desktopPane, internalFrame);
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public void showUserManagement() {
        JInternalFrame internalFrame = new UserManagerInternalFrame();
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    @Action
    public void loadCanReg4PDS() {
        CanReg4PDSImporterInternalFrame internalFrame;
        try {
            internalFrame = new CanReg4PDSImporterInternalFrame(desktopPane, CanRegClientApp.getApplication().getPopulationDatasets());
            internalFrame.setDesktopPane(desktopPane);
            showAndPositionInternalFrame(desktopPane, internalFrame);
        } catch (SecurityException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void setUpNewDatabaseStructureAction() {
        ModifyDatabaseStructureInternalFrame internalFrame = new ModifyDatabaseStructureInternalFrame(desktopPane);
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    @Action
    public void modifyDatabaseStructureAction() {
        ModifyDatabaseStructureInternalFrame internalFrame = new ModifyDatabaseStructureInternalFrame(desktopPane);
        internalFrame.setFileName(Globals.DEFAULT_SYSTEM_XML);
        internalFrame.openXML();
        showAndPositionInternalFrame(desktopPane, internalFrame);
    }

    @Action
    public Task garbleDatabaseAction() {
        int r = JOptionPane.showConfirmDialog(null, "Do you really want to garble the database?\nAll your data will be garbled!");
        if (r == JOptionPane.YES_OPTION) {
            int r2 = JOptionPane.showConfirmDialog(null, "Do you REALLY really want to garble the database?\nAll your data will be scrambled!!!");
            if (r2 == JOptionPane.YES_OPTION) {
                return new GarbleDatabaseActionTask(getApplication());
            }
        }
        return null;

    }

    private class GarbleDatabaseActionTask extends org.jdesktop.application.Task<Object, Void> {

        GarbleDatabaseActionTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to GarbleDatabaseActionTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() throws RemoteException {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            DatabaseGarbler garbler = new DatabaseGarbler();
            garbler.garble(this);
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public void openCanReg5Instructions() {
        // first try to load the updated instructions
        File file = new File("doc");
        String docPath = file.getAbsolutePath();
        String fileName = docPath + Globals.FILE_SEPARATOR + Globals.CANREG_INSTRUCTIONS_LOCAL_FILE;
        File instructionsFile = new File(fileName);
        file = new File(Globals.CANREG_UPDATED_INSTRUCTIONS_LOCAL_FILE);
        if (file.exists()) {
            if (file.lastModified() > new File(docPath + Globals.FILE_SEPARATOR + Globals.CANREG_INSTRUCTIONS_LOCAL_FILE).lastModified()) {
                instructionsFile = file;
            }
        }
        try {
            canreg.common.Tools.openFile(instructionsFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public Task downloadLatestInstructions() {
        return new DownloadLatestInstructionsTask(getApplication());
    }

    private class DownloadLatestInstructionsTask extends org.jdesktop.application.Task<Object, Void> {

        WaitFrame waitFrame;

        DownloadLatestInstructionsTask(org.jdesktop.application.Application app) {
            super(app);
            waitFrame = new WaitFrame();
            waitFrame.setLabel(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("DOWNLOADING HANDBOOK..."));
            waitFrame.setIndeterminate(true);
            desktopPane.add(waitFrame, javax.swing.JLayeredPane.POPUP_LAYER);
            waitFrame.setVisible(true);
            waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
        }

        @Override
        protected Object doInBackground() {
            try {
                canreg.common.Tools.downloadFile(Globals.CANREG_UPDATED_INSTRUCTIONS_URL, Globals.CANREG_INSTRUCTIONS_LOCAL_FILE);
            } catch (IOException ex) {
                Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            waitFrame.setVisible(false);
            desktopPane.remove(waitFrame);
        }
    }

    @Action
    public void openPubCanWebsite() {
        try {
            canreg.common.Tools.browse(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("http://www.pubcan.org"));
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void openReportBug() {
        try {
            canreg.common.Tools.browse(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("BugReportWebsite"));
        } catch (IOException ex) {
            Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public Task openLatestNews() {
        return new OpenLatestNewsTask(getApplication());
    }

    private class OpenLatestNewsTask extends org.jdesktop.application.Task<Object, Void> {

        OpenLatestNewsTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to OpenLatestNewsTask fields, here.
            super(app);
            LatestNewsInternalFrame internalFrame = new LatestNewsInternalFrame();
            showAndPositionInternalFrame(desktopPane, internalFrame);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public Task showChangeLogAction() {
        return new ShowChangeLogActionTask(getApplication());
    }

    private class ShowChangeLogActionTask extends org.jdesktop.application.Task<Object, Void> {

        ShowChangeLogActionTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ShowChangeLogActionTask fields, here.
            super(app);
            ChangeLogInternalFrame internalFrame = new ChangeLogInternalFrame();
            showAndPositionInternalFrame(desktopPane, internalFrame);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public Task installRpackagesAction() {
        return new InstallRpackagesActionTask(getApplication());

    }

    private class InstallRpackagesActionTask extends org.jdesktop.application.Task<Object, Void> {

        private BufferedInputStream is;
        private final WaitFrame waitFrame;

        InstallRpackagesActionTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to InstallRpackagesActionTask fields, here.
            super(app);
            // feedbackLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("LAUNCHING_SERVER..."));
            waitFrame = new WaitFrame();
            waitFrame.setLabel(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("INSTALLING_PACKAGES..."));
            waitFrame.setIndeterminate(true);
            desktopPane.add(waitFrame, javax.swing.JLayeredPane.POPUP_LAYER);
            waitFrame.setVisible(true);
            waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            String rpath = localSettings.getProperty(LocalSettings.R_PATH);

            // does R exist?
            if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
                // throw new FileNotFoundException("R installation invalid/not configured");
                return "Error! R is not installed";
            } else {
                File scriptFile = new File(Globals.R_INSTALL_PACKAGES_SCRIPT);
                if (scriptFile.exists()) {
                    
                    ArrayList<String> commandList = new ArrayList();
                    commandList.add(rpath);
                    commandList.add("--vanilla");
                    commandList.add("--slave");
                    commandList.add("--file=" + scriptFile.getAbsolutePath());
                    
                  //  String command = canreg.common.Tools.encapsulateIfNeeded(rpath) 
                //            + " --slave --file="
                 //           + canreg.common.Tools.encapsulateIfNeeded(scriptFile.getAbsolutePath());
                    System.out.println("Command: " + commandList);
                    System.out.flush();

                    Runtime rt = Runtime.getRuntime();
                    Process pr;
                    try {
                        pr = rt.exec(commandList.toArray(new String[]{}));
                    } catch (IOException ex) {
                        Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
                        return "Error - R script not found.";
                    }
                    try {
                        // collect the output from the R program in a stream
                        is = new BufferedInputStream(pr.getInputStream());
                        pr.waitFor();
                        // convert the output to a string
                        String theString = convertStreamToString(is);
                        Logger.getLogger(CanRegClientView.class.getName()).log(Level.INFO, "Messages from R: \n{0}", theString);
                        // System.out.println(theString.split("\\r?\\n").length);
                        // Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.INFO, null, pr.exitValue());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (java.util.NoSuchElementException ex) {
                        Logger.getLogger(CanRegClientView.class.getName()).log(Level.SEVERE, null, ex);
                        BufferedInputStream errorStream = new BufferedInputStream(pr.getErrorStream());
                        String errorMessage = convertStreamToString(errorStream);
                        System.out.println(errorMessage);
                        return "Error - R says:\n" + errorMessage;
                    } finally {
                        System.out.println(pr.exitValue());
                        Logger.getLogger(CanRegClientView.class.getName()).log(Level.INFO, null, pr.exitValue());
                    }
                } else {
                    return "Error - no R script.";
                }
            }

            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            waitFrame.dispose();
            if (result == null) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("R_packages_installed."), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("MESSAGE"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("R_packages_not_installed."), java.util.ResourceBundle.getBundle("canreg/client/gui/resources/CanRegClientView").getString("MESSAGE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    @Action
    public void canreg4Migration() {
        JInternalFrame internalFrame = new CanReg4MigrationInternalFrame(desktopPane);
        showAndPositionInternalFrame(desktopPane, internalFrame);        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu advancedMenu;
    private javax.swing.JMenu analysisMenu;
    private javax.swing.JMenuItem backupMenuItem;
    private javax.swing.JButton browseEditButton;
    private javax.swing.JMenuItem browseEditMenuItem;
    private javax.swing.JMenuItem canreg4migrationMenuItem;
    private javax.swing.JMenuItem changelogMenuItem;
    private javax.swing.JMenuItem convertCR4SystDefMenuItem;
    private javax.swing.JButton createNewRecordButton;
    private javax.swing.JMenuItem createNewRecordMenuItem;
    private javax.swing.JMenu dataEntryMenu;
    private javax.swing.JMenu databaseStructureMenu;
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JMenuItem duplicateSearchMenuItem;
    private javax.swing.JMenuItem editDictionaryMenuItem;
    private javax.swing.JMenuItem editPDSMenuItem;
    private javax.swing.JMenuItem encrWebsiteMenuItem;
    private javax.swing.JMenuItem exportDataReportsMenuItem;
    private javax.swing.JMenuItem frequenciesMenuItem;
    private javax.swing.JMenuItem garbleDatabaseMenuItem;
    private javax.swing.JButton handbookButton;
    private javax.swing.JMenuItem iacrWebsiteMenuItem;
    private javax.swing.JMenuItem icdo3DocumentationWebsiteMenuItem;
    private javax.swing.JMenuItem importDataMenuItem;
    private javax.swing.JMenuItem importFromCR4MenuItem;
    private javax.swing.JMenuItem incidenceTablesMenuItem;
    private javax.swing.JMenuItem installRPackagesMenuItem;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator14;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenuItem latestNewsMenuItem;
    private javax.swing.JMenu linksMenu;
    private javax.swing.JMenuItem logInMenuItem;
    private javax.swing.JMenuItem logOutMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenu managementMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu migrateMenu;
    private javax.swing.JMenuItem migratePopulationSetMenuItem;
    private javax.swing.JMenuItem nameSexMenuItem;
    private javax.swing.JButton optionsButton;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenu qualityControlMenu;
    private javax.swing.JMenuItem restoreMenuItem;
    private javax.swing.JMenuItem setUpDatabaseMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton tableBuilderButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JLabel userLevelLabel;
    private javax.swing.JMenuItem usersMenuItem;
    private javax.swing.JMenuItem viewWorkFilesMenuItem;
    // End of variables declaration//GEN-END:variables
    // private javax.swing.JInternalFrame internalFrame;
    // private javax.swing.JInternalFrame welcomeInternalFrame;
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private Globals.UserRightLevels userRightsLevel = Globals.UserRightLevels.NOT_LOGGED_IN;
    private static final boolean DEBUG = Globals.DEBUG;
    LocalSettings localSettings;
    private static final int xOffset = 30, yOffset = 30;
    private static final int toolBarHeight = 80;
    private BrowseInternalFrame browseInternalFrame;
}
