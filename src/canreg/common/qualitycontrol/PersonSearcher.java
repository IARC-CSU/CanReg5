package canreg.common.qualitycontrol;

import canreg.common.PersonSearchVariable;
import canreg.server.database.Patient;

/**
 *
 * @author ervikm
 */
public interface PersonSearcher {
    /**
     * 
     * @return
     */
    public float getThreshold();
    /**
     * 
     * @param searchVariables
     */
    public void setSearchVariables(PersonSearchVariable[] searchVariables);
    /**
     * 
     * @param d
     */
    public void setThreshold(float d);
    /**
     * 
     * @param variableNames
     * @param variableWeights
     */
    public void setWeights(String[] variableNames, float[] variableWeights);
    /**
     * 
     * @param patient1
     * @param patient2
     * @return
     */
    public float compare(Patient patient1, Patient patient2);
}
