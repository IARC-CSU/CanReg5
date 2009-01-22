/*
 * RecordEditor.java
 *
 * Created on 16 July 2008, 14:46
 */
package canreg.client.gui.dataentry;

import canreg.client.gui.CanRegClientView;
import canreg.client.gui.tools.PrintUtilities;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.qualitycontrol.CheckResult;
import canreg.server.database.DatabaseRecord;
import canreg.server.database.Dictionary;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author  ervikm
 */
public class RecordEditor extends javax.swing.JInternalFrame implements ActionListener {

    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private LinkedList<DatabaseRecord> patientRecords;
    private TreeMap<Object, RecordEditorPanel> patientRecordsMap;
    private LinkedList<DatabaseRecord> tumourRecords;
    private boolean changesDone = false;
    private JDesktopPane desktopPane;
    private GlobalToolBox globalToolBox;
    private boolean titleSet = false;

    /** Creates new form RecordEditor
     * @param desktopPane 
     */
    public RecordEditor(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;

        initComponents();
        patientRecords = new LinkedList<DatabaseRecord>();
        tumourRecords = new LinkedList<DatabaseRecord>();
        patientRecordsMap = new TreeMap<Object, RecordEditorPanel>();

        addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                int option = JOptionPane.NO_OPTION;
                if (true) { // TODO: Implement "If changes has been made" check...
                    option = JOptionPane.showConfirmDialog(null, "Really close?\nChanges made will be lost.");
                    if (option == JOptionPane.YES_OPTION) {
                        close();
                    }
                } else {
                    close();
                }
            }
        });

        // Add a listener for changing the active tab
        ChangeListener tabbedPaneChangeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                RecordEditorPanel rep = (RecordEditorPanel) pane.getSelectedComponent();
                setActiveRecord(rep);
            }
        };
        // And add the listener to the tabbedPane
        patientTabbedPane.addChangeListener(tabbedPaneChangeListener);
        tumourTabbedPane.addChangeListener(tabbedPaneChangeListener);

    }

    private void addToPatientMap(RecordEditorPanel recordEditorPanel, DatabaseRecord dbr) {
        Object regno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
        if (regno != null) {
            patientRecordsMap.put(regno, recordEditorPanel);
        }
    }

    private void setActiveRecord(RecordEditorPanel rep) {
        DatabaseRecord dbr = rep.getDatabaseRecord();
        if (dbr!=null && dbr instanceof Tumour) {
            Object patientRecordID = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName());
            Component comp = patientRecordsMap.get(patientRecordID);
            if (comp != null) {
                patientTabbedPane.setSelectedComponent(comp);
            }
        }
    }

    /**
     * 
     * @param doc
     */
    public void setGlobalToolBox(GlobalToolBox globalToolBox) {
        this.globalToolBox = globalToolBox;
        this.doc = globalToolBox.getDocument();
    }

    /**
     * 
     */
    public void closing() {
    }

    /**
     * 
     */
    public void close() {
        this.dispose();
    }

    /**
     * 
     * @param dictionary
     */
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * 
     * @param dbr
     */
    public void addRecord(DatabaseRecord dbr) {
        RecordEditorPanel rePanel = new RecordEditorPanel();
        rePanel.setActionListener(this);
        rePanel.setDictionary(dictionary);
        rePanel.setDocument(doc);
        rePanel.setRecord(dbr);
        rePanel.repaint();
        if (dbr instanceof Patient) {
            patientRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
            String regnoString = "n/a";
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0) {
                    regnoString = "n/a";
                } else {
                    patientRecordsMap.put(regno, rePanel);
                }
            }
            patientTabbedPane.addTab(dbr.toString() + ": " + regnoString + " ", rePanel);
            if (!titleSet) {
                Object patno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
                String patnoString = "n/a";
                if (patno != null) {
                    patnoString = patno.toString();
                    if (patnoString.length() > 0) {
                        this.setTitle("Patient ID:" + patnoString);
                        titleSet = true;
                    }
                }
            }
        } else if (dbr instanceof Tumour) {
            tumourRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
            String regnoString = "n/a";
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0) {
                    regnoString = "n/a";
                }
            }
            tumourTabbedPane.addTab(dbr.toString() + ": " + regnoString + " ", rePanel);
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

        jSplitPane1 = new javax.swing.JSplitPane();
        patientTabbedPane = new javax.swing.JTabbedPane();
        tumourTabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditor.class);
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N

        jSplitPane1.setDividerSize(10);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

        patientTabbedPane.setName("patientTabbedPane"); // NOI18N
        jSplitPane1.setTopComponent(patientTabbedPane);

        tumourTabbedPane.setName("tumourTabbedPane"); // NOI18N
        jSplitPane1.setRightComponent(tumourTabbedPane);

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditor.class, this);
        jButton1.setAction(actionMap.get("addTumourAction")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setAction(actionMap.get("addPatientAction")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jButton3.setAction(actionMap.get("saveAllAction")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        jButton4.setAction(actionMap.get("printAction")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 277, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton1)
                .addComponent(jButton2)
                .addComponent(jButton3)
                .addComponent(jButton4))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton1ActionPerformed

    /**
     *
     */
    @Action
    public void addTumourAction() {
        Tumour tumour = new Tumour();
        populateNewRecord(tumour, doc);
        addRecord(tumour);
    }

    /**
     * 
     */
    @Action
    public void addPatientAction() {
        Patient patient = new Patient();
        populateNewRecord(patient, doc);
        addRecord(patient);
    }

    private DatabaseRecord populateNewRecord(DatabaseRecord dbr, Document doc) {
        String tableName = "";
        if (dbr instanceof Tumour) {
            tableName = Globals.TUMOUR_TABLE_NAME;
        } else if (dbr instanceof Patient) {
            tableName = Globals.PATIENT_TABLE_NAME;
        }
        RecordEditorPanel activePatientPanel = (RecordEditorPanel) patientTabbedPane.getSelectedComponent();
        Patient activePatient = (Patient) activePatientPanel.getDatabaseRecord();

        DatabaseVariablesListElement[] variablesInTable = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        for (DatabaseVariablesListElement dbvle : variablesInTable) {
            String type = dbvle.getVariableType();
            if (type.equalsIgnoreCase("date") || type.equalsIgnoreCase("number")) {
                dbr.setVariable(dbvle.getDatabaseVariableName(), -1);
            } else {
                dbr.setVariable(dbvle.getDatabaseVariableName(), "");
            }
        }

        if (dbr instanceof Patient) {
            // copy all information
            for (String variableName : dbr.getVariableNames()) {
                if (variableName.equalsIgnoreCase(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) || variableName.equalsIgnoreCase(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName())) {
                } else {
                    dbr.setVariable(variableName, activePatient.getVariable(variableName));
                }
            }
            // except the database record ID and the patientTable ID
            dbr.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, null);
            dbr.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName(), null);
        }
        return dbr;
    }

    /**
     * 
     */
    @Action
    public void saveAllAction() {
        LinkedList<RecordEditorPanel> reps = new LinkedList<RecordEditorPanel>();

        for (Component comp : patientTabbedPane.getComponents()) {
            reps.add((RecordEditorPanel) comp);
        }

        for (Component comp : tumourTabbedPane.getComponents()) {
            reps.add((RecordEditorPanel) comp);
        }

        for (RecordEditorPanel rep : reps) {
            try {
                DatabaseRecord dbr = saveRecord(rep.getDatabaseRecord());
                rep.refreshDatabaseRecord(dbr);
            } catch (SecurityException ex) {
                Logger.getLogger(RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void refreshTitles(RecordEditorPanel recordEditorPanel, DatabaseRecord dbr){

                    if (dbr instanceof Patient) {
                        // patientRecords.add(dbr);
                        Object regno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
                        String regnoString = "n/a";
                        if (regno != null) {
                            regnoString = regno.toString();
                            if (regnoString.length() == 0) {
                                regnoString = "n/a";
                            } else {
                                // patientRecordsMap.put(regno, recordEditorPanel);
                            }
                        }
                        int index = 0;
                        for (Component comp : patientTabbedPane.getComponents()) {
                            if (comp.equals(recordEditorPanel)) {
                                patientTabbedPane.setTitleAt(index, dbr.toString() + ": " + regnoString);
                            }
                            index++;
                        }
                        if (!titleSet) {
                            Object patno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
                            String patnoString = "n/a";
                            if (patno != null) {
                                patnoString = patno.toString();
                                if (patnoString.length() > 0) {
                                    this.setTitle("Patient ID:" + patnoString);
                                    titleSet = true;
                                }
                            }
                        }
                    } else if (dbr instanceof Tumour) {
                        // tumourRecords.add(dbr);
                        Object regno = dbr.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
                        String regnoString = "n/a";
                        if (regno != null) {
                            regnoString = regno.toString();
                            if (regnoString.length() == 0) {
                                regnoString = "n/a";
                            }
                        }
                        int index = 0;
                        for (Component comp : tumourTabbedPane.getComponents()) {
                            if (comp.equals(recordEditorPanel)) {
                                tumourTabbedPane.setTitleAt(index, dbr.toString() + ": " + regnoString);
                            }
                            index++;
                        }
                    }
    }

    /**
     * 
     */
    @Action
    public void printAction() {
        PrintUtilities.printComponent(patientTabbedPane.getSelectedComponent());
        PrintUtilities.printComponent(tumourTabbedPane.getSelectedComponent());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane patientTabbedPane;
    private javax.swing.JTabbedPane tumourTabbedPane;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source instanceof RecordEditorPanel) {
            if (e.getActionCommand().equalsIgnoreCase("changed")) {
                changesDone = true;
            } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
                int option = JOptionPane.NO_OPTION;
                option = JOptionPane.showConfirmDialog(null, "Permanently delete record?");
                if (option == JOptionPane.YES_OPTION) {
                    boolean success = false;
                    RecordEditorPanel recordEditorPanel = (RecordEditorPanel) source;
                    DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
                    int id = -1;
                    String tableName = null;
                    JTabbedPane tabbedPane = null;
                    if (record instanceof Patient) {
                        Object idObject = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                        if (idObject != null) {
                            id = (Integer) idObject;
                        }
                        tableName = Globals.PATIENT_TABLE_NAME;
                        tabbedPane = patientTabbedPane;
                    } else if (record instanceof Tumour) {
                        Object idObject = record.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                        if (idObject != null) {
                            id = (Integer) idObject;
                        }
                        tableName = Globals.TUMOUR_TABLE_NAME;
                        tabbedPane = tumourTabbedPane;
                    }
                    if (id > 0) {
                        try {
                            success = canreg.client.CanRegClientApp.getApplication().deleteRecord(id, tableName);
                        } catch (SecurityException ex) {
                            Logger.getLogger(RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RemoteException ex) {
                            Logger.getLogger(RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (success) {
                        tabbedPane.remove(recordEditorPanel);
                        JOptionPane.showInternalMessageDialog(this, "Record deleted.");
                    } else {
                        JOptionPane.showInternalMessageDialog(this, "Record not deleted.\nError occured...");
                    }
                }
            } else if (e.getActionCommand().equalsIgnoreCase("checks")) {
                RecordEditorPanel recordEditorPanel = (RecordEditorPanel) source;
                DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
                Patient patient;
                Tumour tumour;
                if (record instanceof Patient) {
                    patient = (Patient) record;
                    RecordEditorPanel otherRecordEditorPanel = (RecordEditorPanel) tumourTabbedPane.getSelectedComponent();
                    tumour = (Tumour) otherRecordEditorPanel.getDatabaseRecord();
                } else {
                    tumour = (Tumour) record;
                    RecordEditorPanel otherRecordEditorPanel = (RecordEditorPanel) patientTabbedPane.getSelectedComponent();
                    patient = (Patient) otherRecordEditorPanel.getDatabaseRecord();
                }
                LinkedList<CheckResult> checkResults = canreg.client.CanRegClientApp.getApplication().performChecks(patient, tumour);
                EditChecksInternalFrame editChecksInternalFrame = new EditChecksInternalFrame();
                String message = "";

                CheckResult.ResultCode worstResultCodeFound = CheckResult.ResultCode.OK;

                for (CheckResult result : checkResults) {

                    if (result.getResultCode() != CheckResult.ResultCode.OK) {
                        message += result + "\n";
                        if (result.getResultCode() == CheckResult.ResultCode.Invalid) {
                            worstResultCodeFound = CheckResult.ResultCode.Invalid;
                        } else if (worstResultCodeFound != CheckResult.ResultCode.Invalid) {
                            if (result.getResultCode() == CheckResult.ResultCode.Query) {
                                worstResultCodeFound = CheckResult.ResultCode.Query;
                            } else if (worstResultCodeFound != CheckResult.ResultCode.Query) {
                                worstResultCodeFound = result.getResultCode();
                            }
                        }
                    }
                    System.out.println(result);
                }

                if (worstResultCodeFound == CheckResult.ResultCode.OK) {
                    message += "Cross-check conclusion: Valid";
                }

                editChecksInternalFrame.setCrossChecksTextAreaText(message);
                editChecksInternalFrame.setResultTextFieldText(worstResultCodeFound.toString());

                if (worstResultCodeFound != CheckResult.ResultCode.Invalid) {
                    // If no errors were found we generate ICD10 code
                    ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                    if (conversionResult != null) {
                        if (conversionResult[0].getResultCode() != ConversionResult.ResultCode.Invalid) {
                            editChecksInternalFrame.setICD10TextFieldText(conversionResult[0].getValue() + "");
                        }
                    }
                }
                CanRegClientView.showAndCenterInternalFrame(desktopPane, editChecksInternalFrame);
            } else if (e.getActionCommand().equalsIgnoreCase("save")) {
                RecordEditorPanel recordEditorPanel = (RecordEditorPanel) source;
                DatabaseRecord databaseRecord = recordEditorPanel.getDatabaseRecord();
                if (databaseRecord instanceof Tumour) {
                    // set the patient id to the active patient number
                    RecordEditorPanel patientRecordEditorPanel = (RecordEditorPanel) patientTabbedPane.getSelectedComponent();
                    DatabaseRecord patientDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
                    associateTumourRecordToPatientRecord(databaseRecord, patientDatabaseRecord);
                }
                try {
                    DatabaseRecord dbr = saveRecord(databaseRecord);
                    recordEditorPanel.refreshDatabaseRecord(dbr);
                    refreshTitles(recordEditorPanel, dbr);
                    if (dbr instanceof Patient){
                        addToPatientMap(recordEditorPanel, dbr);
                    }
                } catch (SecurityException ex) {
                    Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(RecordEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private DatabaseRecord saveRecord(DatabaseRecord databaseRecord) throws SecurityException, RemoteException {
        // id is the internal database id
        if (databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) == null &&
                databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) == null) {
            int id = canreg.client.CanRegClientApp.getApplication().saveRecord(databaseRecord);
            if (databaseRecord instanceof Patient) {
                // databaseRecord.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, id);
                databaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.PATIENT_TABLE_NAME);
            } else if (databaseRecord instanceof Tumour) {
                // databaseRecord.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, id);
                databaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.TUMOUR_TABLE_NAME);
            }
            JOptionPane.showInternalMessageDialog(this, "New record saved.");
        } else {
            canreg.client.CanRegClientApp.getApplication().editRecord(databaseRecord);
            // TODO: Retrieve updated data if not data can be lost. Get the patient/tumour?
            int id = -1;
            if (databaseRecord instanceof Patient) {
                id = (Integer) databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                databaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.PATIENT_TABLE_NAME);
            } else if (databaseRecord instanceof Tumour) {
                id = (Integer) databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                databaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.TUMOUR_TABLE_NAME);
            }
            JOptionPane.showInternalMessageDialog(this, "Record saved.");
        }
        return databaseRecord;
    }

    private DatabaseRecord associateTumourRecordToPatientRecord(DatabaseRecord tumourDatabaseRecord, DatabaseRecord patientDatabaseRecord) {
        tumourDatabaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName(),
                patientDatabaseRecord.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName()));
        tumourDatabaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName(),
                patientDatabaseRecord.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName()));
        return tumourDatabaseRecord;
    }
}
