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

import canreg.common.Globals;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class Migrator {

    private static final Logger LOGGER = Logger.getLogger(Migrator.class.getName());
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
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.06") < 0) {
                migrateTo_5_00_06(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.17") < 0) {
                migrateTo_5_00_17(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.19") < 0) {
                migrateTo_5_00_19(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.43") < 0) {
                migrateTo_5_00_43(canRegDAO);
            }
        }
        // canRegDAO.setSystemPropery("DATABASE_VERSION", newVersion);
    }

    private void migrateTo_4_99_5(CanRegDAO db) {
        // no users in the table from before so we just drop it
        db.dropAndRebuildUsersTable();
        db.setSystemPropery("DATABASE_VERSION", "4.99.5");
        LOGGER.log(Level.INFO, "Migrated the database to version 4.99.5.");
    }

    private void migrateTo_5_00_06(CanRegDAO db) {
        try {
            db.dropAndRebuildKeys();
            db.setSystemPropery("DATABASE_VERSION", "5.00.06");
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.06.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void migrateTo_5_00_17(CanRegDAO db) {
        try {
            db.addColumnToTable("USER_ROLE", "INT", Globals.USERS_TABLE_NAME);
            db.setSystemPropery("DATABASE_VERSION", "5.00.17");
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.17.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void migrateTo_5_00_19(CanRegDAO db) {
        try {
            db.dropColumnFromTable("USER_ROLE", Globals.USERS_TABLE_NAME );
            db.addColumnToTable("USER_ROLE", "VARCHAR(255)", Globals.USERS_TABLE_NAME);
            db.setSystemPropery("DATABASE_VERSION", "5.00.19");
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.19.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    private void migrateTo_5_00_43(CanRegDAO db) {
        try {
            try {            
                db.upgrade();
                db.setSystemPropery("DATABASE_VERSION", "5.00.43");
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }            
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.43.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
