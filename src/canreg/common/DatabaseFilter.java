/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DatabaseFilter implements Serializable {
    private String filterString;

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filter) {
        this.filterString = filter;
    }
}
