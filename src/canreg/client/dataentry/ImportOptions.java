/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    public static int UPDATE = 0;
    /**
     * 
     */
    public static int OVERWRITE = 1;
    /**
     * 
     */
    public static int REJECT = 2;
    
    // maxlines = -1 to read whole file...
    private int maxLines;
    private boolean testOnly;
    private int discrepancies;
    private String separator;
    
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

    private String obsoleteTumourFlagVariableName;
    private String obsoletePatientFlagVariableName;

    private Charset fileCharset;

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
    public String getSeparator() {
        return separator;
    }

    /**
     * 
     * @param separator
     */
    public void setSeparator(String separator) {
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
}