/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.client.dataentry;

/**
 *
 * @author morten
 */
public class Relation {
    // Table in the database for the variable
    private String databaseTableName;
    // ID of the variable in the database
    private int databaseTableVariableID;
    // Column number of the variable in the input file
    private int fileColumnNumber;
    // Variable type
    private String variableType;
    //
    private String databaseVariableName;    
    // 
    private String fileVariableName;

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

    public int getFileColumnNumber() {
        return fileColumnNumber;
    }

    public void setFileColumnNumber(int fileColumnNumber) {
        this.fileColumnNumber = fileColumnNumber;
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

    public String getFileVariableName() {
        return fileVariableName;
    }

    public void setFileVariableName(String fileVariableName) {
        this.fileVariableName = fileVariableName;
    }
}
