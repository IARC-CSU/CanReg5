package canreg.server.management;

import canreg.common.Globals;
import java.util.Set;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */

public class SystemDefinitionConverter {

    private String canReg4FileName;
    private static boolean debug = true;
    private String namespace = "ns3:";
    private Document doc;
    private DataInputStream dataStream;
    private JTextField nameTextField = null;
    private JTextField codeTextField = null;
    private String registryName;
    private String registryCode;
    private String[] standardVariablesCR4 = {
        Globals.StandardVariableNames.TumourID.toString(),
        Globals.StandardVariableNames.IncidenceDate.toString(),
        Globals.StandardVariableNames.BirthDate.toString(),
        Globals.StandardVariableNames.Age.toString(),
        Globals.StandardVariableNames.Sex.toString(),
        Globals.StandardVariableNames.Topography.toString(),
        Globals.StandardVariableNames.Morphology.toString(),
        Globals.StandardVariableNames.Behaviour.toString(),
        Globals.StandardVariableNames.BasisDiagnosis.toString(),
        Globals.StandardVariableNames.ICD10.toString(),
        Globals.StandardVariableNames.MultPrimCode.toString(),
        Globals.StandardVariableNames.CheckStatus.toString(),
        Globals.StandardVariableNames.PersonSearch.toString(),
        Globals.StandardVariableNames.TumourRecordStatus.toString(),
        Globals.StandardVariableNames.FirstName.toString(),
        Globals.StandardVariableNames.Surname.toString(),
        Globals.StandardVariableNames.TumourUpdateDate.toString(),
        Globals.StandardVariableNames.Lastcontact.toString(),
        Globals.StandardVariableNames.Grade.toString(),
        Globals.StandardVariableNames.ICCC.toString(),
        Globals.StandardVariableNames.AddressCode.toString(),
        Globals.StandardVariableNames.MultPrimSeq.toString(),
        Globals.StandardVariableNames.MultPrimTot.toString(),
        Globals.StandardVariableNames.Stage.toString(),
        Globals.StandardVariableNames.Source1.toString(),
        Globals.StandardVariableNames.Source2.toString(),
        Globals.StandardVariableNames.Source3.toString(),
        Globals.StandardVariableNames.Source4.toString(),
        Globals.StandardVariableNames.Source5.toString(),
        Globals.StandardVariableNames.Source6.toString()
    };
    private String[] dictionaryFontTypeValues = {"Latin", "Asian"};
    private String[] dictionaryTypeValues = {"Simple", "Compound"};
    private String[] fillInStatusValues = {"Optional", "Mandatory", "Automatic", "System"};
    private String[] variableTypeValues = {"Number", "Alpha", "Date", "Dict", "AsianText"};
    private String[] mpCopyValues = {"Must", "Prob", "Intr", "Othr"};
    private TreeMap<String, String> variableToTableMap;
    private int recordIDlength;

