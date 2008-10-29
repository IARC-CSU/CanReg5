package canreg.common;

/**
 *
 * @author ervikm
 */
public class DatabaseDictionaryListElement {

    private int dictionaryID;
    private String name;
    private String font;
    private String type;
    private int codeLength;
    private int categoryDescriptionLength;
    private int fullDictionaryCodeLength;
    private int fullDictionaryCategoryDescriptionLength;

    /**
     * 
     * @return
     */
    public int getDictionaryID() {
        return dictionaryID;
    }

    /**
     * 
     * @param dictionaryID
     */
    public void setDictionaryID(int dictionaryID) {
        this.dictionaryID = dictionaryID;
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
    public int getFullDictionaryCategoryDescriptionLength() {
        return fullDictionaryCategoryDescriptionLength;
    }

    /**
     * 
     * @param fullDictionaryCategoryDescriptionLength
     */
    public void setFullDictionaryCategoryDescriptionLength(int fullDictionaryCategoryDescriptionLength) {
        this.fullDictionaryCategoryDescriptionLength = fullDictionaryCategoryDescriptionLength;
    }

    /**
     * 
     * @param o
     * @return
     */
    public boolean equals(DatabaseVariablesListElement o) {
        return dictionaryID == o.getDatabaseTableVariableID();
    }

    @Override
    public String toString() {
        return name;
    }
}
