/*
 * PDSEditorInternalFrame.java
 *
 * Created on 29 September 2008, 15:59
 */
package canreg.client.gui.dataentry;

import canreg.client.CanRegClientApp;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.FastFilterInternalFrame;
import canreg.client.gui.tools.ExcelAdapter;
import canreg.common.Globals;
import canreg.server.database.AgeGroupStructure;
import canreg.server.database.PopulationDataset;
import canreg.server.database.PopulationDatasetsEntry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author  ervikm
 */
public class PDSEditorInternalFrame extends javax.swing.JInternalFrame implements ActionListener {

    private FastFilterInternalFrame filterWizardInternalFrame;
    private JDesktopPane dtp;
    private Document doc;
    private PopulationDataset pds;
    private JTextField dateTextField;
    private PopulationDataset[] worldPopulations;

    /** Creates new form PDSEditorInternalFrame
     * @param dtp
     * @param worldPopulations 
     */
    public PDSEditorInternalFrame(JDesktopPane dtp, PopulationDataset[] worldPopulations) {
        this.dtp = dtp;
        this.worldPopulations = worldPopulations;
        initComponents();
        initValues();
    }

    /**
     * 
     * @param pds
     */
    public void setPopulationDataset(PopulationDataset pds) {
        this.pds = pds;
        if (pds != null) {
            nameTextField.setText(pds.getPopulationDatasetName());
            filterTextField.setText(pds.getFilter());
            sourceTextField.setText(pds.getSource());
            descriptionTextArea.setText(pds.getDescription());
            ageGroupStructureComboBox.setSelectedItem(pds.getAgeGroupStructure());
            if (ageGroupStructureComboBox.getSelectedItem() == null && pds.getAgeGroupStructure() != null) {
                ageGroupStructureComboBox.addItem(pds.getAgeGroupStructure());
                ageGroupStructureComboBox.setSelectedItem(pds.getAgeGroupStructure());
            }
            dateTextField.setText(pds.getDate());
            refreshPopulationDataSetTable();
            lockedToggleButton.setSelected(true);
            if (pds.isWorldPopulationBool()) {
                standardPopulationComboBox.setVisible(false);
                editStandardPopulationButton.setVisible(false);
                standardPopulationLabel.setVisible(false);
            } else {
                boolean found = false;
                PopulationDataset worldPopulation = null;
                int i = 0;
                while (!found && i < worldPopulations.length) {
                    worldPopulation = worldPopulations[i++];
                    if (worldPopulation != null) {
                        found = worldPopulation.getPopulationDatasetID() == pds.getWorldPopulationID();
                    }
                }
                if (worldPopulation != null) {
                    standardPopulationComboBox.setSelectedItem(worldPopulation);
                }
            }
            lockTheFields();
        }
    }

