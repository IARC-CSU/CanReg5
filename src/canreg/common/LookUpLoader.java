package canreg.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
public class LookUpLoader {
    /**
     * 
     * @param inStream
     * @param codeLength
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static Map<String, String> load(InputStream inStream, int codeLength) throws FileNotFoundException, IOException, URISyntaxException{
        Map<String, String> table = new LinkedHashMap <String, String>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        String line = br.readLine();
        while(line!=null){
            table.put(line.substring(0,codeLength), line.substring(codeLength));
            line = br.readLine();
        }
        return table;
    }
}
