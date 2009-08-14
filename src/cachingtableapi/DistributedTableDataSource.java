package cachingtableapi;

/**
 * Interface that isolates all of the remote method calls necessary to
 * retrieve data from a distributed table model in an efficient way.
 * @author Jeremy Dickson, 2003.
 **/
public interface DistributedTableDataSource {

    /**
     * Returns an object contain descriptive data for the table, (column
     * names, class types, number of rows and columns). This method will
     * only ever be called once when a table is displayed.
     * @return
     * @throws Exception If something goes wrong.
     */
    DistributedTableDescription getTableDescription() throws DistributedTableDescriptionException;

    /**
     * Method used for data retrieval.
     * @param from
     * @param to
     * @return A two dimensional array [row][column] of data from the remote
     * table data store.
     * @throws Exception If something goes wrong.
     */
    Object[][] retrieveRows(int from, int to) throws DistributedTableDescriptionException;

    /**
     * Initiates a sort by calling <code>sort</code> on the DistributedTableDataSource.
     * @param sortColumn The column to sort on.
     * @param ascending Whether the table should be sorted in an ascending or descending order.
     * @param selectedRows The row indexes that are currently seleted in the table.
     * @return An array of the indexes of the selected rows in the table after the sort.
     * @throws Exception If something goes wrong.
     */
    int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws DistributedTableDescriptionException;

    /**
     * Sets the rows and columns that are selected by calling <code>setSelectedRowsAndColumns</code>
     * on the DistributedTableDataSource.
     * @param selectedRows An array of the selected row indexes.
     * @param selectedColumns An array of the selected column indexes.
     * @throws Exception If something goes wrong.
     */
    void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns) throws DistributedTableDescriptionException;

    /**
     * Returns an array corresponding to the row indexes that are currently
     * selected.
     * @return
     * @throws Exception If something goes wrong.
     */
    int[] getSelectedRows() throws DistributedTableDescriptionException;

    /**
     * Returns an array corresponding to the column indexes that are currently
     * selected.
     * @return
     * @throws Exception If something goes wrong.
     */
    int[] getSelectedColumns() throws DistributedTableDescriptionException;
}
