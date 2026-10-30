package canreg.client.analysis;

import canreg.TestFixtures;
import canreg.client.analysis.TableBuilderInterface.ChartType;
import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.client.analysis.Tools.KeyCancerGroupsEnum;
import canreg.common.database.AgeGroupStructure;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class AnalysisToolsAndTableBuilderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        TestFixtures.initHeadlessLocalSettings();
    }

    @Test
    public void testEditorialTableToolsParseAndLookupICD10Groups() {
        String[] config = new String[]{"C00-02", "C15", "C16.0-2", "C44", "MES", "KAP", "O&U", "ALL"};
        LinkedList[] groups = EditorialTableTools.generateICD10Groups(config);

        assertEquals(8, groups.length);
        // C00-02 should cover 0..29 (30 codes)
        assertEquals(30, groups[0].size());
        // C15 should cover 150..159 (10 codes)
        assertEquals(10, groups[1].size());
        // C16.0-2 should cover 160, 161, 162 (3 codes)
        assertEquals(3, groups[2].size());
        // Special groups MES, KAP, O&U, ALL return empty numeric lists
        assertTrue(groups[4].isEmpty());
        assertTrue(groups[7].isEmpty());

        assertEquals(0, EditorialTableTools.getICD10index(10, groups));
        assertEquals(1, EditorialTableTools.getICD10index(155, groups));
        assertEquals(2, EditorialTableTools.getICD10index(161, groups));
        assertEquals(-1, EditorialTableTools.getICD10index(999, groups));

        assertEquals(3, EditorialTableTools.getICD10index("C44", config));
        assertEquals(7, EditorialTableTools.getICD10index("all", config));
        assertEquals(-1, EditorialTableTools.getICD10index("UNKNOWN", config));
    }

    @Test
    public void testAssignICDGroupIndex() {
        String[] config = new String[]{"C15", "C67", "MES", "KAP", "MPD", "MDS", "O&U", "ALL"};
        LinkedList[] groups = EditorialTableTools.generateICD10Groups(config);

        Map<KeyCancerGroupsEnum, Integer> keyMap = new EnumMap<KeyCancerGroupsEnum, Integer>(KeyCancerGroupsEnum.class);
        keyMap.put(KeyCancerGroupsEnum.bladderCancerGroupIndex, 1);
        keyMap.put(KeyCancerGroupsEnum.mesotheliomaCancerGroupIndex, 2);
        keyMap.put(KeyCancerGroupsEnum.kaposiSarkomaCancerGroupIndex, 3);
        keyMap.put(KeyCancerGroupsEnum.myeloproliferativeDisordersCancerGroupIndex, 4);
        keyMap.put(KeyCancerGroupsEnum.myelodysplasticSyndromesCancerGroupIndex, 5);
        keyMap.put(KeyCancerGroupsEnum.otherCancerGroupsIndex, 6);
        keyMap.put(KeyCancerGroupsEnum.allCancerGroupsIndex, 7);

        // Morphology 9140 -> Kaposi sarcoma (index 3)
        assertEquals(3, Tools.assignICDGroupIndex(keyMap, "C449", "9140", groups));
        // Morphology 9050 -> Mesothelioma (index 2)
        assertEquals(2, Tools.assignICDGroupIndex(keyMap, "C384", "9050", groups));
        // Regular C15 -> index 0
        assertEquals(0, Tools.assignICDGroupIndex(keyMap, "C159", "8070", groups));
        // Unlisted C-code -> Other (index 6)
        assertEquals(6, Tools.assignICDGroupIndex(keyMap, "C509", "8500", groups));
        // D09.0 / D41.4 -> Bladder (index 1)
        assertEquals(1, Tools.assignICDGroupIndex(keyMap, "D090", "8120", groups));
        // D45 -> MPD (index 4)
        assertEquals(4, Tools.assignICDGroupIndex(keyMap, "D45", "9950", groups));
        // D46 -> MDS (index 5)
        assertEquals(5, Tools.assignICDGroupIndex(keyMap, "D469", "9989", groups));
        // Other D-code -> DONT_COUNT
        assertEquals(Tools.DONT_COUNT, Tools.assignICDGroupIndex(keyMap, "D120", "8140", groups));
    }

    @Test
    public void testSumUpTheRestDarkenAndCharts() throws Exception {
        LinkedList<CancerCasesCount> counts = new LinkedList<CancerCasesCount>();
        counts.add(new CancerCasesCount("C15", "Oesophagus", 10.0, 0));
        counts.add(new CancerCasesCount("C44", "Skin", 25.0, 1));
        counts.add(new CancerCasesCount("C50", "Breast", 40.0, 2));

        // Exclude index 1 (Skin)
        double rest = Tools.sumUpTheRest(counts, Arrays.asList(1));
        assertEquals(50.0, rest, 0.001);

        Color original = new Color(100, 200, 50);
        Color darkened = Tools.darken(original);
        assertEquals(90, darkened.getRed());
        assertEquals(180, darkened.getGreen());
        assertEquals(45, darkened.getBlue());

        JFreeChart pieChart = Tools.generateJChart(
                counts, "pie.png", "Top Cancers", FileTypes.png, ChartType.PIE,
                true, true, 5.0, 80.0, Color.BLUE, "Site");
        assertNotNull(pieChart);
        String pieCsv = Tools.getChartData(pieChart, ",", true);
        assertTrue(pieCsv.contains("Breast"));
        assertTrue(pieCsv.contains("Other"));

        File csvFile = new File(tempFolder.getRoot(), "chart.csv");
        Tools.exportChartAsCSV(pieChart, csvFile);
        assertTrue(csvFile.exists());
        assertTrue(csvFile.length() > 0);
    }

    @Test
    public void testWritePopulationsToFileAndPopulationPyramidBuilder() throws Exception {
        PopulationDataset refPop = new PopulationDataset();
        refPop.setPopulationDatasetID(0);
        refPop.setPopulationDatasetName("World Standard");
        refPop.setAgeGroupStructure(new AgeGroupStructure(5, 85));
        refPop.setReferencePopulationBool(true);
        for (int i = 0; i < 18; i++) {
            refPop.addAgeGroup(new PopulationDatasetsEntry(i, 1, 5000));
            refPop.addAgeGroup(new PopulationDatasetsEntry(i, 2, 5000));
        }

        PopulationDataset pop = new PopulationDataset();
        pop.setPopulationDatasetID(1);
        pop.setPopulationDatasetName("Test Registry, 2020");
        pop.setFilter("");
        pop.setAgeGroupStructure(new AgeGroupStructure(5, 85));
        pop.setReferencePopulation(refPop);
        for (int i = 0; i < 18; i++) {
            pop.addAgeGroup(new PopulationDatasetsEntry(i, 1, 1000 + i * 10));
            pop.addAgeGroup(new PopulationDatasetsEntry(i, 2, 1100 + i * 10));
        }

        File popCsv = new File(tempFolder.getRoot(), "populations.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(popCsv));
        try {
            Tools.writePopulationsToFile(bw, 2020, new PopulationDataset[]{pop}, ",");
        } finally {
            bw.close();
        }
        assertTrue(popCsv.exists());
        assertTrue(popCsv.length() > 100);

        PopulationPyramidTableBuilder builder = new PopulationPyramidTableBuilder();
        assertTrue(builder.areThesePopulationDatasetsCompatible(new PopulationDataset[]{pop}));
        assertArrayEquals(new String[]{"a", "b", "c"}, AbstractEditorialTableBuilder.breakDownLine(',', "a,b,c"));

        LinkedList<ConfigFields> configList = new LinkedList<ConfigFields>();
        ConfigFields tableLabel = new ConfigFields("table_label");
        tableLabel.addValue("Population Pyramid");
        configList.add(tableLabel);

        ConfigFields sexLabel = new ConfigFields("sex_label");
        sexLabel.addValue("Male");
        sexLabel.addValue("Female");
        configList.add(sexLabel);

        File outPrefix = new File(tempFolder.getRoot(), "pyramid_report");
        LinkedList<String> files = builder.buildTable(
                "Test Registry",
                outPrefix.getAbsolutePath(),
                2020,
                2020,
                new Object[0][0],
                new PopulationDataset[]{pop},
                new PopulationDataset[]{refPop},
                configList,
                new String[0],
                FileTypes.ps,
                "en");

        assertEquals(1, files.size());
        File psFile = new File(files.getFirst());
        assertTrue(psFile.exists());
        assertTrue(psFile.length() > 200);
    }
}

