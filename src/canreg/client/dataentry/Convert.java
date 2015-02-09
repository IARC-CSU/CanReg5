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
 * @author Hemant Dharam Dhivar, IARC-Mumbai Regional Hub, hemant_dhivar@yahoo.com
 */
package canreg.client.dataentry;

import canreg.common.Globals;
import canreg.client.CanRegClientApp;
import canreg.client.gui.dataentry.ImportView;
import canreg.server.management.SystemDescription;
import canreg.common.DatabaseVariablesListElement;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.RemoteException;

import javax.xml.parsers.*;
import javax.swing.JOptionPane;

import org.w3c.dom.*;
import org.jdesktop.application.Task;
import org.apache.commons.lang.StringEscapeUtils;
import org.paradox.ParadoxConnection;
import org.paradox.metadata.ParadoxTable;
import org.paradox.data.TableData;

import au.com.bytecode.opencsv.CSVWriter;
import org.xml.sax.SAXException;

public class Convert {
    
    static boolean debug = true;
    static FileWriter txt_fw, csv_fw;
    static BufferedWriter txt_bw, csv_bw;
    static ParadoxConnection pconn;

    public static boolean convertDictionary(Task<Object, String> task, String filepath, String dictionaryfile, String regcode) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean success = false;

        int totalNode = 0;
        int convertedNode = 0;

        String xml = Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER+Globals.FILE_SEPARATOR+regcode+".xml";
        String dic =filepath+Globals.FILE_SEPARATOR+regcode+".txt";

        File xmlfile = new File(xml);
        File dicfile = new File(dic);

