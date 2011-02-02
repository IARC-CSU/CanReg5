package canreg.client.analysis;

/**
 * <p>Title: CI5-IX tools</p>
 *
 * <p>Description: Various tools for CI5-IX</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: IARC-DEP</p>
 *
 * @author Morten Johannes Ervik
 * @version 1.0
 */
import java.util.LinkedList;

public class ConfigFields {

    private String fieldName;
    private LinkedList<String> listOfValues;

    public ConfigFields(String name) {
        fieldName = name;
        listOfValues = new LinkedList();
    }

    /**
     * @return the listOfValues
     */
    public LinkedList<String> getListOfValues() {
        return listOfValues;
    }

    public void addValue(String value){
        listOfValues.add(value);
    }

    public boolean containsValue(String name) {
        for (String fieldNameString : listOfValues) {
            if (fieldNameString.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getFieldName() {
        return fieldName;
    }
}
