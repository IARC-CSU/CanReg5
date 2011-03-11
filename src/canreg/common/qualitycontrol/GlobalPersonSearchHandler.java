package canreg.common.qualitycontrol;

import other.cachingtableapi.DistributedTableDescription;

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
