package canreg.common;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
class Translation {
    private Map<Object, Object> translationMap;
    private Object defaultValue;

    protected Translation() {
        translationMap = new HashMap<Object, Object>();
    }

    protected void setTranslation(Map<Object,Object> translationMap){
        this.translationMap = translationMap;
    }

    protected Object translate(Object originalValue){
        Object returnValue = translationMap.get(originalValue);
        if (returnValue==null){
            returnValue = defaultValue;
        }
        return returnValue;
    }

    /**
     * @return the defaultValue
     */
    protected Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    protected void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    void addTranslation(Object value, Object translatedValue) {
        translationMap.put(value, translatedValue);
    }
}