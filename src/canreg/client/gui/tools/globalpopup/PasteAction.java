package canreg.client.gui.tools.globalpopup;

// inspired by Santhosh Kumar T - santhosh@in.fiorano.com

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

class PasteAction extends AbstractAction{
    private JTextComponent jComponent;

    public PasteAction(JTextComponent jComponent){
        super("Paste");
        this.jComponent = jComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        jComponent.paste();
    }

    @Override
    public boolean isEnabled(){
        if (jComponent.isEditable() && jComponent.isEnabled()){
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
            return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        }else
            return false;
    }
}
