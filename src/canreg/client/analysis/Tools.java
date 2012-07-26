/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.analysis;

import canreg.common.database.IncompatiblePopulationDataSetException;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Map;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
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

    public static String getChartData(JFreeChart chart, String separatingCharacter, boolean quotesOn) {

        String endLine = "\n";
        String quotes = "";
        if (quotesOn) {
            quotes = "\"";
        }

        StringBuilder stringBuilder = new StringBuilder();
        String plotType = chart.getPlot().getPlotType();
        // System.out.println("Plot Type: " + plotType);
        
        if (plotType.equalsIgnoreCase("Pie Plot")) {
            PiePlot plot = (PiePlot) chart.getPlot();
            PieDataset dataset = plot.getDataset();

            Comparable key;

            for (int i = 0; i < dataset.getKeys().size(); i++) {
                key = dataset.getKey(i);
                stringBuilder.append(quotes).append(key).append(quotes).append(separatingCharacter).append(quotes).append(dataset.getValue(key)).append(quotes).append(endLine);
            }
        } else if (plotType.equalsIgnoreCase("Category Plot")) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            CategoryDataset dataset = plot.getDataset();

            Comparable rowkey;
            Comparable columnkey;

            for (int r = 0; r < dataset.getRowCount(); r++) {
                rowkey = dataset.getRowKey(r);
                columnkey = dataset.getColumnKey(r); // this is weird... but it works!
                stringBuilder.append(quotes).append(columnkey).append(quotes).append(separatingCharacter).append(quotes).append(dataset.getValue(rowkey, columnkey)).append(quotes).append(endLine);
            }
        }
        return stringBuilder.toString();
    }

    public static void exportChartAsCSV(JFreeChart jFreeChart, File file) throws IOException {
        BufferedWriter bos = new BufferedWriter(new FileWriter(file));
        bos.append(getChartData(jFreeChart, ",", true));
        bos.flush();
        bos.close();
    }

    /**
     * 
     */
    public enum KeyCancerGroupsEnum {

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
    public static void writePopulationsToFile(
            BufferedWriter popoutput,
            int startYear,
            PopulationDataset[] populations,
            String separator) throws IOException, IncompatiblePopulationDataSetException {
        String popheader = "YEAR" + separator;
        popheader += "AGE_GROUP_LABEL" + separator;
        popheader += "AGE_GROUP" + separator;
        popheader += "SEX" + separator;
        popheader += "COUNT" + separator;
        popheader += "REFERENCE_COUNT";
        popoutput.append(popheader);
        popoutput.newLine();
        int thisYear = startYear;
        for (PopulationDataset popset : populations) {
            if (popset != null) {
                String[] ageGroupNames = popset.getAgeGroupStructure().getAgeGroupNames();
                for (PopulationDatasetsEntry pop : popset.getAgeGroups()) {
                    popoutput.append(thisYear + "").append(separator);
                    popoutput.append(ageGroupNames[pop.getAgeGroup()]).append(separator);
                    popoutput.append(pop.getStringRepresentationOfAgeGroupsForFile(separator)).append(separator);
                    // get reference pop
                    popoutput.append(popset.getWorldPopulationForAgeGroupIndex(pop.getSex(), pop.getAgeGroup()) + "");
                    popoutput.newLine();
                }
            }
            thisYear++;
        }
        popoutput.flush();
    }

    /**
     * 
     * @param keyGroupsMap - Important - needs a otherCancerGroupsIndex to be able to calculate all sites
     * @param icdString
     * @param morphologyString
     * @param cancerGroupsLocal
     * @return This is set to DONT_COUNT if it should not be counted and -1 if it is not classifiable but countable
     */
    public static int assignICDGroupIndex(
            Map<KeyCancerGroupsEnum, Integer> keyGroupsMap,
            String icdString,
            String morphologyString,
            LinkedList[] cancerGroupsLocal) {
        Integer icdIndex = null;
        Integer icdNumber = -1;

        // try first only with morphology
        if (morphologyString.length() > 0) {
            int morphology = Integer.parseInt(morphologyString);
            if (morphology == 9140 && keyGroupsMap.get(KeyCancerGroupsEnum.kaposiSarkomaCancerGroupIndex) != null) {
                icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.kaposiSarkomaCancerGroupIndex);
            } else if ((int) (morphology / 10) == 905 && keyGroupsMap.get(KeyCancerGroupsEnum.mesotheliomaCancerGroupIndex) != null) {
                icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.mesotheliomaCancerGroupIndex);
            }
        }

        // Not found only with morphology        
        if (icdIndex == null || icdIndex < 0) {
            if (icdString.length() > 0
                    && icdString.trim().substring(0, 1).equals("C")) {
                icdString = icdString.trim().substring(1);
                icdNumber = Integer.parseInt(icdString);
                if (icdString.length() < 3) {
                    icdNumber = icdNumber * 10;
                }
                icdIndex = EditorialTableTools.getICD10index(icdNumber, cancerGroupsLocal);
                // Group still not found - put it in others...
                if (icdIndex < 0 && keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex) != null && keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex) >= 0) {
                    icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);
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
                    icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.bladderCancerGroupIndex);
                    if (icdIndex == null || icdIndex < 0 && keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex) >= 0) {
                        icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);
                    }
                } else if ((int) (icdNumber / 10) == 45 || (int) (icdNumber / 10) == 47) {
                    icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.myeloproliferativeDisordersCancerGroupIndex);
                    if (icdIndex == null || icdIndex < 0 && keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex) >= 0) {
                        icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);
                    }
                } else if ((int) (icdNumber / 10) == 46) {
                    icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.myelodysplasticSyndromesCancerGroupIndex);
                    if (icdIndex == null || icdIndex < 0 && keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex) >= 0) {
                        icdIndex = keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);
                    }
                } else {
                    icdIndex = DONT_COUNT;
                }
            }
        }
        if (icdIndex == null) {
            if (keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex) != null) {
                return keyGroupsMap.get(KeyCancerGroupsEnum.otherCancerGroupsIndex);
            } else {
                return -1;
            }
        } else {
            return icdIndex;
        }
    }

    /**
     * Exports a JFreeChart to a SVG file.
     * 
     * @param chart JFreeChart to export
     * @param bounds the dimensions of the viewport
     * @param svgFile the output file.
     * @throws IOException if writing the svgFile fails.
     */
    public static void exportChartAsSVG(
            JFreeChart chart,
            Rectangle bounds,
            File svgFile) throws IOException {
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

    public static void exportChartAsPDF(
            JFreeChart chart,
            Rectangle bounds,
            File file) throws IOException, DocumentException {

        System.out.println(file.getPath());

        PdfWriter writer = null;
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();

        document.addCreator("CanReg5");
        document.addCreationDate();

        writer = PdfWriter.getInstance(document, new FileOutputStream(
                file));
        document.open();
        PdfContentByte contentByte = writer.getDirectContent();
        PdfTemplate template = contentByte.createTemplate(bounds.width, bounds.height);
        Graphics2D graphics2d = template.createGraphics(bounds.width, bounds.height,
                new DefaultFontMapper());
        Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, bounds.width,
                bounds.height);

        chart.draw(graphics2d, rectangle2d);

        graphics2d.dispose();
        contentByte.addTemplate(template, 0, 0);

        document.close();
    }
}
