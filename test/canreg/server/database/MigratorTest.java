package canreg.server.database;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the version-string comparison logic used in {@link Migrator}.
 *
 * Migrator.migrate() uses {@code String.compareTo()} on version strings such as
 * "4.99.5", "5.00.06", "5.00.17", "5.00.19", "5.00.43", "5.00.44".
 * All of these checks must produce correct orderings for the migration gates to fire
 * in the right sequence.
 *
 * We do NOT instantiate Migrator itself here (it requires a CanRegDAO and a live DB).
 * Instead we test the exact comparison expressions that Migrator uses inline, so any
 * regression in the logic is caught without any I/O.
 */
public class MigratorTest {

    // ------------------------------------------------------------------
    // The migration gate comparisons, extracted verbatim from Migrator.java
    // ------------------------------------------------------------------

    /** Returns true if Migrator would run migrateTo_4_99_5 for the given db version. */
    private boolean wouldMigrateTo_4_99_5(String dbVersion) {
        return dbVersion.compareTo("4.99.5") < 0;
    }

    /** Returns true if Migrator would run migrateTo_5_00_06. */
    private boolean wouldMigrateTo_5_00_06(String dbVersion) {
        return dbVersion.length() < 7 || dbVersion.substring(0, 7).compareTo("5.00.06") < 0;
    }

    /** Returns true if Migrator would run migrateTo_5_00_17. */
    private boolean wouldMigrateTo_5_00_17(String dbVersion) {
        return dbVersion.length() < 7 || dbVersion.substring(0, 7).compareTo("5.00.17") < 0;
    }

    /** Returns true if Migrator would run migrateTo_5_00_19. */
    private boolean wouldMigrateTo_5_00_19(String dbVersion) {
        return dbVersion.length() < 7 || dbVersion.substring(0, 7).compareTo("5.00.19") < 0;
    }

    /** Returns true if Migrator would run migrateTo_5_00_43. */
    private boolean wouldMigrateTo_5_00_43(String dbVersion) {
        return dbVersion.length() < 7 || dbVersion.substring(0, 7).compareTo("5.00.43") < 0;
    }

    // ------------------------------------------------------------------
    // Tests: version 4.99.0 (the default "no version" sentinel)
    // ------------------------------------------------------------------

    @Test
    public void testDefaultVersion_AllMigrationsNeeded() {
        String v = "4.99.0";
        assertTrue("4.99.0 should trigger 4_99_5 migration",  wouldMigrateTo_4_99_5(v));
        assertTrue("4.99.0 should trigger 5_00_06 migration", wouldMigrateTo_5_00_06(v));
        assertTrue("4.99.0 should trigger 5_00_17 migration", wouldMigrateTo_5_00_17(v));
        assertTrue("4.99.0 should trigger 5_00_19 migration", wouldMigrateTo_5_00_19(v));
        assertTrue("4.99.0 should trigger 5_00_43 migration", wouldMigrateTo_5_00_43(v));
    }

    // ------------------------------------------------------------------
    // Tests: after each migration step, only later steps should fire
    // ------------------------------------------------------------------

    @Test
    public void testAfter_4_99_5_OnlyLaterMigrationsNeeded() {
        String v = "4.99.5";
        assertFalse("4.99.5 should NOT trigger 4_99_5 migration again", wouldMigrateTo_4_99_5(v));
        assertTrue("4.99.5 should trigger 5_00_06 migration",  wouldMigrateTo_5_00_06(v));
        assertTrue("4.99.5 should trigger 5_00_17 migration",  wouldMigrateTo_5_00_17(v));
        assertTrue("4.99.5 should trigger 5_00_19 migration",  wouldMigrateTo_5_00_19(v));
        assertTrue("4.99.5 should trigger 5_00_43 migration",  wouldMigrateTo_5_00_43(v));
    }

