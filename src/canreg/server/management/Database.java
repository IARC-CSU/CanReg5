package canreg.server.management;

import canreg.common.Globals;
import java.sql.Connection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author morten
 */
public class Database {

    /**
     * 
     * @param doc
     * @param con
     * @return
     */
    public static boolean buildTableOfDatabases(Document doc, Connection con) {
        String queryLine = "";

        NodeList nodes = doc.getElementsByTagName(Globals.NAMESPACE + "dictionaries");
        Element dictionariesElement = (Element) nodes.item(0);

        NodeList dictionaries = dictionariesElement.getElementsByTagName(Globals.NAMESPACE + "dictionary");

        for (int i = 0; i < dictionaries.getLength(); i++) {

            // Get element
            Element element = (Element) dictionaries.item(i);

            // Add it to the database
            queryLine += ", ";
            queryLine += createDictionary(element);

        }
        return true;
    }

    private static String createDictionary(Element element) {
        String queryLine = "";

        return queryLine;
    }
}
