/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2019  International Agency for Research on Cancer
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
 * @author Patricio Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.importers;

import java.io.Serializable;

/**
 *
 * @author Patricio Carranza, patocarranza@gmail.com
 */
public class RCheksImportVariables implements Serializable {
    
    private String patientFilePath;
    private String tumourFilePath;
    private String sourceFilePath;
    private String patientFileSeparator;
    private String tumourFileSeparator;
    private String sourceFileSeparator;
    private String[] patientVarNameInImportFile;
    private String[] patientVarNameInDatabase; 
    private String[] tumourVarNameInImportFile;
    private String[] tumourVarNameInDatabase;
    private String[] sourceVarNameInImportFile;
    private String[] sourceVarNameInDatabase;
    private String systemDescriptionXMLPath;
    private String dictionaryFilePath;
    

    public String getPatientFilePath() {
        return patientFilePath;
    }

    public void setPatientFilePath(String PatientFilePath) {
        this.patientFilePath = PatientFilePath;
    }

    public String getTumourFilePath() {
        return tumourFilePath;
    }

    public void setTumourFilePath(String TumourFilePath) {
        this.tumourFilePath = TumourFilePath;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String SourceFilePath) {
        this.sourceFilePath = SourceFilePath;
    }

    public String getPatientFileSeparator() {
        return patientFileSeparator;
    }

    public void setPatientFileSeparator(String PatientFileSeparator) {
        this.patientFileSeparator = PatientFileSeparator;
    }

    public String getTumourFileSeparator() {
        return tumourFileSeparator;
    }

    public void setTumourFileSeparator(String TumourFileSeparator) {
        this.tumourFileSeparator = TumourFileSeparator;
    }

    public String getSourceFileSeparator() {
        return sourceFileSeparator;
    }

    public void setSourceFileSeparator(String SourceFileSeparator) {
        this.sourceFileSeparator = SourceFileSeparator;
    }

    public String[] getPatientVarNameInImportFile() {
        return patientVarNameInImportFile;
    }

    public void setPatientVarNameInImportFile(String[] patientVarNameInImportFile) {
        this.patientVarNameInImportFile = patientVarNameInImportFile;
    }

    public String[] getPatientVarNameInDatabase() {
        return patientVarNameInDatabase;
    }

    public void setPatientVarNameInDatabase(String[] patientVarNameInDatabase) {
        this.patientVarNameInDatabase = patientVarNameInDatabase;
    }

    public String[] getTumourVarNameInImportFile() {
        return tumourVarNameInImportFile;
    }

    public void setTumourVarNameInImportFile(String[] tumourVarNameInImportFile) {
        this.tumourVarNameInImportFile = tumourVarNameInImportFile;
    }

    public String[] getTumourVarNameInDatabase() {
        return tumourVarNameInDatabase;
    }

    public void setTumourVarNameInDatabase(String[] tumourVarNameInDatabase) {
        this.tumourVarNameInDatabase = tumourVarNameInDatabase;
    }

    public String[] getSourceVarNameInImportFile() {
        return sourceVarNameInImportFile;
    }

    public void setSourceVarNameInImportFile(String[] sourceVarNameInImportFile) {
        this.sourceVarNameInImportFile = sourceVarNameInImportFile;
    }

    public String[] getSourceVarNameInDatabase() {
        return sourceVarNameInDatabase;
    }

    public void setSourceVarNameInDatabase(String[] sourceVarNameInDatabase) {
        this.sourceVarNameInDatabase = sourceVarNameInDatabase;
    }

    public String getSystemDescriptionXMLPath() {
        return systemDescriptionXMLPath;
    }

    public void setSystemDescriptionXMLPath(String systemDescriptionXMLPath) {
        this.systemDescriptionXMLPath = systemDescriptionXMLPath;
    }

    public String getDictionaryFilePath() {
        return dictionaryFilePath;
    }

    public void setDictionaryFilePath(String dictionaryFilePath) {
        this.dictionaryFilePath = dictionaryFilePath;
    }
    
    
}
