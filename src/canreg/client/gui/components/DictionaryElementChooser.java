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

/*
 * DictionaryElementChooser.java
 *
 * Created on 16-Nov-2010, 15:08:21
 */
package canreg.client.gui.components;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import canreg.client.dataentry.DictionaryHelper;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DictionaryElementChooser extends javax.swing.JInternalFrame {

    private ActionListener listener;
    protected Dictionary dictionary = null;
    boolean firstPass = true;
    private DictionaryEntry oldElement;
    private String categoryCode = null;
    public static String OK_ACTION = "DICTIONARY_CHOSEN";
    private javax.swing.Action upAction;
    private javax.swing.Action downAction;

    /** Creates new form DictionaryElementChooser */
    public DictionaryElementChooser(ActionListener listener) {
        this.listener = listener;
        initComponents();

        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke pgup = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
        KeyStroke pgdown = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);

        upAction = new UpAction("Go up", null,
                "",
                new Integer(KeyEvent.VK_UP));
        downAction = new DownAction("Go down", null,
                "",
                new Integer(KeyEvent.VK_DOWN));
        
        /* Not working yet, so we remove it...
        this.registerKeyboardAction(upAction, "lineup", up,
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.registerKeyboardAction(downAction, "linedown", down,
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         */
        // filterEdit.requestFocusInWindow();

        descriptionButton.setSelected(true);
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    public DictionaryEntry getSelectedElement() {
        if (dictionaryEntryList.getSelectedValue() != null) {
            return (DictionaryEntry) dictionaryEntryList.getSelectedValue();
        } else {
            return null;
        }
    }

    public void setSelectedElement(DictionaryEntry ddle) {
        oldElement = ddle;
        if (ddle != null && dictionary.isCompoundDictionary() && firstPass) {
            dictionaryEntryList.setSelectedValue(
                    dictionary.getDictionaryEntries().
                    get(ddle.getCode().substring(0, dictionary.getCodeLength())), true);
        } else {
            dictionaryEntryList.setSelectedValue(ddle, true);
        }
    }

    public void setFirstPass() {
        firstPass = true;
    }

    public void setDictionary(Dictionary dictionary) {
        DictionaryEntry selected = (DictionaryEntry) dictionaryEntryList.getSelectedValue();
        this.dictionary = dictionary;
        setTitle(dictionary.getName());
        DictionaryEntry tempentry;
        TreeSet<DictionaryEntry> possibleValuesCollection = new TreeSet<DictionaryEntry>();
        // todo: populate list with possible values
        if (dictionary.isCompoundDictionary() && firstPass) {
            Iterator<String> it = dictionary.getDictionaryEntries().keySet().iterator();
            while (it.hasNext()) {
                tempentry = dictionary.getDictionaryEntries().get(it.next());
                if (tempentry.getCode().length() < dictionary.getFullDictionaryCodeLength()) {
                    possibleValuesCollection.add(tempentry);
                }
            }
        } else if (dictionary.isCompoundDictionary() && !firstPass) {
            if (getSelectedElement() != null) {
                categoryCode = getSelectedElement().getCode();
            }
            if (categoryCode != null) {
                possibleValuesCollection.addAll(
                        Arrays.asList(
                        DictionaryHelper.getDictionaryEntriesStartingWith(
                        categoryCode, dictionary.getDictionaryEntries().values().toArray(new DictionaryEntry[0]))));

            } else // we're on the second pass with no starts with selected, so we show all codes
            {
                Iterator<String> it = dictionary.getDictionaryEntries().keySet().iterator();
                while (it.hasNext()) {
                    tempentry = dictionary.getDictionaryEntries().get(it.next());
                    if (tempentry.getCode().length() == dictionary.getFullDictionaryCodeLength()) {
                        possibleValuesCollection.add(tempentry);
                    }
                }
            }
            if (selected != null && oldElement != null
                    && oldElement.getCode().startsWith(selected.getCode())) {
                selected = oldElement;
            }
        } else {
            possibleValuesCollection.addAll(dictionary.getDictionaryEntries().values());
            selected = oldElement;
        }

        EventList<DictionaryEntry> possibleValuesEventList = new BasicEventList<DictionaryEntry>();
        possibleValuesEventList.addAll(possibleValuesCollection);
        MatcherEditor<DictionaryEntry> textMatcherEditor = new TextComponentMatcherEditor<DictionaryEntry>(filterEdit, new DictionaryElementTextFilterator());
        FilterList<DictionaryEntry> textFilteredDictioaryEntries = new FilterList<DictionaryEntry>(possibleValuesEventList, textMatcherEditor);
        EventListModel<DictionaryEntry> eventListModel = new EventListModel<DictionaryEntry>(textFilteredDictioaryEntries);
        dictionaryEntryList.setModel(eventListModel);
        dictionaryEntryList.setSelectedValue(selected, true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        sortByLabel = new javax.swing.JLabel();
        filterLabel = new javax.swing.JLabel();
        filterEdit = new javax.swing.JTextField();
        descriptionButton = new javax.swing.JButton();
        codeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        dictionaryEntryList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(DictionaryElementChooser.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        sortByLabel.setText(resourceMap.getString("sortByLabel.text")); // NOI18N
        sortByLabel.setName("sortByLabel"); // NOI18N

        filterLabel.setText(resourceMap.getString("filterLabel.text")); // NOI18N
        filterLabel.setName("filterLabel"); // NOI18N

        filterEdit.setText(resourceMap.getString("filterEdit.text")); // NOI18N
        filterEdit.setName("filterEdit"); // NOI18N
        filterEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                filterEditMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                filterEditMouseReleased(evt);
            }
        });
        filterEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterEditKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterEditKeyTyped(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(DictionaryElementChooser.class, this);
        descriptionButton.setAction(actionMap.get("sortByDescriptionSelected")); // NOI18N
        descriptionButton.setName("descriptionButton"); // NOI18N

        codeButton.setAction(actionMap.get("sortByCodeSelected")); // NOI18N
        codeButton.setName("codeButton"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sortByLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionButton))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(filterEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(descriptionButton)
                .addComponent(codeButton)
                .addComponent(sortByLabel)
                .addComponent(filterLabel))
        );

        filterEdit.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "downAction");
        filterEdit.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upAction");

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        dictionaryEntryList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        dictionaryEntryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dictionaryEntryList.setName("dictionaryEntryList"); // NOI18N
        dictionaryEntryList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dictionaryEntryListMouseClicked(evt);
            }
        });
        dictionaryEntryList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dictionaryEntryListKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(dictionaryEntryList);

        jButton1.setAction(actionMap.get("okAction")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("cancelAction")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dictionaryEntryListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dictionaryEntryListMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            okAction();
        }
    }//GEN-LAST:event_dictionaryEntryListMouseClicked

    private void filterEditKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterEditKeyTyped
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            enterKeyPressed();
        } else if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ESCAPE) {
            escapeKeyPressed();
        }
    }//GEN-LAST:event_filterEditKeyTyped

    private void filterEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterEditKeyPressed
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_DOWN) {
        } else if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_UP) {
            int position = dictionaryEntryList.getFirstVisibleIndex();
            if (position > 0) {
                dictionaryEntryList.setSelectedIndex(position - 1);
            } else {
                dictionaryEntryList.setSelectedIndex(dictionaryEntryList.getMaxSelectionIndex());
            }
        }
    }//GEN-LAST:event_filterEditKeyPressed

    private void dictionaryEntryListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dictionaryEntryListKeyPressed
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            enterKeyPressed();
        } else if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ESCAPE) {
            escapeKeyPressed();
        }
    }//GEN-LAST:event_dictionaryEntryListKeyPressed

    private void filterEditMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterEditMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(filterEdit, evt);
    }//GEN-LAST:event_filterEditMousePressed

    private void filterEditMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterEditMouseReleased
       MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(filterEdit, evt);
    }//GEN-LAST:event_filterEditMouseReleased

    private void enterKeyPressed() {
        if (!dictionaryEntryList.isSelectionEmpty()) {
            okAction();
        } else {
            int position = dictionaryEntryList.getFirstVisibleIndex();
            if (position > -1) {
                dictionaryEntryList.setSelectedIndex(position);
                okAction();
            }
        }
    }

    @Action
    public void okAction() {
        // TODO: check compound
        String value = getSelectedElement().getCode();
        // if compoud change list and redo once
        if (dictionary.isCompoundDictionary() && value.length() < dictionary.getFullDictionaryCodeLength()) {
            // change the list
            firstPass = false;
            setDictionary(dictionary);
        } else {
            //send ok
            listener.actionPerformed(new ActionEvent(this, 0, OK_ACTION));
            //then dispose
            firstPass = true;
            this.dispose();
        }
    }

    @Action
    public void cancelAction() {
        firstPass = true;
        this.dispose();
    }

    @Action
    public void sortByCodeSelected() {
        for (DictionaryEntry de : dictionary.getDictionaryEntries().values()) {
            de.setSortByCode();
        }
        codeButton.setSelected(true);
        descriptionButton.setSelected(false);
        setDictionary(dictionary);
    }

    @Action
    public void sortByDescriptionSelected() {
        for (DictionaryEntry de : dictionary.getDictionaryEntries().values()) {
            de.setSortByDescription();
        }
        codeButton.setSelected(false);
        descriptionButton.setSelected(true);
        setDictionary(dictionary);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton codeButton;
    private javax.swing.JButton descriptionButton;
    private javax.swing.JList dictionaryEntryList;
    private javax.swing.JTextField filterEdit;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel sortByLabel;
    // End of variables declaration//GEN-END:variables

    private void escapeKeyPressed() {
        if (firstPass) {
            this.dispose();
        } else {
            setFirstPass();
            setDictionary(dictionary);
        }
    }

    void clearFilter() {
        filterEdit.setText("");
    }

    private class DownAction extends AbstractAction {

        public DownAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Down!");
            int position = dictionaryEntryList.getSelectedIndex();
            if (position < 0) {
                dictionaryEntryList.getFirstVisibleIndex();
            }
            if (position > -1 && position < dictionaryEntryList.getMaxSelectionIndex() - 1) {
                dictionaryEntryList.setSelectedIndex(position + 1);
            } else {
                dictionaryEntryList.setSelectedIndex(0);
            }
        }
    }

    private class UpAction extends AbstractAction {

        public UpAction(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Up!");
            int position = dictionaryEntryList.getSelectedIndex();
            if (position < 0) {
                dictionaryEntryList.getLastVisibleIndex();
            }
            if (position > 0) {
                dictionaryEntryList.setSelectedIndex(position - 1);
            } else {
                dictionaryEntryList.setSelectedIndex(dictionaryEntryList.getMaxSelectionIndex());
            }
        }
    }
}
