/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class PersonSearchListener implements ActionListener, Serializable {
    private ActionListener actionListener;
    
    public void setActionListener(ActionListener actionListener){
        this.actionListener = actionListener;
    }
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println(command);
        if (command.startsWith("range")) {
            // recordsInRangeField.setText(command.substring(6));
            actionListener.actionPerformed(e);
        }
    }
}
