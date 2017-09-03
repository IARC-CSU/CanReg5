/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016 International Agency for Research on Cancer
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
 * RangeFilterPanel.java
 *
 * Created on 27 May 2008, 16:38
 */
package canreg.client.gui.components;

import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Tools;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDesktopPane;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public final class RangeFilterPanel extends javax.swing.JPanel implements ActionListener {

    private Document doc;
    // private DatabaseVariablesListElement[] variablesInDB;
    private DatabaseIndexesListElement[] indexesInDB;
    private List<String> filterCollection;
    private JDesktopPane dtp;
    private FastFilterInternalFrame filterWizardInternalFrame;
    private ActionListener actionListener;
    // private DatabaseVariablesListElement[] variablesInDB;
    private DatabaseVariablesListElement[] variablesInTable;
    private DatabaseVariablesListElement[] patientVariablesInDB;
    private DatabaseVariablesListElement[] tumourVariablesInDB;
    private DatabaseVariablesListElement[] sourceVariablesInDB;
    private boolean rangeEnabled = true;
    private final GlobalToolBox globalToolBox;

    /**
     * Creates new form RangeFilterPanel
     */
    public RangeFilterPanel() {
        initComponents();
        // comment the following lines away if you want to change the GUIs using this bean.
        globalToolBox = canreg.client.CanRegClientApp.getApplication().getGlobalToolBox();
        setDeskTopPane(canreg.client.CanRegClientApp.getApplication().getDeskTopPane());
        ////
    }

    /**
     *
     */
    public void close() {
        filterWizardInternalFrame.dispose();
    }

    /**
     *
     * @return
     */
    public Document getDatabseDescription() {
        return doc;
    }

    public void setDatabaseDescription(Document doc) {
        this.doc = doc;
        indexesInDB = canreg.common.Tools.getIndexesListElements(doc, Globals.NAMESPACE);

        rangeComboBox.setModel(new javax.swing.DefaultComboBoxModel(getArrayOfIndexedVariables(indexesInDB)));
        patientVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.PATIENT_TABLE_NAME);
        tumourVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.TUMOUR_TABLE_NAME);
        sourceVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.SOURCE_TABLE_NAME);

        Comparator comparator = new Comparator<DatabaseVariablesListElement>() {
            @Override
            public int compare(DatabaseVariablesListElement object1, DatabaseVariablesListElement o2) {
                return object1.toString().compareToIgnoreCase(o2.toString());
            }
        };

        Arrays.sort(patientVariablesInDB, comparator);
        Arrays.sort(tumourVariablesInDB, comparator);
        Arrays.sort(sourceVariablesInDB, comparator);

        refreshVariableList();
        refreshFilterComboBox();
        refreshIndexList();
        setSelectedTable(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME);
    }

    private static DatabaseVariablesListElement[] getArrayOfIndexedVariables(DatabaseIndexesListElement[] indexes) {
        Set<DatabaseVariablesListElement> set = new TreeSet<DatabaseVariablesListElement>(
                new Comparator<DatabaseVariablesListElement>() {
                    @Override
                    public int compare(DatabaseVariablesListElement o1, DatabaseVariablesListElement o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
        for (DatabaseIndexesListElement index : indexes) {
            set.addAll(Arrays.asList(index.getVariableListElementsInIndex()));
        }
        return set.toArray(new DatabaseVariablesListElement[0]);
    }

    /**
     *
     * @return
     */
    public String getFilter() {
        if (useFilterCheckBox.isSelected()) {
            // since database is not case sensitive - we uppercase the filter using our standard method...
            // TODO: Check this against Turkish.
            // return Tools.toUpperCaseStandardized(filterComboBox.getSelectedItem().toString().trim());
            return filterComboBox.getSelectedItem().toString().trim();
        } else {
            return "";
        }
    }

    /**
     *
     * @return
     */
    public String getSelectedTable() {
        return tableChooserComboBox.getSelectedItem().toString();
    }

    /**
     *
     * @param dtp
     */
    public void setDeskTopPane(JDesktopPane dtp) {
        this.dtp = dtp;
    }

    /**
     *
     * @param active
     */
    public void setFilterActive(boolean active) {
        useFilterCheckBox.setSelected(active);
    }

    /**
     *
     * @param enabled
     */
    public void setRefreshButtonEnabled(boolean enabled) {
        refreshTableButton.setEnabled(enabled);
    }

    /**
     *
     * @param visible
     */
    public void setTableChooserVisible(boolean visible) {
        tableChooserPanel.setVisible(visible);
        setSelectedTable(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME);
    }

    /**
     *
     * @param visible
     */
    public void setRecordPanelvisible(boolean visible) {
        recordsPanel.setVisible(visible);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rangePanel = new javax.swing.JPanel();
        rangeComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        rangeStartTextField = new javax.swing.JTextField();
        rangeEndTextField = new javax.swing.JTextField();
        filterPanel = new javax.swing.JPanel();
        filterComboBox = new javax.swing.JComboBox();
        useFilterCheckBox = new javax.swing.JCheckBox();
        wizardButton = new javax.swing.JButton();
        recordsPanel = new javax.swing.JPanel();
        recordsShownField = new javax.swing.JTextField();
        recordsShownLabel = new javax.swing.JLabel();
        andLabel = new javax.swing.JLabel();
        tableChooserPanel = new javax.swing.JPanel();
        tableChooserComboBox = new javax.swing.JComboBox();
        sortByChooserPanel = new javax.swing.JPanel();
        sortByChooserComboBox = new javax.swing.JComboBox();
        refreshTableButton = new javax.swing.JButton();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RangeFilterPanel.class);
        rangePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("rangePanel.border.title"))); // NOI18N
        rangePanel.setToolTipText(resourceMap.getString("rangePanel.toolTipText")); // NOI18N
        rangePanel.setEnabled(false);
        rangePanel.setName("rangePanel"); // NOI18N

        rangeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Record Number", "ICD10", "Names" }));
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RangeFilterPanel.class, this);
        rangeComboBox.setAction(actionMap.get("rangeComboboxChanged")); // NOI18N
        rangeComboBox.setName("rangeComboBox"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        rangeStartTextField.setToolTipText(resourceMap.getString("rangeStartTextField.toolTipText")); // NOI18N
        rangeStartTextField.setMinimumSize(new java.awt.Dimension(50, 20));
        rangeStartTextField.setName("rangeStartTextField"); // NOI18N
        rangeStartTextField.setPreferredSize(new java.awt.Dimension(70, 20));
        rangeStartTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                rangeStartTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rangeStartTextFieldMouseReleased(evt);
            }
        });

        rangeEndTextField.setToolTipText(resourceMap.getString("rangeEndTextField.toolTipText")); // NOI18N
        rangeEndTextField.setMinimumSize(new java.awt.Dimension(50, 20));
        rangeEndTextField.setName("rangeEndTextField"); // NOI18N
        rangeEndTextField.setPreferredSize(new java.awt.Dimension(70, 20));
        rangeEndTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                rangeEndTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rangeEndTextFieldMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout rangePanelLayout = new javax.swing.GroupLayout(rangePanel);
        rangePanel.setLayout(rangePanelLayout);
        rangePanelLayout.setHorizontalGroup(
            rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rangePanelLayout.createSequentialGroup()
                .addComponent(rangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rangeStartTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rangeEndTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        rangePanelLayout.setVerticalGroup(
            rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rangePanelLayout.createSequentialGroup()
                .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(rangeStartTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)
                        .addComponent(rangeEndTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5))
        );

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("filterPanel.border.title"))); // NOI18N
        filterPanel.setName("filterPanel"); // NOI18N

        filterComboBox.setEditable(true);
        filterComboBox.setToolTipText(resourceMap.getString("filterComboBox.toolTipText")); // NOI18N
        filterComboBox.setName("filterComboBox"); // NOI18N
        filterComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                filterComboBoxMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                filterComboBoxMouseReleased(evt);
            }
        });

        useFilterCheckBox.setText(resourceMap.getString("useFilterCheckBox.text")); // NOI18N
        useFilterCheckBox.setToolTipText(resourceMap.getString("useFilterCheckBox.toolTipText")); // NOI18N
        useFilterCheckBox.setName("useFilterCheckBox"); // NOI18N

        wizardButton.setAction(actionMap.get("filterWizardAction")); // NOI18N
        wizardButton.setToolTipText(resourceMap.getString("wizardButton.toolTipText")); // NOI18N
        wizardButton.setName("wizardButton"); // NOI18N

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filterPanelLayout.createSequentialGroup()
                .addComponent(useFilterCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(wizardButton))
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wizardButton)
                    .addComponent(useFilterCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        recordsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("recordsPanel.border.title"))); // NOI18N
        recordsPanel.setName("recordsPanel"); // NOI18N

        recordsShownField.setEditable(false);
        recordsShownField.setText(resourceMap.getString("recordsShownField.text")); // NOI18N
        recordsShownField.setToolTipText(resourceMap.getString("recordsShownField.toolTipText")); // NOI18N
        recordsShownField.setName("recordsShownField"); // NOI18N

        recordsShownLabel.setText(resourceMap.getString("recordsShownLabel.text")); // NOI18N
        recordsShownLabel.setName("recordsShownLabel"); // NOI18N

        javax.swing.GroupLayout recordsPanelLayout = new javax.swing.GroupLayout(recordsPanel);
        recordsPanel.setLayout(recordsPanelLayout);
        recordsPanelLayout.setHorizontalGroup(
            recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, recordsPanelLayout.createSequentialGroup()
                .addComponent(recordsShownField, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recordsShownLabel))
        );
        recordsPanelLayout.setVerticalGroup(
            recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(recordsShownField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(recordsShownLabel))
        );

        andLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        andLabel.setText(resourceMap.getString("andLabel.text")); // NOI18N
        andLabel.setName("andLabel"); // NOI18N

        tableChooserPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("tableChooserPanel.border.title"))); // NOI18N
        tableChooserPanel.setName("tableChooserPanel"); // NOI18N

        tableChooserComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tumour", "Patient", "Tumour+Patient", "Source", "Source+Tumour", "All" }));
        tableChooserComboBox.setToolTipText(resourceMap.getString("tableChooserComboBox.toolTipText")); // NOI18N
        tableChooserComboBox.setName("tableChooserComboBox"); // NOI18N
        tableChooserComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableChooserComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tableChooserPanelLayout = new javax.swing.GroupLayout(tableChooserPanel);
        tableChooserPanel.setLayout(tableChooserPanelLayout);
        tableChooserPanelLayout.setHorizontalGroup(
            tableChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableChooserComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 101, Short.MAX_VALUE)
        );
        tableChooserPanelLayout.setVerticalGroup(
            tableChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableChooserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        sortByChooserPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("sortByChooserPanel.border.title"))); // NOI18N
        sortByChooserPanel.setName("sortByChooserPanel"); // NOI18N

        sortByChooserComboBox.setToolTipText(resourceMap.getString("sortByChooserComboBox.toolTipText")); // NOI18N
        sortByChooserComboBox.setName("sortByChooserComboBox"); // NOI18N
        sortByChooserComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByChooserComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sortByChooserPanelLayout = new javax.swing.GroupLayout(sortByChooserPanel);
        sortByChooserPanel.setLayout(sortByChooserPanelLayout);
        sortByChooserPanelLayout.setHorizontalGroup(
            sortByChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sortByChooserComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 124, Short.MAX_VALUE)
        );
        sortByChooserPanelLayout.setVerticalGroup(
            sortByChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sortByChooserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        refreshTableButton.setAction(actionMap.get("refreshAction")); // NOI18N
        refreshTableButton.setToolTipText(resourceMap.getString("refreshTableButton.toolTipText")); // NOI18N
        refreshTableButton.setName("refreshTableButton"); // NOI18N
        refreshTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rangePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tableChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(recordsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(andLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(refreshTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recordsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sortByChooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tableChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rangePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(andLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshIndexList() {
        DatabaseIndexesListElement[] indexesInTableTemp;
        String tableName = tableChooserComboBox.getSelectedItem().toString();
        // TODO: tidy this for sources
        if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
            LinkedList<DatabaseIndexesListElement> tempIndexesInTable = new LinkedList<DatabaseIndexesListElement>();
            for (DatabaseIndexesListElement indexesInDB1 : indexesInDB) {
                if (indexesInDB1.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME) || indexesInDB1.getDatabaseTableName().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                    tempIndexesInTable.add(indexesInDB1);
                }
            }
            indexesInTableTemp = new DatabaseIndexesListElement[tempIndexesInTable.size()];
            for (int i = 0; i
                    < indexesInTableTemp.length; i++) {
                indexesInTableTemp[i] = tempIndexesInTable.get(i);
            }
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            LinkedList<DatabaseIndexesListElement> tempIndexesInTable = new LinkedList<DatabaseIndexesListElement>();
            for (DatabaseIndexesListElement indexesInDB1 : indexesInDB) {
                if (indexesInDB1.getDatabaseTableName().equalsIgnoreCase(Globals.PATIENT_TABLE_NAME) || indexesInDB1.getDatabaseTableName().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                    tempIndexesInTable.add(indexesInDB1);
                }
            }
            indexesInTableTemp = new DatabaseIndexesListElement[tempIndexesInTable.size()];
            for (int i = 0; i
                    < indexesInTableTemp.length; i++) {
                indexesInTableTemp[i] = tempIndexesInTable.get(i);
            }
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            indexesInTableTemp = indexesInDB;
        } else {
            LinkedList<DatabaseIndexesListElement> tempIndexesInTable = new LinkedList<DatabaseIndexesListElement>();
            for (DatabaseIndexesListElement indexesInDB1 : indexesInDB) {
                if (indexesInDB1.getDatabaseTableName().equalsIgnoreCase(tableName)) {
                    tempIndexesInTable.add(indexesInDB1);
                }
            }
            indexesInTableTemp = new DatabaseIndexesListElement[tempIndexesInTable.size()];
            for (int i = 0; i
                    < indexesInTableTemp.length; i++) {
                indexesInTableTemp[i] = tempIndexesInTable.get(i);
            }
        }

        Object tempIndex = rangeComboBox.getSelectedItem();
        String tempStart = rangeStartTextField.getText();
        String tempEnd = rangeEndTextField.getText();

        rangeComboBox.setModel(new DefaultComboBoxModel(getArrayOfIndexedVariables(indexesInTableTemp)));

        if (Tools.findInArray(indexesInTableTemp, tempIndex) > -1) {
            rangeComboBox.setSelectedItem(tempIndex);
            rangeStartTextField.setText(tempStart);
            rangeEndTextField.setText(tempEnd);
        } else {
            rangeStartTextField.setText("");
            rangeEndTextField.setText("");
        }
        rangeEnabled = !(indexesInTableTemp.length == 0);
        rangePanel.setVisible(rangeEnabled);
        andLabel.setVisible(rangeEnabled);
    }

private void refreshTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshTableButtonActionPerformed
}//GEN-LAST:event_refreshTableButtonActionPerformed

private void tableChooserComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableChooserComboBoxActionPerformed
    refreshVariableList();
    refreshIndexList();
    selectedTableChanged(tableChooserComboBox.getSelectedItem().toString());
    filterWizardInternalFrame.setTableName(tableChooserComboBox.getSelectedItem().toString());
    if (actionListener != null) {
        actionListener.actionPerformed(new ActionEvent(this, 0, "tableChanged"));
    }
}//GEN-LAST:event_tableChooserComboBoxActionPerformed

private void sortByChooserComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByChooserComboBoxActionPerformed
    if (actionListener != null) {
        actionListener.actionPerformed(new ActionEvent(this, 0, "sortby"));
    }
}//GEN-LAST:event_sortByChooserComboBoxActionPerformed

