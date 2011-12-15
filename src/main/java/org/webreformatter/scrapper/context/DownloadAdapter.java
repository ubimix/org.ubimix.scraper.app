/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;

import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.scrapper.protocol.AccessManager.CredentialInfo;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

/**
 * @author kotelnikov
 */
public class DownloadAdapter extends RuntimeContextAdapter {

    protected final static String KEY_CLEARCACHE = "clearcache";

    protected final static String KEY_RESOURCE_SUFFIX = "resourceSuffix";

    public static IAdapterFactory getAdapterFactory() {
        return new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> T getAdapter(Object instance, Class<T> type) {
                if (type != DownloadAdapter.class
                    || !(instance instanceof RuntimeContext)) {
                    return null;
                }
                return (T) new DownloadAdapter((RuntimeContext) instance);
            }
        };
    }

    public static void register(CompositeAdapterFactory adapters) {
        adapters.registerAdapterFactory(
            DownloadAdapter.getAdapterFactory(),
            DownloadAdapter.class);
    }

    private HttpStatusCode fStatusCode;

    public DownloadAdapter(RuntimeContext context) {
        super(context);
    }

    public String getMimeType() throws IOException {
        String result = null;
        IWrfResource resource = loadResource();
        if (resource != null) {
            MimeTypeAdapter adapter = resource
                .getAdapter(MimeTypeAdapter.class);
            result = adapter.getMimeType();
        }
        return result;
    }

    public HttpStatusCode getStatusCode() {
        return fStatusCode;
    }

    public boolean isOK() {
        if (fStatusCode == null) {
            return false;
        }
        return fStatusCode.isOk()
            || HttpStatusCode.STATUS_304.equals(fStatusCode); /* NOT_MODIFIED */
    }

    public IWrfResource loadResource() throws IOException {
        Uri url = fRuntimeContext.getUrl();
        String resourceNameSuffix = fRuntimeContext
            .getParameter(KEY_RESOURCE_SUFFIX);
        IWrfResource resource = fRuntimeContext.getResource(
            RESOURCE_DOWNLOAD,
            resourceNameSuffix);
        boolean clearCache = fRuntimeContext
            .getParameter(KEY_CLEARCACHE, false);
        boolean ok = !clearCache && !fRuntimeContext.isExpired(resource);
        if (ok) {
            fStatusCode = HttpStatusCode.STATUS_304; /* NOT_MODIFIED */
        } else {
            AccessManager accessManager = fRuntimeContext.getAccessManager();
            ApplicationContext applicationContext = fRuntimeContext
                .getApplicationContext();
            IProtocolHandler protocolHandler = applicationContext
                .getProtocolHandler();
            CredentialInfo credentials = accessManager != null ? accessManager
                .getCredentials(url) : null;
            String login = null;
            String password = null;
            if (credentials != null) {
                login = credentials.getLogin();
                password = credentials.getPassword();
            }

            IUrlTransformer urlTransformer = fRuntimeContext
                .getDownloadUrlTransformer();
            Uri downloadUrl = urlTransformer != null ? urlTransformer
                .transform(url) : url;
            if (downloadUrl != null) {
                fStatusCode = protocolHandler.handleRequest(
                    downloadUrl,
                    login,
                    password,
                    resource);
                ok = isOK();
            } else {
                fStatusCode = HttpStatusCode.STATUS_404;
            }
            if (ok) {
                fRuntimeContext.touch(resource);
            }
        }
        return resource;
    }

}
