/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author morten
 */
public class Patient implements Serializable, DatabaseRecord {

    public HashMap<String, Object> variables;
    
    /**
     * Creates a new instance of Patient
     */
    public Patient() {
        variables = new HashMap <String, Object> ();
    }
    
    public void setVariable(String variableName, Object value){
        variables.put(variableName, value);
    }
    
    public Object getVariable(String variableName){
        return variables.get(variableName);
    }
    
    @Override
    public String toString(){
        // Very temporary for debugging purposes
        return (String) variables.get("FamN");
    }

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
