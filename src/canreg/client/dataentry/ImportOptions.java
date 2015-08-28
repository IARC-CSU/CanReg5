/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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


package canreg.client.dataentry;

import java.nio.charset.Charset;

/**
 *
 * @author ervikm
 */
public class ImportOptions {

    /**
     * 
     */
    public final static int UPDATE = 0;
    /**
     * 
     */
    public final static int OVERWRITE = 1;
    /**
     * 
     */
    public final static int REJECT = 2;
    // maxlines = -1 to read whole file...
    private int maxLines;
    private boolean testOnly;
    private int discrepancies;
    private char separator;
    private boolean doChecks;
    private boolean doPersonSearch;
    private boolean queryNewNames;
    private boolean dataFromPreviousCanReg;
    private String multiplePrimaryVariableName;
    private String patientIDVariableName;
    private String tumourIDVariablename;
    private String patientRecordIDVariableName;
    private String tumourRecordIDVariablename;
    private String tumourUpdateDateVariableName;
    private String patientUpdateDateVariableName;
    private String patientIDTumourTableVariableName;
    private String patientRecordIDTumourTableVariableName;
    private String tumourIDSourceTableVariableName;
    private String obsoleteTumourFlagVariableName;
    private String obsoletePatientFlagVariableName;
    private String tumourSequenceVariableName;
    private String tumourCheckStatus;
    private String tumourRecordStatus;
    private String ICD10VariableName;
    private Charset fileCharset;
    private Charset[] fileCharsets;
    private char[] separators;
    private String reportFileName;
    private String firstNameVariableName;
    private String sexVariableName;
    private String sourceIDVariablename;

    /**
     * 
     * @return
     */
    public int getMaxLines() {
        return maxLines;
    }

    /**
     * 
     * @param maxLines
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    /**
     * 
     * @return
     */
    public boolean isTestOnly() {
        return testOnly;
    }

    /**
     * 
     * @param testOnly
     */
    public void setTestOnly(boolean testOnly) {
        this.testOnly = testOnly;
    }

    /**
     * 
     * @return
     */
    public int getDiscrepancies() {
        return discrepancies;
    }

    /**
     * 
     * @param discrepancies
     */
    public void setDiscrepancies(int discrepancies) {
        this.discrepancies = discrepancies;
    }

    /**
     * 
     * @return
     */
    public boolean isDoChecks() {
        return doChecks;
    }

    /**
     * 
     * @param doChecks
     */
    public void setDoChecks(boolean doChecks) {
        this.doChecks = doChecks;
    }

    /**
     * 
     * @return
     */
    public boolean isDoPersonSearch() {
        return doPersonSearch;
    }

    /**
     * 
     * @param doPersonSearch
     */
    public void setDoPersonSearch(boolean doPersonSearch) {
        this.doPersonSearch = doPersonSearch;
    }

    /**
     * 
     * @return
     */
    public boolean isQueryNewNames() {
        return queryNewNames;
    }

    /**
     * 
     * @param queryNewNames
     */
    public void setQueryNewNames(boolean queryNewNames) {
        this.queryNewNames = queryNewNames;
    }

    /**
     * 
     * @return
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * 
     * @param separator
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * 
     * @return
     */
    public boolean isDataFromPreviousCanReg() {
        return dataFromPreviousCanReg;
    }

    /**
     * 
     * @param dataFromPreviousCanReg
     */
    public void setDataFromPreviousCanReg(boolean dataFromPreviousCanReg) {
        this.dataFromPreviousCanReg = dataFromPreviousCanReg;
    }

    /**
     * @return the multiplePrimaryVariableName
     */
    public String getMultiplePrimaryVariableName() {
        return multiplePrimaryVariableName;
    }

    /**
     * @param multiplePrimaryVariableName the multiplePrimaryVariableName to set
     */
    public void setMultiplePrimaryVariableName(String multiplePrimaryVariableName) {
        this.multiplePrimaryVariableName = multiplePrimaryVariableName;
    }

