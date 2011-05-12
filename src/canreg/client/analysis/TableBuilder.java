package canreg.client.analysis;

/**
 * <p>Title: CI5-IX tools</p>
 *
 * <p>Description: Various tools for CI5-IX</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: IARC-DEP</p>
 *
 * @author Morten Johannes Ervik
 * @version 1.0
 */
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

public abstract class TableBuilder {

    public enum FileTypes {

        ps,
        pdf,
        csv,
        html,
        txt
    };
    protected double estdPop18[] = {0.08, 0.07, 0.07, 0.07, 0.07, 0.07, 0.07, 0.07, 0.07,
        0.07, 0.07, 0.06, 0.05, 0.04, 0.03, 0.02, 0.01, 0.01};
    protected double wstdPopNormalized[] = {0, 0.12, 0.10, 0.09, 0.09, 0.08, 0.08, 0.06,
        0.06, 0.06,
        0.06, 0.05, 0.04, 0.04, 0.03, 0.02, 0.01,
        0.005, 0.005, 0, 1};
    protected double cumPop18[] = {5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
        5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0};
    protected double wstdPop[] = {0, 12000, 10000, 9000, 9000, 8000, 8000, 6000, 6000,
        6000,
        6000, 5000, 4000, 4000, 3000, 2000, 1000, 500, 500, 0,
        100000};
    protected static String[] continentLabels = {java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("AFRICA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("AMERICA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("AMERICA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("ASIA"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("EUROPE"),
        java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("OCEANIA")};
    // childCancerReference[sex][age][l/u]
    static double[][][] childCancerReference = {{{12.3, 24.7}, {8.5, 15.6},
            {8.5, 15.0}
        }, {{9.7, 21.4}, {6.9, 12.0}, {6.8, 13.6}
        }
    };
    // Deafault number of Age groups = 0 year group + 85/5 + '85+' + unknown age + total = 21
    int numberOfAgeGroups = 21;
    int allAgeGroupsIndex = 20;
    int unknownAgeGroupIndex = 19;
    int unknownAgeInt = 99; // TODO: Make this dynamic!
    double microscopicallyVerifiedTestTreshold = 1.96;
    double mortallityIncidenceTestTreshold = 1.96;
    int highestPopulationAgeGroup = 18;
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
    String tablesPath = "tables/";
    String libPath = "lib/";
    String dataPath = "data/";
    String convertedPath = "converted/";
    String registryDictionary = libPath + "/CI5V9.dic";
    String noC44Dictionary = libPath + "/NoC44.dic";
    String noMIIllDefDictionary = libPath + "/NoMIIllDef.dic";
    boolean noC44 = false;
    boolean noOldData = false;
    boolean noOldMortalityData = false;
    boolean isSpecialized = false;
    int minimumCasesPerYearLimit = 10;
    int minimumCasesLimit = 0;
    boolean notPublishedVol8 = true;
    String v7Path = "CI5VII/";
    String v8Path = "CI5VIII/";
    String v7DataPath = v7Path + "Data/";
    String v8DataPath = v8Path + "Data/";
    String v7RegistryDictionary = v7Path + "CI5V7.TXT";
    String v8RegistryDictionary = libPath + "Ci5V8.dic";
    String v8PublishedRegistryDictionary = v8Path + "Ci5Viii.dic";
    String v7IncidenceDataDescriptionFilename = libPath
            + "ci5-vii-incidence.dct";
    String v7PopulationDataDescriptionFilename = libPath
            + "ci5-vii-population.dct";
    String v7MortalityDataDescriptionFilename = libPath
            + "ci5-vii-mortality.dct";
    String v8IncidenceDataDescriptionFilename = libPath
            + "ci5-viii-incidence.dct";
    String v8PopulationDataDescriptionFilename = libPath
            + "ci5-viii-population.dct";
    String v8MortalityDataDescriptionFilename = libPath
            + "ci5-viii-mortality.dct";
    // foundAgeGroups always corresponds to "our" age group numbering
    boolean[] foundAgeGroups = new boolean[numberOfAgeGroups];
    String ageLabel[] = {"0", "1-", "5-", "10-", "15-", "20-", "25-", "30-",
        "35-", "40-", "45-", "50-", "55-", "60-", "65-", "70-",
        "75-", "80-", "85+", "Unknown", "Totals"};
    LinkedList cancerGroups[] = null;
    LinkedList lineBreaks = null;

    public abstract StandardVariableNames[] getVariablesNeeded();

    public abstract FileTypes[] getFileTypesGenerated();

    public abstract LinkedList<String> buildTable(String tableHeader,
            String reportFileName,
            int startYear,
            int endYear,
            Object[][] incidenceData,
            PopulationDataset[] populations,
            PopulationDataset[] standardPopulations,
            LinkedList<ConfigFields> configList,
            String[] engineParameters) throws NotCompatibleDataException;

    // Added 30.07.2007
    public boolean showMIIllDefSite(String registryNumber) {
        boolean found = false;
        FileReader dataFile;
        String line;
        try {
            dataFile = new FileReader(noMIIllDefDictionary);
            line = readLine(dataFile);
            while (!found && !line.equals("EOF")) {
                found = line.trim().equalsIgnoreCase(registryNumber);
                line = readLine(dataFile);
            }
        } catch (IOException e) {
            System.out.println("MIIllDef-file error.");
        }
        return !found;
    }

    public boolean areThesePopulationDatasetsOK(PopulationDataset[] sets) {
        if (sets.length > 0 && sets[0] != null) {

            String filterString = sets[0].getFilter().replaceAll(" ", "");
            for (PopulationDataset population : sets) {
                if (!filterString.equalsIgnoreCase(population.getFilter().replaceAll(" ", ""))) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public void loadIncidenceData(String dataFileName,
            LinkedList<FieldDescription> fieldDescriptionList, LinkedList cancerGroups[], int years[],
            int bladderGroup, double casesArray[][][]) {

        FileReader dataFile;
        int records = 0;
        // generate statistics
        try {
            dataFile = new FileReader(dataFileName);
            String line = readLine(dataFile);
            String sexString;
            String icdString;
            String yearString;
            String ageGroupString;
            int sex, icdNumber, year, icdIndex, yearIndex, ageGroup;
            while (!line.equals("EOF")) {
                if (records % recordsPerFeedback == 0) {
                    System.out.println("Processing record number: " + records);
                }
                // Set default
                icdIndex = -1;
                // Unknown sex group = 3
                sex = 3;
                // Extract data
                sexString = getContentOfField(fieldDescriptionList,
                        "sex", line);
                sex = Integer.parseInt(sexString.trim());

                // sex = 3 is unknown sex

                if (sex > 2) {
                    sex = 3;
                }

                icdString = getContentOfField(fieldDescriptionList,
                        "icd", line).trim();
                if (icdString.length() > 0
                        && icdString.trim().substring(0, 1).equals("C")) {
                    icdString = icdString.trim().substring(1);
                    icdNumber = Integer.parseInt(icdString);
                    if (icdString.length() < 3) {
                        icdNumber = icdNumber * 10;
                    }
                    icdIndex = getICD10index(icdNumber, cancerGroups);
                    if (icdIndex == -1) {
                        icdIndex = -1;
                    }
                } else if (icdString.length() > 0
                        && icdString.trim().substring(0, 1).equals("D")) {
                    icdString = icdString.trim().substring(1);
                    icdNumber = Integer.parseInt(icdString);
                    if (icdNumber == 90 || icdNumber == 414) {
                        icdIndex = bladderGroup;
                    }
                }
                yearString = getContentOfField(fieldDescriptionList,
                        "year_of_incidence", line);
                year = Integer.parseInt(yearString);
                yearIndex = year - years[0];
                ageGroupString = getContentOfField(fieldDescriptionList,
                        "age_group", line);
                ageGroup = Integer.parseInt(ageGroupString);

                if (sex <= numberOfSexes && icdIndex >= 0
                        && icdIndex <= cancerGroups.length
                        && year <= years[1]
                        && year >= years[0]) {
                    // Generate statistics
                    casesArray[icdIndex][sex - 1][ageGroup]++;
                } else {
                    //  System.out.println("Out of bouds: sex: " + sex +
                    //                    ", icdIndex: " + icdIndex + ", year: " +
                    //                    year);
                }
                records++;
                // Read next line
                line = readLine(dataFile);
            }
            System.out.println("Processed " + records + " records.");

        } catch (IOException e) {
            System.out.println("Data-file error.");
        }

    }

    public int loadMortalityData(String populationFileName,
            LinkedList<FieldDescription> mortalityFieldDescriptionList, int[][] mortalityArray,
            int populationCode) {

        FileReader dataFile;
        int yearsInFile = 0;

        // mortalityArray formatted: int[sex][icdGroup];

        // load mortality data
        try {
            dataFile = new FileReader(populationFileName);
            String line = readLine(dataFile);
            yearsInFile = Integer.parseInt(line.substring(14, line.length()).
                    trim());

            line = readLine(dataFile);
            String yearString;
            String sexString;
            String ageGroupString;
            String casesString;
            String icdString;
            String populationGroupString;
            int sex, year, yearIndex, ageGroup, cases, icdGroup,
                    populationGroup;
            while (!line.equals("EOF") && !line.trim().equals("")) {
                // Load data

                populationGroupString = getContentOfField(
                        mortalityFieldDescriptionList,
                        "population_code", line);
                if (populationGroupString != null) {
                    populationGroup = Integer.parseInt(populationGroupString.trim());
                }
                sexString = getContentOfField(mortalityFieldDescriptionList,
                        "sex", line);
                sex = Integer.parseInt(sexString.trim()) - 1;

                // yearString = getContentOfField(mortalityFieldDescriptionList,
                //                               "year_of_death", line);
                // year = Integer.parseInt(yearString.trim());
                // yearIndex = year - years[0];

                ageGroupString = getContentOfField(
                        mortalityFieldDescriptionList,
                        "age_group", line);
                ageGroup = Integer.parseInt(ageGroupString.trim());
                foundAgeGroups[ageGroup] = true;

                icdString = getContentOfField(mortalityFieldDescriptionList,
                        "icd_group", line);
                icdGroup = Integer.parseInt(icdString.trim());

                // sfdjiwesgsg

                casesString = getContentOfField(mortalityFieldDescriptionList,
                        "cases", line);
                cases = Integer.parseInt(casesString.trim());
                if (ageGroup != allAgeGroupsIndex) {
                    //mortalityArray[sex][icdGroup] += cases;
                } else {
                    // System.out.println(mortalityArray[sex][icdGroup] + " " +
                    //                   cases);
                    mortalityArray[sex][icdGroup] += cases;
                }

                // Read next line
                line = readLine(dataFile);
            }
            dataFile.close();
        } catch (IOException e) {
            System.out.println("Mortality-file error." + e);
        }
        return yearsInFile;
    }

    public boolean loadOldMortalityData(String mortalityFileName,
            LinkedList<FieldDescription> mortalityFieldDescriptionList, int[][] mortalityArray,
            LinkedList[] icd9Groups) {

        //returns true if file is loaded successfully...

        FileReader dataFile;

        // mortalityArray formatted: int[sex][icdGroup];

        // load mortality data
        try {
            dataFile = new FileReader(mortalityFileName);
            String line = readLine(dataFile);
            String yearString;
            String sexString;
            String ageGroupString;
            String casesString;
            String icdString;
            String populationGroupString;
            int sex, year, yearIndex, ageGroup, cases, icdGroup,
                    populationGroup;
            while (!line.equals("EOF") && !line.trim().equals("")) {
                // Load data

                populationGroupString = getContentOfField(
                        mortalityFieldDescriptionList,
                        "population_code", line);
                if (populationGroupString != null) {
                    populationGroup = Integer.parseInt(populationGroupString.trim());
                }
                sexString = getContentOfField(mortalityFieldDescriptionList,
                        "sex", line);
                sex = Integer.parseInt(sexString.trim()) - 1;

                // yearString = getContentOfField(mortalityFieldDescriptionList,
                //                               "year_of_death", line);
                // year = Integer.parseInt(yearString.trim());
                // yearIndex = year - years[0];

                // ageGroupString = getContentOfField(
                //        mortalityFieldDescriptionList,
                //        "age_group", line);
                // ageGroup = Integer.parseInt(ageGroupString.trim());
                // foundAgeGroups[ageGroup] = true;

                icdString = getContentOfField(mortalityFieldDescriptionList,
                        "icd9", line);
                icdGroup = getICD9index(Integer.parseInt(icdString.trim()),
                        icd9Groups);

                try {
                    casesString = getContentOfField(
                            mortalityFieldDescriptionList,
                            "cases", line);
                } catch (ArrayIndexOutOfBoundsException aie) {
                    // Sometimes the old data has less wide lines so we add some spaces
                    casesString = getContentOfField(
                            mortalityFieldDescriptionList,
                            "cases", line + "   ");
                }
                cases = Integer.parseInt(casesString.trim());

                if (icdGroup >= 0) {
                    mortalityArray[sex][icdGroup] += cases;
                }
                mortalityArray[sex][allCancerGroupsIndex] += cases;

                if (skinCancerGroupIndex > 0
                        && icdGroup != skinCancerGroupIndex) {
                    mortalityArray[sex][allCancerGroupsButSkinIndex] += cases;
                }

                // Read next line
                line = readLine(dataFile);
            }
            dataFile.close();
            return true;
        } catch (IOException e) {
            System.out.println("No old mortality data." + e);
            return false;
        }
    }

    public int findHighestAgeGroup(boolean[] foundAgeGroups) {
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

    public void loadPopulationData(String populationFileName,
            LinkedList<FieldDescription> populFieldDescriptionList, double[][] populationArray,
            boolean[] foundAgeGroups, boolean oldData) {
        FileReader dataFile;
        // load population data
        try {
            dataFile = new FileReader(populationFileName);
            String line = readLine(dataFile);
            String yearString;
            String sexString;
            String ageGroupString;
            String popString;
            int sex, year, yearIndex, ageGroup, population;
            while (!line.equals("EOF") && !line.trim().equals("")) {
                // Load data
                sexString = getContentOfField(populFieldDescriptionList,
                        "sex", line);
                sex = Integer.parseInt(sexString.trim()) - 1;
                ageGroupString = getContentOfField(populFieldDescriptionList,
                        "age_group", line);
                ageGroup = Integer.parseInt(ageGroupString.trim());

                //adjust for the age groupings of old data...
                if (oldData) {
                    ageGroup--;
                }

                foundAgeGroups[ageGroup] = true;

                popString = getContentOfField(populFieldDescriptionList,
                        "person_years", line);
                population = Integer.parseInt(popString.trim());
                populationArray[sex][ageGroup] += population;
                // Read next line
                line = readLine(dataFile);

                if (oldData && allAgeGroupsIndex > 0) {
                    populationArray[sex][allAgeGroupsIndex] += population;
                }

            }
            dataFile.close();
        } catch (IOException e) {
            System.out.println("Population-file error." + e);
        }
    }

    public int loadPopulationDataByYear(String populationFileName,
            LinkedList<FieldDescription> populFieldDescriptionList, double[][][] populationArray,
            boolean[] foundAgeGroups, boolean oldData) {
        FileReader dataFile;
        // load population data

        int numberOfYearsInFile = findNumberOfYearsInPopulationData(
                populationFileName, populFieldDescriptionList);
        int numberOfYearsStated = 0;

        try {
            dataFile = new FileReader(populationFileName);
            String line = readLine(dataFile);
            String yearString;
            String sexString;
            String ageGroupString;
            String popString;
            String populationCodeString;
            int sex, year, yearIndex, ageGroup, population, populationCode;
            yearIndex = 0;

            int interval_start = Integer.parseInt(getContentOfField(
                    populFieldDescriptionList,
                    "interval_start", line));
            int interval_end = Integer.parseInt(getContentOfField(
                    populFieldDescriptionList,
                    "interval_end", line));
            boolean firstLine = true;
            String firstSex = null;
            String firstAgeGroup = null;
            String firstPopulationCode = null;

            numberOfYearsStated = (interval_end - interval_start) + 1;

            numberOfYearsInFile = Math.min(numberOfYearsInFile, numberOfYearsStated);

            while (!line.equals("EOF") && !line.trim().equals("")) {
                // Load data
                sexString = getContentOfField(populFieldDescriptionList,
                        "sex", line);
                populationCodeString = getContentOfField(
                        populFieldDescriptionList,
                        "population_code", line);
                sex = Integer.parseInt(sexString.trim()) - 1;
                ageGroupString = getContentOfField(populFieldDescriptionList,
                        "age_group", line);
                ageGroup = Integer.parseInt(ageGroupString.trim());

                //adjust for the age groupings of old data...
                if (oldData) {
                    ageGroup--;
                }

                if (firstLine) {
                    firstSex = sexString;
                    firstAgeGroup = ageGroupString;
                    firstPopulationCode = populationCodeString;
                    yearIndex = 0;
                    firstLine = false;
                } else if (populationCodeString.equalsIgnoreCase(
                        firstPopulationCode)
                        && sexString.equalsIgnoreCase(firstSex)
                        && ageGroupString.equalsIgnoreCase(firstAgeGroup)) {
                    yearIndex++;
                    if (yearIndex >= numberOfYearsInFile) {
                        yearIndex = 0;
                    }
                }

                foundAgeGroups[ageGroup] = true;

                popString = getContentOfField(populFieldDescriptionList,
                        "person_years", line);
                population = Integer.parseInt(popString.trim());
                if (yearIndex < populationArray.length) {
                    populationArray[yearIndex][sex][ageGroup] += population;
                } else {
                    System.out.println("Population file error...");
                }
                // Read next line
                line = readLine(dataFile);
                /*
                if (allAgeGroupsIndex > 0) {
                populationArray[yearIndex][sex][allAgeGroupsIndex] +=
                population;
                }
                 */
            }
            dataFile.close();

            // We have to adjust the data?

            if (numberOfYearsInFile < numberOfYearsStated) {
                double ratio = numberOfYearsStated / numberOfYearsInFile;
                for (int ageGroupIndex = 0;
                        ageGroupIndex < populationArray[0][0].length; ageGroupIndex++) {
                    for (int sexIndex = 0; sexIndex < populationArray[0].length;
                            sexIndex++) {
                        // generate a total
                        double total = 0;
                        for (yearIndex = 0; yearIndex < numberOfYearsStated;
                                yearIndex++) {
                            total += populationArray[yearIndex][sexIndex][ageGroupIndex];
                        }
                        double fillValue = total / ratio;
                        for (yearIndex = 0; yearIndex < numberOfYearsStated;
                                yearIndex++) {
                            populationArray[yearIndex][sexIndex][ageGroupIndex] =
                                    fillValue;
                        }
                        populationArray[numberOfYearsStated][sexIndex][ageGroupIndex] = total;
                    }
                }
            } else {
                // generate totals

                for (int ageGroupIndex = 0;
                        ageGroupIndex < populationArray[0][0].length;
                        ageGroupIndex++) {
                    for (int sexIndex = 0; sexIndex < populationArray[0].length;
                            sexIndex++) {
                        // generate a total
                        double total = 0;
                        for (yearIndex = 0; yearIndex < numberOfYearsStated;
                                yearIndex++) {
                            total += populationArray[yearIndex][sexIndex][ageGroupIndex];
                        }
                        populationArray[numberOfYearsStated][sexIndex][ageGroupIndex] = total;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Population-file error." + e);
        }

        return numberOfYearsInFile;

    }

    public int findNumberOfYearsInPopulationData(String populationFileName,
            LinkedList<FieldDescription> populFieldDescriptionList) {

        int numberOfYearsInFile = 0;

        boolean firstLine = true;
        String firstSex = null;
        String firstAgeGroup = null;
        String firstPopulationCode = null;

        FileReader dataFile;
        // load population data
        try {
            dataFile = new FileReader(populationFileName);
            String line = readLine(dataFile);
            String yearString;
            String sexString;
            String ageGroupString;
            String popString;
            int sex, year, yearIndex, ageGroup, populationCode;
            while (!line.equals("EOF") && !line.trim().equals("")) {
                // Load data
                sexString = getContentOfField(populFieldDescriptionList,
                        "sex", line);
                //sex = Integer.parseInt(sexString.trim()) - 1;
                ageGroupString = getContentOfField(populFieldDescriptionList,
                        "age_group", line);
                // ageGroup = Integer.parseInt(ageGroupString.trim());

                popString = getContentOfField(populFieldDescriptionList,
                        "population_code", line);
                //populationCode = Integer.parseInt(popString.
                //                              trim());

                if (popString == null) {
                    popString = "default";
                }

                if (firstLine) {
                    firstSex = sexString;
                    firstAgeGroup = ageGroupString;
                    firstPopulationCode = popString;
                    numberOfYearsInFile++;
                    firstLine = false;
                } else if (sexString.equalsIgnoreCase(firstSex)
                        && ageGroupString.equalsIgnoreCase(firstAgeGroup)
                        && popString.equalsIgnoreCase(firstPopulationCode)) {
                    numberOfYearsInFile++;
                }
                // Read next line
                line = readLine(dataFile);
            }
            dataFile.close();
        } catch (IOException e) {
            System.out.println("Population-file error." + e);
        }
        return numberOfYearsInFile;
    }

    public String readLine(FileReader file) {
        String str = new String("");
        int c;
        try {
            c = file.read();
            //First skip blank lines
            while (c == '\n' || c == '\r') {
                c = file.read();
            }
            while (c != '\n' && c != '\r') {
                // If we find an EOF-mark we terminate and return EOF - that means we need a enter after the last line of data
                if (c == -1) {
                    str = "EOF";
                    break;
                }
                str = str + (char) c;
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while reading a line.");
        }
        //System.out.println("Returning " + str);
        return str;
    }

    public String getContentOfField(LinkedList<FieldDescription> fieldDescriptionList,
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

    public String[] breakDownLine(char separatingCharacter, String line) {
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

    public String[] breakDownFile(char separatingCharacter, InputStreamReader isr) {
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

    public LinkedList readDescription(String descriptionFileName) {
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

    public double findHighestPopulationCount(double[][] population) {
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

    public String drawBox(int x1, int y1, int x2, int y2) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        return drawBox(xy1, xy2);
    }

    public String drawBox(int x1, int y1, int x2, int y2, double fill) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        return drawBox(xy1, xy2, fill);
    }

    public String drawBox(int x1, int y1, int x2, int y2, double fill,
            boolean outLine) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        return drawBox(xy1, xy2, fill, outLine);
    }

    public String drawBox(int[] xy1, int[] xy2) {
        return drawBox(xy1, xy2, -1);
    }

    public String drawBox(int[] xy1, int[] xy2, double fill) {
        return drawBox(xy1, xy2, fill, true);
    }

    public String drawBox(int[] xy1, int[] xy2, double fill, boolean outLine) {
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

    public String drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        int xy3[] = {x3, y3};
        return drawTriangle(xy1, xy2, xy3);
    }

    public String drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3,
            double fill) {
        int xy1[] = {x1, y1};
        int xy2[] = {x2, y2};
        int xy3[] = {x3, y3};
        return drawTriangle(xy1, xy2, xy3, fill);
    }

    public String drawTriangle(int[] xy1, int[] xy2, int[] xy3) {
        return drawTriangle(xy1, xy2, xy3, -1);
    }

    public String drawTriangle(int[] xy1, int[] xy2, int[] xy3, double fill) {
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

    public LinkedList[] generateICD10Groups(String[] config) {
        LinkedList[] tempCancerGroups = new LinkedList[config.length];
        for (int n = 0; n < config.length; n++) {
            String group = config[n];
            tempCancerGroups[n] = parseICD10Group(group);
        }
        return tempCancerGroups;
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

    public LinkedList parseICD10Group(String group) {
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
                        cancerGroup.add(new Integer(i + 1));
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
            // added 07/03/07 Morten
            // skip C's
            if (!finished && (group.substring(offset, offset + 1).equals("C"))) {
                offset = offset + 1;
            }
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

    public LinkedList parseLineBreaks(String[] lineBreakStrings) {
        LinkedList tempLineBreaks = new LinkedList();
        for (int n = 0; n < lineBreakStrings.length; n++) {
            String[] line = breakDownLine(',', lineBreakStrings[n]);
            int bre[] = {Integer.parseInt(line[0]), Integer.parseInt(line[1])};
            tempLineBreaks.add(bre);
        }
        return tempLineBreaks;
    }

    public String array3DToString(double[][][] array) {
        String line = null;
        for (int i = 0; i < array.length; i++) {
            line += array2DToString(array[i]);
            line += "\n";
        }
        return line;
    }

    public String array2DToString(double[][] array) {
        String line = null;
        for (int i = 0; i < array.length; i++) {
            line += arrayToString(array[i]);
            line += "\n";
        }
        return line;
    }

    public String arrayToString(double array[]) {
        String line = null;
        for (int i = 0; i < array.length; i++) {
            line += array[i] + "\t";
        }
        return line;
    }

    public int getICD10index(int icdNumber, LinkedList cancerGroups[]) {
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

    public int getICD10index(String groupName, String cancerLabels[]) {
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

    public void grayed(int Y, int H, FileWriter pf) {
        try {
            pf.write("0.90 SG\n");
            pf.write("20 " + Y + " MT 580 " + Y + " LT 580 " + (Y - H)
                    + " LT 20 " + (Y - H) + " LT CP fill\n");
            pf.write("0 SG\n");
        } catch (Exception e) {
            System.out.println("FileOut error...");
        }
    }

    public String formatNumber(double number) {
        return formatNumber(number, 1);
    }

    public String formatNumber(double number, int decimals) {
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
