/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server.security;

import java.security.PrivilegedExceptionAction;
import java.security.AccessController;

/**
 *
 * @author morten
 */
public class ValidateMethodCall implements PrivilegedExceptionAction {
    private String priveledgedMethodName;
    
   /**
   *  Make sure that the current user (defined by its context) has
   *  the permissions to call the "methodName" method. For checking
   *  this, a ServerPermission is required.
   *
   *  authrmi.permissions.ServerPermission "methodName"
   *    -> authorizes the call of a certain method
   *  authrmi.permissions.ServerPermission "*"
   *    -> authorizes the call of all methods
   */
    
    public ValidateMethodCall(String methodName)  {
       priveledgedMethodName = methodName;
    }

    public Object run() throws Exception {
        // Only has to check if the appropriate ServerPermission is owned by
        // the user. If not an exception is thrown.
        
        // disabled 26.03.2008 Morten
        // look into access control later!
         // AccessController.checkPermission(new ServerPermission(priveledgedMethodName));

        return null;
    }

}
