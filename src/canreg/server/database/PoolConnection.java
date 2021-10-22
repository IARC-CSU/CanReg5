package canreg.server.database;

import com.zaxxer.hikari.HikariConfig;
import java.util.Properties;


/**
 * A class containing everything to configure HikaryCP
 */
public class PoolConnection {
    
    /**
     * returns HikariConfig containing JDBC connection properties
     * which will be used by HikariDataSource object.
     */
    public static HikariConfig getHikariConfig(String dbUrl, Properties dbProperties) {
        HikariConfig hikaConfig = new HikariConfig();
        hikaConfig.setJdbcUrl(dbUrl);
        hikaConfig.setUsername(dbProperties.getProperty("user"));
        hikaConfig.setPassword(dbProperties.getProperty("password"));
        hikaConfig.setDriverClassName(dbProperties.getProperty("dataSourceClassName"));
        // Information about the pool
        //the maximum connection which can be created by or resides in the pool
        hikaConfig.setMaximumPoolSize(Integer.parseInt(dbProperties.getProperty("maximumPoolSize")));
        //how much time a user can wait to get a connection from the pool.
        //if it exceeds the time limit then a SQlException is thrown
        hikaConfig.setConnectionTimeout(Long.parseLong(dbProperties.getProperty("connectionTimeout")));
        //The maximum time a connection can sit idle in the pool.
        // If it exceeds the time limit it is removed form the pool.
        hikaConfig.setIdleTimeout(Long.parseLong(dbProperties.getProperty("idleTimeout")));
        hikaConfig.setMaxLifetime(Long.parseLong(dbProperties.getProperty("maxLifetime")));
        hikaConfig.setSchema(dbProperties.getProperty("schema"));
        return hikaConfig;
    }
}
