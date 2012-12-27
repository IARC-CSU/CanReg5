/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

package canreg.server.database.derby;

import canreg.common.Globals;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Logger.getLogger(Backup.class.getName()).log(Level.INFO, "Backed up database to {0}", backupdirectory);

        System.out.println();
        return backupdirectory;
    }
}
