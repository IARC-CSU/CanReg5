/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.dataentry;

import canreg.client.CanRegClientApp;
import canreg.common.Globals;
import canreg.server.CanRegServerInterface;
import canreg.server.database.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
        noNeedToLookAtPatientVariables.add(io.getPatientIDVariableName().toLowerCase());
        noNeedToLookAtPatientVariables.add(io.getPatientRecordIDVariableName().toLowerCase());
        HashMap mpCodes = new HashMap();
        int numberOfLinesRead = 0;
        BufferedReader bufferedReader = null;
        try {
            // Tro to detect the encoding...
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, io.getFileCharset());
            // Returns the name of the character encoding

            Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding " + isr.getEncoding());

            int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(file.getAbsolutePath());
            bufferedReader = new BufferedReader(isr);
            String line = bufferedReader.readLine();
            // Skip first line
            line = bufferedReader.readLine();
            // patientNumber

            int linesToRead = io.getMaxLines();
            if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                linesToRead = numberOfRecordsInFile;
            }
            while (line != null && (numberOfLinesRead < linesToRead)) {
                // We allow for null tasks...
                boolean needToSavePatientAgain = true;
                int patientDatabaseRecordID = -1;

                if (task != null) {
                    task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
                }
                String[] lineElements = canreg.common.Tools.breakDownLine(io.getSeparator(), line);
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
                }
                // debugOut(patient.toString());

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
                            tumour.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
                        }
                    }
                }

                // Build source part
                Set<Source> sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
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
                }
                sources.add(source);
                tumour.setSources(sources);

                // debugOut(tumour.toString());
                // add patient to the database
                Object patientID = patient.getVariable(io.getPatientIDVariableName());

                if (patientID == null) {
                    // save the record to get the new patientID;
                    patientDatabaseRecordID = server.savePatient(patient);
                    patient = (Patient) server.getRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME, false);
                    patientID = patient.getVariable(io.getPatientIDVariableName());
                }

                if (io.isDataFromPreviousCanReg()) {
                    // set update date for the patient the same as for the tumour
                    Object updateDate = tumour.getVariable(io.getTumourUpdateDateVariableName());
                    patient.setVariable(io.getPatientUpdateDateVariableName(), updateDate);

                    // Set the patientID the same as the tumourID initially

                    Object tumourSequence = tumour.getVariable(io.getTumourSequenceVariableName());
                    String tumourSequenceString = tumourSequence + "";
                    while (tumourSequenceString.length() < Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD) {
                        tumourSequenceString = "0" + tumourSequenceString;
                    }
                    Object patientRecordID = patientID + "" + tumourSequenceString;

                    // If this is a multiple primary tumour...
                    String mpCodeString = (String) tumour.getVariable(io.getMultiplePrimaryVariableName());
                    if (mpCodeString != null && mpCodeString.length() > 0) {
                        patientID = lookUpPatientID(mpCodeString, patientID, mpCodes);
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
                if (needToSavePatientAgain) {
                    if (patientDatabaseRecordID > 0) {
                        server.editPatient(patient);
                    } else {
                        patientDatabaseRecordID = server.savePatient(patient);
                    }
                }

                int tumourDatabaseIDNumber = server.saveTumour(tumour);

                //Read next line of data
                line = bufferedReader.readLine();
                numberOfLinesRead++;

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
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        task.firePropertyChange("finished", null, null);
        return success;
    }

    public static boolean importFiles(Task<Object, Void> task, Document doc, List<canreg.client.dataentry.Relation> map, File[] files, CanRegServerInterface server, ImportOptions io) throws SQLException, RemoteException, SecurityException, RecordLockedException {
        boolean success = false;
        Set<String> noNeedToLookAtPatientVariables = new TreeSet<String>();
        noNeedToLookAtPatientVariables.add(io.getPatientIDVariableName().toLowerCase());
        noNeedToLookAtPatientVariables.add(io.getPatientRecordIDVariableName().toLowerCase());
        HashMap mpCodes = new HashMap();
        int numberOfLinesRead = 0;
        BufferedReader bufferedReader = null;
        int linesToRead = io.getMaxLines();
        String[] lineElements;
        try {
            // first we get the patients
            FileInputStream patientFIS = new FileInputStream(files[0]);
            InputStreamReader patientISR = new InputStreamReader(patientFIS, io.getFileCharsets()[0]);

            Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding " + patientISR.getEncoding());

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
                boolean needToSavePatientAgain = true;
                int patientDatabaseRecordID = -1;

                if (task != null) {
                    task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
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
                }
                // debugOut(patient.toString());

                // debugOut(tumour.toString());
                // add patient to the database
                Object patientID = patient.getVariable(io.getPatientIDVariableName());

                if (patientID == null) {
                    // save the record to get the new patientID;
                    patientDatabaseRecordID = server.savePatient(patient);
                    patient = (Patient) server.getRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME, false);
                    patientID = patient.getVariable(io.getPatientIDVariableName());
                }

                if (needToSavePatientAgain) {
                    if (patientDatabaseRecordID > 0) {
                        server.editPatient(patient);
                    } else {
                        patientDatabaseRecordID = server.savePatient(patient);
                    }
                }

                //Read next line of data
                line = bufferedReader.readLine();
                numberOfLinesRead++;

                if (Thread.interrupted()) {
                    //We've been interrupted: no more importing.
                    throw new InterruptedException();
                }
            }




            // then we get the tumours

            patientFIS = new FileInputStream(files[1]);
            patientISR = new InputStreamReader(patientFIS, io.getFileCharsets()[1]);

            Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding " + patientISR.getEncoding());

            numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[1].getAbsolutePath());
            bufferedReader = new BufferedReader(patientISR);
            line = bufferedReader.readLine();
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
                    task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
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
                }

                int tumourDatabaseIDNumber = server.saveTumour(tumour);

                //Read next line of data
                line = bufferedReader.readLine();
                numberOfLinesRead++;

                if (Thread.interrupted()) {
                    //We've been interrupted: no more importing.
                    throw new InterruptedException();
                }
            }




            // then at last we get the sources
            patientFIS = new FileInputStream(files[2]);
            patientISR = new InputStreamReader(patientFIS, io.getFileCharsets()[2]);

            Logger.getLogger(Import.class.getName()).log(Level.CONFIG, "Name of the character encoding " + patientISR.getEncoding());

            numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(files[2].getAbsolutePath());
            bufferedReader = new BufferedReader(patientISR);
            line = bufferedReader.readLine();
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
                    task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
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
                }

                Tumour tumour = new Tumour();
                try {
                    tumour = CanRegClientApp.getApplication().getTumourRecordBasedOnTumourID(
                            (String) source.getVariable(io.getTumourIDSourceTableVariableName()), false);
                } catch (Exception ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                }
                Set<Source> sources = tumour.getSources();
                sources.add(source);
                // get the tumour...
                tumour.setSources(sources);

                int tumourDatabaseIDNumber = server.saveTumour(tumour);

                //Read next line of data
                line = bufferedReader.readLine();
                numberOfLinesRead++;

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
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
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
}
