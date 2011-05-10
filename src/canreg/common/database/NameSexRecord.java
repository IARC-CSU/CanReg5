/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common.database;

import canreg.common.database.DatabaseRecord;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class NameSexRecord extends DatabaseRecord implements Serializable {

    private String name;
    private int sex;
    private int id;

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
    public int getSex() {
        return sex;
    }

    /**
     * 
     * @param sex
     */
    public void setSex(int sex) {
        this.sex = sex;
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
}