    /**
     * @param args 
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:\nSystemDefinitionConverter <CanReg4 system definition file>");
        } else {
            try {
                SystemDefinitionConverter sdc = new SystemDefinitionConverter();
                sdc.convert(args[0]);
            } catch (FileNotFoundException ex) {
                System.out.println(args[0] + " not found. " + ex);
                Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 
     * @param canReg4FileName
     * @return
     * @throws java.io.FileNotFoundException
     */
    public String convert(String canReg4FileName) throws FileNotFoundException {
        this.canReg4FileName = canReg4FileName;

        variableToTableMap = new TreeMap<String, String>();

        try {

            //Create instance of DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //Get the DocumentBuilder
            DocumentBuilder parser = factory.newDocumentBuilder();
            //Create blank DOM Document
            doc = parser.newDocument();

            doc.setXmlStandalone(true);

            //Create the root
            Element root = doc.createElement(namespace + "canreg");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xmlns:ns3", "http://xml.netbeans.org/schema/canregSystemFileSchema");
            root.setAttribute("xsi:schemaLocation", "http://xml.netbeans.org/schema/canregSystemFileSchema ../../META-INF/canregSystemFileSchema.xsd");

            doc.appendChild(root);

            // Open the file
            InputStream istream = new FileInputStream(canReg4FileName);
            // Decode using a DataInputStream - you can use this if the
            // bytes are written as IEEE format.
            dataStream = new DataInputStream(istream);

            try {
                String temp;
                Element element;
                Element generalParentElement;

                // Create the general part
                //
                generalParentElement = doc.createElement(namespace + "general");
                root.appendChild(generalParentElement);

                // Read and add the 3 letter code
                registryCode = new String(readBytes(3));
                if (codeTextField != null) {
                    codeTextField.setText(registryCode);
                }
                generalParentElement.appendChild(createElement(namespace + "registry_code", registryCode));
                // Read the region code
                generalParentElement.appendChild(createElement(namespace + "region_code", readBytes(1)));
                // Read the Registry name
                registryName = new String(readText().replace('|', ' '));
                if (nameTextField != null) {
                    nameTextField.setText(registryName);
                }
                generalParentElement.appendChild(createElement(namespace + "registry_name", registryName));
                // Read the working language
                generalParentElement.appendChild(createElement(namespace + "working_language", readBytes(1)));

                // Create the Dictionary part
                //
                Element dictionariesParentElement = doc.createElement(namespace + "dictionaries");
                root.appendChild(dictionariesParentElement);

                int numberOfDictionaries = readNumber(2);
                debugOut("Number of dictionaries: " + numberOfDictionaries);

                for (int i = 0; i < numberOfDictionaries; i++) {
                    element = doc.createElement(namespace + "dictionary");
                    dictionariesParentElement.appendChild(element);
                    element.appendChild(createElement(namespace + "dictionary_id", "" + i));
                    element.appendChild(createElement(namespace + "name", readText()));
                    element.appendChild(createElement(namespace + "font", dictionaryFontTypeValues[dataStream.readByte()]));
                    byte type = dataStream.readByte();
                    element.appendChild(createElement(namespace + "type", dictionaryTypeValues[type]));
                    if (type == (byte) 0) {
                        element.appendChild(createElement(namespace + "code_length", "" + 0));
                        element.appendChild(createElement(namespace + "category_description_length", "" + 0));
                    } else if (type == (byte) 1) {
                        element.appendChild(createElement(namespace + "code_length", "" + readNumber(2)));
                        element.appendChild(createElement(namespace + "category_description_length", "" + readNumber(2)));
                    } else {
                        debugOut("Error during parsing of the dictionaries...");
                    }
                    element.appendChild(createElement(namespace + "full_dictionary_code_length", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "full_dictionary_description_length", "" + readNumber(2)));
                }

                // Create the Groups part
                //
                Element groupsParentElement = doc.createElement(namespace + "groups");
                root.appendChild(groupsParentElement);

                int numberOfGroups = readNumber(2);
                debugOut("Number of Groups: " + numberOfGroups);

                for (int i = 0; i < numberOfGroups; i++) {
                    element = doc.createElement(namespace + "group");
                    groupsParentElement.appendChild(element);
                    element.appendChild(createElement(namespace + "group_id", "" + i));
                    element.appendChild(createElement(namespace + "name", readText()));
                    //skip unused variables
                    // Group order
                    readNumber(2);
                    //Group height
                    readNumber(2);
                }

                // Create the Variables part
                //
                Element variablesParentElement = doc.createElement(namespace + "variables");
                root.appendChild(variablesParentElement);

                int numberOfVariables = readNumber(2);
                debugOut("Number of Variables: " + numberOfVariables);

                for (int i = 0; i < numberOfVariables; i++) {
                    element = doc.createElement(namespace + "variable");
                    variablesParentElement.appendChild(element);

                    element.appendChild(createElement(namespace + "variable_id", "" + i));
                    element.appendChild(createElement(namespace + "full_name", readText()));

                    // short_name is the name that will be used in the database
                    String nameInDatabase = readText();

                    if (canreg.server.database.Tools.isReservedWord(nameInDatabase)) {
                        System.out.println("Warning: " + nameInDatabase.toUpperCase() + " is a reserverd word.");
                        System.out.println("Please revise the XML file manually before building CanReg5 database...");
                    }
                    element.appendChild(createElement(namespace + "short_name", nameInDatabase));

                    element.appendChild(createElement(namespace + "english_name", readText()));

                    String groupIDString = "" + dataStream.readByte();
                    element.appendChild(createElement(namespace + "group_id", "" + groupIDString));
                    element.appendChild(createElement(namespace + "variable_name_X_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "variable_name_Y_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "variable_X_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "variable_Y_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "fill_in_status", fillInStatusValues[dataStream.readByte()]));
                    element.appendChild(createElement(namespace + "multiple_primary_copy", mpCopyValues[dataStream.readByte()]));
                    byte type = dataStream.readByte();
                    element.appendChild(createElement(namespace + "variable_type", variableTypeValues[type]));
                    // Varb Type  (0 number, 1 alpha, 2 date, 3 dict, 4 asian text)
                    if (type == (byte) 0 || type == (byte) 1 || type == (byte) 4) {
                        element.appendChild(createElement(namespace + "variable_length", "" + readNumber(2)));
                    } else if (type == (byte) 2) {
                        element.appendChild(createElement(namespace + "variable_length", "8"));
                    } else if (type == (byte) 3) {
                        int dictionaryNumber = readNumber(2);
                        Element dictionaryElement = (Element) doc.getElementsByTagName(namespace + "dictionary").item(dictionaryNumber);
                        String dictionaryType = dictionaryElement.getElementsByTagName(namespace + "type").item(0).getTextContent();
                        //  debugOut(dictionaryElement.getTagName() + " " + dictionaryType);
                        element.appendChild(createElement(namespace + "use_dictionary", "" + dictionaryElement.getElementsByTagName(namespace + "name").item(0).getTextContent()));
                        // (0 Simple, 1 Compound)
                        if (dictionaryType.equalsIgnoreCase(dictionaryTypeValues[0])) {
                            element.appendChild(createElement(namespace + "category_X_pos", "0"));
                            element.appendChild(createElement(namespace + "category_Y_pos", "0"));
                        } else if (dictionaryType.equalsIgnoreCase(dictionaryTypeValues[1])) {
                            element.appendChild(createElement(namespace + "category_X_pos", "" + readNumber(2)));
                            element.appendChild(createElement(namespace + "category_Y_pos", "" + readNumber(2)));
                        } else {
                             debugOut("Invalid dict type...");
                        }
                        element.appendChild(createElement(namespace + "dictionary_X_pos", "" + readNumber(2)));
                        element.appendChild(createElement(namespace + "dictionary_Y_pos", "" + readNumber(2)));
                    } else {
                         debugOut("Invalid variable description...");
                    }
                    // Place variable in the right table
                    int groupID = Integer.parseInt(groupIDString);
                    Element groupElement = (Element) doc.getElementsByTagName(namespace + "group").item(groupID);
                    String groupName = groupElement.getElementsByTagName(namespace + "name").item(0).getTextContent();

                    if ((groupName.equalsIgnoreCase("patient") || groupName.equalsIgnoreCase("follow up") || nameInDatabase.equalsIgnoreCase("PerS")) && !(nameInDatabase.equalsIgnoreCase("age"))) {
                        element.appendChild(createElement(namespace + "table", Globals.PATIENT_TABLE_NAME));
                        variableToTableMap.put(nameInDatabase, Globals.PATIENT_TABLE_NAME);
                    } else {
                        element.appendChild(createElement(namespace + "table", Globals.TUMOUR_TABLE_NAME));
                        variableToTableMap.put(nameInDatabase, Globals.TUMOUR_TABLE_NAME);
                    }
                }

                // Read the indexes part
                // We build the doc for this later.

                int numberOfIndexes = readNumber(2);
                 debugOut("Number of Indexes: "+numberOfIndexes);

                TreeMap<String, LinkedList<String>> indexMap = new TreeMap<String, LinkedList<String>>();
                // first scan
                for (int i = 0; i < numberOfIndexes; i++) {

                    String indexName = readText();
                    LinkedList variables = new LinkedList<String>();

                    for (int j = 0; j < 3; j++) {
                        int variableIndex = readNumber(2);
                        if (variableIndex >= 0) {
                            Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                            String variableName = variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent();
                            variables.add(variableName);
                        }
                    }
                    indexMap.put(indexName, variables);
                }

                // Create the Person Search part
                //
                Element searchVariablesParentElement = doc.createElement(namespace + "search_variables");
                root.appendChild(searchVariablesParentElement);

                int numberOfSearchVarbs = readNumber(2);
                debugOut("Number of search variables: " + numberOfSearchVarbs);
                for (int i = 0; i < numberOfSearchVarbs; i++) {
                    element = doc.createElement(namespace + "search_variable");
                    searchVariablesParentElement.appendChild(element);

                    int variableIndex = readNumber(2);
                    // Element childElement = createElement(namespace + "name", readText());
                    // element.appendChild(childElement);

                    Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                    String variableName = variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent();

                    element.appendChild(createElement(namespace + "variable_name", variableName));
                    element.appendChild(createElement(namespace + "weigth", "" + readNumber(2)));

                }
                searchVariablesParentElement.appendChild(createElement(namespace + "minimum_match", "" + readNumber(2)));

                // Create the Standard variable part
                //
                int numberOfStandardVarbs = readNumber(2);
                 debugOut("Number of Standard variables:"+numberOfStandardVarbs);
                for (int i = 0; i < numberOfStandardVarbs; i++) {
                    int variableIndex = readNumber(2);
                    if (variableIndex > -1) {
                        Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                        //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
                        variableElement.appendChild(createElement(namespace + "standard_variable_name", standardVariablesCR4[i]));
                        // Grab some information
                        if (variableIndex == 0){
                            String recordIDlengthString = variableElement.getElementsByTagName(namespace + "variable_length").item(0).getTextContent();
                            if (recordIDlengthString!=null){
                                recordIDlength = Integer.parseInt(recordIDlengthString);
                            }
                        }
                    }
                }


                // Add the new System variables
                //    private Element createVariable(int variableId, String fullName, String shortName,
                //    String englishName, int groupID, String fillInStatus, String multiplePrimaryCopy,
                //    String variableType, int variableLength, int useDictionary, String table, String standardVariableName) {
                int variableNumber = numberOfVariables;


                {
                    /**
                     * Obsolete-flags
                     */
                    String variableName = Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Number", 1, -1, Globals.TUMOUR_TABLE_NAME, variableName));
                    variableName = Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Number", 1, -1, Globals.PATIENT_TABLE_NAME, variableName));
                    /**
                     * PatientID
                     */
                    variableName = Globals.StandardVariableNames.PatientID.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Alpha", recordIDlength, -1, Globals.PATIENT_TABLE_NAME, variableName));
                    /**
                     * PatientRecordID
                     */
                    variableName = Globals.StandardVariableNames.PatientRecordID.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Alpha", recordIDlength, -1, Globals.PATIENT_TABLE_NAME, variableName));

                    /**
                     * Pointer to Patient from Tumour
                     */
                    variableName = Globals.StandardVariableNames.PatientIDTumourTable.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Alpha", recordIDlength, -1, Globals.TUMOUR_TABLE_NAME, variableName));
                    variableName = Globals.StandardVariableNames.PatientRecordIDTumourTable.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Alpha", recordIDlength, -1, Globals.TUMOUR_TABLE_NAME, variableName));

                    /**
                     * "Updated by" fields
                     */
                    variableName = Globals.StandardVariableNames.PatientUpdatedBy.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Alpha", 16, -1, Globals.PATIENT_TABLE_NAME, variableName));
                    variableName = Globals.StandardVariableNames.TumourUpdatedBy.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Alpha", 16, -1, Globals.TUMOUR_TABLE_NAME, variableName));

                    /**
                     * Update dates
                     */
                    variableName = Globals.StandardVariableNames.PatientUpdateDate.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Number", -1, -1, Globals.PATIENT_TABLE_NAME, variableName));

                    /*
                     * Record status Patient table
                     */
                    variableName = Globals.StandardVariableNames.PatientRecordStatus.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Number", -1, -1, Globals.PATIENT_TABLE_NAME, variableName));

                     /*
                     * Check status Patient table
                     */
                    variableName = Globals.StandardVariableNames.PatientCheckStatus.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Number", -1, -1, Globals.PATIENT_TABLE_NAME, variableName));

                    /*
                     * Unduplication status Tumour table
                     */
                    variableName = Globals.StandardVariableNames.TumourUnduplicationStatus.toString();
                    variablesParentElement.appendChild(
                            createVariable(variableNumber++, variableName, variableName, variableName,
                            -1, "Automatic", "Othr", "Number", -1, -1, Globals.TUMOUR_TABLE_NAME, variableName));
                }

                // Build the indexes
                Element indexParentElement = doc.createElement(namespace + "indexes");
                root.appendChild(indexParentElement);

                // Index some important new variables to speed things up
                LinkedList<String> tempIndexList;

                // Patient ID in tumour table index
                tempIndexList = new LinkedList<String>();
                tempIndexList.add(Globals.StandardVariableNames.PatientIDTumourTable.toString());
                tempIndexList.add(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString());
                indexMap.put(Globals.StandardVariableNames.PatientIDTumourTable.toString(), tempIndexList);

                // Patient ID in patient table index
                tempIndexList = new LinkedList<String>();
                tempIndexList.add(Globals.StandardVariableNames.PatientID.toString());
                tempIndexList.add(Globals.StandardVariableNames.PatientRecordID.toString());
                indexMap.put(Globals.StandardVariableNames.PatientID.toString(), tempIndexList);

                // Split the indexes that needs to be split
                indexMap = splitIndexMapInTumourAndPatient(indexMap, variableToTableMap);

                // then build doc for the indexes
                Set<String> indexNames = indexMap.keySet();
                for (String indexName : indexNames) {
                    String table = null;

                    element = doc.createElement(namespace + "index");
                    indexParentElement.appendChild(element);
                    Element childElement = createElement(namespace + "name", indexName);
                    element.appendChild(childElement);

                    LinkedList<String> variablesInThisIndex = indexMap.get(indexName);

                    String tableOfThisIndex = variableToTableMap.get(variablesInThisIndex.getFirst());
                    childElement = createElement(namespace + "table", tableOfThisIndex);
                    element.appendChild(childElement);
                    for (String variableName : variablesInThisIndex) {
                        Element thisElement = doc.createElement(namespace + "indexed_variable");
                        element.appendChild(thisElement);
                        thisElement.appendChild(createElement(namespace + "variable_name", variableName));
                    }
                }

                // Create the Miscellaneous part
                //
                Element miscellaneousParentElement = doc.createElement(namespace + "miscellaneous");
                root.appendChild(miscellaneousParentElement);

                Element codingElement = doc.createElement(namespace + "coding");
                miscellaneousParentElement.appendChild(codingElement);
                Element settingsElement = doc.createElement(namespace + "settings");
                miscellaneousParentElement.appendChild(settingsElement);

                codingElement.appendChild(createElement(namespace + "male_code", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "female_code", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "unknown_sex_code", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "date_format", "" + dataStream.readByte()));
                codingElement.appendChild(createElement(namespace + "date_separator", readBytes(1)));
                settingsElement.appendChild(createElement(namespace + "fast_safe_mode", "" + dataStream.readByte()));
                codingElement.appendChild(createElement(namespace + "morphology_length", readBytes(1)));
                settingsElement.appendChild(createElement(namespace + "mult_prim_rules", "" + dataStream.readByte()));
                settingsElement.appendChild(createElement(namespace + "special_registry", "" + dataStream.readByte()));
                settingsElement.appendChild(createElement(namespace + "password_rules", "" + dataStream.readByte()));
                settingsElement.appendChild(createElement(namespace + "data_entry_language", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "registration_number_type", "" + dataStream.readByte()));
                codingElement.appendChild(createElement(namespace + "mult_prim_code_length", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "basis_diag_codes", "" + dataStream.readByte()));

            } catch (EOFException e) {
                // Nothing to do
            } catch (IOException e) {
                // Nothing to do
            } finally {

                File file = new File(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER); // Check to see it the canreg system folder exists
                if (!file.exists()) {
                    file.mkdirs(); // create it if necessary
                }
                canreg.server.xml.Tools.writeXmlFile(doc, Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + registryCode + ".xml");
                dataStream.close();
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, "Something wrong with the file... ", ex);
        }
        return ("Success");
    }

