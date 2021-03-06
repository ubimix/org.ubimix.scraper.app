/**
 * 
 */
package org.ubimix.scraper.core;

import java.io.IOException;

import org.ubimix.commons.uri.Uri;
import org.ubimix.resources.IContentAdapter;
import org.ubimix.resources.IWrfResource;
import org.ubimix.resources.adapters.cache.CachedResourceAdapter;
import org.ubimix.scraper.protocol.AccessManager;
import org.ubimix.scraper.protocol.AccessManager.CredentialInfo;
import org.ubimix.scraper.protocol.CompositeProtocolHandler;
import org.ubimix.scraper.protocol.HttpStatusCode;
import org.ubimix.scraper.protocol.ProtocolHandlerUtils;

/**
 * @author kotelnikov
 */
public class DownloadAdapter extends AppContextAdapter {

    /**
     * @author kotelnikov
     */
    public interface IUrlTransformer {

        IUrlTransformer EMPTY = new IUrlTransformer() {
            @Override
            public Uri transform(Uri uri) {
                return uri;
            }
        };

        Uri transform(Uri uri);
    }

    private AccessManager fAccessManager = new AccessManager();

    private boolean fDownloadExistingResources;

    private CompositeProtocolHandler fProtocolHandler = new CompositeProtocolHandler();

    public DownloadAdapter(AppContext appContext) {
        super(appContext);
        ProtocolHandlerUtils.registerDefaultProtocols(fProtocolHandler);
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
        addCredentials(url, login, pwd);
    }

    /**
     * This method is used to associate credentials with all resources starting
     * with the specified base URL.
     * 
     * @param baseUrl the basic URL associated with the specified credentials
     * @param login the login used to access resources
     * @param pwd the password associated with the specified login
     */
    public void addCredentials(Uri baseUrl, String login, String pwd) {
        fAccessManager.setCredentials(baseUrl, new CredentialInfo(login, pwd));
    }

    public boolean downloadExistingResources() {
        return fDownloadExistingResources;
    }

    public void downloadExistingResources(boolean download) {
        fDownloadExistingResources = download;
    }

    public HttpStatusCode loadResource(Uri url, IWrfResource resource)
        throws IOException {
        HttpStatusCode statusCode = HttpStatusCode.STATUS_404;
        if (url != null) {
            boolean download = downloadExistingResources()
                || !resource.getAdapter(IContentAdapter.class).exists();
            CachedResourceAdapter cacheAdapter = resource
                .getAdapter(CachedResourceAdapter.class);
            if (!download) {
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
                statusCode = fProtocolHandler.handleRequest(
                    url,
                    login,
                    password,
                    resource);
                cacheAdapter.setStatusCode(statusCode.getStatusCode());
            }
        }
        return statusCode;
    }

}
