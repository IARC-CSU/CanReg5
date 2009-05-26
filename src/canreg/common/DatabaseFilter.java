package canreg.common;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author ervikm
 */
public class DatabaseFilter implements Serializable {

    /**
     * 
     */
    static public enum QueryType {
        /**
         * 
         */
        BROWSER,
        /**
         * 
         */
        FREQUENCIES_BY_YEAR,
        /**
         * 
         */
        PERSON_SEARCH
    }
    private String filterString;
    private Set<DatabaseVariablesListElement> databaseVariables;
    private QueryType queryType;
    private String sortByVariable;
    private DatabaseIndexesListElement rangeDatabaseIndexedListElement;
    private String rangeStart;
    private String rangeEnd;

    /**
     * 
     * @return
     */
    public Set<DatabaseVariablesListElement> getDatabaseVariables() {
        return databaseVariables;
    }

    /**
     * 
     * @return
     */
    public String getSortByVariable() {
        return sortByVariable;
    }

    /**
     * 
     * @param databaseVariables
     */
    public void setDatabaseVariables(Set<DatabaseVariablesListElement> databaseVariables) {
        this.databaseVariables = databaseVariables;
    }

    /**
     * 
     * @param databaseVariable
     */
    public void addDatabaseVariable(DatabaseVariablesListElement databaseVariable) {
        if (databaseVariables == null) {
            databaseVariables = new LinkedHashSet<DatabaseVariablesListElement>();
        }
        databaseVariables.add(databaseVariable);
    }

    /**
     * 
     * @return
     */
    public String getFilterString() {
        return filterString;
    }

    /**
     * 
     * @param filterString
     */
    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    /**
     * 
     * @return
     */
    public QueryType getQueryType() {
        return queryType;
    }

    /**
     * 
     * @param queryType
     */
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public void setRange(Object[] range) {
        if (range != null && range.length==3) {
            setRangeDatabaseIndexedListElement((DatabaseIndexesListElement) range[0]);
            setRangeStart((String) range[1]);
            setRangeEnd((String) range[2]);
        } else {
            // TODO: Add an exception
        }
    }

    /**
     * @return the rangeDatabaseIndexedListElement
     */
    public DatabaseIndexesListElement getRangeDatabaseIndexedListElement() {
        return rangeDatabaseIndexedListElement;
    }

    /**
     * @param rangeDatabaseIndexedListElement the rangeDatabaseIndexedListElement to set
     */
    public void setRangeDatabaseIndexedListElement(DatabaseIndexesListElement rangeDatabaseIndexedListElement) {
        this.rangeDatabaseIndexedListElement = rangeDatabaseIndexedListElement;
    }

    /**
     * @return the rangeStart
     */
    public String getRangeStart() {
        return rangeStart;
    }

    /**
     * @param rangeStart the rangeStart to set
     */
    public void setRangeStart(String rangeStart) {
        this.rangeStart = rangeStart;
    }

    /**
     * @return the rangeEnd
     */
    public String getRangeEnd() {
        return rangeEnd;
    }

    /**
     * @param rangeEnd the rangeEnd to set
     */
    public void setRangeEnd(String rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    /**
     * 
     * @param sortByVariable
     */
    public void setSortByVariable(String sortByVariable) {
        this.sortByVariable = sortByVariable;
    }
}
