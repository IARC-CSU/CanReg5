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

package canreg.common.conversions;

import canreg.common.Globals;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class ConversionResult implements Serializable {
    private Globals.StandardVariableNames variableName;
    private Object value;
    
    private String message ="";
    private ResultCode resultCode;

    /**
     * 
     * @return
     */
    public ResultCode getResultCode() {
        return resultCode;
    }

    /**
     * 
     * @param resultCode
     */
    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
    
    /**
     * 
     */
    public enum ResultCode {
        /**
         * 
         */
        NotDone,  // Relevant variable not declared
        /**
         * 
         */
        OK,
        /**
         * 
         */
        Query ,   // Warning, or other problem, tho' not invalid
        /**
         * 
         */
        Rare,     // Rare, possibly an error
        /**
         * 
         */
        Missing,   // Relevant variable missing value
        /**
         * 
         */
        Invalid
 }

    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames getVariableName() {
        return variableName;
    }

    /**
     * 
     * @param variableName
     */
    public void setVariableName(Globals.StandardVariableNames variableName) {
        this.variableName = variableName;
    }

    /**
     * 
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * 
     * @param value
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * 
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
