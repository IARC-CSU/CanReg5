package canreg.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class RulesLoader {
    /**
     * 
     * @param resourceAsStream
     * @param topographyRule9CodeLength
     * @return
     * @throws java.io.IOException
     */
    public static Map<Integer, String> load(InputStream resourceAsStream, int topographyRule9CodeLength) throws IOException {
                Map<Integer, String> table = new LinkedHashMap <Integer, String>();
                
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
        
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
