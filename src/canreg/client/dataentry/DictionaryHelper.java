/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2018  International Agency for Research on Cancer
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Some static tools to work with dictionaries
 *
 * @author ervikm
 */
public class DictionaryHelper {

    /**
     *
     * @param dictionary
     * @return
     */
    public static Map<String, DictionaryEntry> buildDictionaryEntriesFromMap(Map<String, String> dictionary) {
        Map<String, DictionaryEntry> dictionaryEntries = new LinkedHashMap<>();
        Iterator<String> iterator = dictionary.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String code = iterator.next();
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
        return app.deleteDictionaryEntries(dictionaryID, null);
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
        LinkedList <DictionaryEntry> dictionaryEntries = new LinkedList<>();
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
        Map<Integer, String> errors = new LinkedHashMap<>();
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
        if (contents.isEmpty()) {
            errors.put(0, "Empty dictionary");
        } else {
            for (DictionaryEntry de : contents) {
                // first check length of code if we have a dictionary
                String code = de.getCode();
                if (dictionary != null) {
                    if ((dictionary.isAllowCodesOfDifferentLength() && code.length() <= fullCodeLength) || 
                            (code.length() == codeLength || code.length() == fullCodeLength)) {
                        // all is swell
                    }
                    else {
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
            app.saveDictionaryEntry(entry, null);
            // dictionaryEntriesMap.put(entry.getCode(), entry.getDescription());
        }
    }

    /**
     * Returns a list of DictionaryEntry's which code starts with the passed
     * parameter. Example: if codeStart = 0023, then all entries that have those
     * 4 digits at the start will be returned.
     *
     * @param codeStart
     * @param dictionaryEntries
     * @return
     */
    public static List<DictionaryEntry> getDictionaryEntriesCodeStartingWith(String codeStart,
            DictionaryEntry[] dictionaryEntries) {
        LinkedList<DictionaryEntry> entriesList = new LinkedList<>();
        for (DictionaryEntry entry : dictionaryEntries) {
            String code = entry.getCode();
            if (code.startsWith(codeStart) && code.length() > codeStart.length()) {
                entriesList.add(entry);
            }
        }
        return entriesList;
    }

    /**
     * Returns the DictionaryEntry with the best code match. Example: we have
     * the dictionary entries 00235 0023579 0023. If we run this method passing
     * 00235 as parameter, then the Dictionary Entry with 00235 is returned. But
     * if we pass 0023, then the Dictionary Entry with 0023 is returned.
     *
     * @param code
     * @param dictionaryEntries
     * @return
     */
    public static DictionaryEntry getDictionaryEntryBestMatchingSubcode(String code,
            DictionaryEntry[] dictionaryEntries) {
        for (int i = (code.length() - 1); i > 0; i--) {
            String possibleMatch = code.substring(0, i);
            for (DictionaryEntry entry : dictionaryEntries) {
                if (entry.getCode().equalsIgnoreCase(possibleMatch)) {
                    return entry;
                }
            }
        }
        return null;
    }
}
