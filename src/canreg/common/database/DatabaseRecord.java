/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

 package canreg.common.database;

import canreg.common.Translator;
import fr.iarc.cin.iarctools.Globals.IARCStandardVariableNames;
import fr.iarc.cin.iarctools.tools.RecordInterface;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;

/**
 *
 * @author ervikm
 */
public class DatabaseRecord implements Serializable, RecordInterface {

    private HashMap<String, Object> variables;
    private EnumMap<IARCStandardVariableNames, String> map;
    private Translator translator;

    /**
     * 
     */
    public DatabaseRecord() {
        variables = new HashMap<String, Object>();
    }

    /**
     * 
     * @param variableName
     * @param value
     */
    public void setVariable(String variableName, Object value) {
        variables.put(canreg.common.Tools.toLowerCaseStandardized(variableName), value);
    }

    /**
     * 
     * @param variableName
     * @return
     */
    public Object getVariable(String variableName) {
        return variables.get(canreg.common.Tools.toLowerCaseStandardized(variableName));
    }

    /**
     * 
     * @return
     */
    public String[] getVariableNames() {
        return variables.keySet().toArray(new String[0]);
    }

    public void setMapIARCstandardVariablesVariableName(EnumMap<IARCStandardVariableNames, String> map) {
        this.map = map;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    @Override
    public Object getValueByIARCStandardVariableName(IARCStandardVariableNames iarcVariableName) {
        String variableName = map.get(iarcVariableName);
        Object value = null;
        if (variableName != null) {
            variables.get(variableName);
            if (translator != null) {
                value = translator.translate(variableName, value);
            }
        }
        return value;
    }
}
