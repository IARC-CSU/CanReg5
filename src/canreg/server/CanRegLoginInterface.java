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

import javax.security.auth.login.LoginException;

/**
 *
 * @author ervikm
 */
public interface CanRegLoginInterface extends java.rmi.Remote{

    /**
     * Returns the name of the CanReg system running on the server
     * @return The name
     * @throws java.rmi.RemoteException
     */
    public String getSystemName() throws java.rmi.RemoteException ;

    /**
     * Returns the version of CanReg running on the server
     * @return The version number
     * @throws java.rmi.RemoteException
     */
    public String getSystemVersion()throws java.rmi.RemoteException ;
    
   /** Method that lets clients login, returning an interface to the server.
   * @param username The name of the user.
   * @param password The password of the user.
   * @return A reference to a proxy of the server object.
    * @throws java.rmi.RemoteException 
    * @throws SecurityException If the client is not allowed to login.
    * @throws LoginException 
    */
   public CanRegServerInterface login(String username, char[] password)
      throws java.rmi.RemoteException, SecurityException, LoginException;
}
;