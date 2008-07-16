/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.exceptions;

/**
 *
 * @author morten
 */
public class  WrongCanRegVersionException extends Exception {
    
    String message=null;
    
    public WrongCanRegVersionException(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return message;
    }
}
