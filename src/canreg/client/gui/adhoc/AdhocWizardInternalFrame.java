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
import canreg.client.gui.importers.ImportFilesView;
import canreg.client.gui.management.systemeditor.ModifyDatabaseStructureInternalFrame;
import canreg.common.Globals;
import canreg.server.management.SystemDescription;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;

/**
 *
 * @author Patricio
 */
public class AdhocWizardInternalFrame extends javax.swing.JInternalFrame
                                      /*implements ActionListener*/ {

    private ResourceBundle resourceMap = java.util.ResourceBundle.getBundle("canreg/client/gui/adhoc/resources/AdhocWizardInternalFrame");
    private ModifyDatabaseStructureInternalFrame databaseStructureFrame;
    private ImportFilesView importFilesFrame;
    
    public AdhocWizardInternalFrame() {
        initComponents();
        databaseStructureFrame = new ModifyDatabaseStructureInternalFrame(null);
        databaseStructureFrame.configureForAdHoc();
        
        importFilesFrame = new ImportFilesView();
        importFilesFrame.configureForAdHoc();
        
        tabbedPane.addTab(resourceMap.getString("databaseStructure.tabTitle"), databaseStructureFrame.getMainPanel());
        tabbedPane.addTab(resourceMap.getString("importFiles.tabTitle"), importFilesFrame.getMainPanel());
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        cancelBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();
        nextBtn = new javax.swing.JButton();
        tableBuilderBtn = new javax.swing.JButton();

        setClosable(true);
        setName("Form"); // NOI18N

        tabbedPane.setName("tabbedPane"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(AdhocWizardInternalFrame.class, this);
        cancelBtn.setAction(actionMap.get("cancelButtonAction")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(AdhocWizardInternalFrame.class);
        cancelBtn.setText(resourceMap.getString("cancelBtn.text")); // NOI18N
        cancelBtn.setName("cancelBtn"); // NOI18N

        backBtn.setAction(actionMap.get("backButtonAction")); // NOI18N
        backBtn.setText(resourceMap.getString("backBtn.text")); // NOI18N
        backBtn.setEnabled(false);
        backBtn.setName("backBtn"); // NOI18N

        nextBtn.setAction(actionMap.get("nextButtonAction")); // NOI18N
        nextBtn.setText(resourceMap.getString("nextBtn.text")); // NOI18N
        nextBtn.setName("nextBtn"); // NOI18N

        tableBuilderBtn.setText(resourceMap.getString("tableBuilderBtn.text")); // NOI18N
        tableBuilderBtn.setEnabled(false);
        tableBuilderBtn.setName("tableBuilderBtn"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(cancelBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 209, Short.MAX_VALUE)
                        .addComponent(backBtn)
                        .addGap(81, 81, 81)
                        .addComponent(nextBtn)
                        .addGap(224, 224, 224)
                        .addComponent(tableBuilderBtn))
                    .addComponent(tabbedPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn)
                    .addComponent(backBtn)
                    .addComponent(nextBtn)
                    .addComponent(tableBuilderBtn))
                .addGap(0, 14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton nextBtn;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JButton tableBuilderBtn;
    // End of variables declaration//GEN-END:variables

//    @Override
    private void databaseStructureNext() {
//        if (e.getActionCommand().equals(ModifyDatabaseStructureInternalFrame.INSTALL_SYSTEM)) {
            try {
                File adHocSysDescFolder = new File(Globals.CANREG_SERVER_ADHOC_DB_SYSTEM_DESCRIPTION_FOLDER);
                if( ! adHocSysDescFolder.exists())
                    adHocSysDescFolder.mkdirs();

                String adHocRegistryCode = databaseStructureFrame.getRegistryCode();
                //Each AdHoc DB is unique by its registry code.
                File newAdhocFile = new File(adHocSysDescFolder + Globals.FILE_SEPARATOR + adHocRegistryCode + ".xml");
                if(newAdhocFile.exists()) {
                    JOptionPane.showMessageDialog(null, resourceMap.getString("THE REGISTRY CODE ") + adHocRegistryCode +
                                        " " + resourceMap.getString("ALREADY EXISTS"),
                                        resourceMap.getString("WARNING"), 
                                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                //We need to copy the original adHoc system description so we dont modify the original one located
                //in Canreg's installation folder.
                Files.copy(new File(Globals.ADHOC_SYSTEM_XML).toPath(), newAdhocFile.toPath());
                databaseStructureFrame.saveXML(newAdhocFile.getAbsolutePath());
                CanRegClientApp.getApplication().loginDirect(adHocRegistryCode, "morten", new char[]{'e', 'r', 'v', 'i', 'k'}, true);
                importFilesFrame.resetDocument();
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(null, resourceMap.getString("ERROR CREATING ADHOC DB"), 
                                            "ERROR", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(AdhocWizardInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
//        }
    }
    
    private void importFilesNext() {
        
    }

    @Action
    public void backButtonAction() {
    }

    @Action
    public void nextButtonAction() {
        int currentTabIndex = this.tabbedPane.getSelectedIndex();
        switch(currentTabIndex) {
            case 0:
                databaseStructureNext();
                break;
            case 1:
                importFilesNext();
                break;
        }
        this.tabbedPane.setSelectedIndex(currentTabIndex + 1);
    }

    @Action
    public void cancelButtonAction() {
        this.dispose();
    }
}
