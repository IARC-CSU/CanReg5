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

    public int getDictionaryID() {
        return dictionaryID;
    }

    public void setDictionaryID(int dictionaryID) {
        this.dictionaryID = dictionaryID;
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

    public int getFullDictionaryCategoryDescriptionLength() {
        return fullDictionaryCategoryDescriptionLength;
    }

    public void setFullDictionaryCategoryDescriptionLength(int fullDictionaryCategoryDescriptionLength) {
        this.fullDictionaryCategoryDescriptionLength = fullDictionaryCategoryDescriptionLength;
    }

    public boolean equals(DatabaseVariablesListElement o) {
        return dictionaryID == o.getDatabaseTableVariableID();
    }

    @Override
    public String toString() {
        return name;
    }
}
