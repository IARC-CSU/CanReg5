package canreg.common;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionTest {

    @Test
    public void testVersionEquality() {
        Version v1 = new Version("5.00.46");
        Version v2 = new Version("5.00.46");
        assertEquals(0, v1.compareTo(v2));
        assertEquals(v1, v2);

        Version vLeading = new Version("v5.00.46");
        assertEquals(0, v1.compareTo(vLeading));
    }

    @Test
    public void testMajorMinorPatchComparison() {
        Version vOld = new Version("5.00.45");
        Version vNew = new Version("5.00.46");
        assertTrue(vNew.compareTo(vOld) > 0);
        assertTrue(vOld.compareTo(vNew) < 0);

        Version v4 = new Version("4.00.00");
        Version v5 = new Version("5.00.00");
        assertTrue(v5.compareTo(v4) > 0);
    }

    @Test
    public void testBetaVersions() {
        Version beta1 = new Version("5.00.46BETA1");
        Version beta3 = new Version("5.00.46BETA3");
        Version finalRel = new Version("5.00.46");

        assertTrue(beta3.compareTo(beta1) > 0);
        assertTrue(beta1.compareTo(beta3) < 0);

        // Final release is newer than beta
        assertTrue(finalRel.compareTo(beta3) > 0);
        assertTrue(beta3.compareTo(finalRel) < 0);

        // Beta of newer version is newer than older final release
        Version oldFinal = new Version("5.00.45");
        assertTrue(beta1.compareTo(oldFinal) > 0);
    }

    @Test
    public void testLetterPatchVersions() {
        Version vH = new Version("5.00.44h");
        Version vM = new Version("5.00.44m");
        Version vBase = new Version("5.00.44");

        assertTrue(vM.compareTo(vH) > 0);
        assertTrue(vH.compareTo(vBase) > 0);
    }

    @Test
    public void testReleaseCandidateVersions() {
        Version beta = new Version("5.00.46BETA2");
        Version rc = new Version("5.00.46RC1");
        Version finalRel = new Version("5.00.46");

        assertTrue(rc.compareTo(beta) > 0);
        assertTrue(finalRel.compareTo(rc) > 0);
    }

    @Test
    public void testEmptyAndNullSafety() {
        Version vEmpty = new Version("");
        Version vNull = new Version(null);
        assertEquals(0, vEmpty.compareTo(vNull));

        Version vValid = new Version("5.00.46");
        assertTrue(vValid.compareTo(vEmpty) > 0);
        assertTrue(vValid.compareTo(null) > 0);
    }
}

