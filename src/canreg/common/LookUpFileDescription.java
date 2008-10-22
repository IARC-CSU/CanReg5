/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package canreg.common;

import java.net.URL;

/**
 *
 * @author ervikm
 */
public class LookUpFileDescription {
    private int codeLength;
    private URL url;

    public LookUpFileDescription(URL url, int codeLength) {
        this.url = url;
        this.codeLength = codeLength;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public URL getFileURL() {
        return url;
    }
}
