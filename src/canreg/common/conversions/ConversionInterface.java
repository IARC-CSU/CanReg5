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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.common.conversions;

import canreg.common.Globals;
import canreg.common.conversions.Converter.ConversionName;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public interface ConversionInterface {

    /**
     * 
     * @return
     */
    public ConversionName getConversionName();
    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded();
    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesCreated();
    /**
     * 
     * @param variables
     * @return
     */
    public ConversionResult[] performConversion(Map<Globals.StandardVariableNames, Object> variables);
}
