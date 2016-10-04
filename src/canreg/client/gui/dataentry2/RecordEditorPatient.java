/*
 * Copyright (C) 2016 patri_000
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
 */
package canreg.client.gui.dataentry2;

import canreg.client.gui.components.VariableEditorPanelInterface;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import java.awt.event.ActionListener;
import java.util.Map;
import org.w3c.dom.Document;

/**
 *
 * @author patri_000
 */
public class RecordEditorPatient extends javax.swing.JPanel implements RecordEditorPanel{

    private DatabaseRecord databaseRecord;
    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private DatabaseGroupsListElement[] groupListElements;
    private final GlobalToolBox globalToolBox;
    private boolean hasChanged = false;
    private ActionListener actionListener;
    
    
    public RecordEditorPatient(ActionListener listener) {
        initComponents();
    }
    
    @Override
    public boolean isSaveNeeded() {
        boolean hasChanged = false;

        for(DatabaseVariablesListElement databaseVariablesListElement : variablesInTable) {
            VariableEditorPanelInterface panel = variableEditorPanels.get(databaseVariablesListElement.getDatabaseVariableName());
            if(panel != null) 
                hasChanged = hasChanged || panel.hasChanged();            
        }
        return hasChanged;
    }
    
    @Override
    public void setDocument(Document doc) {
        this.doc = doc;
    }
    
    @Override
    public void setDictionary(Map<Integer, Dictionary> dictionary) {
        this.dictionary = dictionary;
    }
    
    public DatabaseRecord getDatabaseRecord() {
        buildDatabaseRecord();
        return databaseRecord;
    }
    
    public void toggleObsolete(boolean confirmed) {
        //Not implemented in patient record.
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        systemPanel = new javax.swing.JPanel();
        updatedByPanel = new javax.swing.JPanel();
        byLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        mpButton = new javax.swing.JButton();
        dataScrollPane = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();
        variableEditorGroupPanel1 = new canreg.client.gui.dataentry2.components.VariableEditorGroupPanel();
        variableEditorGroupPanel2 = new canreg.client.gui.dataentry2.components.VariableEditorGroupPanel();

        setPreferredSize(new java.awt.Dimension(1200, 510));

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        systemPanel.setMaximumSize(new java.awt.Dimension(32767, 150));
        systemPanel.setPreferredSize(new java.awt.Dimension(400, 90));

        updatedByPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Updated", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        updatedByPanel.setMinimumSize(new java.awt.Dimension(100, 30));

        byLabel.setText("By:");

        userLabel.setText("<username>");

        dateLabel.setText("41/42/5123 ge:51:5df pm");

        javax.swing.GroupLayout updatedByPanelLayout = new javax.swing.GroupLayout(updatedByPanel);
        updatedByPanel.setLayout(updatedByPanelLayout);
        updatedByPanelLayout.setHorizontalGroup(
            updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(updatedByPanelLayout.createSequentialGroup()
                        .addComponent(byLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        updatedByPanelLayout.setVerticalGroup(
            updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanelLayout.createSequentialGroup()
                .addGroup(updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(byLabel)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        searchButton.setText("Person Search");
        searchButton.setMaximumSize(new java.awt.Dimension(200, 100));
        jPanel1.add(searchButton);
        jPanel1.add(filler1);

        mpButton.setText("Exact Search");
        mpButton.setMaximumSize(new java.awt.Dimension(200, 100));
        jPanel1.add(mpButton);

        javax.swing.GroupLayout systemPanelLayout = new javax.swing.GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        systemPanelLayout.setHorizontalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(5, 5, 5)
                .addComponent(updatedByPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );
        systemPanelLayout.setVerticalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updatedByPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );

        jPanel2.add(systemPanel);

        dataScrollPane.setBorder(null);

        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        dataPanel.add(variableEditorGroupPanel1);
        dataPanel.add(variableEditorGroupPanel2);

        dataScrollPane.setViewportView(dataPanel);

        jPanel2.add(dataScrollPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 1, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 1, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel byLabel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScrollPane;
    private javax.swing.JLabel dateLabel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton mpButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JPanel updatedByPanel;
    private javax.swing.JLabel userLabel;
    private canreg.client.gui.dataentry2.components.VariableEditorGroupPanel variableEditorGroupPanel1;
    private canreg.client.gui.dataentry2.components.VariableEditorGroupPanel variableEditorGroupPanel2;
    // End of variables declaration//GEN-END:variables
}
