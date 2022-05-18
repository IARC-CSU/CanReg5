package canreg.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PsToPdfConverter {
    
    private static final Logger LOGGER = Logger.getLogger(PsToPdfConverter.class.getName());
    private String GSC = "";
    // private String GSCARGS = " -q -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -sOutputFile=";
    private final String[] GSCARGS = new String[] {"-q", "-dBATCH", "-dNOPAUSE", "-sDEVICE=pdfwrite"};
    
    public static final String PDFEXT = ".pdf";

    public PsToPdfConverter(String gsc) {
        if (gsc != null && gsc.length() > 0) {
            GSC = gsc;
        }
    }

    public String convert(String psFileName) {
        File psFile = new File(psFileName);
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(GSC);
        commandList.addAll(Arrays.asList(GSCARGS));
        String pdfFileName = "";
        try {
            String filePath = psFile.getAbsolutePath();

            pdfFileName = filePath.substring(0, filePath.lastIndexOf(".")) + PDFEXT;
            
            commandList.add("-sOutputFile=" + pdfFileName);
            commandList.add(psFileName);           
            
            System.out.println(commandList);
            // execute the Ghostscript command
            // this will create the pdf file.  
            Process p = Runtime.getRuntime().exec(commandList.toArray(new String[]{}));
            p.waitFor();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } 
        return pdfFileName;
    }

    public static void main(String[] args) {
        PsToPdfConverter pstopdf = new PsToPdfConverter("C:\\Program Files\\gs\\gs9.10\\bin\\gswin64c.exe");
        pstopdf.convert(args[0]);
    }
}