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

public class DescriptionReader {

    public static int extractInteger(String word) {
        int n = 0;
        String temp = new String();
        // skip what's before
        char[] ca = word.toCharArray();
        while ((n < word.length()) && (ca[n] < '0' || ca[n] > '9')) {
            n++;
        }
        // extract the integer
        while ((n < word.length()) && (ca[n] >= '0' && ca[n] <= '9')) {
            temp = temp + ca[n];
            n++;
        }
        //System.out.println("Extracting: " + temp);
        return Integer.parseInt(temp);
    }

    public static String readLine(FileReader file) {
        String str = new String();
        int c;
        try {
            c = file.read();
            while (c != -1 && c != '\n') {
                str = str + c;
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while reading a word.");
        }
        return str;
    }

    public static String readWord(FileReader file) {
        String str = new String();
        int c = removeWhites(file);
        try {
            while (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                if (c == '\"') {
                    //System.out.println("Reading quotes");
                    c = file.read();
                    while (c != '\"') {
                        str = str + (char) c;
                        c = file.read();
                        // If we find an EOF-mark we terminate and return EOF - that means we need a enter after the last line of data
                        if (c == -1) {
                            str = "EOF";
                            break;
                        }
                    }
                } else {
                    str = str + (char) c;
                }
                // If we find an EOF-mark we terminate and return EOF - that means we need a enter after the last line of data
                if (c == -1) {
                    str = "EOF";
                    break;
                }
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while reading a word.");
        }
        //System.out.println(str);
        return str;
    }

    public static int removeWhites(FileReader file) {
        int c = -1;
        try {
            c = file.read();
            while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                c = file.read();
            }
        } catch (Exception e) {
            System.out.println("File Error -  while removing whites.");
        }
        return c;
    }

}
