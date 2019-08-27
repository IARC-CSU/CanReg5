/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2019  International Agency for Research on Cancer
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
 * @author Patricio Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.adhoc;

import canreg.client.CanRegClientApp;
import canreg.client.gui.management.systemeditor.ModifyDatabaseStructureInternalFrame;
import canreg.client.gui.tools.WaitFrame;
import canreg.common.Globals;
import java.awt.Cursor;
import java.io.File;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Patricio
 */
public class AdHocDatabaseFrame extends javax.swing.JInternalFrame {

    private ResourceBundle resourceMap = java.util.ResourceBundle.getBundle("canreg/client/gui/adhoc/resources/AdHocDatabaseFrame");
    private ModifyDatabaseStructureInternalFrame databaseStructureFrame;
    private JDesktopPane desktopPane;
    
    public AdHocDatabaseFrame(JDesktopPane dtp) {
        initComponents();
        setTitle(resourceMap.getString("Form.title"));
        this.desktopPane = dtp;
        databaseStructureFrame = new ModifyDatabaseStructureInternalFrame(null);
        databaseStructureFrame.configureForAdHoc();
        databaseStructureFrame.setEnabled(false);
        this.jPanel3.add(databaseStructureFrame.getMainPanel());        
        
        this.pack();
        
        dbsCombo.setModel(new javax.swing.DefaultComboBoxModel<>(getAdHocDBsList()));
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chooseDbLabel = new javax.swing.JLabel();
        dbsCombo = new javax.swing.JComboBox<>();
        newDBbutton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cancelBtn = new javax.swing.JButton();
        nextBtn = new javax.swing.JButton();

        setClosable(true);
        setMinimumSize(new java.awt.Dimension(723, 503));
        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(AdHocDatabaseFrame.class);
        chooseDbLabel.setFont(resourceMap.getFont("chooseDbLabel.font")); // NOI18N
        chooseDbLabel.setText(resourceMap.getString("chooseDbLabel.text")); // NOI18N
        chooseDbLabel.setName("chooseDbLabel"); // NOI18N

        dbsCombo.setToolTipText(resourceMap.getString("dbsCombo.toolTipText")); // NOI18N
        dbsCombo.setName("dbsCombo"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(AdHocDatabaseFrame.class, this);
        newDBbutton.setAction(actionMap.get("createNewDatabaseAction")); // NOI18N
        newDBbutton.setText(resourceMap.getString("newDBbutton.text")); // NOI18N
        newDBbutton.setName("newDBbutton"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chooseDbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseDbLabel)
                    .addComponent(dbsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newDBbutton)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        jPanel2.setName("jPanel2"); // NOI18N

        cancelBtn.setAction(actionMap.get("cancelButtonAction")); // NOI18N
        cancelBtn.setText(resourceMap.getString("cancelBtn.text")); // NOI18N
        cancelBtn.setName("cancelBtn"); // NOI18N

        nextBtn.setAction(actionMap.get("nextButtonAction")); // NOI18N
        nextBtn.setText(resourceMap.getString("nextBtn.text")); // NOI18N
        nextBtn.setName("nextBtn"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cancelBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(nextBtn)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn)
                    .addComponent(nextBtn))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private String[] getAdHocDBsList() {
        File adHocSysDescFolder = new File(Globals.CANREG_SERVER_ADHOC_DB_SYSTEM_DESCRIPTION_FOLDER);
        if( ! adHocSysDescFolder.exists() || adHocSysDescFolder.listFiles().length == 0) {
            this.createNewDatabaseAction();
            return new String[]{resourceMap.getString("NO ADHOC DATABASE FOUND")};
        }
        
        String[] adhocFiles = new String[adHocSysDescFolder.listFiles().length];
        int i = 0;
        for(File file : adHocSysDescFolder.listFiles()) {
            adhocFiles[i] = file.getName();
            i++;
        }
        return adhocFiles;
    }

    @Action
    public Task nextButtonAction() {
        return new NextButtonActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
    }

    private class NextButtonActionTask extends org.jdesktop.application.Task<Object, Void> {
        
        WaitFrame waitFrame;
        private final String SUCCESS = "success";
        private final String ERROR = "error";
        
        NextButtonActionTask(org.jdesktop.application.Application app) {
            super(app);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
            waitFrame = new WaitFrame();
            waitFrame.setLabel(resourceMap.getString("LAUNCHING DB..."));
            waitFrame.setIndeterminate(true);
            desktopPane.add(waitFrame, javax.swing.JLayeredPane.POPUP_LAYER);
            waitFrame.setVisible(true);
            waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
        }
        
        @Override 
        protected Object doInBackground() {
            File adHocSysDescFolder = new File(Globals.CANREG_SERVER_ADHOC_DB_SYSTEM_DESCRIPTION_FOLDER);
            if( ! adHocSysDescFolder.exists())
                adHocSysDescFolder.mkdirs();
            
            String adhocRegCode = null;
            if(dbsCombo.getSelectedIndex() != -1 && ! dbsCombo.getSelectedItem().equals(resourceMap.getString("NO ADHOC DATABASE FOUND"))) { 
                adhocRegCode = (String) dbsCombo.getSelectedItem();
                adhocRegCode = adhocRegCode.substring(0, adhocRegCode.indexOf(".xml"));
            }
            else {
                adhocRegCode = databaseStructureFrame.getRegistryCode();
                File newAdhocFile = new File(adHocSysDescFolder + Globals.FILE_SEPARATOR + adhocRegCode + ".xml");
                if(newAdhocFile.exists()) {
                    return resourceMap.getString("THE REGISTRY CODE ") + adhocRegCode +
                                        " " + resourceMap.getString("ALREADY EXISTS");
                }
                
                try {
                  //We need to copy the original adHoc system description so we dont modify the original one located
                  //in Canreg's installation folder.
                    Files.copy(new File(Globals.ADHOC_SYSTEM_XML).toPath(), newAdhocFile.toPath());
                    databaseStructureFrame.saveXML(newAdhocFile.getAbsolutePath());
                } catch(Exception ex) {
                    Logger.getLogger(AdHocDatabaseFrame.class.getName()).log(Level.SEVERE, null, ex);
                    return ERROR;
                }
            }

            try {
                CanRegClientApp.getApplication().loginDirect(adhocRegCode, "morten", new char[]{'e', 'r', 'v', 'i', 'k'}, true);
                return SUCCESS;
            } catch(Exception ex) {
                Logger.getLogger(AdHocDatabaseFrame.class.getName()).log(Level.SEVERE, null, ex);
                return ERROR;
            }

        }
        
        @Override
        protected void succeeded(Object result) {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            waitFrame.dispose();
            String resultString = (String) result;
            
            if(resultString.startsWith(resourceMap.getString("THE REGISTRY CODE ")))              
                JOptionPane.showMessageDialog(null, resultString,
                                        resourceMap.getString("WARNING"), 
                                        JOptionPane.INFORMATION_MESSAGE);
            else if(resultString.equals(SUCCESS)) {
                AdhocWizardInternalFrame internalFrame = new AdhocWizardInternalFrame(desktopPane, CanRegClientApp.getApplication().getServer());
                desktopPane.add(internalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                internalFrame.setLocation(desktopPane.getWidth() / 2 - internalFrame.getWidth() / 2, desktopPane.getHeight() / 2 - internalFrame.getHeight() / 2);
                internalFrame.setVisible(true);
                dispose();
            }
            else
                JOptionPane.showMessageDialog(null, resourceMap.getString("ERROR CREATING ADHOC DB"), 
                                            "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Action
    public void cancelButtonAction() {
        this.dispose();
    }

    @Action
    public void createNewDatabaseAction() {
        databaseStructureFrame.setEnabled(true);
        this.newDBbutton.setEnabled(false);
        this.dbsCombo.setEnabled(false);
        this.dbsCombo.setSelectedIndex(-1);
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel chooseDbLabel;
    private javax.swing.JComboBox<String> dbsCombo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton newDBbutton;
    private javax.swing.JButton nextBtn;
    // End of variables declaration//GEN-END:variables
}
