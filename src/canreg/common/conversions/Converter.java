package canreg.common.conversions;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class Converter {
    private GlobalToolBox toolBox;
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
    public Converter(GlobalToolBox toolBox) {
        this.toolBox = toolBox;
        standardVariables = toolBox.getStandardVariables();
        
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
