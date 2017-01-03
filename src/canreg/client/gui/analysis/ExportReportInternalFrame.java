/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015 International Agency for Research on Cancer
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

/*
 * ExportFrame.java
 *
 * Created on 28 February 2008, 14:12
 */
package canreg.client.gui.analysis;

import au.com.bytecode.opencsv.CSVWriter;
import canreg.client.CanRegClientApp;
import canreg.client.DistributedTableDataSourceClient;
import canreg.client.LocalSettings;
import canreg.client.gui.components.VariablesExportDetailsPanel;
import canreg.client.gui.tools.TableColumnAdjuster;
import canreg.client.gui.tools.XTableColumnModel;
import canreg.common.*;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.cachingtableapi.DistributedTableModel;
import canreg.common.database.Dictionary;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.ibm.icu.util.Calendar;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author morten
 */
public class ExportReportInternalFrame extends javax.swing.JInternalFrame implements ActionListener {

    private JDesktopPane dtp;
    private DistributedTableDescription tableDatadescription;
    private DistributedTableDataSourceClient tableDataSource;
    private DistributedTableModel tableDataModel;
    private JScrollPane resultScrollPane;
    //<ictl.co>
//    private JTable resultTable = new JTable();
    private JTable resultTable = new javax.swing.JTable() {
        //<ictl.co>
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            String columnName = this.getColumnName(column);
            if (variableChooserPanel.isLocalCalendarSelected() && LocalizationHelper.isRtlLanguageActive() &&
                    ("InciD".equalsIgnoreCase(columnName) ||
                            "BirthD".equalsIgnoreCase(columnName) ||
                            "YEAR".equalsIgnoreCase(columnName) ||
                            "DLC".equalsIgnoreCase(columnName) ||
                            "PatientUpdateDate".equalsIgnoreCase(columnName) ||
                            "UpDate".equalsIgnoreCase(columnName)
                    )) {
                if (c instanceof DefaultTableCellRenderer.UIResource) {
                    String value = ((DefaultTableCellRenderer.UIResource) c).getText();
                    ((DefaultTableCellRenderer.UIResource) c).setText(DateHelper.analyseJTableColumnValue(value, getModel().getColumnName(column)));
                }
            }
            return c;
        }
        //</ictl.co>
    };
    //</ictl.co>
    private JFileChooser chooser;
    private String path;
    private LocalSettings localSettings;
    private LinkedList<String> variablesToShow;
    private XTableColumnModel tableColumnModel;
    private final Map<Integer, Dictionary> dictionary;
    private int VARIABLE_NAME_ENGLISH_INDEX = 1;
    private int VARIABLE_NAME_SHORT_INDEX = 0;
    private int VARIABLE_NAME_FULL_INDEX = 2;
    private int VARIABLE_NAME_STANDARD_INDEX = 3;
    private DatabaseVariablesListElement tumourIDdbvle;
    private String fileName;

    /**
     * Creates new form ExportFrame
     *
     * @param dtp is a pointer to the current desktop pane.
     */
    public ExportReportInternalFrame(JDesktopPane dtp) {
        initComponents();
        this.dtp = dtp;
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        dictionary = CanRegClientApp.getApplication().getDictionary();
        rangeFilterPanel.initValues();
        rangeFilterPanel.setDeskTopPane(dtp);
        rangeFilterPanel.setDatabaseDescription(CanRegClientApp.getApplication().getDatabseDescription());
        initOtherComponents();
        initValues();
    }

    /**
     *
     * @param dtp
     */
    public void setDeskTopPane(JDesktopPane dtp) {
        this.dtp = dtp;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resultPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        exportSourceInformationCheckBox = new javax.swing.JCheckBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        rangeFilterPanel = new canreg.client.gui.components.RangeFilterPanel();
        variableChooserPanel = new canreg.client.gui.components.VariablesChooserPanel();
        settingsPanel = new javax.swing.JPanel();
        setupPanel = new javax.swing.JPanel();
        loadSetupButton = new javax.swing.JButton();
        saveSetupButton = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        headingCheckBox = new javax.swing.JCheckBox();
        variableNamesComboBox = new javax.swing.JComboBox();
        variableNamesLabel = new javax.swing.JLabel();
        fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        formatDateCheckBox = new javax.swing.JCheckBox();
        correctUnknownCheckBox = new javax.swing.JCheckBox();
        dateFormatComboBox = new javax.swing.JComboBox();
        exportPanel = new javax.swing.JPanel();
        writeFileButton = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(ExportReportInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setDoubleBuffered(true);
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(800, 400));
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        resultPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("resultPanel.border.title"))); // NOI18N
        resultPanel.setName("resultPanel"); // NOI18N

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 173, Short.MAX_VALUE)
        );

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(ExportReportInternalFrame.class, this);
        exportSourceInformationCheckBox.setAction(actionMap.get("exportSourceInformationTickBoxUpdated")); // NOI18N
        exportSourceInformationCheckBox.setText(resourceMap.getString("exportSourceInformationCheckBox.text")); // NOI18N
        exportSourceInformationCheckBox.setToolTipText(resourceMap.getString("exportSourceInformationCheckBox.toolTipText")); // NOI18N
        exportSourceInformationCheckBox.setName("exportSourceInformationCheckBox"); // NOI18N

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        rangeFilterPanel.setName("rangeFilterPanel"); // NOI18N
        jSplitPane1.setLeftComponent(rangeFilterPanel);

        variableChooserPanel.setName("variableChooserPanel"); // NOI18N
        jSplitPane1.setRightComponent(variableChooserPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(exportSourceInformationCheckBox))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportSourceInformationCheckBox))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        settingsPanel.setName("settingsPanel"); // NOI18N

        setupPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("setupPanel.border.title"))); // NOI18N
        setupPanel.setName("setupPanel"); // NOI18N

        loadSetupButton.setText(resourceMap.getString("loadSetupButton.text")); // NOI18N
        loadSetupButton.setEnabled(false);
        loadSetupButton.setName("loadSetupButton"); // NOI18N

        saveSetupButton.setText(resourceMap.getString("saveSetupButton.text")); // NOI18N
        saveSetupButton.setEnabled(false);
        saveSetupButton.setName("saveSetupButton"); // NOI18N

        javax.swing.GroupLayout setupPanelLayout = new javax.swing.GroupLayout(setupPanel);
        setupPanel.setLayout(setupPanelLayout);
        setupPanelLayout.setHorizontalGroup(
            setupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupPanelLayout.createSequentialGroup()
                .addComponent(loadSetupButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveSetupButton))
        );
        setupPanelLayout.setVerticalGroup(
            setupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupPanelLayout.createSequentialGroup()
                .addGroup(setupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadSetupButton)
                    .addComponent(saveSetupButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("optionsPanel.border.title"))); // NOI18N
        optionsPanel.setName("optionsPanel"); // NOI18N

        headingCheckBox.setText(resourceMap.getString("headingCheckBox.text")); // NOI18N
        headingCheckBox.setEnabled(false);
        headingCheckBox.setName("headingCheckBox"); // NOI18N

        variableNamesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Short", "English", "Full", "Standard" }));
        variableNamesComboBox.setName("variableNamesComboBox"); // NOI18N

        variableNamesLabel.setText(resourceMap.getString("variableNamesLabel.text")); // NOI18N
        variableNamesLabel.setName("variableNamesLabel"); // NOI18N

        fileFormatLabel.setText(resourceMap.getString("fileFormatLabel.text")); // NOI18N
        fileFormatLabel.setName("fileFormatLabel"); // NOI18N

        fileFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tab Separated Values", "Comma Separated" }));
        fileFormatComboBox.setName("fileFormatComboBox"); // NOI18N

        formatDateCheckBox.setAction(actionMap.get("formatDateCheckBoxChanged")); // NOI18N
        formatDateCheckBox.setText(resourceMap.getString("formatDateCheckBox.text")); // NOI18N
        formatDateCheckBox.setToolTipText(resourceMap.getString("formatDateCheckBox.toolTipText")); // NOI18N
        formatDateCheckBox.setName("formatDateCheckBox"); // NOI18N

        correctUnknownCheckBox.setText(resourceMap.getString("correctUnknownCheckBox.text")); // NOI18N
        correctUnknownCheckBox.setToolTipText(resourceMap.getString("correctUnknownCheckBox.toolTipText")); // NOI18N
        correctUnknownCheckBox.setEnabled(false);
        correctUnknownCheckBox.setName("correctUnknownCheckBox"); // NOI18N

        dateFormatComboBox.setEditable(true);
        dateFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "dd/mm/yyyy", "mm/dd/yyyy", "mm/dd/yy", "yyyy/mm/dd" }));
        dateFormatComboBox.setToolTipText(resourceMap.getString("dateFormatComboBox.toolTipText")); // NOI18N
        dateFormatComboBox.setEnabled(false);
        dateFormatComboBox.setName("dateFormatComboBox"); // NOI18N
        dateFormatComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dateFormatComboBoxMousePressed(evt);
            }
        });

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(variableNamesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(variableNamesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 526, Short.MAX_VALUE)
                        .addComponent(headingCheckBox))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(fileFormatLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fileFormatComboBox, 0, 713, Short.MAX_VALUE))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(formatDateCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateFormatComboBox, 0, 691, Short.MAX_VALUE))
                    .addComponent(correctUnknownCheckBox))
                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(variableNamesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(variableNamesLabel)
                    .addComponent(headingCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileFormatLabel)
                    .addComponent(fileFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formatDateCheckBox)
                    .addComponent(dateFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(correctUnknownCheckBox)
                .addGap(28, 28, 28))
        );

        exportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("exportPanel.border.title"))); // NOI18N
        exportPanel.setName("exportPanel"); // NOI18N

        writeFileButton.setAction(actionMap.get("writeFileAction")); // NOI18N
        writeFileButton.setName("writeFileButton"); // NOI18N

        javax.swing.GroupLayout exportPanelLayout = new javax.swing.GroupLayout(exportPanel);
        exportPanel.setLayout(exportPanelLayout);
        exportPanelLayout.setHorizontalGroup(
            exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(writeFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
        );
        exportPanelLayout.setVerticalGroup(
            exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportPanelLayout.createSequentialGroup()
                .addComponent(writeFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addComponent(setupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exportPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(setupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("settingsPanel.TabConstraints.tabTitle"), settingsPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
            .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dateFormatComboBoxMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dateFormatComboBoxMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateFormatComboBoxMousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox correctUnknownCheckBox;
    private javax.swing.JComboBox dateFormatComboBox;
    private javax.swing.JPanel exportPanel;
    private javax.swing.JCheckBox exportSourceInformationCheckBox;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JLabel fileFormatLabel;
    private javax.swing.JCheckBox formatDateCheckBox;
    private javax.swing.JCheckBox headingCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton loadSetupButton;
    private javax.swing.JPanel optionsPanel;
    private canreg.client.gui.components.RangeFilterPanel rangeFilterPanel;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JButton saveSetupButton;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JPanel setupPanel;
    private canreg.client.gui.components.VariablesChooserPanel variableChooserPanel;
    private javax.swing.JComboBox variableNamesComboBox;
    private javax.swing.JLabel variableNamesLabel;
    private javax.swing.JButton writeFileButton;
    // End of variables declaration//GEN-END:variables

    /**
     *
     * @return
     */
    public JDesktopPane getDtp() {
        return dtp;
    }

    private void initValues() {
        String tableName = rangeFilterPanel.getSelectedTable();
        variableChooserPanel.setTableName(tableName);
        variableChooserPanel.setVariablesInTable(rangeFilterPanel.getArrayOfVariablesInSelectedTables());
        variableChooserPanel.initPanel(dictionary);
        rangeFilterPanel.setDeskTopPane(dtp);
        rangeFilterPanel.setActionListener(this);
        rangeFilterPanel.setTablesToChooseFrom(Globals.DEFAULT_TABLE_CHOOSER_TABLE_LIST);
        rangeFilterPanel.setSelectedTable(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME);
        tumourIDdbvle = CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString());
    }

    private void initOtherComponents() {

        resultScrollPane = canreg.common.gui.LazyViewport.createLazyScrollPaneFor(resultTable);

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
                resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE));
        resultPanelLayout.setVerticalGroup(
                resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(resultScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE));

        resultScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setColumnSelectionAllowed(true);
        resultPanel.setVisible(false);

        resultTable.setName("resultTable"); // NOI18N
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // rowClicked(evt);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // columnTableMousePressed(evt);
            }
        });

        // exportSourceInformationCheckBox.setVisible(false);
    }

    /**
     *
     * @return
     */
    @Action
    public Task refresh() {
        // navigationPanel.goToTopAction();
        resultPanel.setVisible(false);
        return new RefreshTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
    }

    private class RefreshTask extends org.jdesktop.application.Task<Object, Void> {

        String tableName = null;
        DatabaseFilter filter = new DatabaseFilter();
        DistributedTableDescription newTableDatadescription = null;

        RefreshTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to RefreshTask fields, here.
            super(app);
            rangeFilterPanel.setRefreshButtonEnabled(false);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
            tableName = rangeFilterPanel.getSelectedTable();
            variablesToShow = variableChooserPanel.getSelectedVariableNames(tableName);
            filter.setFilterString(rangeFilterPanel.getFilter().trim());
            filter.setSortByVariable(rangeFilterPanel.getSortByVariable().trim());
            filter.setRange(rangeFilterPanel.getRange());
            // setProgress(0, 0, 4);
            setMessage("Initiating query...");
            // setProgress(1, 0, 4);
            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
        }

        @Override
        protected Object doInBackground() {
            String result = "OK";
            try {
                newTableDatadescription = canreg.client.CanRegClientApp.getApplication().getDistributedTableDescription(filter, tableName);
            } catch (SQLException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not valid";
            } catch (RemoteException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Remote exception";
            } catch (SecurityException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Security exception";
                // } catch (InterruptedException ignore) {
                //     result = "Ignore";
            } catch (DistributedTableDescriptionException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not OK";
            } catch (UnknownTableException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not OK";
            }

            return result;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().

            // boolean theResult = ;
            if (result.equals("OK")) {

                // release old resultSet
                if (tableDatadescription != null) {
                    try {
                        CanRegClientApp.getApplication().releaseResultSet(tableDatadescription.getResultSetID());
                        tableDataSource = null;
                    } catch (SQLException ex) {
                        Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException securityException) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, securityException);
                    } catch (RemoteException remoteException) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, remoteException);
                    }
                }

                tableDatadescription = newTableDatadescription;

                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());

                if (tableDatadescription != null) {
                    try {
                        tableDataSource = new DistributedTableDataSourceClient(tableDatadescription);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
                }

                if (tableDataSource != null) {
                    try {
                        tableDataModel = new DistributedTableModel(tableDataSource);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // tableDataModel = new PagingTableModel(tableDataSource);
                    Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
                    // setProgress(2, 0, 4);
                }

                setMessage("Starting a new transaction...");

                rangeFilterPanel.setRecordsShown(tableDataModel.getRowCount());

                // setProgress(3, 0, 4);

                setMessage("Fetching data...");
                resultTable.setColumnSelectionAllowed(false);
                resultTable.setModel(tableDataModel);
                tableColumnModel = new XTableColumnModel();
                resultTable.setColumnModel(tableColumnModel);
                resultTable.createDefaultColumnsFromModel();
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());

                // setProgress(4, 0, 4);
                setMessage("Finished");

                updateVariablesShown();

                resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                TableColumnAdjuster tca = new TableColumnAdjuster(resultTable);
                tca.setColumnDataIncluded(false);
                tca.setOnlyAdjustLarger(false);
                tca.adjustColumns();
                resultPanel.setVisible(true);
            } else if (result.equals("Not valid")) {
                JOptionPane.showInternalMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/ExportReportInternalFrame").getString("NOT_A_VALID_FILTER"), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, result);
            }
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            rangeFilterPanel.setRefreshButtonEnabled(true);
        }
    }

    private void updateVariablesShown() {
        String tableName = rangeFilterPanel.getSelectedTable();
        variablesToShow = variableChooserPanel.getSelectedVariableNames(tableName);
        // first set all invisible
        Enumeration<TableColumn> tcs = tableColumnModel.getColumns(false);
        while (tcs.hasMoreElements()) {
            TableColumn column = tcs.nextElement();
            tableColumnModel.setColumnVisible(column, variablesToShow.contains(column.getHeaderValue().toString()));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("refresh".equalsIgnoreCase(e.getActionCommand())) {
            Task refreshTask = refresh();
            refreshTask.execute();
        } else if ("tableChanged".equalsIgnoreCase(e.getActionCommand())) {
            String tableName = rangeFilterPanel.getSelectedTable();
            variableChooserPanel.setTableName(tableName);
            variableChooserPanel.setVariablesInTable(rangeFilterPanel.getArrayOfVariablesInSelectedTables());
            variableChooserPanel.initPanel(dictionary);
            resultPanel.setVisible(false);
            // We can't add the source info if we don't have tumour info...
            if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)
                    || tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)
                    || tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)
                    || tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
                exportSourceInformationCheckBox.setEnabled(false);
                exportSourceInformationCheckBox.setSelected(false);
            } else {
                // disable the export source information tool...
                exportSourceInformationCheckBox.setEnabled(true);
            }
        }
    }

    /**
     *
     * @return
     */
    @Action
    public Task writeFileAction() {
        if (chooser == null) {
            path = localSettings.getProperty("export_data_path");
            if (path == null) {
                chooser = new JFileChooser();
            } else {
                chooser = new JFileChooser(path);
            }
        }
        // Get filename
        int returnVal = chooser.showSaveDialog(this);
        fileName = "";
        String separatingString = "\t";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                //set the file name
                fileName = chooser.getSelectedFile().getCanonicalPath();

                // TODO: Make this dynamic
                if (fileFormatComboBox.getSelectedIndex() == 1) {
                    separatingString = ",";
                    // append standard file extension
                    if (!(fileName.endsWith(".csv") || fileName.endsWith(".CSV"))) {
                        fileName += ".csv";
                    }
                } else {
                    separatingString = "\t";
                    // append standard file extension
                    if (!(fileName.endsWith(".tsv") || fileName.endsWith(".TSV"))
                            && !(fileName.endsWith(".csv") || fileName.endsWith(".CSV")) 
                            && !(fileName.endsWith(".txt") || fileName.endsWith(".TXT"))) 
                    {
                        fileName += ".txt";
                    }
                }

                File file = new File(fileName);
                if (file.exists()) {
                    int choice = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/ExportReportInternalFrame").getString("FILE_EXISTS") + ": " + fileName + ".\n" + java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/ExportReportInternalFrame").getString("OVERWRITE?"), java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/ExportReportInternalFrame").getString("FILE_EXISTS") + ".", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.CANCEL_OPTION) {
                        return null;
                    } else if (choice == JOptionPane.NO_OPTION) {
                        // choose a new file
                        writeFileAction();
                        return null;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            return null;
        }
        return new WriteFileActionTask(fileName, org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class), separatingString);
    }

    private class WriteFileActionTask extends org.jdesktop.application.Task<Object, Void> {

        CSVWriter csvWriter;
        int rowCount;
        int columnCount;
        private boolean formatDate = false;
        private boolean correctUnknown = false;
        private boolean exportSources = false;
        int maxNumberOfSourcesPerTumour = 0;
        private int tumourIDcolumn;
        private Set<String> sourceVariableNames;

        WriteFileActionTask(String fileName, org.jdesktop.application.Application app, String separatingString) {
            super(app);

            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);

            writeFileButton.setEnabled(false);
            // refresh the table if necessary
            if (!resultPanel.isVisible()) {
                Task refresher = refresh();
                refresher.execute();
                while (!refresher.isDone()) {
                    // wait 
                }
            }

            formatDate = formatDateCheckBox.isSelected();
            correctUnknown = correctUnknownCheckBox.isSelected() && formatDate;

            // Lock the table
            resultPanel.setVisible(false);
            rangeFilterPanel.setRefreshButtonEnabled(false);

            Map<String, boolean[]> variablesToExport = new TreeMap<String, boolean[]>();

            // build the map of column names and checked variables boxes
            for (int column = 0; column < resultTable.getColumnCount(); column++) {
                String columnName =
                        canreg.common.Tools.toUpperCaseStandardized(resultTable.getColumnName(column));
                VariablesExportDetailsPanel vedp = variableChooserPanel.getVariablesExportDetailsPanelByName(columnName);
                variablesToExport.put(columnName, vedp.getCheckboxes());
            }

            try {
                File file = new File(fileName);
                localSettings.setProperty("export_data_path", file.getParent());
                // FileWriter bw = new FileWriter(fileName); // TODO: Make choice of encoding dynamic?
                
                Writer bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "UTF-8"));
                
                csvWriter = new CSVWriter(bw, separatingString.charAt(0), '\"');
                rowCount = resultTable.getRowCount();
                columnCount = resultTable.getColumnCount();
            } catch (IOException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Export the sorces?
            if (exportSourceInformationCheckBox.isSelected()) {
                try {
                    exportSources = true;
                    maxNumberOfSourcesPerTumour = CanRegClientApp.getApplication().getDatabaseStats().getMaxNumberOfSourcesPerTumourRecord();
                    Enumeration<TableColumn> columns = tableColumnModel.getColumns(false);
                    boolean found = false;

                    while (tumourIDdbvle != null && !found && columns.hasMoreElements()) {
                        TableColumn column = columns.nextElement();
                        String header = column.getIdentifier().toString();
                        found = tumourIDdbvle.getDatabaseVariableName().equalsIgnoreCase(header);
                        if (found) {
                            for (int i = 0; i < columnCount; i++) {
                                String name = resultTable.getColumnName(i);
                                if (name.equalsIgnoreCase(header)) {
                                    tumourIDcolumn = i;
                                }
                            }
                        }
                    }
                    sourceVariableNames = new LinkedHashSet<String>();
                    DatabaseVariablesListElement[] dbvls = CanRegClientApp.getApplication().getGlobalToolBox().getVariables();
                    for (DatabaseVariablesListElement dbvle : dbvls) {
                        if (dbvle.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                            sourceVariableNames.add(dbvle.getDatabaseVariableName());
                        }
                    }
                    System.out.println("Max number of Sources: " + maxNumberOfSourcesPerTumour);
                    //Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, null, "Max number of Sources: "+ maxNumberOfSourcesPerTumour);
                } catch (RemoteException ex) {
                    Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            // Here we do indeed reference the jtable. However as long as the user does not move the columns it should be ok...
            // TODO: reference the data source instead of the resultTable!

            LinkedList<String> line = new LinkedList<String>();
            VariablesExportDetailsPanel dvle;
            Object value;
            GregorianCalendarCanReg gregorianCanRegCalendar;
            Dictionary dict;

            for (int column = 0; column < columnCount; column++) {
                dvle = variableChooserPanel.getVariablesExportDetailsPanelByName(resultTable.getColumnName(column));
                boolean[] bools = dvle.getCheckboxes();
                String columnName = resultTable.getColumnName(column);
                //
                if (variableNamesComboBox.getSelectedIndex() == VARIABLE_NAME_ENGLISH_INDEX) {
                    columnName = dvle.getVariable().getEnglishName();
                } else if (variableNamesComboBox.getSelectedIndex() == VARIABLE_NAME_FULL_INDEX) {
                    columnName = dvle.getVariable().getFullName();
                } else if (variableNamesComboBox.getSelectedIndex() == VARIABLE_NAME_STANDARD_INDEX) {
                    String standardName = dvle.getVariable().getStandardVariableName();
                    if (standardName != null) {
                        columnName = standardName;
                    } else {
                        // add a star
                        columnName += "*";
                    }
                } else if (variableNamesComboBox.getSelectedIndex() == VARIABLE_NAME_SHORT_INDEX) {
                    // do nothing
                }

                // the raw name
                if (bools[0]) {
                    line.add(columnName);
                }
                // the category
                if (bools[1]) {
                    line.add(columnName + " (cat)");
                }
                // the description
                if (bools[2]) {
                    line.add(columnName + " (desc)");
                }
            }
            // add the source bits if needed
            if (exportSources) {
                for (int i = 0; i < maxNumberOfSourcesPerTumour; i++) {
                    for (String header : sourceVariableNames) {
                        if (maxNumberOfSourcesPerTumour > 1) {
                            line.add(header + (i + 1));
                        } else {
                            line.add(header);
                        }
                    }
                }
            }

            csvWriter.writeNext(line.toArray(new String[0]));
            line = new LinkedList<String>();
            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    dvle = variableChooserPanel.getVariablesExportDetailsPanelByName(resultTable.getColumnName(column));
                    value = resultTable.getValueAt(row, column);
                    //<ictl.co>
                    if (LocalizationHelper.isRtlLanguageActive()) {
                        if (dvle.getVariable().getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)
                                && variableChooserPanel.isLocalCalendarSelected()
                                && DateHelper.analyseContentForDateValue((String) value)) {
                            value = DateHelper.gregorianDateStringToLocaleDateString((String) value, Globals.DATE_FORMAT_STRING);
                        }
                    }
                    //<ictl.oc>
                    boolean[] bools = dvle.getCheckboxes();
                    // the raw code
                    if (bools[0]) {
                        // Should we format the date?
                        if (formatDate && dvle.getVariable().getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
                            try {
                                gregorianCanRegCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg((String) value, Globals.DATE_FORMAT_STRING);
                                if (correctUnknown && gregorianCanRegCalendar != null) {
                                    if (gregorianCanRegCalendar.isUnknownMonth()) {
                                        // Set month to July
                                        gregorianCanRegCalendar.set(Calendar.MONTH, 7 - 1);
                                        gregorianCanRegCalendar.setUnkownMonth(false);
                                        // And day to first
                                        gregorianCanRegCalendar.set(Calendar.DAY_OF_MONTH, 1);
                                        gregorianCanRegCalendar.setUnknownDay(false);
                                    } else if (gregorianCanRegCalendar.isUnknownDay()) {
                                        // Set day to mid-month
                                        gregorianCanRegCalendar.set(Calendar.DAY_OF_MONTH, 15);
                                        gregorianCanRegCalendar.setUnknownDay(false);
                                    }
                                }
                                if (gregorianCanRegCalendar != null) {
                                    //<ictl.co>
                                    if (variableChooserPanel.isLocalCalendarSelected() && LocalizationHelper.isRtlLanguageActive()) {
                                        value = DateHelper.parseGregorianCalendarCanRegToDateStringLocale(gregorianCanRegCalendar, (String) dateFormatComboBox.getSelectedItem());
                                    } else {
                                        value = DateHelper.parseGregorianCalendarCanRegToDateString(gregorianCanRegCalendar, (String) dateFormatComboBox.getSelectedItem());
                                    }
                                    //</ictl.co>
                                }
                            } catch (ParseException ex) {
                                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalArgumentException ex) {
                                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.WARNING, "Value: " + value, ex);
                            }
                        }
                        if (value == null) {
                            value = "";
                        }
                        line.add(value.toString());
                    }
                    // the category
                    if (bools[1]) {
                        String code = (String) value;
                        String category = "Invalid category.";
                        try {
                            int categoryLength = dvle.getDictionary().getCodeLength();
                            if (code.length() >= categoryLength) {
                                category = dvle.getDictionary().getDictionaryEntries().get(code.substring(0, categoryLength)).getDescription();
                            }
                        } catch (NullPointerException npe) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, npe);
                        }
                        line.add(category);
                    }
                    // the description
                    if (bools[2]) {
                        String code = (String) value;
                        String description = "Invalid code.";
                        try {
                            int codeLength = dvle.getDictionary().getFullDictionaryCodeLength();
                            if (code.length() == codeLength) {
                                description = dvle.getDictionary().getDictionaryEntries().get(code).getDescription();
                            }
                        } catch (NullPointerException npe) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, npe);
                        }
                        line.add(description);
                    }
                }
                // if we should export the sources we do that here...
                if (exportSources) {
                    Object tumourIDobj = resultTable.getValueAt(row, tumourIDcolumn);

                    /* int count = 0;
                     int stopCount = 500;
                     try 500 times before giving up...
                     while (tumourIDobj==null&&count<stopCount){
                     try {
                     wait(5);
                     count++;
                     tumourIDobj = resultTable.getValueAt(row, tumourIDcolumn);
                     catch (InterruptedException ex) {
                     Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     }
                     */

                    String tumourID = null;
                    if (tumourIDobj != null) {
                        tumourID = tumourIDobj.toString();
                        Tumour tumour;
                        int numberOfSourcesWritten = 0;
                        try {
                            tumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(tumourID, false);
                            if (tumour != null && tumour.getSources() != null) {
                                for (Source source : tumour.getSources()) {
                                    for (String variableName : sourceVariableNames) {
                                        line.add(source.getVariable(variableName).toString());
                                    }
                                    numberOfSourcesWritten++;
                                }
                            }
                        } catch (DistributedTableDescriptionException ex) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnknownTableException ex) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SecurityException ex) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RecordLockedException ex) {
                            Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            for (; numberOfSourcesWritten < maxNumberOfSourcesPerTumour;) {
                                for (String variableName : sourceVariableNames) {
                                    line.add("");
                                }
                                numberOfSourcesWritten++;
                            }
                        }
                    } else {
                        return false;
                    }
                }
                setProgress(100 * row / rowCount);
                // Garbage collect every 1000 rows?
                if (row % 1000 == 0) {
                    System.gc();
                }

                csvWriter.writeNext(line.toArray(new String[0]));
                line.removeAll(line);
            }

            return true;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);

            boolean success = (Boolean) result;

            try {
                // Runs on the EDT.  Update the GUI based on
                // the result computed by doInBackground().
                csvWriter.flush();
                csvWriter.close();

                rangeFilterPanel.setRefreshButtonEnabled(true);
                resultPanel.setVisible(true);
                writeFileButton.setEnabled(true);

                if (success) {
                    JOptionPane.showMessageDialog(dtp, "Data exported to : " + fileName, "Success", JOptionPane.INFORMATION_MESSAGE);
                    localSettings.writeSettings();
                } else {
                    JOptionPane.showMessageDialog(dtp, "Something went wrong while exporting to : " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException ex) {
                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Action
    public void formatDateCheckBoxChanged() {
        correctUnknownCheckBox.setEnabled(formatDateCheckBox.isSelected());
        dateFormatComboBox.setEnabled(formatDateCheckBox.isSelected());
    }

    @Action
    public void exportSourceInformationTickBoxUpdated() {
        // if tumourid is not selcted we need to refresh the table, so we hide the table as we have it...
        if (!variableChooserPanel.isVariableDataSelected(tumourIDdbvle.getDatabaseVariableName())) {
            resultPanel.setVisible(false);
        }
        variableChooserPanel.setVariableDataSelected(tumourIDdbvle.getDatabaseVariableName(), true);
    }
}
