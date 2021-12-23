package canreg.server.database;

import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;


/**
 *
 */
public class PoolConnection {

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
}
