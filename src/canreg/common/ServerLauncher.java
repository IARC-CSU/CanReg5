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
 * @author Morten Ervik
 */
public class ServerLauncher {

    /**
     * @param args the command line arguments
     */
    public static boolean start(String systemCode, int port) throws AlreadyBoundException {
        // int port = Globals.RMI_PORT;
        boolean success = false;
        try {
            LocateRegistry.createRegistry(port);
        } catch (Exception exception) {
            System.out.println("Registry already running...");
        // Logger.getLogger(ServerLauncher.class.getName()).log(Level.INFO, null, exception);
        }

        System.setProperty("java.security.auth.login.config", Globals.LOGIN_FILENAME);
        System.setProperty("java.security.policy", Globals.POLICY_FILENAME);

        try {

            CanRegServerInterface server = new CanRegServerImpl(systemCode);
            CanRegLoginInterface service = new CanRegLoginImpl(server);
            Naming.bind("rmi://localhost:" + port + "/CanRegLogin" + systemCode, service);
            success = true;

        } catch (MalformedURLException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (RemoteException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        }
        return success;
    }

    public static void main(String[] args) {
        try {
            if (args.length >= 1) {
                start(args[0], 1199);
            }
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(ServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
