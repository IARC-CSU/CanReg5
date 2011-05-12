/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
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