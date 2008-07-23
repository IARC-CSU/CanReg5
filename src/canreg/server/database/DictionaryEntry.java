/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server.database;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DictionaryEntry implements Serializable {
    private int dictionaryID;
    private String code;
    private String description;

    public DictionaryEntry(int dicID, String code, String description){
        this.dictionaryID = dicID;
        this.code = code;
        this.description = description;
    }
    
    public int getDictionaryID() {
        return dictionaryID;
    }

    public void setDictionaryID(int dictionaryID) {
        this.dictionaryID = dictionaryID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return code +" - "+description;
    }
    
}
