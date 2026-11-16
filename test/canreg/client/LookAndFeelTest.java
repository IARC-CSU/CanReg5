package canreg.client;

import canreg.client.gui.tools.UITools;
import java.awt.Font;
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
}
