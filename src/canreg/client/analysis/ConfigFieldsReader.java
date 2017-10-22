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

import java.io.FileReader;
import java.util.LinkedList;

public class ConfigFieldsReader extends DescriptionReader {

    public static LinkedList<ConfigFields> readFile(FileReader file) {
        String lastWord = readWord(file);
        String word = null;
        LinkedList<ConfigFields> li = new LinkedList<ConfigFields>();
        if (!lastWord.equals("EOF")) {
            word = readWord(file);
        } while (!word.equals("EOF")) {
            if (word.equals("{")) {
                li.add(readConfig(lastWord, file));
            }
            lastWord = word;
            word = readWord(file);
        }
        return li;
    }

    private static ConfigFields readConfig(String fieldName,
                                   FileReader file) {
        ConfigFields fieldDesc = new ConfigFields(
                fieldName);
        String word;
        boolean end = false;
        while (!end) {
            word = readWord(file);
            if (word.equals("{")) {
                // Do nothing
            } else if (word.equals("}")) {
                end = true;
            } else if (word.equals("")) {
                // Do nothing
            } else {
                String trans = word;
                fieldDesc.addValue(trans);
            }
        }
        return fieldDesc;
    }

    public static String[] findConfig(String name, LinkedList<ConfigFields> list) {
        ConfigFields cf = null;
        String[] sa = null;
        boolean found = false;
        int m = 0;
        while (!found && m < list.size()) {
            cf = list.get(m++);
            found = cf.getFieldName().equals(name);
        }
        if (found) {
            Object[] oa = cf.getListOfValues().toArray();
            sa = new String[oa.length];
            for (int n = 0; n < oa.length; n++) {
                sa[n] = cf.getListOfValues().get(n);
            }
        }
        return sa;
    }
}
