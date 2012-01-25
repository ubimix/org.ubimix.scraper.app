/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;

import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.transformer.IDocumentTransformer;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class AtomProcessing extends RuntimeContextAdapter {

    private static final String RESOURCE_HTML = "html";

    private static final String RESOURCE_HTML_NORMALIZED = "html-normalized";

    public static IAdapterFactory getAdapterFactory(
            final IDocumentTransformer documentTransformer) {
        return new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> T getAdapter(Object instance, Class<T> type) {
                if (type != AtomProcessing.class
                        || !(instance instanceof RuntimeContext)) {
                    return null;
                }
                return (T) new AtomProcessing((RuntimeContext) instance,
                        documentTransformer);
            }
        };
    }

    public static void register(CompositeAdapterFactory adapterFactory,
            IDocumentTransformer documentTransformer) {
        adapterFactory.registerAdapterFactory(
                AtomProcessing.getAdapterFactory(documentTransformer),
                AtomProcessing.class);
    }

    private IDocumentTransformer fTransformer;

    /**
     * @param documentTransformer
     * @param args
     * @throws Exception
     */
    public AtomProcessing(RuntimeContext runtimeContext,
            IDocumentTransformer documentTransformer) {
        super(runtimeContext);
        fTransformer = documentTransformer;
    }

    public XmlAdapter getResourceAsAtom() throws IOException, XmlException {
        IWrfResource cleanResource = fRuntimeContext
                .getResource(RESOURCE_HTML_NORMALIZED);
        XmlAdapter xmlAdapter = cleanResource.getAdapter(XmlAdapter.class);
        boolean ok = false;
        if (!fRuntimeContext.isExpired(cleanResource)) {
            XmlWrapper xml = xmlAdapter.getWrapper();
            if (xml != null) {
                ok = true;
            } else {
                ok = false;
            }
        }
        if (!ok) {
            XmlWrapper doc = getXHTMLResource();
            if (doc != null) {
                Uri url = fRuntimeContext.getUrl();
                AtomFeed newDoc = fTransformer.transformDocument(url, doc);
                if (newDoc != null) {
                    xmlAdapter.setDocument(newDoc);
                    touch(cleanResource);
                }
                ok = true;
            }
        }
        XmlAdapter result = ok ? xmlAdapter : null;
        return result;
    }

    public AtomFeed getResourceAsAtomFeed() throws XmlException, IOException {
        XmlAdapter xmlAdapter = getResourceAsAtom();
        AtomFeed feed = null;
        if (xmlAdapter != null) {
            feed = xmlAdapter.getWrapperCopy(AtomFeed.class);
        }
        return feed;
    }

    public XmlWrapper getXHTMLResource() throws IOException, XmlException {
        Uri url = fRuntimeContext.getUrl();
        IWrfResource htmlResource = fRuntimeContext.getResource(RESOURCE_HTML);
        XmlAdapter xmlAdapter = htmlResource.getAdapter(XmlAdapter.class);
        boolean exists = false;
        if (!fRuntimeContext.isExpired(htmlResource)) {
            exists = true;
        } else {
            IWrfResource rawResource = fRuntimeContext
                    .getResource(RESOURCE_DOWNLOAD);
            exists = !fRuntimeContext.isExpired(rawResource);
            if (!exists) {
                DownloadAdapter downloadAdapter = fRuntimeContext
                        .getAdapter(DownloadAdapter.class);
                rawResource = downloadAdapter.loadResource();
                if (downloadAdapter.isOK()) {
                    exists = true;
                    onResourceReloaded(url);
                }
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
        XmlWrapper result = exists ? xmlAdapter.getWrapper() : null;
        return result;
    }

    private void onResourceReloaded(Uri url) {
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
