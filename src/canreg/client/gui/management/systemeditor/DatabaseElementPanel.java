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

/*
 * DatabaseElementPanel.java
 *
 * Created on 21-Jan-2010, 14:06:49
 */
package canreg.client.gui.management.systemeditor;

import canreg.common.DatabaseElement;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseElementPanel extends javax.swing.JPanel {

    private DatabaseElement databaseElement;
    private ActionListener listener;
    private int position = 0;
    public static String EDIT_ACTION = "edit";
    public static String STRUCTURE_CHANGE_ACTION = "structure_change";
    public static String MOVE_UP_ACTION = "move_up";
    public static String MOVE_DOWN_ACTION = "move_down";
    public static String REMOVE_ACTION = "remove";

    /** Creates new form DatabaseElementPanel */
    public DatabaseElementPanel() {
        initComponents();
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public DatabaseElementPanel(DatabaseElement databaseElement) {
        this();
        this.databaseElement = databaseElement;
        setDatabaseElement(databaseElement);
    }

    /**
     *
     * @param listener
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        nameTextField = new javax.swing.JTextField();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(DatabaseElementPanel.class);
        setBorder(javax.swing.BorderFactory.createEtchedBorder(resourceMap.getColor("Form.border.highlightColor"), resourceMap.getColor("Form.border.shadowColor"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(200, 46));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        nameLabel.setName("nameLabel"); // NOI18N
        add(nameLabel);

        jPanel1.setName("jPanel1"); // NOI18N

        nameTextField.setEditable(false);
        nameTextField.setName("nameTextField"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(DatabaseElementPanel.class, this);
        upButton.setAction(actionMap.get("moveUpAction")); // NOI18N
        upButton.setName("upButton"); // NOI18N

        downButton.setAction(actionMap.get("moveDown")); // NOI18N
        downButton.setName("downButton"); // NOI18N

        removeButton.setAction(actionMap.get("removeAction")); // NOI18N
        removeButton.setName("removeButton"); // NOI18N

        editButton.setAction(actionMap.get("editAction")); // NOI18N
        editButton.setName("editButton"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editButton))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(nameTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(downButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(editButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void removeAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, STRUCTURE_CHANGE_ACTION));
            listener.actionPerformed(new ActionEvent(this, 0, REMOVE_ACTION));
        }
    }

    @Action
    public void moveUpAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, MOVE_UP_ACTION));
        }
    }

    @Action
    public void moveDown() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, MOVE_DOWN_ACTION));
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downButton;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the databaseElement
     */
    public DatabaseElement getDatabaseElement() {
        return databaseElement;
    }

    /**
     * @param databaseElement the databaseElement to set
     */
    public final void setDatabaseElement(DatabaseElement databaseElement) {
        this.databaseElement = databaseElement;
        refresh();
    }

    @Action
    public void editAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, EDIT_ACTION));
        }
    }

    void refresh() {
        nameTextField.setText(databaseElement.getDescriptiveString());
    }

    void setRemovable(boolean editable) {
        editButton.setEnabled(editable);
    }

    void setColorSignal(Color colorize) {
        if (colorize!=null){
            setBorder(javax.swing.BorderFactory.createEtchedBorder(colorize.brighter(), colorize.darker()));
        }
    }
}
