/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.pageset.UrlMapper;
import org.webreformatter.pageset.UrlToPathMapper;
import org.webreformatter.pageset.loaders.XmlAccessManagerLoader;
import org.webreformatter.pageset.loaders.XmlUrlMapperLoader;
import org.webreformatter.pageset.loaders.XmlUrlToPathMapperLoader;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.protocol.AccessManager.CredentialInfo;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

/**
 * @author kotelnikov
 */
public class PageSetConfigLoader extends ApplicationContextAdapter {

    public PageSetConfigLoader(ApplicationContext context) {
        super(context);
    }

    public void configureAccessManager(
        AccessManager configAccessManager,
        Uri accessConfigUri,
        PageSetConfig.Builder config) throws IOException, XmlException {
        // Configure access manager
        AccessManager accessManager = new AccessManager();
        config.setAccessManager(accessManager);
        XmlAccessManagerLoader accessManagerLoader = new XmlAccessManagerLoader();
        XmlWrapper accessConfigXml = getConfigXml(
            configAccessManager,
            accessConfigUri);
        if (accessConfigXml != null) {
            accessManagerLoader.configure(
                accessConfigUri,
                accessManager,
                accessConfigXml);
            config.setAccessManager(accessManager);
        }
    }

    public void configurePageSets(
        AccessManager configAccessManager,
        Uri siteConfigUri,
        PageSetConfig.Builder config) throws IOException, XmlException {
        XmlWrapper siteConfigXml = getConfigXml(
            configAccessManager,
            siteConfigUri);
        if (siteConfigXml != null) {
            configurePageSets(siteConfigUri, config, siteConfigXml);
            configureUriMappings(siteConfigUri, config, siteConfigXml);
        }
    }

    protected void configurePageSets(
        Uri siteConfigUri,
        PageSetConfig.Builder config,
        XmlWrapper siteConfigXml) throws XmlException {
        XmlUrlToPathMapperLoader loader = new XmlUrlToPathMapperLoader();
        final UrlToPathMapper urlToPathMapper = loader.configure(
            siteConfigUri,
            siteConfigXml);
        config.setUrlToPathMapper(urlToPathMapper);
        IUrlTransformer downloadUrlTransformer = new IUrlTransformer() {
            public Uri transform(Uri uri) {
                Uri path = urlToPathMapper.uriToPath(uri);
                if (path == null) {
                    return null;
                }
                Uri result = urlToPathMapper.pathToUri(path);
                return result;
            }
        };
        IUrlTransformer localizeUrlTransformer = new IUrlTransformer() {
            public Uri transform(Uri uri) {
                String fragment = uri.getFragment();
                Uri baseUri = uri;
                if (fragment != null) {
                    baseUri = uri.getBuilder().setFragment(null).build();
                }
                Uri result = urlToPathMapper.uriToPath(baseUri);
                if (result == null) {
                    result = uri;
                } else {
                    if (fragment != null) {
                        result = result
                            .getBuilder()
                            .setFragment(fragment)
                            .build();
                    }
                }
                return result;
            }
        };
        config
            .setDownloadUrlTransformer(downloadUrlTransformer)
            .setLocalizeUrlTransformer(localizeUrlTransformer);
    }

    public void configureUriMappings(
        AccessManager configAccessManager,
        Uri siteConfigUri,
        PageSetConfig.Builder config) throws XmlException, IOException {
        XmlWrapper siteConfigXml = getConfigXml(
            configAccessManager,
            siteConfigUri);
        if (siteConfigXml != null) {
            configureUriMappings(siteConfigUri, config, siteConfigXml);
        }
    }

    protected void configureUriMappings(
        Uri siteConfigUri,
        PageSetConfig.Builder config,
        XmlWrapper siteConfigXml) throws XmlException {
        XmlUrlMapperLoader loader = new XmlUrlMapperLoader();
        UrlMapper mapper = new UrlMapper();
        config.setUriMapper(mapper);
        loader.configure(siteConfigUri, siteConfigXml, mapper);
    }

    private void configureUrlTransformers(
        AccessManager configAccessManager,
        PageSetConfig.Builder config) {
        // config.setDownloadUrlTransformer(IUrlTransformer.EMPTY);
        // config.setLocalizeUrlTransformer(IUrlTransformer.EMPTY);
    }

    protected AccessManager getAccessManager(
        AccessManager configAccessManager,
        Uri configUri) throws Exception {
        AccessManager accessManager = new AccessManager();
        XmlWrapper xml = getConfigXml(null, configUri);
        if (xml != null) {
            XmlAccessManagerLoader accessManagerLoader = new XmlAccessManagerLoader();
            accessManagerLoader.configure(configUri, accessManager, xml);
        }
        return accessManager;
    }

    protected XmlWrapper getConfigXml(
        AccessManager configAccessManager,
        Uri configUri) throws IOException, XmlException {
        String resourceKey = "config";

        IWrfRepository repository = fApplicationContext.getRepository();
        IWrfResourceProvider resourceProvider = repository.getResourceProvider(
            resourceKey,
            true);
        Path resourcePath = UriToPath.getPath(configUri);
        IWrfResource configResource = resourceProvider.getResource(
            resourcePath,
            true);
        IProtocolHandler protocolHandler = fApplicationContext
            .getProtocolHandler();
        CredentialInfo credentials = configAccessManager != null
            ? configAccessManager.getCredentials(configUri)
            : null;
        String login = null;
        String password = null;
        if (credentials != null) {
            login = credentials.getLogin();
            password = credentials.getPassword();
        }
        HttpStatusCode code = protocolHandler.handleRequest(
            configUri,
            login,
            password,
            configResource);
        XmlWrapper xml = null;
        if (!code.isError()) {
            XmlAdapter xmlAdapter = configResource.getAdapter(XmlAdapter.class);
            xml = xmlAdapter.getWrapper();
        }
        return xml;
    }

    public PageSetConfig loadPageSetConfig(
        AccessManager configAccessManager,
        Uri siteConfigUri,
        Uri accessConfigUri) throws XmlException, IOException {
        PageSetConfig.Builder config = PageSetConfig.builder();
        configureAccessManager(configAccessManager, accessConfigUri, config);
        configurePageSets(configAccessManager, siteConfigUri, config);
        configureUriMappings(configAccessManager, siteConfigUri, config);
        configureUrlTransformers(configAccessManager, config);
        PageSetConfig result = config.build();
        return result;
    }

    public PageSetConfig loadPageSetConfig(
        Uri siteConfigUri,
        Uri accessConfigUri) throws XmlException, IOException {
        return loadPageSetConfig(null, siteConfigUri, accessConfigUri);
    }

}
