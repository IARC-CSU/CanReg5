package canreg.common.database;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link PopulationDataset} and {@link PopulationDatasetsEntry}.
 */
public class PopulationDatasetTest {

    @Test
    public void testPopulationDatasetEntriesAndTotals() throws Exception {
        PopulationDataset pds = new PopulationDataset();
        pds.setPopulationDatasetID(1);
        pds.setPopulationDatasetName("Test Pop 2020");
        pds.setDate("20200101");
        pds.setSource("Census");
        pds.setFilter("YEAR = 2020");
        pds.setDescription("Sample population");

        pds.addAgeGroup(new PopulationDatasetsEntry(0, 1, 1000)); // Male 0-4
        pds.addAgeGroup(new PopulationDatasetsEntry(0, 2, 950));  // Female 0-4
        pds.addAgeGroup(new PopulationDatasetsEntry(1, 1, 1200)); // Male 5-9
        pds.addUnkownAgeGroup(1, 10);

        assertEquals(1000, pds.getAgeGroupCount(1, 0));
        assertEquals(950, pds.getAgeGroupCount(2, 0));
        assertEquals(1200, pds.getAgeGroupCount(1, 1));
        assertEquals(0, pds.getAgeGroupIndex(3));
        assertEquals(1, pds.getAgeGroupIndex(7));

        PopulationDataset worldPop = new PopulationDataset();
        worldPop.setPopulationDatasetID(2);
        worldPop.addAgeGroup(new PopulationDatasetsEntry(0, 1, 12000));
        pds.setReferencePopulation(worldPop);

        assertEquals(2, pds.getReferencePopulationID());
        assertEquals(12000, pds.getReferencePopulationForAgeGroupIndex(1, 0));

        String fileRepr = pds.getStringRepresentationOfAgeGroupsForFile(",");
        assertTrue(fileRepr.contains("2020,0,1,1000"));
        assertTrue(fileRepr.contains("2020,0,2,950"));
    }

    @Test(expected = IncompatiblePopulationDataSetException.class)
    public void testIncompatibleReferencePopulationThrowsException() throws Exception {
        PopulationDataset pds = new PopulationDataset();
        pds.setAgeGroupStructure(new AgeGroupStructure(5, 85));

        PopulationDataset incompatibleRef = new PopulationDataset();
        incompatibleRef.setAgeGroupStructure(new AgeGroupStructure(10, 70));
        pds.setReferencePopulation(incompatibleRef);

        pds.getReferencePopulationForAgeGroupIndex(1, 0);
    }
}

