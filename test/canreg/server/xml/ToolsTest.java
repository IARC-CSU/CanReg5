package canreg.server.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for canreg.server.xml.Tools class.
 * Verify DOM elements text-content retrieval using dynamically-constructed documents.
 */
public class ToolsTest {

    public ToolsTest() {
    }

    /**
     * Helper to build an in-memory test XML Document.
     */
    private Document createTestDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Structure:
        // <root>
        //   <parent>
        //     <child>hello-world</child>
        //   </parent>
        // </root>
        Element root = doc.createElement("root");
        doc.appendChild(root);
        
        Element parent = doc.createElement("parent");
        root.appendChild(parent);
        
        Element child = doc.createElement("child");
        child.setTextContent("hello-world");
        parent.appendChild(child);
        
        return doc;
    }

    /**
     * Test of getTextContent method, of class Tools.
     */
    @Test
    public void testGetTextContentValid() throws Exception {
        System.out.println("testGetTextContentValid");
        Document doc = createTestDocument();
        
        // Single level
        String[] trail1 = {"root"};
        // Note: getTextContent on a parent node concatenates descendant texts in standard DOM,
        // so "root" and "parent" will both return "hello-world"
        assertEquals("hello-world", Tools.getTextContent(trail1, doc).trim());
        
        // Nested trail
        String[] trail2 = {"root", "parent", "child"};
        assertEquals("hello-world", Tools.getTextContent(trail2, doc));
    }

    /**
     * Test of getTextContent method with empty trails.
     */
    @Test
    public void testGetTextContentEmptyTrail() throws Exception {
        System.out.println("testGetTextContentEmptyTrail");
        Document doc = createTestDocument();
        
        String[] trailEmpty = {};
        assertNull(Tools.getTextContent(trailEmpty, doc));
    }

    /**
     * Test of getTextContent method with non-existent tag expecting NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testGetTextContentNonExistent() throws Exception {
        System.out.println("testGetTextContentNonExistent");
        Document doc = createTestDocument();
        
        String[] trailInvalid = {"root", "missing-tag"};
        Tools.getTextContent(trailInvalid, doc);
    }
}
