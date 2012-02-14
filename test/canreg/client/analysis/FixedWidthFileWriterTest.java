/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.client.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ErvikM
 */
public class FixedWidthFileWriterTest {
    
    public FixedWidthFileWriterTest() {
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
     * Test of setOutputFileName method, of class FixedWidthFileWriter.
     */
    @Test
    public void testSetOutputFileName() {
        System.out.println("setOutputFileName");
        String fileName = "C:\\Documents and Settings\\ervikm\\My Documents\\NetBeansProjects\\CanReg\\src\\canreg\\common\\ruby\\export_format_naaccr1946.ver11_3.d02032011.tsv";
        FixedWidthFileWriter instance = null;
        try {
            instance = new FixedWidthFileWriter(1946);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FixedWidthFileWriterTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FixedWidthFileWriterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean expResult = false;
        boolean result = instance.setOutputFileName("test");
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeLine method, of class FixedWidthFileWriter.
     */
    @Test
    public void testWriteLine() {
        System.out.println("writeLine");
        Map lineElements = null;
        FixedWidthFileWriter instance = null;
        boolean expResult = false;
        boolean result = instance.writeLine(lineElements);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of defineOrder method, of class FixedWidthFileWriter.
     */
    @Test
    public void testDefineOrder() {
        System.out.println("defineOrder");
        String[] order = null;
        FixedWidthFileWriter instance = null;
        instance.defineOrder(order);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
