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
public class LookUpLoader {
    public static Map<String, String> load(LookUpFileDescription lookUpFileDescription) throws FileNotFoundException, IOException, URISyntaxException{
        Map<String, String> table = new LinkedHashMap <String, String>();
        File file = new File(lookUpFileDescription.getFileURL().toURI());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        int codeLength = lookUpFileDescription.getCodeLength();
        while(line!=null){
            table.put(line.substring(0,codeLength), line.substring(codeLength));
            line = br.readLine();
        }
        return table;
    }
}
