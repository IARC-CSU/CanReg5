package canreg.client.dataentry;

import au.com.bytecode.opencsv.CSVReader;
import cachingtableapi.DistributedTableDescriptionException;
import canreg.client.CanRegClientApp;
import canreg.common.Globals;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.server.CanRegServerInterface;
import canreg.server.database.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.SQLException;
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
import org.jdesktop.application.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author ervikm
 */
public class Import {

    private static String namespace = "ns3:";
    private static boolean debug = true;
    public static String FINISHED = "finished";
    public static String PROGRESS = "progress";
    public static String RECORD = "record";
    public static String PATIENTS = "patients";
    public static String TUMOURS = "tumours";
    public static String SOURCES = "sources";

    /**
     * 
     * @param task
     * @param doc
     * @param map
     * @param file
     * @param server
     * @param io
     * @return
     */
    public static boolean importFile(Task<Object, Void> task, Document doc, List<canreg.client.dataentry.Relation> map, File file, CanRegServerInterface server, ImportOptions io) throws SQLException, RemoteException, SecurityException, RecordLockedException {
        boolean success = false;
        Set<String> noNeedToLookAtPatientVariables = new TreeSet<String>();

        noNeedToLookAtPatientVariables.add(canreg.common.Tools.toLowerCaseStandardized(io.getPatientIDVariableName()));
        noNeedToLookAtPatientVariables.add(canreg.common.Tools.toLowerCaseStandardized(io.getPatientRecordIDVariableName()));

        String firstNameVariableName = io.getFirstNameVariableName();
        String sexVariableName = io.getSexVariableName();

        CSVReader reader = null;

        HashMap mpCodes = new HashMap();

        int numberOfLinesRead = 0;

        Map<String, Integer> nameSexTable = server.getNameSexTables();

        try {
            // Tro to detect the encoding...
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, io.getFileCharset());
            // Returns the name of the character encoding

            Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", isr.getEncoding());

            int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(file.getAbsolutePath());

            reader = new CSVReader(new FileReader(file), io.getSeparator());
            String[] lineElements;

            int linesToRead = io.getMaxLines();
            if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                linesToRead = numberOfRecordsInFile;
            }


            // skip the first line
            reader.readNext();

