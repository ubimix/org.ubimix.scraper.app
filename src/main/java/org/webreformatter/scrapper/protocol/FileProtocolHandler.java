/**
 * 
 */
package org.webreformatter.scrapper.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;

/**
 * @author kotelnikov
 */
public class FileProtocolHandler implements IProtocolHandler {

    /**
     * 
     */
    public FileProtocolHandler() {
    }

    private HttpStatusCode copyFileResource(Path path, IWrfResource resource) {
        try {
            IContentAdapter contentAdapter = resource
                .getAdapter(IContentAdapter.class);
            FileInputStream input = new FileInputStream(path.toString());
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

    /**
     * @see org.webreformatter.scrapper.protocol.IProtocolHandler#handleRequest(org.webreformatter.commons.uri.Uri,
     *      java.lang.String, java.lang.String,
     *      org.webreformatter.resources.IWrfResource)
     */
    public HttpStatusCode handleRequest(
        Uri uri,
        String login,
        String password,
        IWrfResource resource) {
        File file = new File(uri.toString());
        HttpStatusCode result = HttpStatusCode.STATUS_404;
        if (file.exists()) {
            if (!file.isFile()) {
                result = HttpStatusCode.STATUS_404;
            } else {
                Path path = uri.getPath();
                copyFileResource(path, resource);
                result = HttpStatusCode.STATUS_200;
            }
        }
        return result;
    }
}
