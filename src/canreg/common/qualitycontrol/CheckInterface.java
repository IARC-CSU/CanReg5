/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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

package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public abstract class CheckInterface {
    protected Map<StandardVariableNames, DatabaseVariablesListElement> variableListElementMap;

    /**
     * 
     * @return the variables needed to perform this check.
     */
    public abstract Globals.StandardVariableNames[] getVariablesNeeded();

    /**
     *  This is just a mapping from the standard variable names to the names used in the CanReg database.
     */
    public void setVariableListElementsMap(Map<Globals.StandardVariableNames, DatabaseVariablesListElement> variableListElementMap) {
        this.variableListElementMap = variableListElementMap;
    }

    /**
     * 
     * @return a standard name of this check.
     */
    public abstract Checker.CheckNames getCheckName();
    /**
     * 
     * @param variables
     * @return
     */
    public abstract CheckResult performCheck(Map<Globals.StandardVariableNames, Object> variables);
}
