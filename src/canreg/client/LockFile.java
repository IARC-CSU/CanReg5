/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017  International Agency for Research on Cancer
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
package canreg.client;

import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.Globals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ErvikM
 */
public class LockFile {

    private final String lockFileName;
    private TreeMap<String, Set<Integer>> locksMap;
    private ObjectOutputStream out = null;
    private static final Logger LOGGER = Logger.getLogger(LockFile.class.getName());

    public LockFile(String identifier) {
        lockFileName = Globals.CANREG_CLIENT_FOLDER + Globals.FILE_SEPARATOR + identifier + Globals.CANREG_LOCAL_LOCKED_RECORDS_FILE_NAME_SUFFIX;
        locksMap = new TreeMap<>();
        loadMap();
    }

    /**
     *
     * @return
     */
    public Map<String, Set<Integer>> getMap() {
        return locksMap;
    }

    private void loadMap() {
        File file = new File(lockFileName);
        boolean nf;
        try {
            nf = file.createNewFile();
            if (nf) {
                try (FileInputStream fis = new FileInputStream(lockFileName)) {
                    boolean success = false;
                    try (ObjectInputStream in = new ObjectInputStream(fis)) {
                        locksMap = (TreeMap<String, Set<Integer>>) in.readObject();
                        success = true;
                    } catch (NullPointerException | ClassNotFoundException | java.io.EOFException | java.io.StreamCorruptedException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                        new TechnicalError().errorDialog();
                    } finally {
                        if (!success) {
                            locksMap = new TreeMap<>();
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
                if (locksMap == null) {
                    locksMap = new TreeMap<>();
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
    }

    public void writeMap() {
        try (FileOutputStream fos = new FileOutputStream(lockFileName)) {
            out = new ObjectOutputStream(fos);
            out.writeObject(locksMap);
            out.flush();
            out.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
    }

    public void closeMap() {
        try {
            writeMap();
            out.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }
    }

    public int getNumberOfRecordsLocked() {
        int records = 0;
        for (String key : locksMap.keySet()) {
            if (locksMap.get(key) == null) {
                continue;
            }
            for (Integer i : locksMap.get(key)) {
                records++;
            }
        }
        return records;
    }
}
