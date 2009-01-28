/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.dataentry;

import canreg.server.CanRegServerInterface;
import canreg.server.database.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    public static boolean importFile(Task<Object, Void> task, Document doc, List<canreg.client.dataentry.Relation> map, File file, CanRegServerInterface server, ImportOptions io) throws SQLException {
        boolean success = false;

        HashMap mpCodes = new HashMap();

        BufferedReader bufferedReader = null;
        try {
            // Tro to detect the encoding...
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, io.getFileCharset());
            // Returns the name of the character encoding 
            System.out.println("Name of the character encoding " + isr.getEncoding());

            int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(file.getAbsolutePath()) - 1;
            bufferedReader = new BufferedReader(isr);
            String line = bufferedReader.readLine();
            // Skip first line
            line = bufferedReader.readLine();
            // patientNumber
            int patientDatabaseRecordID = 0;
            int tumourDatabaseIDNumber = 0;
            int numberOfLinesRead = 1;
            int linesToRead = io.getMaxLines();
            if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                linesToRead = numberOfRecordsInFile;
            }
            while (line != null && (numberOfLinesRead < linesToRead)) {
                // We allow for null tasks...
                if (task != null) {
                    task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
                }
                String[] lineElements = canreg.common.Tools.breakDownLine(io.getSeparator(), line);
                // Build patient part
                Patient patient = new Patient();
                for (int i = 0; i < map.size(); i++) {
                    Relation rel = map.get(i);
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("patient")) {
                        if (rel.getVariableType().equalsIgnoreCase("Number") || rel.getVariableType().equalsIgnoreCase("Date")) {
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
                        if (rel.getVariableType().equalsIgnoreCase("Number") || rel.getVariableType().equalsIgnoreCase("Date")) {
                            if (lineElements[rel.getFileColumnNumber()].length() > 0) {
                                tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(lineElements[rel.getFileColumnNumber()]));
                            }
                        } else {
                            tumour.setVariable(rel.getDatabaseVariableName(), lineElements[rel.getFileColumnNumber()]);
                        }
                    }
                }

                // debugOut(tumour.toString());
                // add patient to the database

                if (io.isDataFromPreviousCanReg()) {
                    // set update date for the patient the same as for the tumour
                    Object updateDate = tumour.getVariable(io.getTumourUpdateDateVariableName());
                    patient.setVariable(io.getPatientUpdateDateVariableName(), updateDate);

                    // Set the patientID the same as the tumourID initially
                    Object patientID = tumour.getVariable(io.getTumourIDVariablename());
                    Object patientRecordID = patientID;

                    // And store the record ID
                    patient.setVariable(io.getPatientRecordIDVariableName(), patientRecordID);

                    // If this is a multiple primary tumour...
                    String mpCodeString = (String) tumour.getVariable(io.getMultiplePrimaryVariableName());
                    if (mpCodeString != null && mpCodeString.length() > 0) {
                        patientID = lookUpPatientID(mpCodeString, patientID, mpCodes);
                    }
                    //
                    patient.setVariable(io.getPatientIDVariableName(), patientID);

                    // Set the patient ID number on the tumour
                    tumour.setVariable(io.getPatientIDTumourTableVariableName(), patientID);
                    tumour.setVariable(io.getPatientRecordIDTumourTableVariableName(), patientRecordID);

                    // Set the deprecated flag to 0 - no obsolete records from CR4
                    tumour.setVariable(io.getObsoleteTumourFlagVariableName(), 0);
                    patient.setVariable(io.getObsoletePatientFlagVariableName(), 0);
                }

                patientDatabaseRecordID = server.savePatient(patient);
                tumourDatabaseIDNumber = server.saveTumour(tumour);

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
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (NumberFormatException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (InterruptedException ex) {
            success = true;
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
            System.out.println("\t[QueryGenerator] " + msg);
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
