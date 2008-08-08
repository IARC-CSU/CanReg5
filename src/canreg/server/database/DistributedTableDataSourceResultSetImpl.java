/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import cachingtableapi.DistributedTableDataSource;
import cachingtableapi.DistributedTableDescription;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class DistributedTableDataSourceResultSetImpl implements DistributedTableDataSource, Serializable {

    DistributedTableDescription distributedTableDescription;
    ResultSet resultSet;
    int columnCount;
    int rowCount;
    String[] columnNames;
    Class[] columnClasses;

    public DistributedTableDataSourceResultSetImpl(ResultSet resultSet) throws SQLException, Exception {
        super();
        this.resultSet = resultSet;

        // To get the number of rows - skip to the last and then get ID before skipping back...
        resultSet.last();                 // Jump to last row
        rowCount = resultSet.getRow();    // get the row count
        resultSet.beforeFirst();          // reset to allow forward cursor processing

        // rowCount = 1000;

        ResultSetMetaData metaData = resultSet.getMetaData();
        columnCount = metaData.getColumnCount();
        columnNames = getColumnNames(metaData);
        columnClasses = getColumnClasses(metaData);

        distributedTableDescription = new DistributedTableDescription(columnNames, columnClasses, rowCount);
    }

    public DistributedTableDataSourceResultSetImpl(int rowCount, ResultSet resultSet) throws SQLException, Exception {
        super();
        this.resultSet = resultSet;
        this.rowCount = rowCount;

        ResultSetMetaData metaData = resultSet.getMetaData();
        columnCount = metaData.getColumnCount();
        columnNames = getColumnNames(metaData);
        columnClasses = getColumnClasses(metaData);

        distributedTableDescription = new DistributedTableDescription(columnNames, columnClasses, rowCount);
    }

    public DistributedTableDescription getTableDescription() throws Exception {
        return distributedTableDescription;
    }

    public Object[][] retrieveRows(int from, int to) throws Exception {
        LinkedList<Object[]> rows = new LinkedList<Object[]>();
        int pos = resultSet.getRow();
        resultSet.relative(pos - from);
        boolean hasMore = resultSet.next();
        while (hasMore) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = resultSet.getObject(i + 1);
            }
            rows.add(row);
            hasMore = resultSet.next();
        }
        Object[][] rowsArray = new Object[rows.size()][columnCount];

        for (int i = 0; i < rows.size(); i++) {
            rowsArray[i] = rows.get(i);
        }

        return rowsArray;
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

    private Class[] getColumnClasses(ResultSetMetaData metaData) throws SQLException {
        int ccount = metaData.getColumnCount();
        Class[] columns = new Class[ccount];
        for (int i = 0; i < ccount; i++) {
            columns[i] = getColumnClass(metaData.getColumnType(i + 1));
        }
        return columns;
    }

    private Class getColumnClass(int type) {
        String cname;
        switch (type) {
            case Types.BIT: {
                cname = "java.lang.Boolean";
                break;
            }
            case Types.TINYINT: {
                cname = "java.lang.Byte";
                break;
            }
            case Types.SMALLINT: {
                cname = "java.lang.Short";
                break;
            }
            case Types.INTEGER: {
                cname = "java.lang.Integer";
                break;
            }
            case Types.BIGINT: {
                cname = "java.lang.Long";
                break;
            }
            case Types.FLOAT:
            case Types.REAL: {
                cname = "java.lang.Float";
                break;
            }
            case Types.DOUBLE: {
                cname = "java.lang.Double";
                break;
            }
            case Types.NUMERIC: {
                cname = "java.lang.Number";
                break;
            }
            case Types.DECIMAL: {
                cname = "java.math.BigDecimal";
                break;
            }
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR: {
                cname = "java.lang.String";
                break;
            }
            case Types.DATE: {
                cname = "java.sql.Date";
                break;
            }
            case Types.TIME: {
                cname = "java.sql.Time";
                break;
            }
            case Types.TIMESTAMP: {
                cname = "java.sql.Timestamp";
                break;
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: {
                cname = "byte[]";
                break;
            }
            case Types.OTHER:
            case Types.JAVA_OBJECT: {
                cname = "java.lang.Object";
                break;
            }
            case Types.CLOB: {
                cname = "java.sql.Clob";
                break;
            }
            case Types.BLOB: {
                cname = "java.ssql.Blob";
                break;
            }
            case Types.REF: {
                cname = "java.sql.Ref";
                break;
            }
            case Types.STRUCT: {
                cname = "java.sql.Struct";
                break;
            }
            default: {
                //return super.getColumnClass(column);
                cname = "java.lang.Object";
            }
        }
        try {
            return Class.forName(cname);
        } catch (Exception e) {
            e.printStackTrace();
            return Object.class;
        }

    }

    private String[] getColumnNames(ResultSetMetaData metaData) throws SQLException {
        int ccount = metaData.getColumnCount();
        String[] columns = new String[ccount];

        for (int i = 0; i < ccount; i++) {
            columns[i] = metaData.getColumnLabel(i + 1);
        }

        return columns;
    }
}
