package canreg.client.gui.importers;

import canreg.TestFixtures;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import canreg.server.CanRegServerInterface;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImportRecordTest {

    private GlobalToolBox globalToolBox;

    @Before
    public void setUp() {
        globalToolBox = TestFixtures.createGlobalToolBox();
    }

    @Test
    public void testImportPatientPreservesIDsWhenNotPromotingFromHolding() throws Exception {
        final Patient[] savedPatientHolder = new Patient[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("savePatient".equals(method.getName())) {
                            savedPatientHolder[0] = (Patient) args[0];
                            return 101;
                        }
                        return null;
                    }
                });

        Patient patient = new Patient();
        String patientIdVar = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        String patientRecIdVar = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();

        patient.setVariable(patientIdVar, "1999298901");
        patient.setVariable(patientRecIdVar, "199929890101");

        StringWriter reportWriter = new StringWriter();
        int result = Import.importPatient(server, ImportOptions.REJECT, "199929890101",
                patient, reportWriter, true, false, false, globalToolBox);

        assertEquals(101, result);
        assertNotNull("savePatient should have been called", savedPatientHolder[0]);
        assertEquals("PatientID must be preserved during file import into holding/production DB",
                "1999298901", savedPatientHolder[0].getVariable(patientIdVar));
        assertEquals("PatientRecordID must be preserved during file import into holding/production DB",
                "199929890101", savedPatientHolder[0].getVariable(patientRecIdVar));
    }

    @Test
    public void testImportPatientResetsIDsWhenPromotingFromHoldingToProduction() throws Exception {
        final Patient[] savedPatientHolder = new Patient[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("savePatient".equals(method.getName())) {
                            savedPatientHolder[0] = (Patient) args[0];
                            return 202;
                        }
                        return null;
                    }
                });

        Patient patient = new Patient();
        String patientIdVar = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        String patientRecIdVar = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();

        patient.setVariable(patientIdVar, "1999298901");
        patient.setVariable(patientRecIdVar, "199929890101");

        StringWriter reportWriter = new StringWriter();
        int result = Import.importPatient(server, ImportOptions.REJECT, "199929890101",
                patient, reportWriter, false, false, true, globalToolBox);

        assertEquals(202, result);
        assertNotNull("savePatient should have been called", savedPatientHolder[0]);
        assertEquals("PatientID must be cleared when promoting from holding to production",
                "", savedPatientHolder[0].getVariable(patientIdVar));
        assertEquals("PatientRecordID must be cleared when promoting from holding to production",
                "", savedPatientHolder[0].getVariable(patientRecIdVar));
    }

    @Test
    public void testImportTumourSkipsSavingWhenPatientNotFound() throws Exception {
        final AtomicBoolean saveTumourCalled = new AtomicBoolean(false);
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            saveTumourCalled.set(true);
                            return 303;
                        }
                        return null;
                    }
                });

        Tumour tumour = new Tumour();
        StringWriter reportWriter = new StringWriter();

        Import.importTumour(server, ImportOptions.REJECT, "TUM123", "NON_EXISTENT_PRID",
                tumour, null, reportWriter, true, false, false);

        assertFalse("saveTumour must NOT be called when patient does not exist in DB",
                saveTumourCalled.get());
        assertTrue("Report must record that no patient matches this tumour",
                reportWriter.toString().contains("No patient matches this Tumour"));
    }

    @Test
    public void testImportPatientHandlesNullPatientGracefully() throws Exception {
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return null;
                    }
                });

        StringWriter reportWriter = new StringWriter();
        int result = Import.importPatient(server, ImportOptions.UPDATE, "1999230201",
                null, reportWriter, false, false, true, globalToolBox);

        assertEquals(-1, result);
    }

    @Test
    public void testImportTumourAndSourceHandleNullGracefully() throws Exception {
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return null;
                    }
                });

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.UPDATE, "TUM_NULL", "PRID_NULL",
                null, null, reportWriter, false, false, true);

        Import.importSource(server, ImportOptions.UPDATE, "SRC_NULL", "SRC_VAR",
                null, "TUM_NULL", reportWriter, false, false, true);
    }

    @Test
    public void testImportCacheOperations() {
        Import.ImportCache cache = new Import.ImportCache();
        Patient patient = new Patient();
        Tumour tumour = new Tumour();

        assertNull(cache.getPatientByPRID("PRID_1"));
        assertNull(cache.getPatientByPID("PID_1"));
        assertNull(cache.getTumourByTID("TID_1"));

        cache.putPatientByPRID("PRID_1", patient);
        cache.putPatientByPID("PID_1", patient);
        cache.putTumourByTID("TID_1", tumour);

        assertSame(patient, cache.getPatientByPRID("PRID_1"));
        assertSame(patient, cache.getPatientByPID("PID_1"));
        assertSame(tumour, cache.getTumourByTID("TID_1"));

        cache.clear();
        assertNull(cache.getPatientByPRID("PRID_1"));
        assertNull(cache.getPatientByPID("PID_1"));
        assertNull(cache.getTumourByTID("TID_1"));
    }

    @Test
    public void testImportPatientPopulatesCache() throws Exception {
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("savePatient".equals(method.getName())) {
                            return 303;
                        }
                        return null;
                    }
                });

        Patient patient = new Patient();
        String patientIdVar = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        String patientRecIdVar = globalToolBox.translateStandardVariableNameToDatabaseListElement(
                Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();

        patient.setVariable(patientIdVar, "PID_TEST_CACHE");
        patient.setVariable(patientRecIdVar, "PRID_TEST_CACHE");

        Import.ImportCache cache = new Import.ImportCache();
        StringWriter reportWriter = new StringWriter();
        int result = Import.importPatient(server, ImportOptions.REJECT, "PRID_TEST_CACHE",
                patient, reportWriter, false, false, false, globalToolBox, cache);

        assertEquals(303, result);
        assertEquals(303, patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME));
        assertSame(patient, cache.getPatientByPRID("PRID_TEST_CACHE"));
        assertSame(patient, cache.getPatientByPID("PID_TEST_CACHE"));
    }

    @Test
    public void testImportTumourSetsRecordIdAndPopulatesCache() throws Exception {
        final AtomicBoolean saveCalled = new AtomicBoolean(false);
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            saveCalled.set(true);
                            return 404;
                        }
                        return null;
                    }
                });

        Tumour tumour = new Tumour();
        tumour.setVariable("TumourID", "TID_TEST_404");
        tumour.setVariable("PatientIDtumourtable", "PID_TEST_404");
        tumour.setVariable("PatientRecordIDtumourtable", "PRID_TEST_404");

        Patient patient = new Patient();
        patient.setVariable("PatientRecordID", "PRID_TEST_404");

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_TEST_404", patient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.REJECT, "TID_TEST_404", "PRID_TEST_404",
                tumour, null, reportWriter, false, false, false, cache);

        assertTrue("saveTumour should have been called", saveCalled.get());
        assertEquals(404, tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME));
        assertSame(tumour, cache.getTumourByTID("TID_TEST_404"));
    }

    @Test
    public void testImportPatientDoesNotOverwriteHoldingRecordIdWhenFromHoldingToProduction() throws Exception {
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("savePatient".equals(method.getName())) {
                            return 9999;
                        }
                        return null;
                    }
                });

        Patient patient = new Patient();
        patient.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, 42); // holding DB PRID

        StringWriter reportWriter = new StringWriter();
        int result = Import.importPatient(server, ImportOptions.REJECT, "REC_42",
                patient, reportWriter, false, false, true, globalToolBox); // fromHoldingToProduction = true

        assertEquals(9999, result);
        assertEquals("Holding PRID must not be overwritten when fromHoldingToProduction is true",
                42, patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME));
    }

    @Test
    public void testImportPatientUpdatesExistingPatientPreservingProductionPRID() throws Exception {
        final AtomicBoolean editCalled = new AtomicBoolean(false);
        final AtomicBoolean saveCalled = new AtomicBoolean(false);
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("editPatientFromHoldingToProduction".equals(method.getName())) {
                            editCalled.set(true);
                            return true;
                        }
                        if ("savePatient".equals(method.getName())) {
                            saveCalled.set(true);
                            return 9999;
                        }
                        return null;
                    }
                });

        Patient existingProductionPatient = new Patient();
        existingProductionPatient.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, 555); // production PRID
        existingProductionPatient.setVariable("PatientRecordID", "PRID_EXISTING");
        existingProductionPatient.setVariable("FirstName", "Alice");

        Patient holdingPatient = new Patient();
        holdingPatient.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, 77); // holding PRID
        holdingPatient.setVariable("PatientRecordID", "PRID_EXISTING");
        holdingPatient.setVariable("FirstName", "Alice-Updated");

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_EXISTING", existingProductionPatient);

        StringWriter reportWriter = new StringWriter();
        int result = Import.importPatient(server, ImportOptions.UPDATE, "PRID_EXISTING",
                holdingPatient, reportWriter, false, false, true, globalToolBox, cache);

        assertTrue("editPatientFromHoldingToProduction should have been called for existing patient", editCalled.get());
        assertFalse("savePatient should NOT have been called for existing patient", saveCalled.get());
        assertEquals("Production PRID must be returned, not holding PRID", 555, result);
        assertEquals("Production record PRID must remain 555",
                555, existingProductionPatient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME));
        assertEquals("Patient field should be updated", "Alice-Updated", existingProductionPatient.getVariable("FirstName"));
    }

    @Test
    public void testImportTumourUpdatesExistingTumourInProduction() throws Exception {
        final AtomicBoolean editTumourCalled = new AtomicBoolean(false);
        final AtomicBoolean saveTumourCalled = new AtomicBoolean(false);
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("editTumourFromHoldingToProduction".equals(method.getName())) {
                            editTumourCalled.set(true);
                            return null;
                        }
                        if ("saveTumour".equals(method.getName())) {
                            saveTumourCalled.set(true);
                            return 9999;
                        }
                        return null;
                    }
                });

        Tumour existingProductionTumour = new Tumour();
        existingProductionTumour.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, 888); // production TRID
        existingProductionTumour.setVariable("TumourID", "TID_EXISTING_01");
        existingProductionTumour.setVariable("PatientRecordIDtumourtable", "PRID_EXISTING");
        existingProductionTumour.setVariable("IncidenceDate", "20200101");

        Patient existingProductionPatient = new Patient();
        existingProductionPatient.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, 555);
        existingProductionPatient.setVariable("PatientRecordID", "PRID_EXISTING");

        Tumour holdingTumour = new Tumour();
        holdingTumour.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, 88); // holding TRID
        holdingTumour.setVariable("TumourID", "TID_EXISTING_01");
        holdingTumour.setVariable("PatientRecordIDtumourtable", "PRID_EXISTING");
        holdingTumour.setVariable("IncidenceDate", "20200202");

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_EXISTING", existingProductionPatient);
        cache.putTumourByTID("TID_EXISTING_01", existingProductionTumour);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.UPDATE, "TID_EXISTING_01", "PRID_EXISTING",
                holdingTumour, null, reportWriter, false, false, true, cache);

        assertTrue("editTumourFromHoldingToProduction should have been called for existing tumour", editTumourCalled.get());
        assertFalse("saveTumour should NOT have been called for existing tumour", saveTumourCalled.get());
        assertEquals("Production TRID must remain 888",
                888, existingProductionTumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME));
        assertEquals("Tumour variable should be updated",
                "20200202", existingProductionTumour.getVariable("IncidenceDate"));
    }

    @Test
    public void testImportTumourOverwritesExistingTumourInProduction() throws Exception {
        final AtomicBoolean editTumourCalled = new AtomicBoolean(false);
        final AtomicBoolean saveTumourCalled = new AtomicBoolean(false);
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("editTumourFromHoldingToProduction".equals(method.getName())) {
                            editTumourCalled.set(true);
                            return null;
                        }
                        if ("saveTumour".equals(method.getName())) {
                            saveTumourCalled.set(true);
                            return 9999;
                        }
                        return null;
                    }
                });

        Tumour existingProductionTumour = new Tumour();
        existingProductionTumour.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, 777); // production TRID
        existingProductionTumour.setVariable("TumourID", "TID_OVERWRITE_01");
        existingProductionTumour.setVariable("PatientRecordIDtumourtable", "PRID_EXISTING");
        existingProductionTumour.setVariable("IncidenceDate", "20190101");

        Patient existingProductionPatient = new Patient();
        existingProductionPatient.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, 555);
        existingProductionPatient.setVariable("PatientRecordID", "PRID_EXISTING");

        Tumour holdingTumour = new Tumour();
        holdingTumour.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, 77); // holding TRID
        holdingTumour.setVariable("TumourID", "TID_OVERWRITE_01");
        holdingTumour.setVariable("PatientRecordIDtumourtable", "PRID_EXISTING");
        holdingTumour.setVariable("IncidenceDate", "20210505");

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_EXISTING", existingProductionPatient);
        cache.putTumourByTID("TID_OVERWRITE_01", existingProductionTumour);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.OVERWRITE, "TID_OVERWRITE_01", "PRID_EXISTING",
                holdingTumour, null, reportWriter, false, false, true, cache);

        assertTrue("editTumourFromHoldingToProduction should have been called for overwritten tumour", editTumourCalled.get());
        assertFalse("saveTumour should NOT have been called for overwritten tumour", saveTumourCalled.get());
        assertEquals("Production TRID must remain 777",
                777, existingProductionTumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME));
        assertEquals("Tumour variable should be overwritten",
                "20210505", existingProductionTumour.getVariable("IncidenceDate"));
    }

    @Test
    public void testImportTumourSavesNewTumourWhenNotExistingInProduction() throws Exception {
        final AtomicBoolean saveTumourCalled = new AtomicBoolean(false);
        final Tumour[] savedTumourHolder = new Tumour[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            saveTumourCalled.set(true);
                            savedTumourHolder[0] = (Tumour) args[0];
                            return 909;
                        }
                        if ("getDatabseDescription".equals(method.getName())) {
                            return TestFixtures.loadTestSystemDocument();
                        }
                        return null;
                    }
                });

        Patient existingProductionPatient = new Patient();
        existingProductionPatient.setVariable("PatientRecordID", "PRID_EXISTING");

        Tumour newHoldingTumour = new Tumour();
        newHoldingTumour.setVariable("TumourID", "HOLDING_TEMP_TID");
        newHoldingTumour.setVariable("PatientRecordIDtumourtable", "PRID_EXISTING");

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_EXISTING", existingProductionPatient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.UPDATE, "", "PRID_EXISTING",
                newHoldingTumour, null, reportWriter, false, false, true, cache);

        assertTrue("saveTumour should have been called for new tumour", saveTumourCalled.get());
        assertNotNull(savedTumourHolder[0]);
        assertEquals("TumourID must be reset before saving so production assigns the new ID",
                "", savedTumourHolder[0].getVariable("TumourID"));
    }

    @Test
    public void testImportTumourGeneratesMissingICD10AndICCCWhenChecksFailOrDisabled() throws Exception {
        final Tumour[] savedTumourHolder = new Tumour[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            savedTumourHolder[0] = (Tumour) args[0];
                            return 910;
                        }
                        if ("getDatabseDescription".equals(method.getName())) {
                            return TestFixtures.loadTestSystemDocument();
                        }
                        return null;
                    }
                });

        String sexVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Sex.toString());
        String topVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Topography.toString());
        String morVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Morphology.toString());
        String behVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Behaviour.toString());
        String icd10Var = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.ICD10.toString());
        String icccVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.ICCC.toString());
        String recStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.TumourRecordStatus.toString());
        String checkStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.CheckStatus.toString());

        Patient patient = new Patient();
        patient.setVariable("PatientRecordID", "PRID_GEN_CODES");
        patient.setVariable(sexVar, "2");

        Tumour tumour = new Tumour();
        tumour.setVariable("TumourID", "TID_GEN_01");
        tumour.setVariable("PatientRecordIDtumourtable", "PRID_GEN_CODES");
        tumour.setVariable(topVar, "C50.9");
        tumour.setVariable(morVar, "8500");
        tumour.setVariable(behVar, "3");

        ImportOptions io = new ImportOptions();
        io.setDoChecks(true);
        io.setICD10VariableName(icd10Var);
        io.setICCCVariableName(icccVar);
        io.setTumourRecordStatus(recStatusVar);
        io.setTumourCheckStatus(checkStatusVar);

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_GEN_CODES", patient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.REJECT, "TID_GEN_01", "PRID_GEN_CODES",
                tumour, io, reportWriter, false, false, false, cache);

        assertNotNull("saveTumour should have been called", savedTumourHolder[0]);
        assertEquals("Topography should be normalized to 3 digits", "509", savedTumourHolder[0].getVariable(topVar));
        assertEquals("Missing ICD10 should be generated", "C509", savedTumourHolder[0].getVariable(icd10Var));
        assertEquals("Missing ICCC should be generated", "11f", savedTumourHolder[0].getVariable(icccVar));
    }

    @Test
    public void testImportTumourGeneratesMissingBehaviourICD10AndICCCFromTopographyAndMorphologyOnly() throws Exception {
        final Tumour[] savedTumourHolder = new Tumour[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            savedTumourHolder[0] = (Tumour) args[0];
                            return 911;
                        }
                        if ("getDatabseDescription".equals(method.getName())) {
                            return TestFixtures.loadTestSystemDocument();
                        }
                        return null;
                    }
                });

        String topVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Topography.toString());
        String morVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Morphology.toString());
        String behVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Behaviour.toString());
        String icd10Var = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.ICD10.toString());
        String icccVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.ICCC.toString());
        String recStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.TumourRecordStatus.toString());
        String checkStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.CheckStatus.toString());

        Patient patient = new Patient();
        patient.setVariable("PatientRecordID", "PRID_TOP_MOR_ONLY");

        Tumour tumour = new Tumour();
        tumour.setVariable("TumourID", "TID_TOP_MOR_01");
        tumour.setVariable("PatientRecordIDtumourtable", "PRID_TOP_MOR_ONLY");
        tumour.setVariable(topVar, "C50.9");
        tumour.setVariable(morVar, "8500");
        // Behaviour is intentionally left unmapped / null

        ImportOptions io = new ImportOptions();
        io.setDoChecks(true);
        io.setICD10VariableName(icd10Var);
        io.setICCCVariableName(icccVar);
        io.setTumourRecordStatus(recStatusVar);
        io.setTumourCheckStatus(checkStatusVar);

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_TOP_MOR_ONLY", patient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.REJECT, "TID_TOP_MOR_01", "PRID_TOP_MOR_ONLY",
                tumour, io, reportWriter, false, false, false, cache);

        assertNotNull("saveTumour should have been called", savedTumourHolder[0]);
        assertEquals("Topography should be normalized to 3 digits", "509", savedTumourHolder[0].getVariable(topVar));
        assertEquals("Missing Behaviour should be inferred from Morphology", "3", savedTumourHolder[0].getVariable(behVar));
        assertEquals("Missing ICD10 should be generated", "C509", savedTumourHolder[0].getVariable(icd10Var));
        assertEquals("Missing ICCC should be generated", "11f", savedTumourHolder[0].getVariable(icccVar));
    }

    @Test
    public void testImportTumourDefaultsRecordStatusTo1WhenMissingAndDoChecksFalse() throws Exception {
        final Tumour[] savedTumourHolder = new Tumour[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            savedTumourHolder[0] = (Tumour) args[0];
                            return 301;
                        }
                        if ("getDatabseDescription".equals(method.getName())) {
                            return TestFixtures.loadTestSystemDocument();
                        }
                        return null;
                    }
                });

        String recStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.TumourRecordStatus.toString());

        Patient patient = new Patient();
        patient.setVariable("PatientRecordID", "PRID_DEF_RECS_01");

        Tumour tumour = new Tumour();
        tumour.setVariable("TumourID", "TID_DEF_RECS_01");
        tumour.setVariable("PatientRecordIDtumourtable", "PRID_DEF_RECS_01");
        // TumourRecordStatus is intentionally omitted / null

        ImportOptions io = new ImportOptions();
        io.setDoChecks(false);
        io.setTumourRecordStatus(recStatusVar);

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_DEF_RECS_01", patient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.REJECT, "TID_DEF_RECS_01", "PRID_DEF_RECS_01",
                tumour, io, reportWriter, false, false, false, cache);

        assertNotNull("saveTumour should have been called", savedTumourHolder[0]);
        assertEquals("Record status must default to 1 when missing", "1", savedTumourHolder[0].getVariable(recStatusVar));
    }

    @Test
    public void testImportTumourUpdatesRecordStatusBasedOnCheckOutcome() throws Exception {
        final Tumour[] savedTumourHolder = new Tumour[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            savedTumourHolder[0] = (Tumour) args[0];
                            return 302;
                        }
                        if ("getDatabseDescription".equals(method.getName())) {
                            return TestFixtures.loadTestSystemDocument();
                        }
                        return null;
                    }
                });

        String recStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.TumourRecordStatus.toString());
        String checkStatusVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.CheckStatus.toString());
        String inciDateVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.IncidenceDate.toString());
        String birthDateVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.BirthDate.toString());
        String ageVar = globalToolBox.translateStandardVariableNameToDatabaseVariableName(
                Globals.StandardVariableNames.Age.toString());

        Patient patient = new Patient();
        patient.setVariable("PatientRecordID", "PRID_CHECK_ERR");
        patient.setVariable(birthDateVar, "19800101");

        // Tumour with conflicting / impossible age and incidence date (age 100 with birth date 1980 and incidence 1985 -> Invalid)
        Tumour tumour = new Tumour();
        tumour.setVariable("TumourID", "TID_CHECK_ERR");
        tumour.setVariable("PatientRecordIDtumourtable", "PRID_CHECK_ERR");
        tumour.setVariable(inciDateVar, "19850101");
        tumour.setVariable(ageVar, 100);

        ImportOptions io = new ImportOptions();
        io.setDoChecks(true);
        io.setTumourRecordStatus(recStatusVar);
        io.setTumourCheckStatus(checkStatusVar);

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByPRID("PRID_CHECK_ERR", patient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.REJECT, "TID_CHECK_ERR", "PRID_CHECK_ERR",
                tumour, io, reportWriter, false, false, false, cache);

        assertNotNull("saveTumour should have been called", savedTumourHolder[0]);
        assertEquals("Record status must be 0 when checks detect errors", "0", savedTumourHolder[0].getVariable(recStatusVar));
    }

    @Test
    public void testImportTumourFallbackToPatientByTID() throws Exception {
        final Tumour[] savedTumourHolder = new Tumour[1];
        CanRegServerInterface server = (CanRegServerInterface) Proxy.newProxyInstance(
                CanRegServerInterface.class.getClassLoader(),
                new Class<?>[]{CanRegServerInterface.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("saveTumour".equals(method.getName())) {
                            savedTumourHolder[0] = (Tumour) args[0];
                            return 303;
                        }
                        return null;
                    }
                });

        Patient patient = new Patient();
        patient.setVariable("PatientID", "PID_FALLBACK");
        patient.setVariable("PatientRecordID", "PRID_FALLBACK");

        Tumour tumour = new Tumour();
        tumour.setVariable("TumourID", "TID_SINGLE_FILE");

        ImportOptions io = new ImportOptions();
        io.setTumourIDVariablename("TumourID");
        io.setPatientIDVariableName("PatientID");
        io.setPatientRecordIDVariableName("PatientRecordID");
        io.setPatientIDTumourTableVariableName("PatientIDtumourtable");
        io.setPatientRecordIDTumourTableVariableName("PatientRecordIDtumourtable");

        Import.ImportCache cache = new Import.ImportCache();
        cache.putPatientByTID("TID_SINGLE_FILE", patient);

        StringWriter reportWriter = new StringWriter();
        Import.importTumour(server, ImportOptions.REJECT, "TID_SINGLE_FILE", null,
                tumour, io, reportWriter, false, false, false, cache);

        assertNotNull("saveTumour should have been called", savedTumourHolder[0]);
        assertEquals("PatientRecordIDtumourtable should be linked via TID cache fallback",
                "PRID_FALLBACK", savedTumourHolder[0].getVariable("PatientRecordIDtumourtable"));
        assertEquals("PatientIDtumourtable should be set from patient via TID cache fallback",
                "PID_FALLBACK", savedTumourHolder[0].getVariable("PatientIDtumourtable"));
    }
}
