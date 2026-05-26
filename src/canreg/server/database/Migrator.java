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
package canreg.server.database;

import canreg.client.CanRegClientApp;
import canreg.client.gui.tools.WaitFrame;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.Patient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class Migrator {

    private static final Logger LOGGER = Logger.getLogger(Migrator.class.getName());
    private String newVersion;
    private CanRegDAO canRegDAO;

    /**
     *
     * @param newVersion
     * @param canRegDAO
     */
    public Migrator(String newVersion, CanRegDAO canRegDAO) {
        this.newVersion = newVersion;
        this.canRegDAO = canRegDAO;
    }

    /**
     *
     * @param descriptionFilePath path to the description xml file
     * @return if the initialize() needs to be done again
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public boolean migrate(String descriptionFilePath) throws ParserConfigurationException, IOException, SAXException {
        String databaseVersion = canRegDAO.getSystemPropery("DATABASE_VERSION");

        if (databaseVersion == null) {
            databaseVersion = "4.99.0";
        }

        if (!databaseVersion.equalsIgnoreCase(newVersion)) {
            if (databaseVersion.compareTo("4.99.5") < 0) {
                migrateTo_4_99_5(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.06") < 0) {
                migrateTo_5_00_06(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.17") < 0) {
                migrateTo_5_00_17(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.19") < 0) {
                migrateTo_5_00_19(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.43") < 0) {
                migrateTo_5_00_43(canRegDAO);
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.43") >= 0) {
                // after or equals to version 5.00.44
                String namespace = Globals.NAMESPACE;
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new File(descriptionFilePath));
                NodeList variables = doc.getDocumentElement().getChildNodes();
                NodeList nl = doc.getElementsByTagName(namespace + "variable");
                boolean uuidPresent = false;
                for (int i = 0; i < nl.getLength(); i++) {
                    Element e = (Element) nl.item(i);
                    String aaa = e.getElementsByTagName(namespace+ "variable_type").item(0).getTextContent();
                    if (aaa.equalsIgnoreCase(Globals.VARIABLE_TYPE_UUID)) {
                        uuidPresent = true;
                        System.out.println(aaa);
                    }
                }
                if (!uuidPresent) {
                    Element uuid = doc.createElement(namespace + "variable");
                    uuid.appendChild(createElement(doc, namespace + "variable_id", String.valueOf(nl.getLength())));
                    uuid.appendChild(createElement(doc, namespace + "full_name", Globals.VARIABLE_TYPE_UUID));
                    uuid.appendChild(createElement(doc, namespace + "short_name", Globals.VARIABLE_TYPE_UUID));
                    uuid.appendChild(createElement(doc, namespace + "english_name", Globals.VARIABLE_TYPE_UUID));
                    uuid.appendChild(createElement(doc, namespace + "group_id", "-1"));
                    uuid.appendChild(createElement(doc, namespace + "fill_in_status", "System"));
                    uuid.appendChild(createElement(doc, namespace + "multiple_primary_copy", "Othr"));
                    uuid.appendChild(createElement(doc, namespace + "variable_type", "UUID"));
                    uuid.appendChild(createElement(doc, namespace + "table", "Patient"));
                    uuid.appendChild(createElement(doc, namespace + "standard_variable_name", Globals.VARIABLE_TYPE_UUID));
                    for (int i=0; i < variables.getLength(); i++) {
                        System.out.println(variables.item(i).getNodeName());
                        if (variables.item(i).getNodeName().equals(namespace+"variables")) {
                            variables.item(i).appendChild(uuid);
                            break;
                        }
                    }
                    try (FileOutputStream output = new FileOutputStream(descriptionFilePath)) {
                        writeXml(doc, output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (TransformerException ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
            }
            if (databaseVersion.length() < 7 || databaseVersion.substring(0, 7).compareTo("5.00.44") < 0) {
                JDesktopPane desktopPane = CanRegClientApp.getApplication().getDesktopPane();
                for (Component component: desktopPane.getComponents()) {
                    if (component.getClass() == WaitFrame.class) {
                        WaitFrame waitFrame = (WaitFrame) component;
                        waitFrame.setLabel(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("MIGRATING_DATABASE_LABEL"));
                        waitFrame.setTitle(java.util.ResourceBundle.getBundle("canreg/client/gui/resources/LoginInternalFrame").getString("MIGRATING_DATABASE_TITLE"));
                        waitFrame.setSize(600, 127);
                        waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
                        break;
                    }
                }
                migrateTo_5_00_44(canRegDAO);
            }
        }
        // canRegDAO.setSystemPropery("DATABASE_VERSION", newVersion);
        return false;
    }

    private void migrateTo_4_99_5(CanRegDAO db) {
        // no users in the table from before so we just drop it
        db.dropAndRebuildUsersTable();
        db.setSystemPropery("DATABASE_VERSION", "4.99.5");
        LOGGER.log(Level.INFO, "Migrated the database to version 4.99.5.");
    }

    private void migrateTo_5_00_06(CanRegDAO db) {
        try {
            db.dropAndRebuildKeys();
            db.setSystemPropery("DATABASE_VERSION", "5.00.06");
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.06.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void migrateTo_5_00_17(CanRegDAO db) {
        try {
            db.addColumnToTable("USER_ROLE", "INT", Globals.USERS_TABLE_NAME);
            db.setSystemPropery("DATABASE_VERSION", "5.00.17");
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.17.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void migrateTo_5_00_19(CanRegDAO db) {
        try {
            db.dropColumnFromTable("USER_ROLE", Globals.USERS_TABLE_NAME );
            db.addColumnToTable("USER_ROLE", "VARCHAR(255)", Globals.USERS_TABLE_NAME);
            db.setSystemPropery("DATABASE_VERSION", "5.00.19");
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.19.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    private void migrateTo_5_00_43(CanRegDAO db) {
        try {
            try {            
                db.upgrade();
                db.setSystemPropery("DATABASE_VERSION", "5.00.43");
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }            
            LOGGER.log(Level.INFO, "Migrated the database to version 5.00.43.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void migrateTo_5_00_44(CanRegDAO db) {
        try {
            try {
                db.upgrade();

                db.addColumnToTable("UUID", "VARCHAR(36) UNIQUE", Globals.PATIENT_TABLE_NAME); // add new column to the Patient table
                ArrayList<Patient> patients = db.getAllPatients();
                // set UUID to be not null & unique
                for (Patient patient : patients) {
                    patient.setUuid(); // set UUID
                    db.editPatient(patient, true); // update the patient in the database
                }
                db.setSystemPropery("DATABASE_VERSION", "5.00.44"); // update version number
            } catch (SQLException | UnknownTableException | DistributedTableDescriptionException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (RecordLockedException e) {
                LOGGER.log(Level.SEVERE, "record is locked", e);
                System.out.println("record is locked!");
            }
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        LOGGER.log(Level.INFO, "Migrated the database to version 5.00.44.");
    }

    private Element createElement(Document doc, String variableName, String value) {
        Element childElement = doc.createElement(variableName);
        childElement.appendChild(doc.createTextNode(value));
        return childElement;
    }

    private static void writeXml(Document doc, OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }
}
