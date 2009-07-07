package canreg.server.database;

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

    public int getAgeGroupIndex(int age) {
        return ageGroupStructure.whatAgeGroupIsThisAge(age);
    }

    public void addPopulationDataToArrayForTableBuilder(double[][] populationArray, boolean[] foundAgeGroups, AgeGroupStructure targetAgeGroupStructure) {

        // load population data
        for (PopulationDatasetsEntry pdse : ageGroups) {
            int sex = pdse.getSex()-1;
            if (sex>1){
                sex=2;
            }
            int ageGroup = pdse.getAgeGroup();
            if (ageGroupStructure.getSizeOfFirstGroup()!=1){
                ageGroup = ageGroup+1;
            }
            if (foundAgeGroups!= null && ageGroup<foundAgeGroups.length){
                foundAgeGroups[ageGroup] = true;
            }
            int population = pdse.getCount();
            populationArray[sex][ageGroup] += population;
            // For the total
            populationArray[sex][populationArray[0].length-1] += population;

        }
    }
}
