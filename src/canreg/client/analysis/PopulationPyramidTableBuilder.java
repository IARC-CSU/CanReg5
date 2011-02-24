package canreg.client.analysis;

import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.server.database.AgeGroupStructure;
import canreg.server.database.PopulationDataset;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class PopulationPyramidTableBuilder extends TableBuilder {

    //Some parameters
    int baseline = 500;
    int center = 295;
    int height = 10;
    int maxlength = 200;
    int between = 30;
    private String[] tableLabel;
    private String[] sexLabel;
    private Globals.StandardVariableNames[] variablesNeeded = null;
    private FileTypes[] fileTypesGenerated = new FileTypes[]{FileTypes.ps};

    public PopulationPyramidTableBuilder() {
        super();
        fileTypesGenerated = new FileTypes[]{FileTypes.ps};
    }

    @Override
    public LinkedList<String> buildTable(String registryLabel, String reportFileName, int startYear, int endYear, Object[][] incidenceData, PopulationDataset[] populations, PopulationDataset[] standardPopulations, LinkedList<ConfigFields> configList, String[] engineParameters) throws NotCompatibleDataException {

        LinkedList<String> generatedFiles = new LinkedList<String>();

        tableLabel = ConfigFieldsReader.findConfig("table_label",
                configList);
        sexLabel = ConfigFieldsReader.findConfig("sex_label", configList);

        numberOfYears = endYear - startYear + 1;

        int[] years = {startYear, endYear};
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

        double highest = findHighestPopulationCount(populationArray);

        // Construct the PS-file
        String tableFileName = reportFileName + ".ps";
        generatedFiles.add(tableFileName);

        System.out.println("Constructing " + tableFileName + ".");

        try {
            FileWriter fw = new FileWriter(tableFileName);
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
            fw.write("0 760 MT (" + populations[0].getPopulationDatasetName() + ") CS\n");
            fw.write("0 750 MT (" + tableLabel[0] + ") CS\n");
            fw.write("/col 20 def\n");

            fw.write("Tablefont SF\n");
            /*
            if (numberOfFoundYears8 > 0) {
            // draw the old data
            offset = baseline;
            fw.write("[2 1] 0 setdash\n");
            for (int i = 1; i <= highestPopulationAgeGroup; i++) {
            if (foundAgeGroups8[i]) {
            if (i != highestPopulationAgeGroup) {
            if (populationArray8[0][i] < populationArray[0][i]) {
            fw.write(drawBox((int) (center - between / 2 -
            maxlength * populationArray8[0][i] /
            highest),
            offset,
            (int) (center - between / 2 -
            maxlength * populationArray[0][i] /
            highest),
            offset + height, 0.90, false));
            }
            
            fw.write(drawBox(center - between / 2, offset,
            (int) (center - between / 2 -
            maxlength * populationArray8[0][i] /
            highest),
            offset + height));
            
            if (populationArray8[1][i] < populationArray[1][i]) {
            fw.write(drawBox((int) (center + between / 2 +
            maxlength * populationArray8[1][i] /
            highest),
            offset,
            (int) (center + between / 2 +
            maxlength * populationArray[1][i] /
            highest),
            offset + height, 0.90, false));
            }
            
            fw.write(drawBox(center + between / 2, offset,
            (int) (center + between / 2 +
            maxlength * populationArray8[1][i] /
            highest),
            offset + height));
            
            }
            
            else {
            fw.write(drawTriangle(center - between / 2,
            offset,
            (int) (center - between / 2 -
            maxlength *
            (2 * populationArray8[0][i] /
            ((100 / 5) -
            highestPopulationAgeGroup)) /
            highest),
            offset,
            center - between / 2,
            baseline + height * (100 / 5)));
            fw.write(drawTriangle(center + between / 2,
            offset,
            (int) (center + between / 2 +
            maxlength *
            (2 * populationArray8[1][i] /
            ((100 / 5) -
            highestPopulationAgeGroup)) /
            highest),
            offset,
            center + between / 2,
            baseline + height * (100 / 5)));
            
            }
            }
            offset += height;
            }
            }
             */
            // draw the new data
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
        return generatedFiles;
    }

    @Override
    public boolean areThesePopulationDatasetsOK(PopulationDataset[] sets) {
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

    @Override
    public FileTypes[] getFileTypesGenerated() {
        return fileTypesGenerated;
    }
}
