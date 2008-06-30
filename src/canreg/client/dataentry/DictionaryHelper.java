/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.dataentry;

import canreg.server.CanRegServerInterface;
import canreg.server.database.DictionaryEntry;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

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
