/*
 * Copyright (C) 2016 Patricio
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

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Patricio
 */
public class RecordEditor extends javax.swing.JPanel {
    
    public RecordEditor() {
        initComponents();
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                try {                    
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
	                        
                RecordEditor rec = new RecordEditor();
                //rec.addTab("Record Patient 1", new canreg.client.gui.dataentry2.RecordEditorPatient());
                //rec.addTab("Record Patient 2", new canreg.client.gui.dataentry2.RecordEditorPatient());
                rec.setVisible(true);
                JFrame frame = new JFrame();
                frame.setSize(1200, 700);
                frame.add(rec);
                frame.setVisible(true);
            }
        });
    }

    public void addTab(String title, Component comp) {
        this.patientTabbedPane.addTab(title, comp);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        addTumourRecordButton = new javax.swing.JButton();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jButton1 = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        showObsoleteRecordsCheckBox = new javax.swing.JCheckBox();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jButton3 = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        printButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        savetButton = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        patientTabbedPane = new canreg.client.gui.dataentry2.CustomTabbedPane();
        recordEditorPatient1 = new canreg.client.gui.dataentry2.RecordEditorPatient();
        recordEditorPatient2 = new canreg.client.gui.dataentry2.RecordEditorPatient();

        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1200, 650));
        setLayout(new javax.swing.OverlayLayout(this));

        jPanel1.setMaximumSize(new java.awt.Dimension(20000, 20000));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 24));
        jPanel2.setMinimumSize(new java.awt.Dimension(428, 24));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 24));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        filler1.setName("filler1"); // NOI18N
        jPanel2.add(filler1);

        addTumourRecordButton.setText("Add tumour record");
        addTumourRecordButton.setName("addTumourRecordButton"); // NOI18N
        jPanel2.add(addTumourRecordButton);

        filler6.setName("filler6"); // NOI18N
        jPanel2.add(filler6);

        jButton1.setText("Merge with other Patient ID");
        jButton1.setName("jButton1"); // NOI18N
        jPanel2.add(jButton1);

        filler5.setName("filler5"); // NOI18N
        jPanel2.add(filler5);

        showObsoleteRecordsCheckBox.setText("Enable obsolete records");
        showObsoleteRecordsCheckBox.setName("showObsoleteRecordsCheckBox"); // NOI18N
        jPanel2.add(showObsoleteRecordsCheckBox);

        filler4.setName("filler4"); // NOI18N
        jPanel2.add(filler4);

        jButton3.setText("PDF");
        jButton3.setName("jButton3"); // NOI18N
        jPanel2.add(jButton3);

        filler2.setName("filler2"); // NOI18N
        jPanel2.add(filler2);

        printButton.setText("Print");
        printButton.setName("printButton"); // NOI18N
        jPanel2.add(printButton);

        filler3.setName("filler3"); // NOI18N
        jPanel2.add(filler3);

        savetButton.setText("Save Alll");
        savetButton.setName("savetButton"); // NOI18N
        jPanel2.add(savetButton);

        filler7.setName("filler7"); // NOI18N
        jPanel2.add(filler7);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1634, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(508, Short.MAX_VALUE))
        );

        add(jPanel1);

        patientTabbedPane.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        patientTabbedPane.setName("patientTabbedPane"); // NOI18N

        recordEditorPatient1.setName("recordEditorPatient1"); // NOI18N
        patientTabbedPane.addTab("Patient Record 1", recordEditorPatient1);

        recordEditorPatient2.setName("recordEditorPatient2"); // NOI18N
        patientTabbedPane.addTab("Patient Record 2", recordEditorPatient2);

        add(patientTabbedPane);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTumourRecordButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private canreg.client.gui.dataentry2.CustomTabbedPane patientTabbedPane;
    private javax.swing.JButton printButton;
    private canreg.client.gui.dataentry2.RecordEditorPatient recordEditorPatient1;
    private canreg.client.gui.dataentry2.RecordEditorPatient recordEditorPatient2;
    private javax.swing.JButton savetButton;
    private javax.swing.JCheckBox showObsoleteRecordsCheckBox;
    // End of variables declaration//GEN-END:variables
}
