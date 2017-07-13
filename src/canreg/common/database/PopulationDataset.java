/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017  International Agency for Research on Cancer
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Predicate;

public class PopulationDataset extends DatabaseRecord implements Serializable {

    static String ID_KEY = "ID";
    static String ID_PDS_KEY = "PDS_ID";
    static String PDS_NAME_KEY = "PDS_NAME";
    static String FILTER_KEY = "FILTER";
    static String DATE_KEY = "DATE";
    static String SOURCE_KEY = "SOURCE";
    static String AGE_GROUP_STRUCTURE_KEY = "AGE_GROUP_STRUCTURE";
    static String DESCRIPTION_KEY = "DESCRIPTION";
    static String REFERENCE_POPULATION_ID_KEY = "WORLD_POPULATION_ID";
    static String REFERENCE_POPULATION_BOOL_KEY = "WORLD_POPULATION_BOOL";
    static AgeGroupStructure[] AGE_GROUP_STRUCTURES = {new AgeGroupStructure(5, 85)};
    private int populationDatasetID = -1;
    private String populationDatasetName = null;
    private String filter = null;
    private String date = "";
    private String source = "";
    private AgeGroupStructure ageGroupStructure;
    private String description = "";
    private boolean referencePopulationBool = false;
    private int referencePopulationID = 0;
    private LinkedList<PopulationDatasetsEntry> ageGroups = new LinkedList<>();
    private final int UNKNOWN_AGE_GROUP_CODE = 99;
    private PopulationDataset referencePopulation;
    private Map<String, Integer> ageGroupMap = new HashMap<>();

    /**
     * Creates a new instance of PopulationDatasetsEntry
     */
    public PopulationDataset() {
        super();
        setAgeGroupStructure(new AgeGroupStructure(5, 85));
    }

    /**
     *
     * @param sex
     * @param count
     */
    public void addUnkownAgeGroup(int sex, int count) {
        ageGroupMap.put(sex + "," + UNKNOWN_AGE_GROUP_CODE, count);
        ageGroups.add(new PopulationDatasetsEntry(UNKNOWN_AGE_GROUP_CODE, sex, count));
    }

