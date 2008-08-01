/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.client;

import cachingtableapi.DistributedTableDataSource;
import cachingtableapi.DistributedTableDescription;

/**
 *
 * @author ervikm
 */
public class DistributedTableDataSourceClient implements DistributedTableDataSource {
    DistributedTableDescription distributedTableDescription;

    	/**
	 * Constructor for DemoTableDataSource.
	 */
	public DistributedTableDataSourceClient(DistributedTableDescription distributedTableDescription) throws Exception {
		super();
                this.distributedTableDescription = distributedTableDescription;
	}
    
    
    public DistributedTableDescription getTableDescription() throws Exception {
        return distributedTableDescription;
    }

    public void setTableDescription(DistributedTableDescription distributedTableDescription) {
        this.distributedTableDescription = distributedTableDescription;
    }
    
    public Object[][] retrieveRows(int from, int to) throws Exception {
        return CanRegClientApp.getApplication().retrieveRows(from, to);
    }

    public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int[] getSelectedRows() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int[] getSelectedColumns() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
