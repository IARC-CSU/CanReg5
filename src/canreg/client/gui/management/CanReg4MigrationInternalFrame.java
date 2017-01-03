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
 * @author Hemant Dharam Dhivar, IARC Regional Hub, Mumbai.
 * hemant_dhivar@yahoo.com
 */
package canreg.client.gui.management;

import canreg.client.gui.CanRegClientView;
import canreg.client.CanRegClientApp;
import canreg.common.Globals;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.client.LocalSettings;
import canreg.server.management.SystemDefinitionConverter;
import canreg.exceptions.WrongCanRegVersionException;
import canreg.client.dataentry.Relation;
import canreg.client.gui.components.VariableMappingPanel;
import canreg.client.dataentry.ImportOptions;

import java.io.*;
import java.io.File;
import java.util.Random;
import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.awt.Cursor;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;

import javax.swing.*;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.security.auth.login.LoginException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.apache.commons.lang.WordUtils;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

public class CanReg4MigrationInternalFrame extends javax.swing.JInternalFrame {

    public static boolean isPaused;
    private static String namespace = "ns3:";
    private JDesktopPane desktopPane;
    private static boolean debug = true;
    private String dictionary, data;
    private static String defpath, filepath;
    private String regcode;
    private JList list;
    private DefaultListModel<String> dlm = new DefaultListModel();
    private ArrayList<String> deflist = new ArrayList();
    private ArrayList<String> paths = new ArrayList();
    private ArrayList<String> registryCodes = new ArrayList();
    private SearchSystemDefTask ssdTask;
    //private MigrationTask mTask;
    private MigrationTask cTask;
    private String CR4Path = Globals.CANREG4_SYSTEM_FOLDER;
    private File inFile;
    private LocalSettings localSettings;
    private JFileChooser chooser;
    private String DEF_FILE_EXTENSION = "DEF";
    private char[] password = {'e','r','v','i','k'};
    private DatabaseVariablesListElement[] variablesInDB;
    private List<VariableMappingPanel> panelList;
    private GlobalToolBox globalToolBox;
    private boolean needToRebuildVariableMap = true;
    private Document doc;

    SystemDefinitionConverter sdc = new SystemDefinitionConverter();

    /** Creates new form CanReg4MigrationInternalFrame */
    public CanReg4MigrationInternalFrame(JDesktopPane dtp) {
        this.desktopPane = dtp;
        initComponents();
        initActions();
    }

