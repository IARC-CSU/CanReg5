/*
 * DictionaryElementChooser.java
 *
 * Created on 16-Nov-2010, 15:08:21
 */
package canreg.client.gui.components;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import canreg.client.dataentry.DictionaryHelper;
import canreg.server.database.Dictionary;
import canreg.server.database.DictionaryEntry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
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
    public static String OK_ACTION = "DICTIONARY_CHOSEN";

    /** Creates new form DictionaryElementChooser */
    public DictionaryElementChooser(ActionListener listener) {
        this.listener = listener;
        initComponents();
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    public DictionaryEntry getSelectedElement() {
        if (jList1.getSelectedValue() != null) {
            return (DictionaryEntry) jList1.getSelectedValue();
        } else {
            return null;
        }
    }

    public void setSelectedElement(DictionaryEntry ddle) {
        oldElement = ddle;
        if (ddle != null && dictionary.isCompoundDictionary() && firstPass) {
            jList1.setSelectedValue(
                    dictionary.getDictionaryEntries().
                    get(ddle.getCode().substring(0, dictionary.getCodeLength())), true);
        } else {
            jList1.setSelectedValue(ddle, true);
        }
    }

    public void setDictionary(Dictionary dictionary) {
        DictionaryEntry selected = (DictionaryEntry) jList1.getSelectedValue();
        this.dictionary = dictionary;
        setTitle(dictionary.getName());
        DictionaryEntry tempentry;
        TreeSet<DictionaryEntry> possibleValuesCollection = new TreeSet<DictionaryEntry>();
        // todo: populate list with possible values
        if (dictionary.isCompoundDictionary() && firstPass) {
            possibleValuesCollection = new TreeSet<DictionaryEntry>();
            Iterator<String> it = dictionary.getDictionaryEntries().keySet().iterator();
            while (it.hasNext()) {
                tempentry = dictionary.getDictionaryEntries().get(it.next());
                if (tempentry.getCode().length() < dictionary.getFullDictionaryCodeLength()) {
                    possibleValuesCollection.add(tempentry);
                }
            }
        } else if (dictionary.isCompoundDictionary() && !firstPass) {
            String value = getSelectedElement().getCode();
            possibleValuesCollection.addAll(
                    Arrays.asList(
                    DictionaryHelper.getDictionaryEntriesStartingWith(
                    value, dictionary.getDictionaryEntries().values().toArray(new DictionaryEntry[0]))));
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
        jList1.setModel(eventListModel);
        jList1.setSelectedValue(selected, true);
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
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        filterEdit = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(DictionaryElementChooser.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(DictionaryElementChooser.class, this);
        jButton3.setAction(actionMap.get("sortByDescriptionSelected")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        jButton4.setAction(actionMap.get("sortByCodeSelected")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        filterEdit.setText(resourceMap.getString("filterEdit.text")); // NOI18N
        filterEdit.setName("filterEdit"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(jButton4)
                .addComponent(jButton3)
                .addComponent(jLabel2)
                .addComponent(filterEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setName("jList1"); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jButton1.setAction(actionMap.get("okAction")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("cancelAction")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(217, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            okAction();
        }
    }//GEN-LAST:event_jList1MouseClicked

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
        setDictionary(dictionary);
    }

    @Action
    public void sortByDescriptionSelected() {
        for (DictionaryEntry de : dictionary.getDictionaryEntries().values()) {
            de.setSortByDescription();
        }
        setDictionary(dictionary);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField filterEdit;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
