/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.common;

import canreg.common.qualitycontrol.PersonSearcher.CompareAlgorithms;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class PersonSearchVariable implements Serializable {

    private float weight = 0.0f;
    private float discPower = 1.0f;
    private float reliability = 1.0f;
    private float presence = 1.0f;
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
//<ictl.co>
        } else if (variableType.equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)) {
            this.compareAlgorithm = CompareAlgorithms.alpha;
//</ictl.co>
        } else {
            this.compareAlgorithm = CompareAlgorithms.alpha;
        }
    }

    /**
     * @return the discPower
     */
    public float getDiscPower() {
        return discPower;
    }

    /**
     * @param discPower the discPower to set
     */
    public void setDiscPower(float discPower) {
        this.discPower = discPower;
    }

    /**
     * @return the reliability
     */
    public float getReliability() {
        return reliability;
    }

    /**
     * @param reliability the reliability to set
     */
    public void setReliability(float reliability) {
        this.reliability = reliability;
    }

    /**
     * @return the presence
     */
    public float getPresence() {
        return presence;
    }

    /**
     * @param presence the presence to set
     */
    public void setPresence(float presence) {
        this.presence = presence;
    }
}