private void filterComboBoxMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterComboBoxMousePressed
    if (evt.isPopupTrigger()) {
        // this does not work...
        // Point pt = SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), filterComboBox);
        // JPopupMenu menu = new MyPopUpMenu(comp);
        // menu.show(filterComboBox, pt.x, pt.y);
    }
}//GEN-LAST:event_filterComboBoxMousePressed

private void filterComboBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterComboBoxMouseReleased
    if (evt.isPopupTrigger()) {
        // this does not work...
        // Point pt = SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), filterComboBox);
        // JPopupMenu menu = new MyPopUpMenu(comp);
        // menu.show(filterComboBox, pt.x, pt.y);
    }
}//GEN-LAST:event_filterComboBoxMouseReleased

private void rangeStartTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rangeStartTextFieldMouseReleased
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(rangeStartTextField, evt);
}//GEN-LAST:event_rangeStartTextFieldMouseReleased

private void rangeStartTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rangeStartTextFieldMousePressed
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(rangeStartTextField, evt);
}//GEN-LAST:event_rangeStartTextFieldMousePressed

private void rangeEndTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rangeEndTextFieldMousePressed
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(rangeEndTextField, evt);
}//GEN-LAST:event_rangeEndTextFieldMousePressed

private void rangeEndTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rangeEndTextFieldMouseReleased
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(rangeEndTextField, evt);
}//GEN-LAST:event_rangeEndTextFieldMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel andLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox rangeComboBox;
    private javax.swing.JTextField rangeEndTextField;
    private javax.swing.JPanel rangePanel;
    private javax.swing.JTextField rangeStartTextField;
    private javax.swing.JPanel recordsPanel;
    private javax.swing.JTextField recordsShownField;
    private javax.swing.JLabel recordsShownLabel;
    private javax.swing.JButton refreshTableButton;
    private javax.swing.JComboBox sortByChooserComboBox;
    private javax.swing.JPanel sortByChooserPanel;
    private javax.swing.JComboBox tableChooserComboBox;
    private javax.swing.JPanel tableChooserPanel;
    private javax.swing.JCheckBox useFilterCheckBox;
    private javax.swing.JButton wizardButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Get the details on the range.
     *
     * @return an array of Objects of the following format: [the database index
     * list object, the start, the end]
     */
    public Object[] getRange() {
        Object[] range = new Object[3];
        if (rangeEnabled) {
            range[0] = rangeComboBox.getSelectedItem();
            DatabaseVariablesListElement variable = (DatabaseVariablesListElement) range[0];
            range[1] = rangeStartTextField.getText();
            range[2] = rangeEndTextField.getText();
            String range1 = (String) range[1];
            String range2 = (String) range[2];
            if (range1.length() > 0) {
                range[1] = variable.getSQLqueryFormat(range1);
            }
            if (range2.length() > 0) {
                range[2] = variable.getSQLqueryFormat(range2);
            }
        } else {
            range = null;
        }
        return range;
    }

    /**
     *
     * @return
     */
    public String getFromValue() {
        return rangeStartTextField.getText();
    }

    /**
     *
     * @return
     */
    public String getToValue() {
        return rangeEndTextField.getText();
    }

    /**
     *
     * @return
     */
    public String getIndexName() {
        return rangeComboBox.getSelectedItem().toString();
    }

    /**
     * Initialize the values of the panel.
     *
     */
    public void initValues() {
        filterCollection = new LinkedList<String>();

        filterWizardInternalFrame = new FastFilterInternalFrame();
        filterWizardInternalFrame.setActionListener(this);

        filterWizardInternalFrame.setTableName(tableChooserComboBox.getSelectedItem().toString());
    }

    private void refreshVariableList() {
        String tableName = tableChooserComboBox.getSelectedItem().toString();
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            LinkedList<DatabaseVariablesListElement> variablesInTableList = new LinkedList<DatabaseVariablesListElement>();
            variablesInTableList.addAll(Arrays.asList(tumourVariablesInDB));
            variablesInTableList.addAll(Arrays.asList(patientVariablesInDB));
            variablesInTable = variablesInTableList.toArray(new DatabaseVariablesListElement[0]);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
            LinkedList<DatabaseVariablesListElement> variablesInTableList = new LinkedList<DatabaseVariablesListElement>();
            variablesInTableList.addAll(Arrays.asList(sourceVariablesInDB));
            variablesInTableList.addAll(Arrays.asList(tumourVariablesInDB));
            variablesInTable = variablesInTableList.toArray(new DatabaseVariablesListElement[0]);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            variablesInTable = new DatabaseVariablesListElement[patientVariablesInDB.length];
            variablesInTable = patientVariablesInDB;
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            variablesInTable = new DatabaseVariablesListElement[tumourVariablesInDB.length];
            variablesInTable = tumourVariablesInDB;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            variablesInTable = new DatabaseVariablesListElement[sourceVariablesInDB.length];
            variablesInTable = sourceVariablesInDB;
        } else  { // default to returning all  if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            LinkedList<DatabaseVariablesListElement> variablesInTableList = new LinkedList<DatabaseVariablesListElement>();
            variablesInTableList.addAll(Arrays.asList(patientVariablesInDB));
            variablesInTableList.addAll(Arrays.asList(tumourVariablesInDB));
            variablesInTableList.addAll(Arrays.asList(sourceVariablesInDB));
            variablesInTable = variablesInTableList.toArray(new DatabaseVariablesListElement[0]);
        }
        sortByChooserComboBox.setModel(new DefaultComboBoxModel(variablesInTable));
    }

    /**
     * Refreshes the filter combobox.
     *
     */
    private void refreshFilterComboBox() {
        String[] filters = new String[1];
        if (filterCollection != null && filterCollection.toArray(filters).length > 0) {
            filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(filterCollection.toArray(filters)));
        } else {
            String[] str = new String[1];
            str[0] = "";
            filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(str));
        }
    }

    /**
     * Adds a filter to the filter combobox if this is not already there.
     *
     * @param String the filter to add
     * @return integer specifying the new filters position in the combobox
     */
    private int addFilterToComboBox(String filter) {
        int position = filterCollection.indexOf(filter);
        if (position < 0) {
            filterCollection.add(filter);
            return filterCollection.indexOf(filter);
        } else {
            return position;
        }
    }

    /**
     *
     */
    @Action
    public void refreshAction() {
        int position = -1;
        Object filter = filterComboBox.getSelectedItem();
        if (filter != null) {
            String filterString = filter.toString();
            position = addFilterToComboBox(filterString);
            refreshFilterComboBox();
            filterComboBox.setSelectedIndex(position);
        }
        if (actionListener != null) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "refresh"));
        }
    }

    /**
     * Adds a filter to the filter combobox if this is not already there and
     * sets it as the current active filter.
     *
     * @param filter
     */
    public void setFilter(String filter) {
        int position = addFilterToComboBox(filter);
        refreshFilterComboBox();
        filterComboBox.setSelectedIndex(position);
    }

    /**
     *
     */
    @Action
    public void filterWizardAction() {

        if (filterWizardInternalFrame.getParent() == null) {
            dtp.add(filterWizardInternalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
            filterWizardInternalFrame.setLocation(dtp.getWidth() / 2 - filterWizardInternalFrame.getWidth() / 2, dtp.getHeight() / 2 - filterWizardInternalFrame.getHeight() / 2);
            filterWizardInternalFrame.setVisible(false);
            filterWizardInternalFrame.setActionListener(this);
        }
        if (filterWizardInternalFrame.isVisible()) {
            filterWizardInternalFrame.toFront();
            try {
                filterWizardInternalFrame.setSelected(true);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(RangeFilterPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //<iclt.co>
            if (filterComboBox.getSelectedItem() != null && !StringUtils.isEmpty(String.valueOf(filterComboBox.getSelectedItem())))
                filterWizardInternalFrame.setTextPane(filterComboBox.getSelectedItem().toString()+" AND ");
            else
                filterWizardInternalFrame.setTextPane("");
            //<ictl.co>
            filterWizardInternalFrame.setVisible(true);
        }
    }

    /**
     *
     * @param rec
     */
    public void setRecordsShown(int rec) {
        recordsShownField.setText("" + rec);
    }

    /**
     *
     * @param visible
     */
    public void setSortByVariableShown(boolean visible) {
        sortByChooserPanel.setVisible(visible);
    }

    /**
     *
     * @return
     */
    public String getSortByVariable() {
        DatabaseVariablesListElement dble = (DatabaseVariablesListElement) sortByChooserComboBox.getSelectedItem();
        return dble.getDatabaseVariableName();
    }

    /**
     *
     */
    @Action
    public void refreshTableAction() {
        String filter = filterComboBox.getSelectedItem().toString();
        int position = addFilterToComboBox(filter);
        refreshFilterComboBox();
        filterComboBox.setSelectedIndex(position);
        if (actionListener != null) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "refresh"));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().getClass() == FastFilterInternalFrame.class) {
            setFilter(e.getActionCommand());
            useFilterCheckBox.setSelected(true);
        }
    }

    /**
     *
     * @param actionListener
     */
    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Action
    public void rangeComboboxChanged() {
        rangeStartTextField.setText("");
        rangeEndTextField.setText("");
    }

    public void setTablesToChooseFrom(String[] tables) {
        tableChooserComboBox.setModel(new javax.swing.DefaultComboBoxModel(tables));
    }

    public DatabaseVariablesListElement[] getArrayOfVariablesInSelectedTables() {
        return variablesInTable;
    }

    public void setSelectedTable(String table) {
        tableChooserComboBox.setSelectedItem(table);
        // selectedTableChanged(table);
    }

    private void selectedTableChanged(String table) {
        // set sort by variable
        // System.out.println(table);
        Arrays.sort(variablesInTable);
        if (table.contains(Globals.TUMOUR_TABLE_NAME)) {
            int n = Arrays.binarySearch(variablesInTable, globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString()));
            sortByChooserComboBox.setSelectedItem(variablesInTable[n]);
        } else if (table.contains(Globals.PATIENT_TABLE_NAME)) {
            int n = Arrays.binarySearch(variablesInTable, globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()));
            sortByChooserComboBox.setSelectedItem(variablesInTable[n]);
        } else if (table.contains(Globals.SOURCE_TABLE_NAME)) {
            int n = Arrays.binarySearch(variablesInTable, globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()));
            sortByChooserComboBox.setSelectedItem(variablesInTable[n]);
        }
    }

    public void setTable(String tableName) {
        tableChooserComboBox.setSelectedItem(tableName);
    }
}
