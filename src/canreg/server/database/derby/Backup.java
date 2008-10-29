package canreg.server.database.derby;

import canreg.common.Globals;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author ervikm
 */
public class Backup {

    /**
     * 
     * @param conn
     * @param backupdirectory
     * @return
     * @throws java.sql.SQLException
     */
    public static String backUpDatabase(Connection conn, String backupdirectory) throws SQLException {
// Get today's date as a string:
        java.text.SimpleDateFormat todaysDate =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        backupdirectory += Globals.FILE_SEPARATOR +
                todaysDate.format((java.util.Calendar.getInstance()).getTime());

        CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
        cs.setString(1, backupdirectory);
        cs.execute();
        cs.close();
        System.out.println("Backed up database to " + backupdirectory);
        return backupdirectory;
    }
}
