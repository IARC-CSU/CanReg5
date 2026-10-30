package canreg;

import canreg.client.LocalSettings;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.Tools;
import canreg.server.management.SystemDescription;
import java.io.File;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shared test fixtures and helper builders for CanReg5 unit tests.
 */
public final class TestFixtures {

    public static final File TEST_SYSTEM_XML = new File("test/canreg/test-system.xml");
    public static final File TRN_SYSTEM_XML = new File("conf/database/TRN.xml");
    public static final File TEST_SETTINGS_XML = new File("test/canreg/test-settings.xml");
    public static final File TEST_DICTIONARIES_JSON = new File("test/canreg/test-dictionaries.json");

    private TestFixtures() {
    }

    /**
     * Initializes headless LocalSettings in {@link Tools} for hermetic tests.
     */
    public static void initHeadlessLocalSettings() {
        LocalSettings localSettings = new LocalSettings(TEST_SETTINGS_XML);
        Tools.setLocalSettings(localSettings);
    }

    /**
     * Loads the test SystemDescription from {@code test/canreg/test-system.xml}.
     */
    public static SystemDescription loadTestSystemDescription() {
        initHeadlessLocalSettings();
        return new SystemDescription(TEST_SYSTEM_XML.getAbsolutePath());
    }

    /**
     * Loads the test system XML DOM Document from {@code test/canreg/test-system.xml}.
     */
    public static org.w3c.dom.Document loadTestSystemDocument() {
        return loadTestSystemDescription().getSystemDescriptionDocument();
    }

    /**
     * Creates a GlobalToolBox initialized from {@code test/canreg/test-system.xml}.
     */
    public static canreg.common.GlobalToolBox createGlobalToolBox() {
        return new canreg.common.GlobalToolBox(loadTestSystemDocument());
    }

    /**
     * Returns the {@code test/canreg/test-system.xml} File.
     */
    public static File getTestSystemXmlFile() {
        return TEST_SYSTEM_XML;
    }

    /**
     * Builds a map of standard variable names to DatabaseVariablesListElements
     * suitable for passing to {@code CheckInterface.setVariableListElementsMap}.
     */
    public static Map<Globals.StandardVariableNames, DatabaseVariablesListElement> createStandardVariablesMap() {
        Map<Globals.StandardVariableNames, DatabaseVariablesListElement> map =
                new TreeMap<Globals.StandardVariableNames, DatabaseVariablesListElement>();

        DatabaseVariablesListElement ageVar = new DatabaseVariablesListElement("Tumour", 1, "AGE", "Number");
        ageVar.setVariableLength(3);
        ageVar.setUnknownCode("999");
        ageVar.setStandardVariableName(Globals.StandardVariableNames.Age.toString());
        map.put(Globals.StandardVariableNames.Age, ageVar);

        DatabaseVariablesListElement birthDateVar = new DatabaseVariablesListElement("Patient", 2, "BIRTHD", "Date");
        birthDateVar.setVariableLength(8);
        birthDateVar.setUnknownCode("99999999");
        birthDateVar.setStandardVariableName(Globals.StandardVariableNames.BirthDate.toString());
        map.put(Globals.StandardVariableNames.BirthDate, birthDateVar);

        DatabaseVariablesListElement incidenceDateVar = new DatabaseVariablesListElement("Tumour", 3, "INCID", "Date");
        incidenceDateVar.setVariableLength(8);
        incidenceDateVar.setUnknownCode("99999999");
        incidenceDateVar.setStandardVariableName(Globals.StandardVariableNames.IncidenceDate.toString());
        map.put(Globals.StandardVariableNames.IncidenceDate, incidenceDateVar);

        DatabaseVariablesListElement dlcVar = new DatabaseVariablesListElement("Patient", 4, "DLC", "Date");
        dlcVar.setVariableLength(8);
        dlcVar.setUnknownCode("99999999");
        dlcVar.setStandardVariableName(Globals.StandardVariableNames.Lastcontact.toString());
        map.put(Globals.StandardVariableNames.Lastcontact, dlcVar);

        DatabaseVariablesListElement sexVar = new DatabaseVariablesListElement("Patient", 5, "SEX", "Dict");
        sexVar.setVariableLength(1);
        sexVar.setStandardVariableName(Globals.StandardVariableNames.Sex.toString());
        map.put(Globals.StandardVariableNames.Sex, sexVar);

        DatabaseVariablesListElement topVar = new DatabaseVariablesListElement("Tumour", 6, "TOP", "Dict");
        topVar.setVariableLength(3);
        topVar.setStandardVariableName(Globals.StandardVariableNames.Topography.toString());
        map.put(Globals.StandardVariableNames.Topography, topVar);

        DatabaseVariablesListElement morVar = new DatabaseVariablesListElement("Tumour", 7, "MOR", "Dict");
        morVar.setVariableLength(4);
        morVar.setStandardVariableName(Globals.StandardVariableNames.Morphology.toString());
        map.put(Globals.StandardVariableNames.Morphology, morVar);

        DatabaseVariablesListElement behVar = new DatabaseVariablesListElement("Tumour", 8, "BEH", "Dict");
        behVar.setVariableLength(1);
        behVar.setStandardVariableName(Globals.StandardVariableNames.Behaviour.toString());
        map.put(Globals.StandardVariableNames.Behaviour, behVar);

        DatabaseVariablesListElement basVar = new DatabaseVariablesListElement("Tumour", 9, "BAS", "Dict");
        basVar.setVariableLength(1);
        basVar.setStandardVariableName(Globals.StandardVariableNames.BasisDiagnosis.toString());
        map.put(Globals.StandardVariableNames.BasisDiagnosis, basVar);

        DatabaseVariablesListElement gradeVar = new DatabaseVariablesListElement("Tumour", 10, "GRADE", "Dict");
        gradeVar.setVariableLength(1);
        gradeVar.setStandardVariableName(Globals.StandardVariableNames.Grade.toString());
        map.put(Globals.StandardVariableNames.Grade, gradeVar);

        return map;
    }

    /**
     * Returns the standard variables as a LinkedList for {@code Checker} and {@code Converter}.
     */
    public static LinkedList<DatabaseVariablesListElement> createStandardVariablesList() {
        return new LinkedList<DatabaseVariablesListElement>(createStandardVariablesMap().values());
    }
}

