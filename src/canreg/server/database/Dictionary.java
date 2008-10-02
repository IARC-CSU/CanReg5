/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server.database;

/**
 *
 * @author morten
 */
public class Dictionary {
    private int dictionaryId;
    private String name;
    private String font;
    private String type;
    private int codeLength = 0;
    private int categoryDescriptionLength = 0;
    private int fullDictionaryCodeLength = 0;
    private int fullDictionaryDescriptionLength = 0;

    public int getDictionaryId() {
        return dictionaryId;
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
    
    private static int stringToInt(String s){
        return Integer.parseInt(s);
    }
}
