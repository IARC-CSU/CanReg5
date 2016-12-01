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
import com.itextpdf.text.DocumentException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
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
public class CasesByAgeGroupChartTableBuilder implements TableBuilderInterface, JChartTableBuilderInterface {

    // private static int YEAR_COLUMN = 0;
    private static int SEX_COLUMN = 1;
    private static int ICD10_COLUMN = 2;
    private static int MORPHOLOGY_COLUMN = 3;
    // private static int BEHAVIOUR_COLUMN = 4;
    private static int AGE_COLUMN = 5;
    private static int CASES_COLUMN = 6;
    private static Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.Age,};
    private static FileTypes[] fileTypesGenerated = {
        FileTypes.png,
        FileTypes.pdf,
        FileTypes.svg,
        FileTypes.jchart,
        FileTypes.csv
    };
    private boolean legendOn = false;
    private LinkedList[] cancerGroupsLocal;
    private JFreeChart[] charts = new JFreeChart[2];
    private NumberFormat format;
    private String[] icd10GroupDescriptions;
    private int numberOfCancerGroups;
    private int numberOfSexes = 2;

    private int DONT_COUNT = -999;
    private EnumMap<KeyCancerGroupsEnum, Integer> keyGroupsMap;
    private int otherCancerGroupsIndex;
    private Integer skinCancerGroupIndex;
    private int allCancerGroupsIndex;
    private int allCancerGroupsButSkinIndex;
    private ChartType chartType;
    private boolean useR = true;
    private LocalSettings localSettings;
    private String rpath;
    private int unknownAgeInt = Globals.DEFAULT_UNKNOWN_AGE_CODE;

    public CasesByAgeGroupChartTableBuilder() {
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
        // String footerString = java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString("TABLE BUILT ") + new Date() + java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString(" BY CANREG5.");

        LinkedList<String> generatedFiles = new LinkedList<String>();

        if (Arrays.asList(engineParameters).contains("barchart")) {
            chartType = ChartType.BAR;
        } else {
            chartType = ChartType.PIE;
        }

        if (Arrays.asList(engineParameters).contains("legend")) {
            legendOn = true;
        }
        if (Arrays.asList(engineParameters).contains("r")) {
            useR = true;
        }

        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        rpath = localSettings.getProperty(LocalSettings.R_PATH);
        // does R exist?
        if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
            useR = false; // force false if R is not installed
        }

        icd10GroupDescriptions = ConfigFieldsReader.findConfig(
                "ICD10_groups",
                configList);

        cancerGroupsLocal = EditorialTableTools.generateICD10Groups(icd10GroupDescriptions);

        // indexes
        keyGroupsMap = new EnumMap<KeyCancerGroupsEnum, Integer>(KeyCancerGroupsEnum.class);

        keyGroupsMap.put(KeyCancerGroupsEnum.allCancerGroupsIndex, EditorialTableTools.getICD10index("ALL", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.skinCancerGroupIndex, EditorialTableTools.getICD10index("C44", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.otherCancerGroupsIndex, EditorialTableTools.getICD10index("O&U", icd10GroupDescriptions));
        keyGroupsMap.put(KeyCancerGroupsEnum.allCancerGroupsButSkinIndex, EditorialTableTools.getICD10index("ALLbC44", icd10GroupDescriptions));

        skinCancerGroupIndex = keyGroupsMap.get(KeyCancerGroupsEnum.skinCancerGroupIndex);
        allCancerGroupsIndex = keyGroupsMap.get(KeyCancerGroupsEnum.allCancerGroupsIndex);
        allCancerGroupsButSkinIndex = keyGroupsMap.get(KeyCancerGroupsEnum.allCancerGroupsButSkinIndex);
        otherCancerGroupsIndex = keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);

        numberOfCancerGroups = cancerGroupsLocal.length;
        int columnToCount = allCancerGroupsIndex;

        List<AgeGroup> ageGroups = new LinkedList<AgeGroup>();

        // TODO: Make these dynamic?
        ageGroups.add(new AgeGroup(0, 14));
        ageGroups.add(new AgeGroup(15, 29));
        ageGroups.add(new AgeGroup(30, 49));
        ageGroups.add(new AgeGroup(50, 69));
        ageGroups.add(new AgeGroup(70, null));

        double[] casesLine;

        if (incidenceData != null) {
            String sexString, icdString;
            String morphologyString;
            double casesArray[][][] = new double[numberOfSexes][ageGroups.size()][numberOfCancerGroups];
            double cum64Array[][][] = new double[numberOfSexes][ageGroups.size()][numberOfCancerGroups];
            double cum74Array[][][] = new double[numberOfSexes][ageGroups.size()][numberOfCancerGroups];
            double asrArray[][][] = new double[numberOfSexes][ageGroups.size()][numberOfCancerGroups];

            int sex, icdIndex, cases, age;
            List<Integer> dontCount = new LinkedList<Integer>();
            // all sites but skin?
            if (Arrays.asList(engineParameters).contains("noC44")) {
                dontCount.add(skinCancerGroupIndex);
                tableHeader += ", excluding C44";
                columnToCount = allCancerGroupsButSkinIndex;
            }

            for (Object[] dataLine : incidenceData) {

                // Set default
                icdIndex = -1;
                cases = 0;
                age = 0;

                // Extract data
                sexString = (String) dataLine[SEX_COLUMN];
                sex = Integer.parseInt(sexString.trim());

                // sex = 3 is unknown sex
                if (sex > 2) {
                    sex = 3;
                } else {
                    sex -= 1; // sex 1 male maps to column 0...
                }

                morphologyString = (String) dataLine[MORPHOLOGY_COLUMN];
                icdString = (String) dataLine[ICD10_COLUMN];

                icdIndex = Tools.assignICDGroupIndex(keyGroupsMap, icdString, morphologyString, cancerGroupsLocal);

                if (!dontCount.contains(icdIndex) && icdIndex != DONT_COUNT) {
                    // Extract cases
                    cases = (Integer) dataLine[CASES_COLUMN];
                    age = (Integer) dataLine[AGE_COLUMN];
                    for (int group = 0; group < ageGroups.size(); group++) {
                        if (ageGroups.get(group).fitsInAgeGroup(age)) {
                            if (sex <= numberOfSexes && icdIndex >= 0) {
                                casesArray[sex][group][icdIndex] += cases;
                            } else {
                                if (otherCancerGroupsIndex >= 0) {
                                    casesArray[sex][group][otherCancerGroupsIndex] += cases;
                                }
                            }
                            if (allCancerGroupsIndex >= 0) {
                                casesArray[sex][group][allCancerGroupsIndex] += cases;
                            }
                            if (allCancerGroupsButSkinIndex >= 0
                                    && skinCancerGroupIndex >= 0
                                    && icdIndex != skinCancerGroupIndex) {
                                casesArray[sex][group][allCancerGroupsButSkinIndex] += cases;
                            }
                        }
                    }
                } else {
                   // System.out.println("Not counted: " + icdString + "/" + morphologyString);
                }
            }

            //if (populations != null && populations.length > 0) {
            //    // calculate pops
            //    for (PopulationDataset pop : populations) {
            //        for (AgeGroup ag : ageGroups) {
            //            try {
            //                addPopulationDataSetToAgeGroup(pop, ag);
            //            } catch (IncompatiblePopulationDataSetException ex) {
            //                Logger.getLogger(CasesByAgeGroupChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
            //            }
            //        }
            //    }
            // }

            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(1);

            for (int sexNumber : new int[]{0, 1}) {

                String fileName = reportFileName + "-" + sexLabel[sexNumber] + "." + fileType.toString();
                File file = new File(fileName);

                List<CancerCasesCount> casesCounts = new LinkedList<CancerCasesCount>();
                Double total = 0.0;

                for (int group = 0; group < ageGroups.size(); group++) {
                    CancerCasesCount thisElement = new CancerCasesCount(
                            null,
                            ageGroups.get(group).toString(),
                            0.0,
                            group);
                    casesLine = casesArray[sexNumber][group];
                    thisElement.setCount(thisElement.getCount() + casesLine[columnToCount]);
                    total += casesLine[columnToCount];
                    casesCounts.add(thisElement);
                }

                if (useR
                        && !fileType.equals(FileTypes.jchart)
                        && !fileType.equals(FileTypes.csv)) {
                    String header = tableHeader + ", \n" + TableBuilderInterface.sexLabel[sexNumber];
                    generatedFiles.addAll(Tools.generateRChart(casesCounts, fileName, header, fileType, chartType, false, 0.0, rpath, false, "Age Group"));
                } else {
                    Color color;
                    if (sexNumber == 0) {
                        color = Color.BLUE;
                    } else {
                        color = Color.RED;
                    }
                    String header = tableHeader + ", " + TableBuilderInterface.sexLabel[sexNumber];

                    charts[sexNumber] = Tools.generateJChart(casesCounts, fileName, header, fileType, chartType, false, legendOn, 0.0, total, color, "Age Group");
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

    private void addPopulationDataSetToAgeGroup(PopulationDataset pds, AgeGroup ag) throws IncompatiblePopulationDataSetException {
        Double count = 0.0;
        Double refCount = 0.0;
        for (int sex : new int[]{0, 1}) {
            for (int age = ag.getMin(); age <= ag.getMax(); age++) {
                int index = pds.getAgeGroupIndex(age);
                count += pds.getAgeGroupCount(sex, index);
                refCount += pds.getWorldPopulationForAgeGroupIndex(sex, index);
            }
            ag.setPopulation(sex, count / (ag.getMax() - ag.getMin() + 1));
            ag.setReferencePopulation(sex, refCount / (ag.getMax() - ag.getMin() + 1));
        }
    }

    private class AgeGroup {

        private Integer min, max;
        private Double[] populationCount;
        private Double[] referencePopulationCount;

        public AgeGroup(Integer min, Integer max) {
            populationCount = new Double[2];
            referencePopulationCount = new Double[2];
            this.min = min;
            if (max == null) {
                this.max = Integer.MAX_VALUE;
            } else {
                this.max = max;
            }
        }

        public boolean fitsInAgeGroup(Integer age) {
            return (age >= min && age <= max);
        }

        @Override
        public String toString() {
            if (max < Integer.MAX_VALUE) {
                return min + " - " + max;
            } else {
                return min + "+";
            }
        }

        public void setPopulation(int sex, Double population) {
            populationCount[sex] = population;
        }

        public Double getPopulation(int sex) {
            return populationCount[sex];
        }

        public void setReferencePopulation(int sex, Double population) {
            this.referencePopulationCount[sex] = population;
        }

        public Double getReferencePopulation(int sex) {
            return referencePopulationCount[sex];
        }

        private int getMin() {
            return min;
        }

        private int getMax() {
            return max;
        }
    }
    @Override
    public void setUnknownAgeCode(int unknownAgeCode) {
        this.unknownAgeInt = unknownAgeCode;
    }
}
