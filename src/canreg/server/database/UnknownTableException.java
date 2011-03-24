package canreg.server.database;

/**
 *
 * @author ervikm
 */
public class UnknownTableException extends Exception {

    /**
     *
     * @param string
     */
    public UnknownTableException(String string) {
        super(string);
    }

}
