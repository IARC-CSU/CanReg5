/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017  International Agency for Research on Cancer
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
 * FastFilterInternalFrame.java
 *
 * Created on 29 February 2008, 14:44
 */
package canreg.client.gui.components;

import canreg.client.CanRegClientApp;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.LocalizationHelper;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.commons.lang.ArrayUtils;
import com.toedter.calendar.JDateChooser;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author  morten
 */
public class FastFilterInternalFrame extends javax.swing.JInternalFrame implements ActionListener {

    private DatabaseVariablesListElement[] variablesInTable;
    private Document doc;
    private String tableName = Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME;
    private Map<Integer, Dictionary> dictionaries;
    Dictionary dictionary;
    private Map<String, DictionaryEntry> possibleValuesMap;
    private ActionListener actionListener;
    private int maxLength;
    private boolean dictionaryPopUp = true;
    private boolean currentSelectionAdded = false;
    private DatabaseVariablesListElement[] patientVariablesInDB; // = canreg.common.Tools.getVariableListElements(CanRegClientApp.getApplication().getDatabseDescription(), Globals.NAMESPACE, Globals.PATIENT_TABLE_NAME);
    private DatabaseVariablesListElement[] tumourVariablesInDB; // = canreg.common.Tools.getVariableListElements(CanRegClientApp.getApplication().getDatabseDescription(), Globals.NAMESPACE, Globals.TUMOUR_TABLE_NAME);
    private DatabaseVariablesListElement[] sourceVariablesInDB; // = canreg.common.Tools.getVariableListElements(CanRegClientApp.getApplication().getDatabseDescription(), Globals.NAMESPACE, Globals.SOURCE_TABLE_NAME);
    private Comparator comparator;
    private DictionaryElementChooser dictionaryElementChooser = null;
    private JTextField dictionaryElementChooserAssignedTextField = null;

