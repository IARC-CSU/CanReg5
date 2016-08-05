/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016  International Agency for Research on Cancer
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

import canreg.common.Globals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public LockFile(String identifier) {
        lockFileName = Globals.CANREG_CLIENT_FOLDER + Globals.FILE_SEPARATOR + identifier + Globals.CANREG_LOCAL_LOCKED_RECORDS_FILE_NAME_SUFFIX;
        locksMap = new TreeMap<String, Set<Integer>>();
        loadMap();
    }

    public TreeMap<String, Set<Integer>> getMap() {
        return locksMap;
    }

    private void loadMap() {
        FileInputStream fis;
        ObjectInputStream in = null;
        try {
            new File(lockFileName).createNewFile();
            fis = new FileInputStream(lockFileName);
            boolean success = false;
            try {
                in = new ObjectInputStream(fis);
                locksMap = (TreeMap<String, Set<Integer>>) in.readObject();
                success = true;
            } catch (NullPointerException ex) {
                Logger.getLogger(LockFile.class.getName()).log(Level.INFO, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(LockFile.class.getName()).log(Level.INFO, null, ex);
            } catch (java.io.EOFException ex) {
                Logger.getLogger(LockFile.class.getName()).log(Level.INFO, null, ex);
            } catch (java.io.StreamCorruptedException ex) {
                Logger.getLogger(LockFile.class.getName()).log(Level.INFO, null, ex);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (!success) {
                    locksMap = new TreeMap<String, Set<Integer>>();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LockFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LockFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (locksMap == null) {
            locksMap = new TreeMap<String, Set<Integer>>();
        }
    }

    public void writeMap() {
        try {
            FileOutputStream fos = new FileOutputStream(lockFileName);
            out = new ObjectOutputStream(fos);
            out.writeObject(locksMap);
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LockFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LockFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeMap() {
        try {
            writeMap();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(LockFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumberOfRecordsLocked() {
        int records = 0;
        for (String key : locksMap.keySet()) {
            for (Integer i : locksMap.get(key)) {
                records++;
            }
        }
        return records;
    }
}
