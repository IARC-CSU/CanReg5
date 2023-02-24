/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.common.qualitycontrol;

import canreg.common.PersonSearchVariable;
import canreg.common.database.Patient;

public interface PersonSearcher {

    public PersonSearchVariable[] getSearchVariables();

    public enum CompareAlgorithms {

        alpha,
        date,
        number,
        code,
        soundex,
        exact,
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
