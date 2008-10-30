package canreg.exceptions;

/**
 *
 * @author ervikm
 */
public class  SystemUnavailableException extends Exception {
    
    String message=null;
    
    /**
     * 
     * @param message
     */
    public SystemUnavailableException(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return message;
    }
}
