package org.webreformatter.scrapper.protocol;

import java.io.IOException;
import java.io.InputStream;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;

/**
 * This implementation of the {@link IProtocolHandler} loads the content from a
 * local repository (see {@link IWrfRepository}).
 * 
 * @author kotelnikov
 */
public class WrfRepositoryProtocolHandler implements IProtocolHandler {

    private IWrfResourceProvider fResourceProvider;

    public WrfRepositoryProtocolHandler(IWrfResourceProvider resourceProvider) {
        fResourceProvider = resourceProvider;
    }

    public HttpStatusCode handleRequest(
        Uri uri,
        String login,
        String password,
        IWrfResource target) {
        try {
            Path path = UriToPath.getPath(uri);
            HttpStatusCode status = HttpStatusCode.STATUS_404;

            IWrfResource source = fResourceProvider.getResource(path, true);
            IContentAdapter sourceContentAdapter = source
                .getAdapter(IContentAdapter.class);
            if (sourceContentAdapter.exists()) {
                InputStream input = sourceContentAdapter.getContentInput();
                try {
                    IContentAdapter outputContentAdapter = target
                        .getAdapter(IContentAdapter.class);
                    outputContentAdapter.writeContent(input);
                    status = HttpStatusCode.STATUS_200;
                } finally {
                    input.close();
                }
            }

            CachedResourceAdapter targetPropertyAdapter = target
                .getAdapter(CachedResourceAdapter.class);
            targetPropertyAdapter.copyPropertiesFrom(source);
            targetPropertyAdapter.setStatusCode(status.getStatusCode());
            targetPropertyAdapter.touch();

            return status;
        } catch (IOException e) {
            return HttpStatusCode.STATUS_505;
        }
    }
}