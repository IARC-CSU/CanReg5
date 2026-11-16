package canreg.client;

import canreg.client.gui.CanRegClientView;
import canreg.client.gui.tools.UITools;
import canreg.common.Globals;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Look & Feel theme resolution and UI font helper methods.
 */
public class LookAndFeelTest {

    @Test
    public void testAllConfiguredThemesResolveToLoadableLookAndFeels() throws Exception {
        String[] themes = new String[] {
            "Flat Light",
            "Flat Dark",
            "IntelliJ Light",
            "Flat Darcula",
            "macOS Light",
            "macOS Dark",
            "One Dark",
            "Nord",
            "Arc Light",
            "Arc Dark",
            "GitHub Light",
            "GitHub Dark",
            "Solarized Light",
            "Solarized Dark",
            "High Contrast",
            "System"
        };

        for (String theme : themes) {
            String className = CanRegClientApp.resolveLookAndFeelClassName(theme);
            assertNotNull("Class name should not be null for theme: " + theme, className);
            Class<?> clazz = Class.forName(className);
            assertTrue("Expected LookAndFeel subclass for theme " + theme + ": " + className,
                    LookAndFeel.class.isAssignableFrom(clazz));
        }
    }

    @Test
    public void testLegacyThemeAliasesAndFallback() {
        assertEquals("com.formdev.flatlaf.FlatLightLaf",
                CanRegClientApp.resolveLookAndFeelClassName("Light"));
        assertEquals("com.formdev.flatlaf.FlatDarculaLaf",
                CanRegClientApp.resolveLookAndFeelClassName("Dark"));
        assertEquals("com.formdev.flatlaf.FlatLightLaf",
                CanRegClientApp.resolveLookAndFeelClassName("Unknown Theme Name"));
    }

