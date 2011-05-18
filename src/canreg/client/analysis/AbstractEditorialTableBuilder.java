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

import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.PopulationDataset;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.text.NumberFormat;

public abstract class AbstractEditorialTableBuilder implements TableBuilderInterface{

    protected static double estdPop18[] = {0.08, 0.07, 0.07, 0.07, 0.07, 0.07, 0.07, 0.07, 0.07,
        0.07, 0.07, 0.06, 0.05, 0.04, 0.03, 0.02, 0.01, 0.01};
    protected static double wstdPopNormalized[] = {0, 0.12, 0.10, 0.09, 0.09, 0.08, 0.08, 0.06,
        0.06, 0.06,
        0.06, 0.05, 0.04, 0.04, 0.03, 0.02, 0.01,
        0.005, 0.005, 0, 1};
    protected static double cumPop18[] = {5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
        5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0};
    protected static double wstdPop[] = {0, 12000, 10000, 9000, 9000, 8000, 8000, 6000, 6000,
        6000,
        6000, 5000, 4000, 4000, 3000, 2000, 1000, 500, 500, 0,
        100000};
    protected String[] continentLabels = {java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("AFRICA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("AMERICA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("AMERICA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("ASIA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("EUROPE"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("OCEANIA")};
    // childCancerReference[sex][age][l/u]
    static double[][][] childCancerReference = {{{12.3, 24.7}, {8.5, 15.6},
            {8.5, 15.0}
        }, {{9.7, 21.4}, {6.9, 12.0}, {6.8, 13.6}
        }
    };
    // Deafault number of Age groups = 0 year group + 85/5 + '85+' + unknown age + total = 21
    int numberOfAgeGroups = 21;
    int allAgeGroupsIndex = 20;
    static int unknownAgeGroupIndex = 19;
    int unknownAgeInt = 99; // TODO: Make this dynamic!
    static double microscopicallyVerifiedTestTreshold = 1.96;
    static double mortallityIncidenceTestTreshold = 1.96;
    static int highestPopulationAgeGroup = 18;
    int highestMortalityAgeGroup = 18;
    int highestIncidenceAgeGroup = 18;
    int lowestPopulationAgeGroup = 0;
    int lowestMortalityAgeGroup = 0;
    int lowestIncidenceAgeGroup = 0;
    int unknownSexMarker = 3;
    int numberOfSexes = 3;
    int numberOfYears = 0;
    int numberOfYears8 = 0;
    String[] icd10GroupDescriptions;
    int numberOfCancerGroups = 0;
    int bladderCancerGroupIndex = -1;
    int allCancerGroupsButSkinIndex = -1;
    int skinCancerGroupIndex = -1;
    int allCancerGroupsIndex = -1;
    int otherCancerGroupsIndex = -1;
    int kaposiSarkomaCancerGroupIndex = -1;
    int mesotheliomaCancerGroupIndex = -1;
    int leukemiaNOSCancerGroupIndex = -1;
    int myeloproliferativeDisordersCancerGroupIndex = -1;
    int myelodysplasticSyndromesCancerGroupIndex = -1;
    int brainAndCentralNervousSystemCancerGroupIndex = -1;
    int ovaryCancerGroupIndex = -1;
    int continentNumber;
    int populationCode;
    int recordsPerFeedback = 10000;
    static  String tablesPath = "tables/";
    static final String libPath = "lib/";
    static String dataPath = "data/";
    static String convertedPath = "converted/";
    static String registryDictionary = libPath + "/CI5V9.dic";
    static String noC44Dictionary = libPath + "/NoC44.dic";
    static String noMIIllDefDictionary = libPath + "/NoMIIllDef.dic";
    boolean noC44 = false;
    boolean noOldData = false;
    boolean noOldMortalityData = false;
    boolean isSpecialized = false;
    int minimumCasesPerYearLimit = 10;
    int minimumCasesLimit = 0;
    boolean notPublishedVol8 = true;
    static final String v7Path = "CI5VII/";
    static final String v8Path = "CI5VIII/";
    static String v7DataPath = v7Path + "Data/";
    static String v8DataPath = v8Path + "Data/";
    static String v7RegistryDictionary = v7Path + "CI5V7.TXT";
    static String v8RegistryDictionary = libPath + "Ci5V8.dic";
    static String v8PublishedRegistryDictionary = v8Path + "Ci5Viii.dic";
    static String v7IncidenceDataDescriptionFilename = libPath
            + "ci5-vii-incidence.dct";
    static String v7PopulationDataDescriptionFilename = libPath
            + "ci5-vii-population.dct";
    static String v7MortalityDataDescriptionFilename = libPath
            + "ci5-vii-mortality.dct";
    static String v8IncidenceDataDescriptionFilename = libPath
            + "ci5-viii-incidence.dct";
    static String v8PopulationDataDescriptionFilename = libPath
            + "ci5-viii-population.dct";
    static String v8MortalityDataDescriptionFilename = libPath
            + "ci5-viii-mortality.dct";
    // foundAgeGroups always corresponds to "our" age group numbering
    boolean[] foundAgeGroups = new boolean[numberOfAgeGroups];
    String ageLabel[] = {"0", "1-", "5-", "10-", "15-", "20-", "25-", "30-",
        "35-", "40-", "45-", "50-", "55-", "60-", "65-", "70-",
        "75-", "80-", "85+", "Unknown", "Totals"};
    LinkedList cancerGroups[];
    LinkedList lineBreaks;

    @Override
    public abstract StandardVariableNames[] getVariablesNeeded();

    @Override
    public abstract FileTypes[] getFileTypesGenerated();

    @Override
    public abstract LinkedList<String> buildTable(String tableHeader,
            String reportFileName,
            int startYear,
            int endYear,
            Object[][] incidenceData,
            PopulationDataset[] populations,
            PopulationDataset[] standardPopulations,
            LinkedList<ConfigFields> configList,
            String[] engineParameters,
            FileTypes fileType
            ) throws NotCompatibleDataException;

    @Override
    public boolean areThesePopulationDatasetsCompatible(PopulationDataset[] sets) {
        if (sets.length > 0 && sets[0] != null) {
            String filterString = sets[0].getFilter().replaceAll(" ", "");
            for (PopulationDataset population : sets) {
                if (!filterString.equalsIgnoreCase(population.getFilter().replaceAll(" ", ""))) {
                    return false;
                }
            }
        } else {
            return true;
        }
        return true;
    }


    public static int findHighestAgeGroup(boolean[] foundAgeGroups) {
        int highest = 18; // start at 18 - group 19 is unknown age
        while (highest > 0 && !foundAgeGroups[highest]) {
            highest--;
        }
        return highest;
    }

    public int findLowestAgeGroup(boolean[] foundAgeGroups) {
        int lowest = 0; // start at 0 - group 19 is unknown age
        while (lowest < foundAgeGroups.length && lowest < unknownAgeGroupIndex
                && !foundAgeGroups[lowest]) {
            lowest++;
        }
        return lowest;
    }

 

    public static String getContentOfField(LinkedList<FieldDescription> fieldDescriptionList,
            String name, String line) {
        boolean found = false;
        String str = new String();
        int m = 0;
        FieldDescription fieldDescription = null;
        while (!found && m < fieldDescriptionList.size()) {
            // As always; Not the safest to force the linked list to be of a certain type maybe...
            fieldDescription = fieldDescriptionList.get(m++);
            found = name.equalsIgnoreCase(fieldDescription.getName());
        }
        if (found) {
            char[] caIn = line.toCharArray();
            char[] caOut = new char[fieldDescription.getCharacters()];
            for (int n = 0; n < fieldDescription.getCharacters(); n++) {
                caOut[n] = caIn[fieldDescription.getOffset() + n - 1];
            }
            str = new String(caOut);
        } else {
            str = null;
        }
        return str;
    }

    public static String[] breakDownLine(char separatingCharacter, String line) {
        LinkedList<String> elements = new LinkedList();
        int pointer = 0;
        String tmpString = new String();
        boolean finished = false;
        char tmpChar;
        while (pointer < line.length()) {
            tmpChar = line.charAt(pointer);
            if (tmpChar == separatingCharacter) {
                elements.add(tmpString);
                tmpString = new String();
            } else {
                tmpString += tmpChar;
            }
            pointer++;
        }
        if (tmpString != null) {
            elements.add(tmpString);
        }
        String[] elementArray = new String[elements.size()];
        for (int i = 0; i < elementArray.length; i++) {
            elementArray[i] = elements.get(i);
        }
        return elementArray;
    }

    public static String[] breakDownFile(char separatingCharacter, InputStreamReader isr) {
        LinkedList<String> elements = new LinkedList();
        int pointer = 0;
        String tmpString = new String();
        boolean finished = false;
        char tmpChar;
        int tmpInt = -1;
        try {

            tmpInt = isr.read();

            while (tmpInt >= 0 && tmpInt != '\n') {
                tmpChar = (char) tmpInt;
                if (tmpChar == separatingCharacter) {
                    elements.add(tmpString);
                    tmpString = new String();
                } else if (tmpChar == '\"') {
                    tmpInt = isr.read();

                    tmpChar = (char) tmpInt;
                    while (tmpChar != '\"') {
                        tmpString += Character.toChars(tmpInt)[0];
                        tmpInt = isr.read();
                        tmpChar = (char) tmpInt;
                    }
                } else {
                    tmpChar = (char) tmpInt;
                    tmpString += Character.toChars(tmpInt)[0];
                }
                tmpInt = isr.read();
            }
            elements.add(tmpString);
        } catch (IOException iee) {
            System.out.println("Somethings wrong with the file " + isr.toString());
        }
//        if (tmpString!=null)
//            elements.add(tmpString);
        String[] elementArray = new String[elements.size()];
        for (int i = 0; i < elementArray.length; i++) {
            elementArray[i] = elements.get(i);
        }
        return elementArray;
    }

    public static LinkedList readDescription(String descriptionFileName) {
        // read description from file
        try {
            FileReader descriptionFile = new FileReader(descriptionFileName);
            FieldDescriptionReader fdr = new FieldDescriptionReader();
            return FieldDescriptionReader.readFile(descriptionFile);
        } catch (IOException e) {
            System.out.println("Description-File " + descriptionFileName
                    + " not found.");
            return null;
        }
    }

    public static double findHighestPopulationCount(double[][] population) {
        double highest = 0;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < unknownAgeGroupIndex; i++) {
                if (population[j][i] > highest) {
                    highest = population[j][i];
                }
            }
        }
        return highest;
    }

    public static String drawBox(int x1, int y1, int x2, int y2) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        return drawBox(xy1, xy2);
    }

