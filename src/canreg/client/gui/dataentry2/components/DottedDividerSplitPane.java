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
 * @author Patricio Ezequiel Carranza, patocarranza@gmail.com
 */
package canreg.client.gui.dataentry2.components;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Custom painter for a JSplirPane divider. Paints the divider as a set of small
 * circles instead of being a straight line. You can use this painter like this:
 *      jSplitPane.setUI(new DottedDividerSplitPane());
 * And set the border of the splitPane AFTER setting the UI. For example:
 *      jSplitPane.setUI(new DottedDividerSplitPane());
 *      jSplitPane.setBorder(null);
 * @author patri_000
 */
public class DottedDividerSplitPane extends BasicSplitPaneUI {
    
    
    public DottedDividerSplitPane() {
    }
    
       
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        
        return new BasicSplitPaneDivider(this) {
            
            @Override
            public void setBorder(Border b) {
            }

            @Override
            public void paint(Graphics g) {                
                g.setColor(Color.DARK_GRAY);
                int startOval = 0;
                final int ovalSize = divider.getDividerSize() % 2 == 0 ? 
                        divider.getDividerSize() / 2 : 
                        divider.getDividerSize() / 2 + 1;
                
                if(getOrientation() == javax.swing.JSplitPane.HORIZONTAL_SPLIT) {
                    while((startOval+ovalSize) < getSize().height) {
                        g.fillOval(1, startOval, ovalSize, ovalSize);
                        startOval += divider.getDividerSize();
                    }
                } else if(getOrientation() == javax.swing.JSplitPane.VERTICAL_SPLIT) {
                    while((startOval+ovalSize) < getSize().width) {
                        g.fillOval(startOval, 1, ovalSize, ovalSize);
                        startOval += divider.getDividerSize();
                    }
                }
                
                super.paint(g);
            }
        };
    }
}

