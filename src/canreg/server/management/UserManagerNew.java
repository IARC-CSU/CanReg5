package canreg.server.management;

import canreg.server.database.User;
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

    public UserManagerNew(CanRegDAO db) {
        this.db = db;
        Map<String, User> userMap = db.getUsers();
        if (userMap.isEmpty()) {
            installDefaultUsers();
        }
    }

    public int getNumberOfUsersLoggedIn() {
        return fClients.size();
    }

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

    public boolean writePasswordsToFile(){
        return writePasswordFile() && writeLevelsFile();
    }


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

    public void userLoggedIn(String userName) {
        fClients.add(userName);
    }

    public void userLoggedOut(String userName) {
        fClients.remove(fClients.indexOf(userName));
    }

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

    public boolean addUser(User user) {
        boolean success = false;
        db.saveUser(user);
        return success;
    }

    public boolean removeUser(User user) {
        boolean success = false;

        return success;
    }
}
