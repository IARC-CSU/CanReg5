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

import canreg.common.Globals.StandardVariableNames;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 *
 * @author ErvikM
 */
public class FixedWidthFileWriter implements FileWriterInterface {

    private BufferedWriter outFile;
    // private String outFileName;
    private final int lineLength;
    private char blanc = ' ';
    // private String[] order;
    private Pattern pattern = Pattern.compile("([^\\[\\]]*)\\[?([0-9]*),?([0-9]*)\\]?");
    private LinkedList<FileElement> fileElementList = new LinkedList<FileElement>();
    private boolean isPopulationFile;

    public FixedWidthFileWriter(int lineLength) throws FileNotFoundException, IOException {
        this(null, lineLength, false);
    }

    public FixedWidthFileWriter(int lineLength, boolean isPopulationFile) throws FileNotFoundException, IOException {
        this(null, lineLength, isPopulationFile);
    }

    public FixedWidthFileWriter(String definitionFileName, int lineLength, boolean isPopulationFile) throws FileNotFoundException, IOException {
        this.isPopulationFile = isPopulationFile;
        this.lineLength = lineLength;

        // load definitions
        BufferedReader bfr;

        if (definitionFileName == null) {
            bfr = new BufferedReader(new InputStreamReader(getClass()
                    .getResourceAsStream("/canreg/common/ruby/export_format_naaccr1946.ver11_3.d02032011.tsv")));
        } else {
            bfr = new BufferedReader(new FileReader(definitionFileName));
        }

        bfr.readLine(); // skip the first line
        String line = bfr.readLine();

        // name	case_col	pop_col	length	required	std_name
        while (line != null && line.trim().length() != 0) {
            String[] elems = line.split("\t");
            FileElement fe = new FileElement();

            fe.name = elems[0];
            if (!elems[1].isEmpty()) {
                fe.caseCol = Integer.parseInt(elems[1]) - 1; // we 0-reference our columns
            }
            if (!elems[2].isEmpty()) {
                fe.popCol = Integer.parseInt(elems[2]) - 1; // we 0-reference our columns
            }
            fe.length = Integer.parseInt(elems[3]);
            fe.isRequired = elems[4].equalsIgnoreCase("true");
            System.out.println(line);
            // set up variable
            Matcher matcher = pattern.matcher(elems[5]);
            if (matcher.find()) {
                try {
                    // first we try to map it to a standard variable
                    fe.variable = StandardVariableNames.valueOf(matcher.group(1));

                    if (!matcher.group(2).isEmpty()) {
                        fe.substring_start = Integer.parseInt(matcher.group(2));
                    }
                    if (!matcher.group(3).isEmpty()) {
                        fe.substring_length = Integer.parseInt(matcher.group(3));
                    }
                } catch (IllegalArgumentException iae) {
                    // then just a code
                    // fe.variable = matcher.group(1);
                }
            }
            fileElementList.add(fe);
            // System.out.println(fe);
            line = bfr.readLine();
        }
    }

    @Override
    public boolean setOutputFileName(String fileName) {
        try {
            // outFileName = fileName;
            outFile = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException ex) {
            Logger.getLogger(FixedWidthFileWriter.class.getName()).log(Level.WARNING, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean writeLine(Map lineElements) {
        StringBuilder line = getEmptyLine(lineLength);
        // fill the line with data
        DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(false);
        
        for (FileElement fe : fileElementList) {
            if (fe.variable != null && !fe.variable.equals("na")) {
                String value = "";
                Object v = lineElements.get(fe.variable);
                if (v != null) {
                    if (v instanceof Integer) {
                        Integer num = (Integer) v;
                        format.setMinimumIntegerDigits(fe.length);
                        value = format.format(num).toString();
                    } else {
                        value = v.toString();
                    }

                    if (fe.substring_start >= 0 || fe.substring_length >= 0) {
                        if (value.length() > fe.substring_start) {
                            value = value.substring(fe.substring_start, Math.min(fe.substring_start + fe.substring_length, value.length()));
                        } else {
                            value = getEmptyLine(fe.length).toString();
                        }
                    } else if (value.length() > fe.length) {
                        value = value.substring(0, fe.length);
                    }

                    if (isPopulationFile) {
                        if (fe.popCol >= 0) {
                            line.replace(fe.popCol, fe.popCol + (Math.min(fe.length, value.length())), value);
                        }
                    } else {
                        if (fe.caseCol >= 0) {
                            line.replace(fe.caseCol, fe.caseCol + (Math.min(fe.length, value.length())), value);
                        }
                    }
                }
            }
        }
        try {
            outFile.write(line.toString());
            outFile.newLine();
        } catch (IOException ex) {
            Logger.getLogger(FixedWidthFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public void defineOrder(String[] order) {
        // Not aplicable 
        // this.order = order;
    }

    private StringBuilder getEmptyLine(int length) {
        String blancLine = new String(new char[length]).replace('\0', blanc);
        StringBuilder line = new StringBuilder(blancLine);
        return line;
    }

    void close() throws IOException {
        outFile.flush();
        outFile.close();
    }

    private class FileElement {

        String name;
        Object variable; //either standard variable or string
        int substring_start = -1;
        int substring_length = -1;
        int caseCol = -1;
        int popCol = -1;
        int length;
        boolean isRequired;

        @Override
        public String toString() {
            return name + " (" + variable + "), start: " + substring_start + ", length: " + substring_length;
        }
    }
}
