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


package canreg.client.analysis;

import java.io.FileReader;
import java.util.LinkedList;

class FieldDescriptionReader extends DescriptionReader {

    public static <FieldDescription> LinkedList readFile(FileReader file) {
        String word = readWord(file);
        LinkedList<FieldDescription> li = new LinkedList();
        while (!word.equals("EOF")) {
            if (word.equals("dictionary")) {
                li = (LinkedList<FieldDescription>) readDictionary(file);
                break;
            }
            word = readWord(file);
        }
        return li;
    }

    public static LinkedList<FieldDescription> readDictionary(FileReader file) {
        int offsetCounter = 1;
        LinkedList<FieldDescription> list = new LinkedList();
        String word;
        boolean end = false;
        while (!end) {
            word = readWord(file);
            if (word.equals("{")) {
                // Do nothing
            } else if (word.length() > 7 &&
                       word.subSequence(0, 7).equals("_column")) {
                offsetCounter = extractInteger(word);
            } else if (word.equals("}")) {
                end = true;
            } else if (word.equals("")) {
                // Do nothing
            } else {
                FieldDescription field = new FieldDescription(readWord(file));
                field.setContentType(word);
                field.setCharacters(extractInteger(readWord(file)));
                field.setDescription(readWord(file));
                field.setOffset(offsetCounter);
                offsetCounter += field.getCharacters();
                list.add(field);
            }
        }
        return list;
    }
}
