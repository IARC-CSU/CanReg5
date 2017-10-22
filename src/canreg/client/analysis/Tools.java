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

import canreg.client.analysis.TableBuilderInterface.ChartType;
import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.LocalizationHelper;
import canreg.common.database.IncompatiblePopulationDataSetException;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.DefaultPieDataset;
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

    public static String convertStreamToString(java.io.InputStream is) throws java.util.NoSuchElementException {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    public static LinkedList<String> generateRChart(
            Collection<CancerCasesCount> casesCounts,
            String fileName,
            String header,
            FileTypes fileType,
            ChartType chartType,
            boolean includeOther,
            Double restCount,
            String rpath,
            boolean sortByCount, String xlab) {
        LinkedList<String> generatedFiles = new LinkedList<String>();

        RFileBuilder rff = new RFileBuilder();

        File script = new File(Globals.R_SCRIPTS_PATH + "/makeSureGgplot2IsInstalled.R");
        rff.appendHeader(script.getAbsolutePath());

        rff.appendFileTypePart(fileType, fileName);

        generatedFiles.add(fileName);

        rff.appendData(casesCounts, restCount, includeOther);

        int numberOfCategories = casesCounts.size();
        if (includeOther) {
            numberOfCategories += 1;
        }
        if (sortByCount) {
            rff.appendSort(chartType, numberOfCategories, includeOther, restCount);
        }

        rff.appendPlots(chartType, header, xlab);
        rff.appendWriteOut();

        System.out.println(rff.getScript());

        try {
            File tempFile = File.createTempFile("script", ".R");
            // generatedFiles.add(tempFile.getPath());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(tempFile), "UTF8"));
            writer.append(rff.getScript());
            writer.close();
            Tools.callR(tempFile.getAbsolutePath(), rpath, fileName + "-report.txt");
        } catch (TableErrorException ex) {
            Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return generatedFiles;
    }

    public static JFreeChart generateJChart(
            Collection<CancerCasesCount> casesCounts,
            String fileName,
            String header,
            FileTypes fileType,
            ChartType chartType,
            boolean includeOther,
            boolean legendOn,
            Double restCount,
            Double allCount,
            Color color,
            String labelsCategoryName) {
        JFreeChart chart;
        if (chartType == ChartType.PIE) {
            NumberFormat format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(1);
            DefaultPieDataset dataset = new DefaultKeyedValuesDataset();
            int position = 0;
            for (CancerCasesCount count : casesCounts) {
                dataset.insertValue(position++,
                        count.toString()
                        + " (" + format.format(count.getCount() / allCount * 100) + "%)",
                        count.getCount());
            }
            if (includeOther) {
                dataset.insertValue(position++,
                        "Other: " + restCount.intValue()
                        + " (" + format.format(restCount / allCount * 100) + "%)",
                        restCount);
            }
            chart = ChartFactory.createPieChart(
                    header,
                    dataset, legendOn, false, Locale.getDefault());
            Tools.setPiePlotColours(chart, casesCounts.size() + 1, color.brighter());

        } else { // assume barchart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (CancerCasesCount count : casesCounts) {
                dataset.addValue(count.getCount(),
                        count.getLabel(),
                        count.toString());
            }
            if (includeOther) {
                dataset.addValue(restCount.intValue(), "Other", "Other: " + restCount);
            }
            chart = ChartFactory.createStackedBarChart(
                    header,
                    labelsCategoryName,
                    "Cases",
                    dataset,
                    PlotOrientation.HORIZONTAL,
                    legendOn, true, false);

            Tools.setBarPlotColours(chart, casesCounts.size() + 1, color.brighter());
        }
        return chart;
    }

    static String writeJChartToFile(JFreeChart chart, File file, FileTypes fileType) throws IOException, DocumentException {
        String fileName = file.getPath();
        if (fileType.equals(FileTypes.svg)) {
            Tools.exportChartAsSVG(chart, new Rectangle(1000, 1000), file);
        } else if (fileType.equals(FileTypes.pdf)) {
            Tools.exportChartAsPDF(chart, new Rectangle(500, 400), file);
        } else if (fileType.equals(FileTypes.jchart)) {
        } else if (fileType.equals(FileTypes.csv)) {
            Tools.exportChartAsCSV(chart, file);
        } else {
            ChartUtilities.saveChartAsPNG(file, chart, 1000, 1000);
        }
        return fileName;
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
     * @throws canreg.common.database.IncompatiblePopulationDataSetException
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
                    //<ictl.co>
                    if(LocalizationHelper.isRtlLanguageActive()){
                        if(LocalizationHelper.isPersianLocale() && DateHelper.isGregorianYear(thisYear)){
                            int _thisYear = DateHelper.convertGregorianYearToPersianYear(thisYear);
                            popoutput.append(_thisYear + "").append(separator);
                        }else{
                            popoutput.append(thisYear + "").append(separator);
                        }
                    }else{
                        popoutput.append(thisYear + "").append(separator);
                    }
                    //</ictl.co>
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

    public static LinkedList<String> callR(String rScript, String rpath, String reportFileName) throws TableErrorException {
        LinkedList<String> filesCreated = new LinkedList<String>();
        Runtime rt = Runtime.getRuntime();
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(rpath);
        commandList.add("CMD");
        commandList.add("BATCH");
        commandList.add("--vanilla");
        commandList.add("--slave");
        commandList.add(rScript);
        commandList.add(reportFileName);

        //String command = canreg.common.Tools.encapsulateIfNeeded(rpath)
        //        + " CMD BATCH --vanilla --slave "
        //        + canreg.common.Tools.encapsulateIfNeeded(rScript) + " "
        //        + canreg.common.Tools.encapsulateIfNeeded(reportFileName);
        
        System.out.println(commandList);
        Process pr = null;
        try {
            pr = rt.exec(commandList.toArray(new String[]{}));
            // collect the output from the R program in a stream
            // BufferedInputStream is = new BufferedInputStream(pr.getInputStream());
            pr.waitFor();
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(reportFileName));
            // convert the output to a string
            String theString = convertStreamToString(is);
            Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.INFO, "Messages from R: \n{0}", theString);
            // System.out.println(theString);
            // and add all to the list of files to return
            for (String fileName : theString.split("\n")) {
                if (fileName.startsWith("-outFile:")) {
                    fileName = fileName.replaceFirst("-outFile:", "");
                    if (new File(fileName).exists()) {
                        filesCreated.add(fileName);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(RTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.util.NoSuchElementException ex) {
            Logger.getLogger(RTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
            if (pr != null) {
                BufferedInputStream errorStream = new BufferedInputStream(pr.getErrorStream());
                String errorMessage = convertStreamToString(errorStream);
                System.out.println(errorMessage);
                throw new TableErrorException("R says:\n \"" + errorMessage + "\"");
            }      
        } catch (IOException ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pr != null) {
                System.out.println(pr.exitValue());
            }
        }
        return filesCreated;
    }

    public static double sumUpTheRest(LinkedList<CancerCasesCount> theRestList, List<Integer> dontCountIndexes) {
        double theRest = 0;
        for (CancerCasesCount count : theRestList) {
            if (!dontCountIndexes.contains((Integer) count.getIndex())) {
                theRest += count.getCount();
            } else {
                System.out.println("Found...");
            }
        }
        return theRest;
    }

    public static void setPiePlotColours(JFreeChart chart, int numberOfSections, Color baseColor) {
        Color color = baseColor;
        PiePlot plot = (PiePlot) chart.getPlot();
        for (int i = 0; i < numberOfSections; i++) {
            try {
                plot.setSectionOutlinePaint(plot.getDataset().getKey(i), baseColor.darker().darker().darker());
                color = darken(color);
                plot.setSectionPaint(plot.getDataset().getKey(i), color);
            } catch (java.lang.IndexOutOfBoundsException ex){
                // not data for all the categories - that is fine
                Logger.getLogger(TopNChartTableBuilder.class.getName()).log(Level.INFO, null, ex);
            }
        }
    }

    public static void setBarPlotColours(JFreeChart chart, int numberOfSections, Color baseColor) {
        Color color = baseColor;
        BarRenderer renderer = (BarRenderer) ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        for (int i = 0; i < numberOfSections; i++) {
            renderer.setSeriesPaint(i, color);
            color = darken(color);
        }
    }

    public static Color darken(Color color) {
        return new Color(
                (int) Math.floor(color.getRed() * .9),
                (int) Math.floor(color.getGreen() * .9),
                (int) Math.floor(color.getBlue() * .9));
    }
}
