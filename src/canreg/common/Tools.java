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
package canreg.common;

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.gui.tools.BareBonesBrowserLaunch;
import canreg.client.gui.tools.ImageSelection;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.qualitycontrol.PersonSearcher.CompareAlgorithms;
// import fr.iarc.cin.iarctools.Globals.IARCStandardVariableNames;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.nio.charset.Charset;
import java.util.LinkedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.universalchardet.UniversalDetector;

/**
 *
 * @author ervikm
 */
public class Tools {

    /**
     * 
     * @param objects
     * @param object
     * @return
     */
    public static int findInArray(Object[] objects, Object object) {
        return Arrays.asList(objects).indexOf(object);
    }

    /**
     * 
     * @param separatingCharacter
     * @param line
     * @return
     */
    public static String[] breakDownLine(char separatingCharacter, String line) {
        LinkedList<String> elements = new LinkedList();
        int pointer = 0;
        String tmpString = new String();
        // boolean finished = false;
        char tmpChar;
        while (pointer < line.length()) {
            tmpChar = line.charAt(pointer);
            if (tmpChar == separatingCharacter) {
                elements.add(tmpString);
                tmpString = new String();
            } else {
                if (tmpChar == '\"') {
                    pointer++;
                    tmpChar = line.charAt(pointer);
                    while (tmpChar != '\"' && pointer < line.length()) {
                        tmpString += tmpChar;
                        pointer++;
                        if (pointer < line.length()) {
                            tmpChar = line.charAt(pointer);
                        } else {
                            Logger.getLogger(Tools.class.getName()).log(Level.WARNING, "Warning! Unclosed quote.");
                        }
                    }
                } else {
                    tmpString += tmpChar;
                }
            }
            pointer++;
        }
        elements.add(tmpString);
        String[] elementArray = new String[elements.size()];
        for (int i = 0; i < elementArray.length; i++) {
            elementArray[i] = elements.get(i);
        }
        return elementArray;
    }

    /**
     *
     * @param separatingCharacter
     * @param line
     * @return
     */
    public static String[] breakDownLinePF(char separatingCharacter, String line) {
        PowerfulTokenizer tokenizer = new PowerfulTokenizer(line, "" + separatingCharacter);
        String[] stringArray = new String[tokenizer.countTokens()];
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = tokenizer.nextToken();
        }
        return stringArray;
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @return
     */
    public static PersonSearchVariable[] getPersonSearchVariables(Document doc, String namespace) {
        DatabaseVariablesListElement[] variables = getVariableListElements(doc, namespace);
        NodeList nl = doc.getElementsByTagName(namespace + "search_variable");
        PersonSearchVariable[] searchVariables = new PersonSearchVariable[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            searchVariables[i] = new PersonSearchVariable();
            String variableName = e.getElementsByTagName(namespace + "variable_name").item(0).getTextContent();
            boolean found = false;
            int j = 0;
            DatabaseVariablesListElement element = null;
            while (!found && j < variables.length) {
                element = variables[j++];
                found = element.getDatabaseVariableName().equalsIgnoreCase(variableName);
            }
            searchVariables[i].setVariable(element);
            searchVariables[i].setWeight(Float.parseFloat(e.getElementsByTagName(namespace + "weigth").item(0).getTextContent()));

            if (e.getElementsByTagName(namespace + "prescence").item(0) != null) {
                searchVariables[i].setPresence(Float.parseFloat(e.getElementsByTagName(namespace + "prescence").item(0).getTextContent()));
            }
            if (e.getElementsByTagName(namespace + "disc_power").item(0) != null) {
                searchVariables[i].setDiscPower(Float.parseFloat(e.getElementsByTagName(namespace + "disc_power").item(0).getTextContent()));
            }
            if (e.getElementsByTagName(namespace + "reliability").item(0) != null) {
                searchVariables[i].setReliability(Float.parseFloat(e.getElementsByTagName(namespace + "reliability").item(0).getTextContent()));
            }

            NodeList compareAlgorithmElement = e.getElementsByTagName(namespace + "compare_algorithm");
            if (compareAlgorithmElement.getLength() > 0) {
                searchVariables[i].setAlgorithm(CompareAlgorithms.valueOf(compareAlgorithmElement.item(0).getTextContent()));
            }
        }
        return searchVariables;
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @return
     */
    public static float getPersonSearchMinimumMatch(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "search_variables");
        Element e = (Element) nl.item(0);
        return Float.parseFloat(e.getElementsByTagName(namespace + "minimum_match").item(0).getTextContent());
    }

