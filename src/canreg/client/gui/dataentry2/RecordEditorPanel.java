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
 *         Patricio Ezequiel Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.dataentry2;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.qualitycontrol.CheckResult;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm, patri_000
 */
public interface RecordEditorPanel {
    
    /**
     * The different types of Records.
     */
    enum panelTypes {
        PATIENT, TUMOUR, SOURCE
    }
    
    /**
     * Returns true if this Record has changed data and needs to be saved
     * in the database. False if the Record
     * @return 
     */
    boolean isSaveNeeded();
    
    /**
     * Sets if this Record needs to be saved.
     * @param saveNeeded true if this Record should be saved, false if the 
     * Record hasn't changed.
     */
    void setSaveNeeded(boolean saveNeeded);
    
    /**
     * Prepares this Record to be saved. The saving itself of all Records
     * is handled by canreg.client.gui.dataentry2.RecordEditor
     */
    void prepareToSaveRecord();
    
    /**
     * This method is known as setRecord() in 
     * canreg.client.gui.dataentry.RecordEditorPanel()
     * @param record     
     */
    void setDatabaseRecord(DatabaseRecord record);
    
    DatabaseRecord getDatabaseRecord();
    
    /**
     * Performs a complete refresh of the record:
     * - re-sets the record variables     
     * - re-builds the GUI (this means that ALL panels go to "no save is needed"
     * state)
     * @param record 
     * @param isSaveNeeded if true, is assumed the record has been refreshed but
     * not saved on the database, therefore the method isSaveNeeded will return true.
     * If false, is assumed the record has been previously saved and no new 
     * data has been input.
     */
    void refreshDatabaseRecord(DatabaseRecord record, boolean isSaveNeeded);
    void setDictionary(Map<Integer, Dictionary> dictionary);
    void setDocument(Document doc);    
    LinkedList<DatabaseVariablesListElement> getAutoFillList();
    void setVariable(DatabaseVariablesListElement variable, String value);
    boolean areAllVariablesPresent();
    void setResultCodeOfVariable(String databaseVariableName, CheckResult.ResultCode resultCode);
}
