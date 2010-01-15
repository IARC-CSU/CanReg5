/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

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

    public Tumour() {
        super();
    }

    public Set<Source> getSources() {
        if (sources == null) {
            sources = Collections.synchronizedSet(new LinkedHashSet<Source>());
        }
        return sources;
    }

    public void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return "Tumour Record";
    }
}
