package canreg.common;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PsToPdfConverter {

    private String GSC = "";
    private String GSCARGS = " -q -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -sOutputFile=";
    private static final String PDFEXT = ".pdf";

    public PsToPdfConverter(String gsc) {
        if (gsc != null && gsc.length() > 0) {
            GSC = gsc;
        }
    }

    public String convert(String psFileName) {
        File psFile = new File(psFileName);
        String GSCOMMAND = GSC + GSCARGS;
        String pdfFileName = "";
        try {
            String filePath = psFile.getAbsolutePath();

            pdfFileName = filePath.substring(0, filePath.lastIndexOf(".")) + PDFEXT;
            String command = GSCOMMAND + pdfFileName + " \"" + psFileName+"\"";
            System.out.println(GSCOMMAND + pdfFileName + " \"" + psFileName+"\"");
            // execute the Ghostscript command
            // this will create the pdf file.  
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(PsToPdfConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PsToPdfConverter.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return pdfFileName;
    }

    public static void main(String[] args) {
        PsToPdfConverter pstopdf = new PsToPdfConverter("\"C:\\Program Files\\gs\\gs9.05\\bin\\gswin64c.exe\"");
        pstopdf.convert(args[0]);
    }
}