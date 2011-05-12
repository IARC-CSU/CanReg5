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


package canreg.common.database;

import canreg.common.Globals;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class User implements Serializable {

    static String ID_KEY = "ID";
    static String USERNAME_KEY = "USERNAME";
    static String PASSWORD_KEY = "PASSWORD";
    static String USER_LEVEL_KEY = "USER_LEVEL";
    static String EMAIL_KEY = "EMAIL";
    static String REAL_NAME_KEY = "REAL_NAME";

    private int ID = -1;
    private String userName;
    private Globals.UserRightLevels userRightLevel;
    private char[] password={};
    private String email="";
    private String realName="";

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userRightLevel
     */
    public Globals.UserRightLevels getUserRightLevel() {
        return userRightLevel;
    }

    /**
     * @param userRightLevel the userRightLevel to set
     */
    public void setUserRightLevel(Globals.UserRightLevels userRightLevel) {
        this.userRightLevel = userRightLevel;
    }

    @Override
    public String toString(){
        return userName + " ("+userRightLevel.toString()+")";
    }

    /**
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(char[] password) {
        this.password = password;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the realName
     */
    public String getRealName() {
        return realName;
    }

    /**
     * @param realName the realName to set
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     *
     * @return
     */
    public int getUserRightLevelIndex() {
        return userRightLevel.ordinal();
    }

    /**
     *
     * @param ordinal
     */
    public void setUserRightLevelIndex(int ordinal){
        userRightLevel = Globals.UserRightLevels.values()[ordinal];
    }

    /**
     *
     * @param userLevel
     */
    public void setUserRightLevel(String userLevel) {
        this.userRightLevel = Globals.UserRightLevels.valueOf(userLevel);
    }
}
