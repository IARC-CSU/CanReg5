package canreg.client.analysis;

import canreg.common.Globals.StandardVariableNames;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class FixedWidthFileWriterTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testSetOutputFileNameAndWriteCaseLine() throws Exception {
        File defFile = tempFolder.newFile("test_fw_def.tsv");
        FileWriter fw = new FileWriter(defFile);
        try {
            fw.write("name\tcase_col\tpop_col\tlength\trequired\tstd_name\n");
            fw.write("PatientID\t1\t\t8\ttrue\tPatientID\n");
            fw.write("Sex\t9\t1\t1\ttrue\tSex\n");
            fw.write("Year\t10\t2\t4\ttrue\tIncidenceDate[0,4]\n");
            fw.write("Age\t14\t6\t3\tfalse\tAge\n");
        } finally {
            fw.close();
        }

        File outFile = new File(tempFolder.getRoot(), "cases_fixed.txt");
        FixedWidthFileWriter writer = new FixedWidthFileWriter(defFile.getAbsolutePath(), 20, false);
        assertTrue(writer.setOutputFileName(outFile.getAbsolutePath()));
        writer.defineOrder(new String[]{"PatientID", "Sex", "IncidenceDate", "Age"});

        Map<StandardVariableNames, Object> row = new EnumMap<StandardVariableNames, Object>(StandardVariableNames.class);
        row.put(StandardVariableNames.PatientID, "20200001");
        row.put(StandardVariableNames.Sex, "2");
        row.put(StandardVariableNames.IncidenceDate, "20200515");
        row.put(StandardVariableNames.Age, Integer.valueOf(45));

        assertTrue(writer.writeLine(row));
        writer.close();

        BufferedReader reader = new BufferedReader(new FileReader(outFile));
        try {
            String line = reader.readLine();
            assertNotNull(line);
            assertEquals(20, line.length());
            assertEquals("20200001", line.substring(0, 8));
            assertEquals("2", line.substring(8, 9));
            assertEquals("2020", line.substring(9, 13));
            assertEquals("045", line.substring(13, 16));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWritePopulationLineAndDefaultDefinition() throws Exception {
        FixedWidthFileWriter defaultWriter = new FixedWidthFileWriter(50, true);
        File outFile = new File(tempFolder.getRoot(), "pop_fixed.txt");
        assertTrue(defaultWriter.setOutputFileName(outFile.getAbsolutePath()));

        Map<StandardVariableNames, Object> row = new EnumMap<StandardVariableNames, Object>(StandardVariableNames.class);
        row.put(StandardVariableNames.Sex, "1");
        assertTrue(defaultWriter.writeLine(row));
        defaultWriter.close();

        assertTrue(outFile.exists());
        assertTrue(outFile.length() >= 50);
    }
}
