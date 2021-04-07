/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2021  International Agency for Research on Cancer
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
 * PersonSearchFrame.java
 *
 * Created on 28 February 2008, 10:16
 */
package canreg.client.gui.management;

import canreg.client.gui.*;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.gui.dataentry.BrowseInternalFrame;
import canreg.client.gui.dataentry.RecordEditor;
import canreg.common.DatabaseFilter;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.qualitycontrol.DefaultPersonSearch;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Vector; // Obsolete collection but our GUI needs this for the results browser...
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author morten
 */
public class PersonSearchFrame extends javax.swing.JInternalFrame implements ActionListener {

    private final PersonSearchListener listener;
    private Task duplicateSearchTask;
    private JDesktopPane desktopPane;
    private final Document doc;
    private String personSearchHandlerID;
    int recordsTested;
    int matchesFound;
    private final DefaultTableModel resultTableModel;
    boolean personSearcherRunning = false;
    private GlobalToolBox globalToolBox;
    private String patientIDlookupVariable;
    private String patientIDTumourTablelookupVariable;
    private String tumourIDlookupVariable;
    private LocalSettings localSettings;

    /**
     * Creates new form PersonSearchFrame
     *
     * @param desktopPane
     */
    public PersonSearchFrame(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;

        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        patientIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        patientIDTumourTablelookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName();
        tumourIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();

        localSettings = CanRegClientApp.getApplication().getLocalSettings();

        resultTableModel = new DefaultTableModel(new String[]{java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/PersonSearchFrame").getString("PATIENT A RECORD ID"), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/PersonSearchFrame").getString("PATIENT B RECORD ID"), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/PersonSearchFrame").getString("MATCH %")}, 0) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        // resultTableModel.setColumnIdentifiers(new Object[]{new String(),new String(),(float) 0, true});
        initComponents();
        listener = new PersonSearchListener();
        listener.setActionListener(this);
        doc = canreg.client.CanRegClientApp.getApplication().getDatabseDescription();
        personSearchVariablesPanel1.setDoc(doc);
        // personSearchVariablesPanel1.setSearcher(searcher);
        resultTable.getTableHeader().setReorderingAllowed(false);

        addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                int option = JOptionPane.NO_OPTION;
                if (personSearcherRunning) {
                    option = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/PersonSearchFrame").getString("REALLY CLOSE?") + "\n" + java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/PersonSearchFrame").getString("THE PERSON SEARCH UNDER WAY WILL BE INTERUPTED."), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/PersonSearchFrame").getString("REALLY CLOSE?"), JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        interruptDuplicateSearchAction();
                        close();
                    }
                } else {
                    close();
                }
            }
        });

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        rangeStartTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        rangeEndTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        performButton = new javax.swing.JButton();
        interruptButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        recordsInRangeField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        recordsTestedTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        matchesFoundTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        personSearchVariablesPanel1 = new canreg.client.gui.management.PersonSearchVariablesPanel();
        resultPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        rangeStartTextField.setName("rangeStartTextField"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        rangeEndTextField.setName("rangeEndTextField"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(rangeStartTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(rangeEndTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
                .addContainerGap(300, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(rangeStartTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(rangeEndTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(PersonSearchFrame.class, this);
        performButton.setAction(actionMap.get("performDuplicateSearch")); // NOI18N
        performButton.setText(resourceMap.getString("performButton.text")); // NOI18N
        performButton.setName("performButton"); // NOI18N

        interruptButton.setAction(actionMap.get("interruptDuplicateSearchAction")); // NOI18N
        interruptButton.setText(resourceMap.getString("interruptButton.text")); // NOI18N
        interruptButton.setName("interruptButton"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        recordsInRangeField.setEditable(false);
        recordsInRangeField.setName("recordsInRangeField"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        recordsTestedTextField.setEditable(false);
        recordsTestedTextField.setName("recordsTestedTextField"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        matchesFoundTextField.setEditable(false);
        matchesFoundTextField.setName("matchesFoundTextField"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(recordsInRangeField, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(recordsTestedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(matchesFoundTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)))
                .addContainerGap(240, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(recordsInRangeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(recordsTestedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(matchesFoundTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(interruptButton, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(performButton, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(performButton)
                    .addComponent(interruptButton))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        personSearchVariablesPanel1.setName("personSearchVariablesPanel1"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(personSearchVariablesPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(personSearchVariablesPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        resultPanel.setName("resultPanel"); // NOI18N

        resultScrollPane.setName("resultScrollPane"); // NOI18N

        resultTable.setAutoCreateRowSorter(true);
        resultTable.setModel(resultTableModel);
        resultTable.setCellSelectionEnabled(true);
        resultTable.setName("resultTable"); // NOI18N
        resultScrollPane.setViewportView(resultTable);

        jButton1.setAction(actionMap.get("writeResults")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );

        jTabbedPane1.addTab("Results", resultPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     *
     * @return
     */
    @Action
    public Task performDuplicateSearch() {
        while (resultTableModel.getRowCount() > 0) {
            resultTableModel.removeRow(0);
        }
        duplicateSearchTask = new PerformDuplicateSearchTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
        performButton.setEnabled(false);
        interruptButton.setEnabled(true);
        DefaultPersonSearch searcher = personSearchVariablesPanel1.getSearcher();
        // TODO: File selector?
        try {
            String rangeStart = rangeStartTextField.getText();
            String rangeEnd = rangeEndTextField.getText();
            if (rangeStart != null && rangeStart.trim().length() > 0) {
                rangeStart = ("'" + rangeStart + "'");
            }
            if (rangeEnd != null && rangeEnd.trim().length() > 0) {
                rangeEnd = ("'" + rangeEnd + "'");
            }
            personSearchHandlerID = CanRegClientApp.getApplication().initiateGlobalDuplicateSearch(
                    searcher, rangeStart, rangeEnd);
        } catch (SecurityException | RemoteException ex) {
            Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return duplicateSearchTask;
    }

    private class PerformDuplicateSearchTask extends org.jdesktop.application.Task<Object, Void> {

        PerformDuplicateSearchTask(org.jdesktop.application.Application app) {
            super(app);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
        }

        @Override
        protected Object doInBackground() {
            Map<String, Map<String, Float>> result;
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            personSearcherRunning = true;
            recordsTested = 0;
            matchesFound = 0;
            recordsTestedTextField.setText(recordsTested + "");
            matchesFoundTextField.setText(matchesFound + "");
            try {
                result = CanRegClientApp.getApplication().nextStepGlobalPersonSearch(personSearchHandlerID);
                if (result != null) {
                    recordsTested += Globals.GLOBAL_PERSON_SEARCH_STEP_SIZE;
                }
                while (result != null) {
                    recordsTestedTextField.setText(recordsTested + "");
                    if (result.size() > 0) {
                        for (String patientRecordNumberA : result.keySet()) {
                            Map<String, Float> map = result.get(patientRecordNumberA);
                            matchesFound += map.size();
                            Patient patientA = CanRegClientApp.getApplication().getPatientRecord(patientRecordNumberA, false);
                            String patientNumberA = (String) patientA.getVariable(patientIDlookupVariable).toString();
                            for (String patientRecordNumberB : map.keySet()) {
                                Patient patientB = CanRegClientApp.getApplication().getPatientRecord(patientRecordNumberB, false);
                                String patientNumberB = (String) patientB.getVariable(patientIDlookupVariable).toString();
                                resultTableModel.addRow(new Object[]{patientNumberA, patientNumberB, map.get(patientRecordNumberB)});
                            }
                        }
                        matchesFoundTextField.setText(matchesFound + "");

                        // TODO: Write to file
                    }
                    result = CanRegClientApp.getApplication().nextStepGlobalPersonSearch(personSearchHandlerID);
                    if (result != null) {
                        recordsTested += Globals.GLOBAL_PERSON_SEARCH_STEP_SIZE;
                    }
                }
            } catch (SecurityException | RemoteException | DistributedTableDescriptionException | RecordLockedException | SQLException | UnknownTableException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            performButton.setEnabled(true);
            interruptButton.setEnabled(false);
            personSearcherRunning = false;
            // TODO: Display results
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton interruptButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField matchesFoundTextField;
    private javax.swing.JButton performButton;
    private canreg.client.gui.management.PersonSearchVariablesPanel personSearchVariablesPanel1;
    private javax.swing.JTextField rangeEndTextField;
    private javax.swing.JTextField rangeStartTextField;
    private javax.swing.JTextField recordsInRangeField;
    private javax.swing.JTextField recordsTestedTextField;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JTable resultTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.INFO, command);
        if (command.startsWith("range")) {
            recordsInRangeField.setText(command.substring(6));
        }
    }

    /**
     *
     */
    @Action
    public void interruptDuplicateSearchAction() {
        if (duplicateSearchTask != null) {
            try {
                CanRegClientApp.getApplication().interuptGlobalPersonSearch(personSearchHandlerID);
                // boolean cancelled = duplicateSearchTask.cancel(true);
                // if (!cancelled) {
                //     JOptionPane.showConfirmDialog(this, "Task can not be interupted...");
                // } else {
                //    duplicateSearchTask = null;
                // }
                // personSearcherRunning = false;
                // performButton.setEnabled(true);
                // interruptButton.setEnabled(false);
            } catch (SecurityException | RemoteException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    // Returns the TableColumn associated with the specified column
    // index in the model

    private TableColumn findTableColumn(JTable table, int columnModelIndex) {
        Enumeration enume = table.getColumnModel().getColumns();
        for (; enume.hasMoreElements();) {
            TableColumn col = (TableColumn) enume.nextElement();
            if (col.getModelIndex() == columnModelIndex) {
                return col;
            }
        }
        return null;
    }

    /**
     *
     */
    private void close() {
        this.dispose();
    }

    private void rowClicked(java.awt.event.MouseEvent evt) {
        String referenceTable;

        if (evt.getClickCount() == 2) {
            JTable target = (JTable) evt.getSource();
            int rowNumber = target.getSelectedRow();
            int columnNumber = target.getSelectedColumn();
            if (columnNumber == 0 || columnNumber == 1) {
                // TableModel model = target.getModel();
                ComparePatientsInternalFrame cpif = new ComparePatientsInternalFrame(desktopPane);
                try {
//                  patient = CanRegClientApp.getApplication().getPatientRecord("" + model.getValueAt(rowNumber, columnNumber), false);
                    String patient1ID = (String) target.getValueAt(rowNumber, 0);
                    Patient patient1 = CanRegClientApp.getApplication().getPatientRecordsByID(patient1ID, false)[0];
                    cpif.addMainRecordSet(patient1, null);
                    // find all results with same as ID1
                    for (int row = 0; row < resultTableModel.getRowCount(); row++) {
                        if (patient1ID.equals(target.getValueAt(row, 0))) {
                            String patient2ID = (String) target.getValueAt(row, 1);
                            Patient patient2 = CanRegClientApp.getApplication().getPatientRecordsByID(patient2ID, false)[0];
                            cpif.addRecordSet(patient2, null, (Float) target.getValueAt(row, 2));
                        }
                    }
                    CanRegClientView.showAndPositionInternalFrame(desktopPane, cpif);
                } catch (SQLException | RecordLockedException | UnknownTableException | DistributedTableDescriptionException | RemoteException | SecurityException ex) {
                    Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void rowClickedOld(java.awt.event.MouseEvent evt) {
        String referenceTable;

        if (evt.getClickCount() == 2) {
            JTable target = (JTable) evt.getSource();
            int rowNumber = target.getSelectedRow();
            int columnNumber = target.getSelectedColumn();
            if (columnNumber == 0 || columnNumber == 1) {
                // TableModel model = target.getModel();
                Patient patient;
                try {
//                  patient = CanRegClientApp.getApplication().getPatientRecord("" + model.getValueAt(rowNumber, columnNumber), false);
                    editPatientID("" + target.getValueAt(rowNumber, columnNumber));
                } catch (SecurityException ex) {
                    Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     *
     * @param idString
     */
    public void editPatientID(String idString) {
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor normalCursor = getCursor();
        setCursor(hourglassCursor);

        String tableName = Globals.PATIENT_TABLE_NAME;

        canreg.client.gui.dataentry2.RecordEditor recordEditor = null;
        String dataEntryVersion = localSettings.getProperty(LocalSettings.DATA_ENTRY_VERSION_KEY);
        if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW)) {
            recordEditor = new canreg.client.gui.dataentry2.RecordEditorMainFrame(desktopPane);
        } else {
            recordEditor = new RecordEditor(desktopPane);
        }

        recordEditor.setGlobalToolBox(CanRegClientApp.getApplication().getGlobalToolBox());
        recordEditor.setDictionary(CanRegClientApp.getApplication().getDictionary());
        DatabaseRecord record = null;
        DatabaseFilter filter = new DatabaseFilter();
        filter.setFilterString(patientIDlookupVariable + " = '" + idString + "' ");
        DistributedTableDescription distributedTableDescription;
        Object[][] rows;
        DatabaseRecord[] tumourRecords;

        try {
            distributedTableDescription = CanRegClientApp.getApplication().getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
            int numberOfRecords = distributedTableDescription.getRowCount();

            if (numberOfRecords == 0) {
                /*
                If we don't get any records with that ID - we propose to create one.
                 */
                int answer = JOptionPane.showInternalConfirmDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("NO_PATIENT_WITH_THAT_ID_FOUND,_DO_YOU_WANT_TO_CREATE_ONE?"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("PATIENT_ID_NOT_FOUND"), JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    record = new Patient();
                    record.setVariable(patientIDlookupVariable, idString);
                    CanRegClientApp.getApplication().saveRecord(record);
                    distributedTableDescription = CanRegClientApp.getApplication().getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
                    numberOfRecords = distributedTableDescription.getRowCount();
                } else {
                    setCursor(normalCursor);
                    return;
                }
            }

            rows = CanRegClientApp.getApplication().retrieveRows(distributedTableDescription.getResultSetID(), 0, numberOfRecords);
            CanRegClientApp.getApplication().releaseResultSet(distributedTableDescription.getResultSetID());
            String[] columnNames = distributedTableDescription.getColumnNames();
            int ids[] = new int[numberOfRecords];
            boolean found = false;
            int idColumnNumber = 0;
            // First get the patient IDs matching the tumour
            while (!found && idColumnNumber < columnNames.length) {
                found = columnNames[idColumnNumber++].equalsIgnoreCase(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
            }
            if (found) {
                idColumnNumber--;
                TreeSet<DatabaseRecord> set = new TreeSet<>(new Comparator<DatabaseRecord>() {

                    @Override
                    public int compare(DatabaseRecord o1, DatabaseRecord o2) {
                        return (o1.getVariable(tumourIDlookupVariable).toString().compareTo(o2.getVariable(tumourIDlookupVariable).toString()));
                    }
                });
                // Get all the tumour records for all the patient records...
                for (int j = 0; j < numberOfRecords; j++) {
                    ids[j] = (Integer) rows[j][idColumnNumber];
                    record = CanRegClientApp.getApplication().getRecord(ids[j], Globals.PATIENT_TABLE_NAME, true);
                    recordEditor.addRecord(record);

                    tumourRecords = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID(idString, true);
                    for (DatabaseRecord rec : tumourRecords) {
                        // store them in a set, so we don't show them several times
                        if (rec != null) {
                            set.add(rec);
                        }
                    }
                }
                if (set.isEmpty()) {
                    Tumour rec = new Tumour();
                    rec.setVariable(patientIDTumourTablelookupVariable, idString);
                    set.add(rec);
                }

                for (DatabaseRecord rec : set) {
                    // store them in a map, so we don't show them several times
                    recordEditor.addRecord(rec);
                }
                CanRegClientView.showAndPositionInternalFrame(desktopPane, (JInternalFrame) recordEditor);
                CanRegClientView.maximizeHeight(desktopPane, (JInternalFrame) recordEditor);
            } else {
                JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("RECORD_NOT_FOUND"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (RecordLockedException | DistributedTableDescriptionException | UnknownTableException ex) {
            Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            setCursor(normalCursor);
        }
    }

    private void columnTableMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            JTable target = (JTable) evt.getSource();
            int columnNumber = target.getSelectedColumn();

            JPopupMenu jpm = new JPopupMenu("" + columnNumber);
            // jpm.add("Column " + resultTableModel.getColumn(resultTableModel.getColumnIndexAtX(evt.getX())).getHeaderValue());
            jpm.show(target, evt.getX(), evt.getY());
        }
    }

    @Action
    public void writeResults() {

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV_FILES", "csv");

        JFileChooser chooser = new JFileChooser(localSettings.getProperty(LocalSettings.WORKING_DIR_PATH_KEY));
        chooser.addChoosableFileFilter(filter);
        String fileName;
        boolean success = true;
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();

            if (!(fileName.endsWith(".csv") || fileName.endsWith(".CSV"))) {
                fileName += ".csv";
            }

            try {
                Writer bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fileName), "UTF-8"));
                String[] headers = new String[]{"Patient A ID", "Patient B ID", "Match %"};
                CSVFormat csvFormat = CSVFormat.DEFAULT
                        .withHeader(headers)
                        .withDelimiter(',');
                try (CSVPrinter csvPrinter = new CSVPrinter(bw, csvFormat)) {
                    LinkedList<String> line = new LinkedList<>();
                    for (Object row : resultTableModel.getDataVector()) {
                        for (Object value : (Vector) row) {
                            line.add(value.toString());
                        }
                        csvPrinter.printRecord(line);
                        line.clear();
                    }
                    csvPrinter.flush();
                }

            } catch (IOException ex) {
                Logger.getLogger(InstallNewSystemInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
            if (success) {
                JOptionPane.showMessageDialog(desktopPane, "Data written to: " + fileName, "Success", JOptionPane.INFORMATION_MESSAGE);
                localSettings.writeSettings();
                try {
                    Tools.openFile(fileName);
                } catch (IOException ex) {
                    Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane, "Something went wrong while writing to: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
