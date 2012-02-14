/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common.qualitycontrol;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ervikm
 */
public class CheckGradeTest {

    public CheckGradeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getVariablesNeeded method, of class CheckGrade.
     */
    @Test
    public void testGetVariablesNeeded() {
        System.out.println("getVariablesNeeded");
        CheckGrade instance = new CheckGrade();
        StandardVariableNames[] expResult = new StandardVariableNames[]{
            StandardVariableNames.Behaviour,
            StandardVariableNames.Morphology,
            StandardVariableNames.Grade
        };
        StandardVariableNames[] result = instance.getVariablesNeeded();
        assertEquals(expResult, result);

    }

    /**
     * Test of performCheck method, of class CheckGrade.
     */
    @Test
    public void testPerformCheck() {
        System.out.println("performCheck");
        Map<StandardVariableNames, Object> variables = new LinkedHashMap<StandardVariableNames, Object>();
        variables.put(StandardVariableNames.Behaviour, "3");
        variables.put(StandardVariableNames.Grade, "3");
        variables.put(StandardVariableNames.Morphology, "8331");
        CheckGrade instance = new CheckGrade();
        CheckResult.ResultCode expResult = CheckResult.ResultCode.Invalid;
        CheckResult result = instance.performCheck(variables);
        System.out.println(result);
        assertEquals(expResult, result.getResultCode());
    }
}