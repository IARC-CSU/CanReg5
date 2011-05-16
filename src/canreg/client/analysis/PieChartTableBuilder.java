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

import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.PopulationDataset;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultKeyedValues2DDataset;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author ervikm
 */
public class PieChartTableBuilder extends AbstractEditorialTableBuilder {

    private static int YEAR_COLUMN = 0;
    private static int SEX_COLUMN = 1;
    private static int ICD10_COLUMN = 2;
    private static int MORPHOLOGY_COLUMN = 3;
    private static int BEHAVIOUR_COLUMN = 4;
    private static int CASES_COLUMN = 5;
    private static Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,};
    private static FileTypes[] fileTypesGenerated = {
        FileTypes.png
    };
    private LinkedList[] cancerGroupsLocal;
    private String[] sexLabel;
    private JFreeChart chart;
    private String[] icdLabel;
    private NumberFormat format;

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
            String[] engineParameters) throws NotCompatibleDataException {
        String footerString = java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString("TABLE BUILT ") + new Date() + java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/AgeSpecificCasesPerHundredThousandTableBuilder").getString(" BY CANREG5.");

        LinkedList<String> generatedFiles = new LinkedList<String>();

        icdLabel = ConfigFieldsReader.findConfig("ICD_groups_labels",
                configList);

        // sexLabel = ConfigFieldsReader.findConfig("sex_label", configList);

        sexLabel = new String[]{java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("MALE"), java.util.ResourceBundle.getBundle("canreg/client/analysis/resources/TableBuilder").getString("FEMALE")};

        icd10GroupDescriptions = ConfigFieldsReader.findConfig(
                "ICD10_groups",
                configList);

        cancerGroupsLocal = generateICD10Groups(icd10GroupDescriptions);

        allCancerGroupsIndex = getICD10index("ALL", icd10GroupDescriptions);

        leukemiaNOSCancerGroupIndex = getICD10index(950,
                cancerGroupsLocal);

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
                cancerGroupsLocal);

        brainAndCentralNervousSystemCancerGroupIndex = getICD10index("C70-72",
                icd10GroupDescriptions);

        ovaryCancerGroupIndex = getICD10index(569,
                cancerGroupsLocal);

        numberOfCancerGroups = cancerGroupsLocal.length;
        double[] line;
        double[] casesLine;

        if (incidenceData != null) {
            String sexString, icdString, casesString;
            String morphologyString;
            double casesArray[][] = new double[numberOfCancerGroups][numberOfSexes];

            int sex, icdNumber, icdIndex, cases;

            for (Object[] dataLine : incidenceData) {


                // Set default
                icdIndex = -1;
                cases = 0;

                // Unknown sex group = 3
                sex = 3;
                // Extract data
                sexString = (String) dataLine[SEX_COLUMN];
                sex = Integer.parseInt(sexString.trim());

                // sex = 3 is unknown sex

                if (sex > 2) {
                    sex = 3;
                }

                morphologyString = (String) dataLine[MORPHOLOGY_COLUMN];

                if (morphologyString.length() > 0) {
                    int morphology = Integer.parseInt(morphologyString);
                    if (morphology == 9140) {
                        icdIndex = kaposiSarkomaCancerGroupIndex;
                    } else if ((int) (morphology / 10) == 905) {
                        icdIndex = mesotheliomaCancerGroupIndex;
                    }
                }

                if (icdIndex < 0) {
                    icdString = (String) dataLine[ICD10_COLUMN];
                    if (icdString.length() > 0
                            && icdString.trim().substring(0, 1).equals("C")) {
                        icdString = icdString.trim().substring(1);
                        icdNumber = Integer.parseInt(icdString);
                        if (icdString.length() < 3) {
                            icdNumber = icdNumber * 10;
                        }
                        icdIndex = getICD10index(icdNumber, cancerGroupsLocal);
                        if (icdIndex == -1) {
                            icdIndex = -1;
                        }
                    } else if (icdString.length() > 0
                            && icdString.trim().substring(0, 1).equals("D")) {
                        icdString = icdString.trim().substring(1);
                        icdNumber = Integer.parseInt(icdString);
                        if (icdString.length() < 3) {
                            icdNumber = icdNumber * 10;
                        }
                        if (icdNumber == 90 || icdNumber == 414) {
                            icdIndex = bladderCancerGroupIndex;
                        } else if ((int) (icdNumber / 10) == 45 || (int) (icdNumber / 10) == 47) {
                            icdIndex = myeloproliferativeDisordersCancerGroupIndex;
                        } else if ((int) (icdNumber / 10) == 46) {
                            icdIndex = myelodysplasticSyndromesCancerGroupIndex;
                        }
                    }
                }

                // Extract cases
                cases = (Integer) dataLine[CASES_COLUMN];

                if (sex <= numberOfSexes && icdIndex >= 0
                        && icdIndex <= cancerGroupsLocal.length) {
                    casesArray[icdIndex][sex - 1] += cases;
                } else {
                    if (otherCancerGroupsIndex >= 0) {
                        casesArray[otherCancerGroupsIndex][sex
                                - 1] += cases;
                    }
                }
                if (allCancerGroupsIndex >= 0) {
                    casesArray[allCancerGroupsIndex][sex - 1] += cases;
                }
                if (allCancerGroupsButSkinIndex >= 0
                        && skinCancerGroupIndex >= 0
                        && icdIndex != skinCancerGroupIndex) {
                    casesArray[allCancerGroupsButSkinIndex][sex
                            - 1] += cases;
                }
            }

            // separate top 10 and the rest
            TreeSet<CancerCasesCount> top10Male = new TreeSet<CancerCasesCount>();
            LinkedList<CancerCasesCount> theRestMale = new LinkedList<CancerCasesCount>();

            TreeSet<CancerCasesCount> top10Female = new TreeSet<CancerCasesCount>();
            LinkedList<CancerCasesCount> theRestFemale = new LinkedList<CancerCasesCount>();


            CancerCasesCount otherElement = null;
            CancerCasesCount thisElement = null;

            TreeSet<CancerCasesCount> top10;
            LinkedList<CancerCasesCount> theRest;

            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(2);

            for (int icdGroupNumber = 0; icdGroupNumber < casesArray.length; icdGroupNumber++) {
                casesLine = casesArray[icdGroupNumber];
                for (int sexNumber = 0; sexNumber < 2; sexNumber++) {
                    if (sexNumber == 0) {
                        top10 = top10Male;
                        theRest = theRestMale;
                    } else {
                        top10 = top10Female;
                        theRest = theRestFemale;
                    }

                    thisElement = new CancerCasesCount(
                            icdLabel[icdGroupNumber].substring(3)
                            + " " + format.format(casesLine[sexNumber] / casesArray[allCancerGroupsIndex][sexNumber] * 100) + "%"
                            + " (" + icd10GroupDescriptions[icdGroupNumber] + ")",
                            casesLine[sexNumber]);
                    if (icdGroupNumber != allCancerGroupsButSkinIndex && icdGroupNumber != allCancerGroupsIndex) {
                        if (top10.size() < 10) {
                            top10.add(thisElement);
                        } else {
                            otherElement = top10.last();
                            if (thisElement.compareTo(otherElement) < 0) {
                                top10.remove(otherElement);
                                theRest.add(otherElement);
                                top10.add(thisElement);
                            } else {
                                theRest.add(thisElement);
                            }
                        }
                    }
                }
            }

            // male
            DefaultPieDataset dataset = new DefaultKeyedValuesDataset();
            int position = 0;
            int sexNumber = 0;
            double restCount = casesArray[allCancerGroupsIndex][sexNumber];
            for (CancerCasesCount count : top10Male) {
                System.out.println(count);
                restCount -= count.getCount();
                dataset.insertValue(position++, count.label, count.getCount());
            }
            dataset.insertValue(position++, "Other", restCount);
            chart = ChartFactory.createPieChart(tableHeader + sexLabel[sexNumber], dataset, true, true, Locale.getDefault());
            File file = new File(reportFileName + "-" + sexLabel[sexNumber] + ".png");
            try {
                ChartUtilities.saveChartAsPNG(file, chart, 1000, 1000);
                generatedFiles.add(file.getPath());
            } catch (IOException ex) {
                Logger.getLogger(PieChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }

            // female
            dataset = new DefaultKeyedValuesDataset();
            position = 0;
            sexNumber = 1;
            restCount = casesArray[allCancerGroupsIndex][sexNumber];
            for (CancerCasesCount count : top10Female) {
                System.out.println(count);
                restCount -= count.getCount();
                dataset.insertValue(position++, count.label, count.getCount());
            }
            dataset.insertValue(position++, "Other", restCount);
            chart = ChartFactory.createPieChart(tableHeader + sexLabel[sexNumber], dataset, true, true, Locale.getDefault());
            file = new File(reportFileName + "-" + sexLabel[sexNumber] + ".png");
            try {
                ChartUtilities.saveChartAsPNG(file, chart, 1000, 1000);
                generatedFiles.add(file.getPath());
            } catch (IOException ex) {
                Logger.getLogger(PieChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return generatedFiles;
    }

    private class CancerCasesCount implements Comparable {

        private String icd10;
        private String label;
        private Double count;

        private CancerCasesCount(String string, Double aDouble) {
            label = string;
            count = aDouble;
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
            return label + ": " + count;
        }
    }
}