    /**
     * 
     * @return
     */
    public Document getDoc() {
        return doc;
    }

    /**
     * 
     * @param codeTextField
     */
    public void setCodeField(JTextField codeTextField) {
        this.codeTextField = codeTextField;
    }

    /**
     * 
     * @param nameTextField
     */
    public void setNameField(JTextField nameTextField) {
        this.nameTextField = nameTextField;
    }

    /**
     *
     * @param indexMap A map between the index name and the variables
     * @param variableToTableMap A map between the variable names and the respective tables.
     * @return An indexMap built to suit the two table scheme
     */
    public static TreeMap<String, LinkedList<String>> splitIndexMapInTumourAndPatient(TreeMap<String, LinkedList<String>> indexMap, TreeMap<String, String> variableToTableMap) {
        TreeMap<String, LinkedList<String>> newIndexMap = new TreeMap<String, LinkedList<String>>();
        Set<String> indexesNames = indexMap.keySet();
        for (String indexName : indexesNames) {
            String tableName = null;
            LinkedList<String> variablesInThisTable = new LinkedList<String>();
            LinkedList<String> variablesInOtherTable = new LinkedList<String>();
            String otherTableName = null;
            for (String indexedVariable : indexMap.get(indexName)) {
                String tableOfThisVariable = variableToTableMap.get(indexedVariable);
                if (tableName == null) {
                    tableName = tableOfThisVariable;
                    variablesInThisTable.add(indexedVariable);
                } else if (!tableName.equalsIgnoreCase(tableOfThisVariable)) {
                    otherTableName = tableOfThisVariable;
                    variablesInOtherTable.add(indexedVariable);
                } else {
                    variablesInThisTable.add(indexedVariable);
                }
            }
            if (indexName.endsWith(tableName)) {
                newIndexMap.put(indexName, variablesInThisTable);
            } else {
                newIndexMap.put(indexName + "-" + tableName, variablesInThisTable);
            }
            if (variablesInOtherTable.size() > 0) {
                if (indexName.endsWith(otherTableName)) {
                    newIndexMap.put(indexName, variablesInOtherTable);
                } else {
                    newIndexMap.put(indexName + "-" + otherTableName, variablesInOtherTable);
                }
            }
        }
        return newIndexMap;
    }

