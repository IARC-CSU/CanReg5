package canreg.common;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author ervikm
 */
public class DatabaseFilter implements Serializable {
    

    static public enum QueryType {
        BROWSER,
        FREQUENCIES_BY_YEAR,
        PERSON_SEARCH
    }
    private String filterString;
    private Set<DatabaseVariablesListElement> databaseVariables;
    private QueryType queryType;
    private String sortByVariable;
    
    public Set<DatabaseVariablesListElement> getDatabaseVariables() {
        return databaseVariables;
    }

    public String  getSortByVariable() {
        return sortByVariable;
    }

    public void setDatabaseVariables(Set<DatabaseVariablesListElement> databaseVariables) {
        this.databaseVariables = databaseVariables;
    }
    
    public void addDatabaseVariable(DatabaseVariablesListElement databaseVariable){
        if (databaseVariables==null){
            databaseVariables = new LinkedHashSet<DatabaseVariablesListElement>();
        }
        databaseVariables.add(databaseVariable);
    }
    
    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public void setSortByVariable(String sortByVariable) {
        this.sortByVariable = sortByVariable;
    }
}
