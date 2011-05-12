/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */

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
