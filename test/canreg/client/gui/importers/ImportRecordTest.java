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
}
