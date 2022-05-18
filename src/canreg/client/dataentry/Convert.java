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
 * @author Hemant Dharam Dhivar, IARC-Mumbai Regional Hub,
 * hemant_dhivar@yahoo.com
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.client.dataentry;

import canreg.client.gui.importers.ImportOptions;
import canreg.client.gui.importers.Import;
import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.Globals;
import canreg.client.CanRegClientApp;
import canreg.client.gui.importers.ImportView;
import canreg.server.management.SystemDescription;
import canreg.server.CanRegServerInterface;
import canreg.server.database.*;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.database.NameSexRecord;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tools;
import canreg.common.database.Tumour;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.RemoteException;
import java.util.Objects;

import javax.xml.parsers.*;
import javax.swing.JOptionPane;

import org.w3c.dom.*;
import org.apache.commons.lang.StringEscapeUtils;
import com.googlecode.paradox.ParadoxConnection;
import com.googlecode.paradox.metadata.ParadoxTable;
import com.googlecode.paradox.data.TableData;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.xml.sax.SAXException;

public class Convert {

    static boolean debug = true;
    static FileWriter txt_fw, csv_fw;
    static BufferedWriter txt_bw, csv_bw;
    static ParadoxConnection pconn;
    private static final Logger LOGGER = Logger.getLogger(Convert.class.getName());

    public static boolean convertDictionary(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, String filepath, String dictionaryfile, String regcode) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean success = false;

        String xml = Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + ".xml";
        String dic = filepath + Globals.FILE_SEPARATOR + regcode + ".txt";

        File xmlfile = new File(xml);
        File dicfile = new File(dic);

