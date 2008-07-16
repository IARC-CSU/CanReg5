/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server.database;

/**
 *
 * @author morten
 */
public interface DatabaseRecord {
    public void setVariable(String variableName, Object value);
    public Object getVariable(String variableName);
    public String[] getVariableNames();
}
