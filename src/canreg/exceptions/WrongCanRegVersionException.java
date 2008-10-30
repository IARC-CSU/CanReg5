package canreg.exceptions;

/**
 *
 * @author ervikm
 */
public class  WrongCanRegVersionException extends Exception {
    
    private String message=null;
    
    /**
     * 
     * @param message
     */
    public WrongCanRegVersionException(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return message;
    }
}
