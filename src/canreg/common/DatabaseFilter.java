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
    public String  getSortByVariable() {
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
    public void addDatabaseVariable(DatabaseVariablesListElement databaseVariable){
        if (databaseVariables==null){
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

    /**
     * 
     * @param sortByVariable
     */
    public void setSortByVariable(String sortByVariable) {
        this.sortByVariable = sortByVariable;
    }
}
