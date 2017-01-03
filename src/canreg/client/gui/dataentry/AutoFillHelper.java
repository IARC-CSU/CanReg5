/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.client.gui.dataentry;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import canreg.common.database.DatabaseRecord;
import java.text.ParseException;
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
                    GregorianCalendarCanReg birthDate;
                    GregorianCalendarCanReg incDate;
                    try {
                        birthDate = DateHelper.parseDateStringToGregorianCalendarCanReg(birthValue.toString(), Globals.DATE_FORMAT_STRING);
                        incDate = DateHelper.parseDateStringToGregorianCalendarCanReg(incValue.toString(), Globals.DATE_FORMAT_STRING);
                        int age = (int) DateHelper.yearsBetween(birthDate, incDate);
                        code = age;
                    } catch (ParseException ex) {
                        Logger.getLogger(AutoFillHelper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(AutoFillHelper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NullPointerException nex) {
                        code = dvle.getUnknownCode();
                        if (code == null) {
                            String codeString = "";
                            for (int i = 0; i < dvle.getVariableLength(); i++) {
                                codeString += "9";
                            }
                            code = codeString;
                        }
                        Logger.getLogger(AutoFillHelper.class.getName()).log(Level.WARNING, null, nex);
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
            } else if (Globals.StandardVariableNames.Grade.toString().equalsIgnoreCase(standardVariableName)) {
                recordEditorPanel.setVariable(dvle, "Grade to be calculated");
                DatabaseVariablesListElement morphologyVariableElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Morphology.toString());
                Object value;
                // try to get the variable from the source
                value = sourceOfActionDatabaseRecord.getVariable(morphologyVariableElement.getDatabaseVariableName());
                if (value == null) {
                    value = otherDatabaseRecord.getVariable(morphologyVariableElement.getDatabaseVariableName());
                }
                String valueString = "";
                if (value != null) {
                    valueString = morphologyToGrade(value);
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

    private String morphologyToGrade(Object morphology) {
        String returnString = "";
        if (morphology instanceof String) {
            String morphologyString = (String) morphology;
            if (morphologyString.length() > 5) {
                returnString = morphologyString.substring(5, 6);
            }
        }
        return returnString;
    }

    void setGlobalToolBox(GlobalToolBox globalToolBox) {
        this.globalToolBox = globalToolBox;
    }
}
