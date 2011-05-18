/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */
package canreg.common;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class DatabaseIndexesListElement implements Serializable, DatabaseElement {

    private String indexName;
    private String databaseTableName;
    private LinkedList<String> variableNamesInIndex;
    private DatabaseVariablesListElement mainVariable = null;
    private DatabaseVariablesListElement[] variableListElementsInIndex;

    public DatabaseIndexesListElement(String indexName) {
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
        if (getMainVariable() != null) {
            return getMainVariable().getDatabaseVariableName() + " (" + databaseTableName + ")";
        } else {
            return " (" + databaseTableName + ")";
        }
    }

    /**
     * @return the tableName
     */
    public String getDatabaseTableName() {
        return databaseTableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setDatabaseTableName(String tableName) {
        this.databaseTableName = tableName;
    }

    /**
     * @return the mainVariable
     */
    public String getMainVariableName() {
        if (getVariableNamesInIndex() != null && getVariableNamesInIndex().size() > 0) {
            return getVariableNamesInIndex().getFirst();
        } else if (getMainVariable() != null) {
            return getMainVariable().getDatabaseVariableName();
        } else {
            return null;
        }
    }

    /**
     * @return the variablesInIndex
     */
    public LinkedList<String> getVariableNamesInIndex() {
        return variableNamesInIndex;
    }

    public void setVariablesInIndex(DatabaseVariablesListElement[] variableListElementsInIndex) {
        variableNamesInIndex = new LinkedList<String>();
        for (DatabaseVariablesListElement dvle : variableListElementsInIndex) {
            if (dvle != null) {
                variableNamesInIndex.add(dvle.getDatabaseVariableName());
            } else {
                throw new NullPointerException("Something wrong with index " + indexName);
            }
        }
        this.variableListElementsInIndex = variableListElementsInIndex;
    }

    public DatabaseVariablesListElement[] getVariableListElementsInIndex() {
        return variableListElementsInIndex;
    }

    public void setMainVariable(DatabaseVariablesListElement mainVariable) {
        this.mainVariable = mainVariable;
    }

    /**
     * @return the mainVariable
     */
    public DatabaseVariablesListElement getMainVariable() {
        return mainVariable;
    }
}
