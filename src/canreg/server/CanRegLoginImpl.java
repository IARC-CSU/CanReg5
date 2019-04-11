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
package canreg.server;

import canreg.common.Globals;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.*;
import javax.security.auth.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 *
 * @author ervikm
 */
public class CanRegLoginImpl extends UnicastRemoteObject
        implements CanRegLoginInterface {

    private CanRegServerInterface theServer;

    /**
     * 
     * @param server
     * @throws java.rmi.RemoteException
     * @throws java.net.MalformedURLException
     */
    public CanRegLoginImpl(CanRegServerInterface server)
            throws RemoteException, MalformedURLException {
        // Prevent JAVA to use a random port.
        super(1099);
        
        System.setProperty("java.security.auth.login.config", Globals.LOGIN_FILENAME);
        System.setProperty("java.security.policy", Globals.POLICY_FILENAME);
        this.theServer = server;
    }

    public CanRegLoginImpl(String serverCode, boolean isAdHocDB)
            throws RemoteException, MalformedURLException {
        System.setProperty("java.security.auth.login.config", Globals.LOGIN_FILENAME);
        System.setProperty("java.security.policy", Globals.POLICY_FILENAME);
        theServer = new CanRegServerImpl(serverCode, isAdHocDB);
    }

    @Override
    public CanRegServerInterface login(String username, char[] password)
            throws java.rmi.RemoteException, LoginException {
        LoginContext lc = new LoginContext("CanRegLogin", new RemoteCallbackHandler(username, password));
        lc.login();
        Subject user = lc.getSubject();

        theServer.userLoggedIn(username);

        // Return a reference to a proxy object that encapsulates the access
        // to the theServer, for this client
        return CanRegRegistryProxy.getInstance(theServer, theServer.getCanRegRegistryCode(), user);
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     */
    @Override
    public String getRegistryName() throws RemoteException {
        return theServer.getCanRegRegistryName();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     */
    @Override
    public String getSystemVersion() throws RemoteException {
        return theServer.getCanRegVersion();
    }
}
