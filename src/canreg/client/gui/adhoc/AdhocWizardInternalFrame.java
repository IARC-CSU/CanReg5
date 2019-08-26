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
import canreg.client.gui.analysis.TableBuilderInternalFrame;
import canreg.client.gui.dataentry.BrowseInternalFrame;
import canreg.client.gui.dataentry.PDSChooserInternalFrame;
import canreg.client.gui.importers.ImportFilesView;
import canreg.client.gui.management.systemeditor.ModifyDatabaseStructureInternalFrame;
import canreg.common.Globals;
import canreg.server.CanRegServerInterface;
import canreg.server.management.SystemDescription;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JDesktopPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.jdesktop.application.Action;

/**
 *
 * @author Patricio
 */
public class AdhocWizardInternalFrame extends javax.swing.JInternalFrame
                                      /*implements ActionListener*/ {

    private ResourceBundle resourceMap = java.util.ResourceBundle.getBundle("canreg/client/gui/adhoc/resources/AdhocWizardInternalFrame");
    private ImportFilesView importFilesFrame;
    private BrowseInternalFrame browseFrame;
    private JDesktopPane dtp;
    private PDSChooserInternalFrame populationFrame;
    private boolean changeTabFlag = false;
    private CanRegServerInterface server;
    
    
    public AdhocWizardInternalFrame(JDesktopPane dtp, CanRegServerInterface server) {
        this.dtp = dtp;
        this.server = server;
        initComponents();
        setTitle(resourceMap.getString("Form.title"));
        
        importFilesFrame = new ImportFilesView();
        importFilesFrame.configureForAdHoc();
        
        browseFrame = new BrowseInternalFrame(dtp, this.server);
        
        initPopulationTab();
        
        tabbedPane.addTab(resourceMap.getString("importFiles.tabTitle"), importFilesFrame.getMainPanel());
        //Don't mind this flag, the tabbedPane is a crappy component.
        changeTabFlag = true;
        tabbedPane.addTab(resourceMap.getString("browse.tabTitle"), browseFrame.getMainPanel());
        tabbedPane.addTab(resourceMap.getString("population.tabTitle"), populationFrame.getMainPanel());
    }

    private void initPopulationTab() {
        try {
            populationFrame = new PDSChooserInternalFrame(this.dtp);
            populationFrame.configureForAdHoc(this);
            if(populationFrame.getJList().getModel().getElementAt(0) != null)
                tableBuilderBtn.setEnabled(true);
        } catch(Exception ex) {
            Logger.getLogger(AdhocWizardInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        cancelBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();
        nextBtn = new javax.swing.JButton();
        tableBuilderBtn = new javax.swing.JButton();

        setClosable(true);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(AdhocWizardInternalFrame.class);
        tabbedPane.setFont(resourceMap.getFont("tabbedPane.font")); // NOI18N
        tabbedPane.setName("tabbedPane"); // NOI18N
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(AdhocWizardInternalFrame.class, this);
        cancelBtn.setAction(actionMap.get("cancelButtonAction")); // NOI18N
        cancelBtn.setText(resourceMap.getString("cancelBtn.text")); // NOI18N
        cancelBtn.setName("cancelBtn"); // NOI18N

        backBtn.setAction(actionMap.get("backButtonAction")); // NOI18N
        backBtn.setText(resourceMap.getString("backBtn.text")); // NOI18N
        backBtn.setEnabled(false);
        backBtn.setName("backBtn"); // NOI18N

        nextBtn.setAction(actionMap.get("nextButtonAction")); // NOI18N
        nextBtn.setText(resourceMap.getString("nextBtn.text")); // NOI18N
        nextBtn.setName("nextBtn"); // NOI18N

        tableBuilderBtn.setAction(actionMap.get("tableBuilderButtonAction")); // NOI18N
        tableBuilderBtn.setText(resourceMap.getString("tableBuilderBtn.text")); // NOI18N
        tableBuilderBtn.setEnabled(false);
        tableBuilderBtn.setName("tableBuilderBtn"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cancelBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204, Short.MAX_VALUE)
                .addComponent(backBtn)
                .addGap(81, 81, 81)
                .addComponent(nextBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 225, Short.MAX_VALUE)
                .addComponent(tableBuilderBtn)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn)
                    .addComponent(backBtn)
                    .addComponent(nextBtn)
                    .addComponent(tableBuilderBtn))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 870, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        if( ! changeTabFlag) 
            return;
        
        if(this.tabbedPane.getSelectedIndex() != 0)
            this.backBtn.setEnabled(true);
        else
            this.backBtn.setEnabled(false);
        
        if(this.tabbedPane.getSelectedIndex() == (this.tabbedPane.getTabCount() - 1))
            this.nextBtn.setEnabled(false);
        else
            this.nextBtn.setEnabled(true);
    }//GEN-LAST:event_tabbedPaneStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton nextBtn;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JButton tableBuilderBtn;
    // End of variables declaration//GEN-END:variables


    @Action
    public void backButtonAction() {
        this.tabbedPane.setSelectedIndex(this.tabbedPane.getSelectedIndex() - 1);
        if(this.tabbedPane.getSelectedIndex() == 0)
            this.backBtn.setEnabled(false);
    }

    @Action
    public void nextButtonAction() {
        this.tabbedPane.setSelectedIndex(this.tabbedPane.getSelectedIndex() + 1);
        this.backBtn.setEnabled(true);
    }

    @Action
    public void cancelButtonAction() {
        this.dispose();
    }
    
    
    @Override
    public void dispose() {
        try {
            //Logout sets the server reference to null, that's why we first must do the shutdown
            CanRegClientApp.getApplication().getServer().shutDownServer();
            CanRegClientApp.getApplication().logOut();
        } catch(Exception ex) {
            Logger.getLogger(AdhocWizardInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        super.dispose();
    }

    public void notifyPopulationListChanged() {
        if(populationFrame.getJList().getModel().getElementAt(0) != null)
            this.tableBuilderBtn.setEnabled(true);
        else
            this.tableBuilderBtn.setEnabled(false);
    }

    @Action
    public void tableBuilderButtonAction() {
        TableBuilderInternalFrame internalFrame = new TableBuilderInternalFrame();
        dtp.add(internalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        internalFrame.setLocation(dtp.getWidth() / 2 - internalFrame.getWidth() / 2, dtp.getHeight() / 2 - internalFrame.getHeight() / 2);
        internalFrame.setVisible(true);
    }
}
