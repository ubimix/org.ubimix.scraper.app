/**
 * 
 */
package org.webreformatter.scrapper.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.geo.TileInfo;
import org.webreformatter.commons.geo.TilesLoader;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.atom.AtomFeed;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.resources.impl.WrfResourceRepository;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.scrapper.protocol.AccessManager.CredentialInfo;
import org.webreformatter.scrapper.protocol.CompositeProtocolHandler;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.protocol.ProtocolHandlerUtils;
import org.webreformatter.scrapper.transformer.CompositeTransformer;
import org.webreformatter.scrapper.transformer.XslBasedDocumentTransformer;

/**
 * This class is the parent for all application-specific configurators.
 * 
 * @author kotelnikov
 */
public class ResourceLoader {

    /**
     * @author kotelnikov
     */
    public interface IUrlTransformer {

        IUrlTransformer EMPTY = new IUrlTransformer() {
            public Uri transform(Uri uri) {
                return uri;
            }
        };

        Uri transform(Uri uri);
    }

    /**
     * @author kotelnikov
     */
    public static class MapTilesLoaderListener extends TilesLoader.LoadListener {

        private final static Logger log = Logger
            .getLogger(MapTilesLoaderListener.class.getName());

        private ResourceLoader fLoader;

        private Uri fMapServerUrl;

        private String fPathPrefix;

        private Map<Path, IWrfResource> fResults;

        public MapTilesLoaderListener(
            String pathPrefix,
            ResourceLoader loader,
            Uri mapServerUrl) {
            this(
                pathPrefix,
                loader,
                mapServerUrl,
                new HashMap<Path, IWrfResource>());
        }

        public MapTilesLoaderListener(
            String pathPrefix,
            ResourceLoader loader,
            Uri mapServerUrl,
            Map<Path, IWrfResource> results) {
            fPathPrefix = pathPrefix;
            fResults = results;
            fLoader = loader;
            fMapServerUrl = mapServerUrl;
        }

        public Map<Path, IWrfResource> getMapTiles() {
            return fResults;
        }

        protected void handleError(String msg, Throwable t) {
            log.log(Level.SEVERE, msg, t);
        }

        @Override
        public void onTile(TileInfo tile) {
            String str = tile.getTilePath();
            Uri tileUri = fMapServerUrl
                .getBuilder()
                .appendFullPath(str)
                .build();
            IWrfResource resource = fLoader.getResource("maps", tileUri, null);
            try {
                fLoader.loadResource(tileUri, resource);
                Path path = new Path.Builder(fPathPrefix)
                    .appendPath(str)
                    .build();
                fResults.put(path, resource);
            } catch (IOException e) {
                handleError("Can not load a tile " + tile + ".", e);
            }
        }

    }

    private AccessManager fAccessManager = new AccessManager();

    private CompositeTransformer fDocumentTransformer = new CompositeTransformer();

    private boolean fNoDownload;

    private CompositeProtocolHandler fProtocolHandler = new CompositeProtocolHandler();

    private IWrfRepository fResourceRepository;

    private IUrlTransformer fUrlTransformer = IUrlTransformer.EMPTY;

    public ResourceLoader() {
        this("./data", false);
    }

    public ResourceLoader(IWrfRepository repository) {
        fResourceRepository = repository;
        ProtocolHandlerUtils.registerDefaultProtocols(fProtocolHandler);
    }

    public ResourceLoader(String repositoryPath, boolean reset) {
        this(WrfResourceRepository.newRepository(
            new File(repositoryPath),
            reset));
    }

    /**
     * This method is used to associate credentials with all resources starting
     * with the specified base URL.
     * 
     * @param baseUrl the basic URL associated with the specified credentials
     * @param login the login used to access resources
     * @param pwd the password associated with the specified login
     */
    public void addCredentials(String baseUrl, String login, String pwd) {
        Uri url = new Uri(baseUrl);
        fAccessManager.setCredentials(url, new CredentialInfo(login, pwd));
    }

    /**
     * Returns the URL transformer used to map "logical" resource URLs to real
     * URLs used to download resources. For example the resulting URLs could
     * have additional parameters, not defined in the original documents.
     * 
     * @return the URL transformer used to map "logical" resource URLs to real
     *         URLs used to download resources.
     */
    public IUrlTransformer getDownloadUrlTransformer() {
        return fUrlTransformer;
    }

    public IWrfResource getResource(String storeName, Uri url, String suffix) {
        IWrfResourceProvider store = fResourceRepository.getResourceProvider(
            storeName,
            true);
        Path path = UriToPath.getPath(url);
        Path.Builder builder = path.getBuilder();
        if (suffix != null) {
            builder.appendPath("$").appendPath(suffix);
        }
        Path targetResultPath = builder.build();
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;
    }

