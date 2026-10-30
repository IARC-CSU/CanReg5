package canreg.common.qualitycontrol;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link DefaultMultiplePrimaryTester}.
 */
public class DefaultMultiplePrimaryTesterTest {

    private final DefaultMultiplePrimaryTester tester = new DefaultMultiplePrimaryTester();

    @Test
    public void testSameSiteSameMorphology_Duplicate() {
        // Adenocarcinoma (8140) in colon C180 and C182 (both topGroup 18) -> Duplicate
        int res = tester.multiplePrimaryTest("180", "8140", "182", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptDuplicate, res);
    }

    @Test
    public void testSameMorphologyDifferentSiteGroups_MultiplePrimary() {
        // Adenocarcinoma (8140) in colon (180) vs lung (340) -> Multiple Primary
        int res = tester.multiplePrimaryTest("180", "8140", "340", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptMultPrim, res);
    }

    @Test
    public void testDifferentMorphologyGroupsSameSite_MultiplePrimary() {
        // Squamous cell carcinoma (8070, group 1) vs Adenocarcinoma (8140, group 3) in lung (340) -> Multiple Primary
        int res = tester.multiplePrimaryTest("340", "8070", "340", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptMultPrim, res);
    }

    @Test
    public void testCarcinomaNosAndSpecificCarcinomaSameSite_Duplicate() {
        // Unspecified carcinoma (8010, group 5) and Adenocarcinoma (8140, group 3) in same site (340) -> Duplicate
        int res = tester.multiplePrimaryTest("340", "8010", "340", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptDuplicate, res);
    }

    @Test
    public void testSystemicLymphomaDifferentSites_Duplicate() {
        // B-cell lymphoma (9680, group 9) is systemic (groups 7-15): only one allowed regardless of site -> Duplicate
        int res = tester.multiplePrimaryTest("770", "9680", "160", "9680");
        assertEquals(MultiplePrimaryTesterInterface.mptDuplicate, res);
    }

    @Test
    public void testSpecificHematopoieticAndUnspecifiedLymphoid_Duplicate() {
        // Group 8-13 (e.g. 9650 Hodgkin, group 11) + Group 14 (9590 lymphoma NOS) -> Duplicate
        int res = tester.multiplePrimaryTest("770", "9650", "771", "9590");
        assertEquals(MultiplePrimaryTesterInterface.mptDuplicate, res);
    }

    @Test
    public void testUnknownTopographyOneSide_UnkTopog() {
        // Same morph group (8140) with one unknown primary site (809 -> topGroup 80) -> mptUnkTopog
        int res = tester.multiplePrimaryTest("180", "8140", "809", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptUnkTopog, res);
    }

    @Test
    public void testBothUnknownTopographySameMorph_Duplicate() {
        int res = tester.multiplePrimaryTest("809", "8140", "809", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptDuplicate, res);
    }

    @Test
    public void testInvalidMorphology_Invalid() {
        int res = tester.multiplePrimaryTest("180", "7000", "180", "8140");
        assertEquals(MultiplePrimaryTesterInterface.mptInvalid, res);
    }

    @Test
    public void testGroupedTopographiesTreatSubsitesAsSameOrgan() {
        // C01 and C02 map to topogGroup 1; C33 and C34 map to topogGroup 33; C65-C68 map to topogGroup 65
        assertEquals(1, DefaultMultiplePrimaryTester.getTopographyGroup("C019"));
        assertEquals(1, DefaultMultiplePrimaryTester.getTopographyGroup("029"));
        assertEquals(33, DefaultMultiplePrimaryTester.getTopographyGroup("339"));
        assertEquals(33, DefaultMultiplePrimaryTester.getTopographyGroup("341"));
        assertEquals(65, DefaultMultiplePrimaryTester.getTopographyGroup("670"));
        assertEquals(65, DefaultMultiplePrimaryTester.getTopographyGroup("689"));
    }
}

