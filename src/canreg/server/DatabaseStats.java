package canreg.server;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DatabaseStats implements Serializable {

    private int maxNumberOfSourcesPerTumourRecord = 0;

    public void setMaxNumberOfSourcesPerTumourRecord(int maxNumberOfSourcesPerTumourRecord) {
        this.maxNumberOfSourcesPerTumourRecord = maxNumberOfSourcesPerTumourRecord;
    }

    /**
     * @return the maxNumberOfSourcesPerTumourRecord
     */
    public int getMaxNumberOfSourcesPerTumourRecord() {
        return maxNumberOfSourcesPerTumourRecord;
    }
}
