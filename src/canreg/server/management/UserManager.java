package canreg.server.management;

/**
 *
 * @author morten
 */
public class UserManager {
    
    public UserManager(){
        
    }
    
    public boolean addUser(String username,String password){
        return true;
    }
    public boolean addUser(String username){
        return addUser(username,username);
    }
    
    public boolean verifyPassword(String username, String password) {
        return false;
    }

    public boolean changePassword(String username, String password) {
        return false;
    }
}
