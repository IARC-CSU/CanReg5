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
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import canreg.client.analysis.Tools.KeyCancerGroupsEnum;
import canreg.common.database.IncompatiblePopulationDataSetException;

public class RTableBuilderGrouped implements TableBuilderInterface {

    public static String VARIABLES_NEEDED = "variables_needed";
    public static String FILE_TYPES_GENERATED = "file_types_generated";
    public static String R_SCRIPTS = "r_scripts";
    public static String R_SCRIPTS_ARGUMENTS = "r_scripts_arguments";
    private static Globals.StandardVariableNames[] variablesNeeded = {
        Globals.StandardVariableNames.Sex,
        Globals.StandardVariableNames.Age,
        Globals.StandardVariableNames.ICD10,
        Globals.StandardVariableNames.Morphology,
        Globals.StandardVariableNames.Behaviour,
        Globals.StandardVariableNames.BasisDiagnosis
    };
    private FileTypes[] fileTypesGenerated;
    private String separator = "\t";
    private final LocalSettings localSettings;
    private final String rpath;
    private final String[] tableLabel;
    private final String[] icdGroupLabels;
    private final String[] icd10GroupDescriptions;
    private final LinkedList[] cancerGroupsLocal;
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
    private Map<KeyCancerGroupsEnum, Integer> keyGroupsMap;
    private final String[] rScriptsArguments;

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

        rScriptsArguments = ConfigFieldsReader.findConfig(R_SCRIPTS_ARGUMENTS, configList);

        tableLabel = ConfigFieldsReader.findConfig("table_label", configList);
        icdGroupLabels = ConfigFieldsReader.findConfig("ICD_groups_labels", configList);

        icd10GroupDescriptions = ConfigFieldsReader.findConfig(
                "ICD_groups",
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
            FileTypes fileType) throws NotCompatibleDataException, TableErrorException {
        LinkedList<String> filesCreated = new LinkedList<String>();
        InputStream is;

        List<Integer> dontCount = new LinkedList<Integer>();

        // all sites but skin?
        if (engineParameters != null) {
            if (Arrays.asList(engineParameters).contains("noC44")) {
                dontCount.add(keyGroupsMap.get(KeyCancerGroupsEnum.skinCancerGroupIndex));
            }
        }

        try {
            // write pops to tempfile
            File popfile = File.createTempFile("pop", ".tsv");
            if (populations != null) {
                BufferedWriter popoutput = new BufferedWriter(new FileWriter(popfile));
                Tools.writePopulationsToFile(popoutput, startYear, populations, separator);
                popoutput.close();
            }
            // filesCreated.add(popfile.getPath());

            // prepare incidence table
            String sexString;
            String icdString;
            String morphologyString;
            String yearString;
            String ageString;
            String behaviourString;
            String basisString;

            int sex, icdNumber, year, icdIndex, yearIndex, ageGroup, ageInt, basis, cases;
            File incfile = File.createTempFile("inc", ".tsv");

            if (incidenceData != null) {
                // write inc to tempfile
                BufferedWriter incoutput = new BufferedWriter(new FileWriter(incfile));

                String incheader = "YEAR";
                incheader += separator + "ICD10GROUP";
                incheader += separator + "ICD10GROUPLABEL";
                incheader += separator + "SEX";
                incheader += separator + "AGE_GROUP";
                incheader += separator + "MORPHOLOGY";
                incheader += separator + "BEHAVIOUR";
                incheader += separator + "BASIS";
                incheader += separator + "CASES";
                incoutput.append(incheader);
                incoutput.newLine();
                StringBuilder outLine = new StringBuilder();

                for (Object[] line : incidenceData) {
                    try {
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
                        icdString = (String) line[ICD10_COLUMN];

                        icdIndex = Tools.assignICDGroupIndex(keyGroupsMap, icdString, morphologyString, cancerGroupsLocal);

                        if (icdIndex != Tools.DONT_COUNT && icdIndex >= 0) {
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
                            outLine.append("\"").append(icd10GroupDescriptions[icdIndex]).append("\"").append(separator);
                            outLine.append("\"").append(icdGroupLabels[icdIndex]).append("\"").append(separator);
                            outLine.append(sexString).append(separator);
                            outLine.append(ageGroup).append(separator);
                            outLine.append(morphologyString).append(separator);
                            outLine.append(behaviourString).append(separator);
                            outLine.append(basisString).append(separator);
                            outLine.append(cases);
                            incoutput.append(outLine);
                            incoutput.newLine();
                        }
                        outLine.delete(0, outLine.length());
                    } catch (NumberFormatException nfe) {
                        Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.WARNING, null, nfe);
                    }
                }
                incoutput.flush();
                incoutput.close();
            }
            // filesCreated.add(incfile.getPath());

            File dir = new File(Globals.R_SCRIPTS_PATH);
            // call R
            for (String rScript : rScripts) {

                String command = "\"" + rpath + "\""
                        + " --slave --file="
                        + "\"" + dir.getAbsolutePath() 
                        + Globals.FILE_SEPARATOR
                        + rScript
                        + "\" "
                        + "--args "
                        + "-ft=" + fileType + " "
                        + "-out=\"" + reportFileName + "\" "
                        + "-pop=\"" + popfile.getPath() + "\" "
                        + "-inc=\"" + incfile.getPath() + "\" ";
                // add the rest of the arguments
                if (rScriptsArguments != null) {
                    for (String arg : rScriptsArguments) {
                        command += arg + " ";
                    }
                }

                System.out.println(command);
                System.out.flush();

                Runtime rt = Runtime.getRuntime();
                Process pr = rt.exec(command);
                // collect the output from the R program in a stream
                is = new BufferedInputStream(pr.getInputStream());
                try {
                    pr.waitFor();
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
                    Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.util.NoSuchElementException ex) {
                    Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.SEVERE, null, ex);
                    BufferedInputStream errorStream = new BufferedInputStream(pr.getErrorStream());
                    String errorMessage = convertStreamToString(errorStream);
                    System.out.println(errorMessage);
                    throw new TableErrorException("R says:\n" + errorMessage);
                } finally {
                    System.out.println(pr.exitValue());
                    // Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.INFO, null, pr.exitValue());
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IncompatiblePopulationDataSetException ex) {
            Logger.getLogger(RTableBuilderGrouped.class.getName()).log(Level.WARNING, null, ex);
            throw new NotCompatibleDataException();
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
