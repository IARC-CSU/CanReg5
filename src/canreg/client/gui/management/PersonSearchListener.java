/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.gui.management;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ervikm
 */
public class PersonSearchListener implements ActionListener, Serializable {
    private ActionListener actionListener;
    
    /**
     * 
     * @param actionListener
     */
    public void setActionListener(ActionListener actionListener){
        this.actionListener = actionListener;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        Logger.getLogger(PersonSearchListener.class.getName()).log(Level.INFO, command);
        if (command.startsWith("range")) {
            // recordsInRangeField.setText(command.substring(6));
            actionListener.actionPerformed(e);
        }
    }
}
