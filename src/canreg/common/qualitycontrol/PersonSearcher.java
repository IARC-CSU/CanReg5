package canreg.common.qualitycontrol;

import canreg.common.PersonSearchVariable;
import canreg.server.database.Patient;

/**
 *
 * @author ervikm
 */
public interface PersonSearcher {
    public float getThreshold();
    public void setSearchVariables(PersonSearchVariable[] searchVariables);
    public void setThreshold(float d);
    public void setWeights(String[] variableNames, float[] variableWeights);
    public float compare(Patient patient1, Patient patient2);
}
