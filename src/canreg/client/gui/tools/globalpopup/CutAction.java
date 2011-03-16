package canreg.client.gui.tools.globalpopup;

// inspired by Santhosh Kumar T - santhosh@in.fiorano.com

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

class CutAction extends AbstractAction{
    private JTextComponent jComponent;

    public CutAction(JTextComponent comp){
        super("Cut");
        this.jComponent = comp;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        jComponent.cut();
    }

    @Override
    public boolean isEnabled(){
        return jComponent.isEditable()
                && jComponent.isEnabled()
                && jComponent.getSelectedText()!=null;
    }
}
