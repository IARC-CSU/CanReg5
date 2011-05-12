/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

package canreg.client;

public class ServerDescription {

    private String name;
    private String url;
    private int port;
    private String code;
    private int id;
    private boolean showCode = true;

    /**
     * 
     * @param name The name of the CanReg system on the server (i.e. Training Cancer Registry)
     * @param url The URL of the server (i.e. localhost)
     * @param port The port of the server (i.e. 1199)
     * @param code The code of the CanReg system on the server (i.e. TRN)
     * @param id
     */
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

    /**
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * 
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * 
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 
     * @return
     */
    public boolean isShowCode() {
        return showCode;
    }

    /**
     * 
     * @param showCode
     */
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
