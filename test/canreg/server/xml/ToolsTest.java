package canreg.server.xml;

import canreg.common.DatabaseDictionaryListElement;
import canreg.common.DatabaseGroupsListElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link canreg.server.xml.Tools} class.
 * Verify DOM element text-content retrieval and list-element parsing using
 * dynamically-constructed in-memory documents.
 */
public class ToolsTest {

    public ToolsTest() {
    }

    // ------------------------------------------------------------------
    // Helper: build a simple tree Document
    // ------------------------------------------------------------------

    private Document createTestDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

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

    // ------------------------------------------------------------------
    // Helper: build a minimal CanReg-style Document with groups and dicts
    // ------------------------------------------------------------------

    private Document createCanRegDoc(String namespace) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("canreg");
        doc.appendChild(root);

        // -- Groups --
        Element groups = doc.createElement(namespace + "groups");
        root.appendChild(groups);
        groups.appendChild(createGroup(doc, namespace, "0", "Patient Info", "0"));
        groups.appendChild(createGroup(doc, namespace, "1", "Tumour Info",  "1"));

        // -- Dictionaries --
        Element dicts = doc.createElement(namespace + "dictionaries");
        root.appendChild(dicts);
        dicts.appendChild(createDictionary(doc, namespace, "0", "Sex", "Latin", "Simple", "0", "0", "1", "10"));

        return doc;
    }

    private Element createGroup(Document doc, String ns, String id, String name, String pos) {
        Element g = doc.createElement(ns + "group");
        g.appendChild(text(doc, ns + "group_id",  id));
        g.appendChild(text(doc, ns + "name",      name));
        g.appendChild(text(doc, ns + "group_pos", pos));
        return g;
    }

    private Element createDictionary(Document doc, String ns, String id, String name,
                                     String font, String type, String codeLen, String catDescLen,
                                     String fullCodeLen, String fullDescLen) {
        Element d = doc.createElement(ns + "dictionary");
        d.appendChild(text(doc, ns + "dictionary_id",                    id));
        d.appendChild(text(doc, ns + "name",                             name));
        d.appendChild(text(doc, ns + "font",                             font));
        d.appendChild(text(doc, ns + "type",                             type));
        d.appendChild(text(doc, ns + "code_length",                      codeLen));
        d.appendChild(text(doc, ns + "category_description_length",      catDescLen));
        d.appendChild(text(doc, ns + "full_dictionary_code_length",      fullCodeLen));
        d.appendChild(text(doc, ns + "full_dictionary_description_length", fullDescLen));
        return d;
    }

    private Element text(Document doc, String tag, String value) {
        Element e = doc.createElement(tag);
        e.setTextContent(value);
        return e;
    }

    // ------------------------------------------------------------------
    // getTextContent – existing tests (preserved)
    // ------------------------------------------------------------------

    @Test
    public void testGetTextContentValid() throws Exception {
        System.out.println("testGetTextContentValid");
        Document doc = createTestDocument();

        String[] trail1 = {"root"};
        assertEquals("hello-world", Tools.getTextContent(trail1, doc).trim());

        String[] trail2 = {"root", "parent", "child"};
        assertEquals("hello-world", Tools.getTextContent(trail2, doc));
    }

    @Test
    public void testGetTextContentEmptyTrail() throws Exception {
        System.out.println("testGetTextContentEmptyTrail");
        Document doc = createTestDocument();

        String[] trailEmpty = {};
        assertNull(Tools.getTextContent(trailEmpty, doc));
    }

    @Test(expected = NullPointerException.class)
    public void testGetTextContentNonExistent() throws Exception {
        System.out.println("testGetTextContentNonExistent");
        Document doc = createTestDocument();

        String[] trailInvalid = {"root", "missing-tag"};
        Tools.getTextContent(trailInvalid, doc);
    }

    // ------------------------------------------------------------------
    // getGroupsListElements (canreg.common.Tools – same namespace)
    // ------------------------------------------------------------------

    @Test
    public void testGetGroupsListElements_CountAndNames() throws Exception {
        String ns = "ns3:";
        Document doc = createCanRegDoc(ns);

        DatabaseGroupsListElement[] groups = canreg.common.Tools.getGroupsListElements(doc, ns);
        assertNotNull(groups);
        assertEquals(2, groups.length);

        // Groups are sorted by position
        assertEquals("Patient Info", groups[0].getGroupName());
        assertEquals("Tumour Info",  groups[1].getGroupName());
    }

    @Test
    public void testGetGroupsListElements_IDs() throws Exception {
        String ns = "ns3:";
        Document doc = createCanRegDoc(ns);

        DatabaseGroupsListElement[] groups = canreg.common.Tools.getGroupsListElements(doc, ns);
        assertEquals(0, groups[0].getGroupIndex());
        assertEquals(1, groups[1].getGroupIndex());
    }

    @Test
    public void testGetGroupsListElements_NullDoc_ReturnsEmpty() {
        DatabaseGroupsListElement[] groups = canreg.common.Tools.getGroupsListElements(null, "ns3:");
        assertNotNull(groups);
        assertEquals(0, groups.length);
    }

    // ------------------------------------------------------------------
    // getDictionaryListElements (canreg.common.Tools)
    // ------------------------------------------------------------------

    @Test
    public void testGetDictionaryListElements_Count() throws Exception {
        String ns = "ns3:";
        Document doc = createCanRegDoc(ns);

        DatabaseDictionaryListElement[] dicts = canreg.common.Tools.getDictionaryListElements(doc, ns);
        assertNotNull(dicts);
        assertEquals(1, dicts.length);
    }

    @Test
    public void testGetDictionaryListElements_FieldValues() throws Exception {
        String ns = "ns3:";
        Document doc = createCanRegDoc(ns);

        DatabaseDictionaryListElement[] dicts = canreg.common.Tools.getDictionaryListElements(doc, ns);
        DatabaseDictionaryListElement sex = dicts[0];

        assertEquals("Sex",    sex.getName());
        assertEquals("Latin",  sex.getFont());
        assertEquals("Simple", sex.getType());
        assertEquals(0,        sex.getDictionaryID());
        assertEquals(1,        sex.getFullDictionaryCodeLength());
        assertEquals(10,       sex.getFullDictionaryDescriptionLength());
    }

    @Test
    public void testGetDictionaryListElements_NullDoc_ReturnsEmpty() {
        DatabaseDictionaryListElement[] dicts = canreg.common.Tools.getDictionaryListElements(null, "ns3:");
        assertNotNull(dicts);
        assertEquals(0, dicts.length);
    }
}
