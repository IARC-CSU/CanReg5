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
public class Tumour extends DatabaseRecord implements Serializable {
    
    /**
     * Creates a new instance of Tumour
     */
     public Tumour(){
        super();
     }
     
     @Override
     public String toString(){
        return "Tumour";
     }
}
