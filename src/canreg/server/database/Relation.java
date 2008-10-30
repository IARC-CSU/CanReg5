/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server.database;

/**
 *
 * @author ervikm
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
    
    private String databaseVariableName;

    /**
     * 
     * @return
     */
    public String getDatabaseTableName() {
        return databaseTableName;
    }

    /**
     * 
     * @param databaseTableName
     */
    public void setDatabaseTableName(String databaseTableName) {
        this.databaseTableName = databaseTableName;
    }

    /**
     * 
     * @return
     */
    public int getDatabaseTableVariableID() {
        return databaseTableVariableID;
    }

    /**
     * 
     * @param databaseTableVariableID
     */
    public void setDatabaseTableVariableID(int databaseTableVariableID) {
        this.databaseTableVariableID = databaseTableVariableID;
    }

    /**
     * 
     * @return
     */
    public int getFileColumnNumber() {
        return fileColumnNumber;
    }

    /**
     * 
     * @param fileColumnNumber
     */
    public void setFileColumnNumber(int fileColumnNumber) {
        this.fileColumnNumber = fileColumnNumber;
    }

    /**
     * 
     * @return
     */
    public String getDatabaseVariableName() {
        return databaseVariableName;
    }

    /**
     * 
     * @param databaseVariableName
     */
    public void setDatabaseVariableName(String databaseVariableName) {
        this.databaseVariableName = databaseVariableName;
    }

    /**
     * 
     * @return
     */
    public String getVariableType() {
        return variableType;
    }

    /**
     * 
     * @param variableType
     */
    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }
}
