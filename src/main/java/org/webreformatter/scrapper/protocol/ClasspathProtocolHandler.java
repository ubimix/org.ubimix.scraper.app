package org.webreformatter.scrapper.protocol;

import java.io.IOException;
import java.io.InputStream;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;

public class ClasspathProtocolHandler implements IProtocolHandler {

    private HttpStatusCode copyClasspathResource(
        Path path,
        IWrfResource resource) {
        try {
            IContentAdapter contentAdapter = resource
                .getAdapter(IContentAdapter.class);
            path = path.getBuilder().makeAbsolutePath().build();
            InputStream input = getClass().getResourceAsStream(
                path.toString());
            try {
                contentAdapter.writeContent(input);
            } finally {
                input.close();
            }
            CachedResourceAdapter cacheAdapter = resource
                .getAdapter(CachedResourceAdapter.class);
            cacheAdapter.setLastModified(System.currentTimeMillis());
            return HttpStatusCode.STATUS_200;
        } catch (IOException e) {
            return HttpStatusCode.STATUS_505;
        }
    }

    public HttpStatusCode handleRequest(
        Uri uri,
        String login,
        String password,
        IWrfResource resource) {
        Path path = uri.getPath();
        return copyClasspathResource(path, resource);
    }
}