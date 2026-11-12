package canreg.common.conversions;

import canreg.common.Globals.StandardVariableNames;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link ConversionICDO3toICD10}.
 */
public class ConversionICDO3toICD10Test {

    private final ConversionICDO3toICD10 conv = new ConversionICDO3toICD10();

    @Test
    public void testMetadata() {
        assertEquals(Converter.ConversionName.ICDO3toICD10, conv.getConversionName());
        assertEquals(4, conv.getVariablesNeeded().length);
        assertEquals(1, conv.getVariablesCreated().length);
        assertEquals(StandardVariableNames.ICD10, conv.getVariablesCreated()[0]);
    }

    @Test
    public void testBreastInfiltratingDuctCarcinoma_C509() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "2");
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Morphology, "8500");
        vars.put(StandardVariableNames.Behaviour, "3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("C509", res[0].getValue());
    }

    @Test
    public void testLiverHepatocellularCarcinoma_C220() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "220");
        vars.put(StandardVariableNames.Morphology, "8170");
        vars.put(StandardVariableNames.Behaviour, "3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("C220", res[0].getValue());
    }

    @Test
    public void testMorphologySpecificMelanomaSkin_C43() {
        // 8720/3 in skin of trunk (445) -> C435
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "445");
        vars.put(StandardVariableNames.Morphology, "8720");
        vars.put(StandardVariableNames.Behaviour, "3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("C435", res[0].getValue());
    }

    @Test
    public void testInSituCervix_D06() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Sex, "2");
        vars.put(StandardVariableNames.Topography, "539");
        vars.put(StandardVariableNames.Morphology, "8070");
        vars.put(StandardVariableNames.Behaviour, "2");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("D069", res[0].getValue());
    }

    @Test
    public void testInvalidAndMissingInputs() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        assertEquals(ConversionResult.ResultCode.Missing, conv.performConversion(vars)[0].getResultCode());

        vars.put(StandardVariableNames.Sex, "1");
        vars.put(StandardVariableNames.Topography, "509");
        vars.put(StandardVariableNames.Morphology, "9999");
        vars.put(StandardVariableNames.Behaviour, "3");
        assertEquals(ConversionResult.ResultCode.Invalid, conv.performConversion(vars)[0].getResultCode());
    }

    @Test
    public void testNormalizedTopographyAndMorphologySlashAndMissingSex() {
        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "C50.9");
        vars.put(StandardVariableNames.Morphology, "8500/3");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("C509", res[0].getValue());
    }

    @Test
    public void testInferBehaviourWhenMissing() {
        assertEquals("3", conv.inferBehaviourFromMorphology("8500"));
        assertEquals("2", conv.inferBehaviourFromMorphology("8077"));
        assertEquals("1", conv.inferBehaviourFromMorphology("8442"));
        assertEquals("0", conv.inferBehaviourFromMorphology("8146"));

        Map<StandardVariableNames, Object> vars = new LinkedHashMap<StandardVariableNames, Object>();
        vars.put(StandardVariableNames.Topography, "C53.9");
        vars.put(StandardVariableNames.Morphology, "8077");

        ConversionResult[] res = conv.performConversion(vars);
        assertEquals(ConversionResult.ResultCode.OK, res[0].getResultCode());
        assertEquals("D069", res[0].getValue());
    }
}

