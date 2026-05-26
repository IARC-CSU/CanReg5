package canreg.common;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Soundex.
 * Covers standard Knuth examples, boundary/null cases, ignored chars, and double letter collapse.
 */
public class SoundexTest {

    public SoundexTest() {
    }

    /**
     * Test of soundex method, of class Soundex, using Donald Knuth's standard examples.
     */
    @Test
    public void testSoundexKnuthExamples() {
        System.out.println("testSoundexKnuthExamples");
        
        // Euler, Ellery -> E460
        assertEquals("E460", Soundex.soundex("Euler"));
        assertEquals("E460", Soundex.soundex("Ellery"));
        
        // Gauss, Ghosh -> G200
        assertEquals("G200", Soundex.soundex("Gauss"));
        assertEquals("G200", Soundex.soundex("Ghosh"));
        
        // Hilbert, Heilbronn -> H416
        assertEquals("H416", Soundex.soundex("Hilbert"));
        assertEquals("H416", Soundex.soundex("Heilbronn"));
        
        // Knuth, Kant -> K530
        assertEquals("K530", Soundex.soundex("Knuth"));
        assertEquals("K530", Soundex.soundex("Kant"));
        
        // Lloyd, Ladd -> L300
        assertEquals("L300", Soundex.soundex("Lloyd"));
        assertEquals("L300", Soundex.soundex("Ladd"));
        
        // Lukasiewicz, Lissajous -> L222
        assertEquals("L222", Soundex.soundex("Lukasiewicz"));
        assertEquals("L222", Soundex.soundex("Lissajous"));
    }

    /**
     * Test of soundex method with empty, invalid, and null-equivalent non-alphabetic inputs.
     */
    @Test
    public void testSoundexEmptyAndInvalid() {
        System.out.println("testSoundexEmptyAndInvalid");
        
        // Empty string
        assertNull(Soundex.soundex(""));
        
        // Only non-alphabetic characters
        assertNull(Soundex.soundex("12345"));
        assertNull(Soundex.soundex("!@#$"));
        
        // String starting with a comma (ignored)
        assertNull(Soundex.soundex(",abc"));
    }

    /**
     * Test of soundex method with double letters that should collapse.
     */
    @Test
    public void testSoundexDoubleLetters() {
        System.out.println("testSoundexDoubleLetters");
        
        // 'bb' collapses to one '1'
        assertEquals("A100", Soundex.soundex("Abba"));
        
        // 'ss' collapses to '2'
        assertEquals("G200", Soundex.soundex("Gauss"));
        
        // 'll' collapses
        assertEquals("L300", Soundex.soundex("Ladd"));
    }

    /**
     * Test of soundex method with ignored characters or punctuation (stops at comma).
     */
    @Test
    public void testSoundexIgnoredAndSpecialCharacters() {
        System.out.println("testSoundexIgnoredAndSpecialCharacters");
        
        // Stops at comma
        assertEquals("D650", Soundex.soundex("Darwin, Ian"));
        assertEquals("D650", Soundex.soundex("Darwin"));
        
        // Spaces and hyphens are skipped as they are not A-Z
        assertEquals("M350", Soundex.soundex("M-T-N"));
        assertEquals("M350", Soundex.soundex("M T N"));
    }
}
