/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.server.database;

import canreg.client.gui.importers.Import;
import canreg.server.CanRegServerImpl;
import canreg.server.CanRegServerInterface;
import java.io.File;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 *
 * @author ervikm
 */
public class ImportTest {

    public ImportTest() {
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
     * Test of importFile method, of class Import.
     */
    @Test
    public void importFile() {
        try {
            System.out.println("importFile");
            CanRegServerInterface server = new CanRegServerImpl("TRN");
            Document doc = server.getDatabseDescription();
            System.out.println(QueryGenerator.strSavePatient(doc));
            File file = new File("test/TRN-all.TXT");
            CanRegDAO canRegDAO =  server.getDatabseConnection();
            boolean expResult = true;
            boolean result = false;
            // result = Import.importFile(doc, file, canRegDAO);
            // result = canRegDAO.restoreDatabase("C:\\Temp\\2008-06-19\\CanReg5");
            assertEquals(expResult, result);
            
            // fail("The test case is a prototype.");
        } catch (RemoteException ex) {
            Logger.getLogger(ImportTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

}