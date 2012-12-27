/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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

package canreg.server.management;

import canreg.common.database.User;
import canreg.server.*;
import canreg.common.Globals;
import canreg.server.database.CanRegDAO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class UserManagerNew {

        /**
     *
     */
    static public String DEFAULT_PASS_FILENAME = "Passwords.properties";

    /**
     *
     */
    static public String DEFAULT_LEVELS_FILENAME = "Levels.properties";

    LinkedList fClients = new LinkedList();
    CanRegDAO db;

    /**
     *
     * @param db
     */
    public UserManagerNew(CanRegDAO db) {
        this.db = db;
        Map<String, User> userMap = db.getUsers();
        if (userMap.isEmpty()) {
            installDefaultUsers();
        }
    }

    /**
     *
     * @return
     */
    public int getNumberOfUsersLoggedIn() {
        return fClients.size();
    }

    /**
     *
     * @return
     */
    public List<User> listUsers() {
        Map<String, User> userMap = db.getUsers();
        LinkedList<User> users = new LinkedList<User>();
        for (String userName : userMap.keySet()) {
            users.add(userMap.get(userName));
        }
        return users;
    }

    private void installDefaultUsers() {
        LinkedList<User> users = listDefaultUsers();
        for (User user : users) {
            addUser(user);
        }
    }

    /**
     *
     * @return
     */
    public boolean writePasswordsToFile(){
        return writePasswordFile() && writeLevelsFile();
    }


    /**
     *
     * @return
     */
    public boolean writePasswordFile() {
        boolean success = false;
        OutputStream propOutputStream = null;
        try {

            Map<String, User> userMap = db.getUsers();
            Properties passwords = new Properties();
            for (String username : userMap.keySet()) {
                passwords.setProperty(username, new String(userMap.get(username).getPassword()));
            }
            File file = new File(Globals.CANREG_PASSWORD_FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            propOutputStream = new FileOutputStream(Globals.CANREG_PASSWORD_FILE_NAME);
            passwords.storeToXML(propOutputStream, "Passwords");
            success = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (IOException ex) {
            Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            if (propOutputStream != null) {
                try {
                    propOutputStream.flush();
                    propOutputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     *
     * @return
     */
    public boolean writeLevelsFile() {
        boolean success = false;
        OutputStream propOutputStream = null;
        try {

            Map<String, User> userMap = db.getUsers();
            Properties levels = new Properties();
            for (String username : userMap.keySet()) {
                levels.setProperty(username, userMap.get(username).getUserRightLevel().toString());
            }
            File file = new File(Globals.CANREG_LEVELS_FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            propOutputStream = new FileOutputStream(Globals.CANREG_LEVELS_FILE_NAME);
            levels.storeToXML(propOutputStream, "Levels");
            success = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (IOException ex) {
            Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            if (propOutputStream != null) {
                try {
                    propOutputStream.flush();
                    propOutputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
                    success = false;
                }
            }
        }
        return success;
    }

    private LinkedList<User> listDefaultUsers() {
        LinkedList<User> users = new LinkedList<User>();
        try {
            InputStream levelsPropInputStream = null;
            levelsPropInputStream = CanRegLoginModule.class.getResourceAsStream(DEFAULT_LEVELS_FILENAME);
            Properties levels = new Properties();
            levels.load(levelsPropInputStream);

            InputStream passwordsPropInputStream = null;
            passwordsPropInputStream = CanRegLoginModule.class.getResourceAsStream(DEFAULT_PASS_FILENAME);
            Properties passwords = new Properties();
            passwords.load(passwordsPropInputStream);

            for (String userName : levels.stringPropertyNames()) {
                User user = new User();
                user.setUserName(userName);
                String userRightLevel = levels.getProperty(userName);
                user.setUserRightLevel(Globals.UserRightLevels.valueOf(userRightLevel));
                String password = passwords.getProperty(userName);
                if (password != null) {
                    user.setPassword(password.toCharArray());
                } else {
                    Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, "Password and user rights doesn't match...");
                }
                users.add(user);
            }

        } catch (IOException ex) {
            Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return users;
    }

    /**
     *
     * @param userName
     */
    public void userLoggedIn(String userName) {
        fClients.add(userName);
    }

    /**
     *
     * @param userName
     */
    public void userLoggedOut(String userName) {
        fClients.remove(fClients.indexOf(userName));
    }

    /**
     *
     * @return
     */
    public String[] listCurrentUsers() {
        String[] users = new String[fClients.size()];
        int i = 0;
        for (Iterator it = fClients.iterator(); it.hasNext();) {
            users[i] = (String) it.next();
            // debugOut("element is " + users[i]);
            i++;
        }
        return users;
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean addUser(User user) {
        boolean success = false;
        db.saveUser(user);
        return success;
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean removeUser(User user) {
        boolean success = false;
        // TODO: Why is this empty? And it seems to work... Aie.
        return success;
    }
}