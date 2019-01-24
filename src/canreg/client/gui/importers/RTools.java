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
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Patricio Carranza (patocarranza@gmail.com)
 */
public class RTools {
    
    private static String fixPath(String path) {
        if(path == null || path.isEmpty())
            throw new NullPointerException("File path is null. File must be missing.");
        return path.replace(File.separator, "//");
    }
    
    private static File getScriptPath(String rScript) {
        File dir = new File(Globals.IMPORT_R_SCRIPTS_PATH);
        File userDir = new File(Globals.USER_IMPORT_R_SCRIPTS_PATH);
        File scriptFile = new File(userDir.getAbsolutePath()
                        + Globals.FILE_SEPARATOR
                        + rScript);
        if (!scriptFile.exists()) {
            scriptFile = new File(dir.getAbsolutePath()
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
     * @return
     * @throws FileNotFoundException 
     */
    public static LinkedList<File> runRimportScript(String rScript, File paramsFile) 
            throws FileNotFoundException { 
        String rpath = CanRegClientApp.getApplication().getLocalSettings().getProperty(LocalSettings.R_PATH);
        // does R exist?
        if (rpath == null || rpath.isEmpty() || !new File(rpath).exists()) {
            throw new FileNotFoundException("R installation invalid/not configured");
        }
        
        File scriptFile = getScriptPath(rScript);
        LinkedList<File> filesCreated = new LinkedList<>();
                
        ArrayList<String> commandList = new ArrayList();
        commandList.add(rpath);
        commandList.add("--vanilla");
        commandList.add("--slave");
        commandList.add("--file=" + scriptFile.getAbsolutePath() );
        commandList.add("--args");
        commandList.add("-paramsFile=" + fixPath(paramsFile.getAbsolutePath()));
        System.out.println(commandList);
                
        Process proc = null;
        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec(commandList.toArray(new String[]{}));
            // collect the output from the R script in a stream
            InputStream is = new BufferedInputStream(proc.getInputStream());
            proc.waitFor();
            // convert the output to a string
            String theString = canreg.client.analysis.Tools.convertStreamToString(is);
            Logger.getLogger(RTools.class.getName()).log(Level.INFO, "Messages from R: \n{0}", theString);
            // and add all to the list of files to return
            for (String fileName : theString.split("\\r?\\n")) {
                if (fileName.startsWith("-outFile:")) {
                    fileName = fileName.replaceFirst("-outFile:", "");
                    if (new File(fileName).exists()) 
                        filesCreated.add(new File(fileName));
                }
            }
        } catch (java.util.NoSuchElementException ex) {
            Logger.getLogger(RTools.class.getName()).log(Level.SEVERE, null, ex);
            BufferedInputStream errorStream = new BufferedInputStream(proc.getErrorStream());
            String errorMessage = canreg.client.analysis.Tools.convertStreamToString(errorStream);
            System.out.println(errorMessage);
            throw new RuntimeException("R says:\n \"" + errorMessage + "\"");
        } catch(InterruptedException | IOException ex) {
            Logger.getLogger(RTools.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println(proc.exitValue());
        }
        
        return filesCreated;
    }
}
