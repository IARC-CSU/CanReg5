package cachingtableapi;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class that is responsible for retrieving the data for the table from
 * the server and storing it locally. 
 * @author Jeremy Dickson, 2003.
 */
public class DistributedTableClientCache implements Serializable {
    //THE MAXIMUM SIZE OF THE CACHE
    private int maximumCacheSize = -1;    
    //THE NUMBER OF ROWS THAT ARE RETRIEVED AT A TIME
    private int chunkSize = -1;    
    //THE CACHE OF ROWS
    private Object[] data = null;    
    //AN INDEX- AN INTS ARE STORED CORREPONDING TO A ROWS REAL INDEX IN THE TABLE. THE LOCATION OF THE INDEX IN THIS
    //ARRAY SHOWS WHICH LOCATION TO ACCESS IN THE data ARRAY
    // private int[] rowIndexLookup = null;
    private HashMap <Integer, Integer> rowIndexMap = null;    
    //STORES THE INDEX THAT THE NEXT WRITES TO THE TWO ARRAYS SHOULD TAKE PLACE IN. WHEN IT REACHES
    //THE MAX CACHE SIZE IT GOES BACK TO ZERO
    private int writePositionIndex = 0;    
    //THE SOURCE OF DATA
    private DistributedTableDataSource tableDataSource = null;    
    //THE INDEX IN THE TABLE TO FETCH DATA FROM, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
    private int toIndex = -1;    
    //THE INDEX IN THE TABLE TO FETCH DATA TO, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
    private int fromIndex = -1;    
    //THE LAST INDEX THAT WAS REQUIRED WHEN A FETCH OCCURRED. DETERMINES WHETHER THE USER IS ASCENDING
    //OR DESCENDING THE TABLE
    private int lastRequiredFetchRowIndex = 0;    
    //CONVENIENCE VARIABLE, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
    private int tableIndex = -1;    
    //THE LAST ARRAY INDEX OF THE CACHE TO BE INDEXED
    private int lastRowAccess = 0;    
    //CONVENIENCE
    private int i = 0;
    
    private DistributedTableDescription tableDescription;

    /** Creates new DistributedTableClientCache 
     *@param chunkSize The number of rows of data that are to be 
     * retrieved from the remote store at a time.
     *@param maximumCacheSize The maximum number of rows that will be cached. When this number is exceeded
     *by new data that has been fetched, the oldest data is overwritten.
     *@tableDataSource A source of table data, (via the method <code>retrieveRows</code>).
     */
    public DistributedTableClientCache(int chunkSize, int maximumCacheSize, DistributedTableDataSource tableDataSource) throws Exception {
        this.tableDataSource = tableDataSource;
        this.tableDescription = tableDataSource.getTableDescription();

        //ENSURE CHUNK SIZE NOT TOO SMALL
        if (chunkSize < 50) {
            chunkSize = 50;
        }
        this.chunkSize = chunkSize;

        //ENSURE MAX CACHE SIZE NOT TOO SMALL
        if (maximumCacheSize < 300) {
            maximumCacheSize = 300;
        }
        this.maximumCacheSize = maximumCacheSize;

        //MAKE SURE THE CHUNK SIZE NOT BIGGER THAN THE MAX CACHE SIZE
        if (chunkSize > maximumCacheSize) {
            chunkSize = maximumCacheSize;
        }

        //INITIALIZE THE ARRAYS
        data = new Object[maximumCacheSize];
        //rowIndexLookup = new int[maximumCacheSize];
        rowIndexMap = new HashMap<Integer, Integer>(maximumCacheSize);

        //SET ALL THE ROWS TO -1, (THEY INITIALIZE TO 0).
        for (int i = 0; i < maximumCacheSize; i++) {
            rowIndexMap.put(-1,i);
        }
    }

    /**
     *Retrieves a row from the data cache. If the row is not currently in
     * the cache it will be retrieved from the DistributedTableDataSource
     * object.
     *@param rowIndex The row index in the table that is to be retrieved.
     */
    public Object[] retrieveRowFromCache(int rowIndex) {
        ensureRowCached(rowIndex);
        return (Object[]) data[getIndexOfRowInCache(rowIndex)];
    }

    /**
     *Ensures that a row index in the table is cached and if not a chunk of data is retrieved.
     */
    private void ensureRowCached(int rowIndex) {
        if (!isRowCached(rowIndex)) {
            //HAVE TO FETCH DATA FROM THE REMOTE STORE

            //SET THE toIndex AND fromIndex VARIABLES

            //TEST IF THE USER IS DESCENDING THE TABLE
            if (rowIndex >= lastRequiredFetchRowIndex) {
                fromIndex = rowIndex;
                toIndex = rowIndex + chunkSize;

                try {
                    if (toIndex > tableDescription.getRowCount()) {
                        toIndex = tableDescription.getRowCount();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } //USER IS ASCENDING THE TABLE
            else {
                fromIndex = rowIndex - chunkSize;
                if (fromIndex < 0) {
                    fromIndex = 0;
                }
                toIndex = rowIndex + 1;
            }

            Object[][] rows = null;
            //RETRIEVE THE DATA
            try {
                rows = tableDataSource.retrieveRows(fromIndex, toIndex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("Problem occurred retrieving table data \n");
            }

            //ADD THE DATA TO THE CACHE
            for (int i = 0; i < rows.length; i++) {
                //SET THE VALUE IN THE DATA ARRAY
                data[writePositionIndex] = rows[i];

                //CREATE AN INDEX TO THE NEW CACHED DATA
                tableIndex = fromIndex + i;
                rowIndexMap.put(tableIndex,writePositionIndex);

                //CLOCK UP writePositionIndex AND REZERO IF NECESSARY
                if (writePositionIndex == (maximumCacheSize - 1)) {
                    writePositionIndex = 0;
                } else {
                    writePositionIndex++;
                }
                lastRequiredFetchRowIndex = rowIndex;
            }
        }
    }

    /**
     *Returns whether a particular row index in the table is cached.
     */
    private boolean isRowCached(int rowIndexInTable) {
        return getIndexOfRowInCache(rowIndexInTable) >= 0;
    }

    /**
     *Returns the array index of a particular row index in the table
     */
    private int getIndexOfRowInCache(int rowIndex) {
        Integer index = rowIndexMap.get(rowIndex);
        if (index != null) {
            lastRowAccess = index;
            return index;
        } else {
            return -1;
        }
    }

    /**
     * Called after a sort has been carried out to nullify the data
     * in the cache so that the newly sorted data must be fetched from
     * the server.
     */
    public void sortOccurred() {
        //SET ALL THE ROWS TO -1, (THEY INITIALIZE TO 0).
        for (int i = 0; i < maximumCacheSize; i++) {
            rowIndexMap.put(-1,i);
        }
    }
}
