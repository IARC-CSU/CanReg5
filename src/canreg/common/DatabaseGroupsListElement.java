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

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DatabaseGroupsListElement implements Serializable, DatabaseElement {
    private String groupName;
    private int groupIndex;
    private int groupPosition;

    public DatabaseGroupsListElement(String groupName, int index, int position) {
        this.groupName = groupName;
        this.groupIndex = index;
        this.groupPosition = position;
    }

    /**
     * 
     * @return
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 
     * @param groupName
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * 
     * @return
     */
    public int getGroupIndex() {
        return groupIndex;
    }

    /**
     * 
     * @param groupIndex
     */
    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    /**
     * 
     * @return
     */
    public int getGroupPosition() {
        return groupPosition;
    }

    /**
     * 
     * @param groupPosition
     */
    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    @Override
    public String toString(){
        return groupName;
    }

    @Override
    public String getDescriptiveString() {
        return groupName;
    }

    @Override
    public boolean userVariable() {
        return true;
    }
}
