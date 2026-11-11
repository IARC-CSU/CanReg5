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

    @Test
    public void testBlockedDateStillEvaluatesMonthAndDaySimilarity() {
        DatabaseVariablesListElement firstnVar = new DatabaseVariablesListElement("Patient", 5, "FIRSTN", "Alpha");
        DatabaseVariablesListElement raceVar = new DatabaseVariablesListElement("Patient", 6, "RACE", "Dict");

        DefaultPersonSearch trnSearcher = new DefaultPersonSearch(new DatabaseVariablesListElement[]{
            famnVar, firstnVar, birthdVar, sexVar, raceVar
        });

        PersonSearchVariable psvFamn = new PersonSearchVariable();
        psvFamn.setVariable(famnVar);
        psvFamn.setWeight(25f);
        psvFamn.setAlgorithm(CompareAlgorithms.soundex);

        PersonSearchVariable psvFirstn = new PersonSearchVariable();
        psvFirstn.setVariable(firstnVar);
        psvFirstn.setWeight(20f);
        psvFirstn.setAlgorithm(CompareAlgorithms.soundex);

        PersonSearchVariable psvBirthd = new PersonSearchVariable();
        psvBirthd.setVariable(birthdVar);
        psvBirthd.setWeight(16f);
        psvBirthd.setYearRange(0);
        psvBirthd.setAlgorithm(CompareAlgorithms.date);
        psvBirthd.setBlock(true);

        PersonSearchVariable psvSex = new PersonSearchVariable();
        psvSex.setVariable(sexVar);
        psvSex.setWeight(5f);
        psvSex.setAlgorithm(CompareAlgorithms.code);
        psvSex.setBlock(true);

        PersonSearchVariable psvRace = new PersonSearchVariable();
        psvRace.setVariable(raceVar);
        psvRace.setWeight(8f);
        psvRace.setAlgorithm(CompareAlgorithms.code);
        psvRace.setBlock(true);

        trnSearcher.setSearchVariables(new PersonSearchVariable[]{
            psvFamn, psvFirstn, psvBirthd, psvSex, psvRace
        });

        // Same surname soundex, sex, race, and birth year (1910), but different first name and month/day
        Patient p1 = new Patient();
        p1.setVariable("FAMN", "SOUZA");
        p1.setVariable("FIRSTN", "LOGOSE");
        p1.setVariable("BIRTHD", "19100305");
        p1.setVariable("SEX", "2");
        p1.setVariable("RACE", "W");

        Patient p2 = new Patient();
        p2.setVariable("FAMN", "SOUZA");
        p2.setVariable("FIRSTN", "DOROSI");
        p2.setVariable("BIRTHD", "19101124");
        p2.setVariable("SEX", "2");
        p2.setVariable("RACE", "W");

        // BirthD should score 45 (same year only), not 100:
        // Total = (25*100 + 20*0 + 16*45 + 5*100 + 8*100) / 7400 = 4520 / 7400 = 61.081% (< 70% threshold)
        float score = trnSearcher.compare(p1, p2);
        assertEquals(4520f / 74f, score, 0.01f);
        assertTrue("Expected score below 70% threshold, got: " + score, score < 70.0f);
    }

    @Test
    public void testBlockedVariableWithMissingOrUnknownValueReturnsZero() {
        PersonSearchVariable psvBirth = new PersonSearchVariable();
        psvBirth.setVariable(birthdVar);
        psvBirth.setWeight(50f);
        psvBirth.setAlgorithm(CompareAlgorithms.date);
        psvBirth.setBlock(true);

        PersonSearchVariable psvSex = new PersonSearchVariable();
        psvSex.setVariable(sexVar);
        psvSex.setWeight(50f);
        psvSex.setAlgorithm(CompareAlgorithms.code);
        psvSex.setBlock(true);

        searcher.setSearchVariables(new PersonSearchVariable[]{psvBirth, psvSex});

        Patient p1 = new Patient();
        p1.setVariable("BIRTHD", "");
        p1.setVariable("SEX", "2");

        Patient p2 = new Patient();
        p2.setVariable("BIRTHD", "19531020");
        p2.setVariable("SEX", "2");

        assertEquals(0.0f, searcher.compare(p1, p2), 0.01f);

        Patient p3 = new Patient();
        p3.setVariable("BIRTHD", "19531020");
        p3.setVariable("SEX", "9"); // Unknown code for sex

        assertEquals(0.0f, searcher.compare(p2, p3), 0.01f);
    }

    @Test
    public void testCompareDateYearRangeAndCompareNumber() {
        DatabaseVariablesListElement ageVar = new DatabaseVariablesListElement("Patient", 7, "AGE", "Number");
        DefaultPersonSearch numDateSearcher = new DefaultPersonSearch(new DatabaseVariablesListElement[]{
            birthdVar, ageVar
        });

        PersonSearchVariable psvBirth = new PersonSearchVariable();
        psvBirth.setVariable(birthdVar);
        psvBirth.setWeight(50f);
        psvBirth.setYearRange(1);
        psvBirth.setAlgorithm(CompareAlgorithms.date);

        PersonSearchVariable psvAge = new PersonSearchVariable();
        psvAge.setVariable(ageVar);
        psvAge.setWeight(50f);
        psvAge.setAlgorithm(CompareAlgorithms.number);

        numDateSearcher.setSearchVariables(new PersonSearchVariable[]{psvBirth, psvAge});

        // Years far apart (1920 vs 2005) and different ages (40 vs 35 -> 50 similarity)
        Patient p1 = new Patient();
        p1.setVariable("BIRTHD", "19200110");
        p1.setVariable("AGE", "40");

        Patient p2 = new Patient();
        p2.setVariable("BIRTHD", "20050327");
        p2.setVariable("AGE", "35");

        // Date similarity = 0 (different century, month, day); Age similarity = 50 -> total = 25%
        assertEquals(25.0f, numDateSearcher.compare(p1, p2), 0.01f);
    }
}