    private String translate(String variableName, String value) {
        String newValue = value;

        return value;
    }

    private Element createVariable(int variableId, String fullName, String shortName,
            String englishName, int groupID, String fillInStatus, String multiplePrimaryCopy,
            String variableType, int variableLength, int useDictionary, String table, String standardVariableName) {


        Element element = doc.createElement(namespace + "variable");

        element.appendChild(createElement(namespace + "variable_id", "" + variableId));
        element.appendChild(createElement(namespace + "full_name", fullName));
        element.appendChild(createElement(namespace + "short_name", shortName));
        element.appendChild(createElement(namespace + "english_name", englishName));
        element.appendChild(createElement(namespace + "group_id", "" + groupID));
        element.appendChild(createElement(namespace + "fill_in_status", fillInStatus));
        element.appendChild(createElement(namespace + "multiple_primary_copy", multiplePrimaryCopy));
        element.appendChild(createElement(namespace + "variable_type", variableType));
        // Varb Type  (0 number, 1 alpha, 2 date, 3 dict, 4 asian text)
        if (variableType.equalsIgnoreCase(variableTypeValues[0]) || variableType.equalsIgnoreCase(variableTypeValues[1]) || variableType.equalsIgnoreCase(variableTypeValues[4])) {
            element.appendChild(createElement(namespace + "variable_length", "" + variableLength));
        } else if (variableType.equalsIgnoreCase(variableTypeValues[2])) {
            element.appendChild(createElement(namespace + "variable_length", "8"));
        } else if (variableType.equalsIgnoreCase(variableTypeValues[3])) {
            element.appendChild(createElement(namespace + "use_dictionary", "" + useDictionary));
        // (0 Simple, 1 Compound)
        } else {
            debugOut("Invalid variable description...");
        }
        // Place variable in the right table
        element.appendChild(createElement(namespace + "table", table));
        variableToTableMap.put(shortName, table);
        element.appendChild(createElement(namespace + "standard_variable_name", standardVariableName));
        return element;
    }

