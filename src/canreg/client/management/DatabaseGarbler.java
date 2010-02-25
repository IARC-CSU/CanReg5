package canreg.client.management;

import cachingtableapi.DistributedTableDescription;
import cachingtableapi.DistributedTableDescriptionException;
import canreg.client.CanRegClientApp;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import canreg.server.database.Patient;
import canreg.server.database.RecordLockedException;
import canreg.server.database.Tumour;
import canreg.server.database.UnknownTableException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Task;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class DatabaseGarbler {

    private Map<String, Integer> firstNames;
    private Document doc;
    private TreeMap<String, DatabaseVariablesListElement> standardVariablesMap;
    private final String[] firstNamesArray;
    private String GARBLING_MESSAGE = "Garbling, please wait.................";

    public DatabaseGarbler() throws RemoteException {

        // make tables of all the firstnames in the database by sex
        firstNames = CanRegClientApp.getApplication().getNameSexTables();
        firstNamesArray = firstNames.keySet().toArray(new String[0]);
        // make table of all the lastnames in the database
        doc = CanRegClientApp.getApplication().getDatabseDescription();
        standardVariablesMap = canreg.common.Tools.buildStandardVariablesMap(canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE));
    }

    public void garble(Task<Object, Void> task) {

        if (task != null) {
            task.firePropertyChange("message", " ", "Getting ready to garble...");
        }
        Object[][] rows;

        DatabaseVariablesListElement patientIDVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.PatientID.toString().toUpperCase());
        DatabaseVariablesListElement firstNameVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.FirstName.toString().toUpperCase());
        DatabaseVariablesListElement lastNameVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.Surname.toString().toUpperCase());
        DatabaseVariablesListElement sexVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.Sex.toString().toUpperCase());
        DatabaseVariablesListElement adressCodeVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.AddressCode.toString().toUpperCase());
        DatabaseVariablesListElement incidenceDateVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.IncidenceDate.toString().toUpperCase());
        DatabaseVariablesListElement birthDateVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.BirthDate.toString().toUpperCase());
        DatabaseVariablesListElement ageVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.BirthDate.toString().toUpperCase());

        try {
            // seed set to milliseconds since 01.01.1970
            Random rnd = new Random(new Date().getTime());
            // get all the patient record ids in the database
            DatabaseFilter filter = new DatabaseFilter();
            Set<DatabaseVariablesListElement> set = new TreeSet<DatabaseVariablesListElement>();
            set.add(standardVariablesMap.get(Globals.StandardVariableNames.PatientRecordID.toString().toUpperCase()));
            filter.setDatabaseVariables(null);
            DistributedTableDescription distributedTableDescription = CanRegClientApp.getApplication().getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME);
            rows = CanRegClientApp.getApplication().retrieveRows(distributedTableDescription.getResultSetID(), 0, distributedTableDescription.getRowCount());

            // for all patients in the database
            for (int i = 0; i < distributedTableDescription.getRowCount(); i++) {
                if (task != null) {
                    task.firePropertyChange("progress",
                            (i - 1) * 100 / distributedTableDescription.getRowCount(),
                            (i) * 100 / distributedTableDescription.getRowCount());
                    task.firePropertyChange("message", "G", GARBLING_MESSAGE.substring(0, i % GARBLING_MESSAGE.length()));
                }

                Patient patient;
                Patient patient2;
                Patient patient3;
                String firstName;
                Tumour[] tumors;
                GregorianCalendarCanReg incidenceDateCalendar = null;
                GregorianCalendarCanReg birthDateCalendar = null;

                try {
                    int patientDatabaseRecordID = (Integer) rows[i][0];
                    patient = (Patient) CanRegClientApp.getApplication().getRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME, true);

                    // draw random firstname - of the same sex
                    firstName = firstNamesArray[rnd.nextInt(firstNamesArray.length)];
                    int sex = firstNames.get(firstName.toUpperCase());
                    String oldSexString = (String) patient.getVariable(sexVariableListElement.getDatabaseVariableName());
                    if (oldSexString != null) {
                        int oldSex = Integer.parseInt(oldSexString);
                        while (sex != 9 && sex != oldSex) {
                            firstName = firstNamesArray[rnd.nextInt(firstNamesArray.length)];
                            sex = firstNames.get(firstName.toUpperCase());
                        }
                    }
                    patient.setVariable(firstNameVariableListElement.getDatabaseVariableName(), firstName);

                    // change birthDate
                    String birthDateString = (String) patient.getVariable(birthDateVariableListElement.getDatabaseVariableName());
                    if (birthDateString != null && birthDateString.trim().length() == 8) {
                        try {
                            birthDateCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg(birthDateString, Globals.DATE_FORMAT_STRING);
                            if (birthDateCalendar!=null && !(birthDateCalendar.isUnknownDay() && birthDateCalendar.isUnknownMonth())) {
                                birthDateCalendar = new GregorianCalendarCanReg();
                                birthDateCalendar.set(GregorianCalendarCanReg.YEAR, Integer.parseInt(DateHelper.getYear(birthDateString, Globals.DATE_FORMAT_STRING)));
                                birthDateCalendar.set(GregorianCalendarCanReg.DAY_OF_YEAR, rnd.nextInt(365) + 1);
                                patient.setVariable(birthDateVariableListElement.getDatabaseVariableName(),
                                        DateHelper.parseGregorianCalendarCanRegToDateString(birthDateCalendar, Globals.DATE_FORMAT_STRING));
                            }
                        } catch (ParseException ex) {
                            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    patient.setVariable(birthDateVariableListElement.getDatabaseVariableName(), birthDateString);

                    // read another random patient
                    int patient2RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                    while (patient2RecordID == patientDatabaseRecordID) {
                        patient2RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                    }
                    patient2 = (Patient) CanRegClientApp.getApplication().getRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME, true);
                    while (patient2 == null) {
                        CanRegClientApp.getApplication().releaseRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME);
                        patient2RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                        patient2 = (Patient) CanRegClientApp.getApplication().getRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME, true);
                    }
                    // swap last names with this one
                    String lastName = (String) patient.getVariable(lastNameVariableListElement.getDatabaseVariableName());
                    patient.setVariable(lastNameVariableListElement.getDatabaseVariableName(), patient2.getVariable(lastNameVariableListElement.getDatabaseVariableName()));
                    patient2.setVariable(lastNameVariableListElement.getDatabaseVariableName(), lastName);

                    // and a third one
                    int patient3RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                    while (patient3RecordID == patientDatabaseRecordID || patient3RecordID == patient2RecordID) {
                        patient3RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                    }
                    patient3 = (Patient) CanRegClientApp.getApplication().getRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME, true);
                    while (patient3 == null) {
                        CanRegClientApp.getApplication().releaseRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME);
                        patient3RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                        patient3 = (Patient) CanRegClientApp.getApplication().getRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME, true);
                    }
                    // swap adress fields with this one
                    String addresscode = (String) patient.getVariable(adressCodeVariableListElement.getDatabaseVariableName());
                    patient.setVariable(adressCodeVariableListElement.getDatabaseVariableName(), patient2.getVariable(adressCodeVariableListElement.getDatabaseVariableName()));
                    patient2.setVariable(adressCodeVariableListElement.getDatabaseVariableName(), addresscode);

                    try {
                        // get the tumours of this patient
                        tumors = CanRegClientApp.getApplication().getTumourRecordsBasedOnPatientID((String) patient.getVariable(patientIDVariableListElement.getDatabaseVariableName()), true);
                        for (Tumour tumor : tumors) {
                            // change incidencedate
                            String incidenceDateString = (String) tumor.getVariable(incidenceDateVariableListElement.getDatabaseVariableName());
                            if (incidenceDateString != null && incidenceDateString.trim().length() == 8) {
                                incidenceDateCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg(incidenceDateString, Globals.DATE_FORMAT_STRING);
                                if (incidenceDateCalendar!=null && !(incidenceDateCalendar.isUnknownDay() && incidenceDateCalendar.isUnknownMonth())) {
                                    incidenceDateCalendar = new GregorianCalendarCanReg();
                                    incidenceDateCalendar.set(GregorianCalendarCanReg.YEAR, Integer.parseInt(DateHelper.getYear(incidenceDateString, Globals.DATE_FORMAT_STRING)));
                                    incidenceDateCalendar.set(GregorianCalendarCanReg.DAY_OF_YEAR, rnd.nextInt(365) + 1);
                                    tumor.setVariable(incidenceDateVariableListElement.getDatabaseVariableName(),
                                            DateHelper.parseGregorianCalendarCanRegToDateString(incidenceDateCalendar, Globals.DATE_FORMAT_STRING));
                                }
                                if (incidenceDateCalendar != null && birthDateCalendar != null) {
                                    int age = (int) DateHelper.yearsBetween(birthDateCalendar, incidenceDateCalendar);
                                    tumor.setVariable(ageVariableListElement.getDatabaseVariableName(), age);
                                }
                            }

                            CanRegClientApp.getApplication().releaseRecord((Integer) tumor.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME), Globals.TUMOUR_TABLE_NAME);
                            CanRegClientApp.getApplication().editRecord(tumor);
                        }

                    } catch (ParseException ex) {
                        Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // release the records
                    CanRegClientApp.getApplication().releaseRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME);
                    CanRegClientApp.getApplication().releaseRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME);
                    CanRegClientApp.getApplication().releaseRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME);

                    // save the records
                    CanRegClientApp.getApplication().editRecord(patient);
                    CanRegClientApp.getApplication().editRecord(patient2);
                    CanRegClientApp.getApplication().editRecord(patient3);


                } catch (RecordLockedException ex) {
                    Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // add or subtract randomly up to 365 days on incidence date and bith date
            // recalculate age
            // swap adresses and modify numbers fields
            // generate random source number
            //
            //
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownTableException ex) {
            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DatabaseGarbler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<String>[] getFirstNames() {
        Set<String>[] map = new TreeSet[2];
        map[0] = new TreeSet<String>();
        map[1] = new TreeSet<String>();
        DatabaseFilter filter = new DatabaseFilter();
        DistributedTableDescription newTableDatadescription = null;
        String tableName = Globals.PATIENT_TABLE_NAME;
        // filter.setFilterString(rangeFilterPanel.getFilter().trim());
        // filter.setRange(rangeFilterPanel.getRange());
        filter.setQueryType(DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR);
        // chosenVariables = variablesChooserPanel.getSelectedVariables();
        Set<DatabaseVariablesListElement> chosenVariables = new TreeSet<DatabaseVariablesListElement>();
        // canRegServer.getNameSexTables();
        filter.setDatabaseVariables(chosenVariables);
        return map;
    }
}
