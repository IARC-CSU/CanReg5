package canreg.common;

import canreg.common.qualitycontrol.PersonSearcher.CompareAlgorithms;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class PersonSearchVariable implements Serializable {

    private float weight;
    private DatabaseVariablesListElement databaseVariablesListElement;
    private CompareAlgorithms compareAlgorithm;

    /**
     * 
     * @return
     */
    public String getName() {
        return databaseVariablesListElement.getDatabaseVariableName();
    }

    /**
     * 
     * @return
     */
    public float getWeight() {
        return weight;
    }

    /**
     * 
     * @param weight
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setVariable(DatabaseVariablesListElement databaseVariablesListElement) {
        this.databaseVariablesListElement = databaseVariablesListElement;
        setDefaultAlgorithmName();
    }

    /**
     * @return the algorithmName
     */
    public CompareAlgorithms getCompareAlgorithm() {
        return compareAlgorithm;
    }

    /**
     * @param compareAlgorithm the algorithmName to set
     */
    public void setAlgorithm(CompareAlgorithms compareAlgorithm) {
        if (compareAlgorithm != null) {
            this.compareAlgorithm = compareAlgorithm;
        }
    }

    private void setDefaultAlgorithmName() {
        String variableType = databaseVariablesListElement.getVariableType();
        if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME)) {
            this.compareAlgorithm = CompareAlgorithms.code;
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME)) {
            this.compareAlgorithm = CompareAlgorithms.alpha;
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
            this.compareAlgorithm = CompareAlgorithms.date;
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
            this.compareAlgorithm = CompareAlgorithms.number;
        } else {
            this.compareAlgorithm = CompareAlgorithms.alpha;
        }
    }
}
