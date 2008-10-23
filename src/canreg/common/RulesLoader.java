package canreg.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class RulesLoader {
    public static Map<Integer, String> load(LookUpFileDescription lookUpFileDescription) throws FileNotFoundException, IOException, URISyntaxException{
        Map<Integer, String> table = new LinkedHashMap <Integer, String>();
        File file = new File(lookUpFileDescription.getFileURL().toURI());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        // int codeLength = lookUpFileDescription.getCodeLength();
        int i = 0;
        while(line!=null){
            table.put(i, line);
            line = br.readLine();
            i++;
        }
        return table;
    }
}
