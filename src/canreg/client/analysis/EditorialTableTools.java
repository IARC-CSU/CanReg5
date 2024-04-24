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

package canreg.client.analysis;

import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class EditorialTableTools {

    public static LinkedList[] generateICD10Groups(String[] config) {
        LinkedList[] tempCancerGroups = new LinkedList[config.length];
        for (int n = 0; n < config.length; n++) {
            String group = config[n];
            tempCancerGroups[n] = parseICD10Group(group);
        }
        return tempCancerGroups;
    }

    public static LinkedList parseICD10Group(String group) {
        LinkedList<Integer> cancerGroup = new LinkedList();
        boolean finished = false;
        // First the special cases of all and other&unidentified we just return the empty list
        if (group.equalsIgnoreCase("ALL") || group.equalsIgnoreCase("O&U")
                || group.equalsIgnoreCase("ALLb") || group.equalsIgnoreCase("ALLbC44")) {
            return cancerGroup;
        } else if (group.equals("MES") || group.equals("KAP") || group.equals("MPD") || group.equals("MDS")) {
            return cancerGroup;
        }

        // We can always safely skip the first letter C
        int offset = 1;
        while (!finished) {
            Integer i = Integer.parseInt(group.substring(offset, offset + 2));
            offset = offset + 2;
            i = i * 10;
            if ((group.length() > offset)
                    && group.substring(offset, offset + 1).equals(".")) {
                offset = offset + 1;
                i = i + Integer.parseInt(group.substring(offset, offset + 1));
                cancerGroup.add(i);
                offset = offset + 1;
                if ((group.length() > offset)
                        && group.substring(offset, offset + 1).equals("-")) {
                    offset = offset + 1;
                    Integer j = Integer.parseInt(group.substring(offset,
                            offset + 1));
                    j = j + ((i / 10) * 10);
                    for (int n = j; i < n; i++) {
                        cancerGroup.add(Integer.valueOf(i + 1));
                    }
                    offset = offset + 1;
                }
            } else {
                // add all 10 sub codes
                for (int n = i + 10; i < n; i++) {
                    cancerGroup.add(i);
                }
                if ((group.length() > offset)
                        && group.substring(offset, offset + 1).equals("-")) {
                    offset = offset + 1;
                    int j = Integer.parseInt(group.substring(offset,
                            offset + 2));
                    j = ((j + 1) * 10) - 1;
                    for (int n = j; i <= n; i++) {
                        cancerGroup.add(Integer.valueOf(i));
                    }
                    offset = offset + 2;
                }
            }
            if ((offset == group.length())
                    || !group.substring(offset, offset + 1).equals(",")) {
                finished = true;
            }
            offset = offset + 1;
            // added 07/03/07 Morten
            // skip C's
            if (!finished && (group.substring(offset, offset + 1).equals("C"))) {
                offset = offset + 1;
            }
        }
        return cancerGroup;
    }

    public static int getICD10index(int icdNumber, LinkedList cancerGroups[]) {
        // icdNumber = icdNumber / 10;
        int cancer = -1;
        int index = 0;
        boolean found = false;
        LinkedList group;
        while (!found && (index < (cancerGroups.length))) {
            group = cancerGroups[index];
            for (int m = 0; m < group.size(); m++) {
                int groupIcd = (Integer) group.get(m);
                if (icdNumber == groupIcd) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                index++;
            }
        }
        if (found) {
            return index;
        } else {
            return -1;
        }
    }

    public static int getICD10index(String groupName, String cancerLabels[]) {
        int index = 0;
        boolean found = false;
        // LinkedList group;
        while (!found && (index < (cancerLabels.length))) {
            if (groupName.equalsIgnoreCase(cancerLabels[index])) {
                found = true;
                break;
            }
            if (!found) {
                index++;
            }
        }
        if (found) {
            return index;
        } else {
            return -1;
        }
    }
}
