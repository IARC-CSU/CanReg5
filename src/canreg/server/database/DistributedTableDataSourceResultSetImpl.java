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

package canreg.server.database;

import canreg.common.cachingtableapi.DistributedTableDataSource;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistributedTableDataSourceResultSetImpl implements DistributedTableDataSource, Serializable {

    DistributedTableDescription distributedTableDescription;
    ResultSet resultSet;
    int columnCount;
    int rowCount;
    String[] columnNames;
    Class[] columnClasses;

    /**
     * 
     * @param resultSet
     * @throws java.sql.SQLException
     * @throws DistributedTableDescriptionException
     */
    public DistributedTableDataSourceResultSetImpl(ResultSet resultSet) throws SQLException, DistributedTableDescriptionException {
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

    /**
     * 
     * @param rowCount
     * @param resultSet
     * @throws java.sql.SQLException
     * @throws DistributedTableDescriptionException
     */
    public DistributedTableDataSourceResultSetImpl(int rowCount, ResultSet resultSet) throws SQLException, DistributedTableDescriptionException {
        super();
        this.resultSet = resultSet;
        this.rowCount = rowCount;

        ResultSetMetaData metaData = resultSet.getMetaData();
        columnCount = metaData.getColumnCount();
        columnNames = getColumnNames(metaData);
        columnClasses = getColumnClasses(metaData);

        distributedTableDescription = new DistributedTableDescription(columnNames, columnClasses, rowCount);
    }

    @Override
    public DistributedTableDescription getTableDescription() throws DistributedTableDescriptionException {
        return distributedTableDescription;
    }

    @Override
    public synchronized Object[][] retrieveRows(int from, int to) throws DistributedTableDescriptionException {
        LinkedList<Object[]> rows = new LinkedList<Object[]>();

        try {
            int pos;
            // pos = resultSet.getRow();
            resultSet.absolute(from);
            boolean hasMore = resultSet.next();

            while (hasMore && rows.size() < (to - from)) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = resultSet.getObject(i + 1);
                }
                rows.add(row);
                hasMore = resultSet.next();
                if (!hasMore) {
                    // set pointer to first so that we can keep using resultset
                    resultSet.first();
                    // Logger.getLogger(DistributedTableDataSourceResultSetImpl.class.getName()).log(Level.INFO, "last record reached");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DistributedTableDataSourceResultSetImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new DistributedTableDescriptionException(ex.getMessage());
        }
        Object[][] rowsArray = new Object[rows.size()][columnCount];

        for (int i = 0; i < rows.size(); i++) {
            rowsArray[i] = rows.get(i);
        }

        return rowsArray;
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
        } catch (ClassNotFoundException cnfe) {
            Logger.getLogger(DistributedTableDataSourceResultSetImpl.class.getName()).log(Level.SEVERE, null, cnfe);
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

    /**
     *
     * @throws SQLException
     */
    public void releaseResultSet() throws SQLException{
        resultSet.close();
    }
}
