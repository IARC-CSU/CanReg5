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
package canreg.common;

import canreg.server.*;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the CanRegLogin Service.
 * @author ervikm Ervik
 */
public class ServerLauncher {

    private static final Logger LOGGER = Logger.getLogger(ServerLauncher.class.getName());
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
        CanRegServerInterface server = null;
        LOGGER.log(Level.INFO, "starting the server");
        System.setProperty("java.security.auth.login.config", Globals.LOGIN_FILENAME);
        System.setProperty("java.security.policy", Globals.POLICY_FILENAME);
        System.setProperty("java.rmi.server.useCodebaseOnly", "true");
        // try to create the registry if needed
        try {
            LocateRegistry.createRegistry(port);
        } catch (RemoteException ex) {
            // if it is already running with another server that's fine
            LOGGER.log(Level.WARNING, null, ex);
        }

        try {
            String rmiAddresse = "rmi://" + systemURL + ":" + port + "/CanRegLogin" + systemCode;

            // assume already bound
            boolean alreadyBound = true;

            try {
                // Check to see if service is already bound
                Naming.lookup(rmiAddresse);
            } catch (NotBoundException ex) {
                // LOGGER.log(Level.INFO, null, ex);
                alreadyBound = false;
            }
            
            if (!alreadyBound) {
                server = new CanRegServerImpl(systemCode, false);
                CanRegLoginInterface service = new CanRegLoginImpl(server);
                Naming.bind(rmiAddresse, service);
                success = true;
            }

        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            success = false;
        } catch (AlreadyBoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            success = false;
            if (server != null) {
                try {
                    server.shutDownServer();
                } catch (RemoteException ex1) {
                    LOGGER.log(Level.SEVERE, null, ex1);
                } catch (SecurityException ex1) {
                    LOGGER.log(Level.SEVERE, null, ex1);
                } finally {
                    return success;
                }
            }
            success = false;
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