    public HttpStatusCode loadResource(Uri url, IWrfResource resource)
        throws IOException {
        boolean noDownload = noDownload();
        noDownload &= resource.getAdapter(IContentAdapter.class).exists();
        CachedResourceAdapter cacheAdapter = resource
            .getAdapter(CachedResourceAdapter.class);
        HttpStatusCode statusCode;
        if (noDownload) {
            int code = cacheAdapter.getStatusCode();
            statusCode = HttpStatusCode.getStatusCode(code);
        } else if (!cacheAdapter.isExpired()) {
            statusCode = HttpStatusCode.STATUS_304; /* NOT_MODIFIED */
        } else {
            CredentialInfo credentials = fAccessManager != null
                ? fAccessManager.getCredentials(url)
                : null;
            String login = null;
            String password = null;
            if (credentials != null) {
                login = credentials.getLogin();
                password = credentials.getPassword();
            }

            IUrlTransformer urlTransformer = getDownloadUrlTransformer();
            Uri downloadUrl = urlTransformer != null ? urlTransformer
                .transform(url) : url;
            if (downloadUrl != null) {
                statusCode = fProtocolHandler.handleRequest(
                    downloadUrl,
                    login,
                    password,
                    resource);
            } else {
                statusCode = HttpStatusCode.STATUS_404;
            }
            cacheAdapter.setStatusCode(statusCode.getStatusCode());
        }
        return statusCode;
    }

    public boolean noDownload() {
        return fNoDownload;
    }

    public void setDocumentTransformer(
        String urlBase,
        XslBasedDocumentTransformer transformer) {
        Uri url = new Uri(urlBase);
        setDocumentTransformer(url, transformer);
    }

    public void setDocumentTransformer(
        Uri url,
        XslBasedDocumentTransformer transformer) {
        fDocumentTransformer.addTransformer(url, transformer);
    }

    public void setDownloadUrlTransformer(IUrlTransformer urlTransformer) {
        fUrlTransformer = urlTransformer;
    }

    public void setNoDownload(boolean noDownload) {
        fNoDownload = noDownload;
    }

    public void setXslTransformation(String urlBase, String xslPath)
        throws IOException,
        XmlException {
        File xslFile = new File(xslPath).getAbsoluteFile();
        if (!xslFile.exists()) {
            throw new IllegalArgumentException(
                "An XSL transformation for the '"
                    + urlBase
                    + "' resources was not found. File '"
                    + xslFile
                    + "' ('"
                    + xslPath
                    + "') dos not exist.");
        }
        Uri xslUri = new Uri(xslFile.toURI() + "");
        setXslTransformation(urlBase, xslUri);
    }

    public void setXslTransformation(String urlBase, Uri xslUri)
        throws IOException,
        XmlException {
        IWrfResource xslResource = getResource("tmp", xslUri, null);
        HttpStatusCode status = loadResource(xslUri, xslResource);
        if (!status.isOkOrNotModified()) {
            throw new IllegalArgumentException(
                "An XSL transformation for the '"
                    + urlBase
                    + "' resources could not be loaded. Status: "
                    + status);
        }
        XmlAdapter xslAdapter = xslResource.getAdapter(XmlAdapter.class);
        XmlWrapper xsl = xslAdapter.getWrapper();
        XslBasedDocumentTransformer transformer = new XslBasedDocumentTransformer(
            xsl);
        setDocumentTransformer(urlBase, transformer);
    }

    public void toXml(
        Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource xmlResource) throws IOException, XmlException {
        CachedResourceAdapter rawCache = rawResource
            .getAdapter(CachedResourceAdapter.class);
        CachedResourceAdapter xmlCache = xmlResource
            .getAdapter(CachedResourceAdapter.class);
        long rawResourceModificationTime = rawCache.getLastModified();
        long atomModificationTime = xmlCache.getLastModified();
        boolean needUpdates = rawResourceModificationTime < 0
            || atomModificationTime < 0
            || atomModificationTime < rawResourceModificationTime;
        if (needUpdates) {
            XmlAdapter xmlAdapter = xmlResource.getAdapter(XmlAdapter.class);
            HTMLAdapter htmlAdapter = rawResource.getAdapter(HTMLAdapter.class);
            XmlWrapper doc = htmlAdapter.getWrapper();
            xmlAdapter.setDocument(doc);
            xmlCache.copyPropertiesFrom(rawCache);
        }
    }

    public void transformToAtom(
        Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource atomResource) throws IOException, XmlException {
        CachedResourceAdapter rawCache = rawResource
            .getAdapter(CachedResourceAdapter.class);
        CachedResourceAdapter atomCache = atomResource
            .getAdapter(CachedResourceAdapter.class);
        long rawResourceModificationTime = rawCache.getLastModified();
        long atomModificationTime = atomCache.getLastModified();
        boolean needUpdates = rawResourceModificationTime < 0
            || atomModificationTime < 0
            || atomModificationTime < rawResourceModificationTime;
        if (needUpdates) {
            HTMLAdapter htmlAdapter = rawResource.getAdapter(HTMLAdapter.class);
            XmlWrapper doc = htmlAdapter.getWrapper();
            AtomFeed atomDoc = fDocumentTransformer.transformDocument(
                resourceUri,
                doc);
            XmlAdapter atomAdapter = atomResource.getAdapter(XmlAdapter.class);
            atomAdapter.setDocument(atomDoc);
            atomCache.copyPropertiesFrom(rawCache);
        }
    }
}
