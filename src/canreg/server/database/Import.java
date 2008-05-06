/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    // function without map
    public static boolean importFile(Document doc, File file, CanRegDAO canRegDAO) {
        // create the mapping
        List<Relation> map = constructRelations(doc, file);
        // call import function with map
        return importFile(doc, map, file, canRegDAO);
    }

    // function with map
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
                    if (rel.getDatabaseTableName().equalsIgnoreCase("patient")) {
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
                    if (rel.getDatabaseTableName().equalsIgnoreCase("tumour")) {
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
                                
                if (mpCodeString !=null && mpCodeString.length()>0) {
                    patientIDNumber = lookUpPatientID(mpCodeString, patientIDNumber, mpCodes);
                }
                
                tumour.setVariable("PatientID",patientIDNumber);
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

    private static List<Relation> constructRelations(Document doc, File file) {
        BufferedReader bufferedReader = null;
        LinkedList<Relation> list = new LinkedList();
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            String[] lineElements = canreg.common.Tools.breakDownLine('\t', line);
            NodeList nl = doc.getElementsByTagName(namespace + "variable");
            String[] variableNames = new String[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                variableNames[i] = e.getElementsByTagName(namespace + "short_name").item(0).getTextContent();
            }
            for (int i = 0; i < lineElements.length; i++) {
                boolean found = false;
                int j = 0;
                while (!found && j < variableNames.length) {
                    found = lineElements[i].equalsIgnoreCase(variableNames[j++]);
                }
                if (found) {
                    //backtrack
                    j--;
                    //build relation
                    Relation rel = new Relation();
                    Element e = (Element) nl.item(j);
                    rel.setDatabaseTableName(e.getElementsByTagName(namespace + "table").item(0).getTextContent());
                    rel.setDatabaseTableVariableID(Integer.parseInt(e.getElementsByTagName(namespace + "variable_id").item(0).getTextContent()));
                    rel.setVariableType(e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent());
                    rel.setDatabaseVariableName(variableNames[j]);
                    rel.setFileColumnNumber(i);
                    list.add(rel);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, ex);
            }
            return list;
        }
    }

    private static void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[QueryGenerator] " + msg);
        }
    }

    private static int lookUpPatientID(String mpCodeString, int patientIDNumber, HashMap mpCodes) {
        Object IDNumberObj = mpCodes.get(mpCodeString);
        int id = patientIDNumber;
        if (IDNumberObj==null) {
            mpCodes.put( mpCodeString,patientIDNumber);
        } else {
            id = (Integer) IDNumberObj;
        }
        return id;
    }
}
