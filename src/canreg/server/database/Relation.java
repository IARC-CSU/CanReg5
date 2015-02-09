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