    private Element createElement(String variableName, String value) {
        Element childElement = doc.createElement(variableName);
        childElement.appendChild(doc.createTextNode(value));
        return childElement;
    }

    private Element changeValueOfChild(Element element, String childName, String newValue){
        Element childElement = (Element) element.getElementsByTagName(childName).item(0);
        childElement.setTextContent(newValue);
        return element;
    }

    private String readBytes(int numberOfBytes) throws IOException {
        String temp = "";
        for (int i = 0; i < numberOfBytes; i++) {
            char c = (char) dataStream.readByte();
            temp += c;
        }
        return temp;
    }

    private String readText() throws IOException {
        String temp = "";
        int b = dataStream.readByte();

        while (b != 0) {
            // debugOut(""+b);
            temp += (char) b;
            b = (char) dataStream.readByte();
        }
        return temp;
    }

    private int readNumber(int numberOfBytes) throws IOException {
        int value = 0;
        byte[] byteArray = new byte[numberOfBytes];
        for (int i = 0; i < numberOfBytes; i++) {
            byteArray[i] = dataStream.readByte();
        }
        value = byteArrayToIntLH(byteArray);
        return value;
    }

    // Convert a byte array with the most significant byte in the first position to integer
    /**
     * 
     * @param b
     * @return
     */
    public static final int byteArrayToIntHL(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            if (i == (b.length - 1)) {
                value += (b[i] & 0xFF);
            } else if (i == 0) {
                value += b[i] << ((b.length - i) * 8);
            } else {
                value += (b[i] & 0xFF) << ((b.length - i) * 8);
            }
        }
        return value;
    }

    // Convert a byte array with the most significant byte in the last position to integer
    /**
     * 
     * @param b
     * @return
     */
    public static final int byteArrayToIntLH(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            if (i == 0) {
                value += (b[i] & 0xFF);
            } else if (i == (b.length - 1)) {
                value += b[i] << (i * 8);
            } else {
                value += (b[i] & 0xFF) << (i * 8);
            }
        }
        return value;
    }

    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.INFO, msg);
        }
    }
}
