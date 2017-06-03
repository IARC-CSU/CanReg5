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
 * ComparePatientsInternalFrame.java
 *
 * Created on 07-Jul-2011, 17:23:40
 */
package canreg.client.gui.management;

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.dataentry.RecordEditor;
import canreg.common.*;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;


/**
 *
 * @author ErvikM
 */
public class ComparePatientsInternalFrame extends javax.swing.JInternalFrame {

    private List<Pair<Patient, Tumour[]>> recordSets;
    private Pair<Patient, Tumour[]> mainRecord;
    private TableModel tableModel;
    private String patientIDlookupVariable;
    private String tumourIDlookupVariable;
    private GlobalToolBox globalToolBox;
    private JDesktopPane desktopPane;
    private String patientIDTumourTablelookupVariable;
    private LinkedList<Float> percentList;
    private String[] defaultColumns;
    private String firstColumnName;
    private JTable resultTable;
    private LocalSettings localSettings;

    /** Creates new form ComparePatientsInternalFrame */
    public ComparePatientsInternalFrame(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
        initComponents();
        firstColumnName = "Merge";
        firstColumnName = "-----";
        globalToolBox = canreg.client.CanRegClientApp.getApplication().getGlobalToolBox();
        patientIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        tumourIDlookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        patientIDTumourTablelookupVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName();
        Document doc = globalToolBox.getDocument();
        PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(doc, Globals.NAMESPACE);
        LinkedList<String> defaultColumnsList = new LinkedList<String>();
        // defaultColumnsList.add(patientIDlookupVariable);
        for (PersonSearchVariable psv : searchVariables) {
            defaultColumnsList.add(psv.getName());
        }
        defaultColumns = defaultColumnsList.toArray(new String[0]);
        recordSets = new LinkedList<Pair<Patient, Tumour[]>>();
        percentList = new LinkedList<Float>();
        mergeButton.setVisible(false);
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
    }

    public void setColumnsToShow(String[] columnNames) {
        defaultColumns = columnNames;
    }