            while ((lineElements = reader.readNext()) != null && (numberOfLinesRead < linesToRead)) {
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
                        if (rel.getFileColumnNumber() < lineElements.length) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                    patient.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                                }
                            } else {
                                patient.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
                            }
                        } else {
                            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Something wrong with patient part of line " + numberOfLinesRead + ".", new Exception("Error in line: " + numberOfLinesRead + ". Can't find field: " + rel.getDatabaseVariableName()));
                        }
                    }
                }
                // debugOut(patient.toString());

                // Build tumour part
                Tumour tumour = new Tumour();
                for (int i = 0; i < map.size(); i++) {
                    Relation rel = map.get(i);
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("tumour") && rel.getFileColumnNumber() < lineElements.length) {
                        if (rel.getFileColumnNumber() < lineElements.length) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                    tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                                }
                            } else {
                                tumour.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
                            }
                        } else {
                            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Something wrong with tumour part of line " + numberOfLinesRead + ".", new Exception("Error in line: " + numberOfLinesRead + ". Can't find field: " + rel.getDatabaseVariableName()));
                        }
                    }
                }

                // Build source part
                Set<Source> sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
                Source source = new Source();
                for (int i = 0; i < map.size(); i++) {
                    Relation rel = map.get(i);
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME) && rel.getFileColumnNumber() < lineElements.length) {
                        if (rel.getFileColumnNumber() < lineElements.length) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                    source.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                                }
                            } else {
                                source.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
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
                            tumours = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID(patientID + "", false);
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
                            oldPatients = CanRegClientApp.getApplication().getPatientRecordsByID((String) patientID, false);
                        } catch (RemoteException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SecurityException ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
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


                if (needToSavePatientAgain) {
                    if (patientDatabaseRecordID > 0) {
                        server.editPatient(patient);
                    } else {
                        patientDatabaseRecordID = server.savePatient(patient);
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
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } catch (NumberFormatException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } catch (InterruptedException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Interupted on line: " + (numberOfLinesRead + 1) + ". ", ex);
            success = true;
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ",
                    ex);
            success = false;
        } catch (SQLException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        task.firePropertyChange("finished", null, null);
        return success;
    }

    public static boolean importFiles(Task<Object, Void> task, Document doc, List<canreg.client.dataentry.Relation> map, File[] files, CanRegServerInterface server, ImportOptions io) throws SQLException, RemoteException, SecurityException, RecordLockedException {
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
        HashMap mpCodes = new HashMap();
        int numberOfLinesRead = 0;
        BufferedReader bufferedReader = null;
        int linesToRead = io.getMaxLines();
        String[] lineElements;
        ResultCode worstResultCodeFound;

        try {
            // first we get the patients
            task.firePropertyChange(PROGRESS, 0, 0);
            task.firePropertyChange(PATIENTS, 0, 0);
            if (files[0] != null) {
                reportWriter.write("Starting to import patients from " + files[0].getAbsolutePath() + "\n");
                FileInputStream patientFIS = new FileInputStream(files[0]);
                InputStreamReader patientISR = new InputStreamReader(patientFIS, io.getFileCharsets()[0]);

                Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", patientISR.getEncoding());

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[0].getAbsolutePath());
                bufferedReader = new BufferedReader(patientISR);
                String line = bufferedReader.readLine();
                // Skip first line
                line = bufferedReader.readLine();
                // patientNumber

                if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                    linesToRead = numberOfRecordsInFile;
                }

                while (line != null && (numberOfLinesRead < linesToRead)) {
                    // We allow for null tasks...
                    boolean savePatient = true;
                    boolean deletePatient = false;
                    int oldPatientDatabaseRecordID = -1;

                    if (task != null) {
                        task.firePropertyChange(PROGRESS, ((numberOfLinesRead - 1) * 100 / linesToRead) / 3, ((numberOfLinesRead) * 100 / linesToRead) / 3);
                        task.firePropertyChange(PATIENTS, ((numberOfLinesRead - 1) * 100 / linesToRead), ((numberOfLinesRead) * 100 / linesToRead));
                    }
                    lineElements = canreg.common.Tools.breakDownLine(io.getSeparators()[0], line);

                    // Build patient part
                    Patient patient = new Patient();
                    for (int i = 0; i < map.size(); i++) {
                        Relation rel = map.get(i);
                        if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("patient")) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                    patient.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                                }
                            } else {
                                patient.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }
                    // debugOut(patient.toString());

                    // debugOut(tumour.toString());
                    // add patient to the database
                    Object patientID = patient.getVariable(io.getPatientRecordIDVariableName());
                    Patient oldPatient = null;
                    try {
                        oldPatient = CanRegClientApp.getApplication().getPatientRecordByID(
                                (String) patientID, false);
                    } catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (oldPatient != null) {
                        // deal with discrepancies
                        switch (io.getDiscrepancies()) {
                            case ImportOptions.REJECT:
                                savePatient = false;
                                break;
                            case ImportOptions.UPDATE:
                                String updateReport = updateRecord(oldPatient, patient);
                                if (updateReport.length() > 0) {
                                    reportWriter.write(patient.getVariable(
                                            io.getTumourIDVariablename()) + "\n" + updateReport);
                                }
                                oldPatientDatabaseRecordID = (Integer) oldPatient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                                patient = oldPatient;
                                savePatient = true;
                                break;
                            case ImportOptions.OVERWRITE:
                                // deleteTumour;
                                oldPatientDatabaseRecordID = (Integer) oldPatient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                                String overWriteReport = overwriteRecord(oldPatient, patient);
                                if (overWriteReport.length() > 0) {
                                    reportWriter.write(patient.getVariable(
                                            io.getTumourIDVariablename()) + "\n" + overWriteReport);
                                }
                                patient = oldPatient;
                                savePatient = true;
                                break;
                        }
                        // reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "Tumour already exists.\n");

                    }


                    if (task != null) {
                        task.firePropertyChange(RECORD, 50, 75);
                    }

                    if ((!io.isTestOnly())) {
                        if (deletePatient) {
                            server.deleteRecord(oldPatientDatabaseRecordID, Globals.PATIENT_TABLE_NAME);
                        }
                        if (savePatient) {
                            if (patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
                                server.editPatient(patient);
                            } else {
                                server.savePatient(patient);
                            }
                        }
                    }

                    if (task != null) {
                        task.firePropertyChange(RECORD, 100, 75);
                    }

                    //Read next line of data
                    line = bufferedReader.readLine();
                    numberOfLinesRead++;

                    reportWriter.flush();

                    if (Thread.interrupted()) {
                        //We've been interrupted: no more importing.
                        throw new InterruptedException();
                    }
                }
                reportWriter.write("Finished reading patients.\n\n");
            }
            task.firePropertyChange(PATIENTS, 100, 100);
            task.firePropertyChange("progress", 33, 34);
            // then we get the tumours
            task.firePropertyChange(TUMOURS, 0, 0);
            if (files[1] != null) {
                reportWriter.write("Starting to import tumours from " + files[1].getAbsolutePath() + "\n");

                FileInputStream tumourFIS = new FileInputStream(files[1]);
                InputStreamReader tumourISR = new InputStreamReader(tumourFIS, io.getFileCharsets()[1]);

                Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", tumourISR.getEncoding());

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[1].getAbsolutePath());
                bufferedReader = new BufferedReader(tumourISR);
                String line = bufferedReader.readLine();
                // Skip first line
                line = bufferedReader.readLine();

                // reset the number of lines read
                numberOfLinesRead = 0;

                if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                    linesToRead = numberOfRecordsInFile;
                }
                while (line != null && (numberOfLinesRead < linesToRead)) {
                    // We allow for null tasks...
                    boolean saveTumour = true;
                    boolean deleteTumour = false;

                    if (task != null) {
                        task.firePropertyChange(PROGRESS, 33 + ((numberOfLinesRead - 1) * 100 / linesToRead) / 3, 33 + ((numberOfLinesRead) * 100 / linesToRead) / 3);
                        task.firePropertyChange(TUMOURS, ((numberOfLinesRead - 1) * 100 / linesToRead), ((numberOfLinesRead) * 100 / linesToRead));
                    }
                    lineElements = canreg.common.Tools.breakDownLine(io.getSeparators()[1], line);
                    // Build tumour part
                    Tumour tumour = new Tumour();
                    for (int i = 0; i < map.size(); i++) {
                        Relation rel = map.get(i);
                        if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("tumour")) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                    tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                                }
                            } else {
                                tumour.setVariable(rel.getDatabaseVariableName(),
                                        lineElements[rel.getFileColumnNumber()]);
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }

                    // see if this tumour exists in the database already
                    // TODO: Implement this using arrays and getTumourRexords instead
                    Tumour tumour2 = null;
                    try {
                        tumour2 = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(
                                (String) tumour.getVariable(io.getTumourIDVariablename()), false);
                    } catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (tumour2 != null) {
                        // deal with discrepancies
                        switch (io.getDiscrepancies()) {
                            case ImportOptions.REJECT:
                                saveTumour = false;
                                break;
                            case ImportOptions.UPDATE:
                                String updateReport = updateRecord(tumour2, tumour);
                                if (updateReport.length() > 0) {
                                    reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "\n" + updateReport);
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

                    Patient patient = null;
                    if (io.isDoChecks() && saveTumour) {
                        try {
                            patient = CanRegClientApp.getApplication().getPatientRecord(
                                    (String) tumour.getVariable(io.getPatientRecordIDTumourTableVariableName()), false);
                        } catch (Exception ex) {
                            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if (patient != null) {
                            // run the edits...
                            String message = "";
                            LinkedList<CheckResult> checkResults = canreg.client.CanRegClientApp.getApplication().performChecks(patient, tumour);

                            Map<Globals.StandardVariableNames, CheckResult.ResultCode> mapOfVariablesAndWorstResultCodes = new EnumMap<Globals.StandardVariableNames, CheckResult.ResultCode>(Globals.StandardVariableNames.class);
                            worstResultCodeFound = CheckResult.ResultCode.OK;
                            for (CheckResult result : checkResults) {
                                if (result.getResultCode() != CheckResult.ResultCode.OK && result.getResultCode() != CheckResult.ResultCode.NotDone) {
                                    message += result + "\t";
                                    if (!result.getResultCode().equals(CheckResult.ResultCode.Missing)) {
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

                            if (worstResultCodeFound != CheckResult.ResultCode.Invalid && worstResultCodeFound != CheckResult.ResultCode.Missing) {
                                // If no errors were found we generate ICD10 code
                                // ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                            }

                            if (worstResultCodeFound == CheckResult.ResultCode.OK) {
                                // message += "Cross-check conclusion: Valid";
                            } else {
                                reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "\t" + message + "\n");
                                // System.out.println(tumour.getVariable(io.getTumourIDVariablename()) + " " + message);
                            }
                        } else {
                            reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "\t" + "No patient matches this Tumour." + "\n");
                        }
                    }
                    if (task != null) {
                        task.firePropertyChange(RECORD, 50, 75);
                    }
                    if (!io.isTestOnly()) {
                        if (deleteTumour) {
                            server.deleteRecord((Integer) tumour2.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME), Globals.TUMOUR_TABLE_NAME);
                        }
                        if (saveTumour) {
                            // if tumour has no record ID we save it
                            if (tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) == null) {
                                server.saveTumour(tumour);
                            } // if not we edit it
                            else {
                                server.editTumour(tumour);
                            }
                        }
                    }
                    if (task != null) {
                        task.firePropertyChange(RECORD, 75, 100);
                    }
                    //Read next line of data
                    line = bufferedReader.readLine();
                    numberOfLinesRead++;
                    reportWriter.flush();

                    if (Thread.interrupted()) {
                        //We've been interrupted: no more importing.
                        throw new InterruptedException();
                    }
                }
                reportWriter.write("Finished reading tumours.\n\n");
            }
            task.firePropertyChange(TUMOURS, 100, 100);

            // then at last we get the sources
            task.firePropertyChange(SOURCES, 0, 0);
            task.firePropertyChange(PROGRESS, 66, 66);
            if (files[2] != null) {
                reportWriter.write("Starting to import sources from " + files[2].getAbsolutePath() + "\n");

                FileInputStream sourceFIS = new FileInputStream(files[2]);
                InputStreamReader sourceISR = new InputStreamReader(sourceFIS, io.getFileCharsets()[2]);

                Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding {0}", sourceISR.getEncoding());

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[2].getAbsolutePath());
                bufferedReader = new BufferedReader(sourceISR);
                String line = bufferedReader.readLine();
                // Skip first line
                line = bufferedReader.readLine();

                // reset the number of lines read
                numberOfLinesRead = 0;

                if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                    linesToRead = numberOfRecordsInFile;
                }
                while (line != null && (numberOfLinesRead < linesToRead)) {
                    // We allow for null tasks...
                    boolean needToSavePatientAgain = true;
                    int patientDatabaseRecordID = -1;

                    if (task != null) {
                        task.firePropertyChange(PROGRESS, 67 + ((numberOfLinesRead - 1) * 100 / linesToRead) / 3, 67 + ((numberOfLinesRead) * 100 / linesToRead) / 3);
                        task.firePropertyChange(SOURCES, ((numberOfLinesRead - 1) * 100 / linesToRead), ((numberOfLinesRead) * 100 / linesToRead));
                    }
                    lineElements = canreg.common.Tools.breakDownLine(io.getSeparators()[2], line);

                    // Build source part

                    Source source = new Source();
                    for (int i = 0; i < map.size(); i++) {
                        Relation rel = map.get(i);
                        if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                    source.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                                }
                            } else {
                                source.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
                            }
                        }
                        if (task != null) {
                            task.firePropertyChange(RECORD, i - 1 / map.size() * 50, i / map.size() * 50);
                        }
                    }

                    Tumour tumour = null;
                    try {
                        tumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(
                                (String) source.getVariable(io.getTumourIDSourceTableVariableName()), false);
                    } catch (Exception ex) {
                        Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (task != null) {
                        task.firePropertyChange(RECORD, 50, 75);
                    }
                    if (tumour != null) {
                        Set<Source> sources = tumour.getSources();
                        sources.add(source);
                        // get the tumour...
                        tumour.setSources(sources);
                        if (!io.isTestOnly()) {
                            server.editTumour(tumour);
                        }
                    } else {
                        //implement an error report
                    }
                    if (task != null) {
                        task.firePropertyChange(RECORD, 75, 100);
                    }
                    //Read next line of data
                    line = bufferedReader.readLine();
                    numberOfLinesRead++;
                    reportWriter.flush();

                    if (Thread.interrupted()) {
                        //We've been interrupted: no more importing.
                        throw new InterruptedException();
                    }
                }
                reportWriter.write("Finished reading sources.\n\n");
            }
            task.firePropertyChange(SOURCES, 100, 100);
            task.firePropertyChange(PROGRESS, 100, 100);
            while (!task.isProgressPropertyValid()) {
                // wait untill progress has been updated...
            }
            reportWriter.write("Finished\n");
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } catch (NumberFormatException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ", ex);
            success = false;
        } catch (InterruptedException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.INFO, "Interupted on line: " + (numberOfLinesRead + 1) + ". ", ex);
            success = true;
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, "Error in line: " + (numberOfLinesRead + 1 + 1) + ". ",
                    ex);
            success = false;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (reportWriter != null) {
                try {
                    reportWriter.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        task.firePropertyChange("finished", null, null);
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