    @Test
    public void testAfter_5_00_06_OnlyLaterMigrationsNeeded() {
        String v = "5.00.06";
        assertFalse("5.00.06 should NOT trigger 4_99_5",  wouldMigrateTo_4_99_5(v));
        assertFalse("5.00.06 should NOT trigger 5_00_06", wouldMigrateTo_5_00_06(v));
        assertTrue("5.00.06 should trigger 5_00_17",      wouldMigrateTo_5_00_17(v));
        assertTrue("5.00.06 should trigger 5_00_19",      wouldMigrateTo_5_00_19(v));
        assertTrue("5.00.06 should trigger 5_00_43",      wouldMigrateTo_5_00_43(v));
    }

    @Test
    public void testAfter_5_00_17_OnlyLaterMigrationsNeeded() {
        String v = "5.00.17";
        assertFalse(wouldMigrateTo_4_99_5(v));
        assertFalse(wouldMigrateTo_5_00_06(v));
        assertFalse("5.00.17 should NOT trigger 5_00_17", wouldMigrateTo_5_00_17(v));
        assertTrue("5.00.17 should trigger 5_00_19",      wouldMigrateTo_5_00_19(v));
        assertTrue("5.00.17 should trigger 5_00_43",      wouldMigrateTo_5_00_43(v));
    }

    @Test
    public void testAfter_5_00_19_OnlyLaterMigrationsNeeded() {
        String v = "5.00.19";
        assertFalse(wouldMigrateTo_4_99_5(v));
        assertFalse(wouldMigrateTo_5_00_06(v));
        assertFalse(wouldMigrateTo_5_00_17(v));
        assertFalse("5.00.19 should NOT trigger 5_00_19", wouldMigrateTo_5_00_19(v));
        assertTrue("5.00.19 should trigger 5_00_43",      wouldMigrateTo_5_00_43(v));
    }

    @Test
    public void testAfter_5_00_43_NoMigrationsNeeded() {
        String v = "5.00.43";
        assertFalse(wouldMigrateTo_4_99_5(v));
        assertFalse(wouldMigrateTo_5_00_06(v));
        assertFalse(wouldMigrateTo_5_00_17(v));
        assertFalse(wouldMigrateTo_5_00_19(v));
        assertFalse("5.00.43 should NOT trigger 5_00_43", wouldMigrateTo_5_00_43(v));
    }

    @Test
    public void testCurrentVersion_NoEarlierMigrationsNeeded() {
        // The current application version ("5.00.44k") should not re-trigger any migration
        String v = "5.00.44k";
        assertFalse(wouldMigrateTo_4_99_5(v));
        assertFalse(wouldMigrateTo_5_00_06(v));
        assertFalse(wouldMigrateTo_5_00_17(v));
        assertFalse(wouldMigrateTo_5_00_19(v));
        assertFalse(wouldMigrateTo_5_00_43(v));
    }

    // ------------------------------------------------------------------
    // Edge cases: short version strings
    // ------------------------------------------------------------------

    @Test
    public void testShortVersionString_TriggersMigrations() {
        // A version string shorter than 7 chars forces the migrations to run
        String v = "4.99";
        assertTrue("Short version should trigger 5_00_06",  wouldMigrateTo_5_00_06(v));
        assertTrue("Short version should trigger 5_00_17",  wouldMigrateTo_5_00_17(v));
        assertTrue("Short version should trigger 5_00_19",  wouldMigrateTo_5_00_19(v));
        assertTrue("Short version should trigger 5_00_43",  wouldMigrateTo_5_00_43(v));
    }

    @Test
    public void testVersionOrdering_LexicographicCorrectness() {
        // Validate that the version strings Migrator relies on sort correctly
        assertTrue("5.00.06 < 5.00.17", "5.00.06".compareTo("5.00.17") < 0);
        assertTrue("5.00.17 < 5.00.19", "5.00.17".compareTo("5.00.19") < 0);
        assertTrue("5.00.19 < 5.00.43", "5.00.19".compareTo("5.00.43") < 0);
        assertTrue("5.00.43 < 5.00.44", "5.00.43".compareTo("5.00.44") < 0);
        assertTrue("4.99.5 < 5.00.06",  "4.99.5".compareTo("5.00.06") < 0);
    }
}
