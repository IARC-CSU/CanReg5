package canreg.common;

/**
 *
 * @author ervikm
 */
public class DatabaseIndexesListElement {

    private String indexName;

    DatabaseIndexesListElement(String indexName) {
        this.indexName = indexName;
    }

    /**
     * 
     * @return
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * 
     * @param indexName
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String toString() {
        return getIndexName();
    }
}
