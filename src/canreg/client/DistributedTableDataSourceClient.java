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

package canreg.client;

import canreg.common.cachingtableapi.DistributedTableDataSource;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistributedTableDataSourceClient implements DistributedTableDataSource {
    DistributedTableDescription distributedTableDescription;
    String resultSetID;

    	/**
	 * Constructor for DemoTableDataSource.
         * 
         * @param distributedTableDescription
         * @throws canreg.common.cachingtableapi.DistributedTableDescriptionException
         */
	public DistributedTableDataSourceClient(DistributedTableDescription distributedTableDescription) throws DistributedTableDescriptionException {
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
