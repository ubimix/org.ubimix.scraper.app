package org.webreformatter.pageset;

import java.io.IOException;

import junit.framework.TestCase;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;
import org.webreformatter.pageset.loaders.XmlUrlToPathMapperLoader;

public class XmlUrlToPathMapperLoaderTest extends TestCase {

    private XmlUrlToPathMapperLoader fConfigLoader = new XmlUrlToPathMapperLoader();

    /**
     * @param name
     */
    public XmlUrlToPathMapperLoaderTest(String name) {
        super(name);
    }

    private UrlToPathMapper readConfig(String xml, Uri configUrl)
        throws XmlException,
        IOException {
        XmlContext context = XmlContext.builder().build();
        XmlWrapper wrapper = context.readXML(xml);
        UrlToPathMapper mapper = fConfigLoader.configure(configUrl, wrapper);
        return mapper;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMultiSiteConfig() throws XmlException, IOException {
        String xml = ""
            + "<pageset>\n"
            + "    <site path=\"\" baseUrl=\"http://en.wikipedia.org/wiki/\">\n"
            + "        <url2path from=\"^(.*)$\" to=\"$1.html\" />\n"
            + "        <path2url from=\"^(.*)\\.html$\" to=\"$1\" />\n"
            + "        <path2url from=\"^(.*)$\" to=\"$1\" />\n"
            + "        <url key=\"xsl\" value=\"classpath:/xsl/transform-wikipedia.xsl\" />\n"
            + "        <url key=\"template\" value=\"classpath:/template/template.html\" />\n"
            + "    </site>\n"
            + "    <site path=\"travel\" baseUrl=\"http://wikitravel.org/en/\">\n"
            + "        <url2path from=\"^(.*)$\" to=\"$1.html\" />\n"
            + "        <path2url from=\"^(.*)\\.html$\" to=\"$1\" />\n"
            + "        <path2url from=\"^(.*)$\" to=\"$1\" />\n"
            + "        <url key=\"xsl\" value=\"classpath:/xsl/transform-wikipedia.xsl\" />\n"
            + "        <url key=\"template\" value=\"classpath:/template/template.html\" />\n"
            + "    </site>\n"
            + "</pageset>"
            + "";
        Uri configUrl = new Uri("classpath:./configurations/pageset.xml");
        UrlToPathMapper mapper = readConfig(xml, configUrl);

        Uri url = new Uri("http://en.wikipedia.org/wiki/France");
        Uri testPath = mapper.uriToPath(url);
        assertEquals("France.html", testPath.toString());
        Uri testUrl = mapper.pathToUri(testPath);
        assertEquals(url, testUrl);

        url = new Uri("http://wikitravel.org/en/France");
        testPath = mapper.uriToPath(url);
        assertEquals("travel/France.html", testPath.toString());
        testUrl = mapper.pathToUri(testPath);
        assertEquals(url, testUrl);

    }

    public void testOne() throws Exception {
        Uri configUrl = new Uri(
            "http://www.foo.bar/siteconfigs/MySiteConfig.xml");
        String xml = ""
            + "<pageset>\n"
            + "     <site path=\"abc\""
            + "             baseUrl=\"http://bar.foo.com:8080/JSPWiki/\" >\n"
            + ""
            + "        <url2path from=\"^Wiki.jsp$\" to=\"index.html\" />\n"
            + "        <url2path from=\"^.*page=AtomProcessing.*$\" to=\"index.html\" />\n"
            + "        <url2path from=\"^.*page=([^&amp;]*).*$\" to=\"$1.html\" />\n"
            // For URLs like /JSPWiki/PageName
            + "        <url2path from=\"^([\\w\\d]+)$\" to=\"$1.html\" />\n"
            + ""
            + "        <path2url from=\"^$\" to=\"Wiki.jsp?page=AtomProcessing\" />\n"
            + "        <path2url from=\"^index\\.html$\" to=\"Wiki.jsp?page=AtomProcessing\" />\n"
            + "        <path2url from=\"^(.*)\\.html$\" to=\"Wiki.jsp?page=$1\" />\n"
            + "     </site>"
            + "</pageset>"
            + "";
        UrlToPathMapper config = readConfig(xml, configUrl);
        Uri uri = new Uri(
            "http://bar.foo.com:8080/JSPWiki/Wiki.jsp?page=AtomProcessing");
        Uri path = config.uriToPath(uri);
        assertEquals(new Uri("abc/index.html"), path);
        Uri testUri = config.pathToUri(path);
        assertEquals(uri, testUri);

        path = new Uri("abc/");
        testUri = config.pathToUri(path);
        assertEquals(uri, testUri);

        uri = new Uri("http://bar.foo.com:8080/JSPWiki/Toto");
        path = config.uriToPath(uri);
        assertEquals(new Uri("abc/Toto.html"), path);
        testUri = config.pathToUri(path);
        assertEquals(new Uri(
            "http://bar.foo.com:8080/JSPWiki/Wiki.jsp?page=Toto"), testUri);
    }

}
