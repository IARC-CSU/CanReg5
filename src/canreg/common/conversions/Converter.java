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

package canreg.common.conversions;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class Converter {
    private LinkedList<DatabaseVariablesListElement> standardVariables;
    
    /**
     * 
     */
    public enum ConversionName{
        /**
         * 
         */
        ICDO3toICD10,
        /**
         * 
         */
        ICDO3toICCC3,
        /**
         * 
         */
        BirthIncidencetoAge
    }
    
    Map<ConversionName, ConversionInterface> conversions;

    /**
     * 
     * @param toolBox
     */
    public Converter(LinkedList<DatabaseVariablesListElement> standardVariables) {
        this.standardVariables = standardVariables;
        
        conversions = new LinkedHashMap<ConversionName, ConversionInterface>();

        ConversionInterface conversion;
        // Add ICDO3 to ICD10
        conversion = new ConversionICDO3toICD10();
        conversions.put(conversion.getConversionName(),conversion);
        // Add ICDO3 to ICCC3
        conversion = new ConversionICDO3toICCC3();
        conversions.put(conversion.getConversionName(),conversion);
    }
    
    /**
     * 
     * @param conversionName
     * @param variables
     * @return
     */
    public ConversionResult[] convert(ConversionName conversionName, Map<StandardVariableNames, Object> variables){
        ConversionResult[] result = null;
        ConversionInterface conversion = conversions.get(conversionName);
        if (conversion!=null){
            result = conversion.performConversion(variables);
        }
        return result;
    }
    
    /**
     * 
     * @param conversionName
     * @param patient
     * @param tumour
     * @return
     */
    public synchronized ConversionResult[] performConversion(ConversionName conversionName, Patient patient, Tumour tumour) {
        // Build map of standard variables
        Map<StandardVariableNames, Object> variables = new LinkedHashMap();

        for (DatabaseVariablesListElement dbvle : standardVariables) {
            String name = dbvle.getStandardVariableName();
            StandardVariableNames standardVariable = StandardVariableNames.valueOf(name);
            Object data = null;
            if (dbvle.getTable().equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                data = patient.getVariable(dbvle.getShortName());
            } else if (dbvle.getTable().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                data = tumour.getVariable(dbvle.getShortName());
            }
            variables.put(standardVariable, data);
        }

        return convert(conversionName,variables);
    }
}
