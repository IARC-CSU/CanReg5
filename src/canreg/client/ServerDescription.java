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
    private boolean showCode = true;

    public ServerDescription(String name, String url, int port, String code, int id) {
        this.name = name;
        this.url = url;
        this.port = port;
        this.code = code;
        this.id = id;
    }

    @Override
    public String toString() {
        String str = getName();
        if (showCode) {
            str = getCode() + " - " + str;
            // show address if this is run on the network
            if (!url.equalsIgnoreCase("localhost")){
                str+=" ("+url+")";
            }
        }
        return str;
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

    public boolean isShowCode() {
        return showCode;
    }

    public void setShowCode(boolean showCode) {
        this.showCode = showCode;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ServerDescription && getId() == ((ServerDescription) obj).getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.id;
        return hash;
    }
}
