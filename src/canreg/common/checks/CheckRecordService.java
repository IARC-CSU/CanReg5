package canreg.common.checks;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.server.database.CanRegDAO;

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
     * Map of the variables: key = variable short name lowercase, value = variable definition
     */
    private final Map<String, DatabaseVariablesListElement> mapVariables;

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
        this.mapVariables = new HashMap<>();
        Arrays.stream(canRegDAO.getDatabaseVariablesList()).
                forEach(variable -> mapVariables.put(variable.getShortName().toLowerCase(Locale.ROOT), variable));

    }

    /**
     * Check record.
     *
     * @return list of messages, empty if OK
     */
    public List<String> checkRecord(DatabaseRecord record, String tableName) {
        List<String> result = new ArrayList<>();
        for (String variableName : record.getVariableNames()) {
            String variableNameLowercase = variableName.toLowerCase(Locale.ROOT);
            DatabaseVariablesListElement variableDefinition = mapVariables.get(variableNameLowercase);
            if (variableDefinition == null) {
                result.add("unknown variable: " + variableName);
                continue;
            }
            String variableType = variableDefinition.getVariableType();
            if (Globals.VARIABLE_TYPE_DICTIONARY_NAME.equalsIgnoreCase(variableType)) {
                // Dictionary value
                Dictionary dictionary = dictionaries.get(variableDefinition.getDictionaryID());
                // We consider that the variables and the dictionaries are consistent = dictionary is not null
                
                Object variableValue = record.getVariable(variableName);
                String fillInStatus = variableDefinition.getFillInStatus();

                if (variableValue == null) {
                    if(Globals.FILL_IN_STATUS_MANDATORY_STRING.equalsIgnoreCase(fillInStatus)) {
                        result.add("variable is mandatory: " + variableName);
                    }
                } else {
                    // value is not null
                    if (!dictionary.getDictionaryEntries().containsKey(variableValue.toString())) {
                        result.add("unknown value in dictionary " + variableName + "=" + variableValue);

                    }
                }
            }
        }

        return result;
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

