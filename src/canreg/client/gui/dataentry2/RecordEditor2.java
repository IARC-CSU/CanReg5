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
public class RecordEditor2 extends javax.swing.JPanel {
    
    public RecordEditor2() {
        initComponents();
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                try {
                    //Turn off metal's use of bold fonts
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
	                        
                RecordEditor2 rec = new RecordEditor2();
                rec.addTab("Prueba 1", new JPanel());
                rec.addTab("Prueba 2", new JPanel());
                rec.setVisible(true);
                JFrame frame = new JFrame();
                frame.add(rec);
                frame.setVisible(true);
            }
        });
    }

    public void addTab(String title, Component comp) {
        this.customTabbedPane1.addTab(title, comp);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customTabbedPane1 = new canreg.client.gui.dataentry2.CustomTabbedPane();

        setName("Form"); // NOI18N

        customTabbedPane1.setName("customTabbedPane1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(customTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(customTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private canreg.client.gui.dataentry2.CustomTabbedPane customTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
