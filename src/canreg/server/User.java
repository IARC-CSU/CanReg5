/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server;

import canreg.common.Globals;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class User implements Serializable {
    private String userName;
    private Globals.UserRightLevels userRightLevel;

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
}