    private void refreshPopulationDataSetTable() {
        AgeGroupStructure ags = (AgeGroupStructure) ageGroupStructureComboBox.getSelectedItem();
        String[] ageGroupNames = ags.getAgeGroupNames();
        String[][] ageGroupLabels = new String[ageGroupNames.length][];

        for (int i = 0; i < ageGroupNames.length; i++) {
            ageGroupLabels[i] = new String[]{ageGroupNames[i]};
        }

        ageGroupLabelsTable.setModel(new DefaultTableModel(ageGroupLabels, new String[]{"Age Group"}) {

            Class[] types = new Class[]{
                java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        PopulationDatasetsEntry[] ageGroups;
        Object[][] pdsTableData = new Object[ageGroupNames.length][2];

        for (int i = 0; i < ageGroupNames.length; i++) {
            // pdsTableData[i][0] = ageGroupNames[i];
            pdsTableData[i][0] = new Integer(0);
            pdsTableData[i][0] = new Integer(0);
            /// pdsTableData[i][3] = ageGroupNames[i];
        }

        if (pds != null) {
            ageGroups = pds.getAgeGroups();
            for (PopulationDatasetsEntry pdse : ageGroups) {
                if (pdse.getAgeGroup() < pdsTableData.length && pdse.getSex() <= 2) {
                    pdsTableData[pdse.getAgeGroup()][pdse.getSex() - 1] = pdse.getCount();
                } else {
                    Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.WARNING, "Outside skope: " + pdse.getAgeGroup() + " " + pdse.getSex());
                }
            }
        }

        pdsTable.setModel(new javax.swing.table.DefaultTableModel(
                pdsTableData,
                new String[]{
                    "Male", "Female"
                }) {

            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean[]{
                true, true
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        detailsPanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        ageGroupStructureComboBox = new javax.swing.JComboBox();
        ageGroupStructureLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        descriptionLabel = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        sourceTextField = new javax.swing.JTextField();
        filterTextField = new javax.swing.JTextField();
        filterWizardButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        filterLabel = new javax.swing.JLabel();
        standardPopulationLabel = new javax.swing.JLabel();
        standardPopulationComboBox = new javax.swing.JComboBox();
        editStandardPopulationButton = new javax.swing.JButton();
        otherAgeGroupStructureButton = new javax.swing.JButton();
        dataSetPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        ageGroupLabelsTable = new javax.swing.JTable();
        pdsTable = new javax.swing.JTable();
        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane3 = new javax.swing.JSplitPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        lockedToggleButton = new javax.swing.JToggleButton();
        deleteButton = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PDSEditorInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        detailsPanel.setName("detailsPanel"); // NOI18N

        dateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        dateLabel.setText(resourceMap.getString("dateLabel.text")); // NOI18N
        dateLabel.setName("dateLabel"); // NOI18N

        dateChooser.setName("dateChooser"); // NOI18N

        ageGroupStructureComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ageGroupStructureComboBox.setName("ageGroupStructureComboBox"); // NOI18N
        ageGroupStructureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ageGroupStructureChanged(evt);
            }
        });

        ageGroupStructureLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        ageGroupStructureLabel.setText(resourceMap.getString("ageGroupStructureLabel.text")); // NOI18N
        ageGroupStructureLabel.setName("ageGroupStructureLabel"); // NOI18N

        descriptionScrollPane.setName("descriptionScrollPane"); // NOI18N

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setName("descriptionTextArea"); // NOI18N
        descriptionScrollPane.setViewportView(descriptionTextArea);

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        descriptionLabel.setText(resourceMap.getString("descriptionLabel.text")); // NOI18N
        descriptionLabel.setName("descriptionLabel"); // NOI18N

        sourceLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        sourceLabel.setText(resourceMap.getString("sourceLabel.text")); // NOI18N
        sourceLabel.setName("sourceLabel"); // NOI18N

        sourceTextField.setText(resourceMap.getString("sourceTextField.text")); // NOI18N
        sourceTextField.setName("sourceTextField"); // NOI18N

        filterTextField.setText(resourceMap.getString("filterTextField.text")); // NOI18N
        filterTextField.setName("filterTextField"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(PDSEditorInternalFrame.class, this);
        filterWizardButton.setAction(actionMap.get("filterWizardAction")); // NOI18N
        filterWizardButton.setName("filterWizardButton"); // NOI18N

        nameTextField.setText(resourceMap.getString("nameTextField.text")); // NOI18N
        nameTextField.setToolTipText(resourceMap.getString("nameTextField.toolTipText")+Globals.PDS_DATABASE_NAME_LENGTH);
        nameTextField.setName("nameTextField"); // NOI18N

        nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        nameLabel.setText(resourceMap.getString("nameLabel.text")); // NOI18N
        nameLabel.setName("nameLabel"); // NOI18N

        filterLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        filterLabel.setText(resourceMap.getString("filterLabel.text")); // NOI18N
        filterLabel.setName("filterLabel"); // NOI18N

        standardPopulationLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        standardPopulationLabel.setText(resourceMap.getString("standardPopulationLabel.text")); // NOI18N
        standardPopulationLabel.setName("standardPopulationLabel"); // NOI18N

        standardPopulationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        standardPopulationComboBox.setName("standardPopulationComboBox"); // NOI18N

        editStandardPopulationButton.setAction(actionMap.get("editWorldPopulation")); // NOI18N
        editStandardPopulationButton.setName("editStandardPopulationButton"); // NOI18N

        otherAgeGroupStructureButton.setAction(actionMap.get("otherAction")); // NOI18N
        otherAgeGroupStructureButton.setName("otherAgeGroupStructureButton"); // NOI18N

        javax.swing.GroupLayout detailsPanelLayout = new javax.swing.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(filterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ageGroupStructureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(standardPopulationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detailsPanelLayout.createSequentialGroup()
                        .addComponent(standardPopulationComboBox, 0, 144, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editStandardPopulationButton))
                    .addComponent(dateChooser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(descriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(sourceTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addComponent(filterTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterWizardButton))
                    .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addComponent(ageGroupStructureComboBox, 0, 134, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(otherAgeGroupStructureButton)))
                .addContainerGap())
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterWizardButton)
                    .addComponent(filterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ageGroupStructureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ageGroupStructureLabel)
                    .addComponent(otherAgeGroupStructureButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editStandardPopulationButton)
                    .addComponent(standardPopulationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(standardPopulationLabel))
                .addContainerGap(102, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("detailsPanel.TabConstraints.tabTitle"), detailsPanel); // NOI18N

        dataSetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("dataSetPanel.border.title"))); // NOI18N
        dataSetPanel.setName("dataSetPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jSplitPane1.setDividerLocation(60);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        ageGroupLabelsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Age group"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        ageGroupLabelsTable.setCellSelectionEnabled(true);
        ageGroupLabelsTable.setName("ageGroupLabelsTable"); // NOI18N
        ageGroupLabelsTable.getTableHeader().setReorderingAllowed(false);
        jSplitPane1.setLeftComponent(ageGroupLabelsTable);
        ageGroupLabelsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        ageGroupLabelsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("pdsTable.columnModel.title0")); // NOI18N

        pdsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)},
                {new Integer(0), new Integer(0)}
            },
            new String [] {
                "Male", "Female"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        pdsTable.setName("pdsTable"); // NOI18N
        pdsTable.getTableHeader().setReorderingAllowed(false);
        jSplitPane1.setRightComponent(pdsTable);
        pdsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        pdsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("pdsTable.columnModel.title1")); // NOI18N
        pdsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("pdsTable.columnModel.title2")); // NOI18N
        ExcelAdapter myAd = new ExcelAdapter(pdsTable);

        jScrollPane1.setViewportView(jSplitPane1);

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(60);
        jSplitPane2.setDividerSize(0);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jSplitPane3.setBorder(null);
        jSplitPane3.setDividerLocation(137);
        jSplitPane3.setDividerSize(0);
        jSplitPane3.setResizeWeight(0.5);
        jSplitPane3.setName("jSplitPane3"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jSplitPane3.setLeftComponent(jLabel1);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jSplitPane3.setRightComponent(jLabel2);

        jSplitPane2.setRightComponent(jSplitPane3);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jSplitPane2.setLeftComponent(jLabel3);

        javax.swing.GroupLayout dataSetPanelLayout = new javax.swing.GroupLayout(dataSetPanel);
        dataSetPanel.setLayout(dataSetPanelLayout);
        dataSetPanelLayout.setHorizontalGroup(
            dataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );
        dataSetPanelLayout.setVerticalGroup(
            dataSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSetPanelLayout.createSequentialGroup()
                .addComponent(jSplitPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("dataSetPanel.TabConstraints.tabTitle"), dataSetPanel); // NOI18N

        saveButton.setAction(actionMap.get("saveAction")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        cancelButton.setAction(actionMap.get("cancelAction")); // NOI18N
        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        lockedToggleButton.setAction(actionMap.get("lockedAction")); // NOI18N
        lockedToggleButton.setName("lockedToggleButton"); // NOI18N
        lockedToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lockedToggleButtonActionPerformed(evt);
            }
        });

        deleteButton.setAction(actionMap.get("deletePopulationDataSetAction")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(deleteButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                .addComponent(lockedToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(cancelButton)
                    .addComponent(lockedToggleButton)
                    .addComponent(deleteButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void ageGroupStructureChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ageGroupStructureChanged
    // buildPDSfromTable();
    refreshPopulationDataSetTable();
}//GEN-LAST:event_ageGroupStructureChanged

private void lockedToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lockedToggleButtonActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_lockedToggleButtonActionPerformed

    /**
     *
     */
    @Action
    public void filterWizardAction() {

        if (filterWizardInternalFrame.getParent() == null) {
            dtp.add(filterWizardInternalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
            filterWizardInternalFrame.setLocation(dtp.getWidth() / 2 - filterWizardInternalFrame.getWidth() / 2, dtp.getHeight() / 2 - filterWizardInternalFrame.getHeight() / 2);
            filterWizardInternalFrame.setVisible(false);
        }
        if (filterWizardInternalFrame.isVisible()) {
            filterWizardInternalFrame.toFront();
            try {
                filterWizardInternalFrame.setSelected(true);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            filterWizardInternalFrame.setTextPane("");
            filterWizardInternalFrame.setVisible(true);
        }
    }

    /**
     * 
     */
    public void initValues() {
        // Get the system description
        doc = CanRegClientApp.getApplication().getDatabseDescription();

        // variablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE);

        filterWizardInternalFrame = new FastFilterInternalFrame();
        filterWizardInternalFrame.setTableName("Both");
        filterWizardInternalFrame.setActionListener(this);

        ageGroupStructureComboBox.setModel(new javax.swing.DefaultComboBoxModel(Globals.defaultAgeGroupStructures));
        dateTextField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateChooser.setDateFormatString(Globals.DATE_FORMAT_STRING);
        dateChooser.setDate(new Date());
        standardPopulationComboBox.setModel(new javax.swing.DefaultComboBoxModel(worldPopulations));
        refreshPopulationDataSetTable();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().getClass() == FastFilterInternalFrame.class) {
            filterTextField.setText(e.getActionCommand());
        }
    }

    /**
     * 
     */
    @Action
    public void saveAction() {
        lockedToggleButton.setSelected(true);
        lockTheFields();
        buildPDSfromTable();
        try {
            if (pds.getPopulationDatasetID() < 0) {
                CanRegClientApp.getApplication().saveNewPopulationDataset(pds);
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Successfully saved population dataset: " + pds.getPopulationDatasetName() + ".", "Population dataset successfully saved.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                try {
                    CanRegClientApp.getApplication().deletePopulationDataset(pds.getPopulationDatasetID());
                } catch (SQLException ex) {
                    Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                CanRegClientApp.getApplication().saveNewPopulationDataset(pds);
            }
        } catch (SecurityException ex) {
            Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildPDSfromTable() {
        if (pds == null) {
            pds = new PopulationDataset();
        } else {
            pds.flushAgeGroups();
        }
        pds.setPopulationDatasetName(nameTextField.getText().trim());
        pds.setSource(sourceTextField.getText().trim());
        pds.setFilter(filterTextField.getText().trim());
        pds.setDescription(descriptionTextArea.getText().trim());
        pds.setAgeGroupStructure((AgeGroupStructure) ageGroupStructureComboBox.getSelectedItem());
        pds.setDate(dateTextField.getText());
        pds.setWorldPopulationBool(false);
        PopulationDataset wpds = (PopulationDataset) standardPopulationComboBox.getSelectedItem();
        pds.setWorldPopulationID(wpds.getWorldPopulationID());

        int numberOfAgeGroups = pds.getAgeGroupStructure().getNumberOfAgeGroups();

        if (pdsTable.isEditing()) {
            pdsTable.getCellEditor().stopCellEditing();
        }

        for (int ageGroup = 0; ageGroup < numberOfAgeGroups; ageGroup++) {
            for (int sex = 0; sex <= 1; sex++) {
                Integer count;
                try {

                    count = Integer.parseInt(pdsTable.getValueAt(ageGroup, sex).toString());
                } catch (java.lang.NullPointerException npe) {
                    count = new Integer(0);
                    Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.WARNING, "Missing value in the pds...");
                } catch (java.lang.NumberFormatException nfe){
                    count = new Integer(0);                    
                    Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.WARNING, "Error in the pds...");
                }
                pds.addAgeGroup(new PopulationDatasetsEntry(ageGroup, sex + 1, count));
                System.out.println((sex + 1) + " - " + ageGroup + ": " + count);
            }
        }
    }

    /**
     * 
     */
    @Action
    public void editWorldPopulation() {
        PDSEditorInternalFrame populationDatasetEditorInternalFrame = new PDSEditorInternalFrame(dtp, worldPopulations);
        populationDatasetEditorInternalFrame.setPopulationDataset((PopulationDataset) standardPopulationComboBox.getSelectedItem());
        CanRegClientView.showAndCenterInternalFrame(dtp, populationDatasetEditorInternalFrame);
    }

    /**
     * 
     */
    @Action
    public void lockedAction() {
        lockTheFields();
    }

    /**
     * 
     */
    @Action
    public void cancelAction() {
        this.dispose();
    }

    /**
     * 
     */
    @Action
    public void otherAction() {
    }

    private void lockTheFields() {
        ageGroupStructureComboBox.setFocusable(!lockedToggleButton.isSelected());
        dateTextField.setFocusable(!lockedToggleButton.isSelected());
        detailsPanel.setFocusable(!lockedToggleButton.isSelected());
        editStandardPopulationButton.setFocusable(!lockedToggleButton.isSelected());
        filterTextField.setFocusable(!lockedToggleButton.isSelected());
        filterWizardButton.setFocusable(!lockedToggleButton.isSelected());
        ageGroupStructureComboBox.setFocusable(!lockedToggleButton.isSelected());
        editStandardPopulationButton.setFocusable(!lockedToggleButton.isSelected());
        nameTextField.setFocusable(!lockedToggleButton.isSelected());
        pdsTable.setFocusable(!lockedToggleButton.isSelected());
        saveButton.setFocusable(!lockedToggleButton.isSelected());
        sourceTextField.setFocusable(!lockedToggleButton.isSelected());
        standardPopulationComboBox.setFocusable(!lockedToggleButton.isSelected());
        otherAgeGroupStructureButton.setFocusable(!lockedToggleButton.isSelected());
        descriptionTextArea.setFocusable(!lockedToggleButton.isSelected());
    }

    @Action
    public void deletePopulationDataSetAction() {
        int result = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Do you really want to delete this population dataset: " + pds.getPopulationDatasetName() + ".", "Do you really want to delete this population dataset.", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                CanRegClientApp.getApplication().deletePopulationDataset(pds.getPopulationDatasetID());
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Successfully saved population dataset: " + pds.getPopulationDatasetName() + ".", "Population dataset successfully saved.", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(PDSEditorInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable ageGroupLabelsTable;
    private javax.swing.JComboBox ageGroupStructureComboBox;
    private javax.swing.JLabel ageGroupStructureLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataSetPanel;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JButton editStandardPopulationButton;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JTextField filterTextField;
    private javax.swing.JButton filterWizardButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton lockedToggleButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton otherAgeGroupStructureButton;
    private javax.swing.JTable pdsTable;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JTextField sourceTextField;
    private javax.swing.JComboBox standardPopulationComboBox;
    private javax.swing.JLabel standardPopulationLabel;
    // End of variables declaration//GEN-END:variables
}
