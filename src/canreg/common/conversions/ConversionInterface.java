/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common.conversions;

import canreg.common.Globals;
import canreg.common.conversions.Converter.ConversionName;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public interface ConversionInterface {

    /**
     * 
     * @return
     */
    public ConversionName getConversionName();
    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesNeeded();
    /**
     * 
     * @return
     */
    public Globals.StandardVariableNames[] getVariablesCreated();
    /**
     * 
     * @param variables
     * @return
     */
    public ConversionResult[] performConversion(Map<Globals.StandardVariableNames, Object> variables);
}
