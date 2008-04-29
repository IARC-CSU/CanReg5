/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.xml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
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
 * @author morten
 */
public class Tools {

    // This only works for unique XML 
    public static String getTextContent(String[] trail, Document doc){
        if (trail.length<1) return null;
        
        Element nl = (Element) doc.getElementsByTagName(trail[0]).item(0);
        
        for (int i = 1; i<trail.length; i++){
             nl = (Element) nl.getElementsByTagName(trail[i]).item(0);
        }
        
        return nl.getTextContent();
    }

    public static void flushXMLout(Document doc) {
        NodeList list = doc.getElementsByTagName("*");
        System.out.println("XML Elements: ");
        for (int i = 0; i < list.getLength(); i++) {
            // Get element
            Element element = (Element) list.item(i);
            System.out.println(element.getNodeName() + " " + element.getTextContent());
        }
    }

    public static void writeXmlFile(Document doc, String filename) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            File file = new File(filename);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
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
                System.out.println("Cannot create url for: " + fileName);
                System.exit(0);
            }
        }
        return url;
    }
}
