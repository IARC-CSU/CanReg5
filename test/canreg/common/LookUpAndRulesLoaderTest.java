package canreg.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link LookUpLoader} and {@link RulesLoader}.
 */
public class LookUpAndRulesLoaderTest {

    @Test
    public void testLookUpLoaderStripsCommentsAndSplitsAtCodeLength() throws Exception {
        String content = "8000*01/Neoplasm, NOS\n"
                + "8140-02/Adenocarcinoma, NOS\n"
                + "8500+21\n";
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Map<String, String> map = LookUpLoader.load(in, 4);

        assertEquals(3, map.size());
        assertEquals("*01", map.get("8000"));
        assertEquals("-02", map.get("8140"));
        assertEquals("+21", map.get("8500"));
    }

    @Test
    public void testRulesLoaderLoadAndLoadTable() throws Exception {
        String lines = "RuleLine0\nRuleLine1\n";
        InputStream in1 = new ByteArrayInputStream(lines.getBytes(StandardCharsets.UTF_8));
        Map<Integer, String> rules = RulesLoader.load(in1, 7);
        assertEquals(2, rules.size());
        assertEquals("RuleLine0", rules.get(0));
        assertEquals("RuleLine1", rules.get(1));

        String tableContent = "ColA\tColB\tColC\n1\t2\t3\n";
        InputStream in2 = new ByteArrayInputStream(tableContent.getBytes(StandardCharsets.UTF_8));
        LinkedList<String[]> table = RulesLoader.loadTable(in2);
        assertEquals(2, table.size());
        assertArrayEquals(new String[]{"ColA", "ColB", "ColC"}, table.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, table.get(1));
    }
}

