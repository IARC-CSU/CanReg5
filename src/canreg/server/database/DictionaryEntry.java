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

    /**
     * 
     * @param dicID
     * @param code
     * @param description
     */
    public DictionaryEntry(int dicID, String code, String description){
        this.dictionaryID = dicID;
        this.code = code;
        this.description = description;
    }
    
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
    public String getCode() {
        return code;
    }

    /**
     * 
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return code +" - "+description;
    }
    
    @Override
    public boolean equals(Object o){
        if (o instanceof DictionaryEntry){
            DictionaryEntry de = (DictionaryEntry) o;
            return code.equals(de.getCode());
        } else 
            return super.equals(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.code != null ? this.code.hashCode() : 0);
        return hash;
    }
}
