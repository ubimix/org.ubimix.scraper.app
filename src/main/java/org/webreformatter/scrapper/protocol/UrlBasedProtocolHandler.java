package org.webreformatter.scrapper.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;

/**
 * @author kotelnikov
 */
public class UrlBasedProtocolHandler implements IProtocolHandler {

    private final Logger fLogger = Logger.getLogger(getClass().getName());

    protected void handleError(String msg, Throwable e) {
        fLogger.log(Level.WARNING, msg, e);
    }

    public HttpStatusCode handleRequest(
        Uri uri,
        String login,
        String password,
        IWrfResource resource) {
        try {
            URL url = new URL(uri.toString());
            try {
                IContentAdapter contentAdapter = resource
                    .getAdapter(IContentAdapter.class);
                URLConnection connection = url.openConnection();
                connection.connect();
                try {
                    InputStream input = connection.getInputStream();
                    try {
                        contentAdapter.writeContent(input);
                    } finally {
                        input.close();
                    }
                    CachedResourceAdapter cacheAdapter = resource
                        .getAdapter(CachedResourceAdapter.class);
                    cacheAdapter
                        .setLastModified(System.currentTimeMillis());

                    Map<String, List<String>> fields = connection
                        .getHeaderFields();
                    IPropertyAdapter propertiesAdapter = resource
                        .getAdapter(IPropertyAdapter.class);
                    for (Map.Entry<String, List<String>> entry : fields
                        .entrySet()) {
                        String key = entry.getKey();
                        List<String> values = entry.getValue();
                        String value = values != null && !values.isEmpty()
                            ? values.get(0)
                            : "";
                        propertiesAdapter.setProperty(key, value);
                    }
                } finally {
                }
                return HttpStatusCode.STATUS_200;
            } catch (IOException e) {
                return HttpStatusCode.STATUS_505;
            }

        } catch (IOException e) {
            handleError("Can not download resource", e);
            return HttpStatusCode.STATUS_505;
        }
    }
}