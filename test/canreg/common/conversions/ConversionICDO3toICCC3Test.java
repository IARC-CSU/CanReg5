package canreg.common.conversions;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link ConversionICDO3toICCC3}.
 */
public class ConversionICDO3toICCC3Test {

    private final ConversionICDO3toICCC3 conv = new ConversionICDO3toICCC3();

    @Test
    public void testMetadata() {
        assertEquals(Converter.ConversionName.ICDO3toICCC3, conv.getConversionName());
        assertEquals(4, conv.getVariablesNeeded().length);
        assertEquals(1, conv.getVariablesCreated().length);
        assertEquals(StandardVariableNames.ICCC, conv.getVariablesCreated()[0]);
    }

    @Test
    public void testLymphoidLeukaemia_Group1a() {
        // 9820/3 in bone marrow (421) -> Group 1a
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "421");
        vars.put(StandardVariableNames.Morphology, "9820");
        vars.put(StandardVariableNames.Behaviour, "3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("1a", res[0].getValue());
    }

    @Test
    public void testRetinoblastoma_Group5_NoSubgroup() {
        // 9510/3 in retina (692) -> Group 5 (no subgroup letter)
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "2");
        vars.put(StandardVariableNames.Topography, "692");
        vars.put(StandardVariableNames.Morphology, "9510");
        vars.put(StandardVariableNames.Behaviour, "3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("5", res[0].getValue());
    }

    @Test
    public void testNephroblastoma_Group6a() {
        // 8960/3 in kidney (649) -> Group 6a
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "649");
        vars.put(StandardVariableNames.Morphology, "8960");
        vars.put(StandardVariableNames.Behaviour, "3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("6a", res[0].getValue());
    }

    @Test
    public void testNonMalignantNonCns_Invalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "649");
        vars.put(StandardVariableNames.Morphology, "8960");
        vars.put(StandardVariableNames.Behaviour, "0");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.Invalid, res[0].getResultCode());
    }

    @Test
    public void testMissingAndInvalid() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(ConversionResult.ResultCode.Missing, conv.performConversion(vars)[0].getResultCode());

        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "649");
        vars.put(StandardVariableNames.Morphology, "7000"); // < 8000 -> Invalid
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(ConversionResult.ResultCode.Invalid, conv.performConversion(vars)[0].getResultCode());
    }

    @Test
    public void testNormalizedTopographyAndMorphologySlashAndMissingSex() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "C64.9");
        vars.put(StandardVariableNames.Morphology, "8960/3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("6a", res[0].getValue());
    }
}

