/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common.database;

import canreg.common.Globals;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author ervikm
 */
public class Tumour extends DatabaseRecord implements Serializable {

    /**
     * Creates a new instance of Tumour
     */
    Set<Source> sources;

    /**
     *
     */
    public Tumour() {
        super();
    }

    /**
     *
     * @return
     */
    public Set<Source> getSources() {
        if (sources == null) {
            sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
        }
        return sources;
    }

    /**
     *
     * @param sources
     */
    public void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return "Tumour Record";
    }

    public static String recordStatusCodeToDatabaseVariable(Globals.RecordStatusValues status) {
        if (status == Globals.RecordStatusValues.Confirmed) {
            return Globals.RECORD_STATUS_CONFIRMED_CODE;
        } else {
            return Globals.RECORD_STATUS_PENDING_CODE;
        }
    }
}