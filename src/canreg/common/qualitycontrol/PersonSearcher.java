package canreg.common.qualitycontrol;

import canreg.common.PersonSearchVariable;
import canreg.server.database.Patient;

/**
 *
 * @author ervikm
 */
public interface PersonSearcher {

    public PersonSearchVariable[] getSearchVariables();

    public enum CompareAlgorithms {

        alpha,
        date,
        number,
        code,
        soundex,
        // double_metaphone,
        // caverphone
    };

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
     * @param patient1
     * @param patient2
     * @return
     */
    public float compare(Patient patient1, Patient patient2);
}