    @Action
    private void initActions() {
        for (int i = 0; i<10; i++) {
            String fileName = CR4Path.replaceFirst("C", new String(Character.toChars(67+i)));
            if (new File(fileName).exists()) {
                CR4Path = fileName;
                break;
            }
        }
        ssdTask = new SearchSystemDefTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class), CR4Path);
        ssdTask.execute();
    }

    @Action
    public void cancelAction() throws RemoteException, IOException{
        isPaused = true;
        if (cTask != null) {
            if (JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("REALLY_CANCEL?"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("PLEASE_CONFIRM."), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                cTask.cancel(true);
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("IMPORT_OF_FILE_INTERUPTED"), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("WARNING"), JOptionPane.WARNING_MESSAGE);
                cTask = null;
                this.dispose();
            }
            else {
                isPaused = false;
            }
        } else {
            this.dispose();
        }
   }
     
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        taskOutput = new javax.swing.JTextArea();
        ProgressBar = new javax.swing.JProgressBar();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        browseCR4Button = new javax.swing.JButton();
        cr4Label = new javax.swing.JLabel();
        sysDefTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        TotalProgressBar = new javax.swing.JProgressBar();
        doneButton = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(CanReg4MigrationInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        taskOutput.setColumns(20);
        taskOutput.setFont(resourceMap.getFont("taskOutput.font")); // NOI18N
        taskOutput.setRows(5);
        taskOutput.setName("taskOutput"); // NOI18N
        jScrollPane2.setViewportView(taskOutput);

        ProgressBar.setName("ProgressBar"); // NOI18N
        ProgressBar.setStringPainted(true);

        tabbedPane.setName("tabbedPane"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Registry For Migration"));
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setFont(resourceMap.getFont("jList1.font")); // NOI18N
        jList1.setModel(dlm);
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jList1.setSelectedIndex(0);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 704, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );

        tabbedPane.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(CanReg4MigrationInternalFrame.class, this);
        browseCR4Button.setAction(actionMap.get("browseDefAction")); // NOI18N
        browseCR4Button.setText(resourceMap.getString("browseCR4Button.text")); // NOI18N
        browseCR4Button.setName("browseCR4Button"); // NOI18N

        cr4Label.setText(resourceMap.getString("cr4Label.text")); // NOI18N
        cr4Label.setName("cr4Label"); // NOI18N

        sysDefTextField.setName("sysDefTextField"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cr4Label)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(sysDefTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseCR4Button)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cr4Label, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sysDefTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseCR4Button))
                .addContainerGap(140, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        okButton.setAction(actionMap.get("MigrationAction")); // NOI18N
        okButton.setText(resourceMap.getString("okButton.text")); // NOI18N
        okButton.setToolTipText(resourceMap.getString("okButton.toolTipText")); // NOI18N
        okButton.setActionCommand(resourceMap.getString("okButton.actionCommand")); // NOI18N
        okButton.setEnabled(false);
        okButton.setName("okButton"); // NOI18N

        cancelButton.setAction(actionMap.get("cancelAction")); // NOI18N
        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setEnabled(false);
        cancelButton.setName("cancelButton"); // NOI18N

        TotalProgressBar.setName("TotalProgressBar"); // NOI18N
        TotalProgressBar.setString(resourceMap.getString("TotalProgressBar.string")); // NOI18N
        TotalProgressBar.setStringPainted(true);

        doneButton.setAction(actionMap.get("DoneAction")); // NOI18N
        doneButton.setText(resourceMap.getString("doneButton.text")); // NOI18N
        doneButton.setEnabled(false);
        doneButton.setName("doneButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(doneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(ProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(TotalProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(doneButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TotalProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
    // TODO add your handling code here:
    list = (JList) evt.getSource();
    if ( list.getModel().getSize() > 0 ) {
        if ( list.isSelectedIndex(list.getSelectedIndex()) ) {
            okButton.setEnabled(true);
        }
        regcode = registryCodes.get(list.getSelectedIndex());
        filepath = paths.get(list.getSelectedIndex());
        DBSearch dbs = new DBSearch();
        File[] dbList = dbs.getDBList(filepath);
        int dbSearch = (dbs.searchDB(dbList)) ? 1: 0;
        
        switch ( dbSearch ) {
            case 0:
                okButton.setEnabled(false);
                JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "'"+dlm.get(list.getSelectedIndex())+"' DB Files are Missing.\nPlease Confirm Location '"+CR4Path+regcode+"'", "Missing DB Files.", JOptionPane.PLAIN_MESSAGE);
                break;
            case 1:
                String dicFile = dbs.getDicDB(dbList);
                String datFile = dbs.getDatDB(dbList);

                if ( dicFile == null || datFile == null ) {
                    okButton.setEnabled(false);
                    JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "'"+dlm.get(list.getSelectedIndex())+"' DB Files are Missing.\nPlease Confirm Location '"+CR4Path+regcode+"'", "Missing DB Files.", JOptionPane.PLAIN_MESSAGE);
                }

                if ( dicFile != null  && datFile != null ) {
                    String dicname = "CR4-"+regcode+"D.DB";
                    String dataname = "CR4-"+regcode+"M.DB";

                    int dicdb = dicFile.equalsIgnoreCase(dicname) ? 1: 0;
                    int datdb = datFile.equalsIgnoreCase(dataname) ? 1: 0;

                    switch ( dicdb ) {
                      case 0:
                          okButton.setEnabled(false);
                          JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Incorrect Dictionary Name.\nExpected: "+dicname+", Found "+dicFile, "Incorrect Dictionary File Name.", JOptionPane.PLAIN_MESSAGE);

                          switch ( datdb ) {
                            case 0:
                                 okButton.setEnabled(false);
                                 JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Incorrect Data Name.\nExpected: "+dataname+", Found "+datFile, "Incorrect Data File Name.", JOptionPane.PLAIN_MESSAGE);
                                 break;
                            case 1:
                                 okButton.setEnabled(false);
                                 break;
                         }
                            break;
                       case 1:
                          switch ( datdb ) {
                            case 0:
                                okButton.setEnabled(false);
                                JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Incorrect Data Name.\nExpected: "+dataname+", Found "+datFile, "Incorect Data File Name.", JOptionPane.PLAIN_MESSAGE);

                                break;
                            case 1:
                                dictionary = dicFile;
                                data = datFile;
                                okButton.setEnabled(true);
                                break;
                        }
                        break;
                    }
                }
                break;
        }
    }
}//GEN-LAST:event_jList1MouseClicked

