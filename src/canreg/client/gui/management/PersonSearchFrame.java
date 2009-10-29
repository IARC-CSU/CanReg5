/*
 * PersonSearchFrame.java
 *
 * Created on 28 February 2008, 10:16
 */
package canreg.client.gui.management;

import canreg.client.gui.*;
import cachingtableapi.DistributedTableDescription;
import canreg.client.CanRegClientApp;
import canreg.client.gui.dataentry.BrowseInternalFrame;
import canreg.client.gui.dataentry.RecordEditor;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.qualitycontrol.DefaultPersonSearch;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
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
import javax.swing.table.TableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;

/**
 *
 * @author  morten
 */
public class PersonSearchFrame extends javax.swing.JInternalFrame implements ActionListener {

    private PersonSearchListener listener;
    private Task duplicateSearchTask;
    private JDesktopPane desktopPane;
    private Document doc;
    private DatabaseVariablesListElement[] variablesInDB;
    private String personSearchHandlerID;
    int recordsTested;
    int matchesFound;
    private DefaultTableModel resultTableModel;
    boolean personSearcherRunning = false;
    private GlobalToolBox globalToolBox;
    private String patientIDlookupVariable;
    private String patientRecordIDlookupVariable;
    private String patientIDTumourTablelookupVariable;
    private String tumourIDlookupVariable;

    /** Creates new form PersonSearchFrame */
    public PersonSearchFrame(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;

        globalToolBox = CanRegClientApp.getApplication().getGlobalToolBox();
        patientIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        patientRecordIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
        patientIDTumourTablelookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName();
        tumourIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();

        resultTableModel = new DefaultTableModel(new String[]{"Patient A Record ID", "Patient B Record ID", "Match %"}, 0) {

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
                    option = JOptionPane.showConfirmDialog(null, "Really close?\nThe person search under way will be interupted.", "Really close?", JOptionPane.YES_NO_OPTION);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
                .addContainerGap(15, Short.MAX_VALUE))
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
            .addComponent(personSearchVariablesPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        resultPanel.setName("resultPanel"); // NOI18N

        resultScrollPane.setName("resultScrollPane"); // NOI18N

        resultTable.setAutoCreateRowSorter(true);
        resultTable.setModel(resultTableModel);
        resultTable.setCellSelectionEnabled(true);
        resultTable.setName("resultTable"); // NOI18N
        resultScrollPane.setViewportView(resultTable);

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addContainerGap())
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Results", resultPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
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
            personSearchHandlerID = CanRegClientApp.getApplication().initiateGlobalDuplicateSearch(
                    searcher, rangeStartTextField.getText(), rangeEndTextField.getText());
        } catch (SecurityException ex) {
            Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return duplicateSearchTask;
    }

    private class PerformDuplicateSearchTask extends org.jdesktop.application.Task<Object, Void> {

        PerformDuplicateSearchTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to PerformDuplicateSearchTask fields, here.
            super(app);
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
                        for (String patientA : result.keySet()) {
                            Map<String, Float> map = result.get(patientA);
                            matchesFound += map.size();
                            for (String patientB : map.keySet()) {
                                resultTableModel.addRow(new Object[]{patientA, patientB, map.get(patientB)});
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
            } catch (SecurityException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            performButton.setEnabled(true);
            interruptButton.setEnabled(false);
            personSearcherRunning = false;
        // TODO: Display results
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton interruptButton;
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
            } catch (SecurityException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
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
            TableModel model = target.getModel();
            Patient patient;
            try {
                patient = CanRegClientApp.getApplication().getPatientRecord("" + model.getValueAt(rowNumber, columnNumber), true);
                editPatientID(patient.getVariable(patientIDlookupVariable).toString());
            } catch (SQLException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(PersonSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void editPatientID(String idString) {
        String tableName = Globals.PATIENT_TABLE_NAME;

        RecordEditor recordEditor = new RecordEditor(desktopPane);
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
                If we don't get any records with that ID - we create one.
                 */
                record = new Patient();
                record.setVariable(patientIDlookupVariable, idString);
                CanRegClientApp.getApplication().saveRecord(record);
                distributedTableDescription = CanRegClientApp.getApplication().getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
                numberOfRecords = distributedTableDescription.getRowCount();
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
                TreeSet<DatabaseRecord> set = new TreeSet<DatabaseRecord>(new Comparator<DatabaseRecord>() {

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
                        set.add(rec);
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
                CanRegClientView.showAndCenterInternalFrame(desktopPane, recordEditor);
            } else {
                JOptionPane.showMessageDialog(rootPane, "Record not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BrowseInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
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
}
