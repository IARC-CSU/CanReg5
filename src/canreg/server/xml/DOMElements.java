/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * 
 * @author ervikm
 */
public class DOMElements{
    /**
     * 
     * @param arg
     */
    static public void main(String[] arg){
     try {
       BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
       System.out.print("Enter XML File name: ");
       String xmlFile = bf.readLine();
       File file = new File(xmlFile);
       if(file.exists()){
         // Create a factory
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         // Use the factory to create a builder
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(xmlFile);
         // Get a list of all elements in the document
         NodeList list = doc.getElementsByTagName("*");
         System.out.println("XML Elements: ");
         for (int i=0; i<list.getLength(); i++) {
           // Get element
           Element element = (Element)list.item(i);
           System.out.println(element.getNodeName()+" "+element.getTextContent());
         }
       }
       else{
         System.out.print("File not found!");
       }
     }
     catch (Exception e) {
       System.exit(1);
     }
   }
}
