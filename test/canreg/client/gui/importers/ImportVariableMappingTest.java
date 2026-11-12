package canreg.client.gui.importers;

import canreg.TestFixtures;
import canreg.client.dataentry.Relation;
import canreg.client.gui.components.PreviewFilePanel;
import canreg.client.gui.components.VariableMappingAlternativePanel;
import canreg.client.gui.components.VariablesAssociationPanel;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import canreg.server.CanRegServerInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

/**
 * Unit and integration tests for variable mapping, learned mappings
 * persistence,
 * PreviewFilePanel event deduplication, and Patient-Tumour linking during
 * import
 * with non-standard column names.
 */
public class ImportVariableMappingTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Document doc;
    private GlobalToolBox globalToolBox;

    @Before
    public void setUp() throws Exception {
        doc = TestFixtures.loadTestSystemDocument();
        globalToolBox = new GlobalToolBox(doc);
        ensureApplicationInitialized(globalToolBox, doc);
    }

    private static void ensureApplicationInitialized(GlobalToolBox gtb, Document doc) throws Exception {
        java.lang.reflect.Field appField = org.jdesktop.application.Application.class.getDeclaredField("application");
        appField.setAccessible(true);
        canreg.client.CanRegClientApp app = (canreg.client.CanRegClientApp) appField.get(null);
        if (app == null) {
            app = new canreg.client.CanRegClientApp();
            appField.set(null, app);
        }
        java.lang.reflect.Field gtbField = canreg.client.CanRegClientApp.class.getDeclaredField("globalToolBox");
        gtbField.setAccessible(true);
        gtbField.set(app, gtb);
        java.lang.reflect.Field docField = canreg.client.CanRegClientApp.class.getDeclaredField("doc");
        docField.setAccessible(true);
        docField.set(app, doc);
        java.lang.reflect.Field convField = canreg.client.CanRegClientApp.class.getDeclaredField("converter");
        convField.setAccessible(true);
        convField.set(app, new canreg.common.conversions.Converter(gtb.getStandardVariables()));
    }

    @Test
    public void testConstructRelationsIsTableAwareAndMapsForeignKeyColumns() throws Exception {
        File storageFile = tempFolder.newFile("empty_mappings.properties");
        LearnedVariableMappings emptyLearned = new LearnedVariableMappings(storageFile);

        // In test-system.xml, PatientID short_name is "RegNo" (table Patient)
        // and PatientIDTumourTable short_name is "PatientIDtumourtable" (table Tumour).
        // When a Tumour CSV file uses "RegNo" or "PatientID", constructRelations for
        // Tumour
        // must map it to PatientIDtumourtable in the Tumour table (not RegNo in Patient
        // table).
        String[] tumourColumns = new String[] { "RegNo", "Top", "Mor", "Incid" };
        List<Relation> relations = Import.constructRelations(
                doc, tumourColumns, Globals.TUMOUR_TABLE_NAME, emptyLearned);

        assertEquals(4, relations.size());

        Relation regNoRel = relations.get(0);
        assertEquals("RegNo", regNoRel.getFileVariableName());
        assertEquals("Tumour table must receive PatientIDTumourTable mapping for 'RegNo' column",
                Globals.TUMOUR_TABLE_NAME, regNoRel.getDatabaseTableName());
        assertEquals("PatientIDTumourTable", regNoRel.getDatabaseVariableName());
        assertFalse("Exact foreign-key match is not marked as learned", regNoRel.isLearnedMapping());

        Relation topRel = relations.get(1);
        assertEquals(Globals.TUMOUR_TABLE_NAME, topRel.getDatabaseTableName());
        assertEquals("Top", topRel.getDatabaseVariableName());
    }

    @Test
    public void testLearnedVariableMappingsPersistenceAndCrossTableForeignKeySync() throws Exception {
        File storageFile = new File(tempFolder.getRoot(), "learned_mappings.properties");
        LearnedVariableMappings learned = new LearnedVariableMappings(storageFile);

        DatabaseVariablesListElement[] patientVars = Tools.getVariableListElements(
                doc, Globals.NAMESPACE, Globals.PATIENT_TABLE_NAME);
        DatabaseVariablesListElement[] tumourVars = Tools.getVariableListElements(
                doc, Globals.NAMESPACE, Globals.TUMOUR_TABLE_NAME);

        // Build manual relations for non-standard column names:
        // "pat_key" -> RegNo (PatientID), "sex_cd" -> Sex, "site_icdo" -> Top
        // (Topography)
        List<Relation> userMappings = new ArrayList<Relation>();

        for (DatabaseVariablesListElement v : patientVars) {
            if ("RegNo".equalsIgnoreCase(v.getDatabaseVariableName())) {
                Relation r = new Relation();
                r.setDatabaseTableName(Globals.PATIENT_TABLE_NAME);
                r.setDatabaseTableVariableID(v.getDatabaseTableVariableID());
                r.setDatabaseVariableName(v.getDatabaseVariableName());
                r.setFileColumnNumber(0);
                r.setFileVariableName("pat_key");
                r.setVariableType(v.getVariableType());
                userMappings.add(r);
            } else if ("Sex".equalsIgnoreCase(v.getDatabaseVariableName())) {
                Relation r = new Relation();
                r.setDatabaseTableName(Globals.PATIENT_TABLE_NAME);
                r.setDatabaseTableVariableID(v.getDatabaseTableVariableID());
                r.setDatabaseVariableName(v.getDatabaseVariableName());
                r.setFileColumnNumber(1);
                r.setFileVariableName("sex_cd");
                r.setVariableType(v.getVariableType());
                userMappings.add(r);
            }
        }

        for (DatabaseVariablesListElement v : tumourVars) {
            if ("Top".equalsIgnoreCase(v.getDatabaseVariableName())) {
                Relation r = new Relation();
                r.setDatabaseTableName(Globals.TUMOUR_TABLE_NAME);
                r.setDatabaseTableVariableID(v.getDatabaseTableVariableID());
                r.setDatabaseVariableName(v.getDatabaseVariableName());
                r.setFileColumnNumber(1);
                r.setFileVariableName("site_icdo");
                r.setVariableType(v.getVariableType());
                userMappings.add(r);
            }
        }

        learned.saveMappings(doc, userMappings);
        assertTrue("Storage file should be written to disk", storageFile.exists());

        // Reload from disk in a fresh instance
        LearnedVariableMappings reloaded = new LearnedVariableMappings(storageFile);

        // 1. Patient table should recall pat_key -> RegNo and sex_cd -> Sex
        String[] newPatientCols = new String[] { "pat_key", "sex_cd" };
        List<Relation> patientRels = Import.constructRelations(
                doc, newPatientCols, Globals.PATIENT_TABLE_NAME, reloaded);
        assertEquals("RegNo", patientRels.get(0).getDatabaseVariableName());
        assertTrue(patientRels.get(0).isLearnedMapping());
        assertEquals("Sex", patientRels.get(1).getDatabaseVariableName());
        assertTrue(patientRels.get(1).isLearnedMapping());

        // 2. Tumour table should recall site_icdo -> Top AND automatically infer
        // pat_key -> PatientIDTumourTable!
        String[] newTumourCols = new String[] { "pat_key", "site_icdo" };
        List<Relation> tumourRels = Import.constructRelations(
                doc, newTumourCols, Globals.TUMOUR_TABLE_NAME, reloaded);
        assertEquals("PatientIDTumourTable", tumourRels.get(0).getDatabaseVariableName());
        assertTrue("Cross-table foreign key from learned PatientID should be marked learned",
                tumourRels.get(0).isLearnedMapping());
        assertEquals("Top", tumourRels.get(1).getDatabaseVariableName());
        assertTrue(tumourRels.get(1).isLearnedMapping());
    }

    @Test
    public void testExactColumnMatchAlwaysWinsOverLearnedMapping() throws Exception {
        File storageFile = new File(tempFolder.getRoot(), "priority_mappings.properties");
        LearnedVariableMappings learned = new LearnedVariableMappings(storageFile);

        // Suppose a user previously mapped a column named "Top" to "Mor" by mistake or
        // in a quirky file
        Relation quirky = new Relation();
        quirky.setDatabaseTableName(Globals.TUMOUR_TABLE_NAME);
        quirky.setDatabaseVariableName("Mor");
        quirky.setFileColumnNumber(0);
        quirky.setFileVariableName("Top");
        List<Relation> list = new ArrayList<Relation>();
        list.add(quirky);
        learned.saveMappings(doc, list);

        // When importing a file that has "Top", Pass 1 (exact match) must win and map
        // "Top" -> "Top", NOT "Mor"
        String[] cols = new String[] { "Top" };
        List<Relation> rels = Import.constructRelations(
                doc, cols, Globals.TUMOUR_TABLE_NAME, learned);
        assertEquals("Exact column match must take priority over learned mappings",
                "Top", rels.get(0).getDatabaseVariableName());
        assertFalse(rels.get(0).isLearnedMapping());
    }

    @Test
    public void testVariablesAssociationPanelPreservesManualSelectionAcrossReinit() throws Exception {
        org.junit.Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        ensureApplicationInitialized(globalToolBox, doc);
        File storageFile = tempFolder.newFile("panel_mappings.properties");
        LearnedVariableMappings emptyLearned = new LearnedVariableMappings(storageFile);

        DatabaseVariablesListElement[] tumourVars = Tools.getVariableListElements(
                doc, Globals.NAMESPACE, Globals.TUMOUR_TABLE_NAME);
        String[] cols = new String[] { "custom_pid", "custom_topo", "Incid" };

        List<Relation> initialMap = Import.constructRelations(
                doc, cols, Globals.TUMOUR_TABLE_NAME, emptyLearned);

        VariablesAssociationPanel panel = new VariablesAssociationPanel();
        panel.initializeVariableMappingPanel(initialMap, tumourVars, cols);

        // Manually map PatientIDtumourtable -> "custom_pid" and Top -> "custom_topo"
        VariableMappingAlternativePanel pidPanel = null;
        VariableMappingAlternativePanel topPanel = null;
        for (VariableMappingAlternativePanel vmp : panel.getPanelList()) {
            String shortName = vmp.getDatabaseVariablesListElement().getShortName();
            if ("PatientIDtumourtable".equalsIgnoreCase(shortName)) {
                pidPanel = vmp;
            } else if ("Top".equalsIgnoreCase(shortName)) {
                topPanel = vmp;
            }
        }
        assertNotNull(pidPanel);
        assertNotNull(topPanel);
        assertNull("Unrecognized column should start unmapped", pidPanel.getSelectedFileElement());

        pidPanel.setSelectedFileElement("custom_pid");
        topPanel.setSelectedFileElement("custom_topo");

        // Re-initializing with the same file elements must preserve the user's manual
        // selections
        panel.initializeVariableMappingPanel(initialMap, tumourVars, cols);

        VariableMappingAlternativePanel pidPanelAfter = null;
        VariableMappingAlternativePanel topPanelAfter = null;
        for (VariableMappingAlternativePanel vmp : panel.getPanelList()) {
            String shortName = vmp.getDatabaseVariablesListElement().getShortName();
            if ("PatientIDtumourtable".equalsIgnoreCase(shortName)) {
                pidPanelAfter = vmp;
            } else if ("Top".equalsIgnoreCase(shortName)) {
                topPanelAfter = vmp;
            }
        }
        assertEquals("Manual PatientIDtumourtable mapping must be preserved",
                "custom_pid", pidPanelAfter.getSelectedFileElement());
        assertEquals("Manual Top mapping must be preserved",
                "custom_topo", topPanelAfter.getSelectedFileElement());
    }

    @Test
    public void testPreviewFilePanelDoesNotFireRedundantFileChangedEvents() throws Exception {
        org.junit.Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        ensureApplicationInitialized(globalToolBox, doc);
        File csvFile = tempFolder.newFile("patients.csv");
        Writer w = new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8);
        w.write("pat_id,sex\n1001,1\n");
        w.close();

        final AtomicInteger fileChangedCount = new AtomicInteger(0);
        final AtomicInteger sepChangedCount = new AtomicInteger(0);

        PreviewFilePanel panel = new PreviewFilePanel();
        panel.init(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (PreviewFilePanel.FILE_CHANGED_ACTION.equals(e.getActionCommand())) {
                    fileChangedCount.incrementAndGet();
                } else if (PreviewFilePanel.SEPARATING_CHARACTER_CHANGED_ACTION.equals(e.getActionCommand())) {
                    sepChangedCount.incrementAndGet();
                }
            }
        });

        panel.setFile(csvFile);
        assertEquals("First setFile should fire FILE_CHANGED_ACTION once", 1, fileChangedCount.get());

        // Calling previewAction or setFile again with the same file must NOT fire
        // FILE_CHANGED_ACTION again
        panel.previewAction();
        panel.setFile(csvFile);
        assertEquals("Redundant previewAction/setFile with unchanged path must not fire FILE_CHANGED_ACTION",
                1, fileChangedCount.get());

        int sepCountBefore = sepChangedCount.get();
        panel.comboBoxChanged();
        assertEquals(
                "Redundant comboBoxChanged with unchanged separator must not fire SEPARATING_CHARACTER_CHANGED_ACTION",
                sepCountBefore, sepChangedCount.get());
    }

    @Test
    public void testImportFilesLinksTumoursToPatientsWhenPatientRecordIDNotInCSVOverPassByValueServer()
            throws Exception {
        // Create Patient CSV and Tumour CSV with non-standard column names and NO
        // PatientRecordID column
        File patientCsv = tempFolder.newFile("patients_nonstd.csv");
        Writer pw = new OutputStreamWriter(new FileOutputStream(patientCsv), StandardCharsets.UTF_8);
        pw.write("pat_id,fname,sname,sex_code,bdate\n");
        pw.write("P0001001,John,Doe,1,19500101\n");
        pw.write("P0001002,Jane,Roe,2,19600505\n");
        pw.close();

        File tumourCsv = tempFolder.newFile("tumours_nonstd.csv");
        Writer tw = new OutputStreamWriter(new FileOutputStream(tumourCsv), StandardCharsets.UTF_8);
        tw.write("pat_id,inc_date,topo,morph,behav,basis\n");
        tw.write("P0001001,20200115,509,8500,3,7\n");
        tw.write("P0001002,20200620,349,8140,3,5\n");
        tw.close();

        // Build manual mapping from non-standard CSV columns to database variables
        List<Relation> map = new ArrayList<Relation>();
        addRelation(map, Globals.PATIENT_TABLE_NAME, "RegNo", 0, "pat_id");
        addRelation(map, Globals.PATIENT_TABLE_NAME, "FirstN", 1, "fname");
        addRelation(map, Globals.PATIENT_TABLE_NAME, "FamN", 2, "sname");
        addRelation(map, Globals.PATIENT_TABLE_NAME, "Sex", 3, "sex_code");
        addRelation(map, Globals.PATIENT_TABLE_NAME, "BirthD", 4, "bdate");

        addRelation(map, Globals.TUMOUR_TABLE_NAME, "PatientIDTumourTable", 0, "pat_id");
        addRelation(map, Globals.TUMOUR_TABLE_NAME, "InciD", 1, "inc_date");
        addRelation(map, Globals.TUMOUR_TABLE_NAME, "Top", 2, "topo");
        addRelation(map, Globals.TUMOUR_TABLE_NAME, "Mor", 3, "morph");
        addRelation(map, Globals.TUMOUR_TABLE_NAME, "Beh", 4, "behav");
        addRelation(map, Globals.TUMOUR_TABLE_NAME, "Bas", 5, "basis");

        // Simulate RMI pass-by-value server where savePatient mutates a server-side
        // copy
        // (generating PatientRecordID) and does NOT mutate the caller's Patient
        // instance.
        final Map<Integer, Patient> savedPatientsByDbId = new HashMap<Integer, Patient>();
        final Map<String, List<Patient>> savedPatientsByPid = new HashMap<String, List<Patient>>();
        final List<Tumour> savedTumours = new ArrayList<Tumour>();
        final AtomicInteger nextDbId = new AtomicInteger(1);

        CanRegServerInterface rmiLikeServer = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[] { CanRegServerInterface.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("getDatabseDescription".equals(name)) {
                            return doc;
                        } else if ("savePatient".equals(name)) {
                            Patient callerPatient = (Patient) args[0];
                            // Clone to simulate RMI serialization by value!
                            Patient serverCopy = clonePatient(callerPatient);
                            int dbId = nextDbId.getAndIncrement();
                            String pid = String.valueOf(serverCopy.getVariable("RegNo"));
                            String generatedPrid = pid + "01";
                            serverCopy.setVariable("PatientRecordID", generatedPrid);
                            serverCopy.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, dbId);
                            savedPatientsByDbId.put(dbId, serverCopy);
                            List<Patient> list = savedPatientsByPid.get(pid);
                            if (list == null) {
                                list = new ArrayList<Patient>();
                                savedPatientsByPid.put(pid, list);
                            }
                            list.add(serverCopy);
                            return dbId;
                        } else if ("getRecord".equals(name)) {
                            int dbId = (Integer) args[0];
                            String tableName = (String) args[1];
                            if (Globals.PATIENT_TABLE_NAME.equals(tableName)) {
                                Patient p = savedPatientsByDbId.get(dbId);
                                return p != null ? clonePatient(p) : null;
                            }
                            return null;
                        } else if ("getPatientsByPatientID".equals(name)) {
                            String pid = (String) args[0];
                            List<Patient> list = savedPatientsByPid.get(pid);
                            if (list == null) {
                                return new Patient[0];
                            }
                            return list.toArray(new Patient[list.size()]);
                        } else if ("saveTumour".equals(name)) {
                            Tumour t = (Tumour) args[0];
                            int dbId = nextDbId.getAndIncrement();
                            t.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, dbId);
                            savedTumours.add(t);
                            return dbId;
                        }
                        return null;
                    }
                });

        File reportFile = tempFolder.newFile("import_report.txt");
        ImportOptions io = buildDefaultImportOptions(reportFile);

        File[] files = new File[] { patientCsv, tumourCsv, null };
        boolean success = Import.importFiles(null, map, files, rmiLikeServer, io, false, doc);

        assertTrue("Import of patient and tumour files should succeed", success);
        assertEquals("Two patients should be saved", 2, savedPatientsByDbId.size());
        assertEquals("Two tumours should be saved", 2, savedTumours.size());

        Tumour t1 = savedTumours.get(0);
        assertEquals("P0001001", t1.getVariable("PatientIDTumourTable"));
        assertEquals(
                "Tumour 1 must have its PatientRecordIDTumourTable linked to Patient 1's generated PatientRecordID",
                "P000100101", t1.getVariable("PatientRecordIDTumourTable"));

        Tumour t2 = savedTumours.get(1);
        assertEquals("P0001002", t2.getVariable("PatientIDTumourTable"));
        assertEquals(
                "Tumour 2 must have its PatientRecordIDTumourTable linked to Patient 2's generated PatientRecordID",
                "P000100201", t2.getVariable("PatientRecordIDTumourTable"));
    }

    private void addRelation(List<Relation> map, String tableName, String dbVarName, int colIdx, String fileColName) {
        DatabaseVariablesListElement[] vars = Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        for (DatabaseVariablesListElement v : vars) {
            if (dbVarName.equalsIgnoreCase(v.getDatabaseVariableName())) {
                Relation r = new Relation();
                r.setDatabaseTableName(tableName);
                r.setDatabaseTableVariableID(v.getDatabaseTableVariableID());
                r.setDatabaseVariableName(v.getDatabaseVariableName());
                r.setFileColumnNumber(colIdx);
                r.setFileVariableName(fileColName);
                r.setVariableType(v.getVariableType());
                map.add(r);
                return;
            }
        }
        fail("Variable " + dbVarName + " not found in table " + tableName);
    }

    private Patient clonePatient(Patient src) {
        Patient copy = new Patient();
        for (String varName : src.getVariableNames()) {
            copy.setVariable(varName, src.getVariable(varName));
        }
        return copy;
    }

    private ImportOptions buildDefaultImportOptions(File reportFile) {
        ImportOptions io = new ImportOptions();
        io.setDiscrepancies(ImportOptions.UPDATE);
        io.setMaxLines(-1);
        io.setTestOnly(false);
        io.setSeparators(new char[] { ',', ',', ',' });
        io.setDoChecks(false);
        io.setDoPersonSearch(false);
        io.setQueryNewNames(false);
        io.setDataFromPreviousCanReg(false);
        io.setFilesCharsets(new Charset[] { StandardCharsets.UTF_8, StandardCharsets.UTF_8, StandardCharsets.UTF_8 });
        io.setReportFileName(reportFile.getAbsolutePath());

        io.setMultiplePrimaryVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.MultPrimCode.toString()).getDatabaseVariableName());
        io.setPatientIDTumourTableVariableName(
                globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(
                                Globals.StandardVariableNames.PatientIDTumourTable.toString())
                        .getDatabaseVariableName());
        io.setPatientIDVariableName(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                .getDatabaseVariableName());
        io.setTumourUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName());
        io.setPatientUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName());
        io.setTumourIDVariablename(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString())
                .getDatabaseVariableName());
        io.setPatientRecordIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
        io.setPatientRecordIDTumourTableVariableName(
                globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(
                                Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                        .getDatabaseVariableName());
        io.setTumourIDSourceTableVariableName(
                globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(
                                Globals.StandardVariableNames.TumourIDSourceTable.toString())
                        .getDatabaseVariableName());
        io.setSourceIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName());
        io.setObsoletePatientFlagVariableName(
                globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(
                                Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString())
                        .getDatabaseVariableName());
        io.setObsoleteTumourFlagVariableName(
                globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(
                                Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString())
                        .getDatabaseVariableName());
        io.setTumourSequenceVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.MultPrimSeq.toString()).getDatabaseVariableName());
        io.setTumourCheckStatus(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.CheckStatus.toString()).getDatabaseVariableName());
        io.setTumourRecordStatus(globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.TumourRecordStatus.toString()).getDatabaseVariableName());
        io.setICD10VariableName(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICD10.toString())
                .getDatabaseVariableName());
        io.setICCCVariableName(globalToolBox
                .translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.ICCC.toString()));
        return io;
    }
}