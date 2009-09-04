package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import com.toedter.calendar.JDateChooser;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;

/**
 *
 * @author ervikm
 */
public class DateVariableEditorPanel extends VariableEditorPanel {

    private com.toedter.calendar.JDateChooser dateChooser;
    private JTextField dateField;

    public DateVariableEditorPanel(ActionListener listener) {
        super(listener);
    }

    /**
     * 
     * @param databaseListElement
     */
    @Override
    public void setDatabaseVariablesListElement(DatabaseVariablesListElement databaseListElement) {
        this.databaseListElement = databaseListElement;
        setVariableName(databaseListElement.getFullName());

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString(Globals.DATE_FORMAT_STRING);
        // dateChooser.setDateFormatString("MMMMM d, yyyy");
        splitPane1.remove(splitPane1.getRightComponent());
        splitPane1.setTopComponent(dateChooser);
        dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        codeTextField = dateField;

        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_AUTOMATIC_STRING)) {
            dateField.setFocusable(false);
            dateField.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
            dateField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
        }
        setMaximumLength(databaseListElement.getVariableLength());

        dateField.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                componentFocusGained(evt);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                componentFocusLost(evt);
            }
        });
    }

    /**
     * 
     * @param value
     */
    @Override
    public void setValue(String value) {
        if (value.trim().length() == 0) {
            if (databaseListElement.getFillInStatus().equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
                codeTextField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            }
            codeTextField.setText(value);
        } else {
            codeTextField.setBackground(java.awt.SystemColor.text);
            try {
                GregorianCalendarCanReg date = DateHelper.parseDateStringToGregorianCalendarCanReg(value, Globals.DATE_FORMAT_STRING);
                dateChooser.setCalendar(date);
                String dateString = codeTextField.getText();
                String dateFormatString = dateChooser.getDateFormatString();
                // dateField.setText(value);
                if (date.isUnknownDay()) {
                    dateString = DateHelper.setDay(dateString, dateFormatString, "99");
                }
                if (date.isUnknownMonth()) {
                    dateString = DateHelper.setMonth(dateString, dateFormatString, "99");
                }
                codeTextField.setText(dateString);
            } catch (ParseException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException numberFormatException) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.WARNING, "Value: " + value, numberFormatException);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.WARNING, "Value: " + value, stringIndexOutOfBoundsException);
            }
        }

    }

    /**
     * 
     * @return
     */
    @Override
    public Object getValue() {
        String valueString = codeTextField.getText().trim();
        String valueObjectString = "";
        if (valueString.length() > 0) {
            try {
                String dateFormatString = dateChooser.getDateFormatString();
                // valueObject = Integer.parseInt(valueString.trim());
                GregorianCalendarCanReg tempCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg(valueString, dateFormatString);
                valueObjectString = DateHelper.parseGregorianCalendarCanRegToDateString(tempCalendar, Globals.DATE_FORMAT_STRING);
                // valueObject = Integer.parseInt(valueObjectString.trim());
            } catch (ParseException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return valueObjectString;
    }
}
