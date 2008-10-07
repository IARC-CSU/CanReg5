/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class NameSexRecord extends DatabaseRecord implements Serializable {

    private String name;
    private int sex;
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
