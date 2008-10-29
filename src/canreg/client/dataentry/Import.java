/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.dataentry;

import canreg.server.CanRegServerInterface;
import canreg.server.database.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
 * @author morten
 */
public class Import {

    private static String namespace = "ns3:";
    private static boolean debug = true;

    // function without map - directly on the server...
    // deprecated
    /**
     * 
     * @param doc
     * @param file
     * @param canRegDAO
     * @return
     */
    public static boolean importFile(Document doc, File file, CanRegDAO canRegDAO) {
        // create the mapping
        BufferedReader bufferedReader = null;
        List<Relation> map = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            String[] lineElements = canreg.common.Tools.breakDownLine('\t', line);
            map = constructRelations(doc, lineElements);
        // call import function with map

        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
            }
            return importFile(doc, map, file, canRegDAO);
        }
    }

    // function with map - directly on the server...
    // deprecated
    /**
     * 
     * @param doc
     * @param map
     * @param file
     * @param canRegDAO
     * @return
     */
    public static boolean importFile(Document doc, List<Relation> map, File file, CanRegDAO canRegDAO) {

        boolean success = false;

        HashMap mpCodes = new HashMap();

        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            // Skip first line
            line = bufferedReader.readLine();

            // patientNumber
            int patientIDNumber = 0;
            while (line != null) {
                String[] lineElements = canreg.common.Tools.breakDownLine('\t', line);
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
                debugOut(patient.toString());

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

                debugOut(tumour.toString());
                // add patient to the database
                patientIDNumber = canRegDAO.savePatient(patient);

                // If this is a multiple primary tumour...
                String mpCodeString = (String) tumour.getVariable("MPcode");

                if (mpCodeString != null && mpCodeString.length() > 0) {
                    patientIDNumber = lookUpPatientID(mpCodeString, patientIDNumber, mpCodes);
                }

                tumour.setVariable("PatientID", patientIDNumber);
                canRegDAO.saveTumour(tumour);

                //Read next line of data
                line = bufferedReader.readLine();
            }


            success = true;
        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return success;
    }

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
    public static boolean importFile(Task<Object, Void> task, Document doc, List<canreg.client.dataentry.Relation> map, File file, CanRegServerInterface server, ImportOptions io) {

        boolean success = false;

        HashMap mpCodes = new HashMap();

        BufferedReader bufferedReader = null;
        try {
            int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(file.getAbsolutePath()) - 1;
            bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            // Skip first line
            line = bufferedReader.readLine();
            // patientNumber
            int patientDatabaseIDNumber = 0;
            int tumourDatabaseIDNumber = 0;
            int numberOfLinesRead = 1;
            int linesToRead = io.getMaxLines();
            if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                linesToRead = numberOfRecordsInFile;
            }
            while (line != null && (numberOfLinesRead < linesToRead)) {
                // We allow for null tasks...
                if (task!=null)
                    task.firePropertyChange("progress", (numberOfLinesRead - 1)*100/linesToRead, (numberOfLinesRead)*100/linesToRead);
                String[] lineElements = line.split(io.getSeparator());
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
                patientDatabaseIDNumber = server.savePatient(patient);

                // If this is a multiple primary tumour...
                String mpCodeString = (String) tumour.getVariable("MPcode");

                if (mpCodeString != null && mpCodeString.length() > 0) {
                    patientDatabaseIDNumber = lookUpPatientID(mpCodeString, patientDatabaseIDNumber, mpCodes);
                }
                
                //Set the patient ID number 
                tumour.setVariable("PatientID",patientDatabaseIDNumber);
                tumourDatabaseIDNumber = server.saveTumour(tumour);
                
                //Set the tumour ID number
                patient.setVariable("TumourID", tumourDatabaseIDNumber);
                patient.setVariable("id", patientDatabaseIDNumber);
                // and update it
                server.editPatient(patient);
                
                //Read next line of data
                line = bufferedReader.readLine();
                numberOfLinesRead++;
            }


            success = true;
        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
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
     */
    public static void importDictionary() {
        // TODO!
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

    private static int lookUpPatientID(String mpCodeString, int patientIDNumber, HashMap mpCodes) {
        Object IDNumberObj = mpCodes.get(mpCodeString);
        int id = patientIDNumber;
        if (IDNumberObj == null) {
            mpCodes.put(mpCodeString, patientIDNumber);
        } else {
            id = (Integer) IDNumberObj;
        }
        return id;
    }
}
