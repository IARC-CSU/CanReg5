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

package canreg.common;

import java.util.Comparator;

/**
 *
 * @author ervikm
 */
public class DatabaseVariablesListElementPositionSorter implements Comparator<DatabaseVariablesListElement> {

    @Override
    public int compare(DatabaseVariablesListElement o1, DatabaseVariablesListElement o2) {
        if (o1.getYPos() > o2.getYPos()) {
            return 1;
        } else if (o1.getYPos() < o2.getYPos()) {
            return -1;
        } else {
            if (o1.getXPos() > o2.getXPos()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
