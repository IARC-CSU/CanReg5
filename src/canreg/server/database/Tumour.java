/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.util.HashMap;
import org.w3c.dom.Document;
import java.io.Serializable;
import java.util.Iterator;

/**
 *
 * @author morten
 */
public class Tumour implements Serializable, DatabaseRecord {

    private HashMap variables;

    /**
     * Creates a new instance of Patient
     */
    public Tumour() {
        variables = new HashMap();
    }

    public void setVariable(String variableName, Object value) {
        variables.put(variableName, value);
    }

    public Object getVariable(String variableName) {
        return variables.get(variableName);
    }

    public String[] getVariableNames() {
        String[] names = new String[variables.size()];
        Iterator it = variables.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            names[i++] = (String) it.next();
        }
        return names;
    }
}
