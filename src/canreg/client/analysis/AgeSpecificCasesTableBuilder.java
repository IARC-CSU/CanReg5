/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017  International Agency for Research on Cancer
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
 * @author Jacques Ferlay, CSU/IARC
 */
package canreg.client.analysis;


import static canreg.client.analysis.AbstractEditorialTableBuilder.highestPopulationAgeGroup;
import canreg.common.Globals;
import canreg.common.PsToPdfConverter;
import canreg.common.database.AgeGroupStructure;
import canreg.common.database.PopulationDataset;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.io.IOException;
import java.text.NumberFormat;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class AgeSpecificCasesTableBuilder extends AbstractEditorialTableBuilder {

    private static final Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.BasisDiagnosis};
    private static final int YEAR_COLUMN = 0;
    private static final int SEX_COLUMN = 1;
    private static final int AGE_COLUMN = 2;
    private static final int ICD10_COLUMN = 3;
    //   private static final int MORPHOLOGY_COLUMN = 4;
    //   private static final int BEHAVIOUR_COLUMN = 5;
    private static final int BASIS_DIAGNOSIS_COLUMN = 6;
    private static final int CASES_COLUMN = 7;
    private double[][] standardPopulationArray;
    private String populationString;
    private final int DONT_COUNT = -999;
    private double[][][] casesArray;
    private double[][] populationArray;

    public AgeSpecificCasesTableBuilder() {
        super();
    }

    @Override
    public LinkedList<String> buildTable(String registryLabel,
            String reportFileName,
            int startYear,
            int endYear,
            Object[][] incidenceData,
            PopulationDataset[] populations,
            PopulationDataset[] standardPopulations,
            LinkedList<ConfigFields> configList,
            String[] engineParameters,
            FileTypes fileType, 
            String language) throws NotCompatibleDataException {

        LinkedList<String> generatedFiles = new LinkedList<String>();

        String footerString = java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString("TABLE BUILT ") + new Date() + java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString(" BY CANREG5.");

        String notesString = "";

        if (populations[0].getFilter().length() > 0) {
            notesString = java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString("FILTER USED:") + " " + populations[0].getFilter();
        }

        double tableFontSize = 7.5;
        String font = "Times";

        int[] years = {startYear, endYear};

//      double RegPop[][];
        double totalCases[][];

        String sexLabel[] = null;
        String tableLabel[] = null;
        String icdLabel[] = null;

        LinkedList cancerGroupsLocal[] = null;

        boolean showSeeNotesNote = true;

        char Childc[][] = new char[2][3];

        double casesPerHundredThousand[][][];

        double cumRate64[][];
        double cumRate74[][];

        tableLabel = ConfigFieldsReader.findConfig("table_label",
                configList);
        // sexLabel = ConfigFieldsReader.findConfig("sex_label", configList);
        sexLabel = new String[]{java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("MALE"), java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("FEMALE")};

        icdLabel = ConfigFieldsReader.findConfig("ICD_groups_labels",
                configList);
        icd10GroupDescriptions = ConfigFieldsReader.findConfig(
                "ICD10_groups",
                configList);

        cancerGroupsLocal = EditorialTableTools.generateICD10Groups(icd10GroupDescriptions);

        allCancerGroupsIndex = EditorialTableTools.getICD10index("ALL", icd10GroupDescriptions);

        leukemiaNOSCancerGroupIndex = EditorialTableTools.getICD10index(950,
                cancerGroupsLocal);

        skinCancerGroupIndex = EditorialTableTools.getICD10index("C44",
                icd10GroupDescriptions);

        bladderCancerGroupIndex = EditorialTableTools.getICD10index("C67",
                icd10GroupDescriptions);

        mesotheliomaCancerGroupIndex = EditorialTableTools.getICD10index("C45",
                icd10GroupDescriptions);

        kaposiSarkomaCancerGroupIndex = EditorialTableTools.getICD10index("C46",
                icd10GroupDescriptions);

        myeloproliferativeDisordersCancerGroupIndex = EditorialTableTools.getICD10index("MPD",
                icd10GroupDescriptions);

        myelodysplasticSyndromesCancerGroupIndex = EditorialTableTools.getICD10index("MDS",
                icd10GroupDescriptions);

        allCancerGroupsButSkinIndex = EditorialTableTools.getICD10index("ALLbC44",
                icd10GroupDescriptions);

        leukemiaNOSCancerGroupIndex = EditorialTableTools.getICD10index(950,
                cancerGroupsLocal);

        brainAndCentralNervousSystemCancerGroupIndex = EditorialTableTools.getICD10index("C70-72",
                icd10GroupDescriptions);

        ovaryCancerGroupIndex = EditorialTableTools.getICD10index(569,
                cancerGroupsLocal);

        otherCancerGroupsIndex = EditorialTableTools.getICD10index("O&U", icd10GroupDescriptions);

        numberOfCancerGroups = cancerGroupsLocal.length;

        lineBreaks = parseLineBreaks(ConfigFieldsReader.findConfig("line_breaks", configList));

        numberOfYears = years[1] - years[0] + 1;

        minimumCasesLimit = minimumCasesPerYearLimit * numberOfYears;

        noOldData = true;

        casesPerHundredThousand = new double[numberOfSexes][numberOfAgeGroups][numberOfCancerGroups];

        casesArray = new double[numberOfCancerGroups][numberOfSexes][numberOfAgeGroups];

        // cumRate64 = new double[numberOfSexes][numberOfCancerGroups];
        // cumRate74 = new double[numberOfSexes][numberOfCancerGroups];
        populationArray = new double[numberOfSexes][numberOfAgeGroups];
        foundAgeGroups = new boolean[numberOfAgeGroups];

        if (areThesePopulationDatasetsCompatible(populations)) {
            for (PopulationDataset population : populations) {
                population.addPopulationDataToArrayForTableBuilder(populationArray, foundAgeGroups, new AgeGroupStructure(5, 85, 1));
            }
        } else {
            throw new NotCompatibleDataException();
        }

        populationString = populations[0].getPopulationDatasetName();

        int lastCommaPlace = populationString.lastIndexOf(",");

        if (lastCommaPlace != -1) {
            populationString = populationString.substring(0, lastCommaPlace);
        }

        if (populations[0].getFilter().length() > 0) {
            notesString = java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString("FILTER USED:") + populations[0].getFilter();
        }

        standardPopulationArray = new double[numberOfSexes][numberOfAgeGroups];

        for (PopulationDataset stdPopulation : standardPopulations) {
            stdPopulation.addPopulationDataToArrayForTableBuilder(standardPopulationArray, null, new AgeGroupStructure(5, 85, 1));
        }

        // standardize population array
        for (int sexNumber = 0; sexNumber < numberOfSexes; sexNumber++) {
            for (int ageGroupNumber = 0; ageGroupNumber < numberOfAgeGroups; ageGroupNumber++) {
                standardPopulationArray[sexNumber][ageGroupNumber] = (standardPopulationArray[sexNumber][ageGroupNumber] / standardPopulationArray[sexNumber][numberOfAgeGroups - 1]) * 100000;
            }
        }

        highestPopulationAgeGroup = findHighestAgeGroup(foundAgeGroups);
        lowestPopulationAgeGroup = findLowestAgeGroup(foundAgeGroups);

        int records = 0;
        // generate statistics

        String sexString;
        String icdString;
        String yearString;
        String ageString;
        String basisString;

        int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, ageInt, basis, cases;

        if (incidenceData != null) {
            for (Object[] line : incidenceData) {
                try {
                    // Set default
                    icdIndex = -1;
                    cases = 0;

                    // Unknown sex group = 3
                    sex = 3;
                    // Extract data
                    sexString = (String) line[SEX_COLUMN];
                    sex = Integer.parseInt(sexString.trim());

                    // sex = 3 is unknown sex
                    if (sex > 2) {
                        sex = 3;
                    }

                    // morphologyString = (String) line[MORPHOLOGY_COLUMN];

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
                        icdString = (String) line[ICD10_COLUMN];
                        if (icdString.length() > 0
                                && icdString.trim().substring(0, 1).equals("C")) {
                            icdString = icdString.trim().substring(1);
                            icdNumber = Integer.parseInt(icdString);
                            if (icdString.length() < 3) {
                                icdNumber = icdNumber * 10;
                            }
                            icdIndex = EditorialTableTools.getICD10index(icdNumber, cancerGroupsLocal);
                            if (icdIndex == -1) {
                                icdIndex = -1;
                            }
                        } else if (icdString.length() > 0
                                && icdString.trim().substring(0, 1).equals("D")) {
                            icdIndex = DONT_COUNT; // set don't count as default
                            icdString = icdString.trim().substring(1);
                            icdNumber = Integer.parseInt(icdString);
                            if (icdString.length() < 3) {
                                icdNumber = icdNumber * 10;
                            }
                            if (icdNumber == 90 || icdNumber == 414) {
                                icdIndex = bladderCancerGroupIndex;
                            } else if (((int) (icdNumber / 10)) == 45 || ((int) (icdNumber / 10)) == 47) {
                                icdIndex = myeloproliferativeDisordersCancerGroupIndex;
                            } else if (((int) (icdNumber / 10)) == 46) {
                                icdIndex = myelodysplasticSyndromesCancerGroupIndex;
                            }
                        }
                    }

                    yearString = line[YEAR_COLUMN].toString();
                    year = Integer.parseInt(yearString);
                    yearIndex = year - years[0];
                    ageString = line[AGE_COLUMN].toString();
                    ageInt = Integer.parseInt(ageString);

                    if (ageInt == unknownAgeInt) {
                        ageGroup = unknownAgeGroupIndex;
                    } else {
                        ageGroup = populations[yearIndex].getAgeGroupIndex(ageInt);
                        // Adjust age group
                        if (populations[yearIndex].getAgeGroupStructure().getSizeOfFirstGroup() != 1) {
                            ageGroup += 1;
                        }
                    }

                    // Extract cases
                    cases = (Integer) line[CASES_COLUMN];

                    if (icdIndex != DONT_COUNT && year <= years[1] && year >= years[0]) {

                        // Basis of diagnosis
                        basisString = line[BASIS_DIAGNOSIS_COLUMN].toString();
                        if (basisString != null) {
                            basis = Integer.parseInt(basisString.trim());
                        } else {
                            basis = -1;
                        }

                        if (sex <= numberOfSexes && icdIndex >= 0
                                && icdIndex <= cancerGroupsLocal.length) {

                            casesArray[icdIndex][sex - 1][ageGroup] += cases;

                        } else if (otherCancerGroupsIndex >= 0) {
                            casesArray[otherCancerGroupsIndex][sex
                                    - 1][ageGroup] += cases;
                        }
                        if (allCancerGroupsIndex >= 0) {
                            casesArray[allCancerGroupsIndex][sex - 1][ageGroup] += cases;
                        }
                        if (allCancerGroupsButSkinIndex >= 0
                                && skinCancerGroupIndex >= 0
                                && icdIndex != skinCancerGroupIndex) {
                            casesArray[allCancerGroupsButSkinIndex][sex
                                    - 1][ageGroup] += cases;
                        }
                        records += cases;
                        if (records % recordsPerFeedback == 0) {
                            System.out.println(java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString("PROCESSING RECORD NUMBER: ") + records);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Logger.getLogger(AgeSpecificCasesTableBuilder.class.getName()).log(Level.WARNING, null, nfe);
                }
                // Read next line
            }
        }
        System.out.println(java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString("PROCESSED ") + records + java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesTableBuilder").getString(" RECORDS."));

        // Total casesPerHundredThousand
        totalCases = new double[numberOfSexes][numberOfCancerGroups];
        // Crude rate
        // crudeRate = new double[numberOfSexes][numberOfCancerGroups];

        for (int sexNumber = 0; sexNumber < 2; sexNumber++) {

            // The age groups
            ageLabel[lowestPopulationAgeGroup] = "0-";

            for (int icdGroup = 0; icdGroup < numberOfCancerGroups; icdGroup++) {
                if (icdLabel[icdGroup].substring(0 + sexNumber, 1 + sexNumber).
                        equalsIgnoreCase("1")) {
                    // The age groups

                    double previousAgeGroupCases = 0;
                    double previousAgeGroupPopulation = 0;
                    double previousAgeGroupWstdPopulation = 0;

                    double lastAgeGroupCases = 0;
                    double lastAgeGroupPopulation = 0;
                    double lastAgeGroupWstdPopulation = 0;

                    for (int ageGroupNumber = 1; ageGroupNumber < unknownAgeGroupIndex;
                            ageGroupNumber++) {
                        if (ageGroupNumber == 1) {
                            for (int ag = lowestIncidenceAgeGroup;
                                    ag < ageGroupNumber;
                                    ag++) {
                                previousAgeGroupCases += casesArray[icdGroup][sexNumber][ag];
                                previousAgeGroupPopulation += populationArray[sexNumber][ag];
                                previousAgeGroupWstdPopulation += standardPopulationArray[sexNumber][ag];
                            }
                        }
                        if (foundAgeGroups[ageGroupNumber]
                                && ageGroupNumber < highestPopulationAgeGroup) {
                            casesPerHundredThousand[sexNumber][ageGroupNumber][icdGroup]
                                    = 100000
                                    * (casesArray[icdGroup][sexNumber][ageGroupNumber]
                                    + previousAgeGroupCases)
                                    / (populationArray[sexNumber][ageGroupNumber]
                                    + previousAgeGroupPopulation);

                            previousAgeGroupCases = 0;
                            previousAgeGroupPopulation = 0;
                            previousAgeGroupWstdPopulation = 0;

                        } else {
                            previousAgeGroupCases += casesArray[icdGroup][sexNumber][ageGroupNumber];
                            previousAgeGroupPopulation += populationArray[sexNumber][ageGroupNumber];
                            previousAgeGroupWstdPopulation += standardPopulationArray[sexNumber][ageGroupNumber];
                        }
                    }
                    // We calculate the "leftovers" from the last age group
                    if (previousAgeGroupPopulation > 0) {
                        casesPerHundredThousand[sexNumber][highestPopulationAgeGroup][icdGroup] = 100000
                                * (previousAgeGroupCases)
                                / (previousAgeGroupPopulation);

                    }

                    previousAgeGroupCases = 0;
                    previousAgeGroupPopulation = 0;
                    previousAgeGroupWstdPopulation = 0;

                }
            }
        }

        // ASR, vASR, MV, MI, DCO
        for (int sexNumber = 0; sexNumber < numberOfSexes; sexNumber++) {
            for (int icdGroup = 0; icdGroup < numberOfCancerGroups; icdGroup++) {

                double previousAgeGroupCases = 0;
                double previousAgeGroupPopulation = 0;
                double previousAgeGroupWstdPopulation = 0;

                double lastAgeGroupCases = 0;
                double lastAgeGroupPopulation = 0;
                double lastAgeGroupWstdPopulation = 0;

                totalCases[sexNumber][icdGroup] += casesArray[icdGroup][sexNumber][0];

                for (int ageGroupNumber = 1; ageGroupNumber < unknownAgeGroupIndex;
                        ageGroupNumber++) {
                    if (ageGroupNumber == 1) {
                        for (int ag = lowestIncidenceAgeGroup; ag < ageGroupNumber;
                                ag++) {
                            previousAgeGroupCases += casesArray[icdGroup][sexNumber][ag];
                            previousAgeGroupPopulation += populationArray[sexNumber][ag];
                            previousAgeGroupWstdPopulation += standardPopulationArray[sexNumber][ag];
                        }
                    }
                    if (foundAgeGroups[ageGroupNumber]
                            && ageGroupNumber < highestPopulationAgeGroup
                            && (previousAgeGroupPopulation
                            + populationArray[sexNumber][ageGroupNumber] > 0)) {
                        double asr = calculateASR((previousAgeGroupCases
                                + casesArray[icdGroup][sexNumber][ageGroupNumber]),
                                (previousAgeGroupPopulation
                                + populationArray[sexNumber][ageGroupNumber]),
                                (previousAgeGroupWstdPopulation
                                + standardPopulationArray[sexNumber][ageGroupNumber]));

                        previousAgeGroupCases = 0;
                        previousAgeGroupPopulation = 0;
                        previousAgeGroupWstdPopulation = 0;

                    } else if (ageGroupNumber < highestPopulationAgeGroup) {
                        previousAgeGroupCases += casesArray[icdGroup][sexNumber][ageGroupNumber];
                        previousAgeGroupPopulation += populationArray[sexNumber][ageGroupNumber];
                        previousAgeGroupWstdPopulation += standardPopulationArray[sexNumber][ageGroupNumber];

                    } else {
                        lastAgeGroupCases += casesArray[icdGroup][sexNumber][ageGroupNumber];
                        lastAgeGroupPopulation += populationArray[sexNumber][ageGroupNumber];
                        lastAgeGroupWstdPopulation += standardPopulationArray[sexNumber][ageGroupNumber];
                    }

                    totalCases[sexNumber][icdGroup] += casesArray[icdGroup][sexNumber][ageGroupNumber];
                }

                // We calculate the "leftovers" from the last age group
                if (lastAgeGroupPopulation > 0) {
                    double asr = calculateASR(lastAgeGroupCases,
                            lastAgeGroupPopulation,
                            lastAgeGroupWstdPopulation);

                }

                // and take the unknown age group into account
                totalCases[sexNumber][icdGroup] += casesArray[icdGroup][sexNumber][unknownAgeGroupIndex];

                if (totalCases[sexNumber][icdGroup] > 0) {

                    /* We don't use confidence intervals so this was removed 16.07.07
                    double[] asrlul = calculateASRluL(ASR[sex][icdGroup],
                    variL[sex][icdGroup], wstdPop[allAgeGroupsIndex]);
                    
                    ASRluL[sex][icdGroup][0] = asrlul[0];
                    ASRluL[sex][icdGroup][1] = asrlul[1];
                     */
                    // Cum. Rates
                    if (highestPopulationAgeGroup > 13) {
                        for (int k = 1; k <= 13; k++) {
                            // cumRate64[sexNumber][icdGroup] += casesPerHundredThousand[sexNumber][k][icdGroup] * cumPop18[k] / 1000.0;
                        }
                    }
                    if (highestPopulationAgeGroup > 15) {
                        for (int k = 1; k <= 15; k++) {
                            // cumRate74[sexNumber][icdGroup] += casesPerHundredThousand[sexNumber][k][icdGroup] * cumPop18[k] / 1000.0;
                        }
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

        // Adjust the age labels
        ageLabel[1] = "0-";
        ageLabel[highestPopulationAgeGroup] = ageLabel[highestPopulationAgeGroup].substring(0,
                ageLabel[highestPopulationAgeGroup].length() - 1) + "+";

        // Write it out
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        Writer reportFileWriter;

        if (fileType.equals(FileTypes.csv)) {
            // write tab separated stuff here
            CSVPrinter csvOut;
            for (int sexNumber = 0; sexNumber < numberOfSexes - 1; sexNumber++) {
                try {
                    String tabReportFileName = "";
                    try {
                        tabReportFileName = reportFileName + sexLabel[sexNumber] + ".csv";
                        System.out.println(java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString("WRITING TO ") + tabReportFileName);
                        reportFileWriter = new OutputStreamWriter(new FileOutputStream(tabReportFileName), "UTF-8");
                    } catch (IOException ioe) {
                        System.out.println(java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString("ERROR IN REPORTFILE: ") + tabReportFileName);
                        reportFileWriter = new OutputStreamWriter(System.out);
                    }
                    // reportStream = new PrintStream(tabReportFileName);
                    // write the header line
                    // reportStream = new PrintStream(tabReportFileName);
                    // write the header line
                    LinkedList<String> headers = new LinkedList<String>();
                    headers.add("SITE");
                    headers.add("ALL AGES");
                    headers.add("AGE UNK");
                    // add age groups

                    for (int age = 1; age <= highestPopulationAgeGroup; age++) {
                        headers.add(ageLabel[age]);
                    }

                   // headers.add("CRUDE RATE");
                    headers.add("(%)");
//                    headers.add("CUM 0-64");
//                    headers.add("CUM 0-74");
//                    headers.add("ASR");
                    headers.add("ICD (10th)");

                    CSVFormat format = CSVFormat.DEFAULT
                            .withDelimiter(',')
                            .withHeader(headers.toArray(new String[0]));

                    csvOut = new CSVPrinter(reportFileWriter, format);

                    LinkedList<String> line = new LinkedList<String>();

                    // write the data
                    for (int j = 0; j < numberOfCancerGroups; j++) {
                        if (icdLabel[j].charAt(sexNumber) == '1') {
                            line.add(icdLabel[j].substring(3));
                            line.add(formatNumber(totalCases[sexNumber][j], 0));
                            line.add(formatNumber(casesArray[j][sexNumber][unknownAgeGroupIndex], 0));
                            for (int age = 1; age <= highestPopulationAgeGroup; age++) {
                                if (casesArray[j][sexNumber][age] > 0) {
                                    line.add(formatNumber(casesArray[j][sexNumber][age], 0));
                                } else {
                                    line.add("0");
                                }
                            }
                            // line.add(formatNumber(crudeRate[sexNumber][j], 2));
                            line.add(formatNumber(100 * totalCases[sexNumber][j] / totalCases[sexNumber][allCancerGroupsButSkinIndex]));
                            // line.add(formatNumber(cumRate64[sexNumber][j], 2));
                            // line.add(formatNumber(cumRate74[sexNumber][j], 2));
                            // line.add(formatNumber(ASR[sexNumber][j]));
                            line.add(icd10GroupDescriptions[j]);
                            csvOut.printRecord(line);
                            line.clear();
                        }
                    }

                    try {
                        csvOut.flush();
                        csvOut.close();
                    } catch (IOException ex) {
                        Logger.getLogger(AgeSpecificCasesPerHundredThousandTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    generatedFiles.add(tabReportFileName);
                } catch (IOException ex) {
                    Logger.getLogger(AgeSpecificCasesTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {

            // Make PS-file
            for (int sexNumber = 0; sexNumber < numberOfSexes - 1; sexNumber++) {
                String psFileName = reportFileName + "-" + sexLabel[sexNumber] + ".ps";
                generatedFiles.add(psFileName);
                try {
                    Writer fw = new OutputStreamWriter(new FileOutputStream(psFileName), "UTF-8");

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
                    fw.write("/Helvetica-Bold FF " + (int) (tableFontSize * 2 - 3) + " SCF def\n");
                    fw.write("/Titlefont\n");
                    fw.write("/Helvetica FF " + tableFontSize + " SCF def\n");
                    fw.write("/Tablefont\n");
                    fw.write("/" + font + " FF " + tableFontSize + " SCF def\n");
                    fw.write("/ASRfont\n");
                    fw.write("/" + font + "-Bold FF " + tableFontSize + " SCF def\n");
                    fw.write("/ICDfont\n");
                    fw.write("/" + font + "-Italic FF " + tableFontSize + " SCF def\n");
                    fw.write("/ASRitalicsfont\n");
                    fw.write("/" + font + "-Italic-Bold FF " + tableFontSize + " SCF def\n");
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
                    fw.write("0 525 MT (" + populationString + ") CS\n");
                    fw.write("0 513 MT (" + tableLabel[0] + " - " + sexLabel[sexNumber] + ") CS\n");
//                                                                                              draw the grey frame
                    fw.write("0.85 SG 27 510 translate\n");
                    fw.write("0 -5 MT 785 -5 LT 785 -27 LT 0 -27 LT  CP fill\n");
                    fw.write("0 -510 translate 0.95 SG\n");
                    double k = 475;

                    for (int icd = 0; icd < numberOfCancerGroups; icd++) {
                        if ((icd + 1) < numberOfCancerGroups && icdLabel[icd + 1].charAt(sexNumber) == '1') {
                            int lines = (isLineBreak(icd));
                            if (lines > 0) {
                                k -= 2;
                                fw.write(
                                        "0 " + (k - 2) + " MT 785 " + (k - 2) + " LT 785 " + (k - 2 - (lines * (tableFontSize))) + " LT 0 " + (k - 2 - (lines * (tableFontSize))) + " LT CP fill\n");
                            } else if (lines < 0) {
                                k -= 2;
                            }
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
                        if (icdLabel[j].charAt(sexNumber) == '1') {
                            if (isLineBreak(j - 1) != 0) {
                                k -= 2;
                            }
                            if (j == skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex
                                    || j == myeloproliferativeDisordersCancerGroupIndex || j == brainAndCentralNervousSystemCancerGroupIndex) {
                                fw.write("ICDfont SF\n");
                            } else {
                                fw.write("ICDfont SF\n");
                            }

                            fw.write("745 " + k + " MT ("
                                    + icd10GroupDescriptions[j] + ") show\n");
                            k -= (tableFontSize);
                        }
                    }

                    fw.write("/col col 20 sub def\n");
                    fw.write("0 491 MT ((%)) RS\n");
                    k = 475;
                    for (int j = 0; j < numberOfCancerGroups; j++) {
                        if (icdLabel[j].charAt(sexNumber) == '1') {
                            if (isLineBreak(j - 1) != 0) {
                                k -= 2;
                            }

                            if (j == skinCancerGroupIndex
                                    || j == ovaryCancerGroupIndex
                                    || j == bladderCancerGroupIndex
                                    || j == myelodysplasticSyndromesCancerGroupIndex
                                    || j == myeloproliferativeDisordersCancerGroupIndex
                                    || j == brainAndCentralNervousSystemCancerGroupIndex) {
                                fw.write("ICDfont SF\n");
                            } else {
                                fw.write("Tablefont SF\n");
                            }

                            if (j != allCancerGroupsIndex && allCancerGroupsButSkinIndex >= 0) {
                                fw.write("0 " + k + " MT ("
                                        + formatNumber(100 * totalCases[sexNumber][j]
                                                / totalCases[sexNumber][allCancerGroupsButSkinIndex])
                                        + ") RS\n");
                            }
                            k -= (tableFontSize);
                        }
                    }

                    fw.write("/col 119 def\n");
                    fw.write("0 496 MT (ALL) RS\n");
                    fw.write("0 487 MT (AGES) RS\n");
                    k = 475;
                    for (int j = 0; j < numberOfCancerGroups; j++) {
                        if (icdLabel[j].charAt(sexNumber) == '1') {
                            if (isLineBreak(j - 1) != 0) {
                                k -= 2;
                            }
                            if (j == skinCancerGroupIndex
                                    || j == ovaryCancerGroupIndex
                                    || j == bladderCancerGroupIndex
                                    || j == myelodysplasticSyndromesCancerGroupIndex
                                    || j == myeloproliferativeDisordersCancerGroupIndex
                                    || j == brainAndCentralNervousSystemCancerGroupIndex) {
                                fw.write("ICDfont SF\n");
                            } else {
                                fw.write("Tablefont SF\n");
                            }

                            fw.write("0 " + k + " MT ("
                                    + formatNumber(totalCases[sexNumber][j], 0) + ") RS\n");
                            k -= (tableFontSize);
                        }
                    }
                    fw.write("/col col 20 add def\n");
                    fw.write("0 496 MT (AGE) RS\n");
                    fw.write("0 487 MT (UNK) RS\n");
                    k = 475;
                    for (int j = 0; j < numberOfCancerGroups; j++) {
                        if (icdLabel[j].charAt(sexNumber) == '1') {
                            if (isLineBreak(j - 1) != 0) {
                                k -= 2;
                            }
                            if (j == skinCancerGroupIndex
                                    || j == ovaryCancerGroupIndex
                                    || j == bladderCancerGroupIndex
                                    || j == myelodysplasticSyndromesCancerGroupIndex
                                    || j == myeloproliferativeDisordersCancerGroupIndex
                                    || j == brainAndCentralNervousSystemCancerGroupIndex) {
                                fw.write("ICDfont SF\n");
                            } else {
                                fw.write("Tablefont SF\n");
                            }

                            fw.write("0 " + k + " MT ("
                                    + formatNumber(casesArray[j][sexNumber][unknownAgeGroupIndex], 0)
                                    + ") RS\n");
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
                            if (icdLabel[j].charAt(sexNumber) == '1') {
                                if (isLineBreak(j - 1) != 0) {
                                    k -= 2;
                                }

                                if (j == skinCancerGroupIndex || j == ovaryCancerGroupIndex || j == bladderCancerGroupIndex || j == myelodysplasticSyndromesCancerGroupIndex
                                        || j
                                        == myeloproliferativeDisordersCancerGroupIndex
                                        || j
                                        == brainAndCentralNervousSystemCancerGroupIndex) {
                                    fw.write("ICDfont SF\n");
                                } else {
                                    fw.write("Tablefont SF\n");
                                }

                                if (casesArray[j][sexNumber][age] > 0) {
                                    fw.write("0 " + k + " MT ("
                                            + formatNumber(casesArray[j][sexNumber][age], 0) + ") RS\n");
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
                        if (icdLabel[j].charAt(sexNumber) == '1') {
                            if (isLineBreak(j - 1) != 0) {
                                k -= 2;
                            }
                            if (j == skinCancerGroupIndex
                                    || j == ovaryCancerGroupIndex
                                    || j == bladderCancerGroupIndex
                                    || j == myelodysplasticSyndromesCancerGroupIndex
                                    || j == myeloproliferativeDisordersCancerGroupIndex
                                    || j == brainAndCentralNervousSystemCancerGroupIndex) {
                                fw.write("ICDfont SF\n");
                            } else {
                                fw.write("Tablefont SF\n");
                            }

                            fw.write("3 " + k + " MT (" + icdLabel[j].substring(3)
                                    + ") show\n");
                            k -= (tableFontSize);
                        }
                    }
                    if (showSeeNotesNote) {
                        fw.write("3 0 MT (" + notesString + ") show\n");
                    }

                    // Write the footer
                    fw.write("0 0 MT (" + footerString + ") CS\n");

                    fw.write("showpage\n");
                    System.out.println("Wrote " + psFileName + ".");
                    fw.close();
                } catch (IOException ioe) {
                    System.out.println(ioe);
                }
            }
        }

        if (fileType == FileTypes.pdf) {
            LinkedList<String> newlyGeneratedFiles = new LinkedList<String>();
            for (String fileN : generatedFiles) {
                PsToPdfConverter pstopdf = new PsToPdfConverter(gspath);
                newlyGeneratedFiles.add(pstopdf.convert(fileN));
                // delete the ps file
                File file = new File(fileN);
                file.delete();
            }
            generatedFiles = newlyGeneratedFiles;
        }

        System.out.println("Fini!");

        return generatedFiles;
    }

    /**
     * @return the variablesNeeded
     */
    @Override
    public Globals.StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    @Override
    public boolean areThesePopulationDatasetsCompatible(PopulationDataset[] sets) {
        boolean OK = super.areThesePopulationDatasetsCompatible(sets);
        for (PopulationDataset pds : sets) {
            OK = OK && pds.getAgeGroupStructure().getSizeOfGroups() == 5;
        }
        return OK;
    }
}
