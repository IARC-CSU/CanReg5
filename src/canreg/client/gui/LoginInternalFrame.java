/*
 * LoginInternalFrame.java
 *
 * Created on 02 April 2008, 16:36
 */
package canreg.client.gui;

import canreg.client.CanRegClientApp;
import canreg.client.LocalSettings;
import canreg.client.ServerDescription;
import java.beans.PropertyChangeSupport;
import java.rmi.AlreadyBoundException;
import java.util.Properties;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;

/**
 *
 * @author  morten
 */
public class LoginInternalFrame extends javax.swing.JInternalFrame {

    private static boolean debug = true;
    private javax.swing.JLabel externalFeedbackLabel;
    private FrameView fv;
    private JDesktopPane desktopPane;
    private LocalSettings localSettings;
    protected final PropertyChangeSupport propertyChangeSupport;

    /** Creates new form LoginInternalFrame */
    public LoginInternalFrame(FrameView fv) {
        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        this.fv = fv;
        propertyChangeSupport = new PropertyChangeSupport(this);
        initComponents();
        loadDefaultValues();
    }

    public void setDesktopPane(JDesktopPane dtp) {
        desktopPane = dtp;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loginButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        feedbackLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        canRegSystemComboBox = new javax.swing.JComboBox();
        usernameLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        rememberPasswordCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        serverURLLabel = new javax.swing.JLabel();
        serverURLTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        launchServerButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        codeField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(LoginInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(LoginInternalFrame.class, this);
        loginButton.setAction(actionMap.get("loginAction")); // NOI18N
        loginButton.setText(resourceMap.getString("loginButton.text")); // NOI18N
        loginButton.setName("loginButton"); // NOI18N

        cancelButton.setAction(actionMap.get("cancelAction")); // NOI18N
        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        feedbackLabel.setText(resourceMap.getString("feedbackLabel.text")); // NOI18N
        feedbackLabel.setName("feedbackLabel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        canRegSystemComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        canRegSystemComboBox.setAction(actionMap.get("serverComboboxChanged")); // NOI18N
        canRegSystemComboBox.setName("canRegSystemComboBox"); // NOI18N

        usernameLabel.setText(resourceMap.getString("usernameLabel.text")); // NOI18N
        usernameLabel.setName("usernameLabel"); // NOI18N

        usernameTextField.setText(resourceMap.getString("usernameTextField.text")); // NOI18N
        usernameTextField.setName("usernameTextField"); // NOI18N
        usernameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                usernameTextFieldKeyTyped(evt);
            }
        });

        passwordLabel.setText(resourceMap.getString("passwordLabel.text")); // NOI18N
        passwordLabel.setName("passwordLabel"); // NOI18N

