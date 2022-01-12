package canreg.server.database;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Connection wrapper to handle a unique connection (embedded driver) 
 * that must not be closed at the end of a try with resources.<br>
 * Use trulyClose to force the connection close.
 */
public class DbConnectionWrapper implements Connection {

    private Connection uniqueConnection;

    /**
     * Constructor.
     * @param uniqueConnection connection
     */
    public DbConnectionWrapper(Connection uniqueConnection) {
        this.uniqueConnection = uniqueConnection;
    }

    /**
     * Truly closes the connection.
     * @throws SQLException SQLException
     */
    public void trulyClose() throws SQLException {
        uniqueConnection.close();
    }

    /**
     * This method DOES NOT close the connection.<br>
     * This allows to use the Connection in a try with resources.
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        // do nothing
    }

    @Override
    public Statement createStatement() throws SQLException {
        return uniqueConnection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return uniqueConnection.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return uniqueConnection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return uniqueConnection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        uniqueConnection.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return uniqueConnection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        uniqueConnection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        uniqueConnection.rollback();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return uniqueConnection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return uniqueConnection.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        uniqueConnection.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return uniqueConnection.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        uniqueConnection.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return uniqueConnection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        uniqueConnection.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return uniqueConnection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return uniqueConnection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        uniqueConnection.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return uniqueConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return uniqueConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return uniqueConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return uniqueConnection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        uniqueConnection.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        uniqueConnection.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return uniqueConnection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return uniqueConnection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return uniqueConnection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        uniqueConnection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        uniqueConnection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return uniqueConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return uniqueConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return uniqueConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return uniqueConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return uniqueConnection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return uniqueConnection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return uniqueConnection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return uniqueConnection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return uniqueConnection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return uniqueConnection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return uniqueConnection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        uniqueConnection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        uniqueConnection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return uniqueConnection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return uniqueConnection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return uniqueConnection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return uniqueConnection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        uniqueConnection.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return uniqueConnection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        uniqueConnection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        uniqueConnection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return uniqueConnection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return uniqueConnection.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return uniqueConnection.isWrapperFor(iface);
    }
}
