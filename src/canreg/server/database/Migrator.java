/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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

package canreg.server.database;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class Migrator {

    private String newVersion;
    private CanRegDAO canRegDAO;

    /**
     *
     * @param newVersion
     * @param canRegDAO
     */
    public Migrator(String newVersion, CanRegDAO canRegDAO) {
        this.newVersion = newVersion;
        this.canRegDAO = canRegDAO;
    }

    /**
     *
     */
    public void migrate() {
        String databaseVersion = canRegDAO.getSystemPropery("DATABASE_VERSION");

        if (databaseVersion == null) {
            databaseVersion = "4.99.0";
        }

        if (!databaseVersion.equalsIgnoreCase(newVersion)) {
            if (databaseVersion.compareTo("4.99.5") < 0) {
                migrateTo_4_99_5(canRegDAO);
            }
            if (databaseVersion.compareTo("5.00.06") < 0) {
                migrateTo_5_00_06(canRegDAO);
            }
        }
        canRegDAO.setSystemPropery("DATABASE_VERSION", newVersion);
    }

    private void migrateTo_4_99_5(CanRegDAO db) {
        // no users in the table from before so we just drop it
        db.dropAndRebuildUsersTable();
        db.setSystemPropery("DATABASE_VERSION", "4.99.5");
        Logger.getLogger(Migrator.class.getName()).log(Level.INFO, "Migrated the database to version 4.99.5.");
    }

    private void migrateTo_5_00_06(CanRegDAO db) {
        try {
            db.dropAndRebuildKeys();
            db.setSystemPropery("DATABASE_VERSION", "5.00.06");
            Logger.getLogger(Migrator.class.getName()).log(Level.INFO, "Migrated the database to version 5.00.06.");
        } catch (SQLException ex) {
            Logger.getLogger(Migrator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
