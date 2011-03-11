package other.postscriptviewer;

/*
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */


import java.io.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import other.postscriptviewer.interpreter.*;


/**
 * Render postscript files with the Java 2D(TM) api.  This demo is not meant 
 * to be a fully compliant postscript interpreter.  The demo works with the 
 * provided postscript files and simple postscript files like the examples 
 * from "Postscript By Example".  Most of the language control operators are 
 * implemented and about 20% of the graphic operators are implemented.
 *
 * @version @(#)PostscriptViewer.java	1.3	98/12/17
 * @author Uwe Hoffmann
 */
public class PostscriptViewer extends JApplet implements ActionListener, Runnable {

    static String postscriptFiles[] = { 
            "parrot.ps", "tiger.ps", "golfer.ps", "nozzle.ps", 
            "columbia.ps", "butterfly.ps", 
    };
    static JComboBox combo;
    static URL url;
    JPanel p1;
    Demo demo;


    public void init() {
        p1 = new JPanel(new BorderLayout());
        EmptyBorder eb = new EmptyBorder(5,20,10,20);
        p1.setBorder(eb);
        p1.setBackground(Color.gray);
        combo = new JComboBox();
        combo.setLightWeightPopupEnabled(false);
        for (int i = 0; i < postscriptFiles.length; i++) {
            combo.addItem(postscriptFiles[i]);
        }
        combo.addActionListener(this);
        p1.add("West", combo);
        p1.setToolTipText("click to start/stop iterating");
        p1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (demo.thread == null) demo.start(); else demo.stop();
            }
        });
        getContentPane().add("South", p1);

        JPanel p2 = new JPanel(new BorderLayout());
        eb = new EmptyBorder(20,20,5,20);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        p2.setBorder(new CompoundBorder(eb,bb));
        p2.setBackground(Color.gray);
        p2.add(demo = new Demo());

        getContentPane().add("Center", p2);

        url = PostscriptViewer.class.getResource("psfiles/parrot.ps");

    }


    public void addOpenButton() {
        JButton b = new JButton("Open...");
        b.addActionListener(this);
        p1.add("East", b);
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JFileChooser chooser = new JFileChooser();
            File file = new File("psfiles");
            if (file.exists()) {
                chooser.setCurrentDirectory(file);
            }
            if (chooser.showOpenDialog(this) == 0) {
                File theFile = chooser.getSelectedFile();
                if (theFile != null) {
                    try {
                        url = theFile.toURL();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        } else {
            String str = (String) PostscriptViewer.combo.getSelectedItem();
            url = Demo.class.getResource("psfiles/" + str);
        }
        new Thread(this).start();
    }


    public void run() {
        demo.repaint();
    }



    static class Demo extends Canvas implements Runnable {

        Thread thread; 

        public Demo() {
            setBackground(Color.white);
        }
    

        public void paint(Graphics g) {
            Dimension d = getSize();
            Graphics2D g2 = (Graphics2D) g;
            double s = Math.min(d.width, d.height) / 800.0;
            PAContext context = new PAContext(g2, d);
            if (s != 1.0){
                AffineTransform fitInPage = new AffineTransform();
                fitInPage.scale(s,s);
                g2.transform(fitInPage);
            }
            try {
                InputStream inputStream = url.openStream();
                context.draw(inputStream);	
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void start() {
            if (thread != null) {
                return;
            }
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    
    
        public synchronized void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }
    
    
        public void run() {
            Thread me = Thread.currentThread();
            while (thread == me) {
                for (int i = 0; i < postscriptFiles.length; i++) {
                    combo.setSelectedIndex(i);
                    try {
                        thread.sleep(6000);
                    } catch (InterruptedException e) { return; }
                }
            }
            thread = null;
        } 
    } // End Demo class


    public static void main(String s[]) {
	final PostscriptViewer demo = new PostscriptViewer();
        demo.init();
        demo.addOpenButton();
	WindowListener l = new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}
	};
	Frame f = new Frame("Postscript Viewer Demo");
	f.addWindowListener(l);
	f.add("Center", demo);
	f.pack();
        int w = 390; 
        int h = 440;
	f.setSize(new Dimension(w, h));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
	f.show();
    }
}
