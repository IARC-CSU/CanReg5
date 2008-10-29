/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.client.dataentry;

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
    
    
    
}