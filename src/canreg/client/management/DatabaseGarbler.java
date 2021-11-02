/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.client.management;

import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.client.CanRegClientApp;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.GregorianCalendarCanReg;
import canreg.common.database.Patient;
import canreg.server.database.RecordLockedException;
import canreg.common.database.Tumour;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

    private static final Logger LOGGER = Logger.getLogger(DatabaseGarbler.class.getName());
    private Map<String, Integer> firstNames;
    private Document doc;
    private Map<StandardVariableNames, DatabaseVariablesListElement> standardVariablesMap;
    private final String[] firstNamesArray;
    private String GARBLING_MESSAGE = "Garbling, please wait.................";

    public DatabaseGarbler() throws RemoteException {

        // make tables of all the firstnames in the database by sex
        firstNames = CanRegClientApp.getApplication().getNameSexTables(null);
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

        DatabaseVariablesListElement patientIDVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.PatientID);
        DatabaseVariablesListElement patientRecordIDVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.PatientRecordID);
        DatabaseVariablesListElement patientRecordIDTumourTableVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.PatientRecordIDTumourTable);
        DatabaseVariablesListElement firstNameVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.FirstName);
        DatabaseVariablesListElement lastNameVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.Surname);
        DatabaseVariablesListElement sexVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.Sex);
        DatabaseVariablesListElement adressCodeVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.AddressCode);
        DatabaseVariablesListElement incidenceDateVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.IncidenceDate);
        DatabaseVariablesListElement birthDateVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.BirthDate);
        DatabaseVariablesListElement ageVariableListElement = standardVariablesMap.get(Globals.StandardVariableNames.BirthDate);
        Patient[] patients1 = new Patient[]{};

        try {
            // seed set to milliseconds since 01.01.1970
            Random rnd = new Random(new Date().getTime());
            // get all the patient record ids in the database
            DatabaseFilter filter = new DatabaseFilter();
            Set<DatabaseVariablesListElement> set = new TreeSet<DatabaseVariablesListElement>();
            set.add(standardVariablesMap.get(Globals.StandardVariableNames.PatientRecordID));
            filter.setDatabaseVariables(null);
            DistributedTableDescription distributedTableDescription = 
                    CanRegClientApp.getApplication().getDistributedTableDescription(filter, Globals.PATIENT_TABLE_NAME, null);
            rows = CanRegClientApp.getApplication()
                    .retrieveRows(distributedTableDescription.getResultSetID(), 0, distributedTableDescription.getRowCount(), null);

            // first randomly shuffle all the patient IDs - not evenly distributed, but random enough: http://blog.ryanrampersad.com/2008/10/13/shuffle-an-array-in-java/ 
            for (int i = 0; i < distributedTableDescription.getRowCount(); i++) {
                int randomPos = rnd.nextInt(distributedTableDescription.getRowCount());
                Object[] tempRow = rows[i];                
                rows[i] = rows[randomPos];
                rows[randomPos] = tempRow;
            }
                        
            // for all patients in the database
            for (int i = 0; i < distributedTableDescription.getRowCount(); i++) {
                if (task != null) {
                    task.firePropertyChange("progress",
                            (i - 1) * 100 / distributedTableDescription.getRowCount(),
                            (i) * 100 / distributedTableDescription.getRowCount());
                    task.firePropertyChange("message", "G", GARBLING_MESSAGE.substring(0, i % GARBLING_MESSAGE.length()));
                }

                Patient patient1;
                Patient patient2;
                Patient patient3;
                String newFirstName;
                Tumour[] tumors;
                GregorianCalendarCanReg incidenceDateCalendar = null;
                GregorianCalendarCanReg birthDateCalendar = null;

                try {
                    int patientDatabaseRecordID = (Integer) rows[i][0];
                    patient1 = (Patient) CanRegClientApp.getApplication().getRecord(patientDatabaseRecordID, Globals.PATIENT_TABLE_NAME, false, null);
                    if (patient1 != null) {
                        try {
                            patients1 = CanRegClientApp.getApplication().getPatientsByPatientID((String) patient1.getVariable(patientIDVariableListElement.getDatabaseVariableName()), true, null);
                        } catch (DistributedTableDescriptionException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }

                        // draw random firstname - of the same sex
                        newFirstName = firstNamesArray[rnd.nextInt(firstNamesArray.length)];
                        int sex = firstNames.get(newFirstName.toUpperCase());
                        String oldSexString = (String) patient1.getVariable(sexVariableListElement.getDatabaseVariableName());
                        if (oldSexString != null) {
                            int oldSex = Integer.parseInt(oldSexString);
                            while (sex != 9 && sex != oldSex) {
                                newFirstName = firstNamesArray[rnd.nextInt(firstNamesArray.length)];
                                sex = firstNames.get(newFirstName.toUpperCase());
                            }
                        }

                        // change birthDate
                        String newBirthDateString = (String) patient1.getVariable(birthDateVariableListElement.getDatabaseVariableName());
                        if (newBirthDateString != null && newBirthDateString.trim().length() == 8) {
                            try {
                                birthDateCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg(newBirthDateString, Globals.DATE_FORMAT_STRING);
                                if (birthDateCalendar != null && !(birthDateCalendar.isUnknownDay() && birthDateCalendar.isUnknownMonth())) {
                                    birthDateCalendar = new GregorianCalendarCanReg();
                                    birthDateCalendar.set(GregorianCalendarCanReg.YEAR, Integer.parseInt(DateHelper.getYear(newBirthDateString, Globals.DATE_FORMAT_STRING)));
                                    birthDateCalendar.set(GregorianCalendarCanReg.DAY_OF_YEAR, rnd.nextInt(365) + 1);
                                    newBirthDateString = DateHelper.parseGregorianCalendarCanRegToDateString(birthDateCalendar, Globals.DATE_FORMAT_STRING);
                                }
                            } catch (ParseException | IllegalArgumentException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                                new TechnicalError().errorDialog();
                            }
                        }

                        // read another random patient
                        // TODO read all patient records related to this patient
                        int patient2RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                        while (patient2RecordID == patientDatabaseRecordID) {
                            patient2RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                        }
                        patient2 = (Patient) CanRegClientApp.getApplication().getRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME, false, null);
                        while (patient2 == null) {
                            CanRegClientApp.getApplication().releaseRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME, null);
                            patient2RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                            patient2 = (Patient) CanRegClientApp.getApplication().getRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME, false, null);
                        }
                        // swap last names with this one
                        String oldLastName = (String) patient1.getVariable(lastNameVariableListElement.getDatabaseVariableName());
                        String newLastName = (String) patient2.getVariable(lastNameVariableListElement.getDatabaseVariableName());
                        patient2.setVariable(lastNameVariableListElement.getDatabaseVariableName(), oldLastName);

                        // and a third one
                        // TODO read all patient records related to this patient
                        int patient3RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                        while (patient3RecordID == patientDatabaseRecordID || patient3RecordID == patient2RecordID) {
                            patient3RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                        }
                        patient3 = (Patient) CanRegClientApp.getApplication().getRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME, false, null);
                        while (patient3 == null) {
                            CanRegClientApp.getApplication().releaseRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME, null);
                            patient3RecordID = rnd.nextInt(distributedTableDescription.getRowCount());
                            patient3 = (Patient) CanRegClientApp.getApplication().getRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME, false, null);
                        }

                        // swap adress fields with this one
                        // String oldAddresscode = (String) patient1.getVariable(adressCodeVariableListElement.getDatabaseVariableName());
                        // String newAddressCode = patient2.getVariable(adressCodeVariableListElement.getDatabaseVariableName();
                        // patient1.setVariable(adressCodeVariableListElement.getDatabaseVariableName(), newAddressCode));
                        // patient2.setVariable(adressCodeVariableListElement.getDatabaseVariableName(), oldAddresscode);

                        // update patient records
                        for (Patient patient : patients1) {
                            patient.setVariable(firstNameVariableListElement.getDatabaseVariableName(), newFirstName);
                            patient.setVariable(birthDateVariableListElement.getDatabaseVariableName(), newBirthDateString);
                            patient.setVariable(lastNameVariableListElement.getDatabaseVariableName(), newLastName);
                        }

                        try {
                            // get the tumours of this patient
                            tumors = CanRegClientApp.getApplication().
                                    getTumourRecordsBasedOnPatientID((String) patient1.getVariable(patientIDVariableListElement.getDatabaseVariableName()), false, null);
                            for (Tumour tumor : tumors) {
                                // change incidencedate
                                String incidenceDateString = (String) tumor.getVariable(incidenceDateVariableListElement.getDatabaseVariableName());
                                if (incidenceDateString != null && incidenceDateString.trim().length() == 8) {
                                    incidenceDateCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg(incidenceDateString, Globals.DATE_FORMAT_STRING);
                                    if (incidenceDateCalendar != null && !(incidenceDateCalendar.isUnknownDay() && incidenceDateCalendar.isUnknownMonth())) {
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
                                // point the tumour to the first patient record
                                tumor.setVariable(patientRecordIDTumourTableVariableListElement.getDatabaseVariableName(), patients1[0].getVariable(patientRecordIDVariableListElement.getDatabaseVariableName()));
                                CanRegClientApp.getApplication().releaseRecord((Integer) tumor.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME), Globals.TUMOUR_TABLE_NAME, null);
                                CanRegClientApp.getApplication().editRecord(tumor, null);

                            }

                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            new TechnicalError().errorDialog();
                        } 

                        // release the records
                        for (Patient patient : patients1) {
                            CanRegClientApp.getApplication().releaseRecord((Integer) patient.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME), Globals.PATIENT_TABLE_NAME, null);
                        }
                        CanRegClientApp.getApplication().releaseRecord(patient2RecordID, Globals.PATIENT_TABLE_NAME, null);
                        CanRegClientApp.getApplication().releaseRecord(patient3RecordID, Globals.PATIENT_TABLE_NAME, null);

                        // save the first record - drop the others
                        CanRegClientApp.getApplication().editRecord(patients1[0], null);

                        for (int j = 1; j < patients1.length; j++) {
                            int recordID = (Integer) patients1[j].getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                            CanRegClientApp.getApplication().deleteRecord(recordID, Globals.PATIENT_TABLE_NAME, null);
                        }
                        CanRegClientApp.getApplication().editRecord(patient2, null);
                        CanRegClientApp.getApplication().editRecord(patient3, null);

                    }
                } catch (RecordLockedException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    new TechnicalError().errorDialog();
                }
            }
            // add or subtract randomly up to 365 days on incidence date and bith date
            // recalculate age
            // swap adresses and modify numbers fields
            // generate random source number
            //
            //
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
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