@Action
public void MigrationAction() {
    okButton.setEnabled(false);
    doneButton.setEnabled(false);
    cancelButton.setEnabled(true);
    jList1.setEnabled(false);
    EditDatabaseVariableTableAssociationInternalFrame edvif = new EditDatabaseVariableTableAssociationInternalFrame();
    int addServer = JOptionPane.showInternalConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4SystemConverterInternalFrame").getString("SUCCESSFULLY_CREATED_XML: ") + "\'" + Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + "\'.\n" + java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4SystemConverterInternalFrame").getString("ADD_IT_TO_FAV_SERVERS?"), "Success", JOptionPane.YES_NO_OPTION);
    if (addServer == JOptionPane.YES_OPTION) {
       localSettings = CanRegClientApp.getApplication().getLocalSettings();
       localSettings.addServerToServerList(dlm.get(list.getSelectedIndex()), "localhost", Globals.DEFAULT_PORT, regcode);
       localSettings.writeSettings();
    }
    try {
       edvif.setTitle("Variables and Tables for "+WordUtils.capitalize(dlm.get(list.getSelectedIndex()).toLowerCase()));
       edvif.loadSystemDefinition(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + ".xml");
       edvif.setDesktopPane(desktopPane);
       CanRegClientView.showAndPositionInternalFrame(desktopPane, edvif);
    }
    catch (IOException ex) {
       Logger.getLogger(CanReg4SystemConverterInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ParserConfigurationException ex) {
       Logger.getLogger(CanReg4SystemConverterInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (SAXException ex) {
       Logger.getLogger(CanReg4SystemConverterInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
    }

    edvif.saveButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //logout from canreg system before conversion
            if(CanRegClientApp.getApplication().loggedIn){
               try {
                  CanRegClientApp.getApplication().logOut();
               }
               catch(RemoteException ex) {
                  Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
               }
            }
            //check to see if there is a database already - rename it
            File databaseFolder = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + regcode);
            if (databaseFolder.exists()) {
                int i = 0;
                File folder2 = databaseFolder;
                while (folder2.exists()) {
                   i++;
                   folder2 = new File(Globals.CANREG_SERVER_DATABASE_FOLDER + Globals.FILE_SEPARATOR + regcode + i);
                }
                databaseFolder.renameTo(folder2);
                debugOut("database: "+databaseFolder);
                try {
                   canreg.common.Tools.fileCopy(Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + ".xml",
                   Globals.CANREG_SERVER_SYSTEM_CONFIG_FOLDER + Globals.FILE_SEPARATOR + regcode + i + ".xml");
                }
                catch (IOException ex) {
                   Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ProgressBar.setStringPainted(true);
            cTask = new ProgressTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
            cTask.execute();
            cTask.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) {
                            ProgressBar.setValue((Integer) evt.getNewValue());
                            ProgressBar.setString(evt.getNewValue().toString() + "%");
                        }
                    }
            });
        }
    });
}

private class ProgressTask extends MigrationTask {
    
    ProgressTask(org.jdesktop.application.Application app) {
    }

