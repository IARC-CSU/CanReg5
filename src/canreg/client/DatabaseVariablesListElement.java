/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client;

/**
 *
 * @author ervikm
 */
public class DatabaseVariablesListElement {
    // Table in the database for the variable
    private String databaseTableName;
    // ID of the variable in the database
    private int databaseTableVariableID;
    //
    private String databaseVariableName;
    // Variable type
    private String variableType;

    public DatabaseVariablesListElement(
            String databaseTableName,
            int databaseTableVariableID,
            String databaseVariableName,
            String variableType) {
        this.databaseTableName = databaseTableName;
        this.databaseTableVariableID = databaseTableVariableID;
        this.databaseVariableName = databaseVariableName;
        this.variableType = variableType;
    }

    public String getDatabaseTableName() {
        return databaseTableName;
    }

    public void setDatabaseTableName(String databaseTableName) {
        this.databaseTableName = databaseTableName;
    }

    public int getDatabaseTableVariableID() {
        return databaseTableVariableID;
    }

    public void setDatabaseTableVariableID(int databaseTableVariableID) {
        this.databaseTableVariableID = databaseTableVariableID;
    }

    public String getDatabaseVariableName() {
        return databaseVariableName;
    }

    public void setDatabaseVariableName(String databaseVariableName) {
        this.databaseVariableName = databaseVariableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public boolean equals(DatabaseVariablesListElement o) {
        return databaseTableVariableID == o.getDatabaseTableVariableID() && databaseTableName.equalsIgnoreCase(o.getDatabaseTableName());
    }

    @Override
    public String toString() {
        return databaseVariableName;

    }
}
