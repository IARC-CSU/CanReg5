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
// import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.File;
// import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
// import org.xml.sax.InputSource;
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
    private String[] canreg4dateFormats = new String[]{"dd/mm/yyyy", "mm/dd/yyyy", "budhist", "yyyy/mm/dd"};
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

    public DatabaseVariablesListElement[] getDatabaseVariableListElements() {
        return variableListElements;
    }

    public DatabaseDictionaryListElement[] getDatabaseDictionaryListElements() {
        return dictionaryListElements;
    }

    public DatabaseGroupsListElement[] getDatabaseGroupsListElements() {
        return groupListElements;
    }

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

    public void setTextContentForExistingElement(String elementName, String content) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "elementName");
            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(content);
            }
        }
    }

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
        if (variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ALPHA_NAME) || variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME) || variable.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_ASIAN_TEXT_NAME)) {
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

    public void saveSystemDescriptionXML(String path) {
        File file = new File(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER); // Check to see it the canreg system folder exists
        if (!file.exists()) {
            file.mkdirs(); // create it if necessary
        }
        canreg.server.xml.Tools.writeXmlFile(doc, path);
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
            variablesMap.put(variable.getDatabaseVariableName().toUpperCase(), variable);
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

    public void setRegistryCode(String registryCode) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "registry_code");
            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(registryCode);
            }
        }
    }

    public void setRegionCode(int regionCode) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "region_code");
            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(regionCode + "");
            }
        }
    }

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
        parentElement.appendChild(createElement(namespace + "minimum_match", personSearcher.getThreshold()+""));
    }

    private Element createPersonSearchVariable(PersonSearchVariable variable) {
        Element element = doc.createElement(namespace + "search_variable");
        element.appendChild(createElement(namespace + "variable_name", variable.getName()));
        element.appendChild(createElement(namespace + "weigth", variable.getWeight()+""));
        element.appendChild(createElement(namespace + "compare_algorithm", variable.getCompareAlgorithm().toString()));
        return element;
    }
}
