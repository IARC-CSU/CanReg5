package canreg.client.dataentry;

import canreg.TestFixtures;
import canreg.common.DatabaseDictionaryListElement;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.PopulationDataset;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

public class DictionaryAndPDSHelperTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testBuildDictionaryEntriesAndLookupInXml() {
        Map<String, String> raw = new LinkedHashMap<String, String>();
        raw.put("1", "Male");
        raw.put("2", "Female");

        Map<String, DictionaryEntry> built = DictionaryHelper.buildDictionaryEntriesFromMap(raw);
        assertEquals(2, built.size());
        assertEquals("Male", built.get("1").getDescription());
        assertEquals("Female", built.get("2").getDescription());

        Document doc = TestFixtures.loadTestSystemDocument();
        int sexDictId = DictionaryHelper.getDictionaryIDbyName(doc, "Sex");
        assertTrue("Sex dictionary should exist in test-system.xml", sexDictId >= 0);
        assertEquals(-1, DictionaryHelper.getDictionaryIDbyName(doc, "NonExistentDictionary"));

        assertFalse("Sex is a simple dictionary", DictionaryHelper.isCompoundDictionarybyName(doc, "Sex"));
        assertTrue("Topography is a compound dictionary", DictionaryHelper.isCompoundDictionarybyName(doc, "Topography"));
    }

    @Test
    public void testDictionaryValidationRules() {
        DatabaseDictionaryListElement compoundDict = new DatabaseDictionaryListElement();
        compoundDict.setDictionaryID(1);
        compoundDict.setType("Compound");
        compoundDict.setCodeLength(2);
        compoundDict.setFullDictionaryCodeLength(4);
        compoundDict.setAllowCodesOfDifferentLength(false);

        // Empty dictionary
        Map<Integer, String> emptyErrors = DictionaryHelper.testDictionary(compoundDict, "");
        assertTrue(emptyErrors.containsKey(0));

        // Valid compound dictionary (category length 2, full entry length 4)
        String validText = "01\tCategory One\n0101\tEntry One\n0102\tEntry Two\n";
        Map<Integer, String> noErrors = DictionaryHelper.testDictionary(compoundDict, validText);
        assertTrue("Valid dictionary text should produce no errors", noErrors.isEmpty());

        // Wrong code length (3 chars when only 2 or 4 allowed) and duplicate code
        String invalidText = "01\tCategory One\n010\tWrong Length\n01\tDuplicate Category\n";
        Map<Integer, String> errors = DictionaryHelper.testDictionary(compoundDict, invalidText);
        assertEquals(2, errors.size());
        assertTrue(errors.get(2).contains("Wrong length"));
        assertTrue(errors.get(3).contains("Duplicate code"));
    }

    @Test
    public void testReadCanReg4PDS() throws Exception {
        assertEquals(1, PopulationDataSetHelper.byteArrayToIntLH(new byte[]{1, 0, 0, 0}));
        assertEquals(1, PopulationDataSetHelper.byteArrayToIntHL(new byte[]{0, 0, 0, 1}));

        File pdsFile = tempFolder.newFile("TEST.PDS");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 5 header lines terminated with CRLF
        out.write("CanReg4 Test Population 2020\r\n".getBytes(StandardCharsets.US_ASCII));
        out.write("SEX > 0\r\n".getBytes(StandardCharsets.US_ASCII));
        out.write("20200701\r\n".getBytes(StandardCharsets.US_ASCII));
        out.write("National Statistics Office\r\n".getBytes(StandardCharsets.US_ASCII));
        out.write("World\r\n".getBytes(StandardCharsets.US_ASCII));

        // 18 age groups (5, 85)
        out.write(18);
        for (int i = 0; i < 18; i++) {
            out.write(i * 5); // 1 byte minage
            writeIntLH(out, 1000 + i * 100); // 4 bytes male pop
            writeIntLH(out, 1200 + i * 100); // 4 bytes female pop
            writeShortLH(out, 5000); // 2 bytes world std pop
        }
        // Unknown age male (4 bytes) and female (4 bytes)
        writeIntLH(out, 15);
        writeIntLH(out, 25);

        FileOutputStream fos = new FileOutputStream(pdsFile);
        try {
            fos.write(out.toByteArray());
        } finally {
            fos.close();
        }

        PopulationDataset pds = PopulationDataSetHelper.readCanReg4PDS(pdsFile.getAbsolutePath());
        assertNotNull(pds);
        assertEquals("CanReg4 Test Population 2020", pds.getPopulationDatasetName());
        assertEquals("SEX > 0", pds.getFilter());
        assertEquals("20200701", pds.getDate());
        assertEquals("National Statistics Office", pds.getSource());
        assertEquals(0, pds.getReferencePopulationID());
        assertEquals(1000, pds.getAgeGroupCount(1, 0));
        assertEquals(1200, pds.getAgeGroupCount(2, 0));
        assertEquals(1500, pds.getAgeGroupCount(1, 5));
        assertEquals(1700, pds.getAgeGroupCount(2, 5));
    }

    private void writeShortLH(ByteArrayOutputStream out, int val) {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
    }

    private void writeIntLH(ByteArrayOutputStream out, int val) {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }
}

