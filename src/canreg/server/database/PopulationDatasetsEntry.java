package canreg.server.database;

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
        return "PopulationDatasetEntry: "+ageGroup+ " "+sex+" "+count;
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
}
