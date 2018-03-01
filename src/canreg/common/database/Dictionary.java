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

 package canreg.common.database;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class Dictionary extends canreg.common.DatabaseDictionaryListElement {

    private Map<String, DictionaryEntry> dictionaryEntries;
    private LinkedList[] codes;

    /**
     * 
     */
    public static String COMPOUND_DICTIONARY_TYPE = "Compound";

    /**
     * 
     */
    public Dictionary() {
        dictionaryEntries = new LinkedHashMap();
        codes = new LinkedList[1];
        codes[0] = new LinkedList();
    }

    /**
     * 
     * @return
     */
    public boolean isCompoundDictionary() {
        return COMPOUND_DICTIONARY_TYPE.equalsIgnoreCase(getType());
    }

    /**
     * 
     * @param type
     */
    @Override
    public void setType(String type) {
        this.type = type;
        if (type.equalsIgnoreCase(COMPOUND_DICTIONARY_TYPE)) {
            setCompoundDictionary(true);
            codes = new LinkedList[2];
            codes[0] = new LinkedList();
            codes[1] = new LinkedList();
        }
    }

    /**
     * 
     * @param code
     * @param entry
     */
    public void addDictionaryEntry(String code, DictionaryEntry entry) {
        dictionaryEntries.put(code, entry);
        if (isCompoundDictionary()) {
            if (code.length() == getFullDictionaryCodeLength()) {
                codes[1].add(code);
            } else {
                codes[0].add(code);
            }
        }
    }

    /**
     * 
     * @param code
     * @return
     */
    public DictionaryEntry getDictionaryEntry(String code) {
        return dictionaryEntries.get(code);
    }

    /**
     * 
     * @return
     */
    public Map<String, DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    /**
     * 
     * @param dictionaryEntries
     */
    public void setDictionaryEntries(Map<String, DictionaryEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }

}