    /**
     *
     */
    public void flushAgeGroups() {
        ageGroupMap = new HashMap<>();
        ageGroups = new LinkedList<>();
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
    public final void setAgeGroupStructure(AgeGroupStructure ageGroupStructure) {
        flushAgeGroups();
        this.ageGroupStructure = ageGroupStructure;
        fillWithEmptyPopulationDatasetEntries();
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
    public boolean isReferencePopulationBool() {
        return referencePopulationBool;
    }

    /**
     *
     * @param worldPopulationBool
     */
    public void setReferencePopulationBool(boolean worldPopulationBool) {
        this.referencePopulationBool = worldPopulationBool;
    }

    /**
     *
     * @return
     */
    public int getReferencePopulationID() {
        return referencePopulationID;
    }

    /**
     *
     * @param referencePopulationID
     */
    public void setReferencePopulationID(int referencePopulationID) {
        this.referencePopulationID = referencePopulationID;
    }

    /**
     *
     * @param pdse
     */
    public void addAgeGroup(PopulationDatasetsEntry pdse) {
        ageGroupMap.put(pdse.getSex() + "," + pdse.getAgeGroup(), pdse.getCount());
        Predicate<PopulationDatasetsEntry> allreadyThere = (p) -> (p.getSex() == pdse.getSex()
                && p.getAgeGroup() == pdse.getAgeGroup());
        ageGroups.removeIf(allreadyThere);
        ageGroups.add(pdse);
    }

    private void fillWithEmptyPopulationDatasetEntries() {
        int groups = ageGroupStructure.getNumberOfAgeGroups();
        for (int group = 0; group < groups; group++) {
            for (int sex = 1; sex <= 2; sex++) {
                addAgeGroup(new PopulationDatasetsEntry(group, sex, 0));
            }
        }
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
    @JsonIgnore
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
        ageGroups.stream().forEach((pdse) -> {
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
        });
    }

    @JsonIgnore
    public String getStringRepresentationOfAgeGroupsForFile() {
        return getStringRepresentationOfAgeGroupsForFile("\t");
    }

    @JsonIgnore
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

    @JsonIgnore
    public PopulationDataset getReferencePopulation() {
        return referencePopulation;
    }

    /**
     * @param worldPopulation the referencePopulation to set
     */
    public void setReferencePopulation(PopulationDataset worldPopulation) {
        if (worldPopulation != null) {
            this.referencePopulation = worldPopulation;
            this.referencePopulationID = worldPopulation.getPopulationDatasetID();
        }
    }

    public int getReferencePopulationForAgeGroupIndex(int sex, int index) throws IncompatiblePopulationDataSetException {
        int count = Integer.MIN_VALUE;
        if (referencePopulation != null) {
            AgeGroupStructure wags = referencePopulation.getAgeGroupStructure();
            // if this has the very same age group structure as the worldpop - return this

            if (wags.equals(ageGroupStructure)) {
                count = referencePopulation.getAgeGroupCount(sex, index);
            } else if (wags.getSizeOfGroups() == ageGroupStructure.getSizeOfGroups()) {
                // we have the same general age group size
                if (wags.getSizeOfFirstGroup() == ageGroupStructure.getSizeOfFirstGroup()) {
                    count = getreferencePopulationForAgeGroupIndex(sex, index, 0);
                } // if the size of the groups are the same, 
                // but the first group size is different
                // and smaller than the group size
                else if (ageGroupStructure.getSizeOfFirstGroup() < ageGroupStructure.getSizeOfGroups()) {
                    int offset = -1;
                    switch (index) {
                        case 0:
                            count = referencePopulation.getAgeGroupCount(sex, index)
                                    / ageGroupStructure.getSizeOfGroups();
                            break;
                        case 1:
                            int firstGroupCount = referencePopulation.getAgeGroupCount(sex, 0)
                                    / ageGroupStructure.getSizeOfGroups();
                            count = getreferencePopulationForAgeGroupIndex(sex, index, offset)
                                    - firstGroupCount;
                            break;
                        default:
                            count = getreferencePopulationForAgeGroupIndex(sex, index, offset);
                            break;
                    }
                } // if the size of the groups are the same, 
                // but the first group size is different
                // and bigger than the group size 
                // and its size is a product of the group size
                else if (ageGroupStructure.getSizeOfFirstGroup() > ageGroupStructure.getSizeOfGroups()
                        && ageGroupStructure.getSizeOfFirstGroup() % ageGroupStructure.getSizeOfGroups() == 0) {
                    int offset = -((ageGroupStructure.getSizeOfFirstGroup() / ageGroupStructure.getSizeOfGroups()));
                    if (index == 0) {
                        // group all the ages that 
                        for (int tempIndex = 0; tempIndex < offset; tempIndex++) {
                            count += getReferencePopulationForAgeGroupIndex(sex, index);
                        }
                    } else {
                        count = getreferencePopulationForAgeGroupIndex(sex, index, offset);
                    }
                }
            }
            // Still hasn't assigned a proper value? Throw an exception...
            if (count == Integer.MIN_VALUE) {
                throw new IncompatiblePopulationDataSetException();
            }
        }

        return count;
    }

    private int getreferencePopulationForAgeGroupIndex(int sex, int index, int offset) {
        AgeGroupStructure wags = referencePopulation.getAgeGroupStructure();
        int count = 0;
        index = index + offset;
        if (index < wags.getNumberOfAgeGroups() - 1
                && index < ageGroupStructure.getNumberOfAgeGroups() - 1) {
            count = referencePopulation.getAgeGroupCount(sex, index);
        } else // last group - no cutoff, we sum it all up
         if (index <= wags.getNumberOfAgeGroups() - 1
                    && ageGroupStructure.getCutOfAge() == Integer.MAX_VALUE) {
                for (int tempIndex = index; tempIndex < wags.getNumberOfAgeGroups(); tempIndex++) {
                    count += referencePopulation.getAgeGroupCount(sex, tempIndex);
                }
                // last group - with cutoff, we take that group
            } else if (index <= wags.getNumberOfAgeGroups() - 1
                    && ageGroupStructure.getCutOfAge() != Integer.MAX_VALUE) {
                count = referencePopulation.getAgeGroupCount(sex, index);
            }
        return count;
    }

    public int getAgeGroupCount(int sex, int index) {
        int count = ageGroupMap.get(sex + "," + index);
        return count;
    }

    @Override
    @JsonIgnore
    public String[] getVariableNames() {
        return new String[]{}; // nothing for now
    }
}
