/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */

package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author ervikm
 */
public class Checker {

    private LinkedList<DatabaseVariablesListElement> standardVariables;

    /**
     * 
     */
    public static enum CheckNames {

        /**
         * 
         */
        Grade,
        /**
         * 
         */
        Basis,
        /**
         * 
         */
        AgeTopography,
        /**
         * 
         */
        AgeMorphology,
        /**
         * 
         */
        AgeTopographyMorphology,
        /**
         * 
         */
        TopographyBehaviour,
        /**
         * 
         */
        SexTopography,
        /**
         * 
         */
        SexMorphology,
        /**
         * 
         */
        Morphology,
        /**
         * 
         */
        Topography,
        /**
         * 
         */
        AgeIncidenceDateBirthDate,
        /**
         *
         */
        DateOfLastContact,
        /**
         *
         */
        TopographyMorphology,
        // <ictl.co>
        NCID
        //</ictl.co>
    }
    LinkedList<CheckInterface> checks;

    /**
     * 
     * @param toolBox
     */
    public Checker(LinkedList<DatabaseVariablesListElement> standardVariables) {
        this.standardVariables = standardVariables;
        // Create a set of all standard variables in this database
        Set<StandardVariableNames> variableExistSet = getVariableExistSet(standardVariables);
        Map<StandardVariableNames, DatabaseVariablesListElement> standardVariablesMap = buildStandardVariablesMap(standardVariables);

        checks = new LinkedList<CheckInterface>();

        CheckInterface check;
        // Add basis check  - DEPedits #10
        check = new CheckBasis();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add grade check  - DEPedits #9
        check = new CheckGrade();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add age/topography  - DEPedits #22
        check = new CheckAgeTopography();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add age/histology  - DEPedits #23
        check = new CheckAgeMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add age/topography/histology - DEPedits #24
        check = new CheckAgeTopographyMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add sex/topography  - DEPedits #20
        check = new CheckSexTopography();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add sex/morphology  - DEPedits #21
        check = new CheckSexMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add morphology - DEPedits #7
        check = new CheckMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add topography - DEPedits #6
        check = new CheckTopography();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add topography and morphology - DEPedits #18
        check = new CheckTopographyMorphology();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }

        // Add incidence birth date age check
        check = new CheckAgeIncidenceDateBirthDate();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
        // Add date of last contact check
        check = new CheckDateOfLastContact();
        if (canPerformThisCheck(check, variableExistSet)) {
            check.setVariableListElementsMap(standardVariablesMap);
            checks.add(check);
        }
    }

    /**
     * 
     * @param patient
     * @param tumour
     * @return
     */
    public synchronized LinkedList<CheckResult> performChecks(Patient patient, Tumour tumour) {
        LinkedList<CheckResult> results = new LinkedList<CheckResult>();

        // Build map of standard variables
        Map<StandardVariableNames, Object> variables = new LinkedHashMap();

        // Run through the standard variables one by one and pick up the necessary data
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

    // Create a set of all standard variables in this database
    private static Set<StandardVariableNames> getVariableExistSet(LinkedList<DatabaseVariablesListElement> list) {
        Set<StandardVariableNames> set = new HashSet<StandardVariableNames>();
        for (DatabaseVariablesListElement element : list) {
            try {
                StandardVariableNames standardVariable = StandardVariableNames.valueOf(element.getStandardVariableName());
                set.add(standardVariable);
            } catch (IllegalArgumentException iae) {
                // This should have been detected earlier...
                System.out.println("Invalid standard variable name");
            }
        }
        return set;
    }

    private Map<StandardVariableNames, DatabaseVariablesListElement> buildStandardVariablesMap(LinkedList<DatabaseVariablesListElement> standardVariables) {
        Map<StandardVariableNames, DatabaseVariablesListElement> map = new TreeMap<StandardVariableNames, DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement variable : standardVariables) {
            map.put(StandardVariableNames.valueOf(variable.getStandardVariableName()), variable);
        }
        return map;
    }
}
