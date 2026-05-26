package canreg.common.database;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link AgeGroupStructure}.
 * All tests are hermetic – no I/O, no DB.
 */
public class AgeGroupStructureTest {

    // ------------------------------------------------------------------
    // Constructor / parsing
    // ------------------------------------------------------------------

    @Test
    public void testTwoArgConstructorDefaults() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(5, ags.getSizeOfGroups());
        assertEquals(85, ags.getMaxAge());
        assertEquals(5, ags.getSizeOfFirstGroup()); // should default to sizeOfGroups
    }

    @Test
    public void testStringConstructorTwoArgs() {
        AgeGroupStructure ags = new AgeGroupStructure("5,85");
        assertEquals(5, ags.getSizeOfGroups());
        assertEquals(85, ags.getMaxAge());
    }

    @Test
    public void testStringConstructorFourArgs() {
        AgeGroupStructure ags = new AgeGroupStructure("5,85,1,75");
        assertEquals(5, ags.getSizeOfGroups());
        assertEquals(85, ags.getMaxAge());
        assertEquals(1, ags.getSizeOfFirstGroup());
        assertEquals(75, ags.getCutOfAge());
    }

    @Test
    public void testStringConstructorWithSpaces() {
        AgeGroupStructure ags = new AgeGroupStructure(" 5 , 85 , 5 ");
        assertEquals(5, ags.getSizeOfGroups());
        assertEquals(85, ags.getMaxAge());
    }

    // ------------------------------------------------------------------
    // whatAgeGroupIsThisAge
    // ------------------------------------------------------------------

    @Test
    public void testWhatAgeGroupIsThisAge_zeroIsFirstGroup() {
        // Standard 5-year groups, first group 0-4
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(0, ags.whatAgeGroupIsThisAge(0));
    }

    @Test
    public void testWhatAgeGroupIsThisAge_ageJustBelowFirstGroupBoundary() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(0, ags.whatAgeGroupIsThisAge(4)); // 0-4 → group 0
    }

    @Test
    public void testWhatAgeGroupIsThisAge_ageAtFirstGroupBoundary() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(1, ags.whatAgeGroupIsThisAge(5)); // 5-9 → group 1
    }

    @Test
    public void testWhatAgeGroupIsThisAge_midRange() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(4, ags.whatAgeGroupIsThisAge(20)); // 20-24 → group 4
    }

    @Test
    public void testWhatAgeGroupIsThisAge_atMaxAge() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        // Ages >= maxAge are capped to maxAge and map to the last group
        int grp85 = ags.whatAgeGroupIsThisAge(85);
        int grp90 = ags.whatAgeGroupIsThisAge(90);
        assertEquals(grp85, grp90);
    }

    @Test
    public void testWhatAgeGroupIsThisAge_aboveCutoffReturnsMinusOne() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85, 5, 75);
        assertEquals(-1, ags.whatAgeGroupIsThisAge(80));
    }

    @Test
    public void testWhatAgeGroupIsThisAge_singleYearFirstGroup() {
        // sizeOfFirstGroup=1 means age 0 alone is group 0, ages 1-4 are group 1
        AgeGroupStructure ags = new AgeGroupStructure(5, 85, 1);
        assertEquals(0, ags.whatAgeGroupIsThisAge(0));
        assertEquals(1, ags.whatAgeGroupIsThisAge(1));
        assertEquals(1, ags.whatAgeGroupIsThisAge(4));
        assertEquals(2, ags.whatAgeGroupIsThisAge(5));
    }

    // ------------------------------------------------------------------
    // getAgeGroupNames
    // ------------------------------------------------------------------

    @Test
    public void testAgeGroupNamesNotNull() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertNotNull(ags.getAgeGroupNames());
        assertTrue(ags.getAgeGroupNames().length > 0);
    }

    @Test
    public void testAgeGroupNamesFirstEntryStandardGroups() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals("0-4", ags.getAgeGroupNames()[0]);
    }

    @Test
    public void testAgeGroupNamesFirstEntryWithSingleYearFirstGroup() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85, 1);
        assertEquals("0", ags.getAgeGroupNames()[0]);
        assertEquals("1-4", ags.getAgeGroupNames()[1]);
    }

    @Test
    public void testAgeGroupNamesLastEntryIsPlusGroup() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        String[] names = ags.getAgeGroupNames();
        String last = names[names.length - 1];
        assertTrue("Last age group should end with '+': " + last, last.endsWith("+"));
    }

    @Test
    public void testGetNumberOfAgeGroupsMatchesNamesArray() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(ags.getAgeGroupNames().length, ags.getNumberOfAgeGroups());
    }

    // ------------------------------------------------------------------
    // equals / hashCode
    // ------------------------------------------------------------------

    @Test
    public void testEqualsIdenticalParameters() {
        AgeGroupStructure a = new AgeGroupStructure(5, 85);
        AgeGroupStructure b = new AgeGroupStructure(5, 85);
        assertEquals(a, b);
    }

    @Test
    public void testEqualsDifferentParameters() {
        AgeGroupStructure a = new AgeGroupStructure(5, 85);
        AgeGroupStructure b = new AgeGroupStructure(10, 85);
        assertNotEquals(a, b);
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        AgeGroupStructure a = new AgeGroupStructure(5, 85);
        AgeGroupStructure b = new AgeGroupStructure(5, 85);
        assertEquals("Equal objects must have equal hash codes", a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNonAgeGroupStructure() {
        AgeGroupStructure a = new AgeGroupStructure(5, 85);
        assertNotEquals(a, "not an AgeGroupStructure");
    }

    // ------------------------------------------------------------------
    // getLowestAgeForAgeGroupByIndex / getHighestAgeForAgeGroupByIndex
    // ------------------------------------------------------------------

    @Test
    public void testGetLowestAgeForGroupZero() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(0, ags.getLowestAgeForAgeGroupByIndex(0));
    }

    @Test
    public void testGetLowestAgeForGroupOne() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        // Formula: sizeOfFirstGroup + index * sizeOfGroups = 5 + 1*5 = 10
        // NOTE: this appears to be a bug in the source – group 1 should start at age 5
        // but the implementation returns 10.  Test documents actual behavior.
        assertEquals(10, ags.getLowestAgeForAgeGroupByIndex(1));
    }

    @Test
    public void testGetHighestAgeForGroupZeroIsFirstGroupMinusOne() {
        AgeGroupStructure ags = new AgeGroupStructure(5, 85);
        assertEquals(4, ags.getHighestAgeForAgeGroupByIndex(0));
    }
}
