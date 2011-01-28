package canreg.client.gui.management;

import canreg.common.DatabaseVariablesListElement;

/**
 *
 * @author ervikm
 */
final class DatabaseVariablesListException extends Exception {

    DatabaseVariablesListElement dbvle = null;
    String error = null;

    public DatabaseVariablesListException() {
    }

    public DatabaseVariablesListException(DatabaseVariablesListElement dbvle, String error) {
        setDatabaseVariable(dbvle);
        setError(error);
    }

    public void setDatabaseVariable(DatabaseVariablesListElement dbvle) {
        this.dbvle = dbvle;
    }

    public DatabaseVariablesListElement getDatabaseVariablesListElement() {
        return dbvle;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
