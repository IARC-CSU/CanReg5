package canreg.client.gui.tools.globalpopup;

// inspired by Santhosh Kumar T - santhosh@in.fiorano.com

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

class SelectAllAction extends AbstractAction{
    private JTextComponent jComponent;

    public SelectAllAction(JTextComponent jComponent){
        super("Select All");
        this.jComponent = jComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        jComponent.selectAll();
    }

    @Override
    public boolean isEnabled(){
        return jComponent.isEnabled()
                && jComponent.getText().length()>0;
    }
}
