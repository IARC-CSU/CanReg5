/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2026 International Agency for Research on Cancer
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
 */
package canreg.client.gui.importers;

import canreg.client.dataentry.Relation;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 * Persists user-confirmed column-to-variable mappings per installation so
 * CanReg5
 * can remember non-standard column names across imports without aggressive
 * fuzzy guessing.
 */
public class LearnedVariableMappings {

    private static final Logger LOGGER = Logger.getLogger(LearnedVariableMappings.class.getName());
    public static final String DEFAULT_FILE_NAME = "variable_mappings.properties";

    private static LearnedVariableMappings defaultInstance;

    private final File storageFile;
    private final Properties properties;

    public LearnedVariableMappings() {
        this(new File(Globals.CANREG_CLIENT_FOLDER + Globals.FILE_SEPARATOR + DEFAULT_FILE_NAME));
    }

    public LearnedVariableMappings(File storageFile) {
        this.storageFile = storageFile;
        this.properties = new Properties();
        load();
    }

    public static synchronized LearnedVariableMappings getInstance() {
        if (defaultInstance == null) {
            defaultInstance = new LearnedVariableMappings();
        }
        return defaultInstance;
    }

    public synchronized void load() {
        properties.clear();
        if (storageFile != null && storageFile.exists() && storageFile.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(storageFile);
                properties.load(fis);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Could not load learned variable mappings from " + storageFile, ex);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    public synchronized void save() {
        if (storageFile == null) {
            return;
        }
        File parent = storageFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(storageFile);
            properties.store(fos, "CanReg5 Learned Variable Mappings");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not save learned variable mappings to " + storageFile, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public synchronized void recordMapping(String tableName, String fileColumnName, String databaseVariableName,
            String standardVariableName) {
        if (tableName == null || fileColumnName == null || databaseVariableName == null) {
            return;
        }
        String tableKey = normalize(tableName);
        String colKey = normalize(fileColumnName);
        String dbVarTrimmed = databaseVariableName.trim();
        if (tableKey.isEmpty() || colKey.isEmpty() || dbVarTrimmed.isEmpty()) {
            return;
        }
        properties.setProperty("short." + tableKey + "." + colKey, dbVarTrimmed);
        if (standardVariableName != null && !standardVariableName.trim().isEmpty()) {
            properties.setProperty("std." + tableKey + "." + colKey, standardVariableName.trim());
        }
    }

    public synchronized void saveMappings(Document doc, List<Relation> relations) {
        if (doc == null || relations == null || relations.isEmpty()) {
            return;
        }
        DatabaseVariablesListElement[] allVars = Tools.getVariableListElements(doc, Globals.NAMESPACE);
        boolean changed = false;
        for (Relation rel : relations) {
            if (rel == null || rel.getDatabaseTableVariableID() < 0) {
                continue;
            }
            String fileCol = rel.getFileVariableName();
            String tableName = rel.getDatabaseTableName();
            String dbVarName = rel.getDatabaseVariableName();
            if (fileCol == null || fileCol.trim().isEmpty() || tableName == null || dbVarName == null) {
                continue;
            }
            DatabaseVariablesListElement matchedVar = null;
            for (DatabaseVariablesListElement v : allVars) {
                if (v.getDatabaseTableVariableID() == rel.getDatabaseTableVariableID()
                        && v.getDatabaseTableName().equalsIgnoreCase(tableName)) {
                    matchedVar = v;
                    break;
                }
            }
            if (matchedVar == null) {
                for (DatabaseVariablesListElement v : allVars) {
                    if (v.getDatabaseTableName().equalsIgnoreCase(tableName)
                            && v.getDatabaseVariableName().equalsIgnoreCase(dbVarName)) {
                        matchedVar = v;
                        break;
                    }
                }
            }
            String stdName = matchedVar != null ? matchedVar.getStandardVariableName() : null;
            recordMapping(tableName, fileCol, dbVarName, stdName);
            changed = true;
        }
        if (changed) {
            save();
        }
    }

    public synchronized DatabaseVariablesListElement findLearnedVariable(
            String targetTableName,
            String fileColumnName,
            DatabaseVariablesListElement[] candidateVariables) {
        if (fileColumnName == null || candidateVariables == null || candidateVariables.length == 0) {
            return null;
        }
        String colKey = normalize(fileColumnName);
        if (colKey.isEmpty()) {
            return null;
        }

        if (targetTableName != null && !targetTableName.trim().isEmpty()) {
            String tableKey = normalize(targetTableName);
            String learnedShort = properties.getProperty("short." + tableKey + "." + colKey);
            if (learnedShort != null && !learnedShort.isEmpty()) {
                for (DatabaseVariablesListElement v : candidateVariables) {
                    if (v.getDatabaseTableName().equalsIgnoreCase(targetTableName)
                            && v.getDatabaseVariableName().equalsIgnoreCase(learnedShort)) {
                        return v;
                    }
                }
            }
            String learnedStd = properties.getProperty("std." + tableKey + "." + colKey);
            if (learnedStd != null && !learnedStd.isEmpty()) {
                for (DatabaseVariablesListElement v : candidateVariables) {
                    if (v.getDatabaseTableName().equalsIgnoreCase(targetTableName)
                            && v.getStandardVariableName() != null
                            && v.getStandardVariableName().equalsIgnoreCase(learnedStd)) {
                        return v;
                    }
                }
            }
            // Cross-table foreign key equivalence (e.g. PatientID <-> PatientIDTumourTable)
            String equivalentStd = findEquivalentForeignKeyStandardVariable(targetTableName, colKey);
            if (equivalentStd != null) {
                for (DatabaseVariablesListElement v : candidateVariables) {
                    if (v.getDatabaseTableName().equalsIgnoreCase(targetTableName)
                            && v.getStandardVariableName() != null
                            && v.getStandardVariableName().equalsIgnoreCase(equivalentStd)) {
                        return v;
                    }
                }
            }
        } else {
            // Single-file import across all tables
            String[] tables = new String[] {
                    Globals.PATIENT_TABLE_NAME,
                    Globals.TUMOUR_TABLE_NAME,
                    Globals.SOURCE_TABLE_NAME
            };
            for (String tbl : tables) {
                DatabaseVariablesListElement found = findLearnedVariable(tbl, fileColumnName, candidateVariables);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String findEquivalentForeignKeyStandardVariable(String targetTableName, String colKey) {
        if (Globals.TUMOUR_TABLE_NAME.equalsIgnoreCase(targetTableName)) {
            String patientStd = properties.getProperty("std." + normalize(Globals.PATIENT_TABLE_NAME) + "." + colKey);
            if (Globals.StandardVariableNames.PatientID.toString().equalsIgnoreCase(patientStd)) {
                return Globals.StandardVariableNames.PatientIDTumourTable.toString();
            }
            if (Globals.StandardVariableNames.PatientRecordID.toString().equalsIgnoreCase(patientStd)) {
                return Globals.StandardVariableNames.PatientRecordIDTumourTable.toString();
            }
        } else if (Globals.PATIENT_TABLE_NAME.equalsIgnoreCase(targetTableName)) {
            String tumourStd = properties.getProperty("std." + normalize(Globals.TUMOUR_TABLE_NAME) + "." + colKey);
            if (Globals.StandardVariableNames.PatientIDTumourTable.toString().equalsIgnoreCase(tumourStd)) {
                return Globals.StandardVariableNames.PatientID.toString();
            }
            if (Globals.StandardVariableNames.PatientRecordIDTumourTable.toString().equalsIgnoreCase(tumourStd)) {
                return Globals.StandardVariableNames.PatientRecordID.toString();
            }
        } else if (Globals.SOURCE_TABLE_NAME.equalsIgnoreCase(targetTableName)) {
            String tumourStd = properties.getProperty("std." + normalize(Globals.TUMOUR_TABLE_NAME) + "." + colKey);
            if (Globals.StandardVariableNames.TumourID.toString().equalsIgnoreCase(tumourStd)) {
                return Globals.StandardVariableNames.TumourIDSourceTable.toString();
            }
        }
        return null;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ENGLISH);
    }
}
