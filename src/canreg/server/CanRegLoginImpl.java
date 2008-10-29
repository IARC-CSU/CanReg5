package canreg.server;

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

    /**
     * 
     * @param server
     * @throws java.rmi.RemoteException
     * @throws java.net.MalformedURLException
     */
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

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     */
    public String getSystemName() throws RemoteException {
        return theServer.getCanRegSystemName();
    }

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     */
    public String getSystemVersion() throws RemoteException {
        return theServer.getCanRegVersion();
    }
}

