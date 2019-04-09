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

import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.PopulationDataset;
import java.util.LinkedList;

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
public class QualityIndicatorsTableBuilder extends AbstractEditorialTableBuilder {

    private static Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.BasisDiagnosis};
    private final int unknownAgeCode = Globals.DEFAULT_UNKNOWN_AGE_CODE;
    private static int YEAR_COLUMN = 0;
    private static int SEX_COLUMN = 1;
    private static int AGE_COLUMN = 2;
    private static int ICD10_COLUMN = 3;
    private static int MORPHOLOGY_COLUMN = 4;
    private static int BEHAVIOUR_COLUMN = 5;
    private static final int BASIS_DIAGNOSIS_COLUMN = 6;
    private static final int CASES_COLUMN = 7;
    private double[][] standardPopulationArray;
    private static final FileTypes[] fileTypesGenerated = {
        FileTypes.ps
    };
    /*
    public LinkedList<String> buildTable(String registryLabel,
    String reportFileName,
    int startYear,
    int endYear,
    Object[][] incidenceData,
    PopulationDataset[] populations,
    PopulationDataset[] standardPopulations,
    LinkedList<ConfigFields> configList,
    String[] engineParameters)  throws NotCompatibleDataException  {


    LinkedList<String> generatedFiles = new LinkedList<String>();

    //int REGLIB = 10;
    int MAXYEAR = 0;
    int MAXYEARCT = 0;

    boolean reportToFile = true;

    boolean compareToAnotherRegistry = false;

    boolean comparedToVol8 = true;

    boolean showC44inTable;

    compareToAnotherRegistry = false;

    double casesArray[][][] = null; // a 3D array of  icdgroup, sex and agegroup - with one extra layer in all dimensions containing a sum of all
    double populationArray[][] = null; // contains population count in the following format: [sex][agegroup]
    int mortalityArray[][] = null; //

    //        double RegPop[][];
    double CA[][];
    double CR[][];
    double MV[][];
    double MI[][];
    double ASR[][];
    //double vASR[][];
    double ASRluL[][][];
    double variL[][];
    double DCO[][];
    double BU[][];

    int illDefinedCancerGroupIndex = -1;
    boolean showMIIllDefSite = true;

    char ASRf[][];
    double ASRratio[][];
    char MVf[][];
    char MIf[][];

    double CACT[][];
    double CRCT[][];
    double MVCT[][];
    double MICT[][];
    double ASRCT[][];
    double ASRCTluL[][][];
    double variLCT[][];
    double DCOCT[][];
    double BUCT[][];

    boolean foundAgeGroupsCT[];

    double casesArrayCT[][][] = null; // a 3D array of sex, icd and agegroup - with one extra layer in all dimensions containing a sum of all
    double populationArrayCT[][] = null; // contains population count in the following format: [sex][agegroup]
    int mortalityArrayCT[][] = null; //

    String dayLabel[];
    String monthLabel[];
    String sexLabel[] = null;
    String tableLabel[] = null;
    String icdLabel[] = null;

    LinkedList cancerGroups[] = null;
    LinkedList cancerGroupsICD9[] = null;

    int years[] = null;
    int yearsCT[] = null;

    LinkedList<FieldDescription> incidenceFieldDescriptionList = null;
    LinkedList<FieldDescription> populFieldDescriptionList = null;
    LinkedList<FieldDescription> mortalityFieldDescriptionList = null;
    FileReader configFile;
    LinkedList configList;
    LinkedList mortalityConfigList;

    int yearsInMortalityFile = 0;
    int yearsInMortalityFileCT = 0;

    double mortalityFileRatio = 1;
    double mortalityFileRatioCT = 1;

    String registryLabel = null;
    String registryLabelCT = null;

    // old data

    LinkedList<FieldDescription> incidenceVol8FieldDescriptionList = null;
    LinkedList<FieldDescription> populVol8FieldDescriptionList = null;
    LinkedList<FieldDescription> mortalityVol8FieldDescriptionList = null;

    LinkedList<FieldDescription> incidenceVol7FieldDescriptionList = null;
    LinkedList<FieldDescription> populVol7FieldDescriptionList = null;
    LinkedList<FieldDescription> mortalityVol7FieldDescriptionList = null;

    // Load config
    try {
    ConfigFieldsReader configFieldsReader = new ConfigFieldsReader();
    configFile = new FileReader(configFileName);
    configList = configFieldsReader.readFile(configFile);
    monthLabel = configFieldsReader.findConfig("month_label",
    configList);
    dayLabel = configFieldsReader.findConfig("day_label", configList);
    tableLabel = configFieldsReader.findConfig("table_label",
    configList);
    sexLabel = configFieldsReader.findConfig("sex_label", configList);

    ConfigFieldsReader mortalityConfigFieldsReader = new ConfigFieldsReader();

    FileReader mortalityConfigFile = new FileReader(
    mortalityConfigFileName);

    mortalityConfigList = mortalityConfigFieldsReader.readFile(
    mortalityConfigFile);

    icdLabel = mortalityConfigFieldsReader.findConfig(
    "ICD_groups_labels",
    mortalityConfigList);
    icd10GroupDescriptions = mortalityConfigFieldsReader.findConfig(
    "ICD10_groups",
    mortalityConfigList);

    cancerGroups = generateICD10Groups(icd10GroupDescriptions);

    cancerGroupsICD9 = generateICD9Groups(configFieldsReader.findConfig(
    "ICD9_groups",
    mortalityConfigList));

    allCancerGroupsIndex = getICD10index("ALL", icd10GroupDescriptions);
    allCancerGroupsButSkinIndex = getICD10index("ALLb",
    icd10GroupDescriptions);

    skinCancerGroupIndex = getICD10index("C44", icd10GroupDescriptions);

    leukemiaNOSCancerGroupIndex = getICD10index(950,
    cancerGroups);

    bladderCancerGroupIndex = getICD10index("C67",
    icd10GroupDescriptions);
    otherCancerGroupsIndex = getICD10index("O&U",
    configFieldsReader.findConfig(
    "ICD9_groups",
    mortalityConfigList));

    illDefinedCancerGroupIndex = getICD10index("C76-80",
    icd10GroupDescriptions);

    showMIIllDefSite = showMIIllDefSite(registryNumber);

    numberOfCancerGroups = cancerGroups.length;
    registryLabel = getRegistryLabel(registryNumber, registryDictionary);

    if (compareToContemporaryData) {
    registryLabelCT = getRegistryLabel(compareToRegistry,
    registryDictionary);
    } else {
    registryLabelCT = getRegistryLabel8(compareToRegistry,
    v8RegistryDictionary);
    }

    if (registryLabelCT == null) {
    // we put this to true so that we see that it is not compared to "it self" for sure...
    compareToAnotherRegistry = true;
    }

    if (registryLabel != null && registryLabel.charAt(0) == 's') {
    isSpecialized = true;
    registryLabel = registryLabel.substring(1).trim();
    }

    if (registryLabelCT != null && registryLabelCT.charAt(0) == '7') {
    comparedToVol8 = false;
    registryLabelCT = registryLabel.substring(1).trim();
    }

    years = extractTimeSpan(registryLabel);

    if (years == null) {
    return;
    }

    minimumCasesLimit = minimumCasesPerYearLimit * (years[1] - years[0] + 1);

    MAXYEAR = years[1] - years[0] + 1;

    yearsCT = extractTimeSpan(registryLabelCT);
    if (yearsCT == null) {
    System.out.println("No old data...");
    noOldData = true;
    } else {
    MAXYEARCT = yearsCT[1] - yearsCT[0] + 1;
    }

    mortalityFieldDescriptionList = readDescription(
    mortalityFileDescriptionFileName);

    continentNumber = getContinentNumber(registryNumber);

    // prepare descriptions of old data

    incidenceVol8FieldDescriptionList = readDescription(
    v8IncidenceDataDescriptionFilename);
    populVol8FieldDescriptionList = readDescription(
    v8PopulationDataDescriptionFilename);
    mortalityVol8FieldDescriptionList = readDescription(
    v8MortalityDataDescriptionFilename);



    } catch (IOException e) {
    System.out.println("Config-file error.");
    }

    casesArray = new double[numberOfCancerGroups][numberOfSexes][numberOfAgeGroups];

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

    // Generate totals
    //for (int ageGroup = 0; ageGroup<numberOfAgeGroups; ageGroup++) {
    //    for (int sex = 0; sex<numberOfSexes; sex++) {
    //        populationArray[sex][numberOfAgeGroups]+=populationArray[sex][ageGroup];
    //   }
    //}

    // load mortality data for volume 9
    mortalityArray = new int[numberOfSexes][numberOfCancerGroups];

    mortalityFieldDescriptionList = readDescription(
    mortalityFileDescriptionFileName);

    yearsInMortalityFile = loadMortalityData(mortalityFileName,
    mortalityFieldDescriptionList,
    mortalityArray, populationCode);

    mortalityFileRatio = (double) ((years[1] - years[0]) + 1) /
    yearsInMortalityFile;

    System.out.println("Mortality File Ratio: " + mortalityFileRatio);


    // combine 0th and first group
    //for (int sex = 0; sex < numberOfSexes; sex++) {
    //    populationArray[sex][1] += populationArray[sex][0];
    //}
    // ageLabel[1] = new String("0-");

    FileReader dataFile;
    int records = 0;
    // generate statistics

    // Generate based on death certificate only
    DCO = new double[numberOfSexes][numberOfCancerGroups];
    BU = new double[numberOfSexes][numberOfCancerGroups];

    // and microscopical verification
    MV = new double[numberOfSexes][numberOfCancerGroups];

    try {
    dataFile = new FileReader(incidenceFileName);
    String line = readLine(dataFile);
    String sexString;
    String icdString;
    String yearString;
    String ageGroupString;
    String basisString;
    int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, basis;

    while (!line.equals("EOF")) {
    if (records % recordsPerFeedback == 0) {
    System.out.println("Processing record number: " + records);
    }
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
    if (icdNumber == 90 || icdNumber == 414) {
    icdIndex = bladderCancerGroupIndex;
    } else {
    icdIndex = leukemiaNOSCancerGroupIndex;
    }
    }
    yearString = getContentOfField(
    incidenceFieldDescriptionList,
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
    } else if (basis == 99) {
    BU[sex - 1][icdIndex]++;
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
    } else if (basis == 99) {
    BU[sex - 1][allCancerGroupsIndex]++;
    }

    }
    if (allCancerGroupsButSkinIndex >= 0 &&
    icdIndex != skinCancerGroupIndex) {
    casesArray[allCancerGroupsButSkinIndex][sex -
    1][ageGroup]++;
    if (basis == 0) {
    DCO[sex - 1][allCancerGroupsButSkinIndex]++;
    } else if (basis >= 10 && basis <= 19) {
    MV[sex - 1][allCancerGroupsButSkinIndex]++;
    } else if (basis == 99) {
    BU[sex - 1][allCancerGroupsButSkinIndex]++;
    }

    }
    records++;
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
    ASRluL = new double[numberOfSexes][numberOfCancerGroups][2];
    variL = new double[numberOfSexes][numberOfCancerGroups];

    // Mortality divided by incidence
    MI = new double[numberOfSexes][numberOfCancerGroups];
    // Total cases
    CA = new double[numberOfSexes][numberOfCancerGroups];
    // Crude rate
    CR = new double[numberOfSexes][numberOfCancerGroups];

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
    previousAgeGroupCases += casesArray[icdGroup][sex][ag];
    previousAgeGroupPopulation += populationArray[sex][ag];
    previousAgeGroupWstdPopulation += wstdPop[ag];
    }
    }
    if (foundAgeGroups[ageGroup] &&
    ageGroup < highestPopulationAgeGroup &&
    (previousAgeGroupPopulation +
    populationArray[sex][ageGroup] > 0)) {
    double asr = calculateASRMB((previousAgeGroupCases +
    casesArray[icdGroup][sex][ageGroup]),
    (previousAgeGroupPopulation +
    populationArray[sex][ageGroup]),
    (previousAgeGroupWstdPopulation +
    wstdPop[ageGroup]));

    ASR[sex][icdGroup] += asr;

    variL[sex][icdGroup] +=
    calculateVariLMB((previousAgeGroupCases +
    casesArray[icdGroup][sex][ageGroup]),
    (previousAgeGroupWstdPopulation +
    wstdPop[ageGroup]),
    (previousAgeGroupPopulation +
    populationArray[sex][ageGroup]));

    previousAgeGroupCases = 0;
    previousAgeGroupPopulation = 0;
    previousAgeGroupWstdPopulation = 0;

    } else if (ageGroup < highestPopulationAgeGroup) {
    previousAgeGroupCases += casesArray[icdGroup][sex][ageGroup];
    previousAgeGroupPopulation += populationArray[sex][ageGroup];
    previousAgeGroupWstdPopulation += wstdPop[ageGroup];

    } else {
    lastAgeGroupCases += casesArray[icdGroup][sex][ageGroup];
    lastAgeGroupPopulation += populationArray[sex][ageGroup];
    lastAgeGroupWstdPopulation += wstdPop[ageGroup];
    }

    CA[sex][icdGroup] += casesArray[icdGroup][sex][ageGroup];
    }

    // We calculate the "leftovers" from the last age group
    if (lastAgeGroupPopulation > 0) {
    double asr = calculateASRMB(lastAgeGroupCases,
    lastAgeGroupPopulation,
    lastAgeGroupWstdPopulation);
    ASR[sex][icdGroup] += asr;

    variL[sex][icdGroup] +=
    calculateVariLMB(lastAgeGroupCases,
    lastAgeGroupWstdPopulation, lastAgeGroupPopulation);

    }

    CA[sex][icdGroup] += casesArray[icdGroup][sex][unknownAgeGroupIndex];

    if (CA[sex][icdGroup] > 0) {
    MI[sex][icdGroup] = 100 *
    ((double) mortalityArray[sex][icdGroup] *
    mortalityFileRatio /
    CA[sex][icdGroup]);
    DCO[sex][icdGroup] = 100 * DCO[sex][icdGroup] /
    CA[sex][icdGroup];
    BU[sex][icdGroup] = 100 * BU[sex][icdGroup] /
    CA[sex][icdGroup];
    MV[sex][icdGroup] = 100 * MV[sex][icdGroup] /
    CA[sex][icdGroup];
    CR[sex][icdGroup] = CA[sex][icdGroup] *
    wstdPop[allAgeGroupsIndex] /
    populationArray[sex][allAgeGroupsIndex];

    // adjust the ASR for unknown ages
    if (ASR[sex][icdGroup] > 0) {
    ASR[sex][icdGroup] = ASR[sex][icdGroup] *
    (CA[sex][icdGroup] /
    (CA[sex][icdGroup] -
    casesArray[icdGroup][sex][unknownAgeGroupIndex]));
    }

    double[] asrlul = calculateASRluLMB(ASR[sex][icdGroup],
    variL[sex][icdGroup], wstdPop[allAgeGroupsIndex]);

    ASRluL[sex][icdGroup][0] = asrlul[0];
    ASRluL[sex][icdGroup][1] = asrlul[1];

    } else {
    MI[sex][icdGroup] = -1;
    }
    }
    }

    // load old data//////////////////////////////////////////////////////////////////////////////////

    casesArrayCT = new double[numberOfCancerGroups][numberOfSexes][numberOfAgeGroups];

    populationArrayCT = new double[numberOfSexes][numberOfAgeGroups + 1];

    foundAgeGroupsCT = new boolean[numberOfAgeGroups];

    if (compareToContemporaryData) {
    loadPopulationData("converted/" +
    continentLabels[Integer.parseInt(compareToRegistry.substring(0, 1)) -
    1] +
    "/" + compareToRegistry + ".2",
    populFieldDescriptionList,
    populationArrayCT, foundAgeGroupsCT, false);
    } else {
    loadPopulationData(v8DataPath + "/" + compareToRegistry + ".2",
    populVol8FieldDescriptionList,
    populationArrayCT, foundAgeGroupsCT, true);
    }

    highestPopulationAgeGroup = findHighestAgeGroup(foundAgeGroupsCT);
    lowestPopulationAgeGroup = findLowestAgeGroup(foundAgeGroupsCT);

    mortalityArrayCT = new int[numberOfSexes][numberOfCancerGroups];
    if (compareToContemporaryData) {
    yearsInMortalityFileCT = loadMortalityData("converted/" +
    continentLabels[Integer.parseInt(compareToRegistry.substring(0, 1)) -
    1] +
    "/" +
    compareToRegistry + ".3", mortalityFieldDescriptionList,
    mortalityArrayCT, populationCode);

    mortalityFileRatioCT = (double) ((years[1] - years[0]) + 1) /
    yearsInMortalityFile;

    } else {
    noOldMortalityData = !loadOldMortalityData(v8DataPath + "/" +
    compareToRegistry + ".3",
    mortalityVol8FieldDescriptionList,
    mortalityArrayCT, cancerGroupsICD9);
    }


    // Generate based on death certificate only
    DCOCT = new double[numberOfSexes][numberOfCancerGroups];

    BUCT = new double[numberOfSexes][numberOfCancerGroups];

    // and microscopical verification
    MVCT = new double[numberOfSexes][numberOfCancerGroups];

    if (compareToContemporaryData) {

    try {
    dataFile = new FileReader("converted/" +
    continentLabels[Integer.parseInt(compareToRegistry.substring(0, 1)) -
    1] +
    "/" +
    compareToRegistry +
    ".1");
    String line = readLine(dataFile);
    String sexString;
    String icdString;
    String yearString;
    String ageGroupString;
    String basisString;
    int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, basis;

    while (!line.equals("EOF")) {
    if (records % recordsPerFeedback == 0) {
    System.out.println("Processing record number: " +
    records);
    }
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
    if (icdNumber == 90 || icdNumber == 414) {
    icdIndex = bladderCancerGroupIndex;
    } else {
    icdIndex = leukemiaNOSCancerGroupIndex;
    }
    }
    yearString = getContentOfField(
    incidenceFieldDescriptionList,
    "year_of_incidence", line);
    year = Integer.parseInt(yearString);
    yearIndex = year - years[0];
    ageGroupString = getContentOfField(
    incidenceFieldDescriptionList,
    "age_group", line);
    ageGroup = Integer.parseInt(ageGroupString);

    if (year <= yearsCT[1] && year >= yearsCT[0]) {

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

    casesArrayCT[icdIndex][sex - 1][ageGroup]++;

    //
    if (basis == 00) {
    DCOCT[sex - 1][icdIndex]++;
    } else if (basis >= 10 && basis <= 19) {
    MVCT[sex - 1][icdIndex]++;
    } else if (basis == 99) {
    BUCT[sex - 1][icdIndex]++;
    }

    } else {
    if (otherCancerGroupsIndex >= 0) {
    casesArrayCT[otherCancerGroupsIndex][sex -
    1][ageGroup]++;
    }
    }
    if (allCancerGroupsIndex >= 0) {
    casesArrayCT[allCancerGroupsIndex][sex - 1][ageGroup]++;
    if (basis == 0) {
    DCOCT[sex - 1][allCancerGroupsIndex]++;
    } else if (basis >= 10 && basis <= 19) {
    MVCT[sex - 1][allCancerGroupsIndex]++;
    } else if (basis == 99) {
    BUCT[sex - 1][allCancerGroupsIndex]++;
    }

    }
    if (allCancerGroupsButSkinIndex >= 0 &&
    icdIndex != skinCancerGroupIndex) {
    casesArrayCT[allCancerGroupsButSkinIndex][sex -
    1][ageGroup]++;
    if (basis == 0) {
    DCOCT[sex - 1][allCancerGroupsButSkinIndex]++;
    } else if (basis >= 10 && basis <= 19) {
    MVCT[sex - 1][allCancerGroupsButSkinIndex]++;
    } else if (basis == 99) {
    BUCT[sex - 1][allCancerGroupsButSkinIndex]++;
    }

    }
    records++;
    }
    // Read next line
    line = readLine(dataFile);
    }
    System.out.println("Processed " + records + " records.");

    } catch (IOException e) {
    System.out.println("Data-file error.");
    }

    } else if (!noOldData) {
    try {
    dataFile = new FileReader(v8DataPath + "/" + compareToRegistry +
    ".1");
    String line = readLine(dataFile);
    String sexString;
    String icdString;
    String yearString;
    String ageGroupString;
    String basisString;
    int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, basis;

    while (!line.equals("EOF")) {
    if (records % recordsPerFeedback == 0) {
    System.out.println("Processing record number: " +
    records);
    }
    // Set default
    icdIndex = -1;
    // Unknown sex group = 3
    sex = 3;
    // Extract data
    sexString = getContentOfField(
    incidenceVol8FieldDescriptionList,
    "sex", line);
    sex = Integer.parseInt(sexString.trim());

    // sex = 3 is unknown sex

    if (sex > 2) {
    sex = 3;
    }

    icdString = getContentOfField(
    incidenceVol8FieldDescriptionList,
    "icd", line).trim();
    if (icdString.trim().substring(0, 1).equals("C")) {
    icdString = icdString.trim().substring(1);
    icdNumber = Integer.parseInt(icdString);
    if (icdString.length() < 3) {
    icdNumber = icdNumber * 10;
    }
    icdIndex = getICD10index(icdNumber, cancerGroups);
    if (icdIndex == -1) {
    icdIndex = -1;
    }
    } else {
    icdString = icdString.trim().substring(1);
    icdNumber = Integer.parseInt(icdString);
    if (icdNumber == 90 || icdNumber == 414) {
    icdIndex = bladderCancerGroupIndex;
    } else {
    icdIndex = leukemiaNOSCancerGroupIndex;
    }
    }
    yearString = getContentOfField(
    incidenceVol8FieldDescriptionList,
    "year_of_incidence", line);

    // add 1900 to incidence date for old data...
    year = Integer.parseInt(yearString) + 1900;
    yearIndex = year - yearsCT[0];
    ageGroupString = getContentOfField(
    incidenceVol8FieldDescriptionList,
    "age_group", line);
    ageGroup = Integer.parseInt(ageGroupString.trim()) - 1;

    if (year <= yearsCT[1] && year >= yearsCT[0]) {

    // Basis of diagnosis
    basisString = getContentOfField(
    incidenceVol8FieldDescriptionList,
    "basis", line);
    if (basisString != null) {
    basis = Integer.parseInt(basisString.trim());
    } else {
    basis = -1;
    }

    if (sex <= numberOfSexes && icdIndex >= 0 &&
    icdIndex <= cancerGroups.length) {

    casesArrayCT[icdIndex][sex - 1][ageGroup]++;

    //
    if (basis == 0) {
    DCOCT[sex - 1][icdIndex]++;
    } else if (basis >= 4 && basis <= 8) {
    MVCT[sex - 1][icdIndex]++;
    } else if (basis == 9) {
    BUCT[sex - 1][icdIndex]++;
    }

    } else {
    if (otherCancerGroupsIndex >= 0) {
    casesArrayCT[otherCancerGroupsIndex][sex -
    1][ageGroup]++;
    }
    }
    if (allCancerGroupsIndex >= 0) {
    casesArrayCT[allCancerGroupsIndex][sex -
    1][ageGroup]++;
    if (basis == 0) {
    DCOCT[sex - 1][allCancerGroupsIndex]++;
    } else if (basis >= 4 && basis <= 8) {
    MVCT[sex - 1][allCancerGroupsIndex]++;
    } else if (basis == 9) {
    BUCT[sex - 1][allCancerGroupsIndex]++;
    }

    }
    if (allCancerGroupsButSkinIndex >= 0 &&
    icdIndex != skinCancerGroupIndex) {
    casesArrayCT[allCancerGroupsButSkinIndex][sex -
    1][ageGroup]++;
    if (basis == 0) {
    DCOCT[sex - 1][allCancerGroupsButSkinIndex]++;
    } else if (basis >= 4 && basis <= 8) {
    MVCT[sex - 1][allCancerGroupsButSkinIndex]++;
    } else if (basis == 9) {
    BUCT[sex - 1][allCancerGroupsButSkinIndex]++;
    }

    }
    records++;
    }
    // Read next line
    line = readLine(dataFile);
    }
    System.out.println("Processed " + records + " records.");

    } catch (IOException e) {
    System.out.println("Data-file error.");
    }
    }

    // Get our matrixes ready

    // Age standarized rates
    ASRCT = new double[numberOfSexes][numberOfCancerGroups];
    ASRCTluL = new double[numberOfSexes][numberOfCancerGroups][2];
    variLCT = new double[numberOfSexes][numberOfCancerGroups];

    // Mortality divided by incidence
    MICT = new double[numberOfSexes][numberOfCancerGroups];
    // Total cases
    CACT = new double[numberOfSexes][numberOfCancerGroups];
    // Crude rate
    CRCT = new double[numberOfSexes][numberOfCancerGroups];

    // ASRCT, vASR, MVCT, MICT, DCOCT
    for (int sex = 0; sex < numberOfSexes; sex++) {
    for (int icdGroup = 0; icdGroup < numberOfCancerGroups; icdGroup++) {

    double previousAgeGroupCases = 0;
    double previousAgeGroupPopulation = 0;
    double previousAgeGroupWstdPopulation = 0;

    double lastAgeGroupCases = 0;
    double lastAgeGroupPopulation = 0;
    double lastAgeGroupWstdPopulation = 0;

    CACT[sex][icdGroup] += casesArrayCT[icdGroup][sex][0];

    for (int ageGroup = 1; ageGroup < unknownAgeGroupIndex;
    ageGroup++) {
    if (ageGroup == 1) {
    for (int ag = lowestIncidenceAgeGroup; ag < ageGroup;
    ag++) {
    previousAgeGroupCases += casesArrayCT[icdGroup][sex][ag];
    previousAgeGroupPopulation += populationArrayCT[sex][ag];
    previousAgeGroupWstdPopulation += wstdPop[ag];
    }
    }
    if (foundAgeGroupsCT[ageGroup] &&
    ageGroup < highestPopulationAgeGroup &&
    (previousAgeGroupPopulation +
    populationArrayCT[sex][ageGroup] > 0)) {
    double asr = calculateASRMB((previousAgeGroupCases +
    casesArrayCT[icdGroup][sex][ageGroup]),
    (previousAgeGroupPopulation +
    populationArrayCT[sex][ageGroup]),
    (previousAgeGroupWstdPopulation +
    wstdPop[ageGroup]));

    ASRCT[sex][icdGroup] += asr;

    variLCT[sex][icdGroup] +=
    calculateVariLMB((previousAgeGroupCases +
    casesArrayCT[icdGroup][sex][ageGroup]),
    (previousAgeGroupWstdPopulation +
    wstdPop[ageGroup]),
    (previousAgeGroupPopulation +
    populationArrayCT[sex][ageGroup]));

    previousAgeGroupCases = 0;
    previousAgeGroupPopulation = 0;
    previousAgeGroupWstdPopulation = 0;

    } else if (ageGroup < highestPopulationAgeGroup) {
    previousAgeGroupCases += casesArrayCT[icdGroup][sex][ageGroup];
    previousAgeGroupPopulation += populationArrayCT[sex][ageGroup];
    previousAgeGroupWstdPopulation += wstdPop[ageGroup];

    } else {
    lastAgeGroupCases += casesArrayCT[icdGroup][sex][ageGroup];
    lastAgeGroupPopulation += populationArrayCT[sex][ageGroup];
    lastAgeGroupWstdPopulation += wstdPop[ageGroup];
    }

    CACT[sex][icdGroup] += casesArrayCT[icdGroup][sex][ageGroup];
    }

    // We calculate the "leftovers" from the last age group
    if (lastAgeGroupPopulation > 0) {
    double asr = calculateASRMB(lastAgeGroupCases,
    lastAgeGroupPopulation,
    lastAgeGroupWstdPopulation);
    ASRCT[sex][icdGroup] += asr;

    variLCT[sex][icdGroup] +=
    calculateVariLMB(lastAgeGroupCases,
    lastAgeGroupWstdPopulation, lastAgeGroupPopulation);

    }

    CACT[sex][icdGroup] += casesArrayCT[icdGroup][sex][unknownAgeGroupIndex];

    if (CACT[sex][icdGroup] > 0) {

    MICT[sex][icdGroup] = 100 *
    mortalityArrayCT[sex][icdGroup] *
    mortalityFileRatioCT /
    CACT[sex][icdGroup];

    DCOCT[sex][icdGroup] = 100 * DCOCT[sex][icdGroup] /
    CACT[sex][icdGroup];
    BUCT[sex][icdGroup] = 100 * BUCT[sex][icdGroup] /
    CACT[sex][icdGroup];
    MVCT[sex][icdGroup] = 100 * MVCT[sex][icdGroup] /
    CACT[sex][icdGroup];
    CRCT[sex][icdGroup] = CACT[sex][icdGroup] *
    wstdPop[allAgeGroupsIndex] /
    populationArrayCT[sex][allAgeGroupsIndex];

    // adjust the ASR for unknown ages
    if (ASRCT[sex][icdGroup] > 0) {
    ASRCT[sex][icdGroup] = ASRCT[sex][icdGroup] *
    (CACT[sex][icdGroup] /
    (CACT[sex][icdGroup] -
    casesArrayCT[icdGroup][sex][unknownAgeGroupIndex]));
    }

    double[] asrlul = calculateASRluLMB(ASRCT[sex][icdGroup],
    variLCT[sex][icdGroup], wstdPop[allAgeGroupsIndex]);

    ASRCTluL[sex][icdGroup][0] = asrlul[0];
    ASRCTluL[sex][icdGroup][1] = asrlul[1];

    } else {
    MICT[sex][icdGroup] = -1;
    }
    }
    }



    // Writing
    System.out.println("Writing data...\n");

    PrintStream reportStream = new PrintStream(System.out);
    File reportFile;

    if (reportToFile) {
    String reportFileName = tablesPath +
    (continentLabels[continentNumber - 1]) +
    "/" + registryNumber +
    "_e4.tab";
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

    // Write temporarily to a file
    reportStream.println(registryLabel);
    reportStream.println(tableLabel[0]);
    reportStream.println(tableLabel[1]);

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(1);
    nf.setMinimumFractionDigits(1);

    for (int sex = 0; sex < 2; sex++) {
    reportStream.println(sexLabel[sex]);
    reportStream.println(
    "SITE\tCases\tCR\tASR (lL-uL)\tASR v8\tMV(%)\tDCO(%)\tM/I(%)\tM/I v8(%)\tICD10");
    for (int icdGroup = 0; icdGroup < numberOfCancerGroups; icdGroup++) {
    if (icdLabel[icdGroup].substring(0 + sex, 1 + sex).
    equalsIgnoreCase("1")) {
    String strOut = new String();
    strOut += icdLabel[icdGroup].substring(3);
    strOut += "\t";
    nf.setMinimumFractionDigits(0);
    strOut += nf.format(CA[sex][icdGroup]);
    nf.setMinimumFractionDigits(1);
    strOut += "\t";
    strOut += formatNumber(CR[sex][icdGroup]);
    strOut += "\t";
    strOut += formatNumber(ASR[sex][icdGroup]) + " (" +
    formatNumber(ASRluL[sex][icdGroup][0], 1) + "-" +
    formatNumber(ASRluL[sex][icdGroup][1], 1) + ") " +
    ASRf[sex][icdGroup];
    strOut += "\t";
    strOut += formatNumber(ASRCT[sex][icdGroup]);
    strOut += "\t";
    strOut += formatNumber(MV[sex][icdGroup]) +
    MVf[sex][icdGroup];
    strOut += "\t";
    strOut += formatNumber(DCO[sex][icdGroup]);
    strOut += "\t";
    strOut += formatNumber(MI[sex][icdGroup]) +
    MIf[sex][icdGroup];
    strOut += "\t";
    strOut += formatNumber(MICT[sex][icdGroup]);
    strOut += "\t";
    strOut += icd10GroupDescriptions[icdGroup];
    reportStream.println(strOut);
    }

    }
    if (mortalityArray[sex][allCancerGroupsButSkinIndex] == 0) {

    String strOut = new String();
    strOut += icdLabel[allCancerGroupsIndex].substring(3);
    strOut += "\t";
    nf.setMinimumFractionDigits(0);
    strOut += nf.format(CA[sex][allCancerGroupsIndex]);
    nf.setMinimumFractionDigits(1);
    strOut += "\t";
    strOut += formatNumber(CR[sex][allCancerGroupsIndex]);
    strOut += "\t";
    strOut += formatNumber(ASR[sex][allCancerGroupsIndex]) + " (" +
    formatNumber(ASRluL[sex][allCancerGroupsIndex][0], 1) +
    "-" +
    formatNumber(ASRluL[sex][allCancerGroupsIndex][1], 1) +
    ") " +
    ASRf[sex][allCancerGroupsIndex];
    strOut += "\t";
    strOut += formatNumber(ASRCT[sex][allCancerGroupsIndex]);
    strOut += "\t";
    strOut += formatNumber(MV[sex][allCancerGroupsIndex]) +
    MVf[sex][allCancerGroupsIndex];
    strOut += "\t";
    strOut += formatNumber(DCO[sex][allCancerGroupsIndex]);
    strOut += "\t";
    strOut += formatNumber(MI[sex][allCancerGroupsIndex]) +
    MIf[sex][allCancerGroupsIndex];
    strOut += "\t";
    strOut += formatNumber(MICT[sex][allCancerGroupsIndex]);
    strOut += "\t";
    strOut += icd10GroupDescriptions[allCancerGroupsIndex];
    reportStream.println(strOut);

    }

    }

    reportStream.println("Cases vol 9\n");
    reportStream.println(array3DToString(casesArray));
    reportStream.println("Variance vol 9\n");
    reportStream.println(array2DToString(variL));
    reportStream.println("Population vol 9\n");
    reportStream.println(array2DToString(populationArray));

    reportStream.close();

    String psFileName = tablesPath +
    (continentLabels[continentNumber - 1]) +
    "/" + registryNumber +
    "_e4.ps";
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
    fw.write("/nstr 1 string def\n");
    fw.write("/prtchar {nstr 0 3 -1 roll put nstr show} def\n");
    fw.write("/Mainfont\n");
    fw.write("/Helvetica-Bold FF 11 SCF def\n");
    fw.write("/Titlefont\n");
    fw.write("/Helvetica-Bold FF 10 SCF def\n");
    fw.write("/Tablefont\n");
    fw.write("/Times-Roman FF 9 SCF def\n");
    fw.write("/ICDfont\n");
    fw.write("/Times-Bold FF 9 SCF def\n");
    fw.write("/Italicsfont\n");
    fw.write("/Times-Italic FF 9 SCF def\n");
    fw.write(
    "/LS {dup stringwidth pop col exch sub 0 rmoveto show} def\n");
    fw.write(
    "/CS {dup stringwidth pop 580 exch sub 2 div 0 rmoveto show} def\n");
    fw.write(
    "/RS {dup stringwidth pop col exch sub 0 rmoveto show} def\n");
    fw.write("newpath\n");
    fw.write("Mainfont SF\n");
    fw.write("0 760 MT (" + registryLabel + ") CS\n");
    fw.write("Titlefont SF\n");
    fw.write("0 740 MT (" + tableLabel[1] + ") CS\n");
    fw.write("0 730 MT (" + tableLabel[0] + ") CS\n");
    fw.write("/col 20 def\n");
    fw.write("0 SG\n");
    fw.write("0 700 MT (" + sexLabel[0] + ") CS\n");
    fw.write("0 400 MT (" + sexLabel[1] + ") CS\n");

    fw.write("ICDfont SF\n");
    int Y = 670;
    int sex = 0;
    fw.write("50 " + Y + " MT (S I T E) show\n");
    fw.write("170 " + Y + " MT (Cases) show\n");
    fw.write("230 " + Y + " MT (ASR (l-u)) show\n");

    fw.write("345 " + Y + " MT (MV(%)) show\n");

    if (compareToContemporaryData) {
    fw.write("310 " + Y + " MT (ASR v9*) show\n");
    fw.write("380 " + Y + " MT (MV v9*(%)) show\n");
    } else if (compareToAnotherRegistry) {
    fw.write("310 " + Y + " MT (ASR v8*) show\n");
    fw.write("380 " + Y + " MT (MV v8*(%)) show\n");
    } else {
    fw.write("310 " + Y + " MT (ASR v8) show\n");
    fw.write("380 " + Y + " MT (MV v8(%)) show\n");
    }

    fw.write("425 " + Y + " MT (DCO(%)) show\n");
    fw.write("465 " + Y + " MT (M/I(%)) show\n");
    fw.write("500 " + Y + " MT (UB(%)) show\n");
    fw.write("530 " + Y + " MT (ICD-10) show\n");
    Y -= 15;
    fw.write("Tablefont SF\n");
    for (int icd = 0; icd < numberOfCancerGroups; icd++) {
    if (icdLabel[icd].charAt(sex) == '1' ||
    (icd == allCancerGroupsIndex &&
    mortalityArray[sex][allCancerGroupsButSkinIndex] == 0 &&
    mortalityArray[sex][allCancerGroupsIndex] > 0)) {
    fw.write("Tablefont SF\n");

    String label = icdLabel[icd].substring(2);
    if (icd == illDefinedCancerGroupIndex) {
    label = label + " (" +
    formatNumber(100 * CA[sex][icd] /
    CA[sex][allCancerGroupsButSkinIndex]) +
    "%)";
    }

    fw.write("50 " + Y + " MT (" + label + ") show\n");
    fw.write("530 " + Y + " MT (" + icd10GroupDescriptions[icd] +
    ") show\n");
    if (CA[sex][icd] > 0) {
    fw.write("170 " + Y + " MT (" +
    formatNumber(CA[sex][icd], 0) + ") RS\n");
    } else {
    fw.write("170 " + Y + " MT (       -) RS\n");
    }
    if (ASR[sex][icd] != 0) {
    if (!noOldData && ASRf[sex][icd] != ' ' && ASRf[sex][icd] != 's') {
    if (CA[sex][icd] < minimumCasesLimit) {
    fw.write("Italicsfont SF\n"); // Italics
    } else {
    fw.write("ICDfont SF\n"); // Bold
    }
    fw.write("205 " + Y + " MT (" +
    formatNumber(ASR[sex][icd]) + ") RS\n");
    fw.write("270 " + Y + " MT ( (" +
    formatNumber(ASRluL[sex][icd][0], 1) +
    " - " +
    formatNumber(ASRluL[sex][icd][1], 1) +
    ") " +
    ASRf[sex][icd] + ") RS\n");

    } else {
    fw.write("Tablefont SF\n");
    fw.write("205 " + Y + " MT (" +
    formatNumber(ASR[sex][icd]) + ") RS\n");
    fw.write("270 " + Y + " MT ( (" +
    formatNumber(ASRluL[sex][icd][0], 1) +
    " - " +
    formatNumber(ASRluL[sex][icd][1], 1) +
    ") " + ") RS\n");

    }

    } else {
    fw.write("Tablefont SF\n");
    fw.write("250 " + Y +
    " MT (    -     -          -  ) RS\n");
    }
    if (ASRCT[sex][icd] != 0 &&
    !(isSpecialized && icd == allCancerGroupsButSkinIndex)) {
    fw.write("Tablefont SF\n");
    fw.write("310 " + Y + " MT (" +
    formatNumber(ASRCT[sex][icd]) + ") RS\n");

    } else {
    fw.write("Tablefont SF\n");
    fw.write("310 " + Y +
    " MT (    -   ) RS\n");
    }
    if (MV[sex][icd] != 0) {
    if (!noOldData && MVf[sex][icd] != ' ' && ASRf[sex][icd] != 's') {
    if (CA[sex][icd] < minimumCasesLimit) {
    fw.write("Italicsfont SF\n"); // Italics
    } else {
    fw.write("ICDfont SF\n"); // Bold
    }
    } else {
    fw.write("Tablefont SF\n");
    MVf[sex][icd] = ' ';
    }
    fw.write("350 " + Y + " MT (" +
    formatNumber(MV[sex][icd]) + " " +
    MVf[sex][icd] + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("350 " + Y + " MT (     -   ) RS\n");
    }

    if (MVCT[sex][icd] != 0 &&
    !(isSpecialized && icd == allCancerGroupsButSkinIndex)) {

    fw.write("Tablefont SF\n");
    fw.write("390 " + Y + " MT (" +
    formatNumber(MVCT[sex][icd]) + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("390 " + Y + " MT (     -   ) RS\n");
    }

    if (DCO[sex][icd] > 0) {
    fw.write("Tablefont SF\n");
    fw.write("420 " + Y + " MT (" +
    formatNumber(DCO[sex][icd]) + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("420 " + Y + " MT (     - ) RS\n");
    }
    if (MI[sex][icd] > 0 && ((icd != illDefinedCancerGroupIndex) || (icd == illDefinedCancerGroupIndex && showMIIllDefSite))) {
    if (!noOldData && MIf[sex][icd] != ' ' && ASRf[sex][icd] != 's') {
    if (CA[sex][icd] < minimumCasesLimit) {
    fw.write("Italicsfont SF\n"); // Italics
    } else {
    fw.write("ICDfont SF\n"); // Bold
    }
    } else {
    fw.write("Tablefont SF\n");
    MIf[sex][icd] = ' ';
    }
    fw.write("465 " + Y + " MT (" +
    formatNumber(MI[sex][icd]) + " " +
    MIf[sex][icd] + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("465 " + Y + " MT (     -   ) RS\n");
    }
    if (BU[sex][icd] > 0) {
    fw.write("Tablefont SF\n");
    fw.write("500 " + Y + " MT (" +
    formatNumber(BU[sex][icd]) + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("500 " + Y + " MT (     - ) RS\n");
    }
    Y -= 11;
    }
    }
    fw.write("ICDfont SF\n");
    Y = 370;
    sex = 1;
    fw.write("50 " + Y + " MT (S I T E) show\n");
    fw.write("170 " + Y + " MT (Cases) show\n");
    fw.write("230 " + Y + " MT (ASR (l-u)) show\n");

    fw.write("345 " + Y + " MT (MV(%)) show\n");

    if (compareToContemporaryData) {
    fw.write("310 " + Y + " MT (ASR v9*) show\n");
    fw.write("380 " + Y + " MT (MV v9*(%)) show\n");
    } else if (compareToAnotherRegistry) {
    fw.write("310 " + Y + " MT (ASR v8*) show\n");
    fw.write("380 " + Y + " MT (MV v8*(%)) show\n");
    } else {
    fw.write("310 " + Y + " MT (ASR v8) show\n");
    fw.write("380 " + Y + " MT (MV v8(%)) show\n");
    }

    fw.write("425 " + Y + " MT (DCO(%)) show\n");
    fw.write("465 " + Y + " MT (M/I(%)) show\n");
    fw.write("500 " + Y + " MT (UB(%)) show\n");
    fw.write("530 " + Y + " MT (ICD-10) show\n");
    Y -= 15;
    fw.write("Tablefont SF\n");
    for (int icd = 0; icd < numberOfCancerGroups; icd++) {
    if (icdLabel[icd].charAt(sex) == '1' ||
    (icd == allCancerGroupsIndex &&
    mortalityArray[sex][allCancerGroupsButSkinIndex] == 0 &&
    mortalityArray[sex][allCancerGroupsIndex] > 0)) {
    fw.write("Tablefont SF\n");

    String label = icdLabel[icd].substring(2);
    if (icd == illDefinedCancerGroupIndex) {
    label = label + " (" +
    formatNumber(100 * CA[sex][icd] /
    CA[sex][allCancerGroupsButSkinIndex]) +
    "%)";
    }

    fw.write("50 " + Y + " MT (" + label + ") show\n");
    fw.write("530 " + Y + " MT (" + icd10GroupDescriptions[icd] +
    ") show\n");
    if (CA[sex][icd] > 0) {
    fw.write("170 " + Y + " MT (" +
    formatNumber(CA[sex][icd], 0) + ") RS\n");
    } else {
    fw.write("170 " + Y + " MT (       -) RS\n");
    }
    if (ASR[sex][icd] != 0) {
    if (!noOldData && ASRf[sex][icd] != ' ' && ASRf[sex][icd] != 's') {
    if (CA[sex][icd] < minimumCasesLimit) {
    fw.write("Italicsfont SF\n"); // Italics
    } else {
    fw.write("ICDfont SF\n"); // Bold
    }
    fw.write("205 " + Y + " MT (" +
    formatNumber(ASR[sex][icd]) + ") RS\n");
    fw.write("270 " + Y + " MT ( (" +
    formatNumber(ASRluL[sex][icd][0], 1) +
    " - " +
    formatNumber(ASRluL[sex][icd][1], 1) +
    ") " +
    ASRf[sex][icd] + ") RS\n");

    } else {
    fw.write("Tablefont SF\n");
    fw.write("205 " + Y + " MT (" +
    formatNumber(ASR[sex][icd]) + ") RS\n");
    fw.write("270 " + Y + " MT ( (" +
    formatNumber(ASRluL[sex][icd][0], 1) +
    " - " +
    formatNumber(ASRluL[sex][icd][1], 1) +
    ") " + ") RS\n");

    }

    } else {
    fw.write("Tablefont SF\n");
    fw.write("250 " + Y +
    " MT (    -     -          -  ) RS\n");
    }
    if (ASRCT[sex][icd] != 0 &&
    !(isSpecialized && icd == allCancerGroupsButSkinIndex)) {
    fw.write("Tablefont SF\n");
    fw.write("310 " + Y + " MT (" +
    formatNumber(ASRCT[sex][icd]) + ") RS\n");

    } else {
    fw.write("Tablefont SF\n");
    fw.write("310 " + Y +
    " MT (    -   ) RS\n");
    }
    if (MV[sex][icd] != 0) {
    if (!noOldData && MVf[sex][icd] != ' ' && ASRf[sex][icd] != 's') {
    if (CA[sex][icd] < minimumCasesLimit) {
    fw.write("Italicsfont SF\n"); // Italics
    } else {
    fw.write("ICDfont SF\n"); // Bold
    }
    } else {
    fw.write("Tablefont SF\n");
    MVf[sex][icd] = ' ';
    }
    fw.write("350 " + Y + " MT (" +
    formatNumber(MV[sex][icd]) + " " +
    MVf[sex][icd] + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("350 " + Y + " MT (     -   ) RS\n");
    }

    if (MVCT[sex][icd] != 0 &&
    !(isSpecialized && icd == allCancerGroupsButSkinIndex)) {

    fw.write("Tablefont SF\n");
    fw.write("390 " + Y + " MT (" +
    formatNumber(MVCT[sex][icd]) + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("390 " + Y + " MT (     -   ) RS\n");
    }

    if (DCO[sex][icd] > 0) {
    fw.write("Tablefont SF\n");
    fw.write("420 " + Y + " MT (" +
    formatNumber(DCO[sex][icd]) + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("420 " + Y + " MT (     - ) RS\n");
    }
    if (MI[sex][icd] > 0 && ((icd != illDefinedCancerGroupIndex) || (icd == illDefinedCancerGroupIndex && showMIIllDefSite))) {
    if (!noOldData && MIf[sex][icd] != ' ' &&
    ASRf[sex][icd] != 's') {
    if (CA[sex][icd] < minimumCasesLimit) {
    fw.write("Italicsfont SF\n"); // Italics
    } else {
    fw.write("ICDfont SF\n"); // Bold
    }
    } else {
    fw.write("Tablefont SF\n");
    MIf[sex][icd] = ' ';
    }
    fw.write("465 " + Y + " MT (" +
    formatNumber(MI[sex][icd]) + " " +
    MIf[sex][icd] + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("465 " + Y + " MT (     -   ) RS\n");

    }
    if (BU[sex][icd] > 0) {
    fw.write("Tablefont SF\n");
    fw.write("500 " + Y + " MT (" +
    formatNumber(BU[sex][icd]) + ") RS\n");
    } else {
    fw.write("Tablefont SF\n");
    fw.write("500 " + Y + " MT (     - ) RS\n");
    }
    Y -= 11;
    }
    }
    fw.write("Tablefont SF\n");

    String line = new String();

    if (compareToAnotherRegistry) {
    line += "*";
    }

    if (registryLabelCT != null) {
    line += "Data compared to: " + registryLabelCT;
    if (compareToContemporaryData) {
    line += " (Submitted for CI5 Vol.9.)";
    } else if (comparedToVol8) {
    if (isSpecialized) {
    line +=
    " (Published as a specialized registry in CI5 Vol. 8.)";
    } else if (!publishedInVol8(compareToRegistry)) {
    line += " (Submitted for CI5 Vol.8 - Not published.)";
    } else {
    line += " (Published in CI5 Vol.8.)";
    }
    } else {
    line += ".";
    }
    } else {
    line += "Data cannot be compared to another registry.";
    }

    fw.write("50 100 MT (" + line + ") show\n");

    if (registryLabelCT != null && noOldMortalityData) {
    fw.write(
    "50 90 MT (No old mortality data to compare to.) show\n");
    }



    // Write the date
    Calendar cal = Calendar.getInstance();
    //fw.write("0 20 MT ("+dayLabel[date.-1] + " " + cal.DAY_OF_MONTH + " " + monthLabel[cal.MONTH-1] + " " + cal.YEAR +" - "+ cal.HOUR_OF_DAY +":"+cal.MINUTE+") CS\n");
    fw.write("0 30 MT (" + cal.getTime().toString() + ") CS\n");

    fw.write("showpage\n");

    System.out.println("Wrote " + psFileName + ".");
    fw.close();
    } catch (IOException ioe) {
    System.out.println(ioe);
    }

    System.out.println("Fini!");
    return generatedFiles;
    }

    

    /**
     * @return the variablesNeeded
     */

    @Override
    public LinkedList<String> buildTable(String registryLabel, String reportFileName, int startYear, int endYear, Object[][] incidenceData, PopulationDataset[] populations, PopulationDataset[] standardPopulations, LinkedList<ConfigFields> configList, String[] engineParameters,
            FileTypes fileType, String language) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    @Override
    public FileTypes[] getFileTypesGenerated() {
        return fileTypesGenerated;
    }
}
