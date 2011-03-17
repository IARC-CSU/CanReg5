package canreg.common.cachingtableapi;
import java.io.Serializable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Specialized TableModel that retrieves its data only when it
 * is required. It retrieves the data in blocks of <i>n</i> rows and
 * holds a maximum of <i>m</i> rows before overwriting previous rows.
 * These parameters can be defined in the constructor.
 * This class retrieves its data from an object that implements the 
 * interface <code>DistributedTableDataSource</code> that is also 
 * defined in the constructor.
 * @author Jeremy Dickson, 2003.
 */
public class DistributedTableModel extends AbstractTableModel implements TableModel, Serializable {

	//Used to retrieve table data
	private DistributedTableDataSource tableDataSource;

	//The cache of data that has been retrieved.
	private DistributedTableClientCache tableClientCache;

	//Contains the descriptive elements of the table
	private DistributedTableDescription tableDescription;

	/**
	 * Constructor for CachingTableModel.
         * @param tableDataSource The object from which data should be retrieved.
         * @throws Exception 
	 */
	public DistributedTableModel(DistributedTableDataSource tableDataSource) throws Exception {
		this(tableDataSource, 200, 1000);//will set the two ints to their defaults in the constructor
	}

	/**
	 * Constructor for CachingTableModel.
	 * @param tableDataSource The object from which data should be retrieved.
	 * @param chunkSize The number of rows that should be retrieved from the DistributedTableDataSource at one time
         * @param maximumCacheSize The number of rows that the DistributedTableModel should hold before overwriting data that's not required.
         * @throws Exception 
	 */
	public DistributedTableModel(DistributedTableDataSource tableDataSource, int chunkSize, int maximumCacheSize) throws Exception {
		this.tableDataSource = tableDataSource;
		this.tableDescription = tableDataSource.getTableDescription();
		this.tableClientCache = new DistributedTableClientCache(chunkSize, maximumCacheSize, tableDataSource);
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return tableDescription.getRowCount();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return tableDescription.getColumnCount();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
    @Override
	public String getColumnName(int columnIndex) {
		if(columnIndex < tableDescription.getColumnCount()) {
			return tableDescription.getColumnNames()[columnIndex];
		}
		else {
			return null;
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
    @Override
	public Class getColumnClass(int columnIndex) {
		if(columnIndex < tableDescription.getColumnCount()) {
			return tableDescription.getColumnClasses()[columnIndex];
		}
		else {
			return null;
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        if (tableClientCache!=null){
            Object[] row = tableClientCache.retrieveRowFromCache(rowIndex);
            if (row!=null && row.length>columnIndex){
                value = row[columnIndex];
            }
        }
		return value;
	}


	/**
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
    @Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */	
    @Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {	}
	
	
	/**
	 * @see javax.swing.table.TableModel#addTableModelListener(TableModelListener)
	 */	
    @Override
	public void addTableModelListener(TableModelListener l) {	}
	
	
	/**
	 * @see javax.swing.table.TableModel#removeTableModelListener(TableModelListener)
	 */	
    @Override
	public void removeTableModelListener(TableModelListener l) {	}

	/**
	 * Initiates a sort by calling <code>sort</code> on the DistributedTableDataSource.
	 * @param sortColumn The column to sort on.
	 * @param ascending Whether the table should be sorted in an ascending or descending order.
	 * @param selectedRows The row indexes that are currently seleted in the table.
         * @return An array of the indexes of the selected rows in the table after the sort.
         * @throws Exception 
	 */	
	public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception {
		tableClientCache.sortOccurred();
		return tableDataSource.sort(sortColumn, ascending, selectedRows);	
		
	}

	/**
	 * Sets the rows and columns that are selected by calling <code>setSelectedRowsAndColumns</code>
	 * on the DistributedTableDataSource.
	 * @param selectedRows An array of the selected row indexes.
         * @param selectedColumns An array of the selected column indexes.
         * @throws Exception 
	 */
	public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns) throws Exception {
		tableDataSource.setSelectedRowsAndColumns(selectedRows, selectedColumns);
	}
	
	/**
	 * Returns an array corresponding to the row indexes that are currently 
	 * selected.
         * 
         * @return
         * @throws Exception 
         */
	public int[] getSelectedRows() throws Exception {
		return tableDataSource.getSelectedRows();
	}

	
	/**
	 * Returns an array corresponding to the column indexes that are currently 
	 * selected.
         * 
         * @return
         * @throws Exception 
         */
	public int[] getSelectedColumns() throws Exception {
		return tableDataSource.getSelectedColumns();
	}

}
