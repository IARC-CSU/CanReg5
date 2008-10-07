package canreg.common;

import java.util.LinkedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.net.*;
import java.io.*;
import java.util.Date;

/**
 *
 * @author morten
 */
public class Tools {

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
                        tmpChar = line.charAt(pointer);
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
            if (e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent().equalsIgnoreCase("dict")) {
                String dictionaryName = e.getElementsByTagName(namespace + "use_dictionary").item(0).getTextContent();
                int id = canreg.client.dataentry.DictionaryHelper.getDictionaryIDbyName(doc, dictionaryName);
                variables[i].setDictionaryID(id);
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
        }
        return variables;
    }

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

    public static DatabaseDictionaryListElement[] getDictionaryListElements(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "dictionary");
        DatabaseDictionaryListElement[] dictionaries = new DatabaseDictionaryListElement[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            dictionaries[i] = new DatabaseDictionaryListElement();
            dictionaries[i].setName(e.getElementsByTagName(namespace + "name").item(0).getTextContent());
            dictionaries[i].setDictionaryID(Integer.parseInt(e.getElementsByTagName(namespace + "dictionary_id").item(0).getTextContent()));
        // TODO -- capture more info...
        }
        return dictionaries;
    }

        public static DatabaseGroupsListElement[] getGroupsListElements(Document doc, String namespace) {
        NodeList nl = doc.getElementsByTagName(namespace + "group");
        DatabaseGroupsListElement[] indexes = new DatabaseGroupsListElement[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            indexes[i] = new DatabaseGroupsListElement(
                    e.getElementsByTagName(namespace + "name").item(0).getTextContent(), 
                    Integer.parseInt(e.getElementsByTagName(namespace + "group_id").item(0).getTextContent())
                    );
        }
        return indexes;
    }
    
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

    public static String getFileFromURL(URL url){
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

        }  catch (IOException ioe) {
            System.err.println("I/O Error - " + ioe);
        }
        return contents.toString();
    }
    
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
}