    private void rowClicked(java.awt.event.MouseEvent evt) {
        JTable target = (JTable) evt.getSource();
        if (evt.getClickCount() == 2) {
            if (target.columnAtPoint(evt.getPoint()) != 0) {
                int rowNumber = target.rowAtPoint(evt.getPoint());
                TableModel model = target.getModel();
                int columnNumber = 0;
                String lookUpVariable;
                lookUpVariable = canreg.common.Tools.toUpperCaseStandardized(patientIDlookupVariable);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    if (canreg.common.Tools.toUpperCaseStandardized(model.getColumnName(i)).equals(lookUpVariable)) {
                        columnNumber = i;
                        break;
                    }
                }
                editPatientID("" + model.getValueAt(rowNumber, columnNumber));
            }
        } else if (evt.getClickCount() == 1) {
            int columnNumber = target.columnAtPoint(evt.getPoint());
            if (columnNumber == 0) {
                int rowNumber = target.rowAtPoint(evt.getPoint());
                Boolean oldValue = (Boolean) tableModel.getValueAt(rowNumber, columnNumber);
                tableModel.setValueAt(!oldValue, rowNumber, columnNumber);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        mergeButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        resultScrollPane = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(ComparePatientsInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(ComparePatientsInternalFrame.class, this);
        closeButton.setAction(actionMap.get("closeAction")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        mergeButton.setAction(actionMap.get("mergePatientAction")); // NOI18N
        mergeButton.setText(resourceMap.getString("mergeButton.text")); // NOI18N
        mergeButton.setName("mergeButton"); // NOI18N

        jSplitPane1.setDividerLocation(220);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        resultScrollPane.setName("resultScrollPane"); // NOI18N
        jSplitPane1.setTopComponent(resultScrollPane);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText(resourceMap.getString("jTextArea1.text")); // NOI18N
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        jSplitPane1.setRightComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(409, Short.MAX_VALUE)
                                .addComponent(mergeButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton)
                                .addContainerGap())
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(closeButton)
                                        .addComponent(mergeButton))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void mergePatientAction() {
    }

    @Action
    public void closeAction() {
        this.dispose();
    }

    public void addMainRecordSet(Patient patient, Tumour[] tumours) {
        Pair recordSet = new Pair(patient, tumours);
        mainRecord = recordSet;
        refreshTable();
    }

    public void addRecordSet(Patient patient, Tumour[] tumours, float percent) {
        Pair recordSet = new Pair(patient, tumours);
        recordSets.add(recordSet);
        percentList.add(percent);
        refreshTable();
    }

    private void refreshTable() {
        Patient mainPatient = mainRecord.getFirst();
        String[] columnNames = canreg.common.Tools.arrayConcat(new String[]{
                firstColumnName,
                "Percent", patientIDlookupVariable}, defaultColumns);
        Object[][] data = new Object[recordSets.size() + 1][columnNames.length];
        // add the main one
        List<Object> line = new LinkedList<Object>();
        line.add(true);
        line.add("Original");
        line.add(mainPatient.getVariable(patientIDlookupVariable));
        for (String varb : defaultColumns) {
            line.add(mainPatient.getVariable(varb));
        }
        data[0] = line.toArray();

        // add the rest
        int i = 1;
        for (Pair<Patient, Tumour[]> recordSet : recordSets) {
            Patient patient = recordSet.getFirst();
            List<Object> newLine = new LinkedList<Object>();
            newLine.add(false);
            newLine.add(percentList.get(i - 1) + " %");
            newLine.add(patient.getVariable(patientIDlookupVariable));
            for (String varb : defaultColumns) {
                newLine.add(patient.getVariable(varb));
            }
            data[i] = newLine.toArray();
            i++;
        }
        tableModel = new MyTableModel(data, columnNames);

        resultTable = new javax.swing.JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row,
                                             int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth
                                + getIntercellSpacing().width,
                        tableColumn.getPreferredWidth()));
//<ictl.co>
                if (LocalizationHelper.isRtlLanguageActive()) {
                    if (component instanceof DefaultTableCellRenderer.UIResource) {
                        String value = ((DefaultTableCellRenderer.UIResource) component).getText();
                        if (DateHelper.analyseContentForDateValue(value)) {
                            value = DateHelper.gregorianDateStringToLocaleDateString(value, Globals.DATE_FORMAT_STRING);
                            ((DefaultTableCellRenderer.UIResource) component).setText(value);
                        }
                    }
                }
//</ictl.co>
               return component;
            }
        };

        resultTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        resultTable.setColumnSelectionAllowed(false);
        resultTable.setEnabled(false);
        resultTable.setName("resultTable"); // NOI18N

        resultScrollPane.setViewportView(resultTable);

        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rowClicked(evt);
            }
        });

        resultTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                System.out.println("From Index: " + e.getFromIndex());
                System.out.println("To Index: " + e.getToIndex());
                if (e.getFromIndex() == 0 && e.getToIndex() != 0) {
                    resultTable.moveColumn(e.getToIndex(), e.getFromIndex());
                }
            }

            @Override
            public void columnAdded(TableColumnModelEvent e) {
                // resultTable.columnAdded(e);
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                // resultTable.columnRemoved(e);
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                // resultTable.columnMarginChanged(e);
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
                // resultTable.columnSelectionChanged(e);
            }
        });
        // TableColumnAdjuster tca = new TableColumnAdjuster(resultTable);
        // tca.setColumnDataIncluded(true);
        // tca.setOnlyAdjustLarger(true);
        // tca.adjustColumns();
        resultScrollPane.setViewportView(resultTable);
        // jSplitPane1.setTopComponent(new JScrollPane(resultTable));
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
        if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW))
            recordEditor = new canreg.client.gui.dataentry2.RecordEditorMainFrame(desktopPane);
        else 
            recordEditor = new RecordEditor(desktopPane);
        
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
                TreeSet<DatabaseRecord> set = new TreeSet<DatabaseRecord>(new Comparator<DatabaseRecord>() {

                    @Override
                    public int compare(DatabaseRecord o1, DatabaseRecord o2) {
                        //<ictl.co> FIX NPE Error
                        if (o1.getVariable(tumourIDlookupVariable) == null || o2.getVariable(tumourIDlookupVariable) == null) {
                            return (o1.getVariable(patientIDTumourTablelookupVariable).toString().compareTo(o2.getVariable(patientIDTumourTablelookupVariable).toString()));
                        } else {
                            return (o1.getVariable(tumourIDlookupVariable).toString().compareTo(o2.getVariable(tumourIDlookupVariable).toString()));
                        }
                        //</ictl.co>
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
                CanRegClientView.showAndPositionInternalFrame(desktopPane, (JInternalFrame)recordEditor);
            } else {
                JOptionPane.showMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("RECORD_NOT_FOUND"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (RecordLockedException ex) {
            JOptionPane.showMessageDialog(rootPane, "Record already open.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ComparePatientsInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(ComparePatientsInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownTableException ex) {
            Logger.getLogger(ComparePatientsInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ComparePatientsInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ComparePatientsInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ComparePatientsInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            setCursor(normalCursor);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton mergeButton;
    private javax.swing.JScrollPane resultScrollPane;
    // End of variables declaration//GEN-END:variables

    class MyTableModel extends DefaultTableModel {

        public MyTableModel(Object rowData[][], Object columnNames[]) {
            super(rowData, columnNames);
        }

        @Override
        public Class getColumnClass(int col) {
            if (col == 0) //first column is boolean
            {
                return Boolean.class;
            } else {
                return String.class;  //other columns accept String values  
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0 && row != 0) //first column will be editable, excapt first line
            {
                return true;
            } else {
                return false;
            }
        }
    }
}
