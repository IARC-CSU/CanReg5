package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.server.database.Patient;
import canreg.server.database.Tumour;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class Checks {
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
        SexHistology;
    }

    LinkedList<CheckInterface> checks;
    
    public Checks(GlobalToolBox toolBox){
        this.toolBox = toolBox;
        standardVariables = toolBox.getStandardVariables();
        checks = new LinkedList <CheckInterface> ();
        
        CheckInterface check;
        // Add basis check
        check = new CheckBasis();
        checks.add(check);
        // Add grade check
        check = new CheckGrade();
        checks.add(check);
        // Add age/topography
        check = new CheckAgeTopography();
        checks.add(check);
        // Add age/histology
        check = new CheckAgeHistology();
        checks.add(check);
        // Add age/topography/histology
        check = new CheckAgeTopographyHistology();
        checks.add(check);
        // Add sex/topography
        check = new CheckSexTopography();
        checks.add(check);
        // Add sex/topography
        check = new CheckSexHistology();
        checks.add(check);
    }
    
    public synchronized LinkedList<CheckResult> performChecks(Patient patient, Tumour tumour){
        LinkedList <CheckResult> results = new LinkedList <CheckResult>();

        // Build map of standard variables
        Map<Globals.StandardVariableNames,Object> variables = new LinkedHashMap();

        for (DatabaseVariablesListElement dbvle:standardVariables){
            String name = dbvle.getStandardVariableName();
            Globals.StandardVariableNames standardVariable = Globals.StandardVariableNames.valueOf(name);
            Object data;
            if (dbvle.getTable().equalsIgnoreCase("Patient")) {
                data = patient.getVariable(dbvle.getShortName());
            } else {
                data = tumour.getVariable(dbvle.getShortName());
            }
            variables.put(standardVariable, data);
        }
        for (CheckInterface check:checks){
            results.add(check.performCheck(variables));
        }
        return results;
    }
}
