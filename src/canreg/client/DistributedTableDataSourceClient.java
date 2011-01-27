package canreg.client;

import cachingtableapi.DistributedTableDataSource;
import cachingtableapi.DistributedTableDescription;
import cachingtableapi.DistributedTableDescriptionException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class DistributedTableDataSourceClient implements DistributedTableDataSource {
    DistributedTableDescription distributedTableDescription;
    String resultSetID;

    	/**
	 * Constructor for DemoTableDataSource.
         * 
         * @param distributedTableDescription
         * @throws Exception 
         */
	public DistributedTableDataSourceClient(DistributedTableDescription distributedTableDescription) throws Exception {
		super();
                this.distributedTableDescription = distributedTableDescription;
	}
    
    
    @Override
    public DistributedTableDescription getTableDescription() throws DistributedTableDescriptionException {
        return distributedTableDescription;
    }

    /**
     * 
     * @param distributedTableDescription
     */
    public void setTableDescription(DistributedTableDescription distributedTableDescription) {
        this.distributedTableDescription = distributedTableDescription;
    }
    
    @Override
    public Object[][] retrieveRows(int from, int to) throws DistributedTableDescriptionException {
        Object[][] rows;
        try {
            rows = CanRegClientApp.getApplication().retrieveRows(distributedTableDescription.getResultSetID(), from, to);
        } catch (RemoteException ex) {
            Logger.getLogger(DistributedTableDataSourceClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new DistributedTableDescriptionException(ex.getMessage());
        }
        return rows;
    }

    @Override
    public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws DistributedTableDescriptionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns) throws DistributedTableDescriptionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getSelectedRows() throws DistributedTableDescriptionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getSelectedColumns() throws DistributedTableDescriptionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
