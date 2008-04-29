/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author morten
 */
@Entity
@Table(name = "TUMOURS")
@NamedQueries({@NamedQuery(name = "Tumours.findById", query = "SELECT t FROM Tumours t WHERE t.id = :id"), @NamedQuery(name = "Tumours.findByRegistrynumber", query = "SELECT t FROM Tumours t WHERE t.registrynumber = :registrynumber"), @NamedQuery(name = "Tumours.findByAge", query = "SELECT t FROM Tumours t WHERE t.age = :age"), @NamedQuery(name = "Tumours.findByRecordstatus", query = "SELECT t FROM Tumours t WHERE t.recordstatus = :recordstatus"), @NamedQuery(name = "Tumours.findByCheckstatus", query = "SELECT t FROM Tumours t WHERE t.checkstatus = :checkstatus"), @NamedQuery(name = "Tumours.findByPersonsearch", query = "SELECT t FROM Tumours t WHERE t.personsearch = :personsearch"), @NamedQuery(name = "Tumours.findByFamilyname", query = "SELECT t FROM Tumours t WHERE t.familyname = :familyname"), @NamedQuery(name = "Tumours.findByFirstname", query = "SELECT t FROM Tumours t WHERE t.firstname = :firstname"), @NamedQuery(name = "Tumours.findByMaidenname", query = "SELECT t FROM Tumours t WHERE t.maidenname = :maidenname"), @NamedQuery(name = "Tumours.findBySex", query = "SELECT t FROM Tumours t WHERE t.sex = :sex"), @NamedQuery(name = "Tumours.findByYearofbirth", query = "SELECT t FROM Tumours t WHERE t.yearofbirth = :yearofbirth"), @NamedQuery(name = "Tumours.findByMonthofbirth", query = "SELECT t FROM Tumours t WHERE t.monthofbirth = :monthofbirth"), @NamedQuery(name = "Tumours.findByDayofbirth", query = "SELECT t FROM Tumours t WHERE t.dayofbirth = :dayofbirth"), @NamedQuery(name = "Tumours.findByAddress", query = "SELECT t FROM Tumours t WHERE t.address = :address"), @NamedQuery(name = "Tumours.findByYearofincidence", query = "SELECT t FROM Tumours t WHERE t.yearofincidence = :yearofincidence"), @NamedQuery(name = "Tumours.findByMonthofincidence", query = "SELECT t FROM Tumours t WHERE t.monthofincidence = :monthofincidence"), @NamedQuery(name = "Tumours.findByDayofincidence", query = "SELECT t FROM Tumours t WHERE t.dayofincidence = :dayofincidence"), @NamedQuery(name = "Tumours.findByTopography", query = "SELECT t FROM Tumours t WHERE t.topography = :topography"), @NamedQuery(name = "Tumours.findByMorphology", query = "SELECT t FROM Tumours t WHERE t.morphology = :morphology"), @NamedQuery(name = "Tumours.findByBehaviour", query = "SELECT t FROM Tumours t WHERE t.behaviour = :behaviour"), @NamedQuery(name = "Tumours.findByBasis", query = "SELECT t FROM Tumours t WHERE t.basis = :basis"), @NamedQuery(name = "Tumours.findByIcd10", query = "SELECT t FROM Tumours t WHERE t.icd10 = :icd10"), @NamedQuery(name = "Tumours.findByPatientid", query = "SELECT t FROM Tumours t WHERE t.patientid = :patientid"), @NamedQuery(name = "Tumours.findByMpsequence", query = "SELECT t FROM Tumours t WHERE t.mpsequence = :mpsequence"), @NamedQuery(name = "Tumours.findByMptotal", query = "SELECT t FROM Tumours t WHERE t.mptotal = :mptotal"), @NamedQuery(name = "Tumours.findByYearoflastcontact", query = "SELECT t FROM Tumours t WHERE t.yearoflastcontact = :yearoflastcontact"), @NamedQuery(name = "Tumours.findByMonthoflastcontact", query = "SELECT t FROM Tumours t WHERE t.monthoflastcontact = :monthoflastcontact"), @NamedQuery(name = "Tumours.findByDayoflastcontact", query = "SELECT t FROM Tumours t WHERE t.dayoflastcontact = :dayoflastcontact"), @NamedQuery(name = "Tumours.findByVitalstatus", query = "SELECT t FROM Tumours t WHERE t.vitalstatus = :vitalstatus"), @NamedQuery(name = "Tumours.findByYearofupdate", query = "SELECT t FROM Tumours t WHERE t.yearofupdate = :yearofupdate"), @NamedQuery(name = "Tumours.findByMonthofupdate", query = "SELECT t FROM Tumours t WHERE t.monthofupdate = :monthofupdate"), @NamedQuery(name = "Tumours.findByDayofupdate", query = "SELECT t FROM Tumours t WHERE t.dayofupdate = :dayofupdate"), @NamedQuery(name = "Tumours.findByIccc", query = "SELECT t FROM Tumours t WHERE t.iccc = :iccc"), @NamedQuery(name = "Tumours.findByFamilynamesoundex", query = "SELECT t FROM Tumours t WHERE t.familynamesoundex = :familynamesoundex"), @NamedQuery(name = "Tumours.findByFirstnamesoundex", query = "SELECT t FROM Tumours t WHERE t.firstnamesoundex = :firstnamesoundex")})
public class Tumours implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;
    @Column(name = "REGISTRYNUMBER", nullable = false)
    private String registrynumber;
    @Column(name = "AGE", nullable = false)
    private short age;
    @Column(name = "RECORDSTATUS", nullable = false)
    private short recordstatus;
    @Column(name = "CHECKSTATUS")
    private Short checkstatus;
    @Column(name = "PERSONSEARCH")
    private Short personsearch;
    @Column(name = "FAMILYNAME")
    private String familyname;
    @Column(name = "FIRSTNAME")
    private String firstname;
    @Column(name = "MAIDENNAME")
    private String maidenname;
    @Column(name = "SEX")
    private Short sex;
    @Column(name = "YEAROFBIRTH")
    private Integer yearofbirth;
    @Column(name = "MONTHOFBIRTH")
    private Integer monthofbirth;
    @Column(name = "DAYOFBIRTH")
    private Integer dayofbirth;
    @Column(name = "ADDRESS")
    private Integer address;
    @Column(name = "YEAROFINCIDENCE")
    private Integer yearofincidence;
    @Column(name = "MONTHOFINCIDENCE")
    private Integer monthofincidence;
    @Column(name = "DAYOFINCIDENCE")
    private Integer dayofincidence;
    @Column(name = "TOPOGRAPHY")
    private Integer topography;
    @Column(name = "MORPHOLOGY")
    private Integer morphology;
    @Column(name = "BEHAVIOUR")
    private Integer behaviour;
    @Column(name = "BASIS")
    private Integer basis;
    @Column(name = "ICD10")
    private Integer icd10;
    @Column(name = "PATIENTID")
    private Integer patientid;
    @Column(name = "MPSEQUENCE")
    private Integer mpsequence;
    @Column(name = "MPTOTAL")
    private Integer mptotal;
    @Column(name = "YEAROFLASTCONTACT")
    private Integer yearoflastcontact;
    @Column(name = "MONTHOFLASTCONTACT")
    private Integer monthoflastcontact;
    @Column(name = "DAYOFLASTCONTACT")
    private Integer dayoflastcontact;
    @Column(name = "VITALSTATUS")
    private Integer vitalstatus;
    @Column(name = "YEAROFUPDATE")
    private Integer yearofupdate;
    @Column(name = "MONTHOFUPDATE")
    private Integer monthofupdate;
    @Column(name = "DAYOFUPDATE")
    private Integer dayofupdate;
    @Column(name = "ICCC")
    private String iccc;
    @Column(name = "FAMILYNAMESOUNDEX")
    private String familynamesoundex;
    @Column(name = "FIRSTNAMESOUNDEX")
    private String firstnamesoundex;

    public Tumours() {
    }

    public Tumours(Integer id) {
        this.id = id;
    }

    public Tumours(Integer id, String registrynumber, short age, short recordstatus) {
        this.id = id;
        this.registrynumber = registrynumber;
        this.age = age;
        this.recordstatus = recordstatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        Integer oldId = this.id;
        this.id = id;
        changeSupport.firePropertyChange("id", oldId, id);
    }

    public String getRegistrynumber() {
        return registrynumber;
    }

    public void setRegistrynumber(String registrynumber) {
        String oldRegistrynumber = this.registrynumber;
        this.registrynumber = registrynumber;
        changeSupport.firePropertyChange("registrynumber", oldRegistrynumber, registrynumber);
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        short oldAge = this.age;
        this.age = age;
        changeSupport.firePropertyChange("age", oldAge, age);
    }

    public short getRecordstatus() {
        return recordstatus;
    }

    public void setRecordstatus(short recordstatus) {
        short oldRecordstatus = this.recordstatus;
        this.recordstatus = recordstatus;
        changeSupport.firePropertyChange("recordstatus", oldRecordstatus, recordstatus);
    }

    public Short getCheckstatus() {
        return checkstatus;
    }

    public void setCheckstatus(Short checkstatus) {
        Short oldCheckstatus = this.checkstatus;
        this.checkstatus = checkstatus;
        changeSupport.firePropertyChange("checkstatus", oldCheckstatus, checkstatus);
    }

    public Short getPersonsearch() {
        return personsearch;
    }

    public void setPersonsearch(Short personsearch) {
        Short oldPersonsearch = this.personsearch;
        this.personsearch = personsearch;
        changeSupport.firePropertyChange("personsearch", oldPersonsearch, personsearch);
    }

    public String getFamilyname() {
        return familyname;
    }

    public void setFamilyname(String familyname) {
        String oldFamilyname = this.familyname;
        this.familyname = familyname;
        changeSupport.firePropertyChange("familyname", oldFamilyname, familyname);
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        String oldFirstname = this.firstname;
        this.firstname = firstname;
        changeSupport.firePropertyChange("firstname", oldFirstname, firstname);
    }

    public String getMaidenname() {
        return maidenname;
    }

    public void setMaidenname(String maidenname) {
        String oldMaidenname = this.maidenname;
        this.maidenname = maidenname;
        changeSupport.firePropertyChange("maidenname", oldMaidenname, maidenname);
    }

    public Short getSex() {
        return sex;
    }

    public void setSex(Short sex) {
        Short oldSex = this.sex;
        this.sex = sex;
        changeSupport.firePropertyChange("sex", oldSex, sex);
    }

    public Integer getYearofbirth() {
        return yearofbirth;
    }

    public void setYearofbirth(Integer yearofbirth) {
        Integer oldYearofbirth = this.yearofbirth;
        this.yearofbirth = yearofbirth;
        changeSupport.firePropertyChange("yearofbirth", oldYearofbirth, yearofbirth);
    }

    public Integer getMonthofbirth() {
        return monthofbirth;
    }

    public void setMonthofbirth(Integer monthofbirth) {
        Integer oldMonthofbirth = this.monthofbirth;
        this.monthofbirth = monthofbirth;
        changeSupport.firePropertyChange("monthofbirth", oldMonthofbirth, monthofbirth);
    }

    public Integer getDayofbirth() {
        return dayofbirth;
    }

    public void setDayofbirth(Integer dayofbirth) {
        Integer oldDayofbirth = this.dayofbirth;
        this.dayofbirth = dayofbirth;
        changeSupport.firePropertyChange("dayofbirth", oldDayofbirth, dayofbirth);
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        Integer oldAddress = this.address;
        this.address = address;
        changeSupport.firePropertyChange("address", oldAddress, address);
    }

    public Integer getYearofincidence() {
        return yearofincidence;
    }

    public void setYearofincidence(Integer yearofincidence) {
        Integer oldYearofincidence = this.yearofincidence;
        this.yearofincidence = yearofincidence;
        changeSupport.firePropertyChange("yearofincidence", oldYearofincidence, yearofincidence);
    }

    public Integer getMonthofincidence() {
        return monthofincidence;
    }

    public void setMonthofincidence(Integer monthofincidence) {
        Integer oldMonthofincidence = this.monthofincidence;
        this.monthofincidence = monthofincidence;
        changeSupport.firePropertyChange("monthofincidence", oldMonthofincidence, monthofincidence);
    }

    public Integer getDayofincidence() {
        return dayofincidence;
    }

    public void setDayofincidence(Integer dayofincidence) {
        Integer oldDayofincidence = this.dayofincidence;
        this.dayofincidence = dayofincidence;
        changeSupport.firePropertyChange("dayofincidence", oldDayofincidence, dayofincidence);
    }

    public Integer getTopography() {
        return topography;
    }

    public void setTopography(Integer topography) {
        Integer oldTopography = this.topography;
        this.topography = topography;
        changeSupport.firePropertyChange("topography", oldTopography, topography);
    }

    public Integer getMorphology() {
        return morphology;
    }

    public void setMorphology(Integer morphology) {
        Integer oldMorphology = this.morphology;
        this.morphology = morphology;
        changeSupport.firePropertyChange("morphology", oldMorphology, morphology);
    }

    public Integer getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(Integer behaviour) {
        Integer oldBehaviour = this.behaviour;
        this.behaviour = behaviour;
        changeSupport.firePropertyChange("behaviour", oldBehaviour, behaviour);
    }

    public Integer getBasis() {
        return basis;
    }

    public void setBasis(Integer basis) {
        Integer oldBasis = this.basis;
        this.basis = basis;
        changeSupport.firePropertyChange("basis", oldBasis, basis);
    }

    public Integer getIcd10() {
        return icd10;
    }

    public void setIcd10(Integer icd10) {
        Integer oldIcd10 = this.icd10;
        this.icd10 = icd10;
        changeSupport.firePropertyChange("icd10", oldIcd10, icd10);
    }

    public Integer getPatientid() {
        return patientid;
    }

    public void setPatientid(Integer patientid) {
        Integer oldPatientid = this.patientid;
        this.patientid = patientid;
        changeSupport.firePropertyChange("patientid", oldPatientid, patientid);
    }

    public Integer getMpsequence() {
        return mpsequence;
    }

    public void setMpsequence(Integer mpsequence) {
        Integer oldMpsequence = this.mpsequence;
        this.mpsequence = mpsequence;
        changeSupport.firePropertyChange("mpsequence", oldMpsequence, mpsequence);
    }

    public Integer getMptotal() {
        return mptotal;
    }

    public void setMptotal(Integer mptotal) {
        Integer oldMptotal = this.mptotal;
        this.mptotal = mptotal;
        changeSupport.firePropertyChange("mptotal", oldMptotal, mptotal);
    }

    public Integer getYearoflastcontact() {
        return yearoflastcontact;
    }

    public void setYearoflastcontact(Integer yearoflastcontact) {
        Integer oldYearoflastcontact = this.yearoflastcontact;
        this.yearoflastcontact = yearoflastcontact;
        changeSupport.firePropertyChange("yearoflastcontact", oldYearoflastcontact, yearoflastcontact);
    }

    public Integer getMonthoflastcontact() {
        return monthoflastcontact;
    }

    public void setMonthoflastcontact(Integer monthoflastcontact) {
        Integer oldMonthoflastcontact = this.monthoflastcontact;
        this.monthoflastcontact = monthoflastcontact;
        changeSupport.firePropertyChange("monthoflastcontact", oldMonthoflastcontact, monthoflastcontact);
    }

    public Integer getDayoflastcontact() {
        return dayoflastcontact;
    }

    public void setDayoflastcontact(Integer dayoflastcontact) {
        Integer oldDayoflastcontact = this.dayoflastcontact;
        this.dayoflastcontact = dayoflastcontact;
        changeSupport.firePropertyChange("dayoflastcontact", oldDayoflastcontact, dayoflastcontact);
    }

    public Integer getVitalstatus() {
        return vitalstatus;
    }

    public void setVitalstatus(Integer vitalstatus) {
        Integer oldVitalstatus = this.vitalstatus;
        this.vitalstatus = vitalstatus;
        changeSupport.firePropertyChange("vitalstatus", oldVitalstatus, vitalstatus);
    }

    public Integer getYearofupdate() {
        return yearofupdate;
    }

    public void setYearofupdate(Integer yearofupdate) {
        Integer oldYearofupdate = this.yearofupdate;
        this.yearofupdate = yearofupdate;
        changeSupport.firePropertyChange("yearofupdate", oldYearofupdate, yearofupdate);
    }

    public Integer getMonthofupdate() {
        return monthofupdate;
    }

    public void setMonthofupdate(Integer monthofupdate) {
        Integer oldMonthofupdate = this.monthofupdate;
        this.monthofupdate = monthofupdate;
        changeSupport.firePropertyChange("monthofupdate", oldMonthofupdate, monthofupdate);
    }

    public Integer getDayofupdate() {
        return dayofupdate;
    }

    public void setDayofupdate(Integer dayofupdate) {
        Integer oldDayofupdate = this.dayofupdate;
        this.dayofupdate = dayofupdate;
        changeSupport.firePropertyChange("dayofupdate", oldDayofupdate, dayofupdate);
    }

    public String getIccc() {
        return iccc;
    }

    public void setIccc(String iccc) {
        String oldIccc = this.iccc;
        this.iccc = iccc;
        changeSupport.firePropertyChange("iccc", oldIccc, iccc);
    }

    public String getFamilynamesoundex() {
        return familynamesoundex;
    }

    public void setFamilynamesoundex(String familynamesoundex) {
        String oldFamilynamesoundex = this.familynamesoundex;
        this.familynamesoundex = familynamesoundex;
        changeSupport.firePropertyChange("familynamesoundex", oldFamilynamesoundex, familynamesoundex);
    }

    public String getFirstnamesoundex() {
        return firstnamesoundex;
    }

    public void setFirstnamesoundex(String firstnamesoundex) {
        String oldFirstnamesoundex = this.firstnamesoundex;
        this.firstnamesoundex = firstnamesoundex;
        changeSupport.firePropertyChange("firstnamesoundex", oldFirstnamesoundex, firstnamesoundex);
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
        if (!(object instanceof Tumours)) {
            return false;
        }
        Tumours other = (Tumours) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "canregproto2.Tumours[id=" + id + "]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

}
