package canreg.client.gui.tools;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Steve 
 */
public class MaxLengthDocument extends PlainDocument {

    /**
     * Maximum length of the text
     */
    private final int maxLength;
    private ActionListener actionListener;

    public static String CHANGED_ACTION_STRING = "Changed";
    public static String MAX_LENGTH_ACTION_STRING = "Max length reached";

    /**
     * Default constructor.
     *
     * @param maxLength
     * the maximum number of characters that can be entered in the
     * field
     * @param actionListener 
     */
    public MaxLengthDocument(final int maxLength, ActionListener actionListener) {
        super();
        this.maxLength = maxLength;
        this.actionListener = actionListener;
    }

    /**
     * 
     * @param maxLength
     */
    public MaxLengthDocument(final int maxLength) {
        super();
        this.maxLength = maxLength;
        this.actionListener = null;
    }

    @Override
    public void insertString(final int offset, final String str,
            final AttributeSet attr) throws BadLocationException {

        actionListener.actionPerformed(new ActionEvent(this, 0, CHANGED_ACTION_STRING));

        if (getLength() + str.length() > maxLength) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(offset, str, attr);
        if (getLength() == maxLength&&actionListener!=null) {
            actionListener.actionPerformed(new ActionEvent(this, 0, MAX_LENGTH_ACTION_STRING));
        }
    }
}