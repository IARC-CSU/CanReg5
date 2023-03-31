package canreg.client.gui.dataentry2;

import canreg.common.Globals;
import canreg.common.database.DatabaseRecord;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.application.Action;

/**
 * A simple JDialog window to select a Tumour from a list of Tumours.
 * It can exclude one tumour using its TumourID
 * @author c_chen
 */
public class RecordEditorSourceSelectorInternalFrame extends javax.swing.JDialog {
    
    String selectedValue;
    String validatedValue;
    HashMap<Integer, String> mapTumourNumberDisplayed_tumourId;

    // enum of the dictionaries used in this class
    enum Dictionaries {
        behavior(5),
        morphology(4),
        topography(3);

        private final int dictionaryId;

        Dictionaries(int id) {
            this.dictionaryId = id;
        }
        public int getDictId(){
            return this.dictionaryId;
        }
    }

    /**
     * Creates new customizer SourceSelector
     * @param tumours LinkedList< RecordEditorTumour >
     * @param tumourIdToIgnore tumourId to not display in the dialog
     */
    public RecordEditorSourceSelectorInternalFrame(LinkedList<RecordEditorTumour> tumours, String tumourIdToIgnore) {
        mapTumourNumberDisplayed_tumourId = new HashMap();
        this.validatedValue = null;
        this.selectedValue = "none";
        ListSelectionModel listSelectionModel;
        initComponents();
        label1.setText("Select the tumour where to move the source");
        jButton1.setText("Ok");
        
        ArrayList<Object[]> tumourRowList = new ArrayList<>();
        // read and store data from the tumours passed in args
        for (RecordEditorTumour tumour: tumours) {
            DatabaseRecord databaseRecord = tumour.getDatabaseRecord();
            String tumourRecordNumber = databaseRecord.getVariableAsString(String.valueOf(Globals.StandardVariableNames.TumourID));
            if (tumourRecordNumber.equals(tumourIdToIgnore)) continue;
            String tumorTableId = databaseRecord.getVariableAsString(String.valueOf(Globals.StandardVariableNames.PatientRecordIDTumourTable));
            // Globals.StandardVariableNames.IncidenceDate doesn't exist in the database record
            String incidDate = databaseRecord.getVariableAsString("incid");

            String behaviorDictionaryEntry = databaseRecord.getVariableAsString("beh");
            String morphologyDictionaryEntry = databaseRecord.getVariableAsString("mor");
            String topographyDictionaryEntry = databaseRecord.getVariableAsString("top");

            String behaviorStringValue = (behaviorDictionaryEntry.equals("")) ? "" : tumour.getDictionary().get(Dictionaries.behavior.getDictId()).getDictionaryEntry(behaviorDictionaryEntry).toString();
            String morphologyStringValue = (morphologyDictionaryEntry.equals("")) ? "" : tumour.getDictionary().get(Dictionaries.morphology.getDictId()).getDictionaryEntry(morphologyDictionaryEntry).toString();
            String topographyStringValue = (topographyDictionaryEntry.equals("")) ? "" : tumour.getDictionary().get(Dictionaries.topography.getDictId()).getDictionaryEntry(topographyDictionaryEntry).toString();

            if (tumourRecordNumber.length() != 0) {
                int tumourNumber = Integer.parseInt(tumourRecordNumber.substring(tumorTableId.length()));
                Object[] tumourRow = {"Tumour "+tumourNumber, incidDate, behaviorStringValue, morphologyStringValue, topographyStringValue};
                tumourRowList.add(tumourRow);
                mapTumourNumberDisplayed_tumourId.put(tumourNumber, tumourRecordNumber);
            }
        }

        // initialize the selection table
        String[] titles = {"Tumour number", "Incidence date", "Behavior", "Morphology", "Topography"};
        jTable1 = new JTable(tumourRowList.toArray(new Object[0][]), titles) {
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    tip = getValueAt(rowIndex, colIndex).toString();
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };

        jTable1.setFont(new Font("Segoe UI", 0, 18));
        jTable1.setRowHeight(30);
        jScrollPane1.setViewportView(jTable1);
        jPanel1.add(jScrollPane1);
        listSelectionModel = jTable1.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
        setVisible(true);
    }

    /**
     * ListSelectionListener to keep track of which Tumour has been selected
     */
    class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) { 
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();

            if (lsm.isSelectionEmpty()) {
                selectedValue = "none";
            } else {
                // Find out which index is selected
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        int ii = jTable1.getSelectedRow();
                        selectedValue = jTable1.getModel().getValueAt(ii,0)
                                .toString().substring("Tumour ".length());
                    }
                }
            }
        }
    }

    /** returns the selected value
     * returns "none" if no value was selected
     * @return id of the selected tumour
     */
    public String getValidatedTumourId() {
        return this.validatedValue;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label1 = new java.awt.Label();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select target tumour");
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(1400, 300));

        label1.setAlignment(java.awt.Label.CENTER);
        label1.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        label1.setText("label1");
        getContentPane().add(label1, java.awt.BorderLayout.PAGE_START);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(RecordEditorSourceSelectorInternalFrame.class, this);
        jButton1.setAction(actionMap.get("validateTumour")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(30, 30));
        getContentPane().add(jButton1, java.awt.BorderLayout.PAGE_END);

        jPanel1.setPreferredSize(new java.awt.Dimension(1600, 190));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(1300, 150));

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 24));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Tumour number", "Incidence date", "Topography", "Morphology", "Behaviour "
            }
        ));
        jTable1.setMaximumSize(new java.awt.Dimension(50, 80));
        jTable1.setPreferredSize(new java.awt.Dimension(600, 100));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void validateTumour() {
        if (this.selectedValue.equals("none")) return;
        this.validatedValue = mapTumourNumberDisplayed_tumourId.get(Integer.valueOf(this.selectedValue));
        this.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private java.awt.Label label1;
    // End of variables declaration//GEN-END:variables
}
