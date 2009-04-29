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

    String fieldName;
    LinkedList<String> listOfValues;
    public ConfigFields(String name) {
        fieldName = name;
        listOfValues = new LinkedList();
    }
}
