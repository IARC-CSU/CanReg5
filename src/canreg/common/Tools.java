package canreg.common;

import java.nio.charset.Charset;
import java.util.LinkedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        int position = 0;
        boolean found = false;
        while (!found && position < objects.length) {
            found = object.equals(objects[position++]);
        }
        if (!found) {
            position = -1;
        } else {
            position -= 1;
        }
        return position;
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
        PowerfulTokenizer tokenizer = new PowerfulTokenizer(line, ""+separatingCharacter);
        String[] stringArray = new String[tokenizer.countTokens()];
        for (int i = 0; i<stringArray.length; i++){
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
        NodeList nl = doc.getElementsByTagName(namespace + "search_variable");
        PersonSearchVariable[] variables = new PersonSearchVariable[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            variables[i] = new PersonSearchVariable();
            variables[i].setName(e.getElementsByTagName(namespace + "variable_name").item(0).getTextContent());
            variables[i].setWeight(Integer.parseInt(e.getElementsByTagName(namespace + "weigth").item(0).getTextContent()));
        }
        return variables;
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
        return Integer.parseInt(e.getElementsByTagName(namespace + "minimum_match").item(0).getTextContent());
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @return
     */
    public static DatabaseVariablesListElement[] getVariableListElements(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "variable");
        DatabaseVariablesListElement[] variables = new DatabaseVariablesListElement[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            variables[i] = new DatabaseVariablesListElement(
                    e.getElementsByTagName(namespace + "table").item(0).getTextContent(),
                    Integer.parseInt(e.getElementsByTagName(namespace + "variable_id").item(0).getTextContent()),
                    e.getElementsByTagName(namespace + "short_name").item(0).getTextContent(),
                    e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent());
            if (e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent().equalsIgnoreCase("Dict")) {
                String dictionaryName = e.getElementsByTagName(namespace + "use_dictionary").item(0).getTextContent();
                int id = canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, dictionaryName);
                variables[i].setDictionaryID(id);
                variables[i].setUseDictionary(dictionaryName);
            }

            variables[i].setEnglishName(e.getElementsByTagName(namespace + "english_name").item(0).getTextContent());

            NodeList groupNameNodeList = e.getElementsByTagName(namespace + "group_id");
            if (groupNameNodeList != null && groupNameNodeList.getLength() > 0) {
                variables[i].setGroupID(Integer.parseInt(groupNameNodeList.item(0).getTextContent()));
            }

            variables[i].setFullName(e.getElementsByTagName(namespace + "full_name").item(0).getTextContent());

            NodeList xPosNodeList = e.getElementsByTagName(namespace + "variable_X_pos");
            if (xPosNodeList != null && xPosNodeList.getLength() > 0) {
                variables[i].setXPos(Integer.decode(xPosNodeList.item(0).getTextContent()));
            }
            NodeList yPosNodeList = e.getElementsByTagName(namespace + "variable_Y_pos");
            if (yPosNodeList != null && yPosNodeList.getLength() > 0) {
                variables[i].setYPos(Integer.decode(yPosNodeList.item(0).getTextContent()));
            }
            NodeList variableLengthNodeList = e.getElementsByTagName(namespace + "variable_length");
            if (variableLengthNodeList != null && variableLengthNodeList.getLength() > 0) {
                variables[i].setVariableLength(Integer.decode(variableLengthNodeList.item(0).getTextContent()));
            } else if (e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent().equalsIgnoreCase("dict")) {
                String dictionaryName = e.getElementsByTagName(namespace + "use_dictionary").item(0).getTextContent();
                NodeList dictnl = doc.getElementsByTagName("ns3:dictionary");
                boolean found = false;
                int j = 0;
                Element dictionaryElement = null;
                while (!found && j < dictnl.getLength()) {
                    dictionaryElement = (Element) dictnl.item(j++);
                    found = dictionaryElement.getElementsByTagName("ns3:name").item(0).getTextContent().equalsIgnoreCase(dictionaryName);
                }
                if (found) {
                    variables[i].setVariableLength(Integer.decode(dictionaryElement.getElementsByTagName("ns3:full_dictionary_code_length").item(0).getTextContent()));
                }
            }
            NodeList fillInStatusNodeList = e.getElementsByTagName(namespace + "fill_in_status");
            if (fillInStatusNodeList != null && fillInStatusNodeList.getLength() > 0) {
                variables[i].setFillInStatus(fillInStatusNodeList.item(0).getTextContent());
            }
            NodeList standardVariableNameNodeList = e.getElementsByTagName(namespace + "standard_variable_name");
            if (standardVariableNameNodeList != null && standardVariableNameNodeList.getLength() > 0) {
                variables[i].setStandardVariableName(standardVariableNameNodeList.item(0).getTextContent());
            }

        // TODO
        // Accommodate unknown codes

        }
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
        for (int i = 0; i <
                variablesInTable.length; i++) {
            if (variablesInTable[i].getDatabaseTableName().equalsIgnoreCase(tableName)) {
                tempVariablesInTable.add(variablesInTable[i]);
            }
        }
        variablesInTable = new DatabaseVariablesListElement[tempVariablesInTable.size()];
        for (int i = 0; i <
                variablesInTable.length; i++) {
            variablesInTable[i] = tempVariablesInTable.get(i);
        }
        return variablesInTable;
    }

    /**
     * 
     * @param doc
     * @param namespace
     * @return
     */
    public static DatabaseIndexesListElement[] getIndexesListElements(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "index");
        DatabaseIndexesListElement[] indexes = new DatabaseIndexesListElement[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            indexes[i] = new DatabaseIndexesListElement(
                    e.getElementsByTagName(namespace + "name").item(0).getTextContent());
        }
        return indexes;
    }

    public static TreeMap<String, LinkedList<String>> buildIndexMap(String tableName, Document doc, String namespace) {
        TreeMap<String, LinkedList<String>> indexMap = new TreeMap<String, LinkedList<String>>();

        NodeList nodes = doc.getElementsByTagName(namespace + "indexes");
        Element variablesElement = (Element) nodes.item(0);

        NodeList indexes = variablesElement.getElementsByTagName(namespace + "index");

        // Go through all the indexes definitions
        for (int i = 0; i < indexes.getLength(); i++) {

            // Get element
            Element element = (Element) indexes.item(i);

            // Create line
            String tableNameDB = element.getElementsByTagName(namespace + "table").item(0).getTextContent().toUpperCase();

            if (tableNameDB.equalsIgnoreCase(tableName)) {
                LinkedList<String> indexedVariables = new LinkedList<String>();
                String nameDB = element.getElementsByTagName(namespace + "name").item(0).getTextContent();

                NodeList variables = element.getElementsByTagName(namespace + "indexed_variable");

                if (variables.getLength() > 0) {
                    // we don't allow empty indexes
                    // Go through all the variable definitions
                    for (int j = 0; j < variables.getLength(); j++) {
                        Element variableElement = (Element) variables.item(j);
                        indexedVariables.add(variableElement.getElementsByTagName(namespace + "variable_name").item(0).getTextContent().toUpperCase());
                    }
                    indexMap.put(nameDB, indexedVariables);
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
            indexes[i] = new DatabaseGroupsListElement(
                    e.getElementsByTagName(namespace + "name").item(0).getTextContent(),
                    Integer.parseInt(e.getElementsByTagName(namespace + "group_id").item(0).getTextContent()));
        }
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
        for (int i = 0; i <
                nl.getLength(); i++) {
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
        StringBuffer contents = new StringBuffer();

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
            output.close();
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
            output.close();
        }
        return file;
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

        FileReader in = new FileReader(inputFile);
        FileWriter out = new FileWriter(outputFile);
        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }
        in.close();
        out.close();
    }

    public static String increment(String ID) {
        String IDplusOne = null;
        char lastChar = ID.charAt(ID.length() - 1);
        String theRest = ID.substring(0, ID.length() - 1);
        if (lastChar == '9') {
            lastChar = '0';
            IDplusOne = increment(theRest) + lastChar;
        } else {
            lastChar += 1;
            IDplusOne = theRest + lastChar;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Charset getStandardCharset(Document doc, String namespace) {
        Charset standardEncoding = Charset.defaultCharset();
        NodeList nl = doc.getElementsByTagName(namespace + "data_entry_language");
        if (nl.getLength()>0){
            String dataEntryLanguage = nl.item(0).getTextContent();
            if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ENGLISH)){
                standardEncoding = Charset.forName(Globals.CHARSET_ENGLISH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_FRENCH)){
                standardEncoding = Charset.forName(Globals.CHARSET_FRENCH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_SPANISH)){
                standardEncoding = Charset.forName(Globals.CHARSET_SPANISH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ITALIAN)){
                standardEncoding = Charset.forName(Globals.CHARSET_ITALIAN);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_TURKISH)){
                standardEncoding = Charset.forName(Globals.CHARSET_TURKISH);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ROMANIAN)){
                standardEncoding = Charset.forName(Globals.CHARSET_ROMANIAN);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_PORTUGUESE)){
                standardEncoding = Charset.forName(Globals.CHARSET_PORTUGUESE);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_CHINESE)){
                standardEncoding = Charset.forName(Globals.CHARSET_CHINESE);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_THAI)){
                standardEncoding = Charset.forName(Globals.CHARSET_THAI);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_KOREAN)){
                standardEncoding = Charset.forName(Globals.CHARSET_KOREAN);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_ARABIC)){
                standardEncoding = Charset.forName(Globals.CHARSET_ARABIC);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_FARSI)){
                standardEncoding = Charset.forName(Globals.CHARSET_FARSI);
            } else if (dataEntryLanguage.equalsIgnoreCase(Globals.DATAENTRY_LANGUAGE_RUSSIAN)){
                standardEncoding = Charset.forName(Globals.CHARSET_RUSSIAN);
            }
        }

        return standardEncoding;
    }
}