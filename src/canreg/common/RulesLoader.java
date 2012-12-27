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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class RulesLoader {
    /**
     * 
     * @param resourceAsStream
     * @param topographyRule9CodeLength
     * @return
     * @throws java.io.IOException
     */
    public static Map<Integer, String> load(InputStream resourceAsStream, int topographyRule9CodeLength) throws IOException {
                Map<Integer, String> table = new LinkedHashMap <Integer, String>();
                
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
        
        String line = br.readLine();
        // int codeLength = lookUpFileDescription.getCodeLength();
        int i = 0;
        while(line!=null){
            table.put(i, line);
            line = br.readLine();
            i++;
        }
        return table;
    }

    public static LinkedList<String []> loadTable(InputStream resourceAsStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = br.readLine();
        String[] lineElements;
        LinkedList<String []> lineElementsList = new LinkedList<String []>();
        int maxgroups = 0;
        while (line!=null){
            lineElements = line.split("\t");
            maxgroups = Math.max(maxgroups, lineElements.length);
            lineElementsList.add(lineElements);
            line = br.readLine();
        }
        return lineElementsList;
    }
}
