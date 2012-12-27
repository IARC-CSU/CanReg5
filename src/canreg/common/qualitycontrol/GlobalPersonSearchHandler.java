/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

package canreg.common.qualitycontrol;

import canreg.common.cachingtableapi.DistributedTableDescription;

/**
 *
 * @author ervikm
 */
public class GlobalPersonSearchHandler {
    private PersonSearcher personSearcher;
    private DistributedTableDescription distributedTableDescription;
    private int position;
    private Object[][] patientRecordIDsWithinRange;
    private Object[][] allPatientRecordIDs;

    /**
     * @return the personSearcher
     */
    public PersonSearcher getPersonSearcher() {
        return personSearcher;
    }

    /**
     * @param personSearcher the personSearcher to set
     */
    public void setPersonSearcher(PersonSearcher personSearcher) {
        this.personSearcher = personSearcher;
    }

    /**
     * @return the distributedTableDescription
     */
    public DistributedTableDescription getDistributedTableDescription() {
        return distributedTableDescription;
    }

    /**
     * @param distributedTableDescription the distributedTableDescription to set
     */
    public void setDistributedTableDescription(DistributedTableDescription distributedTableDescription) {
        this.distributedTableDescription = distributedTableDescription;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    public void setPatientRecordIDsWithinRange(Object[][] rowdata) {
        this.patientRecordIDsWithinRange = rowdata;
    }

    public Object[][] getPatientRecordIDsWithinRange() {
        return patientRecordIDsWithinRange;
    }

        public void setAllPatientRecordIDs(Object[][] rowdata) {
        this.allPatientRecordIDs = rowdata;
    }

    public Object[][] getAllPatientRecordIDs() {
        return allPatientRecordIDs;
    }
}
