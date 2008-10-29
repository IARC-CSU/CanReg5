package canreg.exceptions;

/**
 *
 * @author morten
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
