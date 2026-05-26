package canreg.client.analysis;

import java.io.File;
import java.util.LinkedList;
import org.junit.Test;
import static org.junit.Assert.*;

public class TableBuilderSortingTest {

    @Test
    public void testTableBuilderSorting() {
        LinkedList<String> children = new LinkedList<>();
        children.add("/path/to/02_PopulationPyramid.conf");
        children.add("/path/to/10_1_TopICD10barchartcases.conf");
        children.add("/path/to/01_0_ShinyTable.conf");
        children.add("/path/to/01_1_CanregReport.conf");
        children.add("/path/to/AgeSpecificRatesTopXDetailed.conf");
        children.add("/path/to/z_RTest.conf");

        children.sort((String path1, String path2) -> {
            String name1 = new File(path1).getName();
            String name2 = new File(path2).getName();
            int cmp = name1.compareToIgnoreCase(name2);
            if (cmp != 0) {
                return cmp;
            }
            return path1.compareToIgnoreCase(path2);
        });

        assertEquals("/path/to/01_0_ShinyTable.conf", children.get(0));
        assertEquals("/path/to/01_1_CanregReport.conf", children.get(1));
        assertEquals("/path/to/02_PopulationPyramid.conf", children.get(2));
        assertEquals("/path/to/10_1_TopICD10barchartcases.conf", children.get(3));
        assertEquals("/path/to/AgeSpecificRatesTopXDetailed.conf", children.get(4));
        assertEquals("/path/to/z_RTest.conf", children.get(5));
    }
}
