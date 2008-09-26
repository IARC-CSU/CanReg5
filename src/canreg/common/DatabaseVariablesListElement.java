package canreg.common;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DatabaseVariablesListElement implements Serializable {
    // Table in the database for the variable
    private String table;
    // ID of the variable in the database
    private int variableID;
    //
    private String shortName;
    // Variable type
    private String variableType;
    // Dictionary
    private String useDictionary;
    // Dictionary ID
    private int dictionaryID = -1;

    private String fullName;
    private String englishName;
    // private String groupName;
    private String standardVariableName;
    
    private int xPos;
    private int yPos;
    
    private int variableLength;
    
    private String fillInStatus;
    
    private int groupID;
    
    public DatabaseVariablesListElement(
            String databaseTableName,
            int databaseTableVariableID,
            String databaseVariableName,
            String variableType) {
        this.table = databaseTableName;
        this.variableID = databaseTableVariableID;
        this.shortName = databaseVariableName;
        this.variableType = variableType;
    }

    public String getDatabaseTableName() {
        return getTable();
    }

    public void setDatabaseTableName(String databaseTableName) {
        this.setTable(databaseTableName);
    }

    public int getDatabaseTableVariableID() {
        return getVariableID();
    }

    public void setDatabaseTableVariableID(int databaseTableVariableID) {
        this.setVariableID(databaseTableVariableID);
    }

    public String getDatabaseVariableName() {
        return getShortName();
    }

    public void setDatabaseVariableName(String databaseVariableName) {
        this.setShortName(databaseVariableName);
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public boolean equals(DatabaseVariablesListElement o) {
        return getVariableID() == o.getDatabaseTableVariableID() && getTable().equalsIgnoreCase(o.getDatabaseTableName());
    }

    @Override
    public String toString() {
        return getShortName();

    }

    public int getDictionaryID() {
        return dictionaryID;
    }

    public void setDictionaryID(int dictionaryID) {
        this.dictionaryID = dictionaryID;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getVariableID() {
        return variableID;
    }

    public void setVariableID(int variableID) {
        this.variableID = variableID;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getUseDictionary() {
        return useDictionary;
    }

    public void setUseDictionary(String useDictionary) {
        this.useDictionary = useDictionary;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public int getVariableLength() {
        return variableLength;
    }

    public void setVariableLength(int variableLength) {
        this.variableLength = variableLength;
    }

    public String getFillInStatus() {
        return fillInStatus;
    }

    public void setFillInStatus(String fillInStatus) {
        this.fillInStatus = fillInStatus;
    }

    public String getStandardVariableName() {
        return standardVariableName;
    }

    public void setStandardVariableName(String standardVariableName) {
        this.standardVariableName = standardVariableName;
    }
    
    public String getSQLqueryFormat(String string){
        if (getVariableType().equalsIgnoreCase("Dict")||getVariableType().equalsIgnoreCase("Alpha")){
            string = "'"+string+"'";
        }
        return string;
    }
}


