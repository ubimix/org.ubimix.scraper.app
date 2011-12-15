package org.webreformatter.pageset.loaders;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.pageset.UrlToPathMapper;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

/**
 * This class is used to configure {@link CompositeSiteConfig} instances using
 * XML configurations.
 * 
 * @author kotelnikov
 */
public class XmlPageSetConfigLoader extends AbstractXmlLoader {

    public XmlPageSetConfigLoader() {
    }

    /**
     * @param prefix
     * @param configUri
     * @param xml
     * @return
     * @throws XmlException
     */
    public void configureAccess(
        PageSetConfig.Builder config,
        Uri configUri,
        XmlWrapper xml) throws XmlException {
        AccessManager accessManager = new AccessManager();
        XmlAccessManagerLoader accessManagerLoader = new XmlAccessManagerLoader();
        accessManagerLoader.configure(configUri, accessManager, xml);
        config.setAccessManager(accessManager);
    }

    public void configureSites(
        PageSetConfig.Builder config,
        Uri configUrl,
        XmlWrapper xml) throws XmlException {
        XmlUrlToPathMapperLoader loader = new XmlUrlToPathMapperLoader();
        final UrlToPathMapper urlToPathMapper = loader
            .configure(configUrl, xml);
        config.setUrlToPathMapper(urlToPathMapper);

        IUrlTransformer downloadUrlTransformer = new IUrlTransformer() {
            public Uri transform(Uri uri) {
                return uri;
            }
        };
        IUrlTransformer localizeUrlTransformer = new IUrlTransformer() {
            public Uri transform(Uri uri) {
                Uri result = urlToPathMapper.uriToPath(uri);
                if (result == null) {
                    result = uri;
                }
                return result;
            }
        };
        config
            .setDownloadUrlTransformer(downloadUrlTransformer)
            .setLocalizeUrlTransformer(localizeUrlTransformer);
    }

}