    @Override
    protected void process(List<Progress> chunks) {
            if (!isDisplayable()) {
                System.out.println("process: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            for (Progress s: chunks) {
                //System.out.println(s.component+" "+s.value);
                switch (s.component) {
                  case TOTAL:
                      TotalProgressBar.setValue((Integer) s.value);
                      TotalProgressBar.setString(s.value.toString()+"%");
                      break;
                  case FILE:  ProgressBar.setValue((Integer) s.value); break;
                  case LOG:   taskOutput.append((String) s.value);    break;
                  default: throw new AssertionError("Unknown Progress");
                }
            }
    }

    @Override
    public void done() {
        try {
            String result = get();
            cancelButton.setEnabled(false);
            doneButton.setEnabled(true);
            publish(new Progress(Component.LOG, "Migration done Successfully.\n"));
            JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Migration done Successfully.", "Migration done Successfully.", JOptionPane.PLAIN_MESSAGE);
            int deleteTemp = JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Do you want to delete temporary files ?", "Delete temporary files.", JOptionPane.YES_NO_OPTION);
            if ( deleteTemp == JOptionPane.YES_OPTION) {
                String abspath = inFile.getAbsolutePath();
                File dicFile = new File(abspath.substring(0, abspath.lastIndexOf('.'))+".txt");
                inFile.delete();
                dicFile.delete();
            }
        }
        catch (InterruptedException ex) {
            doneButton.setEnabled(true);
            cancelButton.setEnabled(false);
            Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            publish(new Progress(Component.LOG, "Migration failed with "+ex.getCause()));
            JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Migration failed.", "Migration failed.", JOptionPane.PLAIN_MESSAGE);
        }
        catch (java.util.concurrent.ExecutionException ex) {
            doneButton.setEnabled(true);
            cancelButton.setEnabled(false);
            Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            publish(new Progress(Component.LOG, "Migration failed with "+ex.getCause()));
            JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Migration failed.", "Migration failed.", JOptionPane.PLAIN_MESSAGE);
        }
        catch (java.util.concurrent.CancellationException ex) {
            Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            publish(new Progress(Component.LOG, "Migration cancelled.\n"));
        }
    }
}

enum Component { TOTAL, FILE, LOG }

class Progress {
    public final Object value;
    public final Component component;
    public Progress(Component component, Object value) {
        this.component = component;
        this.value = value;
    }
}

private class DBSearch {
    private boolean searchDBs = false;
    private String dicname, dataname;

    private File[] getDBList(String dirPath) {
        File dir = new File(dirPath);

        File[] dbList = dir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".db") || name.endsWith(".DB");
             }
        });
        return dbList;
    }

    private boolean searchDB(File[] fList) {
        for (File files : fList) {
            if ( files.getName().endsWith(".db") || files.getName().endsWith(".DB") ) {
                searchDBs = true;
            }
        }
        return searchDBs;
    }

    private String getDicDB(File[] fList) {
        for (File files : fList) {
             if ( files.getName().endsWith("db") || files.getName().endsWith("DB") ) {
                  if ( files.getName().substring(0, files.getName().lastIndexOf(".")).endsWith("D") ) {
                     dicname = files.getName();
                 }
             }
        }
        return dicname;
    }

    private String getDatDB(File[] fList) {
        for (File files : fList) {
             if ( files.getName().endsWith("db") || files.getName().endsWith("DB") ) {
                 if ( files.getName().substring(0, files.getName().lastIndexOf(".")).endsWith("M") ) {
                     dataname = files.getName();
                 }
             }
        }
        return dataname;
    }
}

public class MigrationTask extends SwingWorker<String, Progress> {
    String filename;
    Map dicimport = null;
    private final Random r = new Random();

    public boolean isIsPaused() {
       return isPaused;
    }

