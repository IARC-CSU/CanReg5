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
        
        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase("Automatic")) {
            dateField.setFocusable(false);
            dateField.setEditable(false);
        }

        setMaximumLength(databaseListElement.getVariableLength());
    }

    @Override
    public void setValue(String value) {
        if (value.length() > 0) {
            try {
                // TODO implement method for unknown dates...
                int year = Integer.parseInt(value.substring(0, 4));
                int month = Integer.parseInt(value.substring(4, 6));
                int day = Integer.parseInt(value.substring(6, 8));
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                dateChooser.setCalendar(calendar);
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
    }
    
    @Override
        public String getValue() {
            return dateField.getText();
    }
}