    public static DatabaseVariablesListElement[] getVariableListElements(Document doc, String namespace) {
        TreeMap<String, DatabaseDictionaryListElement> dictionaryMap = new TreeMap<String, DatabaseDictionaryListElement>();
        for (DatabaseDictionaryListElement dictionary : getDictionaryListElements(doc, namespace)) {
            dictionaryMap.put(dictionary.getName(), dictionary);
        }
        TreeMap<String, DatabaseGroupsListElement> groupsMap = new TreeMap<String, DatabaseGroupsListElement>();
        for (DatabaseGroupsListElement group : getGroupsListElements(doc, namespace)) {
            groupsMap.put(group.getGroupIndex() + "", group);
        }
        return getVariableListElements(doc, namespace, dictionaryMap, groupsMap);
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @param dictionaryMap
     * @param groupsMap
     * @return
     */
    public static DatabaseVariablesListElement[] getVariableListElements(Document doc, String namespace, TreeMap<String, DatabaseDictionaryListElement> dictionaryMap, TreeMap<String, DatabaseGroupsListElement> groupsMap) {
        LocalSettings localSettings = CanRegClientApp.getApplication().getLocalSettings();
                
        String dateFormatString = localSettings.getDateFormatString();

        NodeList nl = doc.getElementsByTagName(namespace + "variable");
        // DatabaseVariablesListElement[] variables = new DatabaseVariablesListElement[nl.getLength()];
        LinkedList<DatabaseVariablesListElement> variablesList = new LinkedList<DatabaseVariablesListElement>();
        // build a list of database variables
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            DatabaseVariablesListElement variable = new DatabaseVariablesListElement(
                    e.getElementsByTagName(namespace + "table").item(0).getTextContent(),
                    Integer.parseInt(e.getElementsByTagName(namespace + "variable_id").item(0).getTextContent()),
                    e.getElementsByTagName(namespace + "short_name").item(0).getTextContent(),
                    e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent());
            if (e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent().equalsIgnoreCase("Dict")) {
                String dictionaryName = e.getElementsByTagName(namespace + "use_dictionary").item(0).getTextContent();
                DatabaseDictionaryListElement dictionary = dictionaryMap.get(dictionaryName);
                variable.setDictionary(dictionary);
            }

            variable.setEnglishName(e.getElementsByTagName(namespace + "english_name").item(0).getTextContent());

            NodeList groupNameNodeList = e.getElementsByTagName(namespace + "group_id");
            if (groupNameNodeList != null && groupNameNodeList.getLength() > 0) {
                variable.setGroup(groupsMap.get(groupNameNodeList.item(0).getTextContent()));
            }

            variable.setFullName(e.getElementsByTagName(namespace + "full_name").item(0).getTextContent());

            NodeList xPosNodeList = e.getElementsByTagName(namespace + "variable_X_pos");
            if (xPosNodeList != null && xPosNodeList.getLength() > 0) {
                variable.setXPos(Integer.decode(xPosNodeList.item(0).getTextContent()));
            }
            NodeList yPosNodeList = e.getElementsByTagName(namespace + "variable_Y_pos");
            if (yPosNodeList != null && yPosNodeList.getLength() > 0) {
                variable.setYPos(Integer.decode(yPosNodeList.item(0).getTextContent()));
            }
            NodeList variableLengthNodeList = e.getElementsByTagName(namespace + "variable_length");
            if (variableLengthNodeList != null && variableLengthNodeList.getLength() > 0) {
                variable.setVariableLength(Integer.decode(variableLengthNodeList.item(0).getTextContent()));
            } else if (e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent().equalsIgnoreCase("dict")) {
                String dictionaryName = e.getElementsByTagName(namespace + "use_dictionary").item(0).getTextContent();
                NodeList dictnl = doc.getElementsByTagName(namespace + "dictionary");
                boolean found = false;
                int j = 0;
                Element dictionaryElement = null;
                while (!found && j < dictnl.getLength()) {
                    dictionaryElement = (Element) dictnl.item(j++);
                    found = dictionaryElement.getElementsByTagName(namespace + "name").item(0).getTextContent().equalsIgnoreCase(dictionaryName);
                }
                if (found) {
                    variable.setVariableLength(Integer.decode(dictionaryElement.getElementsByTagName("ns3:full_dictionary_code_length").item(0).getTextContent()));
                }
            }
            NodeList fillInStatusNodeList = e.getElementsByTagName(namespace + "fill_in_status");
            if (fillInStatusNodeList != null && fillInStatusNodeList.getLength() > 0) {
                variable.setFillInStatus(fillInStatusNodeList.item(0).getTextContent());
            }
            NodeList standardVariableNameNodeList = e.getElementsByTagName(namespace + "standard_variable_name");
            if (standardVariableNameNodeList != null && standardVariableNameNodeList.getLength() > 0) {
                variable.setStandardVariableName(standardVariableNameNodeList.item(0).getTextContent());
            }
            NodeList mpcopyVariableNameNodeList = e.getElementsByTagName(namespace + "multiple_primary_copy");
            if (mpcopyVariableNameNodeList != null && mpcopyVariableNameNodeList.getLength() > 0) {
                variable.setMultiplePrimaryCopy(mpcopyVariableNameNodeList.item(0).getTextContent());
            }
            NodeList unknownValueVariableNameNodeList = e.getElementsByTagName(namespace + "unknown_code");
            if (unknownValueVariableNameNodeList != null && unknownValueVariableNameNodeList.getLength() > 0) {
                variable.setUnknownCode(unknownValueVariableNameNodeList.item(0).getTextContent());
            }
            if (variable.getVariableType().equalsIgnoreCase("Date")) {
                variable.setDateFormatString(dateFormatString);
            }
            // Add variable to the list.
            variablesList.add(variable);
        }
        // add the metavariables
        // variablesList = addMetaVariables(doc, namespace, variablesList);

        DatabaseVariablesListElement[] variables = new DatabaseVariablesListElement[0];
        variables = variablesList.toArray(variables);
        return variables;
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @param tableName
     * @return
     */
    public static DatabaseVariablesListElement[] getVariableListElements(Document doc, String namespace, String tableName) {
        DatabaseVariablesListElement[] variablesInTable = getVariableListElements(doc, namespace);
        LinkedList<DatabaseVariablesListElement> tempVariablesInTable = new LinkedList<DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement variablesInTable1 : variablesInTable) {
            if (variablesInTable1.getDatabaseTableName().equalsIgnoreCase(tableName)) {
                tempVariablesInTable.add(variablesInTable1);
            }
        }
        variablesInTable = new DatabaseVariablesListElement[tempVariablesInTable.size()];
        for (int i = 0; i
                < variablesInTable.length; i++) {
            variablesInTable[i] = tempVariablesInTable.get(i);
        }
        return variablesInTable;
    }

