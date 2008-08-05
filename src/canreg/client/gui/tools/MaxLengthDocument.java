package canreg.client.gui.tools;

import java.awt.Toolkit;
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

    /**
     * Default constructor.
     *
     * @param maxLength
     * the maximum number of characters that can be entered in the
     * field
     */
    public MaxLengthDocument(final int maxLength) {
        super();
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(final int offset, final String str,
            final AttributeSet attr) throws BadLocationException {
        if (getLength() + str.length() > maxLength) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(offset, str, attr);
    }
}