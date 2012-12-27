/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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

package canreg.common.qualitycontrol;

import canreg.common.Globals;
import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class CheckResult implements Serializable {

    private boolean passed = false;
    private String checkName = "Check";
    private String message = "";
    private ResultCode resultCode;
    private LinkedList<Globals.StandardVariableNames> standardVariablesInvolved;

    public CheckResult() {
        standardVariablesInvolved = new LinkedList<Globals.StandardVariableNames>();
    }

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
        NotDone, // Relevant variable not declared
        /**
         * 
         */
        OK,
        /**
         * 
         */
        Query, // Warning, or other problem, tho' not invalid
        /**
         * 
         */
        Rare, // Rare, possibly an error
        /**
         * 
         */
        Missing, // Relevant variable missing value
        /**
         * 
         */
        Invalid
    }

    /**
     * 
     * @return
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * 
     * @param passed
     */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * 
     * @return
     */
    public String getCheckName() {
        return checkName;
    }

    /**
     * 
     * @param checkName
     */
    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    /**
     * 
     * @return
     */
    public String getMessage() {
        return message;
    }

    public Globals.StandardVariableNames[] getVariablesInvolved() {
        return standardVariablesInvolved.toArray(new Globals.StandardVariableNames[0]);
    }

    public void addVariableInvolved(Globals.StandardVariableNames variableInvolved) {
        standardVariablesInvolved.add(variableInvolved);
    }

    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return resultCode + " " + checkName + ": " + message;
    }

    /*
     * 0	Not done
     * 1	Done: OK
     * 2	Done: Rare
     * 3	Done: Invalid
     */
    public static String toDatabaseVariable(ResultCode resultCode) {
        String result = null;
        if (resultCode == ResultCode.NotDone) {
            result = "0";
        } else if (resultCode == ResultCode.OK) {
            result = "1";
        } else if (resultCode == ResultCode.Rare) {
            result = "2";
        } else if (resultCode == ResultCode.Invalid) {
            result = "3";
        } else if (resultCode == ResultCode.Query) {
            result = "1";
        } else if (resultCode == ResultCode.Missing) {
            result = "0";
        }
        return result;
    }

    public static ResultCode toResultCode(String resultCode) {
        ResultCode result = null;
        if (resultCode.equals("0")) {
            result = ResultCode.NotDone;
        } else if (resultCode.equals("1")) {
            result = ResultCode.OK;
        } else if (resultCode.equals("2")) {
            result = ResultCode.Rare;
        } else if (resultCode.equals("3")) {
            result = ResultCode.Invalid;
        }

        /* Never happens
        
        else if (resultCode.equals("1")) {
        result = ResultCode.Query;
        } else if (resultCode.equals("0")) {
        result = ResultCode.Missing;
        }
         */
        return result;
    }

    public static int compareResultSets(ResultCode resultCodeA, ResultCode resultCodeB) {
        if (resultCodeA == resultCodeB) {
            return 0;
        } else if (decideWorstResultCode(resultCodeA, resultCodeB) == resultCodeA) {
            return 1;
        } else {
            return -1;
        }
    }

    public static ResultCode decideWorstResultCode(ResultCode resultCodeA, ResultCode resultCodeB) {
        ResultCode worstResultCodeFound = resultCodeB;
        if (resultCodeA == CheckResult.ResultCode.NotDone) {
            worstResultCodeFound = resultCodeB;
        } else if (resultCodeB == CheckResult.ResultCode.NotDone) {
            worstResultCodeFound = resultCodeA;
        } else if (resultCodeA == CheckResult.ResultCode.OK) {
            worstResultCodeFound = resultCodeB;
        } else if (resultCodeA == CheckResult.ResultCode.Invalid) {
            worstResultCodeFound = CheckResult.ResultCode.Invalid;
        } else if (worstResultCodeFound != CheckResult.ResultCode.Invalid) {
            if (resultCodeA == CheckResult.ResultCode.Query) {
                worstResultCodeFound = CheckResult.ResultCode.Query;
            } else if (worstResultCodeFound != CheckResult.ResultCode.Query) {
                worstResultCodeFound = resultCodeA;
            }
        }
        return worstResultCodeFound;
    }
}
