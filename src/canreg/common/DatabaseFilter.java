/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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


package canreg.common;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author ervikm
 */
public class DatabaseFilter implements Serializable {
    private DatabaseVariablesListElement rangeDatabaseVariablesListElement;

    public void setRangeDatabaseVariablesListElement(DatabaseVariablesListElement rangeDatabaseVariablesListElement) {
        this.rangeDatabaseVariablesListElement = rangeDatabaseVariablesListElement;
    }

    /**
     * @return the rangeDatabaseVariablesListElement
     */
    public DatabaseVariablesListElement getRangeDatabaseVariablesListElement() {
        return rangeDatabaseVariablesListElement;
    }

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
        PERSON_SEARCH,
        /**
         *
         */
        NAMES_SEARCH
    }
    private String filterString = "";
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
            setRangeDatabaseVariablesListElement((DatabaseVariablesListElement) range[0]);
            setRangeStart((String) range[1]);
            setRangeEnd((String) range[2]);
        } else {
            // TODO: Add an exception
        }
    }

    /**
     * @return the rangeDatabaseIndexedListElement
     */
    private DatabaseIndexesListElement getRangeDatabaseIndexedListElement() {
        return rangeDatabaseIndexedListElement;
    }

    /**
     * @param rangeDatabaseIndexedListElement the rangeDatabaseIndexedListElement to set
     */
    public void setRangeDatabaseIndexedListElement(DatabaseIndexesListElement rangeDatabaseIndexedListElement) {
        this.rangeDatabaseVariablesListElement = rangeDatabaseIndexedListElement.getMainVariable();
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
