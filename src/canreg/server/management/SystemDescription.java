package canreg.server.management;

import canreg.common.Globals;
import canreg.server.database.QueryGenerator;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author morten
 */
public class SystemDescription {

    private static boolean debug = Globals.DEBUG;
    private Document doc;
    private DOMParser parser;
    private String fileName;
    private String namespace = "ns3:";

    public SystemDescription(String fileName) {
        this.fileName = fileName;
        try {
            parser = new DOMParser();

            setSystemDescriptionXML(fileName);

            //For debuging purposes
            if (debug) {
                //canreg.server.xml.Tools.writeXmlFile(doc, "test.xml");
                //canreg.server.xml.Tools.flushXMLout(doc);
                debugOut(QueryGenerator.strCreateVariableTable("Patient", doc));
                debugOut(QueryGenerator.strCreateVariableTable("Tumour", doc));
                debugOut(QueryGenerator.strCreateDictionaryTable(doc));
                debugOut(QueryGenerator.strCreateTablesOfDictionaries(doc));
            }
        } catch (SAXException ex) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SystemDescription.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSystemDescriptionXML(String fileName) throws SAXException, IOException {
        parser.parse(new InputSource(new FileInputStream(fileName)));
        doc = parser.getDocument();
    }

    public Document getSystemDescriptionDocument() {
        return doc;
    }

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

    public void setSystemName(String systemName) {
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(namespace + "registry_name");

            if (nl != null) {
                Element element = (Element) nl.item(0);
                element.setTextContent(systemName);
            }
        }
    }

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

    public void addVariable(Variable var) {
    }

    private static void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[SystemDescription] " + msg);
        }
    }
}
