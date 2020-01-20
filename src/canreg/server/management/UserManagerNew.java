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

package canreg.server.management;

import canreg.client.LocalSettings;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm, Patricio Carranza
 */
public class UserManagerNew {

    static public String DEFAULT_PASS_FILENAME = "Passwords.properties";
    static public String DEFAULT_LEVELS_FILENAME = "Levels.properties";        

    private final ConcurrentHashMap<Integer, ClientSessionData> clientSessionsMap;
    private ScheduledExecutorService checkAliveClients;
    CanRegDAO db;


    public UserManagerNew(CanRegDAO db) {
        this.db = db;
        Map<String, User> userMap = db.getUsers();
        if (userMap.isEmpty()) 
            installDefaultUsers();
        
        clientSessionsMap = new ConcurrentHashMap<>();
        
        try {
            checkAliveClients = Executors.newSingleThreadScheduledExecutor();
            LocalSettings localSettings = new LocalSettings("settings.xml");
//            te falta que estos jtextfield solo acepten integers. Fijate en el codigo del jewel en la caja de texto de los channels
            Integer seconds = Integer.parseInt(localSettings.getProperty(LocalSettings.CLIENT_SESSIONS_CHECK_KEY));
            checkAliveClients.scheduleAtFixedRate(new CheckClientSessionAlive(), 0, seconds, TimeUnit.SECONDS);
        } catch (IOException ex) {
            Logger.getLogger(UserManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumberOfUsersLoggedIn() {
        return clientSessionsMap.size();
    }

    public List<User> listUsers() {
        Map<String, User> userMap = db.getUsers();
        LinkedList<User> users = new LinkedList<>();
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
        LinkedList<User> users = new LinkedList<>();
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


    public void userLoggedIn(Integer remoteHashCode, String userName) {
        clientSessionsMap.put(remoteHashCode, new ClientSessionData(userName, remoteHashCode));
    }
    
    public void lockRecord(int recordID, String tableName, Integer remoteHashCode) {
        if(remoteHashCode == null)
            return;
        
        Set lockSet = clientSessionsMap.get(remoteHashCode).records.get(tableName);
        if (lockSet == null) {
            lockSet = new TreeSet<>();
            clientSessionsMap.get(remoteHashCode).records.put(tableName, lockSet);
        }
        lockSet.add(recordID);
    }
    
    public void releaseRecord(int recordID, String tableName, Integer remoteHashCode) {
//        db.releaseRecord(recordID, tableName);
        if(remoteHashCode == null)
            return;
        
        ClientSessionData sessionData = clientSessionsMap.get(remoteHashCode);
        if(sessionData != null) {
            Set lockSet = sessionData.records.get(tableName);
            if (lockSet != null) 
                lockSet.remove(recordID);
        }
    }

    public void userLoggedOut(Integer remoteHashCode) {
        Map<String, Set<Integer>> recordsLocked = clientSessionsMap.remove(remoteHashCode).records;
        for(String tableName : recordsLocked.keySet()) {
            Set<Integer> recordsIds = recordsLocked.get(tableName);
            for(Integer recId : recordsIds)
                db.releaseRecord(recId, tableName);
        }
    }    
    
    public void remotePingReceived(Integer remoteHashCode) {
        if(clientSessionsMap.get(remoteHashCode) != null)
            clientSessionsMap.get(remoteHashCode).pingReceived = true;
    }

    public String[] listCurrentUsers() {
        String[] users = new String[clientSessionsMap.size()];
        int i = 0;
        for(ClientSessionData user : clientSessionsMap.values()) {
            users[i] = user.userName; 
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
        // TODO: Why is this empty? And it seems to work... Aie.
        return success;
    }
    
    
    
    private class ClientSessionData { 
        
        final Integer remoteHashCode;
        final String userName;
        final Map<String, Set<Integer>> records;
        volatile boolean pingReceived;
        
        ClientSessionData(String userName, Integer remoteHashCode) {
            this.records = new HashMap<>();
            this.userName = userName;
            this.remoteHashCode = remoteHashCode;
            this.pingReceived = false;
        }
    }
    
    private class CheckClientSessionAlive implements Runnable {
        @Override
        public void run() {
            LinkedList<Integer> sessionsToRemove = new LinkedList<>();
            
            for(ClientSessionData session : clientSessionsMap.values()) {
                if( ! session.pingReceived)
                    sessionsToRemove.add(session.remoteHashCode);
                session.pingReceived = false;
            }
            
            for(Integer session : sessionsToRemove)
                userLoggedOut(session);
        }
    }
}