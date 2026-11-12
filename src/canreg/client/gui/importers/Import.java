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
import canreg.common.database.HoldingDbCommon;
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
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.conversions.ConversionICDO3toICD10;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.common.qualitycontrol.Checker;
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
import java.util.LinkedHashMap;
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

    private static final Logger LOGGER = Logger.getLogger(Import.class.getName());
    private static final String namespace = Globals.NAMESPACE;
    private static final boolean debug = Globals.DEBUG;
    public static String FINISHED = "finished";
    public static String PROGRESS = "progress";
    public static String RECORD = "record";
    public static String PATIENTS = "patients";
    public static String TUMOURS = "tumours";
    public static String SOURCES = "sources";
    public static String R_SCRIPTS = "r_scripts";
    private static final String RAW_DATA_COLUMN = HoldingDbCommon.RAW_DATA_COLUMN;
    private static final String FORMAT_ERRORS_COLUMN = HoldingDbCommon.FORMAT_ERRORS_COLUMN;    

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

            // LOGGER.log(Level.CONFIG, "Name of the character encoding {0}");
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
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d.",numberOfLinesRead + 1 + 1) , ex);
                                        success = false;
                                    }
                                }
                            } else {
                                patient.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            LOGGER.log(Level.INFO,String.format("Something wrong with patient part of line %d .",numberOfLinesRead), new Exception(String.format("Error in line: %d. Can't find field: %s",numberOfLinesRead,rel.getDatabaseVariableName())));
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
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d .",numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                tumour.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            LOGGER.log(Level.INFO,String.format("Something wrong with tumour part of line : %d.",numberOfLinesRead), new Exception(String.format("Error in line: %d.  Can't find field: %s",numberOfLinesRead,rel.getDatabaseVariableName())));
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
                                        LOGGER.log(Level.SEVERE,String.format("Number format error in line: %d .",numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                source.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            LOGGER.log(Level.INFO, String.format("Something wrong with source part of line  %d.",numberOfLinesRead), new Exception(String.format("Error in line: %d. Can't find field: %s.",numberOfLinesRead,rel.getDatabaseVariableName())));
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
                            LOGGER.log(Level.SEVERE, null, ex);
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
                            LOGGER.log(Level.SEVERE, null, ex);
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
                    generateMissingCodes(server, patient, tumour, io);
                }
                if (tumour.getVariable(io.getPatientIDTumourTableVariableName()) == null) {
                    tumour.setVariable(io.getPatientIDTumourTableVariableName(), patientID);
                }

                if (tumour.getVariable(io.getPatientRecordIDTumourTableVariableName()) == null) {
                    tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                }

                if (io.getTumourRecordStatus() != null) {
                    Object currentRecStatus = tumour.getVariable(io.getTumourRecordStatus());
                    if (currentRecStatus == null || currentRecStatus.toString().trim().isEmpty()) {
                        tumour.setVariable(io.getTumourRecordStatus(), "1");
                    }
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
            LOGGER.log(Level.SEVERE, String.format("Error in line: %d.",numberOfLinesRead + 1 + 1) , ex);
            success = false;
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, String.format("Interupted on line: %d.",numberOfLinesRead + 1) , ex);
            success = true;
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
        }

        return success;
    }
    
    private static Relation createRelation(DatabaseVariablesListElement variable,
                                           int columnIndexInFile,
                                           String variableNameInFile) {
        Relation relation = new Relation();
        relation.setDatabaseTableName(variable.getDatabaseTableName());
        relation.setDatabaseTableVariableID(variable.getVariableID());
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
        
        SystemDescription systemDescription = 
                new SystemDescription(Paths.get(new URI(originalDBDescription.getDocumentURI())).toFile().getAbsolutePath());
        List<DatabaseVariablesListElement> newVariables = 
                HoldingDbCommon.addVariablesForImportToHoldingDB(systemDescription);
        List<Relation> relationsToAdd = buildRelationsForImportToHoldingDB(newVariables, 
                patientAmountOfColumns, tumourAmountOfColumns, sourceAmountOfColumns);
        map.addAll(relationsToAdd);

        //Creation of XML of Holding DB        
        String registryCode = server.getCanRegRegistryCode();
        systemDescription = server.createNewHoldingDB(registryCode, systemDescription);

        CanRegServerInterface holdingProxy = ((CanRegRegistryProxy) server).getInstanceForHoldingDB(systemDescription.getRegistryCode());

        return importFiles(task, map, files, holdingProxy, io, true, originalDBDescription);
    }

    /**
     * Build the new relations for the new variables.
     * @param newVariables new variables
     * @param patientAmountOfColumns patientAmountOfColumns 
     * @param tumourAmountOfColumns tumourAmountOfColumns
     * @param sourceAmountOfColumns sourceAmountOfColumns
     * @return list of new relations
     */
    public static List<Relation> buildRelationsForImportToHoldingDB(List<DatabaseVariablesListElement> newVariables, int patientAmountOfColumns, int tumourAmountOfColumns, int sourceAmountOfColumns) {
        List<Relation> newRelations = new ArrayList<>();
        int variableIndex = 0;

        //Two new variables are added for the holding DB: "Format Errors" and "Raw Data"    
        //First we add the "Fromat Errors". This is the penultimate column in the csv file. This columns
        //indicates which are the columns that have format errors.
        DatabaseVariablesListElement patientFormatErrorsVar = newVariables.get(variableIndex);
        Relation patientFormatErrorsRel = createRelation(patientFormatErrorsVar,
                (patientAmountOfColumns - 2), "format.errors");
        newRelations.add(patientFormatErrorsRel);

        //The "Raw Data" columns contains the full content for that record. Useful so the user can
        //revise format errors but without the forced changes of the data entry GUI.
        variableIndex++;
        DatabaseVariablesListElement patientRawDataVar = newVariables.get(variableIndex);
        Relation patientRawDataRel = createRelation(patientRawDataVar, (patientAmountOfColumns - 1), "all.raw.data");
        newRelations.add(patientRawDataRel);


        //"Format Errors" and "Raw Data" are also added to the tumour and source tables in case
        //the user input 3 files that don't share data.
        variableIndex++;
        DatabaseVariablesListElement tumourFormatErrorsVar = newVariables.get(variableIndex);
        Relation tumourFormatErrorsRel = createRelation(tumourFormatErrorsVar, (tumourAmountOfColumns - 2), "format.errors");
        newRelations.add(tumourFormatErrorsRel);

        variableIndex++;
        DatabaseVariablesListElement tumourRawDataVar = newVariables.get(variableIndex);
        Relation tumourRawDataRel = createRelation(tumourRawDataVar, (tumourAmountOfColumns - 1), "all.raw.data");
        newRelations.add(tumourRawDataRel);

        variableIndex++;
        DatabaseVariablesListElement sourceFormatErrorsVar = newVariables.get(variableIndex);
        Relation sourceFormatErrorsRel = createRelation(sourceFormatErrorsVar, (sourceAmountOfColumns - 2), "format.errors");
        newRelations.add(sourceFormatErrorsRel);

        variableIndex++;
        DatabaseVariablesListElement sourceRawDataVar = newVariables.get(variableIndex);
        Relation sourceRawDataRel = createRelation(sourceRawDataVar, (sourceAmountOfColumns - 1), "all.raw.data");
        newRelations.add(sourceRawDataRel);
        
        return newRelations;
    }

    /**
     * Bounded in-memory LRU cache to avoid repeated roundtrip queries during bulk imports.
     */
    public static class ImportCache {
        private final Map<String, Patient> patientByPRID = new LinkedHashMap<String, Patient>(1024, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Patient> eldest) {
                return size() > 500000;
            }
        };
        private final Map<String, Patient> patientByPID = new LinkedHashMap<String, Patient>(1024, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Patient> eldest) {
                return size() > 500000;
            }
        };
        private final Map<String, Tumour> tumourByTID = new LinkedHashMap<String, Tumour>(1024, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Tumour> eldest) {
                return size() > 500000;
            }
        };
        private final Map<Integer, Patient> patientByLineNumber = new LinkedHashMap<Integer, Patient>(1024, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Patient> eldest) {
                return size() > 500000;
            }
        };
        private final Map<String, Patient> patientByTID = new LinkedHashMap<String, Patient>(1024, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Patient> eldest) {
                return size() > 500000;
            }
        };

        public Patient getPatientByPRID(String prid) {
            return (prid != null) ? patientByPRID.get(prid) : null;
        }

        public void putPatientByPRID(String prid, Patient p) {
            if (prid != null && p != null) {
                patientByPRID.put(prid, p);
            }
        }

        public Patient getPatientByPID(String pid) {
            return (pid != null) ? patientByPID.get(pid) : null;
        }

        public void putPatientByPID(String pid, Patient p) {
            if (pid != null && p != null) {
                patientByPID.put(pid, p);
            }
        }

        public Tumour getTumourByTID(String tid) {
            return (tid != null) ? tumourByTID.get(tid) : null;
        }

        public void putTumourByTID(String tid, Tumour t) {
            if (tid != null && t != null) {
                tumourByTID.put(tid, t);
            }
        }

        public Patient getPatientByLineNumber(int lineNumber) {
            return patientByLineNumber.get(lineNumber);
        }

        public void putPatientByLineNumber(int lineNumber, Patient p) {
            if (p != null) {
                patientByLineNumber.put(lineNumber, p);
            }
        }

        public Patient getPatientByTID(String tid) {
            return (tid != null) ? patientByTID.get(tid) : null;
        }

        public void putPatientByTID(String tid, Patient p) {
            if (tid != null && p != null) {
                patientByTID.put(tid, p);
            }
        }

        public void clear() {
            patientByPRID.clear();
            patientByPID.clear();
            tumourByTID.clear();
            patientByLineNumber.clear();
            patientByTID.clear();
        }
    }

    public static boolean importFiles(Task<Object, Void> task, 
                                      List<canreg.client.dataentry.Relation> map, File[] files, 
                                      CanRegServerInterface server, ImportOptions io, 
                                      boolean intoHoldingDB, Document doc) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, Exception {

        int numberOfLinesRead = 0;
        final int BATCH_SIZE = 500;
        ImportCache cache = new ImportCache();

        // Pre-filter relation mappings by table name to avoid scanning the entire map on every row
        List<Relation> patientRelations = new ArrayList<>();
        List<Relation> tumourRelations = new ArrayList<>();
        List<Relation> sourceRelations = new ArrayList<>();
        for (Relation rel : map) {
            if (rel.getDatabaseTableVariableID() >= 0) {
                if (rel.getDatabaseTableName().equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                    patientRelations.add(rel);
                } else if (rel.getDatabaseTableName().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                    tumourRelations.add(rel);
                } else if (rel.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                    sourceRelations.add(rel);
                }
            }
        }

        Writer reportWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        if (io.getReportFileName() != null && io.getReportFileName().trim().length() > 0) {
            try {
                reportWriter = new BufferedWriter(new FileWriter(io.getReportFileName()));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        boolean success;
        CSVParser parser = null;

        if (files[0] == null && files[1] != null) {
            files[0] = files[1];
            if (io.getSeparators() != null && io.getSeparators().length > 1) {
                io.getSeparators()[0] = io.getSeparators()[1];
            }
            if (io.getFileCharsets() != null && io.getFileCharsets().length > 1) {
                io.getFileCharsets()[0] = io.getFileCharsets()[1];
            }
        }
        boolean isSingleFile = false;
        try {
            if (files[0] != null && files[1] != null &&
                    files[0].getCanonicalPath().equals(files[1].getCanonicalPath())) {
                isSingleFile = true;
            }
        } catch (IOException ex) {
            if (files[0] != null && files[1] != null &&
                    files[0].getAbsolutePath().equals(files[1].getAbsolutePath())) {
                isSingleFile = true;
            }
        }

        CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(io.getSeparators()[0]);

        int linesToRead = io.getMaxLines();
        FileInputStream patientFIS = null, tumourFIS = null, sourceFIS = null;

        try {
            // first we get the patients
            if (task != null) {
                task.firePropertyChange(PROGRESS, null, 0);
                task.firePropertyChange(PATIENTS, null, 0);
            }
            if (files[0] != null) {
                reportWriter.write("Starting to import patients from " + files[0].getAbsolutePath() + Globals.newline);
                patientFIS = new FileInputStream(files[0]);
                InputStreamReader patientISR = new InputStreamReader(patientFIS, io.getFileCharsets()[0]);

                LOGGER.log(Level.CONFIG, "Name of the character encoding {0}", patientISR.getEncoding());

                int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[0].getAbsolutePath());

                if (linesToRead > 0) {
                    linesToRead = Math.min(numberOfRecordsInFile, linesToRead);
                } else {
                    linesToRead = numberOfRecordsInFile;
                }
                parser = CSVParser.parse(files[0], io.getFileCharsets()[0], format);

                GlobalToolBox globalToolBox = new GlobalToolBox(server.getDatabseDescription());
                int lastPatientProgress = -1;
                int lastOverallProgress = -1;
                int linesSinceLastCommit = 0;

                try {
                    server.openTransaction();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not open transaction for patients import", ex);
                }

                try {
                    for (CSVRecord csvRecord : parser) {
                        int currentPatientProgress = linesToRead > 0 ? (int) (((long) numberOfLinesRead * 100) / linesToRead) : 0;
                        int currentOverallProgress = currentPatientProgress / 3;
                        if (task != null && (currentPatientProgress != lastPatientProgress || numberOfLinesRead % 100 == 0)) {
                            task.firePropertyChange(PROGRESS, lastOverallProgress, currentOverallProgress);
                            task.firePropertyChange(PATIENTS, lastPatientProgress, currentPatientProgress);
                            lastPatientProgress = currentPatientProgress;
                            lastOverallProgress = currentOverallProgress;
                        }

                        // Build patient part
                        Patient patient = new Patient();
                        for (Relation rel : patientRelations) {
                            if (rel.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                                String cellVal = csvRecord.get(rel.getFileColumnNumber());
                                if (cellVal.length() > 0) {
                                    try {
                                        patient.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(cellVal));
                                    } catch (NumberFormatException ex) {
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d", numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                String value = csvRecord.get(rel.getFileColumnNumber());
                                if (rel.getDatabaseVariableName().equalsIgnoreCase(RAW_DATA_COLUMN)) {
                                    value = value.replace("@#$", " <br>");
                                }
                                patient.setVariable(rel.getDatabaseVariableName(), value);
                            }
                        }

                        Object patientRecordID = patient.getVariable(io.getPatientRecordIDVariableName());

                        importPatient(server, io.getDiscrepancies(), (String) patientRecordID,
                                patient, reportWriter,
                                intoHoldingDB, io.isTestOnly(), false, globalToolBox, cache);

                        cache.putPatientByLineNumber(numberOfLinesRead + 1, patient);
                        if (isSingleFile) {
                            for (Relation rel : tumourRelations) {
                                if (rel.getDatabaseVariableName().equalsIgnoreCase(io.getTumourIDVariablename())) {
                                    String tid = csvRecord.get(rel.getFileColumnNumber());
                                    if (tid != null && !tid.trim().isEmpty()) {
                                        cache.putPatientByTID(tid.trim(), patient);
                                    }
                                    break;
                                }
                            }
                        }

                        numberOfLinesRead++;
                        linesSinceLastCommit++;

                        if (linesSinceLastCommit >= BATCH_SIZE) {
                            try {
                                server.commitTransaction();
                                server.openTransaction();
                                linesSinceLastCommit = 0;
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Error committing patient batch transaction", ex);
                            }
                        }

                        if (Thread.interrupted()) {
                            reportWriter.flush();
                            throw new InterruptedException();
                        }
                    }

                    if (linesSinceLastCommit > 0) {
                        try {
                            server.commitTransaction();
                            linesSinceLastCommit = 0;
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Error committing final patient transaction", ex);
                        }
                    }
                } catch (Exception ex) {
                    try {
                        server.rollbackTransaction();
                    } catch (Exception ignore) {
                    }
                    throw ex;
                }

                parser.close();
                reportWriter.write("Finished reading patients." + Globals.newline + Globals.newline);
                reportWriter.flush();
            }
            if (task != null) {
                task.firePropertyChange(PATIENTS, null, 100);
                task.firePropertyChange(PROGRESS, null, 33);
            }

            // then we get the tumours            
            if (task != null) {
                task.firePropertyChange(TUMOURS, null, 0);
            }

            if (files[1] != null) {
                reportWriter.write("Starting to import tumours from " + files[1].getAbsolutePath() + Globals.newline);

                tumourFIS = new FileInputStream(files[1]);
                InputStreamReader tumourISR = new InputStreamReader(tumourFIS, io.getFileCharsets()[1]);

                LOGGER.log(Level.CONFIG, "Name of the character encoding {0}", tumourISR.getEncoding());

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

                int lastTumourProgress = -1;
                int lastOverallProgress = -1;
                int linesSinceLastCommit = 0;

                try {
                    server.openTransaction();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not open transaction for tumours import", ex);
                }

                try {
                    for (CSVRecord csvRecord : parser) {    
                        int currentTumourProgress = linesToRead > 0 ? (int) (((long) numberOfLinesRead * 100) / linesToRead) : 0;
                        int currentOverallProgress = 33 + (currentTumourProgress / 3);
                        if (task != null && (currentTumourProgress != lastTumourProgress || numberOfLinesRead % 100 == 0)) {
                            task.firePropertyChange(PROGRESS, lastOverallProgress, currentOverallProgress);
                            task.firePropertyChange(TUMOURS, lastTumourProgress, currentTumourProgress);
                            lastTumourProgress = currentTumourProgress;
                            lastOverallProgress = currentOverallProgress;
                        }

                        // Build tumour part
                        Tumour tumour = new Tumour();
                        for (Relation rel : tumourRelations) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                String cellVal = csvRecord.get(rel.getFileColumnNumber());
                                if (cellVal.length() > 0) {
                                    try {
                                        tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(cellVal));
                                    } catch (NumberFormatException ex) {
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d", numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                String value = csvRecord.get(rel.getFileColumnNumber());
                                if (rel.getDatabaseVariableName().equalsIgnoreCase(RAW_DATA_COLUMN)) {
                                    value = value.replace("@#$", " <br>");
                                }
                                tumour.setVariable(rel.getDatabaseVariableName(), value);
                            }
                        } 

                        String tumourID = (String) tumour.getVariable(io.getTumourIDVariablename());
                        String patientRecordID = (String) tumour.getVariable(io.getPatientRecordIDTumourTableVariableName());
                        
                        // patientRecordID IS A MUST if the tumour is new, because without that
                        // the new tumourID cannot be calculated!!!
                        if (patientRecordID == null || patientRecordID.isEmpty()) {
                            String patientID = tumour.getVariableAsString(io.getPatientIDTumourTableVariableName());
                            Patient matchedPatient = null;
                            if (patientID != null && !patientID.isEmpty()) {
                                matchedPatient = cache.getPatientByPID(patientID);
                            }
                            if (matchedPatient == null && tumourID != null && !tumourID.isEmpty()) {
                                matchedPatient = cache.getPatientByTID(tumourID);
                            }
                            if (matchedPatient == null && isSingleFile) {
                                matchedPatient = cache.getPatientByLineNumber(numberOfLinesRead + 1);
                            }

                            if (matchedPatient != null) {
                                patientRecordID = matchedPatient.getVariableAsString(io.getPatientRecordIDVariableName());
                                if (patientRecordID == null || patientRecordID.isEmpty()) {
                                    patientRecordID = matchedPatient.getVariableAsString(io.getTumourIDVariablename());
                                }
                                tumour.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);
                                tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                                if (tumour.getVariable(io.getPatientIDTumourTableVariableName()) == null) {
                                    tumour.setVariable(io.getPatientIDTumourTableVariableName(), matchedPatient.getVariableAsString(io.getPatientIDVariableName()));
                                }
                                if (patientRecordID != null && !patientRecordID.isEmpty()) {
                                    cache.putPatientByPRID(patientRecordID, matchedPatient);
                                }
                            } else if (patientID != null && !patientID.isEmpty()) {
                                Patient[] patients = null;
                                Patient cached = cache.getPatientByPID(patientID);
                                if (cached != null) {
                                    String cachedPrid = cached.getVariableAsString(io.getPatientRecordIDVariableName());
                                    if (cachedPrid != null && !cachedPrid.isEmpty()) {
                                        patients = new Patient[]{cached};
                                    } else {
                                        Object recIdObj = cached.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                                        if (recIdObj instanceof Integer && (Integer) recIdObj > 0) {
                                            try {
                                                DatabaseRecord dbRec = server.getRecord((Integer) recIdObj, Globals.PATIENT_TABLE_NAME, false, null);
                                                if (dbRec instanceof Patient) {
                                                    patients = new Patient[]{(Patient) dbRec};
                                                    cache.putPatientByPID(patientID, (Patient) dbRec);
                                                }
                                            } catch (Exception ignore) {
                                            }
                                        }
                                    }
                                }
                                if (patients == null) {
                                    try {
                                        if (CanRegClientApp.getApplication() != null) {
                                            patients = CanRegClientApp.getApplication().getPatientsByPatientID(patientID, false, server);
                                            if (patients != null && patients.length > 0 && patients[0] != null) {
                                                cache.putPatientByPID(patientID, patients[0]);
                                            }
                                        }
                                    } catch (IllegalStateException ignore) {
                                    }
                                }
                                if (patients != null && patients.length > 0 && patients[0] != null) {
                                    patientRecordID = patients[0].getVariableAsString(io.getPatientRecordIDVariableName());
                                    
                                    if (patientRecordID == null || patientRecordID.isEmpty()) {
                                        patientRecordID = patients[0].getVariableAsString(io.getTumourIDVariablename());
                                    }
                                    
                                    tumour.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);
                                    tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                                    cache.putPatientByPRID(patientRecordID, patients[0]);
                                }
                            }
                        }
                        
                        importTumour(server, io.getDiscrepancies(), tumourID, patientRecordID, 
                                     tumour, io, reportWriter, intoHoldingDB, io.isTestOnly(), false, cache);
                        
                        numberOfLinesRead++;
                        linesSinceLastCommit++;

                        if (linesSinceLastCommit >= BATCH_SIZE) {
                            try {
                                server.commitTransaction();
                                server.openTransaction();
                                linesSinceLastCommit = 0;
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Error committing tumour batch transaction", ex);
                            }
                        }

                        if (Thread.interrupted()) {
                            reportWriter.flush();
                            throw new InterruptedException();
                        }
                    }

                    if (linesSinceLastCommit > 0) {
                        try {
                            server.commitTransaction();
                            linesSinceLastCommit = 0;
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Error committing final tumour transaction", ex);
                        }
                    }
                } catch (Exception ex) {
                    try {
                        server.rollbackTransaction();
                    } catch (Exception ignore) {
                    }
                    throw ex;
                }

                parser.close();
                reportWriter.write("Finished reading tumours." + Globals.newline + Globals.newline);
                reportWriter.flush();
            }
            if (task != null) {
                task.firePropertyChange(TUMOURS, null, 100);
                task.firePropertyChange(PROGRESS, null, 66);
            }
            // then at last we get the sources
            if (task != null) {
                task.firePropertyChange(SOURCES, null, 0);
            }
            if (files[2] != null && !sourceRelations.isEmpty()) {
                reportWriter.write("Starting to import sources from " + files[2].getAbsolutePath() + Globals.newline);

                sourceFIS = new FileInputStream(files[2]);
                InputStreamReader sourceISR = new InputStreamReader(sourceFIS, io.getFileCharsets()[2]);

                LOGGER.log(Level.CONFIG, "Name of the character encoding {0}", sourceISR.getEncoding());

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

                int lastSourceProgress = -1;
                int lastOverallProgress = -1;
                int linesSinceLastCommit = 0;

                try {
                    server.openTransaction();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not open transaction for sources import", ex);
                }

                try {
                    for (CSVRecord csvRecord : parser) {   
                        int currentSourceProgress = linesToRead > 0 ? (int) (((long) numberOfLinesRead * 100) / linesToRead) : 0;
                        int currentOverallProgress = 66 + (currentSourceProgress / 3);
                        if (task != null && (currentSourceProgress != lastSourceProgress || numberOfLinesRead % 100 == 0)) {
                            task.firePropertyChange(PROGRESS, lastOverallProgress, currentOverallProgress);
                            task.firePropertyChange(SOURCES, lastSourceProgress, currentSourceProgress);
                            lastSourceProgress = currentSourceProgress;
                            lastOverallProgress = currentOverallProgress;
                        }

                        // Build source part
                        Source source = new Source();
                        for (Relation rel : sourceRelations) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                String cellVal = csvRecord.get(rel.getFileColumnNumber());
                                if (cellVal.length() > 0) {
                                    try {
                                        source.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(cellVal));
                                    } catch (NumberFormatException ex) {
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d", numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                String value = csvRecord.get(rel.getFileColumnNumber());
                                if (rel.getDatabaseVariableName().equalsIgnoreCase(RAW_DATA_COLUMN)) {
                                    value = value.replace("@#$", " <br>");
                                }
                                source.setVariable(rel.getDatabaseVariableName(), value);
                            }
                        }

                        String tumourID = (String) source.getVariable(io.getTumourIDSourceTableVariableName());                    
                        String sourceID = (String) source.getVariable(io.getSourceIDVariablename());
                        
                        importSource(server, io.getDiscrepancies(), sourceID, io.getSourceIDVariablename(), 
                                     source, tumourID, reportWriter, intoHoldingDB, io.isTestOnly(), false, cache);
                                            
                        numberOfLinesRead++;
                        linesSinceLastCommit++;

                        if (linesSinceLastCommit >= BATCH_SIZE) {
                            try {
                                server.commitTransaction();
                                server.openTransaction();
                                linesSinceLastCommit = 0;
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Error committing source batch transaction", ex);
                            }
                        }

                        if (Thread.interrupted()) {
                            reportWriter.flush();
                            throw new InterruptedException();
                        }
                    }

                    if (linesSinceLastCommit > 0) {
                        try {
                            server.commitTransaction();
                            linesSinceLastCommit = 0;
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Error committing final source transaction", ex);
                        }
                    }
                } catch (Exception ex) {
                    try {
                        server.rollbackTransaction();
                    } catch (Exception ignore) {
                    }
                    throw ex;
                }

                reportWriter.write("Finished reading sources." + Globals.newline + Globals.newline);
                reportWriter.flush();
                parser.close();                
            }
            if (task != null) {
                task.firePropertyChange(SOURCES, null, 100);
                task.firePropertyChange(PROGRESS, null, 100);
                while (!task.isProgressPropertyValid()) {
                    // wait untill progress has been updated...
                }
            }
            reportWriter.write("Finished" + Globals.newline);
            reportWriter.flush();
            success = true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, String.format("Error in line: %d",numberOfLinesRead + 1 + 1), ex);
            success = false;
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, String.format("Interupted on line : %d",numberOfLinesRead + 1), ex);
            success = true;
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.SEVERE, String.format("String too short error in line: %d",numberOfLinesRead + 1 + 1), ex);
            success = false;
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
            try {
                reportWriter.flush();
                reportWriter.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
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
            task.firePropertyChange(PATIENTS, null, 100);
            task.firePropertyChange(TUMOURS, null, 100);
            task.firePropertyChange(SOURCES, null, 100);
            task.firePropertyChange(PROGRESS, null, 100);
            task.firePropertyChange("finished", null, null);
        }
                
        return success;
    }

    /**
     * Import the data from a holding database into another one (most often the main database)
     * It will create one new patient record if the PatientRecordID is not found
     * Or it will patch the patient record it found with the data from the holding database
     * @param server
     * @param discrepancyOption What to do with the new data? REJECT/UPDATE/OVERWRITE
     * @param patientRecordID The PatientRecordID of the patient to import
     * @param patientToImport The Patient to import
     * @param reportWriter Report of the import
     * @param intoHoldingDB Is this import sends data to the holdingDatabase?
     * @param isTestOnly Is this for test purpose?
     * @param fromHoldingToProduction Should this send data into the main database?
     * @return
     * @throws SQLException
     * @throws SecurityException
     * @throws RecordLockedException
     * @throws UnknownTableException
     * @throws DistributedTableDescriptionException
     * @throws RemoteException
     * @throws IOException
     * @throws Exception
     */
    public static int importPatient(CanRegServerInterface server, int discrepancyOption,
                                     String patientRecordID, Patient patientToImport, Writer reportWriter, 
                                     boolean intoHoldingDB, boolean isTestOnly, 
                                     boolean fromHoldingToProduction, GlobalToolBox globalToolBox)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException,
                   IOException, Exception {
        return importPatient(server, discrepancyOption, patientRecordID, patientToImport, reportWriter,
                             intoHoldingDB, isTestOnly, fromHoldingToProduction, globalToolBox, null);
    }

    public static int importPatient(CanRegServerInterface server, int discrepancyOption,
                                     String patientRecordID, Patient patientToImport, Writer reportWriter, 
                                     boolean intoHoldingDB, boolean isTestOnly, 
                                     boolean fromHoldingToProduction, GlobalToolBox globalToolBox,
                                     ImportCache cache)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException,
                   IOException, Exception {
        if (patientToImport == null) {
            LOGGER.log(Level.WARNING, "importPatient called with null patientToImport for patientRecordID: {0}", patientRecordID);
            return -1;
        }

        Patient oldPatientRecord = null;
        if (cache != null && patientRecordID != null && !patientRecordID.isEmpty()) {
            oldPatientRecord = cache.getPatientByPRID(patientRecordID);
        }
        if (oldPatientRecord == null && patientRecordID != null && !patientRecordID.isEmpty()) {
            try {
                if (CanRegClientApp.getApplication() != null) {
                    oldPatientRecord = CanRegClientApp.getApplication().getPatientRecord(patientRecordID, false, server);
                    if (oldPatientRecord != null && cache != null) {
                        cache.putPatientByPRID(patientRecordID, oldPatientRecord);
                    }
                }
            } catch (Exception ex1) {
                // Patient not found in DB or application context unavailable.
                LOGGER.log(Level.WARNING, "Patient not found by PRID in DB", ex1);
            }
        }

        // Fallback: if patient not found by patientRecordID, try looking up by PatientID (RegNo)
        if (oldPatientRecord == null && globalToolBox != null && patientToImport != null) {
            DatabaseVariablesListElement patientIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
            if (patientIDVariable != null) {
                String patientID = patientToImport.getVariableAsString(patientIDVariable.getDatabaseVariableName());
                if (patientID != null && !patientID.isEmpty()) {
                    if (cache != null) {
                        oldPatientRecord = cache.getPatientByPID(patientID);
                    }
                    if (oldPatientRecord == null) {
                        try {
                            if (CanRegClientApp.getApplication() != null) {
                                Patient[] patients = CanRegClientApp.getApplication().getPatientsByPatientID(patientID, false, server);
                                if (patients != null && patients.length > 0 && patients[0] != null) {
                                    oldPatientRecord = patients[0];
                                    if (cache != null) {
                                        cache.putPatientByPID(patientID, oldPatientRecord);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Fallback patient lookup by PatientID failed: " + patientID, ex);
                        }
                    }
                }
            }
        }

        boolean savePatient = true;
        // if oldPatientRecord is not null, patch patientToImport with its data
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
                        break;
                    case ImportOptions.OVERWRITE:
    //                                oldPatientDatabaseRecordID = (Integer) oldPatientRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                        String overWriteReport = overwriteRecord(oldPatientRecord, patientToImport);
                        if (overWriteReport.length() > 0) {
                            reportWriter.write("Patient " + patientRecordID + Globals.newline + overWriteReport);
                        }
                        patientToImport = oldPatientRecord;
                        break;
                }
            }
        }

//      if (deletePatient) {
//           server.deleteRecord(oldPatientDatabaseRecordID, Globals.PATIENT_TABLE_NAME);
//      }

        if ( ! isTestOnly && savePatient) {
            int resultPRID = -1;
            DatabaseVariablesListElement patientIDVariable = globalToolBox != null
                    ? globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                    : null;
            DatabaseVariablesListElement patientRecordIDVariable = globalToolBox != null
                    ? globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                    : null;
            String originalPid = (patientIDVariable != null && patientToImport != null)
                    ? patientToImport.getVariableAsString(patientIDVariable.getDatabaseVariableName())
                    : null;

            if (oldPatientRecord != null) { // does the patient record already exist ?
    //              try {
                if (fromHoldingToProduction) {
                    server.editPatientFromHoldingToProduction(patientToImport);
                } else {
                    server.editPatient(patientToImport);
                }
    //              } catch(Exception ex) {
    //                  LOGGER.log(Level.SEVERE, "ERROR EDITING PATIENT " + patientID, ex);
    //              }
                Object prid = oldPatientRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                if (prid instanceof Integer) {
                    resultPRID = (Integer) prid;
                } else {
                    Object pridImport = patientToImport.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                    if (pridImport instanceof Integer) {
                        resultPRID = (Integer) pridImport;
                    }
                }
            } else {
    //              try {
                if (fromHoldingToProduction) {
                    // Reset RegNo and PatientRecordID when promoting from holding to production.
                    // The production database will assign fresh values during the saving process.
                    if (patientIDVariable != null) {
                        patientToImport.setVariable(patientIDVariable.getDatabaseVariableName(), "");
                    }
                    if (patientRecordIDVariable != null) {
                        patientToImport.setVariable(patientRecordIDVariable.getDatabaseVariableName(), "");
                    }
                }
                resultPRID = server.savePatient(patientToImport);
                if (!fromHoldingToProduction && resultPRID > 0) {
                    patientToImport.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, resultPRID);
                    String currentPrid = patientRecordIDVariable != null
                            ? patientToImport.getVariableAsString(patientRecordIDVariable.getDatabaseVariableName())
                            : null;
                    String currentPid = patientIDVariable != null
                            ? patientToImport.getVariableAsString(patientIDVariable.getDatabaseVariableName())
                            : null;
                    if (currentPrid == null || currentPrid.trim().isEmpty()
                            || currentPid == null || currentPid.trim().isEmpty()) {
                        try {
                            DatabaseRecord savedRec = server.getRecord(resultPRID, Globals.PATIENT_TABLE_NAME, false, null);
                            if (savedRec instanceof Patient) {
                                if (patientIDVariable != null) {
                                    String savedPid = savedRec.getVariableAsString(patientIDVariable.getDatabaseVariableName());
                                    if (savedPid != null && !savedPid.isEmpty()) {
                                        patientToImport.setVariable(patientIDVariable.getDatabaseVariableName(), savedPid);
                                    }
                                }
                                if (patientRecordIDVariable != null) {
                                    String savedPrid = savedRec.getVariableAsString(patientRecordIDVariable.getDatabaseVariableName());
                                    if (savedPrid != null && !savedPrid.isEmpty()) {
                                        patientToImport.setVariable(patientRecordIDVariable.getDatabaseVariableName(), savedPrid);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, "Could not fetch saved patient record after savePatient", ex);
                        }
                    }
                }
    //              } catch(Exception ex) {
    //                  LOGGER.log(Level.SEVERE, "ERROR SAVING PATIENT " + patientID, ex);
    //              }
            }

            if (cache != null && patientToImport != null) {
                if (patientRecordID != null && !patientRecordID.isEmpty()) {
                    cache.putPatientByPRID(patientRecordID, patientToImport);
                }
                if (originalPid != null && !originalPid.isEmpty()) {
                    cache.putPatientByPID(originalPid, patientToImport);
                }
                if (patientIDVariable != null) {
                    String pid = patientToImport.getVariableAsString(patientIDVariable.getDatabaseVariableName());
                    if (pid != null && !pid.isEmpty()) {
                        cache.putPatientByPID(pid, patientToImport);
                    }
                }
                if (patientRecordIDVariable != null) {
                    String pridStr = patientToImport.getVariableAsString(patientRecordIDVariable.getDatabaseVariableName());
                    if (pridStr != null && !pridStr.isEmpty()) {
                        cache.putPatientByPRID(pridStr, patientToImport);
                    }
                }
            }
            return resultPRID;
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
        importTumour(server, discrepancyOption, tumourID, patientRecordID, tumourToImport,
                     io, reportWriter, intoHoldingDB, isTestOnly, fromHoldingToProduction, null);
    }

    public static void importTumour(CanRegServerInterface server, int discrepancyOption,
                                    String tumourID, String patientRecordID, Tumour tumourToImport, 
                                    ImportOptions io, Writer reportWriter, 
                                    boolean intoHoldingDB, boolean isTestOnly,
                                    boolean fromHoldingToProduction,
                                    ImportCache cache)
            throws SQLException, SecurityException, RecordLockedException, 
                   UnknownTableException, DistributedTableDescriptionException, RemoteException, 
                   IOException, Exception {
        if (tumourToImport == null) {
            LOGGER.log(Level.WARNING, "importTumour called with null tumourToImport for tumourID: {0}", tumourID);
            return;
        }

        // see if this tumour exists in the database already
        // TODO: Implement this using arrays and getTumourRexords instead
        boolean saveTumour = true;
        boolean deleteTumour = false;
        
        ResultCode worstResultCodeFound;
        Tumour oldTumourRecord = null;
        
        if (cache != null && tumourID != null && !tumourID.isEmpty()) {
            oldTumourRecord = cache.getTumourByTID(tumourID);
        }
        if (oldTumourRecord == null && tumourID != null && !tumourID.isEmpty()) {
            try {
                if (CanRegClientApp.getApplication() != null) {
                    oldTumourRecord = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(tumourID, false, server);
                    if (oldTumourRecord != null && cache != null) {
                        cache.putTumourByTID(tumourID, oldTumourRecord);
                    }
                }
            } catch (Exception ex1) {
                // Tumour not found in DB or application context unavailable.
                LOGGER.log(Level.WARNING, "Tumour not found in DB", ex1);
            }
        }

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
                        if (updateReport.length() > 0 && reportWriter != null) {
                            reportWriter.write(tumourID + Globals.newline + updateReport);
                        }
                        if (tumourToImport.getSources() != null && !tumourToImport.getSources().isEmpty()) {
                            oldTumourRecord.setSources(tumourToImport.getSources());
                        }
                        tumourToImport = oldTumourRecord;
                        saveTumour = true;
                        break;
                    case ImportOptions.OVERWRITE:
                        String overwriteReport = overwriteRecord(oldTumourRecord, tumourToImport);
                        if (overwriteReport.length() > 0 && reportWriter != null) {
                            reportWriter.write(tumourID + Globals.newline + overwriteReport);
                        }
                        if (tumourToImport.getSources() != null && !tumourToImport.getSources().isEmpty()) {
                            oldTumourRecord.setSources(tumourToImport.getSources());
                        }
                        tumourToImport = oldTumourRecord;
                        deleteTumour = false;
                        saveTumour = true;
                        break;
                }
                // reportWriter.write(tumour.getVariable(io.getTumourIDVariablename()) + "Tumour already exists.\n");
            }
        }

        Patient patient = null;
        if (patientRecordID != null && !patientRecordID.isEmpty()) {
            if (cache != null) {
                patient = cache.getPatientByPRID(patientRecordID);
            }
            if (patient == null) {
                try {
                    if (CanRegClientApp.getApplication() != null) {
                        patient = CanRegClientApp.getApplication().getPatientRecord(patientRecordID, false, server);
                        if (patient != null && cache != null) {
                            cache.putPatientByPRID(patientRecordID, patient);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error looking up patient for patientRecordID: " + patientRecordID, ex);
                }
            }
        }

        // Fallback: if patient was not found by patientRecordID, try looking up by PatientID
        if (patient == null && tumourToImport != null) {
            String patientID = (io != null && io.getPatientIDTumourTableVariableName() != null)
                    ? tumourToImport.getVariableAsString(io.getPatientIDTumourTableVariableName()) : null;
            if (patientID != null && !patientID.isEmpty()) {
                String pridVarName = (io != null && io.getPatientRecordIDVariableName() != null)
                        ? io.getPatientRecordIDVariableName()
                        : Globals.StandardVariableNames.PatientRecordID.name();
                if (cache != null) {
                    Patient cachedPatient = cache.getPatientByPID(patientID);
                    if (cachedPatient != null) {
                        String cachedPrid = cachedPatient.getVariableAsString(pridVarName);
                        if (cachedPrid != null && !cachedPrid.isEmpty()) {
                            patient = cachedPatient;
                        } else {
                            Object recIdObj = cachedPatient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                            if (recIdObj instanceof Integer && (Integer) recIdObj > 0) {
                                try {
                                    DatabaseRecord dbRec = server.getRecord((Integer) recIdObj, Globals.PATIENT_TABLE_NAME, false, null);
                                    if (dbRec instanceof Patient) {
                                        patient = (Patient) dbRec;
                                        cache.putPatientByPID(patientID, patient);
                                    }
                                } catch (Exception ignore) {
                                }
                            }
                        }
                    }
                }
                if (patient == null) {
                    try {
                        if (CanRegClientApp.getApplication() != null) {
                            Patient[] patients = CanRegClientApp.getApplication().getPatientsByPatientID(patientID, false, server);
                            if (patients != null && patients.length > 0 && patients[0] != null) {
                                patient = patients[0];
                                if (cache != null) {
                                    cache.putPatientByPID(patientID, patient);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Fallback patient lookup by PatientID failed: " + patientID, ex);
                    }
                }
                if (patient != null) {
                    String resolvedPatientRecordID = patient.getVariableAsString(pridVarName);
                    if (resolvedPatientRecordID != null && !resolvedPatientRecordID.isEmpty()) {
                        patientRecordID = resolvedPatientRecordID;
                        if (cache != null) {
                            cache.putPatientByPRID(patientRecordID, patient);
                        }
                        if (io != null) {
                            if (io.getPatientRecordIDVariableName() != null) {
                                tumourToImport.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);
                            }
                            if (io.getPatientRecordIDTumourTableVariableName() != null) {
                                tumourToImport.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                            }
                        }
                    }
                }
            }
        }

        // Fallback 2: if patient was still not found, try looking up by TumourID in cache
        if (patient == null && tumourID != null && !tumourID.isEmpty() && cache != null) {
            patient = cache.getPatientByTID(tumourID);
            if (patient != null) {
                String pridVarName = (io != null && io.getPatientRecordIDVariableName() != null)
                        ? io.getPatientRecordIDVariableName()
                        : Globals.StandardVariableNames.PatientRecordID.name();
                String pidVarName = (io != null && io.getPatientIDVariableName() != null)
                        ? io.getPatientIDVariableName()
                        : Globals.StandardVariableNames.PatientID.name();
                String resolvedPatientRecordID = patient.getVariableAsString(pridVarName);
                if (resolvedPatientRecordID != null && !resolvedPatientRecordID.isEmpty()) {
                    patientRecordID = resolvedPatientRecordID;
                    cache.putPatientByPRID(patientRecordID, patient);
                    if (io != null) {
                        if (io.getPatientRecordIDVariableName() != null) {
                            tumourToImport.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);
                        }
                        if (io.getPatientRecordIDTumourTableVariableName() != null) {
                            tumourToImport.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);
                        }
                    }
                }
                String pid = patient.getVariableAsString(pidVarName);
                if (pid != null && !pid.isEmpty() && io != null && io.getPatientIDTumourTableVariableName() != null) {
                    tumourToImport.setVariable(io.getPatientIDTumourTableVariableName(), pid);
                }
            }
        }

        if (patient != null) {
            if (io != null) {
                String pridVar = io.getPatientRecordIDVariableName();
                String resolvedPrid = (pridVar != null) ? patient.getVariableAsString(pridVar) : null;
                if (resolvedPrid != null && !resolvedPrid.isEmpty()) {
                    patientRecordID = resolvedPrid;
                    if (io.getPatientRecordIDTumourTableVariableName() != null) {
                        tumourToImport.setVariable(io.getPatientRecordIDTumourTableVariableName(), resolvedPrid);
                    }
                }
                String pidVar = io.getPatientIDVariableName();
                String resolvedPid = (pidVar != null) ? patient.getVariableAsString(pidVar) : null;
                if (resolvedPid != null && !resolvedPid.isEmpty() && io.getPatientIDTumourTableVariableName() != null) {
                    tumourToImport.setVariable(io.getPatientIDTumourTableVariableName(), resolvedPid);
                }
            }
            generateMissingCodes(server, patient, tumourToImport, io);
            if (io != null) {
                String recStatusVarName = io.getTumourRecordStatus();
                if (recStatusVarName == null || recStatusVarName.isEmpty()) {
                    try {
                        GlobalToolBox globalToolBox = new GlobalToolBox(server.getDatabseDescription());
                        DatabaseVariablesListElement recStatusElem = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString());
                        if (recStatusElem != null) {
                            recStatusVarName = recStatusElem.getDatabaseVariableName();
                        }
                    } catch (Exception ignore) {
                    }
                }
                // Add record status as 1 by default if this column is missing
                if (recStatusVarName != null) {
                    Object currentRecStatus = tumourToImport.getVariable(recStatusVarName);
                    if (currentRecStatus == null || currentRecStatus.toString().trim().isEmpty()) {
                        tumourToImport.setVariable(recStatusVarName, "1");
                    }
                }

                if (io.isDoChecks() && saveTumour) {
                    // run the edits...
                    String message = "";
                    LinkedList<CheckResult> checkResults = runChecks(server, patient, tumourToImport);

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
                        // LOGGER.log(Level.INFO, result.toString());
                    }

                    // Update record status (recs) based on outcome of check:
                    // Errors change record status to 0, warnings keep 1.
                    if (worstResultCodeFound == CheckResult.ResultCode.Invalid || worstResultCodeFound == CheckResult.ResultCode.Missing) {
                        if (recStatusVarName != null) {
                            tumourToImport.setVariable(recStatusVarName, "0");
                        }
                    } else {
                        if (recStatusVarName != null) {
                            Object currentRecStatus = tumourToImport.getVariable(recStatusVarName);
                            if (currentRecStatus == null || currentRecStatus.toString().trim().isEmpty() || "1".equals(currentRecStatus.toString().trim())) {
                                tumourToImport.setVariable(recStatusVarName, "1");
                            }
                        }
                    }

                    if (worstResultCodeFound != CheckResult.ResultCode.OK && reportWriter != null) {
                        reportWriter.write(tumourID + "\t" + message + Globals.newline);
                    }
                    tumourToImport.setVariable(io.getTumourCheckStatus(), CheckResult.toDatabaseVariable(worstResultCodeFound));
                }
            }
        } else {
            saveTumour = false;
            if (reportWriter != null) {
                reportWriter.write(tumourID + "\t" + "No patient matches this Tumour. Tumour skipped." + Globals.newline);
            }
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
                if (oldTumourRecord != null) {
                    if (fromHoldingToProduction) {
                        server.editTumourFromHoldingToProduction(tumourToImport);
                    } else {
                        server.editTumour(tumourToImport);
                    }
                } else {
                    if (fromHoldingToProduction) {
                        // Reset TumourID when promoting a new tumour from holding to production
                        // so the production database will assign the next valid sequence TumourID.
                        GlobalToolBox globalToolBox = new GlobalToolBox(server.getDatabseDescription());
                        DatabaseVariablesListElement tumourIDVariable = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString());
                        if (tumourIDVariable != null) {
                            tumourToImport.setVariable(tumourIDVariable.getDatabaseVariableName(), "");
                        }
                    }
                    int savedId = server.saveTumour(tumourToImport);
                    if (!fromHoldingToProduction && savedId > 0) {
                        tumourToImport.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, savedId);
                    }
                }
                if (cache != null && tumourID != null && !tumourID.isEmpty()) {
                    cache.putTumourByTID(tumourID, tumourToImport);
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
        importSource(server, discrepancyOption, sourceRecordID, sourceIDVariablename,
                     sourceToImport, tumourID, reportWriter, intoHoldingDB, isTestOnly,
                     fromHoldingToProduction, null);
    }

    public static void importSource(CanRegServerInterface server, int discrepancyOption,
                                    String sourceRecordID, String sourceIDVariablename, 
                                    Source sourceToImport, String tumourID,  
                                    Writer reportWriter, 
                                    boolean intoHoldingDB, boolean isTestOnly, 
                                    boolean fromHoldingToProduction,
                                    ImportCache cache) 
            throws SecurityException, SQLException, RecordLockedException, 
                   DistributedTableDescriptionException, UnknownTableException, RemoteException,
                   IOException, Exception {
        if (sourceToImport == null) {
            LOGGER.log(Level.WARNING, "importSource called with null sourceToImport for sourceRecordID: {0}", sourceRecordID);
            return;
        }

        Tumour tumour = null;
        if (cache != null && tumourID != null && !tumourID.isEmpty()) {
            tumour = cache.getTumourByTID(tumourID);
        }
        if (tumour == null && tumourID != null && !tumourID.isEmpty()) {
            try {
                if (CanRegClientApp.getApplication() != null) {
                    tumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(tumourID, false, server);
                    if (tumour != null && cache != null) {
                        cache.putTumourByTID(tumourID, tumour);
                    }
                }
            } catch (Exception ex1) {
                LOGGER.log(Level.WARNING, "Tumour not found in DB for tumourID: " + tumourID, ex1);
            }
        }


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
                    if (tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) == null && CanRegClientApp.getApplication() != null) {
                        try {
                            Tumour dbTumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(tumourID, false, server);
                            if (dbTumour != null && dbTumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
                                tumour.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME,
                                        dbTumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME));
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, "Could not resolve tumour record ID from DB", ex);
                        }
                    }
                    if(fromHoldingToProduction)
                        server.editTumourFromHoldingToProduction(tumour);
                    else
                        server.editTumour(tumour);
