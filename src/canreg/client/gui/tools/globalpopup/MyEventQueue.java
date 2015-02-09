/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.client.gui.tools.globalpopup;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;


// inspired by Santhosh Kumar T - santhosh@in.fiorano.com
public class MyEventQueue extends EventQueue{
    @Override
    protected void dispatchEvent(AWTEvent event){
        super.dispatchEvent(event);

        // interested only in mouseevents
        if(!(event instanceof MouseEvent))
            return;

        MouseEvent me = (MouseEvent)event;

        // interested only in popuptriggers
        if(!me.isPopupTrigger())
            return;

        // me.getComponent(...) retunrs the heavy weight component on which event occured
        Component comp = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());

        // interested only in textcomponents
        if(!(comp instanceof JTextComponent))
            return;

        // no popup shown by user code
        if(MenuSelectionManager.defaultManager().getSelectedPath().length>0)
            return;

        // create popup menu and show
        JTextComponent tc = (JTextComponent)comp;
        JPopupMenu menu = new MyPopUpMenu(tc);

        Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), tc);
        menu.show(tc, pt.x, pt.y);
    }
}