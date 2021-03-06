/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2017 International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

/*
 * FrequenciesByYearInternalFrame.java
 *
 * Created on 06 August 2008, 16:03
 */
package canreg.client.gui.analysis;

import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableModel;
import canreg.client.CanRegClientApp;
import canreg.client.DistributedTableDataSourceClient;
import canreg.client.LocalSettings;
import canreg.client.gui.CanRegClientView;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.server.database.UnknownTableException;
import java.awt.Cursor;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author ervikm
 */
public class FrequenciesByYearInternalFrame extends javax.swing.JInternalFrame implements ActionListener {

    private DistributedTableDescription tableDatadescription;
    private DistributedTableDataSourceClient tableDataSource;
    private DistributedTableModel tableDataModel;
    private final JDesktopPane dtp;
    private TableColumnModel tableColumnModel;
    private Set<DatabaseVariablesListElement> chosenVariables;
    private DistributedTableDescription tableDatadescriptionPopUp;
    private static final int MAX_ENTRIES_DISPLAYED_ON_RIGHT_CLICK = 20;
    private TableInternalFrame tableInternalFrame;
    private final Map<Integer, Dictionary> dictionary;
    private JFileChooser chooser = null;
    private final LocalSettings localSettings;

    /**
     * Creates new form FrequenciesByYearInternalFrame
     *
     * @param dtp
     */
    public FrequenciesByYearInternalFrame(JDesktopPane dtp) {
        this.dtp = dtp;
        dictionary = CanRegClientApp.getApplication().getDictionary();
        initComponents();
        initOtherComponents();

        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                close();
            }
        });
    }

    public void close() {
        tableInternalFrame.dispose();
        this.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rangeFilterPanel = new canreg.client.gui.components.RangeFilterPanel();
        variablesChooserPanel = new canreg.client.gui.components.VariablesChooserPanel();
        printTableButton = new javax.swing.JButton();
        resultPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        popOutTableButton = new javax.swing.JButton();
        saveTableButton = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(FrequenciesByYearInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N

        rangeFilterPanel.setName("rangeFilterPanel"); // NOI18N

        variablesChooserPanel.setName("variablesChooserPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(FrequenciesByYearInternalFrame.class, this);
        printTableButton.setAction(actionMap.get("printTableAction")); // NOI18N
        printTableButton.setName("printTableButton"); // NOI18N

        resultPanel.setName("resultPanel"); // NOI18N

        resultScrollPane.setName("resultScrollPane"); // NOI18N

        resultTable.setAutoCreateRowSorter(true);
        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        resultTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        resultTable.setName("resultTable"); // NOI18N
        resultScrollPane.setViewportView(resultTable);

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resultScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE)
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resultScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );

        popOutTableButton.setAction(actionMap.get("popOutTableAction")); // NOI18N
        popOutTableButton.setName("popOutTableButton"); // NOI18N

        saveTableButton.setAction(actionMap.get("saveTableAction")); // NOI18N
        saveTableButton.setToolTipText(resourceMap.getString("saveTableButton.toolTipText")); // NOI18N
        saveTableButton.setName("saveTableButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(rangeFilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 407, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(saveTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printTableButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(popOutTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(variablesChooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(variablesChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(printTableButton)
                            .addComponent(popOutTableButton)
                            .addComponent(saveTableButton)))
                    .addComponent(rangeFilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rangeFilterPanel.initValues();
        rangeFilterPanel.setDeskTopPane(dtp);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton popOutTableButton;
    private javax.swing.JButton printTableButton;
    private canreg.client.gui.components.RangeFilterPanel rangeFilterPanel;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JTable resultTable;
    private javax.swing.JButton saveTableButton;
    private canreg.client.gui.components.VariablesChooserPanel variablesChooserPanel;
    // End of variables declaration//GEN-END:variables

    /**
     *
     * @return
     */
    @Action
    public Task refresh() {
        // navigationPanel.goToTopAction();
        resultScrollPane.setVisible(false);
        return new RefreshTask(org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class));
    }

    private class RefreshTask extends org.jdesktop.application.Task<Object, Void> {

        DatabaseFilter filter = new DatabaseFilter();
        DistributedTableDescription newTableDatadescription = null;
        String tableName;

        RefreshTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to RefreshTask fields, here.
            super(app);
            rangeFilterPanel.setRefreshButtonEnabled(false);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);
            tableName = rangeFilterPanel.getSelectedTable();
            filter.setFilterString(rangeFilterPanel.getFilter().trim());
            filter.setRange(rangeFilterPanel.getRange());
            filter.setQueryType(DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR);
            chosenVariables = variablesChooserPanel.getSelectedVariables();
            filter.setDatabaseVariables(chosenVariables);
        }

        @Override
        protected Object doInBackground() {

            String result = "OK";
            try {
                newTableDatadescription = canreg.client.CanRegClientApp.getApplication().getDistributedTableDescription(filter, tableName);
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
            } catch (SQLException ex) {
                result = "Not valid";
            } catch (RemoteException ex) {
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Remote exception " + ex.getLocalizedMessage();
            } catch (SecurityException ex) {
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Security exception " + ex.getLocalizedMessage();
                // } catch (InterruptedException ignore) {
                //    result = "Ignore";
            } catch (DistributedTableDescriptionException ex) {
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not OK" + ex.getLocalizedMessage();
            } catch (UnknownTableException ex) {
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                result = "Not OK" + ex.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            String resultString = result.toString();
            boolean theResult = resultString.equals("OK");
            if (theResult) {

                // release old resultSet
                if (tableDatadescription != null) {
                    try {
                        CanRegClientApp.getApplication().releaseResultSet(tableDatadescription.getResultSetID());
                    } catch (SQLException ex) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException securityException) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, securityException);
                    } catch (RemoteException remoteException) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, remoteException);
                    }
                }
                tableDataSource = null;

                tableDatadescription = newTableDatadescription;

                Logger.getLogger(ExportReportInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());

                if (tableDatadescription != null) {
                    try {
                        tableDataSource = new DistributedTableDataSourceClient(tableDatadescription);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
                }

                if (tableDataSource != null) {
                    try {
                        tableDataModel = new DistributedTableModel(tableDataSource);
                        // tableDataModel = new PagingTableModel(tableDataSource);
                        resultTable.setModel(tableDataModel);
                        tableColumnModel = resultTable.getColumnModel();
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.INFO, "{0} free memory.", Runtime.getRuntime().freeMemory());
                        // setProgress(2, 0, 4);
                        resultTable.setColumnSelectionAllowed(false);
                        setProgress(4, 0, 4);
                        setMessage(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("FINISHED"));
                        resultTable.setVisible(true);
                        resultScrollPane.setVisible(true);
                        resultScrollPane.revalidate();
                        resultScrollPane.repaint();
                        resultPanel.revalidate();
                        resultPanel.repaint();
                        resultPanel.setVisible(true);
                    } catch (DistributedTableDescriptionException ex) {
                        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } else if (resultString.startsWith("Not valid")) {
                JOptionPane.showInternalMessageDialog(rootPane, java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("NOT_A_VALID_FILTER."), java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showInternalMessageDialog(rootPane, resultString, java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, resultString);
            }
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
            rangeFilterPanel.setRefreshButtonEnabled(true);
        }
    }

    private void initOtherComponents() {
        rangeFilterPanel.setDatabaseDescription(CanRegClientApp.getApplication().getDatabseDescription());
        rangeFilterPanel.setActionListener(this);
        // rangeFilterPanel.setTableChooserVisible(false);
        rangeFilterPanel.setRecordPanelvisible(false);
        rangeFilterPanel.setSortByVariableShown(false);
        rangeFilterPanel.setTablesToChooseFrom(Globals.DEFAULT_FREQUENCY_BY_YEAR_TABLE_CHOOSER_TABLE_LIST);
        rangeFilterPanel.setSelectedTable(Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME);
        resultScrollPane.setVisible(false);

        setTable(rangeFilterPanel.getSelectedTable());

        tableInternalFrame = new TableInternalFrame();

        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // rowClicked(evt);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                rowTableMousePressed(evt);
            }
        });
    }

    private void columnTableMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            JTable target = (JTable) evt.getSource();
            int columnNumber = target.getSelectedColumn();

            JPopupMenu jpm = new JPopupMenu("" + columnNumber);
            jpm.add(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("COLUMN ") + tableColumnModel.getColumn(tableColumnModel.getColumnIndexAtX(evt.getX())).getHeaderValue());
            jpm.show(target, evt.getX(), evt.getY());
        }
    }

    private void rowTableMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            showPopUpMenu(0, evt);
        }

    }

    /**
     *
     * @param offset
     * @param evt
     */
    public void showPopUpMenu(int offset, java.awt.event.MouseEvent evt) {
        JTable target = (JTable) evt.getSource();
        int rowNumber = target.rowAtPoint(new Point(evt.getX(), evt.getY()));
        rowNumber = target.convertRowIndexToModel(rowNumber);

        JPopupMenu jpm = new JPopupMenu();
        jpm.add(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("SHOW_IN_BROWSER"));
        TableModel tableModel = target.getModel();
        // resultTable.get
        // jpm.add("Column " + rowNumber +" " + tableColumnModel.getColumn(tableColumnModel.getColumnIndexAtX(evt.getX())).getHeaderValue());
        int year = Integer.parseInt((String) tableModel.getValueAt(rowNumber, 0));

        String filterString = "INCID >= '" + year * 10000 + "' AND INCID <'" + (year + 1) * 10000 + "'";

        for (DatabaseVariablesListElement dvle : chosenVariables) {
            int columnNumber = tableColumnModel.getColumnIndex(canreg.common.Tools.toUpperCaseStandardized(dvle.getDatabaseVariableName()));
            String value = tableModel.getValueAt(rowNumber, columnNumber).toString();
            filterString += " AND " + canreg.common.Tools.toUpperCaseStandardized(dvle.getDatabaseVariableName()) + " = " + dvle.getSQLqueryFormat(value);
        }
        DatabaseFilter filter = new DatabaseFilter();
        filter.setFilterString(filterString);
        Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.INFO, "FilterString: {0}", filterString);
        try {
            tableDatadescriptionPopUp = canreg.client.CanRegClientApp.getApplication().getDistributedTableDescription(filter, rangeFilterPanel.getSelectedTable());
            Object[][] rows = canreg.client.CanRegClientApp.getApplication().retrieveRows(tableDatadescriptionPopUp.getResultSetID(), 0, MAX_ENTRIES_DISPLAYED_ON_RIGHT_CLICK);
            String[] variableNames = tableDatadescriptionPopUp.getColumnNames();
            for (Object[] row : rows) {
                String line = "";
                int i = 0;
                for (Object obj : row) {
                    if (obj != null) {
                        line += variableNames[i] + ": " + obj.toString() + ", ";
                    }
                    i++;
                }
                jpm.add(line);
            }
        } catch (SQLException ex) {
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DistributedTableDescriptionException ex) {
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownTableException ex) {
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        int cases = (Integer) tableModel.getValueAt(rowNumber, tableColumnModel.getColumnIndex("CASES"));
        if (MAX_ENTRIES_DISPLAYED_ON_RIGHT_CLICK < cases) {
            jpm.add("...");
        }
        MenuItem menuItem = new MenuItem();

        jpm.show(target, evt.getX(), evt.getY());
    }

    private void setTable(String tableName) {
        variablesChooserPanel.setTableName(tableName);
        variablesChooserPanel.setVariablesInTable(rangeFilterPanel.getArrayOfVariablesInSelectedTables());
        variablesChooserPanel.initPanel(dictionary);
        variablesChooserPanel.showOnlyDataColumn();
        resultPanel.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("refresh".equalsIgnoreCase(e.getActionCommand())) {
            Task refreshTask = refresh();
            refreshTask.execute();
        } else if ("tableChanged".equalsIgnoreCase(e.getActionCommand())) {
            setTable(rangeFilterPanel.getSelectedTable());
        }
    }

    /**
     *
     */
    @Action
    public void printTableAction() {
        try {
            if (!resultTable.print(JTable.PrintMode.NORMAL,
                    new MessageFormat(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("CANREG_FREQUENCIES_BY_YEAR") + " - " + rangeFilterPanel.getFilter()),
                    null)) {
                System.err.println(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("USER CANCELLED PRINTING"));
            }
        } catch (java.awt.print.PrinterException e) {
            System.err.format(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/FrequenciesByYearInternalFrame").getString("CANNOT PRINT %S%N"), e.getMessage());
        }
    }

    @Action
    public void popOutTableAction() {
        resultScrollPane.setVisible(false);
        resultScrollPane = tableInternalFrame.setResultTable(resultTable);
        CanRegClientView.showAndPositionInternalFrame(dtp, tableInternalFrame);
        popOutTableButton.setEnabled(false);
    }

    @Action
    public void savePivotAction() {
        // createPivot("test");
    }

    public void createPivot(String fileName) {
        String[] columnNames = new String[resultTable.getColumnCount()];

        // We need 3 columns to work with        
        if (columnNames.length != 3) {
            return;
        }
        // TODO Extend this to at least 4
        String[] nextLine = new String[resultTable.getColumnCount()];

        // Find the column names
        for (int j = 0; j < columnNames.length; j++) {
            columnNames[j] = resultTable.getColumnName(j);
        }

        // variable we lock
        String lockVariable = columnNames[1];

        HashMap<String, HashMap> data = new HashMap<String, HashMap>();
        HashMap<String, String> years;
        Set<String> allYears = new TreeSet<String>();

        // load up the data
        for (int i = 0; i < resultTable.getRowCount(); i++) {
            String year, cases, code;

            for (int j = 0; j < nextLine.length; j++) {
                nextLine[j] = resultTable.getValueAt(i, j).toString();
            }
            year = nextLine[0];
            if (year.trim().length() == 0) {
                year = "MISSING";
            }

            allYears.add(year);

            code = nextLine[1];

            if (code.trim().length() == 0) {
                code = "MISSING";
            }

            cases = nextLine[2];

            years = data.get(code);
            if (years == null) {
                years = new HashMap<String, String>();
                data.put(code, years);
            }
            years.put(year, cases);
        }

        // find variables element for the lockedvariable
        DatabaseVariablesListElement lockedDatabaseVariablesListElement = null;
        for (DatabaseVariablesListElement vle : chosenVariables) {
            if (vle.getShortName().compareToIgnoreCase(lockVariable) == 0) {
                lockedDatabaseVariablesListElement = vle;
            }
        }

        if (lockedDatabaseVariablesListElement == null) {
            return;
        } // this happens if user has updated the selection of variables

        int dictionaryID = lockedDatabaseVariablesListElement.getDictionaryID();
        Dictionary dict = null;
        if (dictionaryID >= 0) {
            dict = dictionary.get(dictionaryID);
        }

        String[] allYearsArray = allYears.toArray(new String[0]);
        Writer writer = null;
        try {
            writer = new FileWriter(fileName);

            // Write the column names
            String[] codeArray = {lockedDatabaseVariablesListElement.getFullName(), lockVariable};

            String[] headers = (String[]) ArrayUtils.addAll(codeArray, allYearsArray);

            String[] codes = data.keySet().toArray(new String[0]);
            Arrays.sort(codes);

            CSVFormat format = CSVFormat.DEFAULT
                    .withDelimiter(',')
                    .withHeader(headers);

            CSVPrinter csvPrinter = new CSVPrinter(writer, format);

            // write the rows    
            for (String code : codes) {
                LinkedList<String> row = new LinkedList<String>();
                if (dict != null) {
                    DictionaryEntry dictionaryEntry = dict.getDictionaryEntry(code);
                    if (dictionaryEntry != null) {
                        row.add(dictionaryEntry.getDescription());
                    } else {
                        row.add("");
                    }
                } else {
                    row.add("");
                }
                row.add(code);

                years = data.get(code);
                for (String year : allYears) {
                    String cell = years.get(year);
                    if (cell == null) {
                        row.add("0");
                    } else {
                        row.add(cell);
                    }
                }
                csvPrinter.printRecord(row);
            }
            csvPrinter.flush();

        } catch (IOException ex) {
            // JOptionPane.showMessageDialog(this, "File NOT written.\n" + ex.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Action
    public void saveTableAction() {
        LinkedList<String> filesCreated = new LinkedList<String>();
        if (!resultTable.isVisible()) {
            refresh();
        }
        resultScrollPane.setVisible(false);
        Writer writer = null;
        try {
            String fileName = null;
            String pivotFileName = null;
            if (chooser == null) {
                String path = localSettings.getProperty(LocalSettings.TABLES_PATH_KEY);
                if (path == null) {
                    chooser = new JFileChooser();
                } else {
                    chooser = new JFileChooser(path);
                }
            }
            int returnVal = chooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    localSettings.setProperty(LocalSettings.TABLES_PATH_KEY, chooser.getSelectedFile().getParentFile().getCanonicalPath());
                    fileName = chooser.getSelectedFile().getAbsolutePath();
                    pivotFileName = fileName + "-pivot.csv";
                    // we force the .csv ending to the file
                    if (!(fileName.endsWith(".csv") || fileName.endsWith(".CSV"))) {
                        fileName += ".csv";
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                // cancelled
                return;
            }
            writer = new FileWriter(fileName);
            // CSVWriter csvwriter = new CSVWriter(writer, ',');
            String[] headers = new String[resultTable.getColumnCount()];

            // Write the column names
            for (int j = 0; j < headers.length; j++) {
                headers[j] = resultTable.getColumnName(j);
            }
            CSVFormat format = CSVFormat.DEFAULT
                    .withDelimiter(',')
                    .withHeader(headers);
            
            CSVPrinter csvPrinter = new CSVPrinter(writer, format);

            Object[] nextLine = new String[resultTable.getColumnCount()];
            
            // write the rows
            for (int i = 0; i < resultTable.getRowCount(); i++) {
                for (int j = 0; j < nextLine.length; j++) {
                    nextLine[j] = resultTable.getValueAt(i, j).toString();
                }
                csvPrinter.printRecord(nextLine);
            }
            csvPrinter.flush();
            csvPrinter.close();
            
            // We need 3 columns to work with        
            if (headers.length == 3) {
                createPivot(pivotFileName);
                filesCreated.add(pivotFileName);
                JOptionPane.showMessageDialog(this, "Table written to file: "+fileName+"\nPivot table written to:" + pivotFileName, "OK", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, "Table written to file: "+fileName, "OK", JOptionPane.INFORMATION_MESSAGE);
            }
            filesCreated.add(fileName);
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File NOT written.\n" + ex.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                for(String fn:filesCreated) {
                    Tools.openFile(fn);
                }
            } catch (IOException ex) {
                Logger.getLogger(FrequenciesByYearInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            resultScrollPane.setVisible(true);
        }
    }
}
