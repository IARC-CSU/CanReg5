package canreg.exceptions;

/**
 *
 * @author morten
 */
public class  WrongCanRegVersionException extends Exception {
    
    private String message=null;
    
    public WrongCanRegVersionException(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return message;
    }
}
