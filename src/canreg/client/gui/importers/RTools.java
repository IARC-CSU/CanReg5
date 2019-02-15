/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2019  International Agency for Research on Cancer
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
 * @author Patricio Carranza (patocarranza@gmail.com)
 */
package canreg.client.gui.importers;

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.common.Globals;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Patricio Carranza (patocarranza@gmail.com)
 */
public class RTools {
    
    /**
     * Converts file paths with system specific file separators (i.e. "\" for Windows)
     * to universal file separator ("//"). This way R scripts can locate files in any
     * file system.
     * @param path
     * @return 
     */
    public static String fixPath(String path) {
        if(path == null || path.isEmpty())
            throw new NullPointerException("File path is null. File must be missing.");
        return path.replace(File.separator, "//");
    }
    
    private static File getScriptPath(String rScript) 
            throws IOException {
        File dir = new File(Globals.IMPORT_R_SCRIPTS_PATH);
        File userDir = new File(Globals.USER_IMPORT_R_SCRIPTS_PATH);
        File scriptFile = new File(userDir.getCanonicalPath()
                        + Globals.FILE_SEPARATOR
                        + rScript);
        if (!scriptFile.exists()) {
            scriptFile = new File(dir.getCanonicalPath()
                    + Globals.FILE_SEPARATOR
                    + rScript);
        }
        return scriptFile;
    }    
    
    
    /**
     * Communicates with R scripts that are specifically developed for data quality
     * and checks when perfoming records imports.
     * @param rScript name of the R script (including the .r suffix) to be run.
     * @param paramsFile file that contains all the parameters/arguments for the script.
     * @param files
     * @return
     * @throws FileNotFoundException 
     */
    public static File[] runRimportScript(String rScript, File paramsFile, File[] files) 
            throws FileNotFoundException, IOException { 
        String rpath = CanRegClientApp.getApplication().getLocalSettings().getProperty(LocalSettings.R_PATH);
        // does R exist?
        if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
            throw new FileNotFoundException("R installation invalid/not configured");
        }
        
        File scriptFile = getScriptPath(rScript);
                
        ArrayList<String> commandList = new ArrayList();
        commandList.add(rpath);
        commandList.add("--vanilla");
        commandList.add("--slave");
        commandList.add("--file=" + fixPath(scriptFile.getCanonicalPath()));
        commandList.add("--args");
        commandList.add("-paramsFile=" + fixPath(paramsFile.getCanonicalPath()));
        System.out.println(commandList);
                
        Process proc = null;
        InputStream inputStr = null;
        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec(commandList.toArray(new String[]{}));
            // collect the output from the R script in a stream
            inputStr = new BufferedInputStream(proc.getInputStream());
            proc.waitFor();
            // convert the output to a string
            String theString = canreg.client.analysis.Tools.convertStreamToString(inputStr);
//            Logger.getLogger(RTools.class.getName()).log(Level.INFO, "Messages from R: \n{0}", theString);            
            
            if(theString.contains("-outFile")){
                for (String fileName : theString.split("\\r?\\n")) {
                    fileName = fileName.replaceFirst("-outFile:", "");
                    if (new File(fileName).exists()) {
                        files = new File[3];
                        files[0] = files[1] = files[2] = new File(fileName);
                        break;
                    }
                }
            } else {
                files = new File[3];
                for (String fileName : theString.split("\\r?\\n")) {
                    fileName = fileName.replaceFirst("-outPatientFile:", "");
                    if(new File(fileName).exists()) { 
                        files[0] = new File(fileName);
                        continue;
                    }
                    fileName = fileName.replaceFirst("-outTumourFile:", "");
                    if(new File(fileName).exists()) {
                        files[1] = new File(fileName);
                        continue;
                    }
                    fileName = fileName.replaceFirst("-outSourceFile:", "");
                    if(new File(fileName).exists()) 
                        files[2] = new File(fileName);
                }
            }
            if(files == null || files[0] == null)
                throw new RuntimeException("No output files were found after running the " + scriptFile + " script.");
        } catch (Exception ex) {
            Logger.getLogger(RTools.class.getName()).log(Level.SEVERE, null, ex);
            BufferedInputStream errorStream = new BufferedInputStream(proc.getErrorStream());
            String errorMessage = canreg.client.analysis.Tools.convertStreamToString(errorStream);
            System.out.println(errorMessage);
            throw new RuntimeException("************R SCRIPT ERROR:\n \"" + errorMessage + "\"");
        } finally {
            if(inputStr != null)
                inputStr.close();
            if(proc != null)
                proc.destroyForcibly();
        }
        
        return files;
    }
}
