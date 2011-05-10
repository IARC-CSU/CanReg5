package canreg.common.database;

import canreg.common.database.DatabaseRecord;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class Source extends DatabaseRecord implements Serializable {

    /**
     * Creates a new instance of Source
     */

     public Source(){
        super();
     }

     @Override
     public String toString(){
        return "Source Record";
     }
}
