package canreg.client.gui.tools.globalpopup;

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
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

    public static void potentiallyShowPopUpMenuTextComponent(JTextComponent textComponent, MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            Point pt = SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), textComponent);
            JPopupMenu menu = new MyPopUpMenu(textComponent);
            menu.show(textComponent, pt.x, pt.y);
        }
    }
}
