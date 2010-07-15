package canreg.common;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class DatabaseIndexesListElement  implements Serializable, DatabaseElement {

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
        return getMainVariableName()+" ("+databaseTableName+")";
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
        if (getVariableNamesInIndex() != null) {
            return getVariableNamesInIndex().getFirst();
        } else {
            return getMainVariable().getDatabaseVariableName();
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
        for (DatabaseVariablesListElement dvle:variableListElementsInIndex){
            if (dvle!=null){
                variableNamesInIndex.add(dvle.getDatabaseVariableName());
            } else {
                throw new NullPointerException("Something wrong with index "+indexName);
            }
        }
        this.variableListElementsInIndex = variableListElementsInIndex;
    }

    public DatabaseVariablesListElement[] getVariableListElementsInIndex(){
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
