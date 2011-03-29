/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import java.beans.PropertyChangeListener;
import javax.swing.event.DocumentListener;

/**
 *
 * @author ervikm
 */
public interface VariableEditorPanelInterface {

    public static java.awt.Color MANDATORY_VARIABLE_MISSING_COLOR = java.awt.Color.PINK;
    public static java.awt.Color VARIABLE_INVALID_COLOR = java.awt.Color.PINK;
    public static java.awt.Color VARIABLE_QUERY_COLOR = java.awt.Color.GREEN;
    public static java.awt.Color VARIABLE_RARE_COLOR = java.awt.Color.YELLOW;
    public static java.awt.Color VARIABLE_OK_COLOR = java.awt.SystemColor.text;
    public static String CHANGED_STRING = "Changed";

    public String getKey();

    public boolean isFilledOK();

    public void setResultCode(ResultCode resultCode);

    public void setSaved();

    public void setInitialValue(String value);

    public boolean hasChanged();

    public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    public void setDocumentListener(DocumentListener listener);

    public void setValue(String value);

    public void updateFilledInStatusColor();

    public Object getValue();

    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement);

    public void removeListener();
}
