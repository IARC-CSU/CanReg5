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
import canreg.common.Globals;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import java.awt.event.ActionListener;
import java.util.Map;
import org.w3c.dom.Document;

/**
 *
 * @author patri_000
 */
public class RecordEditorTumour extends javax.swing.JPanel implements RecordEditorPanel {
   
    private DatabaseRecord databaseRecord;
    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private DatabaseGroupsListElement[] groupListElements;
    private final GlobalToolBox globalToolBox;
    private boolean hasChanged = false;
    private ActionListener actionListener;
    
    
    public RecordEditorTumour(ActionListener actionListener) {
        initComponents(); 
    }
    
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
    
    @Override
    public void toggleObsolete(boolean confirmed) {
        if (confirmed) {
            DatabaseVariablesListElement dbvle = obsoleteFlagVariableListElement;
            if (dbvle != null) {
                boolean obsolete = obsoleteToggleButton.isSelected();
                if (obsolete) {
                    databaseRecord.setVariable(dbvle.getDatabaseVariableName(), Globals.OBSOLETE_VALUE);
                } else {
                    databaseRecord.setVariable(dbvle.getDatabaseVariableName(), Globals.NOT_OBSOLETE_VALUE);
                }
            }
        } else {
            obsoleteToggleButton.setSelected(!obsoleteToggleButton.isSelected());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        systemPanel = new javax.swing.JPanel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        jPanel1 = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        mpButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jPanel3 = new javax.swing.JPanel();
        confirmedCheck = new javax.swing.JCheckBox();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        updatedByPanel = new javax.swing.JPanel();
        byLabel1 = new javax.swing.JLabel();
        userLabel1 = new javax.swing.JLabel();
        dateLabel1 = new javax.swing.JLabel();
        sequencePanel = new javax.swing.JPanel();
        userLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
        jPanel4 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        dataScrollPane = new javax.swing.JScrollPane();
        dataPanel = new javax.swing.JPanel();
        variableEditorGroupPanel1 = new canreg.client.gui.dataentry2.components.VariableEditorGroupPanel();
        variableEditorGroupPanel2 = new canreg.client.gui.dataentry2.components.VariableEditorGroupPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        addSourceRecordButton = new javax.swing.JButton();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        sourceMenuButton = new javax.swing.JButton();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jPanel10 = new javax.swing.JPanel();
        sourcesTabbedPane = new canreg.client.gui.dataentry2.FixWidthRowTabbedPane();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        systemPanel.setMaximumSize(new java.awt.Dimension(32767, 100));
        systemPanel.setPreferredSize(new java.awt.Dimension(1062, 80));
        systemPanel.setLayout(new javax.swing.BoxLayout(systemPanel, javax.swing.BoxLayout.LINE_AXIS));
        systemPanel.add(filler4);

        jPanel1.setMaximumSize(new java.awt.Dimension(32767, 95));
        jPanel1.setPreferredSize(new java.awt.Dimension(130, 50));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        searchButton.setText("Verified: NO");
        searchButton.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel1.add(searchButton);
        jPanel1.add(filler1);

        mpButton.setText("MP Search");
        mpButton.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel1.add(mpButton);

        systemPanel.add(jPanel1);
        systemPanel.add(filler3);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 95));
        jPanel2.setPreferredSize(new java.awt.Dimension(150, 50));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel6.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Record Status");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabel2.setMaximumSize(new java.awt.Dimension(200, 100));
        jLabel2.setPreferredSize(new java.awt.Dimension(68, 23));
        jPanel6.add(jLabel2);

        jPanel2.add(jPanel6);
        jPanel2.add(filler2);

        jPanel3.setMaximumSize(new java.awt.Dimension(200, 45));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        confirmedCheck.setText("Confirmed");
        confirmedCheck.setMaximumSize(new java.awt.Dimension(200, 23));
        jPanel3.add(confirmedCheck);

        jPanel2.add(jPanel3);

        systemPanel.add(jPanel2);
        systemPanel.add(filler5);

        updatedByPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Updated", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        updatedByPanel.setMaximumSize(new java.awt.Dimension(32767, 95));
        updatedByPanel.setMinimumSize(new java.awt.Dimension(100, 30));

        byLabel1.setText("By:");

        userLabel1.setText("<username>");

        dateLabel1.setText("<username>");

        javax.swing.GroupLayout updatedByPanelLayout = new javax.swing.GroupLayout(updatedByPanel);
        updatedByPanel.setLayout(updatedByPanelLayout);
        updatedByPanelLayout.setHorizontalGroup(
            updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(updatedByPanelLayout.createSequentialGroup()
                        .addComponent(byLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
                    .addComponent(dateLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        updatedByPanelLayout.setVerticalGroup(
            updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updatedByPanelLayout.createSequentialGroup()
                .addGroup(updatedByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(byLabel1)
                    .addComponent(userLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        systemPanel.add(updatedByPanel);

        sequencePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sequence", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        sequencePanel.setMaximumSize(new java.awt.Dimension(32767, 95));
        sequencePanel.setMinimumSize(new java.awt.Dimension(100, 30));

        userLabel.setText("Number: ");

        dateLabel.setText("Total: ");

        javax.swing.GroupLayout sequencePanelLayout = new javax.swing.GroupLayout(sequencePanel);
        sequencePanel.setLayout(sequencePanelLayout);
        sequencePanelLayout.setHorizontalGroup(
            sequencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sequencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
                .addContainerGap())
        );
        sequencePanelLayout.setVerticalGroup(
            sequencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequencePanelLayout.createSequentialGroup()
                .addComponent(userLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        systemPanel.add(sequencePanel);

        add(systemPanel);
        add(filler6);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setContinuousLayout(true);

        dataScrollPane.setBorder(null);

        dataPanel.setLayout(new javax.swing.BoxLayout(dataPanel, javax.swing.BoxLayout.PAGE_AXIS));
        dataPanel.add(variableEditorGroupPanel1);
        dataPanel.add(variableEditorGroupPanel2);

        dataScrollPane.setViewportView(dataPanel);

        jSplitPane2.setLeftComponent(dataScrollPane);

        jPanel7.setLayout(new javax.swing.OverlayLayout(jPanel7));

        jPanel8.setOpaque(false);

        jPanel9.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel9.setMinimumSize(new java.awt.Dimension(20, 36));
        jPanel9.setOpaque(false);
        jPanel9.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));
        jPanel9.add(filler7);

        addSourceRecordButton.setText("Add source record");
        addSourceRecordButton.setMaximumSize(new java.awt.Dimension(220, 23));
        addSourceRecordButton.setMinimumSize(new java.awt.Dimension(21, 23));
        jPanel9.add(addSourceRecordButton);
        jPanel9.add(filler8);

        sourceMenuButton.setText("Menu\n");
        sourceMenuButton.setMaximumSize(new java.awt.Dimension(100, 23));
        sourceMenuButton.setMinimumSize(new java.awt.Dimension(30, 23));
        jPanel9.add(sourceMenuButton);
        jPanel9.add(filler9);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 445, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel8);

        sourcesTabbedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourcesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(sourcesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel10);

        jSplitPane2.setRightComponent(jPanel7);

        jPanel4.add(jSplitPane2);

        add(jPanel4);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRecordButton;
    private javax.swing.JLabel byLabel1;
    private javax.swing.JCheckBox confirmedCheck;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JScrollPane dataScrollPane;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel dateLabel1;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton mpButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel sequencePanel;
    private javax.swing.JButton sourceMenuButton;
    private canreg.client.gui.dataentry2.FixWidthRowTabbedPane sourcesTabbedPane;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JPanel updatedByPanel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel userLabel1;
    private canreg.client.gui.dataentry2.components.VariableEditorGroupPanel variableEditorGroupPanel1;
    private canreg.client.gui.dataentry2.components.VariableEditorGroupPanel variableEditorGroupPanel2;
    // End of variables declaration//GEN-END:variables
}
