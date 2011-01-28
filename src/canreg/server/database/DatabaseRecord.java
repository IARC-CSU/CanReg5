package canreg.server.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author ervikm
 */
public class DatabaseRecord implements Serializable {
    private HashMap<String, Object> variables;
    
    /**
     * 
     */
    public DatabaseRecord() {
        variables = new HashMap <String, Object> ();
    }
    
    /**
     * 
     * @param variableName
     * @param value
     */
    public void setVariable(String variableName, Object value){
        variables.put(variableName.toLowerCase(), value);
    }
    
    /**
     * 
     * @param variableName
     * @return
     */
    public Object getVariable(String variableName){
        return variables.get(variableName.toLowerCase());
    }

    /**
     * 
     * @return
     */
    public String[] getVariableNames() {
        String[] names = new String[variables.size()]; 
        Iterator it = variables.keySet().iterator();
        int i = 0;
        while (it.hasNext()){
            names[i++] = (String) it.next();
        }
        return names;
    }
}
