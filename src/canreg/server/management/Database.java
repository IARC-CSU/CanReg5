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

import canreg.common.Globals;
import java.sql.Connection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
