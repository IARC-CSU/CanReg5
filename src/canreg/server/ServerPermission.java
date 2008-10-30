package canreg.server;

/**
 *
 * @author ervikm
 */
public class ServerPermission extends java.security.BasicPermission {

  /**
   * Creates a permission with a name.
   * @param name 
   */
  public ServerPermission(String name) {
    super(name);
  }

  /**
   * Creates a permission with a name and an action string.
   * The action string is not used, but this constructor must exist
   * so that the policy file parser works.
   * @param name 
   * @param actions
   */
  public ServerPermission(String name, String actions) {
    super(name, actions);
  }
}
