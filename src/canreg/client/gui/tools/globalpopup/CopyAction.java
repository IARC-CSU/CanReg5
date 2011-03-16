package canreg.client.gui.tools.globalpopup;

// inspired by Santhosh Kumar T - santhosh@in.fiorano.com

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

class CopyAction extends AbstractAction{
    private JTextComponent jComponent;

    public CopyAction(JTextComponent jComponent){
        super("Copy");
        this.jComponent = jComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        jComponent.copy();
    }

    @Override
    public boolean isEnabled(){
        return jComponent.isEnabled()
                && jComponent.getSelectedText()!=null;
    }
}
