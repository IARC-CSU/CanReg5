package canreg.common.qualitycontrol;

import canreg.server.database.Patient;

/**
 *
 * @author ervikm
 */
public interface PersonSearcher {
    public void setWeights(String[] variableNames, float[] variableWeights);
    public float compare(Patient patient1, Patient patient2);
}
