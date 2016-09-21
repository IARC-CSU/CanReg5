/*
 * Copyright (C) 2016 Patricio Carranza
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
 */
package canreg.client.gui.dataentry2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author Patricio
 * @version 1.00.000
 * last modification: 05/08/2016
 */
public class CustomTabbedPane extends JTabbedPane {
    
    private final JTabbedPane pane = this;
    private int lastTabIndex = 0;
    
    @Override
    public void addTab(String title, Component component) {
        super.addTab(title, component);
        this.setTabComponentAt(lastTabIndex++, new SingleTabComponent(title));
    }
    
    /**
     * Every tab is a custom panel so we can include the "x" button.
     */
    private class SingleTabComponent extends JPanel {        
        public SingleTabComponent(String title) {
            //unset default FlowLayout' gaps
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            this.setOpaque(false);
                        
            JLabel label = new JLabel(title);
            //add more space between the label and the button
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            this.add(label);
            
            //The entire tab itself is a button.
            JButton button = new TabButton();
            this.add(button);
            
            //add more space to the top of the component
            this.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        }
        
        private final MouseListener buttonMouseListener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBorderPainted(true);
                }
            }

            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBorderPainted(false);
                }
            }
        };

        /**
         * The "x" button inside each tab.
         */
        private class TabButton extends JButton implements ActionListener {

            private final int size = 17;

            public TabButton() {            
                this.setPreferredSize(new Dimension(size, size));
                //setToolTipText("close this tab");
                //Make the button looks the same for all Laf's
                this.setUI(new BasicButtonUI());
                //Make it transparent
                this.setContentAreaFilled(false);
                //No need to be focusable
                this.setFocusable(false);
                this.setBorder(BorderFactory.createEtchedBorder());
                this.setBorderPainted(false);
                //Making nice rollover effect
                //we use the same listener for all buttons
                this.addMouseListener(buttonMouseListener);
                this.setRolloverEnabled(true);
                //Close the proper tab by clicking the button
                this.addActionListener(this);
            }

            public void actionPerformed(ActionEvent e) {
                int i = pane.indexOfTabComponent(SingleTabComponent.this);
                if (i != -1) {
                    pane.remove(i);
                }
            }

            //we don't want to update UI for this button
            public void updateUI() {}

            //paint the cross
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                //shift the image for pressed buttons
                if (getModel().isPressed()) {
                    g2.translate(1, 1);
                }
                
                g2.setColor(Color.BLACK);
                if (getModel().isRollover()) {
                    g2.setColor(Color.RED);
                }
                int delta = 5;
                //coordinates for the points x1, y1, x2, y2. Draws a line that joins those points.
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(delta, delta, getWidth() - delta, getHeight() - delta);
                //we change the stroke because otherwise
                //SOME reason it's not drawn with the same thickness (java BUG)
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(getWidth() - delta, delta, delta, getHeight() - delta);
                g2.dispose();
            }
        }          
    }
}
