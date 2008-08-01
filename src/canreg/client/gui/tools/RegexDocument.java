package canreg.client.gui.tools;

import java.awt.Toolkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class RegexDocument extends MaxLengthDocument {

    protected final Pattern pattern;

    /**
     * Contructor used to construct a document object which pattern matches
     * strings as typed.
     *
     * @param regex
     * pattern to match on typed strings
     * @param maxLength
     * maximum length of full string
     */
    public RegexDocument(final String regex, final int maxLength) {
        super(maxLength);
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public void insertString(final int offset, final String str,
            final AttributeSet attr) throws BadLocationException {
        final Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            super.insertString(offset, str, attr);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
