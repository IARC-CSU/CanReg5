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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import org.jdesktop.application.Action;

/**
 *
 * @author  ervikm
 */
public class VariableMappingAlternativePanel extends javax.swing.JPanel {

    private DatabaseVariablesListElement databaseVariableListElement;
    private int fileColumnNumber;
    // private String fileVariable;
    // private String[] fileElements;

    /** Creates new form VariableMappingPanel */
    public VariableMappingAlternativePanel() {
        initComponents();
    }

    public void setFileElements(String[] fileElements) {
        // this.fileElements = fileElements;
        String[] entriesInComboBox = new String[fileElements.length + 1];
        entriesInComboBox[0] = "";
        System.arraycopy(fileElements, 0, entriesInComboBox, 1, fileElements.length);
        fileElementsComboBox.setModel(new javax.swing.DefaultComboBoxModel(entriesInComboBox));
    }

        private void updateWarning(String fileElementSelected) {
        if (fileElementSelected != null && fileElementSelected.trim().length() > 0) {
            if (databaseVariableListElement != null) {
                dbVariableLabel.setText("<html>" + dbVariableLabel(databaseVariableListElement) + "<html>");
                dbVariableLabel.repaint();
            }
        } else {
            if (databaseVariableListElement != null) {
                dbVariableLabel.setText("<html><b>" + dbVariableLabel(databaseVariableListElement) + "</b><html>");
                dbVariableLabel.repaint();
            }
        }
    }

    private static String dbVariableLabel(DatabaseVariablesListElement databaseVariablesListElement) {
        return databaseVariablesListElement.getFullName()
                + " (" 
                + databaseVariablesListElement.getShortName() 
                + " - "
                + databaseVariablesListElement.getFillInStatus()
                + java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableMappingAlternativePanel").getString(" - LENGTH: ") 
                + databaseVariablesListElement.getVariableLength() + ""
                + ")";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        fileElementsComboBox = new javax.swing.JComboBox();
        dbVariableLabel = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        fileElementsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DB Variable 1", "DB Variable 2" }));
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(VariableMappingAlternativePanel.class, this);
        fileElementsComboBox.setAction(actionMap.get("changeComboBoxAction")); // NOI18N
        fileElementsComboBox.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        fileElementsComboBox.setName("fileElementsComboBox"); // NOI18N
        jSplitPane1.setLeftComponent(fileElementsComboBox);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(VariableMappingAlternativePanel.class);
        dbVariableLabel.setText(resourceMap.getString("dbVariableLabel.text")); // NOI18N
        dbVariableLabel.setName("dbVariableLabel"); // NOI18N
        jSplitPane1.setRightComponent(dbVariableLabel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 
     * @return
     */
    public int getDBVariableIndex() {
        return databaseVariableListElement.getDatabaseTableVariableID();
    }

    public void setDBVariable(DatabaseVariablesListElement databaseVariableListElement) {
        this.databaseVariableListElement = databaseVariableListElement;
        // dbVariableLabel.setText(databaseVariableListElement.getFullName());
        updateWarning(getSelectedFileElement());
    }

    public String getSelectedFileElement() {
        String selected = (String) fileElementsComboBox.getSelectedItem();
        if (selected.trim().length() < 1) {
            return null;
        } else {
            return selected;
        }
    }

    public int getSelectedFileColumnNumber() {
        return fileElementsComboBox.getSelectedIndex() - 1;
    }

    public void setSelectedFileElement(String fileElement) {
        if (fileElement != null && fileElement.trim().length() > 0) {
            fileElementsComboBox.setSelectedItem(fileElement);
        } else {
            fileElementsComboBox.setSelectedItem(0);
        }
        updateWarning(fileElement);
    }

    /**
     * 
     */
    @Action
    public void changeComboBoxAction() {
        updateWarning(getSelectedFileElement());
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dbVariableLabel;
    private javax.swing.JComboBox fileElementsComboBox;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

    public DatabaseVariablesListElement getDatabaseVariablesListElement() {
        return databaseVariableListElement;
    }
}
