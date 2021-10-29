/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2021  International Agency for Research on Cancer
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
package canreg.client.gui.importers;

import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.database.Patient;
import canreg.common.database.Tools;
import canreg.common.database.Tumour;
import canreg.common.database.Source;
import canreg.common.database.NameSexRecord;
import canreg.common.database.DatabaseRecord;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.client.CanRegClientApp;
import canreg.client.dataentry.Relation;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.server.CanRegRegistryProxy;
import canreg.server.CanRegServerInterface;
import canreg.server.database.*;
import canreg.server.management.SystemDescription;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVRecord;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author ervikm
 */
public class Import {

    private static final String namespace = Globals.NAMESPACE;
    private static final boolean debug = Globals.DEBUG;
    public static String FINISHED = "finished";
    public static String PROGRESS = "progress";
    public static String RECORD = "record";
    public static String PATIENTS = "patients";
    public static String TUMOURS = "tumours";
    public static String SOURCES = "sources";
    public static String R_SCRIPTS = "r_scripts";
    private static final String RAW_DATA_COLUMN = "RAW_DATA";
    private static final String FORMAT_ERRORS_COLUMN = "FORMAT_ERRORS";    

    /**
     *
     * @param task
     * @param doc
     * @param map
     * @param file
     * @param server
     * @param io
     * @return
     * @throws java.sql.SQLException
     * @throws java.rmi.RemoteException
     * @throws canreg.server.database.RecordLockedException
     */
    public static boolean importFile(Task<Object, String> task, Document doc, List<canreg.client.dataentry.Relation> map, 
                                     File file, CanRegServerInterface server, ImportOptions io) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        //public static boolean importFile(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, Document doc, List<canreg.client.dataentry.Relation> map, File file, CanRegServerInterface server, ImportOptions io) throws SQLException, RemoteException, SecurityException, RecordLockedException {
        boolean success = false;

        Set<String> noNeedToLookAtPatientVariables = new TreeSet<String>();

        noNeedToLookAtPatientVariables.add(io.getPatientIDVariableName());
        noNeedToLookAtPatientVariables.add(io.getPatientRecordIDVariableName());

        String firstNameVariableName = io.getFirstNameVariableName();
        String sexVariableName = io.getSexVariableName();

        CSVParser parser = null;
        CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(io.getSeparator());

        int linesToRead = io.getMaxLines();

        HashMap mpCodes = new HashMap();

        int numberOfLinesRead = 0;

        Map<String, Integer> nameSexTable = server.getNameSexTables();

