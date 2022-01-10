package canreg.server.database;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Connection pool.
 */
public class PoolConnection {

    private static final Logger LOGGER = Logger.getLogger(PoolConnection.class.getName());
    
    public static DataSource DbDatasource(String dbUrl, Properties dbProperties) {

        BasicDataSource basicDS = new BasicDataSource();
        basicDS.setDriverClassName(dbProperties.getProperty("dataSourceClassName"));
        basicDS.setUrl(dbUrl);
        basicDS.setUsername(dbProperties.getProperty("user"));
        basicDS.setPassword(dbProperties.getProperty("password"));
        basicDS.setInitialSize(Integer.parseInt(dbProperties.getProperty("initialPoolSize")));
        basicDS.setMaxTotal(Integer.parseInt(dbProperties.getProperty("maximumPoolSize")));
        basicDS.setMaxIdle(Integer.parseInt(dbProperties.getProperty("maxIdleConnection")));
        basicDS.setMaxConnLifetimeMillis(Long.parseLong(dbProperties.getProperty("maxLifetime")));
        basicDS.setDefaultSchema(dbProperties.getProperty("schema"));
        basicDS.setMaxWaitMillis(Long.parseLong(dbProperties.getProperty("connectionTimeout")));
        return basicDS;
    }

    /**
     * Close the pool
     * @param dataSource datasource
     * @return number of active connections, should be 0 if the pool is fully closed ; -1 if an error happened
     */
    public static int closePool(DataSource dataSource) {
        LOGGER.log(Level.INFO, "CLosing the Connection pool...");
        if(dataSource instanceof BasicDataSource) {
            BasicDataSource basicDataSource = (BasicDataSource) dataSource;
            try {
                // close
                basicDataSource.close();
                
                // Wait maximum 50x100ms
                int nb = 50;
                while(basicDataSource.getNumActive() > 0 && nb > 0) {
                    Thread.sleep(100);
                    nb--;
                }
                if(basicDataSource.getNumActive() == 0) {
                    LOGGER.log(Level.INFO, "Connection pool is fully closed");
                } else {
                    LOGGER.log(Level.INFO, "Connection pool is closed but " + basicDataSource.getNumActive() 
                            + " connections are still active");
                }
                return basicDataSource.getNumActive();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "SQLException in closePool: " + e.getMessage(), e);
                throw new RuntimeException("SQLException in closePool: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "InterruptedException in closePool: " + e.getMessage());
                // Restore interrupted state... 
                Thread.currentThread().interrupt();
            }
        } else {
            LOGGER.log(Level.SEVERE, "Error in closePool: dataSource is null or not a BasicDataSource");
        }
        // Something wrong happened
        return -1;
    }
}