    @Override
    public String doInBackground() {
        int subtask = 1;
        int lengthOfTask = 5; //filelist.size();
        publish(new Progress(Component.LOG, "--------------------------------------------------------------------------\n"));

        while (subtask <= lengthOfTask && !isCancelled()) {
            try {
                migrate(subtask);
            } catch (InterruptedException ie) {
                Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ie);
                return "Interrupted with "+ie;
            }
            publish(new Progress(Component.TOTAL, 100 * subtask / lengthOfTask));
            subtask++;
        }
        return "Done";
    }

    private void migrate(int subtask) throws InterruptedException {
        boolean dic_status = false;
        boolean data_status = false;
        boolean dicimport_status = false;
        boolean dataimport_status = false;
        int current = subtask;
        int lengthOfTask = 10 + r.nextInt(50); 

        while (current <= lengthOfTask && !isCancelled()) {
            if (isPaused) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ie);
                    return;
                }
                continue;
            }

            //dictionary conversion task
            while(!dic_status && subtask == 1) {
                    publish(new Progress(Component.LOG, "Migrating dictionary...\n"));
                    debugOut("Migrating dictionary...");
                    dic_status = CanRegClientApp.getApplication().convertDictionary(cTask, filepath, dictionary, regcode);
            }

            //data conversion task
            if(subtask == 2) {
                publish(new Progress(Component.LOG, "Migrating data...\n"));
                debugOut("Migrating data...");
                data_status = CanRegClientApp.getApplication().convertData(cTask, filepath, data, regcode);
                if(!data_status) {
                    JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Migration failed. Could not read CanReg4 database.", "Migration failed.", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
            }

            //login to canreg system
            while(!CanRegClientApp.getApplication().loggedIn && subtask == 3) {
                publish(new Progress(Component.LOG, "Login to CanReg5 System.\n"));
                String canregSystem = null;
                try {
                    canregSystem = CanRegClientApp.getApplication().loginDirect(regcode, "morten", password,Globals.DEFAULT_PORT);
                    // Closing WelcomeInternalFrame
                    JDesktopPane jdp = new JDesktopPane();
                    jdp = CanRegClientApp.getApplication().getDeskTopPane();
                    JInternalFrame[] frames = jdp.getAllFrames();
                    for(JInternalFrame jf: frames) {
                        debugOut("frames name: "+jf.getName());
                        if ( (jf.getClass().getName()).equals("canreg.client.gui.WelcomeInternalFrame")) {
                           jf.dispose();
                        }
                    }
                }
                catch (LoginException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (NotBoundException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (MalformedURLException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (RemoteException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (java.net.UnknownHostException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (WrongCanRegVersionException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(canregSystem != null) {
                    JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("SUCCESSFULLY_LOGIN"), "Login", JOptionPane.INFORMATION_MESSAGE);
                    publish(new Progress(Component.LOG, "Successfully Login to \""+CanRegClientApp.getApplication().getSystemName()+"\"\n"));
                }
            }

            //import dictionary to canreg system
            while(!dicimport_status && subtask == 4) {
                publish(new Progress(Component.LOG, "Importing dictionary...\n"));
                inFile = new File(filepath+Globals.FILE_SEPARATOR+regcode+".txt");
                filename = filepath+Globals.FILE_SEPARATOR+regcode+".txt";
                dicimport = CanRegClientApp.getApplication().importDictionary(cTask, filename);
                if(dicimport.isEmpty()) {
                    dicimport_status = true;
                }
            }

            //import data to canreg system
            while(!dataimport_status && subtask == 5) {
                inFile = new File(filepath+Globals.FILE_SEPARATOR+regcode+".csv");
                doc = CanRegClientApp.getApplication().getDatabseDescription();
                variablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE);
                globalToolBox = new GlobalToolBox(doc);
                initializeVariableMapping();
                
                try {
                    dataimport_status = CanRegClientApp.getApplication().importCRFile(cTask, doc, buildMap(), inFile, buildImportOptions());
                }
                catch(java.sql.SQLException ex) {
                   Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch(canreg.server.database.RecordLockedException ex) {
                   Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch(RemoteException ex) {
                   Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            int iv = 100 * current / lengthOfTask;
            Thread.sleep(20); 
            publish(new Progress(Component.FILE, iv + 1));
            current++;
        }
        if(dic_status) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("SUCCESSFULLY_MIGRATED_FILE") + " Dictionary to " + filepath +Globals.FILE_SEPARATOR+regcode+ ".txt", java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("DIC_EXPORT"), JOptionPane.INFORMATION_MESSAGE);
            publish(new Progress(Component.LOG, "Successfully converted dictionary.\n"));
        }
        if(data_status) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("SUCCESSFULLY_MIGRATED_FILE") + " Data to " + filepath +Globals.FILE_SEPARATOR+regcode+ ".csv", java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("DATA_EXPORT"), JOptionPane.INFORMATION_MESSAGE);
            publish(new Progress(Component.LOG, "Successfully converted data.\n"));
        }
        if(dicimport_status) {
            publish(new Progress(Component.LOG, "Successfully imported dictionary.\n"));
            debugOut("Successfully imported dictionary.");
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("SUCCESSFULLY_IMPORTED_FILE") + " Dictionary from " + inFile.getAbsolutePath() + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("DIC_IMPORT"), JOptionPane.INFORMATION_MESSAGE);
            publish(new Progress(Component.LOG, "Importing data...\n"));
        }
        if(dataimport_status) {
            publish(new Progress(Component.LOG, "Successfully imported data.\n"));
            debugOut("Successfully imported data.");
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("SUCCESSFULLY_IMPORTED_FILE") + " Data from " + inFile.getAbsolutePath() + ".", java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4MigrationInternalFrame").getString("DATA_IMPORT"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
  }

private boolean searchSysDef(String canreg4Path) {
    boolean defsearch = false;
    File canreg4=new File(canreg4Path);
    boolean exists = canreg4.exists();

   if(exists) {
      File folder = new File(canreg4Path);
      File[] listOfFiles = folder.listFiles();
      for (int i = 0; i < listOfFiles.length; i++) {
         if (listOfFiles[i].isDirectory()) {
             File directory = listOfFiles[i];
             File[] fList = directory.listFiles();
             for (File file : fList){
                  String filename = file.getName();
                  if (filename.endsWith(".def") || filename.endsWith(".DEF")) {
                      defsearch = true;
                  }
             }
         }
      }
   }
   return defsearch;
}

private class SearchSystemDefTask extends org.jdesktop.application.Task<Object, String> {
    private final String canreg4Path;

    SearchSystemDefTask(org.jdesktop.application.Application app, final String canreg4Path) {
        super(app);
        this.canreg4Path = canreg4Path;
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        setCursor(hourglassCursor);        
    }
    @Override
    public Object doInBackground() {
        boolean searchdef = searchSysDef(canreg4Path);
        boolean status = false;
	File canreg4=new File(canreg4Path);
	boolean cr4exists = canreg4.exists();

        if(cr4exists && searchdef) {
            publish("Loading and migrating system definition file from default location.\n");
            File folder = new File(canreg4Path);
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isDirectory()) {
                    File directory = listOfFiles[i];
                    File[] fList = directory.listFiles();
                    for (File file : fList){
                         String filename = file.getName();
                         if (filename.endsWith(".def") || filename.endsWith(".DEF")) {
                            defpath = CR4Path+listOfFiles[i].getName()+Globals.FILE_SEPARATOR+filename;
                            debugOut("loading system definition file from default location. "+defpath);
                            try {
                                sdc.setFileEncoding(Charset.forName(Globals.CHARSET_ENGLISH));
                                sdc.convertAndSaveInSystemFolder(defpath);
                                publish("\""+WordUtils.capitalize(sdc.getRegistryName().toLowerCase())+"\"\n");
                            }
                            catch (IOException ex) {
                                    Logger.getLogger(CanReg4SystemConverterInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            registryCodes.add(sdc.getServerCode());
                            deflist.add(defpath);
                            paths.add(CR4Path+listOfFiles[i].getName());
                            dlm.addElement(sdc.getRegistryName());
                         }
                    }
                }
            }
            status = true;
        }
        else {
            publish("CanReg4 system definition file not found at default location.\nPlease click \"Browse\" to locate system definition file.\n");
            debugOut("System definition file not found at default location.");
            tabbedPane.setSelectedIndex(1);
            browseCR4Button.setEnabled(true);
            status = false;
        }
        return status;
    }
    @Override
    protected void process(final List<String> chunks) {
        // Updates the messages text area
        for (final String string : chunks) {
            taskOutput.append(string);
        }
    }
    @Override
    protected void succeeded(Object defstatus) {
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        setCursor(normalCursor);
        try {
            if ( defstatus.equals(true) ) {
                taskOutput.append("Migration of system definition done successfully.\n");
                taskOutput.append("Select registry and then click 'Ok' to start migration.\n");
                //regSelectButton.setEnabled(true);
            }
        }
        catch(Exception ex) {
            Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        ssdTask = null;
    }
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JProgressBar ProgressBar;
    private javax.swing.JProgressBar TotalProgressBar;
    private javax.swing.JButton browseCR4Button;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel cr4Label;
    private javax.swing.JButton doneButton;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField sysDefTextField;
    private javax.swing.JTabbedPane tabbedPane;
    public javax.swing.JTextArea taskOutput;
    // End of variables declaration//GEN-END:variables

    @Action
    //public Task browseDefAction() {
    public void browseDefAction() {
        String defname = null;
        chooser = new JFileChooser();
        // Filter only the DEF-files.
        FileNameExtensionFilter filter = new FileNameExtensionFilter(java.util.ResourceBundle.getBundle("canreg/client/gui/management/resources/CanReg4SystemConverterInternalFrame").getString("CANREG4 SYSTEM DEFINITION FILE"), DEF_FILE_EXTENSION);
        chooser.addChoosableFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                //set the file name
                sysDefTextField.setText(chooser.getSelectedFile().getCanonicalPath());
                defname = sysDefTextField.getText();
            } catch (IOException ex) {
                Logger.getLogger(CanReg4SystemConverterInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        BrowseDefActionTask bTask = new BrowseDefActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class), defname);
        bTask.execute();
        //return new BrowseDefActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class), defname);
    }

    private class BrowseDefActionTask extends org.jdesktop.application.Task<Object, String> {

        private String defname;
        BrowseDefActionTask(org.jdesktop.application.Application app, String defname) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to BrowseDefActionTask fields, here.
            super(app);
            this.defname = defname;
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
        }
        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            boolean status = false;
            try {
                if(defname!=null) {
                    publish("Migrating System Definition File for ");
                    sdc.setFileEncoding(Charset.forName(Globals.CHARSET_ENGLISH));
                    sdc.convertAndSaveInSystemFolder(defname);
                    publish("\""+WordUtils.capitalize(sdc.getRegistryName().toLowerCase())+"\".\n");
                    registryCodes.add(sdc.getServerCode());
                    //deflist.add(defname);
                    File f = new File(defname);
                    String abpath = f.getAbsolutePath();
                    String defpath = abpath.substring(0,abpath.lastIndexOf(File.separator));
                    debugOut("Loading system definition file manually from path "+defpath);
                    paths.add(defpath);
                    dlm.addElement(sdc.getRegistryName());
                    status = true;
                }
            }
            catch (FileNotFoundException ex) {
               Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            return status;  // return your result
        }
        protected void process(final List<String> chunks) {
            // Updates the messages text area
            for (final String string : chunks) {
                taskOutput.append(string);
            }
        }
        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            try {
                if( result.equals(true) ) {
                    taskOutput.append("Migration of System Definition Done Successfully.\n");
                    taskOutput.append("Select Registry and then click 'Ok' for Migration.\n");
                    JOptionPane.showConfirmDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Migration of System Definition Done Successfully.", "Migration of System Definition", JOptionPane.PLAIN_MESSAGE);
                    tabbedPane.setSelectedIndex(0);
                    //regSelectButton.setEnabled(true);
                }
            }
            catch(Exception ex) {
                Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            ssdTask = null;
        }
    }

    private List<Relation> buildMap() {
        List<Relation> map = new LinkedList();
        int i = 0;
        String dbvar;
        for (VariableMappingPanel vmp : panelList) {
            Relation rel = new Relation();

            DatabaseVariablesListElement dbVLE = vmp.getSelectedDBVariableObject();

            if (dbVLE != null) {
                rel.setDatabaseTableName(dbVLE.getDatabaseTableName());
                rel.setDatabaseTableVariableID(vmp.getDBVariableIndex());
                rel.setDatabaseVariableName(dbVLE.getDatabaseVariableName());
                rel.setFileColumnNumber(i);
                rel.setFileVariableName(vmp.getFileVariableName());
                rel.setVariableType(dbVLE.getVariableType());

                map.add(rel);
            }
            i++;
        }
        return map;
    }

    private ImportOptions buildImportOptions() {
        ImportOptions io = new ImportOptions();

        // Discrepencies
        io.setDiscrepancies(ImportOptions.UPDATE);

        //io.setTestOnly(true);

        //Max lines
        io.setMaxLines(-1);

        // separator
        char seperator = ',';
        io.setSeparator(seperator);

        // CanReg data
        io.setDataFromPreviousCanReg(true);

        // Set standard variable names
        io.setMultiplePrimaryVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimCode.toString()).getDatabaseVariableName());
        io.setPatientIDTumourTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName());
        io.setPatientIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
        io.setTumourUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUpdateDate.toString()).getDatabaseVariableName());
        io.setPatientUpdateDateVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientUpdateDate.toString()).getDatabaseVariableName());
        io.setTumourIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
        io.setPatientRecordIDVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
        io.setSourceIDVariablename(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.SourceRecordID.toString()).getDatabaseVariableName());
        io.setPatientRecordIDTumourTableVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName());
        io.setObsoletePatientFlagVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString()).getDatabaseVariableName());
        io.setObsoleteTumourFlagVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString()).getDatabaseVariableName());
        io.setTumourSequenceVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimSeq.toString()).getDatabaseVariableName());
        io.setFirstNameVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.FirstName.toString()).getDatabaseVariableName());
        io.setSexVariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Sex.toString()).getDatabaseVariableName());
        io.setTumourCheckStatus(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.CheckStatus.toString()).getDatabaseVariableName());
        io.setTumourRecordStatus(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString()).getDatabaseVariableName());
        io.setICD10VariableName(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICD10.toString()).getDatabaseVariableName());

        // Set the characterset
        //Charset fileCharset;
        io.setFileCharset((Charset.forName(Globals.CHARSET_ENGLISH)));

        return io;
    }


    private char getSeparator() {
        char schar = ','; // Default
        return schar;
    }

    private void initializeVariableMapping() {
        if (needToRebuildVariableMap) {
            debugOut("Initialized variable mapping.");
            BufferedReader br = null;
            List<Relation> map = null;
            panelList = new LinkedList();
            try {
                // Remove all variable mappings
                //variablesPanel.removeAll();

                // Read the first line of the file
                br = new BufferedReader(new FileReader(inFile));
                String line = br.readLine();
//                String[] lineElements = canreg.common.Tools.breakDownLine('\t', line);
                String[] lineElements = canreg.common.Tools.breakDownLine(getSeparator(), line);
                // Build variable mapping
                // map = Import.constructRelations(doc, lineElements);
                map = constructRelations(doc, lineElements);

                // Add the panels
                for (Relation rel : map) {
                    VariableMappingPanel vmp = new VariableMappingPanel();
                    panelList.add(vmp);
                    vmp.setDBVariables(variablesInDB);
                    vmp.setFileVariableName(rel.getFileVariableName());
                    vmp.setSelectedDBIndex(rel.getDatabaseTableVariableID());
                    //variablesPanel.add(vmp);
                    //vmp.setVisible(true);
                }

                //variablesPanel.revalidate();
                //variablesPanel.repaint();

            } catch (RemoteException ex) {
                Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                // JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("COULD_NOT_OPEN_FILE:_") + "\'" + fileNameTextField.getText().trim() + "\'.", java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry/resources/ImportView").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                needToRebuildVariableMap = false;
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    public static List<Relation> constructRelations(Document doc, String[] lineElements) {
        LinkedList<Relation> list = new LinkedList();
        NodeList nl = doc.getElementsByTagName(namespace + "variable");
        String[] variableNames = canreg.common.Tools.getVariableNames(doc, namespace);

        //Handling variable names with reserved name
        String[] dbVar = new String[variableNames.length];
        for ( int i = 0; i < variableNames.length; i++ ) {
            if ( variableNames[i].endsWith("_") ) {
                dbVar[i] = variableNames[i].replace("_", "");
            }
            else {
                dbVar[i] = variableNames[i];
            }
        }

        for (int i = 0; i < lineElements.length; i++) {
            boolean found = false;
            int j = 0;
            while (!found && j < variableNames.length) {
                //found = lineElements[i].equalsIgnoreCase(variableNames[j++]);
                found = lineElements[i].equalsIgnoreCase(dbVar[j++]);
            }

            //build relation
            Relation rel = new Relation();
            rel.setFileVariableName(lineElements[i]);
            rel.setFileColumnNumber(i);
            if (found) {
                //backtrack
                j--;
                Element e = (Element) nl.item(j);
                rel.setDatabaseTableName(e.getElementsByTagName(namespace + "table").item(0).getTextContent());
                rel.setDatabaseTableVariableID(Integer.parseInt(e.getElementsByTagName(namespace + "variable_id").item(0).getTextContent()));
                rel.setVariableType(e.getElementsByTagName(namespace + "variable_type").item(0).getTextContent());
                rel.setDatabaseVariableName(variableNames[j]);
            } else {
                rel.setDatabaseTableName("");
                rel.setDatabaseTableVariableID(-1);
                rel.setVariableType("");
                rel.setDatabaseVariableName("");
            }
            list.add(rel);
        }
        return list;
    }

    /**
     * Simple console trace to system.out for debug purposes only.
     *
     * @param message the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            Logger.getLogger(CanReg4MigrationInternalFrame.class.getName()).log(Level.INFO, msg);
        }
    }
    
    @Action
    public void DoneAction() {
        this.dispose();
    }
}
