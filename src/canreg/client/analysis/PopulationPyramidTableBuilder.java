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
import canreg.common.PsToPdfConverter;
import canreg.common.database.AgeGroupStructure;
import canreg.common.database.PopulationDataset;
import com.ibm.icu.util.Calendar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class PopulationPyramidTableBuilder extends AbstractEditorialTableBuilder {

    //Some parameters
    int baseline = 500;
    int center = 295;
    int height = 10;
    int maxlength = 200;
    int between = 30;
    private String[] tableLabel;
    private String[] sexLabel;
    private Globals.StandardVariableNames[] variablesNeeded = null;
    private String populationString;
    
    public PopulationPyramidTableBuilder() {
        super();
        fileTypesGenerated = new FileTypes[]{FileTypes.ps};
    }

    @Override
    public LinkedList<String> buildTable(
            String registryLabel,
            String reportFileName,
            int startYear,
            int endYear,
            Object[][] incidenceData,
            PopulationDataset[] populations,
            PopulationDataset[] standardPopulations,
            LinkedList<ConfigFields> configList,
            String[] engineParameters,
            FileTypes fileType) throws NotCompatibleDataException {

        LinkedList<String> generatedFiles = new LinkedList<String>();

        tableLabel = ConfigFieldsReader.findConfig("table_label",
                configList);
        sexLabel = ConfigFieldsReader.findConfig("sex_label", configList);

        numberOfYears = endYear - startYear + 1;

        // get the pops
        // translate and scan
        double[][] populationArray = new double[numberOfSexes][numberOfAgeGroups + 1];
        foundAgeGroups = new boolean[numberOfAgeGroups + 1];

        for (PopulationDataset population : populations) {
            population.addPopulationDataToArrayForTableBuilder(populationArray, foundAgeGroups, new AgeGroupStructure(5, 85, 1));
        }

        highestPopulationAgeGroup = findHighestAgeGroup(foundAgeGroups);

        // combine group 0 and 1
        populationArray[0][1] += populationArray[0][0];
        populationArray[1][1] += populationArray[1][0];
        ageLabel[1] = "0-";

        ageLabel[highestPopulationAgeGroup] = ageLabel[highestPopulationAgeGroup].substring(0,
                ageLabel[highestPopulationAgeGroup].length() - 1) + "+";

        // adjust from person years to an average
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i <= allAgeGroupsIndex; i++) {
                populationArray[j][i] /= numberOfYears;
            }
        }

        // calculate all age groups group
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < allAgeGroupsIndex; i++) {
                populationArray[j][allAgeGroupsIndex] += populationArray[j][i];
            }
        }

        double highest = findHighestPopulationCount(populationArray);

        populationString = populations[0].getPopulationDatasetName();

        int lastCommaPlace = populationString.lastIndexOf(",");

        if (lastCommaPlace != -1) {
            populationString = populationString.substring(0, lastCommaPlace);
        }

        // Construct the PS-file
        String tableFileName = reportFileName + ".ps";
        generatedFiles.add(tableFileName);

        System.out.println("Constructing " + tableFileName + ".");

        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(tableFileName), "UTF-8");
            
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);

            int Y, X;
            int offset = 0;
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
            fw.write("/Helvetica-Bold FF 12 SCF def\n");
            fw.write("/Titlefont\n");
            fw.write("/Helvetica-Bold FF 10 SCF def\n");
            fw.write("/Tablefont\n");
            fw.write("/Times-Roman FF 8 SCF def\n");
            fw.write("/ASRfont\n");
            fw.write("/Times-Bold FF 8 SCF def\n");
            fw.write(
                    "/RS {dup stringwidth pop col exch sub 0 rmoveto show} def\n");
            fw.write(
                    "/CS {dup stringwidth pop 580 exch sub 2 div 0 rmoveto show} def\n");
            fw.write("newpath\n");
            fw.write("Mainfont SF\n");

            fw.write("0 780 MT (" + registryLabel + ") CS\n");
            fw.write("Titlefont SF\n");
            fw.write("0 760 MT (" + populationString + ") CS\n");
            fw.write("0 750 MT (" + tableLabel[0] + ") CS\n");
            fw.write("/col 20 def\n");

            fw.write("Tablefont SF\n");

            offset = baseline;
            fw.write("[] 0 setdash\n");

            fw.write(".7 setlinewidth\n");

            for (int i = 1; i <= highestPopulationAgeGroup; i++) {
                if (foundAgeGroups[i]) {

                    if (i != highestPopulationAgeGroup) {

                        fw.write(drawBox(center - between / 2,
                                offset,
                                (int) (center - between / 2
                                - maxlength
                                * populationArray[0][i]
                                / highest),
                                offset + height));
                        fw.write(drawBox(center + between / 2, offset,
                                (int) (center + between / 2
                                + maxlength
                                * populationArray[1][i]
                                / highest),
                                offset + height));
                    } else {
                        fw.write(drawTriangle(center - between / 2,
                                offset,
                                (int) (center - between / 2
                                - maxlength
                                * (2 * populationArray[0][i]
                                / ((100 / 5) - highestPopulationAgeGroup))
                                / highest),
                                offset,
                                center - between / 2,
                                baseline + height * (100 / 5)));
                        fw.write(drawTriangle(center + between / 2,
                                offset,
                                (int) (center + between / 2
                                + maxlength
                                * (2 * populationArray[1][i]
                                / ((100 / 5) - highestPopulationAgeGroup))
                                / highest),
                                offset,
                                center + between / 2,
                                baseline + height * (100 / 5)));

                    }
                    fw.write((center - maxlength - 50) + " "
                            + (offset + height / 4) + " MT ("
                            + (int) populationArray[0][i] + ") RS\n");

                    fw.write((center - between / 2) + " "
                            + (offset + height / 4) + " MT ("
                            + ageLabel[i] + ") RS\n");

                    fw.write((center + maxlength + 25) + " "
                            + (offset + height / 4) + " MT ("
                            + (int) populationArray[1][i] + ") show\n");
                }
                offset += height;
            }

            // totals new data
            fw.write((center - maxlength - 50) + " "
                    + (baseline - 30) + " MT ("
                    + (int) populationArray[0][allAgeGroupsIndex] + ") RS\n");

            //   fw.write((center - between / 2) + " " +
            //            (baseline - 30) + " MT (" +
            //            ageLabel[allAgeGroupsIndex] + ") RS\n");

            fw.write((center + maxlength + 25) + " "
                    + (baseline - 30) + " MT ("
                    + (int) populationArray[1][allAgeGroupsIndex] + ") show\n");


            // draw the frame
            fw.write("[] 0 setdash\n");
            /*
            fw.write(drawBox(center - maxlength - 20, baseline,
            center + maxlength + 20,
            baseline + height * numberOfAgeGroups));
             */
            fw.write("0 " + (baseline - 15)
                    + " MT (" + sexLabel[0] + "                            "
                    + sexLabel[1] + ") CS\n");

            String filterString;

            if (populations[0].getFilter().length() > 0) {
                filterString = "Filter associated with this dataset: " + populations[0].getFilter();
            } else {
                filterString = "No filter associated with this dataset.";
            }

            fw.write("0 " + (baseline - 30)
                    + " MT ( " + filterString + " ) CS\n");

            fw.write("0 setlinewidth\n");

            fw.write(drawBox(center - between / 2 - maxlength, baseline,
                    center + between / 2 + maxlength,
                    baseline + height * numberOfAgeGroups));

            fw.write(drawBox(center - between / 2 - maxlength / 2, baseline,
                    center + between / 2 + maxlength / 2,
                    baseline + height * numberOfAgeGroups));

            fw.write((center - between / 2 - 5 - maxlength / 2) + " "
                    + (baseline - 7) + " MT (50%) show\n");
            fw.write((center + between / 2 - 5 + maxlength / 2) + " "
                    + (baseline - 7) + " MT (50%) show\n");

            fw.write(drawBox(center - between / 2, baseline,
                    center + between / 2,
                    baseline + height * numberOfAgeGroups));

            //fw.write(drawBox(200, 200, 300, 300));

            Calendar cal = Calendar.getInstance();
            //fw.write("0 20 MT ("+dayLabel[date.-1] + " " + cal.DAY_OF_MONTH + " " + monthLabel[cal.MONTH-1] + " " + cal.YEAR +" - "+ cal.HOUR_OF_DAY +":"+cal.MINUTE+") CS\n");
            fw.write("0 20 MT (" + cal.getTime().toString() + ") CS\n");

            fw.write("showpage\n");
            fw.close();

        } catch (IOException e) {
            System.out.println("Error while writing PS-file...");
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

        return generatedFiles;
    }

    @Override
    public boolean areThesePopulationDatasetsCompatible(PopulationDataset[] sets) {
        boolean OK = true;
        for (PopulationDataset pds : sets) {
            OK = OK && pds.getAgeGroupStructure().getSizeOfGroups() == 5;
        }
        return OK;
    }

    @Override
    public StandardVariableNames[] getVariablesNeeded() {
        return null;
    }
}
