/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author morten
 */
public class Dictionary implements Serializable {

    private int dictionaryId;
    private String name;
    private String font;
    private String type;
    private int codeLength = 0;
    private int categoryDescriptionLength = 0;
    private int fullDictionaryCodeLength = 0;
    private int fullDictionaryDescriptionLength = 0;
    private Map<String, DictionaryEntry> dictionaryEntries;
    private LinkedList[] codes;
    private boolean compoundDictionary = false;
    public static String COMPOUND_DICTIONARY_TYPE = "Compound";

    public Dictionary() {
        dictionaryEntries = new LinkedHashMap();
        codes = new LinkedList[1];
        codes[0] = new LinkedList();
    }

    public int getDictionaryId() {
        return dictionaryId;
    }

    public boolean isCompoundDictionary() {
        return COMPOUND_DICTIONARY_TYPE.equalsIgnoreCase(type);
    }

    public void setDictionaryId(int dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        if (type.equalsIgnoreCase(COMPOUND_DICTIONARY_TYPE)) {
            compoundDictionary = true;
            codes = new LinkedList[2];
            codes[0] = new LinkedList();
            codes[1] = new LinkedList();
        }
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getCategoryDescriptionLength() {
        return categoryDescriptionLength;
    }

    public void setCategoryDescriptionLength(int categoryDescriptionLength) {
        this.categoryDescriptionLength = categoryDescriptionLength;
    }

    public int getFullDictionaryCodeLength() {
        return fullDictionaryCodeLength;
    }

    public void setFullDictionaryCodeLength(int fullDictionaryCodeLength) {
        this.fullDictionaryCodeLength = fullDictionaryCodeLength;
    }

    public int getFullDictionaryDescriptionLength() {
        return fullDictionaryDescriptionLength;
    }

    public void setFullDictionaryDescriptionLength(int fullDictionaryDescriptionLength) {
        this.fullDictionaryDescriptionLength = fullDictionaryDescriptionLength;
    }

    public void setCategoryDescriptionLength(String textContent) {
        setCategoryDescriptionLength(stringToInt(textContent));
    }

    public void setCodeLength(String textContent) {
        setCodeLength(stringToInt(textContent));
    }

    public void setFullDictionaryCodeLength(String textContent) {
        setFullDictionaryCodeLength(stringToInt(textContent));
    }

    public void setFullDictionaryDescriptionLength(String textContent) {
        setFullDictionaryDescriptionLength(stringToInt(textContent));
    }

    private static int stringToInt(String s) {
        return Integer.parseInt(s);
    }

    public void addDictionaryEntry(String code, DictionaryEntry entry) {
        dictionaryEntries.put(code, entry);
        if (compoundDictionary) {
            if (code.length() == fullDictionaryCodeLength) {
                codes[1].add(code);
            } else {
                codes[0].add(code);
            }
        }
    }

    public DictionaryEntry getDictionaryEntry(String code) {
        return dictionaryEntries.get(code);
    }

    public Map<String, DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void setDictionaryEntries(Map<String, DictionaryEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }
}
