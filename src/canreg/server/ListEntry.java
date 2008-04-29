/*
 * ListEntry.java
 *
 * Copyright 2006 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html .
 *
 */

package canreg.server;


/**
 *
 * @author John O'Conner
 */
public class ListEntry {
    
    /** Creates a new instance of ListEntry */
    public ListEntry() {
        this("","","", -1);
    }
    
    public ListEntry(String lastName, String firstName, String middleName, int id) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.id = id;
    }
    
    public String getName() {
        return lastName + ", " + firstName + " " + middleName;
    }
    
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String name) {
        this.lastName = name;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getMiddleName() {
        return middleName;
    }
    
    public void setMiddleName(String name) {
        this.middleName = name;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String toString( ){
        String value = lastName + ", " + firstName + " " + middleName + ": " + id;
        return value;
        
    }
    
    private String lastName, firstName, middleName;
    private int id;
}
