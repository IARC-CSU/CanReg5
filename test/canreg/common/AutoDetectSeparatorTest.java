package canreg.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/**
 * Unit tests for separator (delimiter) auto-detection in {@link canreg.common.Tools}.
 */
public class AutoDetectSeparatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File createTestFile(String fileName, String content) throws IOException {
        File file = tempFolder.newFile(fileName);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        return file;
    }

    @Test
    public void testAutoDetectCommaSeparated() throws IOException {
        String content = "ID,Name,Diagnosis,Age\n"
                + "1,\"Doe, Jane\",C50.9,45\n"
                + "2,\"Smith, John, Jr.\",C34.1,60\n"
                + "3,\"Brown, Bob\",C18.0,52\n";
        File file = createTestFile("patients.csv", content);
        char sep = Tools.autoDetectSeparator(file, StandardCharsets.UTF_8);
        assertEquals(',', sep);
    }

    @Test
    public void testAutoDetectTabSeparated() throws IOException {
        String content = "ID\tName\tAddress\tDiagnosis\n"
                + "1\tJane Doe\t123 Main St, Apt 4\tC50.9\n"
                + "2\tJohn Smith\t456 High St, Suite 10\tC34.1\n"
                + "3\tBob Brown\t789 Elm St, Box 1\tC18.0\n";
        File file = createTestFile("patients.txt", content);
        char sep = Tools.autoDetectSeparator(file, StandardCharsets.UTF_8);
        assertEquals('\t', sep);
    }

    @Test
    public void testAutoDetectSemicolonSeparated() throws IOException {
        String content = "ID;Name;City;Age\n"
                + "1;Jane Doe;Berlin;45\n"
                + "2;Hans Mueller;Munich;60\n"
                + "3;Greta Weber;Hamburg;52\n";
        File file = createTestFile("records.csv", content);
        char sep = Tools.autoDetectSeparator(file, StandardCharsets.UTF_8);
        assertEquals(';', sep);
    }

    @Test
    public void testAutoDetectPipeSeparated() throws IOException {
        String content = "ID|Name|ICD10|Year\n"
                + "1001|Doe, Jane|C50.9|2021\n"
                + "1002|Smith, John|C34.1|2022\n"
                + "1003|Brown, Bob|C18.0|2023\n";
        File file = createTestFile("tumours.txt", content);
        char sep = Tools.autoDetectSeparator(file, StandardCharsets.UTF_8);
        assertEquals('|', sep);
    }

    @Test
    public void testAutoDetectSingleLineFile() throws IOException {
        File csvFile = createTestFile("single.csv", "ID,Name,Diagnosis,Age\n");
        assertEquals(',', Tools.autoDetectSeparator(csvFile, StandardCharsets.UTF_8));

        File tsvFile = createTestFile("single.txt", "ID\tName\tDiagnosis\tAge\n");
        assertEquals('\t', Tools.autoDetectSeparator(tsvFile, StandardCharsets.UTF_8));

        File semiFile = createTestFile("single_semi.txt", "ID;Name;Diagnosis;Age\n");
        assertEquals(';', Tools.autoDetectSeparator(semiFile, StandardCharsets.UTF_8));

        File pipeFile = createTestFile("single_pipe.txt", "ID|Name|Diagnosis|Age\n");
        assertEquals('|', Tools.autoDetectSeparator(pipeFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testAutoDetectFallbackByExtensionForSingleColumn() throws IOException {
        String singleColumnContent = "PatientID\n1001\n1002\n1003\n";

        File tsvFile = createTestFile("single_col.tsv", singleColumnContent);
        assertEquals('\t', Tools.autoDetectSeparator(tsvFile, StandardCharsets.UTF_8));

        File tabFile = createTestFile("single_col.tab", singleColumnContent);
        assertEquals('\t', Tools.autoDetectSeparator(tabFile, StandardCharsets.UTF_8));

        File csvFile = createTestFile("single_col.csv", singleColumnContent);
        assertEquals(',', Tools.autoDetectSeparator(csvFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testAutoDetectEmptyAndNonExistentFiles() throws IOException {
        File emptyFile = createTestFile("empty.tsv", "");
        assertEquals('\t', Tools.autoDetectSeparator(emptyFile, StandardCharsets.UTF_8));

        File emptyCsv = createTestFile("empty.csv", "");
        assertEquals(',', Tools.autoDetectSeparator(emptyCsv, StandardCharsets.UTF_8));

        File nonExistent = new File(tempFolder.getRoot(), "does_not_exist.tsv");
        assertEquals('\t', Tools.autoDetectSeparator(nonExistent, StandardCharsets.UTF_8));

        assertEquals(',', Tools.autoDetectSeparator((File) null, StandardCharsets.UTF_8));
        assertEquals(',', Tools.autoDetectSeparator((String) null, StandardCharsets.UTF_8));
        assertEquals(',', Tools.autoDetectSeparator("", StandardCharsets.UTF_8));
    }

    @Test
    public void testAutoDetectFilePathStringOverload() throws IOException {
        String content = "A,B,C\n1,2,3\n4,5,6\n";
        File file = createTestFile("string_path.csv", content);
        assertEquals(',', Tools.autoDetectSeparator(file.getAbsolutePath(), StandardCharsets.UTF_8));
    }
}
