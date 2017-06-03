/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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
 * @author Patricio Ezequiel Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.dataentry2;

import canreg.common.GlobalToolBox;
import canreg.common.database.DatabaseRecord;
import java.util.Map;

/**
 * This interface was created only to fabricate polymorphism between the different
 * DataEntry versions in order to switch between them according to options
 * configuration.
 * @author patri_000
 */
public interface RecordEditor {
    
    void setGlobalToolBox(GlobalToolBox globalToolBox);
    void setDictionary(Map<Integer, canreg.common.database.Dictionary> dictionary);
    void addRecord(DatabaseRecord dbr);
    
}