        try {
            String query = "SELECT * FROM \""+dictionaryfile+"\"";
            conn = DriverManager.getConnection("jdbc:paradox:///"+filepath.replaceAll("\\\\", "/"));
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            txt_fw  = new FileWriter(dicfile);
            txt_bw = new BufferedWriter(txt_fw);

            if(xmlfile.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                // Use the factory to create a builder
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlfile);
                doc.getDocumentElement().normalize();
                // Get a list of all elements in the document
                debugOut("Migrating dictionary "+dictionaryfile);
                NodeList nlist = doc.getElementsByTagName("ns3:dictionary");
                totalNode = nlist.getLength();
                    for (int i=0; i<nlist.getLength(); i++) {
                        Node nNode = nlist.item(i);
                        if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                            convertedNode++;
                            if(task!=null) {
                                task.firePropertyChange("progress", (convertedNode-1)*100/totalNode, (convertedNode)*100/totalNode);
                            }

                            Element eElement = (Element) nNode;
                            int dicId = Integer.parseInt(eElement.getElementsByTagName("ns3:dictionary_id").item(0).getTextContent());
                            String dicName = eElement.getElementsByTagName("ns3:name").item(0).getTextContent();
                            String dicType = eElement.getElementsByTagName("ns3:type").item(0).getTextContent();
                            String dic_head = "#"+dicId+" ----"+dicName+"\n";
                            txt_bw.write(dic_head);
                            // Processing dictionary child nodes.
                            processChildNodes(task,dicId,dicType,filepath,dictionaryfile);
                        }//if node ends
                    }//for ends
                }//file if ends
            else {
                debugOut("Files not found");
            }
            txt_bw.close();
            success = true;
        }
        catch(SQLException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (NumberFormatException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (ParserConfigurationException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (DOMException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (SAXException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    public static void processChildNodes(Task<Object, String> task,int dic_id, String dic_type, String filepath, String dictionaryfile) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT * FROM \""+dictionaryfile+"\"";
            conn = DriverManager.getConnection("jdbc:paradox:///"+filepath.replaceAll("\\\\", "/"));
            stmt = conn.createStatement(rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = Integer.parseInt(rs.getString(2).substring(0, 2));
                String optn = rs.getString(2).substring(3);
                if(dic_id+1 == id) {
                    String dic_optn = optn+"\t"+rs.getString(3)+"\n";
                    txt_bw.write(dic_optn);
                }
            }
            txt_bw.write("\n");
        }
        catch(SQLException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        catch(IOException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (NumberFormatException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public static boolean convertData(Task<Object, String> task, String filepath, String datafile, String regcode) {
        Connection conn;
        Statement stmt;
        ResultSet rs_hdr;
        ResultSet rs_data;
        boolean success = false;
        int totalrowcount = 0;

        String csv = filepath+Globals.FILE_SEPARATOR+regcode+".csv";
        
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv), ',', CSVWriter.DEFAULT_ESCAPE_CHARACTER);
            debugOut("Migrating data "+datafile);
            pconn = (ParadoxConnection) DriverManager.getConnection("jdbc:paradox:///"+filepath.replaceAll("\\\\", "/"));
            final ParadoxTable table = TableData.listTables(pconn, datafile).get(0);
            totalrowcount = table.getRowCount();

            SystemDescription sd = new SystemDescription(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + ".xml");
            DatabaseVariablesListElement[] variableListElements;
            variableListElements = sd.getDatabaseVariableListElements();
            ArrayList<String> dbvle = new ArrayList();
            ArrayList<String> cols = new ArrayList();

            // Handling variables names with reservered word by replacing underscore after variable name.
            for (DatabaseVariablesListElement variable : variableListElements) {
                if (variable.getShortName().endsWith("_")) {
                    dbvle.add(variable.getShortName().replace("_", ""));
                }
                else {
                    dbvle.add(variable.getShortName());
                }
            }

            conn = DriverManager.getConnection("jdbc:paradox:///"+filepath.replaceAll("\\\\", "/"));
            final DatabaseMetaData meta = conn.getMetaData();
            rs_hdr = meta.getColumns("", "", datafile, "%");

            //Comparing variables in file and database
            while ( rs_hdr.next() ) {
                for (String dbvar : dbvle) {
                    if (rs_hdr.getString("COLUMN_NAME").equals(dbvar)) {
                        cols.add(rs_hdr.getString("COLUMN_NAME"));
                    }
                }
            }

            String[] strheader = new String[cols.size()];

            String query = "SELECT ";

            for ( int i = 0; i < cols.size(); i++ ) {
                strheader[i] = cols.get(i).toString();
                if ( i == cols.size()-1) {
                    query += strheader[i];
                }
                else {
                    query += strheader[i]+",";
                }
            }

            query += " FROM  \""+datafile+"\"";

            writer.writeNext(strheader);

            int hdrsize = strheader.length;

            String[] strdata = new String[hdrsize];

            stmt = conn.createStatement();
            rs_data = stmt.executeQuery(query);

            while (rs_data.next()) {
                for ( int i = 1; i < rs_data.getMetaData().getColumnCount()+1; i++ ) {
                    switch (rs_data.getMetaData().getColumnType(i)) {
                        case 4:
                            strdata[i-1] = Integer.toString(rs_data.getShort(i));
                        break;
                        case 12:
                            strdata[i-1] = StringEscapeUtils.escapeCsv(rs_data.getString(i));
                        break;
                    }
                }
                writer.writeNext(strdata);
            }
	    writer.close();
            success = true;
        }
        catch(SQLException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException ex) {
            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    public static Map importDictionary(Task<Object, String> task, String file) {
        BufferedReader br = null;
        Map<Integer, Map<Integer, String>> allErrors = new TreeMap();
        debugOut("Importing dictionary from "+file);
        try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName(Globals.CHARSET_ENGLISH));
                br = new BufferedReader(isr);

                String dictionaryString = new String();
                String line = br.readLine();

                int dictionaryID = -1;

                while (line != null) {
                    while (line != null && line.trim().length() == 0) {
                        // skip empty lines
                        line = br.readLine();
                      }
                    if (line == null) {
                        JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("FILE_IS_NOT_CORRECT_FORMAT:_") + "\'" + file + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        //return ("Error");
                    }
                    // first line contains the dictionary id
                    // remove leading #
                    if (line.startsWith("#")) {
                        line = line.substring(1);
                    }

                    line = line.replace(' ', '\t');
                    String[] lineElements = line.split("\t");
                    dictionaryID = Integer.parseInt(lineElements[0]);

                    // read next line;
                    line = br.readLine();

                    // read untill blank line
                    while (line != null && line.trim().length() > 0) {
                        dictionaryString += line + "\n";
                        line = br.readLine();
                    }

                    if (dictionaryString.trim().length() > 0) {
                        try {
                            Map<Integer, String> errors = canreg.client.dataentry.DictionaryHelper.testDictionary(null, dictionaryString);
                            if (errors.size() > 0) {
                                allErrors.put(dictionaryID, errors);
                                Logger.getLogger(Convert.class.getName()).log(Level.WARNING, errors.size() + " errors in dictionary: " + dictionaryID, new Exception());
                            } else {
                                canreg.client.dataentry.DictionaryHelper.replaceDictionary(dictionaryID, dictionaryString, CanRegClientApp.getApplication());
                            }
                            dictionaryString = new String();
                        } catch (RemoteException ex) {
                            Logger.getLogger(Convert.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    // Read next line
                    line = br.readLine();
                    
                    // Skip trailing blank lines
                    while (line != null && line.trim().length() == 0) {
                        line = br.readLine();
                    }
                }

                CanRegClientApp.getApplication().refreshDictionary();

        }
        catch (FileNotFoundException fileNotFoundException) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("COULD_NOT_PREVIEW_THE_FILE:_") + file + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(ImportView.class.getName()).log(Level.WARNING, null, fileNotFoundException);
        }
        catch (IOException ex) {
                Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (NumberFormatException nfe) {
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("SOMETHING_WRONG_WITH_THE_DICTIONARY:_") + "\'" + file + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportCompleteDictionaryInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(ImportView.class.getName()).log(Level.WARNING, null, nfe);
        }
        finally {
                try {
                    br.close();
                }
                catch (IOException ex) {
                    Logger.getLogger(ImportView.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return allErrors;  // return your result
    }

    /**
     * Simple console trace to system.out for debug purposes only.
     *
     * @param message the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(Convert.class.getName()).log(Level.INFO, msg);
        }
    }

}