        try {
//            FileInputStream fis = new FileInputStream(file);
            //           BufferedReader bsr = new BufferedReader(new InputStreamReader(fis, io.getFileCharset()));

            // Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}");
            int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(file.getAbsolutePath());

            if (linesToRead > 0) {
                linesToRead = Math.min(numberOfRecordsInFile, linesToRead);
            } else {
                linesToRead = numberOfRecordsInFile;
            }

            parser = CSVParser.parse(file, io.getFileCharset(), format);

            for (CSVRecord csvRecord : parser) {
                numberOfLinesRead++;
                // We allow for null tasks...
                boolean needToSavePatientAgain = true;
                int patientDatabaseRecordID = -1;

                if (task != null) {
                    task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
                }

                // Build patient part
                Patient patient = new Patient();
                for (int i = 0; i < map.size(); i++) {
                    Relation rel = map.get(i);
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("patient")) {
                        if (rel.getFileColumnNumber() < csvRecord.size()) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        patient.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Number format error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
                                        success = false;
                                    }
                                }
                            } else {
                                patient.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Something wrong with patient part of line " + numberOfLinesRead + ".", new Exception("Error in line: " + numberOfLinesRead + ". Can't find field: " + rel.getDatabaseVariableName()));
                        }
                    }
                }
                // debugOut(patient.toString());

                // Build tumour part
                Tumour tumour = new Tumour();
                for (canreg.client.dataentry.Relation rel : map) {
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("tumour")) {
                        if (rel.getFileColumnNumber() < csvRecord.size()) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Number format error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
                                        success = false;
                                    }
                                }
                            } else {
                                tumour.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Something wrong with tumour part of line " + numberOfLinesRead + ".", new Exception("Error in line: " + numberOfLinesRead + ". Can't find field: " + rel.getDatabaseVariableName()));
                        }
                    }
                }

                // Build source part
                Set<Source> sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
                Source source = new Source();
                for (canreg.client.dataentry.Relation rel : map) {
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                        if (rel.getFileColumnNumber() < csvRecord.size()) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        source.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Number format error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
                                        success = false;
                                    }
                                }
                            } else {
                                source.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Something wrong with source part of line " + numberOfLinesRead + ".", new Exception("Error in line: " + numberOfLinesRead + ". Can't find field: " + rel.getDatabaseVariableName()));
                        }

                    }
                }
                sources.add(source);
                tumour.setSources(sources);

                // debugOut(tumour.toString());
                // add patient to the database
                Object patientID = patient.getVariable(io.getPatientIDVariableName());
                Object patientRecordID = patient.getVariable(io.getPatientRecordIDVariableName());

                if (patientID == null) {
                    // save the record to get the new patientID;
                    patientDatabaseRecordID = server.savePatient(patient);
                    
                    //We can put CanRegServerInterface parameter as null because the lock is false
                    patient = (Patient) server.getRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME, false, null);
                    patientID = patient.getVariable(io.getPatientIDVariableName());
                    patientRecordID = patient.getVariable(io.getPatientRecordIDVariableName());
                }

                if (io.isDataFromPreviousCanReg()) {
                    // set update date for the patient the same as for the tumour
                    Object updateDate = tumour.getVariable(io.getTumourUpdateDateVariableName());
                    patient.setVariable(io.getPatientUpdateDateVariableName(), updateDate);

                    // Set the patientID the same as the tumourID initially
                    // Object tumourSequence = tumour.getVariable(io.getTumourSequenceVariableName());
                    Object tumourSequence = "1";

                    String tumourSequenceString = tumourSequence + "";
                    while (tumourSequenceString.length() < Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD) {
                        tumourSequenceString = "0" + tumourSequenceString;
                    }
                    patientRecordID = patientID + "" + tumourSequenceString;

                    // If this is a multiple primary tumour...
                    String mpCodeString = (String) tumour.getVariable(io.getMultiplePrimaryVariableName());
                    if (mpCodeString != null && mpCodeString.length() > 0) {
                        patientID = lookUpPatientID(mpCodeString, patientID, mpCodes);

                        // rebuild sequenceNumber
                        Tumour[] tumours = new Tumour[0];
                        try {
                            tumours = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID(patientID + "", false, server);
                        } catch (DistributedTableDescriptionException | UnknownTableException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                            new TechnicalError().errorDialog();
                        }

                        tumourSequenceString = (tumours.length + 1) + "";
                        while (tumourSequenceString.length() < Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD) {
                            tumourSequenceString = "0" + tumourSequenceString;
                        }

                        patientRecordID = patientID + "" + tumourSequenceString;
                        Patient[] oldPatients = null;
                        try {
                            oldPatients = CanRegClientApp.getApplication().getPatientsByPatientID((String) patientID, false, server);
                        } catch (RemoteException | SecurityException | UnknownTableException | RecordLockedException | SQLException | DistributedTableDescriptionException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                            new TechnicalError().errorDialog();
                        }
                        for (Patient oldPatient : oldPatients) {
                            if (!Tools.newRecordContainsNewInfo(patient, oldPatient, noNeedToLookAtPatientVariables)) {
                                needToSavePatientAgain = false;
                                patient = oldPatient;
                                patientRecordID = oldPatient.getVariable(io.getPatientRecordIDVariableName());
                            }
                        }
                    }

                    Object tumourID = patientRecordID + "" + tumourSequenceString;
                    //
                    patient.setVariable(io.getPatientIDVariableName(), patientID);
                    tumour.setVariable(io.getTumourIDVariablename(), tumourID);
                    // And store the record ID

                    patient.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);

                    // Set the patient ID number on the tumour
                    tumour.setVariable(io.getPatientIDTumourTableVariableName(), patientID);
                    tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);

                    // Set the deprecated flag to 0 - no obsolete records from CR4
                    tumour.setVariable(io.getObsoleteTumourFlagVariableName(), "0");
                    patient.setVariable(io.getObsoletePatientFlagVariableName(), "0");

                }

                // Set the name in the firstName database
                String sex = (String) patient.getVariable(sexVariableName);
                if (sex != null && sex.length() > 0) {
                    Integer sexCode = Integer.parseInt(sex);
                    String firstNames = (String) patient.getVariable(firstNameVariableName);
                    if (firstNames != null) {
                        String[] firstNamesArray = firstNames.split(" ");
                        for (String firstName : firstNamesArray) {
                            if (firstName != null && firstName.trim().length() > 0) {
                                // here we use the locale specific toUpperCase
                                Integer registeredSexCode = nameSexTable.get(firstName);
                                if (registeredSexCode == null) {
                                    NameSexRecord nsr = new NameSexRecord();
                                    nsr.setName(firstName);
                                    nsr.setSex(sexCode);

                                    server.saveNameSexRecord(nsr, false);

                                    nameSexTable.put(firstName, sexCode);
                                } else if (!Objects.equals(registeredSexCode, sexCode)) {
                                    if (registeredSexCode != 9) {
                                        sexCode = 9;
                                        NameSexRecord nsr = new NameSexRecord();
                                        nsr.setName(firstName);
                                        nsr.setSex(sexCode);
                                        server.saveNameSexRecord(nsr, true);
                                        nameSexTable.remove(firstName);
                                        nameSexTable.put(firstName, sexCode);
                                    }
                                }
                            }
                        }
                    }
                }

                if (needToSavePatientAgain) {
                    if (patientDatabaseRecordID > 0) {
                        server.editPatient(patient);
                    } else {
                        patientDatabaseRecordID = server.savePatient(patient);
                    }
                }
                if (patient != null && tumour != null) {
                    String icd10 = (String) tumour.getVariable(io.getICD10VariableName());
                    if (icd10 == null || icd10.trim().length() == 0) {
                        ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                        tumour.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());
                    }
                    // check if iccc variable is defined
                    if (io.getICCCVariableName() != null) {
                        String iccc = (String) tumour.getVariable(io.getICCCVariableName());
                        if (iccc == null || iccc.trim().length() == 0) {
                            ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumour);
                            tumour.setVariable(io.getICCCVariableName(), conversionResult[0].getValue());
                        }
                    }
                }
                if (tumour.getVariable(io.getPatientIDTumourTableVariableName()) == null) {
                    tumour.setVariable(io.getPatientIDTumourTableVariableName(), patientID);
                }

                if (tumour.getVariable(io.getPatientRecordIDTumourTableVariableName()) == null) {
                    tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                }

                int tumourDatabaseIDNumber = server.saveTumour(tumour);

                if (Thread.interrupted()) {
                    //We've been interrupted: no more importing.
                    throw new InterruptedException();
                }
            }
            if (task != null) {
                task.firePropertyChange("finished", null, null);
            }
            success = true;
        } catch (IOException | NumberFormatException | IndexOutOfBoundsException | SQLException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } catch (InterruptedException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Interupted on line: " + (numberOfLinesRead + 1) + ". ", ex);
            success = true;
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
        }

        return success;
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
    
    private static Relation createRelation(DatabaseVariablesListElement variable, 
                                           int variableID, 
                                           int columnIndexInFile, 
                                           String variableNameInFile) {
        Relation relation = new Relation();
        relation.setDatabaseTableName(variable.getDatabaseTableName());
        relation.setDatabaseTableVariableID(variableID);
        relation.setDatabaseVariableName(variable.getDatabaseVariableName());
        relation.setFileColumnNumber(columnIndexInFile);
        relation.setFileVariableName(variableNameInFile);
        relation.setVariableType(variable.getVariableType());
        return relation;
    }
    
    private static int getAmountOfColumns(File[] files, ImportOptions io, int index) throws IOException {
        if(files[index] != null)
            return canreg.common.Tools.getAmountOfColumnsInCSV(files[index], 
                                                               io.getSeparators()[index],
                                                               io.getFileCharsets()[index]);
        return 0;
    }
    
    public static boolean importFilesIntoHoldingDB(Task<Object, Void> task, Document originalDBDescription, 
                                      List<canreg.client.dataentry.Relation> map, File[] files, 
                                      CanRegServerInterface server, ImportOptions io)
            throws SQLException, RemoteException, SecurityException, URISyntaxException, 
                   IOException, RecordLockedException, UnknownTableException, 
                   DistributedTableDescriptionException, Exception {        
        int patientAmountOfColumns = getAmountOfColumns(files, io, 0);
        int tumourAmountOfColumns = getAmountOfColumns(files, io, 1);
        int sourceAmountOfColumns = getAmountOfColumns(files, io, 2);
        
        SystemDescription systemDescription = new SystemDescription(Paths.get(new URI(originalDBDescription.getDocumentURI())).toFile().getAbsolutePath());
        DatabaseGroupsListElement defaultGroup = systemDescription.getDatabaseGroupsListElements()[1];
        int lastVariableId = systemDescription.getDatabaseVariableListElements().length - 1;
        
        //Two new variables are added for the holding DB: "Format Errors" and "Raw Data"    
        //First we add the "Fromat Errors". This is the penultimate column in the csv file. This columns
        //indicates which are the columns that have format errors.
        DatabaseVariablesListElement patientFormatErrorsVar = createFormatErrorsVariable(Globals.PATIENT_TABLE_NAME, 
                                                                                       defaultGroup, 
                                                                                       ++lastVariableId);
        Relation patientFormatErrorsRel = createRelation(patientFormatErrorsVar, lastVariableId, (patientAmountOfColumns - 2), "format.errors");
        map.add(patientFormatErrorsRel);
        
        //The "Raw Data" columns contains the full content for that record. Useful so the user can
        //revise format errors but without the forced changes of the data entry GUI.
        DatabaseVariablesListElement patientRawDataVar = createRawDataVariable(Globals.PATIENT_TABLE_NAME, 
                                                                             defaultGroup, 
                                                                             ++lastVariableId);
        Relation patientRawDataRel = createRelation(patientRawDataVar, lastVariableId, (patientAmountOfColumns - 1), "all.raw.data");
        map.add(patientRawDataRel);
        
        
        //"Format Errors" and "Raw Data" are also added to the tumour and source tables in case
        //the user input 3 files that don't share data.
        DatabaseVariablesListElement tumourFormatErrorsVar = createFormatErrorsVariable(Globals.TUMOUR_TABLE_NAME, 
                                                                                       defaultGroup, 
                                                                                       ++lastVariableId);
        Relation tumourFormatErrorsRel = createRelation(tumourFormatErrorsVar, lastVariableId, (tumourAmountOfColumns - 2), "format.errors");
        map.add(tumourFormatErrorsRel);
        
        DatabaseVariablesListElement tumourRawDataVar = createRawDataVariable(Globals.TUMOUR_TABLE_NAME, 
                                                                             defaultGroup, 
                                                                             ++lastVariableId);
        Relation tumourRawDataRel = createRelation(tumourRawDataVar, lastVariableId, (tumourAmountOfColumns - 1), "all.raw.data");
        map.add(tumourRawDataRel);
        
        DatabaseVariablesListElement sourceFormatErrorsVar = createFormatErrorsVariable(Globals.SOURCE_TABLE_NAME, 
                                                                                       defaultGroup, 
                                                                                       ++lastVariableId);
        Relation sourceFormatErrorsRel = createRelation(sourceFormatErrorsVar, lastVariableId, (sourceAmountOfColumns - 2), "format.errors");
        map.add(sourceFormatErrorsRel);
        
        DatabaseVariablesListElement sourceRawDataVar = createRawDataVariable(Globals.SOURCE_TABLE_NAME, 
                                                                             defaultGroup, 
                                                                             ++lastVariableId);
        Relation sourceRawDataRel = createRelation(sourceRawDataVar, lastVariableId, (sourceAmountOfColumns - 1), "all.raw.data");
        map.add(sourceRawDataRel);
        
        ArrayList<DatabaseVariablesListElement> variables = new ArrayList<>(Arrays.asList(systemDescription.getDatabaseVariableListElements()));
        variables.add(patientFormatErrorsVar);
        variables.add(patientRawDataVar);
        variables.add(tumourFormatErrorsVar);
        variables.add(tumourRawDataVar);
        variables.add(sourceFormatErrorsVar);
        variables.add(sourceRawDataVar);
        systemDescription.setVariables(variables.toArray(new DatabaseVariablesListElement[variables.size()]));
        
        //Creation of XML of Holding DB        
        String registryCode = server.getCanRegRegistryCode();
        systemDescription = server.createNewHoldingDB(registryCode, systemDescription);

        CanRegServerInterface holdingProxy = ((CanRegRegistryProxy) server).getInstanceForHoldingDB(systemDescription.getRegistryCode());

        return importFiles(task, map, files, holdingProxy, io, true, originalDBDescription);
    }    

    public static boolean importFiles(Task<Object, Void> task, 
                                      List<canreg.client.dataentry.Relation> map, File[] files, 
                                      CanRegServerInterface server, ImportOptions io, 
                                      boolean intoHoldingDB, Document doc) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, Exception {
        
        GlobalToolBox globalToolBox = new GlobalToolBox(doc);
        
        int numberOfLinesRead = 0;
        Writer reportWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        if (io.getReportFileName() != null && io.getReportFileName().trim().length() > 0) {
            try {
                reportWriter = new BufferedWriter(new FileWriter(io.getReportFileName()));
            } catch (IOException ex) {
                Logger.getLogger(Import.class.getName()).log(Level.WARNING, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        boolean success = false;
        Set<String> noNeedToLookAtPatientVariables = new TreeSet<String>();
        noNeedToLookAtPatientVariables.add(canreg.common.Tools.toLowerCaseStandardized(io.getPatientIDVariableName()));
        noNeedToLookAtPatientVariables.add(canreg.common.Tools.toLowerCaseStandardized(io.getPatientRecordIDVariableName()));

        ResultCode worstResultCodeFound;
        CSVParser parser = null;
        CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(io.getSeparators()[0]);

        int linesToRead = io.getMaxLines();
        FileInputStream patientFIS = null, tumourFIS = null, sourceFIS = null;

        try {
            // first we get the patients
            if (task != null) {
                task.firePropertyChange(PROGRESS, 0, 0);
                task.firePropertyChange(PATIENTS, 0, 0);
            }
            if (files[0] != null) {
                reportWriter.write("Starting to import patients from " + files[0].getAbsolutePath() + Globals.newline);
                patientFIS = new FileInputStream(files[0]);
                InputStreamReader patientISR = new InputStreamReader(patientFIS, io.getFileCharsets()[0]);

                Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", patientISR.getEncoding());

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[0].getAbsolutePath());

                numberOfLinesRead = 0;

                if (linesToRead > 0) {
                    linesToRead = Math.min(numberOfRecordsInFile, linesToRead);
                } else {
                    linesToRead = numberOfRecordsInFile;
                }
                parser = CSVParser.parse(files[0], io.getFileCharsets()[0], format);

                for (CSVRecord csvRecord : parser) { 
                    // We allow for null tasks...
                    boolean savePatient = true;
//                    boolean deletePatient = false;
//                    int oldPatientDatabaseRecordID = -1;

                    if (task != null) {
                        task.firePropertyChange(PROGRESS, ((numberOfLinesRead - 1) * 100 / linesToRead) / 3, ((numberOfLinesRead) * 100 / linesToRead) / 3);
                        task.firePropertyChange(PATIENTS, ((numberOfLinesRead - 1) * 100 / linesToRead), ((numberOfLinesRead) * 100 / linesToRead));
                    }

                    // Build patient part
                    Patient patient = new Patient();
                    for (int i = 0; i < map.size(); i++) {
                        Relation rel = map.get(i);
                        if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("patient")) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        patient.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Number format error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
                                        success = false;
                                    }
                                }
                            } else {
                                String value = csvRecord.get(rel.getFileColumnNumber());
                                if(rel.getDatabaseVariableName().equalsIgnoreCase(RAW_DATA_COLUMN))
                                    value = value.replace("@#$", " <br>");
                                patient.setVariable(rel.getDatabaseVariableName(), value);
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }
                    // debugOut(patient.toString());

                    Object patientRecordID = patient.getVariable(io.getPatientRecordIDVariableName());
                    
                    importPatient(server, io.getDiscrepancies(), (String) patientRecordID, 
                                  patient, reportWriter, 
                                  intoHoldingDB, io.isTestOnly(), false);                                       

                    if (task != null) {
                        task.firePropertyChange(RECORD, 100, 75);
                    }

                    numberOfLinesRead++;

                    if (Thread.interrupted()) {
                        //We've been interrupted: no more importing.
                        reportWriter.flush();
                        throw new InterruptedException();
                    }
                }
                parser.close();
                reportWriter.write("Finished reading patients." + Globals.newline + Globals.newline);
                reportWriter.flush();
            }
            if (task != null) {
                task.firePropertyChange(PATIENTS, 100, 100);
                task.firePropertyChange("progress", 33, 34);
            }

            // then we get the tumours            
            if (task != null) {
                task.firePropertyChange(TUMOURS, 0, 0);
            }

            if (files[1] != null) {
                reportWriter.write("Starting to import tumours from " + files[1].getAbsolutePath() + Globals.newline);

                tumourFIS = new FileInputStream(files[1]);
                InputStreamReader tumourISR = new InputStreamReader(tumourFIS, io.getFileCharsets()[1]);

                Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", tumourISR.getEncoding());

                numberOfLinesRead = 0;

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[1].getAbsolutePath());

                if (linesToRead > 0) {
                    linesToRead = Math.min(numberOfRecordsInFile, linesToRead);
                } else {
                    linesToRead = numberOfRecordsInFile;
                }

                format = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withDelimiter(io.getSeparators()[1]);
                parser = CSVParser.parse(files[1], io.getFileCharsets()[1], format);

                for (CSVRecord csvRecord : parser) {    
                    if (task != null) {
                        task.firePropertyChange(PROGRESS, 33 + ((numberOfLinesRead - 1) * 100 / linesToRead) / 3, 33 + ((numberOfLinesRead) * 100 / linesToRead) / 3);
                        task.firePropertyChange(TUMOURS, ((numberOfLinesRead - 1) * 100 / linesToRead), ((numberOfLinesRead) * 100 / linesToRead));
                    }
                    // Build tumour part
                    Tumour tumour = new Tumour();
                    for (int i = 0; i < map.size(); i++) {
                        Relation rel = map.get(i);
                        if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("tumour")) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Number format error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
                                        success = false;
                                    }
                                }
                            } else {
                                String value = csvRecord.get(rel.getFileColumnNumber());
                                if(rel.getDatabaseVariableName().equalsIgnoreCase(RAW_DATA_COLUMN))
                                    value = value.replace("@#$", " <br>");
                                tumour.setVariable(rel.getDatabaseVariableName(), value);
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    } 

                    String tumourID = (String) tumour.getVariable(io.getTumourIDVariablename());
                    String patientRecordID = (String) tumour.getVariable(io.getPatientRecordIDTumourTableVariableName());
                    
                    //patientRecordID IS A MUST if the tumour is new, because without that
                    //the new tumourID cannot be calculated!!!
                    if(patientRecordID == null || patientRecordID.isEmpty()) {
                        String patientID = tumour.getVariableAsString(io.getPatientIDTumourTableVariableName());
                        Patient[] patients = CanRegClientApp.getApplication().getPatientsByPatientID(patientID, false, server);
                        patientRecordID = patients[0].getVariableAsString(io.getPatientRecordIDVariableName());
                        
                        //This should not happen, but... this is our last chance to save the procedure.
                        if(patientRecordID == null || patientRecordID.isEmpty())
                            patientRecordID = patients[0].getVariableAsString(io.getTumourIDVariablename());
                        
                        tumour.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);
                        tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                    }
                    
                    importTumour(server, io.getDiscrepancies(), tumourID, patientRecordID, 
                                 tumour, io, reportWriter, intoHoldingDB, io.isTestOnly(), false);
                    
                    if (task != null) {
                        task.firePropertyChange(RECORD, 75, 100);
                    }
                    //Read next line of data
                    numberOfLinesRead++;

                    if (Thread.interrupted()) {
                        //We've been interrupted: no more importing.
                        reportWriter.flush();
                        throw new InterruptedException();
                    }
                }
                parser.close();
                reportWriter.write("Finished reading tumours." + Globals.newline + Globals.newline);
                reportWriter.flush();
            }
            if (task != null) {
                task.firePropertyChange(TUMOURS, 100, 100);
            }
            // then at last we get the sources
            if (task != null) {
                task.firePropertyChange(SOURCES, 0, 0);
                task.firePropertyChange(PROGRESS, 66, 66);
            }
            if (files[2] != null) {
                reportWriter.write("Starting to import sources from " + files[2].getAbsolutePath() + Globals.newline);

                sourceFIS = new FileInputStream(files[2]);
                InputStreamReader sourceISR = new InputStreamReader(sourceFIS, io.getFileCharsets()[2]);

                Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", sourceISR.getEncoding());

                numberOfLinesRead = 0;

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[2].getAbsolutePath());

                if (linesToRead > 0) {
                    linesToRead = Math.min(numberOfRecordsInFile, linesToRead);
                } else {
                    linesToRead = numberOfRecordsInFile;
                }

                format = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withDelimiter(io.getSeparators()[2]);
                parser = CSVParser.parse(files[2], io.getFileCharsets()[2], format);

                for (CSVRecord csvRecord : parser) {   
                    // We allow for null tasks...
                    if (task != null) {
                        task.firePropertyChange(PROGRESS, 67 + ((numberOfLinesRead - 1) * 100 / linesToRead) / 3, 67 + ((numberOfLinesRead) * 100 / linesToRead) / 3);
                        task.firePropertyChange(SOURCES, ((numberOfLinesRead - 1) * 100 / linesToRead), ((numberOfLinesRead) * 100 / linesToRead));
                    }

                    // Build source part
                    Source source = new Source();
                    for (int i = 0; i < map.size(); i++) {
                        Relation rel = map.get(i);
                        if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        source.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Number format error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
                                        success = false;
                                    }
                                }
                            } else {
                                String value = csvRecord.get(rel.getFileColumnNumber());
                                if(rel.getDatabaseVariableName().equalsIgnoreCase(RAW_DATA_COLUMN))
                                    value = value.replace("@#$", " <br>");
                                source.setVariable(rel.getDatabaseVariableName(), value);
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }

                    String tumourID = (String) source.getVariable(io.getTumourIDSourceTableVariableName());                    
                    String sourceID = (String) source.getVariable(io.getSourceIDVariablename());
                    
                    importSource(server, io.getDiscrepancies(), sourceID, io.getSourceIDVariablename(), 
                                 source, tumourID, reportWriter, intoHoldingDB, io.isTestOnly(), false);
                                        
                    if (task != null) {
                        task.firePropertyChange(RECORD, 75, 100);
                    }

                    numberOfLinesRead++;

                    if (Thread.interrupted()) {
                        //We've been interrupted: no more importing.
                        reportWriter.flush();
                        throw new InterruptedException();
                    }
                }
                reportWriter.write("Finished reading sources." + Globals.newline + Globals.newline);
                reportWriter.flush();
                parser.close();                
            }
            if (task != null) {
                task.firePropertyChange(SOURCES, 100, 100);
                task.firePropertyChange(PROGRESS, 100, 100);
                while (!task.isProgressPropertyValid()) {
                    // wait untill progress has been updated...
                }
            }
            reportWriter.write("Finished" + Globals.newline);
            reportWriter.flush();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } catch (InterruptedException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Interupted on line: " + (numberOfLinesRead + 1) + ". ", ex);
            success = true;
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "String too short error in line: " + (numberOfLinesRead + 1 + 1) + ". ",
                    ex);
            success = false;
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
            try {
                reportWriter.flush();
                reportWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
            
            files = null;
            if(patientFIS != null)
                patientFIS.close();
            if(tumourFIS != null)
                tumourFIS.close();
            if(sourceFIS != null)
                sourceFIS.close();
        }
        if (task != null) {
            task.firePropertyChange(PROGRESS, 100, 100);
            task.firePropertyChange("finished", null, null);
        }
                
        return success;
    }

    public static int importPatient(CanRegServerInterface server, int discrepancyOption,
                                     String patientRecordID, Patient patientToImport, Writer reportWriter, 
                                     boolean intoHoldingDB, boolean isTestOnly, 
                                     boolean fromHoldingToProduction)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException,
                   IOException, Exception {
        GlobalToolBox globalToolBox = new GlobalToolBox(server.getDatabseDescription());
        Patient oldPatientRecord = null;
        try {
            oldPatientRecord = CanRegClientApp.getApplication().getPatientRecord(patientRecordID, false, server);
        } catch(NullPointerException ex1) {
            //Patient not found in DB.
            Logger.getLogger(Import.class.getName()).log(Level.WARNING,"Patient not found in DB" , ex1);
        }
        /*catch (Exception ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } */

        boolean savePatient = true;
        if (oldPatientRecord != null) {
            if(intoHoldingDB) {
                savePatient = false;
            } else {
                // deal with discrepancies                        
                switch (discrepancyOption) {
                    case ImportOptions.REJECT:
                        savePatient = false;
                        break;
                    case ImportOptions.UPDATE:
                        String updateReport = updateRecord(oldPatientRecord, patientToImport);
                        if (updateReport.length() > 0) {
                            reportWriter.write("Patient " + patientRecordID + Globals.newline + updateReport);
                        }
    //                                oldPatientDatabaseRecordID = (Integer) oldPatientRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                        patientToImport = oldPatientRecord;
                        savePatient = true;
                        break;
                    case ImportOptions.OVERWRITE:
    //                                oldPatientDatabaseRecordID = (Integer) oldPatientRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                        String overWriteReport = overwriteRecord(oldPatientRecord, patientToImport);
                        if (overWriteReport.length() > 0) {
                            reportWriter.write("Patient " + patientRecordID + Globals.newline + overWriteReport);
                        }
                        patientToImport = oldPatientRecord;
                        savePatient = true;
                        break;
                }
            }
        } 

//      if (deletePatient) {
//           server.deleteRecord(oldPatientDatabaseRecordID, Globals.PATIENT_TABLE_NAME);
//      }

        if ( ! isTestOnly) {
            if (savePatient) {
                if (patientToImport.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
    //              try {
                        if(fromHoldingToProduction) {
                            if(patientToImport.getVariableAsString(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME_FOR_HOLDING).contains("@H")) {
                                DatabaseVariablesListElement patientIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
                                DatabaseVariablesListElement patientRecordIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString());
                                patientToImport.setVariable(patientIDVariable.getDatabaseVariableName(), "");
                                patientToImport.setVariable(patientRecordIDVariable.getDatabaseVariableName(), "");
                                return server.savePatient(patientToImport);
                            } else
                                server.editPatientFromHoldingToProduction(patientToImport);
                        }
                        else
                            server.editPatient(patientToImport);
    //              } catch(Exception ex) {
    //                  Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR EDITING PATIENT " + patientID, ex);
    //              }
                } else {
    //              try {
                        return server.savePatient(patientToImport);
    //              } catch(Exception ex) {
    //                  Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR SAVING PATIENT " + patientID, ex);
    //              }
                }
            }
        }
        
        return -1;
    }
    
    
    public static void importTumour(CanRegServerInterface server, int discrepancyOption,
                                    String tumourID, String patientRecordID, Tumour tumourToImport, 
                                    ImportOptions io, Writer reportWriter, 
                                    boolean intoHoldingDB, boolean isTestOnly,
                                    boolean fromHoldingToProduction)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException, 
                   IOException, Exception {
        // see if this tumour exists in the database already
        // TODO: Implement this using arrays and getTumourRexords instead
        boolean saveTumour = true;
        boolean deleteTumour = false;
        
        ResultCode worstResultCodeFound;
        Tumour oldTumourRecord = null;
        
        try {
            oldTumourRecord = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(tumourID, false, server);
        } catch(NullPointerException ex1) {
            //Patient not found in DB.
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Patient not found in DB", ex1);
        }
        /*catch (Exception ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } */

        if (oldTumourRecord != null) {
            if(intoHoldingDB) {
                //Tumour is already present in holding DB, if we try to save an exception will rise
                saveTumour = false;
            } else {
                // deal with discrepancies
                switch (discrepancyOption) {
                    case ImportOptions.REJECT:
                        saveTumour = false;
                        break;
                    case ImportOptions.UPDATE:
                        String updateReport = updateRecord(oldTumourRecord, tumourToImport);
                        if (updateReport.length() > 0) {
                            reportWriter.write(tumourID + Globals.newline + updateReport);
                        }
                        tumourToImport = oldTumourRecord;
                        saveTumour = true;
                        break;
                    case ImportOptions.OVERWRITE:
                        // deleteTumour;
                        deleteTumour = true;
                        saveTumour = true;
                        break;
                }
                // reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "Tumour already exists.\n");
            }
        }

        Patient patient = null;
        try {
            patient = CanRegClientApp.getApplication().getPatientRecord(patientRecordID, false, server);
        } catch(NullPointerException ex1) {
            //Patient not found in DB.
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Patient not found in DB", ex1);
        }
        /*catch (Exception ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } */

                                
        if (patient != null && io != null) {
            if (io.isDoChecks() && saveTumour) {
                // run the edits...
                String message = "";
                LinkedList<CheckResult> checkResults = canreg.client.CanRegClientApp.getApplication().performChecks(patient, tumourToImport);

                Map<Globals.StandardVariableNames, CheckResult.ResultCode> mapOfVariablesAndWorstResultCodes 
                        = new EnumMap<Globals.StandardVariableNames, CheckResult.ResultCode>(Globals.StandardVariableNames.class);
                worstResultCodeFound = CheckResult.ResultCode.OK;
                for (CheckResult result : checkResults) {
                    if (result.getResultCode() != CheckResult.ResultCode.OK && result.getResultCode() != CheckResult.ResultCode.NotDone) {
                        if (!result.getResultCode().equals(CheckResult.ResultCode.Missing)) {
                            message += result + "\t";
                            worstResultCodeFound = CheckResult.decideWorstResultCode(result.getResultCode(), worstResultCodeFound);
                            for (Globals.StandardVariableNames standardVariableName : result.getVariablesInvolved()) {
                                CheckResult.ResultCode worstResultCodeFoundForThisVariable = mapOfVariablesAndWorstResultCodes.get(standardVariableName);
                                if (worstResultCodeFoundForThisVariable == null) {
                                    mapOfVariablesAndWorstResultCodes.put(standardVariableName, result.getResultCode());
                                } else if (CheckResult.compareResultSets(result.getResultCode(), worstResultCodeFoundForThisVariable) > 0) {
                                    mapOfVariablesAndWorstResultCodes.put(standardVariableName, result.getResultCode());
                                }
                            }
                        }
                    }
                    // Logger.getLogger(Import.class.getName()).log(Level.INFO, result.toString());
                }
                // always generate ICD10...
                // ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                // tumour.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());

                if (worstResultCodeFound != CheckResult.ResultCode.Invalid && worstResultCodeFound != CheckResult.ResultCode.Missing) {
                    // generate ICD10 codes
                    ConversionResult[] conversionResult = 
                            canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumourToImport);
                    tumourToImport.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());
                    // generate ICCC codes
                    if (io.getICCCVariableName() != null) {
                        String iccc = (String) tumourToImport.getVariable(io.getICCCVariableName());
                        if (iccc == null || iccc.trim().length() == 0) {
                            ConversionResult[] conversionResultICCC = 
                                    canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumourToImport);
                            tumourToImport.setVariable(io.getICCCVariableName(), conversionResultICCC[0].getValue());
                        }
                    }
                } else {
                    tumourToImport.setVariable(io.getTumourRecordStatus(), "0");
                }

                if (worstResultCodeFound == CheckResult.ResultCode.OK) {
                    // message += "Cross-check conclusion: Valid";
                } else {
                    reportWriter.write(tumourID + "\t" + message + Globals.newline);
                    // System.out.println(tumour.getVariable(io.getTumourIDVariablename()) + " " + message);
                }
                tumourToImport.setVariable(io.getTumourCheckStatus(), CheckResult.toDatabaseVariable(worstResultCodeFound));

            } else {
                // try to generate ICD10, if missing, anyway
                String icd10 = (String) tumourToImport.getVariable(io.getICD10VariableName());
                if (icd10 == null || icd10.trim().length() == 0) {
                    ConversionResult[] conversionResult = 
                            canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumourToImport);
                    tumourToImport.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());
                }
                // try to generate ICCC3, if missing, anyway
                String iccc = (String) tumourToImport.getVariable(io.getICCCVariableName());
                if (iccc == null || iccc.trim().length() == 0) {
                    ConversionResult[] conversionResult = 
                            canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumourToImport);
                    tumourToImport.setVariable(io.getICCCVariableName(), conversionResult[0].getValue());
                }
            }
        } else {
            reportWriter.write(tumourID + "\t" + "No patient matches this Tumour." + Globals.newline);
            if(io != null) {
                tumourToImport.setVariable(io.getTumourRecordStatus(), "0");
                tumourToImport.setVariable(io.getTumourCheckStatus(), CheckResult.toDatabaseVariable(ResultCode.Missing));
            }
        }

        if ( ! isTestOnly) {
            if (deleteTumour && oldTumourRecord != null) {
//                THIS FAILS because the implementation of deleteRecord() is trying to delete
//                a tumour without deleting sources, so a DerbySQLIntegrityConstraintViolationException arises.
//                Nevertheless the execution continues after this exception =)
//                server.deleteRecord((Integer) oldTumourRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME), Globals.TUMOUR_TABLE_NAME);
            }
            if (saveTumour) {
                // if tumour has record ID we edit it
                if (tumourToImport.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) != null &&
                    ! tumourToImport.getVariableAsString(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME_FOR_HOLDING).isEmpty()) {
//                  try {
                        if(fromHoldingToProduction) {
                            if(tumourToImport.getVariableAsString(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME_FOR_HOLDING).isEmpty()) {
                                server.saveTumour(tumourToImport);
                            } else
                                server.editTumourFromHoldingToProduction(tumourToImport);
                        }
                        else
                            server.editTumour(tumourToImport);
//                  } catch(Exception ex) {
//                      Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR EDITING TUMOUR " + tumourID, ex);
//                  }
                } // if not we save it
                else {
//                  try {
                        server.saveTumour(tumourToImport);
//                  } catch(Exception ex) {
//                       Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR SAVING TUMOUR " + tumourID, ex);
//                  }
                }
            }
        }
    }
    
    
    public static void importSource(CanRegServerInterface server, int discrepancyOption,
                                    String sourceRecordID, String sourceIDVariablename, 
                                    Source sourceToImport, String tumourID,  
                                    Writer reportWriter, 
                                    boolean intoHoldingDB, boolean isTestOnly, 
                                    boolean fromHoldingToProduction) 
            throws SecurityException, SQLException, RecordLockedException, 
                   DistributedTableDescriptionException, UnknownTableException, RemoteException,
                   IOException, Exception {
        Tumour tumour = null;
        try {
            tumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(tumourID, false, server);
        } catch (NullPointerException ex1) {
            //Patient not found in DB.
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE,"Patient not found in DB", ex1);
        }
        /*catch (Exception ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } */


        boolean addSource = true;

        if (tumour != null) {
            Set<Source> sources = tumour.getSources();
            Iterator<Source> iter = sources.iterator();
            while(iter.hasNext()) {
                Source oldSource = iter.next();
                if (oldSource.getVariable(sourceIDVariablename).equals(sourceRecordID)) {
                    if( ! intoHoldingDB) {
                        // deal with discrepancies
                        switch (discrepancyOption) {
                            case ImportOptions.REJECT:
                                addSource = false;
                                break;
                            case ImportOptions.UPDATE:
                                String updateReport = updateRecord(oldSource, sourceToImport);
                                if (updateReport.length() > 0) {
                                    reportWriter.write("Tumour " + tumourID + Globals.newline + updateReport);
                                }
                                sourceToImport = oldSource;
                                addSource = false;
                                break;
                            case ImportOptions.OVERWRITE:
                                // deleteTumour;
                                iter.remove();
                                addSource = true;
                                break;
                        }
                    } else {
                        addSource = false;
                    }
                }
            }

            if (addSource) {
                sources.add(sourceToImport);
            }
            tumour.setSources(sources);
            if ( ! isTestOnly) {
//              try {
                    if(fromHoldingToProduction)
                        server.editTumourFromHoldingToProduction(tumour);
                    else
                        server.editTumour(tumour);
//              } catch(Exception ex) {
//                  Logger.getLogger(Import.class.getName()).log(Level.SEVERE, 
//                                        "ERROR SAVING SOURCE " + sourceRecordID + 
//                                        " ON TUMOUR " + tumour.getVariable(io.getTumourIDVariablename()),
//                                        ex);
//              }
            }
        } else {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "No tumour with ID " +  tumourID + " was found for source ID " + sourceRecordID);
        }
    }
    
    /**
     *
     * @param doc
     * @param lineElements
     * @return
     */
    public static List<Relation> constructRelations(Document doc, String[] lineElements) {
        LinkedList<Relation> list = new LinkedList();
        NodeList nl = doc.getElementsByTagName(namespace + "variable");
        String[] variableNames = canreg.common.Tools.getVariableNames(doc, namespace);
        for (int i = 0; i < lineElements.length; i++) {
            boolean found = false;
            int j = 0;
            while (!found && j < variableNames.length) {
                found = lineElements[i].equalsIgnoreCase(variableNames[j++]);
            }
            //build relation
            Relation rel = new Relation();
            rel.setFileVariableName(lineElements[i]);
            rel.setFileColumnNumber(i);
            if (found) {
                //backtrack
                j--;
                Element e = (Element) nl.item(j);
                rel.setDatabaseTableName(e.getElementsByTagName(namespace + "table").item(0).getTextContent());
                rel.setDatabaseTableVariableID(Integer.parseInt(e.getElementsByTagName(namespace + "variable_id").item(0).getTextContent()));
                rel.setVariableType(e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent());
                rel.setDatabaseVariableName(variableNames[j]);
            } else {
                rel.setDatabaseTableName("");
                rel.setDatabaseTableVariableID(-1);
                rel.setVariableType("");
                rel.setDatabaseVariableName("");
            }
            list.add(rel);
        }
        return list;
    }

    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(Import.class.getName()).log(Level.INFO, msg);
        }
    }

    private static Object lookUpPatientID(String mpCodeString, Object patientIDNumber, HashMap mpCodes) {
        Object IDNumberObj = mpCodes.get(mpCodeString);
        Object id = patientIDNumber;
        if (IDNumberObj == null) {
            mpCodes.put(mpCodeString, patientIDNumber);
        } else {
            id = IDNumberObj;
        }
        return id;
    }

    private static String updateRecord(DatabaseRecord oldRecord, DatabaseRecord newRecord) {
        String report = "";
        // get all the elements from the newTumour and put them in the oldTumour
        for (String variableName : newRecord.getVariableNames()) {
            Object newData = newRecord.getVariable(variableName);
            if (newData != null) {
                Object oldData = oldRecord.getVariable(variableName);
                oldRecord.setVariable(variableName, newData);
                if (oldData != null) {
                    if (newData.toString().equals(oldData.toString())) {
                        //nothing has changed
                    } else {
                        report += variableName + ": " + oldData + "->" + newData + "\n";
                    }
                } else {
                    report += variableName + ": null ->" + newData + "\n";
                }
            }
        }
        return report;
    }

    private static String overwriteRecord(DatabaseRecord oldRecord, DatabaseRecord newRecord) {
        String report = "";
        // get all the elements from the newTumour and put them in the oldTumour
        for (String variableName : oldRecord.getVariableNames()) {
            if (!variableName.equalsIgnoreCase(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME)) {
                oldRecord.setVariable(variableName, null);
            }
        }
        for (String variableName : newRecord.getVariableNames()) {
            Object newData = newRecord.getVariable(variableName);
            if (newData != null) {
                Object oldData = oldRecord.getVariable(variableName);
                oldRecord.setVariable(variableName, newData);
                if (oldData != null) {
                    if (newData.toString().equals(oldData.toString())) {
                        //nothing has changed
                    } else {
                        report += variableName + ": " + oldData + "->" + newData + "\n";
                    }
                } else {
                    report += variableName + ": null ->" + newData + "\n";
                }
            }
        }
        return report;
    }
}
