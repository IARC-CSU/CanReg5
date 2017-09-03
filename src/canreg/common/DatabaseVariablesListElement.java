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

/**
 *
 * @author ervikm
 */
public class DatabaseVariablesListElement implements Serializable, DatabaseElement, Comparable<DatabaseVariablesListElement> {
    // Table in the database for the variable

    private String table;
    // ID of the variable in the database
    private int variableID;
    //
    private String shortName;
    // Variable type
    private String variableType;
    // Dictionary
    private DatabaseDictionaryListElement databaseDictionaryListElement;
    private String fullName;
    private String englishName;
    // private String groupName;
    private String standardVariableName = null;
    private int xPos = 0;
    private int yPos = 0;
    private int variableLength;
    private String fillInStatus;
    private Object unknownCode = null;
    private String variableFormula = null;
    private String multiplePrimaryCopy = null;
    private DatabaseGroupsListElement group;
    private String dateFormatString = "yyyyMMdd";

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
        return getFullName() + " (" + getTable() + ")";
    }

    /**
     * 
     * @return
     */
    public int getDictionaryID() {
        if (databaseDictionaryListElement == null) {
            return -1;
        } else {
            return databaseDictionaryListElement.getDictionaryID();
        }
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
        if (databaseDictionaryListElement != null) {
            return databaseDictionaryListElement.getName();
        } else {
            return null;
        }
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
        if (group != null) {
            return group.getGroupIndex();
        } else {
            return -1;
        }
    }

    /**
     * 
     * @return
     */
    public String getGroupName() {
        if (group != null) {
            return group.getGroupName();
        } else {
            return "";
        }
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
    public String getSQLqueryFormat(String string) {
        if (getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME) || getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME) || getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME) || getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)/*<ictl.co>*/|| getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)/*</ictl.co>*/) {
            string = "'" + string + "'";
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
        return databaseDictionaryListElement.isCompound();
    }

    /**
     * @return the metaVariable
     */
    public boolean isMetaVariable() {
        return variableType.equalsIgnoreCase("Meta");
    }

    /**
     * @param metaVariable the metaVariable to set
     */
    public void setMetaVariable(boolean metaVariable) {
        if (metaVariable) {
            this.variableType = "Meta";
        }
    }

    /**
     * @return the metaVariableFormula
     */
    public String getMetaVariableFormula() {
        return variableFormula;
    }

    /**
     * @param metaVariableFormula the metaVariableFormula to set
     */
    public void setMetaVariableFormula(String metaVariableFormula) {
        this.variableFormula = metaVariableFormula;
    }

    /**
     * @return the multiplePrimaryCopy
     */
    public String getMultiplePrimaryCopy() {
        return multiplePrimaryCopy;
    }

    /**
     * @param multiplePrimaryCopy the multiplePrimaryCopy to set
     */
    public void setMultiplePrimaryCopy(String multiplePrimaryCopy) {
        this.multiplePrimaryCopy = multiplePrimaryCopy;
    }

    public void setDictionary(DatabaseDictionaryListElement dictionary) {
        this.databaseDictionaryListElement = dictionary;
        this.variableLength = dictionary.getFullDictionaryCodeLength();
        this.unknownCode = dictionary.getUnkownCode();
    }

    public DatabaseDictionaryListElement getDictionary() {
        return databaseDictionaryListElement;
    }

    public String getDictionaryName() {
        if (databaseDictionaryListElement != null) {
            return databaseDictionaryListElement.getName();
        } else {
            return "";
        }
    }

    public void setGroup(DatabaseGroupsListElement group) {
        this.group = group;
    }

    @Override
    public String getDescriptiveString() {
        String desc = "";
        if (getStandardVariableName() != null) {
            desc += "* " + getFullName() + " (StdVar: " + getStandardVariableName() + ") ";
        } else {
            desc += getFullName();
        }
        if (getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME)) {
            desc += " (Dict: " + getDictionaryName() + " Group: " + getGroupName() + ")";
        } else if (getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
            desc += " (Number, Group: " + getGroupName() + ")";
        } else if (getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME)
                || getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME)
                || getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)
                /*<ictl.co>*/ || getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)/*</ictl.co>*/) {
            desc += " (Text, Length: " + getVariableLength() + ", Group: " + getGroupName() + ")";
        } else if (getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
            desc += " (Date, Group: " + getGroupName() + ")";
        } else {
            desc += " (Group: " + getGroupName() + ")";
        }
        desc = desc + "";
        return desc;
    }

    @Override
    public boolean userVariable() {
        return (getGroupID() > 0);
    }

    @Override
    public int compareTo(DatabaseVariablesListElement o) {
        return toString().compareTo(o.toString());
    }
    
    public String getDateFormatString() {
        return dateFormatString;
    }
    public void setDateFormatString(String dateFormat) {
        this.dateFormatString = dateFormat;
    }
}