    /**
     * @return the patientIDVariableName
     */
    public String getPatientIDVariableName() {
        return patientIDVariableName;
    }

    /**
     * @param patientIDVariableName the patientIDVariableName to set
     */
    public void setPatientIDVariableName(String patientIDVariableName) {
        this.patientIDVariableName = patientIDVariableName;
    }

    /**
     * @return the tumourIDVariablename
     */
    public String getTumourIDVariablename() {
        return tumourIDVariablename;
    }

    /**
     * @param tumourIDVariablename the tumourIDVariablename to set
     */
    public void setTumourIDVariablename(String tumourIDVariablename) {
        this.tumourIDVariablename = tumourIDVariablename;
    }

        /**
     * @return the tumourIDVariablename
     */
    public String getSourceIDVariablename() {
        return sourceIDVariablename;
    }

    /**
     * @param tumourIDVariablename the tumourIDVariablename to set
     */
    public void setSourceIDVariablename(String sourceIDVariablename) {
        this.sourceIDVariablename = sourceIDVariablename;
    }
    
    /**
     * @return the tumourUpdateDateVariableName
     */
    String getTumourUpdateDateVariableName() {
        return tumourUpdateDateVariableName;
    }

    /**
     * @param tumourUpdateDateVariableName the tumourUpdateDateVariableName to set
     */
    public void setTumourUpdateDateVariableName(String tumourUpdateDateVariableName) {
        this.tumourUpdateDateVariableName = tumourUpdateDateVariableName;
    }

    /**
     * @return the patientUpdateDateVariableName
     */
    public String getPatientUpdateDateVariableName() {
        return patientUpdateDateVariableName;
    }

    /**
     * @param patientUpdateDateVariableName the patientUpdateDateVariableName to set
     */
    public void setPatientUpdateDateVariableName(String patientUpdateDateVariableName) {
        this.patientUpdateDateVariableName = patientUpdateDateVariableName;
    }

    /**
     * @return the PatientIDTumourTableVariableName
     */
    public String getPatientIDTumourTableVariableName() {
        return patientIDTumourTableVariableName;
    }

    /**
     * @param PatientIDTumourTableVariableName the PatientIDTumourTableVariableName to set
     */
    public void setPatientIDTumourTableVariableName(String PatientIDTumourTableVariableName) {
        this.patientIDTumourTableVariableName = PatientIDTumourTableVariableName;
    }

    /**
     * @return the patientRecordIDVariableName
     */
    public String getPatientRecordIDVariableName() {
        return patientRecordIDVariableName;
    }

    /**
     * @param patientRecordIDVariableName the patientRecordIDVariableName to set
     */
    public void setPatientRecordIDVariableName(String patientRecordIDVariableName) {
        this.patientRecordIDVariableName = patientRecordIDVariableName;
    }

    /**
     * @return the tumourRecordIDVariablename
     */
    public String getTumourRecordIDVariablename() {
        return tumourRecordIDVariablename;
    }

    /**
     * @param tumourRecordIDVariablename the tumourRecordIDVariablename to set
     */
    public void setTumourRecordIDVariablename(String tumourRecordIDVariablename) {
        this.tumourRecordIDVariablename = tumourRecordIDVariablename;
    }

    /**
     * @return the patientRecordIDTumourTableVariableName
     */
    public String getPatientRecordIDTumourTableVariableName() {
        return patientRecordIDTumourTableVariableName;
    }

    /**
     * @param patientRecordIDTumourTableVariableName the patientRecordIDTumourTableVariableName to set
     */
    public void setPatientRecordIDTumourTableVariableName(String patientRecordIDTumourTableVariableName) {
        this.patientRecordIDTumourTableVariableName = patientRecordIDTumourTableVariableName;
    }

    /**
     * @return the deprecatedTumourFlagVariableName
     */
    public String getObsoleteTumourFlagVariableName() {
        return obsoleteTumourFlagVariableName;
    }

