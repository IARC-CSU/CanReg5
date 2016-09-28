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

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author patri_000
 */
public class RecordEditor extends javax.swing.JInternalFrame {

    
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
                JDesktopPane deskPane = new JDesktopPane();
                deskPane.add(rec);
                
                JFrame frame = new JFrame();
                frame.setSize(1200, 700);
                frame.add(deskPane);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }
    
    
    
    public RecordEditor() {
        initComponents();
    }

   
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
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
        jPanel4 = new javax.swing.JPanel();
        patientsTabbedPane = new canreg.client.gui.dataentry2.FixWidthRowTabbedPane();
        recordEditorPatient1 = new canreg.client.gui.dataentry2.RecordEditorPatient();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        getContentPane().setLayout(new javax.swing.OverlayLayout(getContentPane()));

        jPanel1.setMaximumSize(new java.awt.Dimension(20000, 20000));
        jPanel1.setOpaque(false);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel2.setMinimumSize(new java.awt.Dimension(400, 36));
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add(filler1);

        jButton1.setText("Merge with other Patient ID");
        jPanel2.add(jButton1);
        jPanel2.add(filler5);

        showObsoleteRecordsCheckBox.setText("Enable obsolete records");
        jPanel2.add(showObsoleteRecordsCheckBox);
        jPanel2.add(filler4);

        jButton3.setText("PDF");
        jPanel2.add(jButton3);
        jPanel2.add(filler2);

        printButton.setText("Print");
        jPanel2.add(printButton);
        jPanel2.add(filler3);

        savetButton.setText("Save Alll");
        jPanel2.add(savetButton);
        jPanel2.add(filler7);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1016, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(402, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);

        patientsTabbedPane.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        patientsTabbedPane.addTab("Record Patient 1", recordEditorPatient1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(patientsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1016, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(patientsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel4);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private canreg.client.gui.dataentry2.FixWidthRowTabbedPane patientsTabbedPane;
    private javax.swing.JButton printButton;
    private canreg.client.gui.dataentry2.RecordEditorPatient recordEditorPatient1;
    private javax.swing.JButton savetButton;
    private javax.swing.JCheckBox showObsoleteRecordsCheckBox;
    // End of variables declaration//GEN-END:variables
}
