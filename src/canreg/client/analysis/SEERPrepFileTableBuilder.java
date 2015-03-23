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

import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.AgeGroupStructure;
import canreg.common.database.IncompatiblePopulationDataSetException;
import canreg.common.database.PopulationDataset;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SEERPrepFileTableBuilder implements TableBuilderInterface {

    public static String VARIABLES_NEEDED = "variables_needed";
    public static String FILE_TYPES_GENERATED = "file_types_generated";
    private StandardVariableNames[] variablesNeeded;
    private FileTypes[] fileTypesGenerated;
    private int unknownAgeCode = Globals.DEFAULT_UNKNOWN_AGE_CODE;

    public SEERPrepFileTableBuilder(String configFileName) throws FileNotFoundException {
        // parse configFile
        LinkedList<ConfigFields> configList = ConfigFieldsReader.readFile(new FileReader(configFileName));
        // build variables needed map
        String[] variablesNeededArray = ConfigFieldsReader.findConfig(VARIABLES_NEEDED, configList);
        LinkedList<StandardVariableNames> variablesNeededList = new LinkedList<StandardVariableNames>();
        for (String variableName : variablesNeededArray) {
            variablesNeededList.add(StandardVariableNames.valueOf(variableName));
        }
        variablesNeeded = variablesNeededList.toArray(new StandardVariableNames[0]);

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
            FileTypes fileType) throws NotCompatibleDataException, TableErrorException {
        LinkedList<String> filesCreated = new LinkedList<String>();

        try {

            File baseFileName = new File(reportFileName);            

            String ddFileName = baseFileName.getParent() + Globals.FILE_SEPARATOR + baseFileName.getName()+".dd";             
            String populationFileName = baseFileName.getParent() + Globals.FILE_SEPARATOR + "pop-" + baseFileName.getName()+".txd"; 
            String casesFileName = baseFileName.getParent() + Globals.FILE_SEPARATOR + "cases-" + baseFileName.getName()+".txd"; 
       
            AgeGroupStructure ageGroupStructure;
            
            int thisYear = startYear;
            if (populations != null) {
                FixedWidthFileWriter fwfw = new FixedWidthFileWriter(26, true); //TODO: make dynamic
                fwfw.setOutputFileName(populationFileName);
                TreeMap map = new TreeMap();
                // recode the address to XX
                map.put(StandardVariableNames.AddressCode, "XX");
                for (PopulationDataset pop : populations) {
                    ageGroupStructure = pop.getAgeGroupStructure();
                    if (ageGroupStructure.getSizeOfGroups() != 5
                            || ageGroupStructure.getCutOfAge() < 150) {
                        // for now we only allow 5 year age groups and no cutoff age
                        throw (new IncompatiblePopulationDataSetException());
                    }
                    map.put(StandardVariableNames.IncidenceDate, (thisYear * 10000) + "");
                    int offset; // the difference between SEER age group count and CanReg

                    for (int sex = 1; sex <= 2; sex++) {
                        // first the two first groups
                        map.put(StandardVariableNames.Sex, sex);
                        if (ageGroupStructure.getSizeOfFirstGroup() != 1) {
                            int count = pop.getAgeGroupCount(sex, 0);
                            int firstGroup = count / 5;
                            count = count - firstGroup;
                            // first line
                            map.put(StandardVariableNames.AgeGroup, 0);
                            map.put(StandardVariableNames.Count, firstGroup);
                            fwfw.writeLine(map);
                            // second line
                            map.put(StandardVariableNames.AgeGroup, 1);
                            map.put(StandardVariableNames.Count, count);
                            fwfw.writeLine(map);
                            offset = 1;
                        } else {
                            map.put(StandardVariableNames.AgeGroup, 0);
                            map.put(StandardVariableNames.Count, pop.getAgeGroupCount(sex, 0));
                            fwfw.writeLine(map);
                            map.put(StandardVariableNames.AgeGroup, 1);
                            map.put(StandardVariableNames.Count, pop.getAgeGroupCount(sex, 1));
                            fwfw.writeLine(map);
                            offset = 0;

                        }
                        int goToGroup = ageGroupStructure.getNumberOfAgeGroups() - offset;
                        if (ageGroupStructure.getMaxAge() < 85) {
                            // stop at one before and split the rest on the rest
                            goToGroup -= 1;
                        } else if (ageGroupStructure.getMaxAge() > 85) {
                            // stop at 
                            goToGroup = 17 + offset;
                        }

                        for (int i = 1; i < goToGroup; i++) {
                            map.put(StandardVariableNames.AgeGroup, i + offset);
                            map.put(StandardVariableNames.Count, pop.getAgeGroupCount(sex, i));
                            fwfw.writeLine(map);
                        }

                        // last age group(s)
                        if (ageGroupStructure.getMaxAge() < 85) {
                            int count = pop.getAgeGroupCount(sex, ageGroupStructure.getNumberOfAgeGroups() - 1);
                            count = count / (18 - (ageGroupStructure.getNumberOfAgeGroups() - offset) + 1);
                            for (int i = goToGroup; i < (17 + offset); i++) {
                                map.put(StandardVariableNames.AgeGroup, i + offset);
                                map.put(StandardVariableNames.Count, count);
                                fwfw.writeLine(map);
                            }
                        } else if (ageGroupStructure.getMaxAge() > 85) {
                            int count = 0;
                            for (int i = goToGroup; i < ageGroupStructure.getNumberOfAgeGroups(); i++) {
                                count += pop.getAgeGroupCount(sex, i);
                            }
                            map.put(StandardVariableNames.AgeGroup, 18);
                            map.put(StandardVariableNames.Count, count);
                            fwfw.writeLine(map);
                        }
                    }
                    thisYear++;
                }
                fwfw.close();
            }

            if (incidenceData != null) {
                FixedWidthFileWriter fwfw = new FixedWidthFileWriter(1946); //TODO: make dynamic
                fwfw.setOutputFileName(casesFileName);
                for (Object[] row : incidenceData) {
                    TreeMap map = new TreeMap();
                    // map.put(StandardVariableNames.IncidenceDate, row[0]);
                    for (int i = 1; i < row.length - 1; i++) {
                        Object element = row[i];
                        StandardVariableNames variable = variablesNeeded[i - 1];
                        // recode addresses to XX
                        if (variable == StandardVariableNames.AddressCode) {
                            element = "XX";
                        }
                        map.put(variable, element);
                    }
                    // the last element is the cases
                    // no need to worry since we include the unique patient ID...
                    // if not we need to write the case several times.
                    // int cases = (Integer) row[row.length-1];
                    // for(int j=0;j<cases;j++) {
                    fwfw.writeLine(map);
                    // }
                }
                fwfw.close();
                // filesCreated.add(reportFileName); //can't open it with the system.
            }
            // File dir = new File(Globals.TABLES_CONF_PATH);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/canreg/common/ruby/naaccr1946.ver11_3.d02032011.dd")));

            String line = bfr.readLine();
            BufferedWriter bfw = new BufferedWriter
                (new OutputStreamWriter(new FileOutputStream(ddFileName),"ASCII"));
            
            while (line!=null) {
                line = line.replace("$NAME", tableHeader);
                line = line.replace("$CASE_FILE", casesFileName);
                line = line.replace("$POP_FILE", populationFileName);
                bfw.write(line+"\n");
                line = bfr.readLine();            
            }
            bfw.close();
            bfr.close();
            
            filesCreated.add(ddFileName);
            
        } catch (IOException ex) {
            Logger.getLogger(SEERPrepFileTableBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IncompatiblePopulationDataSetException ex) {
            Logger.getLogger(SEERPrepFileTableBuilder.class.getName()).log(Level.WARNING, null, ex);
            throw new NotCompatibleDataException();
        }

        return filesCreated;
    }

    @Override
    public boolean areThesePopulationDatasetsCompatible(PopulationDataset[] populations) {
        for (PopulationDataset pop : populations) {
            AgeGroupStructure ageGroupStructure = pop.getAgeGroupStructure();
            if (ageGroupStructure.getSizeOfGroups() != 5
                    || ageGroupStructure.getCutOfAge() < 150) {
                // for now we only allow 5 year age groups and no cutoff age
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void setUnknownAgeCode(int unknownAgeCode) {
        this.unknownAgeCode = unknownAgeCode;
    }
}