    public static DatabaseIndexesListElement[] getIndexesListElements(Document doc, String namespace) {
        return getIndexesListElements(doc, namespace, null);
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @param variablesMap
     * @return
     */
    public static DatabaseIndexesListElement[] getIndexesListElements(Document doc, String namespace, TreeMap<String, DatabaseVariablesListElement> variablesMap) {
        NodeList nl = doc.getElementsByTagName(namespace + "index");
        if (variablesMap == null) {
            variablesMap = buildVariablesMap(getVariableListElements(doc, namespace));
        }
        DatabaseIndexesListElement[] indexes = new DatabaseIndexesListElement[nl.getLength()];
        Map<String, DatabaseIndexesListElement> patientIndexMap = buildIndexMap(Globals.PATIENT_TABLE_NAME, doc, namespace, variablesMap);
        Map<String, DatabaseIndexesListElement> tumourIndexMap = buildIndexMap(Globals.TUMOUR_TABLE_NAME, doc, namespace, variablesMap);
        Map<String, DatabaseIndexesListElement> sourceIndexMap = buildIndexMap(Globals.SOURCE_TABLE_NAME, doc, namespace, variablesMap);

        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            String indexName = e.getElementsByTagName(namespace + "name").item(0).getTextContent();
            DatabaseIndexesListElement index = new DatabaseIndexesListElement(indexName);
            String tableName = e.getElementsByTagName(namespace + "table").item(0).getTextContent();
            index.setDatabaseTableName(tableName);
            if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
                index = patientIndexMap.get(indexName);
            } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
                index = tumourIndexMap.get(indexName);
            } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
                index = sourceIndexMap.get(indexName);
            }
            indexes[i] = index;
        }
        return indexes;
    }

    /*
     * mapping variable names in capital letters to variable list elements
     *
     */
    public static TreeMap<String, DatabaseVariablesListElement> buildVariablesMap(DatabaseVariablesListElement[] variableListElements) {
        TreeMap<String, DatabaseVariablesListElement> variablesMap = new TreeMap<String, DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement elem : variableListElements) {
            variablesMap.put(canreg.common.Tools.toUpperCaseStandardized(elem.getDatabaseVariableName()), elem);
        }
        return variablesMap;
    }

    /*
     * mapping standard variables to variable list elements
     *
     */
    public static TreeMap<StandardVariableNames, DatabaseVariablesListElement> buildStandardVariablesMap(DatabaseVariablesListElement[] variableListElements) {
        TreeMap<StandardVariableNames, DatabaseVariablesListElement> variablesMap = new TreeMap<StandardVariableNames, DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement elem : variableListElements) {
            if (elem.getStandardVariableName() != null) {
                variablesMap.put(StandardVariableNames.valueOf(elem.getStandardVariableName()), elem);
            }
        }
        return variablesMap;
    }

    public static Map<String, DatabaseIndexesListElement> buildIndexMap(String tableName, Document doc, String namespace, TreeMap<String, DatabaseVariablesListElement> variablesMap) {
        if (variablesMap == null) {
            variablesMap = buildVariablesMap(getVariableListElements(doc, namespace));
        }

        Map<String, DatabaseIndexesListElement> indexMap = Collections.synchronizedMap(new TreeMap<String, DatabaseIndexesListElement>());

        NodeList nodes = doc.getElementsByTagName(namespace + "indexes");
        Element variablesElement = (Element) nodes.item(0);

        NodeList indexes = variablesElement.getElementsByTagName(namespace + "index");

        // Go through all the indexes definitions
        for (int i = 0; i < indexes.getLength(); i++) {

            // Get element
            Element element = (Element) indexes.item(i);

            // Create line
            String tableNameDB = canreg.common.Tools.toUpperCaseStandardized(element.getElementsByTagName(namespace + "table").item(0).getTextContent());

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                String nameDB = element.getElementsByTagName(namespace + "name").item(0).getTextContent();
                DatabaseIndexesListElement index = new DatabaseIndexesListElement(nameDB);
                index.setDatabaseTableName(tableName);
                NodeList variables = element.getElementsByTagName(namespace + "indexed_variable");
                DatabaseVariablesListElement[] variableArray = new DatabaseVariablesListElement[variables.getLength()];
                LinkedList<DatabaseVariablesListElement> variableLinkedList = new LinkedList<DatabaseVariablesListElement>();
                if (variables.getLength() > 0) {
                    // we don't allow empty indexes
                    // Go through all the variable definitions
                    for (int j = 0; j < variables.getLength(); j++) {
                        Element variableElement = (Element) variables.item(j);
                        String variableName = variableElement.getElementsByTagName(namespace + "variable_name").item(0).getTextContent();
                        DatabaseVariablesListElement variable =
                                variablesMap.get(variableName);
                        if (variable == null) {
                            variable = variablesMap.get(canreg.common.Tools.toUpperCaseStandardized(variableName));
                        }
                        if (variable != null) {
                            variableLinkedList.add(variable);
                        } else {
                            System.out.println("Variable " + variableName + " not found");
                        }
                    }
                    variableArray = variableLinkedList.toArray(variableArray);
                    index.setVariablesInIndex(variableArray);
                    indexMap.put(nameDB, index);
                }
            }
        }
        return indexMap;
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @return
     */
    public static DatabaseDictionaryListElement[] getDictionaryListElements(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "dictionary");
        DatabaseDictionaryListElement[] dictionaries = new DatabaseDictionaryListElement[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            dictionaries[i] = new DatabaseDictionaryListElement();
            dictionaries[i].setName(e.getElementsByTagName(namespace + "name").item(0).getTextContent());
            dictionaries[i].setDictionaryID(Integer.parseInt(e.getElementsByTagName(namespace + "dictionary_id").item(0).getTextContent()));
            dictionaries[i].setFont(e.getElementsByTagName(namespace + "font").item(0).getTextContent());
            dictionaries[i].setType(e.getElementsByTagName(namespace + "type").item(0).getTextContent());
            dictionaries[i].setCodeLength(Integer.parseInt(e.getElementsByTagName(namespace + "code_length").item(0).getTextContent()));
            dictionaries[i].setCategoryDescriptionLength(Integer.parseInt(e.getElementsByTagName(namespace + "category_description_length").item(0).getTextContent()));
            dictionaries[i].setFullDictionaryCodeLength(Integer.parseInt(e.getElementsByTagName(namespace + "full_dictionary_code_length").item(0).getTextContent()));
            dictionaries[i].setFullDictionaryCategoryDescriptionLength(Integer.parseInt(e.getElementsByTagName(namespace + "full_dictionary_description_length").item(0).getTextContent()));
            // lock part
            NodeList elem = e.getElementsByTagName(namespace + "locked");
            if (elem != null && elem.getLength() > 0) {
                dictionaries[i].setLocked("true".equalsIgnoreCase(elem.item(0).getTextContent()));
            } else {
                dictionaries[i].setLocked(false);
            }


            // TODO -- capture more info...
        }
        return dictionaries;
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @return
     */
    public static DatabaseGroupsListElement[] getGroupsListElements(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "group");
        DatabaseGroupsListElement[] indexes = new DatabaseGroupsListElement[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            int position = -1;
            int id = -1;
            try {
                id = Integer.parseInt(e.getElementsByTagName(namespace + "group_id").item(0).getTextContent());
                NodeList positionNodeList = e.getElementsByTagName(namespace + "group_pos");
                if (positionNodeList.item(0) != null) {
                    position = Integer.parseInt(positionNodeList.item(0).getTextContent());
                } else {
                    position = i;
                }
            } catch (NullPointerException npe) {
                throw (npe);
            } catch (NumberFormatException nfe) {
                throw (nfe);
            }
            indexes[i] = new DatabaseGroupsListElement(
                    e.getElementsByTagName(namespace + "name").item(0).getTextContent(),
                    id,
                    position);
        }
        Arrays.sort(indexes, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                DatabaseGroupsListElement group1 = (DatabaseGroupsListElement) o1;
                DatabaseGroupsListElement group2 = (DatabaseGroupsListElement) o2;
                return group1.getGroupPosition() - group2.getGroupPosition();
            }
        });
        return indexes;
    }

    /**
     *
     * @param doc
     * @param namespace
     * @return
     */
    public static String[] getVariableNames(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "variable");
        String[] variableNames = new String[nl.getLength()];
        for (int i = 0; i
                < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            variableNames[i] = e.getElementsByTagName(namespace + "short_name").item(0).getTextContent();
        }

        return variableNames;
    }

    /**
     *
     * @param url
     * @return
     */
    public static String getFileFromURL(URL url) {
        StringBuilder contents = new StringBuilder();

        try {

            // Get an input stream for reading
            InputStream in = url.openStream();

            // Create a buffered input stream for efficency
            BufferedInputStream bufIn = new BufferedInputStream(in);

            // Repeat until end of file
            for (;;) {
                int data = bufIn.read();

                // Check for EOF
                if (data == -1) {
                    break;
                } else {
                    contents.append((char) data);
                }
            }

        } catch (IOException ioe) {
            System.err.println("I/O Error - " + ioe);
        }
        return contents.toString();
    }

    /**
     *
     * @param urlString
     * @return
     */
    public static String getFileFromURL(String urlString) {
        String contents = new String();
        URL url = null;
        try {
            // Create an URL instance
            url = new URL(urlString);
            contents = getFileFromURL(url);
        } catch (MalformedURLException mue) {
            System.err.println("Invalid URL");
        }
        return contents;
    }

    /**
     * 
     * @param url
     * @return
     * @throws java.io.IOException
     */
    public static File getTempFileFromURL(URL url) throws IOException {
        File file = null;
        Writer output = null;
        try {
            file = File.createTempFile("cr5", "tmp");
            output = new BufferedWriter(new FileWriter(file));
            //FileWriter always assumes default encoding is OK!
            output.write(getFileFromURL(url));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return file;
    }

    /**
     * 
     * @param urlString
     * @return
     * @throws java.io.IOException
     */
    public static File getTempFileFromURL(String urlString) throws IOException {
        File file = null;
        Writer output = null;
        try {
            file = File.createTempFile("cr5", "tmp");
            output = new BufferedWriter(new FileWriter(file));
            //FileWriter always assumes default encoding is OK!
            output.write(getFileFromURL(urlString));
        } finally {
            if (output != null){
                output.close();
            }
        }
        return file;
    }

    public static void downloadFile(String urlString, String localFileName) throws IOException {
        java.io.BufferedInputStream in =
                new java.io.BufferedInputStream(
                new java.net.URL(urlString).openStream());
        java.io.FileOutputStream fos = new java.io.FileOutputStream(localFileName);
        java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        int x = 0;
        while ((x = in.read(data, 0, 1024)) >= 0) {
            {
                bout.write(data, 0, x);
            }

        }
        bout.close();
        in.close();
    }

    /**
     * 
     * @param from
     * @param to
     * @throws java.io.IOException
     */
    public static void fileCopy(String from, String to) throws IOException {
        File inputFile = new File(from);
        File outputFile = new File(to);

        //<ictl.co>
        Reader in = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
//        FileReader in = new FileReader(inputFile);
//        FileWriter out = new FileWriter(outputFile);
        //</ictl.co>
        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }
        in.close();
        out.close();
    }

    public static String increment(String ID) {
        String IDplusOne = null;
        if (ID.length() == 0) {
            IDplusOne = "1";
        } else {
            char lastChar = ID.charAt(ID.length() - 1);
            String theRest = ID.substring(0, ID.length() - 1);
            if (lastChar == '9') {
                lastChar = '0';
                IDplusOne = increment(theRest) + lastChar;
            } else {
                lastChar += 1;
                IDplusOne = theRest + lastChar;
            }
        }
        return IDplusOne;
    }

    /**
     * 
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static int numberOfLinesInFile(String file) throws IOException {
        //returns the number of lines in a file
        //author : Ravindra S
        //Symphony software Hyderabad
        int countRec = 0;

        RandomAccessFile randFile = new RandomAccessFile(file, "r");
        long lastRec = randFile.length();
        randFile.close();
        FileReader fileRead = new FileReader(file);
        LineNumberReader lineRead = new LineNumberReader(fileRead);
        lineRead.skip(lastRec);
        countRec = lineRead.getLineNumber() - 1;
        fileRead.close();
        lineRead.close();

        return countRec;
    }

    /**
     * 
     */
    public static void testEnvironment() {
        java.util.Properties prop = System.getProperties();
        java.util.Enumeration enumerator = prop.propertyNames();
        while (enumerator.hasMoreElements()) {
            String key = (String) enumerator.nextElement();
            System.out.println(key + " = " + System.getProperty(key));
        }
        File dir1 = new File(".");
        try {
            System.out.println("Current dir : " + dir1.getCanonicalPath());
            //System.out.println ("Parent  dir : " + dir2.getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void openFile(String fileName) throws IOException {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(new File(fileName));
        } else {
            String osName = System.getProperty("os.name");
            File file = new File(fileName);
            if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + file.getAbsolutePath());
            } else if (osName.startsWith("Mac OS")) {
                Runtime.getRuntime().exec("open " + file.getAbsolutePath());
            } else if (osName.startsWith("Lin")) {
                Runtime.getRuntime().exec("open " + file.getAbsolutePath());
            } else {
                BareBonesBrowserLaunch.openURL(fileName);
            }
        }
    }

    public static void browse(String URL) throws IOException {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(URL));
        } else {
            BareBonesBrowserLaunch.openURL(URL);
        }
    }

    public static void switchFocus() throws AWTException {
        try {
            Robot r = new Robot();
            r.keyPress(KeyEvent.VK_ALT);
            r.keyPress(KeyEvent.VK_TAB);
            r.keyRelease(KeyEvent.VK_ALT);
            r.keyRelease(KeyEvent.VK_TAB);
        } catch (AWTException e) {
            throw e;
        }
    }

    public static String toUpperCaseStandardized(String string) {
        return string.toUpperCase(Locale.ENGLISH);
    }

    public static String toLowerCaseStandardized(String string) {
        return string.toLowerCase(Locale.ENGLISH);
    }

    public static Charset getStandardCharset(Document doc, String namespace) {
        Charset standardEncoding = Charset.defaultCharset();
        NodeList nl = doc.getElementsByTagName(namespace + "data_entry_language");
        if (nl.getLength() > 0) {
            String dataEntryLanguage = nl.item(0).getTextContent();
            if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ENGLISH)) {
                standardEncoding = Charset.forName(Globals.CHARSET_ENGLISH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_FRENCH)) {
                standardEncoding = Charset.forName(Globals.CHARSET_FRENCH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_SPANISH)) {
                standardEncoding = Charset.forName(Globals.CHARSET_SPANISH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ITALIAN)) {
                standardEncoding = Charset.forName(Globals.CHARSET_ITALIAN);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_TURKISH)) {
                standardEncoding = Charset.forName(Globals.CHARSET_TURKISH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ROMANIAN)) {
                standardEncoding = Charset.forName(Globals.CHARSET_ROMANIAN);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_PORTUGUESE)) {
                standardEncoding = Charset.forName(Globals.CHARSET_PORTUGUESE);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_CHINESE)) {
                standardEncoding = Charset.forName(Globals.CHARSET_CHINESE);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_THAI)) {
                standardEncoding = Charset.forName(Globals.CHARSET_THAI);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_KOREAN)) {
                standardEncoding = Charset.forName(Globals.CHARSET_KOREAN);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ARABIC)) {
                standardEncoding = Charset.forName(Globals.CHARSET_ARABIC);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_FARSI)) {
                standardEncoding = Charset.forName(Globals.CHARSET_FARSI);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_RUSSIAN)) {
                standardEncoding = Charset.forName(Globals.CHARSET_RUSSIAN);
            }
        }
        return standardEncoding;
    }

    public static Set<String> getMissingStandardVariables(DatabaseVariablesListElement[] elements) {
        /*
         * Table 1. Basic information for cancer registries
        Name According to local usage
        Sex
        Date of birth or age Estimate if not known
        Demographic
        Address Usual residence
        Ethnic groupb When population consists of two or more groups
        Incidence date
        Most valid basis of diagnosis
        Topography (site) Primary tumour
        Morphology (histology)
        Behaviour
        Source of information E.g., hospital record no., name of physician
        - - - -
         */
        String NAMES_STRING = "Name (According to local usage)";
        String SEX_STRING = "Sex";
        String DOB_OR_AGE_STRING = "Date of birth or age (Estimate if not known)";
        String ADDRESS_STRING = "Address";
        String INC_DATE_STRING = "Incidence date";
        String BASIS_STRING = "Most valid basis of diagnosis";
        String TOPOGRAPHY_STRING = "Topography (site)";
        String MORPHO_STRING = "Morphology (histology)";
        String BEHAVIOUR_STRING = "Behaviour";
        String SOURCE_STRING = "Source of information (E.g., hospital record no., name of physician)";

        Set<String> missingStandardVariableNames = new HashSet<String>();

        missingStandardVariableNames.add(NAMES_STRING);
        missingStandardVariableNames.add(SEX_STRING);
        missingStandardVariableNames.add(DOB_OR_AGE_STRING);
        missingStandardVariableNames.add(ADDRESS_STRING);
        missingStandardVariableNames.add(INC_DATE_STRING);
        missingStandardVariableNames.add(BASIS_STRING);
        missingStandardVariableNames.add(TOPOGRAPHY_STRING);
        missingStandardVariableNames.add(MORPHO_STRING);
        missingStandardVariableNames.add(BEHAVIOUR_STRING);
        missingStandardVariableNames.add(SOURCE_STRING);

        //
        for (DatabaseVariablesListElement element : elements) {
            String standardVariable = element.getStandardVariableName();
            if (standardVariable != null) {
                // if we find a name we remove the name element
                if (standardVariable.equalsIgnoreCase(StandardVariableNames.FirstName.toString())
                        || standardVariable.equalsIgnoreCase(StandardVariableNames.Surname.toString())) {
                    missingStandardVariableNames.remove(NAMES_STRING);
                    // etc
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.Sex.toString())) {
                    missingStandardVariableNames.remove(SEX_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.BirthDate.toString())
                        || standardVariable.equalsIgnoreCase(StandardVariableNames.Age.toString())) {
                    missingStandardVariableNames.remove(DOB_OR_AGE_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.AddressCode.toString())) {
                    missingStandardVariableNames.remove(ADDRESS_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.IncidenceDate.toString())) {
                    missingStandardVariableNames.remove(INC_DATE_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.BasisDiagnosis.toString())) {
                    missingStandardVariableNames.remove(BASIS_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.Topography.toString())) {
                    missingStandardVariableNames.remove(TOPOGRAPHY_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.Morphology.toString())) {
                    missingStandardVariableNames.remove(MORPHO_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.Behaviour.toString())) {
                    missingStandardVariableNames.remove(BEHAVIOUR_STRING);
                } else if (standardVariable.equalsIgnoreCase(StandardVariableNames.Source1.toString())) {
                    missingStandardVariableNames.remove(SOURCE_STRING);
                }
            }
        }
        return missingStandardVariableNames;
    }

    public static String detectCharacterCodingOfFile(String fileName) throws java.io.IOException {
        byte[] buf = new byte[4096];
        java.io.FileInputStream fis = new java.io.FileInputStream(fileName);

        // (1)
        UniversalDetector detector = new UniversalDetector(null);

        // (2)
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            // System.out.println("Detected encoding = " + encoding);
        } else {
            // System.out.println("No encoding detected.");
        }

        // (5)
        detector.reset();

        return encoding;
    }

    public static Translator getTranslator(Document doc, String NAMESPACE) {
        // TODO scan the doc for things coded non-standard and build a translator
        // for now just return an empty translator
        Translator translator = new Translator();
        // DatabaseVariablesListElement[] variables = getVariableListElements(doc, NAMESPACE);

        return translator;
    }