    /** Creates new form FastFilterInternalFrame */
    public FastFilterInternalFrame() {
        initComponents();
        initValues();
        dictionaryElementChooser = new DictionaryElementChooser(this);
        dictionaryElementChooserAssignedTextField = valueTextField;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        instructionLabel1 = new javax.swing.JLabel();
        instructionLabel2 = new javax.swing.JLabel();
        variableComboBox = new javax.swing.JComboBox();
        operationComboBox = new javax.swing.JComboBox();
        logicalOperatorComboBox = new javax.swing.JComboBox();
        variableLabel = new javax.swing.JLabel();
        operationLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        valuesSplitPane = new javax.swing.JSplitPane();
        valueTextField = new javax.swing.JTextField();
        valueTextField2 = new javax.swing.JTextField();
        //<ictl.co>
        dateValuesSplitPane = new javax.swing.JSplitPane();
        dateChooser = new JDateChooser();
        dateChooser2 = new JDateChooser();
        //</ictl.co>
        jLabel1 = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(FastFilterInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setName("jPanel1"); // NOI18N

        instructionLabel1.setText(resourceMap.getString("instructionLabel1.text")); // NOI18N
        instructionLabel1.setName("instructionLabel1"); // NOI18N

        instructionLabel2.setText(resourceMap.getString("instructionLabel2.text")); // NOI18N
        instructionLabel2.setName("instructionLabel2"); // NOI18N

        variableComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(FastFilterInternalFrame.class, this);
        variableComboBox.setAction(actionMap.get("varibleChosenAction")); // NOI18N
        variableComboBox.setName("variableComboBox"); // NOI18N
        variableComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                variableComboBoxActionPerformed(evt);
            }
        });

        operationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationComboBox.setAction(actionMap.get("operatorSelected")); // NOI18N
        operationComboBox.setName("operationComboBox"); // NOI18N

        logicalOperatorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        logicalOperatorComboBox.setAction(actionMap.get("operatorAction")); // NOI18N
        logicalOperatorComboBox.setName("logicalOperatorComboBox"); // NOI18N

        variableLabel.setText(resourceMap.getString("variableLabel.text")); // NOI18N
        variableLabel.setName("variableLabel"); // NOI18N

        operationLabel.setText(resourceMap.getString("operationLabel.text")); // NOI18N
        operationLabel.setName("operationLabel"); // NOI18N

        valueLabel.setText(resourceMap.getString("valueLabel.text")); // NOI18N
        valueLabel.setName("valueLabel"); // NOI18N

        valuesSplitPane.setResizeWeight(0.5);
        valuesSplitPane.setName("valuesSplitPane"); // NOI18N
        //<ictl.co>
        dateValuesSplitPane.setResizeWeight(0.5);
        dateValuesSplitPane.setName("dateValuesSplitPane"); // NOI18N
        dateValuesSplitPane.setVisible(false);
        //</ictl.co>

        valueTextField.setText(resourceMap.getString("valueTextField.text")); // NOI18N
        valueTextField.setAction(actionMap.get("addAction")); // NOI18N
        valueTextField.setName("valueTextField"); // NOI18N
        valueTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickHandler(evt);
            }
        });
        valuesSplitPane.setLeftComponent(valueTextField);
        //<ictl.co>
        dateValuesSplitPane.setLeftComponent(dateChooser);
        dateChooser.setName("dateChooser"); // NOI18N
        dateChooser.setDateFormatString(Globals.DATE_FORMAT_STRING);
        dateChooser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dateChooserMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dateChooserMouseReleased(evt);
            }
        });
        //</ictl.co>
        valueTextField2.setAction(actionMap.get("addAction")); // NOI18N
        valueTextField2.setName("valueTextField2"); // NOI18N
        valueTextField2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                valueTextField2mouseClickHandler(evt);
            }
        });
        valuesSplitPane.setRightComponent(valueTextField2);
        //<ictl.co>
        dateValuesSplitPane.setRightComponent(dateChooser2);
        dateChooser2.setName("dateChooser2"); // NOI18N
        dateChooser2.setVisible(false);
        dateChooser2.setDateFormatString(Globals.DATE_FORMAT_STRING);
        dateChooser2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dateChooserMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dateChooserMouseReleased(evt);
            }
        });
        //</ictl.co>
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(instructionLabel1)
            .addComponent(instructionLabel2)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(variableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(variableLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(operationLabel)
                    .addComponent(operationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(valuesSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                                                .addComponent(dateValuesSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(valueLabel)
                        .addGap(246, 246, 246)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logicalOperatorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(instructionLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(instructionLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valueLabel)
                    .addComponent(operationLabel)
                    .addComponent(jLabel1)
                    .addComponent(variableLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logicalOperatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(variableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(operationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(valuesSplitPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(dateValuesSplitPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("filterPanel.border.title"))); // NOI18N
        filterPanel.setName("filterPanel"); // NOI18N

        scrollPane.setName("scrollPane"); // NOI18N

        textPane.setToolTipText(resourceMap.getString("textPane.toolTipText")); // NOI18N
        textPane.setName("textPane"); // NOI18N
        textPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                textPaneMousePressed(evt);
            }
        });
        scrollPane.setViewportView(textPane);

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
        );

        cancelButton.setAction(actionMap.get("cancelAction")); // NOI18N
        cancelButton.setToolTipText(resourceMap.getString("cancelButton.toolTipText")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        okButton.setAction(actionMap.get("okAction")); // NOI18N
        okButton.setToolTipText(resourceMap.getString("okButton.toolTipText")); // NOI18N
        okButton.setName("okButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(filterPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okButton))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void mouseClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickHandler
    DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
    if (dictionaryPopUp && (valueTextField.equals(evt.getSource()) || valueTextField2.equals(evt.getSource())) && dbvle.getVariableType().equalsIgnoreCase("dict")) {
        if (possibleValuesMap == null) {
            JOptionPane.showInternalMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/FastFilterInternalFrame").getString("EMPTY_DICTIONARY"), java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/FastFilterInternalFrame").getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        } else {
            // String oldValue = getValue().toString();
            // DictionaryEntry oldSelection = possibleValuesMap.get(oldValue);
            if (dictionaryElementChooser == null) {
                dictionaryElementChooser = new DictionaryElementChooser(this);
            } else {
                dictionaryElementChooser.setFirstPass();
            }
            dictionaryElementChooser.setDictionary(dictionary);
            // dictionaryElementChooser.setSelectedElement(oldSelection);

            // safe to cast as we check it in the first if-statement
            dictionaryElementChooserAssignedTextField = (JTextField) evt.getSource();
            CanRegClientView.showAndPositionInternalFrame(
                    CanRegClientApp.getApplication().getDesktopPane(),
                    dictionaryElementChooser);
        }
    } else {
        // Do nothing
    }
    currentSelectionAdded = false;
}//GEN-LAST:event_mouseClickHandler

private void valueTextField2mouseClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_valueTextField2mouseClickHandler
    this.mouseClickHandler(evt);
}//GEN-LAST:event_valueTextField2mouseClickHandler

private void textPaneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textPaneMousePressed
    MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(textPane, evt);
}//GEN-LAST:event_textPaneMousePressed

private void variableComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_variableComboBoxActionPerformed
    DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
        if (dbvle.getVariableType().equalsIgnoreCase("dict")) {
        if (possibleValuesMap == null) {
            // JOptionPane.showInternalMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/FastFilterInternalFrame").getString("EMPTY_DICTIONARY"), java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/FastFilterInternalFrame").getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        } else {
            if (dictionaryElementChooser == null) {
                dictionaryElementChooser = new DictionaryElementChooser(this);
            }
            dictionaryElementChooser.clearFilter();
        }
    }
}//GEN-LAST:event_variableComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel instructionLabel1;
    private javax.swing.JLabel instructionLabel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox logicalOperatorComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox operationComboBox;
    private javax.swing.JLabel operationLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextPane textPane;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTextField;
    private javax.swing.JTextField valueTextField2;
    //<ictl.co>
    private JDateChooser dateChooser;
    private JDateChooser dateChooser2;
    private javax.swing.JSplitPane dateValuesSplitPane;
    //</ictl.co>
    private javax.swing.JSplitPane valuesSplitPane;
    private javax.swing.JComboBox variableComboBox;
    private javax.swing.JLabel variableLabel;
    // End of variables declaration//GEN-END:variables

    private void initValues() {
        doc = canreg.client.CanRegClientApp.getApplication().getDatabseDescription();
        dictionaries = canreg.client.CanRegClientApp.getApplication().getDictionary();
        String[] logicalOperator = {"", "AND", "OR"};
        logicalOperatorComboBox.setModel(new DefaultComboBoxModel(logicalOperator));
        String[] operators = {"=", "<>", ">", "<", ">=", "<=", "BETWEEN", "LIKE", "IN"};
        operationComboBox.setModel(new DefaultComboBoxModel(operators));
        operationComboBox.setSelectedIndex(0);
        // Get the system description

        patientVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.PATIENT_TABLE_NAME);
        tumourVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.TUMOUR_TABLE_NAME);
        sourceVariablesInDB = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, Globals.SOURCE_TABLE_NAME);

        comparator = new Comparator<DatabaseVariablesListElement>() {

            @Override
            public int compare(DatabaseVariablesListElement object1, DatabaseVariablesListElement o2) {
                return object1.toString().compareToIgnoreCase(o2.toString());
            }
        };

        Arrays.sort(patientVariablesInDB, comparator);
        Arrays.sort(tumourVariablesInDB, comparator);
        Arrays.sort(sourceVariablesInDB, comparator);

        refreshVariableList();
    }

    private void refreshVariableList() {
        if (tableName.equalsIgnoreCase(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            variablesInTable = (DatabaseVariablesListElement[])ArrayUtils.addAll(patientVariablesInDB, tumourVariablesInDB);
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_JOIN_TABLE_NAME)) {
            variablesInTable = (DatabaseVariablesListElement[])ArrayUtils.addAll(sourceVariablesInDB, tumourVariablesInDB);                
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_AND_TUMOUR_AND_PATIENT_JOIN_TABLE_NAME)) {
            variablesInTable = (DatabaseVariablesListElement[])ArrayUtils.addAll(sourceVariablesInDB, tumourVariablesInDB);                
            variablesInTable = (DatabaseVariablesListElement[])ArrayUtils.addAll(variablesInTable, patientVariablesInDB);
        } else if (tableName.equalsIgnoreCase(Globals.PATIENT_TABLE_NAME)) {
            variablesInTable = patientVariablesInDB;
        } else if (tableName.equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME)) {
            variablesInTable = tumourVariablesInDB;
        } else if (tableName.equalsIgnoreCase(Globals.SOURCE_TABLE_NAME)) {
            variablesInTable = sourceVariablesInDB;
        }
        Arrays.sort(variablesInTable, comparator);
        variableComboBox.setModel(new DefaultComboBoxModel(variablesInTable));
        variableComboBox.setSelectedItem(0);
        updatePossibleValues();
        //<ictl.co>
        updateOperations();
        //</ictl.co>
    }
    
    /**
     * 
     * @param str
     */
    public void setTextPane(String str) {
        textPane.setText(str);
    }

    /**
     * 
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
        refreshVariableList();
    }

    /**
     * 
     * @param al
     */
    public void setActionListener(ActionListener al) {
        this.actionListener = al;
    }

    /**
     * 
     */
    @Action
    public void cancelAction() {
        this.setVisible(false);
    }

    /**
     * 
     */
    @Action
    public void okAction() {
        if (currentSelectionIsNotAdded()) {
            addAction();
        }
        actionListener.actionPerformed(new ActionEvent(this, 0, textPane.getText().trim()));
        this.setVisible(false);
    }

    /**
     * 
     */
    @Action
    public void addAction() {
        if (!addDateAction()) {//<ictl.co>
            String newFilterPart = "";
            DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
            newFilterPart +=
                    dbvle.getDatabaseVariableName();
            newFilterPart +=
                    " ";
            newFilterPart +=
                    operationComboBox.getSelectedItem().toString();
            newFilterPart +=
                    " ";
            DatabaseVariablesListElement dvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
            if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                newFilterPart += "'";
            }

            newFilterPart += valueTextField.getText();
            if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                newFilterPart += "'";
            }

            if (operationComboBox.getSelectedItem().toString().equalsIgnoreCase("BETWEEN")) {
                newFilterPart += " AND ";

                if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                    newFilterPart += "'";
                }

                newFilterPart += valueTextField2.getText();
                if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                    newFilterPart += "'";
                }
            }

            newFilterPart += " ";
            newFilterPart +=
                    logicalOperatorComboBox.getSelectedItem().toString();
            newFilterPart +=
                    " ";
            textPane.setText(textPane.getText() + newFilterPart);

            // reset things
            valueTextField.setText("");
            valueTextField2.setText("");
            logicalOperatorComboBox.setSelectedIndex(0);

            currentSelectionAdded = true;
        }
    }

    /**
     * 
     */
    @Action
    public void varibleChosenAction() {
        valueTextField.setText("");
        valueTextField2.setText("");
        updatePossibleValues();
        currentSelectionAdded = false;
        //<ictl.co>
        dateChooser.setDate(null);
        dateChooser2.setDate(null);
        updateOperations();
        //<ictl.co>
    }

    @SuppressWarnings("empty-statement")
    private void updatePossibleValues() {
        DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
        maxLength = dbvle.getVariableLength();
        int id = dbvle.getDictionaryID();
        if (id >= 0) {
            dictionary = dictionaries.get(id);
            if (dictionary != null) {
                // Map sortedmap = new TreeMap(map);
                possibleValuesMap = dictionary.getDictionaryEntries();
            } else {
                possibleValuesMap = null;
            }
        } else {
            possibleValuesMap = null;
        }
    }

    @Action
    public void operatorSelected() {
        //<ictl.co>
        if (!dateOperatorSelected()) {
            //</ictl.co>
            String operator = operationComboBox.getSelectedItem().toString();
            if ("BETWEEN".equalsIgnoreCase(operator)) {
                valueTextField2.setVisible(true);
                valuesSplitPane.setDividerLocation(0.5);
                dictionaryPopUp = true;
            } else if ("LIKE".equalsIgnoreCase(operator)) {
                valueTextField2.setVisible(false);
                valuesSplitPane.setDividerLocation(1);
                dictionaryPopUp = false;
            } else if ("IN".equalsIgnoreCase(operator)) {
                valueTextField2.setVisible(false);
                valuesSplitPane.setDividerLocation(1);
                dictionaryPopUp = false;
            } else {
                valueTextField2.setVisible(false);
                valuesSplitPane.setDividerLocation(1);
                dictionaryPopUp = true;
            }
            currentSelectionAdded = false;
        }
    }

    private boolean currentSelectionIsNotAdded() {
        boolean isAdded = false;
        if (valueTextField.getText().trim().length() == 0 && dateChooser.getDate() == null) {
            isAdded = true;
        } else {
            isAdded = currentSelectionAdded;
        }
        return !isAdded;
    }

    @Action
    public void operatorAction() {
        if (currentSelectionIsNotAdded()) {
            addAction();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(DictionaryElementChooser.OK_ACTION)) {
            dictionaryElementChooserAssignedTextField.setText(dictionaryElementChooser.getSelectedElement().getCode());
        }
    }

    //<ictl.co>
    private boolean dateOperatorSelected() {
        if (variableComboBox.getSelectedItem() instanceof DatabaseVariablesListElement) {
            DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
            if (dbvle.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
                dictionaryPopUp = false;
                String operator = operationComboBox.getSelectedItem().toString();
                if ("BETWEEN".equalsIgnoreCase(operator)) {
                    dateChooser2.setVisible(true);
                    dateValuesSplitPane.setDividerLocation(0.5);
                } else if ("LIKE".equalsIgnoreCase(operator)) {
                    dateChooser2.setVisible(false);
                    dateValuesSplitPane.setDividerLocation(1);
                } else if ("IN".equalsIgnoreCase(operator)) {
                    dateChooser2.setVisible(false);
                    dateValuesSplitPane.setDividerLocation(1);
                } else {
                    dateChooser2.setVisible(false);
                    dateValuesSplitPane.setDividerLocation(1);
                }
                currentSelectionAdded = false;
                return true;
            }
        }
        return false;
    }

    private void updateOperations() {
        if (LocalizationHelper.isRtlLanguageActive()) {
            String[] operators = {"=", "<>", ">", "<", ">=", "<=", "BETWEEN", "LIKE", "IN"};
            DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
            if (dbvle.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
                operators = new String[]{"=", "<>", ">", "<", ">=", "<=", "BETWEEN", "IN"};
                dateValuesSplitPane.setVisible(true);
                dateChooser2.setVisible(false);
                valuesSplitPane.setVisible(false);
            } else {
                dateValuesSplitPane.setVisible(false);
                valuesSplitPane.setVisible(true);
            }
            operationComboBox.setModel(new DefaultComboBoxModel(operators));
        }
    }

    private void dateChooserMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dateChooserMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(valueTextField, evt);
    }//GEN-LAST:event_dateChooserMousePressed

    private void dateChooserMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dateChooserMouseReleased
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(valueTextField, evt);
    }//GEN-LAST:event_dateChooserMouseReleased

    //<ictl.co>
    private boolean addDateAction() {
        DatabaseVariablesListElement dbvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
        if (dbvle.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DATE_NAME)) {
            String newFilterPart = "";
            newFilterPart +=
                    dbvle.getDatabaseVariableName();
            newFilterPart +=
                    " ";
            newFilterPart +=
                    operationComboBox.getSelectedItem().toString();
            newFilterPart +=
                    " ";
            DatabaseVariablesListElement dvle = (DatabaseVariablesListElement) variableComboBox.getSelectedItem();
            if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                newFilterPart += "'";
            }

            newFilterPart += ((JTextField)dateChooser.getDateEditor().getUiComponent()).getText();
            if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                newFilterPart += "'";
            }

            if (operationComboBox.getSelectedItem().toString().equalsIgnoreCase("BETWEEN")) {
                newFilterPart += " AND ";

                if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                    newFilterPart += "'";
                }

                newFilterPart += ((JTextField)dateChooser2.getDateEditor().getUiComponent()).getText();
                if (!dvle.getVariableType().equalsIgnoreCase("Number")) {
                    newFilterPart += "'";
                }
            }

            newFilterPart += " ";
            newFilterPart +=
                    logicalOperatorComboBox.getSelectedItem().toString();
            newFilterPart +=
                    " ";
            textPane.setText(textPane.getText() + newFilterPart);

            // reset things
            dateChooser.setDate(null);
            dateChooser2.setDate(null);
            valueTextField.setText("");
            valueTextField2.setText("");
            logicalOperatorComboBox.setSelectedIndex(0);

            currentSelectionAdded = true;
            return true;
        }
        return false;
    }

    //</ictl.co>
}
