package org.webreformatter.pageset.loaders;

import java.util.List;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.UrlMapper;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

/**
 * This class is used to configure {@link UrlMapper} instances using XML
 * configurations.
 * 
 * <pre>
 * &lt;config>
 *      &lt;site baseUrl="http://en.wikipedia.org/wiki/">
 *          &lt;url key="xsl" value="../xsl/transform-wikipedia.xsl" />
 *          &lt;url key="template" value="../template/template.html" />
 *      &lt;/site>
 * &lt;/config>
 * </pre>
 * 
 * @author kotelnikov
 */
public class XmlUrlMapperLoader extends AbstractXmlLoader {

    private static final String XPATH_BASE_URL = "baseUrl";

    private static final String XPATH_KEY = "key";

    private static final String XPATH_SITE = "//site";

    private static final String XPATH_URL = "url";

    private static final String XPATH_VALUE = "value";

    public XmlUrlMapperLoader() {
    }

    /**
     * Loads URL mappings from the given XML node and adds them to the specified
     * configuration object.
     * 
     * @param configUri the URL of the XML configuration file; it is used to
     *        resolve relative references in the configuration
     * @param xml the XML document containing configuration
     * @param mapper the configuration object to initialize
     * @throws XmlException
     */
    public void configure(Uri configUri, XmlWrapper xml, UrlMapper mapper)
        throws XmlException {
        List<XmlWrapper> sites = xml.evalList(XPATH_SITE);
        if (sites != null) {
            for (XmlWrapper node : sites) {
                String str = getValue(node, XPATH_BASE_URL);
                Uri baseUri = getUrl(configUri, str);
                loadUrls(mapper, baseUri, configUri, node);
            }
        }
    }

    /**
     * Loads URL configurations for the specified XML .
     * 
     * @param xml the XML document containing configuration
     * @param mapper the configuration object to initialize
     * @throws XmlException
     */
    public void configure(XmlWrapper xml, UrlMapper mapper) throws XmlException {
        configure(Uri.EMPTY, xml, mapper);
    }

    private void loadUrls(
        UrlMapper mapper,
        Uri baseUri,
        Uri configUri,
        XmlWrapper node) throws XmlException {
        List<XmlWrapper> list = node.evalList(XPATH_URL);
        for (XmlWrapper entry : list) {
            String key = getValue(entry, XPATH_KEY);
            if (!isEmpty(key)) {
                String str = getValue(entry, XPATH_VALUE);
                Uri uri = getUrl(configUri, str);
                mapper.setUrl(key, baseUri, uri);
            }
        }
    }

}
