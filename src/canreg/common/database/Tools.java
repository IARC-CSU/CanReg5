/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016 International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */


package canreg.common.database;

import java.util.Set;

/**
 *
 * @author ervikm
 */
public class Tools {

    // Ref: http://db.apache.org/derby/docs/10.1/ref/rrefkeywords29722.html    
    private static final String[] reservedWordsSQL = {"ADD",
        "ALL",
        "ALLOCATE",
        "ALTER",
        "AND",
        "ANY",
        "ARE",
        "AS",
        "ASC",
        "ASSERTION",
        "AT",
        "AUTHORIZATION",
        "AVG",
        "BEGIN",
        "BETWEEN",
        "BIT",
        "BOOLEAN",
        "BOTH",
        "BY",
        "CALL",
        "CASCADE",
        "CASCADED",
        "CASE",
        "CAST",
        "CHAR",
        "CHARACTER",
        "CHECK",
        "CLOSE",
        "COLLATE",
        "COLLATION",
        "COLUMN",
        "COMMIT",
        "CONNECT",
        "CONNECTION",
        "CONSTRAINT",
        "CONSTRAINTS",
        "CONTINUE",
        "CONVERT",
        "CORRESPONDING",
        "COUNT",
        "CREATE",
        "CURRENT",
        "CURRENT_DATE",
        "CURRENT_TIME",
        "CURRENT_TIMESTAMP",
        "CURRENT_USER",
        "CURSOR",
        "DEALLOCATE",
        "DEC",
        "DECIMAL",
        "DECLARE",
        "DEFERRABLE",
        "DEFERRED",
        "DELETE",
        "DESC",
        "DESCRIBE",
        "DIAGNOSTICS",
        "DISCONNECT",
        "DISTINCT",
        "DOUBLE",
        "DROP",
        "ELSE",
        "END",
        "ENDEXEC",
        "ESCAPE",
        "EXCEPT",
        "EXCEPTION",
        "EXEC",
        "EXECUTE",
        "EXISTS",
        "EXPLAIN",
        "EXTERNAL",
        "FALSE",
        "FETCH",
        "FIRST",
        "FLOAT",
        "FOR",
        "FOREIGN",
        "FOUND",
        "FROM",
        "FULL",
        "FUNCTION",
        "GET",
        "GET_CURRENT_CONNECTION",
        "GLOBAL",
        "GO",
        "GOTO",
        "GRANT",
        "GROUP",
        "HAVING",
        "HOUR",
        "IDENTITY",
        "IMMEDIATE",
        "IN",
        "INDICATOR",
        "INITIALLY",
        "INNER",
        "INOUT",
        "INPUT",
        "INSENSITIVE",
        "INSERT",
        "INT",
        "INTEGER",
        "INTERSECT",
        "INTO",
        "IS",
        "ISOLATION",
        "JOIN",
        "KEY",
        "LAST",
        "LEFT",
        "LIKE",
        "LONGINT",
        "LOWER",
        "LTRIM",
        "MATCH",
        "MAX",
        "MIN",
        "MINUTE",
        "NATIONAL",
        "NATURAL",
        "NCHAR",
        "NVARCHAR",
        "NEXT",
        "NO",
        "NOT",
        "NULL",
        "NULLIF",
        "NUMERIC",
        "OF",
        "ON",
        "ONLY",
        "OPEN",
        "OPTION",
        "OR",
        "ORDER",
        "OUT",
        "OUTER",
        "OUTPUT",
        "OVERLAPS",
        "PAD",
        "PARTIAL",
        "PREPARE",
        "PRESERVE",
        "PRIMARY",
        "PRIOR",
        "PRIVILEGES",
        "PROCEDURE",
        "PUBLIC",
        "READ",
        "REAL",
        "REFERENCES",
        "RELATIVE",
        "RESTRICT",
        "REVOKE",
        "RIGHT",
        "ROLLBACK",
        "ROWS",
        "RTRIM",
        "SCHEMA",
        "SCROLL",
        "SECOND",
        "SELECT",
        "SESSION_USER",
        "SET",
        "SMALLINT",
        "SOME",
        "SPACE",
        "SQL",
        "SQLCODE",
        "SQLERROR",
        "SQLSTATE",
        "SUBSTR",
        "SUBSTRING",
        "SUM",
        "SYSTEM_USER",
        "TABLE",
        "TEMPORARY",
        "TIMEZONE_HOUR",
        "TIMEZONE_MINUTE",
        "TO",
        "TRAILING",
        "TRANSACTION",
        "TRANSLATE",
        "TRANSLATION",
        "TRUE",
        "UNION",
        "UNIQUE",
        "UNKNOWN",
        "UPDATE",
        "UPPER",
        "USER",
        "USING",
        "VALUES",
        "VARCHAR",
        "VARYING",
        "VIEW",
        "WHENEVER",
        "WHERE",
        "WITH",
        "WORK",
        "WRITE",
        "XML",
        "XMLEXISTS",
        "XMLPARSE",
        "XMLSERIALIZE",
        "YEAR"
    };

    /**
     * 
     * @param word
     * @return
     */
    static public boolean isReservedWord(String word) {
        boolean found = false;
        int i = 0;
        while (!found && i < reservedWordsSQL.length) {
            found = word.equalsIgnoreCase(reservedWordsSQL[i++]);
        }
        return found;
    }

    /**
     *
     * @param newRecord
     * @param oldRecord
     * @param variablesToSkip set of lower cased names of variables to be skipped
     * @return
     */
    static public boolean newRecordContainsNewInfo(DatabaseRecord newRecord, DatabaseRecord oldRecord, Set<String> variablesToSkip) {
        boolean noNewInfo = true;
        // First check if the records are the same class
        if (newRecord.getClass().isInstance(oldRecord)) {
            String[] variableNames = newRecord.getVariableNames();

            int pos = 0;

            Object value1;
            Object value2;

            while (noNewInfo && pos < variableNames.length) {
                if (variablesToSkip.contains(canreg.common.Tools.toLowerCaseStandardized(variableNames[pos]))) {
                    // skip this variable
                } else {
                    // compare
                    value1 = newRecord.getVariable(variableNames[pos]);
                    value2 = oldRecord.getVariable(variableNames[pos]);
                    if (value1==null || value2==null){
                        noNewInfo = (value1==value2);
                    }
                    else if (!value1.equals(value2)) {
                        noNewInfo = false;
                    }
                }
                pos++;
            }
        } else {
            noNewInfo = false;
        }
        return !noNewInfo;
    }
}
