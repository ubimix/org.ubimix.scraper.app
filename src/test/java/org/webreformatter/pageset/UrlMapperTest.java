package org.webreformatter.pageset;

import java.io.IOException;

import junit.framework.TestCase;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.loaders.XmlUrlMapperLoader;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.XmlWrapper.XmlContext;

public class UrlMapperTest extends TestCase {

    private XmlUrlMapperLoader fConfigLoader = new XmlUrlMapperLoader();

    /**
     * @param name
     */
    public UrlMapperTest(String name) {
        super(name);
    }

    private IUrlMapper readConfig(String xml, Uri configUrl)
        throws XmlException,
        IOException {
        XmlContext context = XmlContext.builder().build();
        XmlWrapper wrapper = context.readXML(xml);
        UrlMapper config = new UrlMapper();
        fConfigLoader.configure(configUrl, wrapper, config);
        return config;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testOne() throws Exception {
        Uri configUrl = new Uri(
            "http://www.foo.bar/siteconfigs/MySiteConfig.xml");
        String xml = ""
            + "<pageset>\n"
            + "     <site baseUrl=\"http://bar.foo.com:8080/JSPWiki/\" >\n"
            + "        <url key=\"xsl\" value=\"../xsl/transform-jspwiki.xsl\" />\n"
            + "        <url key=\"test\" value=\"http://www.foo.bar/\" />\n"
            + "     </site>"
            + "</pageset>"
            + "";
        IUrlMapper config = readConfig(xml, configUrl);
        testUri(
            config,
            configUrl,
            "xsl",
            "http://bar.foo.com:8080/JSPWiki/FooBar",
            "../xsl/transform-jspwiki.xsl");
        testUri(
            config,
            configUrl,
            "test",
            "http://bar.foo.com:8080/JSPWiki/FooBar",
            "http://www.foo.bar/");
    }

    private void testUri(
        IUrlMapper config,
        Uri configUrl,
        String type,
        String uri,
        String control) {
        Uri testXslUrl = config.getUrl(type, new Uri(uri));
        Uri xslUrl = configUrl.getResolved(control);
        assertEquals(xslUrl, testXslUrl);
    }
}
