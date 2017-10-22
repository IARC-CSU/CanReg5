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
package canreg.common.database;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class PopulationDatasetsEntry extends DatabaseRecord implements Serializable {

    static String ID_KEY = "ID";
    static String ID_PDS_KEY = "PDS_ID";
    static String AGE_GROUP_KEY = "AGE_GROUP";
    static String COUNT_KEY = "COUNT";
    static String SEX_KEY = "SEX";
    static int SEX_CODE_MALE = 1;
    static int SEX_CODE_FEMALE = 2;
    private int ageGroup;
    private int count;
    private int populationDatasetID;
    private int sex;

    /**
     * Creates a new instance of PopulationDatasetsEntry
     * @param ageGroup 
     * @param sex
     * @param count 
     */
    public PopulationDatasetsEntry(int ageGroup, int sex, int count) {
        super();
        this.ageGroup = ageGroup;
        this.sex = sex;
        this.count = count;
    }

    @Override
    public String toString() {
        return "PopulationDatasetEntry: " + ageGroup + " " + sex + " " + count;
    }

    /**
     * 
     * @return
     */
    public int getAgeGroup() {
        return ageGroup;
    }

    /**
     * 
     * @param ageGroup
     */
    public void setAgeGroup(int ageGroup) {
        this.ageGroup = ageGroup;
    }

    /**
     * 
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * 
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 
     * @return
     */
    public int getPopulationDatasetID() {
        return populationDatasetID;
    }

    /**
     * 
     * @param populationDatasetID
     */
    public void setPopulationDatasetID(int populationDatasetID) {
        this.populationDatasetID = populationDatasetID;
    }

    /**
     * 
     * @return
     */
    public int getSex() {
        return sex;
    }

    /**
     * 
     * @param sex
     */
    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getStringRepresentationOfAgeGroupsForFile() {
        return getStringRepresentationOfAgeGroupsForFile("\t");
    }

    public String getStringRepresentationOfAgeGroupsForFile(String separator) {
        return ageGroup + separator + sex + separator + count;
    }
}
