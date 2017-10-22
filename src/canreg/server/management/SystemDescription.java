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
package canreg.server.management;

import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseIndexesListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.PersonSearchVariable;
import canreg.common.Tools;
import canreg.common.qualitycontrol.PersonSearcher;
import canreg.server.database.QueryGenerator;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author ervikm
 */
public final class SystemDescription {

    private static boolean debug = Globals.DEBUG;
    private Document doc;
    // private DOMParser parser;
    private String namespace = "ns3:";
    private String[] canreg4dateFormats = new String[]{"dd/MM/yyyy", "MM/dd/yyyy", "budhist", "yyyy/MM/dd"};
    private DatabaseDictionaryListElement[] dictionaryListElements;
    private DatabaseGroupsListElement[] groupListElements;
    private DatabaseVariablesListElement[] variableListElements;
    private DatabaseIndexesListElement[] indexListElements;

    /**
     * 
     * @param fileName
     */
    public SystemDescription(String fileName) {
        try {
            setSystemDescriptionXML(fileName);
            //For debuging purposes
            if (debug) {
                //canreg.server.xml.Tools.writeXmlFile(doc, "test.xml");
                //canreg.server.xml.Tools.flushXMLout(doc);
                debugOut(QueryGenerator.strCreateVariableTable("Patient", doc));
                debugOut(QueryGenerator.strCreateVariableTable("Tumour", doc));
                debugOut(QueryGenerator.strCreateVariableTable("Source", doc));
                debugOut(QueryGenerator.strCreateDictionaryTable(doc));
                debugOut(QueryGenerator.strCreateTablesOfDictionaries(doc));
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param fileName
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws ParserConfigurationException
     */
    public void setSystemDescriptionXML(String fileName) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(new File(fileName));
        groupListElements = Tools.getGroupsListElements(doc, namespace);
        dictionaryListElements = Tools.getDictionaryListElements(doc, namespace);
        variableListElements = Tools.getVariableListElements(doc, Globals.NAMESPACE, getDictionaryMap(), getGroupMap());
        indexListElements = Tools.getIndexesListElements(doc, namespace, getVariablesMap());
    }

    /**
     * 
     * @return
     */
    public Document getSystemDescriptionDocument() {
        return doc;
    }

    /**
     * 
     * @return
     */
    public String getSystemName() {
        String name = null;

        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "registry_name");

            if (nl != null) {
                Element element = (Element) nl.item(0);
                name = element.getTextContent();
            }
        }
        return name;
    }

    /**
     *
     * @return
     */
    public String getRegion() {
        String region = null;

        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "region_code");

