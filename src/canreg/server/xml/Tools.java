/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

package canreg.server.xml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
// import javax.xml.transform.stream.StreamResult;
// import javax.xml.transform.Result;
// import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author ervikm
 */
public class Tools {

    // This only works for unique XML 
    /**
     * 
     * @param trail
     * @param doc
     * @return
     */
    public static String getTextContent(String[] trail, Document doc) {
        if (trail.length < 1) {
            return null;
        }

        Element nl = (Element) doc.getElementsByTagName(trail[0]).item(0);

        for (int i = 1; i < trail.length; i++) {
            nl = (Element) nl.getElementsByTagName(trail[i]).item(0);
        }

        return nl.getTextContent();
    }

    /**
     * 
     * @param doc
     */
    public static void flushXMLout(Document doc) {
        NodeList list = doc.getElementsByTagName("*");
        System.out.println("XML Elements: ");
        for (int i = 0; i < list.getLength(); i++) {
            // Get element
            Element element = (Element) list.item(i);
            System.out.println(element.getNodeName() + " " + element.getTextContent());
        }
    }

    /**
     * 
     * @param doc
     * @param filename
     */
    public static void writeXmlFile(Document doc, String filename) {
        //XMLOutputFactory xmlOutputFactory;
        TransformerFactory tfactory = TransformerFactory.newInstance();
        // TransformerHandler transformHandler;
        Transformer serializer;
        StreamResult result = null;
        File file;
        try {
            // Prepare the output file
            file = new File(filename);
            result = new StreamResult(file);

            serializer = tfactory.newTransformer();

            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            serializer.transform(new DOMSource(doc), result);

        } catch (TransformerConfigurationException e) {
            // this is fatal, just dump the stack and throw a runtime exception
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, e);
            // throw new RuntimeException(e);
        } catch (TransformerException e) {
            // this is fatal, just dump the stack and throw a runtime exception
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, e);
            // throw new RuntimeException(e);
        } catch (NullPointerException npe) {
            try {
                // this is fatal, just dump the stack and throw a runtime exception
                if (result != null && result.getWriter() != null) {
                    result.getWriter().flush();
                    result.getWriter().close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, npe);
            throw new RuntimeException(npe);
        } finally {
            try {
                if (result != null && result.getWriter() != null) {
                    result.getWriter().flush();
                    result.getWriter().close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Helper method to create a URL from a file name
    private static URL createURL(String fileName) {
        URL url = null;

        try {
            url = new URL(fileName);
        } catch (MalformedURLException ex) {
            File f = new File(fileName);

            try {
                String path = f.getAbsolutePath();

                // This code that is required to make a valid URL
                // on the Windows platform, due to inconsistencies
                // in what getAbsolutePath returns.
                String fs = System.getProperty("file.separator");

                if (fs.length() == 1) {
                    char sep = fs.charAt(0);

                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }

                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }

                path = "file://" + path;
                url = new URL(path);
            } catch (MalformedURLException e) {
                Logger.getLogger(Tools.class.getName()).log(Level.INFO, "Cannot create url for: " + fileName, e);
                System.exit(0);
            }
        }
        return url;
    }
}
