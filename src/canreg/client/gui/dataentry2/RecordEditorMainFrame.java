/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2021 International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr Patricio Ezequiel
 * Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.dataentry2;

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.gui.CanRegClientView;

import static canreg.client.gui.CanRegClientView.maximizeHeight;

import canreg.client.gui.dataentry.BrowseInternalFrame;
import canreg.client.gui.dataentry.EditChecksInternalFrame;
import canreg.client.gui.dataentry.HoldingRawDataInternalFrame;
import canreg.client.gui.dataentry2.components.DottedDividerSplitPane;
import canreg.client.gui.management.ComparePatientsInternalFrame;
import canreg.client.gui.tools.PrintUtilities;
import canreg.client.gui.tools.WaitFrame;
import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.JComponentToPDF;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.DefaultMultiplePrimaryTester;
import canreg.common.qualitycontrol.MultiplePrimaryTesterInterface;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.Patient;
import canreg.server.database.RecordLockedException;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.server.CanRegServerInterface;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm, patri_000
 */
public class RecordEditorMainFrame extends javax.swing.JInternalFrame
        implements ActionListener, RecordEditor {
    private static final Logger LOGGER = Logger.getLogger(RecordEditorMainFrame.class.getName());
    public static final String CHANGED = "changed";
    public static final String CHECKS = "checks";
    public static final String DELETE = "delete";
    public static final String SAVE = "save";
    public static final String RUN_MP = "runMP";
    public static final String RUN_EXACT = "runExact";
    public static final String OBSOLETE = "obsolete";
    public static final String CHANGE_PATIENT_RECORD = "changePatientRecord";
    public static final String CALC_AGE = "calcAge";
    public static final String AUTO_FILL = "autoFill";
    public static final String PERSON_SEARCH = "person search";
    public static String REQUEST_FOCUS = "request focus";
    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private final Set<DatabaseRecord> patientRecords;
    private final TreeMap<Object, RecordEditorPatient> patientRecordsMap;
    private final Set<DatabaseRecord> tumourRecords;
    private final JDesktopPane desktopPane;
    private GlobalToolBox globalToolBox;
    private boolean titleSet = false;
    private String tumourObsoleteVariableName = null;
    private String patientObsoleteVariableName = null;
    private String tumourSequenceVariableName = null;
    private String tumourSequenceTotalVariableName = null;
    AutoFillHelper autoFillHelper;
    private String patientIDVariableName = null;
    private String patientRecordIDVariableName = null;
    private volatile boolean mouseInsideSave = false;
    private final HashMap<RecordEditorTumour, Boolean> obsoleteToggles;
    private final LocalSettings localSettings;
    private final CanRegServerInterface server;
    private BrowseInternalFrame browser;
    private final List<HoldingRawDataInternalFrame> rawDataFrames;
    private canreg.client.gui.dataentry2.RecordEditor productionRecordEditor;
    private final ChangeListener tabbedPaneChangeListener;
    private final WaitFrame waitFrame;


    public RecordEditorMainFrame(JDesktopPane desktopPane, CanRegServerInterface server, BrowseInternalFrame browser) {
        this.desktopPane = desktopPane;
        this.server = server;
        this.browser = browser;
        this.localSettings = CanRegClientApp.getApplication().getLocalSettings();
        initComponents();
        patientRecords = new LinkedHashSet<DatabaseRecord>();
        tumourRecords = new LinkedHashSet<DatabaseRecord>();
        patientRecordsMap = new TreeMap<Object, RecordEditorPatient>();
        autoFillHelper = new AutoFillHelper();
        obsoleteToggles = new HashMap<RecordEditorTumour, Boolean>();
        rawDataFrames = new LinkedList<>();


        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                // Go through all panels and ask if any changes has been done                
                boolean changesDone = false;
                for (Component component : patientTabbedPane.getComponents()) {
                    RecordEditorPatient panel = (RecordEditorPatient) component;
                    changesDone = changesDone || panel.isSaveNeeded();
                }

                for (Component component : tumourTabbedPane.getComponents()) {
                    RecordEditorTumour panel = (RecordEditorTumour) component;
                    changesDone = changesDone || panel.isSaveNeeded();
                }

                if (changesDone) {
                    int option = JOptionPane.showConfirmDialog(null,
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("REALLY CLOSE?CHANGES MADE WILL BE LOST."),
                            "Warning!", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        releaseResources();
                        dispose();
                    }
                } else {
                    releaseResources();
                    dispose();
                }
            }
        });
        // Add a listener for changing the active tab
        tabbedPaneChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                RecordEditorPanel rep = (RecordEditorPanel) pane.getSelectedComponent();
                if (rep != null) {
                    setActiveRecord(rep);
                }
            }
        };
        // And add the listener to the tabbedPane
        patientTabbedPane.addChangeListener(tabbedPaneChangeListener);
        tumourTabbedPane.addChangeListener(tabbedPaneChangeListener);

        if (this.server == null) {
            this.productionBtn.setVisible(false);
            this.viewFullDataBtn.setVisible(false);
            this.viewProductionRecordBtn.setVisible(false);
        }

        // initializing the waitFrame
        waitFrame = new WaitFrame();
        waitFrame.setLabel(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                .getString("SEARCHING..."));
        waitFrame.setIndeterminate(true);
        desktopPane.add(waitFrame);
        waitFrame.getContentPane().setVisible(true);
        waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2,
                (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
    }

    private void close() {
        for (HoldingRawDataInternalFrame frame : rawDataFrames)
            frame.dispose();
        releaseRecords();
        dispose();
    }

    private void releaseResources() {
        // Release all patient records held
        for (DatabaseRecord record : patientRecords) {
            try {
                Object idObj = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                if (idObj != null) {
                    int id = (Integer) idObj;
                    canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.PATIENT_TABLE_NAME, server);
                }
            } catch (RemoteException | SecurityException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        patientRecords.clear();

        // Release all tumour records held
        for (DatabaseRecord record : tumourRecords) {
            Tumour tumour = (Tumour) record;
            // Release all sources
            for (Source source : tumour.getSources()) {
                try {
                    Object idObj = source.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                    if (idObj != null) {
                        int id = (Integer) idObj;
                        canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.SOURCE_TABLE_NAME, server);
                    }
                } catch (RemoteException | SecurityException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
            try {
                Object idObj = tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                if (idObj != null) {
                    int id = (Integer) idObj;
                    canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.TUMOUR_TABLE_NAME, server);
                }
            } catch (RemoteException | SecurityException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        tumourRecords.clear();

        patientTabbedPane.removeChangeListener(tabbedPaneChangeListener);

        int totalTabs = patientTabbedPane.getTabCount();
        for (int i = 0; i < totalTabs; i++) {
            Component c = patientTabbedPane.getComponentAt(i);
            ((RecordEditorPatient) c).releaseResources();
            c = null;
        }

        patientTabbedPane.removeAll();
        patientTabbedPane = null;

        tumourTabbedPane.removeChangeListener(tabbedPaneChangeListener);

        totalTabs = tumourTabbedPane.getTabCount();
        for (int i = 0; i < totalTabs; i++) {
            Component c = tumourTabbedPane.getComponentAt(i);
            ((RecordEditorTumour) c).releaseResources();
            c = null;
        }

        tumourTabbedPane.removeAll();
        tumourTabbedPane = null;
    }

    private void addToPatientMap(RecordEditorPatient recordEditorPanel, DatabaseRecord dbr) {
        Object regno = dbr.getVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                .getDatabaseVariableName());
        if (regno != null) {
            patientRecordsMap.put(regno, recordEditorPanel);
        }
    }

    private void setActiveRecord(RecordEditorPanel rep) {
        DatabaseRecord dbr = rep.getDatabaseRecord();
        if (dbr != null && dbr instanceof Tumour) {
            Object patientRecordID = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                    .getDatabaseVariableName());
            Component comp = patientRecordsMap.get(patientRecordID);
            if (comp != null) {
                patientTabbedPane.setSelectedComponent(comp);
            }
        }
    }

    public void toggleObsolete(boolean confirmed, RecordEditorTumour tumourSelected) {
        if (confirmed) {
            DatabaseVariablesListElement dbvle = tumourSelected.getObsoleteFlagVariableListElement();
            if (dbvle != null) {
                boolean obsolete = tumourObsoleteToggleButton.isSelected();
                this.obsoleteToggles.put(tumourSelected, obsolete);
                if (obsolete) {
                    tumourSelected.getDatabaseRecord().setVariable(dbvle.getDatabaseVariableName(), Globals.OBSOLETE_VALUE);
                } else {
                    tumourSelected.getDatabaseRecord().setVariable(dbvle.getDatabaseVariableName(), Globals.NOT_OBSOLETE_VALUE);
                }
            }
        } else {
            tumourObsoleteToggleButton.setSelected(!tumourObsoleteToggleButton.isSelected());
        }
    }

    @Override
    public void setGlobalToolBox(GlobalToolBox globalToolBox) {
        this.globalToolBox = globalToolBox;
        this.doc = globalToolBox.getDocument();
        autoFillHelper.setGlobalToolBox(globalToolBox);

        patientObsoleteVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString()).getDatabaseVariableName();
        tumourObsoleteVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString()).getDatabaseVariableName();

        //A verrr debugea que mierda hay en estas dos variableeeees
        tumourSequenceVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimSeq.toString()).getDatabaseVariableName();
        tumourSequenceTotalVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimTot.toString()).getDatabaseVariableName();

        //unused tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        patientIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        patientRecordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
    }

    @Override
    public void setDictionary(Map<Integer, canreg.common.database.Dictionary> dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Adds a new record (Patient or Tumour) to this Frame. Please load patients
     * first and then the tumours
     *
     * @param dbr
     */
    @Override
    public void addRecord(DatabaseRecord dbr) {
        if (dbr instanceof Patient) {
            RecordEditorPatient rePanel = new RecordEditorPatient(this);
            rePanel.setDictionary(dictionary);
            rePanel.setDocument(doc);
            ((RecordEditorPatient) rePanel).setRecordAndBuildPanel(dbr);
            patientRecords.add(dbr);

            String regno = dbr.getVariableAsString(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals
                            .StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
            String regnoString;
            if (regno != null) {
                regnoString = regno;
                if (regnoString.length() == 0)
                    regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
                else
                    patientRecordsMap.put(regno, ((RecordEditorPatient) rePanel));

                if (this.server != null)
                    this.loadProductionRecordEditor(regno);
            } else {
                regnoString = String.valueOf((patientTabbedPane.getTabCount() + 1));
                this.viewProductionRecordBtn.setVisible(false);
            }

            Object patientObsoleteStatus = dbr.getVariable(patientObsoleteVariableName);
            if (patientObsoleteStatus != null && patientObsoleteStatus.equals(Globals.OBSOLETE_VALUE)) {
                regnoString += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString(" (OBSOLETE)");
            }

            String tabTitle = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("PATIENT")
                    + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString(" RECORD ")
                    + ": " + regnoString;

            //Add to all tumour tabs this Patient title
            for (Component tumourComp : this.tumourTabbedPane.getComponents()) {
                RecordEditorTumour tumourPanel = (RecordEditorTumour) tumourComp;

                //When adding this patient, it will automatically select it if
                //it was previously linked to it.
                tumourPanel.addLinkablePatient(tabTitle);

                //We check to see if this tumour was previously linked to 
                //the dbr patient that's being added.
                DatabaseRecord tumourRecord = tumourPanel.getDatabaseRecord();
                String tumourPatientID = (String) tumourRecord.getVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                        .getDatabaseVariableName());

                //If tumourPatientID is null or empty then the tumour is brand new, and
                //we link it to the patient tab that is currently selected
                if ((tumourPatientID == null || tumourPatientID.isEmpty())
                        || tumourPatientID.startsWith(regnoString)) {
                    ((RecordEditorPatient) rePanel).addTumour(tumourPanel);
                }
            }

            patientTabbedPane.addTab(tabTitle, ((RecordEditorPatient) rePanel));

            if (!titleSet) {
                Object patno = dbr.getVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
                String patnoString;// = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
                if (patno != null) {
                    patnoString = patno.toString();
                    if (patnoString.length() > 0) {
                        this.setTitle(java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("PATIENT ID:") + patnoString);
                        titleSet = true;
                    }
                }
            }
        } else if (dbr instanceof Tumour) {
            //If no patients were previosly added, it gets very very difficult 
            //to link the tumours and the patients.
            if (this.patientTabbedPane.getComponentCount() == 0) {
                throw new RuntimeException("Before adding a tumour first load at least 1 patient please");
            }

            RecordEditorTumour rePanel = new RecordEditorTumour((ActionListener) this, (RecordEditorMainFrame) this);
            rePanel.setDictionary(dictionary);
            rePanel.setDocument(doc);
            ((RecordEditorTumour) rePanel).setRecordAndBuildPanel(dbr);
            obsoleteToggles.put(((RecordEditorTumour) rePanel), false);
            tumourRecords.add(dbr);

            //COMMENTED: regno and regnoString is commented because it's never used in the title of the tumour tab.
            /*Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals
                            .StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0)
                    regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");                
            }*/
            Object tumourObsoleteStatus = dbr.getVariable(tumourObsoleteVariableName);
            if (tumourObsoleteStatus != null && tumourObsoleteStatus.equals(Globals.OBSOLETE_VALUE)) {
                //regnoString += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString(" (OBSOLETE)");
                obsoleteToggles.put(((RecordEditorTumour) rePanel), true);
            }

            //Add to this new tumour all the patient tabs titles
            for (int i = 0; i < this.patientTabbedPane.getTabCount(); i++) {
                ((RecordEditorTumour) rePanel).addLinkablePatient(this.patientTabbedPane.getTitleAt(i));

                RecordEditorPatient patientPanel = (RecordEditorPatient) this.patientTabbedPane.getComponentAt(i);
                //We check to see if this tumour that's being added was already
                //linked to an existing patient tab                
                String tumourPatientID = (String) dbr.getVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                        .getDatabaseVariableName());

                //If tumourPatientID is null or empty then the tumour is brand new, and
                //we link it to the patient tab that is currently selected
                if (((tumourPatientID == null || tumourPatientID.isEmpty())
                        && this.patientTabbedPane.getComponentAt(i).equals(this.patientTabbedPane.getSelectedComponent()))
                        || this.patientTabbedPane.getTitleAt(i).contains(tumourPatientID)) {
                    patientPanel.addTumour(((RecordEditorTumour) rePanel));
                    ((RecordEditorTumour) rePanel).setLinkedPatient(this.patientTabbedPane.getTitleAt(i), true);
                }
            }

            tumourTabbedPane.addTab(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("TUMOUR")
                            + ":" + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString(" RECORD ")
                            + " " + (tumourTabbedPane.getTabCount() + 1),
                    ((RecordEditorTumour) rePanel));
        }
        refreshShowObsolete();
    }

    private void loadProductionRecordEditor(String patientRecordID) {
        try {
            String tumourIDVariable = globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString())
                    .getDatabaseVariableName();
            String patientIDVariable = globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                    .getDatabaseVariableName();

            Patient productionPatient = CanRegClientApp.getApplication().getPatientRecord(patientRecordID, false, null);
            if (productionPatient != null) {
                String dataEntryVersion = localSettings.getProperty(LocalSettings.DATA_ENTRY_VERSION_KEY);
                if (dataEntryVersion.equalsIgnoreCase(LocalSettings.DATA_ENTRY_VERSION_NEW))
                    this.productionRecordEditor = new canreg.client.gui.dataentry2
                            .RecordEditorMainFrame(this.desktopPane, null, this.browser);
                else
                    this.productionRecordEditor = new canreg.client.gui.dataentry
                            .RecordEditor(this.desktopPane, null, this.browser);
                this.productionRecordEditor.setGlobalToolBox(CanRegClientApp.getApplication().getGlobalToolBox());
                this.productionRecordEditor.setDictionary(CanRegClientApp.getApplication().getDictionary());
                this.productionRecordEditor.addRecord(productionPatient);

                TreeSet<DatabaseRecord> set = new TreeSet<DatabaseRecord>(new Comparator<DatabaseRecord>() {
                    @Override
                    public int compare(DatabaseRecord o1, DatabaseRecord o2) {
                        return (o1.getVariable(tumourIDVariable).toString().compareTo(o2.getVariable(tumourIDVariable).toString()));
                    }
                });

                DatabaseRecord[] tumourRecords = CanRegClientApp.getApplication()
                        .getTumourRecordsBasedOnPatientID(productionPatient.getVariableAsString(patientIDVariable), false, null);
                for (DatabaseRecord rec : tumourRecords) {
                    // store them in a set, so we don't show them several times
                    if (rec != null)
                        set.add(rec);
                }

                for (DatabaseRecord rec : set) {
                    // store them in a map, so we don't show them several times
                    this.productionRecordEditor.addRecord(rec);
                }
                // make sure the records are locked...
//                CanRegClientApp.getApplication()
//                        .getPatientsByPatientID(productionPatient.getVariableAsString(patientIDVariable), true, null);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            this.viewProductionRecordBtn.setVisible(false);
        }
    }

    @Action
    public void addTumourAction() {
        Tumour tumour = new Tumour();
        populateNewRecord(tumour, doc);
        addRecord(tumour);
        this.tumourTabbedPane.setSelectedIndex(tumourTabbedPane.getComponentCount() - 1);
    }

    @Action
    public void patientMenuAction() {
        patientPopupMenu.show(patientMenuButton, 0, 0);
    }

    @Action
    public void tumourMenuAction() {
        tumourPopupMenu.show(tumourMenuButton, 0, 0);
    }

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

        RecordEditorPatient activePatientPanel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
        Patient activePatient = (Patient) activePatientPanel.getDatabaseRecord();

        DatabaseVariablesListElement[] variablesInTable = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        for (DatabaseVariablesListElement dbvle : variablesInTable) {
            String type = dbvle.getVariableType();
            if (type.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                Object unk = dbvle.getUnknownCode();
                int number = -1;
                if (unk != null) {
                    if (unk instanceof String) {
                        number = Integer.parseInt((String) unk);
                    } else {
                        number = (Integer) unk;
                    }
                }

                if (number == -1) {
                    dbr.setVariable(dbvle.getDatabaseVariableName(), "");
                } else {
                    dbr.setVariable(dbvle.getDatabaseVariableName(), number);
                }
            } else {
                dbr.setVariable(dbvle.getDatabaseVariableName(), "");
            }
        }

        if (dbr instanceof Patient) {
            // copy all information
            for (String variableName : dbr.getVariableNames()) {
                if (variableName.equalsIgnoreCase(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME)
                        || variableName.equalsIgnoreCase(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                        .getDatabaseVariableName())) {
                    /*Nothing here*/
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
     *  Saves all open Records. A Record is either a Patient, a Tumour or a
     *  Source. Only saves a Record if the data of that Record has changed.
     *  The strategy adopted when multiple Records have changes is to try to
     *  save all the changes in one transaction.
     *  If there is an exception all records will be rollback and a message
     *  will be display to the user with a list of all the broken record 
     */
    @Action
    public void saveAllAction() {
        String errorMessage = "";
        try {
            CanRegClientApp.getApplication().openTransaction();
            // return the error message, an empty string if no error
            errorMessage = saveAllActionInternal();
            if (errorMessage.isEmpty()) {
                CanRegClientApp.getApplication().commitTransaction();
            }
        } catch (RuntimeException | RemoteException ex) {
            Logger.getLogger(canreg.client.gui.dataentry2.RecordEditorMainFrame.class.getName())
                    .log(Level.SEVERE, "Error during the transaction every change were rollback", ex);
            errorMessage = java.util.ResourceBundle.
                    getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                    .getString("TECHNICAL ERROR");
        } finally {
            handleEndSaveAll(errorMessage);
        }
    }

    private void handleEndSaveAll(String errorMessage) {
        if (!errorMessage.isEmpty()) {
            try {
                CanRegClientApp.getApplication().rollbackTransaction();
            } catch (RemoteException e) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditorMainFrame.class.getName())
                        .log(Level.SEVERE, "Error during the transaction rollback :" + e.getMessage(), e);
            }
            JOptionPane.showInternalMessageDialog(this,
                    errorMessage,
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("FAILED"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showInternalMessageDialog(this,
                    java.util.ResourceBundle
                            .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("RECORD SAVED."));
        }
    }

    /**
     *  A Record is either a Patient, a Tumour or a Source.
     *  Only saves a Record if the data of that Record has changed.
     *  If there is an exception all the broken record will be return
     * @return an error message containing the broken recors , an empty string of no error 
     */
    @Action
    private String saveAllActionInternal() {
        LinkedList<RecordEditorPatient> successfulPatients = new LinkedList<>();
        LinkedList<String> failedPatients = new LinkedList<>();
        LinkedList<RecordEditorTumour> successfulTumours = new LinkedList<>();
        LinkedList<String> failedTumours = new LinkedList<>();
        String failed = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                .getString("FAILED");

        //First we try to save the patients because we need them saved in order
        //to save the tumours. Only patients with changed data are saved.
        for (int i = 0; i < patientTabbedPane.getTabCount(); i++) {
            RecordEditorPatient patient = (RecordEditorPatient) patientTabbedPane.getComponentAt(i);
            try {
                if (patient.isSaveNeeded()) {
                    patient.prepareToSaveRecord();
                    this.saveRecord(patient);
                }
                successfulPatients.add(patient);
            } catch (SaveRecordException ex) {
                patient.setSaveNeeded(true);
                failedPatients.add(patientTabbedPane.getTitleAt(i) + " (Tab nº" + (i + 1) + ") "
                        + failed + ": " + ex.getLocalizedMessage());
            }
        }

        //Before saving the tumours, the sequences are updated.
        //The sequence will ONLY be updated if it changed (or if the
        //record is new).
        LinkedList<RecordEditorTumour> openTumours = new LinkedList<>();
        for (Component comp : this.tumourTabbedPane.getComponents()) {
            openTumours.add((RecordEditorTumour) comp);
        }
        this.updateAllTumoursSequences(openTumours);

        //Now we try to save all open tumours. 
        //Only tumours with changed data are saved.
        for (int i = 0; i < tumourTabbedPane.getTabCount(); i++) {
            RecordEditorTumour tumour = (RecordEditorTumour) tumourTabbedPane.getComponentAt(i);
            try {
                if (tumour.isSaveNeeded()) {
                    tumour.prepareToSaveRecord();
                    this.saveRecord(tumour);
                }
                //We consider it succesful even if it didn't have any changes.
                successfulTumours.add(tumour);
            } catch (SaveRecordException ex) {
                tumour.setSaveNeeded(true);
                failedTumours.add(tumourTabbedPane.getTitleAt(i) + " (Tab nº" + (i + 1) + ") "
                        + failed + ": " + ex.getLocalizedMessage());
            }
        }

        if (failedTumours.isEmpty() && failedPatients.isEmpty()) {
            // return an empty string if there is no issue
            return "";
        } else {
            StringBuilder str = new StringBuilder();
            for (String pat : failedPatients) {
                str.append(pat).append("\n");
            }
            for (String tum : failedTumours) {
                str.append(tum).append("\n");
            }
            // return a string that contain all the errors
            return str.toString();
        }
    }

    /**
     * Refreshes a record (patient or tumour) id/title. This should be executed
     * when a record has been saved. SO FAR this method is only called to
     * refresh patient titles, tumours titles are not refresh (but the
     * implementation is supported by this method).
     *
     * @param recordEditorPanel record Editor Panel
     */
    private void refreshTitles(RecordEditorPanel recordEditorPanel) {
        DatabaseRecord dbr = recordEditorPanel.getDatabaseRecord();
        if (dbr instanceof Patient) {
            Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle
                    .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0) {
                    regnoString = java.util.ResourceBundle
                            .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
                }
            }

            String newTitle = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("PATIENT")
                    + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString(" RECORD ")
                    + ": " + regnoString;

            for (int index = 0; index < patientTabbedPane.getComponentCount(); index++) {
                //Only the Patient tab passed by parameter gets the title updated
                if (patientTabbedPane.getComponentAt(index).equals(recordEditorPanel)) {

                    //We also update this particular Patient title in the "Tumour linked to" combobox
                    //of all the tumours present 
                    for (Component tumourComp : this.tumourTabbedPane.getComponents()) {
                        RecordEditorTumour tumourPanel = (RecordEditorTumour) tumourComp;
                        tumourPanel.replaceLinkablePatient(this.patientTabbedPane.getTitleAt(index),
                                newTitle);
                    }
                    patientTabbedPane.setTitleAt(index, newTitle);
                    break;
                }
            }

            if (!titleSet) {
                Object patno = dbr.getVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
                String patnoString;// = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
                if (patno != null) {
                    patnoString = patno.toString();
                    if (patnoString.length() > 0) {
                        this.setTitle(java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                .getString("PATIENT ID:") + patnoString);
                        titleSet = true;
                    }
                }
            }
        } //This method is NEVER called with a Tumour passed as parameter, but we
        //leave this here as legacy code in case we want to change the tumours tab titles
        else if (dbr instanceof Tumour) {
            // tumourRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle
                    .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0) {
                    regnoString = java.util.ResourceBundle
                            .getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("N/A");
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

    @Action
    public void printAction() {
        PrintUtilities.printComponent(patientTabbedPane.getSelectedComponent());
        PrintUtilities.printComponent(tumourTabbedPane.getSelectedComponent());
    }

    @Action
    public void changePatientRecord() {
        this.actionPerformed(new ActionEvent(tumourTabbedPane.getSelectedComponent(), 0, RecordEditorMainFrame.CHANGE_PATIENT_RECORD));
    }

    @Action
    public void deletePatientRecord() {
        this.actionPerformed(new ActionEvent(patientTabbedPane.getSelectedComponent(), 0, RecordEditorMainFrame.DELETE));
    }

    @Action
    public void deleteTumourRecord() {
        this.actionPerformed(new ActionEvent(tumourTabbedPane.getSelectedComponent(), 0, RecordEditorMainFrame.DELETE));
    }

    @Action
    public void setObsoleteFlag() {
        this.actionPerformed(new ActionEvent(tumourTabbedPane.getSelectedComponent(), 0, RecordEditorMainFrame.OBSOLETE));
    }

    /**
     * This action is using a SwingWorker as it needs to display the waitFrame * before * the action is triggered.
     * That way, the waitFrame will be displayed first instead of being added to the action queue to be displayed
     * right after the completion of the actionEvent - which doesn't make sense for a waitFrame.
     *
     * Currently, the worker used here can only used by "person_search" action to display the waitFrame. For others
     * action, a new worker needs to be created.
     *
     * Please note, the "Check" action can't be launched inside the worker as it uses an autofill that can't be
     * triggered from a SwingWorker
     *
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SwingWorker<Object, ActionEvent> worker = new SwingWorker<Object, ActionEvent>() {

            PersonSearchResults personSearchResults;

            @Override
            protected Object doInBackground() throws Exception {
                waitFrame.setSelected(true);
                waitFrame.setVisible(true);
                triggerAction(e);
                return true;
            }

            public void triggerAction(ActionEvent e) {
                Object source = e.getSource();
                if (source instanceof RecordEditorPanel && e.getActionCommand().equalsIgnoreCase(PERSON_SEARCH)) {
                    RecordEditorPanel recordEditorPanel = (RecordEditorPanel) source;
                    Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                    setCursor(hourglassCursor);
                    personSearchResults = runPersonSearch((RecordEditorPatient) recordEditorPanel);
                }
            }

            @Override
            protected void done() {
                waitFrame.setVisible(false);
                handleRunPersonSearchResults(personSearchResults);
            }
        };

        if (e.getActionCommand().equalsIgnoreCase(PERSON_SEARCH)) {
            worker.execute();
        } else {
            Object source = e.getSource();
            if (e.getActionCommand().equalsIgnoreCase(REQUEST_FOCUS)) {
                try {
                    setSelected(true);
                } catch (PropertyVetoException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            } else if (source instanceof RecordEditorPanel) {
                RecordEditorPanel recordEditorPanel = (RecordEditorPanel) source;
                if (e.getActionCommand().equalsIgnoreCase(CHANGED)) {
                    //changesDone();
                } else if (e.getActionCommand().equalsIgnoreCase(DELETE)) {
                    deleteRecord(recordEditorPanel);
                } else if (e.getActionCommand().equalsIgnoreCase(CHECKS)) {
                    runChecks(recordEditorPanel);
                    //} else if (e.getActionCommand().equalsIgnoreCase(SAVE)) {
                    //    saveRecord(recordEditorPanel);
                } else if (e.getActionCommand().equalsIgnoreCase(CHANGE_PATIENT_RECORD)) {
                    changePatientRecord((RecordEditorTumour) recordEditorPanel);
                } else if (e.getActionCommand().equalsIgnoreCase(OBSOLETE)) {
                    int option = JOptionPane.showConfirmDialog(null,
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("REALLY CHANGE OBSOLETE-STATUS?"),
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("REALLY CHANGE OBSOLETE-STATUS?"),
                            JOptionPane.YES_NO_OPTION);
                    boolean toggle = (option == JOptionPane.YES_OPTION);
                    toggleObsolete(toggle, (RecordEditorTumour) tumourTabbedPane.getSelectedComponent());
                    if (toggle) {
                        refreshShowObsolete();
                    }
                } else if (e.getActionCommand().equalsIgnoreCase(RUN_MP)) {
                    runMPsearch((RecordEditorTumour) recordEditorPanel);
                } else if (e.getActionCommand().equalsIgnoreCase(RUN_EXACT)) {
                    runExactSearch((RecordEditorPatient) recordEditorPanel);
                } else if (e.getActionCommand().equalsIgnoreCase(CALC_AGE)) {
                    // this should be called at any time any of the fields birth date or incidence date gets changed
                    DatabaseRecord sourceDatabaseRecord = recordEditorPanel.getDatabaseRecord();
                    DatabaseRecord patientDatabaseRecord;
                    // TODO: implement calculate age
                    if (sourceDatabaseRecord instanceof Tumour) {
                        RecordEditorPanel patientRecordEditorPanel = (RecordEditorPanel) patientTabbedPane.getSelectedComponent();
                        patientDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
                    } else {
                        // get all the tumour records
                    }
                    // ???
                    // doesn't seem to be usefull nor to work: it only "refreshes" either the tumour or the patient
                    // it doesn't refresh the second one and leaves it empty
                } else if (e.getActionCommand().equalsIgnoreCase(AUTO_FILL)) {
                    LinkedList<DatabaseVariablesListElement> autoFillList = recordEditorPanel.getAutoFillList();
                    DatabaseRecord sourceOfActionDatabaseRecord = recordEditorPanel.getDatabaseRecord();
                    DatabaseRecord otherDatabaseRecord;
                    if (sourceOfActionDatabaseRecord instanceof Tumour) {
                        RecordEditorPatient patientRecordEditorPanel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
                        otherDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
                        autoFillHelper.autoFill(autoFillList, sourceOfActionDatabaseRecord, otherDatabaseRecord, recordEditorPanel);
                        patientRecordEditorPanel.refreshDatabaseRecord(otherDatabaseRecord, patientRecordEditorPanel.isSaveNeeded());
                        recordEditorPanel.refreshDatabaseRecord(sourceOfActionDatabaseRecord, recordEditorPanel.isSaveNeeded());
                    } else if (sourceOfActionDatabaseRecord instanceof Patient) {
                        RecordEditorTumour tumourRecordEditorPanel = (RecordEditorTumour) tumourTabbedPane.getSelectedComponent();
                        otherDatabaseRecord = tumourRecordEditorPanel.getDatabaseRecord();
                        autoFillHelper.autoFill(autoFillList, sourceOfActionDatabaseRecord, otherDatabaseRecord, recordEditorPanel);
                        tumourRecordEditorPanel.refreshDatabaseRecord(otherDatabaseRecord, tumourRecordEditorPanel.isSaveNeeded());
                        recordEditorPanel.refreshDatabaseRecord(sourceOfActionDatabaseRecord, recordEditorPanel.isSaveNeeded());
                    }
//                 autoFillHelper.autoFill(autoFillList, sourceOfActionDatabaseRecord, otherDatabaseRecord, recordEditorPanel);
                }
            }
        }
    }

    private DatabaseRecord saveRecord(DatabaseRecord databaseRecord)
            throws SecurityException, RemoteException, SQLException, RecordLockedException {
        DatabaseRecord newDatabaseRecord = null;
        if (databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) == null
                && databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) == null) {
            // id is the internal database id
            int id = canreg.client.CanRegClientApp.getApplication().saveRecord(databaseRecord, server);
            if (databaseRecord instanceof Patient) {
                // databaseRecord.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, id);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.PATIENT_TABLE_NAME, true, server);
                patientRecords.remove(databaseRecord);
                patientRecords.add(newDatabaseRecord);
            } else if (databaseRecord instanceof Tumour) {
                // databaseRecord.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, id);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.TUMOUR_TABLE_NAME, true, server);
                tumourRecords.remove(databaseRecord);
                tumourRecords.add(newDatabaseRecord);
            }
        } else {
            int id;
            if (databaseRecord instanceof Patient) {
                id = (Integer) databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.PATIENT_TABLE_NAME, server);
                canreg.client.CanRegClientApp.getApplication().editRecord(databaseRecord, server);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.PATIENT_TABLE_NAME, true, server);
                patientRecords.remove(databaseRecord);
                patientRecords.add(newDatabaseRecord);
            } else if (databaseRecord instanceof Tumour) {
                id = (Integer) databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.TUMOUR_TABLE_NAME, server);
                canreg.client.CanRegClientApp.getApplication().editRecord(databaseRecord, server);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.TUMOUR_TABLE_NAME, true, server);
                tumourRecords.remove(databaseRecord);
                tumourRecords.add(newDatabaseRecord);
            }
        }
        return newDatabaseRecord;
    }

    private boolean associateTumourRecordToPatientRecord(DatabaseRecord tumourDatabaseRecord,
                                                         DatabaseRecord patientDatabaseRecord) {
        boolean success;
        if (patientDatabaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
            Object patientID = patientDatabaseRecord.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                    .getDatabaseVariableName());
            tumourDatabaseRecord.setVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString())
                            .getDatabaseVariableName(),
                    patientID);
            tumourDatabaseRecord.setVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                            .getDatabaseVariableName(),
                    patientDatabaseRecord.getVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                            .getDatabaseVariableName()));
            success = true;
        } else {
            success = false;
        }
        return success;
    }

    private DatabaseRecord associatePatientRecordToPatientID(DatabaseRecord patientDatabaseRecord, String patientID) {
        patientDatabaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                        .getDatabaseVariableName(),
                patientID);
        return patientDatabaseRecord;
    }

    private DatabaseRecord associateTumourRecordToPatientID(DatabaseRecord tumourDatabaseRecord, String patientID) {
        tumourDatabaseRecord.setVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString())
                        .getDatabaseVariableName(),
                patientID);
        tumourDatabaseRecord.setVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString())
                        .getDatabaseVariableName(),
                patientID);
        tumourDatabaseRecord.setVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString())
                        .getDatabaseVariableName(),
                Globals.RECORD_STATUS_PENDING_CODE);
        tumourDatabaseRecord.setVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUnduplicationStatus.toString())
                        .getDatabaseVariableName(),
                Globals.UNDUPLICATION_NOT_DONE_CODE);
        return tumourDatabaseRecord;
    }

    private void refreshShowObsolete() {
        boolean showObsolete = showObsoleteRecordsCheckBox.isSelected();
        for (Component comp : patientTabbedPane.getComponents()) {
            RecordEditorPatient rep = (RecordEditorPatient) comp;
            DatabaseRecord dbr = rep.getDatabaseRecord();
            String obsoleteFlag = (String) dbr.getVariable(patientObsoleteVariableName);
            if (!showObsolete && obsoleteFlag.equals(Globals.OBSOLETE_VALUE)) {
                patientTabbedPane.setEnabledAt(patientTabbedPane.indexOfComponent(rep), false);
            } else {
                patientTabbedPane.setEnabledAt(patientTabbedPane.indexOfComponent(rep), true);
            }
        }
        for (Component comp : tumourTabbedPane.getComponents()) {
            RecordEditorTumour rep = (RecordEditorTumour) comp;
            DatabaseRecord dbr = rep.getDatabaseRecord();
            String obsoleteFlag = (String) dbr.getVariable(tumourObsoleteVariableName);
            if (!showObsolete && obsoleteFlag.equals(Globals.OBSOLETE_VALUE)) {
                tumourTabbedPane.setEnabledAt(tumourTabbedPane.indexOfComponent(rep), false);
            } else {
                tumourTabbedPane.setEnabledAt(tumourTabbedPane.indexOfComponent(rep), true);
            }
        }
    }

    @Action
    public void toggleShowObsoleteRecords() {
        refreshShowObsolete();
    }

    @Action
    public void changePatientID() {
        String requestedPatientID = JOptionPane.showInputDialog(null,
                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("PLEASE ENTER PATIENT ID:"),
                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("CHANGE TO WHICH PATIENTID?"),
                JOptionPane.QUESTION_MESSAGE);
        if (requestedPatientID != null) {
            try {
                Patient[] patientDatabaseRecord = CanRegClientApp.getApplication().getPatientsByPatientID(requestedPatientID, false, server);
                if (patientDatabaseRecord != null && patientDatabaseRecord.length > 0) {
                    for (DatabaseRecord patient : patientRecords) {
                        patient = associatePatientRecordToPatientID(patient, requestedPatientID);
                        saveRecord(patient);
                    }
                    for (DatabaseRecord tumour : tumourRecords) {
                        tumour = associateTumourRecordToPatientID(tumour, requestedPatientID);
                        saveRecord(tumour);
                    }
                    JOptionPane.showInternalMessageDialog(this,
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("RECORD MOVED."));
                } else {
                    JOptionPane.showInternalMessageDialog(this,
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("NO SUCH PATIENT ID."),
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("FAILED"),
                            JOptionPane.WARNING_MESSAGE);
                    changePatientID();
                }
            } catch (canreg.server.database.RecordLockedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("RECORD_LOCKED"),
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("RECORD_LOCKED"),
                        JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        } else {
            // do nothing - cancel operation...
        }
    }

    private void releaseRecords() {
        // Release all patient records held
        for (DatabaseRecord record : patientRecords) {
            try {
                Object idObj = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                if (idObj != null) {
                    int id = (Integer) idObj;
                    canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.PATIENT_TABLE_NAME, server);
                }
            } catch (RemoteException | SecurityException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        patientRecords.clear();

        // Release all tumour records held
        for (DatabaseRecord record : tumourRecords) {
            Tumour tumour = (Tumour) record;
            // Release all sources
            for (Source source : tumour.getSources()) {
                try {
                    Object idObj = source.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                    if (idObj != null) {
                        int id = (Integer) idObj;
                        canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.SOURCE_TABLE_NAME, server);
                    }
                } catch (RemoteException | SecurityException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
            try {
                Object idObj = tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                if (idObj != null) {
                    int id = (Integer) idObj;
                    canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.TUMOUR_TABLE_NAME, server);
                }
            } catch (RemoteException | SecurityException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        tumourRecords.clear();
    }

    public JSplitPane getMainSplitPane() {
        return this.jSplitPane1;
    }

    /**
     * Updates the "sequence" and "total" variables of all open tumours ONLY if
     * their "sequence" or "total" value has changed (this applies when the
     * record is new). This implementation considers that ALL open tumours
     * belong to the same patient (it does not takes under consideration if the
     * user prefers to have different sequencing for tumours belonging to
     * different open patients, the user will have to open each patient on a
     * separate RecordEditor and save changes for the sequences to change). This
     * method DOES NOT save these updates in the DB, but it DOES re-draw the
     * labels for the "sequence" and "tumour" variables shown in the
     * RecordEditorTumour. Because there is a memory leak when saving tumours,
     * these method should be called RIGHT BEFORE saving a tumour.
     *
     * @param tumoursToUpdate collection of tumours to be updated.
     */
    private void updateAllTumoursSequences(Collection<RecordEditorTumour> tumoursToUpdate) {
        int totalTumours = 0;
        for (RecordEditorTumour tumourRecord : tumoursToUpdate) {
            Tumour tumour = (Tumour) tumourRecord.getDatabaseRecord();
            boolean obsolete = tumour.getVariable(tumourObsoleteVariableName)
                    .toString().equalsIgnoreCase(Globals.OBSOLETE_VALUE);
            if (!obsolete) {
                totalTumours++;
            }
        }

        int tumourSequence = 0;
        for (RecordEditorTumour tumour : tumoursToUpdate) {
            boolean tumourChanged = false;
            Tumour tumourRecord = (Tumour) tumour.getDatabaseRecord();
            boolean obsolete = tumourRecord.getVariable(tumourObsoleteVariableName)
                    .toString().equalsIgnoreCase(Globals.OBSOLETE_VALUE);

            if (!obsolete) {

                //IARC CanReg's manual states that if only 1 tumour is present, the
                //sequence of that only tumour is 0
                if (totalTumours > 1) {
                    tumourSequence++;
                }

                //If the "sequence" value already present is different than the
                //newly calculated value, then the value is updated and the tumour
                //will have to be saved (even if it didn't have changes in the
                //rest of the data)
                if (!((String) tumourRecord.getVariable(tumourSequenceVariableName))
                        .equalsIgnoreCase(Integer.toString(tumourSequence))) {
                    tumourRecord.setVariable(tumourSequenceVariableName, tumourSequence + "");
                    tumourChanged = true;
                }
            } else {
                if (!((String) tumourRecord.getVariable(tumourSequenceVariableName))
                        .equalsIgnoreCase(Integer.toString(tumourSequence))) {
                    tumourRecord.setVariable(tumourSequenceVariableName, "-");
                    tumourChanged = true;
                }
            }

            if (!((String) tumourRecord.getVariable(tumourSequenceTotalVariableName))
                    .equalsIgnoreCase(Integer.toString(totalTumours))) {
                tumourRecord.setVariable(tumourSequenceTotalVariableName, totalTumours + "");
                tumourChanged = true;
            }

            //Tumour with data changes from before entering this method will need
            //to be saved (it doesn't matter if the values in this method were 
            //not updated).
            if (tumourChanged) {
                //DO NOT TOUCH THIS ORDER OF EXECUTION!!!!!! OTHERWISE
                //THE RECORD STATUS WILL BREAK WHEN CREATING/DELETING TUMOURS!!
                //ALSO DO NOT TRY TO UNDERSTAND WHY A FALSE WAS PASSED FIRST
                //AND A TRUE RIGHT AFTER, JUST PRETEND THIS IS PERFECT
                tumour.refreshDatabaseRecord(tumourRecord, false);
                tumour.setSaveNeeded(true);
            }
        }
    }

    private void deleteRecord(RecordEditorPanel recordEditorPanel) {
        int option = JOptionPane.showConfirmDialog(null,
                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("PERMANENTLY DELETE RECORD?"));
        if (option == JOptionPane.YES_OPTION) {
            DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
            String patientTitle = null;
            if (record instanceof Patient) {
                for (int i = 0; i < patientTabbedPane.getComponentCount(); i++) {
                    if (this.patientTabbedPane.getComponentAt(i) == recordEditorPanel) {
                        patientTitle = this.patientTabbedPane.getTitleAt(i);
                    }
                }
            }

            boolean success = deleteRecord(record);
            if (success) {
                if (record instanceof Patient) {
                    this.patientTabbedPane.remove((Component) recordEditorPanel);
                    //Remove the deleted patient from all tumours
                    for (Component tumourPanel : this.tumourTabbedPane.getComponents()) {
                        ((RecordEditorTumour) tumourPanel).removeLinkablePatient(patientTitle);
                    }
                } else if (record instanceof Tumour) {
                    tumourTabbedPane.remove((Component) recordEditorPanel);
                    //We update the sequence of all remaining tumours
                    LinkedList<RecordEditorTumour> tumoursLeft = new LinkedList<RecordEditorTumour>();
                    for (Component comp : tumourTabbedPane.getComponents()) {
                        tumoursLeft.add((RecordEditorTumour) comp);
                    }
                    updateAllTumoursSequences(tumoursLeft);
                    for (int i = 0; i < tumourTabbedPane.getTabCount(); i++) {
                        try {
                            RecordEditorTumour tumour = (RecordEditorTumour) tumourTabbedPane.getComponentAt(i);
                            tumour.prepareToSaveRecord();
                            this.saveRecord(tumour);
                        } catch (SaveRecordException ex) {
                            LOGGER.log(Level.SEVERE, null, ex.getLocalizedMessage());
                        }
                    }
                }

                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                .getString("RECORD DELETED."));
            } else {
                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                .getString("RECORD NOT DELETED.ERROR OCCURED..."));
            }
        }
    }

    private void runChecks(RecordEditorPanel recordEditorPanel) {
        RecordEditorTumour tumourRecordEditorPanel = null;
        RecordEditorPatient patientRecordEditorPanel = null;
        DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
        ResultCode worstResultCodeFound;
        String message = "";
        Patient patient;
        Tumour tumour;
        if (record instanceof Patient) {
            patient = (Patient) record;
            patientRecordEditorPanel = (RecordEditorPatient) recordEditorPanel;
            tumourRecordEditorPanel = (RecordEditorTumour) tumourTabbedPane.getSelectedComponent();
            tumour = (Tumour) tumourRecordEditorPanel.getDatabaseRecord();
        } else {
            tumour = (Tumour) record;
            tumourRecordEditorPanel = (RecordEditorTumour) recordEditorPanel;

            //The checks are run against the currently selected patient on the 
            //"Tumour linked to" comboBox
            for (int i = 0; i < this.patientTabbedPane.getTabCount(); i++) {
                if (this.patientTabbedPane.getTitleAt(i).equals(((RecordEditorTumour) recordEditorPanel).getLinkedPatient())) {
                    patientRecordEditorPanel = (RecordEditorPatient) this.patientTabbedPane.getComponentAt(i);
                }
            }
            if (patientRecordEditorPanel == null) {
                patientRecordEditorPanel = (RecordEditorPatient) this.patientTabbedPane.getSelectedComponent();
            }
            patient = (Patient) patientRecordEditorPanel.getDatabaseRecord();
        }

        EditChecksInternalFrame editChecksInternalFrame = new EditChecksInternalFrame();

        // Check to see if all mandatory variables are there
        boolean allPresent = patientRecordEditorPanel.areAllVariablesPresent();
        allPresent = allPresent && tumourRecordEditorPanel.areAllVariablesPresent();

        if (!allPresent) {
            editChecksInternalFrame.setMandatoryVariablesTextAreaText(
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("MANDATORY VARIABLES MISSING."));
            worstResultCodeFound = CheckResult.ResultCode.Missing;
            message += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                    .getString("NOT PERFORMED.");
        } else {
            editChecksInternalFrame.setMandatoryVariablesTextAreaText(
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame").getString("ALL MANDATORY VARIABLES PRESENT."));
            // Run the checks on the data
            LinkedList<CheckResult> checkResults = canreg.client.CanRegClientApp.getApplication().performChecks(patient, tumour);

            Map<Globals.StandardVariableNames, CheckResult.ResultCode> mapOfVariablesAndWorstResultCodes
                    = new EnumMap<>(Globals.StandardVariableNames.class);
            worstResultCodeFound = CheckResult.ResultCode.OK;
            for (CheckResult result : checkResults) {
                if (result.getResultCode() != CheckResult.ResultCode.OK && result.getResultCode() != CheckResult.ResultCode.NotDone) {
                    if (!result.getResultCode().equals(CheckResult.ResultCode.Missing)) {
                        message += result + "\n";
                        worstResultCodeFound = CheckResult.decideWorstResultCode(result.getResultCode(), worstResultCodeFound);
                        for (Globals.StandardVariableNames standardVariableName : result.getVariablesInvolved()) {
                            CheckResult.ResultCode worstResultCodeFoundForThisVariable = mapOfVariablesAndWorstResultCodes.get(standardVariableName);
                            if (worstResultCodeFoundForThisVariable == null) {
                                mapOfVariablesAndWorstResultCodes.put(standardVariableName, result.getResultCode());
                            } else if (CheckResult.compareResultSets(result.getResultCode(), worstResultCodeFoundForThisVariable) > 0) {
                                mapOfVariablesAndWorstResultCodes.put(standardVariableName, result.getResultCode());
                            }
                        }
                    }
                }
                // LOGGER.log(Level.INFO, result.toString());
            }

            if (worstResultCodeFound != CheckResult.ResultCode.Invalid && worstResultCodeFound != CheckResult.ResultCode.Missing) {
                // If no errors were found we generate ICD10 code
                ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication()
                        .performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                if (conversionResult != null) {
                    if (conversionResult[0].getResultCode() != ConversionResult.ResultCode.Invalid) {
                        editChecksInternalFrame.setICD10TextFieldText(conversionResult[0].getValue() + "");
                        DatabaseVariablesListElement ICD10databaseVariablesElement
                                = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICD10.toString());
                        if (ICD10databaseVariablesElement != null) {
                            tumour.setVariable(ICD10databaseVariablesElement.getDatabaseVariableName(), conversionResult[0].getValue());
                        }
                    }
                }
                // ...and ICCC3 code
                conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumour);
                if (conversionResult != null) {
                    if (conversionResult[0].getResultCode() != ConversionResult.ResultCode.Invalid) {
                        editChecksInternalFrame.setICCCTextFieldText(conversionResult[0].getValue() + "");
                        DatabaseVariablesListElement ICCCdatabaseVariablesElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICCC.toString());
                        if (ICCCdatabaseVariablesElement != null) {
                            tumour.setVariable(ICCCdatabaseVariablesElement.getDatabaseVariableName(), conversionResult[0].getValue());
                        }
                    }
                }
            }

            tumourRecordEditorPanel.refreshDatabaseRecord(tumour, tumourRecordEditorPanel.isSaveNeeded());

            if (worstResultCodeFound == CheckResult.ResultCode.OK) {
                message += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("CROSS-CHECK CONCLUSION: VALID");
            } else {
                // set the various variable panels to respective warnings
                for (Globals.StandardVariableNames standardVariableName : mapOfVariablesAndWorstResultCodes.keySet()) {
                    DatabaseVariablesListElement dbvle = globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(standardVariableName.toString());

                    if (dbvle.getDatabaseTableName().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                        tumourRecordEditorPanel.setResultCodeOfVariable(dbvle.getDatabaseVariableName(),
                                mapOfVariablesAndWorstResultCodes.get(standardVariableName));
                    } else if (dbvle.getDatabaseTableName().equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                        patientRecordEditorPanel.setResultCodeOfVariable(dbvle.getDatabaseVariableName(),
                                mapOfVariablesAndWorstResultCodes.get(standardVariableName));
                    }
                }
            }
        }
        tumourRecordEditorPanel.setChecksResultCode(worstResultCodeFound);

        editChecksInternalFrame.setCrossChecksTextAreaText(message);
        editChecksInternalFrame.setResultTextFieldText(worstResultCodeFound.toString());

        CanRegClientView.showAndPositionInternalFrame(desktopPane, editChecksInternalFrame);
    }

    /**
     * Saves a record (one Patient or one Tumour, including the tumour sources).
     * It is VERY important that the caller of this method catches a possible
     * SaveRecordException() because there are multiple points of failure in
     * this method: server security exceptions, sql exceptions, locking
     * exceptions, etc.
     *
     * @param recordEditorPanel
     * @return
     * @throws SaveRecordException
     */
    private DatabaseRecord saveRecord(RecordEditorPanel recordEditorPanel)
            throws SaveRecordException {
        boolean OK = true;
        DatabaseRecord databaseRecord = recordEditorPanel.getDatabaseRecord();
        if (databaseRecord instanceof Tumour) {
            RecordEditorTumour tumourPanel = (RecordEditorTumour) recordEditorPanel;
            String linkedPatient = tumourPanel.getLinkedPatient();

            //There's no linked patient. If there's only one patient tab then we
            //automatically link the tumour to that patient. If there's more than
            //one tab patient then we ask the user to select a patient.
            if (linkedPatient == null) {
                if (this.patientTabbedPane.getTabCount() > 1) {
                    throw new SaveRecordException(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("PLEASE SELECT PATIENT RECORD FIRST."));
                } else {
                    tumourPanel.setLinkedPatient(this.patientTabbedPane.getTitleAt(0));
                    linkedPatient = this.patientTabbedPane.getTitleAt(0);
                }
            }

            RecordEditorPatient patientRecordEditorPanel = null;
            DatabaseRecord patientDatabaseRecord = null;
            for (int i = 0; i < this.patientTabbedPane.getTabCount(); i++) {
                if (this.patientTabbedPane.getTitleAt(i).equals(linkedPatient)) {
                    patientRecordEditorPanel = (RecordEditorPatient) this.patientTabbedPane.getComponentAt(i);
                }
            }
            if (patientRecordEditorPanel == null) {
                throw new SaveRecordException(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("PATIENT NOT OPEN."));
            } else {
                patientDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
            }

            OK = associateTumourRecordToPatientRecord(databaseRecord, patientDatabaseRecord);
            if (!OK) {
                throw new SaveRecordException(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("PLEASE SAVE PATIENT RECORD FIRST."));
            }
        } else if (databaseRecord instanceof Patient) {
            // see if the patient has been given ID already by looking at other patient records
            String patientID = null;
            for (DatabaseRecord patient : patientRecords) {
                Object tempPatientID = patient.getVariable(patientIDVariableName);
                if (patientID == null && tempPatientID != null) {
                    patientID = (String) tempPatientID;
                    if (patientID.trim().length() == 0) {
                        patientID = null;
                    }
                }
            }
            if (patientID == null) {
                databaseRecord.setVariable(patientIDVariableName, null);
                databaseRecord.setVariable(patientRecordIDVariableName, null);
            } else {
                databaseRecord.setVariable(patientIDVariableName, patientID);
            }
        }
        if (OK) {
            try {
                databaseRecord = saveRecord(databaseRecord);

                //false is passed as the second parameter because if we get up to
                //here, then the record has already been succesfully saved in the database :)
                recordEditorPanel.refreshDatabaseRecord(databaseRecord, false);

                //If the saved record is a patient, then this patient tab and
                //its tumours must refresh the patient id/title
                if (databaseRecord instanceof Patient) {
                    addToPatientMap((RecordEditorPatient) recordEditorPanel, databaseRecord);
                    refreshTitles(recordEditorPanel);
                }
            } //Bubble all exceptions all the way to saveAllAction(), so all JOptionPanes
            //are handled from there.
            catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                throw new SaveRecordException(ex.getLocalizedMessage());
            }
        }
        return databaseRecord;
    }

    private void changePatientRecord(RecordEditorTumour tumourRecordEditorPanel) {
        Tumour tumourDatabaseRecord = (Tumour) tumourRecordEditorPanel.getDatabaseRecord();
        String requestedPatientRecordID = JOptionPane.showInputDialog(null,
                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("PLEASE ENTER PATIENT RECORD ID:"),
                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                        .getString("MOVE TUMOUR TO WHICH PATIENT RECORD?"),
                JOptionPane.QUESTION_MESSAGE);
        if (requestedPatientRecordID != null) {
            // First see if it is one of the records shown
            RecordEditorPatient patientRecordEditorPanel = patientRecordsMap.get(requestedPatientRecordID);
            Patient patientDatabaseRecord = null;
            if (patientRecordEditorPanel != null) {
                patientDatabaseRecord = (Patient) patientRecordEditorPanel.getDatabaseRecord();
            } else {
                try {
                    patientDatabaseRecord = CanRegClientApp.getApplication().getPatientRecord(requestedPatientRecordID, false, server);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }

            if (patientDatabaseRecord != null) {
                boolean OK = associateTumourRecordToPatientRecord(tumourDatabaseRecord,
                        patientDatabaseRecord);
                if (OK) {
                    try {
                        saveRecord(tumourDatabaseRecord);
                        tumourTabbedPane.remove(tumourRecordEditorPanel);
                        JOptionPane.showInternalMessageDialog(this,
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                        .getString("RECORD MOVED."));
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                        new TechnicalError().errorDialog();
                    }
                    tumourRecordEditorPanel.refreshDatabaseRecord(tumourDatabaseRecord, false);
                } else {
                    JOptionPane.showInternalMessageDialog(this,
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("PLEASE SAVE PATIENT RECORD FIRST."),
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("FAILED"),
                            JOptionPane.WARNING_MESSAGE);
                }

            } else {
                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                .getString("NO SUCH PATIENT RECORD."),
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                .getString("FAILED"),
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void runMPsearch(RecordEditorTumour recordEditorPanel) {
        DatabaseRecord databaseRecordA = recordEditorPanel.getDatabaseRecord();
        String topographyA = (String) databaseRecordA.getVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Topography.toString())
                .getDatabaseVariableName());
        String morphologyA = (String) databaseRecordA.getVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Morphology.toString())
                .getDatabaseVariableName());

        MultiplePrimaryTesterInterface multiplePrimaryTester = new DefaultMultiplePrimaryTester();
        if (tumourTabbedPane.getComponents().length > 1) {
            for (Component tumourPanelComponent : tumourTabbedPane.getComponents()) {
                RecordEditorTumour tumourPanel = (RecordEditorTumour) tumourPanelComponent;
                if (!recordEditorPanel.equals(tumourPanel)) {
                    DatabaseRecord dbr = tumourPanel.getDatabaseRecord();
                    String topographyB = (String) dbr.getVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Topography.toString())
                            .getDatabaseVariableName());
                    String morphologyB = (String) dbr.getVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Morphology.toString())
                            .getDatabaseVariableName());
                    int result = multiplePrimaryTester.multiplePrimaryTest(topographyA, morphologyA, topographyB, morphologyB);
                    databaseRecordA.setVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString())
                            .getDatabaseVariableName(), result);

                    if (result == MultiplePrimaryTesterInterface.mptDuplicate) {
                        // set pending
                        recordEditorPanel.setPending();
                    }

                    JOptionPane.showInternalMessageDialog(this,
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("RESULT: ") + MultiplePrimaryTesterInterface.mptCodes[result],
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("RESULT"),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showInternalMessageDialog(this,
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("ONLY ONE TUMOUR."),
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("RESULT"),
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Gets the RecordEditorPatient's databaseRecord and triggers performDuplicateSearch()
     * @param recordEditorPanel The panel containing data of the targeted patient to look for duplicates in the database
     * @return PersonSearchResults : The data to be displayed resulting of the search
     */
    private PersonSearchResults runPersonSearch(RecordEditorPatient recordEditorPanel) {
        Map<String, Float> map;
        try {
            DatabaseRecord sourceOfActionDatabaseRecord = recordEditorPanel.getDatabaseRecord();
            // buildDatabaseRecord();
            map = canreg.client.CanRegClientApp.getApplication().performDuplicateSearch((Patient) sourceOfActionDatabaseRecord, null, server);
            //remove patients with the same patientID -- already mapped
            String patientRecordID = (String) sourceOfActionDatabaseRecord.getVariable(patientIDVariableName);
            String records = "";
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            if (map.keySet().size() > 0) {
                // add records to the comparator
                ComparePatientsInternalFrame cpif = new ComparePatientsInternalFrame(desktopPane);
                cpif.addMainRecordSet((Patient) sourceOfActionDatabaseRecord, null);
                for (String prid : map.keySet()) {
                    if (patientRecordID.equals(prid)) {
                        // do nothing
                    } else {
                        try {
                            Patient patient2 = canreg.client.CanRegClientApp.getApplication().getPatientRecord(prid, false, server);
                            cpif.addRecordSet(patient2, null, map.get(prid));
                            records += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString("PATIENT ID: ") + patient2.getVariable(patientIDVariableName)
                                    + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                    .getString(", SCORE: ") + map.get(prid) + "%\n";
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            new TechnicalError().errorDialog();
                        }
                    }
                }
                setCursor(normalCursor);
                return new PersonSearchResults(records, this, cpif);

            } else {
                setCursor(normalCursor);
                return new PersonSearchResults("", this, null);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        } finally {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
        }
        return null; // this can't happen
    }

    /**
     * The purpose of the "PersonSearchResults" class is to separate the data handling and processing from the data displaying
     * By doing so, it is possible to "close" the loading window when the "ok" button is displayed instead of closing
     * them at the same time. This is only done like this for UI purposes.
     * The data processing is handled by runPersonSearch and the data display is handled by handleRunPersonSearchResults()
     */
    static final class PersonSearchResults {
        private final String records;
        private final Component parentComponent;
        private final ComparePatientsInternalFrame cpif;

        public PersonSearchResults(String records, Component parentComponent, ComparePatientsInternalFrame cpif) {
            this.records = records;
            this.parentComponent = parentComponent;
            this.cpif = cpif;
        }
    }

    /**
     * displays the results of runPersonSearch(): open an information box then displays the results of the search, if there's any
     * @param results The results from running runPersonSearch()
     */
    private void handleRunPersonSearchResults(PersonSearchResults results) {
        if (results == null) return; // this line should never be triggered as runPersonSearch shouldn't return null
        if (!results.records.equalsIgnoreCase("")) { // potential duplicates found
            JOptionPane.showInternalMessageDialog(results.parentComponent,
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("POTENTIAL DUPLICATES FOUND:") + results.records);
            CanRegClientView.showAndPositionInternalFrame(desktopPane, results.cpif);
        } else { // no potential duplicate found
            JOptionPane.showInternalMessageDialog(results.parentComponent,
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                            .getString("NO POTENTIAL DUPLICATES FOUND."));
            // recordEditorPanel.setPersonSearchStatus();
        }
    }

    private boolean deleteRecord(DatabaseRecord record) {
        boolean success = false;
        int id = -1;
        String tableName = null;
        if (record instanceof Patient) {
            Object idObject = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) {
                id = (Integer) idObject;
            }
            tableName = Globals.PATIENT_TABLE_NAME;
        } else if (record instanceof Tumour) {
            // delete sources first.
            Tumour tumour = (Tumour) record;
            for (Source source : tumour.getSources()) {
                deleteRecord(source);
            }

            Object idObject = record.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) {
                id = (Integer) idObject;
            }
            tableName = Globals.TUMOUR_TABLE_NAME;
        } else if (record instanceof Source) {
            Object idObject = record.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) {
                id = (Integer) idObject;
            }
            tableName = Globals.SOURCE_TABLE_NAME;
        }
        if (id >= 0) {
            try {
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, tableName, server);
                success = canreg.client.CanRegClientApp.getApplication().deleteRecord(id, tableName, server);
            } catch (SQLException ex) {
                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditorMainFrame")
                                .getString("THIS RECORD HAS OTHER RECORDS ASSIGNED TO IT.PLEASE DELETE OR MOVE THOSE FIRST."));
                LOGGER.log(Level.WARNING, null, ex);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        return success;
    }

    @Action
    public void writePDF() {
        Set<DatabaseRecord> records = new LinkedHashSet();
        RecordEditorPatient panel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
        records.add(panel.getDatabaseRecord());
        // String fileName = CanRegClientApp.getApplication().getLocalSettings().getProperty(LocalSettings.WORKING_DIR_PATH_KEY) + Globals.FILE_SEPARATOR + "Patient";
        String fileName = Globals.CANREG_PATIENT_PDFS_FOLDER + Globals.FILE_SEPARATOR;

        Object id = panel.getDatabaseRecord().getPatientID();
        if (id != null && id.toString().length() > 0) {
            fileName += id.toString();
        } else {
            fileName += "Patient";
        }
        fileName += ".pdf";

        for (Component component : tumourTabbedPane.getComponents()) {
            RecordEditorTumour tumourPanel = (RecordEditorTumour) component;
            Tumour tumour = (Tumour) tumourPanel.getDatabaseRecord();
            records.add(tumour);
            records.addAll(tumour.getSources());
        }

        JComponentToPDF.databaseRecordsToPDF(records, fileName, globalToolBox);
        // System.out.println("Written to ");
        try {
            canreg.common.Tools.openFile(fileName);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
    }

    private void runExactSearch(RecordEditorPatient recordEditorPanel) {
        DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
        String[] variables = record.getVariableNames();
        LinkedList searchStringComponents = new LinkedList<String>();
        Map<String, DatabaseVariablesListElement> map = globalToolBox.getVariablesMap();
        // make a list of variables to skip
        // and numbers
        Set<String> skippable = new TreeSet<String>();
        Set<String> numbers = new TreeSet<String>();
        for (String key : map.keySet()) {
            DatabaseVariablesListElement databaseVariable = map.get(key);
            String stdVarbName = databaseVariable.getStandardVariableName();
            if (stdVarbName == null) {
                stdVarbName = "";
            }
            if (!stdVarbName.equals(Globals.StandardVariableNames.PatientCheckStatus.toString())
                    && !stdVarbName.equals(Globals.StandardVariableNames.PatientID.toString())
                    && !stdVarbName.equals(Globals.StandardVariableNames.PatientRecordID.toString())
                    && !stdVarbName.equals(Globals.StandardVariableNames.PatientRecordStatus.toString())
                    && !stdVarbName.equals(Globals.StandardVariableNames.PatientUpdateDate.toString())
                    && !stdVarbName.equals(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString())
                    && !stdVarbName.equals(Globals.StandardVariableNames.PatientUpdatedBy.toString())) {
                skippable.add(canreg.common.Tools.toLowerCaseStandardized(key));
            }
            if (!databaseVariable.getVariableType().equals(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                numbers.add(canreg.common.Tools.toLowerCaseStandardized(key));
            }
        }

        for (String varb : variables) {
            String content = record.getVariableAsString(varb);
            DatabaseVariablesListElement databaseVariable = map.get(varb);
            if (!content.trim().isEmpty() && skippable.contains(canreg.common.Tools.toLowerCaseStandardized(varb))) {
                if (!numbers.contains(canreg.common.Tools.toLowerCaseStandardized(varb))) {
                    searchStringComponents.add(varb + "=" + content);
                } else {
                    searchStringComponents.add(varb + " LIKE " + "'%" + content + "%'");
                }
            }
        }

        String searchString = canreg.common.Tools.combine(searchStringComponents, " AND ");

        if (searchString.trim().isEmpty()) {
            JOptionPane.showInternalMessageDialog(this,
                    //                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("THIS RECORD HAS OTHER RECORDS ASSIGNED TO IT.PLEASE DELETE OR MOVE THOSE FIRST.")
                    "Please enter some data first.");
        } else {
            if (browser == null)
                browser = new BrowseInternalFrame(desktopPane, server);
            else {
                browser.close();
                desktopPane.remove(browser);
                desktopPane.validate();
                browser = new BrowseInternalFrame(desktopPane, server);
            }
            CanRegClientView.showAndPositionInternalFrame(desktopPane, browser);
            maximizeHeight(desktopPane, browser);
            browser.setFilterField(searchString);
            browser.setTable(Globals.PATIENT_TABLE_NAME);
            browser.actionPerformed(new ActionEvent(this, 1, "refresh"));
        }
    }

    public void setObsoleteToggleButtonSelected(RecordEditorTumour tumour, boolean selected) {
        this.obsoleteToggles.put(tumour, selected);
    }

    public boolean isObsoleteToggleButtonSelected(RecordEditorTumour tumour) {
        return this.obsoleteToggles.get(tumour);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        patientPopupMenu = new javax.swing.JPopupMenu();
        patientDeleteMenuItem = new javax.swing.JMenuItem();
        tumourPopupMenu = new javax.swing.JPopupMenu();
        tumourDeleteMenuItem = new javax.swing.JMenuItem();
        tumourObsoleteToggleButton = new javax.swing.JRadioButtonMenuItem();
        tumourChangePatientRecordMenuItem = new javax.swing.JMenuItem();
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
        filler21 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
        jPanel2 = new javax.swing.JPanel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jButton1 = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        showObsoleteRecordsCheckBox = new javax.swing.JCheckBox();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        filler22 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        filler23 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        saveAllButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        productionBtn = new javax.swing.JButton();
        viewProductionRecordBtn = new javax.swing.JButton();
        viewFullDataBtn = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jButton3 = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        printButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 32767));
        filler18 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
        filler19 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 25), new java.awt.Dimension(0, 25), new java.awt.Dimension(32767, 25));
        patientTabbedPane = new JTabbedPane();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        patientMenuButton = new javax.swing.JButton();
        filler20 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jPanel7 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 25), new java.awt.Dimension(0, 25), new java.awt.Dimension(32767, 25));
        tumourTabbedPane = new JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        addTumourRecordButton = new javax.swing.JButton();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        tumourMenuButton = new javax.swing.JButton();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditorMainFrame.class, this);
        patientDeleteMenuItem.setAction(actionMap.get("deletePatientRecord")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditorMainFrame.class);
        patientDeleteMenuItem.setText(resourceMap.getString("deleteMenuItem.text")); // NOI18N
        patientDeleteMenuItem.setName("deleteRecord"); // NOI18N
        patientDeleteMenuItem.setIcon(resourceMap.getIcon("deleteRecord.Action.icon"));
        patientPopupMenu.add(patientDeleteMenuItem);

        tumourDeleteMenuItem.setAction(actionMap.get("deleteTumourRecord")); // NOI18N
        tumourDeleteMenuItem.setText(resourceMap.getString("deleteMenuItem.text")); // NOI18N
        tumourDeleteMenuItem.setToolTipText(resourceMap.getString("deleteRecord.Action.shortDescription")); // NOI18N
        tumourDeleteMenuItem.setName("deleteRecord"); // NOI18N
        tumourDeleteMenuItem.setIcon(resourceMap.getIcon("deleteRecord.Action.icon"));
        tumourPopupMenu.add(tumourDeleteMenuItem);

        tumourObsoleteToggleButton.setAction(actionMap.get("setObsoleteFlag")); // NOI18N
        tumourObsoleteToggleButton.setSelected(true);
        tumourObsoleteToggleButton.setText(resourceMap.getString("obsoleteToggleButton.text")); // NOI18N
        tumourObsoleteToggleButton.setToolTipText(resourceMap.getString("setObsoleteFlag.Action.shortDescription")); // NOI18N
        tumourPopupMenu.add(tumourObsoleteToggleButton);

        tumourChangePatientRecordMenuItem.setAction(actionMap.get("changePatientRecord")); // NOI18N
        tumourChangePatientRecordMenuItem.setText(resourceMap.getString("changePatientRecord.Action.text")); // NOI18N
        tumourChangePatientRecordMenuItem.setToolTipText(resourceMap.getString("changePatientRecord.Action.shortDescription")); // NOI18N
        tumourPopupMenu.add(tumourChangePatientRecordMenuItem);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(1200, 524));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));
        getContentPane().add(filler17);
        getContentPane().add(filler21);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 28));
        jPanel2.setMinimumSize(new java.awt.Dimension(400, 28));
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 28));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add(filler9);
        jPanel2.add(filler6);

        jButton1.setAction(actionMap.get("changePatientID")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jPanel2.add(jButton1);
        jPanel2.add(filler5);

        showObsoleteRecordsCheckBox.setAction(actionMap.get("toggleShowObsoleteRecords")); // NOI18N
        showObsoleteRecordsCheckBox.setText(resourceMap.getString("showObsoleteRecordsCheckBox.text")); // NOI18N
        showObsoleteRecordsCheckBox.setFocusable(false);
        jPanel2.add(showObsoleteRecordsCheckBox);
        jPanel2.add(filler13);
        jPanel2.add(filler22);
        jPanel2.add(filler23);

        saveAllButton.setAction(actionMap.get("saveAllAction")); // NOI18N
        saveAllButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        saveAllButton.setText(resourceMap.getString("saveAllAction.Action.text")); // NOI18N
        saveAllButton.setToolTipText(resourceMap.getString("saveAllAction.Action.shortDescription")); // NOI18N
        saveAllButton.setActionCommand("      Save All      ");
        saveAllButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 0)));
        saveAllButton.setFocusable(false);
        saveAllButton.setMaximumSize(new java.awt.Dimension(150, 23));
        saveAllButton.setOpaque(true);
        saveAllButton.addMouseListener(new MouseAdapter() {
            //background and foreground colors change when the button is being pressed
            @Override
            public void mousePressed(MouseEvent e) {
                saveAllButton.setBackground(Color.WHITE);
                saveAllButton.setForeground(Color.BLACK);
            }

            //background and foreground colors return to original color when
            //the button is not being pressed (or focus color if the mouse is
            //still inside the button, which is usually the case)
            @Override
            public void mouseReleased(MouseEvent e) {
                if (mouseInsideSave) {
                    saveAllButton.setBackground(Color.GRAY);
                    saveAllButton.setForeground(Color.WHITE);
                } else {
                    saveAllButton.setBackground(Color.BLACK);
                    saveAllButton.setForeground(Color.WHITE);
                }
            }

            //background and foreground colors change when the mouse is on top
            //of the button.
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseInsideSave = true;
                saveAllButton.setBackground(Color.GRAY);
                saveAllButton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseInsideSave = false;
                saveAllButton.setBackground(Color.BLACK);
                saveAllButton.setForeground(Color.WHITE);
            }
        });
        jPanel2.add(saveAllButton);
        jPanel2.add(filler1);

        productionBtn.setAction(actionMap.get("productionButtonAction")); // NOI18N
        productionBtn.setText(resourceMap.getString("productionBtn.text")); // NOI18N
        jPanel2.add(productionBtn);

        viewProductionRecordBtn.setAction(actionMap.get("viewProductionRecordAction")); // NOI18N
        viewProductionRecordBtn.setText(resourceMap.getString("viewProductionRecordBtn.text")); // NOI18N
        jPanel2.add(viewProductionRecordBtn);

        viewFullDataBtn.setAction(actionMap.get("viewFullDataAction")); // NOI18N
        viewFullDataBtn.setText(resourceMap.getString("allCaseDataBtn.text")); // NOI18N
        jPanel2.add(viewFullDataBtn);
        jPanel2.add(filler4);

        jButton3.setAction(actionMap.get("writePDF")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setFocusable(false);
        jPanel2.add(jButton3);
        jPanel2.add(filler2);

        printButton.setAction(actionMap.get("printAction")); // NOI18N
        printButton.setText(resourceMap.getString("printButton.text")); // NOI18N
        printButton.setFocusable(false);
        jPanel2.add(printButton);
        jPanel2.add(filler3);

        getContentPane().add(jPanel2);
        getContentPane().add(filler18);
        getContentPane().add(filler19);

        jPanel1.setMaximumSize(new java.awt.Dimension(20000, 20000));
        jPanel1.setOpaque(false);

        jSplitPane1.setDividerSize(7);
        if (localSettings.isDataEntryVerticalSources()) {
            jSplitPane1.setResizeWeight(0.30);
            jSplitPane1.setDividerLocation(370);
        } else
            jSplitPane1.setResizeWeight(0.4);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSplitPane1.setUI(new DottedDividerSplitPane());

        jPanel3.setPreferredSize(new java.awt.Dimension(450, 451));
        jPanel3.setLayout(new javax.swing.OverlayLayout(jPanel3));

        jPanel11.setOpaque(false);
        jPanel11.setPreferredSize(new java.awt.Dimension(450, 451));
        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel11.add(filler15);

        patientTabbedPane.setFocusable(false);
        patientTabbedPane.setPreferredSize(new java.awt.Dimension(450, 451));
        jPanel11.add(patientTabbedPane);

        jPanel3.add(jPanel11);

        jPanel15.setOpaque(false);
        jPanel15.setPreferredSize(new java.awt.Dimension(450, 451));

        jPanel16.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel16.setMinimumSize(new java.awt.Dimension(20, 36));
        jPanel16.setOpaque(false);
        jPanel16.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel16.setLayout(new javax.swing.BoxLayout(jPanel16, javax.swing.BoxLayout.LINE_AXIS));
        jPanel16.add(filler16);

        patientMenuButton.setAction(actionMap.get("patientMenuAction")); // NOI18N
        patientMenuButton.setText(resourceMap.getString("menuButton.text")); // NOI18N
        patientMenuButton.setFocusable(false);
        patientMenuButton.setMaximumSize(new java.awt.Dimension(100, 23));
        patientMenuButton.setMinimumSize(new java.awt.Dimension(30, 23));
        jPanel16.add(patientMenuButton);
        jPanel16.add(filler20);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
                jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
                jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel15Layout.createSequentialGroup()
                                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 412, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel15);

        jSplitPane1.setLeftComponent(jPanel3);

        jPanel7.setPreferredSize(new java.awt.Dimension(700, 451));
        jPanel7.setLayout(new javax.swing.OverlayLayout(jPanel7));

        jPanel10.setFocusable(false);
        jPanel10.setOpaque(false);
        jPanel10.setPreferredSize(new java.awt.Dimension(700, 451));
        jPanel10.setRequestFocusEnabled(false);
        jPanel10.setVerifyInputWhenFocusTarget(false);
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.PAGE_AXIS));

        filler14.setEnabled(false);
        filler14.setFocusable(false);
        filler14.setRequestFocusEnabled(false);
        filler14.setVerifyInputWhenFocusTarget(false);
        jPanel10.add(filler14);

        tumourTabbedPane.setFocusable(false);
        tumourTabbedPane.setPreferredSize(new java.awt.Dimension(700, 451));
        tumourTabbedPane.setVerifyInputWhenFocusTarget(false);
        tumourTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tumourTabbedPaneStateChanged(evt);
            }
        });
        jPanel10.add(tumourTabbedPane);

        jPanel7.add(jPanel10);

        jPanel8.setOpaque(false);
        jPanel8.setPreferredSize(new java.awt.Dimension(700, 451));

        jPanel9.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel9.setMinimumSize(new java.awt.Dimension(20, 36));
        jPanel9.setOpaque(false);
        jPanel9.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));
        jPanel9.add(filler10);

        addTumourRecordButton.setAction(actionMap.get("addTumourAction")); // NOI18N
        addTumourRecordButton.setMaximumSize(new java.awt.Dimension(220, 23));
        addTumourRecordButton.setMinimumSize(new java.awt.Dimension(21, 23));
        //This is a hack to automatically put the focus on the first variable
        //of a tumour when a new tumour is added
        addTumourRecordButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                addTumourRecordButtonFocusGained(evt);
            }
        });
        jPanel9.add(addTumourRecordButton);
        jPanel9.add(filler11);

        tumourMenuButton.setAction(actionMap.get("tumourMenuAction")); // NOI18N
        tumourMenuButton.setText(resourceMap.getString("menuButton.text")); // NOI18N
        tumourMenuButton.setFocusable(false);
        tumourMenuButton.setMaximumSize(new java.awt.Dimension(100, 23));
        tumourMenuButton.setMinimumSize(new java.awt.Dimension(30, 23));
        jPanel9.add(tumourMenuButton);
        jPanel9.add(filler12);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 412, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel8);

        jSplitPane1.setRightComponent(jPanel7);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1080, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tumourTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tumourTabbedPaneStateChanged
        RecordEditorTumour tumourTab = (RecordEditorTumour) tumourTabbedPane.getSelectedComponent();
        if (tumourTab != null) {
            this.tumourObsoleteToggleButton.setSelected(this.obsoleteToggles.get(tumourTab));
        }
    }//GEN-LAST:event_tumourTabbedPaneStateChanged

    private void addTumourRecordButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_addTumourRecordButtonFocusGained
        //This is a nasty cheat to put the focus on the first variable of a newly
        //created tumour. We play with the OverlayLayout z-order to be able to put 
        //the focus on the panel that we want (which is jPanel8)
        jPanel7.remove(jPanel10);
        jPanel7.add(jPanel10);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
        jPanel7.remove(jPanel8);
        jPanel7.add(jPanel8);
    }//GEN-LAST:event_addTumourRecordButtonFocusGained

    @Action
    public void productionButtonAction() {
        boolean result = this.browser.productionButtonAction();
        if (result) {
            close();
        }
    }

    @Action
    public void viewFullDataAction() {
        StringBuilder formatErrors = new StringBuilder();
        StringBuilder rawData = new StringBuilder("<html>");
        HoldingRawDataInternalFrame frame = new HoldingRawDataInternalFrame();
        rawDataFrames.add(frame);

        String tumourIDVariable = globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString())
                .getDatabaseVariableName();
        String patientIDVariable = globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                .getDatabaseVariableName();

        //WE ALWAYS ASUME THERE'S ONLY ONE PATIENT TAB OPEN!!
        for (DatabaseRecord patient : this.patientRecords) {
            formatErrors.append("<strong>PATIENT " + patient.getVariableAsString(patientIDVariable)
                    + ":</strong><br>" + patient.getVariableAsString("format_errors") + "<br><br>");
            rawData.append("<strong>PATIENT " + patient.getVariableAsString(patientIDVariable) +
                    ":</strong><br>" + patient.getVariableAsString("raw_data") + "<br>");

            for (DatabaseRecord tumour : this.tumourRecords) {
                formatErrors.append("<strong>TUMOUR " + tumour.getVariableAsString(tumourIDVariable)
                        + ":</strong><br>" + tumour.getVariableAsString("format_errors") + "<br><br>");
                rawData.append("<strong>TUMOUR " + tumour.getVariableAsString(tumourIDVariable) +
                        ":</strong><br>" + tumour.getVariableAsString("raw_data") + "<br>");

                for (Source source : ((Tumour) tumour).getSources()) {
                    String sourceRecordId = source.getVariableAsString("sourcerecordid");
                    formatErrors.append("<strong>SOURCE " + sourceRecordId + ":</strong><br>")
                            .append(source.getVariableAsString("format_errors")).append("<br><br>");
                    rawData.append("<strong>SOURCE " + sourceRecordId + ":</strong><br>")
                            .append(source.getVariableAsString("raw_data")).append("<br><br>");
                }
            }
        }

        rawData.append("</html>");
        frame.setData(formatErrors.toString(), rawData.toString());
        CanRegClientView.showAndPositionInternalFrame(desktopPane, frame);
    }

    @Action
    public void viewProductionRecordAction() {
        if (this.productionRecordEditor != null) {
            CanRegClientView.showAndPositionInternalFrame(this.desktopPane, (JInternalFrame) productionRecordEditor);
            CanRegClientView.maximizeHeight(this.desktopPane, (JInternalFrame) productionRecordEditor);
        } else {
            JOptionPane.showMessageDialog(null,
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/RecordEditor").getString("RECORD NOT PRESENT"),
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/BrowseInternalFrame").getString("ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTumourRecordButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler18;
    private javax.swing.Box.Filler filler19;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler20;
    private javax.swing.Box.Filler filler21;
    private javax.swing.Box.Filler filler22;
    private javax.swing.Box.Filler filler23;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuItem patientDeleteMenuItem;
    private javax.swing.JButton patientMenuButton;
    private javax.swing.JPopupMenu patientPopupMenu;
    private javax.swing.JTabbedPane patientTabbedPane;
    private javax.swing.JButton printButton;
    private javax.swing.JButton productionBtn;
    private javax.swing.JButton saveAllButton;
    private javax.swing.JCheckBox showObsoleteRecordsCheckBox;
    private javax.swing.JMenuItem tumourChangePatientRecordMenuItem;
    private javax.swing.JMenuItem tumourDeleteMenuItem;
    private javax.swing.JButton tumourMenuButton;
    private javax.swing.JRadioButtonMenuItem tumourObsoleteToggleButton;
    private javax.swing.JPopupMenu tumourPopupMenu;
    private javax.swing.JTabbedPane tumourTabbedPane;
    private javax.swing.JButton viewFullDataBtn;
    private javax.swing.JButton viewProductionRecordBtn;
    // End of variables declaration//GEN-END:variables

}