    public static String drawBox(int x1, int y1, int x2, int y2, double fill) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        return drawBox(xy1, xy2, fill);
    }

    public static String drawBox(int x1, int y1, int x2, int y2, double fill,
            boolean outLine) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        return drawBox(xy1, xy2, fill, outLine);
    }

    public static String drawBox(int[] xy1, int[] xy2) {
        return drawBox(xy1, xy2, -1);
    }

    public static String drawBox(int[] xy1, int[] xy2, double fill) {
        return drawBox(xy1, xy2, fill, true);
    }

    public static String drawBox(int[] xy1, int[] xy2, double fill, boolean outLine) {
        String boxCode = new String();

        /* Example code
        newpath
        40 730 MT 40 573 LT
        40 573 MT 196 573 LT
        closepath
         */


        if (outLine) {
            boxCode += "newpath\n";

            boxCode += xy1[0] + " " + xy1[1] + " MT " + xy1[0] + " " + xy2[1]
                    + " LT\n";
            boxCode += xy1[0] + " " + xy2[1] + " MT " + xy2[0] + " " + xy2[1]
                    + " LT\n";
            boxCode += xy2[0] + " " + xy2[1] + " MT " + xy2[0] + " " + xy1[1]
                    + " LT\n";
            boxCode += xy2[0] + " " + xy1[1] + " MT " + xy1[0] + " " + xy1[1]
                    + " LT\n";
            boxCode += "closepath\n";
        }
        if (fill >= 0) {
            boxCode += "gsave\n";
            boxCode += fill + " setgray\n";
            boxCode += Math.min(xy1[0], xy2[0]) + " " + Math.min(xy1[1], xy2[1])
                    + " " + (Math.max(xy1[0], xy2[0]) - Math.min(xy1[0], xy2[0]))
                    + " " + (Math.max(xy1[1], xy2[1]) - Math.min(xy1[1], xy2[1]))
                    + " rectfill\n";
            boxCode += "grestore\n";
        }
        boxCode += "stroke\n";

        return boxCode;
    }

    public static String drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        int xy3[] = {x3, y3};
        return drawTriangle(xy1, xy2, xy3);
    }

    public static String drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3,
            double fill) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        int xy3[] = {x3, y3};
        return drawTriangle(xy1, xy2, xy3, fill);
    }

    public static String drawTriangle(int[] xy1, int[] xy2, int[] xy3) {
        return drawTriangle(xy1, xy2, xy3, -1);
    }

    public static String drawTriangle(int[] xy1, int[] xy2, int[] xy3, double fill) {
        String boxCode = new String();

        /* Example code
        newpath
        40 730 MT 40 573 LT
        40 573 MT 196 573 LT
        closepath
         */

        boxCode += "newpath\n";

        boxCode += xy1[0] + " " + xy1[1] + " MT " + xy2[0] + " " + xy2[1]
                + " LT\n";
        boxCode += xy2[0] + " " + xy2[1] + " MT " + xy3[0] + " " + xy3[1]
                + " LT\n";
        boxCode += xy3[0] + " " + xy3[1] + " MT " + xy1[0] + " " + xy1[1]
                + " LT\n";
        boxCode += "closepath\n";
        if (fill >= 0) {
            boxCode += fill + " setgray\n";
            boxCode += "fill\n";
        }

        boxCode += "stroke\n";

        return boxCode;
    }

    public int getContinentNumber(String registryNumber) {
        int cn = -1;
        try {
            cn = Integer.parseInt(registryNumber.substring(0, 1));
        } catch (NumberFormatException nfe) {
            System.out.println("Faulty registry number...");
        }
        return cn;
    }


    public String[] getInfoArray(String content,
            int columnNumber,
            String infoFile,
            char separatingCharacter) {
        String[] infoArray = null;
        String str = null;

        BufferedReader br;
        FileInputStream fis;
        FileReader fr;

        try {
            //br = new BufferedReader(new InputStreamReader(new FileInputStream(
            //       infoFile)));
            fis = new FileInputStream(infoFile);
            InputStreamReader isr = new InputStreamReader(fis);
            System.out.println("Coding: " + isr.getEncoding());
            //fr = new FileReader(infoFile);
            String[] tmpInfoArray = breakDownFile(separatingCharacter, isr);

            while (tmpInfoArray != null && tmpInfoArray.length > 1) {
                if (content.equals(tmpInfoArray[columnNumber])) {
                    infoArray = tmpInfoArray;
                    break;
                }
                tmpInfoArray = breakDownFile(separatingCharacter, isr);
            }
        } catch (IOException ioe) {
            System.out.println("Dictionary-file error...");
            return null;
        }
        return infoArray;
    }



    public LinkedList[] generateICD9Groups(String[] config) {
        LinkedList[] tempCancerGroups = new LinkedList[config.length];
        for (int n = 0; n < config.length; n++) {
            String group = config[n];
            tempCancerGroups[n] = parseICD9Group(group);
        }
        return tempCancerGroups;
    }

    public LinkedList parseICD10GroupOld(String group) {
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
            cancerGroup.add(i);
            offset = offset + 2;
            if ((group.length() > offset)
                    && group.substring(offset, offset + 1).equals("-")) {
                offset = offset + 1;
                Integer j = Integer.parseInt(group.substring(offset, offset + 2));
                for (int n = i; n < j; n++) {
                    cancerGroup.add(new Integer(n + 1));
                }
                offset = offset + 2;
            }
            if ((offset == group.length())
                    || !group.substring(offset, offset + 1).equals(",")) {
                finished = true;
            }
            offset = offset + 1;
        }
        return cancerGroup;
    }



    public LinkedList parseICD9Group(String group) {
        LinkedList<Integer> cancerGroup = new LinkedList();
        boolean finished = false;
        // First the special cases of all and other&unidentified we just return the empty list
        if (group.equalsIgnoreCase("ALL") || group.equalsIgnoreCase("ALLb")
                || group.equalsIgnoreCase("O&U")) {
            return cancerGroup;
        } else if (group.equals("MES")) {
            return cancerGroup;
        } else if (group.equals("KAP")) {
            return cancerGroup;
        } else {
            int offset = 0;
            while (!finished) {
                Integer i = Integer.parseInt(group.substring(offset, offset + 3));
                offset = offset + 3;
                i = i * 10;
                if ((group.length() > offset)
                        && group.substring(offset, offset + 1).equals(".")) {
                    offset = offset + 1;
                    i = i + Integer.parseInt(group.substring(offset, offset + 1));
                    cancerGroup.add(i);
                    if ((group.length() > offset)
                            && group.substring(offset, offset + 1).equals("-")) {
                        offset = offset + 2;
                        Integer j = Integer.parseInt(group.substring(offset,
                                offset + 1));
                        for (int n = i; n < j; n++) {
                            cancerGroup.add(new Integer(n + 1));
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
                                offset + 3));
                        j = (j + 1) * 10 - 1;
                        for (int n = j; i < n; i++) {
                            cancerGroup.add(new Integer(i));
                        }
                        offset = offset + 2;
                    }
                }
                if ((offset == group.length())
                        || !group.substring(offset, offset + 1).equals(",")) {
                    finished = true;
                }
                offset = offset + 1;
            }
        }
        return cancerGroup;
    }

    public static LinkedList parseLineBreaks(String[] lineBreakStrings) {
        LinkedList tempLineBreaks = new LinkedList();
        for (int n = 0; n < lineBreakStrings.length; n++) {
            String[] line = breakDownLine(',', lineBreakStrings[n]);
            int bre[] = {Integer.parseInt(line[0]), Integer.parseInt(line[1])};
            tempLineBreaks.add(bre);
        }
        return tempLineBreaks;
    }

    public static String array3DToString(double[][][] array) {
        String line = null;
        for (int i = 0; i < array.length; i++) {
            line += array2DToString(array[i]);
            line += "\n";
        }
        return line;
    }

    public static String array2DToString(double[][] array) {
        String line = null;
        for (int i = 0; i < array.length; i++) {
            line += arrayToString(array[i]);
            line += "\n";
        }
        return line;
    }

    public static String arrayToString(double array[]) {
        String line = null;
        for (int i = 0; i < array.length; i++) {
            line += array[i] + "\t";
        }
        return line;
    }

    public int getICD9index(int icdNumber, LinkedList cancerGroups[]) {
        int cancer = -1;
        int index = 0;
        boolean found = false;
        LinkedList group;

        if (icdNumber < 1000) {
            icdNumber = icdNumber * 10;
        }

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

    public int isLineBreak(int line) {
        boolean found = false;
        int n = 0;
        int[] bre = new int[2];
        while (!found && n < lineBreaks.size()) {
            bre = (int[]) lineBreaks.get(n++);
            if (line == bre[0]) {
                return bre[1];
            }
        }
        return 0;
    }

    public static void grayed(int Y, int H, FileWriter pf) {
        try {
            pf.write("0.90 SG\n");
            pf.write("20 " + Y + " MT 580 " + Y + " LT 580 " + (Y - H)
                    + " LT 20 " + (Y - H) + " LT CP fill\n");
            pf.write("0 SG\n");
        } catch (Exception e) {
            System.out.println("FileOut error...");
        }
    }

    public static String formatNumber(double number) {
        return formatNumber(number, 1);
    }

    public static String formatNumber(double number, int decimals) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(decimals);
        nf.setMinimumFractionDigits(decimals);
        if (number == -1) {
            return "-";
        } else {
            return nf.format(number);
        }
    }

    //Tests by PP
    public double calculateASR(double cases, double zurpop, double worldp) {
        double asr = 0;
        if (zurpop > 0) {
            asr = (cases / zurpop) * worldp;
        }
        return asr;
    }

    public double[] calculateASRluL(double asr, double variL,
            double worldp) {
        double[] asrlul = {0, 0, 0};
        double varL = (variL / worldp) / worldp;
        double esL = Math.sqrt(varL);
        asrlul[0] = Math.exp(Math.log(asr) - 1.96 * esL);
        asrlul[1] = Math.exp(Math.log(asr) + 1.96 * esL);
        return asrlul;
    }

    public double calculateEsL(double asr, double variL,
            double worldp) {
        double varL = (variL / worldp) / worldp;
        double esL = Math.sqrt(varL);
        return esL;
    }

    public double calculateVariL(double cases, double worldp, double dummy) {
        double varil = 0;
        if (cases > 0) {
            varil = worldp * worldp / cases;
        }
        return varil;
    }

    public double averagePercentageAnnualChange(double newValue,
            double newMidpoint,
            double oldValue,
            double oldMidpoint) {
        double avg = 0;
        if (oldValue != 0) {
            //avg = 100 * (((newValue - oldValue))) / ((oldValue)) /
            //     ((newNumberOfYears + oldNumberOfYears) / 2);
            // avg = 100 * Math.exp(Math.log((newValue/newNumberOfYears)/(oldValue/oldNumberOfYears)/newNumberOfYears));
            //avg = 100 *
            //      (1 - Math.exp((Math.log(oldValue) - Math.log(newValue)) /
            //               ((newNumberOfYears + oldNumberOfYears) / 2)));
            double years = newMidpoint - oldMidpoint;
            double valueRatio = newValue / oldValue;
            avg = (Math.exp(Math.log(valueRatio) / years) - 1) * 100;
        }
        return avg;
    }

    public boolean significantChangeInASR(double newASR, double newVar,
            double oldASR, double oldVar) {
        double worldp = 100000;
        double rrL = Math.log(newASR) - Math.log(oldASR);
        double varRRL = (newVar / worldp) / worldp + (oldVar / worldp) / worldp;

        //double rr = Math.exp(rrL);
        double rrlL = Math.exp(rrL - 1.96 * Math.sqrt(varRRL));
        double rruL = Math.exp(rrL + 1.96 * Math.sqrt(varRRL));
        boolean significant = !(rrlL <= 1 && rruL >= 1);
        return significant;
    }

    public boolean mortalityIncidenceTest(double D8, double C8, double D9,
            double C9) {
        double Z = (Math.log(D8 / C8) - Math.log(D9 / C9))
                / Math.sqrt(1 / D8 + 1 / C8 + 1 / D9 + 1 / C9);
        return !(Z >= (-mortallityIncidenceTestTreshold)
                && Z <= (mortallityIncidenceTestTreshold));
    }

    public boolean microscopicallyVerifiedTest(double MV8, double C8,
            double MV9, double C9) {
        double odd8 = MV8 / (C8 - MV8);
        double odd9 = MV9 / (C9 - MV9);
        double Z = (Math.log(odd9) - Math.log(odd8))
                / Math.sqrt(1 / MV8 + 1 / (C8 - MV8) + 1 / MV9 + 1 / (C9 - MV9));
        // System.out.println("MV8: " + MV8 + " C8: " + C8 + " MV9: " + MV9 +
        //                    " C9: " + C9 + " Z: " + Z);
        return !(Z >= (-microscopicallyVerifiedTestTreshold)
                && Z <= (microscopicallyVerifiedTestTreshold));
    }

    //MB-tests
    public double calculateASRMB(double cases, double zurpop, double worldp) {
        double asr = 0;
        if (zurpop > 0) {
            asr = (cases / zurpop) * worldp;
        }
        return asr;
    }

    public double[] calculateASRluLMB(double asr, double variL,
            double worldp) {
        double[] asrlul = {0, 0, 0};
        double var = (variL / worldp) / worldp;
        double es = Math.sqrt(var);
        asrlul[0] = Math.max(0, (asr / 100000 - 1.96 * es) * 100000);
        asrlul[1] = (asr / 100000 + 1.96 * es) * 100000;

        // The old way:
        // asrlul[0] = Math.exp(Math.log(asr) - 1.96 * es);
        // asrlul[1] = Math.exp(Math.log(asr) + 1.96 * es);
        return asrlul;
    }

    public double calculateEsLMB(double asr, double variL,
            double worldp) {
        double varL = (variL / worldp) / worldp;
        double esL = Math.sqrt(varL);
        return esL;
    }

    public double calculateVariLMB(double cases, double worldp, double zurpop) {
        // not the variance, but the denuminator of the variance
        double varil = 0;
        if (zurpop > 0) {
            varil = worldp * worldp * (zurpop - cases) * cases / (zurpop * zurpop * zurpop);
        }
        return varil;
    }

    // The old way with a different signature... not the safest way, but necessary here, I'm afraid...
    public double calculateVariLMB(double cases, double worldp) {

        double varil = 0;
        if (cases > 0) {
            varil = worldp * worldp / cases;
        }
        return varil;
    }

    public double averagePercentageAnnualChangeMB(double newValue,
            double newMidpoint,
            double oldValue,
            double oldMidpoint) {
        double avg = 0;
        if (oldValue != 0) {
            //avg = 100 * (((newValue - oldValue))) / ((oldValue)) /
            //     ((newNumberOfYears + oldNumberOfYears) / 2);
            // avg = 100 * Math.exp(Math.log((newValue/newNumberOfYears)/(oldValue/oldNumberOfYears)/newNumberOfYears));
            //avg = 100 *
            //      (1 - Math.exp((Math.log(oldValue) - Math.log(newValue)) /
            //               ((newNumberOfYears + oldNumberOfYears) / 2)));
            double years = newMidpoint - oldMidpoint;
            double valueRatio = newValue / oldValue;
            avg = (Math.exp(Math.log(valueRatio) / years) - 1) * 100;
        }
        return avg;
    }

    public boolean significantChangeInASRMB(double newASR, double newVar,
            double oldASR, double oldVar) {
        double worldp = 100000;
        double rrL = Math.log(newASR) - Math.log(oldASR);
        //double varRRL = (newVar / worldp) / worldp + (oldVar / worldp) / worldp;
        double varRRL = (newVar / ((newASR / worldp) * (newASR / worldp) * (worldp * worldp))) + (oldVar / ((oldASR / worldp) * (oldASR / worldp) * (worldp * worldp)));
        // double varRRL = (newVar / Math.pow((newASR),2))
        //               + (oldVar / Math.pow((oldASR),2));
        //double rr = Math.exp(rrL);
        double rrl = Math.exp(rrL - 1.96 * Math.sqrt(varRRL));
        double rru = Math.exp(rrL + 1.96 * Math.sqrt(varRRL));

        boolean significant = !(rrl <= 1 && rru >= 1);
        return significant;
    }

    public boolean mortalityIncidenceTestMB(double D8, double C8, double D9,
            double C9) {
        double Z = (Math.log(D8 / C8) - Math.log(D9 / C9))
                / Math.sqrt(1 / D8 + 1 / C8 + 1 / D9 + 1 / C9);
        return !(Z >= (-mortallityIncidenceTestTreshold)
                && Z <= (mortallityIncidenceTestTreshold));
    }

    public boolean microscopicallyVerifiedTestMB(double MV8, double C8,
            double MV9, double C9) {
        double odd8 = MV8 / (C8 - MV8);
        double odd9 = MV9 / (C9 - MV9);
        double Z = (Math.log(odd9) - Math.log(odd8))
                / Math.sqrt(1 / MV8 + 1 / (C8 - MV8) + 1 / MV9 + 1 / (C9 - MV9));
        // System.out.println("MV8: " + MV8 + " C8: " + C8 + " MV9: " + MV9 +
        //                    " C9: " + C9 + " Z: " + Z);
        return !(Z >= (-microscopicallyVerifiedTestTreshold)
                && Z <= (microscopicallyVerifiedTestTreshold));
    }

    public String[] breakDownString(char separatingCharacter, String line) {
        LinkedList<String> elements = new LinkedList();
        int pointer = 0;
        String tmpString = new String();
        boolean finished = false;
        char tmpChar;
        while (pointer < line.length()) {
            tmpChar = line.charAt(pointer);
            if (tmpChar == separatingCharacter) {
                elements.add(tmpString);
                tmpString = new String();
            } else {
                if (tmpChar == '\"') {
                    pointer++;
                    tmpChar = line.charAt(pointer);
                    while (tmpChar != '\"' && pointer < line.length()) {
                        tmpString += tmpChar;
                        pointer++;
                        tmpChar = line.charAt(pointer);
                    }
                } else {
                    tmpString += tmpChar;
                }
            }
            pointer++;
        }
        elements.add(tmpString);
        String[] elementArray = new String[elements.size()];
        for (int i = 0; i < elementArray.length; i++) {
            elementArray[i] = elements.get(i);
        }
        return elementArray;
    }

    public void includeFile(String filename, FileWriter fw) {
        String line = new String();
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    filename)));
            line = br.readLine();
            while (line != null) {
                fw.write(line + "\n");
                line = br.readLine();
            }
        } catch (IOException ioe) {
            System.out.println("Include file error...");
        }
    }
}
