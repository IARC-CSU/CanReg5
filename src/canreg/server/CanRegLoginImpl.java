/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author morten
 */
public class CanRegLoginImpl extends UnicastRemoteObject
        implements CanRegLoginInterface {

    private CanRegServerInterface theServer;

    public CanRegLoginImpl(CanRegServerInterface server)
            throws RemoteException, MalformedURLException {
        this.theServer = server;
    }

    public CanRegServerInterface login(String username, String password)
            throws java.rmi.RemoteException, LoginException {

        LoginContext lc = new LoginContext("CanRegLogin", new RemoteCallbackHandler(username, password));
        lc.login();
        Subject user = lc.getSubject();

        theServer.userLoggedIn(username);
        
        // Return a reference to a proxy object that encapsulates the access
        // to the theServer, for this client
        return new CanRegServerProxy(user, theServer);
    }

    public String getSystemName() throws RemoteException {
        return theServer.getCanRegSystemName();
    }

    public String getSystemVersion() throws RemoteException {
        return Globals.VERSION_STRING;
    }
}

