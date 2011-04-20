package canreg.common;

import canreg.server.*;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the CanRegLogin Service.
 * @author ervikm Ervik
 */
public class ServerLauncher {

    /**
     * @param systemCode 
     * @param port 
     * @param args the command line arguments
     * @return 
     * @throws AlreadyBoundException
     */
    public static boolean start(String systemURL, String systemCode, int port) throws AlreadyBoundException {
        // int port = Globals.RMI_PORT;
        boolean success = false;
        try {
            LocateRegistry.createRegistry(port);
        } catch (Exception exception) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.WARNING, "Registry already running...", exception);
        }

        System.setProperty("java.security.auth.login.config", Globals.LOGIN_FILENAME);
        System.setProperty("java.security.policy", Globals.POLICY_FILENAME);
        CanRegServerInterface server = null;
        try {

            server = new CanRegServerImpl(systemCode);
            CanRegLoginInterface service = new CanRegLoginImpl(server);
            Naming.bind("rmi://" + systemURL + ":" + port + "/CanRegLogin" + systemCode, service);
            success = true;

        } catch (MalformedURLException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
            if (server != null) {
                try {
                    server.shutDownServer();
                } catch (RemoteException ex1) {
                    Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex1);
                } catch (SecurityException ex1) {
                    Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            success = false;
        } catch (RemoteException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        }
        return success;
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length >= 1) {
                start("localhost", args[0], 1199);
            }
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
