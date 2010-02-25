package canreg.client.gui.dataentry;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.server.database.DatabaseRecord;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                Object code = null;
                DatabaseVariablesListElement incidenceDateVariableElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString());
                Object incValue;
                DatabaseVariablesListElement birthDateVariableElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.BirthDate.toString());
                Object birthValue;

                // try to get the variable from the source
                incValue = sourceOfActionDatabaseRecord.getVariable(incidenceDateVariableElement.getDatabaseVariableName());
                if (incValue == null) {
                    incValue = otherDatabaseRecord.getVariable(incidenceDateVariableElement.getDatabaseVariableName());
                }

                birthValue = sourceOfActionDatabaseRecord.getVariable(birthDateVariableElement.getDatabaseVariableName());
                if (birthValue == null) {
                    birthValue = otherDatabaseRecord.getVariable(birthDateVariableElement.getDatabaseVariableName());
                }

                if (birthValue != null && incValue != null) {
                    Calendar birthDate;
                    Calendar incDate;
                    try {
                        birthDate = DateHelper.parseDateStringToGregorianCalendarCanReg(birthValue.toString(), Globals.DATE_FORMAT_STRING);
                        incDate = DateHelper.parseDateStringToGregorianCalendarCanReg(incValue.toString(), Globals.DATE_FORMAT_STRING);
                        int age = (int) DateHelper.yearsBetween(birthDate, incDate);
                        code = age;
                    } catch (ParseException ex) {
                        Logger.getLogger(AutoFillHelper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(AutoFillHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    code = dvle.getUnknownCode();
                    if (code == null) {
                        String codeString = "";
                        for (int i = 0; i < dvle.getVariableLength(); i++) {
                            codeString += "9";
                        }
                        code = codeString;
                    }
                }
                recordEditorPanel.setVariable(dvle, code + "");
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