    /**
     * @param deprecatedTumourFlagVariableName the deprecatedTumourFlagVariableName to set
     */
    public void setObsoleteTumourFlagVariableName(String deprecatedTumourFlagVariableName) {
        this.obsoleteTumourFlagVariableName = deprecatedTumourFlagVariableName;
    }

    /**
     * @return the deprecatedPatientFlagVariableName
     */
    public String getObsoletePatientFlagVariableName() {
        return obsoletePatientFlagVariableName;
    }

    /**
     * @param deprecatedPatientFlagVariableName the deprecatedPatientFlagVariableName to set
     */
    public void setObsoletePatientFlagVariableName(String deprecatedPatientFlagVariableName) {
        this.obsoletePatientFlagVariableName = deprecatedPatientFlagVariableName;
    }

    /**
     * @return the fileCharset
     */
    public Charset getFileCharset() {
        return fileCharset;
    }

    /**
     * @param fileCharset the fileCharset to set
     */
    public void setFileCharset(Charset fileCharset) {
        this.fileCharset = fileCharset;
    }

    /**
     * @return the tumourSequenceVariableName
     */
    public String getTumourSequenceVariableName() {
        return tumourSequenceVariableName;
    }

    /**
     * @param tumourSequenceVariableName the tumourSequenceVariableName to set
     */
    public void setTumourSequenceVariableName(String tumourSequenceVariableName) {
        this.tumourSequenceVariableName = tumourSequenceVariableName;
    }

    public void setFilesCharsets(Charset[] charsets) {
        this.fileCharsets = charsets;
    }

    public void setSeparators(char[] separators) {
        this.separators = separators;
    }

    /**
     * @return the fileCharsets
     */
    public Charset[] getFileCharsets() {
        return fileCharsets;
    }

    /**
     * @return the separators
     */
    public char[] getSeparators() {
        return separators;
    }

    /**
     * @return the tumourIDSourceTableVariableName
     */
    public String getTumourIDSourceTableVariableName() {
        return tumourIDSourceTableVariableName;
    }

    /**
     * @param tumourIDSourceTableVariableName the tumourIDSourceTableVariableName to set
     */
    public void setTumourIDSourceTableVariableName(String tumourIDSourceTableVariableName) {
        this.tumourIDSourceTableVariableName = tumourIDSourceTableVariableName;
    }

    /**
     * @return the reportFileName
     */
    public String getReportFileName() {
        return reportFileName;
    }

    /**
     * @param reportFileName the reportFileName to set
     */
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    /**
     * @return the firstNameVariableName
     */
    public String getFirstNameVariableName() {
        return firstNameVariableName;
    }

    /**
     * @param firstNameVariableName the firstNameVariableName to set
     */
    public void setFirstNameVariableName(String firstNameVariableName) {
        this.firstNameVariableName = firstNameVariableName;
    }

    /**
     * @return the sexVariableName
     */
    public String getSexVariableName() {
        return sexVariableName;
    }

    /**
     * @param sexVariableName the sexVariableName to set
     */
    public void setSexVariableName(String sexVariableName) {
        this.sexVariableName = sexVariableName;
    }

    /**
     * @return the tumourCheckStatus
     */
    public String getTumourCheckStatus() {
        return tumourCheckStatus;
    }

    /**
     * @param tumourCheckStatus the tumourCheckStatus to set
     */
    public void setTumourCheckStatus(String tumourCheckStatus) {
        this.tumourCheckStatus = tumourCheckStatus;
    }

    /**
     * @return the tumourRecordStatus
     */
    public String getTumourRecordStatus() {
        return tumourRecordStatus;
    }

    /**
     * @param tumourRecordStatus the tumourRecordStatus to set
     */
    public void setTumourRecordStatus(String tumourRecordStatus) {
        this.tumourRecordStatus = tumourRecordStatus;
    }

    /**
     * @return the ICD10VariableName
     */
    public String getICD10VariableName() {
        return ICD10VariableName;
    }

    /**
     * @param ICD10VariableName the ICD10VariableName to set
     */
    public void setICD10VariableName(String ICD10VariableName) {
        this.ICD10VariableName = ICD10VariableName;
    }
}
