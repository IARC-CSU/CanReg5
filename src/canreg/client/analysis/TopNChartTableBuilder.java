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

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.client.analysis.Tools.KeyCancerGroupsEnum;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.IncompatiblePopulationDataSetException;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import com.itextpdf.text.DocumentException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PiePlot;

/**
 *
 * @author ervikm
 */
public class TopNChartTableBuilder implements TableBuilderInterface, JChartTableBuilderInterface {

    // private static int YEAR_COLUMN = 0;
    private static final int SEX_COLUMN = 1;
    private static final int ICD10_COLUMN = 2;
    private static final int MORPHOLOGY_COLUMN = 3;
    // private static int BEHAVIOUR_COLUMN = 4;
    private static final int AGE_COLUMN = 5;
    private static final int CASES_COLUMN = 6;
    private static final Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.Age
    };
    private static final FileTypes[] fileTypesGenerated = {
        FileTypes.png,
        FileTypes.pdf,
        FileTypes.svg,
        FileTypes.jchart,
        FileTypes.csv
    };
    private boolean legendOn = false;
    private boolean useR = true; //change this to false...
    private LocalSettings localSettings;
    private String rpath;
    private LinkedList[] cancerGroupsLocal;
    private final JFreeChart[] charts = new JFreeChart[2];
    private String[] icdLabel;
    private String[] icd10GroupDescriptions;
    private int numberOfCancerGroups;
    private final int numberOfSexes = 3;
    private final int DONT_COUNT = -999;
    private EnumMap<KeyCancerGroupsEnum, Integer> keyGroupsMap;
    private int otherCancerGroupsIndex;
    private Integer skinCancerGroupIndex;
    private int allCancerGroupsIndex;
    private int allCancerGroupsButSkinIndex;
    private final int topNLimit = 10;
    private ChartType chartType;
    private PopulationDataset periodPop;
    private CountType countType = CountType.CASES;
    private boolean includeOther = false;
    private int unknownAgeCode = Globals.DEFAULT_UNKNOWN_AGE_CODE;

    public TopNChartTableBuilder() {
        ChartTheme chartTheme = new StandardChartTheme("sansserif");
        ChartFactory.setChartTheme(chartTheme);
    }

    @Override
    public StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    @Override
    public FileTypes[] getFileTypesGenerated() {
        return fileTypesGenerated;
    }

    @Override
    public LinkedList<String> buildTable(String tableHeader, String reportFileName,
            int startYear, int endYear,
            Object[][] incidenceData,
            PopulationDataset[] populations, // can be null
            PopulationDataset[] standardPopulations,
            LinkedList<ConfigFields> configList,
            String[] engineParameters,
            FileTypes fileType) throws NotCompatibleDataException {
        String footerString = java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString("TABLE BUILT ") + new Date() + java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString(" BY CANREG5.");

        LinkedList<String> generatedFiles = new LinkedList<String>();

        if (Arrays.asList(engineParameters).contains("barchart")) {
            chartType = ChartType.BAR;
        } else {
            chartType = ChartType.PIE;
            includeOther = true;
        }

        if (Arrays.asList(engineParameters).contains("legend")) {
            legendOn = true;
        }

        if (Arrays.asList(engineParameters).contains("r")) {
            useR = true;
        }

        if (Arrays.asList(engineParameters).contains("asr")) {
            countType = CountType.ASR;
        } else if (Arrays.asList(engineParameters).contains("cum64")) {
            countType = CountType.CUM64;
        } else if (Arrays.asList(engineParameters).contains("cum74")) {
            countType = CountType.CUM74;
        } else if (Arrays.asList(engineParameters).contains("per100000")) {
            countType = CountType.PER_HUNDRED_THOUSAND;
        } else {
            // default to cases
            countType = CountType.CASES;
        }

        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        rpath = localSettings.getProperty(LocalSettings.R_PATH);
        // does R exist?
        if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
            useR = false; // force false if R is not installed
        }

        icdLabel = ConfigFieldsReader.findConfig("ICD_groups_labels",
                configList);

        icd10GroupDescriptions = ConfigFieldsReader.findConfig(
                "ICD10_groups",
                configList);

        cancerGroupsLocal = EditorialTableTools.generateICD10Groups(icd10GroupDescriptions);

        // indexes
        keyGroupsMap = new EnumMap<KeyCancerGroupsEnum, Integer>(KeyCancerGroupsEnum.class);

        keyGroupsMap.put(KeyCancerGroupsEnum.allCancerGroupsIndex, EditorialTableTools.getICD10index("ALL", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.leukemiaNOSCancerGroupIndex, EditorialTableTools.getICD10index(950, cancerGroupsLocal));
        keyGroupsMap.put(KeyCancerGroupsEnum.skinCancerGroupIndex, EditorialTableTools.getICD10index("C44", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.bladderCancerGroupIndex, EditorialTableTools.getICD10index("C67", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.mesotheliomaCancerGroupIndex, EditorialTableTools.getICD10index("C45", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.kaposiSarkomaCancerGroupIndex, EditorialTableTools.getICD10index("C46", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.myeloproliferativeDisordersCancerGroupIndex, EditorialTableTools.getICD10index("MPD", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.myelodysplasticSyndromesCancerGroupIndex, EditorialTableTools.getICD10index("MDS", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.allCancerGroupsButSkinIndex, EditorialTableTools.getICD10index("ALLbC44", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.brainAndCentralNervousSystemCancerGroupIndex, EditorialTableTools.getICD10index("C70-72", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.ovaryCancerGroupIndex, EditorialTableTools.getICD10index(569, cancerGroupsLocal));
        keyGroupsMap.put(KeyCancerGroupsEnum.otherCancerGroupsIndex, EditorialTableTools.getICD10index("O&U", icd10GroupDescriptions));

        otherCancerGroupsIndex = keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);
        skinCancerGroupIndex = keyGroupsMap.get(KeyCancerGroupsEnum.skinCancerGroupIndex);
        allCancerGroupsIndex = keyGroupsMap.get(KeyCancerGroupsEnum.allCancerGroupsIndex);
        allCancerGroupsButSkinIndex = keyGroupsMap.get(KeyCancerGroupsEnum.allCancerGroupsButSkinIndex);

        numberOfCancerGroups = cancerGroupsLocal.length;

        double[] countsRow;

        if (populations != null && populations.length > 0) {
            if (populations[0].getPopulationDatasetID() < 0) {
                countType = CountType.CASES;
            } else {
                // calculate period pop
                periodPop = new PopulationDataset();
                periodPop.setAgeGroupStructure(populations[0].getAgeGroupStructure());
                periodPop.setWorldPopulation(populations[0].getWorldPopulation());
                for (PopulationDatasetsEntry pde : populations[0].getAgeGroups()) {
                    int count = 0;
                    for (PopulationDataset pds : populations) {
                        count += pds.getAgeGroupCount(pde.getSex(), pde.getAgeGroup());
                    }
                    periodPop.addAgeGroup(new PopulationDatasetsEntry(pde.getAgeGroup(), pde.getSex(), count));
                }
            }
        }

        if (incidenceData != null) {
            String sexString, icdString, morphologyString;
            double countArray[][] = new double[numberOfCancerGroups][numberOfSexes];

            int sex, icdIndex, numberOfCases, age;
            double adjustedCases;
            List<Integer> dontCount = new LinkedList<Integer>();
            // all sites but skin?
            if (Arrays.asList(engineParameters).contains("noC44")) {
                dontCount.add(skinCancerGroupIndex);
                tableHeader += ", excluding C44";
            }

            for (Object[] dataLine : incidenceData) {

                // Set default
                adjustedCases = 0.0;

                // Extract data
                sexString = (String) dataLine[SEX_COLUMN];
                sex = Integer.parseInt(sexString.trim());

                // sex = 3 is unknown sex
                if (sex > 2) {
                    sex = 3;
                }

                morphologyString = (String) dataLine[MORPHOLOGY_COLUMN];
                icdString = (String) dataLine[ICD10_COLUMN];

                icdIndex = Tools.assignICDGroupIndex(keyGroupsMap, icdString, morphologyString, cancerGroupsLocal);

                age = (Integer) dataLine[AGE_COLUMN];

                if (!dontCount.contains(icdIndex) && icdIndex != DONT_COUNT) {
                    // Extract cases
                    numberOfCases = (Integer) dataLine[CASES_COLUMN];
                    if (countType == CountType.PER_HUNDRED_THOUSAND) {
                        adjustedCases = (100000.0 * numberOfCases) / periodPop.getAgeGroupCount(sex, periodPop.getAgeGroupIndex(age));
                    } else if (countType == CountType.ASR) {
                        try {
                            adjustedCases = 100.0 * (periodPop.getWorldPopulationForAgeGroupIndex(sex, periodPop.getAgeGroupIndex(age)) * numberOfCases) / periodPop.getAgeGroupCount(sex, periodPop.getAgeGroupIndex(age));
                        } catch (IncompatiblePopulationDataSetException ex) {
                            Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (countType == CountType.CUM64) {
                        if (age < 65) {
                            adjustedCases = (100000.0 * numberOfCases) / periodPop.getAgeGroupCount(sex, periodPop.getAgeGroupIndex(age)) * 5.0 / 1000.0;
                        }
                    } else if (countType == CountType.CUM74) {
                        if (age < 75) {
                            adjustedCases = (100000.0 * numberOfCases) / periodPop.getAgeGroupCount(sex, periodPop.getAgeGroupIndex(age)) * 5.0 / 1000.0;
                        }
                    } else {
                        adjustedCases = numberOfCases;
                    }

                    if (sex <= numberOfSexes && icdIndex >= 0
                            && icdIndex <= cancerGroupsLocal.length) {
                        countArray[icdIndex][sex - 1] += adjustedCases;
                    } else {
                        if (otherCancerGroupsIndex >= 0) {
                            countArray[otherCancerGroupsIndex][sex
                                    - 1] += adjustedCases;
                        }
                    }
                    if (allCancerGroupsIndex >= 0) {
                        countArray[allCancerGroupsIndex][sex - 1] += adjustedCases;
                    }
                    if (allCancerGroupsButSkinIndex >= 0
                            && skinCancerGroupIndex >= 0
                            && icdIndex != skinCancerGroupIndex) {
                        countArray[allCancerGroupsButSkinIndex][sex
                                - 1] += adjustedCases;
                    }
                }
            }

            // separate top 10 and the rest
            TreeSet<CancerCasesCount> topNMale = new TreeSet<CancerCasesCount>(new Comparator<CancerCasesCount>() {

                @Override
                public int compare(CancerCasesCount o1, CancerCasesCount o2) {
                    if (o1.getCount().equals(o2.getCount())) {
                        return -o1.toString().compareTo(o2.toString());
                    } else {
                        return -(o1.getCount().compareTo(o2.getCount()));
                    }
                }
            });
            LinkedList<CancerCasesCount> theRestMale = new LinkedList<CancerCasesCount>();

            TreeSet<CancerCasesCount> topNFemale = new TreeSet<CancerCasesCount>(new Comparator<CancerCasesCount>() {

                @Override
                public int compare(CancerCasesCount o1, CancerCasesCount o2) {
                    if (o1.getCount().equals(o2.getCount())) {
                        return -o1.toString().compareTo(o2.toString());
                    } else {
                        return -(o1.getCount().compareTo(o2.getCount()));
                    }
                }
            });
            LinkedList<CancerCasesCount> theRestFemale = new LinkedList<CancerCasesCount>();

            CancerCasesCount otherElement;
            CancerCasesCount thisElement;

            TreeSet<CancerCasesCount> topN;
            LinkedList<CancerCasesCount> theRest;

            for (int icdGroupNumber = 0; icdGroupNumber < countArray.length; icdGroupNumber++) {
                countsRow = countArray[icdGroupNumber];
                for (int sexNumber = 0; sexNumber < 2; sexNumber++) {

                    if (sexNumber == 0) {
                        topN = topNMale;
                        theRest = theRestMale;
                    } else {
                        topN = topNFemale;
                        theRest = theRestFemale;
                    }

                    if (countsRow[sexNumber] > 0) {
                        thisElement = new CancerCasesCount(
                                icd10GroupDescriptions[icdGroupNumber],
                                icdLabel[icdGroupNumber].substring(3),
                                countsRow[sexNumber],
                                icdGroupNumber);

                        // if this is the "other" group - add it immediately to "the rest"
                        if (icdGroupNumber == otherCancerGroupsIndex) {
                            theRest.add(thisElement);
                            // if not we check if this is one of the collection groups
                        } else if (icdGroupNumber != allCancerGroupsButSkinIndex
                                && icdGroupNumber != allCancerGroupsIndex) {
                            // if it is less than N cancers in top N - add it
                            if (topN.size() < topNLimit) {
                                topN.add(thisElement);
                            } else {
                                // otherwise we need to compare it to the last element in the top 10
                                otherElement = topN.last();
                                if (thisElement.compareTo(otherElement) < 0) {
                                    topN.remove(otherElement);
                                    theRest.add(otherElement);
                                    topN.add(thisElement);
                                } else {
                                    theRest.add(thisElement);
                                }
                            }
                        }
                    }
                }
            }

            for (int sexNumber : new int[]{0, 1}) {
                String fileName = reportFileName + "-" + sexLabel[sexNumber] + "." + fileType.toString();
                File file = new File(fileName);

                TreeSet<CancerCasesCount> casesCounts;
                Double restCount = Tools.sumUpTheRest(theRestMale, dontCount);

                if (sexNumber == 0) {
                    casesCounts = topNMale;
                } else {
                    casesCounts = topNFemale;
                }


                if (useR
                        && !fileType.equals(FileTypes.jchart)
                        && !fileType.equals(FileTypes.csv)) {
                    String header = "Top 10 by " + countType + ", \n" + tableHeader + ", " + TableBuilderInterface.sexLabel[sexNumber];
                    generatedFiles.addAll(Tools.generateRChart(casesCounts, fileName, header, fileType, chartType, includeOther, restCount, rpath, true, "Site"));
                } else {
                    double allCount = countArray[allCancerGroupsIndex][sexNumber];
                    Color color;
                    if (sexNumber == 0) {
                        color = Color.BLUE;
                    } else {
                        color = Color.RED;
                    }
                    String header = "Top 10 by " + countType + ", " + tableHeader + ", " + TableBuilderInterface.sexLabel[sexNumber];
                    charts[sexNumber] = Tools.generateJChart(casesCounts, fileName, header, fileType, chartType, includeOther, legendOn, restCount, allCount, color, "Site");
                    try {
                        generatedFiles.add(Tools.writeJChartToFile(charts[sexNumber], file, fileType));
                    } catch (IOException ex) {
                        Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (DocumentException ex) {
                        Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return generatedFiles;
    }

    @Override
    public boolean areThesePopulationDatasetsCompatible(PopulationDataset[] populations) {
        return true;
    }

    @Override
    public JFreeChart[] getCharts() {

        // set the plots circular before returning them if we have a pie chart
        if (chartType == ChartType.PIE) {
            for (JFreeChart chart : charts) {
                PiePlot plot = (PiePlot) chart.getPlot();
                plot.setCircular(true);
            }
        }

        return charts;
    }

    @Override
    public void setUnknownAgeCode(int unknownAgeCode) {
        this.unknownAgeCode = unknownAgeCode;
    }
}
