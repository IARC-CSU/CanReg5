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

// import fr.iarc.cin.iarctools.Globals.IARCStandardVariableNames;
import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 * 
 * This contains all non-static information needed by the CanReg program.
 */
public class GlobalToolBox {

    private final Document doc;
    private final Map<String, DatabaseVariablesListElement> standardVariableNameToDatabaseVariableListElementMap;
    private final Map<Integer, DatabaseGroupsListElement> groupIDToDatabaseGroupListElementMap;
    // private Map<IARCStandardVariableNames, String> mapIARCstandardVariablesVariableName;
    private final DatabaseVariablesListElement[] databaseVariablesListElements;
    private final Charset standardCharSet;
    private final Translator translator;
    private final Map<String, DatabaseVariablesListElement> databaseVariableNameToDatabaseVariableListElementMap;
    private final LocalSettings localSettings;

    /**
     * 
     * @param doc
     */
    public GlobalToolBox(Document doc) {
        this.doc = doc;
        groupIDToDatabaseGroupListElementMap = buildGroupMap(Tools.getGroupsListElements(doc, Globals.NAMESPACE));
        databaseVariablesListElements = Tools.getVariableListElements(doc, Globals.NAMESPACE);
        standardVariableNameToDatabaseVariableListElementMap = buildStandardVariablesMap(databaseVariablesListElements);
        databaseVariableNameToDatabaseVariableListElementMap = buildDBVariablesMap(databaseVariablesListElements);
        standardCharSet = Tools.getStandardCharset(doc, Globals.NAMESPACE);
        translator = Tools.getTranslator(doc, Globals.NAMESPACE);
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        // mapIARCstandardVariablesVariableName = Tools.getMapIARCstandardVariablesVariableName(doc, Globals.NAMESPACE);
    }

    public Document getDocument() {
        return doc;
    }

    public Charset getStandardCharset() {
        return standardCharSet;
    }

    public Translator getTranslator() {
        return translator;
    }

    /**
     * 
     * @return
     */
    public DatabaseVariablesListElement[] getVariables() {
        return databaseVariablesListElements;
    }

    /**
     * 
     * @return
     */
    public Map<String, DatabaseVariablesListElement> getVariablesMap() {
        return databaseVariableNameToDatabaseVariableListElementMap;
    }

    /**
     * 
     * @param standardVariableName
     * @return
     */
    public DatabaseVariablesListElement translateStandardVariableNameToDatabaseListElement(String standardVariableName) {
        return standardVariableNameToDatabaseVariableListElementMap.get(standardVariableName);
    }
    
        /**
     * 
     * @param standardVariableName
     * @return
     */
    public String translateStandardVariableNameToDatabaseVariableName(String standardVariableName) {
        DatabaseVariablesListElement temp = translateStandardVariableNameToDatabaseListElement(standardVariableName);
        if (temp==null)
            return null;
        else
            return temp.getDatabaseVariableName();
    }

    /**
     * 
     * @return
     */
    public LinkedList<DatabaseVariablesListElement> getStandardVariables() {
        Iterator<DatabaseVariablesListElement> it = standardVariableNameToDatabaseVariableListElementMap.values().iterator();
        DatabaseVariablesListElement dvle;
        LinkedList<DatabaseVariablesListElement> variables = new LinkedList<DatabaseVariablesListElement>();
        while (it.hasNext()) {
            dvle = it.next();
            variables.add(dvle);
        }
        return variables;
    }

    /**
     * 
     * @param groupID
     * @return
     */
    public DatabaseGroupsListElement translateGroupIDToDatabaseGroupListElement(Integer groupID) {
        return groupIDToDatabaseGroupListElementMap.get(groupID);
    }

    private static Map<Integer, DatabaseGroupsListElement> buildGroupMap(DatabaseGroupsListElement[] listElements) {
        Map map = new LinkedHashMap();
        for (DatabaseGroupsListElement dvle : listElements) {
            Integer groupID;
            groupID = dvle.getGroupIndex();
            if (groupID != null) {
                map.put(groupID, dvle);
            }
        }
        return map;
    }

    private static Map<String, DatabaseVariablesListElement> buildStandardVariablesMap(DatabaseVariablesListElement[] listelements) {
        Map map = new LinkedHashMap();
        // First build the real variables
        for (DatabaseVariablesListElement dvle : listelements) {
            String standardVariableName = dvle.getStandardVariableName();
            if (standardVariableName != null && standardVariableName.length() > 0) {
                map.put(standardVariableName, dvle);
            }
        }
        // Then build meta variables - i.e. behaviour as fifth digit in morphology

        return map;
    }

    private Map<String, DatabaseVariablesListElement> buildDBVariablesMap(DatabaseVariablesListElement[] listelements) {
        Map map = new LinkedHashMap();
        // First build the real variables
        for (DatabaseVariablesListElement dvle : listelements) {
            String dbVariableName = dvle.getDatabaseVariableName();
            if (dbVariableName != null && dbVariableName.length() > 0) {
                map.put(dbVariableName, dvle);
            }
        }
        // Then build meta variables - i.e. behaviour as fifth digit in morphology ?
        return map;
    }

    public String getPatientIDVariableName(DatabaseRecord record) {
        String name = null;
        if(record instanceof Patient){
            DatabaseVariablesListElement element = translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
            if (element!=null){
                name = element.getDatabaseVariableName();
            }
        } else if(record instanceof Tumour){
            DatabaseVariablesListElement element = translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString());
            if (element!=null){
                name = element.getDatabaseVariableName();
            }
        } else if(record instanceof Source){
            DatabaseVariablesListElement element = translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourIDSourceTable.toString());
            if (element!=null){
                name = element.getDatabaseVariableName();
            }
        }
        return name;
    }
    /**
     * @return the mapIARCstandardVariablesVariableName
     * 
    //   Commented away to be able to disable the IARCtools package... 
    //    
    
    
    public Map<IARCStandardVariableNames, String> getMapIARCstandardVariablesVariableName() {
    return mapIARCstandardVariablesVariableName;
    }
     */
    
    public int getUnknownAgeCode() {
        int code = 999;
        DatabaseVariablesListElement element = translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Age.toString());
        if (element!=null){
            int codeLenght = element.getVariableLength();
            code = 0;
            for (int i = 0; i<codeLenght;i++){
                code += 9*Math.pow(10, i);
            }
        }
        System.out.println("Unknown age code = "+code);
        return code;
    }
    public String getDateFormatString() {
        return localSettings.getProperty(LocalSettings.DATE_FORMAT_KEY);
    }
}
