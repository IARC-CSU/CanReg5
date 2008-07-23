/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.dataentry;

import canreg.common.Globals;
import canreg.server.CanRegServerInterface;
import canreg.server.database.DictionaryEntry;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Some static tools to work with dictionaries
 * @author ervikm
 */
public class DictionaryHelper {
    public static HashMap<Integer, HashMap<String, String>> getDictionaryFromServer(CanRegServerInterface server) throws RemoteException {
        return server.getDictionary();
    }
    
    public static HashMap<String, String> getDictionaryByID(HashMap<Integer, HashMap<String, String>> dictionary, int dictionaryID){
        return dictionary.get(dictionaryID);
    }
    
    public static DictionaryEntry[] buildDictionaryEntriesFromMap(Map<String, String> dictionary) {
        DictionaryEntry[] dictionaryEntries = new DictionaryEntry[dictionary.size()];
        Iterator <String> iterator = dictionary.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()){
            String code = iterator.next().toString();
            String description = dictionary.get(code);
            dictionaryEntries[i++] = new DictionaryEntry(0, code, description);
        }
        return dictionaryEntries;
    }
    
    public static int getDictionaryIDbyName(Document doc, String name){
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
            found = name.equalsIgnoreCase(element.getElementsByTagName(Globals.NAMESPACE + "name").item(0).getTextContent());          
        }
        if (found) {
            id = i-1;
        }
        return id;
    }
    
    public static boolean clearDictionary(int dictionaryID, CanRegServerInterface server) throws RemoteException {
        return server.deleteDictionaryEntries(dictionaryID);
    }

    public static int saveDictionaryEntry(DictionaryEntry dictionaryEntry, CanRegServerInterface server) throws SecurityException, RemoteException {
        return server.saveDictionaryEntry(dictionaryEntry);
    }

    private static Vector<DictionaryEntry> parseDictionaryText(int dictionaryID, String str) {
        Vector dictionaryEntries = new Vector<DictionaryEntry>();
        String[] stringArray = str.split("[\\n|\\r]");
        if (stringArray[0].trim().length() > 0) {
            for (String string : stringArray) {
                String[] lineArray = string.split("\\t");
                if (lineArray.length > 1) {
                    dictionaryEntries.add(new DictionaryEntry(dictionaryID, lineArray[0], lineArray[1]));
                }
            }
        }
        return dictionaryEntries;
    }

    public static void replaceDictionary(int dictionaryID, String str, CanRegServerInterface server) throws RemoteException {
        Vector<DictionaryEntry> dictionaryEntries = parseDictionaryText(dictionaryID, str);
        HashMap<String, String> dictionaryEntriesMap = new HashMap<String, String>();

        boolean removed = clearDictionary(dictionaryID, server);

        for (DictionaryEntry entry : dictionaryEntries) {
            server.saveDictionaryEntry(entry);
            dictionaryEntriesMap.put(entry.getCode(),entry.getDescription());
        }
    }
}
