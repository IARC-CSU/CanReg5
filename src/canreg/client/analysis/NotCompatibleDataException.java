/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.analysis;

/**
 *
 * @author ervikm
 */
public class NotCompatibleDataException extends Exception {

    private String message;

    public void setMessage(String message) {
        this.message = message;
    }
}
