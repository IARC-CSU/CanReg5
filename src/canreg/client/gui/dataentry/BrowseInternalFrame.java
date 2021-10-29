/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2021 International Agency for Research on Cancer
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
 * BrowseInternalFrame.java
 *
 * Created on 07 February 2008, 12:19
 */
package canreg.client.gui.dataentry;

import canreg.client.CanRegClientApp;
import canreg.client.DistributedTableDataSourceClient;
import canreg.client.LocalSettings;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.importers.Import;
import canreg.client.gui.tools.TableColumnAdjuster;
import canreg.client.gui.tools.XTableColumnModel;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.DatabaseFilter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.cachingtableapi.DistributedTableModel;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.CanRegRegistryProxy;
import canreg.server.CanRegServerInterface;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author morten
 */
public class BrowseInternalFrame extends javax.swing.JInternalFrame implements ActionListener {

    private final JDesktopPane dtp;
    private DistributedTableDescription tableDatadescription;
    private DistributedTableDataSourceClient tableDataSource;
    private TableModel tableDataModel;
    private JScrollPane resultScrollPane;
    String sortByVariableName;
    private LocalSettings localSettings;
    private final JTable resultTable = new JTable() {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer,
                int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (isCellSelected(row, column)) {
                c.setBackground(getSelectionBackground());
            } else if (isHighlighted(column)) {
                c.setBackground(Color.yellow);
            } else {
                c.setBackground(getBackground());
            }
            return c;
        }
    };
    private XTableColumnModel tableColumnModel;
    private LinkedList<String> variablesToShow;
    private final GlobalToolBox globalToolBox;
    private final String patientIDlookupVariable;
    private final String patientIDTumourTablelookupVariable;
    private final String patientRecordIDTumourTablelookupVariable;
    private final String tumourIDlookupVariable;
    private final String tumourIDSourceTableLookupVariable;
    private final String sourceIDlookupVariable;
    int patientIDLength;
    int tumourIDLength;
    // private int highlightedColumnNumber = 0;
    private final String patientRecordIDVariable;
    private final CanRegServerInterface server;   
    
    

    public BrowseInternalFrame(JDesktopPane dtp, CanRegServerInterface server) {
        this.dtp = dtp;
        this.server = server;         
        
        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        patientIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        patientIDTumourTablelookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName();
        patientRecordIDTumourTablelookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
        patientRecordIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        tumourIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        tumourIDSourceTableLookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString()).getDatabaseVariableName();
        sourceIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName();
        patientIDLength = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getVariableLength();
        tumourIDLength = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getVariableLength();
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        initComponents();
        initOtherComponents();
        initValues();
        
        if(this.server == null)
            this.holdingOptions.setVisible(false);
        
        pack();
    }
    ///
    // org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, new java.util.List(), jTable1);

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        createNextButton = new javax.swing.JButton();
        editTableRecordButton = new javax.swing.JButton();
        patientNumberTextField = new javax.swing.JTextField();
        editPatientNumberButton = new javax.swing.JButton();
        tumourNumberTextField = new javax.swing.JTextField();
        editTumourNumberButton = new javax.swing.JButton();
        rangeFilterPanel = new canreg.client.gui.components.RangeFilterPanel();
        navigationPanel = new canreg.client.gui.components.NavigationPanel();
        variablesPanel = new canreg.client.gui.components.DisplayVariablesPanel();
        resultPanel = new javax.swing.JPanel();
        holdingOptions = new javax.swing.JPanel();
        selectAllChkBox = new javax.swing.JCheckBox();
        productionBtn = new javax.swing.JButton();
        deleteHoldingBtn = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(BrowseInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(598, 435));
        setName("Form"); // NOI18N
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                browserClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        mainPanel.setName("mainPanel"); // NOI18N

        buttonsPanel.setName("buttonsPanel"); // NOI18N

        createNextButton.setText(resourceMap.getString("createNextButton.text")); // NOI18N
        createNextButton.setEnabled(false);
        createNextButton.setName("createNextButton"); // NOI18N

        editTableRecordButton.setText(resourceMap.getString("editTableRecordButton.text")); // NOI18N
        editTableRecordButton.setName("editTableRecordButton"); // NOI18N

        patientNumberTextField.setText(resourceMap.getString("patientNumberTextField.text")); // NOI18N
        patientNumberTextField.setName("patientNumberTextField"); // NOI18N
        patientNumberTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                patientNumberTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                patientNumberTextFieldMouseReleased(evt);
            }
        });
        patientNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                editPatientIDKeyTyped(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(BrowseInternalFrame.class, this);
        editPatientNumberButton.setAction(actionMap.get("editPatientID")); // NOI18N
        editPatientNumberButton.setText(resourceMap.getString("editPatientNumberButton.text")); // NOI18N
        editPatientNumberButton.setName("editPatientNumberButton"); // NOI18N

        tumourNumberTextField.setName("tumourNumberTextField"); // NOI18N
        tumourNumberTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tumourNumberTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tumourNumberTextFieldMouseReleased(evt);
            }
        });
        tumourNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                editTumourRecordKeyTyped(evt);
            }
        });

        editTumourNumberButton.setAction(actionMap.get("editTumourID")); // NOI18N
        editTumourNumberButton.setText(resourceMap.getString("editTumourNumberButton.text")); // NOI18N
        editTumourNumberButton.setName("editTumourNumberButton"); // NOI18N

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editPatientNumberButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(editTableRecordButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(createNextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(patientNumberTextField, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(editTumourNumberButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tumourNumberTextField)
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addComponent(createNextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editTableRecordButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editPatientNumberButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(patientNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editTumourNumberButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tumourNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        rangeFilterPanel.setName("rangeFilterPanel"); // NOI18N

        navigationPanel.setName("navigationPanel"); // NOI18N

        variablesPanel.setName("variablesPanel"); // NOI18N

        resultPanel.setName("resultPanel"); // NOI18N

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 267, Short.MAX_VALUE)
        );

        holdingOptions.setName("holdingOptions"); // NOI18N

        selectAllChkBox.setText(resourceMap.getString("selectAllChkBox.text")); // NOI18N
        selectAllChkBox.setName("selectAllChkBox"); // NOI18N
        selectAllChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllChkBoxActionPerformed(evt);
            }
        });

        productionBtn.setAction(actionMap.get("productionButtonAction")); // NOI18N
        productionBtn.setText(resourceMap.getString("productionBtn.text")); // NOI18N
        productionBtn.setName("productionBtn"); // NOI18N

        deleteHoldingBtn.setAction(actionMap.get("deleteHoldingDBAction")); // NOI18N
        deleteHoldingBtn.setText(resourceMap.getString("deleteHoldingBtn.text")); // NOI18N
        deleteHoldingBtn.setName("deleteHoldingBtn"); // NOI18N

        javax.swing.GroupLayout holdingOptionsLayout = new javax.swing.GroupLayout(holdingOptions);
        holdingOptions.setLayout(holdingOptionsLayout);
        holdingOptionsLayout.setHorizontalGroup(
            holdingOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(holdingOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectAllChkBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productionBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deleteHoldingBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        holdingOptionsLayout.setVerticalGroup(
            holdingOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, holdingOptionsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(holdingOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectAllChkBox)
                    .addComponent(productionBtn)
                    .addComponent(deleteHoldingBtn))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(rangeFilterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(navigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(variablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(holdingOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(variablesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(navigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangeFilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(holdingOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        rangeFilterPanel.initValues();
        rangeFilterPanel.setDeskTopPane(dtp);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private boolean isHighlighted(int columnNumber) {
        TableColumn column = tableColumnModel.getColumn(columnNumber);
        if (sortByVariableName.equals(column.getHeaderValue())) {
            return true;
        } else {
            return false;
        }
    }

    private void initOtherComponents() {
        editTableRecordButton.setVisible(false);
        createNextButton.setVisible(false);

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
                rowClicked(evt);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                columnTableMousePressed(evt);
            }
        });

    }

    private void initValues() {
        // Last:
        // hook the navigationpanel up to the resulttable
        navigationPanel.setTable(resultTable);
        rangeFilterPanel.setActionListener(this);
        rangeFilterPanel.setTablesToChooseFrom(Globals.DEFAULT_TABLE_CHOOSER_TABLE_LIST);
        rangeFilterPanel.setDatabaseDescription(globalToolBox.getDocument());
        variablesPanel.setDatabaseVariables(CanRegClientApp.getApplication().getGlobalToolBox().getVariables());
        // Task task = refresh();
        // task.run();
        // rangeFilterPanel.setRecordsTotal(tableDataModel.getRowCount());
    }

private void browserClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_browserClosed
    /* Remove this for now since we only allow one browser and we reuse it...
     rangeFilterPanel.close();
     if (tableDatadescription != null) {
     try {
     CanRegClientApp.getApplication().releaseResultSet(tableDatadescription.getResultSetID());
     } catch (SecurityException ex) {
     Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
     } catch (RemoteException ex) {
     Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     */
}//GEN-LAST:event_browserClosed

private void editPatientIDKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editPatientIDKeyTyped
    if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
        editPatientID();
    }
}//GEN-LAST:event_editPatientIDKeyTyped

private void editTumourRecordKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editTumourRecordKeyTyped
    if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
        editTumourID();
    }
}//GEN-LAST:event_editTumourRecordKeyTyped

private void patientNumberTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_patientNumberTextFieldMouseReleased
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(patientNumberTextField, evt);
}//GEN-LAST:event_patientNumberTextFieldMouseReleased

