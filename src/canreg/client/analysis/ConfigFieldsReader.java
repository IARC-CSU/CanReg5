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

import java.io.FileReader;
import java.util.LinkedList;

public class ConfigFieldsReader extends DescriptionReader {

    public static LinkedList<ConfigFields> readFile(FileReader file) {
        String lastWord = readWord(file);
        String word = null;
        LinkedList<ConfigFields> li = new LinkedList<ConfigFields>();
        if (!lastWord.equals("EOF")) {
            word = readWord(file);
        } while (!word.equals("EOF")) {
            if (word.equals("{")) {
                li.add(readConfig(lastWord, file));
            }
            lastWord = word;
            word = readWord(file);
        }
        return li;
    }

    private static ConfigFields readConfig(String fieldName,
                                   FileReader file) {
        LinkedList list = new LinkedList();
        ConfigFields fieldDesc = new ConfigFields(
                fieldName);
        String word;
        boolean end = false;
        while (!end) {
            word = readWord(file);
            if (word.equals("{")) {
                // Do nothing
            } else if (word.equals("}")) {
                end = true;
            } else if (word.equals("")) {
                // Do nothing
            } else {
                String trans = new String();
                trans = word;
                fieldDesc.addValue(trans);
            }
        }
        return fieldDesc;
    }

    public static String[] findConfig(String name, LinkedList<ConfigFields> list) {
        ConfigFields cf = null;
        String[] sa = null;
        boolean found = false;
        int m = 0;
        while (!found && m < list.size()) {
            cf = list.get(m++);
            found = cf.getFieldName().equals(name);
        }
        if (found) {
            Object[] oa = cf.getListOfValues().toArray();
            sa = new String[oa.length];
            for (int n = 0; n < oa.length; n++) {
                sa[n] = cf.getListOfValues().get(n);
            }
        }
        return sa;
    }
}
