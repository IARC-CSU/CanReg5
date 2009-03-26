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
    // Dictionary Compund?
    private boolean dictionaryCompound = false;

    private String fullName;
    private String englishName;
    // private String groupName;
    private String standardVariableName;
    
    private int xPos;
    private int yPos;
    
    private int variableLength;
    
    private String fillInStatus;
    
    private int groupID;
    
    private Object unknownCode;
    
    /**
     * 
     * @param databaseTableName
     * @param databaseTableVariableID
     * @param databaseVariableName
     * @param variableType
     */
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

    /**
     * 
     * @return
     */
    public String getDatabaseTableName() {
        return getTable();
    }

    /**
     * 
     * @param databaseTableName
     */
    public void setDatabaseTableName(String databaseTableName) {
        this.setTable(databaseTableName);
    }

    /**
     * 
     * @return
     */
    public int getDatabaseTableVariableID() {
        return getVariableID();
    }

    /**
     * 
     * @param databaseTableVariableID
     */
    public void setDatabaseTableVariableID(int databaseTableVariableID) {
        this.setVariableID(databaseTableVariableID);
    }

    /**
     * 
     * @return
     */
    public String getDatabaseVariableName() {
        return getShortName();
    }

    /**
     * 
     * @param databaseVariableName
     */
    public void setDatabaseVariableName(String databaseVariableName) {
        this.setShortName(databaseVariableName);
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

    /**
     * 
     * @param o
     * @return
     */
    public boolean equals(DatabaseVariablesListElement o) {
        return getVariableID() == o.getDatabaseTableVariableID() && getTable().equalsIgnoreCase(o.getDatabaseTableName());
    }

    @Override
    public String toString() {
        return getShortName();

    }

    /**
     * 
     * @return
     */
    public int getDictionaryID() {
        return dictionaryID;
    }

    /**
     * 
     * @param dictionaryID
     */
    public void setDictionaryID(int dictionaryID) {
        this.dictionaryID = dictionaryID;
    }

    /**
     * 
     * @return
     */
    public String getTable() {
        return table;
    }

    /**
     * 
     * @param table
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * 
     * @return
     */
    public int getVariableID() {
        return variableID;
    }

    /**
     * 
     * @param variableID
     */
    public void setVariableID(int variableID) {
        this.variableID = variableID;
    }

    /**
     * 
     * @return
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * 
     * @param shortName
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * 
     * @return
     */
    public String getUseDictionary() {
        return useDictionary;
    }

    /**
     * 
     * @param useDictionary
     */
    public void setUseDictionary(String useDictionary) {
        this.useDictionary = useDictionary;
    }

    /**
     * 
     * @return
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * 
     * @param fullName
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * 
     * @return
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * 
     * @param englishName
     */
    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    /**
     * 
     * @return
     */
    public int getGroupID() {
        return groupID;
    }

    /**
     * 
     * @param groupID
     */
    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    /**
     * 
     * @return
     */
    public int getXPos() {
        return xPos;
    }

    /**
     * 
     * @param xPos
     */
    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    /**
     * 
     * @return
     */
    public int getYPos() {
        return yPos;
    }

    /**
     * 
     * @param yPos
     */
    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    /**
     * 
     * @return
     */
    public int getVariableLength() {
        return variableLength;
    }

    /**
     * 
     * @param variableLength
     */
    public void setVariableLength(int variableLength) {
        this.variableLength = variableLength;
    }

    /**
     * 
     * @return
     */
    public String getFillInStatus() {
        return fillInStatus;
    }

    /**
     * 
     * @param fillInStatus
     */
    public void setFillInStatus(String fillInStatus) {
        this.fillInStatus = fillInStatus;
    }

    /**
     * 
     * @return
     */
    public String getStandardVariableName() {
        return standardVariableName;
    }

    /**
     * 
     * @param standardVariableName
     */
    public void setStandardVariableName(String standardVariableName) {
        this.standardVariableName = standardVariableName;
    }
    
    /**
     * 
     * @param string
     * @return
     */
    public String getSQLqueryFormat(String string){
        if (getVariableType().equalsIgnoreCase("Dict")||getVariableType().equalsIgnoreCase("Alpha")){
            string = "'"+string+"'";
        }
        return string;
    }

    /**
     * 
     * @return
     */
    public Object getUnknownCode() {
        return unknownCode;
    }

    /**
     * 
     * @param unknownCode
     */
    public void setUnknownCode(Object unknownCode) {
        this.unknownCode = unknownCode;
    }

    /**
     * @return the dictionaryCompound
     */
    public boolean isDictionaryCompound() {
        return dictionaryCompound;
    }

    /**
     * @param dictionaryCompound the dictionaryCompound to set
     */
    public void setDictionaryCompound(boolean dictionaryCompound) {
        this.dictionaryCompound = dictionaryCompound;
    }
}


