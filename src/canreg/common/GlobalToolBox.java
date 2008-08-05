package canreg.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 * 
 * This contains all non-static information needed by the CanReg program.
 */
public class GlobalToolBox {
    private Document doc;
    private Map<String, DatabaseVariablesListElement> standardVariableNameToDatabaseVariableListElementMap;
    private Map<Integer, DatabaseGroupsListElement> groupIDToDatabaseGroupListElementMap;
    private DatabaseVariablesListElement[] databaseVariablesListElements;
    
    public GlobalToolBox(Document doc) {
        this.doc = doc;        
        groupIDToDatabaseGroupListElementMap = buildGroupMap(Tools.getGroupsListElements(doc, Globals.NAMESPACE));
        databaseVariablesListElements = Tools.getVariableListElements(doc, Globals.NAMESPACE);
        standardVariableNameToDatabaseVariableListElementMap = buildVariablesMap(databaseVariablesListElements);
    }    
    
    public DatabaseVariablesListElement translateStandardVariableNameToDatabaseListElement(String standardVariableName){
        return standardVariableNameToDatabaseVariableListElementMap.get(standardVariableName);
    }

    public DatabaseGroupsListElement translateStandardVariableNameToDatabaseListElement(Integer groupID){
        return groupIDToDatabaseGroupListElementMap.get(groupID);
    }
    
    private static Map<Integer, DatabaseGroupsListElement> buildGroupMap(DatabaseGroupsListElement[] listElements){
        Map map = new LinkedHashMap();
        for (DatabaseGroupsListElement dvle:listElements){
            Integer groupID = dvle.getGroupIndex();
            if (groupID!=null){
                map.put(groupID, dvle);
            }
        }
        return map;
    }
    
    private static Map<String, DatabaseVariablesListElement> buildVariablesMap(DatabaseVariablesListElement[] listelements){
        Map map = new LinkedHashMap();
        for (DatabaseVariablesListElement dvle:listelements){
            String standardVariableName = dvle.getStandardVariableName();
            if (standardVariableName!=null){
                map.put(standardVariableName, dvle);
            }
        }
        return map;
    }
    
}
