/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.client.gui.dataentry;

/**
 *
 * @author ervikm
 */
public class ImportOptions {
    static int UPDATE = 0;
    static int OVERWRITE = 1;
    static int REJECT = 2;
    
    // maxlines = -1 to read whole file...
    private int maxLines;
    private boolean testOnly;
    private int discrepancies;
    
    private boolean doChecks;
    private boolean doPersonSearch;
    private boolean queryNewNames;

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public boolean isTestOnly() {
        return testOnly;
    }

    public void setTestOnly(boolean testOnly) {
        this.testOnly = testOnly;
    }

    public int getDiscrepancies() {
        return discrepancies;
    }

    public void setDiscrepancies(int discrepancies) {
        this.discrepancies = discrepancies;
    }

    public boolean isDoChecks() {
        return doChecks;
    }

    public void setDoChecks(boolean doChecks) {
        this.doChecks = doChecks;
    }

    public boolean isDoPersonSearch() {
        return doPersonSearch;
    }

    public void setDoPersonSearch(boolean doPersonSearch) {
        this.doPersonSearch = doPersonSearch;
    }

    public boolean isQueryNewNames() {
        return queryNewNames;
    }

    public void setQueryNewNames(boolean queryNewNames) {
        this.queryNewNames = queryNewNames;
    }
    
    
    
}