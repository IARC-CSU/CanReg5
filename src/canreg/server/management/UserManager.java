package canreg.server.management;

/**
 *
 * @author morten
 */
public class UserManager {
    
    /**
     * 
     */
    public UserManager(){
        
    }
    
    /**
     * 
     * @param username
     * @param password
     * @return
     */
    public boolean addUser(String username,String password){
        return true;
    }
    /**
     * 
     * @param username
     * @return
     */
    public boolean addUser(String username){
        return addUser(username,username);
    }
    
    /**
     * 
     * @param username
     * @param password
     * @return
     */
    public boolean verifyPassword(String username, String password) {
        return false;
    }

    /**
     * 
     * @param username
     * @param password
     * @return
     */
    public boolean changePassword(String username, String password) {
        return false;
    }
}