//              } catch(Exception ex) {
//                  LOGGER.log(Level.SEVERE, 
//                                        "ERROR SAVING SOURCE " + sourceRecordID + 
//                                        " ON TUMOUR " + tumour.getVariable(io.getTumourIDVariablename()),
//                                        ex);
//              }
                    if (cache != null && tumourID != null && !tumourID.isEmpty()) {
                        cache.putTumourByTID(tumourID, tumour);
                    }
            }
        } else {
            LOGGER.log(Level.SEVERE, "No tumour with ID {0} was found for source ID {1}", new Object[]{tumourID, sourceRecordID});
            if (reportWriter != null) {
                reportWriter.write(sourceRecordID + "\t" + "No tumour matches this Source (Tumour ID: " + tumourID + "). Source skipped." + Globals.newline);
            }
        }
    }
    
    /**
     *
     * @param doc
     * @param lineElements
     * @return
     */
    public static List<Relation> constructRelations(Document doc, String[] lineElements) {
        return constructRelations(doc, lineElements, null, LearnedVariableMappings.getInstance());
    }

    public static List<Relation> constructRelations(Document doc, String[] lineElements, String targetTableName) {
        return constructRelations(doc, lineElements, targetTableName, LearnedVariableMappings.getInstance());
    }

    public static List<Relation> constructRelations(
            Document doc,
            String[] lineElements,
            String targetTableName,
            LearnedVariableMappings learnedMappings) {
        LinkedList<Relation> list = new LinkedList<Relation>();
        if (lineElements == null) {
            return list;
        }
        DatabaseVariablesListElement[] allVariables = canreg.common.Tools.getVariableListElements(doc, namespace);
        List<DatabaseVariablesListElement> candidateList = new ArrayList<DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement v : allVariables) {
            if (targetTableName == null || targetTableName.trim().isEmpty()
                    || v.getDatabaseTableName().equalsIgnoreCase(targetTableName)) {
                candidateList.add(v);
            }
        }
        DatabaseVariablesListElement[] candidateVariables = candidateList.toArray(new DatabaseVariablesListElement[candidateList.size()]);

        DatabaseVariablesListElement[] matchedByCol = new DatabaseVariablesListElement[lineElements.length];
        boolean[] learnedByCol = new boolean[lineElements.length];
        Set<Integer> claimedVarIds = new LinkedHashSet<Integer>();

        // Pass 1a: Exact match on shortName, standardVariableName, fullName, or englishName within candidateVariables
        for (int i = 0; i < lineElements.length; i++) {
            String colName = lineElements[i] != null ? lineElements[i].trim() : "";
            if (colName.isEmpty()) {
                continue;
            }
            DatabaseVariablesListElement exact = findExactVariableMatch(colName, candidateVariables, claimedVarIds);
            if (exact != null) {
                matchedByCol[i] = exact;
                claimedVarIds.add(exact.getDatabaseTableVariableID());
            }
        }

        // Pass 1b: Exact foreign-key match when targetTableName is Tumour or Source
        // (e.g., column "RegNo" or "PatientID" in Tumour file -> PatientIDTumourTable)
        if (targetTableName != null && !targetTableName.trim().isEmpty()) {
            for (int i = 0; i < lineElements.length; i++) {
                if (matchedByCol[i] != null) {
                    continue;
                }
                String colName = lineElements[i] != null ? lineElements[i].trim() : "";
                if (colName.isEmpty()) {
                    continue;
                }
                DatabaseVariablesListElement fkMatch = findForeignKeyVariableMatch(
                        colName, targetTableName, allVariables, candidateVariables, claimedVarIds);
                if (fkMatch != null) {
                    matchedByCol[i] = fkMatch;
                    claimedVarIds.add(fkMatch.getDatabaseTableVariableID());
                }
            }
        }

        // Pass 2: Learned manual mappings from previous imports on this installation
        if (learnedMappings != null) {
            for (int i = 0; i < lineElements.length; i++) {
                if (matchedByCol[i] != null) {
                    continue;
                }
                String colName = lineElements[i] != null ? lineElements[i].trim() : "";
                if (colName.isEmpty()) {
                    continue;
                }
                DatabaseVariablesListElement learned = learnedMappings.findLearnedVariable(
                        targetTableName, colName, candidateVariables);
                if (learned != null && !claimedVarIds.contains(learned.getDatabaseTableVariableID())) {
                    matchedByCol[i] = learned;
                    learnedByCol[i] = true;
                    claimedVarIds.add(learned.getDatabaseTableVariableID());
                }
            }
        }

        for (int i = 0; i < lineElements.length; i++) {
            Relation rel = new Relation();
            rel.setFileVariableName(lineElements[i]);
            rel.setFileColumnNumber(i);
            DatabaseVariablesListElement matched = matchedByCol[i];
            if (matched != null) {
                rel.setDatabaseTableName(matched.getDatabaseTableName());
                rel.setDatabaseTableVariableID(matched.getDatabaseTableVariableID());
                rel.setVariableType(matched.getVariableType());
                rel.setDatabaseVariableName(matched.getDatabaseVariableName());
                rel.setLearnedMapping(learnedByCol[i]);
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

    private static DatabaseVariablesListElement findExactVariableMatch(
            String colName,
            DatabaseVariablesListElement[] candidateVariables,
            Set<Integer> claimedVarIds) {
        // 1. Short name (databaseVariableName)
        for (DatabaseVariablesListElement v : candidateVariables) {
            if (!claimedVarIds.contains(v.getDatabaseTableVariableID())
                    && v.getDatabaseVariableName() != null
                    && colName.equalsIgnoreCase(v.getDatabaseVariableName().trim())) {
                return v;
            }
        }
        // 2. Standard variable name
        for (DatabaseVariablesListElement v : candidateVariables) {
            if (!claimedVarIds.contains(v.getDatabaseTableVariableID())
                    && v.getStandardVariableName() != null
                    && colName.equalsIgnoreCase(v.getStandardVariableName().trim())) {
                return v;
            }
        }
        // 3. Full name or English name
        for (DatabaseVariablesListElement v : candidateVariables) {
            if (!claimedVarIds.contains(v.getDatabaseTableVariableID())) {
                if (v.getFullName() != null && colName.equalsIgnoreCase(v.getFullName().trim())) {
                    return v;
                }
                if (v.getEnglishName() != null && colName.equalsIgnoreCase(v.getEnglishName().trim())) {
                    return v;
                }
            }
        }
        return null;
    }

    private static DatabaseVariablesListElement findForeignKeyVariableMatch(
            String colName,
            String targetTableName,
            DatabaseVariablesListElement[] allVariables,
            DatabaseVariablesListElement[] candidateVariables,
            Set<Integer> claimedVarIds) {
        String targetStandardName = null;
        if (Globals.TUMOUR_TABLE_NAME.equalsIgnoreCase(targetTableName)) {
            if (matchesParentVariable(colName, Globals.PATIENT_TABLE_NAME,
                    Globals.StandardVariableNames.PatientID.toString(), allVariables)) {
                targetStandardName = Globals.StandardVariableNames.PatientIDTumourTable.toString();
            } else if (matchesParentVariable(colName, Globals.PATIENT_TABLE_NAME,
                    Globals.StandardVariableNames.PatientRecordID.toString(), allVariables)) {
                targetStandardName = Globals.StandardVariableNames.PatientRecordIDTumourTable.toString();
            }
        } else if (Globals.SOURCE_TABLE_NAME.equalsIgnoreCase(targetTableName)) {
            if (matchesParentVariable(colName, Globals.TUMOUR_TABLE_NAME,
                    Globals.StandardVariableNames.TumourID.toString(), allVariables)) {
                targetStandardName = Globals.StandardVariableNames.TumourIDSourceTable.toString();
            }
        }
        if (targetStandardName != null) {
            for (DatabaseVariablesListElement v : candidateVariables) {
                if (!claimedVarIds.contains(v.getDatabaseTableVariableID())
                        && v.getStandardVariableName() != null
                        && v.getStandardVariableName().equalsIgnoreCase(targetStandardName)) {
                    return v;
                }
            }
        }
        return null;
    }

    private static boolean matchesParentVariable(
            String colName,
            String parentTableName,
            String parentStandardName,
            DatabaseVariablesListElement[] allVariables) {
        if (colName.equalsIgnoreCase(parentStandardName)) {
            return true;
        }
        for (DatabaseVariablesListElement v : allVariables) {
            if (v.getDatabaseTableName().equalsIgnoreCase(parentTableName)
                    && v.getStandardVariableName() != null
                    && v.getStandardVariableName().equalsIgnoreCase(parentStandardName)) {
                if (v.getDatabaseVariableName() != null && colName.equalsIgnoreCase(v.getDatabaseVariableName().trim())) {
                    return true;
                }
                if (v.getFullName() != null && colName.equalsIgnoreCase(v.getFullName().trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void debugOut(String msg) {
        if (debug) {
            LOGGER.log(Level.INFO, msg);
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

    private static boolean isInternalRecordIdVariable(String variableName) {
        return variableName != null && (
                variableName.equalsIgnoreCase(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) ||
                variableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) ||
                variableName.equalsIgnoreCase(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME)
        );
    }

    /**
     * patches the data from oldRecord using the data from newRecord
     * @param oldRecord
     * @param newRecord
     * @return a report of the differences between oldRecord and newRecord
     */
    private static String updateRecord(DatabaseRecord oldRecord, DatabaseRecord newRecord) {
        if (oldRecord == null || newRecord == null) {
            return "";
        }
        String report = "";
        // get all the elements from the newRecord and put them in the oldRecord
        for (String variableName : newRecord.getVariableNames()) {
            if (isInternalRecordIdVariable(variableName)) {
                continue;
            }
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

    /**
     * Overwrites oldRecord's values and set newRecord value
     * @param oldRecord
     * @param newRecord
     * @return a report of the differences between oldRecord and newRecord
     */
    private static String overwriteRecord(DatabaseRecord oldRecord, DatabaseRecord newRecord) {
        if (oldRecord == null || newRecord == null) {
            return "";
        }
        String report = "";
        // clear variables from oldRecord (except internal database record IDs)
        for (String variableName : oldRecord.getVariableNames()) {
            if (!isInternalRecordIdVariable(variableName)) {
                oldRecord.setVariable(variableName, null);
            }
        }
        for (String variableName : newRecord.getVariableNames()) {
            if (isInternalRecordIdVariable(variableName)) {
                continue;
            }
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

    private static GlobalToolBox resolveGlobalToolBox(CanRegServerInterface server) {
        try {
            if (CanRegClientApp.getApplication() != null) {
                GlobalToolBox gtb = CanRegClientApp.getApplication().getGlobalToolBox();
                if (gtb != null) {
                    return gtb;
                }
            }
        } catch (Exception ignore) {
        }
        try {
            if (server != null && server.getDatabseDescription() != null) {
                return new GlobalToolBox(server.getDatabseDescription());
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    private static LinkedList<CheckResult> runChecks(CanRegServerInterface server, Patient patient, Tumour tumour) {
        try {
            if (CanRegClientApp.getApplication() != null) {
                LinkedList<CheckResult> res = CanRegClientApp.getApplication().performChecks(patient, tumour);
                if (res != null) {
                    return res;
                }
            }
        } catch (Exception ignore) {
        }
        GlobalToolBox gtb = resolveGlobalToolBox(server);
        if (gtb != null) {
            Checker fallbackChecker = new Checker(gtb.getStandardVariables());
            return fallbackChecker.performChecks(patient, tumour);
        }
        return new LinkedList<CheckResult>();
    }

    private static ConversionResult[] runConversion(
            CanRegServerInterface server, GlobalToolBox gtb,
            Converter.ConversionName conversionName, Patient patient, Tumour tumour) {
        try {
            if (CanRegClientApp.getApplication() != null) {
                ConversionResult[] res = CanRegClientApp.getApplication().performConversions(conversionName, patient, tumour);
                if (res != null) {
                    return res;
                }
            }
        } catch (Exception ignore) {
        }
        if (gtb == null) {
            gtb = resolveGlobalToolBox(server);
        }
        if (gtb != null) {
            Converter fallbackConverter = new Converter(gtb.getStandardVariables());
            return fallbackConverter.performConversion(conversionName, patient, tumour);
        }
        return null;
    }

    private static final ConversionICDO3toICD10 ICDO3_TO_ICD10_HELPER = new ConversionICDO3toICD10();

    private static void generateMissingCodes(CanRegServerInterface server, Patient patient, Tumour tumour, ImportOptions io) {
        if (patient == null || tumour == null) {
            return;
        }
        GlobalToolBox gtb = resolveGlobalToolBox(server);

        if (gtb != null) {
            String topVarName = gtb.translateStandardVariableNameToDatabaseVariableName(
                    Globals.StandardVariableNames.Topography.toString());
            if (topVarName != null) {
                String topVal = tumour.getVariableAsString(topVarName);
                if (topVal != null && !topVal.trim().isEmpty()) {
                    String normalizedTop = topVal.trim();
                    if (normalizedTop.startsWith("C") || normalizedTop.startsWith("c")) {
                        normalizedTop = normalizedTop.substring(1);
                    }
                    normalizedTop = normalizedTop.replace(".", "").trim();
                    while (!normalizedTop.isEmpty() && normalizedTop.length() < 3) {
                        normalizedTop = "0" + normalizedTop;
                    }
                    if (!normalizedTop.equals(topVal)) {
                        tumour.setVariable(topVarName, normalizedTop);
                    }
                }
            }

            String morVarName = gtb.translateStandardVariableNameToDatabaseVariableName(
                    Globals.StandardVariableNames.Morphology.toString());
            String behVarName = gtb.translateStandardVariableNameToDatabaseVariableName(
                    Globals.StandardVariableNames.Behaviour.toString());
            if (morVarName != null) {
                String morVal = tumour.getVariableAsString(morVarName);
                if (morVal != null && !morVal.trim().isEmpty()) {
                    String cleanedMor = morVal.trim().replace("/", "");
                    if (cleanedMor.length() == 5) {
                        if (behVarName != null) {
                            String behVal = tumour.getVariableAsString(behVarName);
                            if (behVal == null || behVal.trim().isEmpty()) {
                                tumour.setVariable(behVarName, cleanedMor.substring(4, 5));
                            }
                        }
                        cleanedMor = cleanedMor.substring(0, 4);
                        tumour.setVariable(morVarName, cleanedMor);
                    } else if (cleanedMor.length() > 4) {
                        cleanedMor = cleanedMor.substring(0, 4);
                        tumour.setVariable(morVarName, cleanedMor);
                    } else if (!cleanedMor.equals(morVal)) {
                        tumour.setVariable(morVarName, cleanedMor);
                    }

                    if (behVarName != null) {
                        String behVal = tumour.getVariableAsString(behVarName);
                        if (behVal == null || behVal.trim().isEmpty()) {
                            String inferredBeh = ICDO3_TO_ICD10_HELPER.inferBehaviourFromMorphology(cleanedMor);
                            if (inferredBeh != null && !inferredBeh.isEmpty()) {
                                tumour.setVariable(behVarName, inferredBeh);
                            }
                        }
                    }
                }
            }
        }

        String icd10VarName = (io != null && io.getICD10VariableName() != null)
                ? io.getICD10VariableName()
                : (gtb != null ? gtb.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.ICD10.toString()) : null);
        if (icd10VarName != null) {
            String icd10 = tumour.getVariableAsString(icd10VarName);
            if (icd10 == null || icd10.trim().isEmpty()) {
                ConversionResult[] conversionResult = runConversion(server, gtb, Converter.ConversionName.ICDO3toICD10, patient, tumour);
                if (conversionResult != null && conversionResult.length > 0 && conversionResult[0].getValue() != null) {
                    String val = conversionResult[0].getValue().toString().trim();
                    if (!val.isEmpty() && !val.startsWith("?")) {
                        tumour.setVariable(icd10VarName, val);
                    }
                }
            }
        }

        String icccVarName = (io != null && io.getICCCVariableName() != null)
                ? io.getICCCVariableName()
                : (gtb != null ? gtb.translateStandardVariableNameToDatabaseVariableName(Globals.StandardVariableNames.ICCC.toString()) : null);
        if (icccVarName != null) {
            String iccc = tumour.getVariableAsString(icccVarName);
            if (iccc == null || iccc.trim().isEmpty()) {
                ConversionResult[] conversionResult = runConversion(server, gtb, Converter.ConversionName.ICDO3toICCC3, patient, tumour);
                if (conversionResult != null && conversionResult.length > 0 && conversionResult[0].getValue() != null) {
                    String val = conversionResult[0].getValue().toString().trim();
                    if (!val.isEmpty() && !val.startsWith("?")) {
                        tumour.setVariable(icccVarName, val);
                    }
                }
            }
        }
    }
}
