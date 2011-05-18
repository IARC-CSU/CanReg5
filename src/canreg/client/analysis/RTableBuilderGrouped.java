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

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RTableBuilderGrouped implements TableBuilderInterface {

    public static String VARIABLES_NEEDED = "variables_needed";
    public static String FILE_TYPES_GENERATED = "file_types_generated";
    public static String R_SCRIPTS = "r_scripts";
    private static Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.BasisDiagnosis};
    private FileTypes[] fileTypesGenerated;
    private String separator = "\t";
    private final LocalSettings localSettings;
    private final String rpath;
    private final String[] tableLabel;
    private final String[] icdLabel;
    private final String[] icd10GroupDescriptions;
    private final LinkedList[] cancerGroupsLocal;
    private final int allCancerGroupsIndex;
    private final int leukemiaNOSCancerGroupIndex;
    private final int skinCancerGroupIndex;
    private final int bladderCancerGroupIndex;
    private final int mesotheliomaCancerGroupIndex;
    private final int kaposiSarkomaCancerGroupIndex;
    private final int myeloproliferativeDisordersCancerGroupIndex;
    private final int myelodysplasticSyndromesCancerGroupIndex;
    private final int allCancerGroupsButSkinIndex;
    private final int brainAndCentralNervousSystemCancerGroupIndex;
    private final int ovaryCancerGroupIndex;
    private final int otherCancerGroupsIndex;
    private final int numberOfCancerGroups;
    private static int YEAR_COLUMN = 0;
    private static int SEX_COLUMN = 1;
    private static int AGE_COLUMN = 2;
    private static int ICD10_COLUMN = 3;
    private static int MORPHOLOGY_COLUMN = 4;
    private static int BEHAVIOUR_COLUMN = 5;
    private static int BASIS_DIAGNOSIS_COLUMN = 6;
    private static int CASES_COLUMN = 7;
    private int unknownAgeInt = 999;
    static int numberOfAgeGroups = 21;
    static int allAgeGroupsIndex = 20;
    static int unknownAgeGroupIndex = 19;
    private final String[] rScripts;

    public RTableBuilderGrouped(String configFileName) throws FileNotFoundException {
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        rpath = localSettings.getProperty(LocalSettings.R_PATH);

        // does R exist?
        if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
            throw new FileNotFoundException("R installation invalid/not configured");
        }

        // parse configFile
        LinkedList<ConfigFields> configList = ConfigFieldsReader.readFile(new FileReader(configFileName));

        String[] fileTypesGeneratedArray = ConfigFieldsReader.findConfig(FILE_TYPES_GENERATED, configList);
        LinkedList<FileTypes> fileTypesList = new LinkedList<FileTypes>();
        for (String fileType : fileTypesGeneratedArray) {
            fileTypesList.add(FileTypes.valueOf(fileType));
        }
        fileTypesGenerated = fileTypesList.toArray(new FileTypes[0]);

        rScripts = ConfigFieldsReader.findConfig(R_SCRIPTS, configList);

        tableLabel = ConfigFieldsReader.findConfig("table_label",
                configList);
        icdLabel = ConfigFieldsReader.findConfig("ICD_groups_labels",
                configList);

        icd10GroupDescriptions = ConfigFieldsReader.findConfig(
                "ICD_groups",
                configList);

        cancerGroupsLocal = EditorialTableTools.generateICD10Groups(icd10GroupDescriptions);

        allCancerGroupsIndex = EditorialTableTools.getICD10index("ALL", icd10GroupDescriptions);

        leukemiaNOSCancerGroupIndex = EditorialTableTools.getICD10index(950,
                cancerGroupsLocal);

        skinCancerGroupIndex = EditorialTableTools.getICD10index("C44",
                icd10GroupDescriptions);

        bladderCancerGroupIndex = EditorialTableTools.getICD10index("C67",
                icd10GroupDescriptions);

        mesotheliomaCancerGroupIndex = EditorialTableTools.getICD10index("C45",
                icd10GroupDescriptions);

        kaposiSarkomaCancerGroupIndex = EditorialTableTools.getICD10index("C46",
                icd10GroupDescriptions);

        myeloproliferativeDisordersCancerGroupIndex = EditorialTableTools.getICD10index("MPD",
                icd10GroupDescriptions);

        myelodysplasticSyndromesCancerGroupIndex = EditorialTableTools.getICD10index("MDS",
                icd10GroupDescriptions);

        allCancerGroupsButSkinIndex = EditorialTableTools.getICD10index("ALLbC44",
                icd10GroupDescriptions);

        brainAndCentralNervousSystemCancerGroupIndex = EditorialTableTools.getICD10index("C70-72",
                icd10GroupDescriptions);

        ovaryCancerGroupIndex = EditorialTableTools.getICD10index(569,
                cancerGroupsLocal);

        otherCancerGroupsIndex = EditorialTableTools.getICD10index("O&U", icd10GroupDescriptions);

        numberOfCancerGroups = cancerGroupsLocal.length;

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
    public LinkedList<String> buildTable(String tableHeader,
            String reportFileName,
            int startYear,
            int endYear,
            Object[][] incidenceData,
            PopulationDataset[] populations,
            PopulationDataset[] standardPopulations,
            LinkedList<ConfigFields> configList,
            String[] engineParameters,
            FileTypes fileType) throws NotCompatibleDataException {
        LinkedList<String> filesCreated = new LinkedList<String>();
        InputStream is;

        try {
            // write pops to tempfile
            File popfile = File.createTempFile("pop", ".tsv");
            BufferedWriter popoutput = new BufferedWriter(new FileWriter(popfile));

            String popheader = "YEAR" + separator;
            popheader += "SEX" + separator;
            popheader += "AGE_GROUP" + separator;
            popheader += "COUNT";
            popoutput.append(popheader);
            popoutput.newLine();
            int thisYear = startYear;
            for (PopulationDataset popset : populations) {
                for (PopulationDatasetsEntry pop : popset.getAgeGroups()) {
                    popoutput.append(thisYear+"");
                    popoutput.append(separator);
                    popoutput.append(pop.getStringRepresentationOfAgeGroupsForFile(separator));
                    popoutput.newLine();
                }
                thisYear++;
            }
            popoutput.flush();
            popoutput.close();
            filesCreated.add(popfile.getPath());

            // prepare incidence table
            String sexString;
            String icdString;
            String morphologyString;
            String yearString;
            String ageString;
            String behaviourString;
            String basisString;
            String casesString;

            int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, ageInt, basis, cases;

            if (incidenceData != null) {

                // write inc to tempfile
                File incfile = File.createTempFile("inc", ".tsv");
                BufferedWriter incoutput = new BufferedWriter(new FileWriter(incfile));

                String incheader = "YEAR";
                incheader += separator + "ICD10GROUP";
                incheader += separator + "SEX";
                incheader += separator + "AGEGROUP";
                incheader += separator + "BEHAVIOUR";
                incheader += separator + "BASIS";
                incheader += separator + "CASES";
                incoutput.append(incheader);
                incoutput.newLine();
                StringBuilder outLine = new StringBuilder();

                for (Object[] line : incidenceData) {

                    // Set default
                    icdIndex = -1;
                    cases = 0;

                    // Unknown sex group = 3
                    sex = 3;
                    // Extract data
                    sexString = (String) line[SEX_COLUMN];
                    sex = Integer.parseInt(sexString.trim());

                    // sex = 3 is unknown sex

                    if (sex > 2) {
                        sex = 3;
                    }

                    morphologyString = (String) line[MORPHOLOGY_COLUMN];

                    if (icdIndex < 0) {
                        icdString = (String) line[ICD10_COLUMN];
                        if (icdString.length() > 0
                                && icdString.trim().substring(0, 1).equals("C")) {
                            icdString = icdString.trim().substring(1);
                            icdNumber = Integer.parseInt(icdString);
                            if (icdString.length() < 3) {
                                icdNumber = icdNumber * 10;
                            }
                            icdIndex = EditorialTableTools.getICD10index(icdNumber, cancerGroupsLocal);
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


                    if (icdIndex >= 0) {
                        yearString = line[YEAR_COLUMN].toString();
                        year = Integer.parseInt(yearString);
                        ageString = line[AGE_COLUMN].toString();
                        ageInt = Integer.parseInt(ageString);
                        yearIndex = year - startYear;

                        if (ageInt == unknownAgeInt) {
                            ageGroup = unknownAgeGroupIndex;
                        } else {
                            ageGroup = populations[yearIndex].getAgeGroupIndex(ageInt);
                        }

                        // extract basis
                        basisString = (String) line[BASIS_DIAGNOSIS_COLUMN];
                        // extract behaviour
                        behaviourString = (String) line[BEHAVIOUR_COLUMN];
                        // Extract cases
                        cases = (Integer) line[CASES_COLUMN];

                        outLine.append(year).append(separator);
                        outLine.append(icd10GroupDescriptions[icdIndex]).append(separator);
                        outLine.append(sexString).append(separator);
                        outLine.append(ageGroup).append(separator);
                        outLine.append(behaviourString).append(separator);
                        outLine.append(basisString).append(separator);
                        outLine.append(cases);
                        incoutput.append(outLine);
                        incoutput.newLine();
                    }
                    outLine.delete(0, outLine.length());
                }
                incoutput.flush();
                incoutput.close();

                filesCreated.add(incfile.getPath());

                File dir = new File(Globals.TABLES_CONF_PATH);
                // call R
                for (String rScript : rScripts) {

                    String command = "\"" + rpath + "\""
                            + " --slave -f "
                            + "\"" + dir.getAbsolutePath() + Globals.FILE_SEPARATOR
                            + "r" + Globals.FILE_SEPARATOR
                            + rScript
                            + "\" "
                            + "--args "
                            + fileType
                            + " \"" + reportFileName + "\" "
                            + "\"" + popfile.getPath() + "\" "
                            + "\"" + incfile.getPath() + "\" ";

                    System.out.println(command);
                    System.out.flush();

                    Runtime rt = Runtime.getRuntime();
                    Process pr = rt.exec(command);
                    // collect the output from the R program in a stream
                    is = new BufferedInputStream(pr.getInputStream());
                    try {
                        pr.waitFor();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // convert the output to a string
                    String theString = convertStreamToString(is);

                    // and add all to the list of files to return
                    for (String fileName : theString.split("\n")) {
                        if (new File(fileName).exists()) {
                            filesCreated.add(fileName);
                        }
                    }

                    System.out.println(theString);

                    System.out.println(pr.exitValue());
                }
            }



        } catch (IOException ex) {
            Logger.getLogger(RTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filesCreated;
    }

    private String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    @Override
    public boolean areThesePopulationDatasetsCompatible(PopulationDataset[] populations) {
        return true;
    }
}
