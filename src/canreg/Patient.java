/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author morten
 */
@Entity
@Table(name = "PATIENT")
@NamedQueries({@NamedQuery(name = "Patient.findById", query = "SELECT p FROM Patient p WHERE p.id = :id"), @NamedQuery(name = "Patient.findByFirstname", query = "SELECT p FROM Patient p WHERE p.firstname = :firstname"), @NamedQuery(name = "Patient.findByLastname", query = "SELECT p FROM Patient p WHERE p.lastname = :lastname"), @NamedQuery(name = "Patient.findByMiddlename", query = "SELECT p FROM Patient p WHERE p.middlename = :middlename"), @NamedQuery(name = "Patient.findByAddress", query = "SELECT p FROM Patient p WHERE p.address = :address"), @NamedQuery(name = "Patient.findByLastcontact", query = "SELECT p FROM Patient p WHERE p.lastcontact = :lastcontact"), @NamedQuery(name = "Patient.findByStatus", query = "SELECT p FROM Patient p WHERE p.status = :status"), @NamedQuery(name = "Patient.findByPersonsearch", query = "SELECT p FROM Patient p WHERE p.personsearch = :personsearch")})
public class Patient implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;
    @Column(name = "FIRSTNAME")
    private String firstname;
    @Column(name = "LASTNAME")
    private String lastname;
    @Column(name = "MIDDLENAME")
    private String middlename;
    @Column(name = "ADDRESS")
    private Integer address;
    @Column(name = "LASTCONTACT")
    @Temporal(TemporalType.DATE)
    private Date lastcontact;
    @Column(name = "STATUS")
    private Integer status;
    @Column(name = "PERSONSEARCH")
    private Integer personsearch;

    public Patient() {
    }

    public Patient(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        Integer oldId = this.id;
        this.id = id;
        changeSupport.firePropertyChange("id", oldId, id);
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        String oldFirstname = this.firstname;
        this.firstname = firstname;
        changeSupport.firePropertyChange("firstname", oldFirstname, firstname);
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        String oldLastname = this.lastname;
        this.lastname = lastname;
        changeSupport.firePropertyChange("lastname", oldLastname, lastname);
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        String oldMiddlename = this.middlename;
        this.middlename = middlename;
        changeSupport.firePropertyChange("middlename", oldMiddlename, middlename);
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        Integer oldAddress = this.address;
        this.address = address;
        changeSupport.firePropertyChange("address", oldAddress, address);
    }

    public Date getLastcontact() {
        return lastcontact;
    }

    public void setLastcontact(Date lastcontact) {
        Date oldLastcontact = this.lastcontact;
        this.lastcontact = lastcontact;
        changeSupport.firePropertyChange("lastcontact", oldLastcontact, lastcontact);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        Integer oldStatus = this.status;
        this.status = status;
        changeSupport.firePropertyChange("status", oldStatus, status);
    }

    public Integer getPersonsearch() {
        return personsearch;
    }

    public void setPersonsearch(Integer personsearch) {
        Integer oldPersonsearch = this.personsearch;
        this.personsearch = personsearch;
        changeSupport.firePropertyChange("personsearch", oldPersonsearch, personsearch);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Patient)) {
            return false;
        }
        Patient other = (Patient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "canregproto2.Patient[id=" + id + "]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

}
