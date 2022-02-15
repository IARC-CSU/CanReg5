package canreg.common.checks;

import canreg.client.LocalSettings;
import canreg.common.Tools;
import canreg.common.database.Dictionary;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.database.CanRegDAO;
import canreg.server.management.SystemDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Test of CheckRecordService.
 */
public class CheckRecordServiceTest {

    private File xmlRegistryFile = new File("demo/database/TRN.xml");
    private File settingsFile = new File("test/canreg/test-settings.xml");
    private File dictionariesFile = new File("test/canreg/test-dictionaries.json");
    private CheckRecordService service;
    private SystemDescription systemDescription;
    @Mock
    private CanRegDAO dao;

    private AutoCloseable closeable;

    @Before
    public void before() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        systemDescription = buildSystemDescription(xmlRegistryFile);
        Mockito.when(dao.getDatabaseVariablesList()).thenReturn(systemDescription.getDatabaseVariableListElements());
        service = new CheckRecordService(dao);

        Map<Integer, Dictionary> dictionariesMap = CanRegDAO.buildDictionaryMap(systemDescription.getSystemDescriptionDocument());
        service.setDictionaries(dictionariesMap);

        // Mock the dictionary entries values
        InputStream inputStream = FileUtils.openInputStream(dictionariesFile);
        Map<String, ListDictionaryEntries> dictionaryEntries = new ObjectMapper().readerForMapOf(ListDictionaryEntries.class).readValue(inputStream);
        Assert.assertNotNull(dictionaryEntries);
        for (Map.Entry<String, ListDictionaryEntries> entry : dictionaryEntries.entrySet()) {
            dictionariesMap.get(Integer.parseInt(entry.getKey())).getDictionaryEntries().putAll(entry.getValue().getDictionaryEntries());
        }

    }

    @After
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testCheckPatientOk() {
        Patient patient = new Patient();
        patient.setVariable("regno", "123");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "01/01/1910");
        patient.setVariable("sex", "1");
        List<String> messages = service.checkPatient(patient);
        Assert.assertEquals("[]", messages.toString());
    }

    @Test
    public void testCheckTumourOk() {
        Tumour tumour = new Tumour();
        tumour.setVariable("incid", "20010202");
        tumour.setVariable("beh", "3");
        tumour.setVariable("mor", "8211");
        tumour.setVariable("top", "189");
        tumour.setVariable("bas", "7");
        tumour.setVariable("recs", "1");
        tumour.setVariable("addr", "74000");
        tumour.setVariable("age", 89);
        List<String> messages = service.checkTumour(tumour);
        Assert.assertEquals("[]", messages.toString());
    }

    @Test
    public void testCheckTumourAgeNotNumber() {
        Tumour tumour = new Tumour();
        tumour.setVariable("incid", "20010202");
        tumour.setVariable("beh", "3");
        tumour.setVariable("mor", "8211");
        tumour.setVariable("top", "189");
        tumour.setVariable("bas", "7");
        tumour.setVariable("recs", "1");
        tumour.setVariable("addr", "74000");
        // Age is not a number object
        tumour.setVariable("age", "89");
        List<String> messages = service.checkTumour(tumour);
        Assert.assertEquals("[integer input is expected for variable: age]", messages.toString());
    }
    
    @Test
    public void testCheckSourceOk() {
        Source source = new Source();
        List<String> messages = service.checkSource(source);
        Assert.assertEquals("[]", messages.toString());
    }

    @Test
    public void testCheckRecordMissingVariable() {
        Patient patient = new Patient();
        patient.setVariable("regno", "124");
        // patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "01/01/1910");
        patient.setVariable("sex", "2");
        List<String> messages = service.checkPatient(patient);
        Assert.assertEquals("[variable is mandatory: famn]", messages.toString());
    }

    @Test
    public void testCheckRecordEmptyVariable() {
        Patient patient = new Patient();
        patient.setVariable("regno", "125");
        patient.setVariable("famn", "");
        patient.setVariable("birthd", "01/01/1910");
        patient.setVariable("sex", "1");
        List<String> messages = service.checkPatient(patient);
        Assert.assertEquals("[variable is mandatory: famn]", messages.toString());
    }

    @Test
    public void testCheckRecordUnknownValueInDictionary() {
        Patient patient = new Patient();
        patient.setVariable("regno", "126");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "01/01/1910");
        patient.setVariable("sex", "3");
        List<String> messages = service.checkPatient(patient);
        Assert.assertEquals("[unknown value in dictionary sex=3]", messages.toString());
    }

    @Test
    public void testCheckRecordUnknownVariable() {
        Patient patient = new Patient();
        patient.setVariable("regno", "123");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "01/01/1910");
        patient.setVariable("sex", "2");
        patient.setVariable("unknownVariable", "3");
        List<String> messages = service.checkPatient(patient);
        Assert.assertEquals("[unknown variable: unknownvariable]", messages.toString());
    }

    /**
     * The database properties read in the file configured in "dbConfigFilePath".
     *
     * @return Properties
     * @throws IOException if the file configured in "dbConfigFilePath" cannot be found or read.
     */
    public Properties dbProperties() throws IOException {
        Properties properties = new Properties();
        properties.put("dataSourceClassName", "org.apache.derby.jdbc.ClientDriver");
        // do not use a true url
        properties.put("derby.url", "jdbc:derby://localhost:123/");
        properties.put("user", "dbuser");
        properties.put("password", "dbpwd");
        properties.put("initialPoolSize", "0");
        properties.put("maximumPoolSize", "20");
        properties.put("maxIdleConnection ", "5");
        properties.put("connectionTimeout", "10000");
        properties.put("maxLifetime", "1800000");
        properties.put("schema", "APP");
        properties.put("table", "Patient");
        return properties;
    }

    /**
     * Bean SystemDescription.
     *
     * @param xmlRegistryFile xmlRegistryFile
     * @return SystemDescription
     */
    public SystemDescription buildSystemDescription(File xmlRegistryFile) {
        LocalSettings localSettings = new LocalSettings(settingsFile);
        Tools.setLocalSettings(localSettings);
        return new SystemDescription(xmlRegistryFile.getAbsolutePath());
    }

    /**
     * Bean CanRegDAO to access to the main database (not a holding DB).
     *
     * @param dbProperties      the database properties
     * @param systemDescription systemDescription
     * @return CanRegDAO
     */
    public CanRegDAO buildCanRegDAO(Properties dbProperties, SystemDescription systemDescription) {
        return new CanRegDAO(systemDescription.getRegistryCode(), systemDescription.getSystemDescriptionDocument(),
                dbProperties);
    }


}

