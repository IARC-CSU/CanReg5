/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2016  International Agency for Research on Cancer
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
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.client.gui.components;

import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.GregorianCalendarCanReg;
import com.toedter.calendar.JDateChooser;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
        String dateFormatString = databaseListElement.getDateFormatString();
        dateChooser.setDateFormatString(dateFormatString);
        // dateChooser.setDateFormatString("MMMMM d, yyyy");
        splitPane1.remove(splitPane1.getRightComponent());
        splitPane1.setTopComponent(dateChooser);
        dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        codeTextField = dateField;
        codeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                codeTextFieldKeyTyped(evt);
            }
        });

        String fillInStatus = databaseListElement.getFillInStatus();
        if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_AUTOMATIC_STRING)) {
            dateField.setFocusable(false);
            dateField.setEditable(false);
        } else if (fillInStatus.equalsIgnoreCase(Globals.FILL_IN_STATUS_MANDATORY_STRING)) {
            dateField.setBackground(MANDATORY_VARIABLE_MISSING_COLOR);
            mandatory = true;
        }
        // setMaximumLength(databaseListElement.getVariableLength());
        setMaximumLength(dateFormatString.length());

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

        dateField.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(dateField, evt);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(dateField, evt);
            }
        });
    }

    private void codeTextFieldMousePressed(java.awt.event.MouseEvent evt) {
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(dateField, evt);
    }

    private void codeTextFieldMouseReleased(java.awt.event.MouseEvent evt) {
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(dateField, evt);
    }

    private void codeTextFieldKeyTyped(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            transferFocusToNext();
        }
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
                if (date != null) {
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
                } else {
                    codeTextField.setText(value);
                }
            } catch (ParseException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException numberFormatException) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.WARNING, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("VALUE: ") + value, numberFormatException);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.WARNING, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("VALUE: ") + value, ex);
            } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.WARNING, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("VALUE: ") + value, stringIndexOutOfBoundsException);
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
        String valueObjectString = null;
        if (valueString.length() > 0) {
            try {
                String dateFormatString = dateChooser.getDateFormatString();
                GregorianCalendarCanReg tempCalendar = DateHelper.parseDateStringToGregorianCalendarCanReg(valueString, dateFormatString);
                if (tempCalendar != null) {
                    valueObjectString = DateHelper.parseGregorianCalendarCanRegToDateString(tempCalendar, Globals.DATE_FORMAT_STRING);
                } 
            } catch (ParseException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(DateVariableEditorPanel.class.getName()).log(Level.WARNING, java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString("VALUE: ") + valueString + java.util.ResourceBundle.getBundle("canreg/client/gui/components/resources/VariableEditorPanel").getString(", DATE FORMAT: ") + dateChooser.getDateFormatString(), ex);
            } finally {
                // if the date is malformed we just return the data as is.
                if (valueObjectString == null || valueObjectString.isEmpty()) {
                    valueObjectString = codeTextField.getText().trim();
                }
            }
        }
        return valueObjectString;
    }
}
