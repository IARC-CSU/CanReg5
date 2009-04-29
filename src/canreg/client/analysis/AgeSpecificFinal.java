package canreg.client.analysis;

import java.util.LinkedList;
import java.io.IOException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileWriter;
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
public class AgeSpecificFinal extends EditorialTable {

    public AgeSpecificFinal(String registryNumber,
                           String configFileName,
                           String incidenceFileName,
                           String incidenceFileDescriptionFileName,
                           String populationFileName,
                           String populationFileDescriptionFileName,
                           String mortalityFileName,
                           String mortalityFileDescriptionFileName,
                           String populationCodeString) {

        String footerString = "Cancer Incidence in Five Continents Vol. IX, IARC - 2007";

        String notesString = "See notes on population page.";

        String populationConfigFile = "lib/pyramidsfinal.conf";

        double tableFontSize = 7.5 ;
        String font = "Times";

        //int REGLIB = 10;
        int MAXYEAR = 0;
        int MAXYEAR8 = 0;

        boolean reportToFile = true;

        double casesArray[][][] = null; // a 3D array of sex, icd and agegroup - with one extra layer in all dimensions containing a sum of all
        double populationArray[][] = null; // contains population count in the following format: [sex][agegroup]

//      double RegPop[][];
        double CA[][];
        double CR[][];
        double MV[][];
        double ASR[][];
        double ASRbyAgeGroup[][][];
        double ratei[][];
//      double vASR[][];
        double ASRluL[][][];
        double variL[][];
        double variLbyAgeGroup[][][];
        double DCO[][];

        char ASRf[][];
        double ASRratio[][];
        char MVf[][];

        double CA8[][];
        double CR8[][];
        double MV8[][];
        double ASR8[][];
        double ASR8byAgeGroup[][][];
        double ASR8luL[][][];
        double variL8[][];
        double variL8byAgeGroup[][][];
        double DCO8[][];

        boolean foundAgeGroups8[];

        double casesArray8[][][] = null; // a 3D array of sex, icd and agegroup - with one extra layer in all dimensions containing a sum of all
        double populationArray8[][] = null; // contains population count in the following format: [sex][agegroup]

        String dayLabel[];
        String monthLabel[];
        String sexLabel[] = null;
        String tableLabel[] = null;
        String icdLabel[] = null;

        LinkedList cancerGroups[] = null;
        int years[] = null;
        int years8[] = null;

        LinkedList<FieldDescription> incidenceFieldDescriptionList = null;
        LinkedList<FieldDescription> populFieldDescriptionList = null;
        FileReader configFile;
        LinkedList configList;

        String registryLabel = null;
        String registryLabel8 = null;

        String sourceDataBaseFileName = null;
        int registryNumberColumn = -1;
        int notesColumn = -1;
        int sourceColumn = -1;

        String notes = null;
        String source = null;

        String[] infoArray = null;

        char separatingCharacter = ',';

        boolean showSeeNotesNote = true;

        char Childc[][] = new char[2][3];

        double casesPerHundredThousand[][][];

        double cumRate64[][];
        double cumRate74[][];

        // Load config
        try {
            ConfigFieldsReader populationConfigFieldsReader = new ConfigFieldsReader();
            configFile = new FileReader(populationConfigFile);
            configList = populationConfigFieldsReader.readFile(configFile);

            sourceDataBaseFileName = populationConfigFieldsReader.findConfig(
                    "source_database",
                    configList)[0];
            registryNumberColumn = Integer.parseInt(populationConfigFieldsReader.
                                                    findConfig("registry_number_column",
                    configList)[0]);
            notesColumn = Integer.parseInt(populationConfigFieldsReader.findConfig(
                    "notes_column",
                    configList)[0]);
            sourceColumn = Integer.parseInt(populationConfigFieldsReader.findConfig(
                    "source_column",
                    configList)[0]);

            separatingCharacter = populationConfigFieldsReader.findConfig(
                    "separating_character",
                    configList)[0].charAt(0);

            infoArray = getInfoArray(registryNumber + "00",
                                     registryNumberColumn,
                                     sourceDataBaseFileName,
                                     separatingCharacter);

            /* Test removed 16th of July 2007

             if (infoArray!=null && infoArray.length>notesColumn) {
                showSeeNotesNote = (infoArray[notesColumn].trim().length()>0);
            }

            else showSeeNotesNote = false; */

            ConfigFieldsReader configFieldsReader = new ConfigFieldsReader();
            configFile = new FileReader(configFileName);
            configList = configFieldsReader.readFile(configFile);

            tableLabel = configFieldsReader.findConfig("table_label",
                    configList);
            sexLabel = configFieldsReader.findConfig("sex_label", configList);
            icdLabel = configFieldsReader.findConfig("ICD_groups_labels",
                    configList);
            icd10GroupDescriptions = configFieldsReader.findConfig(
                    "ICD10_groups",
                    configList);

            cancerGroups = generateICD10Groups(icd10GroupDescriptions);

            allCancerGroupsIndex = getICD10index("ALL", icd10GroupDescriptions);

            leukemiaNOSCancerGroupIndex = getICD10index(950,
                    cancerGroups);

            skinCancerGroupIndex = getICD10index("C44",
                                                 icd10GroupDescriptions);

            bladderCancerGroupIndex = getICD10index("C67",
                    icd10GroupDescriptions);

            mesotheliomaCancerGroupIndex = getICD10index("C45",
                    icd10GroupDescriptions);

            kaposiSarkomaCancerGroupIndex = getICD10index("C46",
                    icd10GroupDescriptions);

            myeloproliferativeDisordersCancerGroupIndex = getICD10index("MPD",
                    icd10GroupDescriptions);

            myelodysplasticSyndromesCancerGroupIndex = getICD10index("MDS",
                    icd10GroupDescriptions);

            allCancerGroupsButSkinIndex = getICD10index("ALLbC44",
                    icd10GroupDescriptions);

            leukemiaNOSCancerGroupIndex = getICD10index(950,
                    cancerGroups);

            brainAndCentralNervousSystemCancerGroupIndex = getICD10index("C70-72",
                                                 icd10GroupDescriptions);

            ovaryCancerGroupIndex = getICD10index(569,
                                                 cancerGroups);

            otherCancerGroupsIndex = getICD10index("O&U", icd10GroupDescriptions);

            numberOfCancerGroups = cancerGroups.length;
            registryLabel = getRegistryLabel(registryNumber, registryDictionary);
            registryLabel8 = getRegistryLabel(registryNumber,
                                              v8RegistryDictionary);

            if (registryLabel.charAt(0)=='s') {
                isSpecialized = true;
                registryLabel= registryLabel.substring(1).trim();
            }

            lineBreaks = parseLineBreaks(configFieldsReader.findConfig("line_breaks", configList));

            years = extractTimeSpan(registryLabel);

            if (years == null) {
                return;
            }

            numberOfYears = years[1] - years[0] + 1;

            minimumCasesLimit = minimumCasesPerYearLimit * numberOfYears;

            years8 = extractTimeSpan(registryLabel8);

            if (years8 == null) {
                noOldData = true;
            } else

                numberOfYears8 = years8[1] - years8[0] + 1;

            continentNumber = getContinentNumber(registryNumber);


        } catch (IOException e) {
            System.out.println("Config-file error.");
        }

        casesPerHundredThousand = new double[numberOfSexes][
                                               numberOfAgeGroups][
                                       numberOfCancerGroups];

        casesArray = new double[numberOfCancerGroups][numberOfSexes][
                     numberOfAgeGroups];

        cumRate64 = new double[numberOfSexes][numberOfCancerGroups];
        cumRate74 = new double[numberOfSexes][numberOfCancerGroups];

        incidenceFieldDescriptionList = readDescription(
                incidenceFileDescriptionFileName);

        // load population data for volume 9
        populationArray = new double[numberOfSexes][numberOfAgeGroups + 1];

        populFieldDescriptionList = readDescription(
                populationFileDescriptionFileName);

        loadPopulationData(populationFileName, populFieldDescriptionList,
                           populationArray, foundAgeGroups, false);

        highestPopulationAgeGroup = findHighestAgeGroup(foundAgeGroups);
        lowestPopulationAgeGroup = findLowestAgeGroup(foundAgeGroups);

        FileReader dataFile;
        int records = 0;
        // generate statistics

        // Generate based on death certificate only
        DCO = new double[numberOfSexes][numberOfCancerGroups];

        // and microscopical verification
        MV = new double[numberOfSexes][numberOfCancerGroups];

        try {
            dataFile = new FileReader(incidenceFileName);
            String line = readLine(dataFile);
            String sexString;
            String icdString;
            String morphologyString;
            String yearString;
            String ageGroupString;
            String basisString;
            int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, basis;

            while (!line.equals("EOF")) {

                // Set default
                icdIndex = -1;
                // Unknown sex group = 3
                sex = 3;
                // Extract data
                sexString = getContentOfField(incidenceFieldDescriptionList,
                                              "sex", line);
                sex = Integer.parseInt(sexString.trim());

                // sex = 3 is unknown sex

                if (sex > 2) {
                    sex = 3;
                }

                morphologyString = getContentOfField(
                        incidenceFieldDescriptionList,
                        "morphology", line).trim();

                /*
                                 if (morphologyString.length() > 0) {
                    int morphology = Integer.parseInt(morphologyString);
                    if (morphology == 9140) {
                        String behaviourString = getContentOfField(
                                incidenceFieldDescriptionList,
                                "behaviour", line).trim();
                        if (behaviourString.equals("3")) {
                            icdIndex = kaposiSarkomaCancerGroupIndex;
                        }

                    } else if ((int)(morphology/10) == 905) {
                 String behaviourString = getContentOfField(incidenceFieldDescriptionList,
                                              "behaviour", line).trim();
                        if (behaviourString.equals("3")) {
                            icdIndex = mesotheliomaCancerGroupIndex;
                        }
                    }
                                 }
                 */

                if (icdIndex < 0) {
                    icdString = getContentOfField(incidenceFieldDescriptionList,
                                                  "icd", line).trim();
                    if (icdString.length() > 0 &&
                        icdString.trim().substring(0, 1).equals("C")) {
                        icdString = icdString.trim().substring(1);
                        icdNumber = Integer.parseInt(icdString);
                        if (icdString.length() < 3) {
                            icdNumber = icdNumber * 10;
                        }
                        icdIndex = getICD10index(icdNumber, cancerGroups);
                        if (icdIndex == -1) {
                            icdIndex = -1;
                        }
                    } else if (icdString.length() > 0 &&
                               icdString.trim().substring(0, 1).equals("D")) {
                        icdString = icdString.trim().substring(1);
                        icdNumber = Integer.parseInt(icdString);
                        if (icdString.length() < 3) {
                            icdNumber = icdNumber * 10;
                        }
                        if (icdNumber == 90 || icdNumber == 414) {
                            icdIndex = bladderCancerGroupIndex;
                        } else if ((int)(icdNumber/10) == 45 || (int) (icdNumber/10) == 47) {
                            icdIndex = myeloproliferativeDisordersCancerGroupIndex;
                        }
                        else if ((int)(icdNumber/10) == 46) {
                            icdIndex = myelodysplasticSyndromesCancerGroupIndex;
                        }
                    }

                }

                yearString = getContentOfField(incidenceFieldDescriptionList,
                                               "year_of_incidence", line);
                year = Integer.parseInt(yearString);
                yearIndex = year - years[0];
                ageGroupString = getContentOfField(
                        incidenceFieldDescriptionList,
                        "age_group", line);
                ageGroup = Integer.parseInt(ageGroupString);

                if (year <= years[1] && year >= years[0]) {

                    // Basis of diagnosis
                    basisString = getContentOfField(
                            incidenceFieldDescriptionList,
                            "basis", line);
                    if (basisString != null) {
                        basis = Integer.parseInt(basisString.trim());
                    } else {
                        basis = -1;
                    }

                    if (sex <= numberOfSexes && icdIndex >= 0 &&
                        icdIndex <= cancerGroups.length) {

                        casesArray[icdIndex][sex - 1][ageGroup]++;

                        //
                        if (basis == 00) {
                            DCO[sex - 1][icdIndex]++;
                        } else if (basis >= 10 && basis <= 19) {
                            MV[sex - 1][icdIndex]++;
                        }
                    } else {
                        if (otherCancerGroupsIndex >= 0) {
                            casesArray[otherCancerGroupsIndex][sex -
                                    1][ageGroup]++;
                        }
                    }
                    if (allCancerGroupsIndex >= 0) {
                        casesArray[allCancerGroupsIndex][sex - 1][ageGroup]++;
                        if (basis == 0) {
                            DCO[sex - 1][allCancerGroupsIndex]++;
                        } else if (basis >= 10 && basis <= 19) {
                            MV[sex - 1][allCancerGroupsIndex]++;
                        }
                    }
                    if (allCancerGroupsButSkinIndex >= 0 &&
                        skinCancerGroupIndex >= 0 &&
                        icdIndex != skinCancerGroupIndex) {
                        casesArray[allCancerGroupsButSkinIndex][sex -
                                1][ageGroup]++;
                        if (basis == 0) {
                            DCO[sex - 1][allCancerGroupsButSkinIndex]++;
                        } else if (basis >= 10 && basis <= 19) {
                            MV[sex - 1][allCancerGroupsButSkinIndex]++;
                        }
                    }
                    records++;
                    if (records % recordsPerFeedback == 0) {
                        System.out.println("Processing record number: " + records);
                    }
                }
                // Read next line
                line = readLine(dataFile);
            }
            System.out.println("Processed " + records + " records.");

        } catch (IOException e) {
            System.out.println("Data-file error.");
        }

        // Get our matrixes ready

        // Age standarized rate
        ASR = new double[numberOfSexes][numberOfCancerGroups];
        ASRbyAgeGroup = new double[numberOfSexes][numberOfCancerGroups][
                        numberOfAgeGroups];
        ASRluL = new double[numberOfSexes][numberOfCancerGroups][2];
        variL = new double[numberOfSexes][numberOfCancerGroups];
        variLbyAgeGroup = new double[numberOfSexes][numberOfCancerGroups][
                          numberOfAgeGroups];

        // Total casesPerHundredThousand
        CA = new double[numberOfSexes][numberOfCancerGroups];
        // Crude rate
        CR = new double[numberOfSexes][numberOfCancerGroups];

        for (int sex = 0; sex < 2; sex++) {

            // The age groups
            ageLabel[lowestPopulationAgeGroup] = "0-";

            for (int icdGroup = 0; icdGroup < numberOfCancerGroups; icdGroup++) {
                if (icdLabel[icdGroup].substring(0 + sex, 1 + sex).
                    equalsIgnoreCase("1")) {
                    // The age groups

                    double previousAgeGroupCases = 0;
                    double previousAgeGroupPopulation = 0;
                    double previousAgeGroupWstdPopulation = 0;

                    double lastAgeGroupCases = 0;
                    double lastAgeGroupPopulation = 0;
                    double lastAgeGroupWstdPopulation = 0;

                    for (int ageGroup = 1; ageGroup < unknownAgeGroupIndex;
                                        ageGroup++) {
                        if (ageGroup == 1) {
                            for (int ag = lowestIncidenceAgeGroup;
                                          ag < ageGroup;
                                          ag++) {
                                previousAgeGroupCases += casesArray[icdGroup][
                                        sex][
                                        ag];
                                previousAgeGroupPopulation += populationArray[
                                        sex][
                                        ag];
                                previousAgeGroupWstdPopulation += wstdPop[ag];
                            }
                        }
                        if (foundAgeGroups[ageGroup] &&
                            ageGroup < highestPopulationAgeGroup) {
                            casesPerHundredThousand[sex][ageGroup][icdGroup] =
                                    100000 *
                                    (casesArray[icdGroup][sex][ageGroup] +
                                     previousAgeGroupCases) /
                                    (populationArray[sex][ageGroup] +
                                     previousAgeGroupPopulation);

                            previousAgeGroupCases = 0;
                            previousAgeGroupPopulation = 0;
                            previousAgeGroupWstdPopulation = 0;

                        } else {
                            previousAgeGroupCases += casesArray[icdGroup][sex][
                                    ageGroup];
                            previousAgeGroupPopulation += populationArray[sex][
                                    ageGroup];
                            previousAgeGroupWstdPopulation += wstdPop[ageGroup];
                        }
                    }
                    // We calculate the "leftovers" from the last age group
                    if (previousAgeGroupPopulation > 0) {
                        casesPerHundredThousand[sex][highestPopulationAgeGroup][
                                icdGroup] = 100000 *
                                            (previousAgeGroupCases) /
                                            (previousAgeGroupPopulation);

                    }

                    previousAgeGroupCases = 0;
                    previousAgeGroupPopulation = 0;
                    previousAgeGroupWstdPopulation = 0;

                }
            }
        }


        // ASR, vASR, MV, MI, DCO
        for (int sex = 0; sex < numberOfSexes; sex++) {
            for (int icdGroup = 0; icdGroup < numberOfCancerGroups; icdGroup++) {

                double previousAgeGroupCases = 0;
                double previousAgeGroupPopulation = 0;
                double previousAgeGroupWstdPopulation = 0;

                double lastAgeGroupCases = 0;
                double lastAgeGroupPopulation = 0;
                double lastAgeGroupWstdPopulation = 0;

                CA[sex][icdGroup] += casesArray[icdGroup][sex][0];

                for (int ageGroup = 1; ageGroup < unknownAgeGroupIndex;
                                    ageGroup++) {
                    if (ageGroup == 1) {
                        for (int ag = lowestIncidenceAgeGroup; ag < ageGroup;
                                      ag++) {
                            previousAgeGroupCases += casesArray[icdGroup][sex][
                                    ag];
                            previousAgeGroupPopulation += populationArray[sex][
                                    ag];
                            previousAgeGroupWstdPopulation += wstdPop[ag];
                        }
                    }
                    if (foundAgeGroups[ageGroup] &&
                        ageGroup < highestPopulationAgeGroup &&
                        (previousAgeGroupPopulation +
                         populationArray[sex][ageGroup] > 0)) {
                        double asr = calculateASR((previousAgeGroupCases +
                                casesArray[icdGroup][sex][ageGroup]),
                                                  (previousAgeGroupPopulation +
                                populationArray[sex][ageGroup]),
                                                  (
                                previousAgeGroupWstdPopulation +
                                wstdPop[ageGroup]));

                        ASR[sex][icdGroup] += asr;

                        ASRbyAgeGroup[sex][icdGroup][ageGroup] = asr;

                        /* We don't use confidence intervals so this was removed 16.07.07
                        double varil =
                                calculateVariL((previousAgeGroupCases +
                                                casesArray[icdGroup][sex][
                                                ageGroup]),
                                               (previousAgeGroupWstdPopulation +
                                                wstdPop[ageGroup]),
                                                  (previousAgeGroupPopulation +
                                populationArray[sex][ageGroup])
);

                        variL[sex][icdGroup] += varil;
                        variLbyAgeGroup[sex][icdGroup][ageGroup] = varil;
                        */

                        previousAgeGroupCases = 0;
                        previousAgeGroupPopulation = 0;
                        previousAgeGroupWstdPopulation = 0;

                    } else if (ageGroup < highestPopulationAgeGroup) {
                        previousAgeGroupCases += casesArray[icdGroup][sex][
                                ageGroup];
                        previousAgeGroupPopulation += populationArray[sex][
                                ageGroup];
                        previousAgeGroupWstdPopulation += wstdPop[ageGroup];

                    } else {
                        lastAgeGroupCases += casesArray[icdGroup][sex][
                                ageGroup];
                        lastAgeGroupPopulation += populationArray[sex][
                                ageGroup];
                        lastAgeGroupWstdPopulation += wstdPop[ageGroup];
                    }

                    CA[sex][icdGroup] += casesArray[icdGroup][sex][ageGroup];
                }

                // We calculate the "leftovers" from the last age group
                if (lastAgeGroupPopulation > 0) {
                    double asr = calculateASR(lastAgeGroupCases,
                                              lastAgeGroupPopulation,
                                              lastAgeGroupWstdPopulation);
                    ASR[sex][icdGroup] += asr;

                    ASRbyAgeGroup[sex][icdGroup][highestPopulationAgeGroup] =
                            asr;
                            /* We don't use confidence intervals so this was removed 16.07.07
                    double varil = calculateVariL(lastAgeGroupCases,
                                                  lastAgeGroupWstdPopulation, lastAgeGroupPopulation);

                    variL[sex][icdGroup] += varil;

                    variLbyAgeGroup[sex][icdGroup][highestPopulationAgeGroup] =
                            varil;
                    */

                }

                // and take the unknown age group into account
                CA[sex][icdGroup] += casesArray[icdGroup][sex][
                        unknownAgeGroupIndex];

                if (CA[sex][icdGroup] > 0) {

                    DCO[sex][icdGroup] = 100 * (int) DCO[sex][icdGroup] /
                                         CA[sex][icdGroup];
                    MV[sex][icdGroup] = 100 * (int) MV[sex][icdGroup] /
                                        CA[sex][icdGroup];
                    CR[sex][icdGroup] = CA[sex][icdGroup] *
                                        wstdPop[allAgeGroupsIndex] /
                                        (populationArray[sex][allAgeGroupsIndex]);

                   /* We don't use confidence intervals so this was removed 16.07.07
                    double[] asrlul = calculateASRluL(ASR[sex][icdGroup],
                            variL[sex][icdGroup], wstdPop[allAgeGroupsIndex]);

                    ASRluL[sex][icdGroup][0] = asrlul[0];
                    ASRluL[sex][icdGroup][1] = asrlul[1];
                    */


                    // Cum. Rates
                    if (highestPopulationAgeGroup > 13) {
                        for (int k = 1; k <= 13; k++)
                            cumRate64[sex][icdGroup] += casesPerHundredThousand[sex][k][icdGroup]*cumPop18[k]/1000.0;
                    }
                    if (highestPopulationAgeGroup > 15) {
                    for (int k = 1; k <= 15; k++)
                            cumRate74[sex][icdGroup] += casesPerHundredThousand[sex][k][icdGroup]*cumPop18[k]/1000.0;
                    }

                    // adjust the ASR and cum rates for unknown ages
                    if (ASR[sex][icdGroup] > 0){
                        double ratio = CA[sex][icdGroup] /
                                              (CA[sex][icdGroup] -
                                               casesArray[icdGroup][sex][unknownAgeGroupIndex]);
                        ASR[sex][icdGroup] *= ratio;
                        cumRate64[sex][icdGroup] *= ratio;
                        cumRate74[sex][icdGroup] *= ratio;

                    }
/*                    if (!isSpecialized) {
                            cumRate64[sex][allCancerGroupsIndex] += cumRate64[sex][icdGroup];
                            cumRate74[sex][allCancerGroupsIndex] += cumRate74[sex][icdGroup];
                            if (icdGroup!=skinCancerGroupIndex) {
                                cumRate64[sex][allCancerGroupsIndex] += cumRate64[sex][icdGroup];
                                cumRate74[sex][allCancerGroupsIndex] += cumRate74[sex][icdGroup];
                            }
                        }
                      */
                }
            }
        }

        // Get our matrixes ready

        ASRf = new char[numberOfSexes][numberOfCancerGroups];

        // Writing
        System.out.println("Writing data...\n");

        PrintStream reportStream = new PrintStream(System.out);
        File reportFile;

        if (reportToFile) {
            String reportFileName = tablesPath +
                                    (continentLabels[continentNumber - 1]) +
                                    "/" + registryNumber +
                                    "AS.tab";
            try {
                reportFile = new File(reportFileName);
                reportStream = new PrintStream(reportFile);
                System.out.println("Writing to " + reportFileName);
            } catch (Exception ioe) {
                System.out.println("Error in reportfile...\n");
                reportStream = new PrintStream(System.out);
            }
            ;
        }

        // Adjust the age labels
        ageLabel[1] = "0-";
        ageLabel[highestPopulationAgeGroup] = ageLabel[
                                              highestPopulationAgeGroup].
                                              substring(0,
                ageLabel[highestPopulationAgeGroup].length() - 1) + "+";

        // Write it out
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        // Make PS-file

        for (int sex = 0; sex < numberOfSexes - 1; sex++) {
            String psFileName = tablesPath +

                                "Final/Tables/" + registryNumber +
                                "AS" + (sex+1) + ".ps";
            try {
                FileWriter fw = new FileWriter(psFileName);
                nf.setMaximumFractionDigits(1);
                nf.setMinimumFractionDigits(1);

                fw.write("/RLT {rlineto} def\n");
                fw.write("/LT {lineto} def\n");
                fw.write("/MT {moveto} def\n");
                fw.write("/SCF {scalefont} def\n");
                fw.write("/SF {setfont} def\n");
                fw.write("/SG {setgray} def\n");
                fw.write("/FF {findfont} def\n");
                fw.write("/SLW {setlinewidth} def\n");
                fw.write("/CP {closepath} def\n");
                fw.write("/Mainfont\n");
                fw.write("/Helvetica-Bold FF "+(int)(tableFontSize*2-3)+" SCF def\n");
                fw.write("/Titlefont\n");
                fw.write("/Helvetica FF "+tableFontSize+" SCF def\n");
                fw.write("/Tablefont\n");
                fw.write("/"+font+" FF "+tableFontSize+" SCF def\n");
                fw.write("/ASRfont\n");
                fw.write("/"+font+"-Bold FF "+tableFontSize+" SCF def\n");
                fw.write("/ICDfont\n");
                fw.write("/"+font+"-Italic FF "+tableFontSize+" SCF def\n");
                fw.write("/ASRitalicsfont\n");
                fw.write("/"+font+"-Italic-Bold FF "+tableFontSize+" SCF def\n");
                fw.write("/col 735 def\n");
                fw.write(
                        "/RS {dup stringwidth pop col exch sub 0 rmoveto show} def\n");
                fw.write(
                        "/CS {dup stringwidth pop 810 exch sub 2 div 0 rmoveto show} def\n");
                fw.write("/nstr 1 string def\n");
                fw.write("/prtchar {nstr 0 3 -1 roll put nstr show} def\n");
                fw.write("newpath\n");
                fw.write("90 rotate -20 -570 translate\n"); //  Landscape
                fw.write("Mainfont SF\n");
                fw.write("0 535 MT (" + registryLabel + ") CS\n");
                fw.write("Titlefont SF\n");
                //fw.write("0 525 MT (" + tableLabel[sex + 1] + ") CS\n");
                fw.write("0 513 MT (" + tableLabel[0]
                         + " - " + sexLabel[sex]
                         + ") CS\n");
//                                                                                              draw the grey frame
                fw.write("0.85 SG 27 510 translate\n");
                fw.write("0 -5 MT 785 -5 LT 785 -27 LT 0 -27 LT  CP fill\n");
                fw.write("0 -510 translate 0.95 SG\n");
                double k = 475;

                for (int icd = 0; icd < numberOfCancerGroups; icd++) {
                    if ((icd+1)<numberOfCancerGroups && icdLabel[icd+1].charAt(sex) == '1') {
                        int lines = (isLineBreak(icd));
                        if (lines > 0) {
                            k -= 2;
                            fw.write(
                                    "0 "+(k-2)+" MT 785 "+(k-2)+" LT 785 "+(k - 2 - (lines * (tableFontSize)))+" LT 0 "+(k - 2 - (lines * (tableFontSize)))+" LT CP fill\n");
                        } else if (lines<0)

                            k -= 2;
                        k -= tableFontSize;
                    }
                }

    /*
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {

                        int lines = (isLineBreak(j));
                        if (lines > 0) {
                            k -= 2;

                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - lines * tableFontSize) + " LT 0 " + (k - lines * tableFontSize) +
                                    " LT CP fill\n");

                        } else if (lines > 0)
                            k -= 2;
                        k -= lines * tableFontSize;




                        if (IsLineBreak(j)) {
                            k -= 2;
                        }
                        //  draw the grey frames
                        if (j == 8) {
                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - 35) + " LT 0 " + (k - 35) +
                                    " LT CP fill\n");
                        } else if (j == 34) {
                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - 26) + " LT 0 " + (k - 26) +
                                    " LT CP fill\n");
                        } else if (j == 16 || j == 22 || j == 40) {
                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - 18) + " LT 0 " + (k - 18) +
                                    " LT CP fill\n");
                        } else if (j == 27) {
                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - 42) + " LT 0 " + (k - 42) +
                                    " LT CP fill\n");
                        } else if (j == 47) {
                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - 34) + " LT 0 " + (k - 34) +
                                    " LT CP fill\n");
                        } else if (j == 53) {
                            fw.write(
                                    "0 " + (k + tableFontSize) + " MT 774 " + (k + tableFontSize) +
                                    " LT 774 " + (k - 12) + " LT 0 " + (k - 12) +
                                    " LT CP fill\n");
                        }
                        k -= (tableFontSize);
                    }

                }
    */
                fw.write("0 SG\n");

                fw.write("ICDfont SF\n");
                fw.write(" 740 496 MT (ICD) show\n");
                fw.write(" 740 487 MT ((10th)) show\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j==skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex || j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else fw.write("ICDfont SF\n");

                        fw.write("745 " + k + " MT (" +
                                 icd10GroupDescriptions[j] + ") show\n");
                        k -= (tableFontSize);
                    }
                }



                fw.write("/col col 0 sub def\n");
                fw.write("ASRfont SF\n");
                fw.write("0 496 MT (ASR) RS\n");
                fw.write("0 487 MT ((W)) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j==skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex || j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ASRitalicsfont SF\n");
                        } else fw.write("ASRfont SF\n");

                        fw.write("0 " + k + " MT (" + formatNumber(ASR[sex][j]) +
                                 ") RS\n");
                        k -= (tableFontSize);
                    }
                }

                fw.write("/col col 20 sub def\n");
                fw.write("Tablefont SF\n");
                fw.write("0 496 MT (CUM) RS\n");
                fw.write("0 487 MT (0-74) RS\n");
                k = 475;
                if (cumRate74[sex][allCancerGroupsIndex] > 0)
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1) != 0) {
                            k -= 2;
                        }
                        if (j == skinCancerGroupIndex ||
                            j == ovaryCancerGroupIndex ||
                            j == bladderCancerGroupIndex ||
                            j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex ||
                            j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else
                            fw.write("Tablefont SF\n");

                        fw.write("0 " + k + " MT (" + formatNumber(cumRate74[sex][j],2) +
                                 ") RS\n");
                        k -= (tableFontSize);
                    }
                }


                fw.write("/col col 20 sub def\n");
                fw.write("Tablefont SF\n");
                fw.write("0 496 MT (CUM) RS\n");
                fw.write("0 487 MT (0-64) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j==skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex || j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else fw.write("Tablefont SF\n");

                        fw.write("0 " + k + " MT (" + formatNumber(cumRate64[sex][j],2) +
                                 ") RS\n");
                        k -= (tableFontSize);
                    }
                }
                /* No MVs shown
                fw.write("Tablefont SF\n");
                fw.write("/col col 20 sub def\n");
                fw.write("0 496 MT (MV) RS\n");
                fw.write("0 487 MT ((%)) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }

                        if (j==skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex || j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else fw.write("Tablefont SF\n");

                        if (CA[sex][j] >= 0) {
                            fw.write("0 " + k + " MT (" +
                                     formatNumber(MV[sex][j]) + ") RS\n");
                        } else {
                            fw.write("0 " + k + " MT (      -) RS\n");
                        }
                        k -= (tableFontSize);
                    }
                }
                */
                fw.write("/col col 20 sub def\n");
                fw.write("0 491 MT ((%)) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }

                        if (j == skinCancerGroupIndex ||
                            j == ovaryCancerGroupIndex ||
                            j == bladderCancerGroupIndex ||
                            j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex ||
                            j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else
                            fw.write("Tablefont SF\n");

                        if (j != allCancerGroupsIndex && allCancerGroupsButSkinIndex>=0)
                        fw.write("0 " + k + " MT (" +
                                 formatNumber(100 * CA[sex][j] /
                                              CA[sex][
                                              allCancerGroupsButSkinIndex]) +
                                 ") RS\n"
                                );
                        k -= (tableFontSize);
                    }
                }
                fw.write("/col col 20 sub def\n");
                fw.write("0 496 MT (CRUDE) RS\n");
                fw.write("0 487 MT (RATE) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j == skinCancerGroupIndex ||
                            j == ovaryCancerGroupIndex ||
                            j == bladderCancerGroupIndex ||
                            j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex ||
                            j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else
                            fw.write("Tablefont SF\n");

                        fw.write("0 " + k + " MT (" + formatNumber(CR[sex][j]) +
                                 ") RS\n");
                        k -= (tableFontSize);
                    }
                }
                fw.write("/col 119 def\n");
                fw.write("0 496 MT (ALL) RS\n");
                fw.write("0 487 MT (AGES) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j == skinCancerGroupIndex ||
                            j == ovaryCancerGroupIndex ||
                            j == bladderCancerGroupIndex ||
                            j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex ||
                            j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else
                            fw.write("Tablefont SF\n");

                        fw.write("0 " + k + " MT (" +
                                 formatNumber(CA[sex][j], 0) + ") RS\n");
                        k -= (tableFontSize);
                    }
                }
                fw.write("/col col 20 add def\n");
                fw.write("0 496 MT (AGE) RS\n");
                fw.write("0 487 MT (UNK) RS\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j == skinCancerGroupIndex ||
                            j == ovaryCancerGroupIndex ||
                            j == bladderCancerGroupIndex ||
                            j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex ||
                            j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else
                            fw.write("Tablefont SF\n");

                        fw.write("0 " + k + " MT (" +
                                 formatNumber(casesArray[j][sex][
                                              unknownAgeGroupIndex], 0) +
                                 ") RS\n");
                        k -= (tableFontSize);
                    }
                }

                if (highestPopulationAgeGroup == numberOfAgeGroups - 4) {
                    fw.write("/col 145 def\n");
                } else if (highestPopulationAgeGroup == numberOfAgeGroups - 5) {
                    fw.write("/col 176 def\n");
                } else if (highestPopulationAgeGroup == numberOfAgeGroups - 6) {
                    fw.write("/col 208 def\n");
                } else {
                    fw.write("/col 145 def\n");
                }

                for (int age = 1; age <= highestPopulationAgeGroup; age++) {
                    fw.write("/col col 26 add def\n");
                    fw.write("0 491 MT (" + ageLabel[age] + ") RS\n");
                    // fw.write("/col col 5 sub def\n");
                    k = 475;
                    for (int j = 0; j < numberOfCancerGroups; j++) {
                        if (icdLabel[j].charAt(sex) == '1') {
                            if (isLineBreak(j - 1)!=0) {
                                k -= 2;
                            }

                            if (j==skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex ||
                                j ==
                                myeloproliferativeDisordersCancerGroupIndex ||
                                j ==
                                brainAndCentralNervousSystemCancerGroupIndex) {
                                fw.write("ICDfont SF\n");
                            } else
                                fw.write("Tablefont SF\n");


                            if (casesPerHundredThousand[sex][age][j] > 0) {
                                fw.write("0 " + k + " MT (" +
                                         formatNumber(casesPerHundredThousand[
                                        sex][age][j]) + ") RS\n");
                            } else {
                                fw.write("0 " + k + " MT (    -  ) RS\n");
                            }
                            k -= (tableFontSize);
                        }
                    }
                }
                fw.write("3 492 MT ( S I T E) show\n");
                k = 475;
                for (int j = 0; j < numberOfCancerGroups; j++) {
                    if (icdLabel[j].charAt(sex) == '1') {
                        if (isLineBreak(j - 1)!=0) {
                            k -= 2;
                        }
                        if (j == skinCancerGroupIndex ||
                            j == ovaryCancerGroupIndex ||
                            j == bladderCancerGroupIndex ||
                            j == myelodysplasticSyndromesCancerGroupIndex ||
                            j == myeloproliferativeDisordersCancerGroupIndex ||
                            j == brainAndCentralNervousSystemCancerGroupIndex) {
                            fw.write("ICDfont SF\n");
                        } else
                            fw.write("Tablefont SF\n");

                        fw.write("3 " + k + " MT (" + icdLabel[j].substring(3) +
                                 ") show\n");
                        k -= (tableFontSize);
                    }
                }
                if (showSeeNotesNote) {
                    fw.write("3 0 MT ("+ notesString +") show\n");
                }

                // Write the footer
                fw.write("0 0 MT ("+ footerString +") CS\n");

                fw.write("showpage\n");
                System.out.println("Wrote " + psFileName + ".");
                fw.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }

        System.out.println("Fini!");
    }

    private static void help() {
        System.out.println("Usage:");
        System.out.println(
                "java AgeSpecificFinal <registry number> [ -cf <configFile>] [ -fdf <fieldDescriptionFile>] [ -pfdf <populationFile>] [ -pfd <populationFileDescription>]");
        System.out.println(
                "Where <fieldDescriptionFile> is in STATA's dct-format describing the data file.");
        System.out.println(
                "<configFile> contains a list of configurations (see examples).");
        System.out.println(
                "<populationFile> contains statistics on the population.");
        System.out.println(
                "<populationFileDescriptionFileName> is in STATA's dct-format describing the populationfile.");
    }

    public static void main(String[] args) {

        System.out.println("Age Specific Final generator");
        boolean stop = false;
        if ((args.length < 1) || (args[0].equals("--help")) ||
            (args[0].equals("-h"))) {
            help();
        } else {
            String registryNumber = args[0];

            // Set defaults
            String incidenceFileName = null;
            String populationFileName = null;
            String mortalityFileName = null;
            String configFileName = null;

            String incidenceFileDescriptionFileName =
                    "lib/ci5-ix-incidence.dct";
            String populationFileDescriptionFileName =
                    "lib/ci5-ix-population.dct";
            String mortalityFileDescriptionFileName =
                    "lib/ci5-ix-mortality.dct";
            String populationCodeString = "00";

            if (registryNumber != null) {
                try {
                    int continentNumber = Integer.parseInt(registryNumber.
                            substring(0, 1));
                    try {
                        configFileName = "lib/ASfinal.conf";
                        // For later:
                        //                 continentLabels[Integer.
                        //                 parseInt(args[0].substring(0, 1)) - 1]

                        incidenceFileName = "converted/" +
                                            continentLabels[Integer.
                                            parseInt(args[0].substring(0, 1)) -
                                            1] +
                                            "/" + args[0] + ".1";
                        populationFileName = "converted/" +
                                             continentLabels[Integer.
                                             parseInt(args[0].substring(0, 1)) -
                                             1] +
                                             "/" + args[0] + ".2";
                        mortalityFileName = "converted/" +
                                            continentLabels[Integer.
                                            parseInt(args[0].substring(0, 1)) -
                                            1] +
                                            "/" + args[0] + ".3";

                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(
                                "Faulty registrynumber -- first digit too high.");
                        stop = true;
                    }
                } catch (NumberFormatException ne) {
                    System.out.println(
                            "Faulty registrynumber -- first digit not a number.");
                    stop = true;
                }
            } else {
                System.out.println(
                        "No population file or registry number specified...");
                stop = true;
            }

            for (int n = 1; n < args.length; n = n + 2) {
                try {
                    if (args[n].equals("-cf")) {
                        configFileName = args[n + 1];
                    } else if (args[n].equals("-fdf")) {
                        incidenceFileDescriptionFileName = args[n + 1];
                    } else if (args[n].equals("-pf")) {
                        populationFileName = args[n + 1];
                    } else if (args[n].equals("-pfdf")) {
                        populationFileDescriptionFileName = args[n + 1];
                    } else if (args[n].equals("-pc")) {
                        populationCodeString = args[n + 1];
                    } else {
                        help();
                        stop = true;
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    help();
                    stop = true;
                    break;
                }
            }

            if (!stop) {
                AgeSpecificFinal AgeSpecificFinal = new AgeSpecificFinal(
                        registryNumber, configFileName,
                        incidenceFileName, incidenceFileDescriptionFileName,
                        populationFileName, populationFileDescriptionFileName,
                        mortalityFileName, mortalityFileDescriptionFileName,
                        populationCodeString);
            }
        }
    }
}
