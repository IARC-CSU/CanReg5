package canreg.common;

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
    private Document doc;
    private Map<String, DatabaseVariablesListElement> standardVariableNameToDatabaseVariableListElementMap;
    private Map<Integer, DatabaseGroupsListElement> groupIDToDatabaseGroupListElementMap;
    private DatabaseVariablesListElement[] databaseVariablesListElements;
    
    /**
     * 
     * @param doc
     */
    public GlobalToolBox(Document doc) {
        this.doc = doc;        
        groupIDToDatabaseGroupListElementMap = buildGroupMap(Tools.getGroupsListElements(doc, Globals.NAMESPACE));
        databaseVariablesListElements = Tools.getVariableListElements(doc, Globals.NAMESPACE);
        standardVariableNameToDatabaseVariableListElementMap = buildVariablesMap(databaseVariablesListElements);
    }

    public Document getDocument() {
        return doc;
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
     * @param standardVariableName
     * @return
     */
    public DatabaseVariablesListElement translateStandardVariableNameToDatabaseListElement(String standardVariableName){
        return standardVariableNameToDatabaseVariableListElementMap.get(standardVariableName);
    }
    
    /**
     * 
     * @return
     */
    public LinkedList<DatabaseVariablesListElement> getStandardVariables(){
        Iterator<DatabaseVariablesListElement> it = standardVariableNameToDatabaseVariableListElementMap.values().iterator();
        DatabaseVariablesListElement dvle;
        LinkedList<DatabaseVariablesListElement> variables = new LinkedList<DatabaseVariablesListElement>();
        while (it.hasNext()){
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
    public DatabaseGroupsListElement translateGroupIDToDatabaseGroupListElement(Integer groupID){
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
