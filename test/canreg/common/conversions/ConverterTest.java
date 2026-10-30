package canreg.common.conversions;

import canreg.TestFixtures;
import canreg.common.database.Patient;
import canreg.common.database.Tumour;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Converter}.
 */
public class ConverterTest {

    @Test
    public void testPerformConversionsOnPatientAndTumour() {
        Converter converter = new Converter(TestFixtures.createStandardVariablesList());

        Patient patient = new Patient();
        patient.setVariable("SEX", "2");

        Tumour tumour = new Tumour();
        tumour.setVariable("TOP", "509");
        tumour.setVariable("MOR", "8500");
        tumour.setVariable("BEH", "3");

        ConversionResult[] icd10Results = converter.performConversion(
                Converter.ConversionName.ICDO3toICD10, patient, tumour);
        assertNotNull(icd10Results);
        assertEquals(ConversionResult.ResultCode.OK, icd10Results[0].getResultCode());
        assertEquals("C509", icd10Results[0].getValue());

        ConversionResult[] iccc3Results = converter.performConversion(
                Converter.ConversionName.ICDO3toICCC3, patient, tumour);
        assertNotNull(iccc3Results);
        assertEquals(ConversionResult.ResultCode.OK, iccc3Results[0].getResultCode());
        assertEquals("11f", iccc3Results[0].getValue());
    }
}