        try {
            String query = "SELECT * FROM \"" + dictionaryfile + "\"";
            conn = DriverManager.getConnection("jdbc:paradox:///" + filepath.replaceAll("\\\\", "/"));
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            txt_fw = new FileWriter(dicfile);
            txt_bw = new BufferedWriter(txt_fw);

            if (xmlfile.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                // Use the factory to create a builder
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlfile);
                doc.getDocumentElement().normalize();
                // Get a list of all elements in the document
                debugOut("Migrating dictionary " + dictionaryfile);
                NodeList nlist = doc.getElementsByTagName("ns3:dictionary");
                for (int i = 0; i < nlist.getLength(); i++) {
                    Node nNode = nlist.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        int dicId = Integer.parseInt(eElement.getElementsByTagName("ns3:dictionary_id").item(0).getTextContent());
                        String dicName = eElement.getElementsByTagName("ns3:name").item(0).getTextContent();
                        String dicType = eElement.getElementsByTagName("ns3:type").item(0).getTextContent();
                        String dic_head = "#" + dicId + " ----" + dicName + "\n";
                        txt_bw.write(dic_head);
                        // Processing dictionary child nodes.
                        processChildNodes(task, dicId, dicType, filepath, dictionaryfile);
                    }//if node ends
                }//for ends
            }//file if ends
            else {
                debugOut("Files not found");
            }
            txt_bw.close();
            success = true;
        } catch (SQLException | IOException | NumberFormatException | ParserConfigurationException | DOMException | SAXException ex) {
           LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
        return success;
    }

    public static void processChildNodes(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, int dic_id, String dic_type, String filepath, String dictionaryfile) {
        Connection conn;
        Statement stmt;
        ResultSet rs;

        try {
            String query = "SELECT * FROM \"" + dictionaryfile + "\"";
            conn = DriverManager.getConnection("jdbc:paradox:///" + filepath.replaceAll("\\\\", "/"));
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = Integer.parseInt(rs.getString(2).substring(0, 2));
                String optn = rs.getString(2).substring(3);
                if (dic_id + 1 == id) {
                    String dic_optn = optn + "\t" + rs.getString(3) + "\n";
                    txt_bw.write(dic_optn);
                }
            }
            txt_bw.write("\n");
        } catch (SQLException | IOException | NumberFormatException ex) {
           LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
    }

    public static boolean convertData(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, String filepath, String datafile, String regcode) {
        Connection conn;
        Statement stmt;
        ResultSet rs_hdr;
        ResultSet rs_data;
        boolean success = false;
        int totalrowcount = 0;
        int rowsImported = 0;

        String csv = filepath + Globals.FILE_SEPARATOR + regcode + ".csv";
        CSVPrinter printer;
        try {

            debugOut("Migrating data " + datafile);
            pconn = (ParadoxConnection) DriverManager.getConnection("jdbc:paradox:///" + filepath.replaceAll("\\\\", "/"));
            final ParadoxTable table = TableData.listTables(datafile, pconn).get(0);
            totalrowcount = table.getRowCount();

            SystemDescription sd = new SystemDescription(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + ".xml");
            DatabaseVariablesListElement[] variableListElements;
            variableListElements = sd.getDatabaseVariableListElements();
            ArrayList<String> dbvle = new ArrayList();
            ArrayList<String> cols = new ArrayList();

            // Handling variables names with reservered word by replacing underscore after variable name.
            for (DatabaseVariablesListElement variable : variableListElements) {
                if (variable.getShortName().endsWith("_")) {
                    dbvle.add(variable.getShortName().replace("_", ""));
                } else {
                    dbvle.add(variable.getShortName());
                }
            }

            conn = DriverManager.getConnection("jdbc:paradox:///" + filepath.replaceAll("\\\\", "/"));

            final DatabaseMetaData meta = conn.getMetaData();
            rs_hdr = meta.getColumns("", "", datafile, "%");

            //Comparing variables in file and database
            while (rs_hdr.next()) {
                for (String dbvar : dbvle) {
                    if (rs_hdr.getString("COLUMN_NAME").equals(dbvar) || rs_hdr.getString("COLUMN_NAME").replaceAll(" ", "_").equals(dbvar)) {
                        cols.add(rs_hdr.getString("COLUMN_NAME"));
                    }
                }
            }

            String[] strheader = new String[cols.size()];

            String query = "SELECT ";

            for (int i = 0; i < cols.size(); i++) {
                strheader[i] = cols.get(i);
                if (i == cols.size() - 1) {
                    query += "\"" + strheader[i] + "\"";
                } else {
                    query += "\"" + strheader[i] + "\",";
                }
            }

            query += " FROM  \"" + datafile + "\"";
            CSVFormat format = CSVFormat.DEFAULT.
                    withFirstRecordAsHeader().
                    withHeader(strheader).
                    withDelimiter(',');

            debugOut(query);
            printer = new CSVPrinter(new FileWriter(csv), format);

            int hdrsize = strheader.length;

            Object[] strdata = new String[hdrsize];

            stmt = conn.createStatement();
            rs_data = stmt.executeQuery(query);

            if (Globals.DEBUG) {
                Statement stmt2 = conn.createStatement();
                String q = "SELECT RecNum FROM \"" + datafile + "\"";
                ResultSet rs_all_data = stmt2.executeQuery(q);
                debugOut(rs_all_data.toString());
            }

            while (rs_data.next()) {
                for (int i = 1; i < rs_data.getMetaData().getColumnCount() + 1; i++) {
                    switch (rs_data.getMetaData().getColumnType(i)) {
                        case 4:
                            strdata[i - 1] = Integer.toString(rs_data.getShort(i));
                            break;
                        case 12:
                            strdata[i - 1] = StringEscapeUtils.escapeCsv(rs_data.getString(i));
                            break;
                    }
                }
                printer.printRecord(strdata);
                rowsImported++;
            }
            printer.flush();
            printer.close();
            success = true;
        } catch (SQLException | IOException ex) {
           LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
        success = success && (rowsImported == totalrowcount);
        return success;
    }

    public static Map importDictionary(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task, String file) {
        BufferedReader br = null;
        Map<Integer, Map<Integer, String>> allErrors = new TreeMap();
        debugOut("Importing dictionary from " + file);
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName(Globals.CHARSET_ENGLISH));
            br = new BufferedReader(isr);

            String dictionaryString = new String();
            String line = br.readLine();

            int dictionaryID = -1;

            while (line != null) {
                while (line != null && line.trim().length() == 0) {
                    // skip empty lines
                    line = br.readLine();
                }
                if (line == null) {
                    JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("FILE_IS_NOT_CORRECT_FORMAT:_") + "\'" + file + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                    //return ("Error");
                } else {
                    // first line contains the dictionary id
                    // remove leading #
                    if (line.startsWith("#")) {
                        line = line.substring(1);
                    }

                    line = line.replace(' ', '\t');
                    String[] lineElements = line.split("\t");
                    dictionaryID = Integer.parseInt(lineElements[0]);

                    // read next line;
                    line = br.readLine();
                }
                // read untill blank line
                while (line != null && line.trim().length() > 0) {
                    dictionaryString += line + "\n";
                    line = br.readLine();
                }

                if (dictionaryString.trim().length() > 0) {
                    try {
                        Map<Integer, String> errors = canreg.client.dataentry.DictionaryHelper.testDictionary(null, dictionaryString);
                        if (errors.size() > 0) {
                            allErrors.put(dictionaryID, errors);
                            LOGGER.log(Level.WARNING, String.format(" %d errors in dictionary: %d ",errors.size(),dictionaryID), new Exception());
                        } else {
                            canreg.client.dataentry.DictionaryHelper.replaceDictionary(dictionaryID, dictionaryString, CanRegClientApp.getApplication());
                        }
                        dictionaryString = new String();
                    } catch (RemoteException ex) {
                       LOGGER.log(Level.SEVERE, null, ex);
                        new TechnicalError().errorDialog();
                    }
                }
                // Read next line
                line = br.readLine();

                // Skip trailing blank lines
                while (line != null && line.trim().length() == 0) {
                    line = br.readLine();
                }
            }

            CanRegClientApp.getApplication().refreshDictionary();

        } catch (FileNotFoundException fileNotFoundException) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("COULD_NOT_PREVIEW_THE_FILE:_") + file + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.WARNING, null, fileNotFoundException);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        } catch (NumberFormatException nfe) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("SOMETHING_WRONG_WITH_THE_DICTIONARY:_") + "\'" + file + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.WARNING, null, nfe);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }
        return allErrors;  // return your result
    }

    public static boolean importFile(canreg.client.gui.management.CanReg4MigrationInternalFrame.MigrationTask task,
                                     Document doc, List<canreg.client.dataentry.Relation> map, File file, 
                                     CanRegServerInterface server, ImportOptions io) 
            throws SQLException, RemoteException, SecurityException, RecordLockedException {
        boolean success = false;

        Set<String> noNeedToLookAtPatientVariables = new TreeSet<>();

        noNeedToLookAtPatientVariables.add(canreg.common.Tools.toLowerCaseStandardized(io.getPatientIDVariableName()));
        noNeedToLookAtPatientVariables.add(canreg.common.Tools.toLowerCaseStandardized(io.getPatientRecordIDVariableName()));

        String firstNameVariableName = io.getFirstNameVariableName();
        String sexVariableName = io.getSexVariableName();

        CSVParser parser = null;

        HashMap mpCodes = new HashMap();

        int numberOfLinesRead = 0;

        Map<String, Integer> nameSexTable = server.getNameSexTables();

        try {
            // LOGGER.log(Level.CONFIG, "Name of the character encoding {0}");

            int numberOfRecordsInFile = canreg.common.Tools.numberOfLinesInFile(file.getAbsolutePath());

            debugOut("Importing data from " + file);

            CSVFormat format = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withDelimiter(io.getSeparator());

            parser = CSVParser.parse(file, io.getFileCharset(), format);

            int linesToRead = io.getMaxLines();
            if (linesToRead == -1 || linesToRead > numberOfRecordsInFile) {
                linesToRead = numberOfRecordsInFile;
            }

            for (CSVRecord csvRecord : parser) {
                numberOfLinesRead++;
                // We allow for null tasks...
                boolean needToSavePatientAgain = true;
                int patientDatabaseRecordID = -1;

                if (task != null) {
                    if (canreg.client.gui.management.CanReg4MigrationInternalFrame.isPaused) {
                        task.firePropertyChange("paused", false, true);
                    }
                    if (!canreg.client.gui.management.CanReg4MigrationInternalFrame.isPaused) {
                        task.firePropertyChange("paused", true, false);
                        task.firePropertyChange("progress", (numberOfLinesRead - 1) * 100 / linesToRead, (numberOfLinesRead) * 100 / linesToRead);
                    }
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
                                        LOGGER.log(Level.SEVERE,String.format("Number format error in line: %d.",numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                patient.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            LOGGER.log(Level.INFO, String.format("Something wrong with patient part of line : %d.",numberOfLinesRead), new Exception(String.format("Error in line: %d. Can't find field: %s",numberOfLinesRead,rel.getDatabaseVariableName())));
                        }
                    }
                }
                // debugOut(patient.toString());

                // Build tumour part
                Tumour tumour = new Tumour();
                for (int i = 0; i < map.size(); i++) {
                    Relation rel = map.get(i);
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase("tumour") && rel.getFileColumnNumber() < csvRecord.size()) {
                        if (rel.getFileColumnNumber() < csvRecord.size()) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        tumour.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d. ",numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                tumour.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            new TechnicalError().errorDialog();
                            LOGGER.log(Level.INFO,String.format("Something wrong with tumour part of line %d.",numberOfLinesRead), new Exception(String.format("Error in line: %d. Can't find field: %s",numberOfLinesRead,rel.getDatabaseVariableName())));
                        }
                    }
                }

                // Build source part
                Set<Source> sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
                Source source = new Source();
                for (int i = 0; i < map.size(); i++) {
                    Relation rel = map.get(i);
                    if (rel.getDatabaseTableVariableID() >= 0 && rel.getDatabaseTableName().equalsIgnoreCase(Globals.SOURCE_TABLE_NAME) && rel.getFileColumnNumber() < csvRecord.size()) {
                        if (rel.getFileColumnNumber() < csvRecord.size()) {
                            if (rel.getVariableType().equalsIgnoreCase("Number")) {
                                if (csvRecord.get(rel.getFileColumnNumber()).length() > 0) {
                                    try {
                                        source.setVariable(rel.getDatabaseVariableName(), Integer.parseInt(csvRecord.get(rel.getFileColumnNumber())));
                                    } catch (NumberFormatException ex) {
                                        LOGGER.log(Level.SEVERE, String.format("Number format error in line: %d.", numberOfLinesRead + 1 + 1), ex);
                                        success = false;
                                    }
                                }
                            } else {
                                source.setVariable(rel.getDatabaseVariableName(), StringEscapeUtils.unescapeCsv(csvRecord.get(rel.getFileColumnNumber())));
                            }
                        } else {
                            LOGGER.log(Level.INFO, String.format("Something wrong with source part of line %d.",numberOfLinesRead), new Exception("Error in line: " + numberOfLinesRead + ". Can't find field: " + rel.getDatabaseVariableName()));
                            new TechnicalError().errorDialog();
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
                            tumours = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID(patientID + "", false, null);
                        }
                        catch (Exception ex) {
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
                            oldPatients = CanRegClientApp.getApplication().getPatientsByPatientID((String) patientID, false, null);
                        } catch (Exception ex) {
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
                    String icd10 = (String) tumour.getVariable(io.getICD10VariableName());
                    if (icd10 == null || icd10.trim().length() == 0) {
                        ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                        tumour.setVariable(io.getICD10VariableName(), conversionResult[0].getValue());
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
            task.firePropertyChange("finished", null, null);
            success = true;
        } catch (IOException | NumberFormatException | IndexOutOfBoundsException | SQLException ex) {
            LOGGER.log(Level.SEVERE,String.format("Error in line: %d.",numberOfLinesRead + 1 + 1), ex);
            success = false;
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, String.format("Interupted on line: %d.", numberOfLinesRead + 1), ex);
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

    /**
     * Simple console trace to system.out for debug purposes only.
     *
     * @param message the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
           LOGGER.log(Level.INFO, msg);
        }
    }

}
