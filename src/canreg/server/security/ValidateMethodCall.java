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
package canreg.server.security;

import java.security.PrivilegedExceptionAction;

/**
 *
 * @author ervikm
 */
public class ValidateMethodCall implements PrivilegedExceptionAction {

    private final String priveledgedMethodName;

    /**
     *  Make sure that the current user (defined by its context) has
     *  the permissions to call the "methodName" method. For checking
     *  this, a ServerPermission is required.
     *
     *  authrmi.permissions.ServerPermission "methodName"
     *    -> authorizes the call of a certain method
     *  authrmi.permissions.ServerPermission "*"
     *    -> authorizes the call of all methods
     * @param methodName 
     */
    public ValidateMethodCall(String methodName) {
        priveledgedMethodName = methodName;
    }

    @Override
    public Object run() throws Exception {
        // Only has to check if the appropriate ServerPermission is owned by
        // the user. If not an exception is thrown.

        // disabled 26.03.2008 Morten
        // look into access control later!
        // AccessController.checkPermission(new ServerPermission(priveledgedMethodName));

        return null;
    }
}
