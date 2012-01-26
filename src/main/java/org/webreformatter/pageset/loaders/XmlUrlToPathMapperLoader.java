package org.webreformatter.pageset.loaders;

import java.util.List;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.pageset.UrlToPathMapper;
import org.webreformatter.pageset.UrlToPathMapper.SiteUrlMapper;

/**
 * This class is used to configure {@link CompositeSiteConfig} instances using
 * XML configurations.
 * 
 * @author kotelnikov
 */
public class XmlUrlToPathMapperLoader extends AbstractXmlLoader {

    private static final String XPATH_BASE_URL = "baseUrl";

    private static final String XPATH_FROM = "from";

    private static final String XPATH_PATH = "path";

    private static final String XPATH_PATH_TO_URL = "path2url";

    private static final String XPATH_SITE = "//site";

    private static final String XPATH_TO = "to";

    private static final String XPATH_URL_TO_PATH = "url2path";

    public XmlUrlToPathMapperLoader() {
    }

    /**
     * Creates and returns a new URL-to-path configuration object from the given
     * XML.
     * 
     * @param configUri the URL of the XML configuration file; it is used to
     *        resolve relative references in the configuration
     * @param xml the XML document containing configuration
     * @throws XmlException
     */
    public UrlToPathMapper configure(Uri configUrl, XmlWrapper xml)
        throws XmlException {
        UrlToPathMapper mapper = new UrlToPathMapper();
        configure(configUrl, xml, mapper);
        return mapper;
    }

    /**
     * Loads all site configurations from the given XML object and adds them to
     * the specified configuration object.
     * 
     * @param configUrl the URL of the XML configuration file; it is used to
     *        resolve relative references in the configuration
     * @param xml the XML document containing configuration
     * @param mapper the configuration object to initialize
     * @throws XmlException
     */
    public void configure(Uri configUrl, XmlWrapper xml, UrlToPathMapper mapper)
        throws XmlException {
        List<XmlWrapper> sites = xml.evalList(XPATH_SITE);
        if (sites != null) {
            for (XmlWrapper node : sites) {
                configureSite(configUrl, node, mapper);
            }
        }
    }

    /**
     * Loads a single site configuration from the given XML object and adds it
     * to the specified configuration object.
     * 
     * @param configUrl the URL of the XML configuration file; it is used to
     *        resolve relative references in the configuration
     * @param node the XML document node (element) containing configuration
     * @param mapper the configuration object to initialize
     * @throws XmlException
     */
    private void configureSite(
        Uri configUrl,
        XmlWrapper node,
        UrlToPathMapper mapper) throws XmlException {

        String str = getValue(node, XPATH_BASE_URL);
        Uri baseUri = getUrl(configUrl, str);

        String pathStr = getValue(node, XPATH_PATH);
        Uri basePath = !isEmpty(pathStr) ? new Uri(pathStr.trim()) : Uri.EMPTY;

        SiteUrlMapper base = mapper.addBase(baseUri, basePath);

        SiteUrlMapper.RegexpUrlTransformer pathToUri = base.getPathToUri();
        SiteUrlMapper.RegexpUrlTransformer uriToPath = base.getUriToPath();
        loadLinkConverter(node, XPATH_URL_TO_PATH, uriToPath);
        loadLinkConverter(node, XPATH_PATH_TO_URL, pathToUri);
    }

    /**
     * @param e the XML node containing regexp rules used to configure the
     *        transformer
     * @param name the name of tags containing regexp transformations
     * @param transformer the configuration object to define
     * @throws XmlException
     */
    private void loadLinkConverter(
        XmlWrapper e,
        String name,
        SiteUrlMapper.RegexpUrlTransformer transformer) throws XmlException {
        List<XmlWrapper> list = e.evalList(name);
        for (XmlWrapper url2path : list) {
            String from = getValue(url2path, XPATH_FROM);
            String to = getValue(url2path, XPATH_TO);
            transformer.addTranformation(from, to);
        }
    }

}
