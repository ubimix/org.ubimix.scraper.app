/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.webreformatter.scrapper.context.HtmlPropertiesExtractor;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.XmlWrapper.XmlContext;

/**
 * @author kotelnikov
 */
public class HtmlPropertiesExtractorTest extends TestCase {

    /**
     * @param name
     */
    public HtmlPropertiesExtractorTest(String name) {
        super(name);
    }

    protected void checkXml(XmlWrapper value, String control)
        throws XmlException,
        IOException {
        assertNotNull(value);
        XmlContext xmlContext = value.getXmlContext();
        String str = reparseXML(xmlContext, control);
        String test = reparseXML(xmlContext, value.toString(false));
        assertEquals(str, test);
    }

    protected String reparseXML(XmlContext xmlContext, String xml)
        throws XmlException,
        IOException {
        String control = xmlContext.readXML(xml).toString(false);
        return control;
    }

    public void test() throws Exception {
        testPropertiesExtraction(
            "<div xmlns='http://www.w3.org/1999/xhtml'>\n"
                + "<ul>\n"
                + "     <li>a: A</li>\n"
                + "     <li>b: B</li>\n"
                + "</ul>\n"
                + "</div>",
            "<div xmlns='http://www.w3.org/1999/xhtml'>\n\n</div>",
            "a",
            "A",
            "b",
            "B");
        testPropertiesExtraction(
            "<div xmlns='http://www.w3.org/1999/xhtml'>"
                + "before"
                + "<ul>\n"
                + "     <li>a: A</li>\n"
                + "     <li>b: B</li>\n"
                + "</ul>"
                + "after"
                + "</div>",
            "<div xmlns='http://www.w3.org/1999/xhtml'>beforeafter</div>",
            "a",
            "A",
            "b",
            "B");
    }

    public void testBig() throws XmlException, IOException {
        String xml = "<?xml version='1.0' encoding='utf-8'?>\n"
            + " <entry xmlns='http://www.w3.org/2005/Atom'>\n"
            + "   <title>My Guide</title>\n"
            + "   <link href='http://example.org/guideOne/'/>\n"
            + "   <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>\n"
            + "   <updated>2011-11-01T18:30:02Z</updated>\n"
            + "   <content>\n"
            + "     <div xmlns='http://www.w3.org/1999/xhtml'>\n"
            + "         <ul>\n"
            + "           <li>author: <a href='about/author.html'>John Smith</a></li>\n"
            + "           <li>seeAlso: <a href='http://en.wikipedia.org/wiki/Semantic_Web'>Semantic Web</a></li>\n"
            + "           <li>subtitle: A short description of this guide.</li>\n"
            + "         </ul>\n"
            + "         <h1>First Itinerary</h1>\n"
            + "         <ul>\n"
            + "             <li>a: A</li>\n"
            + "             <li>b: B</li>\n"
            + "             <li>info: <p>This is an <a href='foo.bar'>info</a> block.</p></li>\n"
            + "         </ul>\n"
            + "         <p>First paragraph</p>"
            + "         <ol>\n"
            + "             <li><a href='pointOne.html'>Point One</a></li>\n"
            + "             <li><a href='pointTwo.html'>Point Two</a>\n"
            + "                 <ul>\n"
            + "                     <li>instructions: Go ahead, turn left, come back</li>\n"
            + "                     <li>time: 5 min</li>\n"
            + "                 </ul>\n"
            + "             </li>\n"
            + "             <li><a href='pointThree.html'>Point Three</a></li>\n"
            + "         </ol>\n"
            + "         <p>Second paragraph</p>\n"
            + "     </div>\n"
            + "   </content>\n"
            + " </entry>";
        String control = "<?xml version='1.0' encoding='utf-8'?>\n"
            + " <entry xmlns='http://www.w3.org/2005/Atom'>\n"
            + "   <title>My Guide</title>\n"
            + "   <link href='http://example.org/guideOne/'/>\n"
            + "   <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>\n"
            + "   <updated>2011-11-01T18:30:02Z</updated>\n"
            + "   <content>\n"
            + "     <div xmlns='http://www.w3.org/1999/xhtml'>\n"
            + "         \n"
            + "         <h1>First Itinerary</h1>\n"
            + "         <ul>\n"
            + "             <li>a: A</li>\n"
            + "             <li>b: B</li>\n"
            + "             <li>info: <p>This is an <a href='foo.bar'>info</a> block.</p></li>\n"
            + "         </ul>\n"
            + "         <p>First paragraph</p>"
            + "         <ol>\n"
            + "             <li><a href='pointOne.html'>Point One</a></li>\n"
            + "             <li><a href='pointTwo.html'>Point Two</a>\n"
            + "                 <ul>\n"
            + "                     <li>instructions: Go ahead, turn left, come back</li>\n"
            + "                     <li>time: 5 min</li>\n"
            + "                 </ul>\n"
            + "             </li>\n"
            + "             <li><a href='pointThree.html'>Point Three</a></li>\n"
            + "         </ol>\n"
            + "         <p>Second paragraph</p>\n"
            + "     </div>\n"
            + "   </content>\n"
            + " </entry>";
        testPropertiesExtraction(
            xml,
            control,
            "author",
            "<a href=\"about/author.html\" xmlns=\"http://www.w3.org/1999/xhtml\">John Smith</a>",
            "seeAlso",
            "<a href=\"http://en.wikipedia.org/wiki/Semantic_Web\" xmlns=\"http://www.w3.org/1999/xhtml\">Semantic Web</a>",
            "subtitle",
            "A short description of this guide.");
    }

    private void testProperties(
        Map<String, Object> properties,
        Object... keyValuePairs) {
        assertNotNull(properties);
        assertEquals(keyValuePairs.length / 2, properties.size());
        for (int i = 0; i < keyValuePairs.length;) {
            Object key = keyValuePairs[i++];
            Object control = i < keyValuePairs.length
                ? keyValuePairs[i++]
                : null;
            Object test = properties.get(key);
            assertEquals(control, test);
        }
    }

    public void testPropertiesExtraction(
        String str,
        String control,
        Object... keyValuePairs) throws XmlException, IOException {
        XmlContext context = XmlContext.builder(
            "html",
            "http://www.w3.org/1999/xhtml").build();
        XmlWrapper xml = context.readXML(str);
        XmlWrapper controlXml = context.readXML(control);
        HtmlPropertiesExtractor extractor = new HtmlPropertiesExtractor();
        Map<String, Object> properties = extractor.extractProperties(xml);
        testProperties(properties, keyValuePairs);
        assertEquals(controlXml.toString(), xml.toString());
    }

}
