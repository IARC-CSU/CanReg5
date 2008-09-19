package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import com.toedter.calendar.JDateChooser;
import java.util.Calendar;
import javax.swing.JTextField;

/**
 *
 * @author ervikm
 */
public class DateVariableEditorPanel extends VariableEditorPanel {

    private com.toedter.calendar.JDateChooser dateChooser;
    private JTextField dateField;

    @Override
    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement) {
        this.databaseListElement = databaseListElement;
        setVariableName(databaseListElement.getFullName());

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString(Globals.DATE_FORMAT_STRING);
        splitPane1.remove(splitPane1.getRightComponent());
        splitPane1.setTopComponent(dateChooser);
        dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        textField1 = dateField;

        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase("Automatic")) {
            dateField.setFocusable(false);
            dateField.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase("Mandatory")) {
            // consider making global or configurable
            textField1.setBackground(java.awt.Color.YELLOW);
        }
        setMaximumLength(databaseListElement.getVariableLength());
    }

    @Override
    public void setValue(String value) {

        if (value.trim().length() == 0 && databaseListElement.getFillInStatus().equalsIgnoreCase("Mandatory")) {
            // consider making global or configurable
            dateField.setBackground(mandatoryMissingColor);
        } else {
            if (databaseListElement.getFillInStatus().equalsIgnoreCase("Mandatory")) {
                dateField.setBackground(java.awt.SystemColor.text);
            }
        }
        try {
            // TODO implement method for unknown dates...
            int year = Integer.parseInt(value.substring(0, 4));
            int month = Integer.parseInt(value.substring(4, 6));
            int day = Integer.parseInt(value.substring(6, 8));
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day); //month - the value used to set the MONTH calendar field. Month value is 0-based. e.g., 0 for January.
            dateChooser.setCalendar(calendar);
            // dateField.setText(value);
            if (year == 9999 || year == 0000 || month == 99 || month == 00 || day == 00 || day == 99) {
                // Unknown date
                dateField.setText(value);
            }
        } catch (NumberFormatException numberFormatException) {
            System.out.println(value);
        } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
            System.out.println(value);
        }
    }

    @Override
    public Object getValue() {
        Object valueObject = null;
        String valueString = dateField.getText();

        if (valueString.trim().length() > 0) {
            valueObject = Integer.parseInt(valueString.trim());
        }

        return valueObject;
    }
}
