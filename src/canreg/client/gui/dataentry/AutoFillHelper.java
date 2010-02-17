package canreg.client.gui.dataentry;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.server.database.DatabaseRecord;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
class AutoFillHelper {

    private GlobalToolBox globalToolBox;

    public void autoFill(LinkedList<DatabaseVariablesListElement> autoFillList, DatabaseRecord sourceOfActionDatabaseRecord, DatabaseRecord otherDatabaseRecord, RecordEditorPanel recordEditorPanel) {
        for (DatabaseVariablesListElement dvle : autoFillList) {
            String standardVariableName = dvle.getStandardVariableName();
            if (standardVariableName == null) {
                // recordEditorPanel.setVariable(dvle, "");
            } else if (Globals.StandardVariableNames.Age.toString().equalsIgnoreCase(standardVariableName)) {
                String code = "";
                // Object unknownCodeObject = dvle.getUnknownCode();
                // if (unknownCodeObject != null) {
                //    code = unknownCodeObject.toString();
                // } else {
                // old school unknown code
                // TODO: Calculate age in autofill!
                    for (int i = 0; i < dvle.getVariableLength(); i++) {
                        code += "9";
                    }
                // }
                recordEditorPanel.setVariable(dvle, code);
            } else if (Globals.StandardVariableNames.Behaviour.toString().equalsIgnoreCase(standardVariableName)) {
                recordEditorPanel.setVariable(dvle, "Behaviour to be calculated");
                DatabaseVariablesListElement morphologyVariableElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Morphology.toString());
                Object value;
                // try to get the variable from the source
                value = sourceOfActionDatabaseRecord.getVariable(morphologyVariableElement.getDatabaseVariableName());
                if (value == null) {
                    value = otherDatabaseRecord.getVariable(morphologyVariableElement.getDatabaseVariableName());
                }
                String valueString = "";
                if (value != null) {
                    valueString = morphologyToBehaviour(value);
                }
                recordEditorPanel.setVariable(dvle, valueString);
            }
        }
    }

    private String morphologyToBehaviour(Object morphology) {
        String returnString = "";
        if (morphology instanceof String) {
            String morphologyString = (String) morphology;
            if (morphologyString.length() > 4) {
                returnString = morphologyString.substring(4, 5);
            }
        }
        return returnString;
    }

    void setGlobalToolBox(GlobalToolBox globalToolBox) {
        this.globalToolBox = globalToolBox;
    }
}
