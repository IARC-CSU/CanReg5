/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016 International Agency for Research on Cancer
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
package canreg.server.management;

import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Tools;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemDefinitionConverter {

    /*
    
    CanReg4
    
    System Definition File Format
    
    General
    3 chars			Registry Code
    1 char			Region
    (1-Africa, 2-Americas, 3-EastMed, 4-Europe, 5-SEAsia, 6-West Pacific)
    Text (3 parts)		Registry Name
    1 char			Working Language (Translatable messages, screen labels, help boxes etc)
    (E English, F French, S Spanish, I Italian, P Portuguese, T Turkish,
    G Greek, R Romanian, A Arabic, C Chinese, H Thai etc)
    
    Dictionaries
    Num			Number of Dictionaries
    For each dictionary….
    Text			Dict Name
    1 char			Font (Latin, Asian)
    1 char			Type (Simple, Compound)
    Num			Category Code length
    Num			Category Description length
    Num			Full dict Code length
    Num			Full dict Desc length
    
    Variable Groups
    Num			Number of Groups
    For each group….
    Text			Group Name
    Num			Group Order (order to be displayed in Data Entry Form)
    Num			Group Height (Pixels)
    
    Variables
    Num			Number of Variables
    For each variable….
    Text			Full Variable Name
    Text			Short Varb Name
    Text			English Varb Name
    1 char			In which Group
    Num,Num		Variable Name screen position X, Y pixels
    Num,Num		Data screen position X, Y
    1 char			Fill-in status (0 Optional, 1 Mandatory, 2 Automatic)
    1 char			Mult Prim Copy  (0 Must Same, 1 Prob Same, 2 Interest, 3 Other)
    1 char			Varb Type  (0 number, 1 alpha, 2 date, 3 dict, 4 asian text)
    Num			Varb Length
    Num			Use Dictionary number
    Num,Num		Category screen pos X, Y
    Num,Num		Dictionary screen pos X, Y
    
    
    
    Indexes
    Num			Number of Indexes
    For each index….
    Text			Index Name
    Num,Num,Num	Sort Variable 1,2,3,
    
    Person Search
    Num			Number of Search Varbs
    For each search variable….
    Num			Variable number
    Num			Weighting (1-25)
    Num			Minimum Match %
    
    Standard Registry Variables
    Num			Number of Standard Registry Variables
    For each Standard Registry Variable….
    Num			Variable Position
    (-1 Not Used,  0 Registration No,  1 Incid Date,  2 Birth Date,  3 Age,  4 Sex,
    5 Topog,  6 Morph,  7 Behav,  8 Basis Diag,  9 ICD10,  10 Mult Prim Code,
    11 Check Status,  12 Person Search,  13 Record Status,
    14 First Name,  15 Surname,  16 Update date,  17 Date Last Contact,
    18 Grade,  19 ICCCcode,  20 Address,  21 MP Sequence,  22 MP Total,
    23 Stage,  24-29 Source1-Source6,  30)
    
    Miscellaneous
    3 chars			Male code, Female, Unknown sex
    1 char			Date Format  (0 Europe,  1 USA,  2 Buddhist,  3 Chinese)
    1 char			Date Separator
    1 char			Fast/Safe Mode
    1 char			Morphology Length  (4 digits / 5 digits)
    1 char			Mult. Prim. Rules  (0-IARC; 1-Local)
    1 char			Special Registry  (0-Norm; 1-MECC; 2-HospReg)
    1 char			Password  (0-Norm; 1-Min length & numeric; 2-Periodic change)
    1 char			Data Entry Language (Required for Names, Address etc)
    1 char			Registry Number Type (Numeric, Alphanumeric)
    1 char			Mult. Prim. Code Length (3, or 4 if > 500k cases)
    1 char			Basis Diag. Codes  (0-IARC; 1-Local)
    
     */
    private static final boolean debug = true;
    private final String namespace = "ns3:";
    private Document doc;
    private DataInputStream dataStream;
    private JTextField nameTextField = null;
    private JTextField codeTextField = null;
    private String registryName;
    private String registryCode;
    private final String[] standardVariablesCR4 = {
        Globals.StandardVariableNames.PatientID.toString(),
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
    private final String[] dictionaryFontTypeValues = {
        "Latin",
        "Asian"
    };
    private final String[] dictionaryTypeValues = {
        "Simple",
        "Compound"
    };
    private final String[] fillInStatusValues = {
        Globals.FILL_IN_STATUS_OPTIONAL_STRING,
        Globals.FILL_IN_STATUS_MANDATORY_STRING,
        Globals.FILL_IN_STATUS_AUTOMATIC_STRING,
        Globals.FILL_IN_STATUS_SYSTEM_STRING
    };
    private final String[] variableTypeValues = {
        Globals.VARIABLE_TYPE_NUMBER_NAME,
        Globals.VARIABLE_TYPE_ALPHA_NAME,
        Globals.VARIABLE_TYPE_DATE_NAME,
        Globals.VARIABLE_TYPE_DICTIONARY_NAME,
        Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME,
            /*</ictl.co>*/ Globals.VARIABLE_TYPE_NCID_NAME/*</ictl.co>*/
    };
    private final String[] mpCopyValues = {
        "Must",
        "Prob",
        "Intr",
        "Othr"
    };
    private TreeMap<String, String> variableToTableMap;
    private TreeMap<String, Integer> standardVariableToIndexMap;
    private int recordIDlength;
    // private int bahaviourIndex = -1;
    // private String morphologyVariableName;
    // private Charset charset = null;
    private CharsetDecoder charsetDecoder = null;

    /**
     * @param args 
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:\nSystemDefinitionConverter <CanReg4 system definition file> [Charset name]");
        } else {
            try {
                SystemDefinitionConverter sdc = new SystemDefinitionConverter();
                if (args.length == 2) {
                    Charset cs = Charset.forName(args[1]);
                    sdc.setFileEncoding(cs);
                }
                sdc.convertAndSaveInSystemFolder(args[0]);
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
     * TODO: look into how the translation of non-latin charachers are treated, ie. Turkish. Ref: mail from Cankut Yatuk 16.02.2009
     */
    public String convertAndSaveInSystemFolder(String canReg4FileName) throws FileNotFoundException {
        try {
            convertToXML(canReg4FileName);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, "Something wrong with the file... ", ex);
        } finally {
            File file = new File(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER); // Check to see it the canreg system folder exists
            if (!file.exists()) {
                file.mkdirs(); // create it if necessary
            }
            canreg.server.xml.Tools.writeXmlFile(doc, Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + registryCode + ".xml");
        }
        return ("Success");
    }

    public String convertAndSave(String canReg4FileName, String outFileName) throws FileNotFoundException {
        try {
            convertToXML(canReg4FileName);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, "Something wrong with the file... ", ex);
        } finally {
            File file = new File(outFileName).getParentFile();
            if (!file.exists()) {
                file.mkdirs(); // create it if necessary
            }
            canreg.server.xml.Tools.writeXmlFile(doc, outFileName);
        }
        return ("Success");
    }

    public void convertToXML(String canReg4FileName) throws ParserConfigurationException, IOException {
        variableToTableMap = new TreeMap<String, String>();
        standardVariableToIndexMap = new TreeMap<String, Integer>();
        DatabaseDictionaryListElement[] dictionaryListElements;
        Map<String, DatabaseVariablesListElement> variablesMap = Collections.synchronizedMap(new TreeMap<String, DatabaseVariablesListElement>());
        Map<String, DatabaseIndexesListElement> indexMap = Collections.synchronizedMap(new TreeMap<String, DatabaseIndexesListElement>());

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
            registryCode = readBytes(3);
            if (codeTextField != null) {
                codeTextField.setText(registryCode);
            }
            generalParentElement.appendChild(createElement(namespace + "registry_code", registryCode));
            // Read the region code
            generalParentElement.appendChild(createElement(namespace + "region_code", readBytes(1)));
            // Read the Registry name
            registryName = readText().replace('|', ' ');
            if (nameTextField != null) {
                nameTextField.setText(registryName);
            }
            generalParentElement.appendChild(createElement(namespace + "registry_name", registryName));
            // Read the working language
            char workingLanguageCode = readBytes(1).charAt(0);

            // generalParentElement.appendChild(createElement(namespace + "working_language", readBytes(1)));
            String workingLanguage = translateLanguageCode(workingLanguageCode);

            generalParentElement.appendChild(createElement(namespace + "working_language", workingLanguage));

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
            int[] groupOrder = new int[numberOfGroups];

            for (int i = 0; i < numberOfGroups; i++) {
                element = doc.createElement(namespace + "group");
                groupsParentElement.appendChild(element);
                element.appendChild(createElement(namespace + "group_id", "" + i));
                element.appendChild(createElement(namespace + "name", readText()));
                //skip unused variables
                // Group order
                int order = readNumber(2);
                groupOrder[i] = order;
                //Group height
                readNumber(2);
            }

            // group order
            for (int i = 0; i < numberOfGroups; i++) {
                int groupIndex = groupOrder[i];
                Element variableElement = (Element) doc.getElementsByTagName(namespace + "group").item(groupIndex);
                variableElement.appendChild(createElement(namespace + "group_pos", "" + i));
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

                while (canreg.common.database.Tools.isReservedWord(nameInDatabase)) {
                    System.out.println("Warning: " + canreg.common.Tools.toUpperCaseStandardized(nameInDatabase) + " is a reserverd word.");
                    System.out.println("Please revise the XML file manually before building CanReg5 database...");
                    nameInDatabase += "_";
                }

                // replace .s with _s
                nameInDatabase = nameInDatabase.replace('.', '_');
                // replace blanks with _s
                nameInDatabase = nameInDatabase.replace(' ', '_');

                element.appendChild(createElement(namespace + "short_name", nameInDatabase));
                String englishName = readText();
                element.appendChild(createElement(namespace + "english_name", englishName));

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

                // Decide on the group

                // Default set to tumour
                String tableName = Globals.TUMOUR_TABLE_NAME;

                if (groupName.equalsIgnoreCase("Patient")) {
                    if ((englishName.equalsIgnoreCase("age"))
                            || (englishName.toLowerCase().startsWith("addr"))
                            || (englishName.toLowerCase().startsWith("occu"))) {
                        tableName = Globals.TUMOUR_TABLE_NAME;
                    } else {
                        tableName = Globals.PATIENT_TABLE_NAME;
                    }
                } else if (groupName.toLowerCase().startsWith("follow")
                        || groupName.toLowerCase().startsWith("suiv")) {
                    tableName = Globals.PATIENT_TABLE_NAME;
                } else if (groupName.toLowerCase().startsWith("source")
                        || groupName.toLowerCase().startsWith("hosp")) {
                    tableName = Globals.SOURCE_TABLE_NAME;
                } else if (groupName.toLowerCase().startsWith("new control panel")) {
                    if ((englishName.equalsIgnoreCase("Reg.No."))
                            || (englishName.toLowerCase().startsWith("addr"))
                            || (englishName.toLowerCase().startsWith("per"))
                            || (englishName.toLowerCase().startsWith("occu"))) {
                        tableName = Globals.PATIENT_TABLE_NAME;
                    } else {
                        tableName = Globals.TUMOUR_TABLE_NAME;
                    }
                }
                element.appendChild(createElement(namespace + "table", tableName));
                variableToTableMap.put(canreg.common.Tools.toUpperCaseStandardized(nameInDatabase), tableName);
            }
            variablesMap = Tools.buildVariablesMap(Tools.getVariableListElements(doc, namespace));
            // Read the indexes part
            // We build the doc for this later.
            int numberOfIndexes = readNumber(2);
            debugOut("Number of Indexes: " + numberOfIndexes);

            // first scan
            for (int i = 0; i < numberOfIndexes; i++) {

                String indexName = readText();
                LinkedList<DatabaseVariablesListElement> variables = new LinkedList<DatabaseVariablesListElement>();

                for (int j = 0; j < 3; j++) {
                    int variableIndex = readNumber(2);
                    if (variableIndex >= 0) {
                        Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                        String variableName = variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent();
                        variables.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(variableName)));
                    }
                }
                DatabaseIndexesListElement index = new DatabaseIndexesListElement(indexName);
                index.setVariablesInIndex(variables.toArray(new DatabaseVariablesListElement[0]));
                indexMap.put(indexName, index);
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
            debugOut("Number of Standard variables:" + numberOfStandardVarbs);
            dictionaryListElements = Tools.getDictionaryListElements(doc, namespace);
            for (int i = 0; i < numberOfStandardVarbs; i++) {
                int variableIndex = readNumber(2);
                if (variableIndex > -1) {
                    Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                    //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
                    variableElement.appendChild(createElement(namespace + "standard_variable_name", standardVariablesCR4[i]));
                    standardVariableToIndexMap.put(standardVariablesCR4[i], variableIndex);
                    // Grab some information
                    // Registration number
                    if (i == 0) {
                        String recordIDlengthString = variableElement.getElementsByTagName(namespace + "variable_length").item(0).getTextContent();
                        if (recordIDlengthString != null) {
                            recordIDlength = Integer.parseInt(recordIDlengthString);
                        }
                    } // Topography (5), Morphology (6), Behaviour (7)
                    else if (i == 5 || i == 6 || i == 7) {
                        String dictionaryName = variableElement.getElementsByTagName(namespace + "use_dictionary").item(0).getTextContent();
                        DatabaseDictionaryListElement dbdle = findDictionaryListElementByName(dictionaryName, dictionaryListElements);
                        int dictionaryID = dbdle.getDictionaryID();
                        Element dictionaryElement = (Element) doc.getElementsByTagName(namespace + "dictionary").item(dictionaryID);
                        dictionaryElement.appendChild(createElement(namespace + "locked", "true"));
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
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 1, -1, Globals.TUMOUR_TABLE_NAME, variableName));
                variableName = Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 1, -1, Globals.PATIENT_TABLE_NAME, variableName));
                /**
                 * PatientID
                 */
                variableName = Globals.StandardVariableNames.TumourID.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, recordIDlength + Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD + Globals.ADDITIONAL_DIGITS_FOR_TUMOUR_ID, -1, Globals.TUMOUR_TABLE_NAME, variableName));
                /**
                 * PatientRecordID
                 */
                variableName = Globals.StandardVariableNames.PatientRecordID.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, recordIDlength + Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD, -1, Globals.PATIENT_TABLE_NAME, variableName));

                /**
                 * Pointer to Patient from Tumour
                 */
                variableName = Globals.StandardVariableNames.PatientIDTumourTable.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, recordIDlength, -1, Globals.TUMOUR_TABLE_NAME, variableName));
                variableName = Globals.StandardVariableNames.PatientRecordIDTumourTable.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, recordIDlength + Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD, -1, Globals.TUMOUR_TABLE_NAME, variableName));

                /**
                 * "Updated by" fields
                 */
                variableName = Globals.StandardVariableNames.PatientUpdatedBy.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 16, -1, Globals.PATIENT_TABLE_NAME, variableName));
                variableName = Globals.StandardVariableNames.TumourUpdatedBy.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 16, -1, Globals.TUMOUR_TABLE_NAME, variableName));

                /**
                 * Update dates
                 */
                variableName = Globals.StandardVariableNames.PatientUpdateDate.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_DATE_NAME, 8, -1, Globals.PATIENT_TABLE_NAME, variableName));

                /*
                 * Record status Patient table
                 */
                variableName = Globals.StandardVariableNames.PatientRecordStatus.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 1, -1, Globals.PATIENT_TABLE_NAME, variableName));

                /*
                 * Check status Patient table
                 */
                variableName = Globals.StandardVariableNames.PatientCheckStatus.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 1, -1, Globals.PATIENT_TABLE_NAME, variableName));

                /*
                 * Unduplication status Tumour table
                 */
                variableName = Globals.StandardVariableNames.TumourUnduplicationStatus.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, 1, -1, Globals.TUMOUR_TABLE_NAME, variableName));

                /**
                 * Pointer to Tumour from Source
                 */
                variableName = Globals.StandardVariableNames.TumourIDSourceTable.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, recordIDlength + Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD + Globals.ADDITIONAL_DIGITS_FOR_TUMOUR_ID, -1, Globals.SOURCE_TABLE_NAME, variableName));

                /**
                 * Pointer to Tumour from Source
                 */
                variableName = Globals.StandardVariableNames.SourceRecordID.toString();
                variablesParentElement.appendChild(
                        createVariable(variableNumber++, variableName, variableName, variableName,
                        -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", Globals.VARIABLE_TYPE_ALPHA_NAME, recordIDlength + Globals.ADDITIONAL_DIGITS_FOR_PATIENT_RECORD + Globals.ADDITIONAL_DIGITS_FOR_TUMOUR_ID + Globals.ADDITIONAL_DIGITS_FOR_SOURCE_ID, -1, Globals.SOURCE_TABLE_NAME, variableName));
            }

            // rebuild the variablesmap
            variablesMap = Tools.buildVariablesMap(Tools.getVariableListElements(doc, namespace));

            // Build the indexes
            Element indexParentElement = doc.createElement(namespace + "indexes");
            root.appendChild(indexParentElement);

            // Index some important new variables to speed things up
            LinkedList<DatabaseVariablesListElement> tempIndexList;

            // Patient ID in tumour table index
            tempIndexList = new LinkedList<DatabaseVariablesListElement>();
            tempIndexList.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(Globals.StandardVariableNames.PatientIDTumourTable.toString())));
            tempIndexList.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())));
            DatabaseIndexesListElement patientIDindex = new DatabaseIndexesListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString());
            patientIDindex.setDatabaseTableName(Globals.TUMOUR_TABLE_NAME);
            patientIDindex.setVariablesInIndex(tempIndexList.toArray(new DatabaseVariablesListElement[0]));
            indexMap.put(patientIDindex.getIndexName(), patientIDindex);

            // Tumour ID in tumour table index
            tempIndexList = new LinkedList<DatabaseVariablesListElement>();
            tempIndexList.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(Globals.StandardVariableNames.TumourID.toString())));
            tempIndexList.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())));
            DatabaseIndexesListElement tumourIDindex = new DatabaseIndexesListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString());
            tumourIDindex.setDatabaseTableName(Globals.TUMOUR_TABLE_NAME);
            tumourIDindex.setVariablesInIndex(tempIndexList.toArray(new DatabaseVariablesListElement[0]));
            indexMap.put(tumourIDindex.getIndexName(), tumourIDindex);

            // Source ID in source table index
            tempIndexList = new LinkedList<DatabaseVariablesListElement>();
            tempIndexList.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(Globals.StandardVariableNames.SourceRecordID.toString())));
            tempIndexList.add(variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(Globals.StandardVariableNames.TumourIDSourceTable.toString())));
            DatabaseIndexesListElement sourceIDindex = new DatabaseIndexesListElement(Globals.StandardVariableNames.SourceRecordID.toString());
            sourceIDindex.setDatabaseTableName(Globals.SOURCE_TABLE_NAME);
            sourceIDindex.setVariablesInIndex(tempIndexList.toArray(new DatabaseVariablesListElement[0]));
            indexMap.put(sourceIDindex.getIndexName(), sourceIDindex);

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

                LinkedList<String> variablesInThisIndex = indexMap.get(indexName).getVariableNamesInIndex();

                String tableOfThisIndex = variableToTableMap.get(canreg.common.Tools.toUpperCaseStandardized(variablesInThisIndex.getFirst()));
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
            int morphologyLength = Integer.parseInt(readBytes(1));
            codingElement.appendChild(createElement(namespace + "morphology_length", "" + morphologyLength));
            settingsElement.appendChild(createElement(namespace + "mult_prim_rules", "" + dataStream.readByte()));
            settingsElement.appendChild(createElement(namespace + "special_registry", "" + dataStream.readByte()));
            settingsElement.appendChild(createElement(namespace + "password_rules", "" + dataStream.readByte()));
            char dataEntryLanguageCode = readBytes(1).charAt(0);
            settingsElement.appendChild(createElement(namespace + "data_entry_language", translateLanguageCode(dataEntryLanguageCode)));
            codingElement.appendChild(createElement(namespace + "registration_number_type", "" + dataStream.readByte()));
            codingElement.appendChild(createElement(namespace + "mult_prim_code_length", readBytes(1)));
            codingElement.appendChild(createElement(namespace + "basis_diag_codes", "" + dataStream.readByte()));

            /*
            //add metavariables
            if (morphologyLength == 5) {
            String variableName = Globals.StandardVariableNames.Behaviour.toString();
            String formula = "SUBSTR(" + morphologyVariableName + ",5,1)";
            variablesParentElement.appendChild(
            createMetaVariable(variableNumber++, variableName, variableName, variableName,
            -1, Globals.FILL_IN_STATUS_AUTOMATIC_STRING, "Othr", "Meta", 1, -1, Globals.TUMOUR_TABLE_NAME, variableName, formula));
            }
             */

            // If we have 5 digit morpho we generate behaviour automatically
            if (morphologyLength == 5) {
                int variableIndex = standardVariableToIndexMap.get(Globals.StandardVariableNames.Behaviour.toString());
                Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
                Element oldElement = (Element) variableElement.getElementsByTagName(namespace + "fill_in_status").item(0);
                variableElement.replaceChild(createElement(namespace + "fill_in_status", Globals.FILL_IN_STATUS_AUTOMATIC_STRING), oldElement);
            }
            // TODO put the groups in the right order...

        } catch (EOFException e) {
            // Nothing to do
        } catch (IOException e) {
            // Nothing to do
        } finally {
            dataStream.close();
        }

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
    public static Map<String, DatabaseIndexesListElement> splitIndexMapInTumourAndPatient(Map<String, DatabaseIndexesListElement> indexMap, Map<String, String> variableToTableMap) {
        TreeMap<String, DatabaseIndexesListElement> newIndexMap = new TreeMap<String, DatabaseIndexesListElement>();
        Set<String> indexesNames = indexMap.keySet();
        for (String indexName : indexesNames) {
            DatabaseIndexesListElement index = indexMap.get(indexName);
            String tableName = null;
            LinkedList<DatabaseVariablesListElement> variablesInThisTable = new LinkedList<DatabaseVariablesListElement>();
            LinkedList<DatabaseVariablesListElement> variablesInOtherTable = new LinkedList<DatabaseVariablesListElement>();
            String otherTableName = null;
            for (DatabaseVariablesListElement indexedVariable : index.getVariableListElementsInIndex()) {
                String tableOfThisVariable = indexedVariable.getDatabaseTableName();
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
            // put it in the indexmap
            if (indexName.endsWith(tableName)) {
                index.setVariablesInIndex(variablesInThisTable.toArray(new DatabaseVariablesListElement[0]));
                newIndexMap.put(indexName, index);
            } else {
                // indexMap.remove(indexName);
                index.setIndexName(indexName + "-" + tableName);
                newIndexMap.put(index.getIndexName(), index);
                index.setVariablesInIndex(variablesInThisTable.toArray(new DatabaseVariablesListElement[0]));
            }
            if (variablesInOtherTable.size() > 0) {
                DatabaseIndexesListElement otherIndex;
                if (indexName.endsWith(otherTableName)) {
                    otherIndex = new DatabaseIndexesListElement(indexName);
                    otherIndex.setDatabaseTableName(otherTableName);
                    otherIndex.setVariablesInIndex(variablesInOtherTable.toArray(new DatabaseVariablesListElement[0]));
                    newIndexMap.put(otherIndex.getIndexName(), otherIndex);
                } else {
                    otherIndex = new DatabaseIndexesListElement(indexName + "-" + otherTableName);
                    otherIndex.setDatabaseTableName(otherTableName);
                    otherIndex.setVariablesInIndex(variablesInOtherTable.toArray(new DatabaseVariablesListElement[0]));
                    newIndexMap.put(otherIndex.getIndexName(), otherIndex);
                }
            }
        }
        return newIndexMap;
    }

    private String translate(String variableName, String value) {
        String newValue = value;

        return value;
    }

    private Element createMetaVariable(int variableId, String fullName, String shortName,
            String englishName, int groupID, String fillInStatus, String multiplePrimaryCopy,
            String variableType, int variableLength, int useDictionary, String table, String standardVariableName, String formula) {
        Element element = createVariable(variableId, fullName, shortName, englishName, groupID, fillInStatus, multiplePrimaryCopy, variableType, variableLength, useDictionary, table, standardVariableName);
        element.appendChild(createElement(namespace + "variable_formula", "" + formula));
        return element;
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
        variableToTableMap.put(canreg.common.Tools.toUpperCaseStandardized(shortName), table);
        element.appendChild(createElement(namespace + "standard_variable_name", standardVariableName));
        return element;
    }

    private Element createElement(String variableName, String value) {
        Element childElement = doc.createElement(variableName);
        childElement.appendChild(doc.createTextNode(value));
        return childElement;
    }

    private Element changeValueOfChild(Element element, String childName, String newValue) {
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
        byte b = dataStream.readByte();
        byte[] bytes;
        LinkedList<Byte> charList = new LinkedList<Byte>();

        while (b != 0) {
            // debugOut(""+b);
            temp += (char) b;
            charList.add(b);
            b = dataStream.readByte();
        }
        bytes = new byte[charList.size()];
        int i = 0;
        for (Byte o : charList) {
            bytes[i++] = o;
        }
        temp = translateCharset(bytes, charsetDecoder);
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
    public static int byteArrayToIntHL(byte[] b) {
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
    public static int byteArrayToIntLH(byte[] b) {
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

    /**
     *
     * @param charset
     */
    public void setFileEncoding(Charset charset) {
        // this.charset = charset;
        if (charset != null) {
            charsetDecoder = charset.newDecoder();
        }
    }

    private static String translateCharset(byte[] bytes, CharsetDecoder decoder) {
        String translatedString = new String(bytes);
        if (decoder != null) {
            // create a byte buffer
            ByteBuffer bytebuf = ByteBuffer.wrap(bytes);
            // System.out.println(new String(bytebuf.array()));
            try {
                CharBuffer chabuf = decoder.decode(bytebuf);
                translatedString = chabuf.toString();
            } catch (CharacterCodingException ex) {
                Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return translatedString;
    }

    private String translateLanguageCode(char workingLanguageCode) {
        String workingLanguage = Globals.DATAENTRY_LANGUAGE_ENGLISH; // default working language
        switch (workingLanguageCode) {
            // reference: http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
            case 'E':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_ENGLISH;
                break;
            case 'F':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_FRENCH;
                break;
            case 'S':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_SPANISH;
                break;
            case 'I':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_ITALIAN;
                break;
            case 'P':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_PORTUGUESE;
                break;
            case 'T':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_TURKISH;
                break;
            case 'G':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_GREEK;
                break;
            case 'R':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_ROMANIAN;
                break;
            case 'A':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_ARABIC;
                break;
            case 'C':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_CHINESE;
                break;
            case 'H':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_THAI;
                break;
            case 'K':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_KOREAN;
                break;
            case 'Z':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_FARSI;
                break;
            case 'U':
                workingLanguage = Globals.DATAENTRY_LANGUAGE_RUSSIAN;
                break;
        }
        return workingLanguage;
    }

    private static DatabaseDictionaryListElement findDictionaryListElementByName(String dictionaryName, DatabaseDictionaryListElement[] dictionaryListElements) {
        DatabaseDictionaryListElement dictionaryListElement = null;
        boolean found = false;
        int i = 0;
        while (!found && i < dictionaryListElements.length) {
            dictionaryListElement = dictionaryListElements[i++];
            found = dictionaryListElement.getName().equalsIgnoreCase(dictionaryName);
        }
        if (!found) {
            dictionaryListElement = null;
        }
        return dictionaryListElement;
    }
    
    public String getRegistryName() {
        return registryName;
    }

    public String getServerCode() {
        return registryCode;
    }
}
