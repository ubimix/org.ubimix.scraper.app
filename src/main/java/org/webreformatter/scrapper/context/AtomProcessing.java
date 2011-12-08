/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;

import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.normalizer.IDocumentNormalizer;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class AtomProcessing extends ApplicationContextAdapter {

    public static IAdapterFactory getAdapterFactory(
        final IDocumentNormalizer documentNormalizer) {
        return new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> T getAdapter(Object instance, Class<T> type) {
                if (type != AtomProcessing.class
                    || !(instance instanceof ApplicationContext)) {
                    return null;
                }
                return (T) new AtomProcessing(
                    (ApplicationContext) instance,
                    documentNormalizer);
            }
        };
    }

    private IWrfResourceProvider fHtmlStore;

    private IWrfResourceProvider fNormalizedStore;

    private IDocumentNormalizer fNormalizer;

    private IWrfResourceProvider fRawDataStore;

    /**
     * @param documentNormalizer
     * @param args
     * @throws Exception
     */
    public AtomProcessing(
        ApplicationContext applicationContext,
        IDocumentNormalizer documentNormalizer) {
        super(applicationContext);
        fRawDataStore = getStore("download");
        fHtmlStore = getStore("html");
        fNormalizedStore = getStore("html-normalized");
        fNormalizer = documentNormalizer;
    }

    public AtomFeed getResourceAsAtomFeed(RuntimeContext context)
        throws XmlException,
        IOException {
        Uri url = context.getUrl();
        Path path = UriToPath.getPath(url);
        IWrfResource cleanResource = fNormalizedStore.getResource(path, true);
        XmlAdapter xmlAdapter = cleanResource.getAdapter(XmlAdapter.class);
        boolean ok = false;
        if (!context.isExpired(cleanResource)) {
            ok = true;
        } else {
            XmlWrapper doc = getXHTMLResource(context);
            if (doc != null) {
                AtomFeed newDoc = fNormalizer.getNormalizedContent(
                    context,
                    url,
                    doc);
                xmlAdapter.setDocument(newDoc);
                touch(cleanResource);
                ok = true;
            }
        }
        AtomFeed feed = null;
        if (ok) {
            feed = xmlAdapter.getWrapperCopy(AtomFeed.class);
        }
        return feed;
    }

    protected IWrfResourceProvider getStore(String name) {
        IWrfRepository repository = fApplicationContext.getRepository();
        return repository.getResourceProvider(name, true);
    }

    // FIXME: move to the AutoContentNoramlizer class

    public XmlWrapper getXHTMLResource(RuntimeContext context)
        throws IOException,
        XmlException {
        Uri url = context.getUrl();
        Path path = UriToPath.getPath(url);
        IWrfResource htmlResource = fHtmlStore.getResource(path, true);
        XmlAdapter xmlAdapter = htmlResource.getAdapter(XmlAdapter.class);
        boolean exists = false;
        if (context.isExpired(htmlResource)) {
            IWrfResource rawResource = fRawDataStore.getResource(path, true);
            exists = !context.isExpired(rawResource);
            if (!exists) {
                CoreAdapter adapter = fApplicationContext
                    .getAdapter(CoreAdapter.class);
                AccessManager accessManager = context.getAccessManager();
                HttpStatusCode code = adapter.download(
                    accessManager,
                    url,
                    rawResource);
                exists = code.isOk() || HttpStatusCode.STATUS_304.equals(code) /* NOT_MODIFIED */;
                touch(rawResource);
                onResourceReloaded(url);
            }
            if (exists) {
                MimeTypeAdapter mimeTypeAdapter = rawResource
                    .getAdapter(MimeTypeAdapter.class);
                String mimeType = mimeTypeAdapter.getMimeType();
                if (mimeType.startsWith("text/html")) {
                    HTMLAdapter htmlAdapter = rawResource
                        .getAdapter(HTMLAdapter.class);
                    XmlWrapper doc = htmlAdapter.getWrapper();
                    exists = doc != null;
                    if (exists) {
                        xmlAdapter.setDocument(doc);
                        CachedResourceAdapter adapter = htmlResource
                            .getAdapter(CachedResourceAdapter.class);
                        adapter.updateMetadataFrom(rawResource);
                    }
                }
            } else {
                htmlResource.remove();
            }
        }
        XmlWrapper result = exists ? xmlAdapter.getWrapperCopy() : null;
        return result;
    }

    private void onResourceReloaded(Uri url) {
        removeResource(fHtmlStore, url);
        removeResource(fNormalizedStore, url);
    }

    private void removeResource(IWrfResourceProvider store, Uri uri) {
        Path path = UriToPath.getPath(uri);
        IWrfResource resource = store.getResource(path, false);
        if (resource != null) {
            resource.remove();
        }
    }

    protected void touch(IWrfResource resource) throws IOException {
        CachedResourceAdapter adapter = resource
            .getAdapter(CachedResourceAdapter.class);
        adapter.touch();
    }

}
