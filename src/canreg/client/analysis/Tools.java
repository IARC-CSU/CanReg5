/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.analysis;

import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Map;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class Tools {

    /**
     * 
     */
    public static int DONT_COUNT = -999;

    /**
     * 
     */
    public enum KeyGroupsEnum {

        /**
         * 
         */
        allCancerGroupsIndex,
        /**
         * 
         */
        leukemiaNOSCancerGroupIndex,
        /**
         * 
         */
        skinCancerGroupIndex,
        /**
         * 
         */
        bladderCancerGroupIndex,
        /**
         * 
         */
        mesotheliomaCancerGroupIndex,
        /**
         * 
         */
        kaposiSarkomaCancerGroupIndex,
        /**
         * 
         */
        myeloproliferativeDisordersCancerGroupIndex,
        /**
         * 
         */
        myelodysplasticSyndromesCancerGroupIndex,
        /**
         * 
         */
        allCancerGroupsButSkinIndex,
        /**
         * 
         */
        brainAndCentralNervousSystemCancerGroupIndex,
        /**
         * 
         */
        ovaryCancerGroupIndex,
        /**
         * 
         */
        otherCancerGroupsIndex,
        /**
         * 
         */
        numberOfCancerGroups
    }

    /**
     * 
     * @param popoutput
     * @param startYear
     * @param populations
     * @param separator
     * @throws IOException
     */
    public static void writePopulationsToFile(BufferedWriter popoutput, int startYear, PopulationDataset[] populations, String separator) throws IOException {
        String popheader = "YEAR" + separator;
        popheader += "AGE_GROUP_LABEL" + separator;
        popheader += "AGE_GROUP" + separator;
        popheader += "SEX" + separator;
        popheader += "COUNT";
        popoutput.append(popheader);
        popoutput.newLine();
        int thisYear = startYear;
        for (PopulationDataset popset : populations) {
            String[] ageGroupNames = popset.getAgeGroupStructure().getAgeGroupNames(); 
            for (PopulationDatasetsEntry pop : popset.getAgeGroups()) {
                popoutput.append(thisYear + "").append(separator);
                popoutput.append(ageGroupNames[pop.getAgeGroup()]).append(separator);
                popoutput.append(pop.getStringRepresentationOfAgeGroupsForFile(separator));
                popoutput.newLine();
            }
            thisYear++;
        }
        popoutput.flush();
    }

    /**
     * 
     * @param keyGroupsMap
     * @param icdString
     * @param morphologyString
     * @param cancerGroupsLocal
     * @return This is set to DONT_COUNT if it should not be counted and -1 if it is not classifiable but countable
     */
    public static int assignICDGroupIndex(Map<KeyGroupsEnum, Integer> keyGroupsMap, String icdString, String morphologyString, LinkedList[] cancerGroupsLocal) {
        int icdIndex = -1;
        int icdNumber = -1;

        // try first only with morphology
        if (morphologyString.length() > 0) {
            int morphology = Integer.parseInt(morphologyString);
            if (morphology == 9140) {
                icdIndex = keyGroupsMap.get(KeyGroupsEnum.kaposiSarkomaCancerGroupIndex);
            } else if ((int) (morphology / 10) == 905) {
                icdIndex = keyGroupsMap.get(KeyGroupsEnum.mesotheliomaCancerGroupIndex);
            }
        }

        // Not found only with morphology        
        if (icdIndex < 0) {
            if (icdString.length() > 0
                    && icdString.trim().substring(0, 1).equals("C")) {
                icdString = icdString.trim().substring(1);
                icdNumber = Integer.parseInt(icdString);
                if (icdString.length() < 3) {
                    icdNumber = icdNumber * 10;
                }
                icdIndex = EditorialTableTools.getICD10index(icdNumber, cancerGroupsLocal);
                // Group still not found - put it in others...
                if (icdIndex < 0 && keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex) >= 0) {
                    icdIndex = keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex);
                }
            } else if (icdString.length() > 0
                    && icdString.trim().substring(0, 1).equals("D")) // only collect certain Ds                        
            {
                icdString = icdString.trim().substring(1);
                icdNumber = Integer.parseInt(icdString);
                if (icdString.length() < 3) {
                    icdNumber = icdNumber * 10;
                }
                if (icdNumber == 90 || icdNumber == 414) {
                    icdIndex = keyGroupsMap.get(KeyGroupsEnum.bladderCancerGroupIndex);
                    if (icdIndex < 0 && keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex) >= 0) {
                        icdIndex = keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex);
                    }
                } else if ((int) (icdNumber / 10) == 45 || (int) (icdNumber / 10) == 47) {
                    icdIndex = keyGroupsMap.get(KeyGroupsEnum.myeloproliferativeDisordersCancerGroupIndex);
                    if (icdIndex < 0 && keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex) >= 0) {
                        icdIndex = keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex);
                    }
                } else if ((int) (icdNumber / 10) == 46) {
                    icdIndex = keyGroupsMap.get(KeyGroupsEnum.myelodysplasticSyndromesCancerGroupIndex);
                    if (icdIndex < 0 && keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex) >= 0) {
                        icdIndex = keyGroupsMap.get(KeyGroupsEnum.otherCancerGroupsIndex);
                    }
                } else {
                    icdIndex = DONT_COUNT;
                }
            }
        }
        return icdIndex;
    }

    /**
     * Exports a JFreeChart to a SVG file.
     * 
     * @param chart JFreeChart to export
     * @param bounds the dimensions of the viewport
     * @param svgFile the output file.
     * @throws IOException if writing the svgFile fails.
     */
    public static void exportChartAsSVG(JFreeChart chart, Rectangle bounds, File svgFile) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        chart.draw(svgGenerator, bounds);

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);
        outputStream.flush();
        outputStream.close();
    }
}
