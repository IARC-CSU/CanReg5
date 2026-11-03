package canreg.server.management;

import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

public class SystemDefinitionConverterTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testByteArrayToIntLHAndHL() {
        // Little-endian (LH): least significant byte first
        assertEquals(1, SystemDefinitionConverter.byteArrayToIntLH(new byte[]{1, 0}));
        assertEquals(258, SystemDefinitionConverter.byteArrayToIntLH(new byte[]{2, 1}));
        assertEquals(-1, SystemDefinitionConverter.byteArrayToIntLH(new byte[]{(byte) 0xFF, (byte) 0xFF}));

        // Big-endian (HL)
        assertEquals(1, SystemDefinitionConverter.byteArrayToIntHL(new byte[]{0, 1}));
    }

    @Test
    public void testSplitIndexMapInTumourAndPatient() {
        DatabaseVariablesListElement patVar = new DatabaseVariablesListElement(
                Globals.PATIENT_TABLE_NAME, 0, "RegNo", Globals.VARIABLE_TYPE_ALPHA_NAME);

        DatabaseVariablesListElement tumVar = new DatabaseVariablesListElement(
                Globals.TUMOUR_TABLE_NAME, 1, "Top", Globals.VARIABLE_TYPE_ALPHA_NAME);

        DatabaseIndexesListElement mixedIndex = new DatabaseIndexesListElement("MixedIdx");
        mixedIndex.setVariablesInIndex(new DatabaseVariablesListElement[]{patVar, tumVar});

        Map<String, DatabaseIndexesListElement> indexMap = new TreeMap<String, DatabaseIndexesListElement>();
        indexMap.put("MixedIdx", mixedIndex);

        Map<String, String> variableToTableMap = new TreeMap<String, String>();
        variableToTableMap.put("REGNO", Globals.PATIENT_TABLE_NAME);
        variableToTableMap.put("TOP", Globals.TUMOUR_TABLE_NAME);

        Map<String, DatabaseIndexesListElement> split = SystemDefinitionConverter.splitIndexMapInTumourAndPatient(
                indexMap, variableToTableMap);

        assertEquals(2, split.size());
        assertTrue(split.containsKey("MixedIdx-" + Globals.PATIENT_TABLE_NAME));
        assertTrue(split.containsKey("MixedIdx-" + Globals.TUMOUR_TABLE_NAME));
    }

    @Test
    public void testConvertAndSaveFromSyntheticCanReg4Def() throws Exception {
        File defFile = tempFolder.newFile("TEST.DEF");
        File xmlOut = new File(tempFolder.getRoot(), "TEST.xml");

        writeMinimalCanReg4Def(defFile);

        SystemDefinitionConverter converter = new SystemDefinitionConverter();
        converter.setFileEncoding(StandardCharsets.UTF_8);
        String result = converter.convertAndSave(defFile.getAbsolutePath(), xmlOut.getAbsolutePath());

        assertEquals("Success", result);
        assertEquals("TST", converter.getServerCode());
        assertEquals("Test Registry Name", converter.getRegistryName());
        assertNotNull(converter.getDoc());
        assertTrue(xmlOut.exists());

        Document doc = converter.getDoc();
        assertTrue(doc.getElementsByTagName("ns3:variable").getLength() >= 10);
        assertTrue(doc.getElementsByTagName("ns3:dictionary").getLength() == 1);
    }

    private void writeMinimalCanReg4Def(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 1. General: 3 chars Registry Code ("TST"), 1 char Region ('4'), null-terminated Registry Name, 1 char Language ('E')
        out.write("TST".getBytes(StandardCharsets.US_ASCII));
        out.write('4');
        writeNullTerminated(out, "Test|Registry|Name");
        out.write('E');

        // 2. Dictionaries: 1 simple dictionary
        writeShortLH(out, 1);
        writeNullTerminated(out, "SexDict");
        out.write(0); // Font: Latin
        out.write(0); // Type: Simple
        writeShortLH(out, 1); // full_dictionary_code_length
        writeShortLH(out, 10); // full_dictionary_description_length

        // 3. Groups: 2 groups ("Patient", "Tumour")
        writeShortLH(out, 2);
        writeNullTerminated(out, "Patient");
        writeShortLH(out, 0); // order
        writeShortLH(out, 100); // height
        writeNullTerminated(out, "Tumour");
        writeShortLH(out, 1); // order
        writeShortLH(out, 100); // height

        // 4. Variables: 2 variables (RegNo in Patient, Top in Tumour)
        writeShortLH(out, 2);
        // Variable 0: RegNo
        writeNullTerminated(out, "Registration Number");
        writeNullTerminated(out, "RegNo");
        writeNullTerminated(out, "Reg.No.");
        out.write(0); // group 0 (Patient)
        writeShortLH(out, 10);
        writeShortLH(out, 10);
        writeShortLH(out, 50);
        writeShortLH(out, 10);
        out.write(1); // fill-in status: Mandatory
        out.write(0); // mp copy: Must
        out.write(1); // type: Alpha
        writeShortLH(out, 8); // length: 8

        // Variable 1: Top
        writeNullTerminated(out, "Topography");
        writeNullTerminated(out, "Top");
        writeNullTerminated(out, "Topography");
        out.write(1); // group 1 (Tumour)
        writeShortLH(out, 10);
        writeShortLH(out, 30);
        writeShortLH(out, 50);
        writeShortLH(out, 30);
        out.write(1); // Mandatory
        out.write(3); // Othr
        out.write(1); // Alpha
        writeShortLH(out, 4); // length 4

        // 5. Indexes: 1 index on variable 0
        writeShortLH(out, 1);
        writeNullTerminated(out, "RegNoIdx");
        writeShortLH(out, 0);
        writeShortLH(out, -1);
        writeShortLH(out, -1);

        // 6. Person Search: 1 search variable (variable 0, weight 10), min match 70
        writeShortLH(out, 1);
        writeShortLH(out, 0);
        writeShortLH(out, 10);
        writeShortLH(out, 70);

        // 7. Standard variables: 1 (index 0 = PatientID -> variable 0)
        writeShortLH(out, 1);
        writeShortLH(out, 0);

        // 8. Miscellaneous
        out.write("129".getBytes(StandardCharsets.US_ASCII)); // male, female, unknown
        out.write(0); // date_format
        out.write('/'); // date_separator
        out.write(0); // fast_safe_mode
        out.write('4'); // morphology_length (4 digits so behaviour replacement is not needed)
        out.write(0); // mult_prim_rules
        out.write(0); // special_registry
        out.write(0); // password_rules
        out.write('E'); // data_entry_language
        out.write(0); // registration_number_type
        out.write('3'); // mult_prim_code_length
        out.write(0); // basis_diag_codes

        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.write(out.toByteArray());
        } finally {
            fos.close();
        }
    }

    private void writeNullTerminated(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
        out.write(0);
    }

    private void writeShortLH(ByteArrayOutputStream out, int val) {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
    }
}