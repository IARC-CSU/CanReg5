package canreg.client.analysis;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ConfigAndFieldDescriptionReaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testConfigFieldsReaderReadsAndFindsConfig() throws Exception {
        File confFile = tempFolder.newFile("sample.conf");
        FileWriter fw = new FileWriter(confFile);
        try {
            fw.write("table_label {\n\"Age-Specific Incidence\"\n}\n");
            fw.write("sex_label {\n\"Male\"\n\"Female\"\n}\n");
            fw.write("ICD10_groups {\n\"C00-14\"\n\"C15\"\n\"ALL\"\n}\n");
        } finally {
            fw.close();
        }

        FileReader fr = new FileReader(confFile);
        try {
            LinkedList<ConfigFields> list = ConfigFieldsReader.readFile(fr);
            assertEquals(3, list.size());

            String[] labels = ConfigFieldsReader.findConfig("table_label", list);
            assertNotNull(labels);
            assertEquals(1, labels.length);
            assertEquals("Age-Specific Incidence", labels[0]);

            String[] sexes = ConfigFieldsReader.findConfig("sex_label", list);
            assertArrayEquals(new String[]{"Male", "Female"}, sexes);

            assertNull(ConfigFieldsReader.findConfig("non_existent", list));
        } finally {
            fr.close();
        }
    }

    @Test
    public void testFieldDescriptionReaderAndGetContentOfField() throws Exception {
        File dctFile = tempFolder.newFile("sample.dct");
        FileWriter fw = new FileWriter(dctFile);
        try {
            fw.write("infile dictionary {\n");
            fw.write("_column(1)\n");
            fw.write("str8 regno %8s \"Registration Number\"\n");
            fw.write("byte sex %1f \"Sex\"\n");
            fw.write("int age %3f \"Age\"\n");
            fw.write("}\n");
        } finally {
            fw.close();
        }

        FileReader fr = new FileReader(dctFile);
        try {
            @SuppressWarnings("unchecked")
            LinkedList<FieldDescription> fields = FieldDescriptionReader.readFile(fr);
            assertEquals(3, fields.size());
            assertEquals("regno", fields.get(0).getName());
            assertEquals(8, fields.get(0).getCharacters());
            assertEquals(1, fields.get(0).getOffset());

            assertEquals("sex", fields.get(1).getName());
            assertEquals(1, fields.get(1).getCharacters());
            assertEquals(9, fields.get(1).getOffset());

            assertEquals("age", fields.get(2).getName());
            assertEquals(3, fields.get(2).getCharacters());
            assertEquals(10, fields.get(2).getOffset());

            String sampleLine = "202000012045";
            assertEquals("20200001", AbstractEditorialTableBuilder.getContentOfField(fields, "regno", sampleLine));
            assertEquals("2", AbstractEditorialTableBuilder.getContentOfField(fields, "sex", sampleLine));
            assertEquals("045", AbstractEditorialTableBuilder.getContentOfField(fields, "age", sampleLine));
            assertNull(AbstractEditorialTableBuilder.getContentOfField(fields, "missing", sampleLine));
        } finally {
            fr.close();
        }
    }
}

