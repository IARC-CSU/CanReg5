package canreg.server;

import javax.security.auth.login.LoginException;

/**
 *
 * @author morten
 */
public interface CanRegLoginInterface extends java.rmi.Remote{

    /**
     * 
     * @return
     * @throws java.rmi.RemoteException
     */
    public String getSystemName() throws java.rmi.RemoteException ;

    /**
     * 
     * @return
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
   public CanRegServerInterface login(String username, String password)
      throws java.rmi.RemoteException, SecurityException, LoginException;
}
;