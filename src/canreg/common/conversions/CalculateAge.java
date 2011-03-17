/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.common.conversions;

import canreg.common.DateHelper;
import canreg.common.Globals;
import canreg.common.Globals.StandardVariableNames;
import canreg.common.GregorianCalendarCanReg;
import canreg.common.conversions.Converter.ConversionName;
import java.text.ParseException;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class CalculateAge implements ConversionInterface {

    private static ConversionName conversionName = ConversionName.BirthIncidencetoAge;
    private static StandardVariableNames[] variablesNeeded = new StandardVariableNames[]{
        StandardVariableNames.BirthDate,
        StandardVariableNames.IncidenceDate
    };
    private static StandardVariableNames[] variablesCreated = new StandardVariableNames[]{
        StandardVariableNames.Age
    };

    @Override
    public ConversionName getConversionName() {
        return conversionName;
    }

    @Override
    public StandardVariableNames[] getVariablesNeeded() {
        return variablesNeeded;
    }

    @Override
    public StandardVariableNames[] getVariablesCreated() {
        return variablesCreated;
    }

    @Override
    public ConversionResult[] performConversion(Map<StandardVariableNames, Object> variables) {
        ConversionResult result = new ConversionResult();
        try {
            String birthDateCode = variables.get(StandardVariableNames.BirthDate).toString();
            GregorianCalendarCanReg bdcal = DateHelper.parseDateStringToGregorianCalendarCanReg(birthDateCode, Globals.DATE_FORMAT_STRING);
            String incidenceDateCode = variables.get(StandardVariableNames.IncidenceDate).toString();
            GregorianCalendarCanReg inccal = DateHelper.parseDateStringToGregorianCalendarCanReg(incidenceDateCode, Globals.DATE_FORMAT_STRING);
            int yearsBetween = (int) DateHelper.yearsBetween(inccal, bdcal);
            if (yearsBetween >= 0) {
                result.setValue(yearsBetween);
                result.setResultCode(ConversionResult.ResultCode.OK);
            }
        } catch (ParseException ex) {
            result.setResultCode(ConversionResult.ResultCode.Invalid);
            result.setMessage("Not a date,");
        } catch (NumberFormatException numberFormatException) {
            result.setResultCode(ConversionResult.ResultCode.Invalid);
            result.setMessage("Not a date,");
        } catch (IllegalArgumentException ex) {
            result.setResultCode(ConversionResult.ResultCode.Invalid);
            result.setMessage("Not a date,");
        } catch (NullPointerException nullPointerException) {
            result.setResultCode(ConversionResult.ResultCode.Missing);
            result.setMessage("Missing variable(s) needed.");
        }
        return new ConversionResult[]{result};
    }
}
