package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.PersonSearchVariable;
import canreg.common.database.Patient;
import canreg.common.qualitycontrol.PersonSearcher.CompareAlgorithms;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link DefaultPersonSearch}.
 */
public class DefaultPersonSearchTest {

    private DefaultPersonSearch searcher;
    private DatabaseVariablesListElement famnVar;
    private DatabaseVariablesListElement birthdVar;
    private DatabaseVariablesListElement sexVar;
    private DatabaseVariablesListElement addrVar;

    @Before
    public void setUp() {
        famnVar = new DatabaseVariablesListElement("Patient", 1, "FAMN", "Alpha");
        birthdVar = new DatabaseVariablesListElement("Patient", 2, "BIRTHD", "Date");
        birthdVar.setUnknownCode("99999999");
        sexVar = new DatabaseVariablesListElement("Patient", 3, "SEX", "Dict");
        sexVar.setUnknownCode("9");
        addrVar = new DatabaseVariablesListElement("Patient", 4, "ADDR", "Dict");

        searcher = new DefaultPersonSearch(new DatabaseVariablesListElement[]{
            famnVar, birthdVar, sexVar, addrVar
        });

        PersonSearchVariable psvName = new PersonSearchVariable();
        psvName.setVariable(famnVar);
        psvName.setWeight(50f);
        psvName.setAlgorithm(CompareAlgorithms.alpha);

        PersonSearchVariable psvBirth = new PersonSearchVariable();
        psvBirth.setVariable(birthdVar);
        psvBirth.setWeight(30f);
        psvBirth.setYearRange(2);
        psvBirth.setAlgorithm(CompareAlgorithms.date);

        PersonSearchVariable psvSex = new PersonSearchVariable();
        psvSex.setVariable(sexVar);
        psvSex.setWeight(20f);
        psvSex.setAlgorithm(CompareAlgorithms.code);

        searcher.setSearchVariables(new PersonSearchVariable[]{psvName, psvBirth, psvSex});
    }

    @Test
    public void testIdenticalPatientsScore100Percent() {
        Patient p1 = new Patient();
        p1.setVariable("FAMN", "SMITH");
        p1.setVariable("BIRTHD", "19700415");
        p1.setVariable("SEX", "1");

        Patient p2 = new Patient();
        p2.setVariable("FAMN", "SMITH");
        p2.setVariable("BIRTHD", "19700415");
        p2.setVariable("SEX", "1");

        assertEquals(100.0f, searcher.compare(p1, p2), 0.01f);
    }

    @Test
    public void testCompletelyDifferentPatientsScoreLow() {
        Patient p1 = new Patient();
        p1.setVariable("FAMN", "SMITH");
        p1.setVariable("BIRTHD", "19700415");
        p1.setVariable("SEX", "1");

        Patient p2 = new Patient();
        p2.setVariable("FAMN", "GONZALEZ");
        p2.setVariable("BIRTHD", "20051128");
        p2.setVariable("SEX", "2");

        float score = searcher.compare(p1, p2);
        assertTrue("Expected low score for completely different records, got: " + score, score < 30.0f);
    }

    @Test
    public void testBlockedVariableMismatchReturnsZero() {
        PersonSearchVariable psvSoundex = new PersonSearchVariable();
        psvSoundex.setVariable(famnVar);
        psvSoundex.setWeight(50f);
        psvSoundex.setAlgorithm(CompareAlgorithms.soundex);
        psvSoundex.setBlock(true);

        PersonSearchVariable psvSex = new PersonSearchVariable();
        psvSex.setVariable(sexVar);
        psvSex.setWeight(50f);
        psvSex.setAlgorithm(CompareAlgorithms.exact);

        searcher.setSearchVariables(new PersonSearchVariable[]{psvSoundex, psvSex});

        Patient p1 = new Patient();
        p1.setVariable("FAMN", "ROBERT");
        p1.setVariable("SEX", "1");

        Patient p2 = new Patient();
        p2.setVariable("FAMN", "RUPERT"); // Same soundex R163
        p2.setVariable("SEX", "1");

        assertEquals(100.0f, searcher.compare(p1, p2), 0.01f);

        Patient p3 = new Patient();
        p3.setVariable("FAMN", "ANDERSON"); // Different soundex, blocked -> 0
        p3.setVariable("SEX", "1");

        assertEquals(0.0f, searcher.compare(p1, p3), 0.01f);
    }

    @Test
    public void testGetPatientVariablesAndCompareDataOnly() {
        Patient p1 = new Patient();
        p1.setVariable("FAMN", "SMITH");
        p1.setVariable("BIRTHD", "19700415");
        p1.setVariable("SEX", "1");
        p1.setVariable("PRID", "0001");

        Object[] vars1 = searcher.getPatientVariables(p1, "PRID");
        assertEquals(4, vars1.length);
        assertEquals("0001", vars1[3]);
        assertEquals(100.0f, searcher.compareDataOnly(vars1, vars1), 0.01f);
    }
}

