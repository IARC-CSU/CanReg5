package canreg.common.checks;

import canreg.client.LocalSettings;
import canreg.common.Tools;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.database.CanRegDAO;
import canreg.server.management.SystemDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Test of CheckRecordService.
 */
public class CheckRecordServiceTest {

    private File xmlRegistryFile = new File("test/canreg/test-system.xml");
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
        patient.setVariable("birthd", "19100120");
        patient.setVariable("sex", "1");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>BIRTHD: </strong>19100120<br>" +
                        "<strong>FAMN: </strong>FamilyName<br>" +
                        "<strong>REGNO: </strong>123<br>" +
                        "<strong>SEX: </strong>1<br>",
                sortRawData(patient));
        Assert.assertEquals("", patient.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckTumourOk() {
        Tumour tumour = new Tumour();
        tumour.setVariable("incid", "20010202");
        tumour.setVariable("beh", "3");
        tumour.setVariable("mor", "8002");
        tumour.setVariable("top", "12");
        tumour.setVariable("bas", "7");
        tumour.setVariable("recs", "1");
        tumour.setVariable("addr", "74000");
        tumour.setVariable("age", 89);
        List<CheckMessage> messages = service.checkTumour(tumour);
        Assert.assertEquals("[]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>ADDR: </strong>74000<br>" +
                        "<strong>AGE: </strong>89<br>" +
                        "<strong>BAS: </strong>7<br>" +
                        "<strong>BEH: </strong>3<br>" +
                        "<strong>INCID: </strong>20010202<br>" +
                        "<strong>MOR: </strong>8002<br>" +
                        "<strong>RECS: </strong>1<br>" +
                        "<strong>TOP: </strong>12<br>",
                sortRawData(tumour));
        Assert.assertEquals("", tumour.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));

    }

    @Test
    public void testCheckTumourAgeNotNumber() {
        Tumour tumour = new Tumour();
        tumour.setVariable("incid", "20010202");
        tumour.setVariable("beh", "3");
        tumour.setVariable("mor", "8002");
        tumour.setVariable("top", "12");
        tumour.setVariable("bas", "7");
        tumour.setVariable("recs", "1");
        tumour.setVariable("addr", "74000");
        // Age is not a number object
        tumour.setVariable("age", "89");
        List<CheckMessage> messages = service.checkTumour(tumour);
        Assert.assertEquals("[{level='error', variable='age', value='89', message='this value is not an integer'}]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>ADDR: </strong>74000<br>" +
                        "<strong>AGE: </strong>89 (Error: this value is not an integer)<br>" +
                        "<strong>BAS: </strong>7<br>" +
                        "<strong>BEH: </strong>3<br>" +
                        "<strong>INCID: </strong>20010202<br>" +
                        "<strong>MOR: </strong>8002<br>" +
                        "<strong>RECS: </strong>1<br>" +
                        "<strong>TOP: </strong>12<br>",
                sortRawData(tumour));
        Assert.assertEquals("AGE<br>", tumour.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckTumourMissingAge() {
        Tumour tumour = new Tumour();
        tumour.setVariable("incid", "20010202");
        tumour.setVariable("beh", "3");
        tumour.setVariable("mor", "8002");
        tumour.setVariable("top", "12");
        tumour.setVariable("bas", "7");
        tumour.setVariable("recs", "1");
        tumour.setVariable("addr", "74000");
        // Age is missing
        // tumour.setVariable("age", 89);
        List<CheckMessage> messages = service.checkTumour(tumour);
        Assert.assertEquals("[{level='warning', variable='age', value='', message='this variable is mandatory'}]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>ADDR: </strong>74000<br>" +
                        "<strong>AGE: </strong> (Warning: this variable is mandatory)<br>" +
                        "<strong>BAS: </strong>7<br>" +
                        "<strong>BEH: </strong>3<br>" +
                        "<strong>INCID: </strong>20010202<br>" +
                        "<strong>MOR: </strong>8002<br>" +
                        "<strong>RECS: </strong>1<br>" +
                        "<strong>TOP: </strong>12<br>",
                sortRawData(tumour));
        Assert.assertEquals("AGE<br>", tumour.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }
    
    @Test
    public void testCheckSourceOk() {
        Source source = new Source();
        List<CheckMessage> messages = service.checkSource(source);
        Assert.assertEquals("[]", messages.toString());
        Assert.assertEquals("<br>", sortRawData(source));
        Assert.assertEquals("", source.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckRecordMissingVariable() {
        Patient patient = new Patient();
        patient.setVariable("regno", "124");
        // patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "19100101");
        patient.setVariable("sex", "2");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='warning', variable='famn', value='', message='this variable is mandatory'}]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>BIRTHD: </strong>19100101<br>" +
                        "<strong>FAMN: </strong> (Warning: this variable is mandatory)<br>" +
                        "<strong>REGNO: </strong>124<br>" +
                        "<strong>SEX: </strong>2<br>",
                sortRawData(patient));
        Assert.assertEquals("FAMN<br>", patient.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckRecordEmptyVariable() {
        Patient patient = new Patient();
        patient.setVariable("regno", "125");
        patient.setVariable("famn", "");
        patient.setVariable("birthd", "19050120");
        patient.setVariable("sex", "1");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='warning', variable='famn', value='', message='this variable is mandatory'}]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>BIRTHD: </strong>19050120<br>" +
                        "<strong>FAMN: </strong> (Warning: this variable is mandatory)<br>" +
                        "<strong>REGNO: </strong>125<br>" +
                        "<strong>SEX: </strong>1<br>",
                sortRawData(patient));
        Assert.assertEquals("FAMN<br>", patient.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckRecordVariableTooLong() {
        Patient patient = new Patient();
        patient.setVariable("regno", "123456789");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "19050120");
        patient.setVariable("sex", "1");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='regno', value='123456789', message='the length 9 exceeds the maximum length 8'}]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>BIRTHD: </strong>19050120<br>" +
                        "<strong>FAMN: </strong>FamilyName<br>" +
                        "<strong>REGNO: </strong>123456789 (Error: the length 9 exceeds the maximum length 8)<br>" +
                        "<strong>SEX: </strong>1<br>",
                sortRawData(patient));
        Assert.assertEquals("REGNO<br>", patient.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckRecordVariableDictTooLong() {
        Patient patient = new Patient();
        patient.setVariable("regno", "12345678");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "19050120");
        patient.setVariable("sex", "12");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[" +
                "{level='error', variable='sex', value='12', message='this code is not in the dictionary'}, " +
                "{level='error', variable='sex', value='12', message='the length 2 exceeds the maximum length 1'}" +
                "]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>BIRTHD: </strong>19050120<br>" +
                        "<strong>FAMN: </strong>FamilyName<br>" +
                        "<strong>REGNO: </strong>12345678<br>" +
                        "<strong>SEX: </strong>12 (Error: this code is not in the dictionary, Error: the length 2 exceeds the maximum length 1)<br>",
                sortRawData(patient));
        Assert.assertEquals("SEX<br>", patient.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    @Test
    public void testCheckRecordDateOk() {
        Patient patient = new Patient();
        patient.setVariable("regno", "123");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("sex", "1");
        List<CheckMessage> messages;

        patient.setVariable("birthd", "19100120");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[]", messages.toString());

        // 29/02 in 1912
        cleanAddedVariables(patient);
        patient.setVariable("birthd", "19120229");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[]", messages.toString());
    }

    @Test
    public void testCheckRecordDateKo() {
        Patient patient = new Patient();
        patient.setVariable("regno", "125");
        patient.setVariable("famn", "TheName");
        patient.setVariable("sex", "1");
        List<CheckMessage> messages;

        // date with '/'
        cleanAddedVariables(patient);
        patient.setVariable("birthd", "1910/01/20");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='1910/01/20', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        // date with '-'
        cleanAddedVariables(patient);
        patient.setVariable("birthd", "1910-01-20");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='1910-01-20', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        cleanAddedVariables(patient);
        patient.setVariable("birthd", "01/01/1910");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='01/01/1910', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        cleanAddedVariables(patient);
        patient.setVariable("birthd", "19101301");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='19101301', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        cleanAddedVariables(patient);
        patient.setVariable("birthd", "1910131");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='1910131', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        cleanAddedVariables(patient);
        patient.setVariable("birthd", " 19101301");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value=' 19101301', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        cleanAddedVariables(patient);
        patient.setVariable("birthd", "19100431");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='19100431', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

        cleanAddedVariables(patient);
        patient.setVariable("birthd", "19090229");
        messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='birthd', value='19090229', message='this date is not a valid date yyyyMMdd'}]", messages.toString());

    }

    @Test
    public void testCheckRecordMultipleMessages() {
        Patient patient = new Patient();
        patient.setVariable("regno", "125");
        patient.setVariable("famn", "");
        patient.setVariable("birthd", "1905-01-20");
        patient.setVariable("sex", "3");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[" +
                "{level='error', variable='birthd', value='1905-01-20', message='this date is not a valid date yyyyMMdd'}, " +
                "{level='warning', variable='famn', value='', message='this variable is mandatory'}, " +
                "{level='error', variable='sex', value='3', message='this code is not in the dictionary'}" +
                "]", messages.toString());
        Assert.assertEquals("" +
                        "<strong>BIRTHD: </strong>1905-01-20 (Error: this date is not a valid date yyyyMMdd)<br>" +
                        "<strong>FAMN: </strong> (Warning: this variable is mandatory)<br>" +
                        "<strong>REGNO: </strong>125<br>" +
                        "<strong>SEX: </strong>3 (Error: this code is not in the dictionary)<br>",
                sortRawData(patient));
        Assert.assertEquals("BIRTHD<br>FAMN<br>SEX<br>", patient.getVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS));
    }

    private void cleanAddedVariables(DatabaseRecord databaseRecord) {
        databaseRecord.setVariable(CheckRecordService.VARIABLE_RAW_DATA, null);
        databaseRecord.setVariable(CheckRecordService.VARIABLE_FORMAT_ERRORS, null);
    }

    @Test
    public void testCheckRecordUnknownValueInDictionary() {
        Patient patient = new Patient();
        patient.setVariable("regno", "126");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "19100101");
        patient.setVariable("sex", "3");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='error', variable='sex', value='3', message='this code is not in the dictionary'}]", messages.toString());
    }

    @Test
    public void testCheckRecordUnknownVariable() {
        Patient patient = new Patient();
        patient.setVariable("regno", "123");
        patient.setVariable("famn", "FamilyName");
        patient.setVariable("birthd", "19100101");
        patient.setVariable("sex", "2");
        patient.setVariable("unknownVariable", "3");
        List<CheckMessage> messages = service.checkPatient(patient);
        Assert.assertEquals("[{level='warning', variable='unknownvariable', value='3', message='unknown variable'}]", messages.toString());
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

    private String sortRawData(DatabaseRecord databaseRecord) {
        return Arrays.asList(
                        StringUtils.splitByWholeSeparator(
                                databaseRecord.getVariable(CheckRecordService.VARIABLE_RAW_DATA).toString(), "<br>"))
                .stream()
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining("<br>"))
                + "<br>";
    }

}

