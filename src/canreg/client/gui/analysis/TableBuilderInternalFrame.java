/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TableBuilderInternalFrame.java
 *
 * Created on 28-Apr-2009, 16:33:32
 */
package canreg.client.gui.analysis;

import cachingtableapi.DistributedTableDescription;
import canreg.client.CanRegClientApp;
import canreg.client.DistributedTableDataSourceClient;
import canreg.client.LocalSettings;
import canreg.client.analysis.AgeSpecificCasesPerHundredThousandTableBuilder;
import canreg.client.analysis.AgeSpecificCasesTableBuilder;
import canreg.client.analysis.ConfigFields;
import canreg.client.analysis.ConfigFieldsReader;
import canreg.client.analysis.NotCompatibleDataException;
import canreg.client.analysis.PopulationPyramidTableBuilder;
import canreg.client.analysis.TableBuilder;
import canreg.client.analysis.TableBuilderListElement;
import canreg.client.gui.components.LabelAndComboBoxJPanel;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.server.database.PopulationDataset;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class TableBuilderInternalFrame extends javax.swing.JInternalFrame {

    private Map<Integer, PopulationDataset> populationDatasetsMap;
    private PopulationDataset[] populationDatasetsArray;
    private LinkedList<LabelAndComboBoxJPanel> populationDatasetChooserPanels;
    private LocalSettings localSettings;
    private String path;
    private JFileChooser chooser;

    /** Creates new form TableBuilderInternalFrame */
    public TableBuilderInternalFrame() {
        initComponents();
        initData();

        localSettings = CanRegClientApp.getApplication().getLocalSettings();
        path = localSettings.getProperty("tables_path");

        if (path == null) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(path);
        }

        // Add a listener for changing the active tab
        ChangeListener tabbedPaneChangeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                // initializeVariableMappingTab();
                changeTab(tabbedPane.getSelectedIndex());
            }
        };
        // And add the listener to the tabbedPane

        tabbedPane.addChangeListener(tabbedPaneChangeListener);
        changeTab(0);

        // disable filter tab
        Component filterTab = tabbedPane.getComponents()[3];
        tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(filterTab), false);
    }

    private String generateHeadingString() {
        String heading;
        int startYear = startYearChooser.getValue();
        int endYear = endYearChooser.getValue();
        heading = canreg.client.CanRegClientApp.getApplication().getSystemName() + " (" + startYear;
        if (endYear != startYear) {
            heading += "-" + endYear;
        }
        heading += ")";
        return heading;
    }

    protected void changeTab(int tabNumber) {
        tabbedPane.setSelectedIndex(tabNumber);
        nextButton.setEnabled(tabNumber < tabbedPane.getTabCount() - 1);
        backButton.setEnabled(tabNumber > 0);
        headerOfTableTextField.setText(generateHeadingString());
    }

    /**
     *
     */
    @Action
    public void jumpToPreviousTabAction() {
        int tabNumber = tabbedPane.getSelectedIndex();
        if (tabNumber >= 1) {
            tabbedPane.setSelectedIndex(--tabNumber);
            changeTab(tabNumber);
        }
    }

    /**
     *
     */
    @Action
    public void jumpToNextTabAction() {
        int tabNumber = tabbedPane.getSelectedIndex();
        if (tabNumber < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(++tabNumber);
            changeTab(tabNumber);
        }
    }

    /**
     *
     */
    @Action
    public void cancelAction() {
        this.dispose();
    }

    private PopulationDataset[] getSelectedPopulations() {
        PopulationDataset[] populations = new PopulationDataset[populationDatasetChooserPanels.size()];
        int i = 0;
        for (LabelAndComboBoxJPanel panel : populationDatasetChooserPanels) {
            populations[i++] = (PopulationDataset) panel.getComboBoxSelectedItem();
        }

        return populations;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        tableTypePanel = new javax.swing.JPanel();
        tableTypeScrollPane = new javax.swing.JScrollPane();
        tableTypeList = new javax.swing.JList();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextPane = new javax.swing.JTextPane();
        descriptionLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        previewLabel = new javax.swing.JLabel();
        previewImageLabel = new javax.swing.JLabel();
        rangePanel = new javax.swing.JPanel();
        startYearChooser = new com.toedter.calendar.JYearChooser();
        startYearLabel = new javax.swing.JLabel();
        endYearLabel = new javax.swing.JLabel();
        endYearChooser = new com.toedter.calendar.JYearChooser();
        jLabel6 = new javax.swing.JLabel();
        midYearTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        numberOfYearsTextField = new javax.swing.JTextField();
        warningLabel = new javax.swing.JLabel();
        populationDatasetChooserPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        populationDatasetChoosersPanel = new javax.swing.JPanel();
        filterPanel = new javax.swing.JPanel();
        rangeFilterPanel = new canreg.client.gui.components.RangeFilterPanel();
        writeOutPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        headerOfTableLabel = new javax.swing.JLabel();
        headerOfTableTextField = new javax.swing.JTextField();
        backButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();

        setClosable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(TableBuilderInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        tabbedPane.setName("tabbedPane"); // NOI18N

        tableTypePanel.setName("tableTypePanel"); // NOI18N

        tableTypeScrollPane.setName("tableTypeScrollPane"); // NOI18N

        tableTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Incidence per 100000 by Age groups (Annual)", "Incidence per 100000 by Age groups (Period)" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        tableTypeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableTypeList.setName("tableTypeList"); // NOI18N
        tableTypeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                tableTypeListValueChanged(evt);
            }
        });
        tableTypeList.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tableTypeListPropertyChange(evt);
            }
        });
        tableTypeScrollPane.setViewportView(tableTypeList);

        descriptionScrollPane.setName("descriptionScrollPane"); // NOI18N

        descriptionTextPane.setEditable(false);
        descriptionTextPane.setName("descriptionTextPane"); // NOI18N
        descriptionScrollPane.setViewportView(descriptionTextPane);

        descriptionLabel.setName("descriptionLabel"); // NOI18N

        typeLabel.setName("typeLabel"); // NOI18N

        previewLabel.setName("previewLabel"); // NOI18N

        previewImageLabel.setName("previewImageLabel"); // NOI18N

        javax.swing.GroupLayout tableTypePanelLayout = new javax.swing.GroupLayout(tableTypePanel);
        tableTypePanel.setLayout(tableTypePanelLayout);
        tableTypePanelLayout.setHorizontalGroup(
            tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeLabel)
                    .addComponent(tableTypeScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                    .addGroup(tableTypePanelLayout.createSequentialGroup()
                        .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionLabel)
                            .addComponent(descriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(previewLabel)
                            .addComponent(previewImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))))
                .addContainerGap())
        );
        tableTypePanelLayout.setVerticalGroup(
            tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(typeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableTypeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionLabel)
                    .addComponent(previewLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                    .addComponent(descriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("tableTypePanel.TabConstraints.tabTitle"), tableTypePanel); // NOI18N

        rangePanel.setName("rangePanel"); // NOI18N

        startYearChooser.setName("startYearChooser"); // NOI18N
        startYearChooser.setStartYear(-292278994);
        startYearChooser.setValue(1999);
        startYearChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                startYearChooserPropertyChange(evt);
            }
        });

        startYearLabel.setText(resourceMap.getString("startYearLabel.text")); // NOI18N
        startYearLabel.setName("startYearLabel"); // NOI18N

        endYearLabel.setText(resourceMap.getString("endYearLabel.text")); // NOI18N
        endYearLabel.setName("endYearLabel"); // NOI18N

        endYearChooser.setDayChooser(null);
        endYearChooser.setName("endYearChooser"); // NOI18N
        endYearChooser.setStartYear(-292278994);
        endYearChooser.setValue(2001);
        endYearChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                endYearChooserPropertyChange(evt);
            }
        });

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        midYearTextField.setEditable(false);
        midYearTextField.setName("midYearTextField"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        numberOfYearsTextField.setEditable(false);
        numberOfYearsTextField.setName("numberOfYearsTextField"); // NOI18N

        warningLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        warningLabel.setText(resourceMap.getString("warningLabel.text")); // NOI18N
        warningLabel.setName("warningLabel"); // NOI18N

        javax.swing.GroupLayout rangePanelLayout = new javax.swing.GroupLayout(rangePanel);
        rangePanel.setLayout(rangePanelLayout);
        rangePanelLayout.setHorizontalGroup(
            rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                    .addGroup(rangePanelLayout.createSequentialGroup()
                        .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(endYearLabel)
                            .addComponent(startYearLabel)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                        .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(startYearChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(endYearChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(numberOfYearsTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .addComponent(midYearTextField, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        rangePanelLayout.setVerticalGroup(
            rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(startYearLabel)
                    .addComponent(startYearChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(endYearLabel)
                    .addComponent(endYearChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(midYearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(numberOfYearsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warningLabel)
                .addContainerGap(183, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("rangePanel.TabConstraints.tabTitle"), rangePanel); // NOI18N

        populationDatasetChooserPanel.setName("populationDatasetChooserPanel"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        populationDatasetChoosersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        populationDatasetChoosersPanel.setName("populationDatasetChoosersPanel"); // NOI18N
        populationDatasetChoosersPanel.setLayout(new java.awt.GridLayout(0, 1));
        jScrollPane1.setViewportView(populationDatasetChoosersPanel);

        javax.swing.GroupLayout populationDatasetChooserPanelLayout = new javax.swing.GroupLayout(populationDatasetChooserPanel);
        populationDatasetChooserPanel.setLayout(populationDatasetChooserPanelLayout);
        populationDatasetChooserPanelLayout.setHorizontalGroup(
            populationDatasetChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(populationDatasetChooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(populationDatasetChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        populationDatasetChooserPanelLayout.setVerticalGroup(
            populationDatasetChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(populationDatasetChooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("populationDatasetChooserPanel.TabConstraints.tabTitle"), populationDatasetChooserPanel); // NOI18N

        filterPanel.setEnabled(false);
        filterPanel.setFocusable(false);
        filterPanel.setName("filterPanel"); // NOI18N
        filterPanel.setRequestFocusEnabled(false);

        rangeFilterPanel.setName("rangeFilterPanel"); // NOI18N

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rangeFilterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rangeFilterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("filterPanel.TabConstraints.tabTitle"), filterPanel); // NOI18N

        writeOutPanel.setName("writeOutPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(TableBuilderInternalFrame.class, this);
        jButton1.setAction(actionMap.get("generatePostScriptTablesAction")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setEnabled(false);
        jButton2.setName("jButton2"); // NOI18N

        headerOfTableLabel.setText(resourceMap.getString("headerOfTableLabel.text")); // NOI18N
        headerOfTableLabel.setName("headerOfTableLabel"); // NOI18N

        headerOfTableTextField.setText(resourceMap.getString("headerOfTableTextField.text")); // NOI18N
        headerOfTableTextField.setName("headerOfTableTextField"); // NOI18N

        javax.swing.GroupLayout writeOutPanelLayout = new javax.swing.GroupLayout(writeOutPanel);
        writeOutPanel.setLayout(writeOutPanelLayout);
        writeOutPanelLayout.setHorizontalGroup(
            writeOutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(writeOutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(writeOutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(writeOutPanelLayout.createSequentialGroup()
                        .addComponent(headerOfTableLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(headerOfTableTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
                .addContainerGap())
        );
        writeOutPanelLayout.setVerticalGroup(
            writeOutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(writeOutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(writeOutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(headerOfTableLabel)
                    .addComponent(headerOfTableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(214, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("writeOutPanel.TabConstraints.tabTitle"), writeOutPanel); // NOI18N

        backButton.setAction(actionMap.get("jumpToPreviousTabAction")); // NOI18N
        backButton.setName("backButton"); // NOI18N

        cancelButton.setAction(actionMap.get("cancelAction")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        nextButton.setAction(actionMap.get("jumpToNextTabAction")); // NOI18N
        nextButton.setName("nextButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(backButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextButton)
                    .addComponent(cancelButton)
                    .addComponent(backButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void populatePopulationDataSetChooser() {
        populationDatasetChoosersPanel.removeAll();
        if (populationDatasetChooserPanels == null) {
            populationDatasetChooserPanels = new LinkedList<LabelAndComboBoxJPanel>();
        }
        populationDatasetChooserPanels.clear();
        for (int i = 0; i <= (endYearChooser.getYear() - startYearChooser.getYear()); i++) {
            LabelAndComboBoxJPanel panel = new LabelAndComboBoxJPanel();
            panel.setComboBoxModel(new DefaultComboBoxModel(populationDatasetsArray));
            panel.setLabel((startYearChooser.getYear() + i) + ":");
            populationDatasetChoosersPanel.add(panel);
            populationDatasetChooserPanels.add(panel);
            panel.setVisible(true);
        }
        populationDatasetChoosersPanel.revalidate();
        populationDatasetChoosersPanel.repaint();
    }

    private void startYearChooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_startYearChooserPropertyChange
        if (startYearChooser.getYear() > endYearChooser.getYear()) {
            endYearChooser.setYear(startYearChooser.getYear());
            warningLabel.setText("Start cannot be before end - moving end.");
        } else if (startYearChooser.getYear() <= endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE) {
            endYearChooser.setYear(startYearChooser.getYear() + Globals.MAX_POPULATION_DATASETS_IN_TABLE);
            warningLabel.setText("Max span allowed is " + Globals.MAX_POPULATION_DATASETS_IN_TABLE + " - moving end.");
        }
        updateRangeFields();
    }//GEN-LAST:event_startYearChooserPropertyChange

    private void endYearChooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_endYearChooserPropertyChange
        if (startYearChooser.getYear() > endYearChooser.getYear()) {
            startYearChooser.setYear(endYearChooser.getYear());
            warningLabel.setText("End cannot be before start - moving start.");
        } else if (startYearChooser.getYear() <= endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE) {
            startYearChooser.setYear(endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE);
            warningLabel.setText("Max span allowed is " + Globals.MAX_POPULATION_DATASETS_IN_TABLE + " - moving start.");
        }
        updateRangeFields();
    }//GEN-LAST:event_endYearChooserPropertyChange

    private void tableTypeListPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tableTypeListPropertyChange
        // do nothing
    }//GEN-LAST:event_tableTypeListPropertyChange

    private void tableTypeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_tableTypeListValueChanged
        TableBuilderListElement etle = (TableBuilderListElement) tableTypeList.getSelectedValue();
        ImageIcon icon = new ImageIcon(Globals.TABLES_PREVIEW_PATH + "/" + Globals.DEFAULT_PREVIEW_FILENAME,
                etle.getName());

        if (etle != null) {
            descriptionTextPane.setText(etle.getDescription());
            if (etle.getPreviewImageFilename() != null) {
                icon = new ImageIcon(Globals.TABLES_PREVIEW_PATH + "/" + etle.getPreviewImageFilename(),
                        etle.getName());
                previewImageLabel.setIcon(icon);
            }
        } else {
            descriptionTextPane.setText("");
        }
        previewImageLabel.setIcon(icon);
    // tableTypePanel.revalidate();
    // tableTypePanel.repaint();
    }//GEN-LAST:event_tableTypeListValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextPane descriptionTextPane;
    private com.toedter.calendar.JYearChooser endYearChooser;
    private javax.swing.JLabel endYearLabel;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel headerOfTableLabel;
    private javax.swing.JTextField headerOfTableTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField midYearTextField;
    private javax.swing.JButton nextButton;
    private javax.swing.JTextField numberOfYearsTextField;
    private javax.swing.JPanel populationDatasetChooserPanel;
    private javax.swing.JPanel populationDatasetChoosersPanel;
    private javax.swing.JLabel previewImageLabel;
    private javax.swing.JLabel previewLabel;
    private canreg.client.gui.components.RangeFilterPanel rangeFilterPanel;
    private javax.swing.JPanel rangePanel;
    private com.toedter.calendar.JYearChooser startYearChooser;
    private javax.swing.JLabel startYearLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JList tableTypeList;
    private javax.swing.JPanel tableTypePanel;
    private javax.swing.JScrollPane tableTypeScrollPane;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel warningLabel;
    private javax.swing.JPanel writeOutPanel;
    // End of variables declaration//GEN-END:variables

    private void initData() {

        // get population datasets
        try {
            populationDatasetsMap = canreg.client.CanRegClientApp.getApplication().getPopulationDatasets();
            Collection<PopulationDataset> populationDatasetsCollection;
            Collection<PopulationDataset> populationDatasetsCollection2 = new LinkedList<PopulationDataset>();
            populationDatasetsCollection = populationDatasetsMap.values();
            for (PopulationDataset pd : populationDatasetsCollection) {
                if (!pd.isWorldPopulationBool()) {
                    populationDatasetsCollection2.add(pd);
                }
            }
            populationDatasetsArray = populationDatasetsCollection2.toArray(new PopulationDataset[0]);
            populatePopulationDataSetChooser();
        } catch (SecurityException ex) {
            Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        //get table builder engines
        refreshTableTypeList();
    }

    private void updateRangeFields() {
        String midYearString = "";
        int numberOfYears = endYearChooser.getYear() - startYearChooser.getYear() + 1;
        int midYear = startYearChooser.getYear() + numberOfYears / 2;
        midYearString = midYear + "";
        if (numberOfYears % 2 != 1) {
            midYearString = (midYear - 1) + "-" + midYearString;
        }
        midYearTextField.setText(midYearString);
        numberOfYearsTextField.setText(numberOfYears + "");

        populatePopulationDataSetChooser();
    }

    private void refreshTableTypeList() {
        //get directory of .confs

        File dir = new File(Globals.TABLES_CONF_PATH);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return (name.endsWith(".conf"));
            }
        };
        String[] children = dir.list(filter);

        LinkedList<TableBuilderListElement> tableTypeLinkedList = new LinkedList<TableBuilderListElement>();
        DefaultListModel listModel = new DefaultListModel();
        if (children == null) {
            // Either dir does not exist or is not a directory
        } else {
            //open one by one using configreader
            //make list
            for (int i = 0; i < children.length; i++) {
                // Get filename of file or directory
                String filename = children[i];

                try {
                    String[] tempArray = null;
                    String configFileName = Globals.TABLES_CONF_PATH + "/" + filename;
                    FileReader fileReader = new FileReader(configFileName);
                    LinkedList<ConfigFields> configFields = ConfigFieldsReader.readFile(fileReader);
                    TableBuilderListElement etle = new TableBuilderListElement();
                    etle.setConfigFileName(configFileName);
                    etle.setConfigFields(configFields);

                    tempArray = ConfigFieldsReader.findConfig("table_label", configFields);
                    if (tempArray != null && tempArray.length > 0) {
                        etle.setName(tempArray[0]);
                        System.out.println(tempArray[0]);
                    }

                    tempArray = ConfigFieldsReader.findConfig("table_description", configFields);
                    if (tempArray != null && tempArray.length > 0) {
                        etle.setDescription(tempArray[0]);
                        System.out.println(tempArray[0]);
                    }

                    tempArray = ConfigFieldsReader.findConfig("table_engine", configFields);
                    if (tempArray != null && tempArray.length > 0) {
                        etle.setEngineName(tempArray[0]);
                        System.out.println(tempArray[0]);
                    }

                    String[] engineParameters = ConfigFieldsReader.findConfig("engine_parameters", configFields);
                    etle.setEngineParameters(engineParameters);

                    tempArray = ConfigFieldsReader.findConfig("preview_image", configFields);
                    if (tempArray != null && tempArray.length > 0) {
                        etle.setPreviewImageFilename(tempArray[0]);
                        System.out.println(tempArray[0]);
                    }

                    tableTypeLinkedList.add(etle);
                    listModel.addElement(etle);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            tableTypeList.setModel(listModel);
        }
    }

    @Action
    public void generatePostScriptTablesAction() {
        boolean filterError = false;
        TableBuilder tableBuilder = null;
        TableBuilderListElement tble = (TableBuilderListElement) tableTypeList.getSelectedValue();

        if (tble == null) {
            JOptionPane.showMessageDialog(this, "No table type selected.", "No table type selected.", JOptionPane.ERROR_MESSAGE);
            return;
        } else if (tble.getEngineName().equalsIgnoreCase("incidencerates")) {
            tableBuilder = new AgeSpecificCasesPerHundredThousandTableBuilder();
        } else if (tble.getEngineName().equalsIgnoreCase("numberofcases")) {
            tableBuilder = new AgeSpecificCasesTableBuilder();
        } else if (tble.getEngineName().equalsIgnoreCase("populationpyramids")) {
            tableBuilder = new PopulationPyramidTableBuilder();
        }

        Set<DatabaseVariablesListElement> variables = new LinkedHashSet<DatabaseVariablesListElement>();
        DistributedTableDescription tableDatadescription;

        if (tableBuilder == null) {
            JOptionPane.showMessageDialog(this, "Table type not yet implemented.", "Table type not yet implemented.", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            String heading = headerOfTableTextField.getText();
            int startYear = startYearChooser.getValue();
            int endYear = endYearChooser.getValue();
            PopulationDataset[] populations = getSelectedPopulations();
            PopulationDataset[] standardPopulations = new PopulationDataset[populations.length];

            if (tableBuilder.areThesePopulationDatasetsOK(populations)) {
                String fileName = null;
                // Choose file name;
                int returnVal = chooser.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        localSettings.setProperty("tables_path", chooser.getSelectedFile().getCanonicalPath());
                        fileName = chooser.getSelectedFile().getAbsolutePath();
                    } catch (IOException ex) {
                        Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // cancelled
                    return;
                }

                int i = 0;
                String populationFilterString = "";
                for (PopulationDataset pop : populations) {
                    int stdPopID = pop.getWorldPopulationID();
                    standardPopulations[i++] = populationDatasetsMap.get(stdPopID);
                    if (populationFilterString.trim().length() == 0) {
                        populationFilterString = pop.getFilter();
                    } else if (!populationFilterString.equalsIgnoreCase(pop.getFilter())) {
                        // population filters not matching on all the pds...
                        filterError = true;
                    }
                }

                Globals.StandardVariableNames[] variablesNeeded = tableBuilder.getVariablesNeeded();
                if (variablesNeeded != null) {
                    for (Globals.StandardVariableNames standardVariableName : variablesNeeded) {
                        variables.add(canreg.client.CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(standardVariableName.toString()));
                    }
                }
                DatabaseFilter filter = new DatabaseFilter();
                String tableName = "both";
                String filterString = rangeFilterPanel.getFilter().trim();

                if (filterString.length() != 0) {
                    filterString += " AND ";
                }

                if (populationFilterString.length() != 0) {
                    filterString += populationFilterString + " AND ";
                }

                DatabaseVariablesListElement incidenceDate = canreg.client.CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString());
                filterString += incidenceDate.getDatabaseVariableName() + " >= '" + startYear * 10000 + "' AND " + incidenceDate.getDatabaseVariableName() + " < '" + (endYear + 1) * 10000 + "'";
                filter.setFilterString(filterString);

                System.out.println(filterString);

                filter.setQueryType(DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR);
                filter.setDatabaseVariables(variables);
                DistributedTableDataSourceClient tableDataSource;
                Object[][] incidenceData = null;
                try {
                    tableDatadescription = canreg.client.CanRegClientApp.getApplication().getDistributedTableDescription(filter, tableName);
                    tableDataSource = new DistributedTableDataSourceClient(tableDatadescription);
                    if (tableDatadescription.getRowCount() > 0) {
                        incidenceData = tableDataSource.retrieveRows(0, tableDatadescription.getRowCount());
                    }

                    LinkedList<String> filesGenerated = tableBuilder.buildTable(heading, fileName, startYear, endYear, incidenceData, populations, standardPopulations, tble.getConfigFields(), tble.getEngineParameters());
                    JOptionPane.showMessageDialog(this, "Tables built.", "Tables built.", JOptionPane.INFORMATION_MESSAGE);

                    // Opening the resulting files...
                    for (String resultFileName : filesGenerated) {
                        canreg.common.Tools.openFile(resultFileName);
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NotCompatibleDataException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }catch (Exception ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Population set not compatible.", "No tables built.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
