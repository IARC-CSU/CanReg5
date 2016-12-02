/*
 * Copyright (C) 2016 patri_000
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
 * @author patri_000
 */
public interface RecordEditorPanel {
    
    enum panelTypes {
        PATIENT, TUMOUR, SOURCE
    }
    
    boolean isSaveNeeded();
    void setSaveNeeded(boolean saveNeeded);
    
    /**
     * This method is known as setRecord() in canreg.client.gui.dataentry.RecordEditorPanel()
     * @param dbr 
     */
    void setDatabaseRecord(DatabaseRecord record);
    DatabaseRecord getDatabaseRecord();
    void refreshDatabaseRecord(DatabaseRecord record);
    void setDictionary(Map<Integer, Dictionary> dictionary);
    void setDocument(Document doc);
    //void toggleObsolete(boolean confirmed);
    LinkedList<DatabaseVariablesListElement> getAutoFillList();
    void setVariable(DatabaseVariablesListElement variable, String value);
    boolean areAllVariablesPresent();
    void setResultCodeOfVariable(String databaseVariableName, CheckResult.ResultCode resultCode);
}
