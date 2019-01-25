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
import canreg.common.PasswordService;
import canreg.exceptions.SystemUnavailableException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.login.LoginException;

/**
 * &ltp> LoginModule authenticates users from a single credential only
 * &ltp> On successfull authentication, a &ltcode&gtTopsecurityPrincipal</code> object 
 * with the user name is added to the Subject.
 */
public class CanRegLoginModule implements LoginModule {

    private final boolean debug = Globals.DEBUG;

    // validation objects
    private Subject subject;
    private RMILoginPrincipal entity;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    // tracking authentication status
    private static final int NOT = 0,  OK = 1,  COMMIT = 2;
    private int status;
    // current user
    private String username;
    private char[] password;

    /**
     * Initializes the LoginModule.&ltp>
     *
     * @param subject the Subject to be authenticated as provided through the JAAS interface. &ltp>
     * @param callbackHandler a CallbackHandler for retrieving username and password from the user &ltp>
     * @param sharedState shared LoginModule state. &ltp>
     * @param options options specified in the login Configuration for this particular LoginModule 
     *   (in the java.security.auth.login.config file).
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {

        status = NOT;
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        // sets options according to the java.security.auth.login.config file
        RMILoginPrincipal.setDebug("true".equalsIgnoreCase((String) options.get("debug")));

    }//end initialize()
    /**
     * Simple console trace to system.out for debug purposes only.
     * &ltp>
     *
     * @param msg the message to be printed to the console
     *
     */
    private void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(CanRegLoginModule.class.getName()).log(Level.INFO, msg);
        }
    }

    /**
     * Authenticate the user based on a credential. 
     * If the credential matches as expected,
     * authentication succeeds - otherwise not.&ltp>
     *
     * @return true in all cases since this LoginModule should not be ignored.
     * @exception LoginException if this LoginModule is unable to perform the authentication.
     */
    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available to retrieve user credentials");
        }

        Callback callbacks[] = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        try {
            callbackHandler.handle(callbacks); //get the user credentials
            username = ((NameCallback) callbacks[0]).getName();

            debugOut("user entered user name: " + username);

            char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
            if (tmpPassword == null) // treat a NULL password as an empty password
            {
                tmpPassword = new char[0];
            }
            password = new char[tmpPassword.length];
            System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
            ((PasswordCallback) callbacks[1]).clearPassword(); //wipe out occurrences in memory
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            throw new LoginException("Error: " + ce.getCallback().toString());
        }

        // verify the username/password
        String passwordString = new String(password);

        String realPassword = null;

        try {
            InputStream passPropInputStream = new FileInputStream(Globals.CANREG_PASSWORD_FILE_NAME);
            Properties passwords = new Properties();
            passwords.loadFromXML(passPropInputStream);

            realPassword = passwords.getProperty(username);

        } catch (java.io.IOException e) {
            debugOut("File error: " + Globals.PASS_FILENAME + "\n");
            return false;
        }

        try {
            // debugOut("user entered password: " + PasswordService.getInstance().encrypt(passwordString) );

            if ((realPassword == null) || !realPassword.equals(PasswordService.getInstance().encrypt(passwordString))) {
                {
                    // debugOut("Password does not match: " + realPassword + " " + passwordString + "\n");
                    return false;
                }
            } else {
                debugOut("User "+username+" logged in.");
                status = OK;
                return true;
            }
        } catch (SystemUnavailableException exception) {
            debugOut("Passwordservice error.");
            return false;
        }
    // not good enough
    //  return false;
    }

    /**
     * &ltp> Method called if the LoginContext's
     * overall authentication succeeded (the relevant REQUIRED, 
     * REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * mentioned in file java.security.auth.login.config.. did succeed).
     *
     * &ltp> If this LoginModule's authentication succeeded 
     * (status stored in the variable 'status' by the
     * .login() method), then the .commit() method associates a
     * TopsecurityPrincipal with the Subject located in the
     * LoginModule. If this LoginModule's authentication failed, 
     * any state originally saved is removed.
     *
     * &ltp>
     *
     * @exception LoginException if the commit fails.
     * @return true if this LoginModule's own .login() and .commit()
     *		attempts succeeded, false otherwise.
     */
    @Override
    public boolean commit() throws LoginException {
        if (status == NOT || subject == null) {
            return false;
        } else {
            // add a Principal (authenticated identity) to the Subject

            // assume the user we authenticated is the TopsecurityPrincipal
            entity = new RMILoginPrincipal(username);
            entity.setUserRightLevel(getUserRightLevel(username));
            
            Set entities = subject.getPrincipals();
            if (!entities.contains(entity)) {
                entities.add(entity);
            }

            debugOut("added RMILoginPrincipal to Subject");

            // in any case, clean out state
            username = null;
            password = null;

            status = COMMIT;
            return true;
        }//end if
    }//end commit()
    /**
     * &ltp> This method is called if the LoginContext's
     * overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * mentioned in file java.security.auth.login.config.. did not succeed).
     *
     * &ltp> If this LoginModule's authentication succeeded 
     * (status stored in the variable 'status' by the
     * .login() method and .commit() methods),
     * then the .abort() method cleans up any state that was originally saved.
     *
     * &ltp>
     *
     * @exception LoginException if the abort fails.
     * @return false if this LoginModule's own login and/or commit attempts
     *		failed, true otherwise.
     */
    @Override
    public boolean abort() throws LoginException {
        if (status == NOT) {
            return false;
        } else if (status == OK) {
            // login succeeded but overall authentication failed
            username = null;
            if (password != null) {
                password = null;
            }
            entity = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }//end if/else
        status = NOT;
        return true;
    }//end abort()
    /**
     * Logout of the user.
     *
     * &ltp&gtRemoves the TopsecurityPrincipal added by the .commit() method.&ltp>
     *
     * @exception LoginException if the logout fails.
     * @return true in all cases since this LoginModule should not be ignored.
     */
    @Override
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(entity);
        status = NOT;
        username = null;
        if (password != null) {
            password = null;
        }
        entity = null;
        return true;
    }//end logout()

    private Globals.UserRightLevels getUserRightLevel(String username){

        try {
            InputStream levelsPropInputStream = null;
            levelsPropInputStream = new FileInputStream(Globals.CANREG_LEVELS_FILE_NAME);
            Properties levels = new Properties();
            levels.loadFromXML(levelsPropInputStream);

            String userLevel = levels.getProperty(username);
            return Globals.UserRightLevels.valueOf(userLevel);

        } catch (java.io.IOException e) {
            debugOut("File error: " + Globals.CANREG_LEVELS_FILE_NAME + "\n");
            return null;
        }
    }
}
