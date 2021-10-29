/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2018 International Agency for Research on Cancer
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
 * TableBuilderInternalFrame.java
 *
 * Created on 28-Apr-2009, 16:33:32
 */
package canreg.client.gui.analysis;

import canreg.client.analysis.TableBuilderInterface.FileTypes;
import canreg.client.gui.tools.globalpopup.TechnicalError;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.client.CanRegClientApp;
import canreg.client.DistributedTableDataSourceClient;
import canreg.client.LocalSettings;
import canreg.client.analysis.ConfigFields;
import canreg.client.analysis.ConfigFieldsReader;
import canreg.client.analysis.JChartTableBuilderInterface;
import canreg.client.analysis.NotCompatibleDataException;
import canreg.client.analysis.TableBuilderFactory;
import canreg.client.analysis.TableBuilderInterface;
import canreg.client.analysis.TableBuilderListElement;
import canreg.client.analysis.TableErrorException;
import canreg.client.gui.CanRegClientView;
import canreg.client.gui.components.LabelAndComboBoxJPanel;
import canreg.client.gui.components.RangeFilterPanel;
import canreg.client.gui.tools.globalpopup.MyPopUpMenu;
import canreg.common.DatabaseFilter;
import canreg.common.DatabaseGroupsListElement;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.database.PopulationDataset;
import canreg.server.database.UnknownTableException;
import org.imgscalr.Scalr;
import java.awt.Cursor;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.application.Action;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author ervikm
 */
public class TableBuilderInternalFrame extends javax.swing.JInternalFrame {

    private Map<Integer, PopulationDataset> populationDatasetsMap;
    private PopulationDataset[] populationDatasetsArray;
    private LinkedList<LabelAndComboBoxJPanel> populationDatasetChooserPanels;
    private final LocalSettings localSettings;
    private String path;
    private JFileChooser chooser;
    private final Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
    private final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private TableBuilderInterface tableBuilder = null;
    int filterTabPos = 3;
    private RangeFilterPanel rangeFilterPanel;

