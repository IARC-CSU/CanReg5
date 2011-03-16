package canreg.client.gui.tools.globalpopup;

import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 *
 * @author ervikm
 */
public class MyPopUpMenu extends JPopupMenu {

    public MyPopUpMenu(JTextComponent tc) {
        super();
        add(new CutAction(tc));
        add(new CopyAction(tc));
        add(new PasteAction(tc));
        add(new DeleteAction(tc));
        addSeparator();
        add(new SelectAllAction(tc));
    }
}
