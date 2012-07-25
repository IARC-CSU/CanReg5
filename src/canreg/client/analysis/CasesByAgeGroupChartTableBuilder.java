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

import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.client.analysis.Tools.KeyCancerGroupsEnum;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.PopulationDataset;
import com.itextpdf.text.DocumentException;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.DefaultPieDataset;

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

    public static enum ChartType {

        PIE,
        BAR
    }
    private LinkedList[] cancerGroupsLocal;
    private String[] sexLabel;
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

        sexLabel = new String[]{
            java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("MALE"),
            java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AbstractEditorialTableBuilder").getString("FEMALE")
        };

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
                    System.out.println("Not counted: " + icdString + "/" + morphologyString);
                }
            }

            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(1);
            for (int sexNumber : new int[]{0, 1}) {

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

                int position = 0;
                if (chartType == ChartType.BAR) {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (CancerCasesCount count : casesCounts) {
                        dataset.addValue(count.getCount().intValue(),
                                count.getLabel(),
                                count.toString() + " (" + format.format(count.getCount() / total * 100) + "%)");
                    }
                    charts[sexNumber] = ChartFactory.createStackedBarChart(
                            tableHeader + ", " + sexLabel[sexNumber] + ", " + total.intValue() + " Cases",
                            "Age group",
                            "Cases",
                            dataset,
                            PlotOrientation.HORIZONTAL,
                            legendOn, true, false);
                    if (sexNumber == 0) {
                        setBarPlotColours(charts[sexNumber], ageGroups.size(), Color.BLUE.brighter());
                    } else {
                        setBarPlotColours(charts[sexNumber], ageGroups.size(), Color.RED.brighter());
                    }
                } else { // assume piechart
                    DefaultPieDataset dataset = new DefaultKeyedValuesDataset();
                    for (CancerCasesCount count : casesCounts) {
                        dataset.insertValue(position++,
                                count.toString() + " (" + format.format(count.getCount() / total * 100) + "%)",
                                count.getCount());
                    }
                    charts[sexNumber] = ChartFactory.createPieChart(
                            tableHeader + ", " + sexLabel[sexNumber] + ", " + total.intValue() + " Cases",
                            dataset, legendOn, false, Locale.getDefault());
                    if (sexNumber == 0) {
                        setPiePlotColours(charts[sexNumber], ageGroups.size(), Color.BLUE.brighter());
                    } else {
                        setPiePlotColours(charts[sexNumber], ageGroups.size(), Color.RED.brighter());
                    }
                }

                String fileName = reportFileName + "-" + sexLabel[sexNumber];
                File file = new File(fileName + "." + fileType.toString());

                try {
                    if (fileType.equals(FileTypes.svg)) {
                        Tools.exportChartAsSVG(charts[sexNumber], new Rectangle(1000, 1000), file);
                    } else if (fileType.equals(FileTypes.pdf)) {
                        Tools.exportChartAsPDF(charts[sexNumber], new Rectangle(500, 400), file);
                    } else if (fileType.equals(FileTypes.jchart)) {
                        generatedFiles.add("OK - " + sexLabel[sexNumber]);
                    } else if (fileType.equals(FileTypes.csv)) {
                        Tools.exportChartAsCSV(charts[sexNumber], file);
                    } else {
                        ChartUtilities.saveChartAsPNG(file, charts[sexNumber], 1000, 1000);
                    }
                    generatedFiles.add(file.getPath());
                } catch (IOException ex) {
                    Logger.getLogger(CasesByAgeGroupChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DocumentException ex) {
                    Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
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

    private void setPiePlotColours(JFreeChart chart, int numberOfSections, Color baseColor) {
        Color color = baseColor;
        PiePlot plot = (PiePlot) chart.getPlot();
        for (int i = 0; i < numberOfSections; i++) {
            plot.setSectionOutlinePaint(plot.getDataset().getKey(i), baseColor.darker().darker().darker());
            color = darken(color);
            plot.setSectionPaint(plot.getDataset().getKey(i), color);
        }
    }

    private void setBarPlotColours(JFreeChart chart, int numberOfSections, Color baseColor) {
        Color color = baseColor;
        BarRenderer renderer = (BarRenderer) ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        for (int i = 0; i < numberOfSections; i++) {
            renderer.setSeriesPaint(i, color);
            color = darken(color);
        }
    }

    private Color darken(Color color) {
        return new Color(
                (int) Math.floor(color.getRed() * .9),
                (int) Math.floor(color.getGreen() * .9),
                (int) Math.floor(color.getBlue() * .9));
    }

    private class CancerCasesCount implements Comparable {

        private String icd10;
        private String label;
        private Double count;
        private int index;
        private NumberFormat format;

        private CancerCasesCount(String icd10, String label, Double count, int index) {
            this.label = label;
            this.count = count;
            this.icd10 = icd10;
            this.index = index;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof CancerCasesCount) {
                CancerCasesCount other = (CancerCasesCount) o;
                return -getCount().compareTo(other.getCount());
            } else {
                return 0;
            }
        }

        /**
         * @return the icd10
         */
        public String getIcd10() {
            return icd10;
        }

        /**
         * @param icd10 the icd10 to set
         */
        public void setIcd10(String icd10) {
            this.icd10 = icd10;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * @return the count
         */
        public Double getCount() {
            return count;
        }

        /**
         * @param count the count to set
         */
        public void setCount(Double count) {
            this.count = count;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder(label);
            if (icd10 != null) {
                s.append(" (").append(icd10).append(")");
            }
            s.append(": ").append(count.intValue());
            return s.toString();
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index the index to set
         */
        public void setIndex(int index) {
            this.index = index;
        }
    }

    private class AgeGroup {

        private Integer min, max;

        public AgeGroup(Integer min, Integer max) {
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
    }
}