    /**
     * Creates new form TableBuilderInternalFrame
     */
    public TableBuilderInternalFrame() {
        initComponents();
        setCursor(hourglassCursor);

        initRangeFilterPanel();

        initData();

        localSettings = CanRegClientApp.getApplication().getLocalSettings();

        // Add a listener for changing the active tab
        ChangeListener tabbedPaneChangeListener = (ChangeEvent e) -> {
            // initializeVariableMappingTab();
            changeTab(tabbedPane.getSelectedIndex());
        };
        // And add the listener to the tabbedPane

        tabbedPane.addChangeListener(tabbedPaneChangeListener);
        changeTab(0);

        // remove filter tab
        // Component filterTab = tabbedPane.getComponents()[3];
        tabbedPane.setEnabledAt(filterTabPos, false);

        rangeFilterPanel.setSortByVariableShown(false);
        rangeFilterPanel.setRecordPanelvisible(false);
        rangeFilterPanel.setRefreshButtonEnabled(false);
        rangeFilterPanel.setTableChooserVisible(false);

        //tabbedPane.remove(3);
        setCursor(normalCursor);

        turnAllOutputButtonsOff();
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

    protected final void changeTab(int tabNumber) {
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
            tabNumber -= 1;
            if (!dontUsePopulationDatasetCheckBox.isSelected()) {
                if (tabNumber == filterTabPos) {
                    tabNumber -= 1;
                }
            }
            tabbedPane.setSelectedIndex(tabNumber);
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
            tabNumber += 1;
            if (!dontUsePopulationDatasetCheckBox.isSelected()) {
                if (tabNumber == filterTabPos) {
                    tabNumber += 1;
                }
            }
            tabbedPane.setSelectedIndex(tabNumber);
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

    private PopulationDataset[] generateDummyPopulationDatasets() {
        PopulationDataset dummyPop = new PopulationDataset();
        dummyPop.setFilter(rangeFilterPanel.getFilter());
        dummyPop.setReferencePopulation(new PopulationDataset());
        dummyPop.setReferencePopulationID(0);
        dummyPop.setPopulationDatasetName("");

        PopulationDataset[] populations = new PopulationDataset[populationDatasetChooserPanels.size()];
        for (int i = 0; i < populationDatasetChooserPanels.size(); i++) {
            populations[i] = dummyPop;
        }
        return populations;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        pleaseChooseLabel = new javax.swing.JLabel();
        populationDatasetsScrollPane = new javax.swing.JScrollPane();
        populationDatasetChoosersPanel = new javax.swing.JPanel();
        dontUsePopulationDatasetCheckBox = new javax.swing.JCheckBox();
        filterPanel = new javax.swing.JPanel();
        writeOutPanel = new javax.swing.JPanel();
        postScriptButton = new javax.swing.JButton();
        tabulatedButton = new javax.swing.JButton();
        headerOfTableLabel = new javax.swing.JLabel();
        headerOfTableTextField = new javax.swing.JTextField();
        imageButton = new javax.swing.JButton();
        pdfButton = new javax.swing.JButton();
        svgButton = new javax.swing.JButton();
        wmfButton = new javax.swing.JButton();
        chartViewerButton = new javax.swing.JButton();
        seerPrepButton = new javax.swing.JButton();
        csvButton = new javax.swing.JButton();
        tiffButton = new javax.swing.JButton();
        docxButton = new javax.swing.JButton();
        pptxButton = new javax.swing.JButton();
        browserButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();

        setClosable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(TableBuilderInternalFrame.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        tabbedPane.setName("tabbedPane"); // NOI18N
        tabbedPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tabbedPaneFocusLost(evt);
            }
        });

        tableTypePanel.setName("tableTypePanel"); // NOI18N
        tableTypePanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tableTypePanelFocusLost(evt);
            }
        });

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
                    .addComponent(tableTypeScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(typeLabel)
                    .addGroup(tableTypePanelLayout.createSequentialGroup()
                        .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionLabel)
                            .addComponent(descriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(previewLabel)
                            .addComponent(previewImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))))
                .addContainerGap())
        );
        tableTypePanelLayout.setVerticalGroup(
            tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(typeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableTypeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionLabel)
                    .addComponent(previewLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tableTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(descriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
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
                    .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addGroup(rangePanelLayout.createSequentialGroup()
                        .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(endYearLabel)
                            .addComponent(startYearLabel)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 179, Short.MAX_VALUE)
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
                .addContainerGap(308, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("rangePanel.TabConstraints.tabTitle"), rangePanel); // NOI18N

        populationDatasetChooserPanel.setName("populationDatasetChooserPanel"); // NOI18N

        pleaseChooseLabel.setText(resourceMap.getString("pleaseChooseLabel.text")); // NOI18N
        pleaseChooseLabel.setName("pleaseChooseLabel"); // NOI18N

        populationDatasetsScrollPane.setName("populationDatasetsScrollPane"); // NOI18N

        populationDatasetChoosersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        populationDatasetChoosersPanel.setName("populationDatasetChoosersPanel"); // NOI18N
        populationDatasetChoosersPanel.setLayout(new java.awt.GridLayout(0, 1));
        populationDatasetsScrollPane.setViewportView(populationDatasetChoosersPanel);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(TableBuilderInternalFrame.class, this);
        dontUsePopulationDatasetCheckBox.setAction(actionMap.get("dontUsePopsCheckboxUpdated")); // NOI18N
        dontUsePopulationDatasetCheckBox.setText(resourceMap.getString("dontUsePopulationDatasetCheckBox.text")); // NOI18N
        dontUsePopulationDatasetCheckBox.setToolTipText(resourceMap.getString("dontUsePopulationDatasetCheckBox.toolTipText")); // NOI18N
        dontUsePopulationDatasetCheckBox.setName("dontUsePopulationDatasetCheckBox"); // NOI18N
        dontUsePopulationDatasetCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontUsePopulationDatasetCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout populationDatasetChooserPanelLayout = new javax.swing.GroupLayout(populationDatasetChooserPanel);
        populationDatasetChooserPanel.setLayout(populationDatasetChooserPanelLayout);
        populationDatasetChooserPanelLayout.setHorizontalGroup(
            populationDatasetChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(populationDatasetChooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(populationDatasetChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(populationDatasetsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(pleaseChooseLabel)
                    .addComponent(dontUsePopulationDatasetCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))
                .addContainerGap())
        );
        populationDatasetChooserPanelLayout.setVerticalGroup(
            populationDatasetChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(populationDatasetChooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pleaseChooseLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(populationDatasetsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(dontUsePopulationDatasetCheckBox)
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("populationDatasetChooserPanel.TabConstraints.tabTitle"), populationDatasetChooserPanel); // NOI18N

        filterPanel.setEnabled(false);
        filterPanel.setFocusable(false);
        filterPanel.setName("filterPanel"); // NOI18N
        filterPanel.setRequestFocusEnabled(false);
        tabbedPane.addTab(resourceMap.getString("filterPanel.TabConstraints.tabTitle"), filterPanel); // NOI18N

        writeOutPanel.setName("writeOutPanel"); // NOI18N

        postScriptButton.setAction(actionMap.get("generatePStable")); // NOI18N
        postScriptButton.setText(resourceMap.getString("postScriptButton.text")); // NOI18N
        postScriptButton.setToolTipText(resourceMap.getString("postScriptButton.toolTipText")); // NOI18N
        postScriptButton.setName("postScriptButton"); // NOI18N
        postScriptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postScriptButtonActionPerformed(evt);
            }
        });

        tabulatedButton.setAction(actionMap.get("generateTabulatedTables")); // NOI18N
        tabulatedButton.setText(resourceMap.getString("tabulatedButton.text")); // NOI18N
        tabulatedButton.setToolTipText(resourceMap.getString("tabulatedButton.toolTipText")); // NOI18N
        tabulatedButton.setName("tabulatedButton"); // NOI18N

        headerOfTableLabel.setText(resourceMap.getString("headerOfTableLabel.text")); // NOI18N
        headerOfTableLabel.setName("headerOfTableLabel"); // NOI18N

        headerOfTableTextField.setText(resourceMap.getString("headerOfTableTextField.text")); // NOI18N
        headerOfTableTextField.setName("headerOfTableTextField"); // NOI18N
        headerOfTableTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                headerOfTableTextFieldMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                headerOfTableTextFieldMouseReleased(evt);
            }
        });

        imageButton.setAction(actionMap.get("generatePNGTable")); // NOI18N
        imageButton.setText(resourceMap.getString("imageButton.text")); // NOI18N
        imageButton.setToolTipText(resourceMap.getString("imageButton.toolTipText")); // NOI18N
        imageButton.setName("imageButton"); // NOI18N

        pdfButton.setAction(actionMap.get("generatePDFtable")); // NOI18N
        pdfButton.setText(resourceMap.getString("pdfButton.text")); // NOI18N
        pdfButton.setToolTipText(resourceMap.getString("pdfButton.toolTipText")); // NOI18N
        pdfButton.setName("pdfButton"); // NOI18N
        pdfButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfButtonActionPerformed(evt);
            }
        });

        svgButton.setAction(actionMap.get("generateSVGFile")); // NOI18N
        svgButton.setText(resourceMap.getString("svgButton.text")); // NOI18N
        svgButton.setToolTipText(resourceMap.getString("svgButton.toolTipText")); // NOI18N
        svgButton.setName("svgButton"); // NOI18N
        svgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svgButtonActionPerformed(evt);
            }
        });

        wmfButton.setAction(actionMap.get("generateWMFAction")); // NOI18N
        wmfButton.setText(resourceMap.getString("wmfButton.text")); // NOI18N
        wmfButton.setToolTipText(resourceMap.getString("wmfButton.toolTipText")); // NOI18N
        wmfButton.setName("wmfButton"); // NOI18N
        wmfButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wmfButtonActionPerformed(evt);
            }
        });

        chartViewerButton.setAction(actionMap.get("openInChartViewer")); // NOI18N
        chartViewerButton.setText(resourceMap.getString("chartViewerButton.text")); // NOI18N
        chartViewerButton.setToolTipText(resourceMap.getString("chartViewerButton.toolTipText")); // NOI18N
        chartViewerButton.setName("chartViewerButton"); // NOI18N

        seerPrepButton.setAction(actionMap.get("generateFilesForSEERPrepAction")); // NOI18N
        seerPrepButton.setText(resourceMap.getString("seerPrepButton.text")); // NOI18N
        seerPrepButton.setName("seerPrepButton"); // NOI18N

        csvButton.setAction(actionMap.get("generateCSV")); // NOI18N
        csvButton.setText(resourceMap.getString("csvButton.text")); // NOI18N
        csvButton.setToolTipText(resourceMap.getString("csvButton.toolTipText")); // NOI18N
        csvButton.setName("csvButton"); // NOI18N

        tiffButton.setAction(actionMap.get("generateTIFF")); // NOI18N
        tiffButton.setText(resourceMap.getString("tiffButton.text")); // NOI18N
        tiffButton.setToolTipText(resourceMap.getString("tiffButton.toolTipText")); // NOI18N
        tiffButton.setName("tiffButton"); // NOI18N

        docxButton.setAction(actionMap.get("generateDOCX")); // NOI18N
        docxButton.setText(resourceMap.getString("docxButton.text")); // NOI18N
        docxButton.setName("docxButton"); // NOI18N

        pptxButton.setAction(actionMap.get("generatePPTX")); // NOI18N
        pptxButton.setText(resourceMap.getString("pptxButton.text")); // NOI18N
        pptxButton.setToolTipText(resourceMap.getString("pptxButton.toolTipText")); // NOI18N
        pptxButton.setName("pptxButton"); // NOI18N

        browserButton.setAction(actionMap.get("launchBrowserAction")); // NOI18N
        browserButton.setText(resourceMap.getString("browserButton.text")); // NOI18N
        browserButton.setName("browserButton"); // NOI18N

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
                        .addComponent(headerOfTableTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE))
                    .addComponent(pdfButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(postScriptButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(wmfButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(tabulatedButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(svgButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(imageButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(csvButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(chartViewerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(seerPrepButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(tiffButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(docxButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(pptxButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addComponent(browserButton, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))
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
                .addComponent(pdfButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(postScriptButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(svgButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wmfButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabulatedButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tiffButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(docxButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pptxButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(csvButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chartViewerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seerPrepButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browserButton)
                .addContainerGap(28, Short.MAX_VALUE))
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
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.LEADING)
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
                .addComponent(tabbedPane)
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
            populationDatasetChooserPanels = new LinkedList<>();
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
        guessPopulationSelections();
    }

    private void guessPopulationSelections() {
        Map<Integer, PopulationDataset> map = new HashMap<>();
        for (PopulationDataset pds : populationDatasetsArray) {
            if (pds.getDate().length() >= 4) {
                int year = Integer.parseInt(pds.getDate().substring(0, 4));
                if (map.get(year) == null) {
                    map.put(year, pds);
                }
            }
        }
        for (int i = 0; i <= (endYearChooser.getYear() - startYearChooser.getYear()); i++) {
            populationDatasetChooserPanels.get(i).setComboBoxSelectedItem(map.get(i + startYearChooser.getYear()));
        }
    }

    private void startYearChooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_startYearChooserPropertyChange
//        if (startYearChooser.getYear() > endYearChooser.getYear()) {
//            endYearChooser.setYear(startYearChooser.getYear());
//            warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("START_BEFORE_END_MOVING_END"));
//        } else if (startYearChooser.getYear() <= endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE) {
//            endYearChooser.setYear(startYearChooser.getYear() + Globals.MAX_POPULATION_DATASETS_IN_TABLE);
//            warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("MAX_SPAN_ALLOWED_IS_") + Globals.MAX_POPULATION_DATASETS_IN_TABLE + java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("_MOVING_END."));
//        }
//        updateRangeFields();
    }//GEN-LAST:event_startYearChooserPropertyChange

    private void endYearChooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_endYearChooserPropertyChange
//        if (startYearChooser.getYear() > endYearChooser.getYear()) {
//            startYearChooser.setYear(endYearChooser.getYear());
//            warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("END_BEFORE_START_MOVING_START"));
//        } else if (startYearChooser.getYear() <= endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE) {
//            startYearChooser.setYear(endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE);
//            warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("MAX_SPAN_ALLOWED_IS_") + Globals.MAX_POPULATION_DATASETS_IN_TABLE + java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("_MOVING_START."));
//        }
//        updateRangeFields();
    }//GEN-LAST:event_endYearChooserPropertyChange

    private void tableTypeListPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tableTypeListPropertyChange
        // do nothing
    }//GEN-LAST:event_tableTypeListPropertyChange

    private void tableTypeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_tableTypeListValueChanged
        TableBuilderListElement tble = (TableBuilderListElement) tableTypeList.getSelectedValue();
        ImageIcon icon = new ImageIcon(Globals.TABLES_PREVIEW_PATH + "/" + Globals.DEFAULT_PREVIEW_FILENAME,
                tble.getName());

        turnAllOutputButtonsOff();

        if (tble != null) {

            descriptionTextPane.setText(tble.getDescription());
            if (tble.getPreviewImageFilename() != null) {
                // first try load a user provided image.
                File imageFile = new File(Globals.USER_TABLES_PREVIEW_PATH + "/" + tble.getPreviewImageFilename());
                // if that does not exist, try to load a system file.
                if (!imageFile.exists()) {
                    imageFile = new File(Globals.TABLES_PREVIEW_PATH + "/" + tble.getPreviewImageFilename());
                }
                if (imageFile.exists()) {
                    BufferedImage img = null;
                    try {
                        img = ImageIO.read(imageFile);
                        BufferedImage image = Scalr.resize(img, Math.min(previewImageLabel.getWidth(), previewImageLabel.getHeight()));
                        icon = new ImageIcon(image,
                                tble.getName());
                        previewImageLabel.setIcon(icon);
                    } catch (IOException e) {
                        Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
            try {
                tableBuilder = TableBuilderFactory.getTableBuilder(tble);

                if (tableBuilder != null) {
                    FileTypes[] fileTypes = tableBuilder.getFileTypesGenerated();
                    for (FileTypes filetype : fileTypes) {
                        switch (filetype) {
                            case ps:
                                postScriptButton.setEnabled(true);
                                break;
                            case html:
                                tabulatedButton.setEnabled(true);
                                break;
                            case png:
                                imageButton.setEnabled(true);
                                break;
                            case pdf:
                                pdfButton.setEnabled(true);
                                break;
                            case svg:
                                svgButton.setEnabled(true);
                                break;
                            case wmf:
                                wmfButton.setEnabled(true);
                                break;
                            case jchart:
                                chartViewerButton.setEnabled(true);
                                break;
                            case seer:
                                seerPrepButton.setEnabled(true);
                                break;
                            case csv:
                                csvButton.setEnabled(true);
                                break;
                            case tiff:
                                tiffButton.setEnabled(true);
                                break;
                            case docx:
                                docxButton.setEnabled(true);
                                break;
                            case pptx:
                                pptxButton.setEnabled(true);
                                break;
                            case browser:
                                browserButton.setEnabled(true);
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            descriptionTextPane.setText("");
        }
        previewImageLabel.setIcon(icon);
        // tableTypePanel.revalidate();
        // tableTypePanel.repaint();

    }//GEN-LAST:event_tableTypeListValueChanged

    private void headerOfTableTextFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headerOfTableTextFieldMousePressed
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(headerOfTableTextField, evt);
    }//GEN-LAST:event_headerOfTableTextFieldMousePressed

    private void headerOfTableTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headerOfTableTextFieldMouseReleased
        MyPopUpMenu.potentiallyShowPopUpMenuTextComponent(headerOfTableTextField, evt);
    }//GEN-LAST:event_headerOfTableTextFieldMouseReleased

    private void tabbedPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tabbedPaneFocusLost
    }//GEN-LAST:event_tabbedPaneFocusLost

    private void tableTypePanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tableTypePanelFocusLost
    }//GEN-LAST:event_tableTypePanelFocusLost

    private void postScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postScriptButtonActionPerformed
        // generateTablesAction();
    }//GEN-LAST:event_postScriptButtonActionPerformed

    private void pdfButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pdfButtonActionPerformed

    private void svgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svgButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_svgButtonActionPerformed

    private void wmfButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wmfButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_wmfButtonActionPerformed
    private void dontUsePopulationDatasetCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontUsePopulationDatasetCheckBoxActionPerformed
        // TODO add your handling code here:
        // dontUsePopsCheckboxUpdated();
    }//GEN-LAST:event_dontUsePopulationDatasetCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton browserButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton chartViewerButton;
    private javax.swing.JButton csvButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextPane descriptionTextPane;
    private javax.swing.JButton docxButton;
    private javax.swing.JCheckBox dontUsePopulationDatasetCheckBox;
    private com.toedter.calendar.JYearChooser endYearChooser;
    private javax.swing.JLabel endYearLabel;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel headerOfTableLabel;
    private javax.swing.JTextField headerOfTableTextField;
    private javax.swing.JButton imageButton;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField midYearTextField;
    private javax.swing.JButton nextButton;
    private javax.swing.JTextField numberOfYearsTextField;
    private javax.swing.JButton pdfButton;
    private javax.swing.JLabel pleaseChooseLabel;
    private javax.swing.JPanel populationDatasetChooserPanel;
    private javax.swing.JPanel populationDatasetChoosersPanel;
    private javax.swing.JScrollPane populationDatasetsScrollPane;
    private javax.swing.JButton postScriptButton;
    private javax.swing.JButton pptxButton;
    private javax.swing.JLabel previewImageLabel;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel rangePanel;
    private javax.swing.JButton seerPrepButton;
    private com.toedter.calendar.JYearChooser startYearChooser;
    private javax.swing.JLabel startYearLabel;
    private javax.swing.JButton svgButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JList tableTypeList;
    private javax.swing.JPanel tableTypePanel;
    private javax.swing.JScrollPane tableTypeScrollPane;
    private javax.swing.JButton tabulatedButton;
    private javax.swing.JButton tiffButton;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel warningLabel;
    private javax.swing.JButton wmfButton;
    private javax.swing.JPanel writeOutPanel;
    // End of variables declaration//GEN-END:variables

    private void turnAllOutputButtonsOff() {
        // set all buttons off
        postScriptButton.setEnabled(false);
        tabulatedButton.setEnabled(false);
        csvButton.setEnabled(false);
        imageButton.setEnabled(false);
        pdfButton.setEnabled(false);
        svgButton.setEnabled(false);
        wmfButton.setEnabled(false);
        chartViewerButton.setEnabled(false);
        seerPrepButton.setEnabled(false);
        seerPrepButton.setEnabled(false);
        seerPrepButton.setEnabled(false);
        tiffButton.setEnabled(false);
        docxButton.setEnabled(false);
        pptxButton.setEnabled(false);
        browserButton.setEnabled(false);
    }

    private void initData() {
        JSpinner spinnerStart = (JSpinner) startYearChooser.getSpinner();
        spinnerStart.getEditor()
                .addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        startYearChooserFocusLost(evt);
                    }

                    private void startYearChooserFocusLost(FocusEvent evt) {
                        if (startYearChooser.getYear() > endYearChooser.getYear()) {
                            endYearChooser.setYear(startYearChooser.getYear());
                            warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("START_BEFORE_END_MOVING_END"));
                        } else if (startYearChooser.getYear() <= endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE) {
                            endYearChooser.setYear(startYearChooser.getYear() + Globals.MAX_POPULATION_DATASETS_IN_TABLE);
                            warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("MAX_SPAN_ALLOWED_IS_") + Globals.MAX_POPULATION_DATASETS_IN_TABLE + java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("_MOVING_END."));
                        } else {
                            warningLabel.setText("");
                        }
                        updateRangeFields();
                    }
                });
        JSpinner spinnerEnd = (JSpinner) endYearChooser.getSpinner();
        spinnerEnd.getEditor().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                endYearChooserFocusLost(evt);
            }

            private void endYearChooserFocusLost(FocusEvent evt) {
                if (startYearChooser.getYear() > endYearChooser.getYear()) {
                    startYearChooser.setYear(endYearChooser.getYear());
                    warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("END_BEFORE_START_MOVING_START"));
                } else if (startYearChooser.getYear() <= endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE) {
                    startYearChooser.setYear(endYearChooser.getYear() - Globals.MAX_POPULATION_DATASETS_IN_TABLE);
                    warningLabel.setText(java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("MAX_SPAN_ALLOWED_IS_") + Globals.MAX_POPULATION_DATASETS_IN_TABLE + java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("_MOVING_START."));
                } else {
                    warningLabel.setText("");
                }
                updateRangeFields();
            }
        });

        rangeFilterPanel.setDatabaseDescription(canreg.client.CanRegClientApp.getApplication().getDatabseDescription());

        // get population datasets
        try {
            populationDatasetsMap = canreg.client.CanRegClientApp.getApplication().getPopulationDatasets(null);
            Collection<PopulationDataset> populationDatasetsCollection;
            Collection<PopulationDataset> populationDatasetsCollection2 = new LinkedList<>();
            populationDatasetsCollection = populationDatasetsMap.values();
            populationDatasetsCollection.stream().filter((pd) -> (!pd.isReferencePopulationBool())).forEachOrdered((pd) -> {
                populationDatasetsCollection2.add(pd);
            });
            populationDatasetsArray = populationDatasetsCollection2.toArray(new PopulationDataset[0]);
            
            Arrays.sort(populationDatasetsArray, (PopulationDataset o1, PopulationDataset o2) -> {
                return o1.getPopulationDatasetName().compareTo(o2.getPopulationDatasetName());
            });
            
            populatePopulationDataSetChooser();
        } catch (SecurityException | RemoteException ex) {
            Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            new TechnicalError().errorDialog();
        }

        //get table builder engines
        refreshTableTypeList();
    }

    private void updateRangeFields() {
        int numberOfYears = endYearChooser.getYear() - startYearChooser.getYear() + 1;
        int midYear = startYearChooser.getYear() + numberOfYears / 2;
        String midYearString = midYear + "";
        if (numberOfYears % 2 != 1) {
            midYearString = (midYear - 1) + "-" + midYearString;
        }
        midYearTextField.setText(midYearString);
        numberOfYearsTextField.setText(numberOfYears + "");

        populatePopulationDataSetChooser();
    }

    private void refreshTableTypeList() {

        FilenameFilter filter = (File dir, String name1) -> (name1.endsWith(".conf"));

        LinkedList<String> children = new LinkedList<>();

        // get directories of .confs
        File[] dirs = {
            new File(Globals.TABLES_CONF_PATH),
            new File(Globals.USER_TABLES_CONF_PATH)
        };

        for (File dir : dirs) {
            if (dir.exists()) {
                for (String fileName : dir.list(filter)) {
                    children.add(dir.getAbsolutePath() + Globals.FILE_SEPARATOR + fileName);
                }
            }
        }

        // LinkedList<TableBuilderListElement> tableTypeLinkedList = new LinkedList<TableBuilderListElement>();
        DefaultListModel listModel = new DefaultListModel();
        //open one by one using configreader
        //make list
        children.forEach((configFileName) -> {
            // Get filename of file or directory
            try {
                String[] tempArray;
                FileReader fileReader = new FileReader(configFileName);
                LinkedList<ConfigFields> configFields = ConfigFieldsReader.readFile(fileReader);
                TableBuilderListElement etle = new TableBuilderListElement();
                etle.setConfigFileName(configFileName);
                etle.setConfigFields(configFields);

                tempArray = ConfigFieldsReader.findConfig("table_label", configFields);
                if (tempArray != null && tempArray.length > 0) {
                    etle.setName(tempArray[0]);
                    // System.out.println(tempArray[0]);
                }

                tempArray = ConfigFieldsReader.findConfig("table_description", configFields);
                if (tempArray != null && tempArray.length > 0) {
                    etle.setDescription(tempArray[0]);
                    // System.out.println(tempArray[0]);
                }

                tempArray = ConfigFieldsReader.findConfig("table_engine", configFields);
                if (tempArray != null && tempArray.length > 0) {
                    etle.setEngineName(tempArray[0]);
                    // System.out.println(tempArray[0]);
                }

                String[] engineParameters = ConfigFieldsReader.findConfig("engine_parameters", configFields);
                etle.setEngineParameters(engineParameters);

                tempArray = ConfigFieldsReader.findConfig("preview_image", configFields);
                if (tempArray != null && tempArray.length > 0) {
                    etle.setPreviewImageFilename(tempArray[0]);
                    // System.out.println(tempArray[0]);
                }

                // tableTypeLinkedList.add(etle);
                listModel.addElement(etle);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        });
        tableTypeList.setModel(listModel);

    }

    private void generateTablesAction(FileTypes filetype) {
        boolean filterError = false;

        TableBuilderListElement tble = (TableBuilderListElement) tableTypeList.getSelectedValue();

        if (tble == null) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("NO_TABLE_TYPE_SELECTED"), java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("NO_TABLE_TYPE_SELECTED"), JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            try {
                tableBuilder = TableBuilderFactory.getTableBuilder(tble);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                new TechnicalError().errorDialog();
            }
        }

        Set<DatabaseVariablesListElement> variables = new LinkedHashSet<>();
        DistributedTableDescription tableDatadescription;
        JChartTableBuilderInterface chartBuilder;

        if (tableBuilder == null) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("TABLE_TYPE_NOT_YET_IMPLEMENTED"), java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("TABLE_TYPE_NOT_YET_IMPLEMENTED"), JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            String language = CanRegClientApp.getApplication().getLocalSettings().getLanguageCode();
            LinkedList<ConfigFields> configFields = tble.getConfigFields();
            String heading = headerOfTableTextField.getText();
            int startYear = startYearChooser.getValue();
            int endYear = endYearChooser.getValue();
            PopulationDataset[] populations;
            if (dontUsePopulationDatasetCheckBox.isSelected()) {
                populations = generateDummyPopulationDatasets();
            } else {
                populations = getSelectedPopulations();
            }
            PopulationDataset[] standardPopulations = new PopulationDataset[populations.length];
            tableBuilder.setUnknownAgeCode(CanRegClientApp.getApplication().getGlobalToolBox().getUnknownAgeCode());

            if (tableBuilder.areThesePopulationDatasetsCompatible(populations)) {
                String fileName = null;
                // Choose file name;
                if (filetype != FileTypes.jchart && filetype != FileTypes.browser) {
                    if (chooser == null) {
                        path = localSettings.getProperty(LocalSettings.TABLES_PATH_KEY);
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
                        } catch (IOException ex) {
                            Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        // cancelled
                        return;
                    }
                }

                setCursor(hourglassCursor);

                int i = 0;
                String populationFilterString = "";
                for (PopulationDataset pop : populations) {
                    if (pop != null) {
                        int stdPopID = pop.getReferencePopulationID();
                        standardPopulations[i++] = populationDatasetsMap.get(stdPopID);
                        if (populationFilterString.trim().length() == 0) {
                            populationFilterString = pop.getFilter();
                        } else if (!populationFilterString.equalsIgnoreCase(pop.getFilter())) {
                            // population filters not matching on all the pds...
                            filterError = true;
                        }
                    }
                }

                Globals.StandardVariableNames[] variablesNeeded = tableBuilder.getVariablesNeeded();
                if (variablesNeeded != null) {
                    for (Globals.StandardVariableNames standardVariableName : variablesNeeded) {
                        variables.add(canreg.client.CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(standardVariableName.toString()));
                    }
                }
                DatabaseFilter filter = new DatabaseFilter();
                String tableName = Globals.TUMOUR_AND_PATIENT_JOIN_TABLE_NAME;
                String filterString = rangeFilterPanel.getFilter().trim();

                if (filterString.length() != 0) {
                    filterString += " AND ";
                }

                if (populationFilterString.length() != 0) {
                    filterString += "( " + populationFilterString + " ) AND ";
                }

                // add the years to the filter
                DatabaseVariablesListElement incidenceDate = canreg.client.CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.IncidenceDate.toString());
                filterString += incidenceDate.getDatabaseVariableName() + " BETWEEN '" + startYear * 10000 + "' AND '" + ((endYear + 1) * 10000 - 1) + "'";

                // filter only the confirmed cases
                DatabaseVariablesListElement recordStatus = canreg.client.CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString());
                filterString += " AND " + recordStatus.getDatabaseVariableName() + " = '1'";

                // filter away obsolete cases
                DatabaseVariablesListElement recordObsoleteStatus = canreg.client.CanRegClientApp.getApplication().getGlobalToolBox().translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString());
                filterString += " AND " + recordObsoleteStatus.getDatabaseVariableName() + " != '1'";

                filter.setFilterString(filterString);

                System.out.println(filterString);

                filter.setQueryType(DatabaseFilter.QueryType.FREQUENCIES_BY_YEAR);
                filter.setDatabaseVariables(variables);
                DistributedTableDataSourceClient tableDataSource;
                Object[][] incidenceData = null;
                
                try {
                    tableDatadescription = canreg.client.CanRegClientApp.getApplication().getDistributedTableDescription(filter, tableName, null);
                    tableDataSource = new DistributedTableDataSourceClient(tableDatadescription, null);
                    if (tableDatadescription.getRowCount() > 0) {
                        incidenceData = tableDataSource.retrieveRows(0, tableDatadescription.getRowCount());
                    } else {
                        // display error - no lines
                        JOptionPane.showMessageDialog(this,
                                "No incidence data available correspondign to the current filter, period and population.",
                                "No incidence data",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    // Build the table(s)
                    LinkedList<String> filesGenerated
                            = tableBuilder.buildTable(
                                    heading,
                                    fileName,
                                    startYear,
                                    endYear,
                                    incidenceData,
                                    populations,
                                    standardPopulations,
                                    configFields,
                                    tble.getEngineParameters(),
                                    filetype, 
                                    language
                            );

                    if (filetype != FileTypes.jchart) {

                        String filesGeneratedList = new String();
                        filesGeneratedList = filesGenerated.stream().map((fileN) -> "\n" + fileN).reduce(filesGeneratedList, String::concat);

                        setCursor(normalCursor);

                        // Opening the resulting files if the list is not empty...
                        if (filesGenerated.isEmpty()) {
                            if (filetype == FileTypes.browser) {
                                // TODO write proper message
                                JOptionPane.showMessageDialog(this,
                                        "Your browser will now launch. Please wait.", // (If not, please open a web browser and go to: http://127.0.0.1:5676/ )",
                                        java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("TABLE(S)_BUILT."),
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Please use \"View work files\" in the \"File\"-menu to open them",
                                        java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("TABLE(S)_BUILT."),
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    filesGeneratedList,
                                    java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("TABLE(S)_BUILT."),
                                    JOptionPane.INFORMATION_MESSAGE);
                            filesGenerated.stream().filter((resultFileName) -> (new File(resultFileName).exists())).forEachOrdered((resultFileName) -> {
                                try {
                                    canreg.common.Tools.openFile(resultFileName);
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(this, "Unable to open: " + resultFileName + "\n" + ex.getLocalizedMessage());
                                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            });
                        }
                    } else {
                        chartBuilder = (JChartTableBuilderInterface) tableBuilder;
                        JFreeChart[] charts = chartBuilder.getCharts();
                        for (JFreeChart chart : charts) {
                            JChartViewerInternalFrame chartViewerInternalFrame = new JChartViewerInternalFrame();
                            chartViewerInternalFrame.setChart(chart);
                            CanRegClientView.showAndPositionInternalFrame(
                                    CanRegClientApp.getApplication().getDesktopPane(),
                                    chartViewerInternalFrame);
                        }
                        setCursor(normalCursor);
                    }

                } catch (SQLException ex) {
                    setCursor(normalCursor);
                    JOptionPane.showMessageDialog(this, "Something wrong with the SQL query: \n" + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException | SecurityException | NotCompatibleDataException | DistributedTableDescriptionException | UnknownTableException ex) {
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TableErrorException ex) {
                    setCursor(normalCursor);
                    Logger.getLogger(TableBuilderInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(
                            this, "Something went wrong while building the table: \n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(normalCursor);
                }
            } else {
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("POPULATION_SET_NOT_COMPATIBLE"), java.util.ResourceBundle.getBundle("canreg/client/gui/analysis/resources/TableBuilderInternalFrame").getString("NO_TABLES_BUILT"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Action
    public void generatePStable() {
        generateTablesAction(FileTypes.ps);
    }

    @Action
    public void generateTabulatedTables() {
        generateTablesAction(FileTypes.html);
    }

    @Action
    public void generatePNGTable() {
        generateTablesAction(FileTypes.png);
    }

    @Action
    public void generatePDFtable() {
        generateTablesAction(FileTypes.pdf);
    }

    @Action
    public void generateSVGFile() {
        generateTablesAction(FileTypes.svg);
    }

    @Action
    public void generateWMFAction() {
        generateTablesAction(FileTypes.wmf);
    }

    @Action
    public void openInChartViewer() {
        generateTablesAction(FileTypes.jchart);
    }

    @Action
    public void generateFilesForSEERPrepAction() {
        generateTablesAction(FileTypes.seer);
    }

    @Action
    public void generateCSV() {
        generateTablesAction(FileTypes.csv);
    }

    @Action
    public void generateTIFF() {
        generateTablesAction(FileTypes.tiff);
    }

    @Action
    public void generateDOCX() {
        generateTablesAction(FileTypes.docx);
    }

    @Action
    public void generatePPTX() {
        generateTablesAction(FileTypes.pptx);
    }

    @Action
    public void launchBrowserAction() {
        generateTablesAction(FileTypes.browser);
    }

    @Action
    public void dontUsePopsCheckboxUpdated() {
        boolean enabled = !dontUsePopulationDatasetCheckBox.isSelected();
        populationDatasetsScrollPane.setVisible(enabled);
        pleaseChooseLabel.setVisible(enabled);
        tabbedPane.setEnabledAt(filterTabPos, !enabled);
    }

    private void initRangeFilterPanel() {
        rangeFilterPanel = new canreg.client.gui.components.RangeFilterPanel();

        filterPanel.setEnabled(false);
        filterPanel.setFocusable(false);
        filterPanel.setName("filterPanel"); // NOI18N
        filterPanel.setRequestFocusEnabled(false);

        rangeFilterPanel.setName("rangeFilterPanel"); // NOI18N
        rangeFilterPanel.initValues();

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
                filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(rangeFilterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
                filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(rangeFilterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }

}
