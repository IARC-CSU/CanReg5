package canreg.client.gui.tools.globalpopup;

import javax.swing.JOptionPane;
import java.util.ResourceBundle;

public class TechnicalError {
    private static final ResourceBundle TRANSLATION_RESOURCE_BUNDLE = java.util.ResourceBundle.getBundle("canreg/client/gui/tools/globalpopup/resources/globalpopup");

    public void errorDialog() {
        JOptionPane.showMessageDialog(null,
                TRANSLATION_RESOURCE_BUNDLE.getString("TECHNICAL ERROR"),
                TRANSLATION_RESOURCE_BUNDLE.getString("ERROR HEADER"),
                JOptionPane.ERROR_MESSAGE);
    }

    public void errorDialog(String message) {
        JOptionPane.showMessageDialog(null,
                message,
                TRANSLATION_RESOURCE_BUNDLE.getString("ERROR HEADER"),
                JOptionPane.ERROR_MESSAGE);
    }
}
