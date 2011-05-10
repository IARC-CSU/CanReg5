/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common.database;

import canreg.common.database.DatabaseRecord;
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
        return "Patient Record";
     }
}