            if (nl != null) {
                Element element = (Element) nl.item(0);
                region = element.getTextContent();
                if (region != null && region.trim().length() > 0) {
                    int regionID = Integer.parseInt(region.trim());
                    try {
                        region = Globals.REGIONS[regionID];
                    } catch (NumberFormatException nfe) {
                        // not a number
                    }
                }
            }
        }

        return region;
    }

    /**
     * 
     * @return
     */
    public String getSystemCode() {
        String name = null;

        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "registry_code");

            if (nl != null) {
                Element element = (Element) nl.item(0);
                name = element.getTextContent();
            }
        }
        return name;
    }

    /**
     * 
     * @param systemName
     */
    public void setSystemName(String systemName) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "registry_name");

            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(systemName);
            }
        }
    }

    /**
     * 
     * @return
     */
    public Element getVariables() {
        Element element = null;
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "variables");
            if (nl != null) {
                element = (Element) nl.item(0);
            }
        }
        return element;
    }

    /**
     *
     * @return
     */
    public DatabaseVariablesListElement[] getDatabaseVariableListElements() {
        return variableListElements;
    }

    /**
     *
     * @return
     */
    public DatabaseDictionaryListElement[] getDatabaseDictionaryListElements() {
        return dictionaryListElements;
    }

    /**
     *
     * @return
     */
    public DatabaseGroupsListElement[] getDatabaseGroupsListElements() {
        return groupListElements;
    }

    /**
     *
     * @return
     */
    public DatabaseIndexesListElement[] getDatabaseIndexesListElements() {
        return indexListElements;
    }

    /**
     * 
     * @return
     */
    public Element getIndexes() {
        Element element = null;
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "indexes");
            if (nl != null) {
                element = (Element) nl.item(0);
            }
        }
        return element;
    }

    /**
     *
     * @return
     */
    public String getDateFormat() {
        Element element = null;
        String dateFormat = null;
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "date_format");
            if (nl != null) {
                element = (Element) nl.item(0);
                dateFormat = element.getTextContent();
                if (dateFormat.trim().length() == 1) {
                    try {
                        int i = Integer.parseInt(dateFormat);
                        dateFormat = canreg4dateFormats[i];
                    } catch (NumberFormatException nfe) {
                        // do nothing
                    }
                }
            }
        }
        return dateFormat;
    }

    /**
     *
     * @param elementName
     * @return
     */
    public String getTextContentFromElement(String elementName) {
        Element element = null;
        String elementContent = null;
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + elementName);
            if (nl != null) {
                element = (Element) nl.item(0);
                elementContent = element.getTextContent();
            }
        }
        return elementContent;
    }

    /**
     *
     * @param elementName
     * @param content
     */
    public void setTextContentForExistingElement(String elementName, String content) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "elementName");
            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(content);
            }
        }
    }

    /**
     *
     * @param variables
     */
    public void setVariables(DatabaseVariablesListElement[] variables) {
        Element variablesParentElement = (Element) doc.getElementsByTagName(namespace + "variables").item(0);
        if (variablesParentElement == null) {
            variablesParentElement = doc.createElement(namespace + "variables");
            doc.appendChild(variablesParentElement);
        }
        //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
        while (variablesParentElement.hasChildNodes()) {
            variablesParentElement.removeChild(variablesParentElement.getLastChild());
        }
        for (DatabaseVariablesListElement variable : variables) {
            Element element = createVariable(variable);
            variablesParentElement.appendChild(element);
        }
    }

    /**
     *
     * @param dictionaries
     */
    public void setDictionaries(DatabaseDictionaryListElement[] dictionaries) {
        Element parentElement = (Element) doc.getElementsByTagName(namespace + "dictionaries").item(0);
        if (parentElement == null) {
            parentElement = doc.createElement(namespace + "dictionaries");
            doc.appendChild(parentElement);
        }
        //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
        while (parentElement.hasChildNodes()) {
            parentElement.removeChild(parentElement.getLastChild());
        }
        for (DatabaseDictionaryListElement dictionary : dictionaries) {
            Element element = createDictionary(dictionary);
            parentElement.appendChild(element);
        }
    }

    /**
     *
     * @param groups
     */
    public void setGroups(DatabaseGroupsListElement[] groups) {
        Element parentElement = (Element) doc.getElementsByTagName(namespace + "groups").item(0);
        if (parentElement == null) {
            parentElement = doc.createElement(namespace + "groups");
            doc.appendChild(parentElement);
        }
        //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
        while (parentElement.hasChildNodes()) {
            parentElement.removeChild(parentElement.getLastChild());
        }
        for (DatabaseGroupsListElement group : groups) {
            Element element = createGroup(group);
            parentElement.appendChild(element);
        }
    }

    private Element createVariable(DatabaseVariablesListElement variable) {
        Element element = doc.createElement(namespace + "variable");
        element.appendChild(createElement(namespace + "variable_id", "" + variable.getVariableID()));
        element.appendChild(createElement(namespace + "full_name", variable.getFullName()));
        element.appendChild(createElement(namespace + "short_name", variable.getShortName()));
        element.appendChild(createElement(namespace + "english_name", variable.getEnglishName()));
        element.appendChild(createElement(namespace + "group_id", "" + variable.getGroupID()));
        element.appendChild(createElement(namespace + "fill_in_status", variable.getFillInStatus()));
        element.appendChild(createElement(namespace + "multiple_primary_copy", variable.getMultiplePrimaryCopy()));
        element.appendChild(createElement(namespace + "variable_type", variable.getVariableType()));
        // Varb Type  (0 number, 1 alpha, 2 date, 3 dict, 4 asian text)
        if (variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME)
                || variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)
                || variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME)
                || variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_TEXT_AREA_NAME)
                /*<ictl.co>*/ || variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NCID_NAME)/*</ictl.co>*/) {
            element.appendChild(createElement(namespace + "variable_length", "" + variable.getVariableLength()));
        } else if (variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
            element.appendChild(createElement(namespace + "variable_length", "" + Globals.DATE_FORMAT_STRING.length()));
        } else if (variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME)) {
            element.appendChild(createElement(namespace + "use_dictionary", "" + variable.getUseDictionary()));
            // (0 Simple, 1 Compound)
        } else {
            debugOut("Invalid variable description...");
        }
        // Place variable in the right table
        element.appendChild(createElement(namespace + "table", variable.getTable()));
        // variableToTableMap.put(shortName, table);
        if (variable.getStandardVariableName() != null) {
            element.appendChild(createElement(namespace + "standard_variable_name", variable.getStandardVariableName()));
        }
        element.appendChild(createElement(namespace + "variable_X_pos", "" + variable.getXPos()));
        element.appendChild(createElement(namespace + "variable_Y_pos", "" + variable.getYPos()));
        return element;
    }

    private Element createDictionary(DatabaseDictionaryListElement dictionary) {
        Element element = doc.createElement(namespace + "dictionary");
        element.appendChild(createElement(namespace + "dictionary_id", "" + dictionary.getDictionaryID()));
        element.appendChild(createElement(namespace + "name", dictionary.getName()));
        element.appendChild(createElement(namespace + "font", dictionary.getFont()));
        element.appendChild(createElement(namespace + "type", dictionary.getType()));
        element.appendChild(createElement(namespace + "code_length", "" + dictionary.getCodeLength()));
        element.appendChild(createElement(namespace + "category_description_length", "" + dictionary.getCategoryDescriptionLength()));
        element.appendChild(createElement(namespace + "full_dictionary_code_length", "" + dictionary.getFullDictionaryCodeLength()));
        element.appendChild(createElement(namespace + "full_dictionary_description_length", "" + dictionary.getFullDictionaryCategoryDescriptionLength()));
        if (dictionary.isLocked()) {
            element.appendChild(createElement(namespace + "locked", "true"));
        }
        return element;
    }

    private Element createElement(String variableName, String value) {
        Element childElement = doc.createElement(variableName);
        childElement.appendChild(doc.createTextNode(value));
        return childElement;
    }

    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.INFO, msg);
        }
    }

    /**
     *
     * @param path
     */
    public boolean saveSystemDescriptionXML(String path) {
        boolean success = false;
        File file = new File(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER); // Check to see it the canreg system folder exists
        if (!file.exists()) {
            file.mkdirs(); // create it if necessary
        }
        try {
            canreg.server.xml.Tools.writeXmlFile(doc, path);
            success = true;
        } catch (RuntimeException npe) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.SEVERE, "Error writing system description...");
        }
        return success;
    }

    private TreeMap<String, DatabaseDictionaryListElement> getDictionaryMap() {
        TreeMap<String, DatabaseDictionaryListElement> dictionaryMap = new TreeMap<String, DatabaseDictionaryListElement>();
        for (DatabaseDictionaryListElement dictionary : dictionaryListElements) {
            dictionaryMap.put(dictionary.getName(), dictionary);
        }
        return dictionaryMap;
    }

    private TreeMap<String, DatabaseGroupsListElement> getGroupMap() {
        TreeMap<String, DatabaseGroupsListElement> groupsMap = new TreeMap<String, DatabaseGroupsListElement>();
        for (DatabaseGroupsListElement group : groupListElements) {
            groupsMap.put(group.getGroupIndex() + "", group);
        }
        return groupsMap;
    }

    private TreeMap<String, DatabaseVariablesListElement> getVariablesMap() {
        TreeMap<String, DatabaseVariablesListElement> variablesMap = new TreeMap<String, DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement variable : variableListElements) {
            variablesMap.put(canreg.common.Tools.toUpperCaseStandardized(variable.getDatabaseVariableName()), variable);
        }
        return variablesMap;
    }

    private Element createGroup(DatabaseGroupsListElement group) {
        Element element = doc.createElement(namespace + "group");
        element.appendChild(createElement(namespace + "group_id", "" + group.getGroupIndex()));
        element.appendChild(createElement(namespace + "name", group.getGroupName()));
        element.appendChild(createElement(namespace + "group_pos", "" + group.getGroupPosition()));
        return element;
    }

    /**
     *
     * @param registryCode
     */
    public void setRegistryCode(String registryCode) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "registry_code");
            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(registryCode);
            }
        }
    }

    /**
     *
     * @param regionCode
     */
    public void setRegionCode(int regionCode) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "region_code");
            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(regionCode + "");
            }
        }
    }

    /**
     *
     * @param databaseIndexesListElement
     */
    public void setIndexes(DatabaseIndexesListElement[] databaseIndexesListElement) {
        Element parentElement = (Element) doc.getElementsByTagName(namespace + "indexes").item(0);
        if (parentElement == null) {
            parentElement = doc.createElement(namespace + "indexes");
            doc.appendChild(parentElement);
        }
        //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
        while (parentElement.hasChildNodes()) {
            parentElement.removeChild(parentElement.getLastChild());
        }
        for (DatabaseIndexesListElement index : databaseIndexesListElement) {
            Element element = createIndex(index);
            parentElement.appendChild(element);
        }
    }

    private Element createIndex(DatabaseIndexesListElement index) {
        Element element = doc.createElement(namespace + "index");
        element.appendChild(createElement(namespace + "name", index.getIndexName()));
        element.appendChild(createElement(namespace + "table", index.getDatabaseTableName()));
        for (DatabaseVariablesListElement variable : index.getVariableListElementsInIndex()) {
            Element variableElement = doc.createElement(namespace + "indexed_variable");
            variableElement.appendChild(createElement(namespace + "variable_name", variable.getDatabaseVariableName()));
            element.appendChild(variableElement);
        }
        return element;
    }

    /**
     *
     * @param personSearcher
     */
    public void setPersonSearcher(PersonSearcher personSearcher) {
        Element parentElement = (Element) doc.getElementsByTagName(namespace + "search_variables").item(0);
        if (parentElement == null) {
            parentElement = doc.createElement(namespace + "search_variables");
            doc.appendChild(parentElement);
        }
        //  debugOut(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
        while (parentElement.hasChildNodes()) {
            parentElement.removeChild(parentElement.getLastChild());
        }
        for (PersonSearchVariable variable : personSearcher.getSearchVariables()) {
            Element element = createPersonSearchVariable(variable);
            parentElement.appendChild(element);
        }
        parentElement.appendChild(createElement(namespace + "minimum_match", personSearcher.getThreshold() + ""));
    }

    private Element createPersonSearchVariable(PersonSearchVariable variable) {
        Element element = doc.createElement(namespace + "search_variable");
        element.appendChild(createElement(namespace + "variable_name", variable.getName()));
        element.appendChild(createElement(namespace + "weigth", variable.getWeight() + ""));
        element.appendChild(createElement(namespace + "disc_power", variable.getDiscPower() + ""));
        element.appendChild(createElement(namespace + "reliability", variable.getReliability() + ""));
        element.appendChild(createElement(namespace + "prescence", variable.getPresence() + ""));
        element.appendChild(createElement(namespace + "compare_algorithm", variable.getCompareAlgorithm().toString()));
        return element;
    }
}
