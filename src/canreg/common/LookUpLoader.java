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

package canreg.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ervikm
 */
public class LookUpLoader {

    /**
     * 
     * @param inStream
     * @param codeLength
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static Map<String, String> load(InputStream inStream, int codeLength) throws FileNotFoundException, IOException, URISyntaxException {
        Map<String, String> table = new LinkedHashMap<String, String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        String line = br.readLine();
        while (line != null) {
            line = removeComments(line);
            table.put(line.substring(0, codeLength), line.substring(codeLength));
            line = br.readLine();
        }
        return table;
    }

    /*__________________________________________________________________
     *
     * This method enables to put off the comments when we get the string read in the files 03_10M.txt
     * If there is comment, the string cannot be converted into an integer.
     */
    private static String removeComments(String ch) {
        Pattern pattern = Pattern.compile("/");
        Matcher matcher = pattern.matcher(ch);
        if (matcher.find()) {
            ch.substring(0, matcher.start());
            return ch.substring(0, matcher.start());
        } else {
            return ch;
        }
    }
}
