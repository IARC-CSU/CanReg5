package canreg.common;

import canreg.exceptions.SystemUnavailableException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link PasswordService}.
 * All tests are hermetic – no I/O, no DB.
 */
public class PasswordServiceTest {

    // ------------------------------------------------------------------
    // getInstance
    // ------------------------------------------------------------------

    @Test
    public void testGetInstanceReturnsSameObject() {
        PasswordService a = PasswordService.getInstance();
        PasswordService b = PasswordService.getInstance();
        assertSame("getInstance must return the same singleton", a, b);
    }

    // ------------------------------------------------------------------
    // encrypt – SHA-256
    // ------------------------------------------------------------------

    @Test
    public void testEncryptSha256KnownValue() throws SystemUnavailableException {
        // SHA-256("password") is a well-known constant
        String result = PasswordService.getInstance().encrypt("password", "SHA-256");
        assertEquals(
            "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
            result
        );
    }

    @Test
    public void testEncryptSha256EmptyString() throws SystemUnavailableException {
        // SHA-256("") is also well-known
        String result = PasswordService.getInstance().encrypt("", "SHA-256");
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            result
        );
    }

    @Test
    public void testEncryptSha256IsDeterministic() throws SystemUnavailableException {
        String first  = PasswordService.getInstance().encrypt("CanReg5", "SHA-256");
        String second = PasswordService.getInstance().encrypt("CanReg5", "SHA-256");
        assertEquals("Same input must always produce the same hash", first, second);
    }

    @Test
    public void testEncryptSha256DifferentInputsDifferentHashes() throws SystemUnavailableException {
        String h1 = PasswordService.getInstance().encrypt("abc", "SHA-256");
        String h2 = PasswordService.getInstance().encrypt("ABC", "SHA-256");
        assertNotEquals("Hashes of different inputs must differ", h1, h2);
    }

    // ------------------------------------------------------------------
    // encrypt – SHA (SHA-1)
    // ------------------------------------------------------------------

    @Test
    public void testEncryptShaKnownValue() throws SystemUnavailableException {
        // SHA-1("password") = 5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8
        String result = PasswordService.getInstance().encrypt("password", "SHA");
        assertEquals(
            "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
            result
        );
    }

    // ------------------------------------------------------------------
    // hexEncode / hexDecode round-trip
    // ------------------------------------------------------------------

    @Test
    public void testHexEncodeDecodeSingleByte() {
        byte[] original = new byte[]{(byte) 0xAB};
        String encoded = PasswordService.hexEncode(original);
        assertEquals("ab", encoded);
        byte[] decoded = PasswordService.hexDecode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testHexEncodeDecodeMultipleBytes() {
        byte[] original = new byte[]{0x00, 0x0F, (byte) 0xFF, 0x7E};
        String encoded = PasswordService.hexEncode(original);
        assertEquals("000fff7e", encoded);
        byte[] decoded = PasswordService.hexDecode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testHexEncodeEmptyArray() {
        byte[] empty = new byte[0];
        String encoded = PasswordService.hexEncode(empty);
        assertEquals("", encoded);
    }

    @Test
    public void testHexDecodeOddLengthString_TruncatesLastChar() {
        // Implementation uses len/2 so odd-length strings are silently truncated:
        // "abc" → len=3, r=new byte[1], only chars 'a','b' are processed, 'c' is ignored.
        byte[] result = PasswordService.hexDecode("abc");
        assertEquals("Odd-length string should produce 1 byte (truncated)", 1, result.length);
        // 'a'=10, 'b'=11 → byte = (10 << 4) | 11 = 0xAB = -85 as signed byte
        assertEquals((byte) 0xAB, result[0]);
    }

    @Test
    public void testHexDecodeInvalidCharacter_SilentlyUsesAsciiValue() {
        // Invalid hex chars ('z') are not rejected; the if-else chain falls through
        // leaving digit1/digit2 as the char's ASCII value (z=122).
        // No exception is thrown – document the lenient actual behavior.
        byte[] result = PasswordService.hexDecode("zz");
        assertEquals("Should return 1 byte even for invalid hex chars", 1, result.length);
    }

    @Test
    public void testHexEncodeOutputIsLowercase() throws SystemUnavailableException {
        String hash = PasswordService.getInstance().encrypt("test", "SHA-256");
        assertEquals("Hex output must be lowercase", hash, hash.toLowerCase());
    }
}