private void patientNumberTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_patientNumberTextFieldMousePressed
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(patientNumberTextField, evt);
}//GEN-LAST:event_patientNumberTextFieldMousePressed

private void tumourNumberTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tumourNumberTextFieldMouseReleased
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(tumourNumberTextField, evt);
}//GEN-LAST:event_tumourNumberTextFieldMouseReleased

private void tumourNumberTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tumourNumberTextFieldMousePressed
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(tumourNumberTextField, evt);
}//GEN-LAST:event_tumourNumberTextFieldMousePressed

    private void selectAllChkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllChkBoxActionPerformed
        if(this.selectAllChkBox.isSelected())
            resultTable.setRowSelectionInterval(0, resultTable.getRowCount() - 1);
        else 
            resultTable.clearSelection();
    }//GEN-LAST:event_selectAllChkBoxActionPerformed

    private void rowClicked(java.awt.event.MouseEvent evt) {
        String referenceTable;
        
        if(evt.getClickCount() == 1) {
            if(resultTable.getSelectedRowCount() != resultTable.getRowCount())
                this.selectAllChkBox.setSelected(false);
            else
                this.selectAllChkBox.setSelected(true);
        } 
        else if(evt.getClickCount() == 2) {
            JTable target = (JTable) evt.getSource();
            int rowNumber = target.getSelectedRow();
            int columnNumber = 0;
            String lookUpVariable;
            if (rangeFilterPanel.getSelectedTable().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)
                    || rangeFilterPanel.getSelectedTable().equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
                lookUpVariable = tumourIDlookupVariable;
                referenceTable = Globals.TUMOUR_TABLE_NAME;
            } else if (rangeFilterPanel.getSelectedTable().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                lookUpVariable = tumourIDSourceTableLookupVariable;
                referenceTable = Globals.TUMOUR_TABLE_NAME;
            } else {
                lookUpVariable = patientIDlookupVariable;
                referenceTable = Globals.PATIENT_TABLE_NAME;
            }
            columnNumber = tableColumnModel.getColumnIndex(canreg.common.Tools.toUpperCaseStandardized(lookUpVariable), false);
            editRecord((String) tableDataModel.getValueAt(rowNumber, columnNumber), referenceTable);
        }
    }

    public void close() {
        rangeFilterPanel.close();
        if (tableDatadescription != null) {
            try {
                CanRegClientApp.getApplication().releaseResultSet(tableDatadescription.getResultSetID(), server);
            } catch (SecurityException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException | SQLException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        this.dispose();
    }

    private void columnTableMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            JTable target = (JTable) evt.getSource();
            int columnNumber = target.getSelectedColumn();

            JPopupMenu jpm = new JPopupMenu("" + columnNumber);
            jpm.add(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("COLUMN ") + tableColumnModel.getColumn(tableColumnModel.getColumnIndexAtX(evt.getX()), true).getHeaderValue());
            jpm.show(target, evt.getX(), evt.getY());
        }
    }

    /**
     *
     * @return
     */
    @Action
    public Task refresh() {
        navigationPanel.goToTopAction();
        resultPanel.setVisible(false);
        return new RefreshTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
    }

    public JComponent getMainPanel() {
        return this.mainPanel;
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
            variablesToShow = variablesPanel.getVariablesToShow(tableName);
            filter.setFilterString(rangeFilterPanel.getFilter().trim());
            filter.setSortByVariable(rangeFilterPanel.getSortByVariable());
            filter.setRange(rangeFilterPanel.getRange());
            sortByVariableName = canreg.common.Tools.toUpperCaseStandardized(
                    rangeFilterPanel.getSortByVariable());
            // setProgress(0, 0, 4);
            setMessage(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("INITIATING QUERY..."));
            // setProgress(1, 0, 4);
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
        }

        @Override
        protected Object doInBackground() {
            String result = "OK";
            try {
                newTableDatadescription = 
                        canreg.client.CanRegClientApp.getApplication().getDistributedTableDescription(filter, tableName, server);
            } catch (UnknownTableException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Unknown table " + ex.getMessage();
            } catch (DistributedTableDescriptionException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not valid " + ex.getMessage();
            } catch (SQLException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not valid " + ex.getMessage();
            } catch (RemoteException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Remote exception ";
            } catch (SecurityException ex) {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Security exception ";
                // } catch (InterruptedException ignore) {
                //    result = "Ignore";
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
                        CanRegClientApp.getApplication().releaseResultSet(tableDatadescription.getResultSetID(), server);
                    } catch (SecurityException | RemoteException | SQLException ex) {
                        Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    tableDataSource = null;
                }

                tableDatadescription = newTableDatadescription;
                // highlightedColumnNumber = Tools.findInArray(tableDatadescription.getColumnNames(), sortByVariableName);

                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());

                if (tableDatadescription != null) {
                    try {
                        tableDataSource = new DistributedTableDataSourceClient(tableDatadescription, server);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        new TechnicalError().errorDialog();
                    }
                    Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
                }

                if (tableDataSource != null) {
                    try {
                        tableDataModel = new DistributedTableModel(tableDataSource);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // tableDataModel = new PagingTableModel(tableDataSource);
                    Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
                    // setProgress(2, 0, 4);
                }

                setMessage(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("STARTING A NEW TRANSACTION..."));

                rangeFilterPanel.setRecordsShown(tableDataModel.getRowCount());

                // setProgress(3, 0, 4);

                setMessage(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("FETCHING DATA..."));
                resultTable.setColumnSelectionAllowed(false);
                resultTable.setModel(tableDataModel);
                tableColumnModel = new XTableColumnModel();
                resultTable.setColumnModel(tableColumnModel);
                resultTable.createDefaultColumnsFromModel();
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());

                // setProgress(4, 0, 4);
                setMessage("Finished");

                updateVariablesShown();

                resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                TableColumnAdjuster tca = new TableColumnAdjuster(resultTable);
                tca.setColumnDataIncluded(false);
                tca.setOnlyAdjustLarger(false);
                tca.adjustColumns();
                resultPanel.setVisible(true);
            } else if (result.toString().startsWith("Not valid")) {
                JOptionPane.showInternalMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("NOT_A_VALID_FILTER.") + "\n"
                        + result.toString().substring("Not valid".length()),
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } else {
                Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, result);
            }
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            rangeFilterPanel.setRefreshButtonEnabled(true);
        }
    }

    private void hideSystemVariables() {
        TableColumn column;
        try {
            column = tableColumnModel.getColumnByModelIndex(
                    tableColumnModel.getColumnIndex("ID"));
            tableColumnModel.setColumnVisible(column, false);
        } catch (IllegalArgumentException iae) {
            //OK
        }
        try {
            column = tableColumnModel.getColumnByModelIndex(
                    tableColumnModel.getColumnIndex("NEXT_RECORD_DB_ID"));
            tableColumnModel.setColumnVisible(column, false);
        } catch (IllegalArgumentException iae) {
            //OK
        }
        try {
            column = tableColumnModel.getColumnByModelIndex(
                    tableColumnModel.getColumnIndex("LAST_RECORD_DB_ID"));
            tableColumnModel.setColumnVisible(column, false);
        } catch (IllegalArgumentException iae) {
            //OK
        }
    }

    private void updateVariablesShown() {
        String tableName = rangeFilterPanel.getSelectedTable();
        variablesToShow = variablesPanel.getVariablesToShow(tableName);
        // first set all invisible
        if (variablesToShow != null) {
            Enumeration<TableColumn> tcs = tableColumnModel.getColumns(false);
            while (tcs.hasMoreElements()) {
                TableColumn column = tcs.nextElement();
                if (column != null) {
                    tableColumnModel.setColumnVisible(column, variablesToShow.contains(column.getHeaderValue().toString()));
                }
            }
        }
    }

    /**
     *
     */
    @Action
    public void editPatientID() {
        String idString = patientNumberTextField.getText().trim();
        if (idString.trim().length() != patientIDLength) {
            JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("PATIENT ID SHOULD BE ") + patientIDLength + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString(" CHARACTERS LONG."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        editPatientID(idString);
    }

    /**
     *
     * @param idString
     */
    public void editPatientID(String idString) {  
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        setCursor(hourglassCursor);

        canreg.client.gui.dataentry2.RecordEditor recordEditor = null;
        String dataEntryVersion = localSettings.getProperty(LocalSettings.DATA_ENTRY_VERSION_KEY);
        if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW))
            recordEditor = new canreg.client.gui.dataentry2.RecordEditorMainFrame(dtp, this.server, this);
        else 
            recordEditor = new RecordEditor(dtp, this.server, this);
        
        recordEditor.setGlobalToolBox(CanRegClientApp.getApplication().getGlobalToolBox());
        recordEditor.setDictionary(CanRegClientApp.getApplication().getDictionary());

        try {
            Patient[] patients = CanRegClientApp.getApplication().getPatientsByPatientID(idString, false, server);

            if (patients.length < 1) {
                /*
                 If we don't get any records with that ID - we propose to create one.
                 */
                int answer = JOptionPane.showInternalConfirmDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("NO_PATIENT_WITH_THAT_ID_FOUND,_DO_YOU_WANT_TO_CREATE_ONE?"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("PATIENT_ID_NOT_FOUND"), JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    Patient patient = new Patient();
                    patient.setVariable(patientIDlookupVariable, idString);
                    CanRegClientApp.getApplication().saveRecord(patient, server);
                    patients = CanRegClientApp.getApplication().getPatientsByPatientID(idString, false, server);
                } else {
                    setCursor(normalCursor);
                    return;
                }
            }
            
            TreeSet<DatabaseRecord> set = new TreeSet<DatabaseRecord>(new Comparator<DatabaseRecord>() {
                @Override
                public int compare(DatabaseRecord o1, DatabaseRecord o2) {
                    return (o1.getVariable(tumourIDlookupVariable).toString().compareTo(o2.getVariable(tumourIDlookupVariable).toString()));
                }
            });
            // Get all the tumour records for all the patient records...
            for (Patient p : patients) {
                recordEditor.addRecord(p);
                DatabaseRecord[] tumourRecords = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID(idString, true, server);
                for (DatabaseRecord rec : tumourRecords) {
                    // store them in a set, so we don't show them several times
                    if (rec != null) {
                        set.add(rec);
                    }
                }
            }

            if (set.isEmpty()) {
                // add a tumour record as well
                if (patients.length > 0) {
                    Tumour tumour = new Tumour();
                    tumour.setVariable(patientIDTumourTablelookupVariable, idString);
                    tumour.setVariable(patientRecordIDTumourTablelookupVariable, patients[0].getVariable(patientRecordIDVariable));
                    CanRegClientApp.getApplication().saveRecord(tumour, server);
                    set.add(tumour);
                } else {
                    Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, "Patient record not saved properly?");
                }
            }

            for (DatabaseRecord rec : set) {
                // store them in a map, so we don't show them several times
                recordEditor.addRecord(rec);
            }
            // make sure the records are locked...
            CanRegClientApp.getApplication().getPatientsByPatientID(idString, true, server);
            CanRegClientView.showAndPositionInternalFrame(dtp, (JInternalFrame)recordEditor);
            CanRegClientView.maximizeHeight(dtp, (JInternalFrame)recordEditor);
        } catch (RecordLockedException ex) {
            JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("RECORD IS ALREADY BEING EDITED..."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.INFO, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            setCursor(normalCursor);
        }
    }
    

    @Action
    public void editTumourID() {
        String idString = tumourNumberTextField.getText().trim();
        if (idString.length() != tumourIDLength) {
            JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("TUMOUR ID SHOULD BE ") + tumourIDLength + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString(" CHARACTERS LONG."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        editTumourID(idString);
    }

    public void editTumourID(String idString) {
        canreg.client.gui.dataentry2.RecordEditor recordEditor = null;
        String dataEntryVersion = localSettings.getProperty(LocalSettings.DATA_ENTRY_VERSION_KEY);
        if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW))
            recordEditor = new canreg.client.gui.dataentry2.RecordEditorMainFrame(dtp, server, this);
        else 
            recordEditor = new RecordEditor(dtp, server, this);
        
        recordEditor.setGlobalToolBox(CanRegClientApp.getApplication().getGlobalToolBox());
        recordEditor.setDictionary(CanRegClientApp.getApplication().getDictionary());
        DatabaseRecord record = null;
        DatabaseFilter filter = new DatabaseFilter();

        filter.setFilterString(tumourIDlookupVariable + " ='" + idString + "'");

        try {
            DistributedTableDescription distributedTableDescription = 
                    CanRegClientApp.getApplication().getDistributedTableDescription(filter, Globals.TUMOUR_TABLE_NAME, server);
            int numberOfRecords = distributedTableDescription.getRowCount();
            if (numberOfRecords == 0) {
                JOptionPane.showMessageDialog(rootPane,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("TUMOUR_RECORD_NOT_FOUND..."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } else {
//<<<<<<< HEAD
//                rows = CanRegClientApp.getApplication()
//                        .retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords, server);
//                CanRegClientApp.getApplication().releaseResultSet(distributedTableDescription.getResultSetID(), server);
//=======
                Object[][] rows = CanRegClientApp.getApplication().retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords, server);
                CanRegClientApp.getApplication().releaseResultSet(distributedTableDescription.getResultSetID(), server);
//>>>>>>> release/R44
                String[] columnNames = distributedTableDescription.getColumnNames();
                int ids[] = new int[numberOfRecords];
                boolean found = false;
                int idColumnNumber = 0;
                while (!found && idColumnNumber < columnNames.length) {
                    found = columnNames[idColumnNumber++].equalsIgnoreCase(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                }
                if (found) {
                    idColumnNumber--;
                    for (int j = 0; j < numberOfRecords; j++) {
                        ids[j] = (Integer) rows[j][idColumnNumber];
                        record = CanRegClientApp.getApplication().getRecord(ids[j], Globals.TUMOUR_TABLE_NAME, false, server);
                        editPatientID((String) record.getVariable(patientIDTumourTablelookupVariable));
                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("VARIABLE_NOT_FOUND..."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (RecordLockedException ex) {
            JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("RECORD IS ALREADY BEING EDITED..."), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        } 
    }


    public void editRecord(String idString, String tableName) {
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            editTumourID(idString);
        } else {
            editPatientID(idString);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton createNextButton;
    private javax.swing.JButton deleteHoldingBtn;
    private javax.swing.JButton editPatientNumberButton;
    private javax.swing.JButton editTableRecordButton;
    private javax.swing.JButton editTumourNumberButton;
    private javax.swing.JPanel holdingOptions;
    private javax.swing.JPanel mainPanel;
    private canreg.client.gui.components.NavigationPanel navigationPanel;
    private javax.swing.JTextField patientNumberTextField;
    private javax.swing.JButton productionBtn;
    private canreg.client.gui.components.RangeFilterPanel rangeFilterPanel;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JCheckBox selectAllChkBox;
    private javax.swing.JTextField tumourNumberTextField;
    private canreg.client.gui.components.DisplayVariablesPanel variablesPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("refresh".equalsIgnoreCase(e.getActionCommand())) {
            Task refreshTask = refresh();
            refreshTask.execute();
        }
    }
    
    public void setFilterField(String filter) {
        rangeFilterPanel.setFilterActive(!filter.trim().isEmpty());
        rangeFilterPanel.setFilter(filter);
    }
    
    public void setTable(String tableName) {
        rangeFilterPanel.setTable(tableName);
    }

    @Action
    public boolean productionButtonAction() {
        int numberOfRecords = resultTable.getSelectedRowCount();
        ResourceBundle importerResourceMap = java.util.ResourceBundle.getBundle("canreg/client/gui/importers/resources/ImportFilesView");
        String[] options = {importerResourceMap.getString("rejectRadioButton.text"), 
                            importerResourceMap.getString("updateRadioButton.text"), 
                            importerResourceMap.getString("overwriteRadioButton.text")};
        ResourceBundle browseResourceMap = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame");
        if(numberOfRecords > 0) {
            int result = JOptionPane.showOptionDialog(null, 
                                         browseResourceMap.getString("YOU ARE IMPORTING ") + " " + numberOfRecords + 
                                                 " " + browseResourceMap.getString("RECORD") + ". " + 
                                                 browseResourceMap.getString("DISCREPANCIES"),
                                         browseResourceMap.getString("IMPORTING RECORDS INTO PRODUCTION "),
                                         JOptionPane.DEFAULT_OPTION, 
                                         JOptionPane.INFORMATION_MESSAGE, null, 
                                         options, options[1]);
            Task refreshTask = null;
            
            if(result > -1) {
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                
                try {
                    setCursor(hourglassCursor);
                    Writer reportWriter = new BufferedWriter(new OutputStreamWriter(System.out));
                    
                    int successfullyDeletedRecord = 0;
                    int errorDeletedRecord = 0;
                    
                    for(Integer rowNumber : resultTable.getSelectedRows()) {
                        String patientRecordIDVariable = globalToolBox
                                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                                .getDatabaseVariableName();
                        int columnNumber = tableColumnModel.getColumnIndex(canreg.common.Tools.toUpperCaseStandardized(patientRecordIDVariable), false);
                        String holdingPatientRecordID = (String) tableDataModel.getValueAt(rowNumber, columnNumber);
                        Patient patientToImport = CanRegClientApp.getApplication().getPatientRecord(holdingPatientRecordID, false, server);
                        int productionPRID = Import.importPatient(CanRegClientApp.getApplication().getServer(), result, 
                                holdingPatientRecordID, patientToImport, reportWriter, false, false, true);

                        Tumour[] tumourRecords = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientRecordID(holdingPatientRecordID, false, server);
                        for(Tumour tumourToImport : tumourRecords) {
                            String tumourID = tumourToImport.getVariableAsString(tumourIDlookupVariable);
                            
                            if(productionPRID != -1) {
                                Patient productionPatient = (Patient) CanRegClientApp.getApplication().getServer()
                                        .getRecord(productionPRID, Globals.PATIENT_TABLE_NAME, false, server.hashCode());
                                holdingPatientRecordID = productionPatient.getVariableAsString(patientRecordIDVariable);
                                
                                String tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
                                String tumourPatientRecordIdVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName();
                                tumourToImport.setVariable(tumourIDVariableName, "");
                                tumourToImport.setVariable(tumourPatientRecordIdVariableName, holdingPatientRecordID);
                                tumourID = "";
                            }
                            
                            Import.importTumour(CanRegClientApp.getApplication().getServer(), result, tumourID, 
                                    holdingPatientRecordID, tumourToImport, null, reportWriter, false, false, true);
                            
                            //NOT NECESSARY, sources are already taken care of in CanRegDAO.editRecord() when
                            //the record being updated is a Tumour (near the end of the method it deletes the sources
                            //and saves them all from scratch).
    //                            for(Source sourceToImport : tumourToImport.getSources()) {
    //                                Import.importSource(CanRegClientApp.getApplication().getServer(), result, 
    //                                        sourceToImport.getVariableAsString(sourceIDlookupVariable),
    //                                        sourceIDlookupVariable, sourceToImport, tumourID, reportWriter, false, false, true);
    //                            }
                    
                            if(deleteRecord(tumourToImport))
                                successfullyDeletedRecord++;
                            else
                                errorDeletedRecord++;
                        }
                        
                        if(deleteRecord(patientToImport)) 
                            successfullyDeletedRecord++;                        
                        else
                            errorDeletedRecord++;
                    }

                    if(errorDeletedRecord == 0) 
                        JOptionPane.showMessageDialog(null, browseResourceMap.getString("SUCCESS"), 
                                successfullyDeletedRecord + " " + browseResourceMap.getString("SUCCESS MESSAGE "), JOptionPane.INFORMATION_MESSAGE);
                    else 
                        JOptionPane.showMessageDialog(null, browseResourceMap.getString("SUCCESS"), 
                                successfullyDeletedRecord + " " + browseResourceMap.getString("SUCCESS MESSAGE ") + "\n" +
                                successfullyDeletedRecord + " " + browseResourceMap.getString("ERROR WITH SOME RECORDS MESSAGE "), JOptionPane.INFORMATION_MESSAGE);
                    
                    refreshTask = refresh();
                    refreshTask.execute();
                    
                    return true;
                } catch(Exception ex) {
                    Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, browseResourceMap.getString("ERROR IMPORTING "), "ERROR", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if(refreshTask == null)
                        setCursor(normalCursor);
                }
            }
        }
        return false;
    }
    
    private boolean deleteRecord(DatabaseRecord record) {
        boolean success = false;
        int id = -1;
        String tableName = null;
        if (record instanceof Patient) {
            Object idObject = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) 
                id = (Integer) idObject;            
            tableName = Globals.PATIENT_TABLE_NAME;
        } else if (record instanceof Tumour) {
            // delete sources first.
            Tumour tumour = (Tumour) record;
            for (Source source : tumour.getSources()) 
                deleteRecord(source);
            
            Object idObject = record.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) 
                id = (Integer) idObject;            
            tableName = Globals.TUMOUR_TABLE_NAME;
        } else if (record instanceof Source) {
            Object idObject = record.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) 
                id = (Integer) idObject;            
            tableName = Globals.SOURCE_TABLE_NAME;
        }
        if (id >= 0) {
            try {
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, tableName, server);
                success = canreg.client.CanRegClientApp.getApplication().deleteRecord(id, tableName, server);
            } catch (Exception ex) {
                Logger.getLogger(canreg.client.gui.dataentry.BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        return success;
    }

    @Action
    public void deleteHoldingDBAction() {
        ResourceBundle browseResourceMap = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame");
        int result = JOptionPane.showConfirmDialog(null, browseResourceMap.getString("ARE YOU SURE "),
                browseResourceMap.getString("CONFIRM"), JOptionPane.YES_NO_OPTION);
        if(result == JOptionPane.OK_OPTION) 
            deleteHoldingDB();
    }
    
    private void deleteHoldingDB() {
        try {
            this.close();
            server.deleteHoldingDB(((CanRegRegistryProxy)server).getHoldingRegistryCode());
            this.dispose();
            canreg.client.CanRegClientApp.getApplication().refreshHoldingDBsList();
        } catch(Exception ex) {
            Logger.getLogger(canreg.client.gui.dataentry.BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
