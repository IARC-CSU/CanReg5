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

public class RTableBuilder implements TableBuilderInterface {

    public static String VARIABLES_NEEDED = "variables_needed";
    public static String FILE_TYPES_GENERATED = "file_types_generated";
    public static String R_SCRIPTS = "r_scripts";
    private StandardVariableNames[] variablesNeeded;
    private FileTypes[] fileTypesGenerated;
    private String separator = "\t";
    private final LocalSettings localSettings;
    private final String rpath;
    private String[] rScripts;

    public RTableBuilder(String configFileName) throws FileNotFoundException {
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        rpath = localSettings.getProperty(LocalSettings.R_PATH);

        // does R exist?
        if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
            throw new FileNotFoundException("R installation invalid/not configured");
        }

        // parse configFile
        LinkedList<ConfigFields> configList = ConfigFieldsReader.readFile(new FileReader(configFileName));
        // build variables needed map
        String[] variablesNeededArray = ConfigFieldsReader.findConfig(VARIABLES_NEEDED, configList);
        LinkedList<StandardVariableNames> variablesNeededList = new LinkedList<StandardVariableNames>();
        for (String variableName : variablesNeededArray) {
            variablesNeededList.add(StandardVariableNames.valueOf(variableName));
        }
        variablesNeeded = variablesNeededList.toArray(new StandardVariableNames[0]);

        rScripts = ConfigFieldsReader.findConfig(R_SCRIPTS, configList);

        // build fileTypesGeneratedMap
        fileTypesGenerated = new FileTypes[]{
            FileTypes.png,
            FileTypes.csv
        };

        String[] fileTypesGeneratedArray = ConfigFieldsReader.findConfig(FILE_TYPES_GENERATED, configList);
        LinkedList<FileTypes> fileTypesList = new LinkedList<FileTypes>();
        for (String fileType : fileTypesGeneratedArray) {
            fileTypesList.add(FileTypes.valueOf(fileType));
        }
        fileTypesGenerated = fileTypesList.toArray(new FileTypes[0]);
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

            // write inc to tempfile
            File incfile = File.createTempFile("inc", ".tsv");
            BufferedWriter incoutput = new BufferedWriter(new FileWriter(incfile));

            String incheader = "YEAR";
            for (StandardVariableNames stdVarbName : variablesNeeded) {
                incheader += separator + stdVarbName.toString().toUpperCase();
            }
            incheader += separator + "CASES";
            incoutput.append(incheader);
            incoutput.newLine();
            StringBuilder line = new StringBuilder();
            for (Object[] row : incidenceData) {
                line.delete(0, line.length());
                for (Object element : row) {
                    line.append(element.toString()).append(separator);
                }
                line.deleteCharAt(line.length() - 1);
                incoutput.append(line);
                incoutput.newLine();
            }
            incoutput.flush();
            incoutput.close();
            // filesCreated.add(incfile.getPath());

            File dir = new File(Globals.TABLES_CONF_PATH);
            // call R

            for (String rScript : rScripts) {
                Runtime rt = Runtime.getRuntime();
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
