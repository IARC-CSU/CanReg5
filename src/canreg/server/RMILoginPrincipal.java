/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server;

/**
 *
 * @author morten
 */
/** Class used for representing an authenticated user in the system. */
public class RMILoginPrincipal
   implements java.security.Principal
{
    private static boolean DEBUG;
    
    /**
     * Toggles debug status on or off.&ltp>
     * 
     * @param debug flag to set the debug status
     */
    public static void setDebug( boolean debug ) {
		DEBUG = debug;	
    }//end setDebug( boolean )
    
   /** The username */
   private String username;

   ////////////////////////
   /** Class constructor.  @param username The username of the user. */
   public RMILoginPrincipal(String username)
   {
        if(username == null) {
        throw new IllegalArgumentException("Null name");
        }
        this.username = username;
        if( DEBUG ) System.out.println( "\t[RMILoginPrincipal] Principal " + username + " successfully created." );
   }
   
   /** Returns the username of the user. @return The username. */
   public String getName()
   {
      return username;
   }
   
    @Override
     public String toString() {
        return "CanRegPrincipal: "+username;
  }
     
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(!(obj instanceof RMILoginPrincipal)) return false;
        RMILoginPrincipal another = (RMILoginPrincipal) obj;
        return username.equals(another.getName());
  }
    @Override
      public int hashCode() {
         return username.hashCode();
  }
}