        passwordField.setText(resourceMap.getString("passwordField.text")); // NOI18N
        passwordField.setName("passwordField"); // NOI18N
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                passwordFieldKeyTyped(evt);
            }
        });

        rememberPasswordCheckBox.setText(resourceMap.getString("rememberPasswordCheckBox.text")); // NOI18N
        rememberPasswordCheckBox.setToolTipText(resourceMap.getString("rememberPasswordCheckBox.toolTipText")); // NOI18N
        rememberPasswordCheckBox.setName("rememberPasswordCheckBox"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usernameLabel)
                    .addComponent(passwordLabel)
                    .addComponent(jLabel1))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(canRegSystemComboBox, 0, 415, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rememberPasswordCheckBox))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(usernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(canRegSystemComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(rememberPasswordCheckBox)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        serverURLLabel.setText(resourceMap.getString("serverURLLabel.text")); // NOI18N
        serverURLLabel.setName("serverURLLabel"); // NOI18N

        serverURLTextField.setText(resourceMap.getString("serverURLTextField.text")); // NOI18N
        serverURLTextField.setName("serverURLTextField"); // NOI18N
        serverURLTextField.setNextFocusableComponent(portField);

        portLabel.setText(resourceMap.getString("portLabel.text")); // NOI18N
        portLabel.setName("portLabel"); // NOI18N

        portField.setText(resourceMap.getString("portField.text")); // NOI18N
        portField.setName("portField"); // NOI18N

        launchServerButton.setAction(actionMap.get("launchCanRegServerAction")); // NOI18N
        launchServerButton.setText(resourceMap.getString("launchServerButton.text")); // NOI18N
        launchServerButton.setName("launchServerButton"); // NOI18N

        jButton1.setAction(actionMap.get("addServerToList")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        nameLabel.setText(resourceMap.getString("nameLabel.text")); // NOI18N
        nameLabel.setName("nameLabel"); // NOI18N

        nameTextField.setEditable(false);
        nameTextField.setText(resourceMap.getString("nameTextField.text")); // NOI18N
        nameTextField.setName("nameTextField"); // NOI18N

        codeField.setText(resourceMap.getString("codeField.text")); // NOI18N
        codeField.setName("codeField"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jButton2.setAction(actionMap.get("testServerConnection")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serverURLLabel)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(launchServerButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(nameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                            .addComponent(serverURLTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(portLabel)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(codeField, 0, 0, Short.MAX_VALUE)
                            .addComponent(portField, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverURLLabel)
                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel)
                    .addComponent(serverURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(launchServerButton)
                    .addComponent(jButton2))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(feedbackLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addGap(109, 109, 109)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loginButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loginButton)
                    .addComponent(cancelButton)
                    .addComponent(feedbackLabel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void loadDefaultValues() {
        Properties p = localSettings.getProperties();
        String rememberPasswordBooleanString = p.getProperty("remember_password");
        boolean rememberPassword = rememberPasswordBooleanString.equalsIgnoreCase("true");
        rememberPasswordCheckBox.setSelected(rememberPassword);
        if (rememberPassword) {
            passwordField.setText(p.getProperty("password"));
        }
        usernameTextField.setText(p.getProperty("username"));
        // Load the server list
        String[] serverNames = localSettings.getServerNames();
        if (serverNames != null) {
            canRegSystemComboBox.setModel(new javax.swing.DefaultComboBoxModel(localSettings.getServerDescriptions().toArray()));
            String lastServerIDString = p.getProperty("last_server_id");
            if (lastServerIDString != null) {
                ServerDescription sd = localSettings.getServerDescription(Integer.parseInt(lastServerIDString));
                portField.setText("" + sd.getPort());
                serverURLTextField.setText(sd.getUrl());
                codeField.setText(sd.getCode());
            }
        } else {
            canRegSystemComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{""}));
        }
    }

    private void saveDefaultValues() {
        Properties p = localSettings.getProperties();
        if (rememberPasswordCheckBox.isSelected()) {
            p.setProperty("remember_password", "true");
            p.setProperty("password", new String(passwordField.getPassword()));
        } else {
            p.setProperty("remember_password", "false");
            p.setProperty("password", "");
        }
        p.setProperty("username", usernameTextField.getText());
        ServerDescription sd = localSettings.getServerDescriptions().get(canRegSystemComboBox.getSelectedIndex());
        p.setProperty("last_server_id", sd.getId() + "");
        localSettings.writeSettings();
    }
 
    private void passwordFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyTyped
        // TODO add your handling code here:
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            loginAction();
        }
    }//GEN-LAST:event_passwordFieldKeyTyped

    private void usernameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameTextFieldKeyTyped
    // TODO add your handling code here:
    }//GEN-LAST:event_usernameTextFieldKeyTyped

    @Action
    public void loginAction() {
        String server = serverURLTextField.getText().trim();
        String port = portField.getText().trim();
        String code = codeField.getText().trim();
        String username = usernameTextField.getText().trim();
        char[] password = passwordField.getPassword();
        String serverObjectString = "rmi://" + server + ":" + port + "/CanRegLogin" + code;

        try {
            String canRegSystemName = CanRegClientApp.getApplication().login(serverObjectString, username, password);
            if (canRegSystemName != null) {
                fv.getFrame().setTitle("CanReg5 - " + canRegSystemName);
                saveDefaultValues();
                this.dispose();
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Successfully logged in to " + canRegSystemName + " as " + username + ".", "Logged in", JOptionPane.INFORMATION_MESSAGE);
            } else {
                feedbackLabel.setText("Error.");
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Could not log in to the CanReg server on " + server + " with the given credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception exception) {
//            System.out.println(exception.getLocalizedMessage());
            feedbackLabel.setText(exception.getLocalizedMessage());
        }
    }

    @Action
    public void cancelAction() {
        this.dispose();
    }

    public void setFeedbackLabel(javax.swing.JLabel label) {
        this.externalFeedbackLabel = label;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox canRegSystemComboBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField codeField;
    private javax.swing.JLabel feedbackLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton launchServerButton;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JCheckBox rememberPasswordCheckBox;
    private javax.swing.JLabel serverURLLabel;
    private javax.swing.JTextField serverURLTextField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables
    /**
     * Simple console trace to system.out for debug purposes only.&Ltp>
     *
     * @param msg the message to be printed to the console
     */
    private static void debugOut(String msg) {
        if (debug) {
            System.out.println("\t[LoginInternalFrame] " + msg);
        }
    }

    @Action
    public Task launchCanRegServerAction() {
        return new LaunchCanRegServerActionTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
    }

    private class LaunchCanRegServerActionTask extends org.jdesktop.application.Task<Object, Void> {

        WaitFrame waitFrame;

        LaunchCanRegServerActionTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to LaunchCanRegServerActionTask fields, here.
            super(app);
            // launchServerButton.setEnabled(false);
            feedbackLabel.setText("Launching server...");
            waitFrame = new WaitFrame();
            waitFrame.setLabel("Launching server...");
            waitFrame.setIndeterminate(true);
            desktopPane.add(waitFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
            waitFrame.setVisible(true);
            waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here...
            String result = "stopped";
            try {
                if (canreg.common.ServerLauncher.start(codeField.getText(), Integer.parseInt(portField.getText()))) {
                    result = "started";
                }
            }
            catch (AlreadyBoundException ex) {
                result = "running";
            // Logger.getLogger(LoginInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Return your result... 
            return result;
        }

        @Override
        protected void succeeded(Object resultObject) {
            waitFrame.dispose();
            String resultString = (String) resultObject;
            if (resultString.equalsIgnoreCase("running")) {
                feedbackLabel.setText("Server already running.");
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Server already running.", "Message", JOptionPane.INFORMATION_MESSAGE);
            } else if (resultString.equalsIgnoreCase("stopped")) {
                feedbackLabel.setText("Server failed to start.");
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Server failed to start.", "Message", JOptionPane.ERROR_MESSAGE);
            } else if (resultString != null) {
                feedbackLabel.setText("Server started.");
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Server started.", "Message", JOptionPane.INFORMATION_MESSAGE);
            } else {
                feedbackLabel.setText("Server failed to start.");
                JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Server failed to start.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            launchServerButton.setEnabled(false);
        }
    }

    @Action
    public boolean testServerConnection() {
        String server = serverURLTextField.getText().trim();
        String port = portField.getText().trim();
        String code = codeField.getText().trim();
        
        String serverObjectString = "rmi://" + server + ":" + port + "/CanRegLogin" + code;

        String systemName = CanRegClientApp.getApplication().testConnection(serverObjectString);

        if (systemName != null) {
            nameTextField.setText(systemName);
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Server connection OK.", "Message", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Server connection not OK.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Action
    public void addServerToList() {
        Properties properties = localSettings.getProperties();

        if (testServerConnection()) {
            //find an available server number
            boolean found = false;
            int i = 0;
            while (!found) {
                found = properties.getProperty("server." + (i++) + ".name") == null;
            }
            // step one back
            i -= 1;


            ServerDescription sd = new ServerDescription(nameTextField.getText(),
                    serverURLTextField.getText(),
                    Integer.parseInt(portField.getText()),
                    codeField.getText(), i);
            localSettings.addServerDescription(sd);
            canRegSystemComboBox.setModel(new javax.swing.DefaultComboBoxModel(localSettings.getServerDescriptions().toArray()));
        }
    }

    @Action
    public void serverComboboxChanged() {
        ServerDescription sd = localSettings.getServerDescriptions().get(canRegSystemComboBox.getSelectedIndex());
        portField.setText(sd.getPort() + "");
        serverURLTextField.setText(sd.getUrl());
        codeField.setText(sd.getCode());
    }
}
