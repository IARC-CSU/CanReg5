/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2018  International Agency for Research on Cancer
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

import canreg.common.database.Patient;
import canreg.common.database.Tools;
import canreg.common.database.Tumour;
import canreg.common.database.Source;
import canreg.common.database.NameSexRecord;
import canreg.common.database.DatabaseRecord;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.client.CanRegClientApp;
import canreg.client.dataentry.Relation;
import canreg.common.DatabaseVariablesListElement;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    public static boolean importFile(Task<Object, String> task, Document doc, List<canreg.client.dataentry.Relation> map, File file, CanRegServerInterface server, ImportOptions io) throws SQLException, RemoteException, SecurityException, RecordLockedException {
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
                    patient = (Patient) server.getRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME, false);
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
                        } catch (DistributedTableDescriptionException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnknownTableException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        tumourSequenceString = (tumours.length + 1) + "";
                        while (tumourSequenceString.length() < Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD) {
                            tumourSequenceString = "0" + tumourSequenceString;
                        }

                        patientRecordID = patientID + "" + tumourSequenceString;
                        Patient[] oldPatients = null;
                        try {
                            oldPatients = CanRegClientApp.getApplication().getPatientRecordsByID((String) patientID, false, server);
                        } catch (RemoteException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SecurityException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (DistributedTableDescriptionException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RecordLockedException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnknownTableException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
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
                                } else if (registeredSexCode != sexCode) {
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
                }
            }
        }

        return success;
    }
    
    public static boolean importFilesIntoHoldingDB(Task<Object, Void> task, Document originalDBDescription, 
                                      List<canreg.client.dataentry.Relation> map, File[] files, 
                                      CanRegServerInterface server, ImportOptions io)
            throws SQLException, RemoteException, SecurityException, URISyntaxException, 
                   IOException, RecordLockedException {        
        SystemDescription systemDescription = new SystemDescription(Paths.get(new URI(originalDBDescription.getDocumentURI())).toFile().getAbsolutePath());
        int lastVariableId = systemDescription.getDatabaseVariableListElements().length - 1;
        
        //Two new variables are added for the holding DB: "Format Errors" and "Raw Data"    
        //First we add the "Fromat Errors". This is the penultimate column in the csv file. This columns
        //indicates which are the columns that have format errors.
        DatabaseVariablesListElement variableFormatErrors = new DatabaseVariablesListElement(Globals.PATIENT_TABLE_NAME, 1, "FORMAT_ERRORS", Globals.VARIABLE_TYPE_ALPHA_NAME);
        //systemDescription.getDatabaseGroupsListElements()[1] is the default group
        variableFormatErrors.setGroup(systemDescription.getDatabaseGroupsListElements()[1]);
        variableFormatErrors.setVariableLength(4000);
        variableFormatErrors.setVariableID(++lastVariableId);
        //The next sets() are mandatory, otherwise the saveXml() throws an exception
        variableFormatErrors.setFullName("Format errors from original CSV");
        variableFormatErrors.setEnglishName("Format Errors");
        variableFormatErrors.setFillInStatus("Optional");
        variableFormatErrors.setMultiplePrimaryCopy("Othr");
        variableFormatErrors.setStandardVariableName("");
        variableFormatErrors.setXPos(0);
        variableFormatErrors.setYPos(0);        
        
        //The "Raw Data" columns contains the full content for that record. Useful so the user can
        //revise format errors but without the forced changes of the data entry GUI.
        DatabaseVariablesListElement variableRawData = new DatabaseVariablesListElement(Globals.PATIENT_TABLE_NAME, 1, "RAW_DATA", Globals.VARIABLE_TYPE_ALPHA_NAME);
        variableRawData.setGroup(systemDescription.getDatabaseGroupsListElements()[1]);
        variableRawData.setVariableLength(32500);
        variableRawData.setVariableID(++lastVariableId);
        variableRawData.setFullName("Raw Data from original CSV");
        variableRawData.setEnglishName("Raw Data");
        variableRawData.setFillInStatus("Optional");
        variableRawData.setMultiplePrimaryCopy("Othr");
        variableRawData.setStandardVariableName("");
        variableRawData.setXPos(0);
        variableRawData.setYPos(0);
        
        ArrayList<DatabaseVariablesListElement> variables = new ArrayList<>(Arrays.asList(systemDescription.getDatabaseVariableListElements()));
        variables.add(variableFormatErrors);
        variables.add(variableRawData);
        systemDescription.setVariables(variables.toArray(new DatabaseVariablesListElement[variables.size()]));
        
        //Creation of XML of Holding DB        
        String registryCode = server.getCanRegRegistryCode();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format((Calendar.getInstance()).getTime());
        int newHoldingDBNumber = server.getLastHoldingDBnumber(registryCode) + 1;
        String dbName = "HOLDING_" + registryCode + "_" +  + newHoldingDBNumber + "_" + dateStr;
        systemDescription = server.createNewHoldingDB(registryCode, dbName, systemDescription);

        CanRegServerInterface holdingProxy = ((CanRegRegistryProxy) server).getInstanceForHoldingDB(registryCode, dbName);

        
//        TE FALTA PROBAR SI ESTO IMPORTA TODOS LOS CASOS EN LA HOLDING (TENES QUE ABRIR LA HOLDING CON EL RAZOR SQL)
//        TAMBIEN COMPARA CUANTOS REGISTROS IMPORTA EN CADA CASO (o sea, usando el output de la betty y sin usar el output de la betty)
        return importFiles(task, systemDescription.getSystemDescriptionDocument(), map, files, holdingProxy, io, true);
    }    

    public static boolean importFiles(Task<Object, Void> task, Document doc, 
                                      List<canreg.client.dataentry.Relation> map, File[] files, 
                                      CanRegServerInterface server, ImportOptions io, 
                                      boolean intoHoldingDB) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        int numberOfLinesRead = 0;
        Writer reportWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        if (io.getReportFileName() != null && io.getReportFileName().trim().length() > 0) {
            try {
                reportWriter = new BufferedWriter(new FileWriter(io.getReportFileName()));
            } catch (IOException ex) {
                Logger.getLogger(Import.class.getName()).log(Level.WARNING, null, ex);
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

        try {
            // first we get the patients
            if (task != null) {
                task.firePropertyChange(PROGRESS, 0, 0);
                task.firePropertyChange(PATIENTS, 0, 0);
            }
            if (files[0] != null) {
                reportWriter.write("Starting to import patients from " + files[0].getAbsolutePath() + Globals.newline);
                FileInputStream patientFIS = new FileInputStream(files[0]);
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
                    if(numberOfLinesRead > 200)
                        break;
                    
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
                                patient.setVariable(rel.getDatabaseVariableName(), csvRecord.get(rel.getFileColumnNumber()));
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }
                    // debugOut(patient.toString());

                    //Records are being imported into the production DB
                    
                    Object patientID = patient.getVariable(io.getPatientRecordIDVariableName());
                    Patient oldPatientRecord = null;
                    try {
                        oldPatientRecord = CanRegClientApp.getApplication().getPatientRecord(
                                (String) patientID, false, server);
                    } catch(NullPointerException ex1) {
                        //Patient not found in DB.
                    }
                    catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    } 

                    if (oldPatientRecord != null) {
                        if(intoHoldingDB) {
                            savePatient = false;
                        } else {
                            // deal with discrepancies                        
                            switch (io.getDiscrepancies()) {
                                case ImportOptions.REJECT:
                                    savePatient = false;
                                    break;
                                case ImportOptions.UPDATE:
                                    String updateReport = updateRecord(oldPatientRecord, patient);
                                    if (updateReport.length() > 0) {
                                        reportWriter.write(patient.getVariable(
                                                io.getTumourIDVariablename()) + Globals.newline + updateReport);
                                    }
    //                                oldPatientDatabaseRecordID = (Integer) oldPatientRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                                    patient = oldPatientRecord;
                                    savePatient = true;
                                    break;
                                case ImportOptions.OVERWRITE:
    //                                oldPatientDatabaseRecordID = (Integer) oldPatientRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                                    String overWriteReport = overwriteRecord(oldPatientRecord, patient);
                                    if (overWriteReport.length() > 0) {
                                        reportWriter.write(patient.getVariable(
                                                io.getTumourIDVariablename()) + Globals.newline + overWriteReport);
                                    }
                                    patient = oldPatientRecord;
                                    savePatient = true;
                                    break;
                            }
                        }
                        
                        if (task != null) {
                            task.firePropertyChange(RECORD, 50, 75);
                        }
                    } 
                    
                    if ((!io.isTestOnly())) {
//                        if (deletePatient) {
//                            server.deleteRecord(oldPatientDatabaseRecordID, Globals.PATIENT_TABLE_NAME);
//                        }
                        Object crap = null;
                        if (savePatient) {
                            patientID = patient.getVariable(io.getPatientRecordIDVariableName());
                            if (patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
                                try {
                                    server.editPatient(patient);
                                } catch(Exception ex) {
                                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR EDITING PATIENT " + patientID, ex);
                                }
                            } else {
                                try {
                                    server.savePatient(patient);
                                } catch(Exception ex) {
                                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR SAVING PATIENT " + patientID, ex);
                                }
                            }
                        }
                    } 

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

                FileInputStream tumourFIS = new FileInputStream(files[1]);
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
                    if(numberOfLinesRead > 200)
                        break;
                    
                    // We allow for null tasks...
                    boolean saveTumour = true;
                    boolean deleteTumour = false;

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
                                tumour.setVariable(rel.getDatabaseVariableName(),
                                        csvRecord.get(rel.getFileColumnNumber()));
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    } 

                    Tumour tumour2 = null;
                    Object tumourID = tumour.getVariable(io.getTumourIDVariablename());
                    // see if this tumour exists in the database already
                    // TODO: Implement this using arrays and getTumourRexords instead
                    try {
                        tumour2 = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(
                                (String) tumourID, false, server);
                    } catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                    if (tumour2 != null) {
                        if(intoHoldingDB) {
                            //Tumour is already present in holding DB, if we try to save an exception will rise
                            saveTumour = false;
                        } else {
                            // deal with discrepancies
                            switch (io.getDiscrepancies()) {
                                case ImportOptions.REJECT:
                                    saveTumour = false;
                                    break;
                                case ImportOptions.UPDATE:
                                    String updateReport = updateRecord(tumour2, tumour);
                                    if (updateReport.length() > 0) {
                                        reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + Globals.newline + updateReport);
                                    }
                                    tumour = tumour2;
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
                        patient = CanRegClientApp.getApplication().getPatientRecord(
                                (String) tumour.getVariable(io.getPatientRecordIDTumourTableVariableName()), false, server);
                    } catch (NullPointerException ex) {
                        //Patient not found in DB
                    } catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    } 

                    if (patient != null) {
                        if (io.isDoChecks() && saveTumour) {
                            // run the edits...
                            String message = "";
                            LinkedList<CheckResult> checkResults = canreg.client.CanRegClientApp.getApplication().performChecks(patient, tumour);

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
                                        canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                                tumour.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());
                                // generate ICCC codes
                                if (io.getICCCVariableName() != null) {
                                    String iccc = (String) tumour.getVariable(io.getICCCVariableName());
                                    if (iccc == null || iccc.trim().length() == 0) {
                                        ConversionResult[] conversionResultICCC = 
                                                canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumour);
                                        tumour.setVariable(io.getICCCVariableName(), conversionResultICCC[0].getValue());
                                    }
                                }
                            } else {
                                tumour.setVariable(io.getTumourRecordStatus(), "0");
                            }

                            if (worstResultCodeFound == CheckResult.ResultCode.OK) {
                                // message += "Cross-check conclusion: Valid";
                            } else {
                                reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "\t" + message + Globals.newline);
                                // System.out.println(tumour.getVariable(io.getTumourIDVariablename()) + " " + message);
                            }
                            tumour.setVariable(io.getTumourCheckStatus(), CheckResult.toDatabaseVariable(worstResultCodeFound));

                        } else {
                            // try to generate ICD10, if missing, anyway
                            String icd10 = (String) tumour.getVariable(io.getICD10VariableName());
                            if (icd10 == null || icd10.trim().length() == 0) {
                                ConversionResult[] conversionResult = 
                                        canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                                tumour.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());
                            }
                            // try to generate ICCC3, if missing, anyway
                            String iccc = (String) tumour.getVariable(io.getICCCVariableName());
                            if (iccc == null || iccc.trim().length() == 0) {
                                ConversionResult[] conversionResult = 
                                        canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumour);
                                tumour.setVariable(io.getICCCVariableName(), conversionResult[0].getValue());
                            }
                        }
                    } else {
                        reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "\t" + "No patient matches this Tumour." + Globals.newline);
                        tumour.setVariable(io.getTumourRecordStatus(), "0");
                        tumour.setVariable(io.getTumourCheckStatus(), CheckResult.toDatabaseVariable(ResultCode.Missing));
                    }
                    if (task != null) {
                        task.firePropertyChange(RECORD, 50, 75);
                    }
                    if (!io.isTestOnly()) {
                        if (deleteTumour && tumour2 != null) {
                            server.deleteRecord((Integer) tumour2.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME), Globals.TUMOUR_TABLE_NAME);
                        }
                        if (saveTumour) {
                            // if tumour has record ID we edit it
                            if (tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
                                try {
                                    server.editTumour(tumour);
                                } catch(Exception ex) {
                                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR EDITING TUMOUR " + tumourID, ex);
                                }
                                
                            } // if not we save it
                            else {
                                try {
                                    server.saveTumour(tumour);
                                } catch(Exception ex) {
                                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "ERROR SAVING TUMOUR " + tumourID, ex);
                                }
                            }
                        }
                    } 
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

                FileInputStream sourceFIS = new FileInputStream(files[2]);
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
                    if(numberOfLinesRead > 300)
                        break;
                    
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
                                source.setVariable(rel.getDatabaseVariableName(), csvRecord.get(rel.getFileColumnNumber()));
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }

                    Tumour tumour = null;
                    try {
                        tumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(
                                (String) source.getVariable(io.getTumourIDSourceTableVariableName()), false, server);
                    } catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                    if (task != null) {
                        task.firePropertyChange(RECORD, 50, 75);
                    }
                    boolean addSource = true;

                    if (tumour != null) {
                        Set<Source> sources = tumour.getSources();
                        Object sourceRecordID = source.getVariable(io.getSourceIDVariablename());
                        // look for source in sources
                        for (Source oldSource : sources) {
                            if (oldSource.getVariable(io.getSourceIDVariablename()).equals(sourceRecordID)) {
                                if( ! intoHoldingDB) {
                                    // deal with discrepancies
                                    switch (io.getDiscrepancies()) {
                                        case ImportOptions.REJECT:
                                            addSource = false;
                                            break;
                                        case ImportOptions.UPDATE:
                                            String updateReport = updateRecord(oldSource, source);
                                            if (updateReport.length() > 0) {
                                                reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + Globals.newline + updateReport);
                                            }
                                            source = oldSource;
                                            addSource = false;
                                            break;
                                        case ImportOptions.OVERWRITE:
                                            // deleteTumour;
                                            sources.remove(oldSource);
                                            addSource = true;
                                            break;
                                    }
                                } else {
                                    addSource = false;
                                }
                            }
                        }
                        if (addSource) {
                            sources.add(source);
                        }
                        tumour.setSources(sources);
                        if (!io.isTestOnly()) {
                            try {
                                server.editTumour(tumour);
                            } catch(Exception ex) {
                                Logger.getLogger(Import.class.getName()).log(Level.SEVERE, 
                                        "ERROR SAVING SOURCE " + sourceRecordID + 
                                        " ON TUMOUR " + tumour.getVariable(io.getTumourIDVariablename()),
                                        ex);
                            }
                        }
                    } else {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, "No tumour for source record.");
                    }
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
                }
            }
            try {
                reportWriter.flush();
                reportWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (task != null) {
            task.firePropertyChange(PROGRESS, 100, 100);
            task.firePropertyChange("finished", null, null);
        }
        return success;
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
