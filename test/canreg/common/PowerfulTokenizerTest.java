package canreg.common;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link PowerfulTokenizer}.
 * All tests are hermetic – no I/O, no DB.
 *
 * <h3>Known pre-existing bugs</h3>
 * <ul>
 *   <li><b>Empty string:</b> {@link PowerfulTokenizer#countTokens()} throws
 *       {@code ArrayIndexOutOfBoundsException} because it pre-allocates
 *       {@code int[] aiIndex = new int[iTokens]} and then immediately accesses
 *       {@code aiIndex[0]} without guarding against zero length.</li>
 *   <li><b>Leading delimiter:</b> {@code countTokens()} over-counts; the iterator
 *       calls produce fewer tokens than announced, causing {@code nextToken()} to
 *       eventually throw {@code NoSuchElementException}.</li>
 * </ul>
 * Tests that expose these bugs are named with a {@code _DueToBug} suffix.
 */
public class PowerfulTokenizerTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Drain all tokens from a tokenizer into a list. */
    private List<String> drain(PowerfulTokenizer pt) {
        List<String> tokens = new ArrayList<String>();
        while (pt.hasMoreTokens()) {
            tokens.add(pt.nextToken());
        }
        return tokens;
    }

    // ------------------------------------------------------------------
    // Basic CSV (no quoted fields)
    // ------------------------------------------------------------------

    @Test
    public void testSimpleCsvThreeFields() {
        PowerfulTokenizer pt = new PowerfulTokenizer("a,b,c", ",");
        List<String> tokens = drain(pt);
        assertEquals(3, tokens.size());
        assertEquals("a", tokens.get(0));
        assertEquals("b", tokens.get(1));
        assertEquals("c", tokens.get(2));
    }

    @Test
    public void testSingleToken_NoDelimiter() {
        PowerfulTokenizer pt = new PowerfulTokenizer("hello", ",");
        List<String> tokens = drain(pt);
        assertEquals(1, tokens.size());
        assertEquals("hello", tokens.get(0));
    }

    // ------------------------------------------------------------------
    // Empty string – BUG: countTokens() throws ArrayIndexOutOfBoundsException
    // ------------------------------------------------------------------

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testEmptyString_CountTokens_ThrowsAIOOBE_DueToBug() {
        // KNOWN BUG: aiIndex = new int[0]; aiIndex[0] = 0; → AIOOBE
        PowerfulTokenizer pt = new PowerfulTokenizer("", ",");
        pt.countTokens();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testEmptyString_HasMoreTokens_ThrowsAIOOBE_DueToBug() {
        // hasMoreTokens() calls countTokens() → same AIOOBE
        PowerfulTokenizer pt = new PowerfulTokenizer("", ",");
        pt.hasMoreTokens();
    }

    // ------------------------------------------------------------------
    // Consecutive delimiters → empty tokens
    // ------------------------------------------------------------------

    @Test
    public void testConsecutiveDelimiters_EmptyTokenBetween() {
        // "a,,b" → ["a", "", "b"]
        PowerfulTokenizer pt = new PowerfulTokenizer("a,,b", ",");
        List<String> tokens = drain(pt);
        assertEquals(3, tokens.size());
        assertEquals("a", tokens.get(0));
        assertEquals("",  tokens.get(1));
        assertEquals("b", tokens.get(2));
    }

    // ------------------------------------------------------------------
    // Leading delimiter – BUG: countTokens() over-counts
    // ------------------------------------------------------------------

    @Test(expected = java.util.NoSuchElementException.class)
    public void testLeadingDelimiter_CountTokensMismatch_DueToBug() {
        // ",a,b" – countTokens() reports 3 but nextToken() only delivers 2
        // before throwing NoSuchElementException.
        // KNOWN BUG: countTokens() over-counts when there is a leading delimiter.
        PowerfulTokenizer pt = new PowerfulTokenizer(",a,b", ",");
        // This will exhaust the underlying StringTokenizer before iTokenNo reaches iTotalTokens
        drain(pt);
    }

    // ------------------------------------------------------------------
    // Quoted fields containing the delimiter
    // ------------------------------------------------------------------

    @Test
    public void testQuotedFieldWithDelimiter() {
        // "hello,\"world,earth\",bye" → ["hello", "world,earth", "bye"]
        PowerfulTokenizer pt = new PowerfulTokenizer("hello,\"world,earth\",bye", ",");
        List<String> tokens = drain(pt);
        assertEquals(3, tokens.size());
        assertEquals("hello",       tokens.get(0));
        assertEquals("world,earth", tokens.get(1));
        assertEquals("bye",         tokens.get(2));
    }

    @Test
    public void testQuotedFieldCountMatchesExpected() {
        // CSV: first,"second,has,commas",third
        String csv = "first,\"second,has,commas\",third";
        PowerfulTokenizer pt = new PowerfulTokenizer(csv, ",");
        List<String> tokens = drain(pt);
        assertEquals(3, tokens.size());
    }

    // ------------------------------------------------------------------
    // countTokens
    // ------------------------------------------------------------------

    @Test
    public void testCountTokensSimple() {
        PowerfulTokenizer pt = new PowerfulTokenizer("a,b,c,d", ",");
        assertEquals(4, pt.countTokens());
    }

    @Test
    public void testCountTokensWithConsecutiveDelimiters() {
        // "a,,b" → 3 tokens
        PowerfulTokenizer pt = new PowerfulTokenizer("a,,b", ",");
        assertEquals(3, pt.countTokens());
    }
}
