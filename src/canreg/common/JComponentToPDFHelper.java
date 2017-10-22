//<ictl.co>
package canreg.common;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by amin on 8/10/2015.
 */
public class JComponentToPDFHelper {
    public static final String DEFAUL_TTFONT_NAME = "arial.ttf";
    public static final String DEFAUL_FONT_DIRECTORY = "/fonts/";
    public static final String DEFAUL_TTFONT_PATH = DEFAUL_FONT_DIRECTORY + DEFAUL_TTFONT_NAME;
    public static final String WORKING_DIR;

    JComponentToPDFHelper() {
    }

    public static Font getDefaultTTFont() throws IOException, DocumentException {
        return getTTFont(DEFAUL_TTFONT_NAME);
    }

    public static Font getResourceTTFont(String name) throws IOException, DocumentException {
        FontFactory.registerDirectory(JComponentToPDF.class.getResource(DEFAUL_FONT_DIRECTORY).getPath(), true);
        InputStream is = BaseFont.getResourceStream(DEFAUL_FONT_DIRECTORY + name);
        byte[] b;
        b = RandomAccessFileOrArray.InputStreamToArray(is);
        BaseFont ttfBaseFont = BaseFont.createFont(name, BaseFont.IDENTITY_H, false, true, b, null);
        Font font = new Font(ttfBaseFont);
        return font;
    }

    public static Font getTTFont(String name) throws IOException, DocumentException {
        BaseFont ttfBaseFont = BaseFont.createFont("file://"+WORKING_DIR + "/fonts/" + name, BaseFont.IDENTITY_H, false);
        Font font = new Font(ttfBaseFont);
        return font;
    }

    static {
        String _temp = JComponentToPDFHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        WORKING_DIR = _temp.substring(0, _temp.lastIndexOf("/"));
    }
}
//</ictl.co>