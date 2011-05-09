package canreg.common;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class Translator {
    Map<String, Translation> translationsMap;

    public Translator(){
        translationsMap = new HashMap<String, Translation>();
    }

    public Object translate(String variableName, Object value){
        Translation translation = translationsMap.get(variableName);
        Object returnValue = value;
        if (translation!=null){
            value = translation.translate(value);
        }
        return returnValue;
    }

    public void addTranslation(String variableName, Object value, Object translatedValue){
        Translation translation = translationsMap.get(variableName);
        if (translation==null){
            translation = new Translation(variableName);
            translationsMap.put(variableName, translation);
        }
        translation.addTranslation(value, translatedValue);
    }
}
