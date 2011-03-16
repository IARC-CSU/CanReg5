package canreg.client.gui.tools.globalpopup;

// inspired by Santhosh Kumar T - santhosh@in.fiorano.com

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

class DeleteAction extends AbstractAction{
    private JTextComponent jComponent;

    public DeleteAction(JTextComponent comp){
        super("Delete");
        this.jComponent = comp;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        jComponent.replaceSelection(null);
    }

    @Override
    public boolean isEnabled(){
        return jComponent.isEditable()
                && jComponent.isEnabled()
                && jComponent.getSelectedText()!=null;
    }
}