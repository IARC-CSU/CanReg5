/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String toString() {
        return getIndexName();
    }
}
