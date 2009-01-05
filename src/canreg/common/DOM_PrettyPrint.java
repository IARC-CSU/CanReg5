package canreg.common;

import java.io.OutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * http://pastebin.com/f7fadd984
 * @author ddaniels
 */

public class DOM_PrettyPrint {

    public static void serialize(Document doc, OutputStream out) throws Exception {

        TransformerFactory  tfactory = TransformerFactory.newInstance();
        Transformer serializer;

        try {
            serializer = tfactory.newTransformer();

            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            serializer.transform(new DOMSource(doc), new StreamResult(out));

        } catch (TransformerException e) {

            // this is fatal, just dump the stack and throw a runtime exception
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
