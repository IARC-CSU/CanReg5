/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import canreg.server.*;
import java.util.HashMap;
import org.w3c.dom.Document;
import java.io.Serializable;

/**
 *
 * @author morten
 */
public class Tumour implements Serializable, DatabaseRecord {

    private HashMap variables;
    private Document doc;

    /**
     * Creates a new instance of Patient
     */
    public Tumour() {
        variables = new HashMap();
    }

    public Tumour(Document doc) {
        this.doc = doc;
        variables = new HashMap();
    }

    public void setVariable(String variableName, Object value) {
        variables.put(variableName, value);
    }

    public Object getVariable(String variableName) {
        return variables.get(variableName);
    }

}
