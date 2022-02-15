package canreg.common.checks;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.database.CanRegDAO;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service to check input data.
 */
public class CheckRecordService {

    /**
     * the dao for the service, main dao or dao for holding db.
     */
    private final CanRegDAO canRegDAO;

    /**
     * Map of the variables for each table: key = table name lowercase, value = map of variables for this table
     */
    private final Map<String, Map<String, DatabaseVariablesListElement>> mapTableVariables;

    /**
     * Dictionaries.
     */
    private Map<Integer, Dictionary> dictionaries;

    /**
     * Constructor.
     *
     * @param canRegDAO the dao for the service, main dao or dao for holding db
     */
    public CheckRecordService(CanRegDAO canRegDAO) {
        this.canRegDAO = canRegDAO;
        this.mapTableVariables = new HashMap<>();
        mapTableVariables.put(Globals.PATIENT_TABLE_NAME.toLowerCase(Locale.ENGLISH), new HashMap<>());
        mapTableVariables.put(Globals.TUMOUR_TABLE_NAME.toLowerCase(Locale.ENGLISH), new HashMap<>());
        mapTableVariables.put(Globals.SOURCE_TABLE_NAME.toLowerCase(Locale.ENGLISH), new HashMap<>());
        Arrays.stream(canRegDAO.getDatabaseVariablesList()).
                forEach(variable -> mapTableVariables.get(variable.getTable().toLowerCase(Locale.ENGLISH))
                        .put(variable.getShortName().toLowerCase(Locale.ENGLISH), variable));

    }

    /**
     * Check patient.
     * @param patient patient to be checked
     * @return list of messages, empty if OK
     */
    public List<String> checkPatient(Patient patient) {
        return checkRecord(patient, Globals.PATIENT_TABLE_NAME);
    }

    /**
     * Check tumour.
     * @param tumour tumour to be checked
     * @return list of messages, empty if OK
     */
    public List<String> checkTumour(Tumour tumour) {
        return checkRecord(tumour, Globals.TUMOUR_TABLE_NAME);
    }

    /**
     * Check source.
     * @param source source to be checked
     * @return list of messages, empty if OK
     */
    public List<String> checkSource(Source source) {
        return checkRecord(source, Globals.SOURCE_TABLE_NAME);
    }
    
    /**
     * Check record.
     * @param dbRrecord record to be checked
     * @param tableName name of the table                  
     * @return list of messages, empty if OK
     */
    private List<String> checkRecord(DatabaseRecord dbRrecord, String tableName) {
        List<String> result = new ArrayList<>();
        Map<String, DatabaseVariablesListElement> mapVariablesForTable =
                mapTableVariables.get(tableName.toLowerCase(Locale.ENGLISH));
        if (mapVariablesForTable == null) {
            result.add("unknown table: " + tableName);
            return result;
        }

        // Build a map of the record variables with lowercase names
        Map<String, Object> mapRecordVariables = new HashMap<>();
        for (String variableName : dbRrecord.getVariableNames()) {
            mapRecordVariables.put(variableName.toLowerCase(Locale.ENGLISH), dbRrecord.getVariable(variableName));
        }

        // Check every variable of the table
        for (Map.Entry<String, DatabaseVariablesListElement> entry : mapVariablesForTable.entrySet()) {
            String variableName = entry.getKey();
            DatabaseVariablesListElement variableDefinition = entry.getValue();
            Object recordVariableValue = mapRecordVariables.get(variableName);
            String variableType = variableDefinition.getVariableType();

            checkDictionaryVariable(result, variableName, variableDefinition, recordVariableValue, variableType);

            checkMandatoryVariable(result, variableName, variableDefinition, recordVariableValue);

            checkNumber(result, variableName, recordVariableValue, variableType);
        }

        // Detect unknown variables
        checkUnknownVariables(dbRrecord, result, mapVariablesForTable);

        return result;
    }

    private void checkDictionaryVariable(List<String> result, String variableName,
                                         DatabaseVariablesListElement variableDefinition, Object recordVariableValue,
                                         String variableType) {
        if (Globals.VARIABLE_TYPE_DICTIONARY_NAME.equalsIgnoreCase(variableType)) {
            // Dictionary value
            Dictionary dictionary = dictionaries.get(variableDefinition.getDictionaryID());
            // We consider that the variables and the dictionaries are consistent = dictionary is not null

            if (recordVariableValue != null
                    && !StringUtils.isEmpty(recordVariableValue.toString())
                    && !dictionary.getDictionaryEntries().containsKey(recordVariableValue.toString())) {
                // value is not null and not empty and not in dictionary
                result.add("unknown value in dictionary " + variableName + "=" + recordVariableValue);
            }
        }
    }

    private void checkMandatoryVariable(List<String> result, String variableName, 
                                        DatabaseVariablesListElement variableDefinition, Object recordVariableValue) {
        if (recordVariableValue == null
                || (variableDefinition.isInputString() && StringUtils.isEmpty(recordVariableValue.toString()))) {
            String fillInStatus = variableDefinition.getFillInStatus();
            if (Globals.FILL_IN_STATUS_MANDATORY_STRING.equalsIgnoreCase(fillInStatus)) {
                result.add("variable is mandatory: " + variableName);
            }
        }
    }

    private void checkUnknownVariables(DatabaseRecord dbRecord, List<String> result, Map<String,
            DatabaseVariablesListElement> mapVariablesForTable) {
        for (String variableName : dbRecord.getVariableNames()) {
            String variableNameLowercase = variableName.toLowerCase(Locale.ENGLISH);
            DatabaseVariablesListElement variableDefinition = mapVariablesForTable.get(variableNameLowercase);
            if (variableDefinition == null) {
                result.add("unknown variable: " + variableName);
            }
        }
    }

    private void checkNumber(List<String> result, String variableName, Object recordVariableValue, String variableType) {
        if(Globals.VARIABLE_TYPE_NUMBER_NAME.equalsIgnoreCase(variableType)
                && recordVariableValue != null && !(recordVariableValue instanceof Integer)) {
            result.add("integer input is expected for variable: " + variableName);
        }
    }

    /**
     * Setter dictionaries.
     *
     * @param dictionaries dictionaries.
     */
    public void setDictionaries(Map<Integer, Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }
}

