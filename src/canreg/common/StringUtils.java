//<ictl.co>
package canreg.common;

import java.io.UnsupportedEncodingException;

/**
 * Created by amin on 8/10/2015.
 */
public class StringUtils {
    StringUtils() {
    }

    public static boolean isNumeric(String str) {
        return str.matches("[+-]?\\d*(\\.\\d+)?");
    }

    public static boolean isAscii(String str){
        try {
            byte[] buffer = str.getBytes("UTF-8");
            for(int i=0;i<buffer.length;++i){
                if(buffer[i] > 0 && buffer[i] < 0x7A){
                    return true;
                }
            }
            return false;
        } catch (UnsupportedEncodingException e) {
            return true;
        }
    }
}

//</ictl.co>