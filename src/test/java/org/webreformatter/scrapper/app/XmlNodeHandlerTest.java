package org.webreformatter.scrapper.app;

import java.io.IOException;
import java.util.List;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;
import org.webreformatter.commons.xml.atom.AtomEntry;
import org.webreformatter.commons.xml.atom.AtomFeed;
import org.webreformatter.resources.AbstractResourceTest;

public class XmlNodeHandlerTest extends AbstractResourceTest {

    public XmlNodeHandlerTest(String name) {
        super(name);
    }

    // * Load interesting items => by XPath
    // * For each found item => get references
    // * Resolve each reference to an absolute one
    // * Load the resulting document
    // * Apply a specific template for the loaded document and save it in a
    // local file.
    // * Format list of references
    // *
    // * Get the image from the resulting document
    // *
    private void handle(XmlWrapper wrapper) throws XmlException {
        String listXPath = "//ul[@class='abc']";
        List<XmlWrapper> lists = wrapper.evalList(listXPath);
        for (XmlWrapper list : lists) {
            List<XmlWrapper> items = list.evalList("li");
            for (XmlWrapper item : items) {
                System.out.println(item);
            }
        }
        wrapper.remove(listXPath);
        System.out.println(wrapper);
    }

    public void test() throws Exception {
        String xml = ""
            + "<div>"
            + "Text before. "
            + "<ul class='abc'>"
            + "<li><a href='first.html'>First</a></li>"
            + "<li><a href='second.html'>Second</a></li>"
            + "<li><a href='third.html'>Third</a></li>"
            + "</ul>"
            + "Text after. "
            + "</div>";
        XmlContext context = XmlContext.build();
        XmlWrapper wrapper = context.readXML(xml);
        handle(wrapper);
    }

    public void testEntryCreation() throws XmlException, IOException {
        AtomFeed feed = new AtomFeed();
        XmlContext context = feed.getXmlContext();
        AtomEntry entry = feed.addEntry();
        XmlWrapper content = context.readXML(""
            + "<div xmlns='http://www.w3.org/1999/xhtml'>"
            + "<h1>Hello</h1>"
            + "<p>world</p>"
            + "</div>"
            + "");
        entry.setContent(content);
        assertEquals(""
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\">"
            + "<atom:content>"
            + "<h1 xmlns=\"http://www.w3.org/1999/xhtml\">Hello</h1>"
            + "<p xmlns=\"http://www.w3.org/1999/xhtml\">world</p>"
            + "</atom:content>"
            + "</atom:entry>"
            + "", entry.toString(true, false));
    }

    public void testFeed() throws Exception {
        String xml = ""
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<feed xmlns='http://www.w3.org/2005/Atom'>\n"
            + "    <title>Example Feed</title>\n"
            + "    <subtitle>A subtitle.</subtitle>\n"
            + "    <link href='http://example.org/feed/' rel='self' />\n"
            + "    <link href='http://example.org/' />\n"
            + "    <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>\n"
            + "    <updated>2003-12-13T18:30:02Z</updated>\n"
            + "    <author>\n"
            + "        <name>John Doe</name>\n"
            + "        <email>johndoe@example.com</email>\n"
            + "    </author>\n"
            + "    <entry>\n"
            + "        <title>Atom-Powered Robots Run Amok</title>\n"
            + "        <link href='http://example.org/2003/12/13/atom03' />\n"
            + "        <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>\n"
            + "        <updated>2003-12-13T18:30:02Z</updated>\n"
            + "        <summary>Some text.</summary>\n"
            + "        <author>\n"
            + "             <name>J.Doe</name>\n"
            + "             <email>johndoe@example.com</email>\n"
            + "         </author>\n"
            + "         <contributor>\n"
            + "             <name>Foo Bar</name>\n"
            + "             <email>foobar@example.com</email>\n"
            + "         </contributor>\n"
            + "         <category term='webdev' "
            + "             label='Web Development'"
            + "             scheme='http://www.example.com/tags'/>\n"
            + "         <category term='standards' "
            + "             label='Web Standards' "
            + "             scheme='http://www.example.com/tags'/>\n"
            + "         <category term='test' />\n"
            + "    </entry>\n"
            + " \n"
            + "</feed>\n";
        XmlContext context = XmlContext.build();
        XmlWrapper wrapper = context.readXML(xml);
        // applyAction(wrapper);
    }
}
