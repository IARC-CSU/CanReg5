/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */
package canreg.client.dataentry;

import canreg.client.CanRegClientApp;
import canreg.common.DatabaseDictionaryListElement;
import canreg.common.Globals;
import canreg.server.CanRegServerInterface;
import canreg.common.database.DictionaryEntry;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Some static tools to work with dictionaries
 * @author ervikm
 */
public class DictionaryHelper {

    /**
     * 
     * @param dictionary
     * @return
     */
    public static Map<String, DictionaryEntry> buildDictionaryEntriesFromMap(Map<String, String> dictionary) {
        Map<String, DictionaryEntry> dictionaryEntries = new LinkedHashMap<String, DictionaryEntry>();
        Iterator<String> iterator = dictionary.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String code = iterator.next().toString();
            String description = dictionary.get(code);
            dictionaryEntries.put(code, new DictionaryEntry(0, code, description));
        }
        return dictionaryEntries;
    }

    /**
     * 
     * @param doc
     * @param name
     * @return
     */
    public static int getDictionaryIDbyName(Document doc, String name) {
        int id = -1;

        // Get the variables node in the XML
        NodeList nodes = doc.getElementsByTagName(Globals.NAMESPACE + "dictionaries");
        Element variablesElement = (Element) nodes.item(0);

        NodeList dictionaries = variablesElement.getElementsByTagName(Globals.NAMESPACE + "dictionary");

        // Go through all the variable definitions
        boolean found = false;
        int i = 0;
        while (!found && i < dictionaries.getLength()) {
            // Get element
            Element element = (Element) dictionaries.item(i++);
            if (element == null) {
                System.err.println("");
                return -1;
            } else {
                Element nameElement = (Element) element.getElementsByTagName(Globals.NAMESPACE + "name").item(0);
                if (nameElement != null) {
                    found = name.equalsIgnoreCase(nameElement.getTextContent());
                } else {
                    System.err.println("Name of dictionary missing?");
                    return -1;
                }
            }
        }
        if (found) {
            id = i - 1;
        }
        return id;
    }

    /**
     *
     * @param doc
     * @param name
     * @return
     */
    public static boolean isCompoundDictionarybyName(Document doc, String name) {
        boolean compound = false;

        // Get the variables node in the XML
        NodeList nodes = doc.getElementsByTagName(Globals.NAMESPACE + "dictionaries");
        Element variablesElement = (Element) nodes.item(0);

        NodeList dictionaries = variablesElement.getElementsByTagName(Globals.NAMESPACE + "dictionary");

        // Go through all the variable definitions
        boolean found = false;
        int i = 0;
        Element element = null;
        while (!found && i < dictionaries.getLength()) {
            // Get element
            element = (Element) dictionaries.item(i++);
            if (element == null) {
                System.err.println("");
                return false;
            } else {
                Element nameElement = (Element) element.getElementsByTagName(Globals.NAMESPACE + "name").item(0);
                if (nameElement != null) {
                    found = name.equalsIgnoreCase(nameElement.getTextContent());
                } else {
                    System.err.println("Name of dictionary missing?");
                    return false;
                }
            }
        }
        if (found) {
            Element compoundElement = (Element) element.getElementsByTagName(Globals.NAMESPACE + "type").item(0);
            if (compoundElement != null) {
                compound = "Compound".equalsIgnoreCase(compoundElement.getTextContent());
            } else {
                System.err.println("Name of dictionary missing?");
                return false;
            }
        }
        return compound;
    }

    /**
     * 
     * @param dictionaryID
     * @param app
     * @return
     * @throws java.rmi.RemoteException
     */
    private static boolean clearDictionary(int dictionaryID, CanRegClientApp app) throws RemoteException {
        return app.deleteDictionaryEntries(dictionaryID);
    }

    /**
     * 
     * @param dictionaryEntry
     * @param server
     * @return
     * @throws java.lang.SecurityException
     * @throws java.rmi.RemoteException
     */
    public static int saveDictionaryEntry(DictionaryEntry dictionaryEntry, CanRegServerInterface server) throws SecurityException, RemoteException {
        return server.saveDictionaryEntry(dictionaryEntry);
    }

    private static LinkedList<DictionaryEntry> parseDictionaryText(int dictionaryID, String str) {
        LinkedList dictionaryEntries = new LinkedList<DictionaryEntry>();
        String[] stringArray = str.split("[\\n|\\r]");
        if (stringArray.length > 0 && stringArray[0].trim().length() > 0) {
            for (String string : stringArray) {
                String[] lineArray = string.split("\\t");
                if (lineArray.length > 1) {
                    dictionaryEntries.add(new DictionaryEntry(dictionaryID, lineArray[0], lineArray[1]));
                }
            }
        }
        return dictionaryEntries;
    }

    /**
     * 
     * @param dictionary
     * @param str
     * @return
     */
    public static Map<Integer, String> testDictionary(DatabaseDictionaryListElement dictionary, String str) {
        if (dictionary != null) {
            return testDictionary(dictionary, parseDictionaryText(dictionary.getDictionaryID(), str));
        } else {
            return testDictionary(dictionary, parseDictionaryText(-1, str));
        }
    }

    private static Map<Integer, String> testDictionary(DatabaseDictionaryListElement dictionary, LinkedList<DictionaryEntry> contents) {
        Map<Integer, String> errors = new LinkedHashMap<Integer, String>();
        Set<String> codes = new LinkedHashSet();
        int codeLength = 0;
        int fullCodeLength = 0;
        if (dictionary != null) {
            codeLength = dictionary.getCodeLength();
            fullCodeLength = dictionary.getFullDictionaryCodeLength();
            if (!dictionary.isCompound()) {
                codeLength = -1;
            }
        }
        int i = 1;
        if (contents.size() == 0) {
            errors.put(0, "Empty dictionary");
        } else {
            for (DictionaryEntry de : contents) {
                // first check length of code if we have a dictionary
                String code = de.getCode();
                if (dictionary != null) {
                    if (code.length() != codeLength && code.length() != fullCodeLength) {
                        errors.put(i, "Line " + i + " - Wrong length: " + code);
                    }
                }
                // Then we check if it is a duplicate
                if (!codes.add(code)) {
                    errors.put(i, "Line " + i + " - Duplicate code: " + code);
                }
                i++;
            }
        }
        return errors;
    }

    /**
     * 
     * @param dictionaryID
     * @param str
     * @param app
     * @throws java.rmi.RemoteException
     */
    public static void replaceDictionary(int dictionaryID, String str, CanRegClientApp app) throws RemoteException {
        LinkedList<DictionaryEntry> dictionaryEntries = parseDictionaryText(dictionaryID, str);
        // Map<String, String> dictionaryEntriesMap = new LinkedHashMap<String, String>();

        boolean removed = clearDictionary(dictionaryID, app);

        for (DictionaryEntry entry : dictionaryEntries) {
            app.saveDictionaryEntry(entry);
            // dictionaryEntriesMap.put(entry.getCode(), entry.getDescription());
        }
    }

    public static DictionaryEntry[] getDictionaryEntriesStartingWith(String start, DictionaryEntry[] dictionaryEntries) {
        LinkedList<DictionaryEntry> entriesList = new LinkedList<DictionaryEntry>();
        for (DictionaryEntry entry : dictionaryEntries) {
            String code = entry.getCode();
            if (code.startsWith(start) && code.length() > start.length()) {
                entriesList.add(entry);
            }
        }
        return entriesList.toArray(new DictionaryEntry[0]);
    }
}
