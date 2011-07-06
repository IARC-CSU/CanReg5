/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

package canreg.common;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DatabaseDictionaryListElement implements Serializable, DatabaseElement {

    private int dictionaryID;
    private String name = null;
    private String font = null;
    private String type = null;
    private int codeLength;
    private int categoryDescriptionLength;
    private int fullDictionaryCodeLength;
    private int fullDictionaryCategoryDescriptionLength;
    private boolean locked = false;
    private boolean compound = false;
    private String unknownCode = null;

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

    /**
     * @return the locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isCompound() {
        return ("Compound".equalsIgnoreCase(getType()));
    }

    public String getUnkownCode() {
        return unknownCode;
    }

    public void setUnkownCode(String unknownCode) {
        this.unknownCode = unknownCode;
    }

    @Override
    public String getDescriptiveString() {
        String desc = getName();
        if (isCompound()){
            desc += " (Compound, Code Length: " + getCodeLength() + ", Full length: "+ getFullDictionaryCodeLength() +")";  
        } else {
            desc += " (Simple, Full length: "+ getFullDictionaryCodeLength() +")";
        }
        return desc;
    }
}
