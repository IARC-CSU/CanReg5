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

package canreg.server;

import canreg.common.Globals;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
/** Class used for representing an authenticated user in the system. */
public class RMILoginPrincipal
   implements java.security.Principal
{
    private static boolean DEBUG;

    private Globals.UserRightLevels userRightLevel;
    /**
     * Toggles debug status on or off.&ltp>
     * 
     * @param debug flag to set the debug status
     */
    public static void setDebug( boolean debug ) {
		DEBUG = debug;	
    }//end setDebug( boolean )
    
   /** The username */
   private String username;

   ////////////////////////
   /** Class constructor.  @param username The username of the user.
    * @param username 
    */
   public RMILoginPrincipal(String username)
   {
        if(username == null) {
        throw new IllegalArgumentException("Null name");
        }
        this.username = username;
        if( DEBUG )
            Logger.getLogger(RMILoginPrincipal.class.getName()).log(Level.INFO, "Principal {0} successfully created.", username);
   }
   
   /** Returns the username of the user. @return The username. */
    @Override
   public String getName()
   {
      return username;
   }
   
    @Override
     public String toString() {
        return "CanRegPrincipal: "+username;
  }
     
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(!(obj instanceof RMILoginPrincipal)) return false;
        RMILoginPrincipal another = (RMILoginPrincipal) obj;
        return username.equals(another.getName());
  }
    @Override
      public int hashCode() {
         return username.hashCode();
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
}
