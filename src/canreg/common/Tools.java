/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common;

import canreg.client.DatabaseVariablesListElement;
import java.util.LinkedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.net.*;
import java.io.*;

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
        boolean finished = false;
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
        }
        return variables;
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

    public static String getFileFromURL(String urlString) {
        StringBuffer contents = new StringBuffer();

        try {
            // Create an URL instance
            URL url = new URL(urlString);

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

        } catch (MalformedURLException mue) {
            System.err.println("Invalid URL");
        } catch (IOException ioe) {
            System.err.println("I/O Error - " + ioe);
        }
        return contents.toString();
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
}