// TODO reimplement method to access the IARCTools package...
//   Commented away to be able to disable the IARCtools package... 
//    
//    public static Map<IARCStandardVariableNames, String> getMapIARCstandardVariablesVariableName(Document doc, String NAMESPACE) {
//        EnumMap<IARCStandardVariableNames, String> map = new EnumMap<IARCStandardVariableNames, String>(IARCStandardVariableNames.class);
//        DatabaseVariablesListElement[] variables = getVariableListElements(doc, NAMESPACE);
//        Map<StandardVariableNames, IARCStandardVariableNames> mapStdVarbNamesIARCStdVarbNames = getMapStdVarbNamesIARCStdVarbNames();
//
//        for (DatabaseVariablesListElement variable : variables) {
//            String stdNameString = variable.getStandardVariableName();
//            if (stdNameString != null) {
//                try {
//                    StandardVariableNames stdVarb = StandardVariableNames.valueOf(stdNameString);
//                    IARCStandardVariableNames iarcStdVarb = mapStdVarbNamesIARCStdVarbNames.get(stdVarb);
//                    if (iarcStdVarb != null) {
//                        map.put(iarcStdVarb, variable.getDatabaseVariableName());
//                    }
//                } catch (java.lang.IllegalArgumentException ex) {
//                    Logger.getLogger(Tools.class.getName()).log(Level.WARNING, "{0} is not a standard variable name...", stdNameString);
//                }
//            }
//        }
//
//        return map;
//    }
//
//    private static Map<StandardVariableNames, IARCStandardVariableNames> getMapStdVarbNamesIARCStdVarbNames() {
//        EnumMap<StandardVariableNames, IARCStandardVariableNames> map = new EnumMap<StandardVariableNames, IARCStandardVariableNames>(StandardVariableNames.class);
//
//        /**
//         * Date
//         * String
//         * Coded yyyyMMdd
//         */
//        map.put(StandardVariableNames.IncidenceDate, IARCStandardVariableNames.IncidenceDate);
//        /**
//         * Date
//         * String
//         * Coded yyyyMMdd
//         */
//        map.put(StandardVariableNames.BirthDate, IARCStandardVariableNames.BirthDate);
//        /**
//         * Age at diagnosis
//         * Number
//         * Unknown age 999
//         */
//        map.put(StandardVariableNames.Age, IARCStandardVariableNames.Age);
//        /**
//         * Gender
//         * Coded 1=Male, 2=Female, 9=Unknown
//         */
//        map.put(StandardVariableNames.Sex, IARCStandardVariableNames.Sex);
//
//        /**
//         * Topography, Site
//         * String
//         * Coded according to ICD-O-3
//         */
//        map.put(StandardVariableNames.Topography, IARCStandardVariableNames.Topography);
//        /**
//         * Morphology, Histology
//         * String
//         * Coded according to ICD-O-3
//         */
//        map.put(StandardVariableNames.Morphology, IARCStandardVariableNames.Morphology);
//        /**
//         * Behaviour
//         * String
//         * Coded according to ICD-O-3
//         */
//        map.put(StandardVariableNames.Behaviour, IARCStandardVariableNames.Behaviour);
//
//        /**
//         * Basis of Diagnosis
//         * String
//         * Coded according to ICD-O-3
//         */
//        map.put(StandardVariableNames.BasisDiagnosis, IARCStandardVariableNames.BasisDiagnosis);
//        /**
//         * ICD10
//         */
//        map.put(StandardVariableNames.ICD10, IARCStandardVariableNames.ICD10);
//        /**
//         * Date
//         * String
//         * Coded yyyyMMdd
//         */
//        map.put(StandardVariableNames.Lastcontact, IARCStandardVariableNames.Lastcontact);
//        /**
//         * Grade
//         * Value between 1 and 9
//         */
//        map.put(StandardVariableNames.Grade, IARCStandardVariableNames.Grade);
//        /**
//         * International Childhood Cancer Classification
//         */
//        map.put(StandardVariableNames.ICCC, IARCStandardVariableNames.ICCC);
//        /**
//         * Stage
//         */
//        map.put(StandardVariableNames.Stage, IARCStandardVariableNames.Stage);
//        return map;
//    }
    // This method writes a image to the system clipboard.
// otherwise it returns null.
    public static void setClipboard(Image image) {
        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    }

    public static <T> T[] arrayConcat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static String combine(String[] s, String glue) {
        int k = s.length;
        if (k == 0) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        out.append(s[0]);
        for (int x = 1; x < k; ++x) {
            out.append(glue).append(s[x]);
        }
        return out.toString();
    }
    
    public static String combine(Collection<String> s, String glue){
        return combine(s.toArray(new String[]{}), glue);
    }
    
//    public static String encapsulateIfNeeded(String fileName){
//       if (fileName.contains(" ")) {
//            return "\'" + fileName + "\'";
//        } else {
//            return fileName;
//        }
//    }
}
