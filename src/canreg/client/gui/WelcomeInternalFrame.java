/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2021  International Agency for Research on Cancer
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

/*
 * WelcomeInternalFrame.java
 *
 * Created on 02 April 2008, 16:24
 */
package canreg.client.gui;

import canreg.client.CanRegClientApp;
import canreg.client.gui.adhoc.AdHocDatabaseFrame;
import canreg.client.gui.management.InstallNewSystemInternalFrame;
import canreg.common.Globals;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;

/**
 *
 * @author  morten
 */
public class WelcomeInternalFrame extends javax.swing.JInternalFrame {

    private FrameView fv;
    private Properties appInfoProperties;
    private JDialog aboutBox;

    /** Creates new form WelcomeInternalFrame
     * @param fv 
     */
    public WelcomeInternalFrame(FrameView fv) {

        this.fv = fv;
        appInfoProperties = new Properties();
        InputStream in = null;
        try {
            //
            // load properties file
            //
            //
            // get Application information
            //
            in = getClass().getResourceAsStream(Globals.APPINFO_PROPERTIES_PATH);
            appInfoProperties.load(in);
            in.close();
            initComponents();
            // set version
            String versionString = "";
            for (String part : Globals.versionStringParts) {
                versionString += appInfoProperties.getProperty(part);
            }
            versionString += "-build" + appInfoProperties.getProperty("program.BUILDNUM");
            versionString += " (" + appInfoProperties.getProperty("program.BUILDDATE") + ")";
            versionLabel.setText(versionLabel.getText() + " " + versionString);
        } catch (IOException ex) {
            Logger.getLogger(WelcomeInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        logoButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        aboutScrollPane = new javax.swing.JScrollPane();
        aboutEditorPane = new javax.swing.JEditorPane();
        jPanel5 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        loginButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        restoreBackupButton = new javax.swing.JButton();
        adHocButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(WelcomeInternalFrame.class);
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(670, 495));
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.GridLayout(1, 2, 0, 11));

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 2));

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new java.awt.GridLayout(2, 1));

        logoButton.setIcon(resourceMap.getIcon("logoButton.icon")); // NOI18N
        logoButton.setBorder(null);
        logoButton.setBorderPainted(false);
        logoButton.setContentAreaFilled(false);
        logoButton.setFocusable(false);
        logoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logoButton.setName("logoButton"); // NOI18N
        jPanel4.add(logoButton);

        jPanel2.setName("jPanel2"); // NOI18N

        aboutScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        aboutScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        aboutScrollPane.setName("aboutScrollPane"); // NOI18N

        aboutEditorPane.setEditable(false);
        aboutEditorPane.setFont(resourceMap.getFont("aboutEditorPane.font")); // NOI18N
        aboutEditorPane.setText(resourceMap.getString("aboutEditorPane.text")); // NOI18N
        aboutEditorPane.setFocusCycleRoot(false);
        aboutEditorPane.setFocusable(false);
        aboutEditorPane.setMinimumSize(new java.awt.Dimension(50, 76));
        aboutEditorPane.setName("aboutEditorPane"); // NOI18N
        aboutEditorPane.setRequestFocusEnabled(false);
        aboutScrollPane.setViewportView(aboutEditorPane);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(aboutScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(aboutScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel2);

        jPanel1.add(jPanel4);

        mainPanel.add(jPanel1);

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(767, 400));
        jPanel5.setLayout(new java.awt.GridLayout(1, 1));

        buttonPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.GridLayout(2, 1));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(WelcomeInternalFrame.class, this);
        loginButton.setAction(actionMap.get("showLoginFrame")); // NOI18N
        loginButton.setIcon(resourceMap.getIcon("loginButton.icon")); // NOI18N
        loginButton.setText(resourceMap.getString("loginButton.text")); // NOI18N
        loginButton.setToolTipText(resourceMap.getString("loginButton.toolTipText")); // NOI18N
        loginButton.setActionCommand(resourceMap.getString("loginButton.actionCommand")); // NOI18N
        loginButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        loginButton.setName("loginButton"); // NOI18N
        loginButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonPanel.add(loginButton);

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.GridLayout());

        restoreBackupButton.setAction(actionMap.get("installNewSystemAction")); // NOI18N
        restoreBackupButton.setToolTipText(resourceMap.getString("restoreBackupButton.toolTipText")); // NOI18N
        restoreBackupButton.setActionCommand(resourceMap.getString("restoreBackupButton.actionCommand")); // NOI18N
        restoreBackupButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        restoreBackupButton.setName("restoreBackupButton"); // NOI18N
        restoreBackupButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPanel6.add(restoreBackupButton);

        adHocButton.setAction(actionMap.get("adHocAnalysisAction")); // NOI18N
        adHocButton.setIcon(resourceMap.getIcon("adHocButton.icon")); // NOI18N
        adHocButton.setText(resourceMap.getString("adHocButton.text")); // NOI18N
        adHocButton.setToolTipText(resourceMap.getString("adHocButton.toolTipText")); // NOI18N
        adHocButton.setActionCommand(resourceMap.getString("adHocButton.actionCommand")); // NOI18N
        adHocButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        adHocButton.setName("adHocButton"); // NOI18N
        adHocButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPanel6.add(adHocButton);

        buttonPanel.add(jPanel6);

        jPanel5.add(buttonPanel);

        mainPanel.add(jPanel5);

        getContentPane().add(mainPanel);

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setForeground(resourceMap.getColor("jLabel1.foreground")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        versionLabel.setText(resourceMap.getString("versionLabel.text")); // NOI18N
        versionLabel.setName("versionLabel"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(768, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(versionLabel))
                .addContainerGap())
        );

        getContentPane().add(jPanel3);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // We want to keep this window "always on top"
    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        this.toFront();
    }//GEN-LAST:event_formFocusLost

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        // TODO add your handling code here:
        if (aboutBox == null) {
            JFrame mainFrame = CanRegClientApp.getApplication().getMainFrame();
            aboutBox = new CanRegClientAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        CanRegClientApp.getApplication().show(aboutBox);
    }//GEN-LAST:event_jLabel1MouseClicked

    
    @Action
    public void showLoginFrame() {
        this.dispose();
        LoginInternalFrame loginInternalFrame = new LoginInternalFrame(fv, desktopPane);
        desktopPane.add(loginInternalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        //JFrame mainFrame = CanRegClientApp.getApplication().getMainFrame();
        loginInternalFrame.setLocation(desktopPane.getWidth() / 2 - loginInternalFrame.getWidth() / 2, desktopPane.getHeight() / 2 - loginInternalFrame.getHeight() / 2);
        loginInternalFrame.setVisible(true);
    }

    
    public void setDesktopPane(JDesktopPane dtp) {
        desktopPane = dtp;
    }

    
    @Action
    public void installNewSystemAction() {
        InstallNewSystemInternalFrame internalFrame = new InstallNewSystemInternalFrame();
        desktopPane.add(internalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        //JFrame mainFrame = CanRegClientApp.getApplication().getMainFrame();
        internalFrame.setLocation(desktopPane.getWidth() / 2 - internalFrame.getWidth() / 2, desktopPane.getHeight() / 2 - internalFrame.getHeight() / 2);
        internalFrame.setVisible(true);
        this.dispose();
    }

    @Action
    public void adHocAnalysisAction() {
        AdHocDatabaseFrame internalFrame = new AdHocDatabaseFrame(desktopPane);
        desktopPane.add(internalFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        internalFrame.setLocation(desktopPane.getWidth() / 2 - internalFrame.getWidth() / 2, desktopPane.getHeight() / 2 - internalFrame.getHeight() / 2);
        internalFrame.setVisible(true);
        this.dispose();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane aboutEditorPane;
    private javax.swing.JScrollPane aboutScrollPane;
    private javax.swing.JButton adHocButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JButton loginButton;
    private javax.swing.JButton logoButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton restoreBackupButton;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JDesktopPane desktopPane;
}
