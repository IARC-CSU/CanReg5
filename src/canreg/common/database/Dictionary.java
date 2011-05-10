package canreg.common.database;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ervikm
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
    public int getDictionaryId() {
        return dictionaryId;
    }

    /**
     * 
     * @return
     */
    public boolean isCompoundDictionary() {
        return COMPOUND_DICTIONARY_TYPE.equalsIgnoreCase(type);
    }

    /**
     * 
     * @param dictionaryId
     */
    public void setDictionaryId(int dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     */
    public String getFont() {
        return font;
    }

    /**
     * 
     * @param font
     */
    public void setFont(String font) {
        this.font = font;
    }

    /**
     * 
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     */
    public void setType(String type) {
        this.type = type;
        if (type.equalsIgnoreCase(COMPOUND_DICTIONARY_TYPE)) {
            compoundDictionary = true;
            codes = new LinkedList[2];
            codes[0] = new LinkedList();
            codes[1] = new LinkedList();
        }
    }

    /**
     * 
     * @return
     */
    public int getCodeLength() {
        return codeLength;
    }

    /**
     * 
     * @param codeLength
     */
    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    /**
     * 
     * @return
     */
    public int getCategoryDescriptionLength() {
        return categoryDescriptionLength;
    }

    /**
     * 
     * @param categoryDescriptionLength
     */
    public void setCategoryDescriptionLength(int categoryDescriptionLength) {
        this.categoryDescriptionLength = categoryDescriptionLength;
    }

    /**
     * 
     * @return
     */
    public int getFullDictionaryCodeLength() {
        return fullDictionaryCodeLength;
    }

    /**
     * 
     * @param fullDictionaryCodeLength
     */
    public void setFullDictionaryCodeLength(int fullDictionaryCodeLength) {
        this.fullDictionaryCodeLength = fullDictionaryCodeLength;
    }

    /**
     * 
     * @return
     */
    public int getFullDictionaryDescriptionLength() {
        return fullDictionaryDescriptionLength;
    }

    /**
     * 
     * @param fullDictionaryDescriptionLength
     */
    public void setFullDictionaryDescriptionLength(int fullDictionaryDescriptionLength) {
        this.fullDictionaryDescriptionLength = fullDictionaryDescriptionLength;
    }

    /**
     * 
     * @param textContent
     */
    public void setCategoryDescriptionLength(String textContent) {
        setCategoryDescriptionLength(stringToInt(textContent));
    }

    /**
     * 
     * @param textContent
     */
    public void setCodeLength(String textContent) {
        setCodeLength(stringToInt(textContent));
    }

    /**
     * 
     * @param textContent
     */
    public void setFullDictionaryCodeLength(String textContent) {
        setFullDictionaryCodeLength(stringToInt(textContent));
    }

    /**
     * 
     * @param textContent
     */
    public void setFullDictionaryDescriptionLength(String textContent) {
        setFullDictionaryDescriptionLength(stringToInt(textContent));
    }

    private static int stringToInt(String s) {
        return Integer.parseInt(s);
    }

    /**
     * 
     * @param code
     * @param entry
     */
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
