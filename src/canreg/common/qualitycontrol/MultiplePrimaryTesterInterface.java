/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common.qualitycontrol;

/**
 *
 * @author ervikm
 */
public interface MultiplePrimaryTesterInterface {
    public static int mptValid = 0;
    public static int mptInvalid = 1;
    public static int mptDuplicate = 2;
    public static int mptMultPrim = 3;
    public static int mptUnkTopog = 4;

    public static String[] mptCodes = { "Valid", "Invalid", "Duplicate", "Multiple Primary", "Unknown Topography" };

    public int multiplePrimaryTest(String topographyOrig, String morphologyOrig,
            String topographySim, String morphologySim);
}
