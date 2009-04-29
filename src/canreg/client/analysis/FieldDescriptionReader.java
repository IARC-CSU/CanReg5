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

class FieldDescriptionReader extends DescriptionReader {
    public FieldDescriptionReader() {
    }

    public <FieldDescription> LinkedList readFile(FileReader file) {
        String word = readWord(file);
        LinkedList<FieldDescription> li = new LinkedList();
        while (!word.equals("EOF")) {
            if (word.equals("dictionary")) {
                li = (LinkedList<FieldDescription>) readDictionary(file);
                break;
            }
            word = readWord(file);
        }
        return li;
    }

    public LinkedList<FieldDescription> readDictionary(FileReader file) {
        int offsetCounter = 1;
        LinkedList<FieldDescription> list = new LinkedList();
        String word;
        boolean end = false;
        while (!end) {
            word = readWord(file);
            if (word.equals("{")) {
                // Do nothing
            } else if (word.length() > 7 &&
                       word.subSequence(0, 7).equals("_column")) {
                offsetCounter = extractInteger(word);
            } else if (word.equals("}")) {
                end = true;
            } else if (word.equals("")) {
                // Do nothing
            } else {
                FieldDescription field = new FieldDescription(readWord(file));
                field.contentType = word;
                field.characters = extractInteger(readWord(file));
                field.description = readWord(file);
                field.offset = offsetCounter;
                offsetCounter += field.characters;
                list.add(field);
            }
        }
        return list;
    }
}