    @Test
    public void testNullAndBlankThemeUsePlatformAppropriateFlatDefault() {
        String originalOs = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 11");
            assertEquals("com.formdev.flatlaf.FlatLightLaf",
                    CanRegClientApp.resolveLookAndFeelClassName(null));
            assertEquals("com.formdev.flatlaf.FlatLightLaf",
                    CanRegClientApp.resolveLookAndFeelClassName("  "));

            System.setProperty("os.name", "Mac OS X");
            assertEquals("com.formdev.flatlaf.themes.FlatMacLightLaf",
                    CanRegClientApp.resolveLookAndFeelClassName(null));
        } finally {
            if (originalOs != null) {
                System.setProperty("os.name", originalOs);
            }
        }
    }

    @Test
    public void testSetAndResetUIFont() {
        FontUIResource custom = new FontUIResource("SansSerif", Font.PLAIN, 14);
        UITools.setUIFont(custom);
        assertEquals(custom, UIManager.get("defaultFont"));

        UITools.resetUIFont();
        assertNotEquals("resetUIFont should clear custom defaultFont override", custom, UIManager.get("defaultFont"));
    }

    private static CanRegClientView.MenuBarControls createTestMenuControls() {
        return new CanRegClientView.MenuBarControls(
                new JButton(),
                new JButton(),
                new JButton(),
                new JButton(),
                new JButton(),
                new JMenuItem(),
                new JMenu(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenu(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenu(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenu(),
                new JMenuItem(),
                new JMenu(),
                new JMenu(),
                new JMenu(),
                new JMenuItem(),
                new JMenuItem(),
                new JMenu(),
                new JMenu(),
                new JMenuItem(),
                new JMenuItem());
    }

    @Test
    public void testMenuEnablingAcrossUserRightsLevelsOnMacOS() {
        CanRegClientView.MenuBarControls controls = createTestMenuControls();

        // NOT_LOGGED_IN: top-level headers remain enabled on macOS, child items disabled
        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.NOT_LOGGED_IN, false, true),
                controls);
        assertTrue(controls.dataEntryMenu().isEnabled());
        assertFalse(controls.browseEditMenuItem().isEnabled());
        assertFalse(controls.createNewRecordMenuItem().isEnabled());
        assertFalse(controls.editDictionaryMenuItem().isEnabled());
        assertFalse(controls.editPDSMenuItem().isEnabled());
        assertFalse(controls.importDataMenuItem().isEnabled());

        assertTrue(controls.analysisMenu().isEnabled());
        assertFalse(controls.frequenciesMenuItem().isEnabled());
        assertFalse(controls.incidenceTablesMenuItem().isEnabled());
        assertFalse(controls.exportDataReportsMenuItem().isEnabled());

        assertTrue(controls.toolsMenu().isEnabled());
        assertTrue(controls.migrateMenu().isEnabled());
        assertTrue(controls.databaseStructureMenu().isEnabled());
        assertTrue(controls.installRPackagesMenuItem().isEnabled());
        assertTrue(controls.completenessAnalyzerMenuItem().isEnabled());
        assertFalse(controls.populationsMenu().isEnabled());
        assertFalse(controls.logOutMenuItem().isEnabled());

        // ANALYST: only analysis items enabled
        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.ANALYST, true, true),
                controls);
        assertTrue(controls.dataEntryMenu().isEnabled());
        assertFalse(controls.browseEditMenuItem().isEnabled());
        assertFalse(controls.createNewRecordMenuItem().isEnabled());
        assertFalse(controls.editDictionaryMenuItem().isEnabled());
        assertFalse(controls.editPDSMenuItem().isEnabled());
        assertFalse(controls.importDataMenuItem().isEnabled());

        assertTrue(controls.analysisMenu().isEnabled());
        assertTrue(controls.frequenciesMenuItem().isEnabled());
        assertTrue(controls.incidenceTablesMenuItem().isEnabled());
        assertTrue(controls.exportDataReportsMenuItem().isEnabled());
        assertTrue(controls.logOutMenuItem().isEnabled());

        // REGISTRAR: data entry + analysis items enabled, management items stay disabled
        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.REGISTRAR, true, true),
                controls);
        assertTrue(controls.dataEntryMenu().isEnabled());
        assertTrue(controls.browseEditMenuItem().isEnabled());
        assertTrue(controls.createNewRecordMenuItem().isEnabled());
        assertFalse(controls.editDictionaryMenuItem().isEnabled());
        assertTrue(controls.editPDSMenuItem().isEnabled());
        assertFalse(controls.importDataMenuItem().isEnabled());
        assertTrue(controls.frequenciesMenuItem().isEnabled());
        assertTrue(controls.incidenceTablesMenuItem().isEnabled());
        assertTrue(controls.exportDataReportsMenuItem().isEnabled());
        assertFalse(controls.populationsMenu().isEnabled());

        // SUPERVISOR (local server): all data entry, analysis, management, and tools enabled
        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.SUPERVISOR, true, true),
                controls);
        assertTrue(controls.dataEntryMenu().isEnabled());
        assertTrue(controls.browseEditMenuItem().isEnabled());
        assertTrue(controls.createNewRecordMenuItem().isEnabled());
        assertTrue(controls.editDictionaryMenuItem().isEnabled());
        assertTrue(controls.editPDSMenuItem().isEnabled());
        assertTrue(controls.importDataMenuItem().isEnabled());
        assertTrue(controls.frequenciesMenuItem().isEnabled());
        assertTrue(controls.incidenceTablesMenuItem().isEnabled());
        assertTrue(controls.exportDataReportsMenuItem().isEnabled());
        assertTrue(controls.populationsMenu().isEnabled());
        assertTrue(controls.restoreMenuItem().isEnabled());

        // SUPERVISOR (remote server): tools header stays enabled on macOS, tools children disabled
        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.SUPERVISOR, false, true),
                controls);
        assertTrue(controls.toolsMenu().isEnabled());
        assertFalse(controls.migrateMenu().isEnabled());
        assertFalse(controls.databaseStructureMenu().isEnabled());
        assertFalse(controls.installRPackagesMenuItem().isEnabled());
        assertFalse(controls.completenessAnalyzerMenuItem().isEnabled());
        assertFalse(controls.populationsMenu().isEnabled());
        assertFalse(controls.restoreMenuItem().isEnabled());
    }

    @Test
    public void testMenuEnablingPreservesWindowsTopLevelBehavior() {
        CanRegClientView.MenuBarControls controls = createTestMenuControls();

        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.NOT_LOGGED_IN, false, false),
                controls);
        assertFalse(controls.dataEntryMenu().isEnabled());
        assertFalse(controls.analysisMenu().isEnabled());
        assertTrue(controls.managementMenu().isEnabled());
        assertTrue(controls.toolsMenu().isEnabled());

        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.ANALYST, false, false),
                controls);
        assertFalse(controls.dataEntryMenu().isEnabled());
        assertTrue(controls.analysisMenu().isEnabled());
        assertFalse(controls.toolsMenu().isEnabled());

        CanRegClientView.applyUserRightsToControls(
                CanRegClientView.UserRightsMenuState.forLevel(Globals.UserRightLevels.SUPERVISOR, true, false),
                controls);
        assertTrue(controls.dataEntryMenu().isEnabled());
        assertTrue(controls.analysisMenu().isEnabled());
        assertTrue(controls.toolsMenu().isEnabled());
        assertTrue(controls.browseEditMenuItem().isEnabled());
        assertTrue(controls.frequenciesMenuItem().isEnabled());
    }
}
