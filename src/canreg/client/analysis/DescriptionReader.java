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

package canreg.client.analysis;

import java.io.FileReader;

public class DescriptionReader {

    public static int extractInteger(String word) {
        int n = 0;
        String temp = new String();
        // skip what's before
        char[] ca = word.toCharArray();
        while ((n < word.length()) && (ca[n] < '0' || ca[n] > '9')) {
            n++;
        }
        // extract the integer
        while ((n < word.length()) && (ca[n] >= '0' && ca[n] <= '9')) {
            temp = temp + ca[n];
            n++;
        }
        //System.out.println("Extracting: " + temp);
        return Integer.parseInt(temp);
    }

    public static String readLine(FileReader file) {
        String str = new String();
        int c;
        try {
            c = file.read();
            while (c != -1 && c != '\n') {
                str = str + c;
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while reading a word.");
        }
        return str;
    }

    public static String readWord(FileReader file) {
        String str = new String();
        int c = removeWhites(file);
        try {
            while (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                if (c == '\"') {
                    //System.out.println("Reading quotes");
                    c = file.read();
                    while (c != '\"') {
                        str = str + (char) c;
                        c = file.read();
                        // If we find an EOF-mark we terminate and return EOF - that means we need a enter after the last line of data
                        if (c == -1) {
                            str = "EOF";
                            break;
                        }
                    }
                } else {
                    str = str + (char) c;
                }
                // If we find an EOF-mark we terminate and return EOF - that means we need a enter after the last line of data
                if (c == -1) {
                    str = "EOF";
                    break;
                }
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while reading a word.");
        }
        //System.out.println(str);
        return str;
    }

    public static int removeWhites(FileReader file) {
        int c = -1;
        try {
            c = file.read();
            while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while removing whites.");
        }
        return c;
    }
}
