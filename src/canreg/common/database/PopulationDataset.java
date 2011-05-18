/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
package canreg.common.database;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class PopulationDataset extends DatabaseRecord implements Serializable {

    static String ID_KEY = "ID";
    static String ID_PDS_KEY = "PDS_ID";
    static String PDS_NAME_KEY = "PDS_NAME";
    static String FILTER_KEY = "FILTER";
    static String DATE_KEY = "DATE";
    static String SOURCE_KEY = "SOURCE";
    static String AGE_GROUP_STRUCTURE_KEY = "AGE_GROUP_STRUCTURE";
    static String DESCRIPTION_KEY = "DESCRIPTION";
    static String WORLD_POPULATION_ID_KEY = "WORLD_POPULATION_ID";
    static String WORLD_POPULATION_BOOL_KEY = "WORLD_POPULATION_BOOL";
    static AgeGroupStructure[] AGE_GROUP_STRUCTURES = {new AgeGroupStructure(5, 85)};
    private int populationDatasetID = -1;
    private String populationDatasetName = null;
    private String filter = null;
    private String date = "";
    private String source = "";
    private AgeGroupStructure ageGroupStructure = new AgeGroupStructure(5, 85);
    private String description = "";
    private boolean worldPopulationBool = false;
    private int worldPopulationID = 0;
    private LinkedList<PopulationDatasetsEntry> ageGroups;
    private int UNKNOWN_AGE_GROUP_CODE = 99;

    /**
     * Creates a new instance of PopulationDatasetsEntry
     */
    public PopulationDataset() {
        super();
        ageGroups = new LinkedList<PopulationDatasetsEntry>();
    }

    /**
     *
     * @param sex
     * @param count
     */
    public void addUnkownAgeGroup(int sex, int count) {
        ageGroups.add(new PopulationDatasetsEntry(UNKNOWN_AGE_GROUP_CODE, sex, count));
    }

    /**
     * 
     */
    public void flushAgeGroups() {
        ageGroups = new LinkedList<PopulationDatasetsEntry>();
    }

    @Override
    public String toString() {
        return populationDatasetName;
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
    public String getPopulationDatasetName() {
        return populationDatasetName;
    }

    /**
     * 
     * @param populationDatasetName
     */
    public void setPopulationDatasetName(String populationDatasetName) {
        this.populationDatasetName = populationDatasetName;
    }

    /**
     * 
     * @return
     */
    public String getFilter() {
        return filter;
    }

    /**
     * 
     * @param filter
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * 
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     * 
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * 
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 
     * @return
     */
    public AgeGroupStructure getAgeGroupStructure() {
        return ageGroupStructure;
    }

    /**
     * 
     * @param ageGroupStructure
     */
    public void setAgeGroupStructure(AgeGroupStructure ageGroupStructure) {
        this.ageGroupStructure = ageGroupStructure;
    }

    /**
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @return
     */
    public boolean isWorldPopulationBool() {
        return worldPopulationBool;
    }

    /**
     * 
     * @param worldPopulationBool
     */
    public void setWorldPopulationBool(boolean worldPopulationBool) {
        this.worldPopulationBool = worldPopulationBool;
    }

    /**
     * 
     * @return
     */
    public int getWorldPopulationID() {
        return worldPopulationID;
    }

    /**
     * 
     * @param worldPopulationID
     */
    public void setWorldPopulationID(int worldPopulationID) {
        this.worldPopulationID = worldPopulationID;
    }

    /**
     * 
     * @param pdse
     */
    public void addAgeGroup(PopulationDatasetsEntry pdse) {
        ageGroups.add(pdse);
    }

    /**
     * 
     * @return
     */
    public PopulationDatasetsEntry[] getAgeGroups() {
        return ageGroups.toArray(new PopulationDatasetsEntry[0]);
    }

    /**
     *
     * @param age
     * @return
     */
    public int getAgeGroupIndex(int age) {
        return ageGroupStructure.whatAgeGroupIsThisAge(age);
    }

    /**
     *
     * @param populationArray
     * @param foundAgeGroups
     * @param targetAgeGroupStructure
     */
    public void addPopulationDataToArrayForTableBuilder(
            double[][] populationArray, // population array to fill
            boolean[] foundAgeGroups, // age groups to fill
            AgeGroupStructure targetAgeGroupStructure) // target age group structure
    {

        // load population data
        for (PopulationDatasetsEntry pdse : ageGroups) {
            int sex = pdse.getSex() - 1;
            if (sex > 1) {
                sex = 2;
            }
            int ageGroup = pdse.getAgeGroup();
            if (ageGroupStructure.getSizeOfFirstGroup() != 1) {
                ageGroup = ageGroup + 1;
            }
            if (foundAgeGroups != null && ageGroup < foundAgeGroups.length) {
                foundAgeGroups[ageGroup] = true;
            }
            int population = pdse.getCount();
            populationArray[sex][ageGroup] += population;
            // For the total
            populationArray[sex][populationArray[0].length - 1] += population;

        }
    }

    public String getStringRepresentationOfAgeGroupsForFile() {
        return getStringRepresentationOfAgeGroupsForFile("\t");
    }

    public String getStringRepresentationOfAgeGroupsForFile(String separator) {
        StringBuilder popString = new StringBuilder();
        for (PopulationDatasetsEntry pop : getAgeGroups()) {
            if (getDate().length() > 4) {
                popString.append(getDate().substring(0, 4));
            } else {
                popString.append(getDate());
            }
            popString.append(separator);
            popString.append(pop.getStringRepresentationOfAgeGroupsForFile(separator));
            popString.append("\r\n");
        }
        return popString.toString();
    }
}