package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals.StandardVariableNames;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ervikm
 */
public class Checker {

    private GlobalToolBox toolBox;
    private LinkedList<DatabaseVariablesListElement> standardVariables;

    public static enum CheckNames {
        Grade,
        Basis,
        AgeTopography,
        AgeHistology,
        AgeTopographyHistology,
        TopographyBehaviour,
        SexTopography,
        SexMorphology,
        Morphology,
        Topography
    }
    LinkedList<CheckInterface> checks;

    public Checker(GlobalToolBox toolBox) {
        this.toolBox = toolBox;
        standardVariables = toolBox.getStandardVariables();
        Set<StandardVariableNames> variableExistSet = getVariableExistSet(standardVariables);

        checks = new LinkedList<CheckInterface>();

        CheckInterface check;
        // Add basis check  - DEPedits #10
        check = new CheckBasis();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add grade check  - DEPedits #9
        check = new CheckGrade();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add age/topography  - DEPedits #22
        check = new CheckAgeTopography();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add age/histology  - DEPedits #23
        check = new CheckAgeHistology();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add age/topography/histology - DEPedits #24
        check = new CheckAgeTopographyHistology();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add sex/topography  - DEPedits #20
        check = new CheckSexTopography();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add sex/morphology  - DEPedits #21
        check = new CheckSexMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add morphology - DEPedits #7
        check = new CheckMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
        // Add topography - DEPedits #6
        check = new CheckTopography();
        if (canPerformThisCheck(check, variableExistSet)) {
            checks.add(check);
        }
    }

    public synchronized LinkedList<CheckResult> performChecks(Patient patient, Tumour tumour) {
        LinkedList<CheckResult> results = new LinkedList<CheckResult>();

        // Build map of standard variables
        Map<StandardVariableNames, Object> variables = new LinkedHashMap();

        for (DatabaseVariablesListElement dbvle : standardVariables) {
            String name = dbvle.getStandardVariableName();
            StandardVariableNames standardVariable = StandardVariableNames.valueOf(name);
            Object data;
            if (dbvle.getTable().equalsIgnoreCase("Patient")) {
                data = patient.getVariable(dbvle.getShortName());
            } else {
                data = tumour.getVariable(dbvle.getShortName());
            }
            variables.put(standardVariable, data);
        }
        for (CheckInterface check : checks) {
            results.add(check.performCheck(variables));
        }
        return results;
    }

    private static boolean canPerformThisCheck(CheckInterface check, Set<StandardVariableNames> variableExistSet) {
        StandardVariableNames[] variablesNeeded = check.getVariablesNeeded();
        boolean canPerform = true;
        for (StandardVariableNames var : variablesNeeded) {
            canPerform = canPerform && variableExistSet.contains(var);
        }
        return canPerform;
    }

    private static Set<StandardVariableNames> getVariableExistSet(LinkedList<DatabaseVariablesListElement> list) {
        Set<StandardVariableNames> set = new HashSet<StandardVariableNames>();
        for (DatabaseVariablesListElement element : list) {
            set.add(StandardVariableNames.valueOf(element.getStandardVariableName()));
        }
        return set;
    }
}
