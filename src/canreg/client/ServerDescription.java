/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.client;

/**
 *
 * @author morten
 */
public class ServerDescription {
    private String name;
    private String url;
    private int port;
    private String code;
    private int id;
    
    public ServerDescription(String name, String url, int port, String code, int id){
        this.name = name;
        this.url = url;
        this.port = port;
        this.code = code;
        this.id = id;
    }
    
    @Override
    public String toString(){
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
