package canreg.common.checks;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.server.database.CanRegDAO;
import org.apache.commons.lang.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service to check input data. <br>
 * Report errors and warnings.<br>
 * - Errors: <br>
 *   - Dictionary variable: the value must be present in dictionary entries.<br>
 *   - Number variable: value must be an integer.<br>
 *   - Date variable: value must be yyyyMMdd.<br>
 *   - Length: the length must not exceed the column length in the table (for text variable except date).<br>
 * - Warnings: <br>
 *   - Mandatory variable: value must not be null or an empty string.<br>
 *   - Unknown variable detected<br>
 *
 * - Does not handle the variables with "Automatic" fill: it will be done when importing in the main db<br>
 * - Builds the raw_data field, input data and missing date only, like: <br>
 * <code>
 *    <strong>REGNO: </strong>20066018 <br> 
 *    <strong>SEX: 3 (this code is not in the dictionary)</strong>
 *    etc.
 * </code>
 * - Builds the format_errors field, like: <br>
 * <code>
 *     SEX<br> 
 * </code>
 */
public class CheckRecordService {

    public static final String VARIABLE_RAW_DATA = "raw_data";
    public static final String VARIABLE_FORMAT_ERRORS = "format_errors";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            // use uuuu instead of yyyy for strict validation:
            .ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Map of the variables for each table: key = table name, value = map of variables for this table.<br>
     * The variable names are in lowercase, like in the DatabaseRecord.
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
        this.mapTableVariables = new HashMap<>();
        mapTableVariables.put(Globals.PATIENT_TABLE_NAME, new TreeMap<>());
        mapTableVariables.put(Globals.TUMOUR_TABLE_NAME, new TreeMap<>());
        mapTableVariables.put(Globals.SOURCE_TABLE_NAME, new TreeMap<>());
        Arrays.stream(canRegDAO.getDatabaseVariablesList()).
                forEach(variable -> mapTableVariables.get(variable.getTable())
                            .put(Tools.toLowerCaseStandardized(variable.getShortName()), variable)
                );

    }

    /**
     * Check patient.
     * @param patient patient to be checked
     * @return list of messages, empty if OK
     */
    public List<CheckMessage> checkPatient(Patient patient) {
        return checkRecord(patient, Globals.PATIENT_TABLE_NAME);
    }

    /**
     * Check tumour.
     * @param tumour tumour to be checked
     * @return list of messages, empty if OK
     */
    public List<CheckMessage> checkTumour(Tumour tumour) {
        return checkRecord(tumour, Globals.TUMOUR_TABLE_NAME);
    }

    /**
     * Check source.
     * @param source source to be checked
     * @return list of messages, empty if OK
     */
    public List<CheckMessage> checkSource(Source source) {
        return checkRecord(source, Globals.SOURCE_TABLE_NAME);
    }
    
    /**
     * Check record.
     * @param dbRecord record to be checked
     * @param tableName name of the table                  
     * @return list of messages, empty if OK
     */
    private List<CheckMessage> checkRecord(DatabaseRecord dbRecord, String tableName) {
        List<CheckMessage> checkMessages = new ArrayList<>();

        // The variable names are in lowercase in dbRecord
        Map<String, DatabaseVariablesListElement> mapVariablesForTable = mapTableVariables.get(tableName);
        if(mapVariablesForTable == null) {
            throw new IllegalArgumentException("Table name is not correct: " + tableName);
        }
        StringBuilder rawData = new StringBuilder(100);
        StringBuilder formatErrors = new StringBuilder();
        
        // Check every variable of the table
        for (Map.Entry<String, DatabaseVariablesListElement> entry : mapVariablesForTable.entrySet()) {
            String variableName = entry.getKey();
            DatabaseVariablesListElement variableDefinition = entry.getValue();
            Object variableValue = dbRecord.getVariable(variableName);
            String variableType = variableDefinition.getVariableType();
            boolean isEmpty = variableValue == null || StringUtils.isEmpty(variableValue.toString());
            List<CheckMessage> variableMessages = new ArrayList<>();
            
            checkDictionaryVariable(variableMessages, variableName, variableDefinition, variableValue, 
                    variableType, isEmpty);

            checkMandatoryVariable(variableMessages, variableName, variableDefinition, isEmpty);

            checkNumber(variableMessages, variableName, variableValue, variableType);

            checkDate(variableMessages, variableName, variableValue, variableType, isEmpty);
            
            checkTextVariableLength(variableMessages, variableName, variableDefinition.getVariableLength(), 
                    variableValue, variableType, isEmpty);

            feedRawData(variableMessages, rawData, variableName, variableValue, isEmpty);
            
            // Add the messages
            if(!variableMessages.isEmpty()) {
                checkMessages.addAll(variableMessages);
                formatErrors.append(variableName.toUpperCase(Locale.ENGLISH)).append("<br>");
            }
        }

        // Detect unknown variables
        checkUnknownVariables(dbRecord, checkMessages, mapVariablesForTable);

        dbRecord.setVariable(VARIABLE_RAW_DATA, rawData.toString());
        dbRecord.setVariable(VARIABLE_FORMAT_ERRORS, formatErrors.toString());
        return checkMessages;
    }

    /**
     * Feed the rawData for one variable if recordVariableValue is not null or variableMessages is not empty. <br>
     * Nothing added if no input and no error / warning for this variable.
     * @param variableMessages the messages for the variable
     * @param rawData the raw data to feed
     * @param variableName the variable name
     * @param recordVariableValue the variable value
     * @param isEmpty true if the value is empty
     */
    private void feedRawData(List<CheckMessage> variableMessages, StringBuilder rawData, String variableName, 
                             Object recordVariableValue, boolean isEmpty) {
        if(!variableMessages.isEmpty() || recordVariableValue != null) {
            // Add the field to the raw data
            rawData.append("<strong>").append(variableName.toUpperCase(Locale.ENGLISH)).append(": </strong>")
                    .append(isEmpty ? StringUtils.EMPTY : recordVariableValue);
            if (!variableMessages.isEmpty()) {
                rawData.append(" (")
                        .append(variableMessages.stream()
                                .map(CheckMessage::shortMessage).collect(Collectors.joining(", ")))
                        .append(')');
            }
            rawData.append("<br>");
        }
    }

    private void checkDictionaryVariable(List<CheckMessage> variableMessages, String variableName,
                                         DatabaseVariablesListElement variableDefinition, Object variableValue,
                                         String variableType, boolean isEmpty) {
        if (!isEmpty && Globals.VARIABLE_TYPE_DICTIONARY_NAME.equalsIgnoreCase(variableType)) {
            // Dictionary value
            Dictionary dictionary = dictionaries.get(variableDefinition.getDictionaryID());
            // We consider that the variables and the dictionaries are consistent = dictionary is not null

            if (!dictionary.getDictionaryEntries().containsKey(variableValue.toString())) {
                // value is not empty and not in dictionary
                variableMessages.add(new CheckMessage(variableName, variableValue, 
                        "this code is not in the dictionary", true));
            }
        }
    }

    private void checkMandatoryVariable(List<CheckMessage> variableMessages, String variableName,
                                        DatabaseVariablesListElement variableDefinition,
                                        boolean isEmpty) {
        if (isEmpty 
                && Globals.FILL_IN_STATUS_MANDATORY_STRING.equalsIgnoreCase(variableDefinition.getFillInStatus())) {
            variableMessages.add(new CheckMessage(variableName, StringUtils.EMPTY,
                    "this variable is mandatory", false));
            
        }
    }

    private void checkUnknownVariables(DatabaseRecord dbRecord, List<CheckMessage> variableMessages, Map<String,
            DatabaseVariablesListElement> mapVariablesForTable) {
        for (String variableName : dbRecord.getVariableNames()) {
            if(dbRecord.getVariable(variableName) != null) {
                // The value is not null
                DatabaseVariablesListElement variableDefinition = mapVariablesForTable.get(variableName);
                if (variableDefinition == null) {
                    // The variable is not known
                    variableMessages.add(new CheckMessage(variableName, dbRecord.getVariable(variableName),
                            "unknown variable", false));
                }
            }
        }
    }

    private void checkNumber(List<CheckMessage> variableMessages, String variableName, Object variableValue, 
                             String variableType) {
        if(Globals.VARIABLE_TYPE_NUMBER_NAME.equalsIgnoreCase(variableType)
                && variableValue != null && !(variableValue instanceof Integer)) {
            variableMessages.add(new CheckMessage(variableName, variableValue,
                    "this value is not an integer", true));
        }
    }

    private void checkDate(List<CheckMessage> variableMessages, String variableName, Object variableValue, 
                           String variableType, boolean isEmpty) {
        if(!isEmpty && Globals.VARIABLE_TYPE_DATE_NAME.equalsIgnoreCase(variableType)) {
            String dateValue = variableValue.toString();
            // Try to parse
            try {
                LocalDate.parse(dateValue, DATE_FORMATTER);
                // The date is OK
            } catch (DateTimeException e) {
                // The date is not OK
                variableMessages.add(new CheckMessage(variableName, variableValue,
                        "this date is not a valid date yyyyMMdd", true));
            }
        }
    }

    private void checkTextVariableLength(List<CheckMessage> variableMessages, String variableName,
                                         int variableLength, Object variableValue,
                                         String variableType, boolean isEmpty) {
        if(!isEmpty 
                && !Globals.VARIABLE_TYPE_NUMBER_NAME.equalsIgnoreCase(variableType)
                && !Globals.VARIABLE_TYPE_DATE_NAME.equalsIgnoreCase(variableType)
        ) {
            // Text variable (except date already checked)
            if(!(variableValue instanceof String)) {
                variableMessages.add(new CheckMessage(variableName, variableValue,
                        "the value must be a text", true));
            } else {
                String valueString = variableValue.toString();
                if (valueString.length() > variableLength) {
                    variableMessages.add(new CheckMessage(variableName, variableValue,
                            "the length " + valueString.length() + " exceeds the maximum length "
                                    + variableLength, true));
                }
            }
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

