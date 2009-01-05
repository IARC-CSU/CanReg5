/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.io.Serializable;


/**
 *
 * @author ervikm
 */
public class Patient extends DatabaseRecord implements Serializable {
    
    /**
     * Creates a new instance of Patient
     */
     public Patient(){
        super();
     }

    @Override
     public String toString(){
        return "Patient";
     }
}
