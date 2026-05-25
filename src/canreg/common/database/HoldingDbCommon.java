package canreg.common.database;

import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.server.management.SystemDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common utilities for Holding DB.
 */
public class HoldingDbCommon {

    public static final String RAW_DATA_COLUMN = "RAW_DATA";
    public static final String FORMAT_ERRORS_COLUMN = "FORMAT_ERRORS";

    /**
     * Build a SystemDescription for a holding DB.
     * @param mainSystemDescription the main SystemDescription
     * @return new SystemDescription with additional variables
     */
    public static SystemDescription buildSystemDescriptionForHoldingDB(SystemDescription mainSystemDescription) {
        SystemDescription systemDescription = new SystemDescription(mainSystemDescription.getDescriptionFilePath());
        HoldingDbCommon.addVariablesForImportToHoldingDB(systemDescription);
        return systemDescription;        
    }
    
    /**
     * Add the 2 variables for the holding DB: "Format Errors" and "Raw Data"
     * @param systemDescription the current system description
     * @return list of new variables
     */
    public static List<DatabaseVariablesListElement> addVariablesForImportToHoldingDB(SystemDescription systemDescription) {
        List<DatabaseVariablesListElement> newVariables = new ArrayList<>();
        DatabaseGroupsListElement defaultGroup = systemDescription.getDatabaseGroupsListElements()[1];
        int lastVariableId = systemDescription.getDatabaseVariableListElements().length - 1;

        //Two new variables are added for the holding DB: "Format Errors" and "Raw Data"    
        //First we add the "Fromat Errors". This is the penultimate column in the csv file. This columns
        //indicates which are the columns that have format errors.
        DatabaseVariablesListElement patientFormatErrorsVar = createFormatErrorsVariable(Globals.PATIENT_TABLE_NAME,
                defaultGroup,
                ++lastVariableId);
        newVariables.add(patientFormatErrorsVar);

        //The "Raw Data" columns contains the full content for that record. Useful so the user can
        //revise format errors but without the forced changes of the data entry GUI.
        DatabaseVariablesListElement patientRawDataVar = createRawDataVariable(Globals.PATIENT_TABLE_NAME,
                defaultGroup,
                ++lastVariableId);
        newVariables.add(patientRawDataVar);

        //"Format Errors" and "Raw Data" are also added to the tumour and source tables in case
        //the user input 3 files that don't share data.
        DatabaseVariablesListElement tumourFormatErrorsVar = createFormatErrorsVariable(Globals.TUMOUR_TABLE_NAME,
                defaultGroup,
                ++lastVariableId);
        newVariables.add(tumourFormatErrorsVar);

        DatabaseVariablesListElement tumourRawDataVar = createRawDataVariable(Globals.TUMOUR_TABLE_NAME,
                defaultGroup,
                ++lastVariableId);
        newVariables.add(tumourRawDataVar);

        DatabaseVariablesListElement sourceFormatErrorsVar = createFormatErrorsVariable(Globals.SOURCE_TABLE_NAME,
                defaultGroup,
                ++lastVariableId);
        newVariables.add(sourceFormatErrorsVar);

        DatabaseVariablesListElement sourceRawDataVar = createRawDataVariable(Globals.SOURCE_TABLE_NAME,
                defaultGroup,
                ++lastVariableId);
        newVariables.add(sourceRawDataVar);

        ArrayList<DatabaseVariablesListElement> variables = new ArrayList<>(Arrays.asList(systemDescription.getDatabaseVariableListElements()));
        variables.addAll(newVariables);
        systemDescription.setVariables(variables.toArray(new DatabaseVariablesListElement[variables.size()]));

        return newVariables;
    }

    private static DatabaseVariablesListElement createFormatErrorsVariable(String databaseTableName,
                                                                           DatabaseGroupsListElement group,
                                                                           int variableID) {
        DatabaseVariablesListElement variableFormatErrors = new DatabaseVariablesListElement(databaseTableName,
                1,
                FORMAT_ERRORS_COLUMN,
                Globals.VARIABLE_TYPE_ALPHA_NAME);
        variableFormatErrors.setGroup(group);
        variableFormatErrors.setVariableLength(4000);
        variableFormatErrors.setVariableID(variableID);
        //The next sets() are mandatory, otherwise the saveXml() throws an exception
        variableFormatErrors.setFullName("Format errors from original CSV");
        variableFormatErrors.setEnglishName("Format Errors");
        variableFormatErrors.setFillInStatus("Optional");
        variableFormatErrors.setMultiplePrimaryCopy("Othr");
        variableFormatErrors.setStandardVariableName("");
        variableFormatErrors.setXPos(0);
        variableFormatErrors.setYPos(0);
        return variableFormatErrors;
    }

    private static DatabaseVariablesListElement createRawDataVariable(String databaseTableName,
                                                                      DatabaseGroupsListElement group,
                                                                      int variableID) {
        DatabaseVariablesListElement variableRawData = new DatabaseVariablesListElement(databaseTableName,
                1,
                RAW_DATA_COLUMN,
                Globals.VARIABLE_TYPE_ALPHA_NAME);
        variableRawData.setGroup(group);
        variableRawData.setVariableLength(32000);
        variableRawData.setVariableID(variableID);
        //The next sets() are mandatory, otherwise the saveXml() throws an exception
        variableRawData.setFullName("Raw Data from original CSV");
        variableRawData.setEnglishName("Raw Data");
        variableRawData.setFillInStatus("Optional");
        variableRawData.setMultiplePrimaryCopy("Othr");
        variableRawData.setStandardVariableName("");
        variableRawData.setXPos(0);
        variableRawData.setYPos(0);
        return variableRawData;
    }    
}
