/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

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
    public Migrator(String newVersion, CanRegDAO canRegDAO){
        this.newVersion = newVersion;
        this.canRegDAO=canRegDAO;
    }

    /**
     *
     */
    public void migrate() {
        String databaseVersion = canRegDAO.getSystemPropery("DATABASE_VERSION");

        if (databaseVersion==null) databaseVersion = "4.99.0";

        if (!databaseVersion.equalsIgnoreCase(newVersion)) {
            if (databaseVersion.compareTo("4.99.5") < 0) {
                migrateTo_4_99_5(canRegDAO);
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
}
