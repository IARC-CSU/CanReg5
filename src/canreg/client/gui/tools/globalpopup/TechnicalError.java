package canreg.client.gui.tools.globalpopup;

import javax.swing.JOptionPane;

public class TechnicalError {
    public void errorDialog(){
        JOptionPane.showMessageDialog(null,
            java.util.ResourceBundle
                .getBundle("canreg/client/gui/tools/globalpopup/resources/globalpopup")
                .getString("TECHNICAL ERROR"),
            java.util.ResourceBundle
                .getBundle("canreg/client/gui/tools/globalpopup/resources/globalpopup")
                .getString("ERROR HEADER"),
            JOptionPane.ERROR_MESSAGE);
    }
}
