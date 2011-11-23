/**
 * 
 */
package org.webreformatter.scrapper.context;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.AccessManager.CredentialInfo;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

/**
 * @author kotelnikov
 */
public class CoreAdapter extends ApplicationContextAdapter {

    /**
     * @param context
     */
    public CoreAdapter(ApplicationContext context) {
        super(context);
    }

    public HttpStatusCode download(
        AccessManager accessManager,
        Uri url,
        IWrfResource resource) {
        IProtocolHandler protocolHandler = fApplicationContext
            .getProtocolHandler();
        CredentialInfo credentials = accessManager != null ? accessManager
            .getCredentials(url) : null;
        String login = null;
        String password = null;
        if (credentials != null) {
            login = credentials.getLogin();
            password = credentials.getPassword();
        }
        HttpStatusCode code = protocolHandler.handleRequest(
            url,
            login,
            password,
            resource);
        return code;
    }

    public IWrfResource download(
        AccessManager accessManager,
        Uri url,
        String prefix) {
        IWrfResource resource = getResource(url, prefix, true);
        HttpStatusCode status = download(accessManager, url, resource);
        IWrfResource result = null;
        if (!status.isError()) {
            result = resource;
        } else {
            // result.delete();
        }
        return result;
    }

    public IWrfResource getResource(Uri url, String resourceKey, boolean create) {
        IWrfRepository repository = fApplicationContext.getRepository();
        IWrfResourceProvider resourceProvider = repository.getResourceProvider(
            resourceKey,
            true);
        Path resourcePath = UriToPath.getPath(url);
        IWrfResource resource = resourceProvider.getResource(
            resourcePath,
            create);
        return resource;
    }

}
