package org.webreformatter.scrapper.context;

import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.images.ImageAdapter;
import org.webreformatter.resources.adapters.images.ImageResizeStrategy;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.atom.AtomFeed;

public class ContentLoader {

    protected final static String KEY_CLEARCACHE = "clearcache";

    protected final static String KEY_RESOURCE_SUFFIX = "resourceSuffix";

    private static final String RESOURCE_DOWNLOAD = "download";

    private static final String RESOURCE_IMAGES = "images";

    private Map<Uri, AtomFeed> fCache = new HashMap<Uri, AtomFeed>();

    private RuntimeContext fContext;

    private Set<Uri> fMissedDocuments = new HashSet<Uri>();

    public ContentLoader(RuntimeContext context) {
        fContext = context;
    }

    protected Uri getExternalUrl(Uri docUri) {
        IUrlTransformer urlTransformer = fContext.getDownloadUrlTransformer();
        Uri externalUrl = docUri;
        if (urlTransformer != null) {
            externalUrl = urlTransformer.transform(docUri);
        }
        return externalUrl;
    }

    public Uri getLocalPath(Uri pageUrl) {
        Uri result = pageUrl;
        IUrlTransformer transformer = fContext.getLocalizeUrlTransformer();
        if (transformer != null) {
            result = transformer.transform(pageUrl);
        }
        return result;
    }

    public IWrfResource getResizedImage(
        Uri uri,
        final int maxWidth,
        final int maxHeight) throws IOException {
        IWrfResource resizedImage = getResizedImageContainer(
            uri,
            maxWidth,
            maxHeight);
        resizeImage(uri, resizedImage, new ImageResizeStrategy() {
            @Override
            public Point getImageSize(Point originalImageSize) {
                return notMoreThan(originalImageSize, maxWidth, maxHeight);
            }
        });
        return resizedImage;
    }

    public IWrfResource getResizedImageContainer(
        Uri uri,
        int maxWidth,
        int maxHeight) {
        String suffix = maxWidth + "x" + maxHeight;
        IWrfResource resizedImage = fContext.getResource(
            RESOURCE_IMAGES,
            uri,
            suffix);
        return resizedImage;
    }

    public RuntimeContext getRuntimeContext() {
        return fContext;
    }

    public AtomFeed loadDocument(Uri docUri) throws XmlException, IOException {
        AtomFeed feed = fCache.get(docUri);
        if (feed == null && !fMissedDocuments.contains(docUri)) {
            Uri externalUrl = getExternalUrl(docUri);
            if (externalUrl != null) {
                RuntimeContext runtimeContext = RuntimeContext
                    .builder(fContext)
                    .setUrl(externalUrl)
                    .build();
                IWrfResource resource = loadResource(runtimeContext);
                if (resource != null) {
                    ApplicationContext appContext = runtimeContext
                        .getApplicationContext();
                    AtomProcessing atomAdapter = appContext
                        .getAdapter(AtomProcessing.class);
                    feed = atomAdapter.getResourceAsAtomFeed(runtimeContext);
                }
            }
            if (feed != null) {
                fCache.put(docUri, feed);
            } else {
                fMissedDocuments.add(docUri);
            }
        }
        return feed;
    }

    private IWrfResource loadResource(RuntimeContext runtimeContext)
        throws IOException {
        Uri url = runtimeContext.getUrl();
        String resourceNameSuffix = runtimeContext
            .getParameter(KEY_RESOURCE_SUFFIX);
        IWrfResource resource = runtimeContext.getResource(
            RESOURCE_DOWNLOAD,
            resourceNameSuffix);
        boolean clearCache = runtimeContext.getParameter(KEY_CLEARCACHE, false);
        boolean ok = !clearCache && !runtimeContext.isExpired(resource);
        if (!ok) {
            ApplicationContext applicationContext = runtimeContext
                .getApplicationContext();
            CoreAdapter coreAdapter = applicationContext
                .getAdapter(CoreAdapter.class);
            AccessManager accessManager = runtimeContext.getAccessManager();
            HttpStatusCode code = coreAdapter.download(
                accessManager,
                url,
                resource);
            ok = code.isOk() || HttpStatusCode.STATUS_304.equals(code) /* NOT_MODIFIED */;
            if (ok) {
                runtimeContext.touch(resource);
            }
        }
        return ok ? resource : null;
    }

    public IWrfResource loadResource(Uri docUri) throws IOException {
        Uri externalUrl = getExternalUrl(docUri);
        IWrfResource result = null;
        if (externalUrl != null) {
            RuntimeContext runtimeContext = RuntimeContext
                .builder(fContext)
                .setUrl(externalUrl)
                .build();
            result = loadResource(runtimeContext);
        }
        return result;
    }

    public boolean resizeImage(
        Uri uri,
        IWrfResource resizedImage,
        ImageResizeStrategy resizeStrategy) throws IOException {
        Uri externalUrl = getExternalUrl(uri);
        RuntimeContext runtimeContext = RuntimeContext
            .builder(fContext)
            .setUrl(externalUrl)
            .build();
        IWrfResource resource = loadResource(runtimeContext);
        if (resource == null) {
            return false;
        }
        ImageAdapter imageAdapter = resource.getAdapter(ImageAdapter.class);
        imageAdapter.resizeImage(resizedImage, resizeStrategy);
        return true;
    }

}